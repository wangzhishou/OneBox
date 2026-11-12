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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 一条备份历史。
 *
 * [localAvailable] 为 false 表示本机副本已按保留策略清理，记录还在但不能再恢复；
 * 若 [documentUri] 仍在（用户保存到网盘时拿到的持久授权），页面仍可尝试从那里恢复。
 */
@Serializable
data class BackupRecord(
    val id: String,
    val createdAt: Long,
    val fileName: String,
    val sizeBytes: Long,
    val schema: Int = BackupFormat.SCHEMA,
    val appVersionCode: Long = 0L,
    val appVersionName: String = "",
    val locale: String = "",
    val roles: List<String> = emptyList(),
    val mmkvStoreCount: Int = 0,
    val hasUserFiles: Boolean = false,
    val hasSettings: Boolean = false,
    val isLegacy: Boolean = false,
    val localAvailable: Boolean = true,
    val destinationLabel: String? = null,
    val documentUri: String? = null,
)

/**
 * 备份历史仓库。
 *
 * 历史刻意不放进 Room：恢复会整库覆盖 `AppDatabase` / `FeatureDatabase`，
 * 历史放进去等于每次恢复都把「自己」也换掉。落成 `filesDir/backup/history.json`，
 * 和备份文件同级，读写都在 IO 线程。
 *
 * 保留策略：最近 [MAX_LOCAL_COPIES] 份保留本机文件，更早的记录降级为「仅记录」；
 * 总记录数上限 [MAX_RECORDS]，避免无限增长。
 */
@Singleton
class BackupHistoryStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val directory: File
        get() = File(context.filesDir, DIRECTORY_NAME).apply { mkdirs() }

    private val historyFile: File
        get() = File(directory, HISTORY_FILE_NAME)

    fun fileOf(record: BackupRecord): File = File(directory, record.fileName)

    /** 恢复前自动留的那份「后悔药」，不进历史列表。 */
    fun preRestoreFile(): File = File(directory, PRE_RESTORE_FILE_NAME)

    suspend fun records(): List<BackupRecord> = withContext(Dispatchers.IO) {
        mutex.withLock { read() }
    }

    suspend fun add(record: BackupRecord): List<BackupRecord> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val merged = (listOf(record) + read().filterNot { it.id == record.id })
                .sortedByDescending { it.createdAt }
            val pruned = prune(merged)
            write(pruned)
            pruned
        }
    }

    suspend fun update(record: BackupRecord): List<BackupRecord> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val next = read().map { if (it.id == record.id) record else it }
            write(next)
            next
        }
    }

    suspend fun remove(id: String): List<BackupRecord> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = read()
            current.firstOrNull { it.id == id }?.let { runCatching { fileOf(it).delete() } }
            val next = current.filterNot { it.id == id }
            write(next)
            next
        }
    }

    private fun read(): List<BackupRecord> {
        if (!historyFile.exists()) return emptyList()
        return runCatching {
            json.decodeFromString<List<BackupRecord>>(historyFile.readText())
        }.getOrDefault(emptyList())
    }

    private fun write(records: List<BackupRecord>) {
        runCatching {
            directory.mkdirs()
            historyFile.writeText(json.encodeToString(records))
        }
    }

    /**
     * 只保留最近 [MAX_LOCAL_COPIES] 份的本机文件；更早的只留历史记录。
     * 记录里标了 `localAvailable = false`，页面据此禁用「恢复」并提示已清理。
     */
    private fun prune(records: List<BackupRecord>): List<BackupRecord> {
        val keepIds = records
            .filter { it.localAvailable }
            .take(MAX_LOCAL_COPIES)
            .map { it.id }
            .toSet()

        val pruned = records.map { record ->
            if (record.localAvailable && record.id !in keepIds) {
                runCatching { fileOf(record).delete() }
                record.copy(localAvailable = false)
            } else {
                record
            }
        }

        return pruned.take(MAX_RECORDS).also { kept ->
            pruned.drop(MAX_RECORDS).forEach { runCatching { fileOf(it).delete() } }
        }
    }

    private companion object {
        const val DIRECTORY_NAME = "backup"
        const val HISTORY_FILE_NAME = "history.json"
        const val PRE_RESTORE_FILE_NAME = "pre_restore.zip"
        const val MAX_LOCAL_COPIES = 3
        const val MAX_RECORDS = 20
    }
}
