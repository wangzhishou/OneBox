package com.wanbaohe.xiangqi.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.domain.BoardSetupDraft
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.SetupPositionIssue
import com.wanbaohe.xiangqi.domain.SetupPositionValidator
import com.wanbaohe.xiangqi.domain.SetupTool
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.ui.board.PieceDisc
import com.wanbaohe.xiangqi.ui.board.XiangqiBoard
import com.wanbaohe.xiangqi.ui.board.pieceLabel
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.wanbaohe.xiangqi.router.LocalXiangqiImmersiveModeState

@Composable
fun XiangqiSetupScreen(
    draft: BoardSetupDraft,
    onDraftChange: (BoardSetupDraft) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    preservesOriginal: Boolean = false,
) {
    BackHandler(onBack = onCancel)
    val issue = remember(draft.boardState) { SetupPositionValidator.validate(draft.startPosition()) }
    var more by remember { mutableStateOf(false) }
    val immersive = LocalXiangqiImmersiveModeState.current

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val scrollBody = maxHeight < 300.dp
            Column(
                modifier = Modifier.fillMaxSize()
                    .then(if (scrollBody) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.xiangqi_setup_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
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
                                stringResource(if (side == Side.RED) R.string.xiangqi_setup_red_first else R.string.xiangqi_setup_black_first),
                                color = if (draft.boardState.sideToMove == side)
                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .then(if (scrollBody) Modifier.height(180.dp) else Modifier.weight(1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    XiangqiBoard(
                        boardState = draft.boardState,
                        selectedPoint = draft.selectedPoint,
                        candidateTargets = emptySet(),
                        onCellTap = { file, rank ->
                            if (!busy) onDraftChange(draft.tap(BoardPoint(file, rank)))
                        },
                        onPieceDrop = if (busy || draft.tool != SetupTool.MOVE) null
                            else { from, to -> onDraftChange(draft.move(from, to)) },
                    )
                }
                GlassSurface(style = GlassStyle.Medium) {
                    Column(Modifier.fillMaxWidth().padding(4.dp)) {
                        Side.entries.forEach { side ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PieceType.entries.forEach { type ->
                                    val piece = Piece(side, type)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .border(
                                                width = 1.dp,
                                                color = if (draft.tool == SetupTool.PLACE && draft.placementPiece == piece)
                                                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = MaterialTheme.shapes.small,
                                            )
                                            .clickable(enabled = !busy) { onDraftChange(draft.selectPiece(piece)) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(Modifier.size(30.dp)) {
                                            PieceDisc(piece, draft.tool == SetupTool.PLACE && draft.placementPiece == piece)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    text = if (issue != null) setupIssueText(issue) else stringResource(
                        if (preservesOriginal) R.string.xiangqi_setup_preserves_original else R.string.xiangqi_setup_hint,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (issue != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                onClick = { onDraftChange(draft.selectTool(SetupTool.MOVE)) },
                enabled = !busy,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(R.string.xiangqi_setup_move),
                    color = if (draft.tool == SetupTool.MOVE) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(
                onClick = { onDraftChange(draft.selectTool(SetupTool.ERASE)) },
                enabled = !busy,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(R.string.xiangqi_setup_erase),
                    color = if (draft.tool == SetupTool.ERASE) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            GlassTonalIconButton(onClick = { onDraftChange(draft.undo()) }, enabled = !busy && draft.undoBoards.isNotEmpty()) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo, stringResource(R.string.xiangqi_undo), Modifier.size(20.dp))
            }
            GlassTonalIconButton(onClick = { onDraftChange(draft.redo()) }, enabled = !busy && draft.redoBoards.isNotEmpty()) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo, stringResource(R.string.xiangqi_redo), Modifier.size(20.dp))
            }
            Box {
                GlassTonalIconButton(onClick = { more = true }, enabled = !busy) {
                    Icon(Icons.Outlined.MoreVert, stringResource(R.string.xiangqi_more_actions), Modifier.size(20.dp))
                }
                DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.xiangqi_setup_reset)) },
                        onClick = { more = false; onDraftChange(draft.reset()) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.xiangqi_setup_clear)) },
                        onClick = { more = false; onDraftChange(draft.clear()) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.xiangqi_export_fen)) },
                        onClick = {
                            more = false
                            Clipboard.copy(FenCodec.encode(draft.boardState), R.string.xiangqi_copied)
                        },
                    )
                    if (immersive != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.xiangqi_fullscreen)) },
                            onClick = { more = false; immersive.toggle() },
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCancel, enabled = !busy) {
                Text(stringResource(R.string.xiangqi_cancel))
            }
            GlassTonalButton(
                onClick = onConfirm,
                enabled = !busy && (!draft.hasChanges || issue == null),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    if (draft.hasChanges) confirmLabel else stringResource(R.string.xiangqi_setup_return),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun setupIssueText(issue: SetupPositionIssue): String {
    fun sideLabel(side: Side): Int =
        if (side == Side.RED) R.string.xiangqi_side_red else R.string.xiangqi_side_black
    return when (issue) {
        is SetupPositionIssue.KingCount -> stringResource(R.string.xiangqi_setup_king_count, stringResource(sideLabel(issue.side)))
        is SetupPositionIssue.TooManyPieces -> stringResource(
            R.string.xiangqi_setup_piece_count, stringResource(sideLabel(issue.side)),
            pieceLabel(Piece(issue.side, issue.type)), SetupPositionValidator.maxCount(issue.type),
        )
        is SetupPositionIssue.InvalidSquare -> stringResource(
            R.string.xiangqi_setup_invalid_square, stringResource(sideLabel(issue.side)), pieceLabel(Piece(issue.side, issue.type)),
        )
        SetupPositionIssue.KingsFacing -> stringResource(R.string.xiangqi_setup_kings_facing)
        SetupPositionIssue.WrongTurn -> stringResource(R.string.xiangqi_setup_wrong_turn)
        SetupPositionIssue.NoLegalMoves -> stringResource(R.string.xiangqi_setup_no_moves)
    }
}
