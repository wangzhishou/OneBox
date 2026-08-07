package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ai.AIModelsPickerBottomSheet
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
import com.wanbaohe.xiangqi.component.XiangqiGameComponent
import com.wanbaohe.xiangqi.component.XiangqiGameUiState
import com.wanbaohe.xiangqi.data.TextExportLabels
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
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
            onDismiss = {
                exportDialog = false
                component.dismissExport()
            },
            onFen = component::exportFen,
            onJson = component::exportJson,
            onText = { component.exportText(exportLabels) },
            onShareImage = ::shareScreenshot,
            isSharing = isCapturing,
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
        val allEngines by component.allAiEngines.collectAsState()
        val modelsByProvider by component.modelsByProvider.collectAsState()
        val currentEngine = component.currentEngineForSide(pickingSideValue)
        AIModelsPickerBottomSheet(
            visible = true,
            allEngines = allEngines,
            modelsByProvider = modelsByProvider,
            selectedEngineName = currentEngine.identityKey(),
            selectedModelName = currentEngine.model.name,
            title = stringResource(R.string.xiangqi_settings_ai_picker_title),
            onSelected = { engine, model ->
                component.switchAiModelForSide(pickingSideValue, engine, model)
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
            .padding(16.dp),
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
            PlayerIdentityCardForSide(
                side = topSide,
                state = state,
                humanDisplayName = humanDisplayName,
                onlineOpponentName = state.onlineOpponentName,
                onlineOpponentAvatarUrl = state.onlineOpponentAvatarUrl,
                humanAvatarUrl = humanAvatarUrl,
                aiServiceName = aiServiceForSide(topSide, redAiService, blackAiService),
                aiModelName = aiModelForSide(topSide, redAiModel, blackAiModel),
                isActiveTurn = state.boardState.sideToMove == topSide,
                playable = playable,
                onPickAiFor = onPickAiFor,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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

            PlayerIdentityCardForSide(
                side = bottomSide,
                state = state,
                humanDisplayName = humanDisplayName,
                onlineOpponentName = state.onlineOpponentName,
                onlineOpponentAvatarUrl = state.onlineOpponentAvatarUrl,
                humanAvatarUrl = humanAvatarUrl,
                aiServiceName = aiServiceForSide(bottomSide, redAiService, blackAiService),
                aiModelName = aiModelForSide(bottomSide, redAiModel, blackAiModel),
                isActiveTurn = state.boardState.sideToMove == bottomSide,
                playable = playable,
                onPickAiFor = onPickAiFor,
            )
        }

        ActionBar(
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
                            onClick = component::openAiModelSettings,
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OnlineDebugPanel(state: XiangqiGameUiState) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun PlayerIdentityCardForSide(
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
    PlayerIdentityCard(
        displayName = displayName,
        subtitle = resolvePlayerSubtitle(
            playerType = playerType,
            gameMode = state.mode,
            aiModelName = aiModelName,
        ),
        avatarSeed = displayName,
        avatarUrl = resolvePlayerAvatarUrl(
            playerType = playerType,
            gameMode = state.mode,
            humanAvatarUrl = humanAvatarUrl,
            onlineOpponentAvatarUrl = onlineOpponentAvatarUrl,
        ),
        sideLabel = stringResource(if (side == Side.RED) R.string.xiangqi_side_red else R.string.xiangqi_side_black),
        statusText = resolvePlayerStatusText(
            side = side,
            playerType = playerType,
            gameMode = state.mode,
            isActiveTurn = isActiveTurn,
            playable = playable,
        ),
        statusColor = resolvePlayerStatusColor(
            side = side,
            isActiveTurn = isActiveTurn,
            playable = playable,
        ),
        onClick = if (playerType == PlayerType.LLM) {
            { onPickAiFor(side) }
        } else null,
    )
}

private fun XiangqiGameUiState.playerTypeFor(side: Side): PlayerType {
    return if (side == Side.RED) redPlayerType else blackPlayerType
}

@Composable
private fun PlayerIdentityCard(
    displayName: String,
    subtitle: String,
    avatarSeed: String,
    avatarUrl: String,
    sideLabel: String,
    statusText: String,
    statusColor: Color,
    onClick: (() -> Unit)? = null,
) {
    val cardShape = MaterialTheme.shapes.large
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                username = avatarSeed,
                avatar = avatarUrl,
                size = 40.dp,
                isLogin = true,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = sideLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = statusColor,
                )
            }
        }
    }

    if (onClick != null) {
        GlassSurface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            style = GlassStyle.Medium,
            content = content,
        )
    } else {
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            style = GlassStyle.Medium,
            shape = cardShape,
        ) {
            content()
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
        modifier = Modifier.fillMaxWidth(),
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
        modifier = Modifier.fillMaxWidth(),
        style = GlassStyle.Medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            GlassTonalIconButton(onClick = onUndo, enabled = allowUndoRedo) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo, contentDescription = null)
            }
            GlassTonalIconButton(onClick = onRedo, enabled = allowUndoRedo) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo, contentDescription = null)
            }
            GlassTonalIconButton(onClick = onAnalysis) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAnalytics, contentDescription = null)
            }
            GlassTonalIconButton(onClick = onExport) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare, contentDescription = null)
            }
            GlassTonalIconButton(onClick = onRestart) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh, contentDescription = null)
            }
            if (showResign) {
                GlassTonalIconButton(onClick = onResign) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlag, contentDescription = null)
                }
            }
            GlassTonalIconButton(onClick = onRename) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit, contentDescription = null)
            }
            GlassTonalIconButton(onClick = onToggleFullscreen) {
                Icon(
                    imageVector = if (isImmersive) Icons.Outlined.FullscreenExit else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Fullscreen,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun ExportDialog(
    exportContent: String,
    onDismiss: () -> Unit,
    onFen: () -> Unit,
    onJson: () -> Unit,
    onText: () -> Unit,
    onShareImage: () -> Unit,
    isSharing: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.xiangqi_export_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassTonalButton(onClick = onFen, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.xiangqi_export_fen))
                    }
                    GlassTonalButton(onClick = onJson, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.xiangqi_export_json))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassTonalButton(onClick = onText, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.xiangqi_export_text))
                    }
                    GlassTonalButton(
                        onClick = onShareImage,
                        enabled = !isSharing,
                        modifier = Modifier.weight(1f),
                    ) {
                        if (isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        } else {
                            Text(stringResource(R.string.xiangqi_share_image))
                        }
                    }
                }
                Text(
                    text = exportContent.ifBlank { stringResource(R.string.xiangqi_empty_export) },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            GlassTonalButton(onClick = onDismiss) {
                Text(stringResource(R.string.xiangqi_close))
            }
        },
    )
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
