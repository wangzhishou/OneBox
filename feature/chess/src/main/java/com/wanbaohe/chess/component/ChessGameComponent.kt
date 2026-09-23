package com.wanbaohe.chess.component

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
import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.application.port.outbound.AudioSettings
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.ChessAiConfig
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.application.port.outbound.ChessAiStore
import com.wanbaohe.chess.application.usecase.AiOrchestrationUseCase
import com.wanbaohe.chess.application.usecase.AudioFeedbackUseCase
import com.wanbaohe.chess.application.usecase.ExportGameUseCase
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.application.usecase.ManageGameUseCase
import com.wanbaohe.chess.application.usecase.OnlinePlayUseCase
import com.wanbaohe.chess.application.usecase.PlayMoveUseCase
import com.wanbaohe.chess.application.usecase.SettingsUseCase
import com.wanbaohe.chess.data.ChessPlyRecord
import com.wanbaohe.chess.data.TextExportLabels
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.GameReducer
import com.wanbaohe.chess.domain.InteractionState
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.GameAction
import com.wanbaohe.chess.domain.model.ConnectionState
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.OnlineRoomConfig
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.domain.model.ChessMove
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChessGameUiState(
    val title: String = "",
    val boardState: BoardState = FenCodec.parse(FenCodec.INITIAL_FEN),
    val legalMoves: List<ChessMove> = emptyList(),
    val interaction: InteractionState = InteractionState(),
    val history: List<ChessPlyRecord> = emptyList(),
    val currentPly: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val mode: GameMode = GameMode.LOCAL_PVP,
    val blackPlayerType: PlayerType = PlayerType.HUMAN,
    val whitePlayerType: PlayerType = PlayerType.HUMAN,
    val isAiThinking: Boolean = false,
    val exportContent: String = "",
    val errorMessage: String = "",
    val blackAiServiceName: String = "",
    val blackAiModelName: String = "",
    val whiteAiServiceName: String = "",
    val whiteAiModelName: String = "",
    val onlineRoomId: String = "",
    val onlineMySide: Side = Side.WHITE,
    val onlineOpponentName: String = "",
    val onlineOpponentAvatarUrl: String = "",
    val onlineConnectionState: ConnectionState = ConnectionState.IDLE,
    val onlineDebugEvents: List<String> = emptyList(),
)

/**
 * 对局页 UI 状态管理器。内部委托给干净的 use case,对外保持简单 API。
 * 支持本地双人/人机/AI 对战/在线双人四种模式。
 */
