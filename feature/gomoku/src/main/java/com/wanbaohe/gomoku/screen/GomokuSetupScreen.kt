package com.wanbaohe.gomoku.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.boardgame.ui.LocalBoardGameImmersiveModeState
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.domain.BoardSetupDraft
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.SetupPositionIssue
import com.wanbaohe.gomoku.domain.SetupPositionValidator
import com.wanbaohe.gomoku.domain.SetupTool
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.ui.board.GomokuBoard
import com.wanbaohe.gomoku.ui.board.StoneDisc

@Composable
fun GomokuSetupScreen(
    draft: BoardSetupDraft,
    onDraftChange: (BoardSetupDraft) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    preservesOriginal: Boolean = false,
    bottomSide: Side = Side.BLACK,
) {
    BackHandler(onBack = onCancel)
    val issue = remember(draft.boardState) { SetupPositionValidator.validate(draft.startPosition()) }
    var more by remember { mutableStateOf(false) }
    val immersive = LocalBoardGameImmersiveModeState.current
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val scrollBody = maxHeight < 280.dp
            Column(
                modifier = Modifier.fillMaxSize()
                    .then(if (scrollBody) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.gomoku_setup_title),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Side.entries.forEach { side ->
                        TextButton(
                            onClick = { onDraftChange(draft.setSideToMove(side)) },
                            enabled = !busy,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                stringResource(if (side == Side.BLACK) R.string.gomoku_setup_black_next else R.string.gomoku_setup_white_next),
                                color = if (draft.boardState.sideToMove == side) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .then(if (scrollBody) Modifier.height(200.dp) else Modifier.weight(1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    GomokuBoard(
                        draft.boardState, draft.selectedPoint, emptySet(),
                        { file, rank -> if (!busy) onDraftChange(draft.tap(BoardPoint(file, rank))) },
                        bottomSide = bottomSide,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Side.entries.forEach { side ->
                        Row(
                            modifier = Modifier.weight(1f).height(44.dp)
                                .border(
                                    1.dp,
                                    if (draft.tool == SetupTool.PLACE && draft.placementSide == side)
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    MaterialTheme.shapes.small,
                                )
                                .clickable(enabled = !busy) { onDraftChange(draft.selectStone(side)) }
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(24.dp)) { StoneDisc(side) }
                            Text(
                                stringResource(if (side == Side.BLACK) R.string.gomoku_setup_place_black else R.string.gomoku_setup_place_white),
                                modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
                Text(
                    if (issue != null) setupIssueText(issue) else stringResource(
                        if (preservesOriginal) R.string.gomoku_setup_preserves_original else R.string.gomoku_setup_hint,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (issue != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { onDraftChange(draft.selectTool(SetupTool.MOVE)) },
                enabled = !busy, modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.gomoku_setup_move), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = if (draft.tool == SetupTool.MOVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(
                onClick = { onDraftChange(draft.selectTool(SetupTool.ERASE)) },
                enabled = !busy, modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.gomoku_setup_erase), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = if (draft.tool == SetupTool.ERASE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            GlassTonalIconButton(onClick = { onDraftChange(draft.undo()) }, enabled = !busy && draft.undoBoards.isNotEmpty()) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo, stringResource(R.string.gomoku_undo), Modifier.size(20.dp))
            }
            GlassTonalIconButton(onClick = { onDraftChange(draft.redo()) }, enabled = !busy && draft.redoBoards.isNotEmpty()) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo, stringResource(R.string.gomoku_redo), Modifier.size(20.dp))
            }
            Box {
                GlassTonalIconButton(onClick = { more = true }, enabled = !busy) {
                    Icon(Icons.Outlined.MoreVert, stringResource(R.string.gomoku_more_actions), Modifier.size(20.dp))
                }
                DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_setup_clear), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { more = false; onDraftChange(draft.clear()) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_setup_reset), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { more = false; onDraftChange(draft.reset()) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_export_fen)) },
                        onClick = { more = false; Clipboard.copy(FenCodec.encode(draft.boardState), R.string.gomoku_copied) })
                    if (immersive != null) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_fullscreen)) },
                            onClick = { more = false; immersive.toggle() })
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCancel, enabled = !busy, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.gomoku_cancel), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            GlassTonalButton(
                onClick = onConfirm, enabled = !busy && (!draft.hasChanges || issue == null),
                modifier = Modifier.weight(2f),
            ) {
                Text(if (draft.hasChanges) confirmLabel else stringResource(R.string.gomoku_setup_return),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun setupIssueText(issue: SetupPositionIssue): String = when (issue) {
    SetupPositionIssue.FullBoard -> stringResource(R.string.gomoku_setup_full_board)
    is SetupPositionIssue.AlreadyWon -> stringResource(
        R.string.gomoku_setup_already_won,
        stringResource(if (issue.side == Side.BLACK) R.string.gomoku_side_black else R.string.gomoku_side_white),
    )
}
