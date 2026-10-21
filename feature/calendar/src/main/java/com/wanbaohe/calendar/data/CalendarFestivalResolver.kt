package com.wanbaohe.calendar.data

import androidx.compose.runtime.Immutable
import java.util.Calendar

/**
 * 某一天的节日/节气条目。分享卡、选中日卡片共用。
 */
@Immutable
data class DayFestival(
    val name: String,
    val type: FestivalType,
)

/**
 * 节日与节气的统一解析入口。
 *
 * 这些规则不再散落在 Compose 或 Component 里：分享页、日历页都从这里取。
 */
object CalendarFestivalResolver {

    /** 当天命中的节日/节气（按 节气 → 农历节日 → 阳历节日 → 法定假日 排序、按名称去重）。 */
    fun today(lunar: LunarDate): List<DayFestival> {
        val result = LinkedHashMap<String, DayFestival>()

        lunar.solarTerm?.takeIf { it.isNotBlank() }?.let {
            result[it] = DayFestival(it, FestivalType.SolarTerm)
        }
        lunar.lunarFestivals.filter { it.isNotBlank() }.forEach {
            result.putIfAbsent(it, DayFestival(it, FestivalType.LunarFestival))
        }
        lunar.solarFestivals.filter { it.isNotBlank() }.forEach {
            result.putIfAbsent(it, DayFestival(it, FestivalType.SolarFestival))
        }
        lunar.legalHolidayName?.takeIf { it.isNotBlank() }?.let {
            result.putIfAbsent(it, DayFestival(it, FestivalType.LegalHoliday))
        }
        return result.values.toList()
    }

    /**
     * 从指定日期起 [days] 天内的节气与节日，按距离排序。
     *
     * 原来的实现放在 [com.wanbaohe.calendar.calendar_view.screenLogic.CalendarViewComponent]
     * 内部，分享页需要同一份数据，因此下沉到这里。
     */
    fun upcoming(
        year: Int,
        month: Int,
        day: Int,
        days: Int = 31,
        limit: Int = 12,
    ): List<UpcomingFestivalItem> {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val result = linkedSetOf<UpcomingFestivalItem>()
        repeat(days) { offset ->
            val cal = start.clone() as Calendar
            cal.add(Calendar.DAY_OF_MONTH, offset)

            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val lunarDay = LunarCalendarCalculator.solarToLunar(y, m, d)
            val dateText = "${m}月${d}日"

            lunarDay.solarTerm?.takeIf { it.isNotBlank() }?.let { name ->
                result += UpcomingFestivalItem(name, dateText, offset, FestivalType.SolarTerm)
            }
            val lunarFestivals = if (lunarDay.lunarFestivals.isNotEmpty()) {
                lunarDay.lunarFestivals
            } else {
                listOfNotNull(lunarDay.lunarFestival)
            }
            lunarFestivals.filter { it.isNotBlank() }.forEach { name ->
                result += UpcomingFestivalItem(name, dateText, offset, FestivalType.LunarFestival)
            }

            val solarFestivals = if (lunarDay.solarFestivals.isNotEmpty()) {
                lunarDay.solarFestivals
            } else {
                listOfNotNull(lunarDay.solarFestival)
            }
            solarFestivals.filter { it.isNotBlank() }.forEach { name ->
                result += UpcomingFestivalItem(name, dateText, offset, FestivalType.SolarFestival)
            }

            if (lunarDay.isLegalHoliday && !lunarDay.legalHolidayName.isNullOrBlank()) {
                result += UpcomingFestivalItem(
                    lunarDay.legalHolidayName,
                    dateText,
                    offset,
                    FestivalType.LegalHoliday
                )
            }
        }

        return result
            .sortedBy { it.daysUntil }
            .take(limit)
    }
}
