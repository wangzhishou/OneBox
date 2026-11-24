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

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import androidx.annotation.StringRes
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageCapture
import androidx.camera.video.Quality
import com.wanbaohe.camera.watermark.R

enum class CameraCaptureMode {
    PHOTO,
    VIDEO,
}

enum class CameraLens {
    BACK,
    FRONT,
}

enum class CameraFlashMode(val cameraXValue: Int) {
    OFF(ImageCapture.FLASH_MODE_OFF),
    AUTO(ImageCapture.FLASH_MODE_AUTO),
    ON(ImageCapture.FLASH_MODE_ON),
}

enum class CameraRatio(val cameraXValue: Int, val value: Float?) {
    FULL(AspectRatio.RATIO_4_3, null),
    RATIO_4_3(AspectRatio.RATIO_4_3, 4f / 3f),
    RATIO_1_1(AspectRatio.RATIO_4_3, 1f),
    RATIO_16_9(AspectRatio.RATIO_16_9, 16f / 9f),
}

enum class CameraTimer(val seconds: Int) {
    OFF(0),
    THREE(3),
    TEN(10),
}

enum class CameraGrid {
    OFF,
    THIRDS,
    GOLDEN,
    DIAGONAL,
}

enum class PhotoQuality(val captureMode: Int, @StringRes val labelRes: Int) {
    FAST(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY, R.string.camera_photo_quality_fast),
    HIGH(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY, R.string.camera_photo_quality_high),
}

enum class VideoQualityOption(val quality: Quality, val shortLabel: String) {
    SD(Quality.SD, "480p"),
    HD(Quality.HD, "720p"),
    FHD(Quality.FHD, "1080p"),
    UHD(Quality.UHD, "4K"),
}

enum class CameraFrameRate(val fps: Int) {
    AUTO(0),
    FPS_30(30),
    FPS_60(60),
}

enum class CreativeFilter(val requestValue: Int, @StringRes val labelRes: Int) {
    ORIGINAL(CameraCharacteristics.CONTROL_EFFECT_MODE_OFF, R.string.camera_filter_original),
    MONO(CameraCharacteristics.CONTROL_EFFECT_MODE_MONO, R.string.camera_filter_mono),
    SEPIA(CameraCharacteristics.CONTROL_EFFECT_MODE_SEPIA, R.string.camera_filter_sepia),
    NEGATIVE(CameraCharacteristics.CONTROL_EFFECT_MODE_NEGATIVE, R.string.camera_filter_negative),
    SOLARIZE(CameraCharacteristics.CONTROL_EFFECT_MODE_SOLARIZE, R.string.camera_filter_solarize),
    POSTERIZE(CameraCharacteristics.CONTROL_EFFECT_MODE_POSTERIZE, R.string.camera_filter_posterize),
    AQUA(CameraCharacteristics.CONTROL_EFFECT_MODE_AQUA, R.string.camera_filter_aqua),
    BLACKBOARD(CameraCharacteristics.CONTROL_EFFECT_MODE_BLACKBOARD, R.string.camera_filter_blackboard),
    WHITEBOARD(CameraCharacteristics.CONTROL_EFFECT_MODE_WHITEBOARD, R.string.camera_filter_whiteboard),
}

enum class CreativeScene(val requestValue: Int, @StringRes val labelRes: Int) {
    AUTO(CameraCharacteristics.CONTROL_SCENE_MODE_DISABLED, R.string.camera_scene_auto),
    PORTRAIT(CameraCharacteristics.CONTROL_SCENE_MODE_PORTRAIT, R.string.camera_scene_portrait),
    LANDSCAPE(CameraCharacteristics.CONTROL_SCENE_MODE_LANDSCAPE, R.string.camera_scene_landscape),
    NIGHT(CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT, R.string.camera_scene_night),
    NIGHT_PORTRAIT(CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT_PORTRAIT, R.string.camera_scene_night_portrait),
    SPORTS(CameraCharacteristics.CONTROL_SCENE_MODE_SPORTS, R.string.camera_scene_sports),
    PARTY(CameraCharacteristics.CONTROL_SCENE_MODE_PARTY, R.string.camera_scene_party),
    BEACH(CameraCharacteristics.CONTROL_SCENE_MODE_BEACH, R.string.camera_scene_beach),
    SNOW(CameraCharacteristics.CONTROL_SCENE_MODE_SNOW, R.string.camera_scene_snow),
    SUNSET(CameraCharacteristics.CONTROL_SCENE_MODE_SUNSET, R.string.camera_scene_sunset),
    CANDLELIGHT(CameraCharacteristics.CONTROL_SCENE_MODE_CANDLELIGHT, R.string.camera_scene_candlelight),
    FIREWORKS(CameraCharacteristics.CONTROL_SCENE_MODE_FIREWORKS, R.string.camera_scene_fireworks),
    THEATRE(CameraCharacteristics.CONTROL_SCENE_MODE_THEATRE, R.string.camera_scene_theatre),
    STEADY_PHOTO(CameraCharacteristics.CONTROL_SCENE_MODE_STEADYPHOTO, R.string.camera_scene_steady_photo),
    ACTION(CameraCharacteristics.CONTROL_SCENE_MODE_ACTION, R.string.camera_scene_action),
    BARCODE(CameraCharacteristics.CONTROL_SCENE_MODE_BARCODE, R.string.camera_scene_barcode),
}

