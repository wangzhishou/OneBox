package com.wanbaohe.cloud.storage.data

import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection

interface CloudStorageRepository {
    fun getConnections(): List<CloudStorageConnection>

    fun getLastConnectionId(): String?

    fun getLastBucket(): String?

    fun getLastPrefix(): String

    fun getLastSearchQuery(): String

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

    fun buildSignedGetUrl(
        connection: CloudStorageConnection,
        bucket: String,
        key: String,
        expiresInSeconds: Int = 900,
    ): String?
}
