package com.shifenmiao.ai.upload

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.ProcessingStep
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 图片处理配置
 */
data class ImageProcessingConfig(
    /** 最大尺寸（像素），默认1920 */
    val maxDimension: Int = DEFAULT_MAX_DIMENSION,
    /** 目标文件大小（字节），默认1MB */
    val targetSizeBytes: Long = DEFAULT_TARGET_SIZE_BYTES,
    /** WebP质量（0-100），默认80 */
    val webpQuality: Int = DEFAULT_WEBP_QUALITY,
) {
    companion object {
        const val DEFAULT_MAX_DIMENSION = 1920
        const val DEFAULT_TARGET_SIZE_BYTES = 1024 * 1024L // 1MB
        const val DEFAULT_WEBP_QUALITY = 80
    }
}

/**
 * 图片处理结果
 */
data class ImageProcessingResult(
    val base64: String,
    val mimeType: String = "image/webp",
    val originalSize: Long,
    val processedSize: Long,
    val originalWidth: Int,
    val originalHeight: Int,
    val processedWidth: Int,
    val processedHeight: Int,
    val cachedFilePath: String? = null,
    val thumbnailBase64: String? = null,
)

/**
 * 文件分析结果
 */
data class FileAnalysisResult(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val size: Long,
    val base64: String? = null,
    val error: String? = null,
)

/**
 * 图片分析器 - 负责图片压缩、尺寸调整、WebP转换
 *
 * 处理流程：
 * 1. 检查图片大小和尺寸
 * 2. 如果尺寸超过限制，按比例缩小
 * 3. 转换为WebP格式（80%质量）
 * 4. 如果仍然超过目标大小，降低质量继续压缩
 * 5. 生成Base64编码
 */
