package com.wanbaohe.cloud.storage.screen.components

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * 把内存里的字节写到系统「下载」目录。
 *
 * 给没有可下载 URL 的协议（SMB）兜底：DownloadManager 走不了，只能应用内拉流再落盘。
 * Android 10+ 走 MediaStore，旧版本写公共 Downloads 目录。
 */
internal fun saveBytesToDownloads(
    context: Context,
    fileName: String,
    bytes: ByteArray,
    mimeType: String?,
): Result<Uri> = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(
                MediaStore.MediaColumns.MIME_TYPE,
                mimeType ?: "application/octet-stream",
            )
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Cannot create download entry")
        resolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: error("Cannot open download stream")
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    } else {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        Uri.fromFile(file)
    }
}
