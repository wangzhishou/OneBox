package com.wanbaohe.calendar.ai.tool

import com.google.gson.Gson
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.calendar.R
import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.calendar.data.LunarJavaBridge
import com.wanbaohe.calendar.data.YiJiCalculator
import javax.inject.Inject

/**
 * AI Agent 工具：农历查询
 *
 * 查询指定公历日期的农历信息，包括干支、生肖、节气、宜忌、时辰吉凶等。
 */
class LunarCalendarTool @Inject constructor(
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "lunar_calendar_query"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_lunar_calendar)

    override val title: String =
        textProvider.string(R.string.agent_tool_lunar_calendar_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_lunar_calendar_summary)

    override val category: ToolCategory = ToolCategory.KNOWLEDGE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_lunar_calendar_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_lunar_calendar_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "year" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_lunar_calendar_param_year)
            ),
            "month" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_lunar_calendar_param_month)
            ),
            "day" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_lunar_calendar_param_day)
            )
        ),
        required = listOf("year", "month", "day")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val args = try {
            Gson().fromJson(arguments, LunarCalendarArgs::class.java)
        } catch (_: Exception) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_lunar_calendar_error_date),
                isError = true
            )
        }

        val year = args.year
        val month = args.month
        val day = args.day

        if (year < 1900 || year > 2100 || month < 1 || month > 12 || day < 1 || day > 31) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_lunar_calendar_error_date),
                isError = true
            )
        }

        return try {
            val lunar = LunarCalendarCalculator.solarToLunar(year, month, day)
            val yiJi = YiJiCalculator.getYiJi(year, month, day)
            val timeSlots = LunarJavaBridge.getTimeSlots(year, month, day)

            val result = buildString {
                appendLine("# ${year}年${month}月${day}日 农历信息")
                appendLine()
                appendLine("## 农历日期")
                appendLine("- 农历：${lunar.year}年 ${lunar.monthName}${if (lunar.isLeapMonth) "(闰)" else ""} ${lunar.dayName}")
                appendLine("- 干支：${lunar.ganZhiYear}年 ${lunar.ganZhiMonth}月 ${lunar.ganZhiDay}日")
                appendLine("- 生肖：${lunar.zodiac}")
                appendLine("- 星座：${lunar.constellation}")
                appendLine()
                appendLine("## 节气与节日")
                lunar.solarTerm?.takeIf { it.isNotBlank() }?.let {
                    appendLine("- 今日节气：$it")
                }
                lunar.solarFestival?.takeIf { it.isNotBlank() }?.let {
                    appendLine("- 阳历节日：$it")
                }
                lunar.lunarFestival?.takeIf { it.isNotBlank() }?.let {
                    appendLine("- 农历节日：$it")
                }
                appendLine()
                appendLine("## 宜忌")
                appendLine("- 宜：${yiJi.yi.joinToString(", ")}")
                appendLine("- 忌：${yiJi.ji.joinToString(", ")}")
                appendLine()
                appendLine("## 时辰吉凶")
                timeSlots.take(6).forEach { slot ->
                    appendLine("- ${slot.ganZhi} (${slot.timeRange})：${slot.luck}")
                }
                appendLine()
                appendLine("## 其他信息")
                appendLine("- 二十八宿：${lunar.star28}")
                appendLine("- 建除十二值：${lunar.jianChu}")
                appendLine("- 纳音五行：${lunar.naYin}")
                appendLine("- 彭祖百忌：${lunar.pengZuGan} ${lunar.pengZuZhi}")
                appendLine("- 冲煞：${lunar.chong} ${lunar.sha}")
                appendLine("- 值神：${lunar.zhiShen}")
                appendLine("- 喜神方位：${lunar.xiShen}")
                appendLine("- 福神方位：${lunar.fuShen}")
                appendLine("- 财神方位：${lunar.caiShen}")
            }

            val deeplink = "onebox://screen/calendar?type=calendar&year=${args.year}&month=${args.month}&day=${args.day}"
            AgentToolResult(
                content = result.trim() + "\n\n---\n\n[打开万年历查看此日]($deeplink)"
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(R.string.agent_tool_lunar_calendar_error_date),
                isError = true
            )
        }
    }

    private data class LunarCalendarArgs(
        val year: Int = 0,
        val month: Int = 0,
        val day: Int = 0
    )
}
