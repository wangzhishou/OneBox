package com.shifenmiao.lifetime.data.holiday

import java.time.LocalDate

/**
 * 节日源接口。
 * 实现负责在未来 [withinDays] 天内拉取公历/农历节日，返回统一的 [PresetHoliday] 列表。
 * 抽象出来便于在测试或特殊场景替换源；具体实现委托给 [LunarHolidayProvider]。
 */
interface HolidayProvider {
    fun upcomingHolidays(
        now: LocalDate = LocalDate.now(),
        withinDays: Long = 90L
    ): List<PresetHoliday>
}

/**
 * 预置节日统一数据结构。
 *
 * - [isLunar] = true 时使用 [lunarMonth] / [lunarDay] 跨年滚动；[targetDate] 仅作展示
 * - [isLunar] = false 时 [targetDate] 必填
 */
data class PresetHoliday(
    val name: String,
    val iconKey: String,
    val isLunar: Boolean,
    val targetDate: LocalDate?,
    val lunarMonth: Int?,
    val lunarDay: Int?,
    val sortOrder: Int = 999,
)
