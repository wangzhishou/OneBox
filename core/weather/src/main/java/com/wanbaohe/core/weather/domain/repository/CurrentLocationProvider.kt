/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.core.weather.domain.repository

data class CurrentLocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double? = null,
    val accuracyMeters: Float? = null,
    val verticalAccuracyMeters: Float? = null,
    val capturedAt: Long = 0L,
)

interface CurrentLocationProvider {
    fun lastKnownLocation(): CurrentLocationSnapshot?
    suspend fun currentLocation(): CurrentLocationSnapshot?
}
