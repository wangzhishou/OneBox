package com.shifenmiao.search.logic

import android.content.Context
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.search.GlobalSearchDestination
import com.shifenmiao.common.search.GlobalSearchResult
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.remote.AnalyticsManager
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.settings.domain.SettingsProvider
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.content.ContentRouter
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class SearchComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    settingsProvider: SettingsProvider,
    val appDatabase: AppDatabase,
    private val analyticsManager: AnalyticsManager,
    private val globalSearchCoordinator: GlobalSearchCoordinator,
    private val contentRouter: ContentRouter,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _searchItemList = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val searchItemList: StateFlow<List<ItemWithCategoriesAndStats>> = _searchItemList

    private val _searchMessageEntity = MutableStateFlow<List<MessageEntity>>(emptyList())
    val searchMessageEntity: StateFlow<List<MessageEntity>> = _searchMessageEntity

    private val _globalSearchResults = MutableStateFlow<List<GlobalSearchResult>>(emptyList())
    val globalSearchResults: StateFlow<List<GlobalSearchResult>> = _globalSearchResults

    private val _isGlobalSearching = MutableStateFlow(false)
    val isGlobalSearching: StateFlow<Boolean> = _isGlobalSearching

    private val _isGlobalSearchTruncated = MutableStateFlow(false)
    val isGlobalSearchTruncated: StateFlow<Boolean> = _isGlobalSearchTruncated

    private val _recommendedFlow = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val recommendedFlow: StateFlow<List<ItemWithCategoriesAndStats>> get() = _recommendedFlow

    private val _recentClickedFlow = MutableStateFlow<List<ItemWithCategoriesAndStats>>(emptyList())
    val recentClickedFlow: StateFlow<List<ItemWithCategoriesAndStats>> get() = _recentClickedFlow

    private var searchJob by smartJob()
    private var searchReportJob by smartJob()
    private val reportedSearchTerms = mutableSetOf<String>()

    @Volatile
    private var searchGeneration = 0L

    init {
        loadRecommended()
        loadRecentClicked()
    }

    private fun loadRecommended() {
        componentScope.launch(ioDispatcher) {
            appDatabase.itemEntityDao().getRecommendedItems().collect {
                _recommendedFlow.value = it.take(10)
            }
        }
    }

    private fun loadRecentClicked() {
        componentScope.launch(ioDispatcher) {
            appDatabase.itemEntityDao().getRecentClickedItems().collect {
                _recentClickedFlow.value = it.take(10)
            }
        }
    }

    fun onSearchQueryChange(text: String) {
        scheduleSearchTermReport(text)
        val query = text.trim()
        val generation = ++searchGeneration
        searchJob = null
        clearSearchResults()
        if (query.isEmpty()) {
            _isGlobalSearching.value = false
            return
        }

        _isGlobalSearching.value = true
        searchJob = componentScope.launch(ioDispatcher) {
            delay(SEARCH_DEBOUNCE_MS)
            supervisorScope {
                launch { collectItemResults(query, generation) }
                launch { collectMessageResults(query, generation) }
                launch { collectGlobalResults(query, generation) }
            }
        }
    }

    private fun clearSearchResults() {
        _searchItemList.value = emptyList()
        _searchMessageEntity.value = emptyList()
        _globalSearchResults.value = emptyList()
        _isGlobalSearchTruncated.value = false
    }

    private suspend fun collectItemResults(query: String, generation: Long) {
        val formattedSearchString = "%$query%"
        val iconSearchString = "%${query.replace(" ", "")}%"
        val queryLower = query.lowercase()
        appDatabase.itemEntityDao()
            .searchByTitleOrDescriptionWithStats(formattedSearchString, iconSearchString)
            .distinctUntilChanged()
            .collect { items ->
                if (generation != searchGeneration) return@collect
                _searchItemList.value = items.sortedWith(
                    compareByDescending<ItemWithCategoriesAndStats> { scoreSearchItem(it, queryLower) }
                        .thenByDescending { it.clickCount }
                        .thenBy { it.item.title }
                )
            }
    }

    private suspend fun collectMessageResults(query: String, generation: Long) {
        val formattedSearchString = "%$query%"
        val queryLower = query.lowercase()
        appDatabase.messageDao().searchQuestionOrAnswer(formattedSearchString)
            .distinctUntilChanged()
            .collect { messages ->
                if (generation != searchGeneration) return@collect
                _searchMessageEntity.value = messages.sortedWith(
                    compareByDescending<MessageEntity> { scoreMessageEntity(it, queryLower) }
                        .thenByDescending { it.createdAt }
                )
            }
    }

    private suspend fun collectGlobalResults(query: String, generation: Long) {
        val pagesBySource = mutableMapOf<String, com.shifenmiao.common.search.GlobalSearchResultPage>()
        try {
            globalSearchCoordinator.search(query, GLOBAL_SEARCH_LIMIT).collect { update ->
                if (generation != searchGeneration) return@collect
                pagesBySource[update.sourceId] = update.page
                _globalSearchResults.value = pagesBySource.values
                    .flatMap { it.results }
                    .sortedWith(
                        compareByDescending<GlobalSearchResult> { it.relevance }
                            .thenBy { it.title.lowercase() }
                    )
                _isGlobalSearchTruncated.value = pagesBySource.values.any { it.isTruncated }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (generation == searchGeneration) {
                _globalSearchResults.value = emptyList()
                _isGlobalSearchTruncated.value = false
            }
        }
        if (generation == searchGeneration) _isGlobalSearching.value = false
    }

    fun openGlobalSearchResult(
        result: GlobalSearchResult,
        context: Context,
        onNavigate: (Screen) -> Unit,
    ) {
        when (val destination = result.destination) {
            is GlobalSearchDestination.Navigate -> onNavigate(destination.screen)
            is GlobalSearchDestination.OpenContent -> contentRouter.route(
                uri = destination.uri.toUri(),
                context = context,
                onNavigate = onNavigate,
                fallbackToExternal = false,
            )
        }
    }

    fun recordClick(itemId: Int) {
        componentScope.launch(ioDispatcher) {
            appDatabase.itemEntityDao().recordClick(itemId, System.currentTimeMillis())
        }
    }

    private fun scheduleSearchTermReport(text: String) {
        searchReportJob = null
        val term = text.trim().lowercase()
        if (term.length < 2 || !reportedSearchTerms.add(term)) return
        searchReportJob = componentScope.launch(ioDispatcher) {
            delay(SEARCH_REPORT_DELAY_MS)
            analyticsManager.logEvent(
                "search",
                mapOf(
                    "search_term" to term.take(100),
                    "result_count" to _searchItemList.value.size +
                        _searchMessageEntity.value.size + _globalSearchResults.value.size
                )
            )
        }
    }

    private fun scoreSearchItem(item: ItemWithCategoriesAndStats, queryLower: String): Int {
        val titleLower = item.item.title.lowercase()
        val descLower = item.item.description.lowercase()
        val iconLower = item.item.iconName.orEmpty().lowercase()
        val keywordsLower = item.item.keywords.lowercase()
        val queryCompact = queryLower.replace(" ", "")
        var score = 0
        when {
            titleLower == queryLower -> score += 400
            titleLower.startsWith(queryLower) -> score += 300
            iconLower == queryCompact -> score += 250
            titleLower.contains(queryLower) -> score += 200
            queryCompact.isNotEmpty() && iconLower.contains(queryCompact) -> score += 150
            keywordsLower.contains(queryLower) -> score += 130
            descLower.contains(queryLower) -> score += 100
        }
        if (item.item.recommend) score += 50
        score += (kotlin.math.ln(item.clickCount.coerceAtLeast(0).toDouble() + 1) * 5).toInt()
        return score
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

    private companion object {
        const val SEARCH_REPORT_DELAY_MS = 1500L
        const val SEARCH_DEBOUNCE_MS = 300L
        const val GLOBAL_SEARCH_LIMIT = 20
    }
}
