package com.wanbaohe.file_transfer.model

import kotlinx.serialization.Serializable

/**
 * 浏览器上传成功后在手机端留下的一条"最近接收"记录。
 *
 * 相对路径都相对于服务端根目录（TransferConfig.rootPath），
 * 这样即使手机存储根目录变了，历史记录仍然能定位。
 */
@Serializable
data class ReceivedFile(
    val id: String,
    val fileName: String,
    /** 相对 rootPath 的文件路径，例如 Download/xxx.apk */
    val relativePath: String,
    /** 相对 rootPath 的目录，例如 Download（根目录为空串） */
    val dir: String,
    val size: Long,
    val timestamp: Long,
    /** 上传来源（浏览器 UA 里的设备名等，可为空） */
    val source: String? = null
)
