package com.wanbaohe.calendar.data

import androidx.compose.runtime.Immutable
import java.util.Calendar
import java.util.GregorianCalendar

/**
 * 择日搜索 — 支持按宜事项或忌事项筛选
 */
object AuspiciousDayFinder {

    /**
     * 搜索符合条件的日期
     *
     * @param isAvoidMode false=吉日择取（匹配宜），true=忌事避讳（匹配忌）
     * @param selectedItems 用户选中的事项列表
     */
    fun findDays(
        startYear: Int,
        startMonth: Int,
        startDay: Int,
        rangeDays: Int = 90,
        selectedItems: Set<String>,
        isAvoidMode: Boolean
    ): List<AuspiciousDayResult> {
        if (selectedItems.isEmpty()) return emptyList()

        val results = mutableListOf<AuspiciousDayResult>()
        val cal = GregorianCalendar(startYear, startMonth - 1, startDay)

        repeat(rangeDays) {
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)

            val yiJi = YiJiCalculator.getYiJi(y, m, d)
            val source = if (isAvoidMode) yiJi.ji else yiJi.yi
            val matched = source.filter { it in selectedItems }

            if (matched.isNotEmpty()) {
                val lunar = LunarCalendarCalculator.solarToLunar(y, m, d)
                val weekDay = LunarCalendarCalculator.getWeekDayName(y, m, d)
                results.add(
                    AuspiciousDayResult(
                        solarYear = y,
                        solarMonth = m,
                        solarDay = d,
                        weekDay = weekDay,
                        lunarMonthName = lunar.monthName,
                        lunarDayName = lunar.dayName,
                        ganZhiDay = lunar.ganZhiDay,
                        matchedItems = matched,
                        allYi = yiJi.yi,
                        allJi = yiJi.ji,
                        zodiac = lunar.zodiac,
                        constellation = lunar.constellation,
                        jianChu = lunar.jianChu,
                    )
                )
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return results
    }
}

@Immutable
data class AuspiciousDayResult(
    val solarYear: Int,
    val solarMonth: Int,
    val solarDay: Int,
    val weekDay: String,
    val lunarMonthName: String,
    val lunarDayName: String,
    val ganZhiDay: String,
    /** 命中的事项（宜或忌，取决于模式） */
    val matchedItems: List<String>,
    val allYi: List<String>,
    val allJi: List<String>,
    val zodiac: String,
    val constellation: String,
    val jianChu: String,
)
