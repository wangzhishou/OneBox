package com.wanbaohe.cloud.storage.model

import androidx.annotation.StringRes
import com.wanbaohe.cloud.storage.R

/**
 * 顶层远程文件协议 —— 决定走哪个 [com.wanbaohe.cloud.storage.data.protocol.RemoteFileSystemAdapter]。
 *
 * 后续扩展新协议（FTP / SFTP / S3 兼容 V2 等）只需新增枚举值与对应 Adapter。
 */
enum class RemoteProtocol(
    @StringRes val titleRes: Int,
) {
    S3_COMPAT(
        titleRes = R.string.cloud_protocol_s3_compat,
    ),
    WEB_DAV(
        titleRes = R.string.cloud_protocol_webdav,
    ),
    SMB(
        titleRes = R.string.cloud_protocol_smb,
    ),
}
