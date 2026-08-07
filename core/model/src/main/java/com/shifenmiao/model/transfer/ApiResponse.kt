package com.shifenmiao.model.transfer

/**
 * 基础API响应
 */
data class ApiResponse(
    val success: Boolean,
    val message: String? = null
)

/**
 * 创建文件夹请求
 */
data class MkdirRequest(
    val path: String,
    val name: String
)

/**
 * 重命名请求
 */
data class RenameRequest(
    val path: String,
    val newName: String
)

/**
 * 删除请求
 */
data class DeleteRequest(
    val paths: List<String>
)

/**
 * 删除响应
 */
data class DeleteResponse(
    val success: Boolean,
    val message: String? = null,
    val deletedCount: Int = 0,
    val failedPaths: List<String> = emptyList()
)

