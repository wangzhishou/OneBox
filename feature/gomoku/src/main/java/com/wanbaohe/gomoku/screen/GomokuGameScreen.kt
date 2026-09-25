package com.wanbaohe.gomoku.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.utils.BaseUtils
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAnalytics
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalImageShareProvider
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.boardgame.model.AiPickerItem
import com.wanbaohe.boardgame.model.AiSourceTag
import com.wanbaohe.boardgame.model.BoardGameAction
import com.wanbaohe.boardgame.model.PlayerBarData
import com.wanbaohe.boardgame.ui.ActionBar
import com.wanbaohe.boardgame.ui.AiPickerBottomSheet
import com.wanbaohe.boardgame.ui.BoardGameOverOverlay
import com.wanbaohe.boardgame.ui.BoardStartOverlay
import com.wanbaohe.boardgame.ui.ErrorStatusCard
import com.wanbaohe.boardgame.ui.FallbackStatusCard
import com.wanbaohe.boardgame.ui.FallbackCardHeight
import com.wanbaohe.boardgame.ui.MinAdaptiveBoardHeight
import com.wanbaohe.boardgame.ui.PlayersBar
import com.wanbaohe.boardgame.ui.StatusCardHeight
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.port.outbound.MoveDecision
import com.wanbaohe.gomoku.component.GomokuGameComponent
import com.wanbaohe.gomoku.component.GomokuGameUiState
import com.wanbaohe.gomoku.data.GomokuPlyRecord
import com.wanbaohe.gomoku.data.TextExportLabels
import com.wanbaohe.gomoku.domain.Notation
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.ConnectionState
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.presentation.localizedGameResultText
import com.wanbaohe.boardgame.ui.LocalBoardGameImmersiveModeState
import com.wanbaohe.gomoku.ui.board.GomokuBoard
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

