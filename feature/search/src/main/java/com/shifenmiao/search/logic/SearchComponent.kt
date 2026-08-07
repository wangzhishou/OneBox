package com.shifenmiao.search.logic

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.settings.domain.SettingsProvider
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent

class SearchComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    settingsProvider: SettingsProvider,
    val appDatabase: AppDatabase,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _searchItemList = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val searchItemList: StateFlow<List<ItemWithCategoriesAndStats>> = _searchItemList

    private val _searchMessageEntity = MutableStateFlow<List<MessageEntity>>(emptyList())
    val searchMessageEntity: StateFlow<List<MessageEntity>> = _searchMessageEntity

    private val _recommendedFlow = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val recommendedFlow: StateFlow<List<ItemWithCategoriesAndStats>> get() = _recommendedFlow

    private val _recentClickedFlow = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val recentClickedFlow: StateFlow<List<ItemWithCategoriesAndStats>> get() = _recentClickedFlow

    init {
        loadRecommended()
        loadRecentClicked()
    }

    private fun loadRecommended() {
        CoroutineScope(ioDispatcher).launch {
            appDatabase.itemEntityDao().getRecommendedItems().collect {
                _recommendedFlow.value = it.take(10)
            }
        }
    }

    private fun loadRecentClicked() {
        CoroutineScope(ioDispatcher).launch {
            appDatabase.itemEntityDao().getRecentClickedItems().collect {
                _recentClickedFlow.value = it.take(10)
            }
        }
    }


    fun onSearchQueryChange(text: String) {
        if (text.isEmpty()) {
            _searchItemList.value = emptyList()
            return
        }
        CoroutineScope(ioDispatcher).launch {
            val formattedSearchString = "%$text%"
            val queryLower = text.lowercase()
            appDatabase.itemEntityDao().searchByTitleOrDescriptionWithStats(formattedSearchString)
                .distinctUntilChanged().collect { items ->
                    _searchItemList.value = items.sortedWith(
                        compareByDescending<ItemWithCategoriesAndStats> { scoreSearchItem(it, queryLower) }
                            .thenByDescending { it.clickCount }
                            .thenBy { it.item.title }
                    )
                }
        }
    }

    fun recordClick(itemId: Int) {
        CoroutineScope(ioDispatcher).launch {
            appDatabase.itemEntityDao().recordClick(itemId, System.currentTimeMillis())
        }
    }

    private fun scoreSearchItem(item: ItemWithCategoriesAndStats, queryLower: String): Int {
        val titleLower = item.item.title.lowercase()
        val descLower = item.item.description.lowercase()
        var score = 0
        when {
            titleLower == queryLower -> score += 400
            titleLower.startsWith(queryLower) -> score += 300
            titleLower.contains(queryLower) -> score += 200
            descLower.contains(queryLower) -> score += 100
        }
        if (item.item.recommend) score += 50
        score += (kotlin.math.ln(item.clickCount.coerceAtLeast(0).toDouble() + 1) * 5).toInt()
        return score
    }

    fun onMessageSearchQueryChange(text: String) {
        if (text.isEmpty()) {
            _searchMessageEntity.value = emptyList()
            return
        }
        CoroutineScope(ioDispatcher).launch {
            val formattedSearchString = "%$text%"
            val queryLower = text.lowercase()
            appDatabase.messageDao().searchQuestionOrAnswer(formattedSearchString)
                .distinctUntilChanged().collect { messages ->
                    _searchMessageEntity.value = messages.sortedWith(
                        compareByDescending<MessageEntity> { scoreMessageEntity(it, queryLower) }
                            .thenByDescending { it.createdAt }
                    )
                }
        }
    }

    private fun scoreMessageEntity(message: MessageEntity, queryLower: String): Int {
        val questionLower = message.question.lowercase()
        val titleLower = message.title.lowercase()
        val answerLower = message.answer.lowercase()
        var score = 0
        when {
            questionLower == queryLower -> score += 400
            questionLower.startsWith(queryLower) -> score += 300
            questionLower.contains(queryLower) -> score += 200
            titleLower.contains(queryLower) -> score += 150
            answerLower.contains(queryLower) -> score += 100
        }
        return score
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): SearchComponent
    }

}