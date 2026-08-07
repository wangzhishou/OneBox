package com.wanbaohe.camera.watermark.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.wanbaohe.camera.watermark.domain.ExportConfig
import com.wanbaohe.camera.watermark.domain.ExportResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 图片导出器
 * 支持多种格式导出水印图片
 */
@Singleton
class WatermarkExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * 导出图片到相册
     * @param bitmap 要导出的图片
     * @param config 导出配置
     * @param oneTimeSaveLocationUri 一次性保存位置 (可选)
     * @return 导出结果
     */
    suspend fun exportToGallery(
        bitmap: Bitmap,
        config: ExportConfig = ExportConfig(),
        oneTimeSaveLocationUri: String? = null
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            // 如果指定了一次性保存位置
            if (oneTimeSaveLocationUri != null) {
                val uri = Uri.parse(oneTimeSaveLocationUri)
                return@withContext exportToUri(bitmap, uri, config)
            }

            val fileName = generateFileName(config.format)
            val mimeType = getMimeType(config.format)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用 MediaStore
                exportWithMediaStore(bitmap, fileName, mimeType, config)
            } else {
                // Android 9 及以下直接写文件
                exportToFile(bitmap, fileName, config)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 导出到指定 Uri
     */
    suspend fun exportToUri(
        bitmap: Bitmap,
        uri: Uri,
        config: ExportConfig = ExportConfig()
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val compressFormat = getCompressFormat(config.format)
                bitmap.compress(compressFormat, config.quality, outputStream)
            }
            ExportResult.Success(uri.toString(), 0)
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error(e.message ?: "导出失败")
        }
    }

    /**
     * 使用 MediaStore 导出（Android 10+）
     */
    private fun exportWithMediaStore(
        bitmap: Bitmap,
        fileName: String,
        mimeType: String,
        config: ExportConfig
    ): ExportResult {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CameraWatermark")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return ExportResult.Error("创建文件失败")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val compressFormat = getCompressFormat(config.format)
            bitmap.compress(compressFormat, config.quality, outputStream)
        }

        // 取消 pending 状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
        }

        // 获取文件大小
        val fileSize = context.contentResolver.openFileDescriptor(uri, "r")?.use {
            it.statSize
        } ?: 0L

        return ExportResult.Success(uri.toString(), fileSize)
    }

    /**
     * 直接写文件导出（Android 9 及以下）
     */
    private fun exportToFile(
        bitmap: Bitmap,
        fileName: String,
        config: ExportConfig
    ): ExportResult {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val watermarkDir = File(picturesDir, "CameraWatermark")
        if (!watermarkDir.exists()) {
            watermarkDir.mkdirs()
        }

        val file = File(watermarkDir, fileName)
        FileOutputStream(file).use { outputStream ->
            val compressFormat = getCompressFormat(config.format)
            bitmap.compress(compressFormat, config.quality, outputStream)
        }

        return ExportResult.Success(file.absolutePath, file.length())
    }

    /**
     * 生成文件名
     */
    private fun generateFileName(format: ImageFormat): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val extension = when (format) {
            is ImageFormat.Jpg -> "jpg"
            is ImageFormat.Png -> "png"
            is ImageFormat.Webp.Lossy, is ImageFormat.Webp.Lossless -> "webp"
            else -> "jpg"
        }
        return "IMG_${timestamp}_WATERMARK.$extension"
    }

    /**
     * 获取 MIME 类型
     */
    private fun getMimeType(format: ImageFormat): String {
        return when (format) {
            is ImageFormat.Jpg -> "image/jpeg"
            is ImageFormat.Png -> "image/png"
            is ImageFormat.Webp.Lossy, is ImageFormat.Webp.Lossless -> "image/webp"
            else -> "image/jpeg"
        }
    }

    /**
     * 获取压缩格式
     */
    private fun getCompressFormat(format: ImageFormat): Bitmap.CompressFormat {
        return when (format) {
            is ImageFormat.Png -> Bitmap.CompressFormat.PNG
            is ImageFormat.Webp.Lossy, is ImageFormat.Webp.Lossless -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (format is ImageFormat.Webp.Lossless) {
                        Bitmap.CompressFormat.WEBP_LOSSLESS
                    } else {
                        Bitmap.CompressFormat.WEBP_LOSSY
                    }
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
            else -> Bitmap.CompressFormat.JPEG
        }
    }
}

