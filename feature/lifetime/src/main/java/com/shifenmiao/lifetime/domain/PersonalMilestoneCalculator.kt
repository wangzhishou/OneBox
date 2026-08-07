package com.shifenmiao.lifetime.domain

import com.shifenmiao.lifetime.domain.model.MilestoneStatus
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object PersonalMilestoneCalculator {

    fun calculateStatus(milestone: PersonalMilestone, birthDate: LocalDate): MilestoneStatus {
        val today = LocalDate.now()

        val targetDate = when {
            milestone.targetDate != null -> milestone.targetDate
            milestone.startDate != null && milestone.targetDays != null -> milestone.startDate.plusDays(milestone.targetDays)
            milestone.targetDays != null -> birthDate.plusDays(milestone.targetDays)
            else -> birthDate.plusYears(30)
        }

        val daysUntil = ChronoUnit.DAYS.between(today, targetDate)
        val daysSince = ChronoUnit.DAYS.between(targetDate, today)
        val isReached = !targetDate.isAfter(today)

        val startDate = milestone.startDate ?: birthDate
        val totalDays = ChronoUnit.DAYS.between(startDate, targetDate)
        val elapsedDays = ChronoUnit.DAYS.between(startDate, today).coerceAtLeast(0)
        val progress = if (totalDays > 0) {
            (elapsedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
        } else {
            1f
        }

        return MilestoneStatus(
            milestone = milestone,
            isReached = isReached,
            daysUntil = maxOf(0, daysUntil),
            daysSince = maxOf(0, daysSince),
            progress = progress,
        )
    }

    fun calculateAll(
        milestones: List<PersonalMilestone>,
        birthDate: LocalDate
    ): List<MilestoneStatus> {
        return milestones.map { calculateStatus(it, birthDate) }
    }
}
