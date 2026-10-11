package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ImmersiveModeState
import com.wanbaohe.xiangqi.ui.XiangqiOpponentPicker
import com.shifenmiao.common.utils.BaseUtils
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalImageShareProvider
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.xiangqi.BuildConfig
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.component.XiangqiGameComponent
import com.wanbaohe.xiangqi.component.XiangqiGameUiState
import com.wanbaohe.xiangqi.data.TextExportLabels
import com.wanbaohe.xiangqi.data.XiangqiPlyRecord
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.presentation.localizedGameResultText
import com.wanbaohe.xiangqi.presentation.localizedResignResultText
import com.wanbaohe.xiangqi.presentation.resigningSideOf
import com.wanbaohe.xiangqi.router.LocalXiangqiImmersiveModeState
import com.wanbaohe.xiangqi.ui.board.XiangqiBoard
import com.wanbaohe.xiangqi.ui.board.xiangqiBoardAspectRatio
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAnalytics
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import com.wanbaohe.boardgame.ui.BoardGameOverOverlay
import com.wanbaohe.boardgame.ui.BoardStartOverlay
import com.wanbaohe.boardgame.model.BoardGameAction
import com.wanbaohe.boardgame.model.PlayerBarData
import com.wanbaohe.boardgame.ui.ActionBar as BoardGameActionBar
import com.wanbaohe.boardgame.ui.PlayersBar as BoardGamePlayersBar
import com.wanbaohe.boardgame.ui.BoardGameToolButton
import com.wanbaohe.boardgame.R as BoardGameR
import kotlinx.coroutines.delay
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedDropdownMenu

