/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 * Licensed under the Apache License, Version 2.0.
 */

package com.shifenmiao.app.watermarking.domain.model

import kotlin.math.roundToInt

enum class PhotoMountStyle(
    val color: Int,
    val defaultWidth: Float,
    val bottomWeight: Float,
    val keylineColor: Int? = null,
) {
    GALLERY(0xFFFAF8F3.toInt(), 0.065f, 1.32f),
    DARKROOM(0xFF242425.toInt(), 0.038f, 1f, 0xFF60605D.toInt()),
    INSTANT(0xFFF6F1E6.toInt(), 0.07f, 2.65f),
}

enum class PhotoMountRatio(val width: Int, val height: Int) {
    NATURAL(0, 0),
    SQUARE(1, 1),
    PORTRAIT(4, 5),
    STORY(9, 16),
    LANDSCAPE(16, 9),
}

data class PhotoMount(
    val style: PhotoMountStyle = PhotoMountStyle.GALLERY,
    val widthRatio: Float = style.defaultWidth,
    val ratio: PhotoMountRatio = PhotoMountRatio.NATURAL,
) {
    companion object {
        const val MIN_WIDTH = 0.02f
        const val MAX_WIDTH = 0.15f
    }
}

data class WatermarkCanvasLayout(
    val width: Int,
    val height: Int,
    val contentLeft: Int = 0,
    val contentTop: Int = 0,
    val bandHeight: Int = 0,
    val keylineWidth: Int = 0,
)

/** 画布只向外扩，照片与已有信息栏保持原尺寸。 */
fun watermarkCanvasLayout(
    photoWidth: Int,
    photoHeight: Int,
    frame: WatermarkFrame,
    mount: PhotoMount?,
): WatermarkCanvasLayout {
    val band = (frame as? WatermarkFrame.BottomBand)?.bandHeightFor(photoWidth) ?: 0
    val contentHeight = photoHeight + band
    if (mount == null) return WatermarkCanvasLayout(photoWidth, contentHeight, bandHeight = band)
    val basis = minOf(photoWidth, photoHeight)
    val widthRatio = mount.widthRatio.takeIf { it.isFinite() }
        ?.coerceIn(PhotoMount.MIN_WIDTH, PhotoMount.MAX_WIDTH) ?: mount.style.defaultWidth
    val edge = (basis * widthRatio).roundToInt().coerceAtLeast(1)
    val bottom = (edge * mount.style.bottomWeight).roundToInt()
    val naturalWidth = photoWidth + edge * 2
    val naturalHeight = contentHeight + edge + bottom
    val ratio = mount.ratio
    val units = if (ratio == PhotoMountRatio.NATURAL) 0 else maxOf(
        ceilDiv(naturalWidth, ratio.width),
        ceilDiv(naturalHeight, ratio.height),
    )
    val width = if (units == 0) naturalWidth else units * ratio.width
    val height = if (units == 0) naturalHeight else units * ratio.height
    return WatermarkCanvasLayout(
        width = width,
        height = height,
        contentLeft = edge + (width - naturalWidth) / 2,
        contentTop = edge + (height - naturalHeight) / 2,
        bandHeight = band,
        keylineWidth = if (mount.style.keylineColor == null) 0 else {
            (basis * 0.0015f).roundToInt().coerceIn(1, edge)
        },
    )
}

private fun ceilDiv(value: Int, divisor: Int): Int = (value - 1) / divisor + 1
