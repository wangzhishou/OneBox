/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.domain.render

import com.shifenmiao.app.watermarking.domain.HiddenWatermark
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.domain.model.ResolvedWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkResolution

interface WatermarkContentResolver {
    suspend fun resolve(
        uri: String,
        document: com.shifenmiao.app.watermarking.domain.model.WatermarkDocument,
        deviceLocation: com.shifenmiao.app.watermarking.domain.model.LocationSnapshot?,
    ): WatermarkResolution
}

data class WatermarkVisual<T>(
    val elementId: String,
    val image: T,
    val width: Int,
    val height: Int,
)

interface WatermarkVisualFactory<T> {
    suspend fun create(
        resolved: ResolvedWatermarkElement,
        canvasWidth: Int,
        canvasHeight: Int,
    ): WatermarkVisual<T>?
}

interface WatermarkRenderer<I> {
    suspend fun render(
        image: I,
        resolution: WatermarkResolution,
        hiddenWatermark: HiddenWatermarkParams,
    ): I?
}

interface HiddenWatermarkDetector<I> {
    suspend fun detect(image: I): HiddenWatermark?
}

interface HiddenWatermarkEmbedder<I> {
    suspend fun embed(
        image: I,
        resolution: WatermarkResolution,
        params: HiddenWatermarkParams,
    ): I?
}
