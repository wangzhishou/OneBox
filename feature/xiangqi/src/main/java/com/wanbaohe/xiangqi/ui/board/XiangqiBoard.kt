package com.wanbaohe.xiangqi.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import kotlin.math.roundToInt

@Composable
fun XiangqiBoard(
    boardState: BoardState,
    selectedPoint: BoardPoint?,
    candidateTargets: Set<BoardPoint>,
    onCellTap: (file: Int, rank: Int) -> Unit,
    modifier: Modifier = Modifier,
    bottomSide: Side = Side.RED,
    riverNotice: String = "",
    lastMove: Pair<BoardPoint, BoardPoint>? = null,
    onPieceDrop: ((BoardPoint, BoardPoint) -> Unit)? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(9f / 10f)
    ) {
        val boardMaxWidth = constraints.maxWidth
        val boardMaxHeight = constraints.maxHeight
        val paddingPx = minOf(
            with(androidx.compose.ui.platform.LocalDensity.current) { 28.dp.toPx() },
            boardMaxWidth / 18f,
            boardMaxHeight / 20f,
        )
        var dragFrom by remember { mutableStateOf<BoardPoint?>(null) }
        var dragPosition by remember { mutableStateOf<Offset?>(null) }
        val dropPiece by rememberUpdatedState(onPieceDrop)
        
        GlassSurface(
            modifier = Modifier.fillMaxSize().pointerInput(boardState, bottomSide, onPieceDrop != null) {
                if (onPieceDrop == null || size.width == 0 || size.height == 0) return@pointerInput
                fun pointAt(offset: Offset): BoardPoint? {
                    val file = ((offset.x - paddingPx) / ((size.width - paddingPx * 2) / 8f)).roundToInt()
                    val rank = ((offset.y - paddingPx) / ((size.height - paddingPx * 2) / 9f)).roundToInt()
                    if (file !in 0..8 || rank !in 0..9) return null
                    return displayPointToBoardPoint(file, rank, bottomSide)
                }
                detectDragGestures(
                    onDragStart = { offset ->
                        dragFrom = pointAt(offset)?.takeIf { boardState.pieceAt(it) != null }
                        dragPosition = offset.takeIf { dragFrom != null }
                    },
                    onDrag = { change, amount ->
                        if (dragFrom != null) {
                            change.consume()
                            dragPosition = dragPosition?.plus(amount)
                        }
                    },
                    onDragEnd = {
                        val from = dragFrom
                        val to = dragPosition?.let(::pointAt)
                        dragFrom = null
                        dragPosition = null
                        if (from != null && to != null) dropPiece?.invoke(from, to)
                    },
                    onDragCancel = { dragFrom = null; dragPosition = null },
                )
            },
            style = GlassStyle.Medium,
        ) {
                val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                val strokeWidth = 2f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val boardWidth = size.width - paddingPx * 2
                    val boardHeight = size.height - paddingPx * 2
                    val horizontalGap = boardHeight / 9f
                    val verticalGap = boardWidth / 8f

                // Draw horizontal lines
                for (rank in 0..9) {
                    val y = paddingPx + rank * horizontalGap
                    drawLine(
                        color = lineColor,
                        start = Offset(paddingPx, y),
                        end = Offset(size.width - paddingPx, y),
                        strokeWidth = strokeWidth,
                    )
                }

                // Draw vertical lines
                for (file in 0..8) {
                    val x = paddingPx + file * verticalGap
                    // Break vertical lines at the river (between rank 4 and 5), except for edge lines
                    if (file == 0 || file == 8) {
                        drawLine(
                            color = lineColor,
                            start = Offset(x, paddingPx),
                            end = Offset(x, size.height - paddingPx),
                            strokeWidth = strokeWidth,
                        )
                    } else {
                        // Top half
                        drawLine(
                            color = lineColor,
                            start = Offset(x, paddingPx),
                            end = Offset(x, paddingPx + 4 * horizontalGap),
                            strokeWidth = strokeWidth,
                        )
                        // Bottom half
                        drawLine(
                            color = lineColor,
                            start = Offset(x, paddingPx + 5 * horizontalGap),
                            end = Offset(x, size.height - paddingPx),
                            strokeWidth = strokeWidth,
                        )
                    }
                }

                // Draw Palaces (Diagonal lines)
                // Top Palace
                drawLine(
                    color = lineColor,
                    start = Offset(paddingPx + 3 * verticalGap, paddingPx),
                    end = Offset(paddingPx + 5 * verticalGap, paddingPx + 2 * horizontalGap),
                    strokeWidth = strokeWidth,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(paddingPx + 5 * verticalGap, paddingPx),
                    end = Offset(paddingPx + 3 * verticalGap, paddingPx + 2 * horizontalGap),
                    strokeWidth = strokeWidth,
                )
                
                // Bottom Palace
                drawLine(
                    color = lineColor,
                    start = Offset(paddingPx + 3 * verticalGap, paddingPx + 7 * horizontalGap),
                    end = Offset(paddingPx + 5 * verticalGap, paddingPx + 9 * horizontalGap),
                    strokeWidth = strokeWidth,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(paddingPx + 5 * verticalGap, paddingPx + 7 * horizontalGap),
                    end = Offset(paddingPx + 3 * verticalGap, paddingPx + 9 * horizontalGap),
                    strokeWidth = strokeWidth,
                )
            }
            
            // Draw Pieces and interaction overlays
            Box(modifier = Modifier.fillMaxSize()) {
                val boardWidthPx = boardMaxWidth - paddingPx * 2
                val boardHeightPx = boardMaxHeight - paddingPx * 2
                val hGapPx = boardHeightPx / 9f
                val vGapPx = boardWidthPx / 8f
                
                for (displayRank in 0 until BoardPoint.RANK_COUNT) {
                    for (displayFile in 0 until BoardPoint.FILE_COUNT) {
                        val point = displayPointToBoardPoint(displayFile, displayRank, bottomSide)
                        val piece = boardState.pieceAt(point)
                        val isSelected = point == selectedPoint
                        val isCandidate = point in candidateTargets
                        val isLastMoveFrom = point == lastMove?.first
                        val isLastMoveTo = point == lastMove?.second
                        
                        val pieceSize = with(androidx.compose.ui.platform.LocalDensity.current) { (vGapPx * 0.85f).toDp() }
                        
                        // Clickable area (centered on intersection)
                        Box(
                            modifier = Modifier
                                .offset { IntOffset((paddingPx + displayFile * vGapPx - vGapPx / 2).roundToInt(), (paddingPx + displayRank * hGapPx - hGapPx / 2).roundToInt()) }
                                .size(with(androidx.compose.ui.platform.LocalDensity.current) { vGapPx.toDp() }, with(androidx.compose.ui.platform.LocalDensity.current) { hGapPx.toDp() })
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onCellTap(point.file, point.rank) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isLastMoveTo -> LastMoveRing(
                                    pieceSize = pieceSize,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    width = 3.dp,
                                )
                                isLastMoveFrom -> LastMoveRing(
                                    pieceSize = pieceSize,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                    width = 2.dp,
                                )
                                isCandidate -> Box(
                                    modifier = Modifier
                                        .size(pieceSize * 0.4f)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                )
                            }
                        }

                        // The Piece itself
                        if (piece != null && point != dragFrom) {
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset((paddingPx + displayFile * vGapPx - vGapPx * 0.425f).roundToInt(), (paddingPx + displayRank * hGapPx - vGapPx * 0.425f).roundToInt()) }
                                    .size(pieceSize)
                            ) {
                                PieceDisc(
                                    piece = piece,
                                    selected = isSelected,
                                )
                            }
                        }
                    }
                }
                val draggedPiece = dragFrom?.let(boardState::pieceAt)
                val position = dragPosition
                if (draggedPiece != null && position != null) {
                    val diameter = vGapPx * 0.85f
                    Box(
                        modifier = Modifier
                            .offset { IntOffset((position.x - diameter / 2).roundToInt(), (position.y - diameter / 2).roundToInt()) }
                            .size(with(androidx.compose.ui.platform.LocalDensity.current) { diameter.toDp() }),
                    ) {
                        PieceDisc(draggedPiece, true)
                    }
                }
                if (riverNotice.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(999.dp),
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = riverNotice,
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

private fun displayPointToBoardPoint(displayFile: Int, displayRank: Int, bottomSide: Side): BoardPoint =
    if (bottomSide == Side.RED) {
        BoardPoint(displayFile, displayRank)
    } else {
        BoardPoint(BoardPoint.FILE_COUNT - 1 - displayFile, BoardPoint.RANK_COUNT - 1 - displayRank)
    }

@Composable
private fun LastMoveRing(
    pieceSize: Dp,
    color: Color,
    width: Dp,
) {
    Box(
        modifier = Modifier
            .size(pieceSize * 1.12f)
            .clip(CircleShape)
            .border(width, color, CircleShape)
    )
}

@Composable
internal fun PieceDisc(
    piece: Piece,
    selected: Boolean,
) {
    val isRed = piece.side == Side.RED
    val text = pieceLabel(piece)
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.error
    val pieceColor = if (isRed) secondaryColor else MaterialTheme.colorScheme.onSurface
    val glowColor = if (isRed) secondaryColor else primaryColor

    val activeBorder = if (selected) primaryColor else MaterialTheme.colorScheme.outlineVariant
    val activeGlow = if (selected) primaryColor.copy(alpha = 0.6f) else Color.Transparent

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shadow(if (selected) 8.dp else 4.dp, CircleShape, ambientColor = if (selected) activeGlow else Color.Black)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = activeBorder,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Inner glow for pieces
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(colors = listOf(glowColor.copy(alpha = 0.2f), Color.Transparent)))
        )
        Text(
            text = text,
            color = pieceColor,
            fontWeight = FontWeight.Bold,
            fontSize = minOf(20f, maxWidth.value * 0.6f).sp,
        )
    }

}

internal fun pieceLabel(piece: Piece): String = when (piece.type) {
    PieceType.KING -> if (piece.side == Side.RED) "帅" else "将"
    PieceType.ADVISOR -> if (piece.side == Side.RED) "仕" else "士"
    PieceType.BISHOP -> if (piece.side == Side.RED) "相" else "象"
    PieceType.KNIGHT -> if (piece.side == Side.RED) "傌" else "馬"
    PieceType.ROOK -> if (piece.side == Side.RED) "俥" else "車"
    PieceType.CANNON -> if (piece.side == Side.RED) "炮" else "砲"
    PieceType.PAWN -> if (piece.side == Side.RED) "兵" else "卒"
}
