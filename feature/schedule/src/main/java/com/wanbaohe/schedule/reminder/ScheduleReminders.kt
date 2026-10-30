package com.wanbaohe.schedule.reminder

import java.time.Instant
import java.time.ZoneId

/**
 * 待办日程的时长与提醒时刻。
 *
 * 带时间的事件默认一小时；只选日期的全天事件在当天早上提醒。
 */
object ScheduleReminders {
    const val TIMED_DURATION_MILLIS = 60L * 60L * 1000L
    const val ALL_DAY_REMIND_HOUR = 8

    fun timedEndMillis(startUtcMillis: Long): Long = startUtcMillis + TIMED_DURATION_MILLIS

    fun triggerAt(startUtcMillis: Long, isAllDay: Boolean, timeZoneId: String): Long {
        if (!isAllDay) return startUtcMillis
        val zone = zoneOf(timeZoneId)
        val date = Instant.ofEpochMilli(startUtcMillis).atZone(zone).toLocalDate()
        return date.atTime(ALL_DAY_REMIND_HOUR, 0).atZone(zone).toInstant().toEpochMilli()
    }

    fun willRemind(
        startUtcMillis: Long,
        isAllDay: Boolean,
        timeZoneId: String = ZoneId.systemDefault().id,
        now: Long = System.currentTimeMillis(),
    ): Boolean = triggerAt(startUtcMillis, isAllDay, timeZoneId) > now

    /** 表单上的截止日期是否还和已加入的日程是同一时刻。 */
    fun matchesDue(
        startUtcMillis: Long,
        isAllDay: Boolean,
        dueAtMillis: Long,
        hasDueTime: Boolean,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        val dueDate = Instant.ofEpochMilli(dueAtMillis).atZone(zoneId).toLocalDate()
        val eventDate = Instant.ofEpochMilli(startUtcMillis).atZone(zoneId).toLocalDate()
        return if (isAllDay) {
            !hasDueTime && eventDate == dueDate
        } else {
            hasDueTime && startUtcMillis == dueAtMillis
        }
    }

    private fun zoneOf(timeZoneId: String): ZoneId {
        return runCatching { ZoneId.of(timeZoneId) }.getOrDefault(ZoneId.systemDefault())
    }
}
