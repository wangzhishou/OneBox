package com.wanbaohe.cloud.storage.data.adapter

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2Dialect
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.data.protocol.ObjectStoragePathResolver
import com.wanbaohe.cloud.storage.data.protocol.RemoteFileSystemAdapter
import com.wanbaohe.cloud.storage.model.CloudBucket
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import java.util.EnumSet
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SMB 协议适配器 —— 基于 [com.hierynomus:smbj] 0.13.x。
 *
 * 运行时为每次操作建立独立 Session，避免 SMB 锁与超时。
 *
 * **性能说明（已知限制）**：SMB 的 NTLM / Kerberos 握手通常需要 3-5 个 RTT，
 * 本实现每个操作都新建连接，因此 listDirectory 的延迟会显著高于 S3 / WebDAV。
 * 后续可优化为 host+port+share → Session 的弱引用缓存（需处理超时与凭据失效）。
 */
@Singleton
class SmbFileSystemAdapter @Inject constructor() : RemoteFileSystemAdapter {

    init {
        if (System.getProperty("jcifs.netbios.hostname") == null) {
            System.setProperty("jcifs.netbios.hostname", "android-client")
        }
    }

    private val config: SmbConfig = SmbConfig.builder()
        .withTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .withSoTimeout(60_000)
        // 显式只启用 SMB2 / SMB3。家庭 / 中小企业 NAS 几乎都支持这两个。
        // SMB1 (NT1) smbj 0.13 并不原生支持，且 SMB1 早已不推荐（勒索软件重灾区）。
        .withDialects(
            SMB2Dialect.SMB_3_1_1,
            SMB2Dialect.SMB_3_0_2,
            SMB2Dialect.SMB_3_0,
            SMB2Dialect.SMB_2_1,
            SMB2Dialect.SMB_2_0_2,
        )
        .build()

    override fun listRoots(connection: CloudStorageConnection): Result<List<CloudBucket>> = runCatching {
        val c = connection.asSmb()
        if (c.share.isNotBlank()) {
            // 已指定 share：直接返回该 share 作为唯一根。
            listOf(CloudBucket(name = c.share))
        } else {
            // 未指定 share：只做"认证 + SMB 协议握手"测试，验证凭据能登入主机。
            // 真正的 share 列举需要 NetBIOS Browse / RPC over IPC$，超出当前范围。
            // 测试连接仍返回 success（让用户在 UI 上看到"凭据 OK"），但返回空 share 列表
            // —— UI 应引导用户编辑连接补充 share 名。
            withSession(c) { /* auth + logon 验证 */ }
            emptyList()
        }
    }

    override fun listDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<List<CloudObjectItem>> = runCatching {
        val c = connection.asSmb()
        val normalized = ObjectStoragePathResolver.normalizePrefix(path)
        withDiskShare(c, root) { share ->
            val listPath = if (normalized.isBlank()) "" else normalized.trimEnd('/')
            val items = mutableListOf<CloudObjectItem>()
            share.list(listPath).forEach { fileId ->
                val fileName = fileId.fileName
                // 过滤 "." / ".." —— SMB 协议层一般不返回，但部分老服务器（SMB1 / NAS）会回。
                if (fileName == "." || fileName == "..") return@forEach
                val isDir = isDirectory(fileId.fileAttributes)
                val childKey = if (normalized.isBlank()) fileName
                    else "${normalized.removeSuffix("/")}/$fileName"
                items += CloudObjectItem(
                    key = if (isDir) "$childKey/" else childKey,
                    displayName = fileName,
                    size = if (isDir) 0L else fileId.endOfFile,
                    lastModified = fileId.creationTime?.toInstant()?.toString(),
                    isDirectory = isDir,
                )
            }
            items
        }
    }

