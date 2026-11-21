/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.domain.model

import com.shifenmiao.model.editor.LayerTransform
import com.t8rin.imagetoolbox.core.domain.image.model.BlendingMode
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import java.util.UUID

data class WatermarkElement(
    val id: String = UUID.randomUUID().toString(),
    val content: WatermarkContent = WatermarkContent.Text(),
    val transform: LayerTransform = LayerTransform(
        centerX = 0.5f,
        centerY = 0.85f,
        alpha = 0.75f,
    ),
    val style: WatermarkStyle = WatermarkStyle(),
    val layout: WatermarkLayout = WatermarkLayout(),
    val overlayMode: BlendingMode = BlendingMode.SrcOver,
    val anchor: WatermarkAnchor = WatermarkAnchor.PHOTO,
    val horizontalAlignment: WatermarkHorizontalAlignment = WatermarkHorizontalAlignment.CENTER,
)

enum class WatermarkHorizontalAlignment { CENTER, START }

data class WatermarkStyle(
    val color: Int = -16777216,
    val backgroundColor: Int = 0,
    val sizeRatio: Float = 0.08f,
    val maxWidthRatio: Float = 0.9f,
    val font: FontType? = null,
    val bold: Boolean = false,
)

data class WatermarkLayout(
    val isRepeated: Boolean = false,
    val repeatSpacing: Float = 1.8f,
)

fun WatermarkElement.isTextBased(): Boolean =
    content !is WatermarkContent.Image && content !is WatermarkContent.Divider

fun defaultWatermarkElement(content: WatermarkContent): WatermarkElement {
    val isStamp = content is WatermarkContent.Stamp
    val isDynamic = content is WatermarkContent.Timestamp ||
        content is WatermarkContent.Altitude ||
        content is WatermarkContent.Weather ||
        content is WatermarkContent.Coordinates ||
        content is WatermarkContent.CameraParams
    return WatermarkElement(
        content = content,
        transform = LayerTransform(
            centerX = if (isStamp) 0.82f else 0.5f,
            centerY = if (isStamp || isDynamic) 0.9f else 0.85f,
            rotation = 0f,
            alpha = when {
                content is WatermarkContent.Image -> 0.8f
                isDynamic -> 0.9f
                else -> 0.75f
            },
        ),
        style = WatermarkStyle(
            color = if (content is WatermarkContent.Text) -16777216 else -1,
            backgroundColor = when {
                isStamp -> -16777216
                isDynamic -> 0x66000000
                else -> 0
            },
            sizeRatio = when {
                isStamp -> 0.055f
                isDynamic -> 0.045f
                else -> 0.08f
            },
        ),
        layout = WatermarkLayout(),
    )
}
