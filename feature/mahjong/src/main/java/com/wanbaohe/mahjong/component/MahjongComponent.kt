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

package com.wanbaohe.mahjong.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.mahjong.application.port.outbound.MahjongStats
import com.wanbaohe.mahjong.application.port.outbound.MahjongStore
import com.wanbaohe.mahjong.application.usecase.NewRoundUseCase
import com.wanbaohe.mahjong.application.usecase.PlayActionUseCase
import com.wanbaohe.mahjong.application.usecase.RunAiTurnUseCase
import com.wanbaohe.mahjong.domain.model.GamePhase
import com.wanbaohe.mahjong.domain.model.MahjongAction
import com.wanbaohe.mahjong.domain.model.MahjongState
import com.wanbaohe.mahjong.domain.model.MahjongTransition
import com.wanbaohe.mahjong.domain.model.Tiles
import com.wanbaohe.mahjong.domain.rules.MahjongEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MahjongScreenPhase { HOME, GAME }

data class MahjongUiState(
    val screen: MahjongScreenPhase = MahjongScreenPhase.HOME,
    val game: MahjongState? = null,
    val hasSavedGame: Boolean = false,
    val selectedTileId: Int? = null,
    val isAiThinking: Boolean = false,
    val showRules: Boolean = false,
    val stats: MahjongStats = MahjongStats(),
) {
    val humanActions: List<MahjongAction>
        get() = game?.let { MahjongEngine.legalActions(it, Tiles.HUMAN_SEAT) }.orEmpty()
}

class MahjongComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val newRound: NewRoundUseCase,
    private val playAction: PlayActionUseCase,
    private val runAiTurn: RunAiTurnUseCase,
    private val store: MahjongStore,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(MahjongUiState())
    val uiState = _uiState.asStateFlow()

    private var aiJob: Job? = null
    private var generation = 0L
    private var isVisible = true

    init {
        _uiState.update {
            it.copy(
                hasSavedGame = store.loadSnapshot() != null,
                stats = store.loadStats(),
            )
        }
        componentContext.lifecycle.doOnStart {
            isVisible = true
            scheduleAi()
        }
        componentContext.lifecycle.doOnStop {
            isVisible = false
            cancelAi()
            persistSnapshot()
        }
        componentContext.lifecycle.doOnDestroy {
            cancelAi()
            persistSnapshot()
        }
    }

    fun startNewGame() {
        cancelAi()
        val game = newRound()
        _uiState.update {
            it.copy(
                screen = MahjongScreenPhase.GAME,
                game = game,
                hasSavedGame = true,
                selectedTileId = null,
                showRules = false,
            )
        }
        persistSnapshot()
        scheduleAi()
    }

    fun continueSavedGame() {
        val saved = store.loadSnapshot()
        if (saved == null) {
            _uiState.update { it.copy(hasSavedGame = false) }
            return
        }
        cancelAi()
        _uiState.update {
            it.copy(
                screen = MahjongScreenPhase.GAME,
                game = saved,
                selectedTileId = null,
                hasSavedGame = true,
            )
        }
        scheduleAi()
    }

    fun returnHome() {
        cancelAi()
        persistSnapshot()
        val hasSaved = _uiState.value.game?.phase != GamePhase.FINISHED &&
            _uiState.value.game != null
        _uiState.update {
            it.copy(
                screen = MahjongScreenPhase.HOME,
                game = null,
                hasSavedGame = hasSaved,
                selectedTileId = null,
                showRules = false,
            )
        }
    }

    fun leaveModule() {
        cancelAi()
        persistSnapshot()
        onGoBack()
    }

    fun onTileSelected(tileId: Int) {
        val canDiscard = _uiState.value.humanActions
            .filterIsInstance<MahjongAction.Discard>()
            .any { it.tileId == tileId }
        if (!canDiscard) return
        if (_uiState.value.selectedTileId == tileId) {
            discardSelected()
        } else {
            _uiState.update { it.copy(selectedTileId = tileId) }
        }
    }

    fun discardSelected() {
        val tileId = _uiState.value.selectedTileId ?: return
        performHumanAction(MahjongAction.Discard(Tiles.HUMAN_SEAT, tileId))
    }

    fun performHumanAction(action: MahjongAction) {
        if (action.seat != Tiles.HUMAN_SEAT) return
        cancelAi()
        applyAction(action)
        scheduleAi()
    }

    fun showRules(show: Boolean) {
        _uiState.update { it.copy(showRules = show) }
    }

    private fun applyAction(action: MahjongAction): Boolean {
        val current = _uiState.value.game ?: return false
        val transition = playAction(current, action) ?: return false
        applyTransition(transition)
        return true
    }

    private fun applyTransition(transition: MahjongTransition) {
        val game = transition.state
        _uiState.update {
            it.copy(
                game = game,
                selectedTileId = null,
                hasSavedGame = game.phase != GamePhase.FINISHED,
            )
        }
        if (game.phase == GamePhase.FINISHED) {
            store.clearSnapshot()
            recordResult(game)
        } else {
            store.saveSnapshot(game)
        }
    }

    private fun recordResult(game: MahjongState) {
        val result = game.result ?: return
        val previous = store.loadStats()
        val humanWon = result.wins.any { it.seat == Tiles.HUMAN_SEAT }
        val stats = previous.copy(
            games = previous.games + 1,
            wins = previous.wins + if (humanWon) 1 else 0,
            draws = previous.draws + if (result.isDraw) 1 else 0,
            bestScore = maxOf(previous.bestScore, game.players[Tiles.HUMAN_SEAT].score),
        )
        store.saveStats(stats)
        _uiState.update { it.copy(stats = stats) }
        if (humanWon) ReviewPromptHost.notifySuccess(ReviewPromptTrigger.GAME_WIN)
    }

    private fun scheduleAi() {
        if (!isVisible || aiJob?.isActive == true) return
        val state = _uiState.value
        if (state.screen != MahjongScreenPhase.GAME || state.game == null ||
            state.game.phase == GamePhase.FINISHED
        ) return

        val expectedGeneration = generation
        aiJob = componentScope.launch {
            try {
                while (isActive && generation == expectedGeneration && isVisible) {
                    val game = _uiState.value.game ?: break
                    if (game.phase == GamePhase.FINISHED ||
                        MahjongEngine.legalActions(game, Tiles.HUMAN_SEAT).isNotEmpty()
                    ) break
                    val seat = (1 until Tiles.PLAYER_COUNT).firstOrNull {
                        MahjongEngine.legalActions(game, it).isNotEmpty()
                    } ?: break

                    _uiState.update { it.copy(isAiThinking = true) }
                    delay(AI_DELAY_MILLIS)
                    val action = withContext(defaultDispatcher) { runAiTurn(game, seat) }
                    ensureActive()
                    if (generation != expectedGeneration || !isVisible) break
                    if (!applyAction(action)) break
                }
            } finally {
                _uiState.update { it.copy(isAiThinking = false) }
            }
        }
    }

    private fun cancelAi() {
        generation++
        aiJob?.cancel()
        aiJob = null
        _uiState.update { it.copy(isAiThinking = false) }
    }

    private fun persistSnapshot() {
        val game = _uiState.value.game ?: return
        if (game.phase == GamePhase.FINISHED) store.clearSnapshot() else store.saveSnapshot(game)
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): MahjongComponent
    }

    private companion object {
        const val AI_DELAY_MILLIS = 320L
    }
}
