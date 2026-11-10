package com.shifenmiao.model.ai

enum class AIChatMode {
    CHAT,
    AGENT,
}

fun Conversation.supportsChatModeSelection(): Boolean {
    return entryType == AIConversationEntryType.ASSISTANT || entryType == AIConversationEntryType.CHAT
}

fun Conversation.resolveChatMode(preferredMode: AIChatMode): AIChatMode {
    return if (supportsChatModeSelection()) preferredMode else AIChatMode.AGENT
}
