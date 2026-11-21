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
import android.graphics.Paint
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Build
import com.t8rin.imagetoolbox.core.data.image.utils.toAndroidBlendMode
import com.t8rin.imagetoolbox.core.data.image.utils.toPorterDuffMode
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkHorizontalAlignment
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisual
import kotlin.math.roundToInt

object WatermarkDrawEngine {
    /**
     * [rect] 是元素锚定空间在输出画布上的像素矩形。
     * 传 null 时用整张画布，保持历史行为；BAND 元素传白带 rect，
     * centerX/centerY 只在该 rect 内归一化。
     */
    fun drawSingle(
        canvas: Canvas,
        visual: WatermarkVisual<Bitmap>,
        element: WatermarkElement,
        rect: RectF? = null,
    ) {
        val transform = element.transform
        val left = rect?.left ?: 0f
        val top = rect?.top ?: 0f
        val width = rect?.width() ?: canvas.width.toFloat()
        val height = rect?.height() ?: canvas.height.toFloat()
        val horizontalCenterOffset = if (element.horizontalAlignment == WatermarkHorizontalAlignment.START) {
            visual.width / 2f
        } else {
            0f
        }
        canvas.save()
        canvas.translate(
            left + transform.centerX * width + horizontalCenterOffset,
            top + transform.centerY * height,
        )
        canvas.rotate(transform.rotation)
        canvas.scale(
            transform.scale * if (transform.flipHorizontal) -1f else 1f,
            transform.scale * if (transform.flipVertical) -1f else 1f,
        )
        canvas.drawBitmap(
            visual.image,
            -visual.width / 2f,
            -visual.height / 2f,
            paint(element),
        )
        canvas.restore()
    }

    fun drawContent(
        canvas: Canvas,
        visual: WatermarkVisual<Bitmap>,
        element: WatermarkElement,
    ) {
        canvas.drawBitmap(
            visual.image,
            0f,
            0f,
            paint(element).apply { alpha = 255 },
        )
    }

    fun drawRepeated(
        canvas: Canvas,
        visual: WatermarkVisual<Bitmap>,
        element: WatermarkElement,
        rect: RectF? = null,
    ) {
        val transform = element.transform
        val left = rect?.left ?: 0f
        val top = rect?.top ?: 0f
        val width = rect?.width() ?: canvas.width.toFloat()
        val height = rect?.height() ?: canvas.height.toFloat()
        val spacing = element.layout.repeatSpacing.coerceIn(1.1f, 6f)
        val stepX = (visual.width * spacing).coerceAtLeast(width / 20f)
        val stepY = (visual.height * spacing).coerceAtLeast(height / 20f)
        if (stepX <= 0f || stepY <= 0f) return
        val centerX = left + transform.centerX * width
        val centerY = top + transform.centerY * height
        val paint = paint(element)
        canvas.save()
        rect?.let(canvas::clipRect)
        canvas.rotate(transform.rotation, centerX, centerY)
        canvas.scale(transform.scale, transform.scale, centerX, centerY)
        val offsetX = centerX % stepX - stepX
        val offsetY = centerY % stepY - stepY
        var row = 0
        var y = offsetY - height / 2f
        while (y < top + height * 1.5f + stepY) {
            val rowOffset = if (row % 2 == 0) 0f else stepX / 2f
            var x = offsetX - width / 2f - stepX + rowOffset
            while (x < left + width * 1.5f + stepX) {
                canvas.drawBitmap(visual.image, x, y, paint)
                x += stepX
            }
            y += stepY
            row++
        }
        canvas.restore()
    }

    private fun paint(element: WatermarkElement): Paint = Paint(
        Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG
    ).apply {
        alpha = (element.transform.alpha.coerceIn(0f, 1f) * 255).roundToInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            blendMode = element.overlayMode.toAndroidBlendMode()
        } else {
            xfermode = PorterDuffXfermode(element.overlayMode.toPorterDuffMode())
        }
    }
}
