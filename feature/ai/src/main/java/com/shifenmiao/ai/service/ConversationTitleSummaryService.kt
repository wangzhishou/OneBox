package com.shifenmiao.ai.service

import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.AiLanguagePrompt
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
            // 提示词正文用英文书写并显式指定输出语言：写死中文会把模型带偏，
            // 导致外语界面/外语会话下生成中文标题（见 AiLanguagePrompt 注释）。
            val input = buildString {
                appendLine("Generate a title for the conversation below.")
                appendLine("Requirements:")
                appendLine("1. Output the title only, with no explanation.")
                appendLine("2. The title must accurately capture the task topic. Do not truncate it,")
                appendLine("   and do not compress it into a fixed short title.")
                appendLine("3. Do not wrap it in quotation marks or brackets, and do not add numbering,")
                appendLine("   a trailing period, or any \"Title:\" prefix.")
                appendLine()
                appendLine("User: $normalizedUser")
                appendLine("Assistant: $normalizedAssistant")
                appendLine()
                appendLine(AiLanguagePrompt.outputInCurrentLanguage("the title"))
            }

            val result = aiPromptExecutor.execute(
                input = input,
                systemPrompt = "You are a conversation title generator. " +
                    "Output exactly one title, written in ${AiLanguagePrompt.currentLanguageName()}.",
                engineMode = AIPromptExecutor.EngineMode.FAST,
                // 会话命名属自动后台任务,不向用户扣分
                billing = AIPromptExecutor.PromptBilling.EXTERNAL,
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
        .removeLeadingTitleLabel()
        .replace(Regex("[\"'“”‘’]"), "")
        .trim()

    /**
     * 去掉模型偶尔带出的 `标题：` / `Title:` / `Başlık:` 之类前缀。
     * 只有在「前缀整词命中某语言的标题说法」时才剥离，避免把 `BMI: 计算方式`
     * 这种标题自带的冒号误删。
     */
    private fun String.removeLeadingTitleLabel(): String {
        val match = LEADING_LABEL_PATTERN.find(this) ?: return this
        val label = match.groupValues[1].trim().lowercase()
        val rest = match.groupValues[2].trim()
        return if (rest.isNotEmpty() && label in TITLE_LABELS) rest else this
    }

    private companion object {
        private val LEADING_LABEL_PATTERN = Regex("^([^:：]{1,24})[:：]\\s*(.+)$")

        /** 各支持语言的「标题」写法（小写），用于剥离模型自作主张加的前缀。 */
        private val TITLE_LABELS = setOf(
            // 东亚
            "标题", "標題", "タイトル", "제목",
            // 英语系
            "title", "conversation title", "chat title",
            // 西欧
            "titel", "titre", "titolo", "título", "titulo", "títol", "başlık", "cím",
            // 北欧与中东欧
            "název", "názov", "tytuł", "titlu", "otsikko", "rubrik", "pealkiri",
            "pavadinimas", "izenburua",
            // 西里尔
            "название", "назва", "загаловак", "наслов", "тақырып",
            // 中东/南亚/东南亚
            "عنوان", "כותרת", "शीर्षक", "শিরোনাম", "શીર્ષક", "ਸਿਰਲੇਖ",
            "தலைப்பு", "శీర్షిక", "මාතෘකාව", "หัวข้อ", "tiêu đề", "judul", "pamagat"
        )
    }
}
