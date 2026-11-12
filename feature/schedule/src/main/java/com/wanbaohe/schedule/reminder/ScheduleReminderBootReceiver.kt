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
