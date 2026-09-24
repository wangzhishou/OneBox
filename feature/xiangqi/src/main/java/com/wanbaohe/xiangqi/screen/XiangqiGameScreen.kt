package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ImmersiveModeState
import com.wanbaohe.xiangqi.ui.XiangqiAiPickerBottomSheet
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
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.xiangqi.BuildConfig
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
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
import com.wanbaohe.xiangqi.router.LocalXiangqiImmersiveModeState
import com.wanbaohe.xiangqi.ui.board.XiangqiBoard
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAnalytics
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import com.wanbaohe.boardgame.ui.BoardGameOverOverlay
import com.wanbaohe.boardgame.ui.BoardStartOverlay
import kotlinx.coroutines.delay

@Composable
fun XiangqiGameScreen(
    component: XiangqiGameComponent,
    modifier: Modifier = Modifier,
    showChrome: Boolean = true,
) {
    val state = component.uiState
    var exportDialog by remember { mutableStateOf(false) }
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
        XiangqiAiPickerBottomSheet(
            visible = true,
            selected = currentSource,
            workingModelTitle = workingModel.title.ifBlank { workingModel.name },
            title = stringResource(R.string.xiangqi_settings_ai_picker_title),
            onSelected = { source ->
                component.switchAiSourceForSide(pickingSideValue, source)
                pickingSide = null
            },
            onDismiss = { pickingSide = null },
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
) {
    val state = component.uiState
    val loginState = LocalLoginState.current
    val immersiveState = LocalXiangqiImmersiveModeState.current

    val playable = state.status == GameStatus.PLAYING || state.status == GameStatus.CHECK
    val humanDisplayName = BaseUtils.getDisplayName(loginState.nickname, loginState.username)
        .ifBlank { stringResource(R.string.xiangqi_player_you) }
    val humanAvatarUrl = loginState.avatar.orEmpty()
    val redAiService =
        state.redAiServiceName.ifBlank { stringResource(R.string.xiangqi_player_ai) }
    val redAiModel =
        state.redAiModelName.ifBlank { stringResource(R.string.xiangqi_settings_empty_value) }
    val blackAiService =
        state.blackAiServiceName.ifBlank { stringResource(R.string.xiangqi_player_ai) }
    val blackAiModel =
        state.blackAiModelName.ifBlank { stringResource(R.string.xiangqi_settings_empty_value) }
    val bottomSide = if (state.mode == GameMode.ONLINE_PVP) state.onlineMySide else Side.RED
    val topSide = bottomSide.opposite()
    // 当前局面的最后一手(撤销后随之回退),棋盘上用以明示上一步
    val lastMove = state.history.getOrNull(state.currentPly - 1)?.let { parseUcciMove(it.moveUcci) }
    // AI 兜底 ("AI_FALLBACK") 已经合法落子完成,不必再多一张提示卡;
    // 只对真错 (AI_ERROR / 自定义错误文案) 显示并提供重试入口。
    val showErrorCard = state.errorMessage.isNotBlank() && state.errorMessage != "AI_FALLBACK"
    // 引擎不可用时会静默回退本地兜底,只表现为"AI 突然变笨"。
    // 这里把它显式说出来,并带上兜底原因(如 "pikafish: http 503"),否则无从排查。
    val fallbackPly = state.history.lastOrNull {
        MoveDecision.isLocalFallback(it.aiReason)
    }?.takeIf { it.ply == state.currentPly }
    val showDebugPanel = BuildConfig.DEBUG && state.mode == GameMode.ONLINE_PVP

    BoxWithConstraints(modifier = modifier) {
        // 高度允许时棋盘按可用高度自适应并水平居中;高度不够则回退为整宽棋盘 + 整页滚动
        val chromeHeight = 32.dp + 52.dp + 16.dp + 60.dp + 16.dp +
            (if (showErrorCard) StatusCardHeight + 16.dp else 0.dp) +
            (if (showDebugPanel) 156.dp else 0.dp) +
            (if (fallbackPly != null) FallbackCardHeight + 16.dp else 0.dp)
        val boardAvailable = maxHeight - chromeHeight
        val adaptive = boardAvailable != Dp.Infinity && boardAvailable >= MinAdaptiveBoardHeight

        if (adaptive) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .capturable(captureController),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayersBar(
                        state = state,
                        topSide = topSide,
                        bottomSide = bottomSide,
                        humanDisplayName = humanDisplayName,
                        humanAvatarUrl = humanAvatarUrl,
                        onlineOpponentName = state.onlineOpponentName,
                        onlineOpponentAvatarUrl = state.onlineOpponentAvatarUrl,
                        redAiService = redAiService,
                        redAiModel = redAiModel,
                        blackAiService = blackAiService,
                        blackAiModel = blackAiModel,
                        playable = playable,
                        onPickAiFor = onPickAiFor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    )
                    GameBoardArea(
                        component = component,
                        state = state,
                        bottomSide = bottomSide,
                        lastMove = lastMove,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                    )
                }
                GameActionBar(component, state, immersiveState, onExport, playable)
                if (showErrorCard) {
                    ErrorStatusCard(component, state, onPickAiFor)
                }
                if (showDebugPanel) {
                    OnlineDebugPanel(state = state)
                }
                if (fallbackPly != null) {
                    FallbackStatusCard(state, fallbackPly, onPickAiFor)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.capturable(captureController),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayersBar(
                        state = state,
                        topSide = topSide,
                        bottomSide = bottomSide,
                        humanDisplayName = humanDisplayName,
                        humanAvatarUrl = humanAvatarUrl,
                        onlineOpponentName = state.onlineOpponentName,
                        onlineOpponentAvatarUrl = state.onlineOpponentAvatarUrl,
                        redAiService = redAiService,
                        redAiModel = redAiModel,
                        blackAiService = blackAiService,
                        blackAiModel = blackAiModel,
                        playable = playable,
                        onPickAiFor = onPickAiFor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    )
                    GameBoardArea(
                        component = component,
                        state = state,
                        bottomSide = bottomSide,
                        lastMove = lastMove,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    )
                }
                GameActionBar(component, state, immersiveState, onExport, playable)
                if (showErrorCard) {
                    ErrorStatusCard(component, state, onPickAiFor)
                }
                if (showDebugPanel) {
                    OnlineDebugPanel(state = state)
                }
                if (fallbackPly != null) {
                    FallbackStatusCard(state, fallbackPly, onPickAiFor)
                }
                Spacer(modifier = Modifier.height(24.dp))
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
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
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
            )
            if (state.status == GameStatus.NOT_STARTED || state.status == GameStatus.PAUSED) {
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
            if (isGameOver) {
                BoardGameOverOverlay(
                    resultTitle = when (state.status) {
                        GameStatus.RED_WINS -> stringResource(R.string.xiangqi_game_over_red)
                        GameStatus.BLACK_WINS -> stringResource(R.string.xiangqi_game_over_black)
                        GameStatus.DRAW -> stringResource(R.string.xiangqi_game_over_draw)
                        else -> stringResource(R.string.xiangqi_resign_result)
                    },
                    restartLabel = stringResource(R.string.xiangqi_game_over_restart),
                    reviewLabel = stringResource(R.string.xiangqi_game_over_review),
                    onRestart = component::restart,
                    onReview = component::openAnalysis,
                    emphasizeResult = state.status != GameStatus.DRAW,
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
) {
    ActionBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onUndo = component::undo,
        onRedo = component::redo,
        onAnalysis = component::openAnalysis,
        onExport = onExport,
        onRestart = component::restart,
        onResign = { component.showResignConfirm = true },
        onRename = { component.showRenameDialog = true },
        onToggleFullscreen = { immersiveState?.toggle() },
        isImmersive = immersiveState?.isImmersive == true,
        allowUndoRedo = state.mode != GameMode.ONLINE_PVP,
        showResign = playable && state.mode != GameMode.LLM_VS_LLM,
    )
}

@Composable
private fun ErrorStatusCard(
    component: XiangqiGameComponent,
    state: XiangqiGameUiState,
    onPickAiFor: (Side) -> Unit,
) {
    StatusCard(
        title = stringResource(R.string.xiangqi_ai_failed),
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
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.xiangqi_retry_ai), maxLines = 1)
                }
                GlassTonalButton(
                    // 与象棋设置共用 XiangqiAiPicker, 不跳全局 AI 设置
                    onClick = { onPickAiFor(state.boardState.sideToMove) },
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
    StatusCard(
        title = stringResource(R.string.xiangqi_ai_local_fallback_title),
        subtitle = stringResource(
            R.string.xiangqi_ai_local_fallback_message,
            fallbackPly.aiReason
                .removePrefix(MoveDecision.LOCAL_FALLBACK_MARKER)
                .ifBlank { "unknown" },
        ),
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
                color = MaterialTheme.colorScheme.primary,
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
private fun PlayersBar(
    state: XiangqiGameUiState,
    topSide: Side,
    bottomSide: Side,
    humanDisplayName: String,
    humanAvatarUrl: String,
    onlineOpponentName: String,
    onlineOpponentAvatarUrl: String,
    redAiService: String,
    redAiModel: String,
    blackAiService: String,
    blackAiModel: String,
    playable: Boolean,
    onPickAiFor: (Side) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Medium,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerBarItem(
                side = topSide,
                state = state,
                humanDisplayName = humanDisplayName,
                onlineOpponentName = onlineOpponentName,
                onlineOpponentAvatarUrl = onlineOpponentAvatarUrl,
                humanAvatarUrl = humanAvatarUrl,
                aiServiceName = aiServiceForSide(topSide, redAiService, blackAiService),
                aiModelName = aiModelForSide(topSide, redAiModel, blackAiModel),
                isActiveTurn = state.boardState.sideToMove == topSide,
                playable = playable,
                onPickAiFor = onPickAiFor,
                mirrored = false,
                modifier = Modifier.weight(1f),
            )
            VsBadge(modifier = Modifier.padding(horizontal = 8.dp))
            PlayerBarItem(
                side = bottomSide,
                state = state,
                humanDisplayName = humanDisplayName,
                onlineOpponentName = onlineOpponentName,
                onlineOpponentAvatarUrl = onlineOpponentAvatarUrl,
                humanAvatarUrl = humanAvatarUrl,
                aiServiceName = aiServiceForSide(bottomSide, redAiService, blackAiService),
                aiModelName = aiModelForSide(bottomSide, redAiModel, blackAiModel),
                isActiveTurn = state.boardState.sideToMove == bottomSide,
                playable = playable,
                onPickAiFor = onPickAiFor,
                mirrored = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlayerBarItem(
    side: Side,
    state: XiangqiGameUiState,
    humanDisplayName: String,
    onlineOpponentName: String,
    onlineOpponentAvatarUrl: String,
    humanAvatarUrl: String,
    aiServiceName: String,
    aiModelName: String,
    isActiveTurn: Boolean,
    playable: Boolean,
    onPickAiFor: (Side) -> Unit,
    mirrored: Boolean,
    modifier: Modifier = Modifier,
) {
    val playerType = state.playerTypeFor(side)
    val displayName = resolvePlayerDisplayName(
        side = side,
        playerType = playerType,
        gameMode = state.mode,
        humanDisplayName = humanDisplayName,
        onlineOpponentName = onlineOpponentName,
        aiServiceName = aiServiceName,
    )
    val active = playable && isActiveTurn
    val subtitle = resolvePlayerSubtitle(
        side = side,
        playerType = playerType,
        gameMode = state.mode,
        aiModelName = aiModelName,
    )
    val avatarUrl = resolvePlayerAvatarUrl(
        playerType = playerType,
        gameMode = state.mode,
        humanAvatarUrl = humanAvatarUrl,
        onlineOpponentAvatarUrl = onlineOpponentAvatarUrl,
    )
    val sideDotColor = if (side == Side.RED) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val clickableModifier = if (playerType == PlayerType.LLM) {
        Modifier.clickable { onPickAiFor(side) }
    } else {
        Modifier
    }
    val content: @Composable () -> Unit = {
        if (mirrored && active) {
            TurnBadge()
            Spacer(modifier = Modifier.width(6.dp))
        }
        if (!mirrored) {
            Avatar(username = displayName, avatar = avatarUrl, size = 34.dp, isLogin = true)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (mirrored) Alignment.End else Alignment.Start) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(sideDotColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!mirrored && active) {
            Spacer(modifier = Modifier.width(6.dp))
            TurnBadge()
        }
        if (mirrored) {
            Spacer(modifier = Modifier.width(8.dp))
            Avatar(username = displayName, avatar = avatarUrl, size = 34.dp, isLogin = true)
        }
    }

    if (active) {
        Box(
            modifier = modifier
                .then(clickableModifier)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = if (mirrored) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (mirrored) Arrangement.End else Arrangement.Start,
                content = { content() },
            )
        }
    } else {
        Row(
            modifier = modifier
                .then(clickableModifier)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (mirrored) Arrangement.End else Arrangement.Start,
            content = { content() },
        )
    }
}

@Composable
private fun VsBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.xiangqi_vs_short),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/** 行棋方的"轮到"角标:虚线描边小胶囊 */
@Composable
private fun TurnBadge(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50),
            )
            .drawBehind {
                drawRoundRect(
                    color = primary,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(5.dp.toPx(), 3.dp.toPx()),
                        ),
                    ),
                    cornerRadius = CornerRadius(size.height / 2, size.height / 2),
                )
            }
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = stringResource(R.string.xiangqi_turn_badge),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = primary,
            maxLines = 1,
        )
    }
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
private val MinAdaptiveBoardHeight = 280.dp

@Composable
private fun StatusCard(
    title: String,
    subtitle: String,
    height: Dp = StatusCardHeight,
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
                    color = MaterialTheme.colorScheme.error,
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
    allowUndoRedo: Boolean,
    showResign: Boolean,
) {
    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            GlassTonalIconButton(onClick = onUndo, enabled = allowUndoRedo) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            GlassTonalIconButton(onClick = onRedo, enabled = allowUndoRedo) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            GlassTonalIconButton(onClick = onAnalysis) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAnalytics,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            GlassTonalIconButton(onClick = onExport) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            GlassTonalIconButton(onClick = onRestart) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            if (showResign) {
                GlassTonalIconButton(onClick = onResign) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            GlassTonalIconButton(onClick = onRename) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            GlassTonalIconButton(onClick = onToggleFullscreen) {
                Icon(
                    imageVector = if (isImmersive) Icons.Outlined.FullscreenExit else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Fullscreen,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
