package com.shifenmiao.lifetime.domain

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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

@Immutable
data class FestivalCount(
    val springFestival: Int = 0,
    val midAutumn: Int = 0,
    val christmas: Int = 0
)

@Immutable
data class RemainingLifeData(
    val years: Long = 0,
    val months: Long = 0,
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val progress: Float = 0f,
    val remainingSpringFestivals: Int = 0,
    val remainingSunrises: Long = 0
)

@Immutable
data class MilestoneCheck(
    val label: String,
    val targetValue: Long,
    val unit: String,
    val isReached: Boolean,
    val progress: Float,
    val dateReached: LocalDate? = null
)

class LifeTimeCalculator {

    companion object {
        private const val EXPECTED_LIFESPAN_YEARS = 100

        fun calculatePastTime(birthDate: LocalDate): LifeTimeData {
            val now = LocalDateTime.now()
            val birthDateTime = birthDate.atStartOfDay()

            val years = ChronoUnit.YEARS.between(birthDateTime, now)
            val months = ChronoUnit.MONTHS.between(birthDateTime, now)
            val days = ChronoUnit.DAYS.between(birthDateTime, now)
            val hours = ChronoUnit.HOURS.between(birthDateTime, now)
            val minutes = ChronoUnit.MINUTES.between(birthDateTime, now)
            val seconds = ChronoUnit.SECONDS.between(birthDateTime, now)

            return LifeTimeData(
                years = years,
                months = months,
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                totalDays = days,
                totalHours = hours,
                totalMinutes = minutes,
                totalSeconds = seconds
            )
        }

        fun calculateFestivals(birthDate: LocalDate): FestivalCount {
            val now = LocalDate.now()
            val years = ChronoUnit.YEARS.between(birthDate, now)

            val springFestival = years.toInt()
            val midAutumn = years.toInt()

            var christmas = years.toInt()
            if (now.monthValue < 12 || (now.monthValue == 12 && now.dayOfMonth < 25)) {
                christmas--
            }

            return FestivalCount(
                springFestival = maxOf(0, springFestival),
                midAutumn = maxOf(0, midAutumn),
                christmas = maxOf(0, christmas)
            )
        }

        fun calculateRemainingLife(
            birthDate: LocalDate,
            expectedAge: Int = EXPECTED_LIFESPAN_YEARS
        ): RemainingLifeData {
            val now = LocalDateTime.now()
            val expectedDeathDate = birthDate.plusYears(expectedAge.toLong()).atStartOfDay()

            if (now.isAfter(expectedDeathDate)) {
                return RemainingLifeData()
            }

            val years = ChronoUnit.YEARS.between(now, expectedDeathDate)
            val months = ChronoUnit.MONTHS.between(now, expectedDeathDate)
            val days = ChronoUnit.DAYS.between(now, expectedDeathDate)
            val hours = ChronoUnit.HOURS.between(now, expectedDeathDate)
            val minutes = ChronoUnit.MINUTES.between(now, expectedDeathDate)
            val seconds = ChronoUnit.SECONDS.between(now, expectedDeathDate)

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

            val remainingSpringFestivals = years.toInt()
            val remainingSunrises = days

            return RemainingLifeData(
                years = years,
                months = months,
                days = days,
                hours = hours,
                minutes = minutes,
                seconds = seconds,
                progress = progress,
                remainingSpringFestivals = maxOf(0, remainingSpringFestivals),
                remainingSunrises = maxOf(0, remainingSunrises)
            )
        }

        fun calculateMilestones(birthDate: LocalDate): List<MilestoneCheck> {
            val now = LocalDate.now()
            val totalDays = ChronoUnit.DAYS.between(birthDate, now)
            val totalHours = ChronoUnit.HOURS.between(birthDate.atStartOfDay(), now.atStartOfDay())
            val totalWeeks = ChronoUnit.WEEKS.between(birthDate, now)
            val totalMonths = ChronoUnit.MONTHS.between(birthDate, now)

            return listOf(
                MilestoneCheck("100天", 100, "天", totalDays >= 100, (totalDays.toFloat() / 100).coerceIn(0f, 1f)),
                MilestoneCheck("1000天", 1000, "天", totalDays >= 1000, (totalDays.toFloat() / 1000).coerceIn(0f, 1f)),
                MilestoneCheck("5000天", 5000, "天", totalDays >= 5000, (totalDays.toFloat() / 5000).coerceIn(0f, 1f)),
                MilestoneCheck("10000天", 10000, "天", totalDays >= 10000, (totalDays.toFloat() / 10000).coerceIn(0f, 1f)),
                MilestoneCheck("10000小时", 10000, "小时", totalHours >= 10000, (totalHours.toFloat() / 10000).coerceIn(0f, 1f)),
                MilestoneCheck("100000小时", 100000, "小时", totalHours >= 100000, (totalHours.toFloat() / 100000).coerceIn(0f, 1f)),
                MilestoneCheck("100周", 100, "周", totalWeeks >= 100, (totalWeeks.toFloat() / 100).coerceIn(0f, 1f)),
                MilestoneCheck("1000周", 1000, "周", totalWeeks >= 1000, (totalWeeks.toFloat() / 1000).coerceIn(0f, 1f)),
                MilestoneCheck("100月", 100, "月", totalMonths >= 100, (totalMonths.toFloat() / 100).coerceIn(0f, 1f)),
                MilestoneCheck("500月", 500, "月", totalMonths >= 500, (totalMonths.toFloat() / 500).coerceIn(0f, 1f)),
            )
        }
    }
}
