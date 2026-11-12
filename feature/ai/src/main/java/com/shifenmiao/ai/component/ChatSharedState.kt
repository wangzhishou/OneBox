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

package com.shifenmiao.ai.component

import android.content.Context
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.state.ChatUIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

/**
 * 聊天组件的共享可变状态容器。
 *
 * AIChatComponent 持有这些 MutableStateFlow 的所有权，
 * 各协作者通过此引用进行读写，确保单一数据源。
 *
 * 线程安全说明：
 * - MutableStateFlow 本身是线程安全的（原子写 + conflated 语义）
 * - [messages] 是普通 MutableList，必须通过 [mutateMessages] / [getMessagesSnapshot] 访问
 * - [componentScope] 和 [ioDispatcher] 是不可变的，无需同步
 */
class ChatSharedState(
    private val _conversation: MutableStateFlow<Conversation>,
    private val _chatUIState: MutableStateFlow<ChatUIState>,
    private val _questionMessageEntity: MutableStateFlow<MessageEntity>,
    private val _answerMessageEntity: MutableStateFlow<MessageEntity>,
    private val messages: MutableList<MessageEntity>,
    val componentScope: CoroutineScope,
    val ioDispatcher: CoroutineContext,
    val applicationContext: Context
) {
    val conversation: StateFlow<Conversation> get() = _conversation
    val chatUIState: StateFlow<ChatUIState> get() = _chatUIState
    val questionMessageEntity: StateFlow<MessageEntity> get() = _questionMessageEntity
    val answerMessageEntity: StateFlow<MessageEntity> get() = _answerMessageEntity

    fun setConversation(value: Conversation) {
        _conversation.value = value
    }

    fun updateConversation(transform: (Conversation) -> Conversation) {
        _conversation.value = transform(_conversation.value)
    }

    fun setChatUiState(value: ChatUIState) {
        _chatUIState.value = value
    }

    fun updateChatUiState(transform: (ChatUIState) -> ChatUIState) {
        _chatUIState.value = transform(_chatUIState.value)
    }

    fun setQuestionMessage(value: MessageEntity) {
        _questionMessageEntity.value = value
    }

    fun updateQuestionMessage(transform: (MessageEntity) -> MessageEntity) {
        _questionMessageEntity.value = transform(_questionMessageEntity.value)
    }

    fun setAnswerMessage(value: MessageEntity) {
        _answerMessageEntity.value = value
    }

    fun updateAnswerMessage(transform: (MessageEntity) -> MessageEntity) {
        _answerMessageEntity.value = transform(_answerMessageEntity.value)
    }

    /** 线程安全的消息列表快照 */
    fun getMessagesSnapshot(): List<MessageEntity> = synchronized(messages) { messages.toList() }

    /** 线程安全地替换消息列表 */
    fun replaceMessages(newMessages: List<MessageEntity>) {
        synchronized(messages) {
            messages.clear()
            messages.addAll(newMessages)
        }
    }

    /** 线程安全地修改消息列表 */
    fun mutateMessages(block: (MutableList<MessageEntity>) -> Unit) {
        synchronized(messages) {
            block(messages)
        }
    }
}
