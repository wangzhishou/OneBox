package com.wanbaohe.gomoku.ui.board

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side
import kotlin.math.roundToInt

/** 天元 + 四角星位 */
private val STAR_POINTS = listOf(
    BoardPoint(3, 3), BoardPoint(3, 11),
    BoardPoint(11, 3), BoardPoint(11, 11),
    BoardPoint(7, 7),
)

/**
 * 五子棋棋盘:15×15 交点网格 + 星位 + 黑白棋子(径向渐变圆)。
 * 点击交点落子([onCellTap]);[lastMove] 是上一步高亮;[bottomSide] 支持翻转视角。
 */
@Composable
fun GomokuBoard(
    boardState: BoardState,
    selectedPoint: BoardPoint?,
    candidateTargets: Set<BoardPoint>,
    onCellTap: (file: Int, rank: Int) -> Unit,
    modifier: Modifier = Modifier,
    bottomSide: Side = Side.BLACK,
    lastMove: BoardPoint? = null,
) {
    BoxWithConstraints(
        modifier = modifier.aspectRatio(1f)
    ) {
        val padding = 20.dp
        val paddingPx = with(androidx.compose.ui.platform.LocalDensity.current) { padding.toPx() }
        val cellCount = BoardPoint.FILE_COUNT - 1
        // 格距在外层 BoxWithConstraints 作用域先算好:内层 Box lambda 里拿不到 constraints
        val gapPx = (constraints.maxWidth - paddingPx * 2) / cellCount

        GlassSurface(
            modifier = Modifier.fillMaxSize(),
            style = GlassStyle.Medium,
        ) {
            val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            val starColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val gap = (size.width - paddingPx * 2) / cellCount

                for (i in 0..cellCount) {
                    val offset = paddingPx + i * gap
                    // 横线
                    drawLine(
                        color = lineColor,
                        start = Offset(paddingPx, offset),
                        end = Offset(size.width - paddingPx, offset),
                        strokeWidth = 2f,
                    )
                    // 竖线
                    drawLine(
                        color = lineColor,
                        start = Offset(offset, paddingPx),
                        end = Offset(offset, size.height - paddingPx),
                        strokeWidth = 2f,
                    )
                }

                // 星位
                STAR_POINTS.forEach { point ->
                    val display = boardPointToDisplay(point, bottomSide)
                    drawCircle(
                        color = starColor,
                        radius = gap * 0.09f,
                        center = Offset(paddingPx + display.first * gap, paddingPx + display.second * gap),
                    )
                }
            }

            // 棋子与交互层
            Box(modifier = Modifier.fillMaxSize()) {

                for (displayRank in 0 until BoardPoint.RANK_COUNT) {
                    for (displayFile in 0 until BoardPoint.FILE_COUNT) {
                        val point = displayPointToBoardPoint(displayFile, displayRank, bottomSide)
                        val stone = boardState.stoneAt(point)
                        val isSelected = point == selectedPoint
                        val isCandidate = point in candidateTargets
                        val isLastMove = point == lastMove

                        val stoneSize = with(androidx.compose.ui.platform.LocalDensity.current) { (gapPx * 0.88f).toDp() }

                        // 点击区域(以交点为中心)
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (paddingPx + displayFile * gapPx - gapPx / 2).roundToInt(),
                                        (paddingPx + displayRank * gapPx - gapPx / 2).roundToInt(),
                                    )
                                }
                                .size(with(androidx.compose.ui.platform.LocalDensity.current) { gapPx.toDp() })
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onCellTap(point.file, point.rank) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                // 上一步高亮:空心圆环
                                isLastMove -> Box(
                                    modifier = Modifier
                                        .size(stoneSize * 1.15f)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                                )
                                // 候选落点:半透明小点
                                isCandidate -> Box(
                                    modifier = Modifier
                                        .size(stoneSize * 0.35f)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                )
                                // 选中点(落子前瞬间的高亮)
                                isSelected -> Box(
                                    modifier = Modifier
                                        .size(stoneSize)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                                )
                            }
                        }

                        // 棋子本体
                        if (stone != null) {
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (paddingPx + displayFile * gapPx - gapPx * 0.44f).roundToInt(),
                                            (paddingPx + displayRank * gapPx - gapPx * 0.44f).roundToInt(),
                                        )
                                    }
                                    .size(stoneSize)
                            ) {
                                StoneDisc(side = stone)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 棋盘坐标 → 显示坐标;bottomSide 为白方时翻转视角 */
private fun displayPointToBoardPoint(displayFile: Int, displayRank: Int, bottomSide: Side): BoardPoint =
    if (bottomSide == Side.BLACK) {
        BoardPoint(displayFile, displayRank)
    } else {
        BoardPoint(BoardPoint.FILE_COUNT - 1 - displayFile, BoardPoint.RANK_COUNT - 1 - displayRank)
    }

private fun boardPointToDisplay(point: BoardPoint, bottomSide: Side): Pair<Int, Int> =
    if (bottomSide == Side.BLACK) {
        point.file to point.rank
    } else {
        BoardPoint.FILE_COUNT - 1 - point.file to BoardPoint.RANK_COUNT - 1 - point.rank
    }

/** 棋子:径向渐变圆(黑深灰→纯黑,白白→浅灰),带细描边 */
@Composable
private fun StoneDisc(side: Side) {
    val isBlack = side == Side.BLACK
    val centerColor = if (isBlack) Color(0xFF4A4A4A) else Color.White
    val edgeColor = if (isBlack) Color(0xFF101010) else Color(0xFFD9D9D9)
    val borderColor = if (isBlack) Color(0xFF000000) else Color(0xFF9E9E9E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.radialGradient(colors = listOf(centerColor, edgeColor)))
            .border(1.dp, borderColor, CircleShape)
    )
}
