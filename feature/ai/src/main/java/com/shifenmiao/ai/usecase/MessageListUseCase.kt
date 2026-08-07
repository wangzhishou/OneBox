package com.shifenmiao.ai.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.shifenmiao.core.R
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.ai.dao.MessageDao
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.ChatPrompt
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.api.RemoteId
import com.shifenmiao.network.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class MessageListUseCase @Inject constructor(
    private val messageDao: MessageDao,
    private val activityLogRecorder: ActivityLogRecorder,
    private val apiService: ApiService
) : BaseUseCase {

    override suspend fun getHistoryMessageList(
        input: Unit
    ): Flow<PagingData<MessageEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 2),
            pagingSourceFactory = {
                messageDao.getEarliestMessagesForPage(20, 0)
            }
        ).flow
    }

    override suspend fun getPrompt(id: RemoteId): ChatPrompt? {
        val response = NetworkUtils.safeApiCall {
            apiService.fetchPrompt(
                id.value
            )
        }
        if (response != null) {
            return if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                null
            }
        }
        return null
    }


    override suspend fun insertQuestionAndAnswer(
        question: MessageEntity,
        answer: MessageEntity,
        conversation: Conversation?,
    ) {
        val beforeId = messageDao.insertReplace(question)
        val afterId = messageDao.insertReplace(answer)

        // 写入新的 activity_log 表
        activityLogRecorder.recordAiChat(
            conversationId = question.conversationId,
            title = question.title,
            appTitle = conversation?.miniAppTitle() ?: AppContext.getString(R.string.ai_tab_chat_title),
            description = question.question,
            questionId = beforeId.toString(),
            answerId = afterId.toString(),
            completionId = answer.completionId,
            entryType = conversation?.entryType?.name.orEmpty(),
            entryRefId = conversation?.entryRefId.orEmpty(),
            timestamp = answer.createdAt
        )
    }

    override suspend fun deleteHistoryByConversationId(conversationId: String) {
        activityLogRecorder.deleteAiChatLog(conversationId)
    }
}
