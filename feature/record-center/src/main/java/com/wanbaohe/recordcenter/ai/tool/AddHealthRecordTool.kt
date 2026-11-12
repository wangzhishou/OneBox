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
import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.registry.RecordTypeCatalog
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import com.wanbaohe.recordcenter.service.RecordCenterService
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * AI Agent 工具:`add_health_record` — 添加健康记录,支持单条与批量两种模式。
 *
 * 单条模式:顶层传 record_type + values(可选 happened_at / note)。
 * 批量模式:传 records 数组,每项字段同单条模式;逐条写入、逐条收集成败,
 * 部分失败不影响其它条目,结果汇总成功/失败条数与每条失败原因。
 *
 * record_type 的枚举与描述由 RecordTypeCatalog 动态生成;
 * values 是 字段名→数值 的自由 JSON object(schema 无法表达动态字段,
 * 字段契约写在工具描述与 values 参数描述里,由 Service 做最终校验)。
 */
class AddHealthRecordTool @Inject constructor(
    private val service: RecordCenterService,
    catalog: RecordTypeCatalog,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    private val schemaSupport = RecordCenterToolSchemaSupport(catalog, textProvider)

    override val name: String = "add_health_record"

    override val description: String =
        textProvider.string(R.string.agent_tool_add_health_record_description) +
            "\n" + schemaSupport.buildTypeFieldsDescription()

    override val title: String =
        textProvider.string(R.string.agent_tool_add_health_record_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_add_health_record_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parallelizable: Boolean = false

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_add_health_record_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_add_health_record_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "records" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_add_health_record_param_records),
            ),
            "record_type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_health_record_param_record_type),
                enum = schemaSupport.recordTypeEnum(),
            ),
            "values" to ToolParameterProperty(
                type = "object",
                description = textProvider.string(R.string.agent_tool_add_health_record_param_values),
            ),
            "happened_at" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_add_health_record_param_happened_at),
            ),
            "note" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_health_record_param_note),
            ),
        ),
        // 批量模式下顶层 record_type/values 可缺省,运行时按模式校验
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val records = json.optJSONArray("records")
            if (records != null) {
                executeBatch(records)
            } else {
                executeSingle(json)
            }
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_add_health_record_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_record_center_unknown_error),
                ),
                isError = true,
            )
        }
    }

    // ── 单条模式 ─────────────────────────────────────

    private suspend fun executeSingle(json: JSONObject): AgentToolResult {
        val request = parseRecordPayload(json)
        val entity = service.addRecord(
            typeKey = request.definition.key,
            values = request.values,
            happenedAt = request.happenedAt,
            note = request.note,
            actor = RecordCenterService.ACTOR_AGENT,
        ).getOrThrow()

        val deeplink = recordCenterListDeeplink(request.definition.key)
        val timeText = formatTime(request.happenedAt)

        val markdown = buildString {
            appendLine("# ${sanitizeMarkdownText(title)}")
            appendLine()
            appendLine(
                "- **${sanitizeMarkdownText(textProvider.string(request.definition.titleRes))}** · " +
                    textProvider.string(R.string.agent_tool_add_health_record_success)
            )
            request.definition.fields.forEach { field ->
                val value = RecordFieldsCodec.decode(entity.fieldsJson)[field.key]
                    ?: return@forEach
                val unit = field.unit.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
                appendLine(
                    "- ${sanitizeMarkdownText(textProvider.string(field.labelRes))}: " +
                        "${RecordFieldsCodec.formatValue(value)}$unit"
                )
            }
            appendLine("- **${textProvider.string(R.string.record_center_label_time)}**: $timeText")
            request.note?.let {
                appendLine(
                    "- **${textProvider.string(R.string.record_center_label_note)}**: ${sanitizeMarkdownText(it)}"
                )
            }
            appendLine()
            appendLine(
                "- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_record_center_open_link), deeplink)}"
            )
        }.trimEnd()

        return recordCenterSuccessResult(
            markdown = markdown,
            deepLinks = listOf(
                toolDeepLinkJson(
                    uri = deeplink,
                    label = textProvider.string(R.string.agent_tool_record_center_open_link),
                    primary = true,
                )
            ),
        ) {
            put("action", "add")
            put("record_id", entity.id)
            put("record_type", request.definition.key)
        }
    }

    // ── 批量模式 ─────────────────────────────────────

    private suspend fun executeBatch(records: JSONArray): AgentToolResult {
        require(records.length() > 0) {
            textProvider.string(R.string.agent_tool_add_health_record_batch_empty)
        }

        val successes = mutableListOf<BatchSuccess>()
        val failures = mutableListOf<BatchFailure>()

        for (index in 0 until records.length()) {
            runCatching {
                val item = records.getJSONObject(index)
                val request = parseRecordPayload(item)
                val entity = service.addRecord(
                    typeKey = request.definition.key,
                    values = request.values,
                    happenedAt = request.happenedAt,
                    note = request.note,
                    actor = RecordCenterService.ACTOR_AGENT,
                ).getOrThrow()
                successes += BatchSuccess(index = index, definition = request.definition, entity = entity)
            }.onFailure { error ->
                failures += BatchFailure(
                    index = index,
                    reason = error.message
                        ?: textProvider.string(R.string.agent_tool_record_center_unknown_error),
                )
            }
        }

        // 成功涉及的类型各出一条跳转链接(去重,首个为 primary)
        val involvedTypes = successes.map { it.definition }.distinctBy { it.key }
        val deepLinks = involvedTypes.mapIndexed { order, definition ->
            toolDeepLinkJson(
                uri = recordCenterListDeeplink(definition.key),
                label = textProvider.string(definition.titleRes),
                primary = order == 0,
            )
        }

        val markdown = buildString {
            appendLine("# ${sanitizeMarkdownText(title)}")
            appendLine()
            appendLine(
                "- **${textProvider.string(R.string.agent_tool_add_health_record_batch_success_count, successes.size)}** · " +
                    "**${textProvider.string(R.string.agent_tool_add_health_record_batch_failure_count, failures.size)}**"
            )
            if (successes.isNotEmpty()) {
                appendLine()
                appendLine("## ${textProvider.string(R.string.agent_tool_add_health_record_batch_section_success)}")
                successes.forEachIndexed { order, success ->
                    val valuesText = success.definition.fields.mapNotNull { field ->
                        RecordFieldsCodec.decode(success.entity.fieldsJson)[field.key]?.let { value ->
                            val unit = field.unit.takeIf { it.isNotBlank() }?.let { " $it" }.orEmpty()
                            "${textProvider.string(field.labelRes)} ${RecordFieldsCodec.formatValue(value)}$unit"
                        }
                    }.joinToString(", ")
                    appendLine(
                        "${order + 1}. ${sanitizeMarkdownText(textProvider.string(success.definition.titleRes))} · " +
                            "$valuesText · ${formatTime(success.entity.happenedAt)}"
                    )
                }
            }
            if (failures.isNotEmpty()) {
                appendLine()
                appendLine("## ${textProvider.string(R.string.agent_tool_add_health_record_batch_section_failure)}")
                failures.forEachIndexed { order, failure ->
                    appendLine("${order + 1}. #${failure.index + 1} ${sanitizeMarkdownText(failure.reason)}")
                }
            }
            if (involvedTypes.isNotEmpty()) {
                appendLine()
                involvedTypes.forEach { definition ->
                    appendLine(
                        "- ${buildMarkdownLink(textProvider.string(definition.titleRes), recordCenterListDeeplink(definition.key))}"
                    )
                }
            }
        }.trimEnd()

        return recordCenterSuccessResult(
            markdown = markdown,
            deepLinks = deepLinks,
        ) {
            put("action", "add_batch")
            put("success_count", successes.size)
            put("failure_count", failures.size)
        }.let { result ->
            // 全部失败视为工具执行失败(此时不注入 deepLinks,与框架约定一致)
            if (successes.isEmpty()) result.copy(isError = true) else result
        }
    }

    // ── 参数解析(单条与批量每项共用) ──────────────────

    private fun parseRecordPayload(json: JSONObject): RecordPayload {
        val typeKey = json.optString("record_type").takeIf { it.isNotBlank() }
            ?: error(textProvider.string(R.string.record_center_error_unknown_type, ""))
        val definition = schemaSupport.requireType(typeKey)

        val valuesJson = json.optJSONObject("values")
            ?: error(
                textProvider.string(
                    R.string.record_center_error_missing_field,
                    textProvider.string(definition.fields.first().labelRes),
                )
            )
        val values = buildMap {
            valuesJson.keys().forEach { key ->
                put(key, valuesJson.getDouble(key).toFloat())
            }
        }

        val happenedAt = json.optLong("happened_at").takeIf { it > 0L }
            ?: System.currentTimeMillis()
        val note = json.optString("note").takeIf { it.isNotBlank() }

        return RecordPayload(
            definition = definition,
            values = values,
            happenedAt = happenedAt,
            note = note,
        )
    }

    private fun formatTime(millis: Long): String {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(TIME_FORMATTER)
    }

    private data class RecordPayload(
        val definition: RecordTypeDefinition,
        val values: Map<String, Float>,
        val happenedAt: Long,
        val note: String?,
    )

    private data class BatchSuccess(
        val index: Int,
        val definition: RecordTypeDefinition,
        val entity: HealthRecordEntity,
    )

    private data class BatchFailure(
        val index: Int,
        val reason: String,
    )

    companion object {
        /** 记录中心路由 key,deeplink 契约与 AppNavigationRegistry 注册一致 */
        internal const val RECORD_CENTER_ROUTE_KEY = "record_center"

        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}
