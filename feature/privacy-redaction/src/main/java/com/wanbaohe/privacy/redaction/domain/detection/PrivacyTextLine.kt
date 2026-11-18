/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.domain.detection

import com.wanbaohe.privacy.redaction.domain.model.PrivacyCategory

data class PrivacyTextLine(
    val text: String,
    val confidence: Float,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

data class PrivacyTextMatch(
    val category: PrivacyCategory,
    val line: PrivacyTextLine,
)
