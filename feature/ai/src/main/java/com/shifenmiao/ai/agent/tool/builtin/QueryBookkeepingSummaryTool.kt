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
import com.wanbaohe.bookkeeping.service.BookkeepingService
import org.json.JSONObject
import java.time.YearMonth
import javax.inject.Inject

/**
 * 内置工具:`query_bookkeeping_summary` — 查询某月账本的概览(总额 / 分类排行 / 最近账目)。
 */
class QueryBookkeepingSummaryTool @Inject constructor(
    private val service: BookkeepingService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "query_bookkeeping_summary"

    override val description: String =
        textProvider.string(R.string.agent_tool_query_bookkeeping_summary_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_query_bookkeeping_summary_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_query_bookkeeping_summary_summary)

    override val category: ToolCategory = ToolCategory.KNOWLEDGE

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> = listOf("账本", "支出", "收入", "统计", "账单", "消费统计", "花了多少")

    override val examples: List<String> = listOf(
        "查一下本月花了多少钱",
        "看看 2026-04 的账本",
        "最近的几笔账目",
    )

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "month" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_query_bookkeeping_summary_param_month),
            ),
            "recent_limit" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_query_bookkeeping_summary_param_recent_limit),
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val monthStr = json.optString("month").takeIf { it.isNotBlank() }
            val month = monthStr?.let {
                runCatching { YearMonth.parse(it) }.getOrElse { error("month 格式错误,需 yyyy-MM") }
            } ?: YearMonth.now()
            val recentLimit = json.optInt("recent_limit", 20).coerceIn(1, 50)

            val summary = service.summarizeMonth(month)
            val recent = service.listRecordsInRange(
                startDate = month.atDay(1),
                endDate = month.atEndOfMonth(),
                limit = recentLimit,
            )
            val bookkeepingDeeplink = bookkeepingDeeplink()
            val addRecordDeeplink = bookkeepingAddRecordDeeplink()

            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendMarkdownBullet(
                        label = textProvider.string(R.string.agent_tool_bookkeeping_label_month),
                        value = month.toString(),
                    )
                    appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_summary)) {
                        appendMarkdownBullet(
                            label = textProvider.string(R.string.agent_tool_bookkeeping_label_expense),
                            value = "¥${centsToYuanText(summary.expenseCents)}",
                        )
                        appendMarkdownBullet(
                            label = textProvider.string(R.string.agent_tool_bookkeeping_label_income),
                            value = "¥${centsToYuanText(summary.incomeCents)}",
                        )
                        appendMarkdownBullet(
                            label = textProvider.string(R.string.agent_tool_bookkeeping_label_balance),
                            value = "¥${centsToYuanText(summary.incomeCents - summary.expenseCents)}",
                        )
                        appendMarkdownBullet(
                            label = textProvider.string(R.string.agent_tool_bookkeeping_label_record_count),
                            value = summary.recordCount.toString(),
                        )
                    }
                    if (summary.expenseByCategory.isNotEmpty()) {
                        appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_expense_categories)) {
                            summary.expenseByCategory.take(10).forEach { item ->
                                appendLine("- ${sanitizeMarkdownText(item.categoryName)} · ¥${centsToYuanText(item.amountCents)}")
                            }
                        }
                    }
                    if (summary.incomeByCategory.isNotEmpty()) {
                        appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_income_categories)) {
                            summary.incomeByCategory.take(10).forEach { item ->
                                appendLine("- ${sanitizeMarkdownText(item.categoryName)} · ¥${centsToYuanText(item.amountCents)}")
                            }
                        }
                    }
                    if (recent.isNotEmpty()) {
                        appendMarkdownSection(textProvider.string(R.string.agent_tool_bookkeeping_section_recent_records)) {
                            recent.forEachIndexed { index, rec ->
                                val editDeeplink = bookkeepingAddRecordDeeplink(rec.id)
                                appendLine("${index + 1}. ${rec.happenedDate} · ${rec.type.displayName(textProvider)} · ¥${centsToYuanText(rec.amountCents)} · ${categoryNameOrDefault(rec.categoryName, textProvider)}")
                                rec.note?.let {
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
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_query_bookkeeping_summary_failed,
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
