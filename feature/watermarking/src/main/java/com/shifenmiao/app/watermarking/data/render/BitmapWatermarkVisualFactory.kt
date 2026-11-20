/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.data.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.utils.toTypeface
import com.shifenmiao.app.watermarking.domain.model.ResolvedWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkAnchor
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkHorizontalAlignment
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisual
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisualFactory
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class BitmapWatermarkVisualFactory @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
    @ApplicationContext private val context: Context,
) : WatermarkVisualFactory<Bitmap> {
    companion object {
        const val BUILTIN_SCHEME = "builtin://"
    }

    override suspend fun create(
        resolved: ResolvedWatermarkElement,
        canvasWidth: Int,
        canvasHeight: Int,
        sizeBasisWidth: Int,
    ): WatermarkVisual<Bitmap>? {
        if (canvasWidth <= 0 || canvasHeight <= 0) return null
        if (resolved.element.content is WatermarkContent.Divider) {
            return createDividerVisual(resolved, sizeBasisWidth)
        }
        resolved.text?.let { text ->
            // 动态内容缺数据时用空串表达「省略」，不能画出空白或 "--"。
            if (text.isBlank()) return null
            return createTextVisual(resolved, text, canvasWidth, sizeBasisWidth)
        }
        val imageData = resolved.imageData ?: return null
        val imageContent = resolved.element.content as? WatermarkContent.Image ?: return null
        val targetSize = (sizeBasisWidth * imageContent.size.coerceIn(0.01f, 1f))
            .roundToInt().coerceIn(1, canvasWidth)
        if (imageData.isBlank()) return imagePlaceholder(resolved.element.id, targetSize)
        // 内置资源或用户图片加载失败都出占位块，不静默把图层丢掉。
        val source = if (imageData.startsWith(BUILTIN_SCHEME)) {
            loadBuiltinDrawable(imageData, targetSize)
        } else {
            imageGetter.getImage(
                data = imageData,
                size = IntegerSize(targetSize, targetSize),
            )
        } ?: return imagePlaceholder(resolved.element.id, targetSize)
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scale = min(
            targetSize / source.width.toFloat(),
            targetSize / source.height.toFloat(),
        )
        val width = (source.width * scale).roundToInt().coerceAtLeast(1)
        val height = (source.height * scale).roundToInt().coerceAtLeast(1)
        val left = (targetSize - width) / 2f
        val top = (targetSize - height) / 2f
        canvas.drawBitmap(
            source,
            null,
            RectF(left, top, left + width, top + height),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
        return WatermarkVisual(resolved.element.id, bitmap, targetSize, targetSize)
    }

    /** 内置 vector logo 显式解析成位图，不依赖 Coil 的 scheme 支持。 */
    private fun loadBuiltinDrawable(imageData: String, targetSize: Int): Bitmap? {
        val resId = imageData.removePrefix(BUILTIN_SCHEME).toIntOrNull() ?: return null
        val drawable = runCatching { ContextCompat.getDrawable(context, resId) }.getOrNull() ?: return null
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val boundsDrawable = drawable.mutate()
        boundsDrawable.setBounds(0, 0, targetSize, targetSize)
        boundsDrawable.draw(Canvas(bitmap))
        return bitmap
    }

    private fun createDividerVisual(
        resolved: ResolvedWatermarkElement,
        sizeBasisWidth: Int,
    ): WatermarkVisual<Bitmap> {
        val content = resolved.element.content as WatermarkContent.Divider
        val basis = sizeBasisWidth.coerceAtLeast(1)
        val height = (basis * content.heightRatio).roundToInt().coerceAtLeast(2)
        val thickness = (basis * content.thicknessRatio).roundToInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(thickness, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawRect(
            0f,
            0f,
            thickness.toFloat(),
            height.toFloat(),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = resolved.element.style.color },
        )
        return WatermarkVisual(resolved.element.id, bitmap, thickness, height)
    }

    /** 还没选图时也要有一块可点的占位，否则图层加出来却看不见、拖不动。 */
    private fun imagePlaceholder(id: String, size: Int): WatermarkVisual<Bitmap> {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val inset = size * 0.08f
        val corner = size * 0.12f
        val bounds = RectF(inset, inset, size - inset, size - inset)
        canvas.drawRoundRect(
            bounds,
            corner,
            corner,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 48
                style = Paint.Style.FILL
            },
        )
        canvas.drawRoundRect(
            bounds,
            corner,
            corner,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                alpha = 200
                style = Paint.Style.STROKE
                strokeWidth = (size * 0.035f).coerceAtLeast(2f)
            },
        )
        return WatermarkVisual(id, bitmap, size, size)
    }

    private fun createTextVisual(
        resolved: ResolvedWatermarkElement,
        text: String,
        canvasWidth: Int,
        sizeBasisWidth: Int,
    ): WatermarkVisual<Bitmap> {
        val element = resolved.element
        val textSize = (sizeBasisWidth * element.style.sizeRatio.coerceIn(0.005f, 1f))
            .coerceAtLeast(1f)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.style.color
            this.textSize = textSize
            textAlign = Paint.Align.LEFT
            element.style.font.toTypeface()?.let(::setTypeface)
        }
        val singleBandLine = element.anchor == WatermarkAnchor.BAND && !text.contains('\n')
        val positionedMaxRatio = when {
            element.anchor != WatermarkAnchor.BAND || element.layout.isRepeated -> 0.9f
            element.horizontalAlignment == WatermarkHorizontalAlignment.START -> {
                (1f - element.transform.centerX).coerceIn(0.05f, 1f)
            }
            else -> {
                minOf(element.transform.centerX, 1f - element.transform.centerX)
                    .coerceIn(0.05f, 0.5f) * 2f
            }
        }
        val maxWidthRatio = min(
            element.style.maxWidthRatio.coerceIn(0.05f, 1f),
            positionedMaxRatio,
        )
        val maxTextWidth = (canvasWidth * maxWidthRatio).roundToInt().coerceAtLeast(1)
        var desiredWidth = text.lines().maxOf { line -> paint.measureText(line) }
        if (singleBandLine && desiredWidth > maxTextWidth) {
            paint.textSize = (paint.textSize * maxTextWidth * 0.98f / desiredWidth).coerceAtLeast(1f)
            desiredWidth = paint.measureText(text)
        }
        val padding = if (element.style.backgroundColor != Color.TRANSPARENT) paint.textSize * 0.3f else 0f
        if (singleBandLine) {
            val metrics = paint.fontMetrics
            val width = ceil(desiredWidth + padding * 2).toInt().coerceAtLeast(1)
            val height = ceil(metrics.descent - metrics.ascent + padding * 2).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            if (element.style.backgroundColor != Color.TRANSPARENT) {
                canvas.drawRoundRect(
                    RectF(0f, 0f, width.toFloat(), height.toFloat()),
                    paint.textSize * 0.25f,
                    paint.textSize * 0.25f,
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = element.style.backgroundColor
                        style = Paint.Style.FILL
                    },
                )
            }
            canvas.drawText(text, padding, padding - metrics.ascent, paint)
            return WatermarkVisual(element.id, bitmap, width, height)
        }
        val textWidth = ceil(min(desiredWidth, maxTextWidth.toFloat())).toInt().coerceAtLeast(1)
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, textWidth)
            .setAlignment(
                if (element.horizontalAlignment == WatermarkHorizontalAlignment.START) {
                    Layout.Alignment.ALIGN_NORMAL
                } else {
                    Layout.Alignment.ALIGN_CENTER
                }
            )
            .setIncludePad(false)
            .build()
        val width = ceil(textWidth + padding * 2).toInt().coerceAtLeast(1)
        val height = ceil(layout.height + padding * 2).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (element.style.backgroundColor != Color.TRANSPARENT) {
            canvas.drawRoundRect(
                RectF(0f, 0f, width.toFloat(), height.toFloat()),
                paint.textSize * 0.25f,
                paint.textSize * 0.25f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = element.style.backgroundColor
                    style = Paint.Style.FILL
                },
            )
        }
        canvas.translate(padding + textWidth / 2f, padding)
        canvas.save()
        canvas.translate(-textWidth / 2f, 0f)
        layout.draw(canvas)
        canvas.restore()
        return WatermarkVisual(element.id, bitmap, width, height)
    }
}
