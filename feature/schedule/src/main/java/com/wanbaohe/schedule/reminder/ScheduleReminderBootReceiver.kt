package com.wanbaohe.schedule.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.t8rin.logger.makeLog
import com.wanbaohe.schedule.service.ScheduleService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 重启后把还没到点的日程提醒重新登记。闹钟不会跨重启保留。
 */
class ScheduleReminderBootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReminderEntryPoint {
        fun scheduleService(): ScheduleService
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderEntryPoint::class.java,
        )
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                entry.scheduleService().refreshReminders()
            }.onFailure { it.makeLog(TAG) }
            pending.finish()
        }
    }

    private companion object {
        const val TAG = "ScheduleReminderBootReceiver"
    }
}
