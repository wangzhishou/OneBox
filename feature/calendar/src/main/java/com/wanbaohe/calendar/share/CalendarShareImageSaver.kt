package com.wanbaohe.calendar.share

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 分享卡保存。
 *
 * - Android 10+：MediaStore，直接进相册，无需权限。
 * - Android 9 及以下：有写外部存储权限时进相册；没有权限时落到应用专属图片目录，
 *   不申请权限也不静默失败（Google 渠道清单里没有 WRITE_EXTERNAL_STORAGE）。
 */
@Singleton
class CalendarShareImageSaver @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun save(
        bitmap: Bitmap,
        fileName: String = defaultFileName(),
    ): CalendarShareSaveOutcome = withContext(Dispatchers.IO) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(bitmap, fileName)
            } else {
                saveLegacy(bitmap, fileName)
            }
        }.getOrElse { error ->
            CalendarShareSaveOutcome.Failed(error.message ?: "save failed")
        }
    }

    private fun saveWithMediaStore(bitmap: Bitmap, fileName: String): CalendarShareSaveOutcome {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/OneBox/Calendar"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return CalendarShareSaveOutcome.Failed("insert failed")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    error("compress failed")
                }
            } ?: error("open output failed")

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return CalendarShareSaveOutcome.Gallery(
                uri = uri,
                fileName = fileName,
                relativePath = "${Environment.DIRECTORY_PICTURES}/OneBox/Calendar"
            )
        } catch (throwable: Throwable) {
            runCatching { resolver.delete(uri, null, null) }
            throw throwable
        }
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(bitmap: Bitmap, fileName: String): CalendarShareSaveOutcome {
        val granted = context.checkSelfPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            val imageUrl = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                fileName,
                null
            )
            if (imageUrl != null) {
                return CalendarShareSaveOutcome.Gallery(
                    uri = Uri.parse(imageUrl),
                    fileName = fileName,
                    relativePath = Environment.DIRECTORY_PICTURES
                )
            }
        }

        val root = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir
        val dir = File(root, "OneBox/Calendar").apply { mkdirs() }
        val file = File(dir, fileName)
        file.outputStream().use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                error("compress failed")
            }
        }
        return CalendarShareSaveOutcome.AppStorage(
            file = file,
            fileName = fileName,
            relativePath = "app pictures/OneBox/Calendar"
        )
    }

    private fun defaultFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "OneBox_Calendar_$stamp.png"
    }
}

sealed interface CalendarShareSaveOutcome {
    /** 已写入系统相册。 */
    data class Gallery(
        val uri: Uri,
        val fileName: String,
        val relativePath: String,
    ) : CalendarShareSaveOutcome

    /** Android 9 及以下无写权限，已写入应用专属图片目录。 */
    data class AppStorage(
        val file: File,
        val fileName: String,
        val relativePath: String,
    ) : CalendarShareSaveOutcome

    data class Failed(val message: String) : CalendarShareSaveOutcome
}
