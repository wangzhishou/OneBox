/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.camera.watermark.domain

data class CameraWatermarkSavedState(
    val settings: CreativeCameraSettings = CreativeCameraSettings(),
    val exposureIndex: Int = 0,
    val zoomRatio: Float = 1f,
    val lastCaptureUri: String = "",
    val settingsTabName: String = "WATERMARK",
)

interface CameraWatermarkPreferences {
    suspend fun load(): CameraWatermarkSavedState
    suspend fun save(state: CameraWatermarkSavedState)
}
