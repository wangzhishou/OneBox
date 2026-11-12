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

package com.wanbaohe.cloud.storage.backup

import com.shifenmiao.common.backup.destination.BackupDestination
import com.shifenmiao.common.backup.destination.BackupDestinationIds
import com.shifenmiao.common.backup.destination.StoredBackup
import com.wanbaohe.cloud.storage.data.CloudStorageRepository
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.service.CloudFileService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 把备份直接写进用户自己的云存储（S3 兼容 / WebDAV / SMB）。
 *
 * 落点取「最后使用过的连接」，目录固定 `onebox-backup/`：
 * - S3 兼容：bucket 就是连接里配的 bucket
 * - WebDAV / SMB：root 由 [CloudFileService.defaultRootName] 给出
 *
 * 这样用户不用为备份再配一次路径；想换落点，在「云存储」里切一下当前连接即可。
 */
@Singleton
class CloudBackupDestination @Inject constructor(
    private val repository: CloudStorageRepository,
    private val service: CloudFileService,
) : BackupDestination {

    override val id: String = BackupDestinationIds.CLOUD

    override suspend fun isAvailable(): Boolean =
        runCatching { repository.getConnections().isNotEmpty() }.getOrDefault(false)

    override suspend fun describeTarget(): String? {
        val connection = activeConnection() ?: return null
        return "${connection.displayName} · $FOLDER/"
    }

    override suspend fun store(backup: File, documentUri: android.net.Uri?): Result<StoredBackup> =
        runCatching {
            val connection = activeConnection() ?: error("no cloud connection configured")
            val root = service.defaultRootName(connection) ?: error("cloud connection has no root")
            val bytes = backup.readBytes()
            service.upload(
                connection = connection,
                root = root,
                path = "$FOLDER/${backup.name}",
                body = bytes,
                contentType = "application/zip",
            ).getOrThrow()
            StoredBackup(destinationId = id)
        }

    private fun activeConnection(): CloudStorageConnection? {
        val connections = repository.getConnections()
        val lastId = repository.getLastConnectionId()
        return connections.firstOrNull { it.id == lastId }
            ?: connections.firstOrNull { it.isDefault }
            ?: connections.firstOrNull()
    }

    companion object {
        const val FOLDER = "onebox-backup"
    }
}
