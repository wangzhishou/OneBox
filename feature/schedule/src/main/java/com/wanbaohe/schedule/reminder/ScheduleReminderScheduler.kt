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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 日程提醒。用 setAndAllowWhileIdle，不申请精确闹钟权限。
 * 到点的一次性提醒，过期的不再登记。
 */
@Singleton
class ScheduleReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager: AlarmManager?
        get() = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun sync(
        eventId: String,
        title: String,
        startUtcMillis: Long,
        isAllDay: Boolean,
        timeZoneId: String,
    ) {
        val triggerAt = ScheduleReminders.triggerAt(startUtcMillis, isAllDay, timeZoneId)
        if (triggerAt <= System.currentTimeMillis()) {
            cancel(eventId)
            return
        }
        val manager = alarmManager ?: return
        runCatching {
            manager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                buildPendingIntent(eventId, title, isAllDay),
            )
            makeLog { "ScheduleReminderScheduler.sync: $eventId" }
        }.onFailure { it.makeLog(TAG) }
    }

    fun cancel(eventId: String) {
        val manager = alarmManager ?: return
        runCatching {
            val pendingIntent = buildPendingIntent(eventId, "", false)
            manager.cancel(pendingIntent)
            pendingIntent.cancel()
        }.onFailure { it.makeLog(TAG) }
    }

    private fun buildPendingIntent(
        eventId: String,
        title: String,
        isAllDay: Boolean,
    ): PendingIntent {
        val intent = Intent(context, ScheduleReminderReceiver::class.java).apply {
            action = ScheduleReminderReceiver.ACTION_SCHEDULE_REMIND
            putExtra(ScheduleReminderReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(ScheduleReminderReceiver.EXTRA_TITLE, title)
            putExtra(ScheduleReminderReceiver.EXTRA_ALL_DAY, isAllDay)
        }
        return PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val TAG = "ScheduleReminderScheduler"
    }
}
