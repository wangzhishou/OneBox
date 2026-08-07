package com.shifenmiao.ai.service

import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.model.ai.AIConversationTitleSource
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.storage.AIChatStorage
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationTitleSummaryService @Inject constructor(
    private val aiPromptExecutor: AIPromptExecutor,
) {
    private val latestFingerprintByConversation = ConcurrentHashMap<String, String>()
    private val generatedTitleCache = ConcurrentHashMap<String, String>()
    private val inFlightFingerprints = ConcurrentHashMap.newKeySet<String>()

    suspend fun generateTitle(
        conversation: Conversation,
        userMessage: String,
        assistantMessage: String,
    ): String? {
        if (!AIChatStorage.isEnableConversationTitleSummary.value) return null
        if (conversation.titleSource == AIConversationTitleSource.MANUAL) return null
        // 标题摘要只在首轮问答落库后触发：
        // 一次用户消息 + 一次助手消息，累计 messageCount 应为 2。
        if (conversation.messageCount != 2) return null
        val normalizedUser = userMessage.normalizeForSummary()
        val normalizedAssistant = assistantMessage.normalizeForSummary()
        if (normalizedUser.isBlank() && normalizedAssistant.isBlank()) return null
        val fingerprint = buildFingerprint(conversation, normalizedUser, normalizedAssistant)
        val previous = latestFingerprintByConversation[conversation.id]
        if (previous == fingerprint) {
            return generatedTitleCache[fingerprint]
        }
        latestFingerprintByConversation[conversation.id] = fingerprint
        generatedTitleCache[fingerprint]?.let { return it }
        if (!inFlightFingerprints.add(fingerprint)) return null

        try {
            val input = buildString {
                appendLine("请基于以下对话生成一个准确的中文标题。")
                appendLine("要求：")
                appendLine("1. 只输出标题，不要解释。")
                appendLine("2. 要准确体现任务主题，不要人为截断，也不要自行压缩成固定短标题。")
                appendLine("3. 不要带书名号、引号、序号、句号。")
                appendLine()
                appendLine("用户：$normalizedUser")
                appendLine("助手：$normalizedAssistant")
            }

            val result = aiPromptExecutor.execute(
                input = input,
                systemPrompt = "你是一个会话标题生成助手，只输出一个标题。",
                engineMode = AIPromptExecutor.EngineMode.FAST,
            )
            if (!result.isSuccess) return null

            val title = result.content.normalizeResultTitle()
                .takeIf { it.isNotBlank() }
                ?: return null
            generatedTitleCache[fingerprint] = title
            latestFingerprintByConversation[conversation.id] = fingerprint
            return title
        } finally {
            inFlightFingerprints.remove(fingerprint)
        }
    }

    private fun buildFingerprint(
        conversation: Conversation,
        userMessage: String,
        assistantMessage: String,
    ): String = listOf(
        conversation.id,
        conversation.entryType.name,
        conversation.entryRefId.orEmpty(),
        userMessage,
        assistantMessage
    ).joinToString(separator = "|")

    private fun String.normalizeForSummary(): String = replace("\n", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun String.normalizeResultTitle(): String = lineSequence()
        .filter { it.isNotBlank() }
        .joinToString(separator = " ") { it.trim() }
        .replace("标题：", "")
        .replace("Title:", "", ignoreCase = true)
        .replace(Regex("[\"'“”‘’]"), "")
        .trim()
}
