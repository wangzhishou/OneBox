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
import javax.inject.Inject

/**
 * AI Agent 工具：历法转换
 *
 * 公历与农历双向转换，支持闰月。
 */
class LunarConvertTool @Inject constructor(
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "lunar_solar_conversion"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_lunar_convert)

    override val title: String =
        textProvider.string(R.string.agent_tool_lunar_convert_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_lunar_convert_summary)

    override val category: ToolCategory = ToolCategory.KNOWLEDGE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_lunar_convert_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_lunar_convert_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "direction" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_lunar_convert_param_direction),
                enum = listOf("solar_to_lunar", "lunar_to_solar")
            ),
            "year" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_lunar_convert_param_year)
            ),
            "month" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_lunar_convert_param_month)
            ),
            "day" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_lunar_convert_param_day)
            ),
            "isLeapMonth" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_lunar_convert_param_is_leap)
            )
        ),
        required = listOf("direction", "year", "month", "day")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val args = try {
            Gson().fromJson(arguments, ConvertArgs::class.java)
        } catch (_: Exception) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_lunar_convert_error),
                isError = true
            )
        }

        val direction = args.direction ?: "solar_to_lunar"
        val year = args.year
        val month = args.month
        val day = args.day

        if (year < 1900 || year > 2100 || month < 1 || month > 12 || day < 1 || day > 31) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_lunar_convert_error),
                isError = true
            )
        }

        return try {
            when (direction) {
                "solar_to_lunar" -> {
                    val lunar = LunarCalendarCalculator.solarToLunar(year, month, day)
                    val timeSlots = LunarJavaBridge.getTimeSlots(year, month, day)

                    val result = buildString {
                        appendLine("# 公历 → 农历转换结果")
                        appendLine()
                        appendLine("**输入**：${year}年${month}月${day}日")
                        appendLine()
                        appendLine("**农历日期**：${lunar.year}年 ${lunar.monthName}${if (lunar.isLeapMonth) "(闰)" else ""} ${lunar.dayName}")
                        appendLine("**干支**：${lunar.ganZhiYear}年 ${lunar.ganZhiMonth}月 ${lunar.ganZhiDay}日")
                        appendLine("**生肖**：${lunar.zodiac}")
                        appendLine("**星座**：${lunar.constellation}")
                        lunar.solarTerm?.takeIf { it.isNotBlank() }?.let {
                            appendLine("**节气**：$it")
                        }
                        appendLine()
                        appendLine("**时辰**：")
                        timeSlots.forEach { slot ->
                            appendLine("- ${slot.ganZhi} (${slot.timeRange})：${slot.luck}")
                        }
                    }
                    val deeplink = "onebox://screen/calendar?type=convert&direction=solar_to_lunar"
                    AgentToolResult(
                        content = result.trim() + "\n\n---\n\n[打开历法转换]($deeplink)"
                    )
                }
                "lunar_to_solar" -> {
                    val isLeap = args.isLeapMonth ?: false
                    val solar = LunarJavaBridge.lunarToSolarDate(year, month, day, isLeap)
                        ?: return AgentToolResult(
                            content = textProvider.string(R.string.agent_tool_lunar_convert_error),
                            isError = true
                        )
                    val lunar = LunarCalendarCalculator.solarToLunar(solar.year, solar.month, solar.day)
                    val timeSlots = LunarJavaBridge.getTimeSlots(solar.year, solar.month, solar.day)

                    val result = buildString {
                        appendLine("# 农历 → 公历转换结果")
                        appendLine()
                        appendLine("**输入**：${year}年${month}月${day}日${if (isLeap) "(闰月)" else ""}")
                        appendLine()
                        appendLine("**公历日期**：${solar.year}年${solar.month}月${solar.day}日")
                        appendLine("**干支**：${lunar.ganZhiYear}年 ${lunar.ganZhiMonth}月 ${lunar.ganZhiDay}日")
                        appendLine("**生肖**：${lunar.zodiac}")
                        appendLine("**星座**：${lunar.constellation}")
                        appendLine()
                        appendLine("**时辰**：")
                        timeSlots.forEach { slot ->
                            appendLine("- ${slot.ganZhi} (${slot.timeRange})：${slot.luck}")
                        }
                    }
                    val deeplink = "onebox://screen/calendar?type=convert&direction=lunar_to_solar"
                    AgentToolResult(
                        content = result.trim() + "\n\n---\n\n[打开历法转换]($deeplink)"
                    )
                }
                else -> AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_lunar_convert_error),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(R.string.agent_tool_lunar_convert_error),
                isError = true
            )
        }
    }

    private data class ConvertArgs(
        val direction: String? = "solar_to_lunar",
        val year: Int = 0,
        val month: Int = 0,
        val day: Int = 0,
        val isLeapMonth: Boolean? = false
    )
}
