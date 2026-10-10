package com.wanbaohe.chess.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.utils.capturable.CaptureController
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalImageShareProvider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.boardgame.ui.BoardGameOverOverlay
import com.wanbaohe.boardgame.ui.BoardStartOverlay
import com.wanbaohe.boardgame.ui.LocalBoardGameImmersiveModeState
import com.wanbaohe.chess.R
import com.wanbaohe.chess.application.port.outbound.MoveDecision
import com.wanbaohe.chess.component.ChessGameComponent
import com.wanbaohe.chess.component.ChessGameUiState
import com.wanbaohe.chess.data.TextExportLabels
import com.wanbaohe.chess.domain.Notation
import com.wanbaohe.chess.domain.model.ConnectionState
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.presentation.localizedGameResultText
import com.wanbaohe.chess.ui.ChessOpponentPicker
import com.wanbaohe.chess.ui.board.ChessBoard
import com.wanbaohe.chess.ui.board.glyph
import kotlinx.coroutines.launch

@Composable
fun ChessGameScreen(component: ChessGameComponent, modifier: Modifier = Modifier, showChrome: Boolean = true) {
    val state = component.uiState
    val draft = component.setupDraft
    if (draft != null) {
        val initial = state.status == GameStatus.NOT_STARTED && state.startedAt == 0L
        ChessSetupScreen(
            draft, component::updateSetup, component::cancelSetup, component::finishSetup,
            confirmLabel = stringResource(if (initial) R.string.chess_setup_apply else R.string.chess_setup_new_game),
            modifier = modifier, busy = state.isUpdating, preservesOriginal = !initial, bottomSide = state.bottomSide(),
        )
        return
    }
    var exportDialog by remember { mutableStateOf(false) }
    var showOrigin by remember { mutableStateOf(false) }
    var pickingSide by remember { mutableStateOf<Side?>(null) }
    val exportLabels = TextExportLabels(
        stringResource(R.string.chess_export_header), stringResource(R.string.chess_export_title_label),
        stringResource(R.string.chess_export_initial_fen_label), stringResource(R.string.chess_export_result_label),
    )
    val resultText = localizedGameResultText(state.status)
    val capture = rememberCaptureController()
    val scope = rememberCoroutineScope()
    val shareProvider = LocalImageShareProvider.current
    var capturing by remember { mutableStateOf(false) }

    fun shareScreenshot() {
        scope.launch {
            capturing = true
            runCatching {
                val bitmap = capture.bitmap()
                shareProvider.shareImage(
                    ImageInfo(width = bitmap.width, height = bitmap.height, imageFormat = ImageFormat.Png.Lossless, originalUri = "chess_screenshot"),
                    bitmap,
                ) { capturing = false }
            }.onFailure { capturing = false }
        }
    }

    val content: @Composable (Modifier) -> Unit = {
        ChessGameContent(component, it, capture, { exportDialog = true }, { side -> pickingSide = side }, { showOrigin = true })
    }
    if (showChrome) {
        BaseScreen(
            title = state.title.ifBlank { stringResource(R.string.chess_game_title) },
            onGoBack = component.onGoBack,
        ) { content(Modifier.fillMaxSize()) }
    } else content(modifier)

    if (exportDialog) ExportDialog(
        exportContent = state.exportContent,
        onExportFen = component::exportFen,
        onExportJson = component::exportJson,
        onExportText = { component.exportText(exportLabels, resultText) },
        onDismiss = { exportDialog = false; component.dismissExport() },
        onShareImage = ::shareScreenshot,
        isSharingImage = capturing,
    )
    if (component.showResignConfirm) Confirmation(
        stringResource(R.string.chess_resign_confirm_title), stringResource(R.string.chess_resign_confirm_message),
        component::resign, { component.showResignConfirm = false },
    )
    if (component.showRestartConfirm) Confirmation(
        stringResource(R.string.chess_restart_confirm_title), stringResource(R.string.chess_restart_confirm_message),
        component::restart, { component.showRestartConfirm = false },
    )
    if (component.showStandardGameConfirm) Confirmation(
        stringResource(R.string.chess_standard_game), stringResource(R.string.chess_standard_game_confirm),
        component::prepareStandardGame, { component.showStandardGameConfirm = false },
    )
    if (component.showRenameDialog) {
        var title by remember { mutableStateOf(state.title) }
        AlertDialog(
            onDismissRequest = { component.showRenameDialog = false },
            title = { Text(stringResource(R.string.chess_rename_game)) },
            text = {
                GlassOutlinedTextField(
                    value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.chess_rename_hint)) }, singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = { component.renameGame(title) }, enabled = title.isNotBlank()) {
                    Text(stringResource(R.string.chess_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { component.showRenameDialog = false }) { Text(stringResource(R.string.chess_cancel)) }
            },
        )
    }
    val promotions = state.interaction.pendingPromotionMoves
    if (promotions.isNotEmpty()) AlertDialog(
        onDismissRequest = component::cancelPromotion,
        title = { Text(stringResource(R.string.chess_promotion_title)) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                promotions.forEach { move ->
                    TextButton(onClick = { component.commitPromotion(move) }, modifier = Modifier.weight(1f)) {
                        Text(Piece(move.piece.side, requireNotNull(move.promotion)).glyph(), fontSize = 28.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = component::cancelPromotion) { Text(stringResource(R.string.chess_cancel)) }
        },
    )
    pickingSide?.let { side ->
        val engines by component.allAiEngines.collectAsState()
        val models by component.modelsByProvider.collectAsState()
        ChessOpponentPicker(
            config = component.gameAiConfigFor(side),
            workingEngine = component.currentEngineForSide(side),
            allEngines = engines,
            modelsByProvider = models,
            onSourceSelected = { component.switchAiSourceForSide(side, it) },
            onModelSelected = { engine, model -> component.switchAiModelForSide(side, engine, model) },
            onDismiss = { pickingSide = null },
        )
    }
    state.origin?.let { origin ->
        if (showOrigin) ChessOriginDialog(origin, { showOrigin = false; component.openSourceGame() }, { showOrigin = false })
    }
}

@Composable
private fun Confirmation(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) }, text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.chess_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.chess_cancel)) } },
    )
}

