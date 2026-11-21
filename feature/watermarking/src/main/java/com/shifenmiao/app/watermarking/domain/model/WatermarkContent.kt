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

sealed interface WatermarkContent {
    data class Text(
        val text: String = "Watermark",
    ) : WatermarkContent

    data class Image(
        val imageData: String = "",
        val size: Float = 0.2f,
        /** 内置矢量 logo 的资源 id；非 0 时优先于 imageData，由渲染层显式解析。 */
        val builtinResId: Int = 0,
    ) : WatermarkContent

    data class Stamp(
        val text: String = "Stamp",
    ) : WatermarkContent

    data class Timestamp(
        val format: String = "yyyy-MM-dd HH:mm",
        val usePhotoTime: Boolean = true,
        val fallbackText: String = "",
    ) : WatermarkContent

    data class Altitude(
        val source: WatermarkValueSource = WatermarkValueSource.PHOTO,
        val manualValue: String = "",
        val useFeet: Boolean = false,
    ) : WatermarkContent

    data class Weather(
        val source: WatermarkValueSource = WatermarkValueSource.PHOTO,
        val manualValue: String = "",
        val includeTemperature: Boolean = true,
        val includeObservationTime: Boolean = false,
    ) : WatermarkContent

    data class Coordinates(
        val source: WatermarkValueSource = WatermarkValueSource.PHOTO,
        val manualValue: String = "",
        val useDecimalFormat: Boolean = true,
    ) : WatermarkContent

    /**
     * 从照片 EXIF 拼出的相机参数行。
     *
     * [omitMissing] 为 true 时缺字段直接省略（不画 "--"、绝不填假值），
     * [manualOverrides] 的 key 是 [ExifFieldKey.name]，由信息面板与这里共享同一份值。
     */
    data class CameraParams(
        val fields: List<ExifFieldKey> = listOf(
            ExifFieldKey.FOCAL_LENGTH,
            ExifFieldKey.APERTURE,
            ExifFieldKey.SHUTTER,
            ExifFieldKey.ISO,
        ),
        val separator: String = "  ",
        val manualOverrides: Map<String, String> = emptyMap(),
        val omitMissing: Boolean = true,
    ) : WatermarkContent

    /** 竖分隔线，画在带内，不依赖外部图片资源。 */
    data class Divider(
        val thicknessRatio: Float = 0.015f,
        val heightRatio: Float = 0.8f,
    ) : WatermarkContent
}

/** 相机参数内容可选择展示的字段，同时用于模板声明 requiredFields。 */
enum class ExifFieldKey {
    CAMERA_MAKE,
    CAMERA_MODEL,
    LENS_MODEL,
    FOCAL_LENGTH,
    FOCAL_LENGTH_35MM,
    APERTURE,
    SHUTTER,
    ISO,
    CAPTURED_AT,
    GPS,
}

enum class WatermarkKind {
    TEXT,
    IMAGE,
    STAMP,
    TIMESTAMP,
    ALTITUDE,
    WEATHER,
    COORDINATES,
    CAMERA_PARAMS,
    DIVIDER,
}

enum class WatermarkValueSource {
    PHOTO,
    DEVICE,
    MANUAL,
}

val WatermarkContent.kind: WatermarkKind
    get() = when (this) {
        is WatermarkContent.Text -> WatermarkKind.TEXT
        is WatermarkContent.Image -> WatermarkKind.IMAGE
        is WatermarkContent.Stamp -> WatermarkKind.STAMP
        is WatermarkContent.Timestamp -> WatermarkKind.TIMESTAMP
        is WatermarkContent.Altitude -> WatermarkKind.ALTITUDE
        is WatermarkContent.Weather -> WatermarkKind.WEATHER
        is WatermarkContent.Coordinates -> WatermarkKind.COORDINATES
        is WatermarkContent.CameraParams -> WatermarkKind.CAMERA_PARAMS
        is WatermarkContent.Divider -> WatermarkKind.DIVIDER
    }

fun WatermarkContent.sourceOrNull(): WatermarkValueSource? = when (this) {
    is WatermarkContent.Altitude -> source
    is WatermarkContent.Weather -> source
    is WatermarkContent.Coordinates -> source
    else -> null
}

/** 天气要用坐标去查，照片没有坐标时会落到设备位置。 */
fun WatermarkDocument.usesDeviceLocation(): Boolean = elements.any { element ->
    when (val content = element.content) {
        is WatermarkContent.Weather -> content.source != WatermarkValueSource.MANUAL
        is WatermarkContent.Altitude -> content.source == WatermarkValueSource.DEVICE
        is WatermarkContent.Coordinates -> content.source == WatermarkValueSource.DEVICE
        else -> false
    }
}