@Singleton
class FileAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        /** 缩略图最大边长（px） */
        private const val THUMBNAIL_MAX_EDGE = 200
        /** 缩略图 WebP 质量 */
        private const val THUMBNAIL_QUALITY = 60
    }

    /**
     * 分析并处理图片，返回Flow用于报告处理进度
     *
     * @param uri 图片URI
     * @param config 处理配置
     * @return Flow，发射处理进度和最终结果
     */
    fun analyzeWithProgress(
        uri: Uri,
        config: ImageProcessingConfig = ImageProcessingConfig()
    ): Flow<AnalyzerProgress> = flow {
        val name = getFileName(uri)
        val mimeType = resolveMimeType(uri)
        val originalSize = getFileSize(uri)

        // 步骤1: 检查图片
        emit(AnalyzerProgress.Step(
            step = ProcessingStep.CHECKING,
            detail = "大小: ${formatSize(originalSize)}"
        ))

        // 检查是否是图片
        if (!mimeType.startsWith("image/")) {
            emit(AnalyzerProgress.Error(context.getString(R.string.attachment_unsupported_type, mimeType)))
            return@flow
        }

        try {
            // 读取原始Bitmap
            val originalBitmap = decodeBitmap(uri) ?: run {
                emit(AnalyzerProgress.Completed(encodeOriginalImage(uri, name, mimeType, originalSize)))
                return@flow
            }

            val originalWidth = originalBitmap.width
            val originalHeight = originalBitmap.height

            // 步骤2: 调整尺寸
            val resizedBitmap = if (originalWidth > config.maxDimension || originalHeight > config.maxDimension) {
                emit(AnalyzerProgress.Step(
                    step = ProcessingStep.RESIZING,
                    detail = "${originalWidth}x${originalHeight} → 计算中..."
                ))
                val scaled = scaleBitmap(originalBitmap, config.maxDimension)
                emit(AnalyzerProgress.Step(
                    step = ProcessingStep.RESIZING,
                    detail = "${originalWidth}x${originalHeight} → ${scaled.width}x${scaled.height}"
                ))
                scaled
            } else {
                originalBitmap
            }

            // 步骤3: 转换为WebP格式
            emit(AnalyzerProgress.Step(
                step = ProcessingStep.CONVERTING,
                detail = "$mimeType → image/webp"
            ))
            var webpBytes = bitmapToWebP(resizedBitmap, config.webpQuality)

            // 步骤4: 如果仍然超过目标大小，降低质量继续压缩
            if (webpBytes.size > config.targetSizeBytes) {
                var currentQuality = config.webpQuality
                while (webpBytes.size > config.targetSizeBytes && currentQuality > 20) {
                    currentQuality -= 10
                    emit(AnalyzerProgress.Step(
                        step = ProcessingStep.COMPRESSING,
                        detail = "质量 $currentQuality%: ${formatSize(webpBytes.size.toLong())}"
                    ))
                    webpBytes = bitmapToWebP(resizedBitmap, currentQuality)
                }
            }

            // 步骤5: 生成Base64编码
            emit(AnalyzerProgress.Step(
                step = ProcessingStep.ENCODING,
                detail = formatSize(webpBytes.size.toLong())
            ))
            val base64 = Base64.encodeToString(webpBytes, Base64.NO_WRAP)

            // 生成缩略图（200px 边长，60% 质量，约 5-10KB）用于 ImageEntity 持久化
            val thumbnailBase64 = generateThumbnailBase64(resizedBitmap, THUMBNAIL_MAX_EDGE, THUMBNAIL_QUALITY)

            // 保存处理后的尺寸
            val processedWidth = resizedBitmap.width
            val processedHeight = resizedBitmap.height

            // 步骤6: 保存到缓存目录
            val cachedFile = saveToCache(webpBytes, name)
            val cachedFilePath = cachedFile?.absolutePath

            // 清理Bitmap
            if (originalBitmap != resizedBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()

            // 完成
            emit(AnalyzerProgress.Completed(
                result = ImageProcessingResult(
                    base64 = "data:image/webp;base64,$base64",
                    mimeType = "image/webp",
                    originalSize = originalSize,
                    processedSize = webpBytes.size.toLong(),
                    originalWidth = originalWidth,
                    originalHeight = originalHeight,
                    processedWidth = processedWidth,
                    processedHeight = processedHeight,
                    cachedFilePath = cachedFilePath,
                    thumbnailBase64 = thumbnailBase64,
                )
            ))
        } catch (e: Exception) {
            val detail = e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
            emit(AnalyzerProgress.Error("${context.getString(R.string.attachment_process_failed)}: $detail"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 不压缩模式：保留原始格式，仅做 Base64 编码和缓存，供聊天附件即时发送使用。
     */
    fun encodeOriginalWithProgress(uri: Uri): Flow<AnalyzerProgress> = flow {
        val name = getFileName(uri)
        val mimeType = resolveMimeType(uri)
        val originalSize = getFileSize(uri)

        emit(AnalyzerProgress.Step(
            step = ProcessingStep.CHECKING,
            detail = "大小: ${formatSize(originalSize)}"
        ))

        if (!mimeType.startsWith("image/")) {
            emit(AnalyzerProgress.Error(context.getString(R.string.attachment_unsupported_type, mimeType)))
            return@flow
        }

        try {
            emit(AnalyzerProgress.Step(
                step = ProcessingStep.ENCODING,
                detail = formatSize(originalSize)
            ))

            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException(context.getString(R.string.attachment_read_failed))
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val (width, height) = readImageBounds(uri)
            val previewBitmap = decodeBitmap(uri, THUMBNAIL_MAX_EDGE)
            val thumbnailBase64 = previewBitmap?.let { bitmap ->
                try {
                    generateThumbnailBase64(bitmap, THUMBNAIL_MAX_EDGE, THUMBNAIL_QUALITY)
                } finally {
                    bitmap.recycle()
                }
            }
            val cachedFile = saveToCache(
                bytes = bytes,
                originalName = name,
                extension = extensionForMimeType(mimeType)
            )

            emit(AnalyzerProgress.Completed(
                result = ImageProcessingResult(
                    base64 = "data:$mimeType;base64,$base64",
                    mimeType = mimeType,
                    originalSize = originalSize,
                    processedSize = bytes.size.toLong(),
                    originalWidth = width,
                    originalHeight = height,
                    processedWidth = width,
                    processedHeight = height,
                    cachedFilePath = cachedFile?.absolutePath,
                    thumbnailBase64 = thumbnailBase64,
                )
            ))
        } catch (e: Exception) {
            val detail = e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
            emit(AnalyzerProgress.Error("${context.getString(R.string.attachment_process_failed)}: $detail"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 简化版本：直接处理图片并返回结果（不带进度）
     */
    suspend fun analyze(
        uri: Uri,
        config: ImageProcessingConfig = ImageProcessingConfig()
    ): FileAnalysisResult {
        val name = getFileName(uri)
        val mimeType = resolveMimeType(uri)
        val size = getFileSize(uri)

        if (!mimeType.startsWith("image/")) {
            return FileAnalysisResult(
                uri = uri,
                name = name,
                mimeType = mimeType,
                size = size,
                error = context.getString(R.string.attachment_unsupported_type, mimeType)
            )
        }

        return try {
            val result = processImage(uri, config)
            FileAnalysisResult(
                uri = uri,
                name = name,
                mimeType = "image/webp",
                size = size,
                base64 = result.base64
            )
        } catch (e: Exception) {
            val detail = e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
            FileAnalysisResult(
                uri = uri,
                name = name,
                mimeType = mimeType,
                size = size,
                error = "${context.getString(R.string.attachment_process_failed)}: $detail"
            )
        }
    }

    /**
     * 处理图片（内部方法）
     */
    private suspend fun processImage(
        uri: Uri,
        config: ImageProcessingConfig
    ): ImageProcessingResult {
        val originalSize = getFileSize(uri)
        val name = getFileName(uri)
        val mimeType = resolveMimeType(uri)
        val originalBitmap = decodeBitmap(uri)
            ?: return encodeOriginalImage(uri, name, mimeType, originalSize)

        try {
            return processBitmap(
                bitmap = originalBitmap,
                name = name,
                originalSize = originalSize,
                config = config,
            )
        } finally {
            // originalBitmap 由 processBitmap 内部按需 recycle,
            // 这里只兜底,processBitmap 内部已正确处理 owner 转移.
            if (!originalBitmap.isRecycled) originalBitmap.recycle()
        }
    }

    /**
     * 纯 Bitmap 入口:把任意来源的 Bitmap(截图、相册解码、相机预览帧等)按聊天附件同款
     * 管道(WebP 压缩 + 尺寸缩放 + 1MB 目标体积 + 缩略图 + 缓存)加工成 [ImageProcessingResult]。
     *
     * 调用方责任:
     * - 传入的 [bitmap] 可以是 ARGB_8888 或 RGB_565;本方法会按需缩放并 recycle 内部副本。
     * - 若调用方仍需持有 [bitmap],请先自己 copy 一份再传入。
     *
     * @param bitmap 源位图,本方法可能 recycle 它(若发生缩放)
     * @param name  用于缓存文件命名(影响 [saveToCache] 的输出路径)
     * @param originalSize 源图原始字节数,用于统计压缩率;若不可知可传 0
     * @param config 处理配置,默认与聊天附件一致(1920px / WebP 80 / 1MB 目标)
     * @param preserveOriginalDimension true = 永不缩尺寸,只压格式与品质。
     *        适用于 UI 自动化截图等"AI 需要根据像素坐标决策"的场景,避免尺寸变化引入坐标误差。
     *        默认 false,保持聊天附件管道行为(允许按 [config.maxDimension] 缩尺寸)。
     */
    suspend fun processBitmap(
        bitmap: Bitmap,
        name: String,
        originalSize: Long = 0L,
        config: ImageProcessingConfig = ImageProcessingConfig(),
        preserveOriginalDimension: Boolean = false,
    ): ImageProcessingResult = withContext(Dispatchers.IO) {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // 调整尺寸 — 仅在非 preserve 模式且超出上限时执行
        val resizedBitmap = if (!preserveOriginalDimension &&
            (originalWidth > config.maxDimension || originalHeight > config.maxDimension)
        ) {
            val scaled = scaleBitmap(bitmap, config.maxDimension)
            if (scaled !== bitmap) bitmap.recycle()
            scaled
        } else {
            bitmap
        }

        // 在 recycle 之前先读取最终尺寸
        val processedWidth = resizedBitmap.width
        val processedHeight = resizedBitmap.height

        // 转换为WebP
        var webpBytes = bitmapToWebP(resizedBitmap, config.webpQuality)

        // 压缩到目标大小 — 仅在品质可降时才降
        var currentQuality = config.webpQuality
        while (webpBytes.size > config.targetSizeBytes && currentQuality > 20) {
            currentQuality -= 10
            webpBytes = bitmapToWebP(resizedBitmap, currentQuality)
        }

        val base64 = Base64.encodeToString(webpBytes, Base64.NO_WRAP)

        // 生成缩略图
        val thumbnailBase64 = generateThumbnailBase64(resizedBitmap, THUMBNAIL_MAX_EDGE, THUMBNAIL_QUALITY)

        // 保存到缓存
        val cachedFile = saveToCache(webpBytes, name)
        val cachedFilePath = cachedFile?.absolutePath

        // 清理
        if (resizedBitmap !== bitmap) {
            resizedBitmap.recycle()
        } else if (!resizedBitmap.isRecycled) {
            resizedBitmap.recycle()
        }

        ImageProcessingResult(
            base64 = "data:image/webp;base64,$base64",
            mimeType = "image/webp",
            originalSize = originalSize,
            processedSize = webpBytes.size.toLong(),
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            processedWidth = processedWidth,
            processedHeight = processedHeight,
            cachedFilePath = cachedFilePath,
            thumbnailBase64 = thumbnailBase64,
        )
    }

    /**
     * 解码 Bitmap，使用 [inSampleSize] 控制内存上限。
     *
     * 流程:
     * 1. 第一次解码 (`inJustDecodeBounds = true`) 仅读取尺寸
     * 2. 计算 `inSampleSize`,使解码后最长边贴近 [maxDimension]
     * 3. 第二次解码生成 Bitmap
     * 4. 处理 EXIF 旋转
     */
    private fun decodeBitmap(
        uri: Uri,
        maxDimension: Int = ImageProcessingConfig.DEFAULT_MAX_DIMENSION
    ): Bitmap? {
        val (srcW, srcH) = readImageBoundsInternal(uri) ?: return null
        val sampleSize = calculateInSampleSize(srcW, srcH, maxDimension)

        decodeBitmapWithStream(uri, sampleSize)?.let { bitmap ->
            return applyExifRotationIfNeeded(uri, bitmap)
        }
        decodeBitmapWithFileDescriptor(uri, sampleSize)?.let { bitmap ->
            return applyExifRotationIfNeeded(uri, bitmap)
        }
        return decodeBitmapWithImageDecoder(uri, maxDimension)
    }

    /**
     * 按 Android 推荐方式计算 `inSampleSize`,
     * 解码器会向下取整到最近的 2 的幂次。
     */
    private fun calculateInSampleSize(srcWidth: Int, srcHeight: Int, maxDimension: Int): Int {
        if (srcWidth <= maxDimension && srcHeight <= maxDimension) return 1
        var sampleSize = 1
        val halfW = srcWidth / 2
        val halfH = srcHeight / 2
        while ((halfW / sampleSize) >= maxDimension || (halfH / sampleSize) >= maxDimension) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun readImageBounds(uri: Uri): Pair<Int, Int> {
        return readImageBoundsInternal(uri) ?: (0 to 0)
    }

    private fun readImageBoundsInternal(uri: Uri): Pair<Int, Int>? {
        val streamBounds = runCatching {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            options.outWidth to options.outHeight
        }.getOrNull()
        if (streamBounds != null && streamBounds.first > 0 && streamBounds.second > 0) {
            return streamBounds
        }

        val descriptorBounds = runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor, null, options)
                options.outWidth to options.outHeight
            }
        }.getOrNull()
        if (descriptorBounds != null && descriptorBounds.first > 0 && descriptorBounds.second > 0) {
            return descriptorBounds
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val imageDecoderBounds = runCatching {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val drawable = ImageDecoder.decodeDrawable(source) { decoder, _, _ ->
                    decoder.isMutableRequired = false
                }
                drawable.intrinsicWidth to drawable.intrinsicHeight
            }.getOrNull()
            if (imageDecoderBounds != null && imageDecoderBounds.first > 0 && imageDecoderBounds.second > 0) {
                return imageDecoderBounds
            }
        }

        return null
    }

    private fun decodeBitmapWithStream(uri: Uri, sampleSize: Int): Bitmap? {
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }
        }.getOrNull()
    }

    private fun decodeBitmapWithFileDescriptor(uri: Uri, sampleSize: Int): Bitmap? {
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        return runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor, null, decodeOptions)
            }
        }.getOrNull()
    }

    private fun decodeBitmapWithImageDecoder(uri: Uri, maxDimension: Int): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return runCatching {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
                val srcWidth = info.size.width
                val srcHeight = info.size.height
                if (srcWidth > 0 && srcHeight > 0) {
                    val longest = maxOf(srcWidth, srcHeight)
                    if (longest > maxDimension) {
                        val scale = maxDimension.toFloat() / longest.toFloat()
                        decoder.setTargetSize(
                            (srcWidth * scale).toInt().coerceAtLeast(1),
                            (srcHeight * scale).toInt().coerceAtLeast(1)
                        )
                    }
                }
            }
            if (decoded.config == Bitmap.Config.HARDWARE) {
                decoded.copy(Bitmap.Config.ARGB_8888, true).also {
                    if (it !== decoded) decoded.recycle()
                }
            } else {
                decoded
            }
        }.getOrNull()
    }

    private fun applyExifRotationIfNeeded(uri: Uri, bitmap: Bitmap): Bitmap {
        val rotation = getExifRotation(uri)
        if (rotation == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(rotation) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }

    /**
     * 获取EXIF旋转角度
     */
    private fun getExifRotation(uri: Uri): Float {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0f
            val exif = inputStream.use { ExifInterface(it) }
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: Exception) {
            0f
        }
    }

    /**
     * 按比例缩放Bitmap
     */
    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scale = if (width > height) {
            maxDimension.toFloat() / width
        } else {
            maxDimension.toFloat() / height
        }

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Bitmap转WebP字节数组
     */
    private fun bitmapToWebP(bitmap: Bitmap, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * 从已有 Bitmap 生成缩略图的 Base64 字符串。
     * 缩略图按 [maxEdge] 等比缩放，WebP 质量为 [quality]，
     * 典型体积 5-15KB，用于 ImageEntity 持久化以避免 DB 膨胀。
     *
     * @return "data:image/webp;base64,..." 格式字符串，失败返回 null
     */
    private fun generateThumbnailBase64(source: Bitmap, maxEdge: Int, quality: Int): String? {
        return try {
            val thumb = scaleBitmap(source, maxEdge)
            val bytes = bitmapToWebP(thumb, quality)
            if (thumb !== source) thumb.recycle()
            "data:image/webp;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 获取文件名
     */
    private fun getFileName(uri: Uri): String {
        val lastPathSegment = uri.lastPathSegment
        if (lastPathSegment != null) {
            val fileName = lastPathSegment.substringAfterLast("/")
            if (fileName.isNotBlank()) {
                return fileName
            }
        }

        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        cursor.getString(nameIndex) ?: "unknown"
                    } else {
                        "unknown"
                    }
                } else {
                    "unknown"
                }
            } ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * 获取文件大小
     */
    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                it.length
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 保存到缓存目录
     */
    private fun saveToCache(bytes: ByteArray, originalName: String, extension: String = "webp"): File? {
        return try {
            val cacheDir = File(context.cacheDir, "image_compressed")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val normalizedExtension = extension.trimStart('.').ifBlank { "bin" }
            val fileName = "${System.currentTimeMillis()}_${originalName.substringBeforeLast(".")}.$normalizedExtension"
            val file = File(cacheDir, fileName)
            file.writeBytes(bytes)
            file
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从缓存文件读取 base64 内容。
     * 缓存文件由 [saveToCache] 生成，位于 cacheDir/image_compressed/。
     *
     * 用途：历史消息从 DB 加载时，attachmentsJson 仅含 localPath（不含 localContent），
     * 通过此方法按需从缓存文件恢复 base64，避免 DB 存储膨胀。
     *
     * @param localPath 缓存文件绝对路径
     * @return data:image/webp;base64,... 格式字符串，文件不存在或读取失败返回 null
     */
    suspend fun readCachedBase64(localPath: String): String? {
        val file = File(localPath)
        if (!file.exists()) return null
        return try {
            val bytes = file.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:${mimeTypeFromExtension(file.extension)};base64,$base64"
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 仅生成预览缩略图,不做完整压缩+缓存,用于附件挂载时的轻量化预览。
     *
     * 与 [analyzeWithProgress] 区别:
     * - 不写缓存文件、不返回 cachedFilePath
     * - 不做 WebP 全图压缩 / Base64 编码完整图
     * - 仅按 [maxEdge] 解码并生成缩略图 base64,体积约 5-15KB
     *
     * 用途:用户在 ChatInput 选图后立即展示缩略图;真正的图像压缩留到发送时执行。
     *
     * @return data:image/webp;base64,... 格式缩略图,失败返回 null
     */
    suspend fun preparePreview(uri: Uri, maxEdge: Int = THUMBNAIL_MAX_EDGE): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap = decodeBitmap(uri, maxEdge) ?: return@withContext null
            try {
                generateThumbnailBase64(bitmap, maxEdge, THUMBNAIL_QUALITY)
            } finally {
                bitmap.recycle()
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 删除由 [saveToCache] 写入的缓存文件。
     *
     * 用途:用户取消选择 / 替换附件 / 编辑历史时,清掉过期的 image_compressed/ 缓存,避免磁盘膨胀。
     * DB 侧 ImageEntity 已通过 ForeignKey.CASCADE 自动清理,这里只负责磁盘文件。
     *
     * @return true 表示文件已不存在或成功删除,false 表示删除失败
     */
    fun deleteCachedFile(localPath: String?): Boolean {
        if (localPath.isNullOrBlank()) return false
        return try {
            val file = File(localPath)
            if (!file.exists()) true else file.delete()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 格式化文件大小
     */
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)}KB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))}MB"
        }
    }

    private fun extensionForMimeType(mimeType: String): String {
        return when (mimeType.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/heic" -> "heic"
            "image/heif" -> "heif"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> mimeType.substringAfter('/', "img")
        }
    }

    private fun mimeTypeFromExtension(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "heic" -> "image/heic"
            "heif" -> "image/heif"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
    }

    private fun resolveMimeType(uri: Uri): String {
        val contentResolverMime = runCatching {
            context.contentResolver.getType(uri)?.trim()?.takeIf {
                it.isNotBlank() && !it.endsWith("/*")
            }
        }.getOrNull()
        if (contentResolverMime != null) return contentResolverMime

        val extension = getFileName(uri).substringAfterLast('.', "")
        return mimeTypeFromExtension(extension).takeUnless { it == "application/octet-stream" }
            ?: "image/jpeg"
    }

    private fun encodeOriginalImage(
        uri: Uri,
        name: String,
        mimeType: String,
        originalSize: Long = getFileSize(uri)
    ): ImageProcessingResult {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException(context.getString(R.string.attachment_read_failed))
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val (width, height) = readImageBounds(uri)
        val previewBitmap = decodeBitmap(uri, THUMBNAIL_MAX_EDGE)
        val thumbnailBase64 = previewBitmap?.let { bitmap ->
            try {
                generateThumbnailBase64(bitmap, THUMBNAIL_MAX_EDGE, THUMBNAIL_QUALITY)
            } finally {
                bitmap.recycle()
            }
        }
        val cachedFile = saveToCache(
            bytes = bytes,
            originalName = name,
            extension = extensionForMimeType(mimeType)
        )
        return ImageProcessingResult(
            base64 = "data:$mimeType;base64,$base64",
            mimeType = mimeType,
            originalSize = originalSize,
            processedSize = bytes.size.toLong(),
            originalWidth = width,
            originalHeight = height,
            processedWidth = width,
            processedHeight = height,
            cachedFilePath = cachedFile?.absolutePath,
            thumbnailBase64 = thumbnailBase64,
        )
    }
}

/**
 * 分析器进度
 */
sealed class AnalyzerProgress {
    /** 处理步骤 */
    data class Step(
        val step: ProcessingStep,
        val detail: String? = null
    ) : AnalyzerProgress()

    /** 处理完成 */
    data class Completed(val result: ImageProcessingResult) : AnalyzerProgress()

    /** 处理错误 */
    data class Error(val message: String) : AnalyzerProgress()
}
