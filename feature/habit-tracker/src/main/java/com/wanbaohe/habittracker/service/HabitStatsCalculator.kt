package com.wanbaohe.habittracker.service

import com.shifenmiao.database.habit.entity.HabitCheckInEntity
import com.shifenmiao.database.habit.entity.HabitEntity
import com.wanbaohe.habittracker.model.HabitRepeat
import com.wanbaohe.habittracker.model.HabitTrendPoint

/**
 * 习惯统计计算器 — 纯函数,输入习惯列表 + 打卡记录,输出各维度统计结果。
 * 不碰 IO,日期一律用 epochDay(LocalDate.toEpochDay())。
 */
object HabitStatsCalculator {

    /** 某日应打卡的习惯(已到期且当时已创建) */
    fun dueHabitsOn(habits: List<HabitEntity>, epochDay: Long): List<HabitEntity> {
        return habits.filter { habit ->
            HabitRepeat.isDueOn(habit, epochDay) && isCreatedBy(habit, epochDay)
        }
    }

    /** 某日打卡情况:done = 已打,due = 应打 */
    fun daySummary(
        habits: List<HabitEntity>,
        checkIns: List<HabitCheckInEntity>,
        epochDay: Long,
    ): Pair<Int, Int> {
        val due = dueHabitsOn(habits, epochDay)
        if (due.isEmpty()) return 0 to 0
        val checkedIds = checkIns.asSequence()
            .filter { it.dateEpochDay == epochDay }
            .map { it.habitId }
            .toSet()
        val done = due.count { it.id in checkedIds }
        return done to due.size
    }

    fun rate(done: Int, due: Int): Float {
        return if (due <= 0) 0f else done.toFloat() / due.toFloat()
    }

    /** 本周一~周日每日打卡率(0f..1f),weekStart 为周一 */
    fun weekRates(
        habits: List<HabitEntity>,
        checkIns: List<HabitCheckInEntity>,
        weekStartEpochDay: Long,
    ): List<Float> {
        return (0L..6L).map { offset ->
            val day = weekStartEpochDay + offset
            val (done, due) = daySummary(habits, checkIns, day)
            rate(done, due)
        }
    }

    /** 近 [days] 天每日打卡率趋势,endEpochDay 为最后一天(通常今天) */
    fun trend(
        habits: List<HabitEntity>,
        checkIns: List<HabitCheckInEntity>,
        endEpochDay: Long,
        days: Int,
    ): List<HabitTrendPoint> {
        val start = endEpochDay - days + 1
        return (start..endEpochDay).map { day ->
            val (done, due) = daySummary(habits, checkIns, day)
            HabitTrendPoint(epochDay = day, rate = rate(done, due))
        }
    }

    /** 今日分布:已打卡 / 未打卡(到期未打) / 未开始(今日不到期) */
    fun distribution(
        habits: List<HabitEntity>,
        checkIns: List<HabitCheckInEntity>,
        todayEpochDay: Long,
    ): Triple<Int, Int, Int> {
        val active = habits.filter { isCreatedBy(it, todayEpochDay) }
        val checkedIds = checkIns.asSequence()
            .filter { it.dateEpochDay == todayEpochDay }
            .map { it.habitId }
            .toSet()
        var done = 0
        var missed = 0
        var notStarted = 0
        active.forEach { habit ->
            when {
                habit.id in checkedIds -> done++
                HabitRepeat.isDueOn(habit, todayEpochDay) -> missed++
                else -> notStarted++
            }
        }
        return Triple(done, missed, notStarted)
    }

    /** 连续打卡天数:从今天(未打则从前一天)向前连续有打卡的天数 */
    fun streakDays(
        habitId: String,
        checkIns: List<HabitCheckInEntity>,
        todayEpochDay: Long,
    ): Int {
        val days = checkIns.asSequence()
            .filter { it.habitId == habitId }
            .map { it.dateEpochDay }
            .toSet()
        if (days.isEmpty()) return 0
        var cursor = if (todayEpochDay in days) todayEpochDay else todayEpochDay - 1
        var streak = 0
        while (cursor in days) {
            streak++
            cursor--
        }
        return streak
    }

    /** 习惯创建日(createdAt 毫秒 → epochDay)是否不晚于目标日 */
    private fun isCreatedBy(habit: HabitEntity, epochDay: Long): Boolean {
        val createdDay = java.time.Instant.ofEpochMilli(habit.createdAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()
        return createdDay <= epochDay
    }
}
