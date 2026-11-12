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

import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudSort
import com.wanbaohe.cloud.storage.model.CloudStorageConnection

interface CloudStorageRepository {
    fun getConnections(): List<CloudStorageConnection>

    fun getLastConnectionId(): String?

    fun getLastBucket(): String?

    fun getLastPrefix(): String

    fun getLastSearchQuery(): String

    fun getSort(): CloudSort

    fun saveSort(sort: CloudSort)

    fun isGridMode(): Boolean

    fun saveUiState(connectionId: String?, bucket: String?, prefix: String, searchQuery: String)

    fun saveGridMode(isGridMode: Boolean)

    fun saveConnection(connection: CloudStorageConnection)

    fun deleteConnection(connectionId: String)

    suspend fun testConnection(connection: CloudStorageConnection): Result<Unit>

    suspend fun listBuckets(connection: CloudStorageConnection): Result<List<CloudBucket>>

    suspend fun listObjects(
        connection: CloudStorageConnection,
        bucket: String,
        prefix: String,
    ): Result<List<CloudObjectItem>>

    suspend fun searchObjects(
        connection: CloudStorageConnection,
        bucket: String,
        query: String,
    ): Result<List<CloudObjectItem>>

    suspend fun headObject(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
    ): Result<CloudObjectItem>

    /**
     * 读取文件字节内容。
     * 对 SMB / 大文件请注意 size 限制 —— AgentTool 端有 256 KB / 1 MB 上限；
     * UI / 业务模块若需更大文件应走流式下载。
     */
    suspend fun readBytes(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
    ): Result<ByteArray>

    suspend fun uploadObject(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
        body: ByteArray,
        contentType: String,
        onProgress: ((Float) -> Unit)? = null,
    ): Result<Unit>

    /**
     * 流式上传（WebDAV / SMB 真正边读边发；S3 暂退回 [uploadObject] 的字节实现）。
     */
    suspend fun uploadStream(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
        contentType: String,
        size: Long,
        openStream: () -> java.io.InputStream?,
        onProgress: ((Float) -> Unit)? = null,
    ): Result<Unit>

    suspend fun createFolder(
        connection: CloudStorageConnection,
        bucket: String,
        prefix: String,
    ): Result<Unit>

    suspend fun deleteObject(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
        isDirectory: Boolean,
    ): Result<Unit>

    suspend fun renameObject(
        connection: CloudStorageConnection,
        bucket: String,
        sourceKey: String,
        targetKey: String,
    ): Result<Unit>

    suspend fun moveObject(
        connection: CloudStorageConnection,
        bucket: String,
        sourceKey: String,
        targetKey: String,
    ): Result<Unit>

    /**
     * 下载所需的额外请求头（WebDAV 需 Basic 认证；S3 走签名 URL、SMB 无 URL 时返回空）。
     */
    fun buildAuthHeaders(connection: CloudStorageConnection): Map<String, String>

    fun buildSignedGetUrl(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
        expiresInSeconds: Int = 900,
    ): String?
}
