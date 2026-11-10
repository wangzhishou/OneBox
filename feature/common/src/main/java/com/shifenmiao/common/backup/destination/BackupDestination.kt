package com.shifenmiao.common.backup.destination

import android.net.Uri
import java.io.File

/**
 * 备份的「落脚点」。
 *
 * 备份一定先落在本机（历史仓库），导出时才经过这里：
 * 现在有「保存到文件（系统保存器，含 Google Drive / OneDrive / Dropbox）」和
 * 「分享（微信收藏 / 国产网盘）」两个实现；以后接网盘凭证（WebDAV / S3）
 * 再加一个实现、注册到 Hilt 的 Set 里即可，页面不用改。
 */
interface BackupDestination {

    val id: String

    /** 该目标是否需要系统保存器返回的文档 URI。 */
    val requiresDocument: Boolean get() = false

    /** 当前是否可用（例如云盘目标需要至少一个已配置的连接）。 */
    suspend fun isAvailable(): Boolean = true

    /** 给用户看的落点描述，例如「阿里云 OSS · onebox-backup/」；不适用时返回 null。 */
    suspend fun describeTarget(): String? = null

    /**
     * @param backup 本机已经打好的备份文件
     * @param documentUri 系统保存器返回的目标 URI；分享类目标不需要
     */
    suspend fun store(backup: File, documentUri: Uri?): Result<StoredBackup>
}

data class StoredBackup(
    val destinationId: String,
    /** 持久可读的定位（SAF 文档 URI），用于以后「从云盘恢复」；分享类为 null。 */
    val documentUri: String? = null,
)

object BackupDestinationIds {
    const val DOCUMENT = "document"
    const val SHARE = "share"
    const val CLOUD = "cloud"
}
