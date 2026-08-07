package com.wanbaohe.xiangqi.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.xiangqi.application.dto.ExportLabels
import com.wanbaohe.xiangqi.application.dto.GameDetail
import com.wanbaohe.xiangqi.application.dto.PlyRecord
import com.wanbaohe.xiangqi.application.port.outbound.AudioSettings
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.usecase.AiOrchestrationUseCase
import com.wanbaohe.xiangqi.application.usecase.AudioFeedbackUseCase
import com.wanbaohe.xiangqi.application.usecase.ExportGameUseCase
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.application.usecase.ManageGameUseCase
import com.wanbaohe.xiangqi.application.usecase.OnlinePlayUseCase
import com.wanbaohe.xiangqi.application.usecase.PlayMoveUseCase
import com.wanbaohe.xiangqi.application.usecase.SettingsUseCase
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.data.XiangqiPlyRecord
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.GameReducer
import com.wanbaohe.xiangqi.domain.InteractionState
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.GameAction
import com.wanbaohe.xiangqi.domain.model.ConnectionState
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.OnlineRoomConfig
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class XiangqiGameUiState(
    val title: String = "",
    val boardState: BoardState = FenCodec.parse(FenCodec.INITIAL_FEN),
    val legalMoves: List<XiangqiMove> = emptyList(),
    val interaction: InteractionState = InteractionState(),
    val history: List<XiangqiPlyRecord> = emptyList(),
    val currentPly: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val mode: GameMode = GameMode.LOCAL_PVP,
    val redPlayerType: PlayerType = PlayerType.HUMAN,
    val blackPlayerType: PlayerType = PlayerType.HUMAN,
    val isAiThinking: Boolean = false,
    val exportContent: String = "",
    val errorMessage: String = "",
    val redAiServiceName: String = "",
    val redAiModelName: String = "",
    val blackAiServiceName: String = "",
    val blackAiModelName: String = "",
    val onlineRoomId: String = "",
    val onlineMySide: Side = Side.RED,
    val onlineOpponentName: String = "",
    val onlineOpponentAvatarUrl: String = "",
    val onlineConnectionState: ConnectionState = ConnectionState.IDLE,
    val onlineDebugEvents: List<String> = emptyList(),
)

/**
 * Backward-compatible UI state manager.
 * Internally delegates to clean use cases; externally preserves the old API.
 */
class XiangqiGameComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val gameId: String,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val gameQuery: GameQueryUseCase,
    private val playMove: PlayMoveUseCase,
    private val manageGame: ManageGameUseCase,
    private val aiOrchestration: AiOrchestrationUseCase,
    private val audioFeedback: AudioFeedbackUseCase,
    private val exportGame: ExportGameUseCase,
    private val settingsUseCase: SettingsUseCase,
    private val aiEngineManager: AIEngineManager,
    private val onlinePlay: OnlinePlayUseCase,
    aiEngineCatalogManager: AIEngineCatalogManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    var uiState by mutableStateOf(XiangqiGameUiState())
        private set

    var showResignConfirm by mutableStateOf(false)
    var showRenameDialog by mutableStateOf(false)

    val allAiEngines: StateFlow<List<AiEngine>> =
        aiEngineCatalogManager.observeAvailableEngines()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modelsByProvider: StateFlow<Map<String, List<AiModel>>> =
        aiEngineCatalogManager.observeModelsByProvider()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private var aiRequestJob: Job? = null
    private var lastRequestedFen: String? = null
    private var audioSettings: AudioSettings = AudioSettings()
    private var onlineMovesObserved = false

    init {
        collectSettings()
        collectAiEngines()
        pauseOnStartup()
        observeGame()
    }

    fun onCellTap(file: Int, rank: Int) {
        if (uiState.isAiThinking) return
        if (!uiState.status.isPlayable()) return
        if (!isLocalOnlineTurn()) return

        val boardBefore = uiState.boardState
        val next = GameReducer.reduce(
            boardState = boardBefore,
            legalMoves = uiState.legalMoves,
            previous = uiState.interaction,
            action = GameAction.TapCell(BoardPoint(file, rank)),
        )
        uiState = uiState.copy(interaction = next)

        next.pendingMove?.let { pending ->
            componentScope.launch {
                when (playMove.commit(gameId, pending)) {
                    is PlayMoveUseCase.Result.Success -> {
                        playSound(boardBefore, pending)
                        if (uiState.mode == GameMode.ONLINE_PVP) {
                            onlinePlay.sendMove(pending)
                        }
                        uiState = uiState.copy(interaction = InteractionState())
                    }
                    is PlayMoveUseCase.Result.Rejected -> {
                        uiState = uiState.copy(interaction = InteractionState())
                    }
                }
            }
        }
    }

    fun undo() {
        if (uiState.mode == GameMode.ONLINE_PVP) return
        cancelAiRequest()
        val steps = if (uiState.mode == GameMode.HUMAN_VS_LLM) 2 else 1
        componentScope.launch { manageGame.undo(gameId, steps) }
    }

    fun redo() {
        if (uiState.mode == GameMode.ONLINE_PVP) return
        cancelAiRequest()
        val steps = if (uiState.mode == GameMode.HUMAN_VS_LLM) 2 else 1
        componentScope.launch { manageGame.redo(gameId, steps) }
    }

    fun restart() {
        cancelAiRequest()
        componentScope.launch { manageGame.restart(gameId) }
    }

    fun start() {
        if (uiState.mode == GameMode.ONLINE_PVP) {
            onlinePlay.sendStart()
        }
        componentScope.launch { manageGame.start(gameId) }
    }

    fun exportFen() {
        componentScope.launch {
            uiState = uiState.copy(exportContent = exportGame.asFen(gameId))
        }
    }

    fun exportJson() {
        componentScope.launch {
            uiState = uiState.copy(exportContent = exportGame.asJson(gameId))
        }
    }

    fun exportText(labels: com.wanbaohe.xiangqi.data.TextExportLabels) {
        componentScope.launch {
            uiState = uiState.copy(exportContent = exportGame.asText(gameId, labels.toAppDto()))
        }
    }

    fun dismissExport() { uiState = uiState.copy(exportContent = "") }
    fun dismissError() { uiState = uiState.copy(errorMessage = "") }

    fun retryAiMove() {
        cancelAiRequest()
        componentScope.launch { requestAiMove() }
    }

    fun openAiModelSettings() {
        onNavigate(Screen.Settings(searchQuery = AppContext.getString(R.string.xiangqi_search_ai_model)))
    }

    fun switchAiModelForSide(side: Side, engine: AiEngine, model: AiModel) {
        when (uiState.mode) {
            GameMode.LLM_VS_LLM -> {
                if (side == Side.RED) aiEngineManager.setDuelEngineA(engine.copy(model = model))
                else aiEngineManager.setDuelEngineB(engine.copy(model = model))
            }
            GameMode.HUMAN_VS_LLM -> aiEngineManager.switchFastModel(engine, model)
            GameMode.LOCAL_PVP -> Unit
            GameMode.ONLINE_PVP -> Unit
        }
        refreshAiDisplay()
    }

    fun currentEngineForSide(side: Side): AiEngine = when (uiState.mode) {
        GameMode.LLM_VS_LLM ->
            if (side == Side.RED) aiEngineManager.getDuelEngineA() else aiEngineManager.getDuelEngineB()
        else -> aiEngineManager.getFastAiEngine()
    }

    fun openAnalysis() {
        onNavigate(Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Analysis(gameId, uiState.currentPly)))
    }

    fun resign() {
        if (uiState.mode == GameMode.ONLINE_PVP) {
            onlinePlay.sendResign()
        }
        val resigningSide = if (uiState.mode == GameMode.ONLINE_PVP) {
            uiState.onlineMySide
        } else {
            uiState.boardState.sideToMove
        }
        componentScope.launch { manageGame.resign(gameId, resigningSide) }
        showResignConfirm = false
    }

    fun renameGame(newTitle: String) {
        componentScope.launch { manageGame.rename(gameId, newTitle) }
        showRenameDialog = false
    }

    /* ─────────── private ─────────── */

    private fun observeGame() {
        componentScope.launch {
            gameQuery.observeById(gameId).collect { detail ->
                detail ?: return@collect
                val boardState = FenCodec.parse(detail.currentFen)
                val legalMoves = GameArbiter.legalMoves(boardState)
                val redInfo = resolveAiDisplay(detail.mode, detail.redPlayerType, Side.RED)
                val blackInfo = resolveAiDisplay(detail.mode, detail.blackPlayerType, Side.BLACK)

                uiState = uiState.copy(
                    title = detail.title,
                    boardState = boardState,
                    legalMoves = legalMoves,
                    history = detail.plies.map { it.toLegacy() },
                    currentPly = detail.currentPly,
                    status = detail.status,
                    mode = detail.mode,
                    redPlayerType = detail.redPlayerType,
                    blackPlayerType = detail.blackPlayerType,
                    isAiThinking = uiState.isAiThinking && isCurrentSideAi(detail),
                    redAiServiceName = redInfo.first,
                    redAiModelName = redInfo.second,
                    blackAiServiceName = blackInfo.first,
                    blackAiModelName = blackInfo.second,
                    onlineRoomId = detail.onlineMetadata.roomId,
                    onlineMySide = detail.onlineMetadata.mySide,
                    onlineOpponentName = detail.onlineMetadata.opponentName,
                    onlineOpponentAvatarUrl = detail.onlineMetadata.opponentAvatarUrl,
                    onlineConnectionState = onlinePlay.connectionState.value,
                    onlineDebugEvents = onlinePlay.debugEvents.value,
                )

                if (detail.mode == GameMode.ONLINE_PVP && !onlineMovesObserved) {
                    connectOnlineIfNeeded(detail)
                    onlineMovesObserved = true
                    observeOpponentMoves()
                    observeOpponentStarted()
                    observeOpponentResigned()
                    observeOnlineConnection()
                    observeOnlineDebugEvents()
                }

                if (shouldRequestAi(detail, boardState)) {
                    requestAiMove()
                } else if (!isCurrentSideAi(detail)) {
                    cancelAiRequest()
                }
            }
        }
    }

    private fun observeOpponentMoves() {
        componentScope.launch {
            onlinePlay.opponentMoves.collect { (from, to) ->
                if (uiState.mode != GameMode.ONLINE_PVP) return@collect
                if (uiState.boardState.sideToMove == uiState.onlineMySide) return@collect
                val move = uiState.legalMoves.find { it.from == from && it.to == to }
                if (move != null) {
                    playMove.commit(gameId, move)
                }
            }
        }
    }

    private fun observeOnlineConnection() {
        componentScope.launch {
            onlinePlay.connectionState.collect { state ->
                uiState = uiState.copy(onlineConnectionState = state)
            }
        }
    }

    private fun observeOnlineDebugEvents() {
        componentScope.launch {
            onlinePlay.debugEvents.collect { events ->
                uiState = uiState.copy(onlineDebugEvents = events)
            }
        }
    }

    private fun connectOnlineIfNeeded(detail: GameDetail) {
        val metadata = detail.onlineMetadata
        if (metadata.roomId.isBlank()) return
        if (onlinePlay.connectionState.value != ConnectionState.IDLE) return
        onlinePlay.connect(
            roomId = metadata.roomId,
            side = metadata.mySide,
            isHost = metadata.mySide == Side.RED,
            config = OnlineRoomConfig.fromFen(metadata.initialFen.ifBlank { detail.initialFen }),
        )
    }

    private fun observeOpponentStarted() {
        componentScope.launch {
            onlinePlay.opponentStarted.collect {
                manageGame.start(gameId)
            }
        }
    }

    private fun observeOpponentResigned() {
        componentScope.launch {
            onlinePlay.opponentResigned.collect {
                val opponentSide = uiState.onlineMySide.opposite()
                manageGame.resign(gameId, opponentSide)
            }
        }
    }

    private fun requestAiMove() {
        val currentFen = FenCodec.encode(uiState.boardState)
        if (lastRequestedFen == currentFen) return
        lastRequestedFen = currentFen
        uiState = uiState.copy(isAiThinking = true, errorMessage = "")

        aiRequestJob = componentScope.launch {
            val slot = when (uiState.mode) {
                GameMode.LLM_VS_LLM ->
                    if (uiState.boardState.sideToMove == Side.RED) EngineSlot.DUEL_A else EngineSlot.DUEL_B
                else -> EngineSlot.FAST
            }
            when (val outcome = aiOrchestration.requestMove(gameId, slot)) {
                is AiOrchestrationUseCase.Outcome.Committed -> {
                    val lastPly = outcome.detail.plies.lastOrNull()
                    if (lastPly != null) {
                        audioFeedback.playForMove(lastPly.beforeFen, lastPly.afterFen, audioSettings)
                    }
                    uiState = uiState.copy(isAiThinking = false)
                }
                is AiOrchestrationUseCase.Outcome.Stale -> {
                    uiState = uiState.copy(isAiThinking = false)
                }
                is AiOrchestrationUseCase.Outcome.Failed -> {
                    uiState = uiState.copy(isAiThinking = false, errorMessage = outcome.reason)
                }
            }
            if (lastRequestedFen == currentFen) lastRequestedFen = null
        }
    }

    private fun shouldRequestAi(detail: GameDetail, boardState: com.wanbaohe.xiangqi.domain.model.BoardState): Boolean {
        val currentFen = FenCodec.encode(boardState)
        val playable = detail.status.isPlayable()
        return playable && isCurrentSideAi(detail) && lastRequestedFen != currentFen
    }

    private fun isCurrentSideAi(detail: GameDetail): Boolean = when (
        FenCodec.parse(detail.currentFen).sideToMove
    ) {
        Side.RED -> detail.redPlayerType == PlayerType.LLM
        Side.BLACK -> detail.blackPlayerType == PlayerType.LLM
    }

    private fun cancelAiRequest() {
        aiRequestJob?.cancel()
        aiRequestJob = null
        lastRequestedFen = null
        componentScope.launch { aiOrchestration.clearTasks(gameId) }
    }

    private fun resolveAiDisplay(mode: GameMode, playerType: PlayerType, side: Side): Pair<String, String> {
        if (playerType != PlayerType.LLM) return "" to ""
        val engine = when (mode) {
            GameMode.LLM_VS_LLM ->
                if (side == Side.RED) aiEngineManager.getDuelEngineA() else aiEngineManager.getDuelEngineB()
            else -> aiEngineManager.getFastAiEngine()
        }
        return (engine.title.ifBlank { engine.name }) to (engine.model.title.ifBlank { engine.model.name })
    }

    private fun refreshAiDisplay() {
        val red = resolveAiDisplay(uiState.mode, uiState.redPlayerType, Side.RED)
        val black = resolveAiDisplay(uiState.mode, uiState.blackPlayerType, Side.BLACK)
        uiState = uiState.copy(
            redAiServiceName = red.first, redAiModelName = red.second,
            blackAiServiceName = black.first, blackAiModelName = black.second,
        )
    }

    private fun collectSettings() {
        componentScope.launch {
            settingsUseCase.observe().collect { audioSettings = it }
        }
    }

    private fun collectAiEngines() {
        listOf(
            aiEngineManager.fastAIEngine,
            aiEngineManager.duelEngineA,
            aiEngineManager.duelEngineB,
        ).forEach { flow ->
            componentScope.launch { flow.collect { refreshAiDisplay() } }
        }
    }

    private fun pauseOnStartup() {
        componentScope.launch { manageGame.pause(gameId) }
    }

    private suspend fun playSound(boardBefore: com.wanbaohe.xiangqi.domain.model.BoardState, move: XiangqiMove) {
        val after = boardBefore.withPieceMoved(move)
        audioFeedback.playForMove(
            FenCodec.encode(boardBefore),
            FenCodec.encode(after),
            audioSettings,
        )
    }

    private fun GameStatus.isPlayable(): Boolean = this == GameStatus.PLAYING || this == GameStatus.CHECK

    private fun isLocalOnlineTurn(): Boolean =
        uiState.mode != GameMode.ONLINE_PVP || uiState.boardState.sideToMove == uiState.onlineMySide

    private fun PlyRecord.toLegacy() = XiangqiPlyRecord(
        ply, moveUcci, moveCn, beforeFen, afterFen, aiReason, aiRawResponse, thinkDurationMs,
    )

    private fun com.wanbaohe.xiangqi.data.TextExportLabels.toAppDto() = ExportLabels(
        header, titleLabel, initialFenLabel, resultLabel,
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            gameId: String,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): XiangqiGameComponent
    }
}
