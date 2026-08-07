package com.shifenmiao.ai.history

import com.shifenmiao.model.ai.AIConversationTitleSource
import com.shifenmiao.model.ai.Conversation

/**
 * 统一维护 AI 会话中心需要的摘要字段，避免聊天页、历史中心、恢复导航各自拼装一套规则。
 */
fun Conversation.withHistorySnapshot(
    defaultTitle: String,
    userMessage: String? = null,
    assistantMessage: String? = null,
    messageIncrement: Int = 0,
    timestamp: Long = System.currentTimeMillis(),
): Conversation {
    val normalizedUser = userMessage.orEmpty().normalizeSingleLine()
    val normalizedAssistant = assistantMessage.orEmpty().normalizeSingleLine()
    val nextTitle = resolveConversationTitle(
        currentTitle = title,
        titleSource = titleSource,
        fallbackTitle = defaultTitle,
        userMessage = normalizedUser,
    )
    val nextPreview = when {
        normalizedAssistant.isNotBlank() -> normalizedAssistant
        normalizedUser.isNotBlank() -> normalizedUser
        lastMessagePreview.isNotBlank() -> lastMessagePreview
        else -> placeholder
    }
    val nextUserPreview = when {
        normalizedUser.isNotBlank() -> normalizedUser
        lastUserMessagePreview.isNotBlank() -> lastUserMessagePreview
        else -> ""
    }
    return copy(
        title = nextTitle.first,
        titleSource = nextTitle.second,
        appTitle = miniAppTitle(),
        lastMessagePreview = nextPreview,
        lastUserMessagePreview = nextUserPreview,
        lastActiveAt = timestamp,
        messageCount = (messageCount + messageIncrement).coerceAtLeast(0),
    )
}

fun Conversation.withSummaryTitle(summaryTitle: String): Conversation {
    val normalized = summaryTitle.normalizeSingleLine()
    if (normalized.isBlank() || titleSource == AIConversationTitleSource.MANUAL) return this
    return copy(
        title = normalized,
        titleSource = AIConversationTitleSource.AI_SUMMARY
    )
}

private fun resolveConversationTitle(
    currentTitle: String,
    titleSource: AIConversationTitleSource,
    fallbackTitle: String,
    userMessage: String,
): Pair<String, AIConversationTitleSource> {
    if (titleSource == AIConversationTitleSource.MANUAL) {
        return currentTitle to titleSource
    }
    val current = currentTitle.trim()
    val fallback = fallbackTitle.trim()
    val shouldGenerateTitle = userMessage.isNotBlank() && (
        current.isBlank() ||
            current == fallback ||
            titleSource == AIConversationTitleSource.SYSTEM
        )
    if (!shouldGenerateTitle) return currentTitle to titleSource

    return userMessage to AIConversationTitleSource.USER_PREFIX
}

private fun String.normalizeSingleLine(): String = replace("\n", " ")
    .replace(Regex("\\s+"), " ")
    .trim()

