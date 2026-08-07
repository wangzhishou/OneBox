package com.wanbaohe.habittracker.ai.tool

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
import com.wanbaohe.habittracker.R
import com.wanbaohe.habittracker.service.HabitService
import java.time.LocalDate
import javax.inject.Inject
import org.json.JSONObject

/**
 * AI Agent 工具:`check_in_habit` — 为指定习惯完成一天打卡。
 *
 * 支持 habit_id 精确或 habit_name 模糊定位,默认打今天的卡。
 */
class CheckInHabitTool @Inject constructor(
    private val service: HabitService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "check_in_habit"

    override val description: String =
        textProvider.string(R.string.agent_tool_check_in_habit_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_check_in_habit_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_check_in_habit_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_check_in_habit_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_check_in_habit_examples)

    override val requiresConfirmation: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "habit_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_check_in_habit_param_habit_id),
            ),
            "habit_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_check_in_habit_param_habit_name),
            ),
            "date" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_check_in_habit_param_date),
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)

            // 定位习惯:id 优先,名称模糊匹配兜底
            val habitId = json.optString("habit_id").takeIf { it.isNotBlank() }
            val habitName = json.optString("habit_name").takeIf { it.isNotBlank() }
            val habit = when {
                habitId != null -> service.getHabit(habitId)
                habitName != null -> service.findHabitByName(habitName)
                else -> null
            } ?: error(
                textProvider.string(
                    R.string.agent_tool_check_in_habit_not_found,
                    habitId ?: habitName.orEmpty(),
                )
            )

            val today = LocalDate.now()
            val date = json.optString("date").takeIf { it.isNotBlank() }?.let { dateStr ->
                runCatching { LocalDate.parse(dateStr) }
                    .getOrElse { error("date 格式错误,需 yyyy-MM-dd") }
            } ?: today
            require(!date.isAfter(today)) { "date 不能晚于今天" }

            val inserted = service.checkIn(
                habitId = habit.id,
                epochDay = date.toEpochDay(),
                actor = HabitService.ACTOR_AGENT,
            ).getOrThrow()

            val deeplink = AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.HabitTracker().routeKey,
            )
            val statusText = if (inserted) {
                textProvider.string(R.string.agent_tool_check_in_habit_success)
            } else {
                textProvider.string(R.string.agent_tool_check_in_habit_already)
            }
            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine("- **${sanitizeMarkdownText(habit.name)}** · $statusText")
                    appendLine(
                        "- **${textProvider.string(R.string.agent_tool_habit_label_date)}**: $date"
                    )
                    appendLine()
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_habit_open_link), deeplink)}")
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_check_in_habit_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }
}
