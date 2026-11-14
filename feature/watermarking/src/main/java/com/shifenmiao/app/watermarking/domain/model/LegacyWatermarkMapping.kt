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
import com.t8rin.imagetoolbox.core.domain.model.Position
import com.shifenmiao.app.watermarking.domain.WatermarkParams
import com.shifenmiao.app.watermarking.domain.WatermarkingType
import com.shifenmiao.app.watermarking.domain.digitalParams

fun WatermarkParams.toDocument(): WatermarkDocument {
    val type = watermarkingType
    val content = when (type) {
        is WatermarkingType.Text -> WatermarkContent.Text(type.text)
        is WatermarkingType.Image -> WatermarkContent.Image(type.imageData.toString(), type.size)
        is WatermarkingType.Stamp.Text -> WatermarkContent.Stamp(type.text)
        is WatermarkingType.Stamp.Time -> WatermarkContent.Timestamp(type.format, usePhotoTime = false)
    }
    val textParams = when (type) {
        is WatermarkingType.Text -> type.params
        is WatermarkingType.Stamp -> type.params
        else -> null
    }
    val position = (type as? WatermarkingType.Stamp)?.position
    return WatermarkDocument(
        elements = listOf(
            WatermarkElement(
                content = content,
                transform = LayerTransform(
                    centerX = position?.x() ?: positionX.coerceIn(0f, 1f),
                    centerY = position?.y() ?: positionY.coerceIn(0f, 1f),
                    rotation = rotation.toFloat(),
                    alpha = alpha,
                ),
                style = WatermarkStyle(
                    color = textParams?.color ?: -1,
                    backgroundColor = textParams?.backgroundColor ?: 0,
                    sizeRatio = textParams?.size ?: 0.08f,
                    font = textParams?.font,
                ),
                layout = WatermarkLayout(isRepeated = isRepeated && position == null),
                overlayMode = overlayMode,
            )
        ),
        hiddenWatermark = HiddenWatermarkParams(
            enabled = type.digitalParams()?.isInvisible == true,
            isLSB = type.digitalParams()?.isLSB != false,
        ),
    )
}

private fun Position.x(): Float = when (this) {
    Position.TopLeft, Position.BottomLeft, Position.CenterLeft -> 0.12f
    Position.TopRight, Position.BottomRight, Position.CenterRight -> 0.88f
    else -> 0.5f
}

private fun Position.y(): Float = when (this) {
    Position.TopLeft, Position.TopRight, Position.TopCenter -> 0.1f
    Position.BottomLeft, Position.BottomRight, Position.BottomCenter -> 0.9f
    else -> 0.5f
}