    override fun stat(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<CloudObjectItem> = runCatching {
        val c = connection.asSmb()
        withDiskShare(c, root) { share ->
            val info = share.getFileInformation(path)
            val basic = info.basicInformation
            CloudObjectItem(
                key = path,
                displayName = ObjectStoragePathResolver.displayName(path, isDirectory = false),
                size = info.standardInformation.endOfFile,
                lastModified = basic.creationTime?.toInstant()?.toString(),
                isDirectory = isDirectory(basic.fileAttributes),
            )
        }
    }

    override fun readBytes(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<ByteArray> = runCatching {
        val c = connection.asSmb()
        withDiskShare(c, root) { share ->
            val file: File = share.openFile(
                path,
                EnumSet.of(AccessMask.GENERIC_READ),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE),
            )
            try {
                val out = java.io.ByteArrayOutputStream()
                file.read(out)
                out.toByteArray()
            } finally {
                file.close()
            }
        }
    }

    override fun upload(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        body: ByteArray,
        contentType: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<Unit> = runCatching {
        val c = connection.asSmb()
        withDiskShare(c, root) { share ->
            val file: File = share.openFile(
                path,
                EnumSet.of(AccessMask.GENERIC_WRITE),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OVERWRITE_IF,
                EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE),
            )
            try {
                if (onProgress != null) {
                    val total = body.size.toLong()
                    val listener = com.hierynomus.smbj.ProgressListener { _, written ->
                        if (total > 0) onProgress(written.toFloat() / total)
                    }
                    file.getOutputStream(listener).use { it.write(body) }
                } else {
                    file.getOutputStream().use { it.write(body) }
                }
            } finally {
                file.close()
            }
        }
    }

    override fun createDirectory(
        connection: CloudStorageConnection,
        root: String,
        path: String,
    ): Result<Unit> = runCatching {
        val c = connection.asSmb()
        withDiskShare(c, root) { share ->
            share.mkdir(path.trimEnd('/'))
        }
    }

    override fun delete(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        isDirectory: Boolean,
    ): Result<Unit> = runCatching {
        val c = connection.asSmb()
        withDiskShare(c, root) { share ->
            if (isDirectory) {
                deleteRecursive(share, path.trimEnd('/'))
            } else {
                share.rm(path)
            }
        }
    }

    private fun deleteRecursive(share: DiskShare, path: String) {
        share.list(path).forEach { child ->
            val fileName = child.fileName
            // 过滤 "." / ".." —— 同 listDirectory，部分老服务器会回。
            // 不滤会导致 deleteRecursive 试图删除 $path/. 自身（可能抛异常 / 无限递归）
            // 或上升到父目录把不该删的文件一起删掉。
            if (fileName == "." || fileName == "..") return@forEach
            val isDir = isDirectory(child.fileAttributes)
            val childPath = "$path/$fileName"
            if (isDir) deleteRecursive(share, childPath) else share.rm(childPath)
        }
        share.rmdir(path, true)
    }

    /**
     * 重命名 / 移动。
     *
     * **限制**：smbj 0.13 + SMB2 协议下，open 必须显式指定 FILE_NON_DIRECTORY_FILE 或
     * FILE_DIRECTORY_FILE，混用会触发 STATUS_INVALID_PARAMETER。本实现当前固定
     * FILE_NON_DIRECTORY_FILE，**只支持重命名文件**；目录重命名需要在调用前先
     * 区分 isDirectory 并切换 createOptions，或者遍历子项后整体删除重建。
     */
    override fun rename(
        connection: CloudStorageConnection,
        root: String,
        fromPath: String,
        toPath: String,
    ): Result<Unit> = runCatching {
        val c = connection.asSmb()
        withDiskShare(c, root) { share ->
            val entry = share.open(
                fromPath,
                EnumSet.of(AccessMask.GENERIC_ALL),
                EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                EnumSet.of(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE),
            )
            try {
                entry.rename(toPath, true)
            } finally {
                entry.close()
            }
        }
    }

    override fun buildDownloadUrl(
        connection: CloudStorageConnection,
        root: String,
        path: String,
        expiresInSeconds: Int,
    ): String? = null

    private fun isDirectory(attrs: Long): Boolean =
        attrs and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value != 0L

    private fun <T> withSession(
        c: CloudStorageConnection.Smb,
        block: (Session) -> T,
    ): T {
        val client = SMBClient(config)
        val conn = client.connect(c.host, c.port)
        try {
            val session = try {
                conn.authenticate(authOf(c))
            } catch (e: com.hierynomus.mssmb2.SMBApiException) {
                // 翻译 SMB 协议错误为可读的中文提示，UI 端会直接展示给用户
                throw translateSmbError(e, c)
            }
            return try {
                block(session)
            } finally {
                session.close()
            }
        } finally {
            conn.close()
            client.close()
        }
    }

    private fun <T> withDiskShare(
        c: CloudStorageConnection.Smb,
        shareName: String,
        block: (DiskShare) -> T,
    ): T = withSession(c) { session ->
        val share = session.connectShare(shareName) as? DiskShare
            ?: throw IllegalStateException("Share '$shareName' is not a disk share")
        try {
            block(share)
        } finally {
            share.close()
        }
    }

    private fun authOf(c: CloudStorageConnection.Smb): AuthenticationContext {
        // 默认域 = WORKGROUP —— 家庭 / 中小企业 NAS 的工作组默认值。
        // 用户没填域时仍能拼出 "WORKGROUP\username" 给 smbj 走 NTLM。
        // 若 NAS 是域控环境，用户应填入真实域名或 NAS 主机名以覆盖该默认。
        val effectiveDomain = c.domain.takeIf { it.isNotBlank() } ?: DEFAULT_WORKGROUP
        val user = if (c.username.contains('\\') || c.username.contains('@')) {
            c.username  // 已是 "DOMAIN\user" / "user@domain" 形式，不重复拼接
        } else if (c.username.isBlank() && c.password.isBlank()) {
            ""  // 匿名 / Guest 模式
        } else {
            "$effectiveDomain\\${c.username}"
        }
        return AuthenticationContext(user, c.password.toCharArray(), null)
    }

    private fun translateSmbError(
        e: com.hierynomus.mssmb2.SMBApiException,
        c: CloudStorageConnection.Smb,
    ): IllegalStateException {
        val statusName = e.status?.name ?: "UNKNOWN"
        val message = when (statusName) {
            "STATUS_LOGON_FAILURE" ->
                AppContext.getString(R.string.cloud_storage_smb_error_logon_failure)

            "STATUS_ACCESS_DENIED" ->
                AppContext.getString(R.string.cloud_storage_smb_error_access_denied)

            "STATUS_BAD_NETWORK_NAME", "STATUS_BAD_NETWORK_PATH" ->
                AppContext.getString(R.string.cloud_storage_smb_error_bad_network)

            "STATUS_OBJECT_NAME_NOT_FOUND", "STATUS_NO_SUCH_FILE" ->
                AppContext.getString(R.string.cloud_storage_smb_error_not_found)

            "STATUS_INVALID_PARAMETER" ->
                AppContext.getString(R.string.cloud_storage_smb_error_invalid_parameter)

            "STATUS_NOT_SUPPORTED" ->
                AppContext.getString(R.string.cloud_storage_smb_error_not_supported)

            "STATUS_CONNECTION_DISCONNECTED", "STATUS_CONNECTION_RESET" ->
                AppContext.getString(R.string.cloud_storage_smb_error_disconnected)

            "STATUS_NETWORK_NAME_DELETED" ->
                AppContext.getString(R.string.cloud_storage_smb_error_share_deleted)

            "STATUS_SHARING_VIOLATION" ->
                AppContext.getString(R.string.cloud_storage_smb_error_sharing_violation)

            else -> AppContext.getContext().getString(
                R.string.cloud_storage_smb_error_generic,
                statusName,
                e.message ?: ""
            )
        }
        return IllegalStateException(message)
    }

    private fun CloudStorageConnection.asSmb(): CloudStorageConnection.Smb =
        this as? CloudStorageConnection.Smb
            ?: throw IllegalStateException("SmbFileSystemAdapter requires Smb connection")

    private companion object {
        private const val DEFAULT_WORKGROUP = "WORKGROUP"
    }
}
