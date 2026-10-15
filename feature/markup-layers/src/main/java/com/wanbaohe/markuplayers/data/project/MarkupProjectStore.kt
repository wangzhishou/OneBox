package com.wanbaohe.markuplayers.data.project

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.util.AtomicFile
import android.webkit.MimeTypeMap
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.serialization.UiFilterCodec
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerSource
import com.wanbaohe.markuplayers.domain.model.LayerEffect
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.project.MarkupDocument
import com.wanbaohe.markuplayers.domain.project.MarkupProjectSummary
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Private, offline project storage under filesDir/image-studio. Assets are immutable, content-
 * addressed raw copies; deleting a project deliberately retains assets referenced by undo or peers.
 * Errors (including cancellation and corrupt projects during refresh) are propagated to the caller.
 */
@Singleton
class MarkupProjectStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val mutex = Mutex()
    private val mutableProjects = MutableStateFlow<List<MarkupProjectSummary>>(emptyList())
    val projects: StateFlow<List<MarkupProjectSummary>> = mutableProjects.asStateFlow()

    suspend fun refresh() = onDisk { directories, cancellation ->
        publishProjects(directories, cancellation)
    }

    suspend fun importAsset(source: String): String = onDisk { directories, cancellation ->
        importAsset(source, directories, cancellation)
    }

    suspend fun storeBitmap(bitmap: Bitmap): String = onDisk { directories, cancellation ->
        storeBitmap(bitmap, directories, cancellation)
    }

    /**
     * 配方素材物化:图层/滤镜引用的外部素材(content://、临时文件等)转存为
     * 私有持久文件;[document] 的 baseUri 是配方占位符(recipe://base),不导入。
     */
    suspend fun materializeRecipeAssets(document: MarkupDocument): MarkupDocument =
        onDisk { directories, cancellation ->
            normalizeDocument(document, directories, cancellation, importBase = false)
        }

    /** Returns the document with every external asset reference replaced by a durable path. */
    suspend fun save(
        id: String,
        name: String,
        document: MarkupDocument,
        thumbnail: Bitmap? = null,
    ): MarkupDocument = onDisk { directories, cancellation ->
        checkProjectId(id)
        val file = projectFile(id, directories)
        val previous = if (file.exists() || File(file.path + ".bak").exists()) {
            readProject(id, directories, cancellation)
        } else null
        val durable = normalizeDocument(document, directories, cancellation)
        val thumbnailPath = thumbnail?.let { storeBitmap(it, directories, cancellation) }
            ?: previous?.summary?.thumbnailPath
        val project = StoredMarkupProject(
            summary = MarkupProjectSummary(
                id = id, name = name, updatedAt = System.currentTimeMillis(),
                thumbnailPath = thumbnailPath, layerCount = durable.layers.size
            ),
            document = durable
        )
        val bytes = MarkupProjectCodec.encode(project).toByteArray(Charsets.UTF_8)
        if (bytes.size > MaxProjectBytes) throw IOException("Project JSON exceeds the size limit")
        cancellation.ensureActive()
        atomicWrite(file, bytes, cancellation)
        publishProjects(directories, cancellation)
        durable
    }

    suspend fun load(id: String): MarkupDocument = onDisk { directories, cancellation ->
        readProject(id, directories, cancellation).document
    }

    suspend fun delete(id: String) = onDisk { directories, cancellation ->
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

    private fun normalizeDocument(
        document: MarkupDocument,
        directories: Directories,
        cancellation: CoroutineContext,
        importBase: Boolean = true,
    ): MarkupDocument {
        val imported = mutableMapOf<String, String>()
        fun asset(source: String): String = imported.getOrPut(source) {
            importAsset(source, directories, cancellation)
        }
        fun image(data: Any): Any = when (data) {
            is Bitmap -> storeBitmap(data, directories, cancellation)
            is Uri -> asset(data.toString())
            is File -> asset(data.absolutePath)
            is String -> if (data.isEmpty()) data else asset(data)
            is Int -> data.also { if (it <= 0) throw IOException("Invalid image resource ID") }
            else -> throw IOException("Unsupported image asset type")
        }
        fun filter(filter: UiFilter<*>): UiFilter<*> = UiFilterCodec.decode(
            UiFilterCodec.encode(filter, ::image)
        )
        fun effects(effects: List<LayerEffect>) = effects.map { it.copy(filter = filter(it.filter)) }
        return document.copy(
            baseUri = if (importBase) asset(document.baseUri) else document.baseUri,
            originalUri = document.originalUri?.let(::asset),
            baseEffects = effects(document.baseEffects),
            layers = document.layers.map { layer ->
                cancellation.ensureActive()
                layer.copy(
                    type = when (val type = layer.type) {
                        is LayerType.Image -> LayerType.Image(when (val data = type.imageData) {
                            is Bitmap -> storeBitmap(data, directories, cancellation)
                            is Uri -> asset(data.toString())
                            is File -> asset(data.absolutePath)
                            is String -> asset(data)
                            else -> throw IOException("Unsupported image layer asset type")
                        })
                        is LayerType.Sticker -> when (val source = type.source) {
                            is StickerSource.Generated -> type.copy(
                                source = source.copy(path = asset(source.path))
                            )
                            is StickerSource.Asset -> type.also { checkAssetPath(source.path) }
                            is StickerSource.Emoji -> type
                        }
                        is LayerType.Text -> when (val font = type.font) {
                            is FontType.File -> type.copy(font = font.copy(path = asset(font.path)))
                            else -> type
                        }
                        is LayerType.Draw, is LayerType.Shape -> type
                    },
                    filter = layer.filter?.let(::filter),
                    effects = effects(layer.effects),
                    imageHistory = layer.imageHistory.map(::asset),
                )
            },
        )
    }

    private fun importAsset(
        source: String,
        directories: Directories,
        cancellation: CoroutineContext,
    ): String {
        if (source.isBlank() || '\u0000' in source) throw IOException("Invalid asset source")
        cancellation.ensureActive()
        if (source.startsWith('/')) {
            return importFile(File(source), directories, cancellation)
        }
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

    private fun storeBitmap(
        bitmap: Bitmap,
        directories: Directories,
        cancellation: CoroutineContext,
    ): String {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
            throw IOException("Cannot persist an invalid bitmap")
        }
        // 硬件位图(HARDWARE config)不能直接编码,先回读成软件位图
        val encodable = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
                ?: throw IOException("Could not read back a hardware bitmap")
        } else bitmap
        try {
            return writeAsset("png", directories, cancellation) { output ->
                if (!encodable.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    throw IOException("Could not encode bitmap")
                }
            }
        } finally {
            if (encodable !== bitmap) encodable.recycle()
        }
    }

    private fun writeAsset(
        extension: String,
        directories: Directories,
        cancellation: CoroutineContext,
        write: (OutputStream) -> Unit,
    ): String {
        val pending = File(directories.assets, ".pending-${UUID.randomUUID()}.part")
        if (!pending.createNewFile()) throw IOException("Could not create asset staging file")
        var failure: Throwable? = null
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileOutputStream(pending).use { fileOutput ->
                val output = DigestOutputStream(CancellableOutput(fileOutput, cancellation), digest)
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
        val summaries = ids.map { id ->
            cancellation.ensureActive()
            readProject(id, directories, cancellation).summary
        }.sortedWith(compareByDescending<MarkupProjectSummary> { it.updatedAt }.thenBy { it.id })
        cancellation.ensureActive()
        mutableProjects.value = summaries
    }

    private fun readProject(
        id: String,
        directories: Directories,
        cancellation: CoroutineContext,
    ): StoredMarkupProject {
        checkProjectId(id)
        cancellation.ensureActive()
        val atomic = AtomicFile(projectFile(id, directories))
        val bytes = atomic.openRead().use { input ->
            val output = java.io.ByteArrayOutputStream()
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
            throw MarkupProjectFormatException("Project is not valid UTF-8", exception)
        }
        val project = MarkupProjectCodec.decode(source)
        if (project.summary.id != id) throw MarkupProjectFormatException("Project ID does not match its file")
        validateAssets(project.document, directories)
        project.summary.thumbnailPath?.let { checkedManagedPath(it, directories) }
        cancellation.ensureActive()
        return project
    }

    private fun validateAssets(document: MarkupDocument, directories: Directories) {
        fun asset(data: Any): Any = when (data) {
            is Int -> data.also { if (it <= 0) throw IOException("Invalid resource ID") }
            is String -> data.also {
                if (it.isNotEmpty()) checkedManagedPath(it, directories)
            }
            else -> throw MarkupProjectFormatException("Invalid persisted filter asset")
        }
        fun filter(filter: UiFilter<*>) {
            UiFilterCodec.encode(filter, ::asset)
        }
        checkedManagedPath(document.baseUri, directories)
        document.originalUri?.let { checkedManagedPath(it, directories) }
        document.baseEffects.forEach { filter(it.filter) }
        document.layers.forEach { layer ->
            when (val type = layer.type) {
                is LayerType.Image -> checkedManagedPath(
                    type.imageData as? String
                        ?: throw MarkupProjectFormatException("Invalid persisted image layer"),
                    directories
                )
                is LayerType.Sticker -> when (val source = type.source) {
                    is StickerSource.Generated -> checkedManagedPath(source.path, directories)
                    is StickerSource.Asset -> checkAssetPath(source.path)
                    is StickerSource.Emoji -> Unit
                }
                is LayerType.Text -> (type.font as? FontType.File)?.let {
                    checkedManagedPath(it.path, directories)
                }
                is LayerType.Draw, is LayerType.Shape -> Unit
            }
            layer.filter?.let(::filter)
            layer.effects.forEach { filter(it.filter) }
            layer.imageHistory.forEach { checkedManagedPath(it, directories) }
        }
    }

    private fun checkedManagedPath(path: String, directories: Directories): String {
        val source = File(path)
        val file = source.canonicalFile
        if (!source.isAbsolute || source.absolutePath != file.path ||
            file.parentFile != directories.assets || !file.name.matches(AssetPattern) ||
            !file.isFile || !file.canRead() || file.length() <= 0L
        ) {
            throw MarkupProjectFormatException("Unsafe, missing or invalid managed asset")
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
        val root = childDirectory(files, "image-studio")
        return Directories(
            projects = childDirectory(root, "projects"),
            assets = childDirectory(root, "assets")
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
        val ExtensionPattern = Regex("[a-z0-9]{1,10}")
        val AssetPattern = Regex("[a-f0-9]{64}\\.[a-z0-9]{1,10}")
    }
}
