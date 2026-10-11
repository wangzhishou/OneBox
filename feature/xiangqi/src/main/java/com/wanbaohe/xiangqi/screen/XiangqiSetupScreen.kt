package com.wanbaohe.xiangqi.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
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
import com.wanbaohe.boardgame.R as BoardGameR
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
import com.wanbaohe.xiangqi.ui.board.xiangqiBoardAspectRatio
import com.wanbaohe.xiangqi.ui.board.pieceLabel
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.wanbaohe.xiangqi.router.LocalXiangqiImmersiveModeState

@Composable
fun XiangqiSetupScreen(
    draft: BoardSetupDraft,
    onDraftChange: (BoardSetupDraft) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    preservesOriginal: Boolean = false,
) {
    val issue = remember(draft.boardState) { SetupPositionValidator.validate(draft.startPosition()) }
    var more by remember { mutableStateOf(false) }
    val immersive = LocalXiangqiImmersiveModeState.current
    val fontScale = LocalDensity.current.fontScale
    BackHandler {
        if (immersive?.isImmersive == true) immersive.exitImmersive()
        else if (!busy) onCancel()
    }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BoardGameSetupHeader(
            title = stringResource(R.string.xiangqi_setup_title),
            sides = listOf(Side.RED, Side.BLACK).map { side ->
                BoardGameSideOption(
                    label = stringResource(if (side == Side.RED) R.string.xiangqi_setup_red_first else R.string.xiangqi_setup_black_first),
                    indicatorColor = if (side == Side.RED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    selected = draft.boardState.sideToMove == side,
                    onClick = { onDraftChange(draft.setSideToMove(side)) },
                )
            },
            enabled = !busy,
        )
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val scrollBody = maxHeight < 300.dp || fontScale > 1.3f
            Column(
                modifier = Modifier.fillMaxSize()
                    .then(if (scrollBody) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth()
                        .then(if (scrollBody) Modifier.height(280.dp) else Modifier.weight(1f)),
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
                        boardAspectRatio = xiangqiBoardAspectRatio(maxWidth.value, maxHeight.value),
                    )
                }
                GlassSurface(style = GlassStyle.Medium, shape = MaterialTheme.shapes.large) {
                    Column(Modifier.fillMaxWidth().padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(Side.RED, Side.BLACK).forEach { side ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                PieceType.entries.forEach { type ->
                                    val piece = Piece(side, type)
                                    val label = "${stringResource(if (side == Side.RED) R.string.xiangqi_side_red else R.string.xiangqi_side_black)} ${pieceLabel(piece)}"
                                    val selected = draft.tool == SetupTool.PLACE && draft.placementPiece == piece
                                    BoardGamePaletteItem(
                                        selected = selected,
                                        onClick = { onDraftChange(draft.selectPiece(piece)) },
                                        enabled = !busy,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .semantics { contentDescription = label },
                                    ) {
                                        Box(Modifier.size(30.dp)) {
                                            PieceDisc(piece, selected)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                BoardGameSetupHint(
                    text = when {
                        issue != null -> setupIssueText(issue)
                        preservesOriginal && draft.hasChanges -> stringResource(R.string.xiangqi_setup_preserves_original)
                        else -> stringResource(R.string.xiangqi_setup_hint)
                    },
                    isError = issue != null,
                )
            }
        }
        ActionBar(
            actions = listOf(
                BoardGameAction(
                    icon = Icons.Outlined.OpenWith, contentDescription = stringResource(R.string.xiangqi_setup_move),
                    enabled = !busy, selected = draft.tool == SetupTool.MOVE,
                    onClick = { onDraftChange(draft.selectTool(SetupTool.MOVE)) },
                ),
                BoardGameAction(
                    icon = Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.xiangqi_setup_erase),
                    enabled = !busy, selected = draft.tool == SetupTool.ERASE,
                    onClick = { onDraftChange(draft.selectTool(SetupTool.ERASE)) },
                ),
                BoardGameAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
                    contentDescription = stringResource(R.string.xiangqi_undo),
                    enabled = !busy && draft.undoBoards.isNotEmpty(), onClick = { onDraftChange(draft.undo()) },
                ),
                BoardGameAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
                    contentDescription = stringResource(R.string.xiangqi_redo),
                    enabled = !busy && draft.redoBoards.isNotEmpty(), onClick = { onDraftChange(draft.redo()) },
                ),
            ),
        ) {
            Box(Modifier.weight(1f)) {
                BoardGameToolButton(
                    icon = Icons.Outlined.MoreVert, label = stringResource(BoardGameR.string.boardgame_more),
                    onClick = { more = true }, enabled = !busy, modifier = Modifier.fillMaxWidth(),
                )
                DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.xiangqi_cancel)) },
                        onClick = { more = false; onCancel() },
                    )
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
        BoardGamePillButton(
            label = stringResource(R.string.xiangqi_setup_return),
            onClick = onConfirm,
            enabled = !busy && (!draft.hasChanges || issue == null),
            modifier = Modifier.fillMaxWidth(),
            primary = true,
        )
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