@Composable
fun XiangqiGameScreen(
    component: XiangqiGameComponent,
    modifier: Modifier = Modifier,
    showChrome: Boolean = true,
) {
    val state = component.uiState
    val setup = component.setupDraft
    if (setup != null) {
        val preparingInitial = state.status == GameStatus.NOT_STARTED && state.startedAt == 0L
        XiangqiSetupScreen(
            draft = setup,
            onDraftChange = component::updateSetup,
            onCancel = component::cancelSetup,
            onConfirm = component::finishSetup,
            modifier = modifier,
            busy = state.isUpdating,
            preservesOriginal = !preparingInitial,
        )
        return
    }
    var exportDialog by remember { mutableStateOf(false) }
    var showOrigin by remember { mutableStateOf(false) }
    var pickingSide by remember { mutableStateOf<Side?>(null) }
    val exportLabels = TextExportLabels(
        header = stringResource(R.string.xiangqi_export_header),
        titleLabel = stringResource(R.string.xiangqi_export_title_label),
        initialFenLabel = stringResource(R.string.xiangqi_export_initial_fen_label),
        resultLabel = stringResource(R.string.xiangqi_export_result_label),
    )
    val localizedResultText = localizedGameResultText(state.status)

    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()
    val shareProvider = LocalImageShareProvider.current
    var isCapturing by remember { mutableStateOf(false) }

    fun shareScreenshot() {
        scope.launch {
            isCapturing = true
            runCatching {
                val bitmap = captureController.bitmap()
                val imageInfo = ImageInfo(
                    width = bitmap.width,
                    height = bitmap.height,
                    imageFormat = ImageFormat.Png.Lossless,
                    originalUri = "xiangqi_screenshot",
                )
                shareProvider.shareImage(imageInfo, bitmap) {
                    isCapturing = false
                }
            }.onFailure {
                isCapturing = false
            }
        }
    }

    val content: @Composable (Modifier) -> Unit = { contentModifier ->
        XiangqiGameContent(
            component = component,
            modifier = contentModifier,
            captureController = captureController,
            onExport = { exportDialog = true },
            onPickAiFor = { side -> pickingSide = side },
            onShowOrigin = { showOrigin = true },
        )
    }

    if (showChrome) {
        BaseScreen(
            title = state.title.ifBlank { stringResource(R.string.xiangqi_game_title) },
            onGoBack = component.onGoBack,
        ) {
            content(Modifier.fillMaxSize())
        }
    } else {
        content(modifier)
    }

    if (exportDialog) {
        ExportDialog(
            exportContent = state.exportContent,
            onExportFen = component::exportFen,
            onExportJson = component::exportJson,
            // 结果文案在 UI 层本地化：导出层不依赖 Android 资源
            onExportText = { component.exportText(exportLabels, localizedResultText) },
            onDismiss = {
                exportDialog = false
                component.dismissExport()
            },
            onShareImage = ::shareScreenshot,
            isSharingImage = isCapturing,
        )
    }

    // Resign confirmation dialog
    if (component.showResignConfirm) {
        AlertDialog(
            onDismissRequest = { component.showResignConfirm = false },
            title = { Text(stringResource(R.string.xiangqi_resign_confirm_title)) },
            text = { Text(stringResource(R.string.xiangqi_resign_confirm_message)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = component::resign) {
                    Text(stringResource(R.string.xiangqi_confirm))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { component.showResignConfirm = false }) {
                    Text(stringResource(R.string.xiangqi_cancel))
                }
            },
        )
    }

    // 重新开局确认
    if (component.showRestartConfirm) {
        AlertDialog(
            onDismissRequest = { component.showRestartConfirm = false },
            title = { Text(stringResource(R.string.xiangqi_restart_confirm_title)) },
            text = { Text(stringResource(R.string.xiangqi_restart_confirm_message)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = component::restart) {
                    Text(stringResource(R.string.xiangqi_confirm))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { component.showRestartConfirm = false }) {
                    Text(stringResource(R.string.xiangqi_cancel))
                }
            },
        )
    }

    if (component.showStandardGameConfirm) {
        AlertDialog(
            onDismissRequest = { component.showStandardGameConfirm = false },
            title = { Text(stringResource(R.string.xiangqi_standard_game)) },
            text = { Text(stringResource(R.string.xiangqi_standard_game_confirm)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = component::prepareStandardGame) {
                    Text(stringResource(R.string.xiangqi_confirm))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { component.showStandardGameConfirm = false }) {
                    Text(stringResource(R.string.xiangqi_cancel))
                }
            },
        )
    }
    val origin = state.origin
    if (showOrigin && origin != null) {
        XiangqiOriginDialog(
            origin = origin,
            onContinueSource = { showOrigin = false; component.openSourceGame() },
            onDismiss = { showOrigin = false },
        )
    }

    // Rename dialog
    if (component.showRenameDialog) {
        var renameText by remember { mutableStateOf(state.title) }
        AlertDialog(
            onDismissRequest = { component.showRenameDialog = false },
            title = { Text(stringResource(R.string.xiangqi_rename_game)) },
            text = {
                GlassOutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.xiangqi_rename_hint)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { component.renameGame(renameText) },
                    enabled = renameText.isNotBlank(),
                ) {
                    Text(stringResource(R.string.xiangqi_confirm))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { component.showRenameDialog = false }) {
                    Text(stringResource(R.string.xiangqi_cancel))
                }
            },
        )
    }

    val pickingSideValue = pickingSide
    if (pickingSideValue != null) {
        val currentSource = component.currentSourceForSide(pickingSideValue)
        val workingModel by component.currentAIEngine.collectAsState()
        val localEngineState by component.localEngineInstallState.collectAsState()
        val engines by component.allAiEngines.collectAsState()
        val models by component.modelsByProvider.collectAsState()
        val storedConfig = if (pickingSideValue == Side.RED) state.redAiConfig else state.blackAiConfig
        XiangqiOpponentPicker(
            config = storedConfig ?: GameAiPlayerConfig.capture(currentSource, component.currentEngineForSide(pickingSideValue)),
            workingEngine = if (state.mode == GameMode.LLM_VS_LLM) component.currentEngineForSide(pickingSideValue) else workingModel,
            allEngines = engines,
            modelsByProvider = models,
            localEnginePackaged = component.isLocalEnginePackaged,
            localEngineState = localEngineState,
            onSourceSelected = { source ->
                component.switchAiSourceForSide(pickingSideValue, source)
            },
            onModelSelected = { engine, model -> component.switchAiModelForSide(pickingSideValue, engine, model) },
            onDismiss = { pickingSide = null },
            onDownloadLocalEngine = component::downloadLocalEngine,
            onCancelLocalEngineDownload = component::cancelLocalEngineDownload,
        )
    }
}

