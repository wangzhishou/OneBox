package com.wanbaohe.cloud.storage.model

import java.util.UUID

/**
 * 远程文件存储连接 —— 协议无关的密封类。
 *
 * 每种协议自带一份配置；新增协议只需新增 [CloudStorageConnection] 子类。
 * [id] / [displayName] / [isDefault] 由 [Root] 抽象统一。
 */
sealed class CloudStorageConnection {

    abstract val id: String
    abstract val displayName: String
    abstract val isDefault: Boolean

    /** 协议标识，便于序列化与路由。 */
    val protocol: RemoteProtocol
        get() = when (this) {
            is S3Compat -> RemoteProtocol.S3_COMPAT
            is WebDav   -> RemoteProtocol.WEB_DAV
            is Smb      -> RemoteProtocol.SMB
        }

    /**
     * 复制并改写 `isDefault` / `displayName` 字段。
     *
     * **不能用 `copy` 作为方法名**：sealed class 内若声明 `abstract fun copy(...)`，
     * 子类实现里写 `copy(...)` 会被解析到 override 自身，导致 StackOverflowError
     * （data class 生成的 `copy` 被 override 遮蔽）。这里改用 `withUpdatedDefaults`
     * 作为桥接名，子类实现里再调 `copy(...)` 时就能正确解析到 data class 的 copy。
     */
    abstract fun withUpdatedDefaults(
        isDefault: Boolean = this.isDefault,
        displayName: String = this.displayName,
    ): CloudStorageConnection

    /** S3 兼容族：OSS / COS / OBS / BOS / AWS S3 / MinIO / R2 等。 */
    data class S3Compat(
        override val id: String = UUID.randomUUID().toString(),
        override val displayName: String,
        val vendor: S3Vendor,
        val endpoint: String,
        val region: String,
        val bucket: String,
        val accessKeyId: String,
        val secretAccessKey: String,
        override val isDefault: Boolean = false,
    ) : CloudStorageConnection() {
        val shortName: String
            get() = displayName.trim().ifBlank { bucket }.take(1).uppercase()

        override fun withUpdatedDefaults(isDefault: Boolean, displayName: String): S3Compat =
            copy(isDefault = isDefault, displayName = displayName)
    }

    /** WebDAV：坚果云 / Nextcloud / 自建 nginx/apache。 */
    data class WebDav(
        override val id: String = UUID.randomUUID().toString(),
        override val displayName: String,
        val baseUrl: String,
        val username: String,
        val password: String,
        val rootPath: String = "/",
        override val isDefault: Boolean = false,
    ) : CloudStorageConnection() {
        val shortName: String
            get() = displayName.trim().ifBlank { baseUrl }.take(1).uppercase()

        override fun withUpdatedDefaults(isDefault: Boolean, displayName: String): WebDav =
            copy(isDefault = isDefault, displayName = displayName)
    }

    /** SMB：Windows 共享 / 群晖 / Linux Samba。 */
    data class Smb(
        override val id: String = UUID.randomUUID().toString(),
        override val displayName: String,
        val host: String,
        val port: Int = 445,
        val share: String,
        val domain: String = "",
        val username: String,
        val password: String,
        override val isDefault: Boolean = false,
    ) : CloudStorageConnection() {
        val shortName: String
            get() = displayName.trim().ifBlank { host }.take(1).uppercase()

        override fun withUpdatedDefaults(isDefault: Boolean, displayName: String): Smb =
            copy(isDefault = isDefault, displayName = displayName)
    }
}
