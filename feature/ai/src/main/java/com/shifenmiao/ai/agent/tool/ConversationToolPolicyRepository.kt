package com.shifenmiao.ai.agent.tool

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shifenmiao.database.ai.dao.ConversationToolPolicyDao
import com.shifenmiao.database.ai.entity.ConversationToolPolicyEntity
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ConversationToolPolicy
import com.shifenmiao.storage.AIChatStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationToolPolicyRepository @Inject constructor(
    private val dao: ConversationToolPolicyDao,
    private val gson: Gson
) {

    suspend fun getPolicy(conversation: Conversation): ConversationToolPolicy? {
        val scopeKey = buildScopeKey(conversation)
        dao.getByConversationId(scopeKey)?.toModel()?.let { return it.normalize() }

        val legacyConversationId = conversation.id
        if (legacyConversationId.isBlank() || legacyConversationId == scopeKey) return null

        val legacyPolicy = dao.getByConversationId(legacyConversationId)?.toModel()?.normalize() ?: return null
        savePolicy(conversation, legacyPolicy)
        return legacyPolicy
    }

    suspend fun savePolicy(conversation: Conversation, policy: ConversationToolPolicy) {
        dao.upsert(
            ConversationToolPolicyEntity(
                conversationId = buildScopeKey(conversation),
                enabledToolNamesJson = gson.toJson(policy.normalize())
            )
        )
    }

    private fun ConversationToolPolicyEntity.toModel(): ConversationToolPolicy {
        if (enabledToolNamesJson.isBlank()) return ConversationToolPolicy()
        runCatching {
            gson.fromJson(enabledToolNamesJson, ConversationToolPolicy::class.java)
        }.getOrNull()?.let { return it.normalize() }

        return ConversationToolPolicy(
            selectedToolNames = decodeStringList(enabledToolNamesJson)
        ).normalize()
    }

    private fun ConversationToolPolicy.normalize(): ConversationToolPolicy {
        return copy(
            workingMode = workingMode,
            selectedToolNames = selectedToolNames
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
                .sorted()
        )
    }

    fun defaultWorkingMode(conversation: Conversation): ChatWorkingMode {
        return when (conversation.entryType) {
            AIConversationEntryType.AGENT,
            AIConversationEntryType.PROMPT,
            AIConversationEntryType.ASSISTANT, -> ChatWorkingMode.AGENT
            AIConversationEntryType.QA,
            AIConversationEntryType.STREAM_QA,
            AIConversationEntryType.DUEL,
            AIConversationEntryType.CHAT -> ChatWorkingMode.ASK
        }
    }

    /**
     * 新会话（尚无会话级策略）生效的默认工作模式。
     *
     * 聊天类会话（硬编码默认为 ASK 且非 DUEL）优先沿用用户上次显式选择
     * 并全局记忆的模式（[AIChatStorage.saveLastChatWorkingMode]）；
     * AGENT / PROMPT / ASSISTANT / DUEL 保持各自硬编码默认，
     * 避免被聊天页的选择带偏。
     */
    fun effectiveDefaultWorkingMode(conversation: Conversation): ChatWorkingMode {
        val typeDefault = defaultWorkingMode(conversation)
        if (typeDefault != ChatWorkingMode.ASK ||
            conversation.entryType == AIConversationEntryType.DUEL
        ) {
            return typeDefault
        }
        return AIChatStorage.loadLastChatWorkingMode() ?: typeDefault
    }

    private fun buildScopeKey(conversation: Conversation): String {
        val scopeRef = when {
            !conversation.entryRefId.isNullOrBlank() -> conversation.entryRefId!!.trim()
            conversation.entryType == AIConversationEntryType.PROMPT && conversation.promptId != null -> conversation.promptId.toString()
            else -> "default"
        }
        return "${conversation.entryType.name}:$scopeRef"
    }

    private fun decodeStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
