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

import com.shifenmiao.model.blog.FeedbackRequest
import com.shifenmiao.network.api.ApiService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AiContentReportRepository {
    suspend fun submit(
        report: AiContentReport,
        reason: AiContentReportReason,
        details: String,
    ): Result<Unit>
}

class AiContentReportRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dispatchersHolder: DispatchersHolder,
) : AiContentReportRepository {

    override suspend fun submit(
        report: AiContentReport,
        reason: AiContentReportReason,
        details: String,
    ): Result<Unit> = withContext(dispatchersHolder.ioDispatcher) {
        runCatching {
            val response = apiService.createFeedback(
                FeedbackRequest(
                    title = "AI content report - ${report.contentType.wireValue} - ${reason.wireValue}",
                    content = buildReportBody(report, reason, details),
                    pictureIds = emptyList(),
                    tagIds = emptyList(),
                    blogType = FEEDBACK_BLOG_TYPE,
                )
            )
            if (!response.isSuccessful) {
                error("AI content report failed: HTTP ${response.code()}")
            }
        }
    }

    private fun buildReportBody(
        report: AiContentReport,
        reason: AiContentReportReason,
        details: String,
    ): String = buildString {
        appendLine(REPORT_MARKER)
        appendLine("Content type: ${report.contentType.wireValue}")
        appendLine("Source: ${report.source}")
        appendLine("Reason: ${reason.wireValue}")
        if (report.model.isNotBlank()) {
            appendLine("Model: ${report.model.bounded(MAX_METADATA_CHARS)}")
        }
        if (report.contentId.isNotBlank()) {
            appendLine("Content ID: ${report.contentId.bounded(MAX_METADATA_CHARS)}")
        }
        if (details.isNotBlank()) {
            appendLine("Reporter details: ${details.bounded(MAX_DETAILS_CHARS)}")
        }
        if (report.prompt.isNotBlank()) {
            appendLine()
            appendLine("--- Related prompt ---")
            append(report.prompt.bounded(MAX_PROMPT_CHARS))
        }
        if (report.content.isNotBlank() && report.content != report.prompt) {
            appendLine()
            appendLine("--- AI-generated content ---")
            append(report.content.bounded(MAX_CONTENT_CHARS))
        }
    }

    private fun String.bounded(limit: Int): String =
        if (length <= limit) this else take(limit) + "\n[truncated]"

    private companion object {
        const val REPORT_MARKER = "AI_CONTENT_REPORT_V1"
        const val FEEDBACK_BLOG_TYPE = 3
        const val MAX_METADATA_CHARS = 500
        const val MAX_DETAILS_CHARS = 2_000
        const val MAX_PROMPT_CHARS = 10_000
        const val MAX_CONTENT_CHARS = 50_000
    }
}
