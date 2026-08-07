package com.shifenmiao.model.note

import com.shifenmiao.model.Category

/**
 * 笔记创建结果
 */
sealed class NoteResult {
    data class Success(val itemId: Int, val title: String) : NoteResult()
    data class Error(val message: String) : NoteResult()
}

/**
 * 笔记详情
 */
data class NoteDetail(
    val itemId: Int,
    val title: String,
    val description: String,
    val data: String,
    val categories: List<Category>
)

/**
 * 笔记摘要（用于列表）
 */
data class NoteSummary(
    val itemId: Int,
    val title: String,
    val description: String
)

/**
 * 笔记保存参数（创建或更新）
 *
 * @param existingItemId 非 null 时为更新已有笔记，null 时为创建新笔记
 */
data class NoteSaveParams(
    val existingItemId: Int? = null,
    val title: String,
    val description: String,
    val data: String,
    val categoryIds: List<Long>
)
