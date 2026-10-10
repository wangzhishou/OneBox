package com.wanbaohe.gomoku.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ModelProvider.AppJson
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.dto.GamePreparation
import com.wanbaohe.gomoku.application.dto.engineSlotFor
import com.wanbaohe.gomoku.application.dto.prepareFrom
import com.wanbaohe.gomoku.application.port.outbound.AudioSettings
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiConfig
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiStore
import com.wanbaohe.gomoku.application.usecase.AiOrchestrationUseCase
import com.wanbaohe.gomoku.application.usecase.AudioFeedbackUseCase
import com.wanbaohe.gomoku.application.usecase.ExportGameUseCase
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.application.usecase.ManageGameUseCase
import com.wanbaohe.gomoku.application.usecase.OnlineGameEvent
import com.wanbaohe.gomoku.application.usecase.OnlinePlayUseCase
import com.wanbaohe.gomoku.application.usecase.PlayMoveUseCase
import com.wanbaohe.gomoku.application.usecase.SettingsUseCase
import com.wanbaohe.gomoku.data.GomokuPlyRecord
import com.wanbaohe.gomoku.data.TextExportLabels
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.BoardSetupDraft
import com.wanbaohe.gomoku.domain.HumanAiHistory
import com.wanbaohe.gomoku.domain.SetupPositionValidator
import com.wanbaohe.gomoku.domain.GameReducer
import com.wanbaohe.gomoku.domain.InteractionState
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.GameAction
import com.wanbaohe.gomoku.domain.model.ConnectionState
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.OnlineRoomConfig
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.domain.model.GomokuMove
import com.wanbaohe.gomoku.presentation.displayNames
import com.wanbaohe.gomoku.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

data class GomokuGameUiState(
    val title: String = "",
    val boardState: BoardState = FenCodec.parse(FenCodec.INITIAL_FEN),
    val legalMoves: List<GomokuMove> = emptyList(),
    val interaction: InteractionState = InteractionState(),
    val history: List<GomokuPlyRecord> = emptyList(),
    val currentPly: Int = 0,
    val status: GameStatus = GameStatus.NOT_STARTED,
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
    val onlineMySide: Side = Side.BLACK,
    val onlineOpponentName: String = "",
    val onlineOpponentAvatarUrl: String = "",
    val onlineConnectionState: ConnectionState = ConnectionState.IDLE,
    val onlineDebugEvents: List<String> = emptyList(),
    val winnerSide: String = "",
    val initialFen: String = FenCodec.INITIAL_FEN,
    val startedAt: Long = 0L,
    val isLoaded: Boolean = false,
    val isUpdating: Boolean = false,
    val blackAiConfig: GameAiPlayerConfig? = null,
    val whiteAiConfig: GameAiPlayerConfig? = null,
    val origin: GameOrigin? = null,
)

/**
 * 对局页 UI 状态管理器。内部委托给干净的 use case,对外保持简单 API。
 * 支持本地双人/人机/AI 对战/在线双人四种模式。
 */
class GomokuGameComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val gameId: String,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted private val onPrepareGame: (GamePreparation) -> Unit,
    @Assisted private val startImmediately: Boolean,
    private val gameQuery: GameQueryUseCase,
    private val playMove: PlayMoveUseCase,
    private val manageGame: ManageGameUseCase,
    private val aiOrchestration: AiOrchestrationUseCase,
    private val audioFeedback: AudioFeedbackUseCase,
    private val exportGame: ExportGameUseCase,
    private val settingsUseCase: SettingsUseCase,
    private val aiEngineManager: AIEngineManager,
    private val gomokuAiStore: GomokuAiStore,
    private val onlinePlay: OnlinePlayUseCase,
    aiEngineCatalogManager: AIEngineCatalogManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    var uiState by mutableStateOf(GomokuGameUiState())
        private set

    var showResignConfirm by mutableStateOf(false)
    var showRestartConfirm by mutableStateOf(false)
    var showStandardGameConfirm by mutableStateOf(false)
    var showRenameDialog by mutableStateOf(false)
    var showGameOverOverlay by mutableStateOf(true)
    var setupDraft by mutableStateOf<BoardSetupDraft?>(null)
        private set

    private var isVisible by mutableStateOf(lifecycle.state >= Lifecycle.State.STARTED)
    private var isNavigatingAway by mutableStateOf(false)
    private var suppressAiRequests = true
    private var immediateStartPending = startImmediately
    private var setupPreviousStatus = GameStatus.NOT_STARTED
    private var mayResumeAfterSetup = false
    private var editingStartedAt = 0L
    private var setupPauseJob: Job? = null
    private var setupSourceDetail: GameDetail? = null
    private var operationJob: Job? = null

    val canSetup: Boolean
        get() = isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isUpdating &&
            !uiState.isAiThinking && uiState.mode != GameMode.ONLINE_PVP && setupDraft == null &&
            (uiState.status.isTerminal() || uiState.status == GameStatus.NOT_STARTED ||
                (uiState.mode != GameMode.LLM_VS_LLM && isHumanTurn()))

    fun beginSetup() {
        if (!canSetup) {
            ActionUtils.showToast(R.string.gomoku_setup_wait_for_turn)
            return
        }
        setupPreviousStatus = uiState.status
        mayResumeAfterSetup = uiState.status == GameStatus.PLAYING
        editingStartedAt = System.currentTimeMillis()
        suppressAiRequests = true
        cancelAiRequest()
        setupSourceDetail = null
        setupDraft = BoardSetupDraft(uiState.boardState)
        setupPauseJob = componentScope.launch {
            awaitPendingMoves()
            manageGame.pause(gameId)
            setupSourceDetail = gameQuery.getById(gameId)
        }
    }

    internal class GomokuOnlineGameSync(
        private val query: GameQueryUseCase,
        private val playMove: PlayMoveUseCase,
        private val manageGame: ManageGameUseCase,
    ) {
        sealed interface Result {
            data class Applied(val detail: GameDetail) : Result
            data class Rejected(val reason: String) : Result
        }

        private val mutationMutex = Mutex()

        suspend fun <T> mutate(block: suspend () -> T): T =
            mutationMutex.withLock { withContext(NonCancellable) { block() } }

        suspend fun accept(gameId: String, event: OnlineGameEvent): Result = mutate {
            val detail = query.getById(gameId)
                ?: return@mutate Result.Rejected("Received an event for a deleted game")
            if (detail.mode != GameMode.ONLINE_PVP) {
                return@mutate Result.Rejected("Received an event for a non-online game")
            }
            val identity = try {
                AppJson.parseToJsonElement(
                    detail.whitePlayerConfigJson.ifBlank { detail.blackPlayerConfigJson },
                ) as? JsonObject
            } catch (_: SerializationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
            fun identityValue(key: String): String? =
                (identity?.get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content
            if (event.roomId.isBlank() || identityValue("roomId") != event.roomId ||
                identityValue("mySide") != event.mySide.name
            ) {
                return@mutate Result.Rejected("Received an event for a different room or side")
            }
            val localPlayer = if (event.mySide == Side.BLACK) detail.blackPlayerType else detail.whitePlayerType
            val remotePlayer = if (event.mySide == Side.BLACK) detail.whitePlayerType else detail.blackPlayerType
            if (localPlayer != PlayerType.HUMAN || remotePlayer != PlayerType.REMOTE) {
                return@mutate Result.Rejected("Received an event for unavailable online players")
            }

            when (event) {
                is OnlineGameEvent.Started -> {
                    if (event.initialFen.isNotBlank()) {
                        val matches = try {
                            FenCodec.parse(event.initialFen) == FenCodec.parse(detail.initialFen)
                        } catch (_: IllegalArgumentException) {
                            false
                        }
                        if (!matches) return@mutate Result.Rejected("Received Start for a different or invalid position")
                    }
                    if (detail.status == GameStatus.PLAYING || detail.status.isTerminal()) {
                        return@mutate Result.Applied(detail)
                    }
                    if (detail.status != GameStatus.NOT_STARTED && detail.status != GameStatus.PAUSED) {
                        return@mutate Result.Rejected("Received Start while the game is ${detail.status}")
                    }
                    val started = manageGame.start(gameId)
                    if (started?.status == GameStatus.PLAYING) Result.Applied(started)
                    else Result.Rejected("Received Start for an unavailable or terminal position")
                }
                is OnlineGameEvent.Move -> {
                    if (detail.status != GameStatus.PLAYING && detail.status != GameStatus.PAUSED) {
                        return@mutate Result.Rejected("Received a move while the game is ${detail.status}")
                    }
                    val board = try {
                        FenCodec.parse(detail.currentFen)
                    } catch (_: IllegalArgumentException) {
                        return@mutate Result.Rejected("Received a move for an invalid stored position")
                    }
                    if (board.sideToMove != event.mySide.opposite()) {
                        return@mutate Result.Rejected("Received an opponent move during the local turn")
                    }
                    val move = GameArbiter.legalMoves(board).firstOrNull { it.to == event.point }
                        ?: return@mutate Result.Rejected("Received an illegal move: ${event.point}")
                    // Earlier versions locally paused live online rooms. A valid peer action
                    // resumes that record; it must not wait in memory for a local Continue.
                    val expected = if (detail.status == GameStatus.PAUSED) manageGame.start(gameId) else detail
                    if (expected == null || expected.status != GameStatus.PLAYING || expected.currentFen != detail.currentFen ||
                        expected.currentPly != detail.currentPly || expected.mode != detail.mode ||
                        expected.blackPlayerConfigJson != detail.blackPlayerConfigJson ||
                        expected.whitePlayerConfigJson != detail.whitePlayerConfigJson
                    ) return@mutate Result.Rejected("Online position changed before the received move could be saved")
                    when (val result = playMove.commit(gameId, move, expectedGame = expected)) {
                        is PlayMoveUseCase.Result.Success -> Result.Applied(result.detail)
                        is PlayMoveUseCase.Result.Rejected -> Result.Rejected("Opponent move rejected: ${result.reason}")
                    }
                }
                is OnlineGameEvent.Resigned -> {
                    if (detail.status.isTerminal()) return@mutate Result.Applied(detail)
                    if (detail.status != GameStatus.PLAYING && detail.status != GameStatus.PAUSED) {
                        return@mutate Result.Rejected("Received resignation while the game is ${detail.status}")
                    }
                    if (detail.status == GameStatus.PAUSED && manageGame.start(gameId)?.status != GameStatus.PLAYING) {
                        return@mutate Result.Rejected("Opponent resignation could not resume its live room")
                    }
                    val resigned = manageGame.resign(gameId, event.mySide.opposite())
                    if (resigned?.status == GameStatus.RESIGNED) Result.Applied(resigned)
                    else Result.Rejected("Opponent resignation could not be saved")
                }
            }
        }

        private fun GameStatus.isTerminal(): Boolean = this in setOf(
            GameStatus.BLACK_WINS, GameStatus.WHITE_WINS, GameStatus.DRAW, GameStatus.RESIGNED,
        )
    }

    fun updateSetup(draft: BoardSetupDraft) {
        if (!uiState.isUpdating) setupDraft = draft
    }

    fun cancelSetup() {
        if (setupDraft == null || uiState.isUpdating) return
        uiState = uiState.copy(isUpdating = true)
        operationJob = componentScope.launch {
            setupPauseJob?.join()
            if (mayResumeAfterSetup && setupPreviousStatus == GameStatus.PLAYING && isVisible && !isNavigatingAway) {
                manageGame.resumeAfterEditing(gameId, editingStartedAt)?.let(::applyPosition)
            }
            setupDraft = null
            setupSourceDetail = null
            mayResumeAfterSetup = false
            suppressAiRequests = !isVisible || isNavigatingAway
            uiState = uiState.copy(isUpdating = false)
            requestAiMove()
        }
    }

    fun finishSetup() {
        val draft = setupDraft ?: return
        if (!draft.hasChanges) {
            cancelSetup()
            return
        }
        if (SetupPositionValidator.validate(draft.startPosition()) != null) {
            ActionUtils.showToast(R.string.gomoku_invalid_fen)
            return
        }
        if (uiState.isUpdating) return
        uiState = uiState.copy(isUpdating = true)
        operationJob = componentScope.launch {
            setupPauseJob?.join()
            val source = setupSourceDetail
            if (source == null || gameQuery.getById(gameId) == null) {
                uiState = uiState.copy(isUpdating = false)
                ActionUtils.showToast(R.string.gomoku_game_missing)
                return@launch
            }
            if (source.status == GameStatus.NOT_STARTED && source.startedAt == 0L && source.currentPly == 0) {
                manageGame.updateInitialPosition(gameId, draft.startPosition())?.let(::applyPosition)
                suppressAiRequests = true
            } else {
                onPrepareGame(source.prepareFrom(draft.startPosition()))
            }
            setupDraft = null
            setupSourceDetail = null
            uiState = uiState.copy(isUpdating = false)
        }
    }

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
     * `stateIn(WhileSubscribed)` 没有下游时会一直停在初值 [GomokuAiConfig],
     * 只读 `.value` 拿到的是过期默认值而不是用户的选择。
     */
    val currentAiConfig: StateFlow<GomokuAiConfig> = gomokuAiStore.observe()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), GomokuAiConfig())

    private var aiRequestJob: Job? = null
    private var aiCleanupJob: Job? = null
    private var moveCommitJob: Job? = null
    private var lastRequestedFen: String? = null
    private var audioSettings: AudioSettings = AudioSettings()
    private var onlineMovesObserved = false
    private val onlineSync = GomokuOnlineGameSync(gameQuery, playMove, manageGame)
    private var onlineCommitJob: Job? = null
    private var onlineSyncError: String? = null

    init {
        lifecycle.doOnStart {
            isVisible = true
            isNavigatingAway = false
            maybeStartImmediately()
            requestAiMove()
        }
        lifecycle.doOnStop {
            isVisible = false
            immediateStartPending = false
            mayResumeAfterSetup = false
            suppressAiRequests = true
            cancelAiRequest()
            componentScope.launch {
                operationJob?.join()
                setupPauseJob?.join()
                awaitPendingMoves()
                pauseOfflineGame()?.let(::applyPosition)
            }
        }
        collectSettings()
        collectAiEngines()
        collectAiConfig()
        observeGame()
    }

    fun onCellTap(file: Int, rank: Int) {
        if (!isVisible || isNavigatingAway || !uiState.isLoaded || uiState.isAiThinking ||
            uiState.isUpdating || setupDraft != null || !isHumanTurn() ||
            onlineCommitJob?.isActive == true
        ) return
        if (!uiState.status.isPlayable()) return
        if (!isLocalOnlineTurn()) return

        val boardBefore = uiState.boardState
        val plyBefore = uiState.currentPly
        val next = GameReducer.reduce(
            boardState = boardBefore,
            legalMoves = uiState.legalMoves,
            previous = uiState.interaction,
            action = GameAction.TapCell(BoardPoint(file, rank)),
        )
        uiState = uiState.copy(interaction = next)

        next.pendingMove?.let { pending ->
            uiState = uiState.copy(isUpdating = true)
            val handling = componentScope.launch(start = CoroutineStart.LAZY) {
                val handlingJob = currentCoroutineContext()[Job]
                var committed = false
                try {
                    serializeOnlineMutation {
                        if (uiState.mode == GameMode.ONLINE_PVP && !ownsOnlineSession()) {
                            reportOnlineSyncError("Local move no longer owns the active room")
                            return@serializeOnlineMutation
                        }
                        val expected = if (uiState.mode == GameMode.ONLINE_PVP) gameQuery.getById(gameId) else null
                        if (uiState.mode == GameMode.ONLINE_PVP && (
                                expected == null || expected.mode != GameMode.ONLINE_PVP ||
                                    expected.onlineMetadata.roomId != uiState.onlineRoomId ||
                                    expected.onlineMetadata.mySide != uiState.onlineMySide ||
                                    expected.currentPly != plyBefore ||
                                    FenCodec.parse(expected.currentFen) != boardBefore ||
                                    expected.status != GameStatus.PLAYING
                                )
                        ) {
                            reportOnlineSyncError("Local position changed before its move was committed")
                            return@serializeOnlineMutation
                        }
                        when (val result = playMove.commit(gameId, pending, expectedGame = expected)) {
                            is PlayMoveUseCase.Result.Success -> {
                                applyPosition(result.detail)
                                if (result.detail.mode == GameMode.ONLINE_PVP) {
                                    if (!onlinePlay.sendMove(
                                            pending, result.detail.onlineMetadata.roomId, result.detail.onlineMetadata.mySide,
                                        )
                                    ) reportOnlineSyncError("Room changed before the local move could be sent")
                                }
                                committed = true
                            }
                            is PlayMoveUseCase.Result.Rejected -> {
                                if (uiState.mode == GameMode.ONLINE_PVP) {
                                    reportOnlineSyncError("Local move rejected: ${result.reason}")
                                }
                            }
                        }
                    }
                    if (committed && isVisible && !isNavigatingAway) {
                        if (uiState.mode == GameMode.ONLINE_PVP) {
                            componentScope.launch {
                                if (isVisible && !isNavigatingAway) playSound(boardBefore, pending)
                            }
                        } else playSound(boardBefore, pending)
                    }
                } finally {
                    if (moveCommitJob === handlingJob) moveCommitJob = null
                    uiState = uiState.copy(
                        interaction = InteractionState(),
                        isUpdating = onlineCommitJob?.isActive == true,
                    )
                }
                if (committed) requestAiMove()
            }
            moveCommitJob = handling
            handling.start()
        }
    }

    fun undo() {
        if (!canUndo) return
        val targetPly = uiState.currentPly - undoSteps()
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true, interaction = InteractionState())
        operationJob = componentScope.launch {
            awaitPendingMoves()
            val latest = gameQuery.getById(gameId)
            finishHistoryChange(latest?.let { manageGame.undo(gameId, (it.currentPly - targetPly).coerceAtLeast(0)) })
        }
    }

    fun redo() {
        if (!canRedo) return
        val targetPly = uiState.currentPly + redoSteps()
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true, interaction = InteractionState())
        operationJob = componentScope.launch {
            awaitPendingMoves()
            val latest = gameQuery.getById(gameId)
            finishHistoryChange(latest?.let { manageGame.redo(gameId, (targetPly - it.currentPly).coerceAtLeast(0)) })
        }
    }

    val canUndo: Boolean get() = isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isUpdating &&
        uiState.mode != GameMode.ONLINE_PVP && setupDraft == null && undoSteps() > 0
    val canRedo: Boolean get() = isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isUpdating &&
        uiState.mode != GameMode.ONLINE_PVP && setupDraft == null && redoSteps() > 0

    private fun humanSide(): Side = if (uiState.blackPlayerType == PlayerType.HUMAN) Side.BLACK else Side.WHITE
    private fun isHumanTurn(): Boolean = when (uiState.boardState.sideToMove) {
        Side.BLACK -> uiState.blackPlayerType == PlayerType.HUMAN
        Side.WHITE -> uiState.whitePlayerType == PlayerType.HUMAN
    }
    private fun isAiTurn(): Boolean = when (uiState.boardState.sideToMove) {
        Side.BLACK -> uiState.blackPlayerType == PlayerType.LLM
        Side.WHITE -> uiState.whitePlayerType == PlayerType.LLM
    }
    private fun undoSteps(): Int = if (uiState.mode == GameMode.HUMAN_VS_LLM)
        HumanAiHistory.undoSteps(uiState.history.map { it.moverSide }, uiState.currentPly, humanSide())
    else if (uiState.currentPly > 0) 1 else 0
    private fun redoSteps(): Int = if (uiState.mode == GameMode.HUMAN_VS_LLM)
        HumanAiHistory.redoSteps(uiState.history.map { it.moverSide }, uiState.currentPly, humanSide())
    else if (uiState.currentPly < uiState.history.size) 1 else 0

    private fun finishHistoryChange(detail: GameDetail?) {
        if (detail != null) applyPosition(detail) else ActionUtils.showToast(R.string.gomoku_game_missing)
        suppressAiRequests = !isVisible || isNavigatingAway
        uiState = uiState.copy(isUpdating = false, errorMessage = "")
        requestAiMove()
    }

    private fun applyPosition(detail: GameDetail) {
        val board = FenCodec.parse(detail.currentFen)
        uiState = uiState.copy(
            boardState = board, legalMoves = GameArbiter.legalMoves(board),
            history = detail.plies, currentPly = detail.currentPly, status = detail.status,
            winnerSide = detail.winnerSide, initialFen = detail.initialFen,
            startedAt = detail.startedAt, blackAiConfig = detail.blackAiConfig,
            whiteAiConfig = detail.whiteAiConfig, origin = detail.origin,
        )
    }

    fun restart() {
        if (!isVisible || isNavigatingAway || !uiState.isLoaded || uiState.isUpdating ||
            setupDraft != null || onlineCommitJob?.isActive == true
        ) return
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true)
        operationJob = componentScope.launch {
            try {
                awaitPendingMoves()
                pauseOfflineGame()
                val detail = gameQuery.getById(gameId) ?: return@launch
                if (detail.mode == GameMode.ONLINE_PVP) {
                    manageGame.restart(gameId)?.let {
                        if (it.id != gameId) onNavigate(Screen.GomokuRouter(Screen.GomokuRouter.Type.Game(it.id)))
                    }
                } else {
                    onPrepareGame(detail.prepareFrom(FenCodec.parse(detail.initialFen), 0)
                        .copy(title = detail.title, origin = detail.origin))
                }
            } finally {
                uiState = uiState.copy(isUpdating = false)
            }
        }
        showRestartConfirm = false
    }

    fun start() {
        if (!isVisible || isNavigatingAway || !uiState.isLoaded || uiState.isUpdating ||
            setupDraft != null || onlineCommitJob?.isActive == true
        ) return
        if (SetupPositionValidator.validate(uiState.boardState) != null) {
            ActionUtils.showToast(R.string.gomoku_invalid_fen)
            return
        }
        uiState = uiState.copy(isUpdating = true)
        withAiAccess(onFailure = { uiState = uiState.copy(isUpdating = false) }) {
            operationJob = componentScope.launch {
                try {
                    awaitPendingMoves()
                    if (!isVisible || isNavigatingAway) return@launch
                    serializeOnlineMutation {
                        if (uiState.mode == GameMode.ONLINE_PVP && !ownsOnlineSession()) {
                            reportOnlineSyncError("Local start no longer owns the active room")
                            return@serializeOnlineMutation
                        }
                        val detail = gameQuery.getById(gameId) ?: return@serializeOnlineMutation
                        if (detail.status == GameStatus.NOT_STARTED || detail.status == GameStatus.PAUSED) {
                            manageGame.start(gameId)?.let { started ->
                                applyPosition(started)
                                if (started.mode == GameMode.ONLINE_PVP && started.status == GameStatus.PLAYING) {
                                    if (!onlinePlay.sendStart(started.onlineMetadata.roomId, started.onlineMetadata.mySide)) {
                                        reportOnlineSyncError("Room changed before the local start could be sent")
                                    }
                                }
                            }
                        }
                    }
                    suppressAiRequests = !isVisible || isNavigatingAway
                } finally {
                    uiState = uiState.copy(isUpdating = onlineCommitJob?.isActive == true)
                }
                requestAiMove()
            }
        }
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
        if (!isVisible || isNavigatingAway || !uiState.status.isPlayable() ||
            !isAiTurn() || setupDraft != null || uiState.isUpdating
        ) return
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true)
        withAiAccess(onFailure = { uiState = uiState.copy(isUpdating = false) }) {
            operationJob = componentScope.launch {
                awaitPendingMoves()
                suppressAiRequests = !isVisible || isNavigatingAway
                uiState = uiState.copy(isUpdating = false)
                requestAiMove()
            }
        }
    }

    fun switchAiModelForSide(side: Side, engine: AiEngine, model: AiModel) {
        val config = GameAiPlayerConfig.capture(GomokuAiSource.WorkingModel, engine.copy(model = model))
        if (!config.isSupported) return
        ActionUtils.ensureLoginAndCheckPoints(
            source = "gomoku_switch_model", point = config.source.startPoints,
            onSuccess = {
                if (isVisible && !isNavigatingAway && !uiState.isUpdating && setupDraft == null) {
                    when (uiState.mode.engineSlotFor(side)) {
                        EngineSlot.FAST -> aiEngineManager.switchFastModel(engine, model)
                        EngineSlot.DUEL_A -> aiEngineManager.setDuelEngineA(engine.copy(model = model))
                        EngineSlot.DUEL_B -> aiEngineManager.setDuelEngineB(engine.copy(model = model))
                    }
                    saveAiConfig(side, config)
                }
            },
        )
    }

    fun switchAiSourceForSide(side: Side, source: GomokuAiSource) {
        if (source == currentSourceForSide(side)) return
        val config = GameAiPlayerConfig.capture(source, currentEngineForSide(side))
        if (source.requiresLogin) {
            ActionUtils.ensureLoginAndCheckPoints(
                source = "gomoku_switch_ai", point = source.startPoints,
                onSuccess = { saveAiConfig(side, config) },
            )
        } else saveAiConfig(side, config)
    }

    private fun saveAiConfig(side: Side, config: GameAiPlayerConfig) {
        val playerType = if (side == Side.BLACK) uiState.blackPlayerType else uiState.whitePlayerType
        if (!isVisible || isNavigatingAway || !uiState.isLoaded || uiState.isUpdating ||
            setupDraft != null || !config.isSupported || playerType != PlayerType.LLM
        ) return
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true)
        operationJob = componentScope.launch {
            awaitPendingMoves()
            manageGame.updateAiConfig(gameId, side, config)?.let(::applyPosition)
            gomokuAiStore.update(gomokuAiStore.get().withSource(uiState.mode.engineSlotFor(side), config.source))
            uiState = uiState.copy(isUpdating = false, errorMessage = "")
            refreshAiDisplay()
            suppressAiRequests = !isVisible || isNavigatingAway
            requestAiMove()
        }
    }

    fun currentSourceForSide(side: Side): GomokuAiSource {
        return savedAiConfig(side)?.source ?: currentAiConfig.value.sourceFor(uiState.mode.engineSlotFor(side))
    }

    fun savedAiConfig(side: Side): GameAiPlayerConfig? =
        if (side == Side.BLACK) uiState.blackAiConfig else uiState.whiteAiConfig

    val currentAIEngine: StateFlow<AiEngine> = aiEngineManager.fastAIEngine

    fun currentEngineForSide(side: Side): AiEngine = when (uiState.mode) {
        GameMode.LLM_VS_LLM ->
            if (side == Side.BLACK) aiEngineManager.getDuelEngineA() else aiEngineManager.getDuelEngineB()
        else -> aiEngineManager.getFastAiEngine()
    }

    fun openAnalysis() {
        onNavigate(Screen.GomokuRouter(Screen.GomokuRouter.Type.Analysis(gameId, uiState.currentPly)))
    }

    fun pauseBeforeNavigating(onPaused: () -> Unit) {
        isNavigatingAway = true
        mayResumeAfterSetup = false
        immediateStartPending = false
        suppressAiRequests = true
        cancelAiRequest()
        componentScope.launch {
            operationJob?.join()
            setupPauseJob?.join()
            awaitPendingMoves()
            pauseOfflineGame()?.let(::applyPosition)
            onPaused()
        }
    }

    fun prepareStandardGame() {
        showStandardGameConfirm = false
        if (!isVisible || isNavigatingAway || !uiState.isLoaded || uiState.mode == GameMode.ONLINE_PVP ||
            uiState.isUpdating || setupDraft != null
        ) return
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true)
        operationJob = componentScope.launch {
            try {
                awaitPendingMoves()
                manageGame.pause(gameId)
                val detail = gameQuery.getById(gameId) ?: return@launch
                onPrepareGame(detail.prepareFrom(FenCodec.parse(FenCodec.INITIAL_FEN), 0).copy(origin = null))
            } finally {
                uiState = uiState.copy(isUpdating = false)
            }
        }
    }

    fun openSourceGame() {
        val origin = uiState.origin ?: return
        componentScope.launch {
            if (gameQuery.getById(origin.gameId) == null) ActionUtils.showToast(R.string.gomoku_game_missing)
            else onNavigate(Screen.GomokuRouter(Screen.GomokuRouter.Type.Game(origin.gameId)))
        }
    }

    private fun withAiAccess(onFailure: () -> Unit = {}, onSuccess: () -> Unit) {
        val configs = buildList {
            if (uiState.blackPlayerType == PlayerType.LLM) add(savedAiConfig(Side.BLACK))
            if (uiState.whitePlayerType == PlayerType.LLM) add(savedAiConfig(Side.WHITE))
        }
        if (configs.any { it == null || !it.isSupported }) {
            uiState = uiState.copy(errorMessage = "AI_OPPONENT_UNAVAILABLE")
            onFailure()
        } else if (configs.any { it?.source?.requiresLogin == true }) {
            ActionUtils.ensureLoginAndCheckPoints(
                source = "gomoku_start",
                point = configs.filterNotNull().maxOf { it.source.startPoints },
                onLoginFailure = {
                    ActionUtils.showToast(com.shifenmiao.core.R.string.login_failed)
                    onFailure()
                },
                onPointsFailure = onFailure,
                onSuccess = onSuccess,
            )
        } else onSuccess()
    }

    fun resign() {
        if (!isVisible || isNavigatingAway || uiState.status != GameStatus.PLAYING ||
            uiState.isUpdating || onlineCommitJob?.isActive == true
        ) return
        val resigningSide = if (uiState.mode == GameMode.ONLINE_PVP) {
            uiState.onlineMySide
        } else if (uiState.mode == GameMode.HUMAN_VS_LLM) {
            humanSide()
        } else {
            uiState.boardState.sideToMove
        }
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true)
        operationJob = componentScope.launch {
            try {
                awaitPendingMoves()
                serializeOnlineMutation {
                    if (uiState.mode == GameMode.ONLINE_PVP && !ownsOnlineSession()) {
                        reportOnlineSyncError("Local resignation no longer owns the active room")
                        return@serializeOnlineMutation
                    }
                    val detail = gameQuery.getById(gameId) ?: return@serializeOnlineMutation
                    if (detail.status == GameStatus.PLAYING) {
                        manageGame.resign(gameId, resigningSide)?.let { resigned ->
                            applyPosition(resigned)
                            if (resigned.mode == GameMode.ONLINE_PVP && resigned.status == GameStatus.RESIGNED) {
                                if (!onlinePlay.sendResign(resigned.onlineMetadata.roomId, resigned.onlineMetadata.mySide)) {
                                    reportOnlineSyncError("Room changed before the local resignation could be sent")
                                }
                            }
                        }
                    }
                }
            } finally {
                uiState = uiState.copy(isUpdating = onlineCommitJob?.isActive == true)
            }
        }
        showResignConfirm = false
    }

    fun renameGame(newTitle: String) {
        componentScope.launch { serializeOnlineMutation { manageGame.rename(gameId, newTitle) } }
        showRenameDialog = false
    }

    /* ─────────── private ─────────── */

    private fun observeOpponentEvents(roomId: String) {
        componentScope.launch(start = CoroutineStart.UNDISPATCHED) {
            onlinePlay.opponentEvents(roomId).collect { event ->
                val previousMove = moveCommitJob
                val previousOperation = operationJob
                val handlingJob = Job()
                onlineCommitJob = handlingJob
                uiState = uiState.copy(isUpdating = true)
                try {
                    // The mailbox retains this event until persistence finishes. Local
                    // writers never join this marker, so waiting for them cannot deadlock.
                    withContext(NonCancellable) {
                        previousMove?.join()
                        previousOperation?.join()
                        persistOnlineEvent(event)
                        onlinePlay.completeOpponentEvent(event)
                    }
                } finally {
                    handlingJob.complete()
                    if (onlineCommitJob === handlingJob) {
                        onlineCommitJob = null
                        uiState = uiState.copy(
                            isUpdating = moveCommitJob?.isActive == true || operationJob?.isActive == true,
                        )
                    }
                }
            }
        }
    }

    private fun ownsOnlineSession(): Boolean = uiState.mode == GameMode.ONLINE_PVP &&
        uiState.onlineRoomId.isNotBlank() && uiState.onlineRoomId == onlinePlay.currentRoomId &&
        uiState.onlineMySide == onlinePlay.mySide

    private suspend fun persistOnlineEvent(event: OnlineGameEvent) {
        when (val result = onlineSync.accept(gameId, event)) {
            is GomokuOnlineGameSync.Result.Applied -> {
                applyPosition(result.detail)
                if (event is OnlineGameEvent.Move) {
                    componentScope.launch {
                        if (isVisible && !isNavigatingAway) {
                            audioFeedback.playForMove(result.detail.currentFen, audioSettings)
                        }
                    }
                }
            }
            is GomokuOnlineGameSync.Result.Rejected -> reportOnlineSyncError(result.reason)
        }
    }

    private suspend fun <T> serializeOnlineMutation(block: suspend () -> T): T =
        if (uiState.mode == GameMode.ONLINE_PVP) onlineSync.mutate(block) else block()

    private suspend fun pauseOfflineGame(): GameDetail? {
        val detail = gameQuery.getById(gameId) ?: return null
        return if (detail.mode == GameMode.ONLINE_PVP) null else manageGame.pause(gameId)
    }

    private fun reportOnlineSyncError(reason: String) {
        val message = "SYNC ERROR: $reason"
        if (onlineSyncError == message) return
        onlineSyncError = message
        uiState = uiState.copy(
            onlineConnectionState = ConnectionState.ERROR,
            onlineDebugEvents = onlineDebugEventsWithError(onlinePlay.debugEvents.value),
        )
        ActionUtils.showToast(R.string.gomoku_signaling_disconnected)
    }

    private fun onlineDebugEventsWithError(events: List<String>): List<String> =
        onlineSyncError?.let { events + it } ?: events

    private fun observeOnlineConnection() {
        componentScope.launch {
            onlinePlay.connectionState.collect { state ->
                uiState = uiState.copy(
                    onlineConnectionState = if (onlineSyncError == null) state else ConnectionState.ERROR,
                )
            }
        }
    }

    private fun observeOnlineDebugEvents() {
        componentScope.launch {
            onlinePlay.debugEvents.collect { events ->
                uiState = uiState.copy(onlineDebugEvents = onlineDebugEventsWithError(events))
            }
        }
    }

    private fun connectOnlineIfNeeded(detail: GameDetail) {
        val metadata = detail.onlineMetadata
        if (metadata.roomId.isBlank()) return
        if (onlinePlay.currentRoomId.isNotBlank()) return
        onlinePlay.connect(
            roomId = metadata.roomId,
            side = metadata.mySide,
            isHost = metadata.mySide == Side.BLACK,
            config = OnlineRoomConfig.fromFen(metadata.initialFen.ifBlank { detail.initialFen }),
        )
    }

    private fun observeGame() {
        componentScope.launch {
            pauseOfflineGame()
            val existing = gameQuery.getById(gameId)
            immediateStartPending = immediateStartPending && existing != null && existing.status == GameStatus.NOT_STARTED &&
                existing.startedAt == 0L && existing.currentPly == 0
            if (existing != null) {
                val defaults = gomokuAiStore.get()
                for (side in Side.entries) {
                    val playerType = if (side == Side.BLACK) existing.blackPlayerType else existing.whitePlayerType
                    val config = if (side == Side.BLACK) existing.blackAiConfig else existing.whiteAiConfig
                    val rawConfig = if (side == Side.BLACK) existing.blackPlayerConfigJson else existing.whitePlayerConfigJson
                    if (playerType == PlayerType.LLM && config == null && GameAiPlayerConfig.isLegacyEmpty(rawConfig)) {
                        val slot = existing.mode.engineSlotFor(side)
                        val engine = when (slot) {
                            EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
                            EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
                            EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
                        }
                        val snapshot = GameAiPlayerConfig.capture(defaults.sourceFor(slot), engine)
                        if (snapshot.isSupported) manageGame.updateAiConfig(gameId, side, snapshot)
                    }
                }
            }
            gameQuery.observeById(gameId).collect { detail ->
                if (detail == null) {
                    cancelAiRequest()
                    uiState = uiState.copy(isLoaded = false, errorMessage = "GAME_MISSING")
                    return@collect
                }
                val boardState = FenCodec.parse(detail.currentFen)
                val legalMoves = GameArbiter.legalMoves(boardState)
                val blackInfo = resolveAiDisplay(detail.blackPlayerType, detail.blackAiConfig)
                val whiteInfo = resolveAiDisplay(detail.whitePlayerType, detail.whiteAiConfig)
                if (detail.status.isTerminal() && !uiState.status.isTerminal()) showGameOverOverlay = true

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
                    onlineConnectionState = if (onlineSyncError == null) onlinePlay.connectionState.value else ConnectionState.ERROR,
                    onlineDebugEvents = onlineDebugEventsWithError(onlinePlay.debugEvents.value),
                    winnerSide = detail.winnerSide,
                    initialFen = detail.initialFen,
                    startedAt = detail.startedAt,
                    isLoaded = true,
                    blackAiConfig = detail.blackAiConfig,
                    whiteAiConfig = detail.whiteAiConfig,
                    origin = detail.origin,
                )

                if (detail.mode == GameMode.ONLINE_PVP && detail.onlineMetadata.roomId.isNotBlank() &&
                    !detail.status.isTerminal() && !onlineMovesObserved
                ) {
                    onlineMovesObserved = true
                    observeOpponentEvents(detail.onlineMetadata.roomId)
                    observeOnlineConnection()
                    observeOnlineDebugEvents()
                    connectOnlineIfNeeded(detail)
                }

                maybeStartImmediately()
                if (shouldRequestAi(detail, boardState)) {
                    requestAiMove()
                }
            }
        }
    }

    private fun requestAiMove() {
        if (!isVisible || isNavigatingAway || suppressAiRequests || setupDraft != null ||
            !uiState.isLoaded || uiState.isUpdating || !uiState.status.isPlayable() ||
            !isAiTurn() || aiRequestJob?.isActive == true
        ) return
        val currentFen = FenCodec.encode(uiState.boardState)
        if (lastRequestedFen == currentFen) return
        lastRequestedFen = currentFen
        uiState = uiState.copy(isAiThinking = true, errorMessage = "")

        val job = componentScope.launch(start = CoroutineStart.LAZY) {
            val requestJob = currentCoroutineContext()[Job]
            var continueAi = false
            try {
                awaitPendingMoves()
                currentCoroutineContext().ensureActive()
                val slot = uiState.mode.engineSlotFor(uiState.boardState.sideToMove)
                when (val outcome = aiOrchestration.requestMove(gameId, slot)) {
                    is AiOrchestrationUseCase.Outcome.Committed -> {
                        applyPosition(outcome.detail)
                        val lastPly = outcome.detail.plies.firstOrNull { it.ply == outcome.detail.currentPly }
                        if (lastPly != null) audioFeedback.playForMove(lastPly.afterFen, audioSettings)
                        continueAi = true
                    }
                    is AiOrchestrationUseCase.Outcome.Stale -> Unit
                    is AiOrchestrationUseCase.Outcome.Failed -> {
                        uiState = uiState.copy(errorMessage = outcome.reason)
                    }
                }
            } finally {
                if (aiRequestJob === requestJob) {
                    aiRequestJob = null
                    if (continueAi) lastRequestedFen = null
                    uiState = uiState.copy(isAiThinking = false)
                }
            }
            if (continueAi) {
                val latest = gameQuery.getById(gameId)
                if (latest != null && shouldRequestAi(latest, FenCodec.parse(latest.currentFen))) {
                    applyPosition(latest)
                    requestAiMove()
                }
            }
        }
        aiRequestJob = job
        job.start()
    }

    private fun shouldRequestAi(detail: GameDetail, boardState: BoardState): Boolean {
        val currentFen = FenCodec.encode(boardState)
        val playable = detail.status.isPlayable()
        return isVisible && !isNavigatingAway && !suppressAiRequests && setupDraft == null &&
            !uiState.isUpdating && aiRequestJob?.isActive != true &&
            playable && isCurrentSideAi(detail) && lastRequestedFen != currentFen
    }

    private fun isCurrentSideAi(detail: GameDetail): Boolean = when (
        FenCodec.parse(detail.currentFen).sideToMove
    ) {
        Side.BLACK -> detail.blackPlayerType == PlayerType.LLM
        Side.WHITE -> detail.whitePlayerType == PlayerType.LLM
    }

    private fun cancelAiRequest() {
        val previousRequest = aiRequestJob
        val previousCleanup = aiCleanupJob
        previousRequest?.cancel()
        aiRequestJob = null
        lastRequestedFen = null
        uiState = uiState.copy(isAiThinking = false)
        aiCleanupJob = componentScope.launch {
            previousCleanup?.join()
            previousRequest?.cancelAndJoin()
            aiOrchestration.clearTasks(gameId)
        }
    }

    private suspend fun awaitPendingMoves() {
        moveCommitJob?.join()
        aiCleanupJob?.join()
    }

    private fun resolveAiDisplay(
        playerType: PlayerType,
        config: GameAiPlayerConfig?,
    ): Pair<String, String> {
        if (playerType != PlayerType.LLM) return "" to ""
        return config?.displayNames() ?: ("" to "")
    }

    private fun refreshAiDisplay() {
        val black = resolveAiDisplay(uiState.blackPlayerType, savedAiConfig(Side.BLACK))
        val white = resolveAiDisplay(uiState.whitePlayerType, savedAiConfig(Side.WHITE))
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

    private fun maybeStartImmediately() {
        if (!immediateStartPending || !isVisible || isNavigatingAway || !uiState.isLoaded ||
            uiState.isUpdating || uiState.status != GameStatus.NOT_STARTED
        ) return
        immediateStartPending = false
        start()
    }

    private suspend fun playSound(boardBefore: BoardState, move: GomokuMove) {
        // 落子音效用「走前局面」推导走后局面:提交成功后 DB observer 尚未回灌,uiState 还是旧局面
        val after = boardBefore.withStonePlaced(move)
        audioFeedback.playForMove(FenCodec.encode(after), audioSettings)
    }

    private fun GameStatus.isPlayable(): Boolean = this == GameStatus.PLAYING
    private fun GameStatus.isTerminal(): Boolean = this == GameStatus.BLACK_WINS ||
        this == GameStatus.WHITE_WINS || this == GameStatus.DRAW || this == GameStatus.RESIGNED

    private fun isLocalOnlineTurn(): Boolean =
        uiState.mode != GameMode.ONLINE_PVP || uiState.boardState.sideToMove == uiState.onlineMySide

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            gameId: String,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            onPrepareGame: (GamePreparation) -> Unit,
            startImmediately: Boolean,
        ): GomokuGameComponent
    }
}
