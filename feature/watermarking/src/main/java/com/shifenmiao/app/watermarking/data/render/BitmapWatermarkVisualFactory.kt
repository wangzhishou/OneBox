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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.utils.toTypeface
import com.shifenmiao.app.watermarking.domain.model.ResolvedWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisual
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisualFactory
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class BitmapWatermarkVisualFactory @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
) : WatermarkVisualFactory<Bitmap> {
    override suspend fun create(
        resolved: ResolvedWatermarkElement,
        canvasWidth: Int,
        canvasHeight: Int,
    ): WatermarkVisual<Bitmap>? {
        if (canvasWidth <= 0 || canvasHeight <= 0) return null
        resolved.text?.let { text ->
            return createTextVisual(resolved, text, canvasWidth)
        }
        val imageData = resolved.imageData ?: return null
        val imageContent = resolved.element.content as? WatermarkContent.Image ?: return null
        val targetSize = (canvasWidth * imageContent.size.coerceIn(0.01f, 1f))
            .roundToInt().coerceIn(1, canvasWidth)
        val source = imageGetter.getImage(
            data = imageData,
            size = IntegerSize(targetSize, targetSize),
        ) ?: return null
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

    private fun createTextVisual(
        resolved: ResolvedWatermarkElement,
        text: String,
        canvasWidth: Int,
    ): WatermarkVisual<Bitmap> {
        val element = resolved.element
        val textSize = (canvasWidth * element.style.sizeRatio.coerceIn(0.005f, 1f))
            .coerceAtLeast(1f)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.style.color
            this.textSize = textSize
            textAlign = Paint.Align.LEFT
            element.style.font.toTypeface()?.let(::setTypeface)
        }
        val maxTextWidth = (canvasWidth * 0.9f).roundToInt().coerceAtLeast(1)
        val desiredWidth = text.lines().maxOf { line -> paint.measureText(line) }
        val textWidth = ceil(min(desiredWidth, maxTextWidth.toFloat())).toInt().coerceAtLeast(1)
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(false)
            .build()
        val padding = if (element.style.backgroundColor != Color.TRANSPARENT) textSize * 0.3f else 0f
        val width = ceil(textWidth + padding * 2).toInt().coerceAtLeast(1)
        val height = ceil(layout.height + padding * 2).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (element.style.backgroundColor != Color.TRANSPARENT) {
            canvas.drawRoundRect(
                RectF(0f, 0f, width.toFloat(), height.toFloat()),
                textSize * 0.25f,
                textSize * 0.25f,
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
