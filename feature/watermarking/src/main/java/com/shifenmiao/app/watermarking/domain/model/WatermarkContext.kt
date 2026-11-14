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

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class LocationSnapshot(
    val coordinates: GeoPoint,
    val altitudeMeters: Double? = null,
    val accuracyMeters: Float? = null,
    val verticalAccuracyMeters: Float? = null,
    val capturedAt: Long = 0L,
)

data class WeatherSnapshot(
    val text: String,
    val temperatureCelsius: String = "",
    val observationTime: String = "",
    val iconCode: String = "",
)

data class PhotoWatermarkContext(
    val dateTime: String? = null,
    val coordinates: GeoPoint? = null,
    val altitudeMeters: Double? = null,
)

data class WatermarkResolution(
    val elements: List<ResolvedWatermarkElement>,
    val photoContext: PhotoWatermarkContext,
    val needsDeviceLocation: Boolean = false,
    val weatherAvailable: Boolean = true,
)

data class ResolvedWatermarkElement(
    val element: WatermarkElement,
    val text: String? = null,
    val imageData: String? = null,
    val hasData: Boolean = true,
)

/**
 * 异步内容解析回来之前的占位结果:静态内容直接可用,动态内容等解析完成后补齐。
 * 让刚添加的图层立刻出现在画布上,同时不把解析规则散落到界面层。
 */
fun WatermarkElement.staticResolution(): ResolvedWatermarkElement = when (val content = this.content) {
    is WatermarkContent.Text -> ResolvedWatermarkElement(
        element = this,
        text = content.text,
        hasData = content.text.isNotBlank(),
    )
    is WatermarkContent.Stamp -> ResolvedWatermarkElement(
        element = this,
        text = content.text,
        hasData = content.text.isNotBlank(),
    )
    is WatermarkContent.Image -> ResolvedWatermarkElement(
        element = this,
        imageData = content.imageData,
        hasData = content.imageData.isNotBlank(),
    )
    else -> ResolvedWatermarkElement(element = this, hasData = false)
}