class ChessGameComponent @AssistedInject constructor(
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
    private val chessAiStore: ChessAiStore,
    private val onlinePlay: OnlinePlayUseCase,
    aiEngineCatalogManager: AIEngineCatalogManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    var uiState by mutableStateOf(ChessGameUiState())
        private set

    var showResignConfirm by mutableStateOf(false)
    var showRenameDialog by mutableStateOf(false)

    val allAiEngines: StateFlow<List<AiEngine>> =
        aiEngineCatalogManager.observeAvailableEngines()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modelsByProvider: StateFlow<Map<String, List<AiModel>>> =
        aiEngineCatalogManager.observeModelsByProvider()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /**
     * 五子棋走棋 AI 配置(每槽位的来源)。
     *
     * ⚠️ 必须声明在 [init] 之前,并由 [collectAiConfig] 真正订阅:
     * `stateIn(WhileSubscribed)` 没有下游时会一直停在初值 [ChessAiConfig],
     * 只读 `.value` 拿到的是过期默认值而不是用户的选择。
     */
    val currentAiConfig: StateFlow<ChessAiConfig> = chessAiStore.observe()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), ChessAiConfig())

    private var aiRequestJob: Job? = null
    private var lastRequestedFen: String? = null
    private var audioSettings: AudioSettings = AudioSettings()
    private var onlineMovesObserved = false

    init {
        collectSettings()
        collectAiEngines()
        collectAiConfig()
        pauseOnStartup()
        observeGame()
    }

    /** 升变选择确认:用户从弹窗选定目标子后提交 */
    fun commitPromotion(move: ChessMove) {
        if (uiState.isAiThinking) return
        if (!isLocalOnlineTurn()) return
        val boardBefore = uiState.boardState
        componentScope.launch {
            when (playMove.commit(gameId, move)) {
                is PlayMoveUseCase.Result.Success -> {
                    playSound(boardBefore, move)
                    if (uiState.mode == GameMode.ONLINE_PVP) {
                        onlinePlay.sendMove(move)
                    }
                }
                is PlayMoveUseCase.Result.Rejected -> Unit
            }
            uiState = uiState.copy(interaction = InteractionState())
        }
    }

    /** 升变弹窗取消:回到未选中状态 */
    fun cancelPromotion() {
        uiState = uiState.copy(interaction = InteractionState())
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

    fun exportText(labels: TextExportLabels, resultText: String) {
        componentScope.launch {
            uiState = uiState.copy(
                exportContent = exportGame.asText(gameId, labels, resultText),
            )
        }
    }

    fun dismissExport() { uiState = uiState.copy(exportContent = "") }
    fun dismissError() { uiState = uiState.copy(errorMessage = "") }

    fun retryAiMove() {
        cancelAiRequest()
        componentScope.launch { requestAiMove() }
    }

    fun switchAiModelForSide(side: Side, engine: AiEngine, model: AiModel) {
        when (uiState.mode) {
            GameMode.LLM_VS_LLM -> {
                if (side == Side.BLACK) aiEngineManager.setDuelEngineA(engine.copy(model = model))
                else aiEngineManager.setDuelEngineB(engine.copy(model = model))
            }
            GameMode.HUMAN_VS_LLM -> aiEngineManager.switchFastModel(engine, model)
            GameMode.LOCAL_PVP -> Unit
            GameMode.ONLINE_PVP -> Unit
        }
        refreshAiDisplay()
    }

    fun switchAiSourceForSide(side: Side, source: ChessAiSource) {
        val slot = when (uiState.mode) {
            GameMode.LLM_VS_LLM -> if (side == Side.BLACK) EngineSlot.DUEL_A else EngineSlot.DUEL_B
            else -> EngineSlot.FAST
        }
        componentScope.launch {
            chessAiStore.update(chessAiStore.get().withSource(slot, source))
            refreshAiDisplay()
        }
    }

    fun currentSourceForSide(side: Side): ChessAiSource {
        val slot = when (uiState.mode) {
            GameMode.LLM_VS_LLM -> if (side == Side.BLACK) EngineSlot.DUEL_A else EngineSlot.DUEL_B
            else -> EngineSlot.FAST
        }
        return currentAiConfig.value.sourceFor(slot)
    }

    val currentAIEngine: StateFlow<AiEngine> = aiEngineManager.fastAIEngine

    fun currentEngineForSide(side: Side): AiEngine = when (uiState.mode) {
        GameMode.LLM_VS_LLM ->
            if (side == Side.BLACK) aiEngineManager.getDuelEngineA() else aiEngineManager.getDuelEngineB()
        else -> aiEngineManager.getFastAiEngine()
    }

    fun openAnalysis() {
        onNavigate(Screen.ChessRouter(Screen.ChessRouter.Type.Analysis(gameId, uiState.currentPly)))
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

    private fun observeOpponentMoves() {
        componentScope.launch {
            onlinePlay.opponentMoves.collect { (from, to) ->
                if (uiState.mode != GameMode.ONLINE_PVP) return@collect
                if (uiState.boardState.sideToMove == uiState.onlineMySide) return@collect
                // 升变载荷不带目标子:多个升变候选共享起止格,默认升后
                val candidates = uiState.legalMoves.filter { it.from == from && it.to == to }
                val move = candidates.firstOrNull {
                    it.promotion == com.wanbaohe.chess.domain.model.PieceType.QUEEN
                } ?: candidates.firstOrNull()
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
            isHost = metadata.mySide == Side.WHITE,
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

    private fun observeGame() {
        componentScope.launch {
            gameQuery.observeById(gameId).collect { detail ->
                detail ?: return@collect
                val boardState = FenCodec.parse(detail.currentFen)
                val legalMoves = GameArbiter.legalMoves(boardState)
                val blackInfo = resolveAiDisplay(detail.mode, detail.blackPlayerType, Side.BLACK)
                val whiteInfo = resolveAiDisplay(detail.mode, detail.whitePlayerType, Side.WHITE)

                uiState = uiState.copy(
                    title = detail.title,
                    boardState = boardState,
                    legalMoves = legalMoves,
                    history = detail.plies,
                    currentPly = detail.currentPly,
                    status = detail.status,
                    mode = detail.mode,
                    blackPlayerType = detail.blackPlayerType,
                    whitePlayerType = detail.whitePlayerType,
                    isAiThinking = uiState.isAiThinking && isCurrentSideAi(detail),
                    blackAiServiceName = blackInfo.first,
                    blackAiModelName = blackInfo.second,
                    whiteAiServiceName = whiteInfo.first,
                    whiteAiModelName = whiteInfo.second,
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

    private fun requestAiMove() {
        val currentFen = FenCodec.encode(uiState.boardState)
        if (lastRequestedFen == currentFen) return
        lastRequestedFen = currentFen
        uiState = uiState.copy(isAiThinking = true, errorMessage = "")

        aiRequestJob = componentScope.launch {
            val slot = when (uiState.mode) {
                GameMode.LLM_VS_LLM ->
                    if (uiState.boardState.sideToMove == Side.BLACK) EngineSlot.DUEL_A else EngineSlot.DUEL_B
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

    private fun shouldRequestAi(detail: GameDetail, boardState: BoardState): Boolean {
        val currentFen = FenCodec.encode(boardState)
        val playable = detail.status.isPlayable()
        return playable && isCurrentSideAi(detail) && lastRequestedFen != currentFen
    }

    private fun isCurrentSideAi(detail: GameDetail): Boolean = when (
        FenCodec.parse(detail.currentFen).sideToMove
    ) {
        Side.BLACK -> detail.blackPlayerType == PlayerType.LLM
        Side.WHITE -> detail.whitePlayerType == PlayerType.LLM
    }

    private fun cancelAiRequest() {
        aiRequestJob?.cancel()
        aiRequestJob = null
        lastRequestedFen = null
        componentScope.launch { aiOrchestration.clearTasks(gameId) }
    }

    private fun resolveAiDisplay(mode: GameMode, playerType: PlayerType, side: Side): Pair<String, String> {
        if (playerType != PlayerType.LLM) return "" to ""
        return when (val source = currentSourceForSide(side)) {
            ChessAiSource.WorkingModel -> {
                val engine = aiEngineManager.getFastAiEngine()
                (engine.title.ifBlank { engine.name }) to (engine.model.title.ifBlank { engine.model.name })
            }
            is ChessAiSource.RemoteEngine -> source.engineId to ""
        }
    }

    private fun refreshAiDisplay() {
        val black = resolveAiDisplay(uiState.mode, uiState.blackPlayerType, Side.BLACK)
        val white = resolveAiDisplay(uiState.mode, uiState.whitePlayerType, Side.WHITE)
        uiState = uiState.copy(
            blackAiServiceName = black.first, blackAiModelName = black.second,
            whiteAiServiceName = white.first, whiteAiModelName = white.second,
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

    /**
     * 订阅走棋 AI 来源配置。
     *
     * 除了让 [currentAiConfig] 保持最新([currentSourceForSide] 读它的 `.value`),
     * 还要在设置页 / 对局内选择器改动后刷新顶栏展示。
     */
    private fun collectAiConfig() {
        componentScope.launch {
            currentAiConfig.collect { refreshAiDisplay() }
        }
    }

    private fun pauseOnStartup() {
        componentScope.launch { manageGame.pause(gameId) }
    }

    private suspend fun playSound(boardBefore: BoardState, move: ChessMove) {
        // 落子音效用「走前局面」推导走后局面:提交成功后 DB observer 尚未回灌,uiState 还是旧局面
        val after = boardBefore.withPieceMoved(move)
        audioFeedback.playForMove(FenCodec.encode(boardBefore), FenCodec.encode(after), audioSettings)
    }

    private fun GameStatus.isPlayable(): Boolean = this == GameStatus.PLAYING || this == GameStatus.CHECK

    private fun isLocalOnlineTurn(): Boolean =
        uiState.mode != GameMode.ONLINE_PVP || uiState.boardState.sideToMove == uiState.onlineMySide

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            gameId: String,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): ChessGameComponent
    }
}
