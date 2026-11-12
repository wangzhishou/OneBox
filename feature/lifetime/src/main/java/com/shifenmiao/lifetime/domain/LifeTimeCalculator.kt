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

package com.shifenmiao.lifetime.domain

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * 已走过的人生。
 *
 * [years] / [days] / [hours] / [minutes] / [seconds] 是**逐级余数**：
 * 45 年 205 天 07:42:11 里，[days] 是去掉整年后剩下的天数（0..365），
 * [hours] 是去掉整年整天后剩下的小时（0..23），依此类推。
 * 直接展示 total* 会让"45 年 / 16617 天"看起来对不上（16617 天其实是 45.5 年）。
 */
@Immutable
data class LifeTimeData(
    val years: Long = 0,
    val months: Long = 0,
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val totalDays: Long = 0,
    val totalHours: Long = 0,
    val totalMinutes: Long = 0,
    val totalSeconds: Long = 0
)

/** 还剩余的人生，字段语义同 [LifeTimeData]。 */
@Immutable
data class RemainingLifeData(
    val years: Long = 0,
    val months: Long = 0,
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val progress: Float = 0f
)

class LifeTimeCalculator {

    companion object {
        private const val EXPECTED_LIFESPAN_YEARS = 100

        fun calculatePastTime(birthDate: LocalDate): LifeTimeData {
            val now = LocalDateTime.now()
            val birthDateTime = birthDate.atStartOfDay()
            if (now.isBefore(birthDateTime)) return LifeTimeData()

            val years = ChronoUnit.YEARS.between(birthDateTime, now)
            val afterYears = birthDateTime.plusYears(years)
            val months = ChronoUnit.MONTHS.between(afterYears, now)

            // 余天按"去掉整年"算：45 年 205 天，而不是拆到月之后只剩 3 天
            val days = ChronoUnit.DAYS.between(afterYears, now)
            val afterDays = afterYears.plusDays(days)
            val hours = ChronoUnit.HOURS.between(afterDays, now)
            val afterHours = afterDays.plusHours(hours)
            val minutes = ChronoUnit.MINUTES.between(afterHours, now)
            val afterMinutes = afterHours.plusMinutes(minutes)
            val seconds = ChronoUnit.SECONDS.between(afterMinutes, now)

            val totalDays = ChronoUnit.DAYS.between(birthDateTime, now)
            val totalHours = ChronoUnit.HOURS.between(birthDateTime, now)
            val totalMinutes = ChronoUnit.MINUTES.between(birthDateTime, now)
            val totalSeconds = ChronoUnit.SECONDS.between(birthDateTime, now)

            return LifeTimeData(
                years = years,
                months = months,
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                totalDays = totalDays,
                totalHours = totalHours,
                totalMinutes = totalMinutes,
                totalSeconds = totalSeconds
            )
        }

        fun calculateRemainingLife(
            birthDate: LocalDate,
            expectedAge: Int = EXPECTED_LIFESPAN_YEARS
        ): RemainingLifeData {
            val now = LocalDateTime.now()
            val expectedDeathDate = birthDate.plusYears(expectedAge.toLong()).atStartOfDay()

            if (!now.isBefore(expectedDeathDate)) {
                return RemainingLifeData()
            }

            val years = ChronoUnit.YEARS.between(now, expectedDeathDate)
            val afterYears = now.plusYears(years)
            val months = ChronoUnit.MONTHS.between(afterYears, expectedDeathDate)

            val days = ChronoUnit.DAYS.between(afterYears, expectedDeathDate)
            val afterDays = afterYears.plusDays(days)
            val hours = ChronoUnit.HOURS.between(afterDays, expectedDeathDate).coerceAtLeast(0)
            val afterHours = afterDays.plusHours(hours)
            val minutes = ChronoUnit.MINUTES.between(afterHours, expectedDeathDate).coerceAtLeast(0)
            val afterMinutes = afterHours.plusMinutes(minutes)
            val seconds = ChronoUnit.SECONDS.between(afterMinutes, expectedDeathDate).coerceAtLeast(0)

            val totalLifeSeconds = ChronoUnit.SECONDS.between(
                birthDate.atStartOfDay(),
                expectedDeathDate
            )
            val livedSeconds = ChronoUnit.SECONDS.between(
                birthDate.atStartOfDay(),
                now
            )
            val progress = if (totalLifeSeconds > 0) {
                (livedSeconds.toFloat() / totalLifeSeconds.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            return RemainingLifeData(
                years = years,
                months = months,
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                progress = progress
            )
        }
    }
}
