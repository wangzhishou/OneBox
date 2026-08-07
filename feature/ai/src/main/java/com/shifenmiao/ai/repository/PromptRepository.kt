package com.shifenmiao.ai.repository

import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.ChatPrompt

interface PromptRepository {
    suspend fun getPromptRecordById(id: Int): PromptRecord?

    /** 通过 itemId 查询（资源表 1:1 关联后专用）。 */
    suspend fun getPromptRecordByItemId(itemId: Int): PromptRecord?

    suspend fun upsertPrompt(
        prompt: ChatPrompt,
        source: Source = prompt.source ?: Source.REMOTE
    ): Int

    /** 1:1 关联下，把 prompt 写入指定 itemId 对应的位置（PK 复用 itemId）。 */
    suspend fun upsertPromptAtItemId(
        prompt: ChatPrompt,
        itemId: Int,
        source: Source = prompt.source ?: Source.REMOTE
    ): Int
}

data class PromptRecord(
    val id: Int,
    val remoteId: Int?,
    val prompt: ChatPrompt,
    val source: Source,
    val updatedAtMillis: Long,
    val isSystemPrompt: Boolean,
)

