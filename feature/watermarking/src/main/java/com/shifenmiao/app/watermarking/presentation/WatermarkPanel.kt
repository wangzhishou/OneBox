/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.resources.R

enum class WatermarkPanel {
    PRESETS,
    MOUNT,
    FILTER,
    TOOLS,
    PROPERTIES,
    LAYERS,
    PRIVACY,
    IMAGES,
}

@Composable
fun WatermarkPanel.title(): String = stringResource(
    when (this) {
        WatermarkPanel.PRESETS -> R.string.presets
        WatermarkPanel.MOUNT -> R.string.watermark_photo_mount_short
        WatermarkPanel.FILTER -> R.string.filter
        WatermarkPanel.TOOLS -> R.string.watermark_tools
        WatermarkPanel.PROPERTIES -> R.string.properties
        WatermarkPanel.LAYERS -> R.string.watermark_layers
        WatermarkPanel.PRIVACY -> com.wanbaohe.privacy.redaction.R.string.privacy_redaction
        WatermarkPanel.IMAGES -> R.string.watermark_batch
    }
)

fun ImageFormat.keepsHiddenWatermark(): Boolean = when (this) {
    ImageFormat.Png.Lossless,
    ImageFormat.Bmp,
    ImageFormat.Webp.Lossless,
    ImageFormat.Tiff,
    ImageFormat.Tif,
    ImageFormat.Qoi,
    ImageFormat.Ico -> true
    else -> false
}
