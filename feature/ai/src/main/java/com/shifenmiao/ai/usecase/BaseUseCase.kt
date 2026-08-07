package com.shifenmiao.ai.usecase

import androidx.paging.PagingData
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.model.ai.ChatPrompt
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.network.api.RemoteId
import kotlinx.coroutines.flow.Flow

interface BaseUseCase {

    suspend fun getHistoryMessageList(
        input: Unit
    ): Flow<PagingData<MessageEntity>>

    suspend fun getPrompt(id: RemoteId): ChatPrompt?


    suspend fun insertQuestionAndAnswer(
        question: MessageEntity,
        answer: MessageEntity,
        conversation: Conversation? = null,
    )

    suspend fun deleteHistoryByConversationId(conversationId: String)
}
