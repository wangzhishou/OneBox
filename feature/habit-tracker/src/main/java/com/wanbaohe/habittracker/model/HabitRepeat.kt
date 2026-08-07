package com.wanbaohe.habittracker.model

import com.shifenmiao.database.habit.entity.HabitEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.habittracker.R
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 习惯重复频率规则 — 纯函数,不碰 IO。
 *
 * repeatType 取值与 HabitEntity.repeat_type 列一致;
 * weekdaysMask 位掩码 bit0=周一 … bit6=周日。
 */
object HabitRepeat {

    const val DAILY = "DAILY"
    const val WEEKLY_TIMES = "WEEKLY_TIMES"
    const val MONTHLY_TIMES = "MONTHLY_TIMES"
    const val CUSTOM_WEEKDAYS = "CUSTOM_WEEKDAYS"

    const val ALL_WEEKDAYS_MASK = 0x7F

    /** LocalDate.getDayOfWeek()(MONDAY=1) → 位掩码 bit */
    fun weekdayBit(date: LocalDate): Int {
        return 1 shl (date.dayOfWeek.value - 1)
    }

    fun maskFor(dayOfWeekValue: Int): Int = 1 shl (dayOfWeekValue - 1)

    fun isWeekdaySet(mask: Int, dayOfWeekValue: Int): Boolean {
        return mask and maskFor(dayOfWeekValue) != 0
    }

    /** 该习惯在指定日期是否到期(可打卡) */
    fun isDueOn(habit: HabitEntity, epochDay: Long): Boolean {
        return isDueOn(
            repeatType = habit.repeatType,
            weekdaysMask = habit.weekdaysMask,
            epochDay = epochDay,
        )
    }

    fun isDueOn(repeatType: String, weekdaysMask: Int, epochDay: Long): Boolean {
        return when (repeatType) {
            // 每周/每月 N 次:周期内任意一天均可打卡
            DAILY, WEEKLY_TIMES, MONTHLY_TIMES -> true
            CUSTOM_WEEKDAYS -> {
                val date = LocalDate.ofEpochDay(epochDay)
                isWeekdaySet(weekdaysMask, date.dayOfWeek.value)
            }
            else -> true
        }
    }

    /** 频率副标题,如 "每日" / "每周 4 次" / "周一·周三·周五" */
    fun frequencySubtitle(habit: HabitEntity): String {
        return frequencySubtitle(
            repeatType = habit.repeatType,
            repeatTarget = habit.repeatTarget,
            weekdaysMask = habit.weekdaysMask,
        )
    }

    fun frequencySubtitle(repeatType: String, repeatTarget: Int, weekdaysMask: Int): String {
        return when (repeatType) {
            DAILY -> AppContext.getString(R.string.habit_freq_daily)
            WEEKLY_TIMES -> AppContext.getContext()
                .getString(R.string.habit_freq_weekly_times, repeatTarget)
            MONTHLY_TIMES -> AppContext.getContext()
                .getString(R.string.habit_freq_monthly_times, repeatTarget)
            CUSTOM_WEEKDAYS -> customWeekdaysSubtitle(weekdaysMask)
            else -> AppContext.getString(R.string.habit_freq_daily)
        }
    }

    /** 自定义星期文案:全选显示"每日",否则按周一~周日拼接 */
    private fun customWeekdaysSubtitle(mask: Int): String {
        if (mask == 0 || mask == ALL_WEEKDAYS_MASK) {
            return AppContext.getString(R.string.habit_freq_daily)
        }
        val labels = AppContext.getContext().resources
            .getStringArray(R.array.habit_weekdays_short)
        return (DayOfWeek.MONDAY.value..DayOfWeek.SUNDAY.value)
            .filter { isWeekdaySet(mask, it) }
            .joinToString("·") { labels[it - 1] }
    }
}
