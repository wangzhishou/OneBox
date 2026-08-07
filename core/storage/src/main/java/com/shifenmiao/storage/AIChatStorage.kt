package com.shifenmiao.storage

import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AIChatStorage {

    const val DEFAULT_MAX_AGENT_ITERATIONS = 30
    const val MIN_MAX_AGENT_ITERATIONS = 10
    const val MAX_MAX_AGENT_ITERATIONS = 60

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.AI_CHAT_SETTING)
    private var cachedConversation: Conversation? = null

    private const val AI_CHAT_SETTING = "ai_chat_setting"
    private const val IS_ENABLE_WEB_SEARCH = "is_enable_web_search"
    private const val IS_ENABLE_REASONING = "is_enable_reasoning"
    private const val IS_ENABLE_CONVERSATION_TITLE_SUMMARY = "is_enable_conversation_title_summary"
    private const val MAX_AGENT_ITERATIONS = "max_agent_iterations"
    private const val LAST_CHAT_WORKING_MODE = "last_chat_working_mode"

    private val _isEnableWebSearch = MutableStateFlow(loadIsEnableWebSearch())
    val isEnableWebSearch: StateFlow<Boolean> get() = _isEnableWebSearch
    private val _isEnableReasoning = MutableStateFlow(loadIsEnableReasoning())
    val isEnableReasoning: StateFlow<Boolean> get() = _isEnableReasoning
    private val _isEnableConversationTitleSummary = MutableStateFlow(loadIsEnableConversationTitleSummary())
    val isEnableConversationTitleSummary: StateFlow<Boolean> get() = _isEnableConversationTitleSummary
    private val _maxAgentIterations = MutableStateFlow(loadMaxAgentIterations())
    val maxAgentIterations: StateFlow<Int> get() = _maxAgentIterations
    private val _lastChatWorkingMode = MutableStateFlow(loadLastChatWorkingMode())
    val lastChatWorkingMode: StateFlow<ChatWorkingMode?> get() = _lastChatWorkingMode

    fun saveConfigs(conversation: Conversation) {
        val copy = conversation.copy()
        mmkv.encode(AI_CHAT_SETTING + conversation.entryType.name, copy, 24 * 60 * 60 * 1)
        cachedConversation = copy
    }

    fun loadConfigs(entryTypeName: String): Conversation? {
        if (cachedConversation != null) {
            return cachedConversation
        }
        cachedConversation =
            mmkv.decodeParcelable(AI_CHAT_SETTING + entryTypeName, Conversation::class.java)
        return cachedConversation
    }

    fun clearConfigs() {
        mmkv.clear()
        cachedConversation = null
    }

    fun saveIsEnableWebSearch(isEnableWebSearch: Boolean) {
        mmkv.encode(IS_ENABLE_WEB_SEARCH, isEnableWebSearch)
        _isEnableWebSearch.value = isEnableWebSearch
    }

    fun loadIsEnableWebSearch(): Boolean {
        return mmkv.decodeBool(IS_ENABLE_WEB_SEARCH, false)
    }

    fun saveIsEnableReasoning(isEnableReasoning: Boolean) {
        mmkv.encode(IS_ENABLE_REASONING, isEnableReasoning)
        _isEnableReasoning.value = isEnableReasoning
    }

    fun loadIsEnableReasoning(): Boolean {
        return mmkv.decodeBool(IS_ENABLE_REASONING, true)
    }

    fun saveIsEnableConversationTitleSummary(enabled: Boolean) {
        mmkv.encode(IS_ENABLE_CONVERSATION_TITLE_SUMMARY, enabled)
        _isEnableConversationTitleSummary.value = enabled
    }

    fun loadIsEnableConversationTitleSummary(): Boolean {
        return mmkv.decodeBool(IS_ENABLE_CONVERSATION_TITLE_SUMMARY, true)
    }

    fun saveMaxAgentIterations(value: Int) {
        val sanitizedValue = sanitizeMaxAgentIterations(value)
        mmkv.encode(MAX_AGENT_ITERATIONS, sanitizedValue)
        _maxAgentIterations.value = sanitizedValue
    }

    fun loadMaxAgentIterations(): Int {
        return sanitizeMaxAgentIterations(
            mmkv.decodeInt(MAX_AGENT_ITERATIONS, DEFAULT_MAX_AGENT_ITERATIONS)
        )
    }

    /**
     * 记录用户上次显式选择的聊天工作模式（ASK/PLAN/AGENT），
     * 新会话（尚无会话级策略）默认沿用该模式。
     */
    fun saveLastChatWorkingMode(mode: ChatWorkingMode) {
        mmkv.encode(LAST_CHAT_WORKING_MODE, mode.name)
        _lastChatWorkingMode.value = mode
    }

    fun loadLastChatWorkingMode(): ChatWorkingMode? {
        val saved = mmkv.decodeString(LAST_CHAT_WORKING_MODE) ?: return null
        return runCatching { ChatWorkingMode.valueOf(saved) }.getOrNull()
    }

    private fun sanitizeMaxAgentIterations(value: Int): Int {
        return value.coerceIn(MIN_MAX_AGENT_ITERATIONS, MAX_MAX_AGENT_ITERATIONS)
    }
}
