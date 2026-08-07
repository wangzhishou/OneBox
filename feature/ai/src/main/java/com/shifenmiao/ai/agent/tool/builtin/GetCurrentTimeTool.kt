package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.R
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/**
 * 内置工具：获取设备当前时间。
 *
 * 返回 ISO 8601 格式的日期时间、星期、时区等信息，
 * 常用于 Agent 需要时间感知的场景（如日程规划、提醒等）。
 */
class GetCurrentTimeTool @Inject constructor(
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "get_current_time"

    override val description: String = textProvider.string(R.string.agent_tool_get_current_time_description)

    override val title: String = textProvider.string(R.string.agent_tool_get_current_time_title)

    override val summary: String = textProvider.string(R.string.agent_tool_get_current_time_summary)

    override val category: ToolCategory = ToolCategory.DEVICE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_get_current_time_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_get_current_time_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val now = Date()
        val tz = TimeZone.getDefault()
        // 使用 Z 代替 XXX，兼容 API 23+
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        isoFormat.timeZone = tz

        val readableFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss EEEE", Locale.getDefault())
        readableFormat.timeZone = tz

        val result = buildString {
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_current_time_line_current,
                    isoFormat.format(now)
                )
            )
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_current_time_line_readable,
                    readableFormat.format(now)
                )
            )
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_current_time_line_timezone,
                    tz.id,
                    tz.displayName
                )
            )
            appendLine(
                textProvider.string(
                    R.string.agent_tool_get_current_time_line_utc_offset,
                    tz.getOffset(now.time) / 3600000
                )
            )
        }

        return AgentToolResult(content = result.trim())
    }
}
