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
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.registry.RecordTypeCatalog
import com.wanbaohe.recordcenter.service.RecordCenterService
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * AI Agent 工具:`get_health_summary` — 各记录类型最新一条数据的总览(含单位与参考范围)。
 */
class GetHealthSummaryTool @Inject constructor(
    private val service: RecordCenterService,
    private val catalog: RecordTypeCatalog,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "get_health_summary"

    override val description: String =
        textProvider.string(R.string.agent_tool_get_health_summary_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_get_health_summary_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_get_health_summary_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_get_health_summary_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_get_health_summary_examples)

    override val parametersSchema: ToolParameters = ToolParameters()

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val latestByType = service.latestPerType().associateBy { it.type }

            if (latestByType.isEmpty()) {
                val deeplink = recordCenterListDeeplink()
                return recordCenterSuccessResult(
                    markdown = buildString {
                        appendLine("# ${sanitizeMarkdownText(title)}")
                        appendLine()
                        appendLine(textProvider.string(R.string.agent_tool_get_health_summary_empty))
                        appendLine()
                        appendLine(
                            "- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_record_center_open_link), deeplink)}"
                        )
                    }.trimEnd(),
                    deepLinks = listOf(
                        toolDeepLinkJson(
                            uri = deeplink,
                            label = textProvider.string(R.string.agent_tool_record_center_open_link),
                            primary = true,
                        )
                    ),
                ) {
                    put("action", "summary")
                    put("count", 0)
                }
            }

            // 有数据的类型各出一条跳转链接(按目录顺序,首个为 primary)
            val involvedTypes = catalog.all().filter { latestByType.containsKey(it.key) }
            val deepLinks = involvedTypes.mapIndexed { order, definition ->
                toolDeepLinkJson(
                    uri = recordCenterListDeeplink(definition.key),
                    label = textProvider.string(definition.titleRes),
                    primary = order == 0,
                )
            }

            recordCenterSuccessResult(
                markdown = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    involvedTypes.forEach { definition ->
                        val entity = latestByType.getValue(definition.key)
                        val fields = RecordFieldsCodec.decode(entity.fieldsJson)
                        val timeText = Instant.ofEpochMilli(entity.happenedAt)
                            .atZone(ZoneId.systemDefault())
                            .format(DATE_FORMATTER)
                        appendLine("## ${sanitizeMarkdownText(textProvider.string(definition.titleRes))}")
                        definition.fields.forEach { field ->
                            val value = fields[field.key] ?: return@forEach
                            val unit = field.unit.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
                            appendLine(
                                "- ${sanitizeMarkdownText(textProvider.string(field.labelRes))}: " +
                                    "${RecordFieldsCodec.formatValue(value)}$unit"
                            )
                        }
                        appendLine("- ${textProvider.string(R.string.record_center_label_time)}: $timeText")
                        definition.referenceRangeRes?.let {
                            appendLine("- ${textProvider.string(it)}")
                        }
                        appendLine()
                    }
                    involvedTypes.forEach { definition ->
                        appendLine(
                            "- ${buildMarkdownLink(textProvider.string(definition.titleRes), recordCenterListDeeplink(definition.key))}"
                        )
                    }
                }.trimEnd(),
                deepLinks = deepLinks,
            ) {
                put("action", "summary")
                put("count", involvedTypes.size)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_get_health_summary_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_record_center_unknown_error),
                ),
                isError = true,
            )
        }
    }

    companion object {
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
