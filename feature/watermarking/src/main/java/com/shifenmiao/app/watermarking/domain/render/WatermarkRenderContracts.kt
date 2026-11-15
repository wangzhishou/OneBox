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
    /**
     * [sizeBasisWidth] 是字号 / 图片尺寸的计算基准。
     * PHOTO 锚定元素等于 canvasWidth；BAND 锚定元素传白带内容高度，
     * 避免带高按宽度成比例时字号随原图宽高比漂移。
     */
    suspend fun create(
        resolved: ResolvedWatermarkElement,
        canvasWidth: Int,
        canvasHeight: Int,
        sizeBasisWidth: Int = canvasWidth,
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
