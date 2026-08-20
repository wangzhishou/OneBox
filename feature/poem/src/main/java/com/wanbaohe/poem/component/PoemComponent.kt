package com.wanbaohe.poem.component

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.poem.model.Poem
import com.wanbaohe.poem.model.isPinyinAligned
import com.wanbaohe.poem.service.PoemInsightService
import com.wanbaohe.poem.service.PoemService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PoemComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("poemId") poemId: Long?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val poemService: PoemService,
    private val insightService: PoemInsightService,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(PoemUiState())
    val uiState = _uiState.asStateFlow()

    /** 当前展示的诗词 id;为空表示尚未选择(空状态) */
    private val currentPoemId = MutableStateFlow(poemId)

    /** 用户主动选择/生成过后,不再用「最新一条历史」自动回填 */
    private var userDrivenSelection = poemId != null

    /** 已尝试过自动生成拼音的诗词 id,避免重复触发 */
    private val pinyinAttempts = mutableSetOf<Long>()

    init {
        // 历史记录:全量供滑动翻页与历史弹层;无 deeplink 且用户未操作时,自动回填最新一首
        componentScope.launch {
            poemService.observeHistory().collect { history ->
                _uiState.update { it.copy(history = history) }
                if (!userDrivenSelection && currentPoemId.value == null) {
                    currentPoemId.value = history.firstOrNull()?.id
                }
            }
        }
        // 当前诗词:响应式观察,拼音/解读/翻译异步写库后自动刷新
        componentScope.launch {
            currentPoemId.filterNotNull().flatMapLatest { id ->
                poemService.observePoem(id)
            }.collect { poem ->
                _uiState.update { it.copy(poem = poem, isLoading = false) }
                maybeGeneratePinyin(poem)
            }
        }
    }

    /** 生成一首:随机取诗 */
    fun refresh() {
        if (uiState.value.isLoading) return
        componentScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, insightError = null) }
            poemService.fetchRandomPoem()
                .onSuccess { poem ->
                    userDrivenSelection = true
                    currentPoemId.value = poem.id
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /** 历史点选:回填到卡片 */
    fun selectPoem(id: Long) {
        userDrivenSelection = true
        _uiState.update { it.copy(error = null, insightError = null, translationError = null) }
        currentPoemId.value = id
    }

    fun generateInsight() {
        val poem = uiState.value.poem ?: return
        if (uiState.value.isGeneratingInsight) return
        componentScope.launch {
            _uiState.update { it.copy(isGeneratingInsight = true, insightError = null) }
            when (val result = insightService.generateInsight(poem)) {
                is PoemInsightService.GenerationResult.Success -> {
                    _uiState.update {
                        it.copy(
                            poem = it.poem?.copy(aiInsight = result.content),
                            isGeneratingInsight = false,
                        )
                    }
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
        val poem = uiState.value.poem ?: return
        if (uiState.value.isGeneratingTranslation) return
        componentScope.launch {
            _uiState.update { it.copy(isGeneratingTranslation = true, translationError = null) }
            when (val result = insightService.generateTranslation(poem)) {
                is PoemInsightService.GenerationResult.Success -> {
                    _uiState.update {
                        it.copy(
                            poem = it.poem?.copy(translation = result.content),
                            isGeneratingTranslation = false,
                        )
                    }
                }

                is PoemInsightService.GenerationResult.Failed -> {
                    _uiState.update {
                        it.copy(isGeneratingTranslation = false, translationError = result.reason)
                    }
                }
            }
        }
    }

    /** 清空历史(保留收藏);当前展示的诗词被清掉时回到空状态 */
    fun clearHistory() {
        componentScope.launch {
            val currentId = currentPoemId.value
            poemService.clearHistory()
            userDrivenSelection = true
            if (currentId != null && poemService.getPoem(currentId) == null) {
                currentPoemId.value = null
                _uiState.update { it.copy(poem = null) }
            }
        }
    }

    fun navigateToSearch() {
        onNavigate(Screen.PoemSearch())
    }

    /** 手动触发生成拼音(按钮入口):无拼音时生成,已有(含对齐失败的)时强制重新生成 */
    fun generatePinyin() {
        maybeGeneratePinyin(uiState.value.poem, force = true)
    }

    /**
     * 生成拼音:静默失败,不阻塞卡片展示;生成中置位供底部状态提示;失败允许下次重试。
     * 自动流程(force=false)在已有可用拼音时跳过;拼音存在但对齐失败时视为无拼音,允许重新生成。
     */
    private fun maybeGeneratePinyin(poem: Poem?, force: Boolean = false) {
        if (poem == null) return
        if (!force && poem.isPinyinAligned()) return
        if (uiState.value.isGeneratingPinyin) return
        if (!pinyinAttempts.add(poem.id)) return
        componentScope.launch {
            _uiState.update { it.copy(isGeneratingPinyin = true) }
            val result = insightService.generatePinyin(poem)
            if (result is PoemInsightService.GenerationResult.Failed) {
                pinyinAttempts.remove(poem.id)
            }
            _uiState.update { it.copy(isGeneratingPinyin = false) }
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("poemId") poemId: Long?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): PoemComponent
    }
}
