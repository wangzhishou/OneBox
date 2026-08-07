package com.shifenmiao.database.ai.dao

import androidx.room.ColumnInfo

/**
 * Token 使用总览聚合结果
 *
 * 统计口径：
 * - 排除 expired = true 的记录
 * - 排除 totalTokens = 0 的记录
 * - 仅统计 role = 'assistant' 侧，避免同一 completionId 下 question+answer 双写导致重复计数
 */
data class TokenUsageSummary(
    @ColumnInfo(name = "total_tokens")
    val totalTokens: Long = 0,

    @ColumnInfo(name = "prompt_tokens")
    val promptTokens: Long = 0,

    @ColumnInfo(name = "completion_tokens")
    val completionTokens: Long = 0,

    @ColumnInfo(name = "request_count")
    val requestCount: Long = 0,
)
