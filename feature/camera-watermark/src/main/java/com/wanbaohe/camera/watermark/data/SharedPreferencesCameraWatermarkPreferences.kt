/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.camera.watermark.data

import android.content.Context
import com.wanbaohe.camera.watermark.domain.CameraCaptureMode
import com.wanbaohe.camera.watermark.domain.CameraFlashMode
import com.wanbaohe.camera.watermark.domain.CameraFrameRate
import com.wanbaohe.camera.watermark.domain.CameraGrid
import com.wanbaohe.camera.watermark.domain.CameraLens
import com.wanbaohe.camera.watermark.domain.CameraRatio
import com.wanbaohe.camera.watermark.domain.CameraTimer
import com.wanbaohe.camera.watermark.domain.CameraWatermarkPreferences
import com.wanbaohe.camera.watermark.domain.CameraWatermarkSavedState
import com.wanbaohe.camera.watermark.domain.CameraWatermarkStyle
import com.wanbaohe.camera.watermark.domain.CameraWhiteBalance
import com.wanbaohe.camera.watermark.domain.CreativeCameraSettings
import com.wanbaohe.camera.watermark.domain.CreativeFilter
import com.wanbaohe.camera.watermark.domain.CreativeScene
import com.wanbaohe.camera.watermark.domain.PhotoQuality
import com.wanbaohe.camera.watermark.domain.VideoQualityOption
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesCameraWatermarkPreferences @Inject constructor(
    @ApplicationContext context: Context,
) : CameraWatermarkPreferences {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun load(): CameraWatermarkSavedState = runCatching {
        val raw = preferences.getString(KEY_STATE, null) ?: return CameraWatermarkSavedState()
        decode(JSONObject(raw))
    }.getOrDefault(CameraWatermarkSavedState())

    override suspend fun save(state: CameraWatermarkSavedState) {
        preferences.edit().putString(KEY_STATE, encode(state)).apply()
    }

    private fun encode(saved: CameraWatermarkSavedState): String {
        val settings = saved.settings
        return JSONObject().apply {
            put("mode", settings.mode.name)
            put("lens", settings.lens.name)
            put("flashMode", settings.flashMode.name)
            put("ratio", settings.ratio.name)
            put("timer", settings.timer.name)
            put("grid", settings.grid.name)
            put("photoQuality", settings.photoQuality.name)
            put("videoQuality", settings.videoQuality.name)
            put("frameRate", settings.frameRate.name)
            put("filter", settings.filter.name)
            put("scene", settings.scene.name)
            put("whiteBalance", settings.whiteBalance.name)
            put("watermarkStyle", settings.watermarkStyle.name)
            put("audioEnabled", settings.audioEnabled)
            put("videoStabilization", settings.videoStabilization)
            put("mirrorFrontCamera", settings.mirrorFrontCamera)
            put("histogramEnabled", settings.histogramEnabled)
            put("locationEnabled", settings.locationEnabled)
            put("altitudeEnabled", settings.altitudeEnabled)
            put("weatherEnabled", settings.weatherEnabled)
            put("timestampEnabled", settings.timestampEnabled)
            put("cameraParametersEnabled", settings.cameraParametersEnabled)
            put("hideUnavailableWatermarkInfo", settings.hideUnavailableWatermarkInfo)
            put("lowLightBoostEnabled", settings.lowLightBoostEnabled)
            put("hdrEnabled", settings.hdrEnabled)
            put("exposureIndex", saved.exposureIndex)
            put("zoomRatio", saved.zoomRatio.toDouble())
            put("lastCaptureUri", saved.lastCaptureUri)
            put("settingsTabName", saved.settingsTabName)
        }.toString()
    }

    private fun decode(json: JSONObject): CameraWatermarkSavedState {
        val defaults = CreativeCameraSettings()
        val settings = CreativeCameraSettings(
            mode = json.readEnum("mode", defaults.mode),
            lens = json.readEnum("lens", defaults.lens),
            flashMode = json.readEnum("flashMode", defaults.flashMode),
            ratio = json.readEnum("ratio", defaults.ratio),
            timer = json.readEnum("timer", defaults.timer),
            grid = json.readEnum("grid", defaults.grid),
            photoQuality = json.readEnum("photoQuality", defaults.photoQuality),
            videoQuality = json.readEnum("videoQuality", defaults.videoQuality),
            frameRate = json.readEnum("frameRate", defaults.frameRate),
            filter = json.readEnum("filter", defaults.filter),
            scene = json.readEnum("scene", defaults.scene),
            whiteBalance = json.readEnum("whiteBalance", defaults.whiteBalance),
            watermarkStyle = json.readEnum("watermarkStyle", defaults.watermarkStyle),
            audioEnabled = json.optBoolean("audioEnabled", defaults.audioEnabled),
            videoStabilization = json.optBoolean("videoStabilization", defaults.videoStabilization),
            mirrorFrontCamera = json.optBoolean("mirrorFrontCamera", defaults.mirrorFrontCamera),
            histogramEnabled = json.optBoolean("histogramEnabled", defaults.histogramEnabled),
            locationEnabled = json.optBoolean("locationEnabled", defaults.locationEnabled),
            altitudeEnabled = json.optBoolean("altitudeEnabled", defaults.altitudeEnabled),
            weatherEnabled = json.optBoolean("weatherEnabled", defaults.weatherEnabled),
            timestampEnabled = json.optBoolean("timestampEnabled", defaults.timestampEnabled),
            cameraParametersEnabled = json.optBoolean("cameraParametersEnabled", defaults.cameraParametersEnabled),
            hideUnavailableWatermarkInfo = json.optBoolean(
                "hideUnavailableWatermarkInfo",
                defaults.hideUnavailableWatermarkInfo,
            ),
            lowLightBoostEnabled = json.optBoolean("lowLightBoostEnabled", defaults.lowLightBoostEnabled),
            hdrEnabled = json.optBoolean("hdrEnabled", defaults.hdrEnabled),
        )
        return CameraWatermarkSavedState(
            settings = settings,
            exposureIndex = json.optInt("exposureIndex", 0),
            zoomRatio = json.optDouble("zoomRatio", 1.0).toFloat().takeIf(Float::isFinite) ?: 1f,
            lastCaptureUri = json.optString("lastCaptureUri", ""),
            settingsTabName = json.optString("settingsTabName", "WATERMARK"),
        )
    }

    private inline fun <reified T : Enum<T>> JSONObject.readEnum(key: String, default: T): T =
        runCatching { enumValueOf<T>(optString(key)) }.getOrDefault(default)

    private companion object {
        const val PREFERENCES_NAME = "camera_watermark_preferences"
        const val KEY_STATE = "saved_state"
    }
}
