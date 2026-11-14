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

data class WatermarkDocument(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val elements: List<WatermarkElement> = listOf(
        defaultWatermarkElement(WatermarkContent.Text())
    ),
    val hiddenWatermark: HiddenWatermarkParams = HiddenWatermarkParams(),
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

data class HiddenWatermarkParams(
    val enabled: Boolean = false,
    val isLSB: Boolean = true,
)
