package com.wanbaohe.cloud.storage.data.protocol

import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection

/**
 * 远程文件系统的协议无关抽象。
 *
 * 屏蔽 S3 / WebDAV / SMB 等底层差异，对外提供统一的"列举 / 读取 / 写入"接口。
 * 所有实现必须在 `Dispatchers.IO` 上下文中被调用，调用方负责线程切换。
 */
internal interface RemoteFileSystemAdapter {

    /**
     * 列出可用的根（bucket / share / WebDAV 单根）。
     *
     * WebDAV 通常返回单元素 [CloudBucket]，SMB 列出所有 share，S3 列出账号下所有 bucket。
     */
    fun listRoots(connection: CloudStorageConnection): Result<List<CloudBucket>>

    /**
     * 列出指定 [root]（即 share / bucket）下 [path] 下的子项。
     *
     * [path] 使用 `/` 作为分隔符；空串代表根。
     */
    fun listDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<List<CloudObjectItem>>

    /** 获取单个对象的元信息。 */
    fun stat(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<CloudObjectItem>

    /** 读取文件字节。 */
    fun readBytes(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<ByteArray>

    /**
     * 上传字节内容到指定路径。
     *
     * @param onProgress 可选进度回调，0f..1f
     */
    fun upload(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        body: ByteArray,
        contentType: String,
        onProgress: ((Float) -> Unit)? = null,
    ): Result<Unit>

    /** 创建目录（SMB 走 `mkdir`，S3 走 `PUT key/`，WebDAV 走 `MKCOL`）。 */
    fun createDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<Unit>

    /**
     * 删除对象。
     *
     * @param isDirectory 对应原 S3 的"伪目录"语义：true 时由实现自行决定是否递归删除。
     */
    fun delete(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        isDirectory: Boolean,
    ): Result<Unit>

    /** 移动 / 重命名（部分协议如 SMB 不支持跨 share 移动，由实现方约束）。 */
    fun rename(
        connection: CloudStorageConnection,
        root: String,
        fromPath: String,
        toPath: String,
    ): Result<Unit>

    /**
     * 生成本地可用的下载链接（HTTP URL）。
     *
     * S3 返回预签名 URL；WebDAV 在不支持签名时直接返回 baseUrl+path；
     * SMB 在 Android 上无法直接给出可外部访问的 URL，固定返回 null。
     */
    fun buildDownloadUrl(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        expiresInSeconds: Int = 900,
    ): String?
}
