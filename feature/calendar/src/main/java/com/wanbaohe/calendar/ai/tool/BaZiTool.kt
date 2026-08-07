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
import com.wanbaohe.calendar.data.BaZiCalculator
import javax.inject.Inject

/**
 * AI Agent 工具：八字排盘
 *
 * 根据出生年月日时推算四柱八字、五行分布、身强身弱、喜用神、大运走势。
 */
class BaZiTool @Inject constructor(
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "bazi_calculation"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_bazi)

    override val title: String =
        textProvider.string(R.string.agent_tool_bazi_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_bazi_summary)

    override val category: ToolCategory = ToolCategory.KNOWLEDGE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_bazi_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_bazi_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "year" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_bazi_param_year)
            ),
            "month" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_bazi_param_month)
            ),
            "day" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_bazi_param_day)
            ),
            "hour" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_bazi_param_hour)
            )
        ),
        required = listOf("year", "month", "day", "hour")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val args = try {
            Gson().fromJson(arguments, BaZiArgs::class.java)
        } catch (_: Exception) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_bazi_error_date),
                isError = true
            )
        }

        val year = args.year
        val month = args.month
        val day = args.day
        val hour = args.hour

        if (year < 1900 || year > 2100 || month < 1 || month > 12 || day < 1 || day > 31 || hour < 0 || hour > 23) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_bazi_error_date),
                isError = true
            )
        }

        return try {
            val baZi = BaZiCalculator.calculateBaZi(year, month, day, hour)
            val daYun = BaZiCalculator.getDaYun(year, month)
            val fortune = BaZiCalculator.getFortuneData(year, month, year)

            val wuXingStr = baZi.wuXingDistribution.entries
                .sortedByDescending { it.value }
                .joinToString(", ") { "${it.key}(${it.value.toInt()}%)" }

            val daYunCurrent = daYun.find { it.isCurrent }

            val result = buildString {
                appendLine("# 八字排盘结果")
                appendLine()
                appendLine("**出生日期**：${year}年${month}月${day}日 ${hour}时")
                appendLine()
                appendLine("## 四柱")
                appendLine("| 柱位 | 天干 | 地支 | 十神 |")
                appendLine("|------|------|------|------|")
                appendLine("| 年柱 | ${baZi.yearPillar.tianGan} | ${baZi.yearPillar.diZhi} | ${baZi.yearPillar.shiShen} |")
                appendLine("| 月柱 | ${baZi.monthPillar.tianGan} | ${baZi.monthPillar.diZhi} | ${baZi.monthPillar.shiShen} |")
                appendLine("| 日柱 | ${baZi.dayPillar.tianGan} | ${baZi.dayPillar.diZhi} | 日主 |")
                appendLine("| 时柱 | ${baZi.hourPillar.tianGan} | ${baZi.hourPillar.diZhi} | ${baZi.hourPillar.shiShen} |")
                appendLine()
                appendLine("## 命局分析")
                appendLine("- 日主：${baZi.dayMaster}")
                appendLine("- 身强身弱：${baZi.strength}")
                appendLine("- 喜用神：${baZi.favorableElements}")
                appendLine()
                appendLine("## 五行分布")
                appendLine(wuXingStr)
                appendLine()
                if (daYunCurrent != null) {
                    appendLine("## 当前大运")
                    appendLine("- ${daYunCurrent.ganZhi}（${daYunCurrent.startYear}年起）")
                    appendLine()
                }
                fortune?.let {
                    appendLine("## 流年运势")
                    appendLine("- ${it.year}年（${it.ganZhiYear}）：${it.title}")
                    appendLine("- 运势评分：${it.fortuneScore}%")
                    appendLine("- 事业评级：${it.careerLevel}")
                }
            }

            val deeplink = "onebox://screen/calendar?type=bazi&year=${args.year}&month=${args.month}&day=${args.day}&hour=${args.hour}"
            AgentToolResult(
                content = result.trim() + "\n\n---\n\n[打开八字排盘]($deeplink)"
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(R.string.agent_tool_bazi_error_date),
                isError = true
            )
        }
    }

    private data class BaZiArgs(
        val year: Int = 0,
        val month: Int = 0,
        val day: Int = 0,
        val hour: Int = 0
    )
}