@Composable
private fun ChessGameContent(
    component: ChessGameComponent,
    modifier: Modifier,
    capture: CaptureController,
    onExport: () -> Unit,
    onPickAiFor: (Side) -> Unit,
    onShowOrigin: () -> Unit,
) {
    val state = component.uiState
    val last = state.history.firstOrNull { it.ply == state.currentPly }
    val lastMove = last?.let { Notation.parseMove(it.moveUcci) }
    val fallback = last?.takeIf { MoveDecision.isLocalFallback(it.aiReason) }
    val disconnected = state.mode == GameMode.ONLINE_PVP &&
        state.onlineConnectionState in setOf(ConnectionState.OPPONENT_DISCONNECTED, ConnectionState.ERROR)
    val error = state.errorMessage.isNotBlank() && state.errorMessage != "AI_FALLBACK"

    BoxWithConstraints(modifier.fillMaxSize()) {
        val statusMax = minOf(110.dp, maxHeight / 4).coerceAtLeast(48.dp)
        val compactHeight = maxHeight < 450.dp
        Column(
            Modifier.fillMaxSize().padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.fillMaxWidth().weight(1f).capturable(capture),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ChessPlayersRow(state, onPickAiFor, onShowOrigin, Modifier.fillMaxWidth().padding(horizontal = 12.dp))
                Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                    Box(contentAlignment = Alignment.Center) {
                        ChessBoard(
                            state.boardState, state.interaction.selectedPoint, state.interaction.candidateTargets,
                            component::onCellTap, bottomSide = state.bottomSide(), lastMove = lastMove,
                        )
                        if (!state.isLoaded) CircularProgressIndicator()
                        if (state.isLoaded && !state.isUpdating &&
                            (state.status == GameStatus.NOT_STARTED || state.status == GameStatus.PAUSED)
                        ) BoardStartOverlay(
                            startLabel = stringResource(
                                if (state.status == GameStatus.PAUSED) R.string.chess_resume_action else R.string.chess_start_action,
                            ),
                            onStart = component::start,
                        )
                        if (compactHeight && state.status.isFinished() && component.showGameOverOverlay) {
                            CompactGameOverDialog(component, state)
                        }
                        if (!compactHeight && state.status.isFinished() && component.showGameOverOverlay) BoardGameOverOverlay(
                            resultTitle = localizedGameResultText(state.status),
                            resultSubtitle = stringResource(when (state.status) {
                                GameStatus.WHITE_WINS -> R.string.chess_game_over_subtitle_white
                                GameStatus.BLACK_WINS -> R.string.chess_game_over_subtitle_black
                                GameStatus.DRAW -> R.string.chess_game_over_subtitle_draw
                                else -> R.string.chess_game_over_subtitle_resign
                            }),
                            emphasizeResult = state.status != GameStatus.DRAW,
                            restartLabel = stringResource(R.string.chess_game_over_play_again),
                            reviewLabel = stringResource(R.string.chess_game_over_review),
                            backLabel = stringResource(R.string.chess_game_over_back),
                            onRestart = component::restart,
                            onReview = component::openAnalysis,
                            onBack = component.onGoBack,
                            dismissLabel = stringResource(R.string.chess_setup_return),
                            onDismiss = component::dismissGameOverOverlay,
                        )
                    }
                }
            }
            GameActions(component, onExport)
            if (state.status == GameStatus.CHECK || state.status.isFinished()) TextButton(
                onClick = { if (state.status.isFinished()) component.reopenGameOverOverlay() },
                modifier = Modifier.heightIn(max = 32.dp),
            ) {
                Text(
                    if (state.status == GameStatus.CHECK) stringResource(R.string.chess_check) else localizedGameResultText(state.status),
                    style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            if (error || fallback != null || disconnected) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).heightIn(max = statusMax),
                    style = GlassStyle.Medium,
                ) {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (error) {
                            Text(
                                state.errorMessage.takeUnless { it == "AI_ERROR" } ?: stringResource(R.string.chess_ai_error),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (state.mode == GameMode.HUMAN_VS_LLM || state.mode == GameMode.LLM_VS_LLM) {
                                    TextButton(onClick = component::retryAiMove, enabled = !state.isUpdating, modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.chess_retry_ai), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    TextButton(onClick = { onPickAiFor(state.boardState.sideToMove) }, modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.chess_player_change), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                TextButton(onClick = component::dismissError, modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.chess_close), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        if (fallback != null) {
                            Text(stringResource(R.string.chess_ai_local_fallback_title), style = MaterialTheme.typography.labelMedium)
                            Text(
                                stringResource(R.string.chess_ai_local_fallback_message,
                                    fallback.aiReason.removePrefix(MoveDecision.LOCAL_FALLBACK_MARKER)),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (disconnected) Text(
                            stringResource(R.string.chess_signaling_disconnected),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactGameOverDialog(component: ChessGameComponent, state: ChessGameUiState) {
    AlertDialog(
        onDismissRequest = component::dismissGameOverOverlay,
        title = { Text(localizedGameResultText(state.status)) },
        text = {
            TextButton(onClick = component::restart, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.chess_game_over_play_again), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        },
        confirmButton = {
            TextButton(onClick = component::openAnalysis) { Text(stringResource(R.string.chess_game_over_review)) }
        },
        dismissButton = {
            TextButton(onClick = component::dismissGameOverOverlay) { Text(stringResource(R.string.chess_setup_return)) }
        },
    )
}

@Composable
private fun GameActions(component: ChessGameComponent, onExport: () -> Unit) {
    val state = component.uiState
    val immersive = LocalBoardGameImmersiveModeState.current
    var more by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassTonalIconButton(onClick = component::undo, enabled = component.canUndo) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo, stringResource(R.string.chess_undo), Modifier.size(20.dp))
        }
        GlassTonalIconButton(onClick = component::redo, enabled = component.canRedo) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo, stringResource(R.string.chess_redo), Modifier.size(20.dp))
        }
        GlassTonalIconButton(onClick = component::beginSetup, enabled = component.canSetup) {
            Icon(Icons.Outlined.Edit, stringResource(R.string.chess_setup_action), Modifier.size(20.dp))
        }
        GlassTonalIconButton(onClick = { immersive?.toggle() }, enabled = immersive != null) {
            Icon(
                if (immersive?.isImmersive == true) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen,
                stringResource(R.string.chess_fullscreen), Modifier.size(20.dp),
            )
        }
        Box {
            GlassTonalIconButton(onClick = { more = true }, enabled = state.isLoaded && !state.isUpdating) {
                Icon(Icons.Outlined.MoreVert, stringResource(R.string.chess_more_actions), Modifier.size(20.dp))
            }
            DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                @Composable
                fun action(label: Int, enabled: Boolean = true, click: () -> Unit) {
                    DropdownMenuItem(
                        text = { Text(stringResource(label)) }, enabled = enabled,
                        onClick = { more = false; click() },
                    )
                }
                action(R.string.chess_analysis_title, click = component::openAnalysis)
                action(R.string.chess_export, click = onExport)
                action(R.string.chess_restart_confirm_title) { component.showRestartConfirm = true }
                if (state.mode != GameMode.ONLINE_PVP) action(R.string.chess_standard_game) { component.showStandardGameConfirm = true }
                if ((state.status == GameStatus.PLAYING || state.status == GameStatus.CHECK) && state.mode != GameMode.LLM_VS_LLM) {
                    action(R.string.chess_resign_confirm_title) { component.showResignConfirm = true }
                }
                action(R.string.chess_rename_game) { component.showRenameDialog = true }
                if (state.origin != null) action(R.string.chess_setup_continue_source, click = component::openSourceGame)
                if (state.status == GameStatus.PLAYING || state.status == GameStatus.CHECK) {
                    action(R.string.chess_pause_action) { component.pause() }
                }
            }
        }
    }
}

private fun GameStatus.isFinished(): Boolean =
    this == GameStatus.WHITE_WINS || this == GameStatus.BLACK_WINS || this == GameStatus.DRAW || this == GameStatus.RESIGNED
