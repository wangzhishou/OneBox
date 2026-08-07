package com.shifenmiao.lifetime.domain

import com.shifenmiao.lifetime.domain.model.FrequencyEvent
import com.shifenmiao.lifetime.domain.model.FrequencyEventStats
import com.shifenmiao.lifetime.domain.model.FrequencyType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object FrequencyCalculator {

    fun calculate(
        event: FrequencyEvent,
        birthDate: LocalDate,
        expectedAge: Int = 100
    ): FrequencyEventStats {
        val today = LocalDate.now()
        val expectedDeathDate = birthDate.plusYears(expectedAge.toLong())

        if (event.frequencyType == FrequencyType.ONE_TIME) {
            return calculateOneTime(event, today, birthDate, expectedAge)
        }

        val completedPeriods = when (event.frequencyType) {
            FrequencyType.DAILY -> ChronoUnit.DAYS.between(birthDate, today)
            FrequencyType.WEEKLY -> ChronoUnit.WEEKS.between(birthDate, today)
            FrequencyType.MONTHLY -> ChronoUnit.MONTHS.between(birthDate, today)
            FrequencyType.YEARLY -> ChronoUnit.YEARS.between(birthDate, today)
            FrequencyType.ONE_TIME -> 0L
        }

        val remainingPeriods = when (event.frequencyType) {
            FrequencyType.DAILY -> ChronoUnit.DAYS.between(today, expectedDeathDate)
            FrequencyType.WEEKLY -> ChronoUnit.WEEKS.between(today, expectedDeathDate)
            FrequencyType.MONTHLY -> ChronoUnit.MONTHS.between(today, expectedDeathDate)
            FrequencyType.YEARLY -> ChronoUnit.YEARS.between(today, expectedDeathDate)
            FrequencyType.ONE_TIME -> 0L
        }

        val completedCount = maxOf(0, completedPeriods * event.timesPerPeriod)
        val remainingCount = maxOf(0, remainingPeriods * event.timesPerPeriod)

        return FrequencyEventStats(
            event = event,
            completedCount = completedCount,
            remainingCount = remainingCount
        )
    }

    private fun calculateOneTime(
        event: FrequencyEvent,
        today: LocalDate,
        birthDate: LocalDate,
        expectedAge: Int
    ): FrequencyEventStats {
        val eventDate = event.specificDate?.let {
            LocalDate.ofEpochDay(it)
        } ?: birthDate

        val daysSince = ChronoUnit.DAYS.between(eventDate, today)
        val yearsSince = ChronoUnit.YEARS.between(eventDate, today)
        val isPast = !eventDate.isAfter(today)

        return FrequencyEventStats(
            event = event,
            completedCount = if (isPast) event.timesPerPeriod.toLong() else 0,
            remainingCount = if (!isPast) event.timesPerPeriod.toLong() else 0,
            yearsSinceEvent = if (isPast) yearsSince else 0,
            daysSinceEvent = if (isPast) maxOf(0, daysSince) else 0
        )
    }

    fun calculateAll(
        events: List<FrequencyEvent>,
        birthDate: LocalDate,
        expectedAge: Int = 100
    ): List<FrequencyEventStats> {
        return events.map { event ->
            calculate(event, birthDate, expectedAge)
        }
    }
}
