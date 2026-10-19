package com.wanbaohe.fileviewer.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.wanbaohe.fileviewer.domain.DocumentSource
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.Closeable
import java.io.FileInputStream
import java.io.FilterInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [DocumentSource] 的 ContentResolver 实现。
 *
 * 全部方法都可能被 WebView 的后台线程并发调用（`shouldInterceptRequest`），
 * 因此不持任何可变状态：每次请求各自开流、各自关闭。
 */
@Singleton
internal class ContentResolverDocumentSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : DocumentSource {

    private val resolver: ContentResolver get() = context.contentResolver

    override fun displayName(uri: Uri): String? {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            runCatching {
                resolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0 && cursor.moveToFirst()) {
                        return cursor.getString(index)?.takeIf { it.isNotBlank() }
                    }
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
    }

    override fun length(uri: Uri): Long {
        // openAssetFileDescriptor 给出的长度对大文件最可靠；provider 不支持时退回 SIZE 列
        runCatching {
            resolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                if (descriptor.length >= 0) return descriptor.length
            }
        }
        runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index >= 0 && cursor.moveToFirst() && !cursor.isNull(index)) {
                    return cursor.getLong(index)
                }
            }
        }
        return -1L
    }

    override fun mimeType(uri: Uri, extension: String): String {
        val declared = runCatching { resolver.getType(uri) }.getOrNull()
        if (!declared.isNullOrBlank() && declared != "application/octet-stream") return declared
        return MIME_OVERRIDES[extension.lowercase()]
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
            ?: "application/octet-stream"
    }

    override fun open(uri: Uri): InputStream? =
        runCatching { resolver.openInputStream(uri) }.getOrNull()

    override fun openRange(uri: Uri, start: Long, endInclusive: Long): InputStream? {
        require(start >= 0 && endInclusive >= start)
        val limit = endInclusive - start + 1

        val descriptor = runCatching { resolver.openAssetFileDescriptor(uri, "r") }.getOrNull()
        if (descriptor != null) {
            val stream = runCatching { FileInputStream(descriptor.fileDescriptor) }.getOrNull()
            if (stream != null) {
                val positioned = runCatching { stream.channel.position(start) }.isSuccess ||
                    runCatching { stream.skipFully(start) }.getOrDefault(false)
                if (positioned) {
                    return LimitedInputStream(stream, limit, descriptor)
                }
                runCatching { stream.close() }
            }
            runCatching { descriptor.close() }
        }

        // provider 不给出可定位的 fd：退回整条流跳过前缀
        val plain = open(uri) ?: return null
        if (plain.skipFully(start)) return LimitedInputStream(plain, limit, null)
        runCatching { plain.close() }
        return null
    }

    private companion object {

        /**
         * 常见文档的后缀 → MIME。
         *
         * 不少 provider 对 Office 文件只报 `application/octet-stream`，
         * `MimeTypeMap` 在旧机器上也不认识 docx/xlsx/pptx，这里兜住。
         */
        val MIME_OVERRIDES = mapOf(
            "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "docm" to "application/vnd.ms-word.document.macroenabled.12",
            "dotx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "xlsm" to "application/vnd.ms-excel.sheet.macroenabled.12",
            "xlsb" to "application/vnd.ms-excel.sheet.binary.macroenabled.12",
            "xltx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
            "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "ppsx" to "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            "pptm" to "application/vnd.ms-powerpoint.presentation.macroenabled.12",
            "potx" to "application/vnd.openxmlformats-officedocument.presentationml.template",
            "doc" to "application/msword",
            "xls" to "application/vnd.ms-excel",
            "ppt" to "application/vnd.ms-powerpoint",
            "odt" to "application/vnd.oasis.opendocument.text",
            "ods" to "application/vnd.oasis.opendocument.spreadsheet",
            "rtf" to "application/rtf",
            "epub" to "application/epub+zip",
            "csv" to "text/csv",
            "md" to "text/markdown",
            "svg" to "image/svg+xml",
        )
    }
}

/** 最多读 [limit] 字节，读满即报 EOF，保证 206 的 Content-Length 不被撑破。 */
private class LimitedInputStream(
    input: InputStream,
    private var remaining: Long,
    private val alsoClose: Closeable?,
) : FilterInputStream(input) {

    override fun read(): Int {
        if (remaining <= 0) return -1
        val value = super.read()
        if (value >= 0) remaining--
        return value
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (remaining <= 0) return -1
        val read = super.read(buffer, offset, minOf(length.toLong(), remaining).toInt())
        if (read > 0) remaining -= read
        return read
    }

    override fun skip(count: Long): Long {
        val skipped = super.skip(minOf(count, remaining))
        if (skipped > 0) remaining -= skipped
        return skipped
    }

    override fun available(): Int = minOf(super.available().toLong(), remaining).toInt()

    override fun close() {
        runCatching { super.close() }
        runCatching { alsoClose?.close() }
    }
}

/** `skip()` 允许一次少跳几个字节，这里跳到目标位置或明确失败。 */
private fun InputStream.skipFully(distance: Long): Boolean {
    var remaining = distance
    while (remaining > 0) {
        val skipped = skip(remaining)
        if (skipped <= 0) {
            if (read() < 0) return false
            remaining--
        } else {
            remaining -= skipped
        }
    }
    return true
}
