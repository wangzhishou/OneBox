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
import com.shifenmiao.database.schedule.entity.ScheduleEventEntity
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.schedule.R
import com.wanbaohe.schedule.service.ScheduleService
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import org.json.JSONArray
import org.json.JSONObject

/**
 * AI Agent 工具:`query_schedule_events` — 按日期范围查询本地日程事件。
 *
 * 范围按闭区间日期计算:start_date 缺省为今天,end_date 缺省为 start_date 起 6 天后
 * (即默认查询含起点的 7 天)。结果按开始时间升序,最多返回 50 条。
 */
class QueryScheduleEventsTool @Inject constructor(
    private val service: ScheduleService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "query_schedule_events"

    override val description: String =
        textProvider.string(R.string.agent_tool_query_schedule_events_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_query_schedule_events_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_query_schedule_events_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_query_schedule_events_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_query_schedule_events_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "start_date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_schedule_events_param_start_date),
            ),
            "end_date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_schedule_events_param_end_date),
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val zone = ZoneId.systemDefault()

            val startDateText = json.optString("start_date").trim()
            val startDate = if (startDateText.isNotEmpty()) {
                parseIsoDate(startDateText)
                    ?: error(textProvider.string(R.string.agent_tool_schedule_invalid_date, startDateText))
            } else {
                LocalDate.now(zone)
            }

            val endDateText = json.optString("end_date").trim()
            val endDate = if (endDateText.isNotEmpty()) {
                parseIsoDate(endDateText)
                    ?: error(textProvider.string(R.string.agent_tool_schedule_invalid_date, endDateText))
            } else {
                startDate.plusDays(DEFAULT_RANGE_DAYS - 1L)
            }
            require(!endDate.isBefore(startDate)) {
                textProvider.string(R.string.agent_tool_query_schedule_events_end_before_start)
            }

            val rangeStartMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val rangeEndMillis = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val events = service.listEventsInRange(rangeStartMillis, rangeEndMillis)
                .take(MAX_RESULTS)

            val deeplink = scheduleScreenDeeplink()
            scheduleSuccessResult(
                markdown = buildMarkdown(events, startDate, endDate, deeplink),
                deepLinks = listOf(
                    toolDeepLinkJson(
                        uri = deeplink,
                        label = textProvider.string(R.string.agent_tool_schedule_open_link),
                        primary = true,
                    )
                ),
            ) {
                put("startDate", startDate.toString())
                put("endDate", endDate.toString())
                put("count", events.size)
                put("events", JSONArray().apply {
                    events.forEach { event ->
                        put(JSONObject().apply {
                            put("id", event.id)
                            put("title", event.title)
                            put("start", formatUtcMillis(event.startUtcMillis))
                            put("end", formatUtcMillis(event.endUtcMillis))
                            put("allDay", event.isAllDay)
                            event.location?.let { put("location", it) }
                            event.description?.let { put("description", it) }
                        })
                    }
                })
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_query_schedule_events_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_schedule_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun buildMarkdown(
        events: List<ScheduleEventEntity>,
        startDate: LocalDate,
        endDate: LocalDate,
        deeplink: String,
    ): String {
        return buildString {
            appendLine("# ${sanitizeMarkdownText(title)} ($startDate ~ $endDate)")
            appendLine()
            if (events.isEmpty()) {
                appendLine(textProvider.string(R.string.agent_tool_query_schedule_events_empty))
            } else {
                events.forEachIndexed { index, event ->
                    val timeText = if (event.isAllDay) {
                        formatUtcMillis(event.startUtcMillis).substringBefore(' ')
                    } else {
                        formatUtcMillis(event.startUtcMillis)
                    }
                    appendLine("${index + 1}. $timeText · ${sanitizeMarkdownText(event.title)}")
                    appendLine("   - id: ${event.id}")
                    event.location?.let {
                        appendLine("   - ${textProvider.string(R.string.agent_tool_schedule_label_location)}: ${sanitizeMarkdownText(it)}")
                    }
                }
            }
            appendLine()
            appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_schedule_open_link), deeplink)}")
        }.trimEnd()
    }

    companion object {
        private const val DEFAULT_RANGE_DAYS = 7L
        private const val MAX_RESULTS = 50
    }
}
