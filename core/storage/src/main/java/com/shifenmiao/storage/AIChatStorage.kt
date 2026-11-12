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

package com.shifenmiao.storage

import com.shifenmiao.model.ai.AIChatMode
import com.shifenmiao.model.ai.Conversation
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AIChatStorage {

    const val DEFAULT_MAX_AGENT_ITERATIONS = 30
    const val MIN_MAX_AGENT_ITERATIONS = 10
    const val MAX_MAX_AGENT_ITERATIONS = 60

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.AI_CHAT_SETTING)

    // 会话快照按语言隔离（"ai_chat_<locale>"）：快照内嵌 AiEngine，含本地化 title/description
    private val localeMmkv: MMKV get() = localizedMmkv(MMKVName.AI_CHAT_SETTING)

    private var cachedConversation: Conversation? = null

    private const val AI_CHAT_SETTING = "ai_chat_setting"
    private const val IS_ENABLE_WEB_SEARCH = "is_enable_web_search"
    private const val IS_ENABLE_REASONING = "is_enable_reasoning"
    private const val IS_ENABLE_CONVERSATION_TITLE_SUMMARY = "is_enable_conversation_title_summary"
    private const val MAX_AGENT_ITERATIONS = "max_agent_iterations"
    private const val IS_ENABLE_MEMORY = "is_enable_memory"
    private const val IS_ENABLE_SKILLS = "is_enable_skills"
    private const val IS_ENABLE_CONTEXT_COMPACTION = "is_enable_context_compaction"
    private const val CHAT_MODE = "chat_mode"

    private val _isEnableWebSearch = MutableStateFlow(loadIsEnableWebSearch())
    val isEnableWebSearch: StateFlow<Boolean> get() = _isEnableWebSearch
    private val _isEnableReasoning = MutableStateFlow(loadIsEnableReasoning())
    val isEnableReasoning: StateFlow<Boolean> get() = _isEnableReasoning
    private val _isEnableConversationTitleSummary = MutableStateFlow(loadIsEnableConversationTitleSummary())
    val isEnableConversationTitleSummary: StateFlow<Boolean> get() = _isEnableConversationTitleSummary
    private val _maxAgentIterations = MutableStateFlow(loadMaxAgentIterations())
    val maxAgentIterations: StateFlow<Int> get() = _maxAgentIterations
    private val _isEnableMemory = MutableStateFlow(loadIsEnableMemory())
    val isEnableMemory: StateFlow<Boolean> get() = _isEnableMemory
    private val _isEnableSkills = MutableStateFlow(loadIsEnableSkills())
    val isEnableSkills: StateFlow<Boolean> get() = _isEnableSkills
    private val _isEnableContextCompaction = MutableStateFlow(loadIsEnableContextCompaction())
    val isEnableContextCompaction: StateFlow<Boolean> get() = _isEnableContextCompaction
    private val _chatMode = MutableStateFlow(loadChatMode())
    val chatMode: StateFlow<AIChatMode> get() = _chatMode

    fun saveConfigs(conversation: Conversation) {
        val copy = conversation.copy()
        localeMmkv.encode(AI_CHAT_SETTING + conversation.entryType.name, copy, 24 * 60 * 60 * 1)
        cachedConversation = copy
    }

    fun loadConfigs(entryTypeName: String): Conversation? {
        if (cachedConversation != null) {
            return cachedConversation
        }
        cachedConversation =
            localeMmkv.decodeParcelable(AI_CHAT_SETTING + entryTypeName, Conversation::class.java)
        return cachedConversation
    }

    /**
     * 清空当前语言的会话快照。开关类字段（web_search/reasoning 等用户偏好）
     * 在全局文件中，不受此清理影响。
     */
    fun clearConfigs() {
        localeMmkv.clear()
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

    fun saveIsEnableMemory(enabled: Boolean) {
        mmkv.encode(IS_ENABLE_MEMORY, enabled)
        _isEnableMemory.value = enabled
    }

    fun loadIsEnableMemory(): Boolean {
        return mmkv.decodeBool(IS_ENABLE_MEMORY, true)
    }

    fun saveIsEnableSkills(enabled: Boolean) {
        mmkv.encode(IS_ENABLE_SKILLS, enabled)
        _isEnableSkills.value = enabled
    }

    fun loadIsEnableSkills(): Boolean {
        return mmkv.decodeBool(IS_ENABLE_SKILLS, true)
    }

    fun saveIsEnableContextCompaction(enabled: Boolean) {
        mmkv.encode(IS_ENABLE_CONTEXT_COMPACTION, enabled)
        _isEnableContextCompaction.value = enabled
    }

    fun loadIsEnableContextCompaction(): Boolean {
        return mmkv.decodeBool(IS_ENABLE_CONTEXT_COMPACTION, true)
    }

    fun saveChatMode(mode: AIChatMode) {
        mmkv.encode(CHAT_MODE, mode.name)
        _chatMode.value = mode
    }

    fun loadChatMode(): AIChatMode {
        val saved = mmkv.decodeString(CHAT_MODE) ?: return AIChatMode.AGENT
        return runCatching { AIChatMode.valueOf(saved) }.getOrDefault(AIChatMode.AGENT)
    }

    private fun sanitizeMaxAgentIterations(value: Int): Int {
        return value.coerceIn(MIN_MAX_AGENT_ITERATIONS, MAX_MAX_AGENT_ITERATIONS)
    }
}
