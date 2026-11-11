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

/**
 * AI 互聊(DUEL)会话的标题与预览规则，集中在这里，避免聊天页、历史中心、Activity Log 各拼一套。
 *
 * 规则（三行各司其职，不再出现三行都是「A × B」）：
 * - 大标题 = 内容：用户填的话题优先，其次第一句发言，最后才回落功能名；
 * - 预览行 = 最后一句发言的真实内容；
 * - 小标题 badge = appTitle（对战双方，如「A × B」），只描述参与方。
 *
 * 纯函数，不依赖 Android / DB，便于直接验证。
 */
object AIDuelConversationTitle {
    const val MAX_LENGTH = 40

    fun resolve(scene: String, firstMessage: String?, fallbackTitle: String): String {
        val byScene = normalize(scene)
        if (byScene.isNotEmpty()) return byScene.abbreviate()
        val byFirstMessage = normalize(firstMessage.orEmpty())
        if (byFirstMessage.isNotEmpty()) return byFirstMessage.abbreviate()
        return fallbackTitle
    }

    /** 发言文本归一化：压平空白、剥掉旧版本写进正文的「【A】/ A：」前缀。 */
    fun normalize(text: String): String {
        val flat = text.replace(Regex("\\s+"), " ").trim()
        return listOf("【A】", "【B】", "A：", "B：", "A:", "B:")
            .firstOrNull { flat.startsWith(it) }
            ?.let { flat.removePrefix(it).trimStart() }
            ?: flat
    }

    private fun String.abbreviate(): String =
        if (length > MAX_LENGTH) take(MAX_LENGTH).trimEnd() + "…" else this
}

