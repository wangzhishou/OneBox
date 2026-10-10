package com.wanbaohe.gomoku.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.utils.BaseUtils
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.utils.capturable.CaptureController
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalImageShareProvider
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.boardgame.model.PlayerBarData
import com.wanbaohe.boardgame.ui.BoardGameOverOverlay
import com.wanbaohe.boardgame.ui.BoardStartOverlay
import com.wanbaohe.boardgame.ui.LocalBoardGameImmersiveModeState
import com.wanbaohe.boardgame.ui.PlayersBar
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.port.outbound.MoveDecision
import com.wanbaohe.gomoku.component.GomokuGameComponent
import com.wanbaohe.gomoku.component.GomokuGameUiState
import com.wanbaohe.gomoku.data.TextExportLabels
import com.wanbaohe.gomoku.domain.Notation
import com.wanbaohe.gomoku.domain.model.ConnectionState
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.presentation.localizedGameResultText
import com.wanbaohe.gomoku.ui.GomokuOpponentPicker
import com.wanbaohe.gomoku.ui.board.GomokuBoard
import kotlinx.coroutines.launch

@Composable
fun GomokuGameScreen(component: GomokuGameComponent, modifier: Modifier = Modifier, showChrome: Boolean = true) {
    val state = component.uiState
    val setup = component.setupDraft
    if (setup != null) {
        GomokuSetupScreen(
            setup, component::updateSetup, component::cancelSetup, component::finishSetup,
            stringResource(if (state.startedAt == 0L && state.currentPly == 0)
                R.string.gomoku_setup_apply else R.string.gomoku_setup_practice),
            modifier = modifier,
            busy = state.isUpdating,
            preservesOriginal = state.status != GameStatus.NOT_STARTED,
            bottomSide = state.humanOrBottomSide(),
        )
        return
    }
    var exportDialog by remember { mutableStateOf(false) }
    var pickingSide by remember { mutableStateOf<Side?>(null) }
    var showOrigin by remember { mutableStateOf(false) }
    val exportLabels = TextExportLabels(
        stringResource(R.string.gomoku_export_header),
        stringResource(R.string.gomoku_export_title_label),
        stringResource(R.string.gomoku_export_initial_fen_label),
        stringResource(R.string.gomoku_export_result_label),
        stringResource(R.string.gomoku_setup_source),
    )
    val resultText = localizedGameResultText(state.status)
    val capture = rememberCaptureController()
    val scope = rememberCoroutineScope()
    val shareProvider = LocalImageShareProvider.current
    var isCapturing by remember { mutableStateOf(false) }
    fun shareScreenshot() {
        scope.launch {
            isCapturing = true
            runCatching {
                val bitmap = capture.bitmap()
                shareProvider.shareImage(
                    ImageInfo(width = bitmap.width, height = bitmap.height, imageFormat = ImageFormat.Png.Lossless, originalUri = "gomoku_screenshot"),
                    bitmap,
                ) { isCapturing = false }
            }.onFailure { isCapturing = false }
        }
    }
    val content: @Composable (Modifier) -> Unit = { contentModifier ->
        GameContent(component, contentModifier, capture, { exportDialog = true }, { pickingSide = it }, { showOrigin = true })
    }
    if (showChrome) {
        BaseScreen(title = state.title.ifBlank { stringResource(R.string.gomoku_game_title) }, onGoBack = component.onGoBack) {
            content(Modifier.fillMaxSize())
        }
    } else content(modifier)

    if (exportDialog) {
        ExportDialog(state.exportContent, component::exportFen, component::exportJson,
            { component.exportText(exportLabels, resultText) },
            { exportDialog = false; component.dismissExport() },
            onShareImage = ::shareScreenshot, isSharingImage = isCapturing)
    }
    if (component.showResignConfirm) {
        ConfirmDialog(R.string.gomoku_resign_confirm_title, R.string.gomoku_resign_confirm_message,
            component::resign, { component.showResignConfirm = false })
    }
    if (component.showRestartConfirm) {
        ConfirmDialog(R.string.gomoku_restart_confirm_title, R.string.gomoku_restart_confirm_message,
            component::restart, { component.showRestartConfirm = false })
    }
    if (component.showStandardGameConfirm) {
        ConfirmDialog(R.string.gomoku_empty_game, R.string.gomoku_empty_game_confirm,
            component::prepareStandardGame, { component.showStandardGameConfirm = false })
    }
    if (component.showRenameDialog) {
        var title by remember { mutableStateOf(state.title) }
        AlertDialog(
            onDismissRequest = { component.showRenameDialog = false },
            title = { Text(stringResource(R.string.gomoku_rename_game)) },
            text = { GlassOutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.gomoku_rename_hint)) }, singleLine = true) },
            confirmButton = { TextButton(onClick = { component.renameGame(title) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.gomoku_confirm)) } },
            dismissButton = { TextButton(onClick = { component.showRenameDialog = false }) { Text(stringResource(R.string.gomoku_cancel)) } },
        )
    }
    pickingSide?.let { side ->
        val engines by component.allAiEngines.collectAsState()
        val models by component.modelsByProvider.collectAsState()
        GomokuOpponentPicker(
            component.savedAiConfig(side) ?: GameAiPlayerConfig.capture(component.currentSourceForSide(side), component.currentEngineForSide(side)),
            component.currentEngineForSide(side), engines, models,
            { component.switchAiSourceForSide(side, it) },
            { engine, model -> component.switchAiModelForSide(side, engine, model) },
            { pickingSide = null },
        )
    }
    state.origin?.let { origin ->
        if (showOrigin) GomokuOriginDialog(origin, { showOrigin = false; component.openSourceGame() }, { showOrigin = false })
    }
}

