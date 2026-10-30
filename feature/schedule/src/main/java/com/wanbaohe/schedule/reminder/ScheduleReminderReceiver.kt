package com.wanbaohe.schedule.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.t8rin.logger.makeLog
import com.wanbaohe.schedule.R

/**
 * 日程到点后弹出通知。未授予通知权限时不弹。
 */
class ScheduleReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SCHEDULE_REMIND) return
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank {
            context.getString(R.string.schedule_screen_title)
        }
        val isAllDay = intent.getBooleanExtra(EXTRA_ALL_DAY, false)
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        runCatching {
            showNotification(context, eventId, title, isAllDay)
        }.onFailure { it.makeLog(TAG) }
    }

    private fun showNotification(context: Context, eventId: String, title: String, isAllDay: Boolean) {
        ensureChannel(context)
        val contentIntent = Intent(
            Intent.ACTION_VIEW,
            "onebox://screen/Schedule".toUri(),
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val body = context.getString(
            if (isAllDay) R.string.schedule_notification_all_day else R.string.schedule_notification_timed,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_schedule_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(eventId.hashCode(), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.schedule_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.schedule_notification_channel_desc)
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_SCHEDULE_REMIND = "com.wanbaohe.schedule.action.SCHEDULE_REMIND"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ALL_DAY = "extra_all_day"

        private const val CHANNEL_ID = "schedule_reminder"
        private const val TAG = "ScheduleReminderReceiver"
    }
}
