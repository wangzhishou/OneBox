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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PoemHistoryComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val poemService: PoemService,
    private val insightService: PoemInsightService,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(PoemHistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        componentScope.launch {
            uiState.map { it.favoritesOnly }
                .distinctUntilChanged()
                .flatMapLatest { favoritesOnly ->
                    if (favoritesOnly) poemService.observeFavorites()
                    else poemService.observeHistory()
                }
                .collect { poems ->
                    _uiState.update { state ->
                        state.copy(
                            poems = poems,
                            selectedPoem = state.selectedPoem?.let { selected ->
                                poems.find { it.id == selected.id } ?: selected
                            },
                        )
                    }
                }
        }
    }

    fun toggleFavoritesOnly() {
        _uiState.update { it.copy(favoritesOnly = !it.favoritesOnly) }
    }

    fun openPoem(poem: Poem) {
        _uiState.update { it.copy(selectedPoem = poem, insightError = null) }
    }

    fun closePoem() {
        _uiState.update {
            it.copy(selectedPoem = null, isGeneratingInsight = false, insightError = null)
        }
    }

    fun deletePoem(poem: Poem) {
        componentScope.launch {
            poemService.deletePoem(poem.id)
            _uiState.update {
                it.copy(selectedPoem = if (it.selectedPoem?.id == poem.id) null else it.selectedPoem)
            }
        }
    }

    fun generateInsight() {
        val poem = uiState.value.selectedPoem ?: return
        if (uiState.value.isGeneratingInsight) return
        componentScope.launch {
            _uiState.update { it.copy(isGeneratingInsight = true, insightError = null) }
            when (val result = insightService.generateInsight(poem)) {
                is PoemInsightService.GenerationResult.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedPoem = it.selectedPoem?.copy(aiInsight = result.content),
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

    fun toggleFavorite() {
        val poem = uiState.value.selectedPoem ?: return
        componentScope.launch {
            poemService.toggleFavorite(poem.id)
            _uiState.update {
                it.copy(selectedPoem = it.selectedPoem?.copy(isFavorite = !poem.isFavorite))
            }
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): PoemHistoryComponent
    }
}
