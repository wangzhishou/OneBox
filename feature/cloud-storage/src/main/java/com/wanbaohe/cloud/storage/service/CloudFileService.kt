package com.wanbaohe.cloud.storage.service

import com.wanbaohe.cloud.storage.data.adapter.S3CompatAdapter
import com.wanbaohe.cloud.storage.data.adapter.SmbFileSystemAdapter
import com.wanbaohe.cloud.storage.data.adapter.WebDavFileSystemAdapter
import com.wanbaohe.cloud.storage.data.protocol.RemoteFileSystemAdapter
import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 远程存储业务编排层 —— 协议路由 + 路径规范化 + 错误归一化 + 线程切换。
 *
 * AgentTool / UI 都应通过本服务访问远程文件，不直接调用 [RemoteFileSystemAdapter]。
 */
@Singleton
class CloudFileService @Inject constructor(
    private val s3Adapter: S3CompatAdapter,
    private val webDavAdapter: WebDavFileSystemAdapter,
    private val smbAdapter: SmbFileSystemAdapter,
) {

    private fun adapterOf(connection: CloudStorageConnection): RemoteFileSystemAdapter = when (connection) {
        is CloudStorageConnection.S3Compat -> s3Adapter
        is CloudStorageConnection.WebDav -> webDavAdapter
        is CloudStorageConnection.Smb -> smbAdapter
    }

    fun protocolOf(connection: CloudStorageConnection) = connection.protocol

    fun defaultRootName(connection: CloudStorageConnection): String? = when (connection) {
        is CloudStorageConnection.S3Compat -> connection.bucket.takeIf { it.isNotBlank() }
        is CloudStorageConnection.WebDav -> connection.rootPath.takeIf { it.isNotBlank() } ?: "/"
        is CloudStorageConnection.Smb -> connection.share.takeIf { it.isNotBlank() }
    }

    suspend fun testConnection(connection: CloudStorageConnection): Result<Unit> =
        withContext(Dispatchers.IO) {
            val ad = adapterOf(connection)
            ad.listRoots(connection).map { }
        }

    suspend fun listRoots(connection: CloudStorageConnection): Result<List<CloudBucket>> =
        withContext(Dispatchers.IO) { adapterOf(connection).listRoots(connection) }

    suspend fun listDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<List<CloudObjectItem>> = withContext(Dispatchers.IO) {
        adapterOf(connection).listDirectory(connection, root, path)
    }

    suspend fun stat(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<CloudObjectItem> = withContext(Dispatchers.IO) {
        adapterOf(connection).stat(connection, root, path)
    }

    suspend fun readBytes(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        adapterOf(connection).readBytes(connection, root, path)
    }

    suspend fun upload(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        body: ByteArray,
        contentType: String,
        onProgress: ((Float) -> Unit)? = null,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        adapterOf(connection).upload(connection, root, path, body, contentType, onProgress)
    }

    suspend fun createDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        adapterOf(connection).createDirectory(connection, root, path)
    }

    suspend fun delete(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        isDirectory: Boolean,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        adapterOf(connection).delete(connection, root, path, isDirectory)
    }

    suspend fun rename(
        connection: CloudStorageConnection,
        root: String,
        fromPath: String,
        toPath: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        adapterOf(connection).rename(connection, root, fromPath, toPath)
    }

    fun buildDownloadUrl(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        expiresInSeconds: Int = 900,
    ): String? = adapterOf(connection).buildDownloadUrl(connection, root, path, expiresInSeconds)
}
