package com.shifenmiao.model.transfer

/**
 * 文件项数据模型
 */
data class FileItem(
    /** 文件名 */
    val name: String,
    /** 完整路径 */
    val path: String,
    /** 文件大小(字节) */
    val size: Long,
    /** 是否为目录 */
    val isDirectory: Boolean,
    /** MIME类型 */
    val mimeType: String?,
    /** 最后修改时间(毫秒时间戳) */
    val lastModified: Long,
    /** 是否可读 */
    val canRead: Boolean = true,
    /** 是否可写 */
    val canWrite: Boolean = true
)

/**
 * 文件列表响应
 */
data class FileListResponse(
    /** 是否成功 */
    val success: Boolean,
    /** 错误信息 */
    val message: String? = null,
    /** 文件列表 */
    val files: List<FileItem> = emptyList(),
    /** 当前路径 */
    val currentPath: String = "",
    /** 是否可以返回上级目录 */
    val canGoUp: Boolean = false,
    /** 父目录路径 */
    val parentPath: String? = null
)

/**
 * 上传结果响应
 */
data class UploadResponse(
    val success: Boolean,
    val message: String? = null,
    val fileName: String? = null,
    val filePath: String? = null
)

/**
 * 设备信息
 */
data class DeviceInfo(
    val deviceName: String,
    val totalSpace: Long,
    val freeSpace: Long,
    val allowUpload: Boolean
)

