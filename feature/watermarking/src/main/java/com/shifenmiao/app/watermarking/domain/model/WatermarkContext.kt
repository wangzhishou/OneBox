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
