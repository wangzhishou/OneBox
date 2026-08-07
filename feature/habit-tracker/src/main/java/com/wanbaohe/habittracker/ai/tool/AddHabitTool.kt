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
import com.wanbaohe.habittracker.model.HabitIcons
import com.wanbaohe.habittracker.model.HabitRepeat
import com.wanbaohe.habittracker.service.HabitService
import javax.inject.Inject
import org.json.JSONObject

/**
 * AI Agent 工具:`add_habit` — 创建新习惯。
 *
 * 与 UI 共用 [HabitService],写入自动落活动日志(actor=AGENT)并按需调度提醒。
 */
class AddHabitTool @Inject constructor(
    private val service: HabitService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "add_habit"

    override val description: String =
        textProvider.string(R.string.agent_tool_add_habit_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_add_habit_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_add_habit_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_add_habit_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_add_habit_examples)

    override val requiresConfirmation: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_habit_param_name),
            ),
            "icon_key" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_habit_param_icon),
                enum = HabitIcons.keys,
            ),
            "repeat_type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_habit_param_repeat_type),
                enum = listOf("daily", "weekly_times", "monthly_times", "custom_weekdays"),
            ),
            "repeat_target" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_add_habit_param_repeat_target),
            ),
            "weekdays" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_add_habit_param_weekdays),
            ),
            "remind_time" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_habit_param_remind_time),
            ),
            "note" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_add_habit_param_note),
            ),
        ),
        required = listOf("name"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val json = if (arguments.isBlank()) JSONObject() else JSONObject(arguments)
            val habitName = json.optString("name").trim()
            require(habitName.isNotEmpty()) { "name 不能为空" }

            val repeatType = when (json.optString("repeat_type", "daily").lowercase()) {
                "daily" -> HabitRepeat.DAILY
                "weekly_times" -> HabitRepeat.WEEKLY_TIMES
                "monthly_times" -> HabitRepeat.MONTHLY_TIMES
                "custom_weekdays" -> HabitRepeat.CUSTOM_WEEKDAYS
                else -> error("未知 repeat_type,允许 daily/weekly_times/monthly_times/custom_weekdays")
            }
            val weekdaysMask = parseWeekdaysMask(json)
            val remindMinutes = json.optString("remind_time")
                .takeIf { it.isNotBlank() }
                ?.let { parseRemindTime(it) }

            val habitId = service.createHabit(
                input = HabitService.HabitInput(
                    name = habitName,
                    iconKey = json.optString("icon_key").ifBlank { HabitIcons.DEFAULT_KEY },
                    colorArgb = null,
                    repeatType = repeatType,
                    repeatTarget = json.optInt("repeat_target", 1),
                    weekdaysMask = weekdaysMask,
                    remindMinutes = remindMinutes,
                    note = json.optString("note").takeIf { it.isNotBlank() },
                ),
                actor = HabitService.ACTOR_AGENT,
            ).getOrThrow()

            val openDeeplink = habitTrackerDeeplink()
            val editDeeplink = habitEditDeeplink(habitId)
            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine("- **${textProvider.string(R.string.agent_tool_habit_label_name)}**: ${sanitizeMarkdownText(habitName)}")
                    appendLine(
                        "- **${textProvider.string(R.string.agent_tool_habit_label_repeat)}**: ${
                            sanitizeMarkdownText(
                                HabitRepeat.frequencySubtitle(
                                    repeatType = repeatType,
                                    repeatTarget = json.optInt("repeat_target", 1).coerceIn(1, 30),
                                    weekdaysMask = weekdaysMask,
                                )
                            )
                        }"
                    )
                    remindMinutes?.let {
                        appendLine(
                            "- **${textProvider.string(R.string.agent_tool_habit_label_reminder)}**: %02d:%02d"
                                .format(it / 60, it % 60)
                        )
                    }
                    appendLine()
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_habit_open_link), openDeeplink)}")
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_habit_edit_link), editDeeplink)}")
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_add_habit_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    /** weekdays 数组(1=周一 … 7=周日)→ 位掩码 */
    private fun parseWeekdaysMask(json: JSONObject): Int {
        val array = json.optJSONArray("weekdays") ?: return 0
        var mask = 0
        for (i in 0 until array.length()) {
            val day = array.optInt(i)
            if (day in 1..7) {
                mask = mask or HabitRepeat.maskFor(day)
            }
        }
        return mask
    }

    /** "HH:mm"(24h)→ 一天内分钟数 */
    private fun parseRemindTime(value: String): Int {
        val match = Regex("^(\\d{1,2}):(\\d{1,2})$").matchEntire(value.trim())
            ?: error("remind_time 格式错误,需 HH:mm")
        val hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].toInt()
        require(hour in 0..23 && minute in 0..59) { "remind_time 超出范围" }
        return hour * 60 + minute
    }

    private fun habitTrackerDeeplink(): String {
        return AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = Screen.HabitTracker().routeKey,
        )
    }

    private fun habitEditDeeplink(habitId: String): String {
        return AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = Screen.HabitTracker().routeKey,
            params = mapOf("type" to "edit", "habit_id" to habitId),
        )
    }
}
