package com.wanbaohe.camera.watermark.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.LogoType
import com.wanbaohe.camera.watermark.domain.WatermarkMetadata
import com.wanbaohe.camera.watermark.domain.WatermarkStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 水印渲染器
 * 负责将水印绘制到图片上
 */
@Singleton
class WatermarkRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader
) {
    /**
     * 渲染带水印的图片
     * @param sourceUri 原图 Uri
     * @param metadata 水印元数据
     * @param style 水印样式
     * @return 带水印的 Bitmap
     */
    suspend fun render(
        sourceUri: Uri,
        metadata: WatermarkMetadata,
        style: WatermarkStyle
    ): Bitmap? = withContext(Dispatchers.Default) {
        try {
            // 1. 加载原图
            val sourceBitmap = loadBitmap(sourceUri) ?: return@withContext null

            // 2. 计算水印区域高度（基于图片宽度比例）
            val watermarkHeightPx = calculateWatermarkHeight(sourceBitmap.width, style)

            // 3. 创建新的画布（原图 + 水印区域）
            val resultBitmap = Bitmap.createBitmap(
                sourceBitmap.width,
                sourceBitmap.height + watermarkHeightPx,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(resultBitmap)

            // 4. 绘制原图
            canvas.drawBitmap(sourceBitmap, 0f, 0f, null)

            // 5. 绘制水印区域
            drawWatermark(
                canvas = canvas,
                metadata = metadata,
                style = style,
                left = 0f,
                top = sourceBitmap.height.toFloat(),
                width = sourceBitmap.width.toFloat(),
                height = watermarkHeightPx.toFloat()
            )

            // 6. 回收原图
            // sourceBitmap 由 Coil 管理，不要手动回收，否则会导致缓存中的 Bitmap 被回收，引发 Crash
            // if (!sourceBitmap.isRecycled) {
            //     sourceBitmap.recycle()
            // }

            resultBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 仅渲染水印预览（不含原图）
     */
    suspend fun renderPreview(
        metadata: WatermarkMetadata,
        style: WatermarkStyle,
        width: Int = 800,
        height: Int = 200
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawWatermark(canvas, metadata, style, 0f, 0f, width.toFloat(), height.toFloat())
        bitmap
    }

    /**
     * 绘制水印区域
     */
    private suspend fun drawWatermark(
        canvas: Canvas,
        metadata: WatermarkMetadata,
        style: WatermarkStyle,
        left: Float,
        top: Float,
        width: Float,
        height: Float
    ) {
        // 使用样式中的内边距，按图片宽度缩放
        val scaleFactor = width / 1080f
        val paddingH = style.paddingHorizontal * scaleFactor  // 水平内边距
        val paddingV = style.paddingVertical * scaleFactor    // 垂直内边距

        // 1. 绘制背景
        val bgPaint = Paint().apply {
            color = style.backgroundColor.toInt()
            this.style = Paint.Style.FILL
        }
        canvas.drawRect(left, top, left + width, top + height, bgPaint)

        // 2. 计算布局参数
        val contentTop = top + paddingV
        val contentBottom = top + height - paddingV
        val contentHeight = contentBottom - contentTop
        val contentLeft = left + paddingH
        val contentRight = left + width - paddingH

        // 3. 计算字体大小（基于内容高度，确保不会超出水印区域）
        // 主字体最大不超过内容高度的 40%，次要字体最大不超过 25%
        val primaryFontSize =
            (contentHeight * style.primaryFontSize / 100f).coerceAtMost(contentHeight * 0.4f)
        val secondaryFontSize =
            (contentHeight * style.secondaryFontSize / 100f).coerceAtMost(contentHeight * 0.25f)

        // 4. 计算右侧文字的实际宽度
        val rightTextWidth =
            measureRightSectionWidth(metadata, style, primaryFontSize, secondaryFontSize)

        // 5. 计算 Logo 尺寸（先计算实际宽度）
        val logoHeight = contentHeight * 0.7f
        val logoActualWidth = calculateLogoWidth(style, logoHeight)
        val logoGap = paddingH  // Logo 和分隔线之间的间距

        // 右侧文字区域：文字右边界对齐 contentRight
        val rightSectionLeft = contentRight - rightTextWidth

        // 分隔线位置
        val dividerLeft = rightSectionLeft - logoGap

        // Logo 中心位置（在分隔线左边，考虑实际宽度）
        val logoCenterX = dividerLeft - logoGap - logoActualWidth / 2

        // 6. 绘制左侧区域（相机信息 + 时间）- 占据 Logo 左边的所有空间
        val leftSectionRight = logoCenterX - logoActualWidth / 2 - logoGap
        drawLeftSection(
            canvas = canvas,
            metadata = metadata,
            style = style,
            left = contentLeft,
            top = contentTop,
            width = leftSectionRight - contentLeft,
            height = contentHeight,
            primaryFontSize = primaryFontSize,
            secondaryFontSize = secondaryFontSize
        )

        // 7. 绘制 Logo
        drawLogo(
            canvas = canvas,
            style = style,
            centerX = logoCenterX,
            centerY = contentTop + contentHeight / 2,
            size = logoHeight
        )

        // 8. 绘制分隔线
        if (style.showDivider) {
            val dividerPaint = Paint().apply {
                color = style.dividerColor.toInt()
                strokeWidth = 2f
            }
            canvas.drawLine(
                dividerLeft,
                contentTop + contentHeight * 0.1f,
                dividerLeft,
                contentBottom - contentHeight * 0.1f,
                dividerPaint
            )
        }

        // 9. 绘制右侧区域（拍摄参数 + GPS）- 文字左对齐，右边界对齐 contentRight
        drawRightSection(
            canvas = canvas,
            metadata = metadata,
            style = style,
            left = rightSectionLeft,
            top = contentTop,
            width = rightTextWidth,
            height = contentHeight,
            primaryFontSize = primaryFontSize,
            secondaryFontSize = secondaryFontSize
        )
    }

    /**
     * 测量右侧区域文字的实际宽度
     */
    private fun measureRightSectionWidth(
        metadata: WatermarkMetadata,
        style: WatermarkStyle,
        primaryFontSize: Float,
        secondaryFontSize: Float
    ): Float {
        // 测量拍摄参数文字宽度
        val params = style.customContent.getTopRightText(metadata)
        val paramsPaint = Paint().apply {
            textSize = primaryFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paramsWidth = if (params.isNotEmpty()) paramsPaint.measureText(params) else 0f

        // 测量 GPS/底部文字宽度
        val bottomRightText = style.customContent.getBottomRightText(metadata)
        val gpsPaint = Paint().apply {
            textSize = secondaryFontSize
        }
        val gpsWidth =
            if (bottomRightText.isNotEmpty()) gpsPaint.measureText(bottomRightText) else 0f

        // 返回两者中的最大宽度
        return maxOf(paramsWidth, gpsWidth)
    }

    /**
     * 计算 Logo 的实际宽度（基于高度和宽高比）
     */
    private fun calculateLogoWidth(style: WatermarkStyle, logoHeight: Float): Float {
        return when (style.logoType) {
            LogoType.NONE -> 0f
            LogoType.CUSTOM -> {
                // 自定义 Logo 暂时假设为正方形，实际绘制时会自适应
                logoHeight
            }

            else -> {
                // 预置 Logo，获取对应资源的宽高比
                val resId = when (style.logoType) {
                    LogoType.LEICA -> R.drawable.leica_logo
                    LogoType.WANBAOHE -> com.shifenmiao.core.R.drawable.logo
                    LogoType.APPLE -> R.drawable.apple_logo
                    LogoType.GOOGLE -> R.drawable.google_logo
                    LogoType.HUAWEI -> R.drawable.huawei_logo
                    LogoType.OPPO -> R.drawable.oppo_logo
                    LogoType.VIVO -> R.drawable.vivo_logo
                    LogoType.XIAOMI -> R.drawable.xiaomi_logo
                    else -> return logoHeight
                }
                try {
                    val drawable = context.getDrawable(resId)
                    if (drawable != null && drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                        val aspectRatio =
                            drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight.toFloat()
                        logoHeight * aspectRatio
                    } else {
                        logoHeight
                    }
                } catch (e: Exception) {
                    logoHeight
                }
            }
        }
    }

    /**
     * 绘制左侧区域：相机信息 + 拍摄时间
     */
    private fun drawLeftSection(
        canvas: Canvas,
        metadata: WatermarkMetadata,
        style: WatermarkStyle,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        primaryFontSize: Float,
        secondaryFontSize: Float
    ) {
        // 主标题：相机信息（使用自定义内容或 EXIF）
        val cameraInfo = style.customContent.getTopLeftText(metadata)
        if (cameraInfo.isNotEmpty()) {
            val titlePaint = Paint().apply {
                color = style.primaryTextColor.toInt()
                textSize = primaryFontSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(cameraInfo, left, top + height * 0.4f, titlePaint)
        }

        // 副标题：拍摄时间或自定义文字（使用自定义内容或 EXIF）
        val bottomLeftText = style.customContent.getBottomLeftText(metadata)
        if (bottomLeftText.isNotEmpty()) {
            val subtitlePaint = Paint().apply {
                color = style.secondaryTextColor.toInt()
                textSize = secondaryFontSize
                isAntiAlias = true
            }
            canvas.drawText(bottomLeftText, left, top + height * 0.75f, subtitlePaint)
        }
    }

    /**
     * 绘制 Logo
     */
    private suspend fun drawLogo(
        canvas: Canvas,
        style: WatermarkStyle,
        centerX: Float,
        centerY: Float,
        size: Float
    ) {
        when (style.logoType) {
            LogoType.LEICA -> drawResourceLogo(
                canvas,
                R.drawable.leica_logo,
                centerX,
                centerY,
                size
            )

            LogoType.WANBAOHE -> drawResourceLogo(
                canvas,
                com.shifenmiao.core.R.drawable.logo,
                centerX,
                centerY,
                size
            )

            LogoType.APPLE -> drawResourceLogo(
                canvas,
                R.drawable.apple_logo,
                centerX,
                centerY,
                size
            )

            LogoType.GOOGLE -> drawResourceLogo(
                canvas,
                R.drawable.google_logo,
                centerX,
                centerY,
                size
            )

            LogoType.HUAWEI -> drawResourceLogo(
                canvas,
                R.drawable.huawei_logo,
                centerX,
                centerY,
                size
            )

            LogoType.OPPO -> drawResourceLogo(canvas, R.drawable.oppo_logo, centerX, centerY, size)
            LogoType.VIVO -> drawResourceLogo(canvas, R.drawable.vivo_logo, centerX, centerY, size)
            LogoType.XIAOMI -> drawResourceLogo(
                canvas,
                R.drawable.xiaomi_logo,
                centerX,
                centerY,
                size
            )

            LogoType.ONEPLUS -> drawResourceLogo(
                canvas,
                R.drawable.oneplus_logo,
                centerX,
                centerY,
                size
            )

            LogoType.CUSTOM -> {
                style.customLogoPath?.let { path ->
                    drawCustomLogo(canvas, path, centerX, centerY, size)
                }
            }

            LogoType.NONE -> { /* 不绘制 */
            }
        }
    }

    /**
     * 从资源文件绘制 Logo（保持宽高比）
     */
    private fun drawResourceLogo(
        canvas: Canvas,
        resId: Int,
        centerX: Float,
        centerY: Float,
        size: Float
    ) {
        try {
            val drawable = context.getDrawable(resId) ?: return
            val intrinsicWidth = drawable.intrinsicWidth
            val intrinsicHeight = drawable.intrinsicHeight

            if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
                // 无法获取原始尺寸，使用正方形
                val logoBitmap = drawable.toBitmap(size.toInt(), size.toInt())
                val left = centerX - size / 2
                val top = centerY - size / 2
                canvas.drawBitmap(logoBitmap, left, top, null)
                return
            }

            // 计算缩放后的尺寸，保持宽高比，高度适应 size
            val aspectRatio = intrinsicWidth.toFloat() / intrinsicHeight.toFloat()
            val targetHeight = size
            val targetWidth = targetHeight * aspectRatio

            val logoBitmap = drawable.toBitmap(targetWidth.toInt(), targetHeight.toInt())
            val left = centerX - targetWidth / 2
            val top = centerY - targetHeight / 2
            canvas.drawBitmap(logoBitmap, left, top, null)
        } catch (e: Exception) {
            // Logo 加载失败，回退到文字 Logo
            drawTextLogo(canvas, "Logo", centerX, centerY, size, Color.GRAY)
        }
    }

    /**
     * 绘制自定义 Logo（用户选择的图片，保持宽高比）
     */
    private suspend fun drawCustomLogo(
        canvas: Canvas,
        logoPath: String,
        centerX: Float,
        centerY: Float,
        size: Float
    ) {
        try {
            val logoBitmap = loadBitmap(Uri.parse(logoPath)) ?: return
            val originalWidth = logoBitmap.width
            val originalHeight = logoBitmap.height

            // 计算缩放后的尺寸，保持宽高比，高度适应 size
            val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
            val targetHeight = size
            val targetWidth = targetHeight * aspectRatio

            val scaledBitmap = Bitmap.createScaledBitmap(
                logoBitmap,
                targetWidth.toInt(),
                targetHeight.toInt(),
                true
            )
            val left = centerX - targetWidth / 2
            val top = centerY - targetHeight / 2
            canvas.drawBitmap(scaledBitmap, left, top, null)
        } catch (e: Exception) {
            // 自定义 Logo 加载失败
        }
    }

    /**
     * 绘制文字 Logo（备用）
     */
    private fun drawTextLogo(
        canvas: Canvas,
        text: String,
        centerX: Float,
        centerY: Float,
        size: Float,
        color: Int
    ) {
        val textPaint = Paint().apply {
            this.color = color
            textSize = size * 0.35f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(text, centerX, centerY + size * 0.12f, textPaint)
    }

    /**
     * 绘制右侧区域：拍摄参数 + GPS（文字左对齐，整体靠右）
     */
    private fun drawRightSection(
        canvas: Canvas,
        metadata: WatermarkMetadata,
        style: WatermarkStyle,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        primaryFontSize: Float,
        secondaryFontSize: Float
    ) {
        // 主标题：拍摄参数（使用自定义内容或 EXIF）
        val params = style.customContent.getTopRightText(metadata)
        if (params.isNotEmpty()) {
            val paramsPaint = Paint().apply {
                color = style.primaryTextColor.toInt()
                textSize = primaryFontSize
                textAlign = Paint.Align.LEFT  // 左对齐
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(params, left, top + height * 0.4f, paramsPaint)
        }

        // 副标题：GPS 坐标或自定义文字（使用自定义内容或 EXIF）
        val bottomRightText = style.customContent.getBottomRightText(metadata)
        if (bottomRightText.isNotEmpty()) {
            val gpsPaint = Paint().apply {
                color = style.secondaryTextColor.toInt()
                textSize = secondaryFontSize
                textAlign = Paint.Align.LEFT  // 左对齐
                isAntiAlias = true
            }
            canvas.drawText(bottomRightText, left, top + height * 0.75f, gpsPaint)
        }
    }

    /**
     * 计算水印区域高度（基于图片宽度和样式设置）
     */
    private fun calculateWatermarkHeight(imageWidth: Int, style: WatermarkStyle): Int {
        // 使用样式中的水印高度（dp），按图片宽度缩放
        // 假设标准屏幕宽度为 1080px，进行比例换算
        val scaleFactor = imageWidth / 1080f
        val scaledHeight = style.watermarkHeight * scaleFactor
        // 限制在合理范围内（支持更高的水印）
        return scaledHeight.toInt().coerceIn(50, 1200)
    }

    /**
     * 加载 Bitmap
     */
    private suspend fun loadBitmap(uri: Uri): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .build()
        val result = imageLoader.execute(request)
        return if (result is SuccessResult) {
            result.image.toBitmap()
        } else {
            null
        }
    }
}

