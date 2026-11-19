/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.common.ai.report

import androidx.annotation.StringRes
import com.shifenmiao.core.R

enum class AiContentType(val wireValue: String) {
    TEXT("text"),
    IMAGE("image"),
}

data class AiContentReport(
    val source: String,
    val contentType: AiContentType,
    val content: String,
    val prompt: String = "",
    val model: String = "",
    val contentId: String = "",
)

enum class AiContentReportReason(
    val wireValue: String,
    @StringRes val labelRes: Int,
) {
    SEXUAL("sexual_or_age_inappropriate", R.string.ai_content_report_reason_sexual),
    HATE("hate_harassment_or_bullying", R.string.ai_content_report_reason_hate),
    VIOLENCE("violence_danger_or_illegal", R.string.ai_content_report_reason_violence),
    DECEPTIVE("misleading_or_deceptive", R.string.ai_content_report_reason_deceptive),
    PRIVACY("personal_or_private_information", R.string.ai_content_report_reason_privacy),
    OTHER("other_offensive_content", R.string.ai_content_report_reason_other),
}
