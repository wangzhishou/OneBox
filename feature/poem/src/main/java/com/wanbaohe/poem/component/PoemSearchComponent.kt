package com.wanbaohe.poem.component

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.poem.model.Poem
import com.wanbaohe.poem.service.PoemInsightService
import com.wanbaohe.poem.service.PoemService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PoemSearchComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("initialQuery") initialQuery: String?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val poemService: PoemService,
    private val insightService: PoemInsightService,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(PoemSearchUiState(query = initialQuery.orEmpty()))
    val uiState = _uiState.asStateFlow()

    init {
        componentScope.launch {
            poemService.fetchDynasties().onSuccess { dynasties ->
                _uiState.update { it.copy(dynasties = dynasties) }
            }
        }
        componentScope.launch {
            poemService.fetchTypes().onSuccess { types ->
                _uiState.update { it.copy(types = types) }
            }
        }
        if (!initialQuery.isNullOrBlank()) {
            search()
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onDynastySelected(dynasty: String?) {
        _uiState.update { it.copy(selectedDynasty = dynasty) }
        search()
    }

    fun onTypeSelected(type: String?) {
        _uiState.update { it.copy(selectedType = type) }
        search()
    }

    fun onAuthorSelected(author: String?) {
        _uiState.update { it.copy(selectedAuthor = author) }
        search()
    }

    /**
     * 搜索:关键词优先;关键词为空但选中了诗人时,以诗人名作为关键词。
     * search API 要求 q≥3 字符,短关键词(如两字诗人)改用 random 带筛选多次取样并本地过滤。
     * 两者皆空则只保留现有结果(靠本地筛选)。
     */
    fun search() {
        val state = uiState.value
        val keyword = state.query.trim()
        val effectiveQuery = keyword.ifBlank { state.selectedAuthor.orEmpty() }
        if (effectiveQuery.isEmpty() || state.isSearching) return
        componentScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            val result = if (effectiveQuery.length >= MIN_QUERY_LENGTH) {
                poemService.searchPoems(effectiveQuery)
            } else {
                // 短关键词:诗人筛选走 author 精确过滤;手输短词走 char 取样 + 本地 contains 过滤
                poemService.fetchRandomPoems(
                    author = state.selectedAuthor,
                    dynasty = state.selectedDynasty,
                    type = state.selectedType,
                    char = keyword.firstOrNull()?.toString(),
                ).map { poems ->
                    if (keyword.isEmpty()) {
                        poems
                    } else {
                        poems.filter { poem ->
                            poem.title.contains(keyword) ||
                                poem.author.contains(keyword) ||
                                poem.content.any { it.contains(keyword) }
                        }
                    }
                }
            }
            result.onSuccess { poems ->
                _uiState.update {
                    it.copy(results = poems, hasSearched = true, isSearching = false)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(hasSearched = true, isSearching = false, error = e.message)
                }
            }
        }
    }

    /** 点击搜索结果:落库 upsert 后页内展示详情,并自动补拼音(静默失败) */
    fun openPoem(poem: Poem) {
        _uiState.update { it.copy(selectedPoem = poem, insightError = null, translationError = null) }
        componentScope.launch(ioDispatcher) {
            poemService.upsertPoem(poem)
            if (poem.pinyin.isNullOrBlank()) {
                insightService.generatePinyin(poem)
                poemService.getPoem(poem.id)?.let { updated ->
                    updateSelectedPoem { updated }
                }
            }
        }
    }

    fun closePoem() {
        _uiState.update {
            it.copy(
                selectedPoem = null,
                isGeneratingInsight = false,
                insightError = null,
                isGeneratingTranslation = false,
                translationError = null,
            )
        }
    }

    fun generateInsight() {
        val poem = uiState.value.selectedPoem ?: return
        if (uiState.value.isGeneratingInsight) return
        componentScope.launch {
            _uiState.update { it.copy(isGeneratingInsight = true, insightError = null) }
            when (val result = insightService.generateInsight(poem)) {
                is PoemInsightService.GenerationResult.Success -> {
                    updateSelectedPoem { it.copy(aiInsight = result.content) }
                    _uiState.update { it.copy(isGeneratingInsight = false) }
                }

                is PoemInsightService.GenerationResult.Failed -> {
                    _uiState.update {
                        it.copy(isGeneratingInsight = false, insightError = result.reason)
                    }
                }
            }
        }
    }

    fun generateTranslation() {
        val poem = uiState.value.selectedPoem ?: return
        if (uiState.value.isGeneratingTranslation) return
        componentScope.launch {
            _uiState.update { it.copy(isGeneratingTranslation = true, translationError = null) }
            when (val result = insightService.generateTranslation(poem)) {
                is PoemInsightService.GenerationResult.Success -> {
                    updateSelectedPoem { it.copy(translation = result.content) }
                    _uiState.update { it.copy(isGeneratingTranslation = false) }
                }

                is PoemInsightService.GenerationResult.Failed -> {
                    _uiState.update {
                        it.copy(isGeneratingTranslation = false, translationError = result.reason)
                    }
                }
            }
        }
    }

    /** 结果卡片/详情上的收藏 toggle,同步结果列表与详情态 */
    fun toggleFavorite(poem: Poem) {
        componentScope.launch {
            poemService.toggleFavorite(poem.id)
            val updated = poem.copy(isFavorite = !poem.isFavorite)
            _uiState.update { state ->
                state.copy(
                    selectedPoem = state.selectedPoem?.takeIf { it.id == poem.id }?.let { updated }
                        ?: state.selectedPoem,
                    results = state.results.map { if (it.id == poem.id) updated else it },
                )
            }
        }
    }

    private fun updateSelectedPoem(transform: (Poem) -> Poem) {
        _uiState.update { state ->
            val updated = state.selectedPoem?.let(transform) ?: return@update state
            state.copy(
                selectedPoem = updated,
                results = state.results.map { if (it.id == updated.id) updated else it },
            )
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("initialQuery") initialQuery: String?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): PoemSearchComponent
    }

    companion object {
        /** search API 要求关键词至少 3 个字符 */
        private const val MIN_QUERY_LENGTH = 3

        /** 诗人筛选固定列表(不调 /api/authors 全量) */
        val FIXED_AUTHORS = listOf(
            "李白", "杜甫", "白居易", "苏轼", "辛弃疾",
            "李清照", "王维", "柳宗元", "陶渊明", "王昌龄",
        )
    }
}
