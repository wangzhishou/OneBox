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
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import org.json.JSONObject

/**
 * AI Agent 工具:`add_schedule_event` — 创建一条本地日程事件。
 *
 * 时间参数接受 ISO 字符串(带偏移的 ISO_OFFSET_DATE_TIME、
 * 本地 ISO 日期时间 `yyyy-MM-dd'T'HH:mm`、或纯日期 `yyyy-MM-dd`),
 * 非法输入返回 isError 并给出格式说明。
 */
class AddScheduleEventTool @Inject constructor(
    private val service: ScheduleService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "add_schedule_event"

    override val description: String =
        textProvider.string(R.string.agent_tool_add_schedule_event_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_add_schedule_event_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_add_schedule_event_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_add_schedule_event_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_add_schedule_event_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_schedule_event_param_title),
            ),
            "start" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_schedule_event_param_start),
            ),
            "end" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_schedule_event_param_end),
            ),
            "all_day" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_add_schedule_event_param_all_day),
            ),
            "description" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_schedule_event_param_description),
            ),
            "location" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_schedule_event_param_location),
            ),
        ),
        required = listOf("title", "start"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val title = json.optString("title").trim()
            require(title.isNotEmpty()) {
                textProvider.string(R.string.agent_tool_add_schedule_event_title_required)
            }

            val startText = json.optString("start").trim()
            val startMillis = parseIsoToUtcMillis(startText)
                ?: error(textProvider.string(R.string.agent_tool_schedule_invalid_datetime, startText))

            val isAllDay = json.optBoolean("all_day", false)
            val endText = json.optString("end").trim()
            val endMillis = if (endText.isNotEmpty()) {
                parseIsoToUtcMillis(endText)
                    ?: error(textProvider.string(R.string.agent_tool_schedule_invalid_datetime, endText))
            } else {
                defaultEndMillis(startMillis, isAllDay)
            }

            val eventId = service.createLocalEvent(
                input = ScheduleService.EventInput(
                    title = title,
                    description = json.optString("description").takeIf { it.isNotBlank() },
                    location = json.optString("location").takeIf { it.isNotBlank() },
                    startUtcMillis = startMillis,
                    endUtcMillis = endMillis,
                    isAllDay = isAllDay,
                    timeZoneId = ZoneId.systemDefault().id,
                ),
                source = "${ScheduleService.SOURCE_AGENT}:add_schedule_event",
            ).getOrThrow()

            val deeplink = scheduleScreenDeeplink()
            scheduleSuccessResult(
                markdown = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine("- ${textProvider.string(R.string.agent_tool_schedule_label_start)}: ${formatUtcMillis(startMillis)}")
                    appendLine("- ${textProvider.string(R.string.agent_tool_schedule_label_end)}: ${formatUtcMillis(endMillis)}")
                    if (isAllDay) {
                        appendLine("- ${textProvider.string(R.string.agent_tool_schedule_label_all_day)}")
                    }
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
                put("start", formatUtcMillis(startMillis))
                put("end", formatUtcMillis(endMillis))
                put("allDay", isAllDay)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_add_schedule_event_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_schedule_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun defaultEndMillis(startMillis: Long, isAllDay: Boolean): Long {
        val zone = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(startMillis).atZone(zone)
        val end = if (isAllDay) start.plusDays(1) else start.plusHours(1)
        return end.toInstant().toEpochMilli()
    }
}
