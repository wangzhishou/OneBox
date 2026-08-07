package com.shifenmiao.ai.agent.tool

/**
 * 目录选取请求，由 [PickFolderTool] 发布，由 UI 层的系统目录选择器消费。
 *
 * @param toolCallId    关联的工具调用 ID
 * @param toolName      工具名称（供日志使用）
 * @param message       弹出目录选择器前展示的提示语（可为空）
 * @param interactionOwnerId  所属会话 ID，用于清理孤立请求
 */
data class FolderPickerRequest(
    val toolCallId: String = "",
    val toolName: String = "",
    val message: String = "",
    val interactionOwnerId: String? = null
)

