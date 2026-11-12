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

package com.shifenmiao.common.backup

import com.shifenmiao.common.backup.destination.BackupDestination
import com.shifenmiao.common.backup.destination.BackupDestinationIds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自动备份的执行者：App 打开时检查一次，到期就在后台做一次备份，
 * 需要的话再直接传到用户自己的云盘。
 *
 * 用单例 + 自己的作用域，而不是页面作用域：页面随时会被销毁，
 * 备份不应该跟着断。
 */
@Singleton
class AutoBackupRunner @Inject constructor(
    private val engine: BackupEngine,
    private val settings: BackupAutoSettings,
    private val destinations: Set<@JvmSuppressWildcards BackupDestination>,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val running = AtomicBoolean(false)

    fun onAppOpened() {
        if (!settings.isDue) return
        if (!running.compareAndSet(false, true)) return
        scope.launch {
            var succeeded = false
            try {
                val record = engine.createBackup().getOrNull()
                if (record != null) {
                    succeeded = true
                    uploadIfRequested(record)
                }
            } finally {
                settings.lastRunAt = System.currentTimeMillis()
                settings.lastRunSucceeded = succeeded
                running.set(false)
            }
        }
    }

    private suspend fun uploadIfRequested(record: BackupRecord) {
        if (!settings.uploadsToCloud) return
        val cloud = destinations.firstOrNull { it.id == BackupDestinationIds.CLOUD } ?: return
        if (!cloud.isAvailable()) return
        cloud.store(engine.fileOf(record), null).onSuccess { stored ->
            engine.markExported(record, cloud.id, stored.documentUri)
        }
    }
}
