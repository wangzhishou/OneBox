package com.wanbaohe.gomoku.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.wanbaohe.boardgame.R as BoardGameR
import com.wanbaohe.boardgame.model.BoardGameAction
import com.wanbaohe.boardgame.model.BoardGameSideOption
import com.wanbaohe.boardgame.ui.ActionBar
import com.wanbaohe.boardgame.ui.BoardGamePaletteItem
import com.wanbaohe.boardgame.ui.BoardGamePillButton
import com.wanbaohe.boardgame.ui.BoardGameSetupHeader
import com.wanbaohe.boardgame.ui.BoardGameSetupHint
import com.wanbaohe.boardgame.ui.BoardGameToolButton
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
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedDropdownMenu

@Composable
fun GomokuSetupScreen(
    draft: BoardSetupDraft,
    onDraftChange: (BoardSetupDraft) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    preservesOriginal: Boolean = false,
    bottomSide: Side = Side.BLACK,
) {
    val issue = remember(draft.boardState) { SetupPositionValidator.validate(draft.startPosition()) }
    var more by remember { mutableStateOf(false) }
    val immersive = LocalBoardGameImmersiveModeState.current
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
            title = stringResource(R.string.gomoku_setup_title),
            sides = Side.entries.map { side ->
                BoardGameSideOption(
                    label = stringResource(if (side == Side.BLACK) R.string.gomoku_setup_black_next else R.string.gomoku_setup_white_next),
                    indicatorColor = if (side == Side.BLACK) Color.Black else Color.White,
                    selected = draft.boardState.sideToMove == side,
                    onClick = { onDraftChange(draft.setSideToMove(side)) },
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val scrollBody = maxHeight < 300.dp || fontScale > 1.3f
            Column(
                modifier = Modifier.fillMaxSize()
                    .then(if (scrollBody) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .then(if (scrollBody) Modifier else Modifier.weight(1f)),
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
                        BoardGamePaletteItem(
                            selected = draft.tool == SetupTool.PLACE && draft.placementSide == side,
                            onClick = { onDraftChange(draft.selectStone(side)) },
                            enabled = !busy,
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(28.dp)) { StoneDisc(side) }
                                Text(
                                    stringResource(if (side == Side.BLACK) R.string.gomoku_setup_place_black else R.string.gomoku_setup_place_white),
                                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                }
                BoardGameSetupHint(
                    text = when {
                        issue != null -> setupIssueText(issue)
                        preservesOriginal && draft.hasChanges -> stringResource(R.string.gomoku_setup_preserves_original)
                        else -> stringResource(R.string.gomoku_setup_hint)
                    },
                    isError = issue != null,
                )
            }
        }
        ActionBar(
            actions = listOf(
                BoardGameAction(
                    icon = Icons.Outlined.OpenWith,
                    contentDescription = stringResource(R.string.gomoku_setup_move),
                    enabled = !busy,
                    selected = draft.tool == SetupTool.MOVE,
                    onClick = { onDraftChange(draft.selectTool(SetupTool.MOVE)) },
                ),
                BoardGameAction(
                    icon = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.gomoku_setup_erase),
                    enabled = !busy,
                    selected = draft.tool == SetupTool.ERASE,
                    onClick = { onDraftChange(draft.selectTool(SetupTool.ERASE)) },
                ),
                BoardGameAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUndo,
                    contentDescription = stringResource(R.string.gomoku_undo),
                    enabled = !busy && draft.undoBoards.isNotEmpty(),
                    onClick = { onDraftChange(draft.undo()) },
                ),
                BoardGameAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRedo,
                    contentDescription = stringResource(R.string.gomoku_redo),
                    enabled = !busy && draft.redoBoards.isNotEmpty(),
                    onClick = { onDraftChange(draft.redo()) },
                ),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(Modifier.weight(1f)) {
                BoardGameToolButton(
                    icon = Icons.Outlined.MoreVert,
                    label = stringResource(BoardGameR.string.boardgame_more),
                    onClick = { more = true },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                )
                // 底部工具栏入口:向上展开;锚定整个 weight 单元格,箭头对准按钮中心
                EnhancedDropdownMenu(
                    expanded = more,
                    onDismissRequest = { more = false },
                    modifier = Modifier.matchParentSize(),
                    expandUp = true,
                ) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_setup_clear), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        enabled = !busy,
                        onClick = { more = false; onDraftChange(draft.clear()) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_setup_reset), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        enabled = !busy,
                        onClick = { more = false; onDraftChange(draft.reset()) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_export_fen), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        enabled = !busy,
                        onClick = { more = false; Clipboard.copy(FenCodec.encode(draft.boardState), R.string.gomoku_copied) })
                    if (immersive != null) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_fullscreen), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            enabled = !busy,
                            onClick = { more = false; immersive.toggle() })
                    }
                    DropdownMenuItem(text = { Text(stringResource(R.string.gomoku_cancel), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        enabled = !busy,
                        onClick = { more = false; onCancel() })
                }
            }
        }
        BoardGamePillButton(
            label = stringResource(R.string.gomoku_setup_return),
            onClick = onConfirm,
            enabled = !busy && (!draft.hasChanges || issue == null),
            modifier = Modifier.fillMaxWidth(),
            primary = true,
        )
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
