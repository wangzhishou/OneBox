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

package com.wanbaohe.period.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.period.R
import com.wanbaohe.period.model.PeriodFlow
import com.wanbaohe.period.model.PeriodMood
import com.wanbaohe.period.service.PeriodService
import org.json.JSONObject
import javax.inject.Inject
class UpdatePeriodRecordTool @Inject constructor(
    private val service: PeriodService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "update_period_record"

    override val description: String =
        textProvider.string(R.string.agent_tool_update_period_record_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_update_period_record_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_update_period_record_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_update_period_record_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_update_period_record_examples)

    override val requiresConfirmation: Boolean = true

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "record_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_period_param_record_id),
            ),
            "date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_period_param_date),
            ),
            "is_period_start" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_period_param_is_start),
            ),
            "is_period_end" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_period_param_is_end),
            ),
            "flow_intensity" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_period_param_flow),
            ),
            "symptoms" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_period_param_symptoms),
            ),
            "mood" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_period_param_mood),
            ),
            "note" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_period_param_note),
            ),
        ),
        required = listOf("record_id"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val recordId = json.optString("record_id").trim()
            require(recordId.isNotEmpty()) { "record_id 不能为空" }
            val existing = service.getRecordUi(recordId) ?: error("period_record_not_found")

            val input = PeriodService.RecordInput(
                recordDate = if (json.has("date")) {
                    AddPeriodRecordTool.parseDate(json.optString("date"))
                } else {
                    existing.recordDate
                },
                isPeriodStart = json.optBoolean("is_period_start", existing.isPeriodStart),
                isPeriodEnd = json.optBoolean("is_period_end", existing.isPeriodEnd),
                flowIntensity = if (json.has("flow_intensity")) {
                    PeriodFlow.fromName(json.optString("flow_intensity"))
                } else {
                    existing.flowIntensity
                },
                symptoms = if (json.has("symptoms")) {
                    AddPeriodRecordTool.parseSymptoms(json.optJSONArray("symptoms"))
                } else {
                    existing.symptoms
                },
                mood = if (json.has("mood")) {
                    PeriodMood.fromName(json.optString("mood"))
                } else {
                    existing.mood
                },
                note = if (json.has("note")) {
                    json.optString("note").takeIf { it.isNotBlank() }
                } else {
                    existing.note
                },
            )
            service.updateRecord(
                recordId = recordId,
                input = input,
                actor = PeriodService.ACTOR_AGENT,
                source = PeriodService.SOURCE_AGENT_UPDATE,
            ).getOrThrow()
            ActionUtils.showToast(R.string.period_toast_recorded)

            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine(
                        "- **${textProvider.string(R.string.agent_tool_period_label_date)}**: " +
                            "${input.recordDate}"
                    )
                    appendLine(
                        "- **${textProvider.string(R.string.agent_tool_period_label_flow)}**: " +
                            input.flowIntensity.name
                    )
                    appendLine()
                    appendLine(
                        "- ${buildMarkdownLink(
                            textProvider.string(R.string.agent_tool_period_detail_link),
                            AddPeriodRecordTool.periodDetailDeeplink(recordId),
                        )}"
                    )
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_update_period_record_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_period_unknown_error),
                ),
                isError = true,
            )
        }
    }
}
