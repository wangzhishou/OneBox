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

/**
 * 编辑器内的「无数据」占位文本。
 *
 * 只在预览路径使用：照片没有对应的 EXIF / 位置 / 天气时，模板的完整版式仍然可见，
 * 图层也仍可点选、拖动、改样式。导出路径不调用这里，成品图永远不含示例值。
 *
 * 占位一律是「格式示意」——数字、单位与符号，机型 / 时间 / 坐标不编造任何具体值，
 * 且不含自然语言词，因此不需要进 string resources、也不需要翻译。
 */
fun WatermarkContent.previewPlaceholder(): String? = when (this) {
    is WatermarkContent.CameraParams -> fields.joinToString(separator) { it.placeholder() }
        .takeIf(String::isNotBlank)

    is WatermarkContent.Timestamp -> format
        .map { char -> if (char.isLetter()) '-' else char }
        .joinToString("")
        .takeIf(String::isNotBlank)

    is WatermarkContent.Coordinates ->
        if (useDecimalFormat) "--.-----°, --.-----°" else "--°--'--\"  --°--'--\""

    is WatermarkContent.Altitude -> if (useFeet) "≈ -- ft" else "≈ -- m"

    is WatermarkContent.Weather -> "--°C"

    // 静态内容本来就有值；Image 未选图的占位块由 visualFactory 自己画；Divider 不依赖数据。
    else -> null
}

private fun ExifFieldKey.placeholder(): String = when (this) {
    ExifFieldKey.CAMERA_MAKE -> "——"
    ExifFieldKey.CAMERA_MODEL -> "———"
    ExifFieldKey.LENS_MODEL -> "———"
    ExifFieldKey.FOCAL_LENGTH, ExifFieldKey.FOCAL_LENGTH_35MM -> "50mm"
    ExifFieldKey.APERTURE -> "f/1.8"
    ExifFieldKey.SHUTTER -> "1/125"
    ExifFieldKey.ISO -> "ISO100"
    ExifFieldKey.CAPTURED_AT -> "----.--.-- --:--:--"
    ExifFieldKey.GPS -> "--°--'--\"  --°--'--\""
}

/** 保留原色相，只压低 alpha，让占位一眼就能和真实内容区分开。 */
fun Int.asPlaceholderColor(): Int = (this and 0x00FFFFFF) or (PLACEHOLDER_ALPHA shl 24)

private const val PLACEHOLDER_ALPHA = 0x88