@Composable
private fun XiangqiGameContent(
    component: XiangqiGameComponent,
    modifier: Modifier,
    captureController: com.t8rin.imagetoolbox.core.ui.utils.capturable.CaptureController,
    onExport: () -> Unit,
    onPickAiFor: (Side) -> Unit,
    onShowOrigin: () -> Unit,
) {
    val state = component.uiState
    val immersiveState = LocalXiangqiImmersiveModeState.current

    val playable = state.status == GameStatus.PLAYING || state.status == GameStatus.CHECK
    val bottomSide = if (state.mode == GameMode.ONLINE_PVP) state.onlineMySide else Side.RED
    // 当前局面的最后一手(撤销后随之回退),棋盘上用以明示上一步
    val lastMove = state.history.getOrNull(state.currentPly - 1)?.let { parseUcciMove(it.moveUcci) }
    // AI 兜底 ("AI_FALLBACK") 已经合法落子完成,不必再多一张提示卡;
    // 只对真错 (AI_ERROR / 自定义错误文案) 显示并提供重试入口。
    val showErrorCard = state.errorMessage.isNotBlank() && state.errorMessage != "AI_FALLBACK"
    // 云端失败后本地引擎接手:棋力正常,每局只软提示一次,不要红色「引擎不可用」。
    // 浅层启发式才是真兜底,单独一张警示卡并引导下载本地引擎。
    val fallbackPly = state.history.lastOrNull {
        MoveDecision.isLocalFallback(it.aiReason)
    }?.takeIf { it.ply == state.currentPly }
    // 云端失败后本地引擎接手：整局最多软提示一次，避免每手都弹
    val swapCandidatePly = state.history.lastOrNull {
        MoveDecision.isLocalEngineSwap(it.aiReason)
    }?.takeIf { it.ply == state.currentPly }
    androidx.compose.runtime.LaunchedEffect(swapCandidatePly?.ply) {
        if (swapCandidatePly != null && !component.localSwapNoticeShownOnce) {
            component.markLocalSwapNoticeShown()
        }
    }
    val localSwapPly = swapCandidatePly?.takeIf { component.localSwapNoticeVisible }
    val showDebugPanel = BuildConfig.DEBUG && state.mode == GameMode.ONLINE_PVP

    BoxWithConstraints(modifier = modifier) {
        val statusDetailsHeight = minOf(180.dp, maxHeight / 3)
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.weight(1f).capturable(captureController),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                XiangqiPlayersRow(
                    state = state,
                    onPickAiFor = onPickAiFor,
                    onShowOrigin = onShowOrigin,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                )
                GameBoardArea(
                    component = component,
                    state = state,
                    bottomSide = bottomSide,
                    lastMove = lastMove,
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
                )
            }
            GameActionBar(component, state, immersiveState, onExport, playable, onShowOrigin)
            if (showErrorCard || showDebugPanel || localSwapPly != null || fallbackPly != null) {
                // Status details can scroll; board controls and the outer tabs stay visible.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = statusDetailsHeight)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (showErrorCard) ErrorStatusCard(component, state, onPickAiFor)
                    if (showDebugPanel) OnlineDebugPanel(state = state)
                    if (localSwapPly != null) {
                        LocalEngineSwapCard(
                            state = state,
                            swapPly = localSwapPly,
                            onDismiss = component::dismissLocalSwapNotice,
                            onPickAiFor = onPickAiFor,
                        )
                    } else if (fallbackPly != null) {
                        FallbackStatusCard(state, fallbackPly, onPickAiFor)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameBoardArea(
    component: XiangqiGameComponent,
    state: XiangqiGameUiState,
    bottomSide: Side,
    lastMove: Pair<BoardPoint, BoardPoint>?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val boardAspectRatio = xiangqiBoardAspectRatio(maxWidth.value, maxHeight.value)
        // 内层 Box 随棋盘实际大小,遮罩层(matchParentSize)只盖住棋盘
        Box(contentAlignment = Alignment.Center) {
            XiangqiBoard(
                boardState = state.boardState,
                selectedPoint = state.interaction.selectedPoint,
                candidateTargets = state.interaction.candidateTargets,
                onCellTap = component::onCellTap,
                bottomSide = bottomSide,
                riverNotice = if (state.status == GameStatus.CHECK) stringResource(R.string.xiangqi_check) else "",
                lastMove = lastMove,
                boardAspectRatio = boardAspectRatio,
            )
            if (state.isLoaded && (state.status == GameStatus.NOT_STARTED || state.status == GameStatus.PAUSED)) {
                BoardStartOverlay(
                    startLabel = stringResource(
                        if (state.status == GameStatus.PAUSED) {
                            R.string.xiangqi_resume_action
                        } else {
                            R.string.xiangqi_start_action
                        }
                    ),
                    onStart = component::start,
                )
            }
            val isGameOver = state.status == GameStatus.RED_WINS ||
                state.status == GameStatus.BLACK_WINS ||
                state.status == GameStatus.DRAW ||
                state.status == GameStatus.RESIGNED
            if (isGameOver && component.showGameOverOverlay) {
                BoardGameOverOverlay(
                    resultTitle = when (state.status) {
                        GameStatus.RED_WINS -> stringResource(R.string.xiangqi_game_over_red)
                        GameStatus.BLACK_WINS -> stringResource(R.string.xiangqi_game_over_black)
                        GameStatus.DRAW -> stringResource(R.string.xiangqi_game_over_draw)
                        else -> localizedResignResultText(state.winnerSide)
                    },
                    restartLabel = stringResource(R.string.xiangqi_game_over_restart),
                    reviewLabel = stringResource(R.string.xiangqi_game_over_review),
                    backLabel = stringResource(R.string.xiangqi_game_over_back),
                    onRestart = component::restart,
                    onReview = component::openAnalysis,
                    onBack = component.onGoBack,
                    dismissLabel = stringResource(R.string.xiangqi_game_over_dismiss),
                    onDismiss = component::dismissGameOverOverlay,
                    emphasizeResult = state.status != GameStatus.DRAW,
                )
            } else if (isGameOver) {
                // 浮层关掉后：结果落在棋盘中线（与「将军」同位置），认输带旗标 + 方色
                CenterResultNotice(
                    status = state.status,
                    winnerSide = state.winnerSide,
                    onClick = component::reopenGameOverOverlay,
                )
            }
        }
    }
}

/**
 * 终局结果徽章：与「将军」同位置的中线胶囊。
 * 认输局带旗帜图标 + 认输方色点，点按可重新打开结果浮层。
 */
@Composable
private fun BoxScope.CenterResultNotice(
    status: GameStatus,
    winnerSide: String,
    onClick: () -> Unit,
) {
    val resigning = resigningSideOf(winnerSide)
    val label = when (status) {
        GameStatus.RESIGNED -> localizedResignResultText(winnerSide)
        GameStatus.RED_WINS -> stringResource(R.string.xiangqi_game_over_red)
        GameStatus.BLACK_WINS -> stringResource(R.string.xiangqi_game_over_black)
        GameStatus.DRAW -> stringResource(R.string.xiangqi_game_over_draw)
        else -> return
    }
    val isResign = status == GameStatus.RESIGNED
    val accent = when {
        resigning == Side.RED -> Color(0xFFC62828)
        resigning == Side.BLACK -> Color(0xFF37474F)
        status == GameStatus.RED_WINS -> Color(0xFFC62828)
        status == GameStatus.BLACK_WINS -> Color(0xFF37474F)
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = Modifier
            .matchParentSize()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = accent.copy(alpha = 0.16f),
            contentColor = accent,
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isResign) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = accent,
                    )
                    if (resigning != null) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (resigning == Side.RED) Color(0xFFC62828) else Color(0xFF37474F)
                                ),
                        )
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = accent,
                )
            }
        }
    }
}

