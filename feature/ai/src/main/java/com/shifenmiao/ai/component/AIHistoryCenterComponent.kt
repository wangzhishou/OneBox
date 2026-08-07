package com.shifenmiao.ai.component

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.ai.model.AIHistoryItem
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.handle.AIConversationNavigation
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.AIConversationTitleSource
import com.shifenmiao.model.state.PageState
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
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
                    .map { entity ->
                        AIHistoryItem(
                            conversationId = entity.conversationId,
                            title = entity.title,
                            appTitle = entity.appTitle,
                            preview = entity.lastMessagePreview.ifBlank {
                                entity.lastUserMessagePreview.ifBlank { entity.placeholder }
                            },
                            entryType = runCatching {
                                AIConversationEntryType.valueOf(entity.entryType)
                            }.getOrDefault(AIConversationEntryType.CHAT),
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

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialFilter: AIConversationEntryType?,
        ): AIHistoryCenterComponent
    }
}