enum class CameraWhiteBalance(val requestValue: Int, @StringRes val labelRes: Int) {
    AUTO(CaptureRequest.CONTROL_AWB_MODE_AUTO, R.string.camera_wb_auto),
    INCANDESCENT(CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT, R.string.camera_wb_incandescent),
    FLUORESCENT(CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT, R.string.camera_wb_fluorescent),
    WARM_FLUORESCENT(CaptureRequest.CONTROL_AWB_MODE_WARM_FLUORESCENT, R.string.camera_wb_warm_fluorescent),
    DAYLIGHT(CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT, R.string.camera_wb_daylight),
    CLOUDY(CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT, R.string.camera_wb_cloudy),
    TWILIGHT(CaptureRequest.CONTROL_AWB_MODE_TWILIGHT, R.string.camera_wb_twilight),
    SHADE(CaptureRequest.CONTROL_AWB_MODE_SHADE, R.string.camera_wb_shade),
}

enum class CameraWatermarkStyle(
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
) {
    NONE(R.string.camera_watermark_none, R.string.camera_watermark_none_description),
    LEICA(R.string.camera_watermark_leica, R.string.camera_watermark_leica_description),
    TIMESTAMP(R.string.camera_watermark_timestamp, R.string.camera_watermark_timestamp_description),
    MINIMAL(R.string.camera_watermark_minimal, R.string.camera_watermark_minimal_description),
    COORDINATES(R.string.camera_watermark_coordinates, R.string.camera_watermark_coordinates_description),
    SIGNATURE(R.string.camera_watermark_signature, R.string.camera_watermark_signature_description),
}

data class CameraWatermarkMetadata(
    val placeLabel: String = "",
    val weatherText: String = "",
    val temperatureCelsius: String = "",
    val windDirection: String = "",
    val windScale: String = "",
    val humidity: String = "",
)

data class CreativeCameraSettings(
    val mode: CameraCaptureMode = CameraCaptureMode.PHOTO,
    val lens: CameraLens = CameraLens.BACK,
    val flashMode: CameraFlashMode = CameraFlashMode.OFF,
    val ratio: CameraRatio = CameraRatio.FULL,
    val timer: CameraTimer = CameraTimer.OFF,
    val grid: CameraGrid = CameraGrid.THIRDS,
    val photoQuality: PhotoQuality = PhotoQuality.FAST,
    val videoQuality: VideoQualityOption = VideoQualityOption.FHD,
    val frameRate: CameraFrameRate = CameraFrameRate.AUTO,
    val filter: CreativeFilter = CreativeFilter.ORIGINAL,
    val scene: CreativeScene = CreativeScene.AUTO,
    val whiteBalance: CameraWhiteBalance = CameraWhiteBalance.AUTO,
    val watermarkStyle: CameraWatermarkStyle = CameraWatermarkStyle.MINIMAL,
    val torchEnabled: Boolean = false,
    val audioEnabled: Boolean = true,
    val videoStabilization: Boolean = false,
    val mirrorFrontCamera: Boolean = true,
    val histogramEnabled: Boolean = false,
    val locationEnabled: Boolean = false,
    val altitudeEnabled: Boolean = true,
    val weatherEnabled: Boolean = true,
    val timestampEnabled: Boolean = true,
    val cameraParametersEnabled: Boolean = true,
    val hideUnavailableWatermarkInfo: Boolean = false,
    val lowLightBoostEnabled: Boolean = false,
    val hdrEnabled: Boolean = false,
)

data class CreativeCameraCapabilities(
    val hasFlashUnit: Boolean = false,
    val hasFrontCamera: Boolean = false,
    val minZoomRatio: Float = 1f,
    val maxZoomRatio: Float = 1f,
    val minExposureIndex: Int = 0,
    val maxExposureIndex: Int = 0,
    val exposureStep: Float = 0f,
    val supportedFilters: Set<CreativeFilter> = setOf(CreativeFilter.ORIGINAL),
    val supportedScenes: Set<CreativeScene> = setOf(CreativeScene.AUTO),
    val supportedWhiteBalances: Set<CameraWhiteBalance> = setOf(CameraWhiteBalance.AUTO),
    val supportedVideoQualities: Set<VideoQualityOption> = setOf(VideoQualityOption.HD),
    val supportedFrameRates: Set<CameraFrameRate> = setOf(CameraFrameRate.AUTO, CameraFrameRate.FPS_30),
    val supportsVideoStabilization: Boolean = false,
    val supportsLowLightBoost: Boolean = false,
    val supportsHdr: Boolean = false,
    val supportsTorchStrength: Boolean = false,
)

data class CreativeCameraUiState(
    val settings: CreativeCameraSettings = CreativeCameraSettings(),
    val capabilities: CreativeCameraCapabilities = CreativeCameraCapabilities(),
    val isCameraReady: Boolean = false,
    val isCapturing: Boolean = false,
    val isSaving: Boolean = false,
    val countdownSeconds: Int? = null,
    val zoomRatio: Float = 1f,
    val exposureIndex: Int = 0,
    val isRecording: Boolean = false,
    val isRecordingPaused: Boolean = false,
    val isRecordingMuted: Boolean = false,
    val recordingDurationMs: Long = 0L,
    val pendingPhoto: Uri? = null,
    val lastCaptureUri: Uri? = null,
    val histogram: List<Int> = emptyList(),
    val settingsTabName: String = "WATERMARK",
    @StringRes val messageRes: Int? = null,
)
