package com.wanbaohe.chess.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side
import kotlin.math.roundToInt

// 白方用空心字形、黑方用实心字形,统一深色字色,视觉上是同一套深色轮廓棋子
private fun Piece.glyph(): String = when (type) {
    PieceType.KING -> if (side == Side.WHITE) "♔" else "♚"
    PieceType.QUEEN -> if (side == Side.WHITE) "♕" else "♛"
    PieceType.ROOK -> if (side == Side.WHITE) "♖" else "♜"
    PieceType.BISHOP -> if (side == Side.WHITE) "♗" else "♝"
    PieceType.KNIGHT -> if (side == Side.WHITE) "♘" else "♞"
    PieceType.PAWN -> if (side == Side.WHITE) "♙" else "♟"
}

/** 格子底色:米白/灰蓝交替 */
private val LIGHT_SQUARE = Color(0xFFF0EBDD)
private val DARK_SQUARE = Color(0xFF8A9BB4)

/**
 * 国际象棋棋盘:8×8 交替格 + a-h/1-8 坐标标注 + Unicode 字形棋子。
 * 选中高亮、候选点、上一步高亮、将军格高亮;[bottomSide] 支持翻转视角。
 */
@Composable
fun ChessBoard(
    boardState: BoardState,
    selectedPoint: BoardPoint?,
    candidateTargets: Set<BoardPoint>,
    onCellTap: (file: Int, rank: Int) -> Unit,
    modifier: Modifier = Modifier,
    bottomSide: Side = Side.WHITE,
    checkNotice: String = "",
    lastMove: Pair<BoardPoint, BoardPoint>? = null,
) {
    BoxWithConstraints(
        modifier = modifier.aspectRatio(1f)
    ) {
        // 外圈留白放坐标标注;内层是真正的 8×8 棋盘
        val padding = 22.dp
        val paddingPx = with(androidx.compose.ui.platform.LocalDensity.current) { padding.toPx() }
        // 格宽在外层作用域先算好:内层 Box lambda 里拿不到 constraints
        val cellPx = (constraints.maxWidth - paddingPx * 2) / BoardPoint.FILE_COUNT
        val inCheckPoint = remember(boardState) {
            if (GameArbiter.isInCheck(boardState, boardState.sideToMove)) {
                GameArbiter.findKing(boardState, boardState.sideToMove)
            } else null
        }

        GlassSurface(
            modifier = Modifier.fillMaxSize(),
            style = GlassStyle.Medium,
        ) {
            val selectedColor = MaterialTheme.colorScheme.primary
            val lastMoveColor = MaterialTheme.colorScheme.tertiary
            val checkColor = MaterialTheme.colorScheme.error
            val candidateColor = MaterialTheme.colorScheme.primary

            Canvas(modifier = Modifier.fillMaxSize()) {
                for (rank in 0 until BoardPoint.RANK_COUNT) {
                    for (file in 0 until BoardPoint.FILE_COUNT) {
                        val point = displayPointToBoardPoint(file, rank, bottomSide)
                        val x = paddingPx + file * cellPx
                        val y = paddingPx + rank * cellPx

                        // 交替底色
                        drawRect(
                            color = if ((point.file + point.rank) % 2 == 0) DARK_SQUARE else LIGHT_SQUARE,
                            topLeft = Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(cellPx, cellPx),
                        )
                        // 高亮层:上一步 > 将军 > 选中
                        when (point) {
                            lastMove?.first, lastMove?.second -> drawRect(
                                color = lastMoveColor.copy(alpha = 0.35f),
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(cellPx, cellPx),
                            )
                            inCheckPoint -> drawRect(
                                color = checkColor.copy(alpha = 0.4f),
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(cellPx, cellPx),
                            )
                            selectedPoint -> drawRect(
                                color = selectedColor.copy(alpha = 0.4f),
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(cellPx, cellPx),
                            )
                        }
                        // 候选落点:空格小圆点,吃子格圆环
                        if (point in candidateTargets) {
                            val center = Offset(x + cellPx / 2, y + cellPx / 2)
                            if (boardState.pieceAt(point) == null) {
                                drawCircle(color = candidateColor.copy(alpha = 0.45f), radius = cellPx * 0.14f, center = center)
                            } else {
                                drawCircle(
                                    color = candidateColor.copy(alpha = 0.55f),
                                    radius = cellPx * 0.44f,
                                    center = center,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = cellPx * 0.07f),
                                )
                            }
                        }
                    }
                }
            }

            // 坐标标注:底行 a-h(贴底)、左列 1-8(贴左)
            for (i in 0 until BoardPoint.FILE_COUNT) {
                val fileLabel = "${'a' + displayPointToBoardPoint(i, 0, bottomSide).file}"
                val rankLabel = "${displayPointToBoardPoint(0, i, bottomSide).rank + 1}"
                Text(
                    text = fileLabel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset {
                            IntOffset(
                                (paddingPx + i * cellPx + cellPx / 2).roundToInt() - 4.dp.roundToPx(),
                                (paddingPx + 8 * cellPx + 2.dp.toPx()).roundToInt(),
                            )
                        },
                )
                Text(
                    text = rankLabel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                (paddingPx / 2 - 4.dp.toPx()).roundToInt(),
                                (paddingPx + i * cellPx + cellPx / 2 - 6.dp.toPx()).roundToInt(),
                            )
                        },
                )
            }

            // 棋子与点击层
            Box(modifier = Modifier.fillMaxSize()) {
                for (displayRank in 0 until BoardPoint.RANK_COUNT) {
                    for (displayFile in 0 until BoardPoint.FILE_COUNT) {
                        val point = displayPointToBoardPoint(displayFile, displayRank, bottomSide)
                        val piece = boardState.pieceAt(point)

                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (paddingPx + displayFile * cellPx).roundToInt(),
                                        (paddingPx + displayRank * cellPx).roundToInt(),
                                    )
                                }
                                .size(with(androidx.compose.ui.platform.LocalDensity.current) { cellPx.toDp() })
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onCellTap(point.file, point.rank) }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (piece != null) {
                                Text(
                                    text = piece.glyph(),
                                    fontSize = with(androidx.compose.ui.platform.LocalDensity.current) { (cellPx * 0.72f).toSp() },
                                    color = if (piece.side == Side.WHITE) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
                if (checkNotice.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = checkNotice,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        }
    }
}

/** 棋盘坐标 → 显示坐标;bottomSide 为黑方时翻转视角(白方在底部是默认) */
private fun displayPointToBoardPoint(displayFile: Int, displayRank: Int, bottomSide: Side): BoardPoint =
    if (bottomSide == Side.WHITE) {
        // 白方在底:屏幕底行是 rank 0
        BoardPoint(displayFile, BoardPoint.RANK_COUNT - 1 - displayRank)
    } else {
        BoardPoint(BoardPoint.FILE_COUNT - 1 - displayFile, displayRank)
    }
