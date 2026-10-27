package com.wanbaohe.textcard.data.project

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.AtomicFile
import android.util.Log
import android.webkit.MimeTypeMap
import com.wanbaohe.textcard.domain.StoredTextCardProject
import com.wanbaohe.textcard.domain.TextCardProjectStore
import com.wanbaohe.textcard.domain.TextCardProjectSummary
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.TextCardDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
internal class AndroidTextCardProjectStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : TextCardProjectStore {

    private val mutex = Mutex()
    private val mutableProjects = MutableStateFlow<List<TextCardProjectSummary>>(emptyList())
    override val projects: StateFlow<List<TextCardProjectSummary>> = mutableProjects.asStateFlow()

    override suspend fun refresh() = onDisk { directories, cancellation ->
        publishProjects(directories, cancellation)
    }

    override suspend fun save(
        id: String,
        name: String,
        document: TextCardDocument,
        pageCount: Int,
        thumbnail: Bitmap?,
    ): TextCardDocument = onDisk { directories, cancellation ->
        checkProjectId(id)
        if (pageCount < 0) throw IOException("Page count must not be negative")
        val file = projectFile(id, directories)
        val previous = if (file.exists() || File(file.path + ".bak").exists()) {
            readProject(id, directories, cancellation)
        } else null
        val durable = importDocumentAssets(document, directories, cancellation)
        val thumbnailPath = thumbnail?.let { storeThumbnail(it, directories, cancellation) }
            ?: previous?.summary?.thumbnailPath
        val project = StoredTextCardProject(
            summary = TextCardProjectSummary(
                id = id,
                name = name,
                updatedAt = System.currentTimeMillis(),
                thumbnailPath = thumbnailPath,
                pageCount = pageCount,
            ),
            document = durable,
        )
        val bytes = TextCardProjectCodec.encode(project).toByteArray(Charsets.UTF_8)
        if (bytes.size > MaxProjectBytes) throw IOException("Project JSON exceeds the size limit")
        cancellation.ensureActive()
        atomicWrite(file, bytes, cancellation)
        publishProjects(directories, cancellation)
        durable
    }

    override suspend fun load(id: String): StoredTextCardProject = onDisk { directories, cancellation ->
        readProject(id, directories, cancellation)
    }

    override suspend fun rename(id: String, name: String) = onDisk { directories, cancellation ->
        val current = readProject(id, directories, cancellation)
        val renamed = current.copy(
            summary = current.summary.copy(name = name, updatedAt = System.currentTimeMillis())
        )
        val bytes = TextCardProjectCodec.encode(renamed).toByteArray(Charsets.UTF_8)
        if (bytes.size > MaxProjectBytes) throw IOException("Project JSON exceeds the size limit")
        cancellation.ensureActive()
        atomicWrite(projectFile(id, directories), bytes, cancellation)
        publishProjects(directories, cancellation)
    }

    override suspend fun delete(id: String) = onDisk { directories, cancellation ->
        checkProjectId(id)
        val file = projectFile(id, directories)
        cancellation.ensureActive()
        AtomicFile(file).delete()
        if (file.exists() || File(file.path + ".bak").exists() || File(file.path + ".new").exists()) {
            throw IOException("Could not delete project $id")
        }
        syncDirectory(directories.projects)
        publishProjects(directories, cancellation)
    }

