package com.shifenmiao.search.logic

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.remote.AnalyticsManager
import com.t8rin.imagetoolbox.core.settings.domain.SettingsProvider
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent

class SearchComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    settingsProvider: SettingsProvider,
    val appDatabase: AppDatabase,
    private val analyticsManager: AnalyticsManager,
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
        scheduleSearchTermReport(text)
        if (text.isEmpty()) {
            _searchItemList.value = emptyList()
            return
        }
        CoroutineScope(ioDispatcher).launch {
            val formattedSearchString = "%$text%"
            // 英文标识符(iconName)不含空格,去掉查询里的空格以便 "file browser" 命中 FileBrowser
            val iconSearchString = "%${text.replace(" ", "")}%"
            val queryLower = text.lowercase()
            appDatabase.itemEntityDao()
                .searchByTitleOrDescriptionWithStats(formattedSearchString, iconSearchString)
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

    private var searchReportJob: Job? = null

    // 本次搜索页会话内已上报过的词,避免重复上报
    private val reportedSearchTerms = mutableSetOf<String>()

    /**
     * 防抖上报搜索词(仅 google 渠道经 Firebase Analytics 生效,其余渠道 no-op):
     * 停止输入 1.5s 后才上报,短于 2 个字符不上报,同一会话同一词只报一次
     */
    private fun scheduleSearchTermReport(text: String) {
        searchReportJob?.cancel()
        val term = text.trim().lowercase()
        if (term.length < 2 || !reportedSearchTerms.add(term)) return
        searchReportJob = CoroutineScope(ioDispatcher).launch {
            delay(SEARCH_REPORT_DELAY_MS)
            analyticsManager.logEvent(
                "search",
                mapOf(
                    "search_term" to term.take(100),
                    "result_count" to _searchItemList.value.size
                )
            )
        }
    }

    private fun scoreSearchItem(item: ItemWithCategoriesAndStats, queryLower: String): Int {
        val titleLower = item.item.title.lowercase()
        val descLower = item.item.description.lowercase()
        // 英文标识符(FileBrowser / Crop / PdfTools…):去掉空格后比较,命中权重低于本地化标题
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
            // CMS 关键词(英文名/同义词/拉丁转写)命中:排在同义词层级
            keywordsLower.contains(queryLower) -> score += 130
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

    private companion object {
        const val SEARCH_REPORT_DELAY_MS = 1500L
    }

}