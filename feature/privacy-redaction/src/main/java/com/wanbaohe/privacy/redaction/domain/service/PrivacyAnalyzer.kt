/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.domain.service

import android.graphics.Bitmap
import com.wanbaohe.privacy.redaction.domain.model.PrivacyRegion

sealed interface PrivacyAnalysisEvent {
    data class PreparingModel(val progress: Float?) : PrivacyAnalysisEvent
    data object Recognizing : PrivacyAnalysisEvent
}

interface PrivacyAnalyzer {
    suspend fun analyze(
        image: Bitmap,
        onEvent: (PrivacyAnalysisEvent) -> Unit = {},
    ): List<PrivacyRegion>
}