@Composable
private fun ConfirmDialog(title: Int, message: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.gomoku_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.gomoku_cancel)) } },
    )
}

@Composable
private fun GameContent(
    component: GomokuGameComponent,
    modifier: Modifier,
    capture: CaptureController,
    onExport: () -> Unit,
    onPickAi: (Side) -> Unit,
    onShowOrigin: () -> Unit,
) {
    val state = component.uiState
    val lastMove = state.history.firstOrNull { it.ply == state.currentPly }?.let { Notation.parse(it.moveUcci) }
    val resultText = localizedGameResultText(state.status)
    val fallback = state.history.firstOrNull { it.ply == state.currentPly && MoveDecision.isLocalFallback(it.aiReason) }
    val disconnected = state.mode == GameMode.ONLINE_PVP && state.onlineConnectionState in
        setOf(ConnectionState.OPPONENT_DISCONNECTED, ConnectionState.ERROR)
    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.weight(1f).capturable(capture), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            GomokuPlayersRow(state, onPickAi, Modifier.fillMaxWidth().padding(horizontal = 12.dp))
            Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.Center) {
                    GomokuBoard(state.boardState, state.interaction.selectedPoint, state.interaction.candidateTargets,
                        component::onCellTap, bottomSide = state.humanOrBottomSide(), lastMove = lastMove)
                    if (state.isLoaded && (state.status == GameStatus.NOT_STARTED || state.status == GameStatus.PAUSED)) {
                        BoardStartOverlay(
                            stringResource(if (state.isUpdating) R.string.gomoku_starting
                                else if (state.status == GameStatus.PAUSED) R.string.gomoku_resume_action else R.string.gomoku_start_play),
                            component::start,
                        )
                    }
                    if (resultText.isNotBlank() && component.showGameOverOverlay) {
                        BoardGameOverOverlay(
                            resultTitle = resultText,
                            restartLabel = stringResource(R.string.gomoku_same_position),
                            reviewLabel = stringResource(R.string.gomoku_game_over_review),
                            onRestart = component::restart,
                            onReview = component::openAnalysis,
                            emphasizeResult = state.status != GameStatus.DRAW,
                            dismissLabel = stringResource(R.string.gomoku_close),
                            onDismiss = { component.showGameOverOverlay = false },
                        )
                    }
                }
            }
        }
        if (resultText.isNotBlank()) {
            TextButton(onClick = { component.showGameOverOverlay = !component.showGameOverOverlay }) {
                Text(resultText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (state.errorMessage.isNotBlank() || fallback != null || disconnected) {
            Column(Modifier.fillMaxWidth().heightIn(max = 100.dp).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
                if (state.errorMessage.isNotBlank()) {
                    Text(
                        stringResource(when (state.errorMessage) {
                            "AI_OPPONENT_UNAVAILABLE" -> R.string.gomoku_ai_opponent_unavailable
                            "GAME_MISSING" -> R.string.gomoku_game_missing
                            else -> R.string.gomoku_ai_failed
                        }),
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall,
                    )
                    Row(Modifier.fillMaxWidth()) {
                        TextButton(onClick = component::retryAiMove, modifier = Modifier.weight(1f),
                            enabled = state.status == GameStatus.PLAYING && !state.isUpdating) {
                            Text(stringResource(R.string.gomoku_retry_ai), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (state.playerTypeFor(state.boardState.sideToMove) == PlayerType.LLM) {
                            TextButton(onClick = { onPickAi(state.boardState.sideToMove) }, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.gomoku_change_opponent), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        TextButton(onClick = component::dismissError, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.gomoku_close), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (fallback != null) {
                    Text(stringResource(R.string.gomoku_ai_local_fallback_title), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (disconnected) {
                    Text(stringResource(R.string.gomoku_signaling_disconnected), maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        GameControls(component, onExport, onShowOrigin)
    }
}

internal fun GomokuGameUiState.humanOrBottomSide(): Side = when (mode) {
    GameMode.HUMAN_VS_LLM -> if (blackPlayerType == PlayerType.HUMAN) Side.BLACK else Side.WHITE
    GameMode.ONLINE_PVP -> onlineMySide
    else -> Side.BLACK
}

internal fun GomokuGameUiState.playerTypeFor(side: Side): PlayerType =
    if (side == Side.BLACK) blackPlayerType else whitePlayerType

@Composable
internal fun GomokuPlayersRow(state: GomokuGameUiState, onPickAiFor: (Side) -> Unit, modifier: Modifier = Modifier) {
    val login = LocalLoginState.current
    val humanName = BaseUtils.getDisplayName(login.nickname, login.username).ifBlank { stringResource(R.string.gomoku_player_you) }
    val rightSide = state.humanOrBottomSide()
    PlayersBar(
        top = gomokuPlayerData(state, rightSide.opposite(), humanName, login.avatar.orEmpty(), onPickAiFor),
        bottom = gomokuPlayerData(state, rightSide, humanName, login.avatar.orEmpty(), onPickAiFor),
        vsLabel = stringResource(R.string.gomoku_vs_short),
        turnLabel = stringResource(R.string.gomoku_turn_badge),
        modifier = modifier,
    )
}

@Composable
private fun gomokuPlayerData(
    state: GomokuGameUiState, side: Side, humanName: String, humanAvatar: String, onPickAi: (Side) -> Unit,
): PlayerBarData {
    val type = state.playerTypeFor(side)
    val sideName = stringResource(if (side == Side.BLACK) R.string.gomoku_side_black else R.string.gomoku_side_white)
    val active = state.isLoaded && !state.isUpdating && state.status == GameStatus.PLAYING && state.boardState.sideToMove == side
    val namedHuman = type == PlayerType.HUMAN && state.mode in setOf(GameMode.HUMAN_VS_LLM, GameMode.ONLINE_PVP)
    val name = when (type) {
        PlayerType.LLM -> (if (side == Side.BLACK) state.blackAiServiceName else state.whiteAiServiceName)
            .ifBlank { stringResource(R.string.gomoku_player_ai) }
        PlayerType.REMOTE -> state.onlineOpponentName.ifBlank { stringResource(R.string.gomoku_player_remote) }
        PlayerType.HUMAN -> if (namedHuman) humanName else sideName
    }
    val detail = when {
        active && type == PlayerType.LLM && state.isAiThinking -> stringResource(R.string.gomoku_thinking)
        active && type == PlayerType.HUMAN -> stringResource(R.string.gomoku_turn_badge)
        type == PlayerType.LLM -> if (side == Side.BLACK) state.blackAiModelName else state.whiteAiModelName
        else -> ""
    }
    return PlayerBarData(
        name = name, subtitle = if (detail.isBlank()) sideName else "$sideName · $detail",
        avatarUrl = if (namedHuman) humanAvatar else if (type == PlayerType.REMOTE) state.onlineOpponentAvatarUrl else "",
        indicatorColor = if (side == Side.BLACK) MaterialTheme.colorScheme.onSurface else Color(0xFF9E9E9E),
        isActiveTurn = active,
        onClick = if (type == PlayerType.LLM && state.isLoaded && !state.isUpdating) ({ onPickAi(side) }) else null,
        actionLabel = stringResource(R.string.gomoku_change_opponent),
    )
}

@Composable
private fun GameControls(component: GomokuGameComponent, onExport: () -> Unit, onShowOrigin: () -> Unit) {
    val state = component.uiState
    val immersive = LocalBoardGameImmersiveModeState.current
    var more by remember { mutableStateOf(false) }
    val enabled = state.isLoaded && !state.isUpdating
    GlassSurface(Modifier.fillMaxWidth().padding(horizontal = 12.dp), style = GlassStyle.Medium) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically) {
            GlassTonalIconButton(onClick = component::undo, enabled = component.canUndo) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo, stringResource(R.string.gomoku_undo), Modifier.size(20.dp))
            }
            GlassTonalIconButton(onClick = component::redo, enabled = component.canRedo) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo, stringResource(R.string.gomoku_redo), Modifier.size(20.dp))
            }
            GlassTonalIconButton(onClick = component::beginSetup, enabled = component.canSetup) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit, stringResource(R.string.gomoku_setup_action), Modifier.size(20.dp))
            }
            GlassTonalIconButton(onClick = { immersive?.toggle() }) {
                Icon(if (immersive?.isImmersive == true) Icons.Outlined.FullscreenExit
                    else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Fullscreen, stringResource(R.string.gomoku_fullscreen), Modifier.size(20.dp))
            }
            Box {
                GlassTonalIconButton(onClick = { more = true }, enabled = enabled) {
                    Icon(Icons.Outlined.MoreVert, stringResource(R.string.gomoku_more_actions), Modifier.size(20.dp))
                }
                DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_library_open_analysis), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { more = false; component.openAnalysis() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_export)) },
                        onClick = { more = false; onExport() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_same_position), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { more = false; component.showRestartConfirm = true })
                    if (state.mode != GameMode.ONLINE_PVP) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_empty_game), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = { more = false; component.showStandardGameConfirm = true })
                    }
                    if (state.origin != null) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_setup_source)) },
                            onClick = { more = false; onShowOrigin() })
                    }
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_rename_game), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { more = false; component.showRenameDialog = true })
                    if (state.status == GameStatus.PLAYING && state.mode != GameMode.LLM_VS_LLM) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_resign_confirm_title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = { more = false; component.showResignConfirm = true })
                    }
                }
            }
        }
    }
}
