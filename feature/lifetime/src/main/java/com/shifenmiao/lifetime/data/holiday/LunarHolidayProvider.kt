package com.shifenmiao.lifetime.data.holiday

import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.calendar.data.LunarJavaBridge
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 节日源实现：合并 [LunarCalendarCalculator] 中的公历/农历节日，
 * 计算出 [now] 之后 [withinDays] 天内会出现的所有节日，按日期升序。
 *
 * 农历节日：通过 [LunarJavaBridge.lunarToSolarDate] 转公历；同年已过期则取下一年
 * 公历节日：直接在 [now, now+withinDays] 范围内匹配；若该年已过则下一年
 */
@Singleton
class LunarHolidayProvider @Inject constructor() : HolidayProvider {

    override fun upcomingHolidays(
        now: LocalDate,
        withinDays: Long,
    ): List<PresetHoliday> {
        val upper = now.plusDays(withinDays)
        val results = mutableListOf<PresetHoliday>()
        val seen = mutableSetOf<String>()

        fun addUnique(holiday: PresetHoliday) {
            val key = "${holiday.name}|${holiday.targetDate}|${holiday.isLunar}"
            if (seen.add(key)) results.add(holiday)
        }

        // 公历节日
        LunarCalendarCalculator.SOLAR_FESTIVALS.forEach { (key, name) ->
            val (m, d) = key.split("-").map { it.toInt() }
            listOf(now.year, now.year + 1).forEach { year ->
                val date = runCatching { LocalDate.of(year, m, d) }.getOrNull() ?: return@forEach
                if (!date.isBefore(now) && !date.isAfter(upper)) {
                    addUnique(
                        PresetHoliday(
                            name = name,
                            iconKey = iconKeyFor(name),
                            isLunar = false,
                            targetDate = date,
                            lunarMonth = null,
                            lunarDay = null,
                            sortOrder = date.toEpochDay().toInt(),
                        )
                    )
                }
            }
        }

        // 农历节日
        LunarCalendarCalculator.LUNAR_FESTIVALS.forEach { (key, name) ->
            val (lm, ld) = key.split("-").map { it.toInt() }
            listOf(now.year, now.year + 1).forEach { year ->
                val solar = LunarJavaBridge.lunarToSolarDate(year, lm, ld, false) ?: return@forEach
                val date = runCatching { LocalDate.of(solar.year, solar.month, solar.day) }
                    .getOrNull() ?: return@forEach
                if (!date.isBefore(now) && !date.isAfter(upper)) {
                    addUnique(
                        PresetHoliday(
                            name = name,
                            iconKey = iconKeyFor(name),
                            isLunar = true,
                            targetDate = date,
                            lunarMonth = lm,
                            lunarDay = ld,
                            sortOrder = date.toEpochDay().toInt(),
                        )
                    )
                }
            }
        }

        return results.sortedBy { it.targetDate }
    }

    private fun iconKeyFor(name: String): String = when (name) {
        "春节", "元宵节", "除夕" -> "Celebration"
        "中秋节" -> "Nightlight"
        "端午节", "清明" -> "Park"
        "七夕" -> "Favorite"
        "圣诞节", "平安夜" -> "Star"
        "情人节" -> "Favorite"
        "元旦", "国庆节", "劳动节" -> "Flag"
        "妇女节", "母亲节" -> "Face"
        "儿童节" -> "ChildCare"
        "教师节" -> "School"
        "建党节", "建军节" -> "Military"
        else -> "Event"
    }
}
