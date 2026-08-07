package com.wanbaohe.visual.automation.service

import android.content.Context
import android.graphics.Bitmap
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UI 截图专用图片处理器。
 *
 * ## 与聊天附件管道的关键差异
 *
 * 聊天附件(`FileAnalyzer.processBitmap`)会按 1920px 上限缩放尺寸,因为用户上传的照片
 * 可能是 4K/8K,扔给 LLM 是浪费 token,LLM 看 UI 元素也完全不影响。
 *
 * 但 UI 自动化截图**不能缩尺寸**:
 * - 截图目的是让 AI 根据截图给出**像素坐标**,然后 runtime 用 `mapCoordinates` 映射回
 *   实际屏幕尺寸执行触摸。如果截图本身被压缩,LLM 给出的是压缩图的坐标,
 *   runtime 必须再做比例换算,引入**累积误差**。
 * - 此外,某些 UI 细节(半像素按钮、密集图标、小字)在 1920px 缩放下可能糊掉,
 *   AI 无法准确识别。
 *
 * 因此截图管道采用 **preserveOriginalDimension = true**:只压格式(JPEG→WebP)+ 压品质
 * (默认 80% 起步,逐级降到 1MB 目标),**完全保留原始宽高**。
 *
 * ## 算法一致性
 *
 * 与 [com.shifenmiao.ai.upload.FileAnalyzer.processBitmap] 保持同步(WebP + 逐级降品质 +
 * 缩略图 + 缓存)。修改算法时请双查。
 */
@Singleton
class ScreenshotImageProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        /** 占位常量,实际截图按 [preserveOriginalDimension] 决定是否启用尺寸上限。 */
        const val DISABLED_MAX_DIMENSION = Int.MAX_VALUE

        const val DEFAULT_TARGET_SIZE_BYTES = 1024L * 1024L // 1MB
        const val DEFAULT_WEBP_QUALITY = 80
        const val MIN_WEBP_QUALITY = 20
        const val QUALITY_STEP = 10
        const val THUMBNAIL_MAX_EDGE = 200
        const val THUMBNAIL_QUALITY = 60
    }

    data class Output(
        /** WebP data URI, e.g. data:image/webp;base64,xxxx */
        val dataUri: String,
        /** 仅 base64,不含 data URI 前缀 */
        val base64: String,
        val mimeType: String = "image/webp",
        val processedSize: Long,
        /** 输出图宽高(若 [preserveOriginalDimension] 为 true 则与输入相同) */
        val processedWidth: Int,
        val processedHeight: Int,
        val cachedFilePath: String?,
        val thumbnailBase64: String?,
    )

    /**
     * 把任意来源的截图 Bitmap 按"只压品质、不动尺寸"策略压缩。
     *
     * @param bitmap 源位图,本方法会 recycle 它(若发生缩放,实际几乎不会发生)
     * @param name 用于缓存文件命名
     * @param originalSize 源图原始字节数,用于统计压缩率;不可知时传 0
     * @param targetSizeBytes 目标体积上限,超过则逐级降品质;默认 1MB
     * @param initialQuality WebP 起始品质,默认 80
     * @param preserveOriginalDimension true = 永不缩尺寸,只压品质(默认,符合截图场景);
     *        false = 允许按 maxDimension 缩尺寸(目前截图场景不会走这里)
     * @param maxDimension 仅在 [preserveOriginalDimension] = false 时生效
     */
    fun process(
        bitmap: Bitmap,
        name: String = "screenshot_${System.currentTimeMillis()}",
        originalSize: Long = 0L,
        targetSizeBytes: Long = DEFAULT_TARGET_SIZE_BYTES,
        initialQuality: Int = DEFAULT_WEBP_QUALITY,
        preserveOriginalDimension: Boolean = true,
        maxDimension: Int = DISABLED_MAX_DIMENSION,
    ): Output {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val resized = if (!preserveOriginalDimension &&
            (originalWidth > maxDimension || originalHeight > maxDimension)
        ) {
            val scaled = scaleBitmap(bitmap, maxDimension)
            if (scaled !== bitmap) bitmap.recycle()
            scaled
        } else {
            bitmap
        }

        val processedWidth = resized.width
        val processedHeight = resized.height

        var webpBytes = encodeWebP(resized, initialQuality)
        var currentQuality = initialQuality
        while (webpBytes.size > targetSizeBytes && currentQuality > MIN_WEBP_QUALITY) {
            currentQuality -= QUALITY_STEP
            webpBytes = encodeWebP(resized, currentQuality)
        }

        val base64 = android.util.Base64.encodeToString(webpBytes, android.util.Base64.NO_WRAP)
        val thumbnailBase64 = generateThumbnailBase64(resized)
        val cachedFilePath = saveToCache(webpBytes, name)

        if (!resized.isRecycled) resized.recycle()

        return Output(
            dataUri = "data:image/webp;base64,$base64",
            base64 = base64,
            processedSize = webpBytes.size.toLong(),
            processedWidth = processedWidth,
            processedHeight = processedHeight,
            cachedFilePath = cachedFilePath,
            thumbnailBase64 = thumbnailBase64,
        )
    }

    private fun encodeWebP(bitmap: Bitmap, quality: Int): ByteArray {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, output)
        return output.toByteArray()
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val ratio = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
        val newWidth = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val newHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun generateThumbnailBase64(source: Bitmap): String? {
        return try {
            val ratio = THUMBNAIL_MAX_EDGE.toFloat() / maxOf(source.width, source.height)
            val needsScale = ratio < 1f
            val thumb = if (needsScale) {
                val w = (source.width * ratio).toInt().coerceAtLeast(1)
                val h = (source.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(source, w, h, true)
            } else {
                source
            }
            try {
                val bytes = encodeWebP(thumb, THUMBNAIL_QUALITY)
                "data:image/webp;base64,${android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)}"
            } finally {
                if (thumb !== source) thumb.recycle()
            }
        } catch (e: Exception) {
            makeLog { "ScreenshotImageProcessor thumbnail failed: ${e.message}" }
            null
        }
    }

    private fun saveToCache(bytes: ByteArray, originalName: String): String? {
        return try {
            val cacheDir = File(context.cacheDir, "ui_automation_screenshots")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val fileName = "${System.currentTimeMillis()}_${originalName.substringBeforeLast(".")}.webp"
            val file = File(cacheDir, fileName)
            file.writeBytes(bytes)
            file.absolutePath
        } catch (e: Exception) {
            makeLog { "ScreenshotImageProcessor saveToCache failed: ${e.message}" }
            null
        }
    }
}