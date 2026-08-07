package com.shifenmiao.ai.repository

import com.shifenmiao.database.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultMessageRepository @Inject constructor(
    private val appDatabase: AppDatabase,
) : MessageRepository {

    override suspend fun deleteMessagesByConversationId(conversationId: String): Int {
        return appDatabase.messageDao().deleteMessagesByConversationId(conversationId)
    }

    override suspend fun deleteMessagesByCompletionId(completionId: String): Int {
        return appDatabase.messageDao().deleteMessagesByCompletionId(completionId)
    }

    override suspend fun updateTitlesByConversationId(
        conversationId: String,
        title: String,
    ): Int {
        return appDatabase.messageDao().updateTitlesByConversationId(
            conversationId = conversationId,
            title = title,
        )
    }
}

