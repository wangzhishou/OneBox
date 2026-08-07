package com.wanbaohe.habittracker.reminder

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
import com.wanbaohe.habittracker.R

/**
 * 习惯提醒接收器 — 闹钟触发后弹出通知,并自续约下一天的闹钟。
 *
 * POST_NOTIFICATIONS 未授权时静默跳过(不打扰用户)。
 */
class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_HABIT_REMIND) return
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME).orEmpty()
        val remindMinutes = intent.getIntExtra(EXTRA_REMIND_MINUTES, -1)

        // 未授权通知权限时静默跳过
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        runCatching {
            showNotification(context, habitId, habitName)
            makeLog { "HabitReminderReceiver.onReceive: notified $habitName" }
        }.onFailure { it.makeLog(TAG) }

        // 自续约下一天(闹钟为一次性,约下一次由调度器计算顺延)
        if (remindMinutes >= 0) {
            HabitReminderScheduler(context).schedule(habitId, habitName, remindMinutes)
        }
    }

    private fun showNotification(context: Context, habitId: String, habitName: String) {
        ensureChannel(context)

        // 点击通知打开打卡主页
        val contentIntent = Intent(
            Intent.ACTION_VIEW,
            "onebox://screen/habit_tracker".toUri(),
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_habit_reminder)
            .setContentTitle(habitName)
            .setContentText(context.getString(R.string.habit_notification_text, habitName))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.habit_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.habit_notification_channel_desc)
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_HABIT_REMIND = "com.wanbaohe.habittracker.action.HABIT_REMIND"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_NAME = "extra_habit_name"
        const val EXTRA_REMIND_MINUTES = "extra_remind_minutes"

        private const val CHANNEL_ID = "habit_reminder"
        private const val TAG = "HabitReminderReceiver"
    }
}
