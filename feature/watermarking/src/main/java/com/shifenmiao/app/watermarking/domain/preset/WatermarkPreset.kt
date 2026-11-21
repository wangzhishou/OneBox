/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.domain.preset

import androidx.annotation.StringRes
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument

data class WatermarkPreset(
    val id: String,
    val name: String = "",
    @StringRes val nameRes: Int = 0,
    val document: WatermarkDocument,
    val isBuiltin: Boolean = false,
    val updatedAt: Long = 0L,
)

interface WatermarkPresetStore {
    suspend fun lastSignature(): String
    suspend fun rememberSignature(text: String)
    suspend fun listCustomPresets(): List<WatermarkPreset>
    suspend fun savePreset(preset: WatermarkPreset): WatermarkPreset
    suspend fun deletePreset(id: String)
}
