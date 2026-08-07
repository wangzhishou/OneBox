package com.wanbaohe.calendar.ai.tool

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.calendar.R
import com.wanbaohe.calendar.data.AuspiciousDayFinder
import java.util.Calendar
import javax.inject.Inject

/**
 * AI Agent 工具：择日查询
 *
 * 按事项搜索未来吉日或需要避开的忌日。
 */
class AuspiciousDayTool @Inject constructor(
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "auspicious_day_query"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_auspicious_day)

    override val title: String =
        textProvider.string(R.string.agent_tool_auspicious_day_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_auspicious_day_summary)

    override val category: ToolCategory = ToolCategory.KNOWLEDGE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_auspicious_day_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_auspicious_day_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "items" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_auspicious_day_param_items)
            ),
            "isAvoidMode" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_auspicious_day_param_is_avoid)
            ),
            "rangeDays" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_auspicious_day_param_range)
            )
        ),
        required = listOf("items")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val args = try {
            Gson().fromJson(arguments, AuspiciousArgs::class.java)
        } catch (_: Exception) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_auspicious_day_error_param),
                isError = true
            )
        }

        val items = args.items ?: emptyList()
        if (items.isEmpty()) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_auspicious_day_no_result),
                isError = true
            )
        }

        val isAvoidMode = args.isAvoidMode ?: false
        val rangeDays = (args.rangeDays ?: 90).coerceIn(1, 365)

        return try {
            val now = Calendar.getInstance()
            val results = AuspiciousDayFinder.findDays(
                startYear = now.get(Calendar.YEAR),
                startMonth = now.get(Calendar.MONTH) + 1,
                startDay = now.get(Calendar.DAY_OF_MONTH),
                rangeDays = rangeDays,
                selectedItems = items.toSet(),
                isAvoidMode = isAvoidMode
            )

            if (results.isEmpty()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_auspicious_day_no_result)
                )
            }

            val modeLabel = if (isAvoidMode) "忌日" else "吉日"
            val result = buildString {
                appendLine("# ${modeLabel}查询结果")
                appendLine()
                appendLine("搜索事项：${items.joinToString(", ")}")
                appendLine("搜索范围：未来 ${rangeDays} 天")
                appendLine()
                appendLine("共找到 ${results.size} 个${modeLabel}：")
                appendLine()
                results.forEach { r ->
                    appendLine("- **${r.solarYear}年${r.solarMonth}月${r.solarDay}日**（${r.lunarMonthName}${r.lunarDayName}）")
                    appendLine("  干支：${r.ganZhiDay}，匹配事项：${r.matchedItems.joinToString(", ")}")
                }
            }

            val deeplink = buildString {
                append("onebox://screen/calendar?type=auspicious")
                if (isAvoidMode) append("&avoid=true")
            }
            AgentToolResult(
                content = result.trim() + "\n\n---\n\n[打开择日查询]($deeplink)"
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(R.string.agent_tool_auspicious_day_error),
                isError = true
            )
        }
    }

    private data class AuspiciousArgs(
        val items: List<String>? = null,
        val isAvoidMode: Boolean? = false,
        val rangeDays: Int? = 90
    )
}
