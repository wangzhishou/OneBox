/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.common.editor

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * 编辑器本地媒体仓库。
 *
 * 用户从 SAF 选中的文件会立即复制到 `filesDir/editor-media`，Markdown 里只写入
 * `onebox-media://editor-media/<fileName>` 这种稳定相对 URI。好处：
 * - 不依赖 SAF 的临时/持久授权，重启、清缓存后还能打开；
 * - 备份时把整个目录打包，恢复到新设备后 URI 不变、重新解析即可；
 * - 文章不绑定当前 App 的绝对路径或包名（debug/release 可互相恢复）。
 */
object EditorMediaStore {
    const val DIRECTORY = "editor-media"
    private const val URI_PREFIX = "onebox-media://$DIRECTORY/"
    private val MEDIA_URI_REGEX = Regex("""onebox-media://editor-media/([A-Za-z0-9._-]+)""")

    data class ImportedMedia(
        val fileName: String,
        val displayName: String,
    )

    fun directory(context: Context): File =
        File(context.filesDir, DIRECTORY).apply { mkdirs() }

    fun markdownUri(fileName: String): String = URI_PREFIX + fileName

    suspend fun import(
        context: Context,
        uri: Uri,
        fallbackBaseName: String = "media",
    ): ImportedMedia? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri)
            val displayName = queryDisplayName(context, uri)
                ?: uri.lastPathSegment
                ?: fallbackBaseName
            val extension = resolveExtension(
                mimeType = mimeType,
                displayName = displayName,
                fallback = fallbackBaseName,
            )
            val baseName = sanitizeBaseName(displayName.substringBeforeLast('.'))
                .ifBlank { sanitizeBaseName(fallbackBaseName).ifBlank { "media" } }
            val fileName = "$baseName-${UUID.randomUUID().toString().take(8)}.$extension"
            val target = File(directory(context), fileName)
            resolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null
            if (target.length() <= 0L) {
                target.delete()
                return@runCatching null
            }
            ImportedMedia(fileName = fileName, displayName = displayName)
        }.getOrNull()
    }

    /**
     * 把正文里的稳定相对 URI 展开成当前设备可读取的 file:// 地址。
     * 只对存在的文件做替换，找不到的保持原样，避免把用户手写的文本吞掉。
     */
    fun resolveForDisplay(context: Context, markdown: String): String {
        if (!markdown.contains(URI_PREFIX)) return markdown
        val dir = directory(context)
        return MEDIA_URI_REGEX.replace(markdown) { match ->
            val file = File(dir, match.groupValues[1])
            if (file.isFile) Uri.fromFile(file).toString() else match.value
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? = runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }
    }.getOrNull()

    private fun resolveExtension(
        mimeType: String?,
        displayName: String,
        fallback: String,
    ): String {
        MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
        displayName.substringAfterLast('.', "")
            .takeIf { it.length in 1..6 && it.all { char -> char.isLetterOrDigit() } }
            ?.let { return it.lowercase() }
        return when {
            fallback.contains("image", ignoreCase = true) -> "jpg"
            fallback.contains("video", ignoreCase = true) -> "mp4"
            fallback.contains("audio", ignoreCase = true) -> "m4a"
            else -> "bin"
        }
    }

    private fun sanitizeBaseName(input: String): String = input
        .replace(Regex("""[\\/:*?"<>|\u0000-\u001F]"""), " ")
        .trim()
        .replace(Regex("\\s+"), "-")
        .take(40)
}