@Composable
private fun GameActionBar(
    component: XiangqiGameComponent,
    state: XiangqiGameUiState,
    immersiveState: ImmersiveModeState?,
    onExport: () -> Unit,
    playable: Boolean,
    onShowOrigin: () -> Unit,
) {
    ActionBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onUndo = component::undo,
        onRedo = component::redo,
        onAnalysis = component::openAnalysis,
        onExport = onExport,
        onRestart = { component.showRestartConfirm = true },
        onResign = { component.showResignConfirm = true },
        onRename = { component.showRenameDialog = true },
        onToggleFullscreen = { immersiveState?.toggle() },
        isImmersive = immersiveState?.isImmersive == true,
        fullscreenEnabled = immersiveState != null,
        undoEnabled = component.canUndo,
        redoEnabled = component.canRedo,
        onSetup = component::beginSetup,
        setupEnabled = component.canSetup,
        onStandardGame = { component.showStandardGameConfirm = true },
        showStandardGame = state.mode != GameMode.ONLINE_PVP,
        onShowOrigin = onShowOrigin.takeIf { state.origin != null },
        enabled = state.isLoaded && !state.isUpdating,
        showResign = playable && state.mode != GameMode.LLM_VS_LLM,
    )
}

@Composable
private fun ErrorStatusCard(
    component: XiangqiGameComponent,
    state: XiangqiGameUiState,
    onPickAiFor: (Side) -> Unit,
) {
    val aiTurn = state.isLoaded && !state.isUpdating &&
        state.playerTypeFor(state.boardState.sideToMove) == PlayerType.LLM
    StatusCard(
        title = stringResource(if (state.isLoaded) R.string.xiangqi_ai_failed else R.string.xiangqi_game_not_found),
        subtitle = when (state.errorMessage) {
            "AI_ERROR" -> stringResource(R.string.xiangqi_ai_failed)
            else -> state.errorMessage
        },
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GlassTonalButton(
                    onClick = component::retryAiMove,
                    enabled = aiTurn,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.xiangqi_retry_ai), maxLines = 1)
                }
                GlassTonalButton(
                    // 与象棋设置共用 XiangqiAiPicker, 不跳全局 AI 设置
                    onClick = { onPickAiFor(state.boardState.sideToMove) },
                    enabled = aiTurn,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.xiangqi_switch_ai_model), maxLines = 1)
                }
                GlassTonalButton(
                    onClick = component::dismissError,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.xiangqi_close), maxLines = 1)
                }
            }
        },
    )
}

