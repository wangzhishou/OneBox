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
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.period.R
import com.wanbaohe.period.service.PeriodService
import org.json.JSONObject
import javax.inject.Inject
class QueryPeriodRecordsTool @Inject constructor(
    private val service: PeriodService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "query_period_records"

    override val description: String =
        textProvider.string(R.string.agent_tool_query_period_records_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_query_period_records_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_query_period_records_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_query_period_records_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_query_period_records_examples)

    override val requiresConfirmation: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "limit" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_period_param_limit),
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val limit = json.optInt("limit", 10).coerceIn(1, 50)
            val records = service.getAllViews(limit = limit)
            val stats = service.getStats()
            val listDeeplink = AddPeriodRecordTool.periodListDeeplink()
            val detailLink = records.firstOrNull()
                ?.let { AddPeriodRecordTool.periodDetailDeeplink(it.id) }

            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine(
                        "- **${textProvider.string(R.string.period_stats_avg_cycle)}**: " +
                            "${stats.averageCycleDays}${textProvider.string(R.string.period_day_unit)}"
                    )
                    appendLine(
                        "- **${textProvider.string(R.string.period_stats_avg_period)}**: " +
                            "${stats.averagePeriodDays}${textProvider.string(R.string.period_day_unit)}"
                    )
                    appendLine(
                        "- **${textProvider.string(R.string.period_stats_next)}**: " +
                            (stats.nextPeriodStart?.toString()
                                ?: textProvider.string(R.string.agent_tool_period_no_next))
                    )
                    appendLine()
                    if (records.isEmpty()) {
                        appendLine(textProvider.string(R.string.agent_tool_query_period_records_empty))
                    } else {
                        records.forEach { record ->
                            val cycleText = record.cycleDay?.let {
                                textProvider.string(R.string.period_cycle_day, it)
                            }.orEmpty()
                            val label = "${record.recordDate} $cycleText"
                            val link = AddPeriodRecordTool.periodDetailDeeplink(record.id)
                            appendLine("- ${buildMarkdownLink(sanitizeMarkdownText(label.trim()), link)}")
                            record.symptoms.take(3).forEach { symptom ->
                                appendLine("  - ${symptom.name}")
                            }
                        }
                    }
                    appendLine()
                    appendLine(
                        "- ${buildMarkdownLink(
                            textProvider.string(R.string.agent_tool_period_open_link),
                            listDeeplink,
                        )}"
                    )
                    if (detailLink != null) {
                        appendLine(
                            "- ${buildMarkdownLink(
                                textProvider.string(R.string.agent_tool_period_detail_link),
                                detailLink,
                            )}"
                        )
                    }
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_query_period_records_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_period_unknown_error),
                ),
                isError = true,
            )
        }
    }
}
