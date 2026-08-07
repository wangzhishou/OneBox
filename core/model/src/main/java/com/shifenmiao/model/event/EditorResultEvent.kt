package com.shifenmiao.model.event

/**
 * 编辑器保存事件，用于通知调用方编辑器已经保存修改
 *
 * @param editDraftId 对应的草稿 ID（DataDraft 表主键）
 * @param text 编辑后的新字符串
 */
data class EditorResultEvent(
    val editDraftId: Long,
    val text: String
)
