package com.shifenmiao.ai.repository

interface MessageRepository {
    suspend fun deleteMessagesByConversationId(conversationId: String): Int

    suspend fun deleteMessagesByCompletionId(completionId: String): Int

    suspend fun updateTitlesByConversationId(
        conversationId: String,
        title: String,
    ): Int
}

