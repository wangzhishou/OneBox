package com.shifenmiao.ai.repository

import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.model.ai.Conversation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultConversationRepository @Inject constructor(
    private val appDatabase: AppDatabase,
) : ConversationRepository {

    override suspend fun getConversationByConversationId(conversationId: String): Conversation? {
        return appDatabase.conversationDao()
            .getConversationByConversationId(conversationId)
            ?.let(ConversationEntity::toConversation)
    }

    override suspend fun updateConversationTitle(
        conversationId: String,
        title: String,
        titleSource: String,
    ): Int {
        return appDatabase.conversationDao().updateTitle(
            conversationId = conversationId,
            title = title,
            titleSource = titleSource,
        )
    }
}

