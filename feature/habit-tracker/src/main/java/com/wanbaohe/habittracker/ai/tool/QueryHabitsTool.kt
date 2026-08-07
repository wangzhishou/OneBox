package com.wanbaohe.habittracker.ai.tool

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.database.habit.repo.HabitRepository
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.habittracker.R
import com.wanbaohe.habittracker.model.HabitRepeat
import com.wanbaohe.habittracker.service.HabitService
import com.wanbaohe.habittracker.service.HabitStatsCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/**
 * AI Agent 工具:`query_habits` — 查询今日习惯清单、完成状态与本周打卡率。
 */
class QueryHabitsTool @Inject constructor(
    private val service: HabitService,
    private val repository: HabitRepository,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "query_habits"

    override val description: String =
        textProvider.string(R.string.agent_tool_query_habits_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_query_habits_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_query_habits_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_query_habits_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_query_habits_examples)

    override val parametersSchema: ToolParameters = ToolParameters()

    override suspend fun execute(arguments: String): AgentToolResult {
        return runCatching {
            val today = LocalDate.now()
            val todayEpochDay = today.toEpochDay()
            val weekStart = today.with(DayOfWeek.MONDAY).toEpochDay()

            val habits = service.listHabits()
            if (habits.isEmpty()) {
                return AgentToolResult(
                    content = buildString {
                        appendLine("# ${sanitizeMarkdownText(title)}")
                        appendLine()
                        appendLine(textProvider.string(R.string.agent_tool_query_habits_empty))
                    },
                )
            }

            val checkIns = repository.getCheckInsBetween(weekStart, todayEpochDay)
            // 连续打卡需要更长窗口(可能跨周),单独取近一年记录
            val streakCheckIns = repository.getCheckInsBetween(todayEpochDay - 365, todayEpochDay)
            val (done, missed, _) = HabitStatsCalculator.distribution(habits, checkIns, todayEpochDay)
            // 本周(Mon~今天)累计打卡率
            var weekDone = 0
            var weekDue = 0
            (weekStart..todayEpochDay).forEach { day ->
                val (d, u) = HabitStatsCalculator.daySummary(habits, checkIns, day)
                weekDone += d
                weekDue += u
            }
            val weekRateText = "${(HabitStatsCalculator.rate(weekDone, weekDue) * 100).toInt()}%"

            val deeplink = AppNavigationRegistry.buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.HabitTracker().routeKey,
            )

            AgentToolResult(
                content = buildString {
                    appendLine("# ${sanitizeMarkdownText(title)}")
                    appendLine()
                    appendLine(
                        "## ${textProvider.string(R.string.agent_tool_query_habits_section_today)} ($today)"
                    )
                    habits.forEach { habit ->
                        val isDue = HabitRepeat.isDueOn(habit, todayEpochDay)
                        val isChecked = checkIns.any {
                            it.habitId == habit.id && it.dateEpochDay == todayEpochDay
                        }
                        val status = when {
                            isChecked -> textProvider.string(R.string.habit_summary_done)
                            isDue -> textProvider.string(R.string.habit_summary_missed)
                            else -> textProvider.string(R.string.habit_stats_not_started)
                        }
                        val streak = if (habit.repeatType == HabitRepeat.DAILY) {
                            HabitStatsCalculator.streakDays(habit.id, streakCheckIns, todayEpochDay)
                                .takeIf { it > 0 }
                                ?.let {
                                    " · ${textProvider.string(R.string.habit_streak_days, it)}"
                                }
                                .orEmpty()
                        } else {
                            ""
                        }
                        appendLine(
                            "- ${sanitizeMarkdownText(habit.name)} · $status · ${
                                sanitizeMarkdownText(HabitRepeat.frequencySubtitle(habit))
                            }$streak"
                        )
                    }
                    appendLine()
                    appendLine("## ${textProvider.string(R.string.agent_tool_query_habits_weekly_rate)}")
                    appendLine("- $weekRateText ($weekDone/$weekDue)")
                    appendLine(
                        "- ${textProvider.string(R.string.habit_summary_done)} $done · " +
                            "${textProvider.string(R.string.habit_summary_missed)} $missed"
                    )
                    appendLine()
                    appendLine("- ${buildMarkdownLink(textProvider.string(R.string.agent_tool_habit_open_link), deeplink)}")
                }.trimEnd(),
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_query_habits_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }
}
