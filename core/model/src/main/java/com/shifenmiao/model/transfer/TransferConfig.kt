package com.shifenmiao.model.transfer

import android.os.Environment

/**
 * 文件传输服务配置
 */
data class TransferConfig(
    /** 服务端口 */
    val port: Int = 8080,
    /** 访问密码，null表示无需密码 */
    val password: String? = null,
    /** 是否允许上传文件 */
    val allowUpload: Boolean = true,
    /** 文件根目录 */
    val rootPath: String = Environment.getExternalStorageDirectory().absolutePath,
    /** 是否显示隐藏文件 */
    val showHiddenFiles: Boolean = false
)

