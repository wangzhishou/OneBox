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

package com.wanbaohe.period.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * 周期计算纯函数 — 不依赖 IO / Compose / 实体类。
 * 预测规则（与产品说明一致）：
 * - 平均周期 = 相邻经期开始日间隔均值；不足 2 个开始记录用默认 28 天
 * - 下次预测 = 最近开始日 + 平均周期
 * - 排卵期 ≈ 下次预测日前 14 天前后 2 天
 * - 平均经期 = 各次开始→结束间隔均值；不足则默认 5 天
 */
object PeriodCalculations {

    const val DEFAULT_CYCLE_DAYS = 28
    const val DEFAULT_PERIOD_DAYS = 5
    const val OVULATION_OFFSET_DAYS = 14
    const val OVULATION_WINDOW_DAYS = 2

    /** 相邻经期开始日间隔均值（天）；不足 2 个开始记录返回默认周期 */
    fun averageCycleDays(startDates: List<LocalDate>, defaultDays: Int = DEFAULT_CYCLE_DAYS): Int {
        val sorted = startDates.distinct().sorted()
        if (sorted.size < 2) return defaultDays
        val gaps = sorted.zipWithNext { a, b -> java.time.temporal.ChronoUnit.DAYS.between(a, b).toInt() }
            .filter { it > 0 }
        if (gaps.isEmpty()) return defaultDays
        return (gaps.average().toInt()).coerceIn(15, 60)
    }

    /** 各次「开始→结束」间隔均值（天）；不足则默认经期天数 */
    fun averagePeriodDays(
        starts: List<LocalDate>,
        ends: List<LocalDate>,
        defaultDays: Int = DEFAULT_PERIOD_DAYS,
    ): Int {
        if (starts.isEmpty() || ends.isEmpty()) return defaultDays
        val sortedStarts = starts.distinct().sorted()
        val sortedEnds = ends.distinct().sorted()
        val durations = mutableListOf<Int>()
        for (start in sortedStarts) {
            val end = sortedEnds.firstOrNull { it >= start } ?: continue
            val days = (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1).toInt()
            if (days in 1..15) durations.add(days)
        }
        if (durations.isEmpty()) return defaultDays
        return durations.average().toInt().coerceIn(1, 15)
    }

    /** 下次预测经期开始日 */
    fun nextPeriodStart(startDates: List<LocalDate>, averageCycleDays: Int): LocalDate? {
        val latest = startDates.maxOrNull() ?: return null
        return latest.plusDays(averageCycleDays.toLong())
    }

    /** 排卵期区间（含首尾） */
    fun ovulationWindow(nextStart: LocalDate?, offsetDays: Int = OVULATION_OFFSET_DAYS): Pair<LocalDate, LocalDate>? {
        val target = nextStart ?: return null
        val day = target.minusDays(offsetDays.toLong())
        return day.minusDays(OVULATION_WINDOW_DAYS.toLong()) to day.plusDays(OVULATION_WINDOW_DAYS.toLong())
    }

    /** 周期第 N 天：相对最近一次经期开始日（含当天 = 1）；无开始记录返回 null */
    fun cycleDayOf(date: LocalDate, startDates: List<LocalDate>): Int? {
        val start = startDates.filter { it <= date }.maxOrNull() ?: return null
        return (java.time.temporal.ChronoUnit.DAYS.between(start, date) + 1).toInt()
    }

    /** 该日是否处于经期窗口（开始日 → 开始+平均经期-1） */
    fun isInPeriodWindow(date: LocalDate, startDates: List<LocalDate>, averagePeriodDays: Int): Boolean {
        val start = startDates.filter { it <= date }.maxOrNull() ?: return false
        val days = java.time.temporal.ChronoUnit.DAYS.between(start, date).toInt()
        return days in 0 until averagePeriodDays
    }

    /** 是否落在排卵期窗口 */
    fun isInOvulationWindow(date: LocalDate, nextStart: LocalDate?): Boolean {
        val window = ovulationWindow(nextStart) ?: return false
        return !date.isBefore(window.first) && !date.isAfter(window.second)
    }

    /** 周期长度趋势：按月取「该月内出现的开始日」与下一开始日的间隔 */
    fun cycleTrendPoints(startDates: List<LocalDate>, months: Int = 6): List<Pair<YearMonth, Int>> {
        val sorted = startDates.distinct().sorted()
        if (sorted.size < 2) return emptyList()
        val pairs = sorted.zipWithNext { a, b ->
            YearMonth.from(a) to java.time.temporal.ChronoUnit.DAYS.between(a, b).toInt()
        }
        return pairs.takeLast(months)
    }

    /** 症状分布计数（按出现次数，一次记录多症状各计 1） */
    fun symptomCounts(records: List<List<PeriodSymptom>>): Map<PeriodSymptom, Int> {
        val counts = linkedMapOf<PeriodSymptom, Int>()
        records.forEach { symptoms ->
            symptoms.distinct().forEach { symptom ->
                counts[symptom] = (counts[symptom] ?: 0) + 1
            }
        }
        return counts.entries
            .sortedByDescending { it.value }
            .associateTo(linkedMapOf()) { it.key to it.value }
    }

    /** 较上期差值文案用：当前周期与上一周期差 */
    fun cycleDelta(startDates: List<LocalDate>): Int? {
        val sorted = startDates.distinct().sorted()
        if (sorted.size < 3) return null
        val gaps = sorted.zipWithNext { a, b ->
            java.time.temporal.ChronoUnit.DAYS.between(a, b).toInt()
        }.filter { it > 0 }
        if (gaps.size < 2) return null
        return gaps.last() - gaps[gaps.lastIndex - 1]
    }

    /** 较上期经期天数差 */
    fun periodDelta(
        starts: List<LocalDate>,
        ends: List<LocalDate>,
    ): Int? {
        if (starts.isEmpty() || ends.isEmpty()) return null
        val sortedStarts = starts.distinct().sorted()
        val sortedEnds = ends.distinct().sorted()
        val durations = mutableListOf<Int>()
        for (start in sortedStarts) {
            val end = sortedEnds.firstOrNull { it >= start } ?: continue
            val days = (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1).toInt()
            if (days in 1..15) durations.add(days)
        }
        if (durations.size < 2) return null
        return durations.last() - durations[durations.lastIndex - 1]
    }
}
