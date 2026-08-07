package com.wanbaohe.calendar.data

import androidx.compose.runtime.Immutable

@Immutable
data class ChineseHourSlot(
    val hour: Int,
    /** 含拼音标注，用于筛选菜单展示，如 "子 (Zi)" */
    val label: String,
    val timeRange: String
) {
    /** 不含拼音的纯中文名称，用于非筛选区域展示，如 "子" */
    val displayName: String get() = label.substringBefore(" (")
}

val CHINESE_HOUR_SLOTS = listOf(
    ChineseHourSlot(23, "子 (Zi)", "23:00-00:59"),
    ChineseHourSlot(1, "丑 (Chou)", "01:00-02:59"),
    ChineseHourSlot(3, "寅 (Yin)", "03:00-04:59"),
    ChineseHourSlot(5, "卯 (Mao)", "05:00-06:59"),
    ChineseHourSlot(7, "辰 (Chen)", "07:00-08:59"),
    ChineseHourSlot(9, "巳 (Si)", "09:00-10:59"),
    ChineseHourSlot(11, "午 (Wu)", "11:00-12:59"),
    ChineseHourSlot(13, "未 (Wei)", "13:00-14:59"),
    ChineseHourSlot(15, "申 (Shen)", "15:00-16:59"),
    ChineseHourSlot(17, "酉 (You)", "17:00-18:59"),
    ChineseHourSlot(19, "戌 (Xu)", "19:00-20:59"),
    ChineseHourSlot(21, "亥 (Hai)", "21:00-22:59")
)

fun getChineseHourSlot(hour: Int): ChineseHourSlot {
    val safeHour = hour.coerceIn(0, 23)
    val index = ((safeHour + 1) / 2) % CHINESE_HOUR_SLOTS.size
    return CHINESE_HOUR_SLOTS[index]
}

fun formatLunarMonthDay(monthName: String, dayName: String, isLeapMonth: Boolean = false): String {
    val normalizedMonth = LunarCalendarCalculator.normalizeLunarMonthName(monthName, isLeapMonth)
    return "$normalizedMonth${dayName.trim()}"
}

