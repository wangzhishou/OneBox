/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.feature.zip.data

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.feature.zip.domain.ArchiveAccessDeniedException
import com.t8rin.imagetoolbox.feature.zip.domain.EncryptedArchiveException
import com.t8rin.imagetoolbox.feature.zip.domain.UnsupportedArchiveFormatException
import com.t8rin.imagetoolbox.feature.zip.domain.ZipArchive
import com.t8rin.imagetoolbox.feature.zip.domain.ZipArchiveEntry
import com.t8rin.imagetoolbox.feature.zip.domain.ZipManager
import com.t8rin.imagetoolbox.feature.zip.domain.ZipProgress
import com.t8rin.imagetoolbox.feature.zip.domain.ZipSourceFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

internal class AndroidZipManager @Inject constructor(
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder
) : DispatchersHolder by dispatchersHolder, ZipManager {

    override suspend fun describeSources(uris: List<String>): List<ZipSourceFile> =
        withContext(ioDispatcher) {
            uris.mapIndexed { index, raw ->
                val uri = raw.toUri()
                val resolvedName = resolveDisplayName(uri)
                val name = resolvedName ?: uri.lastPathSegment ?: "file_${index + 1}"
                ZipSourceFile(
                    uri = raw,
                    name = name,
                    size = resolveSize(uri),
                    extension = name.substringAfterLast('.', "").lowercase(),
                    mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull()
                )
            }
        }

    override suspend fun compress(
        files: List<String>,
        destinationUri: String,
        onProgress: (ZipProgress) -> Unit
    ) = withContext(ioDispatcher) {
        if (files.isEmpty()) return@withContext

        val totalFiles = files.size
        val totalBytes = files.sumOf { resolveSize(it.toUri()) }
        var doneBytes = 0L
        var lastReportedAt = 0L

        fun report(doneFiles: Int, force: Boolean = false) {
            val now = SystemClock.elapsedRealtime()
            if (force || now - lastReportedAt >= PROGRESS_INTERVAL_MS) {
                lastReportedAt = now
                onProgress(
                    ZipProgress(
                        doneBytes = if (totalBytes > 0L) doneBytes.coerceAtMost(totalBytes) else doneBytes,
                        totalBytes = totalBytes,
                        doneFiles = doneFiles,
                        totalFiles = totalFiles
                    )
                )
            }
        }

        onProgress(ZipProgress(totalBytes = totalBytes, totalFiles = totalFiles))

        var completed = false
        try {
            val usedNames = mutableSetOf<String>()
            context.contentResolver
                .openOutputStream(destinationUri.toUri(), "w")
                ?.use { rawOutput ->
                    ZipOutputStream(BufferedOutputStream(rawOutput)).use { output ->
                        files.forEachIndexed { index, rawUri ->
                            coroutineContext.ensureActive()

                            val uri = rawUri.toUri()
                            val sourceName = resolveDisplayName(uri) ?: "file_${index + 1}"
                            val entryName = uniqueEntryName(sourceName, usedNames)

                            context.contentResolver.openInputStream(uri)?.use { rawInput ->
                                output.putNextEntry(ZipEntry(entryName))
                                BufferedInputStream(rawInput).use { input ->
                                    copyWithProgress(input, output) { copied ->
                                        doneBytes += copied
                                        report(doneFiles = index)
                                    }
                                }
                                output.closeEntry()
                            } ?: throw IOException("Unable to read $uri")

                            report(doneFiles = index + 1, force = true)
                        }
                    }
                } ?: throw IOException("Unable to write $destinationUri")

            report(doneFiles = totalFiles, force = true)
            completed = true
        } finally {
            // 取消/失败时把已经创建出来的半成品压缩包删掉
            if (!completed) deleteDocumentQuietly(destinationUri.toUri())
        }
    }

    override suspend fun displayName(uri: String): String? = withContext(ioDispatcher) {
        resolveDisplayName(uri.toUri())
    }

    override suspend fun openArchive(
        uri: String,
        onProgress: (ZipProgress) -> Unit
    ): ZipArchive = withContext(ioDispatcher) {
        val archiveUri = uri.toUri()
        val displayName = resolveDisplayName(archiveUri) ?: "archive.zip"
        if (!isZipArchive(displayName, runCatching { context.contentResolver.getType(archiveUri) }.getOrNull())) {
            throw UnsupportedArchiveFormatException()
        }

        cleanupCacheOnFirstUse()
        val tempFile = copyToCache(
            uri = archiveUri,
            totalBytes = resolveSize(archiveUri),
            onProgress = onProgress
        )
        try {
            val zipFile = openZipFile(tempFile)
            val entriesByPath = linkedMapOf<String, Pair<ZipArchiveEntry, ZipEntry>>()

            zipFile.entries().toList().forEach { zipEntry ->
                val path = normalizedEntryPath(zipEntry.name)
                if (path.isBlank() || isHiddenArchiveEntry(path)) return@forEach

                val isDirectory = zipEntry.isDirectory || path.endsWith("/")
                val entry = ZipArchiveEntry(
                    path = path,
                    size = zipEntry.size.coerceAtLeast(0L),
                    compressedSize = zipEntry.compressedSize.coerceAtLeast(0L),
                    lastModified = zipEntry.time,
                    isDirectory = isDirectory,
                    mimeType = if (isDirectory) null else mimeTypeOf(path)
                )
                entriesByPath[path] = entry to zipEntry
            }

            val sortedEntries = entriesByPath.values
                .map { it.first }
                .sortedWith(
                    compareBy<ZipArchiveEntry> { !it.isDirectory }
                        .thenBy { it.path.lowercase() }
                )

            AndroidZipArchive(
                name = displayName,
                tempFile = tempFile,
                zipFile = zipFile,
                entries = sortedEntries,
                entriesByPath = entriesByPath.mapValues { it.value.second },
                archiveSize = tempFile.length()
            )
        } catch (t: Throwable) {
            tempFile.delete()
            // Android 的 ZipFile 遇到传统加密条目会在构造时抛
            // ZipException("invalid CEN header (encrypted entry)")，这里转成可读的业务异常
            if (t is ZipException && t.message?.contains("encrypt", ignoreCase = true) == true) {
                throw EncryptedArchiveException()
            }
            throw t
        }
    }

    private suspend fun copyToCache(
        uri: Uri,
        totalBytes: Long,
        onProgress: (ZipProgress) -> Unit
    ): File {
        val cacheDirectory = File(context.cacheDir, ZIP_CACHE_DIRECTORY).apply { mkdirs() }
        val tempFile = File.createTempFile("archive_", ".zip", cacheDirectory)
        var copiedBytes = 0L
        var lastReportedAt = 0L

        fun report(force: Boolean = false) {
            val now = SystemClock.elapsedRealtime()
            if (force || now - lastReportedAt >= PROGRESS_INTERVAL_MS) {
                lastReportedAt = now
                onProgress(
                    ZipProgress(
                        doneBytes = copiedBytes,
                        totalBytes = totalBytes
                    )
                )
            }
        }

        onProgress(ZipProgress(totalBytes = totalBytes))
        try {
            openArchiveInputStream(uri)?.use { input ->
                BufferedInputStream(input).use { buffered ->
                    BufferedOutputStream(tempFile.outputStream()).use { output ->
                        copyWithProgress(buffered, output) { copied ->
                            copiedBytes += copied
                            report()
                        }
                    }
                }
            } ?: throw archiveReadFailure(uri)
            report(force = true)
        } catch (t: Throwable) {
            tempFile.delete()
            throw t
        }
        return tempFile
    }

    /**
     * file:// 直开无读权限时 openInputStream 会抛 SecurityException、返回 null，
     * 或把 EACCES 包成 FileNotFoundException("... Permission denied")，
     * 统一转成 [ArchiveAccessDeniedException]，页面据此走 SAF 授权引导而非笼统报错。
     */
    private fun openArchiveInputStream(uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (e: SecurityException) {
            throw if (uri.scheme == "file") ArchiveAccessDeniedException() else e
        } catch (e: FileNotFoundException) {
            throw if (uri.scheme == "file" && isFileAccessDenied(uri, e)) {
                ArchiveAccessDeniedException()
            } else e
        }
    }

    private fun isFileAccessDenied(uri: Uri, e: FileNotFoundException): Boolean {
        val message = e.message.orEmpty()
        if (message.contains("EACCES") || message.contains("Permission denied")) return true
        val file = uri.path?.let(::File) ?: return false
        return file.exists() && !file.canRead()
    }

    private fun archiveReadFailure(uri: Uri): Exception {
        return if (uri.scheme == "file") ArchiveAccessDeniedException()
        else IOException("Unable to read $uri")
    }

    /**
     * 分块拷贝，按 64KB 缓冲推进度；每 80ms 只上报一次，避免大文件时刷新过密。
     */
    private suspend fun copyWithProgress(
        input: InputStream,
        output: OutputStream,
        onBytesCopied: (Long) -> Unit
    ) {
        val buffer = ByteArray(COPY_BUFFER_SIZE)
        while (true) {
            coroutineContext.ensureActive()
            val read = input.read(buffer)
            if (read < 0) break
            if (read == 0) continue
            output.write(buffer, 0, read)
            onBytesCopied(read.toLong())
        }
    }

    /**
     * Java 的 ZipFile 在条目没有置 UTF-8 标志时默认按 UTF-8 解文件名。
     * Windows 资源管理器 / 老版 WinRAR / 7-Zip 常用 GBK 且不置标志，
     * 这里先按 UTF-8 打开，发现替换字符再退回 GBK 重开。
     */
    private fun openZipFile(file: File): ZipFile {
        val utf8Zip = ZipFile(file, Charsets.UTF_8)
        val hasBrokenNames = runCatching {
            utf8Zip.entries().toList().any { it.name.contains('\uFFFD') }
        }.getOrDefault(false)

        if (!hasBrokenNames) return utf8Zip

        runCatching { utf8Zip.close() }
        return runCatching {
            ZipFile(file, Charset.forName("GBK"))
        }.getOrElse {
            ZipFile(file)
        }
    }

    /**
     * ZipEntry 不暴露加密标志，打开一次输入流来探测；
     * 传统加密条目会在 getInputStream 时直接抛 ZipException。
     */
    private fun isZipEntryEncrypted(zipFile: ZipFile, entry: ZipEntry): Boolean = runCatching {
        zipFile.getInputStream(entry).use { }
        false
    }.getOrElse { throwable ->
        throwable is ZipException &&
            throwable.message?.contains("encrypt", ignoreCase = true) == true
    }

    /**
     * 进程被强杀时 close() 不会执行，cacheDir 里会残留临时压缩包。
     * 每个进程第一次打开压缩包时清空一次；此时本进程还没有打开任何包，
     * 不会误删正在使用的缓存。
     */
    private fun cleanupCacheOnFirstUse() {
        synchronized(cacheLock) {
            if (cacheCleaned) return
            cacheCleaned = true
            runCatching {
                File(context.cacheDir, ZIP_CACHE_DIRECTORY)
                    .listFiles()
                    ?.forEach { it.delete() }
            }
        }
    }

    private fun deleteDocumentQuietly(uri: Uri) {
        runCatching { DocumentFile.fromSingleUri(context, uri)?.delete() }
        runCatching { DocumentsContract.deleteDocument(context.contentResolver, uri) }
        runCatching { context.contentResolver.delete(uri, null, null) }
    }

    private fun deleteRecursively(document: DocumentFile) {
        runCatching {
            document.listFiles().forEach { child ->
                if (child.isDirectory) deleteRecursively(child) else child.delete()
            }
        }
        runCatching { document.delete() }
    }

    private fun renameToDesiredName(file: DocumentFile, desiredName: String) {
        if (file.name != desiredName) {
            runCatching { file.renameTo(desiredName) }
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.path?.substringAfterLast('/')?.let(Uri::decode)
        }
        // 目录树 URI（解压目标）在 OpenableColumns 里没有 DISPLAY_NAME，需按 tree 取
        if (runCatching { DocumentsContract.isTreeUri(uri) }.getOrDefault(false)) {
            runCatching { DocumentFile.fromTreeUri(context, uri)?.name }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
        }
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1 && cursor.moveToFirst()) cursor.getString(index) else null
            }
        }.getOrNull() ?: runCatching {
            DocumentFile.fromSingleUri(context, uri)?.name
        }.getOrNull()
    }

    private fun resolveSize(uri: Uri): Long {
        if (uri.scheme == "file") {
            return runCatching { File(uri.path!!).length() }.getOrDefault(0L)
        }
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index != -1 && cursor.moveToFirst() && !cursor.isNull(index)) {
                    cursor.getLong(index)
                } else null
            }
        }.getOrNull() ?: 0L
    }

    private fun isZipArchive(displayName: String, mimeType: String?): Boolean {
        val extension = displayName.substringAfterLast('.', "").lowercase()
        return extension == "zip" || mimeType == "application/zip" ||
            mimeType == "application/x-zip-compressed"
    }

    private fun normalizedEntryPath(rawName: String): String {
        val path = rawName.replace('\\', '/').trimStart('/')
        if (path.split('/').any { it == ".." || it == "." }) return ""
        return path
    }

    private fun isHiddenArchiveEntry(path: String): Boolean {
        val normalized = path.trimEnd('/')
        return normalized == "__MACOSX" ||
            normalized.startsWith("__MACOSX/") ||
            normalized.substringAfterLast('/') == ".DS_Store"
    }

    private fun mimeTypeOf(path: String): String? {
        val extension = path.substringAfterLast('.', "").lowercase()
        if (extension.isBlank()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun uniqueEntryName(
        originalName: String,
        usedNames: MutableSet<String>
    ): String {
        val safeName = originalName.ifBlank { "file" }
        if (usedNames.add(safeName)) return safeName

        val base = safeName.substringBeforeLast('.', safeName)
        val extension = safeName.substringAfterLast('.', "")
        var index = 1
        while (true) {
            val candidate = if (extension.isBlank()) {
                "$base ($index)"
            } else {
                "$base ($index).$extension"
            }
            if (usedNames.add(candidate)) return candidate
            index++
        }
    }

    private inner class AndroidZipArchive(
        override val name: String,
        private val tempFile: File,
        private val zipFile: ZipFile,
        override val entries: List<ZipArchiveEntry>,
        private val entriesByPath: Map<String, ZipEntry>,
        override val archiveSize: Long
    ) : ZipArchive {

        override val totalSize: Long =
            entries.filterNot { it.isDirectory }.sumOf { it.size }

        override val totalCompressedSize: Long =
            entries.filterNot { it.isDirectory }.sumOf { it.compressedSize }

        private val fileEntries: List<ZipArchiveEntry> =
            entries.filterNot { it.isDirectory }

        private val rootFolderName: String =
            name.substringBeforeLast('.', name).ifBlank { "archive" }

        /**
         * 加密条目只会在解压时暴露，先探一遍再把整个解压拒掉，
         * 避免解到一半才失败、留下半成品。
         */
        private fun ensureNoEncryptedEntries() {
            val encrypted = fileEntries.any { item ->
                val zipEntry = entriesByPath[item.path] ?: return@any false
                isZipEntryEncrypted(zipFile, zipEntry)
            }
            if (encrypted) throw EncryptedArchiveException()
        }

        override suspend fun extractAll(
            destinationTreeUri: String,
            onProgress: (ZipProgress) -> Unit
        ): String = withContext(ioDispatcher) {
            // 有一个加密条目就整包拒绝，避免解出一半才报错
            ensureNoEncryptedEntries()

            val tree = DocumentFile.fromTreeUri(context, destinationTreeUri.toUri())
                ?: throw IOException("Unable to open destination folder")
            val root = createUniqueDirectory(tree, rootFolderName)
                ?: throw IOException("Unable to create destination folder")
            val directories = mutableMapOf("" to root)

            val totalFiles = fileEntries.size
            val totalBytes = totalSize
            var doneBytes = 0L
            var lastReportedAt = 0L

            fun report(doneFiles: Int, force: Boolean = false) {
                val now = SystemClock.elapsedRealtime()
                if (force || now - lastReportedAt >= PROGRESS_INTERVAL_MS) {
                    lastReportedAt = now
                    onProgress(
                        ZipProgress(
                            doneBytes = doneBytes,
                            totalBytes = totalBytes,
                            doneFiles = doneFiles,
                            totalFiles = totalFiles
                        )
                    )
                }
            }

            onProgress(ZipProgress(totalBytes = totalBytes, totalFiles = totalFiles))

            var completed = false
            try {
                fileEntries.forEachIndexed { index, entry ->
                    coroutineContext.ensureActive()

                    val parent = ensureDirectory(
                        parentPath = entry.parentPath,
                        root = root,
                        directories = directories
                    )
                    val zipEntry = entriesByPath[entry.path]
                        ?: return@forEachIndexed
                    val target = createUniqueFile(parent, entry.name, entry.mimeType)
                        ?: throw IOException("Unable to create ${entry.name}")

                    zipFile.getInputStream(zipEntry).use { input ->
                        context.contentResolver.openOutputStream(target.uri)?.use { output ->
                            BufferedInputStream(input).use { buffered ->
                                BufferedOutputStream(output).use { bufferedOutput ->
                                    copyWithProgress(buffered, bufferedOutput) { copied ->
                                        doneBytes += copied
                                        report(doneFiles = index)
                                    }
                                }
                            }
                        } ?: throw IOException("Unable to write ${entry.name}")
                    }

                    report(doneFiles = index + 1, force = true)
                }

                report(doneFiles = totalFiles, force = true)
                completed = true
                root.uri.toString()
            } finally {
                // 取消/失败时把已经解出来的半成品文件夹整个删掉
                if (!completed) deleteRecursively(root)
            }
        }

        override suspend fun extractEntry(
            entry: ZipArchiveEntry,
            destinationUri: String,
            onProgress: (ZipProgress) -> Unit
        ) = withContext(ioDispatcher) {
            val zipEntry = entriesByPath[entry.path]
                ?: throw IOException("Entry not found: ${entry.path}")
            if (isZipEntryEncrypted(zipFile, zipEntry)) throw EncryptedArchiveException()

            val totalBytes = entry.size.coerceAtLeast(0L)
            var doneBytes = 0L
            var lastReportedAt = 0L

            fun report(force: Boolean = false) {
                val now = SystemClock.elapsedRealtime()
                if (force || now - lastReportedAt >= PROGRESS_INTERVAL_MS) {
                    lastReportedAt = now
                    onProgress(
                        ZipProgress(
                            doneBytes = doneBytes,
                            totalBytes = totalBytes,
                            doneFiles = if (force) 1 else 0,
                            totalFiles = 1
                        )
                    )
                }
            }

            onProgress(ZipProgress(totalBytes = totalBytes, totalFiles = 1))

            var completed = false
            try {
                zipFile.getInputStream(zipEntry).use { input ->
                    context.contentResolver.openOutputStream(destinationUri.toUri(), "w")?.use { output ->
                        BufferedInputStream(input).use { buffered ->
                            BufferedOutputStream(output).use { bufferedOutput ->
                                copyWithProgress(buffered, bufferedOutput) { copied ->
                                    doneBytes += copied
                                    report()
                                }
                            }
                        }
                    } ?: throw IOException("Unable to write $destinationUri")
                }
                report(force = true)
                completed = true
            } finally {
                if (!completed) deleteDocumentQuietly(destinationUri.toUri())
            }
        }

        override fun close() {
            runCatching { zipFile.close() }
            tempFile.delete()
        }

        private fun ensureDirectory(
            parentPath: String,
            root: DocumentFile,
            directories: MutableMap<String, DocumentFile>
        ): DocumentFile {
            if (parentPath.isBlank()) return root

            var current = root
            var accumulatedPath = ""
            parentPath.split('/').filter { it.isNotBlank() }.forEach { segment ->
                accumulatedPath = if (accumulatedPath.isBlank()) {
                    segment
                } else {
                    "$accumulatedPath/$segment"
                }
                current = directories.getOrPut(accumulatedPath) {
                    current.findFile(segment)?.takeIf { it.isDirectory }
                        ?: current.createDirectory(segment)
                        ?: throw IOException("Unable to create folder $segment")
                }
            }
            return current
        }

        private fun createUniqueDirectory(parent: DocumentFile, preferredName: String): DocumentFile? {
            val safeName = preferredName.replace('/', '_').replace('\\', '_').ifBlank { "archive" }
            if (parent.findFile(safeName) == null) {
                parent.createDirectory(safeName)?.let { return it }
            }

            // 每次解压都落到新文件夹，避免重复解压时新旧文件混在同一目录里
            var index = 1
            while (index < 100) {
                val candidate = "$safeName ($index)"
                if (parent.findFile(candidate) == null) {
                    parent.createDirectory(candidate)?.let { return it }
                }
                index++
            }
            return null
        }

        private fun createUniqueFile(
            parent: DocumentFile,
            preferredName: String,
            mimeType: String?
        ): DocumentFile? {
            val safeName = preferredName.ifBlank { "file" }
            val mime = mimeType ?: "application/octet-stream"
            if (parent.findFile(safeName) == null) {
                parent.createFile(mime, safeName)?.let { file ->
                    renameToDesiredName(file, safeName)
                    return file
                }
            }

            val base = safeName.substringBeforeLast('.', safeName)
            val extension = safeName.substringAfterLast('.', "")
            var index = 1
            while (index < 1000) {
                val candidate = if (extension.isBlank()) {
                    "$base ($index)"
                } else {
                    "$base ($index).$extension"
                }
                if (parent.findFile(candidate) == null) {
                    parent.createFile(mime, candidate)?.let { file ->
                        renameToDesiredName(file, candidate)
                        return file
                    }
                }
                index++
            }
            return null
        }
    }

    private val cacheLock = Any()
    private var cacheCleaned = false

    private companion object {
        const val ZIP_CACHE_DIRECTORY = "zip_browser"
        const val COPY_BUFFER_SIZE = 64 * 1024
        const val PROGRESS_INTERVAL_MS = 80L
    }
}
