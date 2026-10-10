package com.wanbaohe.chess.component

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
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.dto.GamePreparation
import com.wanbaohe.chess.application.dto.aiConfigFor
import com.wanbaohe.chess.application.dto.engineSlotFor
import com.wanbaohe.chess.application.dto.prepareFrom
import com.wanbaohe.chess.application.dto.prepareRestart
import com.wanbaohe.chess.application.dto.prepareStandardOpening
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
import com.wanbaohe.chess.application.usecase.OnlineGameSyncUseCase
import com.wanbaohe.chess.application.usecase.PlayMoveUseCase
import com.wanbaohe.chess.application.usecase.SettingsUseCase
import com.wanbaohe.chess.data.ChessPlyRecord
import com.wanbaohe.chess.data.TextExportLabels
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.BoardSetupDraft
import com.wanbaohe.chess.domain.HumanAiHistory
import com.wanbaohe.chess.domain.SetupPositionValidator
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
import com.wanbaohe.chess.domain.model.GameOrigin
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.domain.model.ChessMove
import com.wanbaohe.chess.presentation.displayNames
import com.wanbaohe.chess.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
    val onlineMySide: Side = Side.WHITE,
    val onlineOpponentName: String = "",
    val onlineOpponentAvatarUrl: String = "",
    val onlineConnectionState: ConnectionState = ConnectionState.IDLE,
    val onlineDebugEvents: List<String> = emptyList(),
    val winnerSide: String = "",
    val initialFen: String = FenCodec.INITIAL_FEN,
    val startedAt: Long = 0L,
    val isLoaded: Boolean = false,
    val isUpdating: Boolean = false,
    val whiteAiConfig: GameAiPlayerConfig? = null,
    val blackAiConfig: GameAiPlayerConfig? = null,
    val origin: GameOrigin? = null,
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
    private val chessAiStore: ChessAiStore,
    private val onlinePlay: OnlinePlayUseCase,
    private val onlineGameSync: OnlineGameSyncUseCase,
    aiEngineCatalogManager: AIEngineCatalogManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    var uiState by mutableStateOf(ChessGameUiState())
        private set

    var showResignConfirm by mutableStateOf(false)
    var showRestartConfirm by mutableStateOf(false)
    var showRenameDialog by mutableStateOf(false)
    var showStandardGameConfirm by mutableStateOf(false)
    var showGameOverOverlay by mutableStateOf(true)
    var setupDraft by mutableStateOf<BoardSetupDraft?>(null)
        private set

    private var setupPreviousStatus = GameStatus.NOT_STARTED
    private var visibilityEpoch = 0L
    private var setupVisibilityEpoch = 0L
    private var editingStartedAt = 0L
    private var setupPauseJob: Job? = null
    private var setupSource: GameDetail? = null
    private var suppressAiRequests = true
    private var pendingImmediateStart = startImmediately
    private var isVisible by mutableStateOf(lifecycle.state >= Lifecycle.State.STARTED)
    private var isNavigatingAway by mutableStateOf(false)

    val canSetup: Boolean
        get() = isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isUpdating &&
            !uiState.isAiThinking && uiState.mode != GameMode.ONLINE_PVP &&
            (uiState.status.isTerminal() || uiState.status == GameStatus.NOT_STARTED ||
                (uiState.mode != GameMode.LLM_VS_LLM && isHumanTurn()))

    fun beginSetup() {
        if (!canSetup) {
            ActionUtils.showToast(R.string.chess_setup_wait_for_turn)
            return
        }
        setupPreviousStatus = if (suppressAiRequests && uiState.status.isPlayable()) GameStatus.PAUSED else uiState.status
        setupVisibilityEpoch = visibilityEpoch
        editingStartedAt = System.currentTimeMillis()
        suppressAiRequests = true
        cancelAiRequest()
        setupSource = null
        setupDraft = BoardSetupDraft(uiState.boardState)
        setupPauseJob = componentScope.launch {
            awaitPendingMoves()
            setupSource = gameQuery.getById(gameId)
            manageGame.pause(gameId)?.let(::applyPosition)
        }
    }

    fun updateSetup(draft: BoardSetupDraft) {
        if (!uiState.isUpdating && setupDraft != null) setupDraft = draft
    }

    fun cancelSetup() {
        if (setupDraft == null || uiState.isUpdating) return
        uiState = uiState.copy(isUpdating = true)
        componentScope.launch {
            setupPauseJob?.join()
            val canResume = setupPreviousStatus.isPlayable() && setupVisibilityEpoch == visibilityEpoch
            if (canResume && isVisible && !isNavigatingAway) {
                manageGame.resumeAfterEditing(gameId, editingStartedAt)?.let(::applyPosition)
                if (!isVisible || isNavigatingAway || setupVisibilityEpoch != visibilityEpoch) {
                    manageGame.pause(gameId)?.let(::applyPosition)
                }
            }
            setupDraft = null
            setupSource = null
            suppressAiRequests = !isVisible || isNavigatingAway || !canResume || !uiState.status.isPlayable()
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
            ActionUtils.showToast(R.string.chess_setup_invalid)
            return
        }
        if (uiState.isUpdating) return
        val epoch = visibilityEpoch
        uiState = uiState.copy(isUpdating = true)
        componentScope.launch {
            setupPauseJob?.join()
            if (!isVisible || isNavigatingAway || visibilityEpoch != epoch) {
                uiState = uiState.copy(isUpdating = false)
                return@launch
            }
            val detail = gameQuery.getById(gameId)
            if (!isVisible || isNavigatingAway || visibilityEpoch != epoch) {
                uiState = uiState.copy(isUpdating = false)
                return@launch
            }
            val source = setupSource ?: detail
            if (source == null || (detail == null && source.status == GameStatus.NOT_STARTED &&
                    source.startedAt == 0L && source.currentPly == 0)
            ) {
                uiState = uiState.copy(isUpdating = false)
                ActionUtils.showToast(R.string.chess_game_not_found)
                return@launch
            }
            if (detail != null && detail.status == GameStatus.NOT_STARTED && detail.startedAt == 0L) {
                manageGame.updateInitialPosition(gameId, draft.startPosition())
                setupDraft = null
            } else {
                setupDraft = null
                onPrepareGame(source.prepareFrom(draft.startPosition()))
            }
            setupSource = null
            uiState = uiState.copy(isUpdating = false)
        }
    }

    fun dismissGameOverOverlay() { showGameOverOverlay = false }
    fun reopenGameOverOverlay() { showGameOverOverlay = true }

    val allAiEngines: StateFlow<List<AiEngine>> =
        aiEngineCatalogManager.observeAvailableEngines()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modelsByProvider: StateFlow<Map<String, List<AiModel>>> =
        aiEngineCatalogManager.observeModelsByProvider()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /**
     * Next-game defaults. Existing games use their persisted opponent snapshots.
     *
     * ⚠️ 必须声明在 [init] 之前,并由 [collectAiConfig] 真正订阅:
     * `stateIn(WhileSubscribed)` 没有下游时会一直停在初值 [ChessAiConfig],
     * 只读 `.value` 拿到的是过期默认值而不是用户的选择。
     */
    val currentAiConfig: StateFlow<ChessAiConfig> = chessAiStore.observe()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), ChessAiConfig())

    private var aiRequestJob: Job? = null
    private var aiCleanupJob: Job? = null
    private var moveCommitJob: Deferred<PlayMoveUseCase.Result>? = null
    private var onlineSyncJob: Deferred<GameDetail?>? = null
    private var startJob: Job? = null
    private var lifecyclePauseJob: Job? = null
    private var startAttempt = 0L
    private var awaitingStartAccess = false
    private var lastRequestedFen: String? = null
    private var audioSettings: AudioSettings = AudioSettings()
    private var onlineMovesObserved = false

    init {
        lifecycle.doOnStart {
            isVisible = true
            isNavigatingAway = false
            if (pendingImmediateStart && uiState.isLoaded) {
                pendingImmediateStart = false
                start()
            }
        }
        lifecycle.doOnStop {
            isVisible = false
            visibilityEpoch++
            suppressAiRequests = true
            pendingImmediateStart = false
            cancelStart()
            cancelAiRequest()
            if (uiState.isLoaded && uiState.mode != GameMode.ONLINE_PVP) {
                val previousPause = lifecyclePauseJob
                val pendingStart = startJob
                val pendingMove = moveCommitJob
                val cleanup = aiCleanupJob
                lifecyclePauseJob = componentScope.launch {
                    previousPause?.join()
                    pendingStart?.join()
                    pendingMove?.join()
                    cleanup?.join()
                    manageGame.pauseOffline(gameId)?.let(::applyPosition)
                }
            }
        }
        collectSettings()
        collectAiEngines()
        collectAiConfig()
        observeGame()
    }

    /** 升变选择确认:用户从弹窗选定目标子后提交 */
    fun commitPromotion(move: ChessMove) {
        if (!canPlayHumanMove() || move !in uiState.interaction.pendingPromotionMoves) return
        commitHumanMove(move)
    }

    /** 升变弹窗取消:回到未选中状态 */
    fun cancelPromotion() {
        uiState = uiState.copy(interaction = InteractionState())
    }

    fun onCellTap(file: Int, rank: Int) {
        if (!canPlayHumanMove()) return

        val boardBefore = uiState.boardState
        val next = GameReducer.reduce(
            boardState = boardBefore,
            legalMoves = uiState.legalMoves,
            previous = uiState.interaction,
            action = GameAction.TapCell(BoardPoint(file, rank)),
        )
        uiState = uiState.copy(interaction = next)

        next.pendingMove?.let(::commitHumanMove)
    }

    private fun canPlayHumanMove(): Boolean =
        isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isAiThinking &&
            (!suppressAiRequests || uiState.mode == GameMode.ONLINE_PVP) &&
            !uiState.isUpdating && setupDraft == null && uiState.status.isPlayable() &&
            isHumanTurn() && isLocalOnlineTurn()

    private fun commitHumanMove(move: ChessMove) {
        if (!ensureOnlineSession()) return
        val before = uiState.boardState
        uiState = uiState.copy(isUpdating = true)
        val commit = componentScope.async(start = CoroutineStart.LAZY) {
            if (!ensureOnlineSession()) PlayMoveUseCase.Result.Rejected("Online session changed")
            else playMove.commit(gameId, move)
        }
        moveCommitJob = commit
        componentScope.launch {
            val result = try {
                commit.await()
            } finally {
                if (moveCommitJob === commit) moveCommitJob = null
            }
            if (result is PlayMoveUseCase.Result.Success) {
                applyPosition(result.detail)
                playSound(before, move)
                if (result.detail.mode == GameMode.ONLINE_PVP && !onlinePlay.sendMove(
                        move, result.detail.onlineMetadata.roomId, result.detail.onlineMetadata.mySide,
                    )
                ) reportOnlineSendFailure()
            }
            uiState = uiState.copy(interaction = InteractionState(), isUpdating = false)
            requestAiMove()
        }
    }

    fun undo() {
        if (!canUndo) return
        val target = uiState.currentPly - undoSteps()
        val epoch = visibilityEpoch
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true, interaction = InteractionState())
        componentScope.launch {
            awaitPendingMoves()
            val current = gameQuery.getById(gameId)
            finishHistoryChange(current?.let { manageGame.undo(gameId, (it.currentPly - target).coerceAtLeast(0)) }, epoch)
        }
    }

    fun redo() {
        if (!canRedo) return
        val target = uiState.currentPly + redoSteps()
        val epoch = visibilityEpoch
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true, interaction = InteractionState())
        componentScope.launch {
            awaitPendingMoves()
            val current = gameQuery.getById(gameId)
            finishHistoryChange(current?.let { manageGame.redo(gameId, (target - it.currentPly).coerceAtLeast(0)) }, epoch)
        }
    }

    private suspend fun finishHistoryChange(detail: GameDetail?, epoch: Long) {
        val latest = if (detail != null && detail.mode != GameMode.ONLINE_PVP &&
            (!isVisible || isNavigatingAway || epoch != visibilityEpoch)
        ) manageGame.pause(gameId) else detail
        suppressAiRequests = !isVisible || isNavigatingAway || epoch != visibilityEpoch || latest?.status?.isPlayable() != true
        if (latest != null) applyPosition(latest) else ActionUtils.showToast(R.string.chess_game_not_found)
        uiState = uiState.copy(isUpdating = false, errorMessage = "")
        requestAiMove()
    }

    private fun applyPosition(detail: GameDetail) {
        val board = FenCodec.parse(detail.currentFen)
        uiState = uiState.copy(
            boardState = board, legalMoves = GameArbiter.legalMoves(board),
            currentPly = detail.currentPly, history = detail.plies,
            status = detail.status, winnerSide = detail.winnerSide,
        )
    }

    val canUndo: Boolean get() = isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isUpdating &&
        uiState.mode != GameMode.ONLINE_PVP && setupDraft == null && undoSteps() > 0
    val canRedo: Boolean get() = isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isUpdating &&
        uiState.mode != GameMode.ONLINE_PVP && setupDraft == null && redoSteps() > 0

    private fun humanSide(): Side = if (uiState.whitePlayerType == PlayerType.HUMAN) Side.WHITE else Side.BLACK

    private fun isHumanTurn(): Boolean = when (uiState.boardState.sideToMove) {
        Side.WHITE -> uiState.whitePlayerType == PlayerType.HUMAN
        Side.BLACK -> uiState.blackPlayerType == PlayerType.HUMAN
    }

    private fun undoSteps(): Int = if (uiState.mode == GameMode.HUMAN_VS_LLM) {
        HumanAiHistory.undoSteps(uiState.history.map { it.moverSide }, uiState.currentPly, humanSide())
    } else if (uiState.currentPly > 0) 1 else 0

    private fun redoSteps(): Int = if (uiState.mode == GameMode.HUMAN_VS_LLM) {
        HumanAiHistory.redoSteps(uiState.history.map { it.moverSide }, uiState.currentPly, humanSide())
    } else if (uiState.currentPly < uiState.history.size) 1 else 0

    fun pauseBeforeNavigating(onPaused: () -> Unit) {
        isNavigatingAway = true
        visibilityEpoch++
        pendingImmediateStart = false
        suppressAiRequests = true
        cancelStart()
        cancelAiRequest()
        componentScope.launch {
            awaitPendingMoves()
            setupPauseJob?.join()
            if (uiState.mode != GameMode.ONLINE_PVP) manageGame.pause(gameId)?.let(::applyPosition)
            onPaused()
        }
    }

    fun restart() {
        if (!isVisible || isNavigatingAway || uiState.isUpdating) return
        val epoch = visibilityEpoch
        suppressAiRequests = true
        cancelAiRequest()
        componentScope.launch {
            awaitPendingMoves()
            if (!isVisible || isNavigatingAway || visibilityEpoch != epoch) return@launch
            if (uiState.mode == GameMode.ONLINE_PVP) {
                val detail = manageGame.restart(gameId) ?: return@launch
                if (detail.id != gameId && isVisible && !isNavigatingAway && visibilityEpoch == epoch) {
                    onNavigate(Screen.ChessRouter(Screen.ChessRouter.Type.Game(detail.id)))
                }
            } else {
                manageGame.pause(gameId)
                val detail = gameQuery.getById(gameId) ?: return@launch
                if (isVisible && !isNavigatingAway && visibilityEpoch == epoch) onPrepareGame(detail.prepareRestart())
            }
        }
        showRestartConfirm = false
    }

    fun start() {
        if (!isVisible || isNavigatingAway || !uiState.isLoaded || uiState.isUpdating ||
            setupDraft != null || startJob?.isCompleted == false ||
            uiState.status !in setOf(GameStatus.NOT_STARTED, GameStatus.PAUSED)
        ) return
        val attempt = ++startAttempt
        awaitingStartAccess = true
        uiState = uiState.copy(isUpdating = true)
        withAiAccess(onFailure = {
            if (attempt == startAttempt) {
                awaitingStartAccess = false
                uiState = uiState.copy(isUpdating = false)
            }
        }) {
            if (attempt != startAttempt || !isVisible || isNavigatingAway) return@withAiAccess
            awaitingStartAccess = false
            val job = componentScope.launch(start = CoroutineStart.LAZY) {
                val thisJob = currentCoroutineContext()[Job]
                try {
                    awaitPendingMoves(waitForStart = false)
                    if (attempt != startAttempt || !isVisible || isNavigatingAway) return@launch
                    suppressAiRequests = false
                    if (uiState.mode == GameMode.ONLINE_PVP &&
                        !onlinePlay.sendStart(uiState.onlineRoomId, uiState.onlineMySide)
                    ) {
                        reportOnlineSendFailure()
                        return@launch
                    }
                    val detail = manageGame.start(gameId)
                    if ((attempt != startAttempt || !isVisible || isNavigatingAway) &&
                        uiState.mode != GameMode.ONLINE_PVP
                    ) {
                        manageGame.pause(gameId)?.let(::applyPosition)
                    } else if (detail != null) applyPosition(detail)
                    else ActionUtils.showToast(R.string.chess_game_not_found)
                } finally {
                    if (startJob === thisJob) {
                        startJob = null
                        uiState = uiState.copy(isUpdating = false)
                    }
                }
                if (attempt == startAttempt) requestAiMove()
            }
            startJob = job
            job.start()
        }
    }

    private fun cancelStart() {
        startAttempt++
        if (awaitingStartAccess || startJob?.isCompleted == false) {
            uiState = uiState.copy(isUpdating = false)
        }
        awaitingStartAccess = false
        startJob?.cancel()
    }

    fun pause() { pauseForModal {} }

    fun pauseForModal(onPaused: () -> Unit) {
        if (!isVisible || isNavigatingAway || !uiState.isLoaded) return
        suppressAiRequests = true
        cancelStart()
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true, interaction = InteractionState())
        componentScope.launch {
            awaitPendingMoves()
            manageGame.pauseOffline(gameId)?.let(::applyPosition)
            uiState = uiState.copy(isUpdating = false)
            if (isVisible && !isNavigatingAway) onPaused()
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
        if (!isVisible || isNavigatingAway || !uiState.status.isPlayable() || isHumanTurn() ||
            setupDraft != null || uiState.isUpdating) return
        cancelAiRequest()
        withAiAccess { requestAiMove() }
    }

    fun switchAiModelForSide(side: Side, engine: AiEngine, model: AiModel) {
        if (!canChangeOpponent(side)) return
        ActionUtils.ensureLoginAndCheckPoints(
            source = "chess_switch_model",
            point = ChessAiSource.WorkingModel.startPoints,
            onSuccess = {
                if (!canChangeOpponent(side)) return@ensureLoginAndCheckPoints
                when (uiState.mode.engineSlotFor(side)) {
                    EngineSlot.DUEL_A -> aiEngineManager.setDuelEngineA(engine.copy(model = model))
                    EngineSlot.DUEL_B -> aiEngineManager.setDuelEngineB(engine.copy(model = model))
                    EngineSlot.FAST -> aiEngineManager.switchFastModel(engine, model)
                }
                saveAiConfig(side, GameAiPlayerConfig.capture(ChessAiSource.WorkingModel, engine.copy(model = model)))
            },
        )
    }

    fun switchAiSourceForSide(side: Side, source: ChessAiSource) {
        val saved = if (side == Side.WHITE) uiState.whiteAiConfig else uiState.blackAiConfig
        if (!canChangeOpponent(side) || source == saved?.takeIf { it.isSupported }?.source) return
        val save = { saveAiConfig(side, GameAiPlayerConfig.capture(source, currentEngineForSide(side))) }
        if (source.requiresLogin) {
            ActionUtils.ensureLoginAndCheckPoints(source = "chess_switch_ai", point = source.startPoints, onSuccess = save)
        } else save()
    }

    private fun canChangeOpponent(side: Side): Boolean =
        isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isUpdating &&
            setupDraft == null && uiState.mode != GameMode.ONLINE_PVP &&
            (if (side == Side.WHITE) uiState.whitePlayerType else uiState.blackPlayerType) == PlayerType.LLM

    private fun saveAiConfig(side: Side, config: GameAiPlayerConfig) {
        if (!canChangeOpponent(side)) return
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true)
        componentScope.launch {
            awaitPendingMoves()
            if (manageGame.updateAiConfig(gameId, side, config) == null) {
                uiState = uiState.copy(
                    isLoaded = false, isUpdating = false,
                    errorMessage = AppContext.getString(R.string.chess_game_not_found),
                )
                return@launch
            }
            chessAiStore.update(chessAiStore.get().withSource(uiState.mode.engineSlotFor(side), config.source))
            uiState = if (side == Side.WHITE) uiState.copy(whiteAiConfig = config, isUpdating = false, errorMessage = "")
                else uiState.copy(blackAiConfig = config, isUpdating = false, errorMessage = "")
            refreshAiDisplay()
            suppressAiRequests = !isVisible || isNavigatingAway || !uiState.status.isPlayable()
            requestAiMove()
        }
    }

    fun currentSourceForSide(side: Side): ChessAiSource {
        val config = if (side == Side.WHITE) uiState.whiteAiConfig else uiState.blackAiConfig
        return config?.source ?: currentAiConfig.value.sourceFor(uiState.mode.engineSlotFor(side))
    }

    fun gameAiConfigFor(side: Side): GameAiPlayerConfig =
        (if (side == Side.WHITE) uiState.whiteAiConfig else uiState.blackAiConfig)
            ?: GameAiPlayerConfig()

    val currentAIEngine: StateFlow<AiEngine> = aiEngineManager.fastAIEngine

    fun currentEngineForSide(side: Side): AiEngine = when (uiState.mode) {
        GameMode.LLM_VS_LLM ->
            if (side == Side.WHITE) aiEngineManager.getDuelEngineA() else aiEngineManager.getDuelEngineB()
        else -> aiEngineManager.getFastAiEngine()
    }

    fun openAnalysis() {
        if (!isVisible || isNavigatingAway) return
        onNavigate(Screen.ChessRouter(Screen.ChessRouter.Type.Analysis(gameId, uiState.currentPly)))
    }

    fun openSourceGame() {
        if (!isVisible || isNavigatingAway) return
        val origin = uiState.origin ?: return
        val epoch = visibilityEpoch
        componentScope.launch {
            val source = gameQuery.getById(origin.gameId)
            if (!isVisible || isNavigatingAway || visibilityEpoch != epoch) return@launch
            if (source == null) ActionUtils.showToast(R.string.chess_game_missing)
            else onNavigate(Screen.ChessRouter(Screen.ChessRouter.Type.Game(origin.gameId)))
        }
    }

    fun prepareStandardGame() {
        showStandardGameConfirm = false
        if (!isVisible || isNavigatingAway || uiState.mode == GameMode.ONLINE_PVP || uiState.isUpdating) return
        val epoch = visibilityEpoch
        suppressAiRequests = true
        cancelAiRequest()
        componentScope.launch {
            awaitPendingMoves()
            manageGame.pause(gameId)
            val detail = gameQuery.getById(gameId) ?: return@launch
            if (isVisible && !isNavigatingAway && visibilityEpoch == epoch) onPrepareGame(detail.prepareStandardOpening())
        }
    }

    private fun withAiAccess(onFailure: () -> Unit = {}, onSuccess: () -> Unit) {
        val configs = buildList {
            if (uiState.whitePlayerType == PlayerType.LLM) add(gameAiConfigFor(Side.WHITE))
            if (uiState.blackPlayerType == PlayerType.LLM) add(gameAiConfigFor(Side.BLACK))
        }
        if (configs.any { !it.isSupported }) {
            uiState = uiState.copy(errorMessage = AppContext.getString(R.string.chess_ai_opponent_unavailable))
            onFailure()
            return
        }
        if (configs.any { it.source.requiresLogin }) {
            ActionUtils.ensureLoginAndCheckPoints(
                source = "chess_start",
                point = configs.maxOf { it.source.startPoints },
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
        if (!ensureOnlineSession()) {
            showResignConfirm = false
            return
        }
        val resigningSide = if (uiState.mode == GameMode.ONLINE_PVP) {
            uiState.onlineMySide
        } else if (uiState.mode == GameMode.HUMAN_VS_LLM) {
            humanSide()
        } else {
            uiState.boardState.sideToMove
        }
        suppressAiRequests = true
        cancelAiRequest()
        componentScope.launch {
            awaitPendingMoves()
            if (!ensureOnlineSession()) return@launch
            val detail = manageGame.resign(gameId, resigningSide)
            if (detail?.mode == GameMode.ONLINE_PVP && detail.status == GameStatus.RESIGNED &&
                !onlinePlay.sendResign(detail.onlineMetadata.roomId, detail.onlineMetadata.mySide)
            ) reportOnlineSendFailure()
        }
        showResignConfirm = false
    }

    fun renameGame(newTitle: String) {
        componentScope.launch { manageGame.rename(gameId, newTitle) }
        showRenameDialog = false
    }

    /* ─────────── private ─────────── */

    private fun ensureOnlineSession(): Boolean {
        if (uiState.mode != GameMode.ONLINE_PVP ||
            onlinePlay.ownsSession(uiState.onlineRoomId, uiState.onlineMySide)
        ) return true
        reportOnlineSendFailure()
        return false
    }

    private fun reportOnlineSendFailure() {
        onlinePlay.reportSyncFailure(uiState.onlineRoomId, "Local action no longer owns the active room")
        uiState = uiState.copy(errorMessage = AppContext.getString(R.string.chess_online_sync_failed))
    }

    private fun observeOpponentMoves(roomId: String) {
        componentScope.launch(start = CoroutineStart.UNDISPATCHED) {
            // Protocol events were already ACKed. Visibility only gates local input and AI,
            // never network synchronization; the single stream also orders start/move/resign.
            onlinePlay.opponentEvents(roomId).collect { event ->
                awaitPendingMoves()
                uiState = uiState.copy(isUpdating = true)
                val sync = componentScope.async(start = CoroutineStart.LAZY) {
                    onlineGameSync.accept(gameId, event)
                }
                onlineSyncJob = sync
                try {
                    val detail = sync.await()
                    if (detail != null) {
                        applyPosition(detail)
                    } else {
                        onlinePlay.reportSyncFailure(event)
                        uiState = uiState.copy(errorMessage = AppContext.getString(R.string.chess_online_sync_failed))
                    }
                } finally {
                    if (onlineSyncJob === sync) onlineSyncJob = null
                    uiState = uiState.copy(isUpdating = false)
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
        if (onlinePlay.currentRoomId.isNotBlank()) return
        onlinePlay.connect(
            roomId = metadata.roomId,
            side = metadata.mySide,
            isHost = metadata.mySide == Side.WHITE,
            config = OnlineRoomConfig.fromFen(metadata.initialFen.ifBlank { detail.initialFen }),
        )
    }

    private fun observeGame() {
        componentScope.launch {
            val existing = gameQuery.getById(gameId)
            if (existing != null) {
                val defaults = chessAiStore.get()
                for (side in Side.entries) {
                    val playerType = if (side == Side.WHITE) existing.whitePlayerType else existing.blackPlayerType
                    val savedJson = if (side == Side.WHITE) existing.whitePlayerConfigJson else existing.blackPlayerConfigJson
                    if (playerType == PlayerType.LLM && GameAiPlayerConfig.isLegacyEmpty(savedJson)) {
                        val slot = existing.mode.engineSlotFor(side)
                        val engine = when (slot) {
                            EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
                            EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
                            EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
                        }
                        manageGame.updateAiConfig(gameId, side, GameAiPlayerConfig.capture(defaults.sourceFor(slot), engine))
                    }
                }
            }
            if (!startImmediately || !isVisible) manageGame.pauseOffline(gameId)
            gameQuery.observeById(gameId).collect { detail ->
                if (detail == null) {
                    suppressAiRequests = true
                    cancelAiRequest()
                    uiState = uiState.copy(isLoaded = false, errorMessage = AppContext.getString(R.string.chess_game_not_found))
                    return@collect
                }
                val boardState = FenCodec.parse(detail.currentFen)
                val legalMoves = GameArbiter.legalMoves(boardState)
                val blackInfo = resolveAiDisplay(detail.blackPlayerType, Side.BLACK, detail.blackAiConfig)
                val whiteInfo = resolveAiDisplay(detail.whitePlayerType, Side.WHITE, detail.whiteAiConfig)
                if (uiState.status.isPlayable() && detail.status.isTerminal()) showGameOverOverlay = true

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
                    initialFen = detail.initialFen,
                    startedAt = detail.startedAt,
                    winnerSide = detail.winnerSide,
                    whiteAiConfig = detail.whiteAiConfig,
                    blackAiConfig = detail.blackAiConfig,
                    origin = detail.origin,
                    isLoaded = true,
                )

                if (detail.mode == GameMode.ONLINE_PVP && detail.onlineMetadata.roomId.isNotBlank() && !onlineMovesObserved) {
                    onlineMovesObserved = true
                    observeOpponentMoves(detail.onlineMetadata.roomId)
                    observeOnlineConnection()
                    observeOnlineDebugEvents()
                    connectOnlineIfNeeded(detail)
                }

                if (pendingImmediateStart && isVisible) {
                    pendingImmediateStart = false
                    start()
                } else if (shouldRequestAi(detail, boardState)) {
                    requestAiMove()
                }
            }
        }
    }

    private fun requestAiMove() {
        if (!isVisible || isNavigatingAway || suppressAiRequests || setupDraft != null ||
            !uiState.isLoaded || uiState.isUpdating || !uiState.status.isPlayable() || isHumanTurn() ||
            aiRequestJob?.isActive == true || uiState.mode == GameMode.ONLINE_PVP
        ) return
        val currentFen = FenCodec.encode(uiState.boardState)
        if (lastRequestedFen == currentFen) return
        lastRequestedFen = currentFen
        uiState = uiState.copy(isAiThinking = true, errorMessage = "")

        val job = componentScope.launch(start = CoroutineStart.LAZY) {
            val requestJob = currentCoroutineContext()[Job]
            awaitPendingMoves()
            currentCoroutineContext().ensureActive()
            val slot = uiState.mode.engineSlotFor(uiState.boardState.sideToMove)
            var continueAi = false
            try {
                when (val outcome = aiOrchestration.requestMove(gameId, slot)) {
                    is AiOrchestrationUseCase.Outcome.Committed -> {
                        applyPosition(outcome.detail)
                        val lastPly = outcome.detail.plies.firstOrNull { it.ply == outcome.detail.currentPly }
                        if (lastPly != null && isVisible && !isNavigatingAway) {
                            audioFeedback.playForMove(lastPly.beforeFen, lastPly.afterFen, audioSettings)
                        }
                        continueAi = true
                    }
                    is AiOrchestrationUseCase.Outcome.Stale -> {
                        continueAi = outcome.reason == AiOrchestrationUseCase.StaleReason.POSITION_CHANGED
                        if (!continueAi) uiState = uiState.copy(errorMessage = AppContext.getString(R.string.chess_ai_error))
                    }
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
                if (latest != null && latest.currentFen != currentFen &&
                    shouldRequestAi(latest, FenCodec.parse(latest.currentFen))
                ) {
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

    private suspend fun awaitPendingMoves(waitForStart: Boolean = true) {
        moveCommitJob?.join()
        onlineSyncJob?.join()
        aiCleanupJob?.join()
        if (waitForStart) startJob?.join()
        lifecyclePauseJob?.join()
    }

    private fun resolveAiDisplay(
        playerType: PlayerType,
        side: Side,
        config: GameAiPlayerConfig? = if (side == Side.WHITE) uiState.whiteAiConfig else uiState.blackAiConfig,
    ): Pair<String, String> {
        if (playerType != PlayerType.LLM) return "" to ""
        return (config ?: GameAiPlayerConfig()).displayNames()
    }

    private fun refreshAiDisplay() {
        val black = resolveAiDisplay(uiState.blackPlayerType, Side.BLACK)
        val white = resolveAiDisplay(uiState.whitePlayerType, Side.WHITE)
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

    private suspend fun playSound(boardBefore: BoardState, move: ChessMove) {
        // 落子音效用「走前局面」推导走后局面:提交成功后 DB observer 尚未回灌,uiState 还是旧局面
        val after = boardBefore.withPieceMoved(move)
        audioFeedback.playForMove(FenCodec.encode(boardBefore), FenCodec.encode(after), audioSettings)
    }

    private fun GameStatus.isPlayable(): Boolean = this == GameStatus.PLAYING || this == GameStatus.CHECK
    private fun GameStatus.isTerminal(): Boolean =
        this == GameStatus.WHITE_WINS || this == GameStatus.BLACK_WINS ||
            this == GameStatus.DRAW || this == GameStatus.RESIGNED

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
        ): ChessGameComponent
    }
}
