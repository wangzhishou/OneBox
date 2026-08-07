package com.shifenmiao.ai.model

import com.shifenmiao.model.ai.AIConversationEntryType

data class AIHistoryItem(
    val conversationId: String,
    val title: String,
    val appTitle: String,
    val preview: String,
    val entryType: AIConversationEntryType,
    val entryRefId: String?,
    val lastActiveAt: Long,
    val messageCount: Int,
)
