package com.wanbaohe.habittracker.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 习惯提醒调度器 — 每天固定时间经 AlarmManager 触发 [HabitReminderReceiver]。
 *
 * 有意使用 setAndAllowWhileIdle(不精确闹钟):
 *  - 不需要 SCHEDULE_EXACT_ALARM 权限,也无需 canScheduleExactAlarms 判断
 *  - 提醒类场景分钟级漂移可接受
 *
 * 重复策略:闹钟只注册下一次,Receiver 触发后再约下一天(自续)。
 */
@Singleton
class HabitReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager: AlarmManager?
        get() = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    /** 为习惯约下一次提醒(remindMinutes 为一天内分钟数) */
    fun schedule(habitId: String, habitName: String, remindMinutes: Int) {
        val manager = alarmManager ?: return
        runCatching {
            manager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerMillis(remindMinutes),
                buildPendingIntent(habitId, habitName, remindMinutes),
            )
            makeLog { "HabitReminderScheduler.schedule: $habitName @ $remindMinutes" }
        }.onFailure { it.makeLog(TAG) }
    }

    /** 取消习惯的提醒(删除习惯 / 关闭提醒时调用) */
    fun cancel(habitId: String) {
        val manager = alarmManager ?: return
        runCatching {
            val pendingIntent = buildPendingIntent(habitId, "", 0)
            manager.cancel(pendingIntent)
            pendingIntent.cancel()
            makeLog { "HabitReminderScheduler.cancel: $habitId" }
        }.onFailure { it.makeLog(TAG) }
    }

    private fun buildPendingIntent(
        habitId: String,
        habitName: String,
        remindMinutes: Int,
    ): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = HabitReminderReceiver.ACTION_HABIT_REMIND
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habitName)
            putExtra(HabitReminderReceiver.EXTRA_REMIND_MINUTES, remindMinutes)
        }
        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** 今天的 remindMinutes 时刻,已过则顺延到明天 */
    private fun nextTriggerMillis(remindMinutes: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, remindMinutes / 60)
            set(Calendar.MINUTE, remindMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private companion object {
        const val TAG = "HabitReminderScheduler"
    }
}
