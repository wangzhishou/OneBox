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
