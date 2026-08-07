package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.service.BookkeepingService
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 内置工具:`query_bookkeeping_records` — 查询指定时间范围内的账目明细。
 */
class QueryBookkeepingRecordsTool @Inject constructor(
    private val service: BookkeepingService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "query_bookkeeping_records"

    override val description: String =
        textProvider.string(R.string.agent_tool_query_bookkeeping_records_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_query_bookkeeping_records_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_query_bookkeeping_records_summary)

    override val category: ToolCategory = ToolCategory.KNOWLEDGE

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_query_bookkeeping_records_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_query_bookkeeping_records_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "month" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_bookkeeping_records_param_month),
            ),
            "start_date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_bookkeeping_records_param_start_date),
            ),
            "end_date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_bookkeeping_records_param_end_date),
            ),
            "type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_bookkeeping_records_param_type),
                enum = listOf("expense", "income", "excluded"),
            ),
            "category" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_bookkeeping_records_param_category),
            ),
            "limit" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_query_bookkeeping_records_param_limit),
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val month = json.optString("month").takeIf { it.isNotBlank() }?.let {
                runCatching { YearMonth.parse(it) }.getOrElse { error("month 格式错误,需 yyyy-MM") }
            }
            val defaultMonth = month ?: YearMonth.now()
            val startDate = json.optString("start_date").takeIf { it.isNotBlank() }?.let {
                runCatching { LocalDate.parse(it) }.getOrElse { error("start_date 格式错误,需 yyyy-MM-dd") }
            } ?: defaultMonth.atDay(1)
            val endDate = json.optString("end_date").takeIf { it.isNotBlank() }?.let {
                runCatching { LocalDate.parse(it) }.getOrElse { error("end_date 格式错误,需 yyyy-MM-dd") }
            } ?: defaultMonth.atEndOfMonth()
            require(!endDate.isBefore(startDate)) { "end_date 不能早于 start_date" }

            val type = json.optString("type").takeIf { it.isNotBlank() }?.lowercase()?.let { raw ->
                when (raw) {
                    "expense" -> BookkeepingRecordType.EXPENSE
                    "income" -> BookkeepingRecordType.INCOME
                    "excluded" -> BookkeepingRecordType.EXCLUDED
                    else -> error("未知 type=$raw,允许 expense/income/excluded")
                }
            }
            val category = json.optString("category").trim().takeIf { it.isNotEmpty() }
            val limit = json.optInt("limit", 20).coerceIn(1, 100)

            val result = service.queryRecords(
                startDate = startDate,
                endDate = endDate,
                type = type,
                categoryName = category,
                limit = limit,
            )

            val bookkeepingDeeplink = bookkeepingDeeplink()
            val addRecordDeeplink = bookkeepingAddRecordDeeplink()

            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_summary)) {
                        month?.let {
                            appendMarkdownBullet(
                                label = textProvider.string(R.string.agent_tool_bookkeeping_label_month),
                                value = it.toString(),
                            )
                        }
                        appendMarkdownBullet(
                            label = textProvider.string(R.string.agent_tool_bookkeeping_label_query_range),
                            value = "${result.startDate} ~ ${result.endDate}",
                        )
                        result.type?.let {
                            appendMarkdownBullet(
                                label = textProvider.string(R.string.agent_tool_bookkeeping_label_type),
                                value = it.displayName(textProvider),
                            )
                        }
                        result.categoryName?.let {
                            appendMarkdownBullet(
                                label = textProvider.string(R.string.agent_tool_bookkeeping_label_category),
                                value = it,
                            )
                        }
                        appendMarkdownBullet(
                            label = textProvider.string(R.string.agent_tool_bookkeeping_label_expense),
                            value = "¥${centsToYuanText(result.summary.expenseCents)}",
                        )
                        appendMarkdownBullet(
                            label = textProvider.string(R.string.agent_tool_bookkeeping_label_income),
                            value = "¥${centsToYuanText(result.summary.incomeCents)}",
                        )
                        appendMarkdownBullet(
                            label = textProvider.string(R.string.agent_tool_bookkeeping_label_excluded),
                            value = "¥${centsToYuanText(result.summary.excludedCents)}",
                        )
                        appendMarkdownBullet(
                            label = textProvider.string(R.string.agent_tool_bookkeeping_label_record_count),
                            value = result.summary.recordCount.toString(),
                        )
                    }
                    if (result.records.isNotEmpty()) {
                        appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_records)) {
                            result.records.forEachIndexed { index, record ->
                                val editDeeplink = bookkeepingAddRecordDeeplink(record.id)
                                appendLine("${index + 1}. ${record.happenedDate} · ${record.type.displayName(textProvider)} · ¥${centsToYuanText(record.amountCents)} · ${categoryNameOrDefault(record.categoryName, textProvider)}")
                                record.note?.let {
                                    appendLine("   - ${sanitizeMarkdownText(textProvider.string(R.string.agent_tool_bookkeeping_label_note))}：${sanitizeMarkdownText(it)}")
                                }
                                appendLine("   - ${buildMarkdownLink(textProvider.string(R.string.agent_tool_query_bookkeeping_records_edit_link_label), editDeeplink)}")
                            }
                        }
                    }
                    appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_quick_actions)) {
                        appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_query_bookkeeping_records_open_link_label), bookkeepingDeeplink)}")
                        appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_query_bookkeeping_records_add_link_label), addRecordDeeplink)}")
                    }
                }.trimEnd()
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_query_bookkeeping_records_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true,
            )
        }
    }

    private fun bookkeepingDeeplink(): String {
        return AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = Screen.Bookkeeping().routeKey,
        )
    }

    private fun bookkeepingAddRecordDeeplink(recordId: String? = null): String {
        val params = buildMap<String, String> {
            put("type", "add_record")
            recordId?.takeIf { it.isNotBlank() }?.let { put("editing_record_id", it) }
        }
        return AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = Screen.Bookkeeping().routeKey,
            params = params,
        )
    }

}