@Composable
private fun FallbackStatusCard(
    state: XiangqiGameUiState,
    fallbackPly: XiangqiPlyRecord,
    onPickAiFor: (Side) -> Unit,
) {
    // 用落库的 moverSide 判定行棋方最可靠(不依赖当前轮到谁)
    val reason = MoveDecision.stripMarkers(fallbackPly.aiReason).ifBlank { "unknown" }
    StatusCard(
        title = stringResource(R.string.xiangqi_ai_local_fallback_title),
        subtitle = stringResource(R.string.xiangqi_ai_local_fallback_message, reason),
        height = FallbackCardHeight,
        // AI 对战下两个座位都是引擎,只给一个"切换模型"入口会让人误解是哪个
        titleTrailing = if (state.mode != GameMode.LLM_VS_LLM) {
            {
                GlassTonalButton(
                    // 复用象棋设置的 AI 选择器, 针对兜底发生的那一方
                    onClick = { onPickAiFor(fallbackPly.moverSide) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        stringResource(R.string.xiangqi_switch_ai_model),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
        } else {
            null
        },
    )
}

/**
 * 云端引擎失败后本地引擎接手的软提示:棋力正常,不要红色恐吓。
 * 每局最多展示一次([XiangqiGameComponent.dismissLocalSwapNotice] 关掉后不再回来)。
 */
@Composable
private fun LocalEngineSwapCard(
    state: XiangqiGameUiState,
    swapPly: XiangqiPlyRecord,
    onDismiss: () -> Unit,
    onPickAiFor: (Side) -> Unit,
) {
    val reason = MoveDecision.stripMarkers(swapPly.aiReason).ifBlank { "unknown" }
    StatusCard(
        title = stringResource(R.string.xiangqi_ai_local_engine_swap_title),
        subtitle = stringResource(R.string.xiangqi_ai_local_engine_swap_message, reason),
        height = StatusCardHeight,
        titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
        titleTrailing = if (state.mode != GameMode.LLM_VS_LLM) {
            {
                GlassTonalButton(
                    onClick = { onPickAiFor(swapPly.moverSide) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        stringResource(R.string.xiangqi_switch_ai_model),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
        } else {
            null
        },
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GlassTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.xiangqi_close), maxLines = 1)
                }
            }
        },
    )
}

/** 把 UCCI 着法(如 "h2e2")解析为起止坐标;rank 0 是红方底线,与 [BoardPoint] 一致 */
private fun parseUcciMove(ucci: String): Pair<BoardPoint, BoardPoint>? {
    if (ucci.length < 4) return null

    fun pointAt(index: Int): BoardPoint? {
        val file = ucci[index].lowercaseChar()
        val rank = ucci[index + 1]
        if (file !in 'a'..'i' || rank !in '0'..'9') return null
        return BoardPoint(file - 'a', BoardPoint.RANK_COUNT - 1 - (rank - '0'))
    }

    val from = pointAt(0) ?: return null
    val to = pointAt(2) ?: return null
    return from to to
}

@Composable
private fun OnlineDebugPanel(state: XiangqiGameUiState) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        style = GlassStyle.Dense,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.xiangqi_online_debug_title),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(
                    R.string.xiangqi_online_debug_state,
                    state.onlineRoomId.takeLast(6).ifBlank { "-" },
                    state.onlineMySide.name,
                    state.onlineConnectionState.name,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val events = state.onlineDebugEvents.takeLast(10)
            if (events.isEmpty()) {
                Text(
                    text = stringResource(R.string.xiangqi_online_debug_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                events.forEach { event ->
                    Text(
                        text = event,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

private fun XiangqiGameUiState.playerTypeFor(side: Side): PlayerType {
    return if (side == Side.RED) redPlayerType else blackPlayerType
}

@Composable
internal fun XiangqiPlayersRow(
    state: XiangqiGameUiState,
    onPickAiFor: (Side) -> Unit,
    modifier: Modifier = Modifier,
    onShowOrigin: () -> Unit = {},
) {
    val login = LocalLoginState.current
    val bottomSide = if (state.mode == GameMode.ONLINE_PVP) state.onlineMySide else Side.RED
    val humanName = BaseUtils.getDisplayName(login.nickname, login.username)
        .ifBlank { stringResource(R.string.xiangqi_player_you) }
    val playable = state.isLoaded && (state.status == GameStatus.PLAYING || state.status == GameStatus.CHECK)

    @Composable
    fun player(side: Side): PlayerBarData {
        val type = state.playerTypeFor(side)
        val active = playable && state.boardState.sideToMove == side
        val name = resolvePlayerDisplayName(
            side, type, state.mode, humanName, state.onlineOpponentName,
            aiServiceForSide(side, state.redAiServiceName, state.blackAiServiceName)
                .ifBlank { stringResource(R.string.xiangqi_player_ai) },
        )
        val subtitle = if (active) stringResource(
            when {
                type == PlayerType.LLM && state.isAiThinking -> R.string.xiangqi_ai_thinking
                type == PlayerType.HUMAN && state.mode == GameMode.HUMAN_VS_LLM -> R.string.xiangqi_your_turn
                side == Side.RED -> R.string.xiangqi_turn_red
                else -> R.string.xiangqi_turn_black
            },
        ) else resolvePlayerSubtitle(
            side, type, state.mode, aiModelForSide(side, state.redAiModelName, state.blackAiModelName),
        )
        return PlayerBarData(
            name = name,
            subtitle = subtitle,
            avatarUrl = resolvePlayerAvatarUrl(type, state.mode, login.avatar.orEmpty(), state.onlineOpponentAvatarUrl),
            indicatorColor = if (side == Side.RED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            isActiveTurn = active,
            onClick = if (type == PlayerType.LLM && state.isLoaded && !state.isUpdating) ({ onPickAiFor(side) }) else null,
            actionLabel = stringResource(R.string.xiangqi_change_opponent),
            avatarIsLoggedIn = type != PlayerType.HUMAN || state.mode == GameMode.LOCAL_PVP || login.isLogin,
        )
    }
    BoardGamePlayersBar(
        top = player(bottomSide.opposite()),
        bottom = player(bottomSide),
        vsLabel = stringResource(R.string.xiangqi_vs_short),
        turnLabel = stringResource(R.string.xiangqi_turn_badge),
        modifier = modifier,
        emphasizeVs = state.status == GameStatus.NOT_STARTED && state.startedAt == 0L,
        onVsClick = onShowOrigin.takeIf { state.origin != null },
        vsActionLabel = stringResource(R.string.xiangqi_setup_source),
    )
}

@Composable
private fun resolvePlayerDisplayName(
    side: Side,
    playerType: PlayerType,
    gameMode: GameMode,
    humanDisplayName: String,
    onlineOpponentName: String,
    aiServiceName: String,
): String {
    return when (playerType) {
        PlayerType.LLM -> aiServiceName
        PlayerType.REMOTE -> onlineOpponentName.ifBlank { stringResource(R.string.xiangqi_player_remote) }
        PlayerType.HUMAN -> if (gameMode == GameMode.HUMAN_VS_LLM || gameMode == GameMode.ONLINE_PVP) {
            humanDisplayName
        } else {
            stringResource(if (side == Side.RED) R.string.xiangqi_player_red else R.string.xiangqi_player_black)
        }
    }
}

@Composable
private fun resolvePlayerSubtitle(
    side: Side,
    playerType: PlayerType,
    gameMode: GameMode,
    aiModelName: String,
): String {
    val sideLabel =
        stringResource(if (side == Side.RED) R.string.xiangqi_player_red else R.string.xiangqi_player_black)
    val suffix = when (playerType) {
        PlayerType.LLM -> aiModelName
        PlayerType.REMOTE -> stringResource(R.string.xiangqi_mode_online)
        PlayerType.HUMAN -> if (gameMode == GameMode.HUMAN_VS_LLM) {
            stringResource(R.string.xiangqi_player_you)
        } else {
            ""
        }
    }
    return if (suffix.isBlank()) sideLabel else "$sideLabel · $suffix"
}

private fun resolvePlayerAvatarUrl(
    playerType: PlayerType,
    gameMode: GameMode,
    humanAvatarUrl: String,
    onlineOpponentAvatarUrl: String,
): String {
    return when (playerType) {
        PlayerType.LLM -> ""
        PlayerType.REMOTE -> onlineOpponentAvatarUrl
        PlayerType.HUMAN -> if (gameMode == GameMode.HUMAN_VS_LLM || gameMode == GameMode.ONLINE_PVP) humanAvatarUrl else ""
    }
}


private fun aiServiceForSide(side: Side, redAiService: String, blackAiService: String): String =
    if (side == Side.RED) redAiService else blackAiService

private fun aiModelForSide(side: Side, redAiModel: String, blackAiModel: String): String =
    if (side == Side.RED) redAiModel else blackAiModel

private val StatusCardHeight = 140.dp
private val FallbackCardHeight = 92.dp

@Composable
private fun StatusCard(
    title: String,
    subtitle: String,
    height: Dp = StatusCardHeight,
    titleColor: Color = MaterialTheme.colorScheme.error,
    titleTrailing: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 12.dp),
        style = GlassStyle.Medium,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                titleTrailing?.invoke()
            }
            VerticalMarqueeText(
                text = subtitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            actions?.invoke()
        }
    }
}

/** 固定高度内自下而上循环滚动的纵向走马灯;文字放得下时静止 */
@Composable
private fun VerticalMarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val scrollState = rememberScrollState()
    var textHeightPx by remember { mutableIntStateOf(0) }
    val gapPx = with(androidx.compose.ui.platform.LocalDensity.current) { 12.dp.roundToPx() }
    val overflowing = scrollState.maxValue > 0

    LaunchedEffect(text, overflowing) {
        if (!overflowing) return@LaunchedEffect
        while (true) {
            scrollState.scrollTo(0)
            delay(1200)
            scrollState.animateScrollTo(textHeightPx + gapPx, tween(2500, easing = LinearEasing))
            delay(1200)
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(6.dp))) {
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = text,
                style = style,
                color = color,
                maxLines = Int.MAX_VALUE,
                onTextLayout = { textHeightPx = it.size.height },
            )
            if (overflowing) {
                Text(
                    text = text,
                    style = style,
                    color = color,
                    maxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}

@Composable
private fun ActionBar(
    modifier: Modifier = Modifier,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onAnalysis: () -> Unit,
    onExport: () -> Unit,
    onRestart: () -> Unit,
    onResign: () -> Unit,
    onRename: () -> Unit,
    onToggleFullscreen: () -> Unit,
    isImmersive: Boolean,
    fullscreenEnabled: Boolean,
    undoEnabled: Boolean,
    redoEnabled: Boolean,
    onSetup: () -> Unit,
    setupEnabled: Boolean,
    onStandardGame: () -> Unit,
    showStandardGame: Boolean,
    onShowOrigin: (() -> Unit)?,
    enabled: Boolean,
    showResign: Boolean,
) {
    var more by remember { mutableStateOf(false) }
    BoardGameActionBar(
        modifier = modifier,
        actions = listOf(
            BoardGameAction(
                icon = if (isImmersive) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen,
                contentDescription = stringResource(R.string.xiangqi_fullscreen),
                enabled = fullscreenEnabled,
                selected = isImmersive,
                onClick = onToggleFullscreen,
            ),
            BoardGameAction(
                icon = Icons.Outlined.GridOn,
                contentDescription = stringResource(R.string.xiangqi_setup_action),
                enabled = setupEnabled,
                onClick = onSetup,
            ),
            BoardGameAction(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
                contentDescription = stringResource(R.string.xiangqi_undo),
                enabled = undoEnabled,
                onClick = onUndo,
            ),
            BoardGameAction(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
                contentDescription = stringResource(R.string.xiangqi_redo),
                enabled = redoEnabled,
                onClick = onRedo,
            ),
        ),
    ) {
        Box(Modifier.weight(1f)) {
            BoardGameToolButton(
                icon = Icons.Outlined.MoreVert,
                label = stringResource(BoardGameR.string.boardgame_more),
                onClick = { more = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
            // 底部工具栏入口:向上展开;锚定整个 weight 单元格,箭头对准按钮中心
            EnhancedDropdownMenu(
                expanded = more,
                onDismissRequest = { more = false },
                modifier = Modifier.matchParentSize(),
                expandUp = true,
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.xiangqi_analyze)) },
                    onClick = { more = false; onAnalysis() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.xiangqi_export)) },
                    onClick = { more = false; onExport() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.xiangqi_game_over_restart)) },
                    onClick = { more = false; onRestart() },
                )
                if (showStandardGame) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.xiangqi_standard_game)) },
                        onClick = { more = false; onStandardGame() },
                    )
                }
                onShowOrigin?.let { showOrigin ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.xiangqi_setup_source)) },
                        onClick = { more = false; showOrigin() },
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.xiangqi_rename_game)) },
                    onClick = { more = false; onRename() },
                )
                if (showResign) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.xiangqi_resign)) },
                        onClick = { more = false; onResign() },
                    )
                }
            }
        }
    }
}
