/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
        // 走 DAO 的 @Transaction 版本, 保证两行原子落库,
        // 避免进程在两条独立 insert 之间死亡形成半持久化(Q/A 只剩一行)
        val (beforeId, afterId) = messageDao.insertQuestionAndAnswer(question, answer)

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
