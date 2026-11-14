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
import android.os.Build
import com.t8rin.imagetoolbox.core.data.image.utils.toAndroidBlendMode
import com.t8rin.imagetoolbox.core.data.image.utils.toPorterDuffMode
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisual
import kotlin.math.roundToInt

object WatermarkDrawEngine {
    fun drawSingle(
        canvas: Canvas,
        visual: WatermarkVisual<Bitmap>,
        element: WatermarkElement,
    ) {
        val transform = element.transform
        canvas.save()
        canvas.translate(
            transform.centerX * canvas.width,
            transform.centerY * canvas.height,
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

    fun drawRepeated(
        canvas: Canvas,
        visual: WatermarkVisual<Bitmap>,
        element: WatermarkElement,
    ) {
        val transform = element.transform
        val spacing = element.layout.repeatSpacing.coerceIn(1.1f, 6f)
        val stepX = (visual.width * spacing).coerceAtLeast(canvas.width / 20f)
        val stepY = (visual.height * spacing).coerceAtLeast(canvas.height / 20f)
        if (stepX <= 0f || stepY <= 0f) return
        val centerX = transform.centerX * canvas.width
        val centerY = transform.centerY * canvas.height
        val paint = paint(element)
        canvas.save()
        canvas.rotate(transform.rotation, centerX, centerY)
        canvas.scale(transform.scale, transform.scale, centerX, centerY)
        val offsetX = centerX % stepX - stepX
        val offsetY = centerY % stepY - stepY
        var row = 0
        var y = offsetY - canvas.height / 2f
        while (y < canvas.height * 1.5f + stepY) {
            val rowOffset = if (row % 2 == 0) 0f else stepX / 2f
            var x = offsetX - canvas.width / 2f - stepX + rowOffset
            while (x < canvas.width * 1.5f + stepX) {
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
