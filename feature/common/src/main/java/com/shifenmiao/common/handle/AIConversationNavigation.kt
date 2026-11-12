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

package com.shifenmiao.common.handle

import com.shifenmiao.model.AIChatObject
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.wanbaohe.core.ui.utils.navigation.Screen

/**
 * AI 会话导航统一入口。
 *
 * 新链路优先使用结构化字段 `entryType + entryRefId`；
 * 旧链路仍允许从 legacy `type` 字符串解析，但兼容逻辑只保留在这里一处。
 */
object AIConversationNavigation {

    fun buildHistoryDetailScreen(
        conversationId: String,
        entryType: AIConversationEntryType = AIConversationEntryType.CHAT,
        entryRefId: String? = null,
        title: String = "",
        appTitle: String = "",
    ): Screen {
        return buildScreen(
            conversation = Conversation(
                id = conversationId,
                title = title,
                appTitle = appTitle,
                showLastMessage = true,
                promptId = entryRefId?.toIntOrNull(),
                entryType = entryType,
                entryRefId = entryRefId,
            )
        )
    }

    fun buildScreen(conversation: Conversation): Screen {
        return when (conversation.entryType) {
            AIConversationEntryType.AGENT -> Screen.AgentDetail(
                chatObject = AIChatObject(
                    agentId = conversation.entryRefId.orEmpty(),
                    conversation = conversation
                )
            )

            AIConversationEntryType.DUEL -> Screen.AIDuelChatScreen(
                conversation = conversation
            )
            AIConversationEntryType.QA,
            AIConversationEntryType.STREAM_QA,
            AIConversationEntryType.CHAT,
            AIConversationEntryType.PROMPT,
            AIConversationEntryType.ASSISTANT -> Screen.AiChatScreen(
                conversation = conversation
            )
        }
    }
}
