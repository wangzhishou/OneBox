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

package com.wanbaohe.schedule.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.schedule.R
import com.wanbaohe.schedule.service.ScheduleService
import javax.inject.Inject
import org.json.JSONObject

/**
 * AI Agent 工具:`delete_schedule_event` — 按事件 id 删除一条本地日程事件。
 *
 * 删除会连带清除系统日历等 provider 绑定,属于不可逆操作,
 * 因此 requiresConfirmation=true,执行前必须由用户确认。
 * event_id 通常来自 `query_schedule_events` 的查询结果。
 */
class DeleteScheduleEventTool @Inject constructor(
    private val service: ScheduleService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "delete_schedule_event"

    override val description: String =
        textProvider.string(R.string.agent_tool_delete_schedule_event_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_delete_schedule_event_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_delete_schedule_event_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val confirmationTitle: String =
        textProvider.string(R.string.agent_tool_delete_schedule_event_confirm_title)

    override val confirmationToolPresentation: String =
        textProvider.string(R.string.agent_tool_delete_schedule_event_confirm_presentation)

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_delete_schedule_event_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_delete_schedule_event_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "event_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_delete_schedule_event_param_event_id),
            ),
        ),
        required = listOf("event_id"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val eventId = json.optString("event_id").trim()
            require(eventId.isNotEmpty()) {
                textProvider.string(R.string.agent_tool_delete_schedule_event_id_required)
            }

            val event = service.getEvent(eventId)
                ?: error(textProvider.string(R.string.agent_tool_delete_schedule_event_not_found, eventId))
            service.deleteEventWithProviderBindings(eventId).getOrThrow()

            val deeplink = scheduleScreenDeeplink()
            scheduleSuccessResult(
                markdown = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine(
                        textProvider.string(
                            R.string.agent_tool_delete_schedule_event_deleted,
                            sanitizeMarkdownText(event.title),
                        )
                    )
                    appendLine()
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_schedule_open_link), deeplink)}")
                }.trimEnd(),
                deepLinks = listOf(
                    toolDeepLinkJson(
                        uri = deeplink,
                        label = textProvider.string(R.string.agent_tool_schedule_open_link),
                        primary = true,
                    )
                ),
            ) {
                put("eventId", eventId)
                put("deletedTitle", event.title)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_delete_schedule_event_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_schedule_unknown_error),
                ),
                isError = true,
            )
        }
    }
}
