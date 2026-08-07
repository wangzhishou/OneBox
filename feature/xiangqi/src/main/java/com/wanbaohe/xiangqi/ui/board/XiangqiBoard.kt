package com.wanbaohe.xiangqi.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(9f / 10f)
    ) {
        val boardMaxWidth = constraints.maxWidth
        val boardMaxHeight = constraints.maxHeight
        val padding = 28.dp
        val paddingPx = with(androidx.compose.ui.platform.LocalDensity.current) { padding.toPx() }
        
        GlassSurface(
            modifier = Modifier.fillMaxSize(),
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
                val boardWidthPx = boardMaxWidth - with(androidx.compose.ui.platform.LocalDensity.current) { padding.toPx() } * 2
                val boardHeightPx = boardMaxHeight - with(androidx.compose.ui.platform.LocalDensity.current) { padding.toPx() } * 2
                val hGapPx = boardHeightPx / 9f
                val vGapPx = boardWidthPx / 8f
                
                for (displayRank in 0 until BoardPoint.RANK_COUNT) {
                    for (displayFile in 0 until BoardPoint.FILE_COUNT) {
                        val point = displayPointToBoardPoint(displayFile, displayRank, bottomSide)
                        val piece = boardState.pieceAt(point)
                        val isSelected = point == selectedPoint
                        val isCandidate = point in candidateTargets
                        
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
                            if (isCandidate) {
                                Box(
                                    modifier = Modifier
                                        .size(pieceSize * 0.4f)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                )
                            }
                        }

                        // The Piece itself
                        if (piece != null) {
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset((paddingPx + displayFile * vGapPx - vGapPx * 0.425f).roundToInt(), (paddingPx + displayRank * hGapPx - hGapPx * 0.425f).roundToInt()) }
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
private fun PieceDisc(
    piece: Piece,
    selected: Boolean,
) {
    val isRed = piece.side == Side.RED
    val text = when (piece.type) {
        PieceType.KING -> if (isRed) "帅" else "将"
        PieceType.ADVISOR -> if (isRed) "仕" else "士"
        PieceType.BISHOP -> if (isRed) "相" else "象"
        PieceType.KNIGHT -> if (isRed) "傌" else "馬"
        PieceType.ROOK -> if (isRed) "俥" else "車"
        PieceType.CANNON -> if (isRed) "炮" else "砲"
        PieceType.PAWN -> if (isRed) "兵" else "卒"
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.error
    val pieceColor = if (isRed) secondaryColor else primaryColor
    val glowColor = if (isRed) secondaryColor else primaryColor
    
    val activeBorder = if (selected) primaryColor else MaterialTheme.colorScheme.outlineVariant
    val activeGlow = if (selected) primaryColor.copy(alpha = 0.6f) else Color.Transparent
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(if (selected) 8.dp else 4.dp, CircleShape, ambientColor = if (selected) activeGlow else Color.Black)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
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
            fontSize = 20.sp,
        )
    }
}
