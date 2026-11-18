/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.presentation.model

enum class PrivacyAnalysisStage {
    IDLE,
    PREPARING_MODEL,
    RECOGNIZING,
    FAILED,
}

data class PrivacyAnalysisState(
    val stage: PrivacyAnalysisStage = PrivacyAnalysisStage.IDLE,
    val progress: Float? = null,
) {
    val isRunning: Boolean
        get() = stage == PrivacyAnalysisStage.PREPARING_MODEL ||
            stage == PrivacyAnalysisStage.RECOGNIZING
}
