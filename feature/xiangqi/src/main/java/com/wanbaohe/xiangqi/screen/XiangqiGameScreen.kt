package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.common.ui.BaseScreen
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
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.wanbaohe.xiangqi.BuildConfig
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.component.XiangqiGameComponent
import com.wanbaohe.xiangqi.component.XiangqiGameUiState
import com.wanbaohe.xiangqi.data.TextExportLabels
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
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmojiEvents
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAnalytics

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

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val isRedTurn = state.boardState.sideToMove == Side.RED
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                XiangqiBoard(
                    boardState = state.boardState,
                    selectedPoint = state.interaction.selectedPoint,
                    candidateTargets = state.interaction.candidateTargets,
                    onCellTap = component::onCellTap,
                    modifier = Modifier.fillMaxWidth(),
                    bottomSide = bottomSide,
                    riverNotice = if (state.status == GameStatus.CHECK) stringResource(R.string.xiangqi_check) else "",
                )
                if (state.status == GameStatus.NOT_STARTED || state.status == GameStatus.PAUSED) {
                    BoardStartOverlay(
                        isResume = state.status == GameStatus.PAUSED,
                        onStart = component::start,
                    )
                }
                val isGameOver = state.status == GameStatus.RED_WINS ||
                    state.status == GameStatus.BLACK_WINS ||
                    state.status == GameStatus.DRAW ||
                    state.status == GameStatus.RESIGNED
                if (isGameOver) {
                    BoardGameOverOverlay(
                        status = state.status,
                        onRestart = component::restart,
                        onReview = component::openAnalysis,
                    )
                }
            }
        }

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

        // AI 兜底 ("AI_FALLBACK") 已经合法落子完成,不必再多一张提示卡;
        // 只对真错 (AI_ERROR / 自定义错误文案) 显示并提供重试入口。
        if (state.errorMessage.isNotBlank() && state.errorMessage != "AI_FALLBACK") {
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
                            Text(stringResource(R.string.xiangqi_retry_ai))
                        }
                        GlassTonalButton(
                            // 与象棋设置共用 XiangqiAiPicker, 不跳全局 AI 设置
                            onClick = { onPickAiFor(state.boardState.sideToMove) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.xiangqi_switch_ai_model))
                        }
                    }
                    GlassTonalButton(
                        onClick = component::dismissError,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.xiangqi_close))
                    }
                },
            )
        }

        if (BuildConfig.DEBUG && state.mode == GameMode.ONLINE_PVP) {
            OnlineDebugPanel(state = state)
        }

        // 引擎不可用时会静默回退本地兜底，只表现为"AI 突然变笨"。
        // 这里把它显式说出来，并带上兜底原因（如 "pikafish: http 503"），否则无从排查。
        val fallbackPly = state.history.lastOrNull {
            MoveDecision.isLocalFallback(it.aiReason)
        }?.takeIf { it.ply == state.currentPly }
        if (fallbackPly != null) {
            // 用落库的 moverSide 判定行棋方最可靠（不依赖当前轮到谁）
            val engineName = when (fallbackPly.moverSide) {
                Side.RED -> state.redAiServiceName.ifBlank { state.redAiModelName }
                Side.BLACK -> state.blackAiServiceName.ifBlank { state.blackAiModelName }
            }
            StatusCard(
                title = stringResource(R.string.xiangqi_ai_local_fallback_title),
                subtitle = stringResource(
                    R.string.xiangqi_ai_local_fallback_message,
                    fallbackPly.aiReason
                        .removePrefix(MoveDecision.LOCAL_FALLBACK_MARKER)
                        .ifBlank { "unknown" },
                ),
                actions = {
                    // AI 对战下两个座位都是引擎，只给一个"切换模型"入口会让人误解是哪个
                    if (state.mode != GameMode.LLM_VS_LLM) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (engineName.isNotBlank()) {
                                Text(
                                    text = engineName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            GlassTonalButton(
                                // 复用象棋设置的 AI 选择器, 针对兜底发生的那一方
                                onClick = { onPickAiFor(fallbackPly.moverSide) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.xiangqi_switch_ai_model))
                            }
                        }
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
            Text(
                text = stringResource(R.string.xiangqi_vs_short),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
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
    val subtitle = if (active) {
        resolvePlayerStatusText(
            side = side,
            playerType = playerType,
            gameMode = state.mode,
            isActiveTurn = isActiveTurn,
            playable = playable,
        )
    } else {
        resolvePlayerSubtitle(
            playerType = playerType,
            gameMode = state.mode,
            aiModelName = aiModelName,
        )
    }
    val accentColor = if (active) {
        resolvePlayerStatusColor(side = side, isActiveTurn = isActiveTurn, playable = playable)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val avatarUrl = resolvePlayerAvatarUrl(
        playerType = playerType,
        gameMode = state.mode,
        humanAvatarUrl = humanAvatarUrl,
        onlineOpponentAvatarUrl = onlineOpponentAvatarUrl,
    )

    Row(
        modifier = modifier.then(
            if (playerType == PlayerType.LLM) Modifier.clickable { onPickAiFor(side) } else Modifier
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (mirrored) Arrangement.End else Arrangement.Start,
    ) {
        if (!mirrored) {
            Avatar(username = displayName, avatar = avatarUrl, size = 32.dp, isLogin = true)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(horizontalAlignment = if (mirrored) Alignment.End else Alignment.Start) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (active) accentColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (mirrored) {
            Spacer(modifier = Modifier.width(8.dp))
            Avatar(username = displayName, avatar = avatarUrl, size = 32.dp, isLogin = true)
        }
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
    playerType: PlayerType,
    gameMode: GameMode,
    aiModelName: String,
): String {
    return when (playerType) {
        PlayerType.LLM -> aiModelName
        PlayerType.REMOTE -> stringResource(R.string.xiangqi_mode_online)
        PlayerType.HUMAN -> if (gameMode == GameMode.HUMAN_VS_LLM) {
            stringResource(R.string.xiangqi_player_you)
        } else if (gameMode == GameMode.ONLINE_PVP) {
            stringResource(R.string.xiangqi_mode_online)
        } else {
            stringResource(R.string.xiangqi_mode_local)
        }
    }
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

@Composable
private fun resolvePlayerStatusText(
    side: Side,
    playerType: PlayerType,
    gameMode: GameMode,
    isActiveTurn: Boolean,
    playable: Boolean,
): String {
    if (!playable) return stringResource(R.string.xiangqi_waiting)
    if (!isActiveTurn) return stringResource(R.string.xiangqi_waiting)
    return when {
        playerType == PlayerType.LLM -> stringResource(R.string.xiangqi_ai_thinking)
        playerType == PlayerType.REMOTE -> stringResource(R.string.xiangqi_waiting_opponent_move)
        gameMode == GameMode.HUMAN_VS_LLM || gameMode == GameMode.ONLINE_PVP -> stringResource(R.string.xiangqi_your_turn)
        side == Side.RED -> stringResource(R.string.xiangqi_turn_red)
        else -> stringResource(R.string.xiangqi_turn_black)
    }
}

@Composable
private fun resolvePlayerStatusColor(
    side: Side,
    isActiveTurn: Boolean,
    playable: Boolean,
): Color {
    return when {
        !playable || !isActiveTurn -> MaterialTheme.colorScheme.onSurfaceVariant
        side == Side.RED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun StatusCard(
    title: String,
    subtitle: String,
    actions: (@Composable () -> Unit)? = null,
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        style = GlassStyle.Medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            actions?.invoke()
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

@Composable
private fun BoxScope.BoardStartOverlay(
    isResume: Boolean,
    onStart: () -> Unit,
) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(MaterialTheme.shapes.large)
            .glassBackground(
                style = GlassStyle.Dense,
                color = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
                shape = MaterialTheme.shapes.large
            ),
        contentAlignment = Alignment.Center,
    ) {
        ExtendedFloatingActionButton(
            onClick = onStart,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                    contentDescription = null,
                )
            },
            text = {
                Text(
                    text = stringResource(
                        if (isResume) R.string.xiangqi_resume_action else R.string.xiangqi_start_action
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun BoxScope.BoardGameOverOverlay(
    status: GameStatus,
    onRestart: () -> Unit,
    onReview: () -> Unit,
) {
    val titleText = when (status) {
        GameStatus.RED_WINS -> stringResource(R.string.xiangqi_game_over_red)
        GameStatus.BLACK_WINS -> stringResource(R.string.xiangqi_game_over_black)
        GameStatus.DRAW -> stringResource(R.string.xiangqi_game_over_draw)
        GameStatus.RESIGNED -> stringResource(R.string.xiangqi_resign_result)
        else -> ""
    }
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(MaterialTheme.shapes.large)
            .background(Color.Black.copy(alpha = 0.36f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTonalButton(onClick = onReview) {
                    Text(stringResource(R.string.xiangqi_game_over_review))
                }
                GlassTonalButton(onClick = onRestart) {
                    Text(stringResource(R.string.xiangqi_game_over_restart))
                }
            }
        }
    }
}
