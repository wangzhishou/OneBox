/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.domain.preset

import com.shifenmiao.model.editor.LayerTransform
import com.t8rin.imagetoolbox.core.resources.R
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkLayout
import com.shifenmiao.app.watermarking.domain.model.WatermarkStyle
import com.shifenmiao.app.watermarking.domain.model.defaultWatermarkElement

object BuiltinWatermarkPresets {
    val entries: List<WatermarkPreset> by lazy {
        listOf(
            preset(
                id = "copyright_tile",
                nameRes = R.string.watermark_preset_copyright,
                elements = listOf(
                    defaultWatermarkElement(WatermarkContent.Text("© OneBox")).copy(
                        transform = LayerTransform(centerX = 0.5f, centerY = 0.5f, rotation = -30f, alpha = 0.35f),
                        style = WatermarkStyle(color = -1, sizeRatio = 0.09f),
                        layout = WatermarkLayout(isRepeated = true),
                    )
                ),
            ),
            preset(
                id = "brand_logo",
                nameRes = R.string.watermark_preset_logo,
                elements = listOf(
                    defaultWatermarkElement(WatermarkContent.Image()).copy(
                        transform = LayerTransform(centerX = 0.82f, centerY = 0.86f, alpha = 0.85f),
                        layout = WatermarkLayout(isRepeated = false),
                    )
                ),
            ),
            preset(
                id = "simple_stamp",
                nameRes = R.string.watermark_preset_stamp,
                elements = listOf(defaultWatermarkElement(WatermarkContent.Stamp("OneBox"))),
            ),
            preset(
                id = "photo_time",
                nameRes = R.string.watermark_preset_time,
                elements = listOf(defaultWatermarkElement(WatermarkContent.Timestamp())),
            ),
            preset(
                id = "outdoor",
                nameRes = R.string.watermark_preset_outdoor,
                elements = listOf(
                    infoElement(WatermarkContent.Timestamp(), 0.28f, 0.88f),
                    infoElement(WatermarkContent.Coordinates(), 0.72f, 0.88f),
                    infoElement(WatermarkContent.Altitude(), 0.72f, 0.95f),
                ),
            ),
            preset(
                id = "weather_checkin",
                nameRes = R.string.watermark_preset_weather,
                elements = listOf(
                    infoElement(WatermarkContent.Weather(), 0.32f, 0.9f),
                    infoElement(WatermarkContent.Timestamp(), 0.76f, 0.9f),
                ),
            ),
            preset(
                id = "coordinates",
                nameRes = R.string.watermark_preset_coordinates,
                elements = listOf(defaultWatermarkElement(WatermarkContent.Coordinates())),
            ),
            preset(
                id = "hidden_copyright",
                nameRes = R.string.watermark_preset_hidden,
                elements = listOf(
                    defaultWatermarkElement(WatermarkContent.Text("© OneBox")).copy(
                        layout = WatermarkLayout(isRepeated = false),
                    )
                ),
                hidden = HiddenWatermarkParams(enabled = true),
            ),
        )
    }

    private fun infoElement(
        content: WatermarkContent,
        centerX: Float,
        centerY: Float,
    ) = defaultWatermarkElement(content).copy(
        transform = LayerTransform(centerX = centerX, centerY = centerY, alpha = 0.9f),
        style = WatermarkStyle(color = -1, backgroundColor = 0x66000000, sizeRatio = 0.042f),
        layout = WatermarkLayout(isRepeated = false),
    )

    private fun preset(
        id: String,
        nameRes: Int,
        elements: List<WatermarkElement>,
        hidden: HiddenWatermarkParams = HiddenWatermarkParams(),
    ) = WatermarkPreset(
        id = id,
        nameRes = nameRes,
        document = WatermarkDocument(elements = elements, hiddenWatermark = hidden),
        isBuiltin = true,
    )
}
