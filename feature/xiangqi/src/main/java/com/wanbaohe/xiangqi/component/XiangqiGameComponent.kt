package com.wanbaohe.xiangqi.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.xiangqi.application.dto.GameDetail
import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.application.dto.GamePreparation
import com.wanbaohe.xiangqi.application.dto.engineSlotFor
import com.wanbaohe.xiangqi.application.dto.prepareFrom
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.application.port.outbound.AudioSettings
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiConfig
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiStore
import com.wanbaohe.xiangqi.application.usecase.AiOrchestrationUseCase
import com.wanbaohe.xiangqi.application.usecase.AudioFeedbackUseCase
import com.wanbaohe.xiangqi.application.usecase.ExportGameUseCase
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.application.usecase.ManageGameUseCase
import com.wanbaohe.xiangqi.application.usecase.OnlinePlayUseCase
import com.wanbaohe.xiangqi.application.usecase.PlayMoveUseCase
import com.wanbaohe.xiangqi.application.usecase.SettingsUseCase
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.data.XiangqiPlyRecord
import com.wanbaohe.xiangqi.data.local.LocalXiangqiEngine
import com.wanbaohe.xiangqi.data.local.XiangqiEngineWeights
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.BoardSetupDraft
import com.wanbaohe.xiangqi.domain.HumanAiHistory
import com.wanbaohe.xiangqi.domain.SetupPositionValidator
import com.wanbaohe.xiangqi.domain.GameReducer
import com.wanbaohe.xiangqi.domain.InteractionState
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.GameAction
import com.wanbaohe.xiangqi.domain.model.ConnectionState
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameOrigin
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.OnlineRoomConfig
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import com.wanbaohe.xiangqi.presentation.displayNames
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
    /**
     * 落库胜方（`Side.name`）。认输局必须靠它反推是谁认输：
     * [GameResultResolver.resignWinnerCode] 存的是**胜方**，认输方是其对面。
     */
    val winnerSide: String = "",
    val initialFen: String = FenCodec.INITIAL_FEN,
    val startedAt: Long = 0L,
    val isLoaded: Boolean = false,
    val isUpdating: Boolean = false,
    val redAiConfig: GameAiPlayerConfig? = null,
    val blackAiConfig: GameAiPlayerConfig? = null,
    val origin: GameOrigin? = null,
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
    private val xiangqiAiStore: XiangqiAiStore,
    private val onlinePlay: OnlinePlayUseCase,
    private val localEngine: LocalXiangqiEngine,
    private val engineWeights: XiangqiEngineWeights,
    aiEngineCatalogManager: AIEngineCatalogManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    var uiState by mutableStateOf(XiangqiGameUiState())
        private set

    var showResignConfirm by mutableStateOf(false)
    var showRestartConfirm by mutableStateOf(false)
    var showStandardGameConfirm by mutableStateOf(false)
    var showRenameDialog by mutableStateOf(false)
    var setupDraft by mutableStateOf<BoardSetupDraft?>(null)
        private set

    private var setupPreviousStatus = GameStatus.NOT_STARTED
    private var editingStartedAt = 0L
    private var setupPauseJob: Job? = null
    private var suppressAiRequests = false
    private var isVisible by mutableStateOf(lifecycle.state >= Lifecycle.State.STARTED)
    private var isNavigatingAway by mutableStateOf(false)

    val canSetup: Boolean
        get() = isVisible && !isNavigatingAway &&
            uiState.isLoaded && !uiState.isUpdating && !uiState.isAiThinking &&
            uiState.mode != GameMode.ONLINE_PVP &&
            (uiState.status.isTerminal() || uiState.status == GameStatus.NOT_STARTED ||
                (uiState.mode != GameMode.LLM_VS_LLM && isHumanTurn()))

    fun beginSetup() {
        if (!canSetup) {
            ActionUtils.showToast(R.string.xiangqi_setup_wait_for_turn)
            return
        }
        setupPreviousStatus = uiState.status
        editingStartedAt = System.currentTimeMillis()
        suppressAiRequests = true
        cancelAiRequest()
        setupDraft = BoardSetupDraft(uiState.boardState)
        setupPauseJob = componentScope.launch {
            awaitPendingMoves()
            manageGame.pause(gameId)
        }
    }

    fun updateSetup(draft: BoardSetupDraft) {
        setupDraft = draft
    }

    fun cancelSetup() {
        if (setupDraft == null || uiState.isUpdating) return
        uiState = uiState.copy(isUpdating = true)
        componentScope.launch {
            setupPauseJob?.join()
            if (setupPreviousStatus.isPlayable() && isVisible && !isNavigatingAway) {
                manageGame.resumeAfterEditing(gameId, editingStartedAt)
                if (!isVisible || isNavigatingAway) manageGame.pause(gameId)
            }
            setupDraft = null
            suppressAiRequests = !isVisible || isNavigatingAway
            uiState = uiState.copy(isUpdating = false)
        }
    }

    fun finishSetup() {
        val draft = setupDraft ?: return
        if (!draft.hasChanges) {
            cancelSetup()
            return
        }
        if (SetupPositionValidator.validate(draft.startPosition()) != null) {
            ActionUtils.showToast(R.string.xiangqi_setup_invalid)
            return
        }
        if (uiState.isUpdating) return
        uiState = uiState.copy(isUpdating = true)
        componentScope.launch {
            setupPauseJob?.join()
            val detail = gameQuery.getById(gameId)
            if (detail == null) {
                uiState = uiState.copy(isUpdating = false)
                ActionUtils.showToast(R.string.xiangqi_game_not_found)
                return@launch
            }
            if (detail.status == GameStatus.NOT_STARTED) {
                manageGame.updateInitialPosition(gameId, draft.startPosition())
                setupDraft = null
                suppressAiRequests = false
            } else {
                setupDraft = null
                onPrepareGame(detail.prepareFrom(draft.startPosition()))
            }
            uiState = uiState.copy(isUpdating = false)
        }
    }

    /** 终局结果浮层是否展示；可关闭以便就地复盘，再点结果区可重新打开 */
    var showGameOverOverlay by mutableStateOf(true)

    /** 云端失败后本地引擎接手的软提示：整局最多展示一次 */
    var localSwapNoticeShownOnce by mutableStateOf(false)
    var localSwapNoticeVisible by mutableStateOf(false)

    fun markLocalSwapNoticeShown() {
        localSwapNoticeShownOnce = true
        localSwapNoticeVisible = true
    }

    fun dismissLocalSwapNotice() {
        localSwapNoticeVisible = false
    }

    /** 本地引擎安装状态（供 AI 选择器展示下载进度 / 引导下载） */
    val localEngineInstallState: StateFlow<XiangqiEngineWeights.InstallState> = engineWeights.state
    val isLocalEnginePackaged: Boolean = localEngine.isPackaged()

    fun downloadLocalEngine() {
        engineWeights.startDownload()
    }

    fun cancelLocalEngineDownload() {
        engineWeights.cancelDownload()
    }

    fun dismissGameOverOverlay() {
        showGameOverOverlay = false
    }

    fun reopenGameOverOverlay() {
        showGameOverOverlay = true
    }

    val allAiEngines: StateFlow<List<AiEngine>> =
        aiEngineCatalogManager.observeAvailableEngines()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modelsByProvider: StateFlow<Map<String, List<AiModel>>> =
        aiEngineCatalogManager.observeModelsByProvider()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /**
     * 象棋专用走棋 AI 配置（每槽位的来源）。
     *
     * ⚠️ 必须声明在 [init] 之前，并由 [collectAiConfig] 真正订阅：
     * `stateIn(WhileSubscribed)` 没有下游时会一直停在初值 [XiangqiAiConfig]
     * （即默认 Pikafish），只读 `.value` 拿到的是过期默认值而不是用户的选择。
     */
    val currentAiConfig: StateFlow<XiangqiAiConfig> = xiangqiAiStore.observe()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), XiangqiAiConfig())

    private var aiRequestJob: Job? = null
    private var aiCleanupJob: Job? = null
    private var moveCommitJob: Deferred<PlayMoveUseCase.Result>? = null
    private var lastRequestedFen: String? = null
    private var audioSettings: AudioSettings = AudioSettings()
    private var onlineMovesObserved = false

    init {
        lifecycle.doOnStart {
            isVisible = true
            isNavigatingAway = false
            requestAiMove()
        }
        lifecycle.doOnStop {
            isVisible = false
            suppressAiRequests = true
            cancelAiRequest()
            if (uiState.isLoaded && uiState.mode != GameMode.ONLINE_PVP) {
                componentScope.launch {
                    awaitPendingMoves()
                    manageGame.pause(gameId)?.let(::applyPosition)
                }
            }
        }
        collectSettings()
        collectAiEngines()
        collectAiConfig()
        observeGame()
    }

    fun onCellTap(file: Int, rank: Int) {
        if (!isVisible || isNavigatingAway) return
        if (!uiState.isLoaded || uiState.isAiThinking || uiState.isUpdating || setupDraft != null) return
        if (!uiState.status.isPlayable()) return
        if (!isLocalOnlineTurn()) return
        if (!isHumanTurn()) return

        val boardBefore = uiState.boardState
        val next = GameReducer.reduce(
            boardState = boardBefore,
            legalMoves = uiState.legalMoves,
            previous = uiState.interaction,
            action = GameAction.TapCell(BoardPoint(file, rank)),
        )
        uiState = uiState.copy(interaction = next)

        next.pendingMove?.let { pending ->
            uiState = uiState.copy(isUpdating = true)
            val commit = componentScope.async(start = CoroutineStart.LAZY) {
                playMove.commit(gameId, pending)
            }
            moveCommitJob = commit
            componentScope.launch {
                val result = try {
                    commit.await()
                } finally {
                    if (moveCommitJob === commit) moveCommitJob = null
                }
                when (result) {
                    is PlayMoveUseCase.Result.Success -> {
                        applyPosition(result.detail)
                        playSound(boardBefore, pending)
                        if (uiState.mode == GameMode.ONLINE_PVP) {
                            onlinePlay.sendMove(pending)
                        }
                        uiState = uiState.copy(interaction = InteractionState(), isUpdating = false)
                        requestAiMove()
                    }
                    is PlayMoveUseCase.Result.Rejected -> {
                        uiState = uiState.copy(interaction = InteractionState(), isUpdating = false)
                    }
                }
            }
        }
    }

    fun undo() {
        if (!canUndo) return
        val targetPly = uiState.currentPly - undoSteps()
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true, interaction = InteractionState())
        componentScope.launch {
            awaitPendingMoves()
            val current = gameQuery.getById(gameId)
            finishHistoryChange(
                current?.let { manageGame.undo(gameId, (it.currentPly - targetPly).coerceAtLeast(0)) },
            )
        }
    }

    fun redo() {
        if (!canRedo) return
        val targetPly = uiState.currentPly + redoSteps()
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true, interaction = InteractionState())
        componentScope.launch {
            awaitPendingMoves()
            val current = gameQuery.getById(gameId)
            finishHistoryChange(
                current?.let { manageGame.redo(gameId, (targetPly - it.currentPly).coerceAtLeast(0)) },
            )
        }
    }

    private fun finishHistoryChange(detail: GameDetail?) {
        suppressAiRequests = !isVisible || isNavigatingAway
        if (detail == null) {
            uiState = uiState.copy(isUpdating = false)
            ActionUtils.showToast(R.string.xiangqi_game_not_found)
            return
        }
        applyPosition(detail)
        uiState = uiState.copy(isUpdating = false, errorMessage = "")
        requestAiMove()
    }

    private fun applyPosition(detail: GameDetail) {
        val board = FenCodec.parse(detail.currentFen)
        uiState = uiState.copy(
            boardState = board,
            legalMoves = GameArbiter.legalMoves(board),
            currentPly = detail.currentPly,
            history = detail.plies,
            status = detail.status,
            winnerSide = detail.winnerSide,
        )
    }

    val canUndo: Boolean get() = isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isUpdating &&
        uiState.mode != GameMode.ONLINE_PVP && setupDraft == null && undoSteps() > 0
    val canRedo: Boolean get() = isVisible && !isNavigatingAway && uiState.isLoaded && !uiState.isUpdating &&
        uiState.mode != GameMode.ONLINE_PVP && setupDraft == null && redoSteps() > 0

    private fun humanSide(): Side = if (uiState.redPlayerType == PlayerType.HUMAN) Side.RED else Side.BLACK
    private fun isHumanTurn(): Boolean = when (uiState.boardState.sideToMove) {
        Side.RED -> uiState.redPlayerType == PlayerType.HUMAN
        Side.BLACK -> uiState.blackPlayerType == PlayerType.HUMAN
    }

    private fun undoSteps(): Int = if (uiState.mode == GameMode.HUMAN_VS_LLM) {
        HumanAiHistory.undoSteps(uiState.history.map { it.moverSide }, uiState.currentPly, humanSide())
    } else if (uiState.currentPly > 0) 1 else 0

    private fun redoSteps(): Int = if (uiState.mode == GameMode.HUMAN_VS_LLM) {
        HumanAiHistory.redoSteps(uiState.history.map { it.moverSide }, uiState.currentPly, humanSide())
    } else if (uiState.currentPly < uiState.history.size) 1 else 0

    fun restart() {
        if (uiState.mode != GameMode.ONLINE_PVP) suppressAiRequests = true
        cancelAiRequest()
        componentScope.launch {
            awaitPendingMoves()
            if (uiState.mode == GameMode.ONLINE_PVP) {
                val detail = manageGame.restart(gameId) ?: return@launch
                if (detail.id != gameId) onNavigate(Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Game(detail.id)))
            } else {
                manageGame.pause(gameId)
                val detail = gameQuery.getById(gameId)
                if (detail == null) {
                    ActionUtils.showToast(R.string.xiangqi_game_not_found)
                    return@launch
                }
                onPrepareGame(
                    detail.prepareFrom(FenCodec.parse(detail.initialFen))
                        .copy(title = detail.title, origin = detail.origin),
                )
            }
        }
        showRestartConfirm = false
    }

    fun start() {
        if (!isVisible || isNavigatingAway || !uiState.isLoaded || uiState.isUpdating) return
        uiState = uiState.copy(isUpdating = true)
        withAiAccess(onFailure = { uiState = uiState.copy(isUpdating = false) }) {
            componentScope.launch {
                awaitPendingMoves()
                if (!isVisible || isNavigatingAway) {
                    uiState = uiState.copy(isUpdating = false)
                    return@launch
                }
                suppressAiRequests = false
                if (uiState.mode == GameMode.ONLINE_PVP) onlinePlay.sendStart()
                val detail = manageGame.start(gameId)
                if ((!isVisible || isNavigatingAway) && uiState.mode != GameMode.ONLINE_PVP) {
                    manageGame.pause(gameId)
                } else if (detail != null) {
                    applyPosition(detail)
                } else {
                    ActionUtils.showToast(R.string.xiangqi_game_not_found)
                }
                uiState = uiState.copy(isUpdating = false)
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

    fun exportText(labels: com.wanbaohe.xiangqi.data.TextExportLabels, resultText: String) {
        componentScope.launch {
            uiState = uiState.copy(
                exportContent = exportGame.asText(gameId, labels, resultText),
            )
        }
    }

    fun dismissExport() { uiState = uiState.copy(exportContent = "") }
    fun dismissError() { uiState = uiState.copy(errorMessage = "") }

    fun retryAiMove() {
        if (!uiState.status.isPlayable() || isHumanTurn() || setupDraft != null || uiState.isUpdating) return
        cancelAiRequest()
        withAiAccess { requestAiMove() }
    }

    fun switchAiModelForSide(side: Side, engine: AiEngine, model: AiModel) {
        ActionUtils.ensureLoginAndCheckPoints(
            source = "xiangqi_switch_model",
            point = XiangqiAiSource.WorkingModel.startPoints,
            onSuccess = {
                when (uiState.mode) {
                    GameMode.LLM_VS_LLM -> {
                        if (side == Side.RED) aiEngineManager.setDuelEngineA(engine.copy(model = model))
                        else aiEngineManager.setDuelEngineB(engine.copy(model = model))
                    }
                    GameMode.HUMAN_VS_LLM -> aiEngineManager.switchFastModel(engine, model)
                    GameMode.LOCAL_PVP, GameMode.ONLINE_PVP -> Unit
                }
                saveAiConfig(side, GameAiPlayerConfig.capture(XiangqiAiSource.WorkingModel, engine.copy(model = model)))
            },
        )
    }

    fun switchAiSourceForSide(side: Side, source: XiangqiAiSource) {
        if (source == currentSourceForSide(side)) return
        if (source.requiresLogin) {
            ActionUtils.ensureLoginAndCheckPoints(
                source = "xiangqi_switch_ai",
                point = source.startPoints,
                onSuccess = { saveAiConfig(side, GameAiPlayerConfig.capture(source, currentEngineForSide(side))) },
            )
        } else {
            saveAiConfig(side, GameAiPlayerConfig.capture(source, currentEngineForSide(side)))
        }
    }

    private fun saveAiConfig(side: Side, config: GameAiPlayerConfig) {
        suppressAiRequests = true
        cancelAiRequest()
        uiState = uiState.copy(isUpdating = true)
        componentScope.launch {
            awaitPendingMoves()
            manageGame.updateAiConfig(gameId, side, config)
            xiangqiAiStore.update(xiangqiAiStore.get().withSource(uiState.mode.engineSlotFor(side), config.source))
            uiState = if (side == Side.RED) uiState.copy(redAiConfig = config, isUpdating = false, errorMessage = "")
                else uiState.copy(blackAiConfig = config, isUpdating = false, errorMessage = "")
            refreshAiDisplay()
            suppressAiRequests = !isVisible || isNavigatingAway
            requestAiMove()
        }
    }

    fun currentSourceForSide(side: Side): XiangqiAiSource {
        val config = if (side == Side.RED) uiState.redAiConfig else uiState.blackAiConfig
        return config?.source ?: currentAiConfig.value.sourceFor(uiState.mode.engineSlotFor(side))
    }

    val currentAIEngine: StateFlow<AiEngine> = aiEngineManager.fastAIEngine

    fun currentEngineForSide(side: Side): AiEngine = when (uiState.mode) {
        GameMode.LLM_VS_LLM ->
            if (side == Side.RED) aiEngineManager.getDuelEngineA() else aiEngineManager.getDuelEngineB()
        else -> aiEngineManager.getFastAiEngine()
    }

    fun openAnalysis() {
        onNavigate(Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Analysis(gameId, uiState.currentPly)))
    }

    fun pauseBeforeNavigating(onPaused: () -> Unit) {
        isNavigatingAway = true
        suppressAiRequests = true
        cancelAiRequest()
        componentScope.launch {
            awaitPendingMoves()
            if (uiState.mode != GameMode.ONLINE_PVP) {
                manageGame.pause(gameId)?.let(::applyPosition)
            }
            onPaused()
        }
    }

    fun openSourceGame() {
        val origin = uiState.origin ?: return
        componentScope.launch {
            if (gameQuery.getById(origin.gameId) == null) {
                ActionUtils.showToast(R.string.xiangqi_game_missing)
            } else {
                onNavigate(Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Game(origin.gameId)))
            }
        }
    }

    fun prepareStandardGame() {
        showStandardGameConfirm = false
        if (uiState.mode == GameMode.ONLINE_PVP) return
        suppressAiRequests = true
        cancelAiRequest()
        componentScope.launch {
            awaitPendingMoves()
            manageGame.pause(gameId)
            val detail = gameQuery.getById(gameId)
            if (detail == null) {
                ActionUtils.showToast(R.string.xiangqi_game_not_found)
                return@launch
            }
            onPrepareGame(detail.prepareFrom(FenCodec.parse(FenCodec.INITIAL_FEN)).copy(origin = null))
        }
    }

    private fun withAiAccess(onFailure: () -> Unit = {}, onSuccess: () -> Unit) {
        val sources = buildList {
            if (uiState.redPlayerType == PlayerType.LLM) add(currentSourceForSide(Side.RED))
            if (uiState.blackPlayerType == PlayerType.LLM) add(currentSourceForSide(Side.BLACK))
        }
        if (sources.any { it == XiangqiAiSource.LocalEngine } &&
            (!isLocalEnginePackaged || localEngineInstallState.value !is XiangqiEngineWeights.InstallState.Installed)
        ) {
            ActionUtils.showToast(R.string.xiangqi_setup_local_engine_required)
            onFailure()
            return
        }
        if (sources.any { it.requiresLogin }) {
            ActionUtils.ensureLoginAndCheckPoints(
                source = "xiangqi_start",
                point = sources.maxOf { it.startPoints },
                onLoginFailure = {
                    ActionUtils.showToast(com.shifenmiao.core.R.string.login_failed)
                    onFailure()
                },
                onPointsFailure = onFailure,
                onSuccess = onSuccess,
            )
        } else {
            onSuccess()
        }
    }

    fun resign() {
        if (uiState.mode == GameMode.ONLINE_PVP) {
            onlinePlay.sendResign()
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
            manageGame.resign(gameId, resigningSide)
        }
        showResignConfirm = false
    }

    fun renameGame(newTitle: String) {
        componentScope.launch { manageGame.rename(gameId, newTitle) }
        showRenameDialog = false
    }

    /* ─────────── private ─────────── */

    private fun observeGame() {
        componentScope.launch {
            val existing = gameQuery.getById(gameId)
            if (existing != null) {
                val defaults = xiangqiAiStore.get()
                for (side in Side.entries) {
                    val playerType = if (side == Side.RED) existing.redPlayerType else existing.blackPlayerType
                    val config = if (side == Side.RED) existing.redAiConfig else existing.blackAiConfig
                    if (playerType == PlayerType.LLM && config == null) {
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
            if (startImmediately && !suppressAiRequests) manageGame.start(gameId) else manageGame.pause(gameId)
            if (suppressAiRequests && existing?.mode != GameMode.ONLINE_PVP) manageGame.pause(gameId)
            gameQuery.observeById(gameId).collect { detail ->
                if (detail == null) {
                    cancelAiRequest()
                    uiState = uiState.copy(isLoaded = false, errorMessage = AppContext.getString(R.string.xiangqi_game_not_found))
                    return@collect
                }
                val boardState = FenCodec.parse(detail.currentFen)
                val legalMoves = GameArbiter.legalMoves(boardState)
                val redInfo = resolveAiDisplay(detail.mode, detail.redPlayerType, Side.RED, detail.redAiConfig)
                val blackInfo = resolveAiDisplay(detail.mode, detail.blackPlayerType, Side.BLACK, detail.blackAiConfig)

                val previousStatus = uiState.status
                val becameTerminal = previousStatus.isPlayable() && detail.status.isTerminal()
                val becamePlayable = detail.status.isPlayable()
                if (becameTerminal) showGameOverOverlay = true
                if (becamePlayable) {
                    showGameOverOverlay = true
                    // 新的一局/回到可下状态后允许再提示一次本地引擎接手
                    if (!previousStatus.isPlayable()) {
                        localSwapNoticeShownOnce = false
                        localSwapNoticeVisible = false
                    }
                }

                uiState = uiState.copy(
                    title = detail.title,
                    boardState = boardState,
                    legalMoves = legalMoves,
                    history = detail.plies,
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
                    winnerSide = detail.winnerSide,
                    initialFen = detail.initialFen,
                    startedAt = detail.startedAt,
                    isLoaded = true,
                    redAiConfig = detail.redAiConfig,
                    blackAiConfig = detail.blackAiConfig,
                    origin = detail.origin,
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
        if (!isVisible || isNavigatingAway || suppressAiRequests || setupDraft != null ||
            !uiState.isLoaded || !uiState.status.isPlayable() || isHumanTurn() ||
            aiRequestJob?.isActive == true
        ) return
        val currentFen = FenCodec.encode(uiState.boardState)
        if (lastRequestedFen == currentFen) return
        lastRequestedFen = currentFen
        uiState = uiState.copy(isAiThinking = true, errorMessage = "")

        val job = componentScope.launch(start = CoroutineStart.LAZY) {
            val requestJob = currentCoroutineContext()[Job]
            awaitPendingMoves()
            currentCoroutineContext().ensureActive()
            val slot = when (uiState.mode) {
                GameMode.LLM_VS_LLM ->
                    if (uiState.boardState.sideToMove == Side.RED) EngineSlot.DUEL_A else EngineSlot.DUEL_B
                else -> EngineSlot.FAST
            }
            var continueAi = false
            try {
                when (val outcome = aiOrchestration.requestMove(gameId, slot)) {
                    is AiOrchestrationUseCase.Outcome.Committed -> {
                        applyPosition(outcome.detail)
                        val lastPly = outcome.detail.plies.firstOrNull { it.ply == outcome.detail.currentPly }
                        if (lastPly != null) {
                            audioFeedback.playForMove(lastPly.beforeFen, lastPly.afterFen, audioSettings)
                        }
                        continueAi = true
                    }
                    is AiOrchestrationUseCase.Outcome.Stale -> {
                        continueAi = outcome.reason == AiOrchestrationUseCase.StaleReason.POSITION_CHANGED
                        if (!continueAi) uiState = uiState.copy(errorMessage = "AI_ERROR")
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
                if (latest != null && shouldRequestAi(latest, FenCodec.parse(latest.currentFen))) {
                    applyPosition(latest)
                    requestAiMove()
                }
            }
        }
        aiRequestJob = job
        job.start()
    }

    private fun shouldRequestAi(detail: GameDetail, boardState: com.wanbaohe.xiangqi.domain.model.BoardState): Boolean {
        val currentFen = FenCodec.encode(boardState)
        val playable = detail.status.isPlayable()
        return isVisible && !isNavigatingAway && !suppressAiRequests && setupDraft == null &&
            aiRequestJob?.isActive != true &&
            playable && isCurrentSideAi(detail) && lastRequestedFen != currentFen
    }

    private fun isCurrentSideAi(detail: GameDetail): Boolean = when (
        FenCodec.parse(detail.currentFen).sideToMove
    ) {
        Side.RED -> detail.redPlayerType == PlayerType.LLM
        Side.BLACK -> detail.blackPlayerType == PlayerType.LLM
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
        mode: GameMode,
        playerType: PlayerType,
        side: Side,
        config: GameAiPlayerConfig? = if (side == Side.RED) uiState.redAiConfig else uiState.blackAiConfig,
    ): Pair<String, String> {
        if (playerType != PlayerType.LLM) return "" to ""
        return (config ?: GameAiPlayerConfig.capture(
            currentAiConfig.value.sourceFor(mode.engineSlotFor(side)),
            currentEngineForSide(side),
        )).displayNames()
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

    /**
     * 订阅象棋 AI 来源配置。
     *
     * 除了让 [currentAiConfig] 保持最新（[currentSourceForSide] 读它的 `.value`），
     * 还要在设置页 / 对局内选择器改动后刷新顶栏展示，否则界面会一直停在默认 Pikafish。
     */
    private fun collectAiConfig() {
        componentScope.launch {
            currentAiConfig.collect { refreshAiDisplay() }
        }
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

    private fun GameStatus.isTerminal(): Boolean = this == GameStatus.RED_WINS ||
        this == GameStatus.BLACK_WINS ||
        this == GameStatus.DRAW ||
        this == GameStatus.RESIGNED

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
        ): XiangqiGameComponent
    }
}