    private suspend fun <T> onDisk(
        action: (Directories, CoroutineContext) -> T,
    ): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            coroutineContext.ensureActive()
            action(directories(), coroutineContext)
        }
    }

    private fun importDocumentAssets(
        document: TextCardDocument,
        directories: Directories,
        cancellation: CoroutineContext,
    ): TextCardDocument {
        cancellation.ensureActive()
        val background = document.background
        if (background !is BackgroundSpec.Image) return document
        return document.copy(
            background = background.copy(uri = importAsset(background.uri, directories, cancellation))
        )
    }

    private fun importAsset(
        source: String,
        directories: Directories,
        cancellation: CoroutineContext,
    ): String {
        if (source.isBlank() || '\u0000' in source) throw IOException("Invalid asset source")
        cancellation.ensureActive()
        if (source.startsWith('/')) return importFile(File(source), directories, cancellation)
        val uri = Uri.parse(source)
        return when (uri.scheme?.lowercase()) {
            "file" -> {
                if (!uri.authority.isNullOrEmpty() && uri.authority != "localhost") {
                    throw IOException("Unsupported file URI authority")
                }
                val path = uri.path ?: throw IOException("File URI has no path")
                if (!File(path).isAbsolute) throw IOException("File URI must be absolute")
                importFile(File(path), directories, cancellation)
            }
            "content" -> {
                if (uri.authority.isNullOrBlank()) throw IOException("Content URI has no authority")
                val extension = contentExtension(uri)
                val input = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("Could not open content asset")
                input.use {
                    writeAsset(extension, directories, cancellation) { output ->
                        copyStream(it, output, cancellation)
                    }
                }
            }
            else -> throw IOException("Only absolute files, file:// and content:// assets are supported")
        }
    }

    private fun importFile(
        source: File,
        directories: Directories,
        cancellation: CoroutineContext,
    ): String {
        val file = source.canonicalFile
        if (file.parentFile == directories.assets) {
            return checkedManagedPath(file.path, directories)
        }
        if (!file.isFile || !file.canRead()) throw IOException("Asset file is missing or unreadable")
        return FileInputStream(file).use { input ->
            writeAsset(safeExtension(file.name), directories, cancellation) { output ->
                copyStream(input, output, cancellation)
            }
        }
    }

    private fun storeThumbnail(
        bitmap: Bitmap,
        directories: Directories,
        cancellation: CoroutineContext,
    ): String? {
        var sourceCopy: Bitmap? = null
        var scaledCopy: Bitmap? = null
        return try {
            if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return null
            val source = bitmap.encodable() ?: return null
            if (source !== bitmap) sourceCopy = source
            val longest = maxOf(source.width, source.height)
            val scaled = if (longest > ThumbnailSide) {
                val scale = ThumbnailSide.toFloat() / longest
                Bitmap.createScaledBitmap(
                    source,
                    (source.width * scale).roundToInt().coerceAtLeast(1),
                    (source.height * scale).roundToInt().coerceAtLeast(1),
                    true,
                ).also { if (it !== source) scaledCopy = it }
            } else source
            writeAsset(
                extension = "png",
                directories = directories,
                cancellation = cancellation,
                interruptible = false,
            ) { output ->
                if (!scaled.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    cancellation.ensureActive()
                    throw IOException("Could not encode project thumbnail")
                }
                cancellation.ensureActive()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Throwable) {
            Log.w(LogTag, "Could not store project thumbnail", failure)
            null
        } finally {
            scaledCopy?.recycle()
            sourceCopy?.recycle()
        }
    }

    private fun Bitmap.encodable(): Bitmap? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            config == Bitmap.Config.HARDWARE -> copy(Bitmap.Config.ARGB_8888, false)
        config == Bitmap.Config.ALPHA_8 -> copy(Bitmap.Config.ARGB_8888, false)
        else -> this
    }

    private fun writeAsset(
        extension: String,
        directories: Directories,
        cancellation: CoroutineContext,
        interruptible: Boolean = true,
        write: (OutputStream) -> Unit,
    ): String {
        val pending = File(directories.assets, ".pending-${UUID.randomUUID()}.part")
        if (!pending.createNewFile()) throw IOException("Could not create asset staging file")
        var failure: Throwable? = null
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileOutputStream(pending).use { fileOutput ->
                val output = DigestOutputStream(
                    if (interruptible) CancellableOutput(fileOutput, cancellation) else fileOutput,
                    digest,
                )
                write(output)
                output.flush()
                cancellation.ensureActive()
                fileOutput.fd.sync()
            }
            if (pending.length() == 0L) throw IOException("Cannot persist an empty asset")
            val digestHex = digest.digest().hex()
            val destination = File(directories.assets, "$digestHex.$extension")
            cancellation.ensureActive()
            if (destination.exists()) {
                if (!destination.isFile || destination.length() != pending.length() ||
                    checksum(destination, cancellation) != digestHex
                ) {
                    throw IOException("Existing managed asset is corrupt")
                }
            } else if (!pending.renameTo(destination)) {
                throw IOException("Could not publish managed asset")
            } else {
                syncDirectory(directories.assets)
            }
            cancellation.ensureActive()
            return checkedManagedPath(destination.path, directories)
        } catch (exception: Throwable) {
            failure = exception
            throw exception
        } finally {
            if (pending.exists() && !pending.delete()) {
                val cleanupFailure = IOException("Could not remove asset staging file")
                if (failure == null) throw cleanupFailure
                failure.addSuppressed(cleanupFailure)
            }
        }
    }

    private fun contentExtension(uri: Uri): String {
        val displayName = context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && !cursor.isNull(index)) cursor.getString(index) else null
            } else null
        }
        val named = displayName?.let(::safeExtension)?.takeUnless { it == "asset" }
        if (named != null) return named
        val mimeExtension = context.contentResolver.getType(uri)?.let {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
        }
        return mimeExtension?.lowercase()?.takeIf { it.matches(ExtensionPattern) } ?: "asset"
    }

    private fun safeExtension(name: String): String = name.substringAfterLast('.', "")
        .lowercase().takeIf { it.matches(ExtensionPattern) } ?: "asset"

    private fun publishProjects(directories: Directories, cancellation: CoroutineContext) {
        val files = directories.projects.listFiles()
            ?: throw IOException("Could not list projects")
        val ids = files.mapNotNull { file ->
            when {
                file.name.endsWith(".json") -> file.name.removeSuffix(".json")
                file.name.endsWith(".json.bak") -> file.name.removeSuffix(".json.bak")
                else -> null
            }
        }.toSet()
        val summaries = ids.mapNotNull { id ->
            cancellation.ensureActive()
            try {
                readProject(id, directories, cancellation).summary
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Log.w(LogTag, "Skipping unreadable text card project $id", failure)
                null
            }
        }.sortedWith(compareByDescending<TextCardProjectSummary> { it.updatedAt }.thenBy { it.id })
        cancellation.ensureActive()
        mutableProjects.value = summaries
    }

    private fun readProject(
        id: String,
        directories: Directories,
        cancellation: CoroutineContext,
    ): StoredTextCardProject {
        checkProjectId(id)
        cancellation.ensureActive()
        val atomic = AtomicFile(projectFile(id, directories))
        val bytes = atomic.openRead().use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(CopyBufferSize)
            var total = 0
            while (true) {
                cancellation.ensureActive()
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > MaxProjectBytes) throw IOException("Project JSON exceeds the size limit")
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
        val source = try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString()
        } catch (exception: CharacterCodingException) {
            throw TextCardProjectFormatException("Project is not valid UTF-8", exception)
        }
        val project = TextCardProjectCodec.decode(source)
        if (project.summary.id != id) {
            throw TextCardProjectFormatException("Project ID does not match its file")
        }
        (project.document.background as? BackgroundSpec.Image)?.let {
            checkedManagedPath(it.uri, directories)
        }
        project.summary.thumbnailPath?.let { checkedManagedPath(it, directories) }
        cancellation.ensureActive()
        return project
    }

    private fun checkedManagedPath(path: String, directories: Directories): String {
        val source = File(path)
        val file = source.canonicalFile
        if (!source.isAbsolute || source.absolutePath != file.path ||
            file.parentFile != directories.assets || !file.name.matches(AssetPattern) ||
            !file.isFile || !file.canRead() || file.length() <= 0L
        ) {
            throw TextCardProjectFormatException("Unsafe, missing or invalid managed asset")
        }
        return file.path
    }

    private fun atomicWrite(file: File, bytes: ByteArray, cancellation: CoroutineContext) {
        val atomic = AtomicFile(file)
        val output = atomic.startWrite()
        var committed = false
        try {
            CancellableOutput(output, cancellation).write(bytes)
            output.flush()
            output.fd.sync()
            cancellation.ensureActive()
            atomic.finishWrite(output)
            committed = true
            if (!file.isFile || file.length() != bytes.size.toLong() ||
                File(file.path + ".new").exists() || File(file.path + ".bak").exists()
            ) {
                throw IOException("Could not commit project JSON")
            }
            syncDirectory(file.parentFile ?: throw IOException("Project has no parent directory"))
        } catch (exception: Throwable) {
            if (!committed) atomic.failWrite(output)
            throw exception
        }
    }

    private fun directories(): Directories {
        val files = context.filesDir.canonicalFile
        val root = childDirectory(files, "text-card")
        return Directories(
            projects = childDirectory(root, "projects"),
            assets = childDirectory(root, "assets"),
        )
    }

    private fun childDirectory(parent: File, name: String): File {
        val directory = File(parent, name)
        if (directory.canonicalFile.parentFile != parent) {
            throw IOException("Invalid project storage directory")
        }
        if (!directory.isDirectory) {
            if (!directory.mkdirs() || !directory.isDirectory) {
                throw IOException("Could not create project storage directory")
            }
            syncDirectory(parent)
        }
        return directory.canonicalFile
    }

    private fun syncDirectory(directory: File) {
        try {
            val descriptor = Os.open(directory.path, OsConstants.O_RDONLY, 0)
            try {
                Os.fsync(descriptor)
            } finally {
                Os.close(descriptor)
            }
        } catch (exception: ErrnoException) {
            throw IOException("Could not synchronize project storage directory", exception)
        }
    }

    private fun projectFile(id: String, directories: Directories): File {
        checkProjectId(id)
        return File(directories.projects, "$id.json").also {
            listOf(it, File(it.path + ".bak"), File(it.path + ".new")).forEach { artifact ->
                if (artifact.canonicalFile != artifact.absoluteFile ||
                    artifact.canonicalFile.parentFile != directories.projects ||
                    (artifact.exists() && !artifact.isFile)
                ) {
                    throw IOException("Unsafe project file path")
                }
            }
        }
    }

    private fun copyStream(input: InputStream, output: OutputStream, cancellation: CoroutineContext) {
        val buffer = ByteArray(CopyBufferSize)
        while (true) {
            cancellation.ensureActive()
            val count = input.read(buffer)
            if (count < 0) break
            output.write(buffer, 0, count)
        }
    }

    private fun checksum(file: File, cancellation: CoroutineContext): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(CopyBufferSize)
            while (true) {
                cancellation.ensureActive()
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().hex()
    }

    private fun ByteArray.hex(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private data class Directories(val projects: File, val assets: File)

    private class CancellableOutput(
        output: OutputStream,
        private val cancellation: CoroutineContext,
    ) : FilterOutputStream(output) {
        override fun write(value: Int) {
            cancellation.ensureActive()
            out.write(value)
        }

        override fun write(bytes: ByteArray, offset: Int, length: Int) {
            cancellation.ensureActive()
            out.write(bytes, offset, length)
        }
    }

    private companion object {
        const val CopyBufferSize = 64 * 1024
        const val MaxProjectBytes = 32 * 1024 * 1024
        const val ThumbnailSide = 640
        const val LogTag = "TextCardProjectStore"
        val ExtensionPattern = Regex("[a-z0-9]{1,10}")
        val AssetPattern = Regex("[a-f0-9]{64}\\.[a-z0-9]{1,10}")
    }
}
