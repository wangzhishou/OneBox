/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.data.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.privacy.redaction.domain.model.PrivacyDocument
import com.wanbaohe.privacy.redaction.domain.model.PrivacyMask
import com.wanbaohe.privacy.redaction.domain.model.PrivacyRegion
import com.wanbaohe.privacy.redaction.domain.render.PrivacyRedactionRenderer
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

internal class BitmapPrivacyRedactionRenderer @Inject constructor(
    dispatchersHolder: DispatchersHolder,
) : PrivacyRedactionRenderer<Bitmap>, DispatchersHolder by dispatchersHolder {
    override suspend fun render(image: Bitmap, document: PrivacyDocument): Bitmap =
        withContext(defaultDispatcher) {
            if (document.enabledRegions.isEmpty()) return@withContext image
            val output = image.copy(Bitmap.Config.ARGB_8888, true) ?: return@withContext image
            val canvas = Canvas(output)
            document.enabledRegions.forEach { region ->
                val sourceRect = region.pixelRect(image.width, image.height) ?: return@forEach
                when (document.mask) {
                    PrivacyMask.MOSAIC -> drawMosaic(canvas, image, sourceRect, document.strength)
                    PrivacyMask.SOLID -> canvas.drawRect(
                        RectF(sourceRect),
                        solidPaint,
                    )
                }
            }
            output
        }

    private fun drawMosaic(canvas: Canvas, source: Bitmap, rect: Rect, strength: Float) {
        val width = rect.width()
        val height = rect.height()
        val targetPixelHeight = (18f - strength.coerceIn(0f, 1f) * 14f)
            .roundToInt().coerceIn(4, 18)
        val pixelHeight = targetPixelHeight
            .coerceAtMost((height / 2).coerceAtLeast(1))
            .coerceIn(1, 18)
        val pixelWidth = (width * pixelHeight.toFloat() / height)
            .roundToInt()
            .coerceAtMost((width / 2).coerceAtLeast(1))
            .coerceIn(1, 512)
        val crop = Bitmap.createBitmap(source, rect.left, rect.top, width, height)
        val small = Bitmap.createScaledBitmap(crop, pixelWidth, pixelHeight, false)
        val mosaic = Bitmap.createScaledBitmap(small, width, height, false)
        canvas.drawBitmap(mosaic, rect.left.toFloat(), rect.top.toFloat(), null)
        if (mosaic !== small && mosaic !== crop && mosaic !== source) mosaic.recycle()
        if (small !== crop && small !== source) small.recycle()
        if (crop !== source) crop.recycle()
    }

    private fun PrivacyRegion.pixelRect(width: Int, height: Int): Rect? {
        val left = floor(bounds.left * width).toInt().coerceIn(0, width - 1)
        val top = floor(bounds.top * height).toInt().coerceIn(0, height - 1)
        val right = ceil(bounds.right * width).toInt().coerceIn(left + 1, width)
        val bottom = ceil(bounds.bottom * height).toInt().coerceIn(top + 1, height)
        return Rect(left, top, right, bottom).takeIf { it.width() > 0 && it.height() > 0 }
    }

    private val solidPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF202020.toInt()
        style = Paint.Style.FILL
    }

}
