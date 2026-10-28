package com.shifenmiao.marktodo.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object TodoTaskTimeRules {

    fun isOverdue(
        dueDateMillis: Long?,
        hasDueTime: Boolean,
        isCompleted: Boolean,
        now: Long,
        today: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        if (dueDateMillis == null || isCompleted) return false
        return if (hasDueTime) {
            dueDateMillis < now
        } else {
            dueDateMillis.toLocalDate(zoneId) < today
        }
    }

    fun isDueSoon(
        dueDateMillis: Long?,
        hasDueTime: Boolean,
        isCompleted: Boolean,
        now: Long,
        today: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        if (dueDateMillis == null || isCompleted) return false
        return if (hasDueTime) {
            dueDateMillis in now..(now + ONE_DAY_MILLIS)
        } else {
            dueDateMillis.toLocalDate(zoneId) == today
        }
    }

    private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

    private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
}
