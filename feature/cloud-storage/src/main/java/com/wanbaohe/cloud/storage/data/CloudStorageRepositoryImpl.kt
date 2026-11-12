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

package com.wanbaohe.cloud.storage.data

import android.content.Context
import com.wanbaohe.cloud.storage.data.protocol.ObjectStoragePathResolver
import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudSort
import com.wanbaohe.cloud.storage.model.CloudSortMode
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.service.CloudFileService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 远程存储仓库实现 —— 业务门面，所有协议相关操作委派给 [CloudFileService]。
 *
 * UI / AgentTool 只看这个接口；协议细节（S3 / WebDAV / SMB）由 Service + Adapter 隔离。
 */
@Singleton
class CloudStorageRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val cloudFileService: CloudFileService,
) : CloudStorageRepository {

    private val secureStore = CloudStorageSecureStore(context)
    private val prefs = CloudStoragePrefs(context)

    // ── 连接 / 偏好设置 ──────────────────────────────

    override fun getConnections(): List<CloudStorageConnection> = secureStore.loadConnections()

    override fun getLastConnectionId(): String? = prefs.loadLastConnectionId()

    override fun getLastBucket(): String? = prefs.loadLastBucket()

    override fun getLastPrefix(): String = prefs.loadLastPrefix()

    override fun getLastSearchQuery(): String = prefs.loadSearchQuery()

    override fun getSort(): CloudSort = CloudSort(
        mode = prefs.loadSortMode()
            ?.let { name -> runCatching { CloudSortMode.valueOf(name) }.getOrNull() }
            ?: CloudSortMode.NAME,
        ascending = prefs.loadSortAscending(),
    )

    override fun saveSort(sort: CloudSort) {
        prefs.saveSort(sort.mode.name, sort.ascending)
    }

    override fun isGridMode(): Boolean = prefs.loadGridMode()

    override fun saveUiState(
        connectionId: String?,
        bucket: String?,
        prefix: String,
        searchQuery: String,
    ) {
        prefs.saveLastConnectionId(connectionId)
        prefs.saveLastBucket(bucket)
        prefs.saveLastPrefix(prefix)
        prefs.saveSearchQuery(searchQuery)
    }

    override fun saveGridMode(isGridMode: Boolean) {
        prefs.saveGridMode(isGridMode)
    }

    override fun saveConnection(connection: CloudStorageConnection) {
        val current = getConnections().filterNot { it.id == connection.id }.toMutableList()
        val normalized = if (connection.isDefault || current.isEmpty()) {
            current.replaceAll { it.withUpdatedDefaults(isDefault = false) }
            connection.withUpdatedDefaults(isDefault = true)
        } else {
            connection
        }
        current += normalized
        secureStore.saveConnections(current)
    }

    override fun deleteConnection(connectionId: String) {
        val remaining = getConnections().filterNot { it.id == connectionId }.toMutableList()
        if (remaining.isNotEmpty() && remaining.none { it.isDefault }) {
            remaining[0] = remaining[0].withUpdatedDefaults(isDefault = true)
        }
        secureStore.saveConnections(remaining)
    }

    // ── 远端操作（协议无关） ──────────────────────────

    override suspend fun testConnection(connection: CloudStorageConnection): Result<Unit> =
        cloudFileService.testConnection(connection)

    override suspend fun listBuckets(
        connection: CloudStorageConnection,
    ): Result<List<CloudBucket>> = cloudFileService.listRoots(connection)

    override suspend fun listObjects(
        connection: CloudStorageConnection,
        bucket: String,
        prefix: String,
    ): Result<List<CloudObjectItem>> = cloudFileService.listDirectory(
        connection = connection,
        root = bucket,
        path = prefix,
    )

    override suspend fun searchObjects(
        connection: CloudStorageConnection,
        bucket: String,
        query: String,
    ): Result<List<CloudObjectItem>> = withContext(Dispatchers.IO) {
        val prefix = ObjectStoragePathResolver.normalizePrefix(query)
        listObjects(connection, bucket, prefix).map { items ->
            if (query.contains('/')) items
            else items.filter { it.displayName.contains(query.trim(), ignoreCase = true) }
        }
    }

    override suspend fun headObject(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
    ): Result<CloudObjectItem> = cloudFileService.stat(connection, bucket, key)

    override suspend fun readBytes(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
    ): Result<ByteArray> = cloudFileService.readBytes(connection, bucket, key)

    override suspend fun uploadObject(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
        body: ByteArray,
        contentType: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<Unit> = cloudFileService.upload(
        connection = connection,
        root = bucket,
        path = key,
        body = body,
        contentType = contentType,
        onProgress = onProgress,
    )

    override suspend fun uploadStream(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
        contentType: String,
        size: Long,
        openStream: () -> java.io.InputStream?,
        onProgress: ((Float) -> Unit)?,
    ): Result<Unit> = cloudFileService.uploadStream(
        connection = connection,
        root = bucket,
        path = key,
        contentType = contentType,
        size = size,
        openStream = openStream,
        onProgress = onProgress,
    )

    override suspend fun createFolder(
        connection: CloudStorageConnection,
        bucket: String,
        prefix: String,
    ): Result<Unit> = cloudFileService.createDirectory(
        connection = connection,
        root = bucket,
        path = ObjectStoragePathResolver.normalizePrefix(prefix),
    )

    override suspend fun deleteObject(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
        isDirectory: Boolean,
    ): Result<Unit> = cloudFileService.delete(connection, bucket, key, isDirectory)

    override suspend fun renameObject(
        connection: CloudStorageConnection,
        bucket: String,
        sourceKey: String,
        targetKey: String,
    ): Result<Unit> = moveObject(connection, bucket, sourceKey, targetKey)

    override suspend fun moveObject(
        connection: CloudStorageConnection,
        bucket: String,
        sourceKey: String,
        targetKey: String,
    ): Result<Unit> = cloudFileService.rename(connection, bucket, sourceKey, targetKey)

    override fun buildAuthHeaders(
        connection: CloudStorageConnection,
    ): Map<String, String> = when (connection) {
        is CloudStorageConnection.WebDav -> if (connection.username.isNotBlank()) {
            mapOf("Authorization" to okhttp3.Credentials.basic(connection.username, connection.password))
        } else {
            emptyMap()
        }
        else -> emptyMap()
    }

    override fun buildSignedGetUrl(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
        expiresInSeconds: Int,
    ): String? = runCatching {
        cloudFileService.buildDownloadUrl(connection, bucket, key, expiresInSeconds)
    }.getOrNull()
}
