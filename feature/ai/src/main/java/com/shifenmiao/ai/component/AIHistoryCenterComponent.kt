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

package com.shifenmiao.ai.component

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.ai.history.AIDuelConversationTitle
import com.shifenmiao.ai.model.AIDuelConfigCodec
import com.shifenmiao.ai.model.AIHistoryItem
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.handle.AIConversationNavigation
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.AIConversationTitleSource
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.state.PageState
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

class AIHistoryCenterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val initialFilter: AIConversationEntryType?,
    @ApplicationContext private val applicationContext: Context,
    private val appDatabase: AppDatabase,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _pageState = MutableStateFlow(PageState.INITIALIZING)
    val pageState: StateFlow<PageState> = _pageState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedFilter = MutableStateFlow(initialFilter)
    val selectedFilter: StateFlow<AIConversationEntryType?> = _selectedFilter.asStateFlow()

    private val _items = MutableStateFlow<List<AIHistoryItem>>(emptyList())
    val items: StateFlow<List<AIHistoryItem>> = _items.asStateFlow()
    private var historyJob: Job? = null

    init {
        observeHistory()
    }

    fun onQueryChange(value: String) {
        _query.value = value
        observeHistory()
    }

    fun onFilterChange(value: AIConversationEntryType?) {
        _selectedFilter.value = value
        observeHistory()
    }

    fun renameConversation(conversationId: String, title: String) {
        val normalized = title.trim()
        if (normalized.isEmpty()) {
            ActionUtils.showToast(applicationContext.getString(R.string.ai_history_rename_empty))
            return
        }
        componentScope.launch(ioDispatcher) {
            appDatabase.conversationDao().updateTitle(
                conversationId = conversationId,
                title = normalized,
                titleSource = AIConversationTitleSource.MANUAL.name,
            )
            appDatabase.messageDao().updateTitlesByConversationId(
                conversationId = conversationId,
                title = normalized
            )
        }
    }

    fun deleteConversation(conversationId: String) {
        componentScope.launch(ioDispatcher) {
            appDatabase.messageDao().deleteMessagesByConversationId(conversationId)
            appDatabase.conversationDao().deleteConversationByConversationId(conversationId)
            // 顺手清掉会话级记忆/技能开关，避免死行堆积
            appDatabase.conversationMemoryPolicyDao().deleteByConversationId(conversationId)
        }
    }

    fun openConversation(item: AIHistoryItem, onNavigate: (Screen) -> Unit) {
        val screen = AIConversationNavigation.buildHistoryDetailScreen(
            conversationId = item.conversationId,
            title = item.title,
            appTitle = item.appTitle,
            entryType = item.entryType,
            entryRefId = item.entryRefId,
        )
        onNavigate(screen)
    }

    private fun observeHistory() {
        historyJob?.cancel()
        historyJob = componentScope.launch(ioDispatcher) {
            _pageState.value = PageState.INITIALIZING
            appDatabase.conversationDao().observeHistoryConversations().collectLatest { entities ->
                val keyword = _query.value.trim()
                val filter = _selectedFilter.value
                val messageCountsByConversationId = entities.map { it.conversationId }
                    .distinct()
                    .takeIf { it.isNotEmpty() }
                    ?.let { conversationIds ->
                        appDatabase.messageDao()
                            .getActiveMessageCountsByConversationIds(conversationIds)
                            .associate { it.conversationId to it.messageCount }
                    }
                    .orEmpty()
                _items.value = entities.asSequence()
                    .mapNotNull { entity ->
                        val entryType = runCatching {
                            AIConversationEntryType.valueOf(entity.entryType)
                        }.getOrDefault(AIConversationEntryType.CHAT)
                        val storedPreview = entity.lastMessagePreview.ifBlank {
                            entity.lastUserMessagePreview.ifBlank { entity.placeholder }
                        }
                        // 旧版互聊把「A × B」同时写进标题、预览和 badge；按真实发言重建展示字段，
                        // 一条发言都没有的残留草稿（0 条消息）不进历史中心
                        val duelDisplay = if (entryType == AIConversationEntryType.DUEL) {
                            resolveDuelHistoryDisplay(entity, storedPreview)
                                ?: return@mapNotNull null
                        } else {
                            null
                        }
                        AIHistoryItem(
                            conversationId = entity.conversationId,
                            title = duelDisplay?.first ?: entity.title,
                            appTitle = entity.appTitle,
                            preview = duelDisplay?.second ?: storedPreview,
                            entryType = entryType,
                            entryRefId = entity.entryRefId,
                            lastActiveAt = entity.lastActiveAt,
                            messageCount = messageCountsByConversationId[entity.conversationId] ?: 0,
                        )
                    }
                    .filter { item ->
                        (filter == null || item.entryType == filter) &&
                            (keyword.isBlank() ||
                                item.title.contains(keyword, ignoreCase = true) ||
                                item.appTitle.contains(keyword, ignoreCase = true) ||
                                item.preview.contains(keyword, ignoreCase = true))
                    }
                    .sortedByDescending { it.lastActiveAt }
                    .toList()
                _pageState.value = PageState.IDLE
            }
        }
    }

    /**
     * 互聊历史的展示规则：大标题取话题/开场发言，预览取最后一句发言，
     * badge 仍是 appTitle（对战双方）。返回 null 表示会话没有任何发言，应当隐藏。
     * 兼容旧版本落下的空标题行，不需要数据迁移。
     */
    private fun resolveDuelHistoryDisplay(
        entity: ConversationEntity,
        storedPreview: String,
    ): Pair<String, String>? {
        val turns = appDatabase.messageDao()
            .getMessagesByConversationId(entity.conversationId)
            .filter { it.entryType == AIConversationEntryType.DUEL && !it.expired }
            .mapNotNull { message ->
                AIDuelConversationTitle.normalize(
                    if (message.role == RoleType.USER.value) message.question else message.answer
                ).takeIf { it.isNotBlank() }
            }
        if (turns.isEmpty()) return null

        val scene = AIDuelConfigCodec.decodeOrNull(entity.prompt)?.scene.orEmpty()
        val title = if (entity.title.isNotBlank()) {
            entity.title
        } else {
            AIDuelConversationTitle.resolve(
                scene = scene,
                firstMessage = turns.first(),
                fallbackTitle = applicationContext.getString(R.string.ai_duel_chat_title),
            )
        }
        // 旧数据把「A × B」当预览写进了 last_message_preview，不能再用它当内容摘要
        val preview = if (storedPreview.isBlank() || storedPreview == entity.appTitle) {
            turns.last()
        } else {
            storedPreview
        }
        return title to preview
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialFilter: AIConversationEntryType?,
        ): AIHistoryCenterComponent
    }
}
