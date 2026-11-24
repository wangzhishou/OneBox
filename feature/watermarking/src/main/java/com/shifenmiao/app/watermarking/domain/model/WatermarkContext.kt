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
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val lensModel: String? = null,
    val focalLength: String? = null,
    val focalLength35mm: String? = null,
    val aperture: String? = null,
    val shutter: String? = null,
    val iso: String? = null,
    /** 文件时间只在拍摄时间全部缺失时兜底，展示时必须标注「非拍摄时间」。 */
    val fileDateTime: String? = null,
) {
    fun valueOf(field: ExifFieldKey): String? = when (field) {
        ExifFieldKey.CAMERA_MAKE -> cameraMake
        ExifFieldKey.CAMERA_MODEL -> cameraModel
        ExifFieldKey.LENS_MODEL -> lensModel
        ExifFieldKey.FOCAL_LENGTH -> focalLength
        ExifFieldKey.FOCAL_LENGTH_35MM -> focalLength35mm
        ExifFieldKey.APERTURE -> aperture
        ExifFieldKey.SHUTTER -> shutter
        ExifFieldKey.ISO -> iso
        ExifFieldKey.CAPTURED_AT -> dateTime ?: fileDateTime
        ExifFieldKey.GPS -> coordinates?.let { "%.5f, %.5f".format(java.util.Locale.US, it.latitude, it.longitude) }
    }
}

data class WatermarkResolution(
    val elements: List<ResolvedWatermarkElement>,
    val photoContext: PhotoWatermarkContext,
    val needsDeviceLocation: Boolean = false,
    val weatherAvailable: Boolean = true,
    /** 输出画布版式，由 resolve 从 document 透传，预览与导出共用。 */
    val frame: WatermarkFrame = WatermarkFrame.InPlace,
    val mount: PhotoMount? = null,
)

data class ResolvedWatermarkElement(
    val element: WatermarkElement,
    val text: String? = null,
    val imageData: String? = null,
    val hasData: Boolean = true,
    /**
     * 值是否来自照片本身（EXIF 拍摄时间 / GPS / 海拔）。
     * 动态内容在照片里没有对应信息、靠设备位置补出来时为 false，
     * 属性面板据此继续保留「获取」卡片，让用户能看到已经取到的值。
     */
    val fromPhoto: Boolean = true,
    val isFileTime: Boolean = false,
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
        imageData = content.imageData.ifBlank {
            content.builtinResId.takeIf { it != 0 }?.let { "builtin://$it" }
        },
        hasData = content.imageData.isNotBlank() || content.builtinResId != 0,
    )
    is WatermarkContent.Divider -> ResolvedWatermarkElement(
        element = this,
        hasData = true,
    )
    else -> ResolvedWatermarkElement(element = this, hasData = false)
}
