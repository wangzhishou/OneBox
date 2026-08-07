package com.shifenmiao.ai.repository

import com.shifenmiao.model.ai.Conversation

interface ConversationRepository {
    suspend fun getConversationByConversationId(conversationId: String): Conversation?

    suspend fun updateConversationTitle(
        conversationId: String,
        title: String,
        titleSource: String,
    ): Int
}