@Composable
fun GomokuGameScreen(
    component: GomokuGameComponent,
    modifier: Modifier = Modifier,
    showChrome: Boolean = true,
) {
    val state = component.uiState
    var exportDialog by remember { mutableStateOf(false) }
    var pickingSide by remember { mutableStateOf<Side?>(null) }
    val exportLabels = TextExportLabels(
        header = stringResource(R.string.gomoku_export_header),
        titleLabel = stringResource(R.string.gomoku_export_title_label),
        initialFenLabel = stringResource(R.string.gomoku_export_initial_fen_label),
        resultLabel = stringResource(R.string.gomoku_export_result_label),
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
                    originalUri = "gomoku_screenshot",
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
        GomokuGameContent(
            component = component,
            modifier = contentModifier,
            captureController = captureController,
            onExport = { exportDialog = true },
            onPickAiFor = { side -> pickingSide = side },
        )
    }

    if (showChrome) {
        BaseScreen(
            title = state.title.ifBlank { stringResource(R.string.gomoku_game_title) },
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

    // 认输确认
    if (component.showResignConfirm) {
        AlertDialog(
            onDismissRequest = { component.showResignConfirm = false },
            title = { Text(stringResource(R.string.gomoku_resign_confirm_title)) },
            text = { Text(stringResource(R.string.gomoku_resign_confirm_message)) },
            confirmButton = {
                TextButton(onClick = component::resign) {
                    Text(stringResource(R.string.gomoku_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { component.showResignConfirm = false }) {
                    Text(stringResource(R.string.gomoku_cancel))
                }
            },
        )
    }

    // 重新开局确认
    if (component.showRestartConfirm) {
        AlertDialog(
            onDismissRequest = { component.showRestartConfirm = false },
            title = { Text(stringResource(R.string.gomoku_restart_confirm_title)) },
            text = { Text(stringResource(R.string.gomoku_restart_confirm_message)) },
            confirmButton = {
                TextButton(onClick = component::restart) {
                    Text(stringResource(R.string.gomoku_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { component.showRestartConfirm = false }) {
                    Text(stringResource(R.string.gomoku_cancel))
                }
            },
        )
    }

    // 重命名
    if (component.showRenameDialog) {
        var renameText by remember { mutableStateOf(state.title) }
        AlertDialog(
            onDismissRequest = { component.showRenameDialog = false },
            title = { Text(stringResource(R.string.gomoku_rename_game)) },
            text = {
                GlassOutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.gomoku_rename_hint)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { component.renameGame(renameText) },
                    enabled = renameText.isNotBlank(),
                ) {
                    Text(stringResource(R.string.gomoku_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { component.showRenameDialog = false }) {
                    Text(stringResource(R.string.gomoku_cancel))
                }
            },
        )
    }

    val pickingSideValue = pickingSide
    if (pickingSideValue != null) {
        val workingModel by component.currentAIEngine.collectAsState()
        val loginTag = stringResource(R.string.gomoku_ai_tag_login)
        val pointsTag = stringResource(R.string.gomoku_ai_tag_points)
        val freeTag = stringResource(R.string.gomoku_ai_tag_free)
        val engineName = stringResource(R.string.gomoku_ai_source_engine_name)
        val engineDesc = stringResource(R.string.gomoku_ai_source_engine_desc)
        val sources = GomokuAiSource.presets
        val items = remember(workingModel, loginTag, pointsTag, freeTag, engineName, engineDesc) {
            sources.map { source ->
                when (source) {
                    GomokuAiSource.WorkingModel -> AiPickerItem(
                        title = workingModel.title.ifBlank { workingModel.name },
                        subtitle = workingModel.model.title.ifBlank { workingModel.model.name },
                        tags = listOf(
                            AiSourceTag(loginTag, Color(0xFFF08A5D)),
                            AiSourceTag(pointsTag, Color(0xFF4F46E5)),
                        ),
                    )
                    is GomokuAiSource.RemoteEngine -> AiPickerItem(
                        title = engineName,
                        subtitle = engineDesc,
                        tags = listOf(AiSourceTag(freeTag, Color(0xFF3D8B7A))),
                        trailingIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory,
                    )
                }
            }
        }
        AiPickerBottomSheet(
            visible = true,
            title = stringResource(R.string.gomoku_settings_ai_picker_title),
            description = stringResource(R.string.gomoku_ai_picker_desc),
            items = items,
            selectedItem = items.getOrNull(sources.indexOf(component.currentSourceForSide(pickingSideValue))),
            onSelected = {
                val index = items.indexOf(it)
                if (index >= 0) {
                    component.switchAiSourceForSide(pickingSideValue, sources[index])
                }
                pickingSide = null
            },
            onDismiss = { pickingSide = null },
        )
    }
}

@Composable
private fun GomokuGameContent(
    component: GomokuGameComponent,
    modifier: Modifier,
    captureController: com.t8rin.imagetoolbox.core.ui.utils.capturable.CaptureController,
    onExport: () -> Unit,
    onPickAiFor: (Side) -> Unit,
) {
    val state = component.uiState
    val loginState = LocalLoginState.current
    val immersiveState = LocalBoardGameImmersiveModeState.current

    val playable = state.status == GameStatus.PLAYING
    val humanDisplayName = BaseUtils.getDisplayName(loginState.nickname, loginState.username)
        .ifBlank { stringResource(R.string.gomoku_player_you) }
    val humanAvatarUrl = loginState.avatar.orEmpty()
    val blackAiService =
        state.blackAiServiceName.ifBlank { stringResource(R.string.gomoku_player_ai) }
    val blackAiModel =
        state.blackAiModelName.ifBlank { stringResource(R.string.gomoku_settings_empty_value) }
    val whiteAiService =
        state.whiteAiServiceName.ifBlank { stringResource(R.string.gomoku_player_ai) }
    val whiteAiModel =
        state.whiteAiModelName.ifBlank { stringResource(R.string.gomoku_settings_empty_value) }
    // 人机与在线对战时人类始终在下方;本地双人/AI 对战黑方(先手)在下方
    val bottomSide = when (state.mode) {
        GameMode.HUMAN_VS_LLM -> if (state.blackPlayerType == PlayerType.HUMAN) Side.BLACK else Side.WHITE
        GameMode.ONLINE_PVP -> state.onlineMySide
        else -> Side.BLACK
    }
    val topSide = bottomSide.opposite()
    // 当前局面的最后一手(撤销后随之回退),棋盘上用以明示上一步
    val lastMove = state.history.getOrNull(state.currentPly - 1)?.let { Notation.parse(it.moveUcci) }
    // AI 兜底 ("AI_FALLBACK") 已经合法落子完成,不必再多一张提示卡;
    // 只对真错 (AI_ERROR / 自定义错误文案) 显示并提供重试入口。
    val showErrorCard = state.errorMessage.isNotBlank() && state.errorMessage != "AI_FALLBACK"
    val fallbackPly = state.history.lastOrNull {
        MoveDecision.isLocalFallback(it.aiReason)
    }?.takeIf { it.ply == state.currentPly }
    // 在线对战的断线/错误提示(象棋同款:只在在线模式显示,不遮挡棋盘)
    val onlineDisconnected = state.mode == GameMode.ONLINE_PVP &&
        (state.onlineConnectionState == ConnectionState.OPPONENT_DISCONNECTED ||
            state.onlineConnectionState == ConnectionState.ERROR)

    BoxWithConstraints(modifier = modifier) {
        // 高度允许时棋盘按可用高度自适应并水平居中;高度不够则回退为整宽棋盘 + 整页滚动
        val chromeHeight = 32.dp + 52.dp + 16.dp + 60.dp + 16.dp +
            (if (showErrorCard) StatusCardHeight + 16.dp else 0.dp) +
            (if (onlineDisconnected) FallbackCardHeight + 16.dp else 0.dp) +
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
                        top = playerBarData(state, topSide, humanDisplayName, humanAvatarUrl, blackAiService, blackAiModel, whiteAiService, whiteAiModel, playable, onPickAiFor),
                        bottom = playerBarData(state, bottomSide, humanDisplayName, humanAvatarUrl, blackAiService, blackAiModel, whiteAiService, whiteAiModel, playable, onPickAiFor),
                        vsLabel = stringResource(R.string.gomoku_vs_short),
                        turnLabel = stringResource(R.string.gomoku_turn_badge),
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
                GameActionBar(component, state, onExport, playable)
                if (showErrorCard) {
                    ErrorStatusCard(
                        title = stringResource(R.string.gomoku_ai_failed),
                        subtitle = when (state.errorMessage) {
                            "AI_ERROR" -> stringResource(R.string.gomoku_ai_failed)
                            else -> state.errorMessage
                        },
                        retryLabel = stringResource(R.string.gomoku_retry_ai),
                        onRetry = component::retryAiMove,
                        dismissLabel = stringResource(R.string.gomoku_close),
                        onDismiss = component::dismissError,
                        secondaryLabel = stringResource(R.string.gomoku_switch_ai_model),
                        onSecondary = { onPickAiFor(state.boardState.sideToMove) },
                    )
                }
                if (fallbackPly != null) {
                    FallbackStatusCard(
                        title = stringResource(R.string.gomoku_ai_local_fallback_title),
                        subtitle = stringResource(
                            R.string.gomoku_ai_local_fallback_message,
                            fallbackPly.aiReason
                                .removePrefix(MoveDecision.LOCAL_FALLBACK_MARKER)
                                .ifBlank { "unknown" },
                        ),
                        switchLabel = if (state.mode != GameMode.LLM_VS_LLM) {
                            stringResource(R.string.gomoku_switch_ai_model)
                        } else null,
                        onSwitch = if (state.mode != GameMode.LLM_VS_LLM) {
                            { onPickAiFor(fallbackPly.moverSide) }
                        } else null,
                    )
                }
                if (onlineDisconnected) {
                    FallbackStatusCard(
                        title = stringResource(R.string.gomoku_mode_online),
                        subtitle = stringResource(R.string.gomoku_signaling_disconnected),
                        switchLabel = null,
                        onSwitch = null,
                    )
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
                        top = playerBarData(state, topSide, humanDisplayName, humanAvatarUrl, blackAiService, blackAiModel, whiteAiService, whiteAiModel, playable, onPickAiFor),
                        bottom = playerBarData(state, bottomSide, humanDisplayName, humanAvatarUrl, blackAiService, blackAiModel, whiteAiService, whiteAiModel, playable, onPickAiFor),
                        vsLabel = stringResource(R.string.gomoku_vs_short),
                        turnLabel = stringResource(R.string.gomoku_turn_badge),
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
                GameActionBar(component, state, onExport, playable)
                if (showErrorCard) {
                    ErrorStatusCard(
                        title = stringResource(R.string.gomoku_ai_failed),
                        subtitle = when (state.errorMessage) {
                            "AI_ERROR" -> stringResource(R.string.gomoku_ai_failed)
                            else -> state.errorMessage
                        },
                        retryLabel = stringResource(R.string.gomoku_retry_ai),
                        onRetry = component::retryAiMove,
                        dismissLabel = stringResource(R.string.gomoku_close),
                        onDismiss = component::dismissError,
                        secondaryLabel = stringResource(R.string.gomoku_switch_ai_model),
                        onSecondary = { onPickAiFor(state.boardState.sideToMove) },
                    )
                }
                if (fallbackPly != null) {
                    FallbackStatusCard(
                        title = stringResource(R.string.gomoku_ai_local_fallback_title),
                        subtitle = stringResource(
                            R.string.gomoku_ai_local_fallback_message,
                            fallbackPly.aiReason
                                .removePrefix(MoveDecision.LOCAL_FALLBACK_MARKER)
                                .ifBlank { "unknown" },
                        ),
                        switchLabel = if (state.mode != GameMode.LLM_VS_LLM) {
                            stringResource(R.string.gomoku_switch_ai_model)
                        } else null,
                        onSwitch = if (state.mode != GameMode.LLM_VS_LLM) {
                            { onPickAiFor(fallbackPly.moverSide) }
                        } else null,
                    )
                }
                if (onlineDisconnected) {
                    FallbackStatusCard(
                        title = stringResource(R.string.gomoku_mode_online),
                        subtitle = stringResource(R.string.gomoku_signaling_disconnected),
                        switchLabel = null,
                        onSwitch = null,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/** 组装玩家条数据:LLM 方显示服务/模型名并可点击切换,人类方显示昵称 */
@Composable
private fun playerBarData(
    state: GomokuGameUiState,
    side: Side,
    humanDisplayName: String,
    humanAvatarUrl: String,
    blackAiService: String,
    blackAiModel: String,
    whiteAiService: String,
    whiteAiModel: String,
    playable: Boolean,
    onPickAiFor: (Side) -> Unit,
): PlayerBarData {
    val playerType = state.playerTypeFor(side)
    val aiService = if (side == Side.BLACK) blackAiService else whiteAiService
    val aiModel = if (side == Side.BLACK) blackAiModel else whiteAiModel
    val sideLabel = stringResource(
        if (side == Side.BLACK) R.string.gomoku_player_black else R.string.gomoku_player_white
    )
    val (name, subtitle) = when (playerType) {
        PlayerType.LLM -> aiService to if (aiModel.isBlank()) sideLabel else "$sideLabel · $aiModel"
        PlayerType.REMOTE -> state.onlineOpponentName.ifBlank {
            stringResource(R.string.gomoku_player_remote)
        } to "$sideLabel · ${stringResource(R.string.gomoku_player_remote)}"
        PlayerType.HUMAN -> if (state.mode == GameMode.HUMAN_VS_LLM || state.mode == GameMode.ONLINE_PVP) {
            humanDisplayName to "$sideLabel · ${stringResource(R.string.gomoku_player_you)}"
        } else {
            sideLabel to ""
        }
    }
    return PlayerBarData(
        name = name,
        subtitle = subtitle,
        avatarUrl = when (playerType) {
            PlayerType.REMOTE -> state.onlineOpponentAvatarUrl
            PlayerType.HUMAN -> if (state.mode == GameMode.HUMAN_VS_LLM || state.mode == GameMode.ONLINE_PVP) humanAvatarUrl else ""
            PlayerType.LLM -> ""
        },
        indicatorColor = if (side == Side.BLACK) MaterialTheme.colorScheme.onSurface else Color(0xFF9E9E9E),
        isActiveTurn = playable && state.boardState.sideToMove == side,
        onClick = if (playerType == PlayerType.LLM) ({ onPickAiFor(side) }) else null,
    )
}

private fun GomokuGameUiState.playerTypeFor(side: Side): PlayerType =
    if (side == Side.BLACK) blackPlayerType else whitePlayerType

@Composable
private fun GameBoardArea(
    component: GomokuGameComponent,
    state: GomokuGameUiState,
    bottomSide: Side,
    lastMove: BoardPoint?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // 内层 Box 随棋盘实际大小,遮罩层(matchParentSize)只盖住棋盘
        Box(contentAlignment = Alignment.Center) {
            GomokuBoard(
                boardState = state.boardState,
                selectedPoint = state.interaction.selectedPoint,
                candidateTargets = state.interaction.candidateTargets,
                onCellTap = component::onCellTap,
                bottomSide = bottomSide,
                lastMove = lastMove,
            )
            if (state.status == GameStatus.NOT_STARTED || state.status == GameStatus.PAUSED) {
                BoardStartOverlay(
                    startLabel = stringResource(
                        if (state.status == GameStatus.PAUSED) R.string.gomoku_resume_action else R.string.gomoku_start_action
                    ),
                    onStart = component::start,
                )
            }
            val isGameOver = state.status == GameStatus.BLACK_WINS ||
                state.status == GameStatus.WHITE_WINS ||
                state.status == GameStatus.DRAW ||
                state.status == GameStatus.RESIGNED
            if (isGameOver) {
                BoardGameOverOverlay(
                    resultTitle = when (state.status) {
                        GameStatus.BLACK_WINS -> stringResource(R.string.gomoku_game_over_black)
                        GameStatus.WHITE_WINS -> stringResource(R.string.gomoku_game_over_white)
                        GameStatus.DRAW -> stringResource(R.string.gomoku_game_over_draw)
                        GameStatus.RESIGNED -> stringResource(R.string.gomoku_resign_result)
                        else -> ""
                    },
                    resultSubtitle = when (state.status) {
                        GameStatus.BLACK_WINS -> stringResource(R.string.gomoku_game_over_subtitle_black)
                        GameStatus.WHITE_WINS -> stringResource(R.string.gomoku_game_over_subtitle_white)
                        GameStatus.DRAW -> stringResource(R.string.gomoku_game_over_subtitle_draw)
                        GameStatus.RESIGNED -> stringResource(R.string.gomoku_game_over_subtitle_resign)
                        else -> ""
                    },
                    emphasizeResult = state.status != GameStatus.DRAW,
                    restartLabel = stringResource(R.string.gomoku_game_over_play_again),
                    reviewLabel = stringResource(R.string.gomoku_game_over_review),
                    backLabel = stringResource(R.string.gomoku_game_over_back),
                    onRestart = component::restart,
                    onReview = component::openAnalysis,
                    onBack = component.onGoBack,
                )
            }
        }
    }
}

@Composable
private fun GameActionBar(
    component: GomokuGameComponent,
    state: GomokuGameUiState,
    onExport: () -> Unit,
    playable: Boolean,
) {
    val immersiveState = LocalBoardGameImmersiveModeState.current
    val actions = buildList {
        add(BoardGameAction(icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo, onClick = component::undo))
        add(BoardGameAction(icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo, onClick = component::redo))
        add(BoardGameAction(icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAnalytics, onClick = component::openAnalysis))
        add(BoardGameAction(icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare, onClick = onExport))
        add(BoardGameAction(icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh, onClick = { component.showRestartConfirm = true }))
        if (playable && state.mode != GameMode.LLM_VS_LLM) {
            add(BoardGameAction(icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag, onClick = { component.showResignConfirm = true }))
        }
        add(BoardGameAction(icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit, onClick = { component.showRenameDialog = true }))
        add(
            BoardGameAction(
                icon = if (immersiveState?.isImmersive == true) Icons.Outlined.FullscreenExit else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Fullscreen,
                onClick = { immersiveState?.toggle() },
            )
        )
    }
    ActionBar(
        actions = actions,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    )
}
