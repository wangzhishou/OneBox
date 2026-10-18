package com.wanbaohe.chess.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.boardgame.ui.BoardCueMotion
import com.wanbaohe.boardgame.ui.drawPulsingDot
import com.wanbaohe.boardgame.ui.rememberBoardCueMotion
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side
import kotlin.math.roundToInt

// 白方用空心字形、黑方用实心字形,统一深色字色,视觉上是同一套深色轮廓棋子
internal fun Piece.glyph(): String = when (type) {
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
 * 棋面油墨:格子是**固定浅色材质**(不跟主题走),所以画在格子上的前景色也必须与主题无关。
 *
 * 踩过的坑:棋子字形与高亮曾取 `onSurface` / `onPrimaryContainer` / `onTertiaryContainer`,
 * 这些角色在暗色主题下会翻成浅色 —— 白到发灰的字形压在米白格上等于隐形(棋子、
 * 候选落点、上一步高亮全部看不清,导出 GIF 因为用固定浅色底更是整盘看不见棋子)。
 * 这里钉死一套浅底专用油墨,浅色/暗色/动态取色下表现一致。
 */
private val PIECE_INK = Color(0xFF262626)
private val LAST_MOVE_TINT = Color(0xFFB8860B)
private val SELECTED_TINT = Color(0xFF2E7D32)
private val CANDIDATE_INK = Color(0xFF262626)
private val CHECK_TINT = Color(0xFFC62828)

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
    onPieceDrop: ((BoardPoint, BoardPoint) -> Unit)? = null,
    forExport: Boolean = false,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val boardSize = minOf(maxWidth, maxHeight).coerceAtLeast(1.dp)
        BoxWithConstraints(Modifier.size(boardSize)) {
        // 外圈留白放坐标标注;内层是真正的 8×8 棋盘
        val padding = minOf(22.dp, boardSize / 12)
        val paddingPx = with(androidx.compose.ui.platform.LocalDensity.current) { padding.toPx() }
        // 格宽在外层作用域先算好:内层 Box lambda 里拿不到 constraints
        val cellPx = (constraints.maxWidth - paddingPx * 2) / BoardPoint.FILE_COUNT
        val inCheckPoint = remember(boardState) {
            if (GameArbiter.isInCheck(boardState, boardState.sideToMove)) {
                GameArbiter.findKing(boardState, boardState.sideToMove)
            } else null
        }
        val motion = rememberBoardCueMotion()

        GlassSurface(
            modifier = Modifier.fillMaxSize(),
            style = if (forExport) GlassStyle.None else GlassStyle.Medium,
            color = if (forExport) Color(0xFFF3E8CE) else Color.Unspecified,
        ) {
            val selectedColor = SELECTED_TINT
            val lastMoveColor = LAST_MOVE_TINT
            val checkColor = CHECK_TINT
            val candidateColor = CANDIDATE_INK

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
                        // 高亮层:上一步 > 将军 > 选中。对局时上一步和候选点改由上层动画画。
                        when (point) {
                            lastMove?.first, lastMove?.second -> if (forExport) {
                                drawRect(
                                    color = lastMoveColor.copy(alpha = 0.35f),
                                    topLeft = Offset(x, y),
                                    size = Size(cellPx, cellPx),
                                )
                            }
                            inCheckPoint -> drawRect(
                                color = checkColor.copy(alpha = 0.4f),
                                topLeft = Offset(x, y),
                                size = Size(cellPx, cellPx),
                            )
                            selectedPoint -> drawRect(
                                color = selectedColor.copy(alpha = 0.4f),
                                topLeft = Offset(x, y),
                                size = Size(cellPx, cellPx),
                            )
                        }
                        // 候选落点:空格小圆点,吃子格圆环
                        if (forExport && point in candidateTargets) {
                            val center = Offset(x + cellPx / 2, y + cellPx / 2)
                            if (boardState.pieceAt(point) == null) {
                                drawCircle(color = candidateColor.copy(alpha = 0.45f), radius = cellPx * 0.14f, center = center)
                            } else {
                                drawCircle(
                                    color = candidateColor.copy(alpha = 0.55f),
                                    radius = cellPx * 0.44f,
                                    center = center,
                                    style = Stroke(width = cellPx * 0.07f),
                                )
                            }
                        }
                        }
                    }
                }
            }
            if (!forExport && (candidateTargets.isNotEmpty() || lastMove != null)) {
                ChessCueLayer(
                    boardState = boardState,
                    candidateTargets = candidateTargets,
                    lastMove = lastMove,
                    bottomSide = bottomSide,
                    paddingPx = paddingPx,
                    cellPx = cellPx,
                    motion = motion,
                )
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
                        .align(Alignment.TopStart)
                        .absoluteOffset {
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
                        .absoluteOffset {
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
                        var dragOffset by remember(boardState, point) { mutableStateOf(Offset.Zero) }
                        var dragStart by remember(boardState, point) { mutableStateOf(Offset.Zero) }
                        var dragging by remember(boardState, point) { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .zIndex(if (dragging) 1f else 0f)
                                .absoluteOffset {
                                    IntOffset(
                                        (paddingPx + displayFile * cellPx).roundToInt(),
                                        (paddingPx + displayRank * cellPx).roundToInt(),
                                    )
                                }
                                .size(with(androidx.compose.ui.platform.LocalDensity.current) { cellPx.toDp() })
                                .then(
                                    if (onPieceDrop == null || piece == null) Modifier else Modifier.pointerInput(
                                        boardState, point, bottomSide, cellPx, onPieceDrop,
                                    ) {
                                        detectDragGestures(
                                            onDragStart = {
                                                dragStart = it
                                                dragOffset = Offset.Zero
                                                dragging = true
                                            },
                                            onDragCancel = { dragOffset = Offset.Zero; dragging = false },
                                            onDragEnd = {
                                                val position = dragStart + dragOffset
                                                val targetFile = displayFile + kotlin.math.floor(position.x / cellPx).toInt()
                                                val targetRank = displayRank + kotlin.math.floor(position.y / cellPx).toInt()
                                                if (targetFile in 0..7 && targetRank in 0..7) {
                                                    onPieceDrop?.invoke(point, displayPointToBoardPoint(targetFile, targetRank, bottomSide))
                                                }
                                                dragOffset = Offset.Zero
                                                dragging = false
                                            },
                                        ) { change, amount ->
                                            change.consume()
                                            dragOffset += amount
                                        }
                                    }
                                )
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
                                    color = PIECE_INK,
                                    modifier = Modifier.graphicsLayer {
                                        translationX = dragOffset.x
                                        translationY = dragOffset.y
                                    },
                                    fontWeight = FontWeight.Normal,
                                )
                            }
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

@Composable
private fun ChessCueLayer(
    boardState: BoardState,
    candidateTargets: Set<BoardPoint>,
    lastMove: Pair<BoardPoint, BoardPoint>?,
    bottomSide: Side,
    paddingPx: Float,
    cellPx: Float,
    motion: BoardCueMotion,
) {
    val dotColor = MaterialTheme.colorScheme.tertiaryContainer
    val dotEdge = MaterialTheme.colorScheme.onTertiaryContainer
    Canvas(Modifier.fillMaxSize()) {
        val move = lastMove
        if (move != null) {
            val glow = motion.glow
            val stroke = 1.15.dp.toPx()
            val inset = stroke / 2f + 1.dp.toPx()
            for (displayRank in 0 until BoardPoint.RANK_COUNT) {
                for (displayFile in 0 until BoardPoint.FILE_COUNT) {
                    val point = displayPointToBoardPoint(displayFile, displayRank, bottomSide)
                    if (point != move.first && point != move.second) continue
                    val topLeft = Offset(paddingPx + displayFile * cellPx, paddingPx + displayRank * cellPx)
                    val alpha = if (point == move.second) 0.28f + 0.16f * glow else 0.16f + 0.1f * glow
                    drawRect(
                        color = LAST_MOVE_TINT.copy(alpha = alpha),
                        topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
                        size = Size(cellPx - inset * 2f, cellPx - inset * 2f),
                        style = Stroke(width = stroke),
                    )
                }
            }
        }
        if (candidateTargets.isEmpty()) return@Canvas
        val hint = motion.hint
        for (displayRank in 0 until BoardPoint.RANK_COUNT) {
            for (displayFile in 0 until BoardPoint.FILE_COUNT) {
                val point = displayPointToBoardPoint(displayFile, displayRank, bottomSide)
                if (point !in candidateTargets) continue
                val center = Offset(
                    paddingPx + displayFile * cellPx + cellPx / 2f,
                    paddingPx + displayRank * cellPx + cellPx / 2f,
                )
                if (boardState.pieceAt(point) == null) {
                    drawPulsingDot(
                        center = center,
                        baseRadius = cellPx * 0.16f,
                        color = dotColor,
                        hint = hint,
                        edgeColor = dotEdge,
                        edgeWidth = 1.5.dp.toPx(),
                    )
                } else {
                    val phase = hint.coerceIn(0f, 1f)
                    drawCircle(
                        color = dotColor.copy(alpha = 0.28f + 0.2f * phase),
                        radius = cellPx * 0.36f,
                        center = center,
                    )
                    drawCircle(
                        color = dotEdge.copy(alpha = 0.35f + 0.15f * phase),
                        radius = cellPx * 0.4f,
                        center = center,
                        style = Stroke(width = cellPx * 0.045f),
                    )
                }
            }
        }
    }
}

/** 棋盘坐标 → 显示坐标;bottomSide 为黑方时翻转视角(白方在底部是默认) */
internal fun displayPointToBoardPoint(displayFile: Int, displayRank: Int, bottomSide: Side): BoardPoint =
    if (bottomSide == Side.WHITE) {
        // 白方在底:屏幕底行是 rank 0
        BoardPoint(displayFile, BoardPoint.RANK_COUNT - 1 - displayRank)
    } else {
        BoardPoint(BoardPoint.FILE_COUNT - 1 - displayFile, displayRank)
    }
