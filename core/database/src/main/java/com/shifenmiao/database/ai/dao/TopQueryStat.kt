package com.shifenmiao.database.ai.dao

import androidx.room.ColumnInfo

/**
 * 单条查询的 Token 消耗排行项
 *
 * 取 role = 'user' 侧的问题文本，按 total_tokens 降序排列。
 */
data class TopQueryStat(
    @ColumnInfo(name = "conversation_id")
    val conversationId: String = "",

    @ColumnInfo(name = "question")
    val question: String = "",

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "entry_type")
    val entryType: String = "",

    @ColumnInfo(name = "entry_ref_id")
    val entryRefId: String? = null,

    @ColumnInfo(name = "model")
    val model: String = "",

    @ColumnInfo(name = "engine")
    val engine: String = "",

    @ColumnInfo(name = "total_tokens")
    val totalTokens: Long = 0,
)
