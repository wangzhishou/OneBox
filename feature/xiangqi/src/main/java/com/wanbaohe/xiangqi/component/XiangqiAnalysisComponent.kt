package com.wanbaohe.xiangqi.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.xiangqi.application.usecase.AudioFeedbackUseCase
import com.wanbaohe.xiangqi.application.usecase.ExportGameUseCase
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.application.usecase.SettingsUseCase
import com.wanbaohe.xiangqi.data.XiangqiPlyRecord
import com.wanbaohe.xiangqi.data.TextExportLabels
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.GameStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class XiangqiAnalysisUiState(
    val title: String = "",
    val boardState: BoardState = FenCodec.parse(FenCodec.INITIAL_FEN),
    val currentPly: Int = 0,
    val maxPly: Int = 0,
    val plies: List<XiangqiPlyRecord> = emptyList(),
    val exportContent: String = "",
    val status: GameStatus = GameStatus.PLAYING,
    val isAutoPlaying: Boolean = false,
)

class XiangqiAnalysisComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val gameId: String,
    @Assisted initialPly: Int,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val gameQuery: GameQueryUseCase,
    private val exportGame: ExportGameUseCase,
    private val audioFeedback: AudioFeedbackUseCase,
    private val settingsUseCase: SettingsUseCase,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    var uiState by mutableStateOf(XiangqiAnalysisUiState())
        private set

    private var targetInitialPly by mutableIntStateOf(initialPly)
    private var autoPlayJob: Job? = null

    fun toggleAutoPlay() {
        if (uiState.isAutoPlaying) {
            stopAutoPlay()
        } else {
            startAutoPlay()
        }
    }

    fun startAutoPlay() {
        stopAutoPlay()
        if (uiState.currentPly >= uiState.maxPly) {
            goToStart()
        }
        uiState = uiState.copy(isAutoPlaying = true)
        autoPlayJob = componentScope.launch {
            while (uiState.isAutoPlaying) {
                delay(800)
                if (!uiState.isAutoPlaying) break
                if (uiState.currentPly >= uiState.maxPly) {
                    stopAutoPlay()
                    break
                }
                goNext()
            }
        }
    }

    fun stopAutoPlay() {
        autoPlayJob?.cancel()
        autoPlayJob = null
        uiState = uiState.copy(isAutoPlaying = false)
    }

    init {
        componentScope.launch {
            gameQuery.observeById(gameId).collect { detail ->
                detail ?: return@collect
                val plies = detail.plies.map { it.toLegacy() }
                val maxPly = plies.maxOfOrNull { it.ply } ?: 0
                val target = if (targetInitialPly >= 0) {
                    targetInitialPly.coerceIn(0, maxPly).also { targetInitialPly = -1 }
                } else {
                    detail.currentPly.coerceIn(0, maxPly)
                }
                updatePly(
                    plies,
                    detail.initialFen,
                    detail.title,
                    target,
                    play = false,
                )
            }
        }
    }

    fun goToStart() {
        stopAutoPlay()
        updatePly(uiState.plies, uiState.plies.firstOrNull()?.beforeFen ?: FenCodec.INITIAL_FEN, uiState.title, 0)
    }

    fun goPrev() {
        stopAutoPlay()
        val target = (uiState.currentPly - 1).coerceAtLeast(0)
        updatePly(uiState.plies, uiState.plies.firstOrNull()?.beforeFen ?: FenCodec.INITIAL_FEN, uiState.title, target, play = target > 0)
    }

    fun goNext() {
        val target = (uiState.currentPly + 1).coerceAtMost(uiState.maxPly)
        updatePly(uiState.plies, uiState.plies.firstOrNull()?.beforeFen ?: FenCodec.INITIAL_FEN, uiState.title, target, play = target > uiState.currentPly)
    }

    fun goToEnd() {
        stopAutoPlay()
        updatePly(uiState.plies, uiState.plies.firstOrNull()?.beforeFen ?: FenCodec.INITIAL_FEN, uiState.title, uiState.maxPly)
    }

    fun openCurrentGame() {
        onNavigate(Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Game(gameId)))
    }

    fun exportFen() {
        uiState = uiState.copy(exportContent = FenCodec.encode(uiState.boardState))
    }

    fun exportJson() {
        componentScope.launch {
            uiState = uiState.copy(exportContent = exportGame.asJson(gameId))
        }
    }

    fun exportText(labels: TextExportLabels) {
        componentScope.launch {
            uiState = uiState.copy(exportContent = exportGame.asText(gameId, labels.toAppDto()))
        }
    }

    fun dismissExport() {
        uiState = uiState.copy(exportContent = "")
    }

    private fun updatePly(
        plies: List<XiangqiPlyRecord>,
        initialFen: String,
        title: String,
        targetPly: Int,
        play: Boolean = false,
    ) {
        val fen = when {
            targetPly <= 0 || plies.isEmpty() -> initialFen
            else -> plies.firstOrNull { it.ply == targetPly }?.afterFen
                ?: plies.lastOrNull()?.afterFen
                ?: initialFen
        }
        val boardState = FenCodec.parse(fen)
        val status = GameArbiter.evaluateStatus(boardState)
        val maxPly = plies.maxOfOrNull { it.ply } ?: 0
        val safeTargetPly = when {
            plies.isEmpty() -> 0
            targetPly <= 0 -> 0
            targetPly > maxPly -> maxPly
            else -> targetPly
        }
        uiState = uiState.copy(
            title = title,
            boardState = boardState,
            currentPly = safeTargetPly,
            maxPly = maxPly,
            plies = plies,
            status = status,
        )
        if (play && safeTargetPly > 0) {
            playReplaySound(plies, safeTargetPly, status)
        }
    }

    private fun playReplaySound(plies: List<XiangqiPlyRecord>, targetPly: Int, status: GameStatus) {
        val record = plies.firstOrNull { it.ply == targetPly } ?: return
        componentScope.launch {
            val settings = settingsUseCase.current()
            audioFeedback.playForMove(record.beforeFen, record.afterFen, settings)
        }
    }

    private fun com.wanbaohe.xiangqi.application.dto.PlyRecord.toLegacy() = XiangqiPlyRecord(
        ply, moveUcci, moveCn, beforeFen, afterFen, aiReason, aiRawResponse, thinkDurationMs,
    )

    private fun TextExportLabels.toAppDto() = com.wanbaohe.xiangqi.application.dto.ExportLabels(
        header, titleLabel, initialFenLabel, resultLabel,
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            gameId: String,
            initialPly: Int,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): XiangqiAnalysisComponent
    }
}
