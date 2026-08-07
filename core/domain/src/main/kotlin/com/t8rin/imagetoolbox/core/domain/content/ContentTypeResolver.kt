package com.t8rin.imagetoolbox.core.domain.content

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
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
    fun resolve(uri: Uri, context: Context): ContentType
}

/**
 * 默认实现 — 综合 MIME type、文件扩展名、DocumentsContract 三种手段判断。
 */
class DefaultContentTypeResolver : ContentTypeResolver {

    override fun resolve(uri: Uri, context: Context): ContentType {
        val mimeType = resolveMimeType(uri, context)
        val extension = resolveExtension(uri)

        return when {
            isDirectory(uri, context) -> ContentType.Directory

            mimeType?.startsWith("image/") == true || extension in IMAGE_EXTENSIONS ->
                ContentType.Image(mimeType)

            mimeType == "application/pdf" || extension in PDF_EXTENSIONS ->
                ContentType.Pdf(mimeType)

            extension in MARKDOWN_EXTENSIONS ->
                ContentType.Markdown(mimeType)

            mimeType == "text/html" || extension in HTML_EXTENSIONS ->
                ContentType.Html(mimeType)

            mimeType?.startsWith("text/") == true || extension in TEXT_EXTENSIONS ->
                ContentType.Text(mimeType)

            mimeType?.startsWith("audio/") == true || extension in AUDIO_EXTENSIONS ->
                ContentType.Audio(mimeType)

            mimeType?.startsWith("video/") == true || extension in VIDEO_EXTENSIONS ->
                ContentType.Video(mimeType)

            isArchiveMimeType(mimeType) || extension in ARCHIVE_EXTENSIONS ->
                ContentType.Archive(mimeType)

            isDocumentMimeType(mimeType) || extension in DOCUMENT_EXTENSIONS ->
                ContentType.Document(mimeType)

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

    private fun resolveExtension(uri: Uri): String {
        return uri.path?.substringAfterLast('.', "")?.lowercase().orEmpty()
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
            mimeType?.contains("directory") == false
        }
    }

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
