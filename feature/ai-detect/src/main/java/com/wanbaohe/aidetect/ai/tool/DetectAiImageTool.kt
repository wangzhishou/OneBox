/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.aidetect.ai.tool

import android.net.Uri
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.jsonStringOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.aidetect.R
import com.wanbaohe.aidetect.service.AiDetectError
import com.wanbaohe.aidetect.service.AiDetectException
import com.wanbaohe.aidetect.service.AiDetectService
import javax.inject.Inject

/**
 * AI Agent 工具：图片 AIGC 检测。
 *
 * 调用朱雀 AIGC 检测判断一张图片是否由 AI 生成。
 * 检测结果的历史写入由 [AiDetectService] 内建完成，
 * 用户可在 AI 检测助手的历史页回看 Agent 的检测记录。
 */
class DetectAiImageTool @Inject constructor(
    private val service: AiDetectService,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "detect_ai_image"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_detect_ai_image)

    override val title: String =
        textProvider.string(R.string.agent_tool_detect_ai_image_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_detect_ai_image_summary)

    override val category: ToolCategory = ToolCategory.IMAGE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_detect_ai_image_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_detect_ai_image_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "image_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_detect_ai_image_param_image_uri)
            )
        ),
        required = listOf("image_uri")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val args = try {
            AppJson.decodeFromString<DetectAiImageArgs>(arguments)
        } catch (_: Exception) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_detect_ai_image_error_args),
                isError = true
            )
        }
        val imageUri = args?.image_uri?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_detect_ai_image_error_args),
                isError = true
            )

        return service.detectImage(Uri.parse(imageUri)).fold(
            onSuccess = { result ->
                val verdict = AiDetectService.verdictKey(result.confidence)
                val payload = LinkedHashMap<String, Any?>().apply {
                    put("verdict", verdict)
                    put("verdict_label", verdictLabel(verdict))
                    put("confidence", result.confidence)
                }
                AgentToolResult(content = jsonStringOf(payload))
            },
            onFailure = { error ->
                AgentToolResult(content = errorMessage(error), isError = true)
            }
        )
    }

    private fun verdictLabel(verdict: String): String = textProvider.string(
        when (verdict) {
            AiDetectService.VERDICT_LIKELY_AI -> R.string.ai_detect_verdict_likely_ai
            AiDetectService.VERDICT_MAYBE_AI -> R.string.ai_detect_verdict_maybe_ai
            else -> R.string.ai_detect_verdict_likely_human
        }
    )

    private fun errorMessage(error: Throwable): String = textProvider.string(
        when ((error as? AiDetectException)?.error) {
            AiDetectError.IMAGE_TOO_LARGE -> R.string.ai_detect_error_image_too_large
            AiDetectError.IMAGE_TOO_SMALL -> R.string.ai_detect_error_image_too_small
            AiDetectError.IMAGE_UNREADABLE -> R.string.ai_detect_error_image_unreadable
            else -> R.string.ai_detect_error_network
        }
    )

    @Serializable
    private data class DetectAiImageArgs(
        val image_uri: String? = null
    )
}
