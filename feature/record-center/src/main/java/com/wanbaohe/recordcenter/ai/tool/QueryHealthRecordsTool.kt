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

package com.wanbaohe.recordcenter.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.registry.RecordTypeCatalog
import com.wanbaohe.recordcenter.service.RecordCenterService
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * AI Agent 工具:`query_health_records` — 查询某类型近 N 天的记录与统计(平均/最高/最低)。
 */
class QueryHealthRecordsTool @Inject constructor(
    private val service: RecordCenterService,
    catalog: RecordTypeCatalog,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    private val schemaSupport = RecordCenterToolSchemaSupport(catalog, textProvider)

    override val name: String = "query_health_records"

    override val description: String =
        textProvider.string(R.string.agent_tool_query_health_records_description) +
            "\n" + schemaSupport.buildTypeFieldsDescription()

    override val title: String =
        textProvider.string(R.string.agent_tool_query_health_records_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_query_health_records_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_query_health_records_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_query_health_records_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "record_type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_health_records_param_record_type),
                enum = schemaSupport.recordTypeEnum(),
            ),
            "days" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_query_health_records_param_days),
            ),
        ),
        required = listOf("record_type"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val typeKey = json.optString("record_type").takeIf { it.isNotBlank() }
                ?: error(textProvider.string(R.string.record_center_error_unknown_type, ""))
            val definition = schemaSupport.requireType(typeKey)
            val days = json.optInt("days").takeIf { it > 0 } ?: DEFAULT_DAYS

            val now = System.currentTimeMillis()
            val from = now - days * DAY_MILLIS
            val records = service.listRange(typeKey, from, now)

            val deeplink = recordCenterListDeeplink(typeKey)
            val deepLinks = listOf(
                toolDeepLinkJson(
                    uri = deeplink,
                    label = textProvider.string(R.string.agent_tool_record_center_open_link),
                    primary = true,
                )
            )

            if (records.isEmpty()) {
                return recordCenterSuccessResult(
                    markdown = buildString {
                        appendLine("# ${sanitizeMarkdownText(title)}")
                        appendLine()
                        appendLine(textProvider.string(R.string.agent_tool_query_health_records_empty))
                        appendLine()
                        appendLine(
                            "- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_record_center_open_link), deeplink)}"
                        )
                    }.trimEnd(),
                    deepLinks = deepLinks,
                ) {
                    put("action", "query")
                    put("record_type", typeKey)
                    put("count", 0)
                }
            }

            // 统计口径:chartFieldKeys(即图上绘制的字段)
            val decoded = records.map { it to RecordFieldsCodec.decode(it.fieldsJson) }
            val stats = definition.chartFieldKeys.mapNotNull { fieldKey ->
                val field = definition.fields.firstOrNull { it.key == fieldKey } ?: return@mapNotNull null
                val values = decoded.mapNotNull { (_, fields) -> fields[fieldKey] }
                if (values.isEmpty()) return@mapNotNull null
                val unit = field.unit.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
                Triple(field, values, unit)
            }

            recordCenterSuccessResult(
                markdown = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine(
                        "- **${sanitizeMarkdownText(textProvider.string(definition.titleRes))}** · " +
                            "${textProvider.string(R.string.record_center_range_days, days)} · " +
                            "${records.size}"
                    )
                    definition.referenceRangeRes?.let {
                        appendLine("- ${textProvider.string(it)}")
                    }
                    appendLine()

                    if (stats.isNotEmpty()) {
                        appendLine("## ${textProvider.string(R.string.agent_tool_query_health_records_section_stats)}")
                        stats.forEach { (field, values, unit) ->
                            appendLine(
                                "- ${sanitizeMarkdownText(textProvider.string(field.labelRes))}: " +
                                    "${textProvider.string(R.string.record_center_stats_average)} " +
                                    "${RecordFieldsCodec.formatStatsValue(values.average().toFloat())}$unit · " +
                                    "${textProvider.string(R.string.record_center_stats_max)} " +
                                    "${RecordFieldsCodec.formatStatsValue(values.max())}$unit · " +
                                    "${textProvider.string(R.string.record_center_stats_min)} " +
                                    "${RecordFieldsCodec.formatStatsValue(values.min())}$unit"
                            )
                        }
                        appendLine()
                    }

                    appendLine("## ${textProvider.string(R.string.agent_tool_query_health_records_section_records)}")
                    decoded.sortedByDescending { it.first.happenedAt }
                        .take(MAX_LISTED_RECORDS)
                        .forEach { (entity, fields) ->
                            val timeText = Instant.ofEpochMilli(entity.happenedAt)
                                .atZone(ZoneId.systemDefault())
                                .format(DATE_FORMATTER)
                            val valuesText = definition.fields.mapNotNull { field ->
                                fields[field.key]?.let { value ->
                                    val unit = field.unit.takeIf { u -> u.isNotBlank() }
                                        ?.let { " $it" }.orEmpty()
                                    "${textProvider.string(field.labelRes)} ${RecordFieldsCodec.formatValue(value)}$unit"
                                }
                            }.joinToString(", ")
                            val noteText = entity.note?.takeIf { it.isNotBlank() }
                                ?.let { " · ${sanitizeMarkdownText(it)}" }.orEmpty()
                            appendLine("- $timeText · $valuesText$noteText")
                        }
                    appendLine()
                    appendLine(
                        "- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_record_center_open_link), deeplink)}"
                    )
                }.trimEnd(),
                deepLinks = deepLinks,
            ) {
                put("action", "query")
                put("record_type", typeKey)
                put("days", days)
                put("count", records.size)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_query_health_records_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_record_center_unknown_error),
                ),
                isError = true,
            )
        }
    }

    companion object {
        private const val DEFAULT_DAYS = 30
        private const val DAY_MILLIS = 24L * 60 * 60 * 1000
        private const val MAX_LISTED_RECORDS = 30

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
