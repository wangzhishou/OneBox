package com.shifenmiao.database.ai.dao

import androidx.room.ColumnInfo

/**
 * 按模型/引擎聚合的 Token 消耗统计
 *
 * 统计口径与 [TokenUsageSummary] 一致。
 */
data class ModelUsageStat(
    @ColumnInfo(name = "model")
    val model: String = "",

    @ColumnInfo(name = "engine")
    val engine: String = "",

    @ColumnInfo(name = "total_tokens")
    val totalTokens: Long = 0,

    @ColumnInfo(name = "request_count")
    val requestCount: Long = 0,
)
