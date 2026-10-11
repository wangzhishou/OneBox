package com.wanbaohe.chess.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.boardgame.model.BoardGameAction
import com.wanbaohe.boardgame.model.BoardGameSideOption
import com.wanbaohe.boardgame.ui.ActionBar
import com.wanbaohe.boardgame.ui.BoardGamePaletteItem
import com.wanbaohe.boardgame.ui.BoardGamePillButton
import com.wanbaohe.boardgame.ui.BoardGameSetupHeader
import com.wanbaohe.boardgame.ui.BoardGameSetupHint
import com.wanbaohe.boardgame.ui.BoardGameToolButton
import com.wanbaohe.boardgame.ui.LocalBoardGameImmersiveModeState
import com.wanbaohe.boardgame.R as BoardGameR
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
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedDropdownMenu

@Composable
fun ChessSetupScreen(
    draft: BoardSetupDraft,
    onDraftChange: (BoardSetupDraft) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    preservesOriginal: Boolean = false,
    bottomSide: Side = Side.WHITE,
) {
    val immersive = LocalBoardGameImmersiveModeState.current
    BackHandler {
        if (immersive?.isImmersive == true) immersive.exitImmersive()
        else if (!busy) onCancel()
    }
    val issue = remember(draft.boardState) { SetupPositionValidator.validate(draft.startPosition()) }
    var more by remember { mutableStateOf(false) }
    val warning = when {
        issue != null -> setupIssueText(issue)
        preservesOriginal && draft.hasChanges -> stringResource(R.string.chess_setup_preserves_original)
        else -> stringResource(R.string.chess_setup_hint)
    }
    val fontScale = LocalDensity.current.fontScale
    val paletteItemHeight = maxOf(44.dp, 36.dp * fontScale)
    Column(
        modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BoardGameSetupHeader(
            title = stringResource(R.string.chess_setup_title),
            sides = listOf(Side.WHITE, Side.BLACK).map { side ->
                BoardGameSideOption(
                    label = stringResource(if (side == Side.WHITE) R.string.chess_setup_white_first else R.string.chess_setup_black_first),
                    indicatorColor = if (side == Side.WHITE) Color.White else Color.Black,
                    selected = draft.boardState.sideToMove == side,
                    onClick = { onDraftChange(draft.setSideToMove(side)) },
                )
            },
            enabled = !busy,
        )
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val paletteHeight = paletteItemHeight * 2 + 12.dp
            val warningHeight = 40.dp * fontScale + 8.dp
            val boardHeight = minOf(
                maxWidth,
                (maxHeight - paletteHeight - warningHeight - 8.dp).coerceAtLeast(minOf(maxWidth, 200.dp)),
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier.fillMaxWidth().height(boardHeight),
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
                GlassSurface(style = GlassStyle.Medium, shape = MaterialTheme.shapes.large) {
                    Column(Modifier.fillMaxWidth().padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(Side.WHITE, Side.BLACK).forEach { side ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PieceType.entries.forEach { type ->
                                    val piece = Piece(side, type)
                                    val label = "${stringResource(if (side == Side.WHITE) R.string.chess_side_white else R.string.chess_side_black)} ${pieceLabel(type)}"
                                    BoardGamePaletteItem(
                                        selected = draft.tool == SetupTool.PLACE && draft.placementPiece == piece,
                                        onClick = { onDraftChange(draft.selectPiece(piece)) },
                                        enabled = !busy,
                                        modifier = Modifier.weight(1f).height(paletteItemHeight)
                                            .semantics { contentDescription = label },
                                    ) {
                                        Text(
                                            piece.glyph(), fontSize = 28.sp,
                                            color = MaterialTheme.colorScheme.onSurface, maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                BoardGameSetupHint(
                    text = warning,
                    isError = issue != null,
                )
            }
        }
        ActionBar(
            modifier = Modifier.fillMaxWidth(),
            actions = listOf(
                BoardGameAction(
                    icon = Icons.Outlined.OpenWith, contentDescription = stringResource(R.string.chess_setup_move),
                    enabled = !busy, selected = draft.tool == SetupTool.MOVE,
                    onClick = { onDraftChange(draft.selectTool(SetupTool.MOVE)) },
                ),
                BoardGameAction(
                    icon = Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.chess_setup_erase),
                    enabled = !busy, selected = draft.tool == SetupTool.ERASE,
                    onClick = { onDraftChange(draft.selectTool(SetupTool.ERASE)) },
                ),
                BoardGameAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
                    contentDescription = stringResource(R.string.chess_undo),
                    enabled = !busy && draft.undoBoards.isNotEmpty(),
                    onClick = { onDraftChange(draft.undo()) },
                ),
                BoardGameAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
                    contentDescription = stringResource(R.string.chess_redo),
                    enabled = !busy && draft.redoBoards.isNotEmpty(),
                    onClick = { onDraftChange(draft.redo()) },
                ),
            ),
        ) {
            Box(Modifier.weight(1f)) {
                BoardGameToolButton(
                    icon = Icons.Outlined.MoreVert, label = stringResource(BoardGameR.string.boardgame_more),
                    onClick = { more = true }, enabled = !busy, modifier = Modifier.fillMaxWidth(),
                )
                EnhancedDropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.chess_cancel)) }, enabled = !busy,
                        onClick = { more = false; onCancel() })
                    DropdownMenuItem(text = { Text(stringResource(R.string.chess_setup_reset)) },
                        enabled = !busy, onClick = { more = false; onDraftChange(draft.reset()) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.chess_setup_clear)) },
                        enabled = !busy, onClick = { more = false; onDraftChange(draft.clear()) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.chess_export_fen)) },
                        enabled = !busy, onClick = { more = false; Clipboard.copy(FenCodec.encode(draft.boardState), R.string.chess_copied) })
                    if (immersive != null) DropdownMenuItem(text = { Text(stringResource(R.string.chess_fullscreen)) },
                        enabled = !busy, onClick = { more = false; immersive.toggle() })
                }
            }
        }
        BoardGamePillButton(
            label = stringResource(R.string.chess_setup_return),
            onClick = onConfirm,
            enabled = !busy && (!draft.hasChanges || issue == null),
            modifier = Modifier.fillMaxWidth(),
            primary = true,
        )
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
