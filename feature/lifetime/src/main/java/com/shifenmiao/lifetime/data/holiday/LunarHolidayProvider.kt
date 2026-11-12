/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.lifetime.data.holiday

import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.calendar.data.LunarJavaBridge
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 节日源实现：计算出 [now] 之后 [withinDays] 天内会出现的所有节日，按日期升序。
 *
 * 按系统语言分流：
 * - 中文（zh）：合并 [LunarCalendarCalculator] 中的公历/农历节日。
 *   农历节日通过 [LunarJavaBridge.lunarToSolarDate] 转公历；同年已过期则取下一年。
 *   公历节日直接在 [now, now+withinDays] 范围内匹配；若该年已过则下一年。
 * - 非中文：只产出国际节日——从共享公历节日表中筛出的可复用条目，
 *   加上本模块自建的几个国际节日（含母亲节/感恩节等按年第 N 个周几浮动的节日）。
 *   name 仍用中文作为稳定 key，由展示层 PresetDisplayNames 映射到多语言资源。
 */
@Singleton
class LunarHolidayProvider @Inject constructor() : HolidayProvider {

    override fun upcomingHolidays(
        now: LocalDate,
        withinDays: Long,
    ): List<PresetHoliday> {
        return if (Locale.getDefault().language == "zh") {
            chineseHolidays(now, withinDays)
        } else {
            internationalHolidays(now, withinDays)
        }
    }

    /** 中文环境：公历 + 农历全量节日 */
    private fun chineseHolidays(
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

    /** 非中文环境：仅国际节日 */
    private fun internationalHolidays(
        now: LocalDate,
        withinDays: Long,
    ): List<PresetHoliday> {
        val upper = now.plusDays(withinDays)
        val results = mutableListOf<PresetHoliday>()
        val seen = mutableSetOf<String>()

        fun add(name: String, date: LocalDate) {
            if (date.isBefore(now) || date.isAfter(upper)) return
            if (seen.add("$name|$date")) {
                results.add(
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

        // 从共享公历节日表中筛出的国际节日
        LunarCalendarCalculator.SOLAR_FESTIVALS.forEach { (key, name) ->
            if (name !in INTERNATIONAL_SOLAR_NAMES) return@forEach
            val (m, d) = key.split("-").map { it.toInt() }
            listOf(now.year, now.year + 1).forEach { year ->
                runCatching { LocalDate.of(year, m, d) }.getOrNull()?.let { add(name, it) }
            }
        }

        // 本模块自建的国际节日（含按年第 N 个周几浮动的节日）
        EXTRA_INTERNATIONAL_HOLIDAYS.forEach { (name, dateOfYear) ->
            listOf(now.year, now.year + 1).forEach { year ->
                add(name, dateOfYear(year))
            }
        }

        return results.sortedBy { it.targetDate }
    }

    private fun iconKeyFor(name: String): String = when (name) {
        "春节", "元宵节", "除夕" -> "Celebration"
        "中秋节" -> "Nightlight"
        "端午节", "清明" -> "Park"
        "七夕" -> "Favorite"
        "圣诞节", "平安夜", "万圣节" -> "Star"
        "情人节", "感恩节" -> "Favorite"
        "元旦", "国庆节", "劳动节" -> "Flag"
        "妇女节", "母亲节", "父亲节" -> "Face"
        "儿童节" -> "ChildCare"
        "教师节" -> "School"
        "建党节", "建军节" -> "Military"
        "黑色星期五", "跨年夜" -> "Celebration"
        else -> "Event"
    }

    companion object {
        /** 共享公历节日表中可复用的国际节日（按中文 name 匹配） */
        private val INTERNATIONAL_SOLAR_NAMES = setOf(
            "元旦", "情人节", "妇女节", "愚人节", "地球日",
            "劳动节", "儿童节", "环境日", "平安夜", "圣诞节"
        )

        /** 本模块自建的国际节日：name → 按年计算日期 */
        private val EXTRA_INTERNATIONAL_HOLIDAYS: List<Pair<String, (Int) -> LocalDate>> = listOf(
            "母亲节" to { year -> nthWeekdayOfMonth(year, 5, DayOfWeek.SUNDAY, 2) },
            "父亲节" to { year -> nthWeekdayOfMonth(year, 6, DayOfWeek.SUNDAY, 3) },
            "万圣节" to { year -> LocalDate.of(year, 10, 31) },
            "感恩节" to { year -> nthWeekdayOfMonth(year, 11, DayOfWeek.THURSDAY, 4) },
            "黑色星期五" to { year -> nthWeekdayOfMonth(year, 11, DayOfWeek.THURSDAY, 4).plusDays(1) },
            "跨年夜" to { year -> LocalDate.of(year, 12, 31) },
        )

        /** 某年某月第 [n] 个 [dayOfWeek]，如感恩节 = 11 月第 4 个周四 */
        private fun nthWeekdayOfMonth(
            year: Int,
            month: Int,
            dayOfWeek: DayOfWeek,
            n: Int,
        ): LocalDate {
            val first = LocalDate.of(year, month, 1)
            val firstMatch = first.with(TemporalAdjusters.nextOrSame(dayOfWeek))
            return firstMatch.plusWeeks((n - 1).toLong())
        }
    }
}
