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
        val imageData: String = DEFAULT_IMAGE,
        val size: Float = 0.2f,
    ) : WatermarkContent {
        companion object {
            const val DEFAULT_IMAGE = "file:///android_asset/svg/emotions/aasparkles.svg"
        }
    }

    data class Stamp(
        val text: String = "Stamp",
    ) : WatermarkContent

    data class Timestamp(
        val format: String = "yyyy-MM-dd HH:mm",
        val usePhotoTime: Boolean = true,
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
}

enum class WatermarkKind {
    TEXT,
    IMAGE,
    STAMP,
    TIMESTAMP,
    ALTITUDE,
    WEATHER,
    COORDINATES,
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
    }

fun WatermarkContent.sourceOrNull(): WatermarkValueSource? = when (this) {
    is WatermarkContent.Altitude -> source
    is WatermarkContent.Weather -> source
    is WatermarkContent.Coordinates -> source
    else -> null
}
