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
