package com.shifenmiao.common.handle

import com.shifenmiao.model.AIChatObject
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

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
