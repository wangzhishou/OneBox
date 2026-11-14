/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.model

import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAltitude
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocationOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkKind

fun WatermarkKind.icon(): ImageVector = when (this) {
    WatermarkKind.TEXT -> Icons.Outlined.LineText
    WatermarkKind.IMAGE -> Icons.Outlined.LineImage
    WatermarkKind.STAMP -> MaterialIcons.Outlined.Star
    WatermarkKind.TIMESTAMP -> Icons.Outlined.LineTimer
    WatermarkKind.ALTITUDE -> Icons.Outlined.LineAltitude
    WatermarkKind.WEATHER -> MaterialIcons.Outlined.Cloud
    WatermarkKind.COORDINATES -> Icons.Outlined.LineLocationOn
}

@Composable
fun WatermarkKind.title(): String = stringResource(
    when (this) {
        WatermarkKind.TEXT -> R.string.text
        WatermarkKind.IMAGE -> R.string.image
        WatermarkKind.STAMP -> R.string.stamp
        WatermarkKind.TIMESTAMP -> R.string.timestamp
        WatermarkKind.ALTITUDE -> R.string.watermark_altitude
        WatermarkKind.WEATHER -> R.string.watermark_weather
        WatermarkKind.COORDINATES -> R.string.watermark_coordinates
    }
)

fun WatermarkContent.sampleText(): String = when (this) {
    is WatermarkContent.Text -> text
    is WatermarkContent.Image -> "Logo"
    is WatermarkContent.Stamp -> text
    is WatermarkContent.Timestamp -> "2026-11-14 12:00"
    is WatermarkContent.Altitude -> "≈ 128 m"
    is WatermarkContent.Weather -> "☀ 22°C"
    is WatermarkContent.Coordinates -> "31.23040°, 121.47370°"
}
