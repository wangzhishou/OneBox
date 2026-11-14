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
import com.t8rin.imagetoolbox.core.resources.R

enum class WatermarkPanel {
    PRESETS,
    CONTENT,
    STYLE,
    LAYERS,
    IMAGES,
}

@Composable
fun WatermarkPanel.title(): String = stringResource(
    when (this) {
        WatermarkPanel.PRESETS -> R.string.presets
        WatermarkPanel.CONTENT -> R.string.content
        WatermarkPanel.STYLE -> R.string.watermark_style
        WatermarkPanel.LAYERS -> R.string.watermark_layers
        WatermarkPanel.IMAGES -> R.string.watermark_images
    }
)
