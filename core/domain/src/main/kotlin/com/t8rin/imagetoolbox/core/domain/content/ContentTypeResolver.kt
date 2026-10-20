package com.t8rin.imagetoolbox.core.domain.content

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.t8rin.imagetoolbox.core.domain.content.ContentType.Companion.ARCHIVE_EXTENSIONS
import com.t8rin.imagetoolbox.core.domain.content.ContentType.Companion.AUDIO_EXTENSIONS
import com.t8rin.imagetoolbox.core.domain.content.ContentType.Companion.DOCUMENT_EXTENSIONS
import com.t8rin.imagetoolbox.core.domain.content.ContentType.Companion.HTML_EXTENSIONS
import com.t8rin.imagetoolbox.core.domain.content.ContentType.Companion.IMAGE_EXTENSIONS
import com.t8rin.imagetoolbox.core.domain.content.ContentType.Companion.MARKDOWN_EXTENSIONS
import com.t8rin.imagetoolbox.core.domain.content.ContentType.Companion.PDF_EXTENSIONS
import com.t8rin.imagetoolbox.core.domain.content.ContentType.Companion.TEXT_EXTENSIONS
import com.t8rin.imagetoolbox.core.domain.content.ContentType.Companion.VIDEO_EXTENSIONS

/**
 * 内容类型解析器 — 给定一个 URI，返回其内容类别。
 *
 * 这是整个 App 中**唯一的**文件类型判断源。
 * 所有"这个 URI 是什么类型的文件"的问题都应该通过此接口解决，
 * 禁止在业务层重复实现类型判断逻辑。
 */
interface ContentTypeResolver {
    /**
     * @param mimeTypeHint 调用方已知的 MIME 类型（如分享 Intent 的 type）。
     * 外部分享的 content URI 有时读不到文件名/类型，此时用这个提示兜底。
     */
    fun resolve(
        uri: Uri,
        context: Context,
        mimeTypeHint: String? = null
    ): ContentType
}

/**
 * 默认实现 — 综合 MIME type、文件扩展名、DocumentsContract 三种手段判断。
 */
class DefaultContentTypeResolver : ContentTypeResolver {

    override fun resolve(
        uri: Uri,
        context: Context,
        mimeTypeHint: String?
    ): ContentType {
        val detectedMimeType = resolveMimeType(uri, context)
        // zip 的 MIME 提示优先于被文件管理器误标的 text/plain 等类型；
        // 其他类型仍以真实探测结果为准，hint 只在探测不到时兜底。
        val mimeType = when {
            mimeTypeHint.isZipMime() -> mimeTypeHint
            detectedMimeType != null && detectedMimeType != "application/octet-stream" -> detectedMimeType
            mimeTypeHint != null -> mimeTypeHint
            else -> detectedMimeType
        }
        val extension = resolveExtension(uri, context)

        return when {
            isDirectory(uri, context) -> ContentType.Directory

            // 扩展名是压缩包时优先按压缩包处理，避免被误标的 text/plain、audio/* 抢走
            extension in ARCHIVE_EXTENSIONS ->
                ContentType.Archive(mimeType, extension)

            mimeType?.startsWith("image/") == true || extension in IMAGE_EXTENSIONS ->
                ContentType.Image(mimeType)

            mimeType == "application/pdf" || extension in PDF_EXTENSIONS ->
                ContentType.Pdf(mimeType)

            extension in MARKDOWN_EXTENSIONS ->
                ContentType.Markdown(mimeType)

            mimeType == "text/html" || extension in HTML_EXTENSIONS ->
                ContentType.Html(mimeType)

            // 文档要排在 text/* 之前：RTF 在部分机型上被判成 text/rtf，
            // 先落到 Text 就会被代码编辑器打开，而不是文档预览。
            isDocumentMimeType(mimeType) || extension in DOCUMENT_EXTENSIONS ->
                ContentType.Document(mimeType)

            // 压缩包 MIME 排在 text/audio/video 之前；epub 已在上面按 Document 处理
            isArchiveMimeType(mimeType) ->
                ContentType.Archive(mimeType, extension)

            mimeType?.startsWith("text/") == true || extension in TEXT_EXTENSIONS ->
                ContentType.Text(mimeType)

            mimeType?.startsWith("audio/") == true || extension in AUDIO_EXTENSIONS ->
                ContentType.Audio(mimeType)

            mimeType?.startsWith("video/") == true || extension in VIDEO_EXTENSIONS ->
                ContentType.Video(mimeType)

            else -> ContentType.Unknown(mimeType)
        }
    }

    private fun resolveMimeType(uri: Uri, context: Context): String? {
        return context.contentResolver.getType(uri) ?: run {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            if (extension.isNullOrBlank()) null
            else MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        }
    }

    /**
     * 取文件扩展名：优先从 URI path 取；content:// 数字 ID 型 URI
     * （如 content://media/external/file/123）path 里没有文件名，
     * 此时查 DISPLAY_NAME 拿真实文件名再取扩展名。
     */
    private fun resolveExtension(uri: Uri, context: Context): String {
        val fromPath = uri.path?.substringAfterLast('.', "")?.lowercase().orEmpty()
        if (fromPath.isNotBlank() && fromPath.length <= 10 && !fromPath.contains('/')) {
            return fromPath
        }
        return resolveDisplayName(uri, context)
            ?.substringAfterLast('.', "")
            ?.lowercase()
            .orEmpty()
    }

    private fun resolveDisplayName(uri: Uri, context: Context): String? {
        if (uri.scheme != "content") return null
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null, null, null
            )?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx != -1 && cursor.moveToFirst()) cursor.getString(idx) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isDirectory(uri: Uri, context: Context): Boolean {
        return try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(DocumentsContract.Document.COLUMN_MIME_TYPE),
                null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    if (idx != -1) {
                        it.getString(idx) == DocumentsContract.Document.MIME_TYPE_DIR
                    } else false
                } else false
            } ?: false
        } catch (_: Exception) {
            val mimeType = context.contentResolver.getType(uri)
            mimeType?.contains("directory") == true
        }
    }

    private fun String?.isZipMime(): Boolean =
        this == "application/zip" || this == "application/x-zip-compressed"

    private fun isArchiveMimeType(mimeType: String?): Boolean {
        return mimeType?.let {
            it == "application/zip" ||
                it == "application/x-rar-compressed" ||
                it == "application/x-7z-compressed" ||
                it == "application/gzip" ||
                it == "application/x-tar" ||
                it == "application/x-bzip2" ||
                it == "application/x-xz"
        } == true
    }

    private fun isDocumentMimeType(mimeType: String?): Boolean {
        return mimeType?.let {
            it == "application/msword" ||
                it == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                it == "application/vnd.ms-excel" ||
                it == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                it == "application/vnd.ms-powerpoint" ||
                it == "application/vnd.openxmlformats-officedocument.presentationml.presentation" ||
                it == "application/rtf" ||
                it == "application/epub+zip"
        } == true
    }
}
