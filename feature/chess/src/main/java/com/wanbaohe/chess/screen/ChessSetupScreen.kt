package com.wanbaohe.chess.screen

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.boardgame.ui.LocalBoardGameImmersiveModeState
import com.wanbaohe.chess.R
import com.wanbaohe.chess.domain.BoardSetupDraft
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.SetupPositionIssue
import com.wanbaohe.chess.domain.SetupPositionValidator
import com.wanbaohe.chess.domain.SetupTool
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.ui.board.ChessBoard
import com.wanbaohe.chess.ui.board.glyph

@Composable
fun ChessSetupScreen(
    draft: BoardSetupDraft,
    onDraftChange: (BoardSetupDraft) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    preservesOriginal: Boolean = false,
    bottomSide: Side = Side.WHITE,
) {
    BackHandler(onBack = onCancel)
    val issue = remember(draft.boardState) { SetupPositionValidator.validate(draft.startPosition()) }
    var more by remember { mutableStateOf(false) }
    val immersive = LocalBoardGameImmersiveModeState.current
    Column(
        modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val scroll = maxHeight < 320.dp
            Column(
                Modifier.fillMaxSize().then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.chess_setup_title), style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Side.entries.forEach { side ->
                        TextButton(
                            onClick = { onDraftChange(draft.setSideToMove(side)) },
                            enabled = !busy, modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                stringResource(if (side == Side.WHITE) R.string.chess_setup_white_first else R.string.chess_setup_black_first),
                                color = if (draft.boardState.sideToMove == side) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                Box(
                    Modifier.fillMaxWidth().then(if (scroll) Modifier.height(190.dp) else Modifier.weight(1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    ChessBoard(
                        boardState = draft.boardState,
                        selectedPoint = draft.selectedPoint,
                        candidateTargets = emptySet(),
                        bottomSide = bottomSide,
                        onCellTap = { file, rank -> if (!busy) onDraftChange(draft.tap(BoardPoint(file, rank))) },
                        onPieceDrop = if (busy || draft.tool != SetupTool.MOVE) null
                            else { from, to -> onDraftChange(draft.move(from, to)) },
                    )
                }
                Side.entries.forEach { side ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PieceType.entries.forEach { type ->
                            val piece = Piece(side, type)
                            val label = "${stringResource(if (side == Side.WHITE) R.string.chess_side_white else R.string.chess_side_black)} ${pieceLabel(type)}"
                            Box(
                                Modifier.weight(1f).height(40.dp)
                                    .border(
                                        1.dp, if (draft.tool == SetupTool.PLACE && draft.placementPiece == piece)
                                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        MaterialTheme.shapes.small,
                                    )
                                    .semantics { contentDescription = label }
                                    .clickable(enabled = !busy) { onDraftChange(draft.selectPiece(piece)) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(piece.glyph(), fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                Text(
                    if (issue != null) setupIssueText(issue) else stringResource(
                        if (preservesOriginal) R.string.chess_setup_preserves_original else R.string.chess_setup_hint,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (issue != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            listOf(SetupTool.MOVE to R.string.chess_setup_move, SetupTool.ERASE to R.string.chess_setup_erase).forEach { (tool, label) ->
                TextButton(onClick = { onDraftChange(draft.selectTool(tool)) }, enabled = !busy, modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(label), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        color = if (draft.tool == tool) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            GlassTonalIconButton(onClick = { onDraftChange(draft.undo()) }, enabled = !busy && draft.undoBoards.isNotEmpty()) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo, stringResource(R.string.chess_undo), Modifier.size(20.dp))
            }
            GlassTonalIconButton(onClick = { onDraftChange(draft.redo()) }, enabled = !busy && draft.redoBoards.isNotEmpty()) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo, stringResource(R.string.chess_redo), Modifier.size(20.dp))
            }
            Box {
                GlassTonalIconButton(onClick = { more = true }, enabled = !busy) {
                    Icon(Icons.Outlined.MoreVert, stringResource(R.string.chess_more_actions), Modifier.size(20.dp))
                }
                DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.chess_setup_reset)) },
                        onClick = { more = false; onDraftChange(draft.reset()) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.chess_setup_clear)) },
                        onClick = { more = false; onDraftChange(draft.clear()) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.chess_export_fen)) },
                        onClick = { more = false; Clipboard.copy(FenCodec.encode(draft.boardState), R.string.chess_copied) })
                    if (immersive != null) DropdownMenuItem(text = { Text(stringResource(R.string.chess_fullscreen)) },
                        onClick = { more = false; immersive.toggle() })
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCancel, enabled = !busy, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.chess_cancel), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            GlassTonalButton(
                onClick = onConfirm, enabled = !busy && (!draft.hasChanges || issue == null), modifier = Modifier.weight(2f),
            ) {
                Text(
                    if (draft.hasChanges) confirmLabel else stringResource(R.string.chess_setup_return),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun pieceLabel(type: PieceType): String = stringResource(when (type) {
    PieceType.KING -> R.string.chess_piece_king
    PieceType.QUEEN -> R.string.chess_piece_queen
    PieceType.ROOK -> R.string.chess_piece_rook
    PieceType.BISHOP -> R.string.chess_piece_bishop
    PieceType.KNIGHT -> R.string.chess_piece_knight
    PieceType.PAWN -> R.string.chess_piece_pawn
})

@Composable
internal fun setupIssueText(issue: SetupPositionIssue): String = when (issue) {
    is SetupPositionIssue.KingCount -> stringResource(
        R.string.chess_setup_king_count,
        stringResource(if (issue.side == Side.WHITE) R.string.chess_side_white else R.string.chess_side_black),
    )
    is SetupPositionIssue.ImpossibleMaterial -> stringResource(
        R.string.chess_setup_material,
        stringResource(if (issue.side == Side.WHITE) R.string.chess_side_white else R.string.chess_side_black),
    )
    SetupPositionIssue.KingsAdjacent -> stringResource(R.string.chess_setup_kings_adjacent)
    SetupPositionIssue.PawnOnPromotionRank -> stringResource(R.string.chess_setup_pawn_rank)
    SetupPositionIssue.InvalidCastling -> stringResource(R.string.chess_setup_castling)
    SetupPositionIssue.InvalidEnPassant -> stringResource(R.string.chess_setup_en_passant)
    SetupPositionIssue.WrongTurn -> stringResource(R.string.chess_setup_wrong_turn)
    SetupPositionIssue.NoLegalMoves -> stringResource(R.string.chess_setup_no_moves)
}
