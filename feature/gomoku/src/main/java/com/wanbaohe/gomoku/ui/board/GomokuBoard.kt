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
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.boardgame.ui.BoardCueMotion
import com.wanbaohe.boardgame.ui.boardPaddingForPieces
import com.wanbaohe.boardgame.ui.drawMoveHalo
import com.wanbaohe.boardgame.ui.drawPulsingDot
import com.wanbaohe.boardgame.ui.rememberBoardCueMotion
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side
import kotlin.math.roundToInt

/** 棋子直径与格距的比例。 */
private const val STONE_TO_GAP_RATIO = 0.88f

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
    forExport: Boolean = false,
) {
    val motion = rememberBoardCueMotion()
    BoxWithConstraints(
        modifier = modifier.aspectRatio(1f)
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val cellCount = BoardPoint.FILE_COUNT - 1
        // 留白要装得下最外圈棋子的一半,否则平板这类大棋盘上边路的棋子会被裁掉
        // (20dp 上限先到顶,棋子直径却跟着格距继续变大,见 boardPaddingForPieces)。
        val paddingPx = boardPaddingForPieces(
            basePaddingPx = minOf(with(density) { 20.dp.toPx() }, with(density) { maxWidth.toPx() } * 0.055f),
            boardWidthPx = constraints.maxWidth.toFloat(),
            boardHeightPx = constraints.maxHeight.toFloat(),
            fileGaps = cellCount.toFloat(),
            rankGaps = cellCount.toFloat(),
            pieceRatio = STONE_TO_GAP_RATIO,
            marginPx = with(density) { 1.dp.toPx() },
        )
        // 格距在外层 BoxWithConstraints 作用域先算好:内层 Box lambda 里拿不到 constraints
        val gapPx = (constraints.maxWidth - paddingPx * 2) / cellCount

        GlassSurface(
            modifier = Modifier.fillMaxSize(),
            style = if (forExport) GlassStyle.None else GlassStyle.Medium,
            color = if (forExport) Color(0xFFF3E8CE) else Color.Unspecified,
        ) {
            val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = if (forExport) 0.65f else 0.35f)
            val starColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            val strokeWidth = if (forExport) {
                with(androidx.compose.ui.platform.LocalDensity.current) { 1.dp.toPx() }
            } else 2f

            Canvas(modifier = Modifier.fillMaxSize()) {
                val gap = (size.width - paddingPx * 2) / cellCount

                for (i in 0..cellCount) {
                    val offset = paddingPx + i * gap
                    // 横线
                    drawLine(
                        color = lineColor,
                        start = Offset(paddingPx, offset),
                        end = Offset(size.width - paddingPx, offset),
                        strokeWidth = strokeWidth,
                    )
                    // 竖线
                    drawLine(
                        color = lineColor,
                        start = Offset(offset, paddingPx),
                        end = Offset(offset, size.height - paddingPx),
                        strokeWidth = strokeWidth,
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = AbsoluteAlignment.TopLeft) {
                if (!forExport && (candidateTargets.isNotEmpty() || lastMove != null)) {
                    GomokuCueLayer(
                        candidateTargets = candidateTargets,
                        lastMove = lastMove,
                        bottomSide = bottomSide,
                        paddingPx = paddingPx,
                        gapPx = gapPx,
                        motion = motion,
                    )
                }

                for (displayRank in 0 until BoardPoint.RANK_COUNT) {
                    for (displayFile in 0 until BoardPoint.FILE_COUNT) {
                        val point = displayPointToBoardPoint(displayFile, displayRank, bottomSide)
                        val stone = boardState.stoneAt(point)
                        val isSelected = point == selectedPoint
                        val isCandidate = point in candidateTargets
                        val isLastMove = point == lastMove

                        val stoneSize = with(androidx.compose.ui.platform.LocalDensity.current) { (gapPx * STONE_TO_GAP_RATIO).toDp() }

                        // 点击区域(以交点为中心)
                        Box(
                            modifier = Modifier
                                .absoluteOffset {
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
                                // 导出 GIF 用静止圆环;对局里改由上层动画画。
                                forExport && isLastMove -> Box(
                                    modifier = Modifier
                                        .size(stoneSize * 1.15f)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.onPrimaryContainer, CircleShape)
                                )
                                forExport && isCandidate -> Box(
                                    modifier = Modifier
                                        .size(stoneSize * 0.35f)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                )
                                // 选中点(落子前瞬间的高亮)
                                isSelected -> Box(
                                    modifier = Modifier
                                        .size(stoneSize * 1.15f)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.onPrimaryContainer, CircleShape)
                                )
                            }
                        }

                        // 棋子本体
                        if (stone != null) {
                            Box(
                                modifier = Modifier
                                    .absoluteOffset {
                                        IntOffset(
                                            (paddingPx + displayFile * gapPx - gapPx * 0.44f).roundToInt(),
                                            (paddingPx + displayRank * gapPx - gapPx * 0.44f).roundToInt(),
                                        )
                                    }
                                    .size(stoneSize)
                            ) {
                                StoneDisc(side = stone, forExport = forExport)
                                if (forExport && isLastMove) {
                                    Box(
                                        Modifier
                                            .align(Alignment.Center)
                                            .size(stoneSize * 0.24f)
                                            .background(if (stone == Side.BLACK) Color.White else Color.Black, CircleShape),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GomokuCueLayer(
    candidateTargets: Set<BoardPoint>,
    lastMove: BoardPoint?,
    bottomSide: Side,
    paddingPx: Float,
    gapPx: Float,
    motion: BoardCueMotion,
) {
    val dotColor = MaterialTheme.colorScheme.tertiaryContainer
    val dotEdge = MaterialTheme.colorScheme.onTertiaryContainer
    val haloColor = MaterialTheme.colorScheme.onPrimaryContainer
    Canvas(Modifier.fillMaxSize()) {
        val radius = gapPx * STONE_TO_GAP_RATIO / 2f
        fun centerOf(point: BoardPoint): Offset {
            val display = boardPointToDisplay(point, bottomSide)
            return Offset(paddingPx + display.first * gapPx, paddingPx + display.second * gapPx)
        }
        if (candidateTargets.isNotEmpty()) {
            val hint = motion.hint
            candidateTargets.forEach { point ->
                if (point == lastMove) return@forEach
                drawPulsingDot(
                    center = centerOf(point),
                    baseRadius = radius * 0.38f,
                    color = dotColor,
                    hint = hint,
                    edgeColor = dotEdge,
                    edgeWidth = 1.5.dp.toPx(),
                )
            }
        }
        val move = lastMove ?: return@Canvas
        drawMoveHalo(
            center = centerOf(move),
            radius = radius,
            color = haloColor,
            glow = motion.glow,
            strokeWidth = 1.15.dp.toPx(),
            strong = true,
        )
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
internal fun StoneDisc(side: Side, forExport: Boolean = false) {
    val isBlack = side == Side.BLACK
    val centerColor = if (isBlack) Color(0xFF4A4A4A) else Color.White
    val edgeColor = if (isBlack) Color(0xFF101010) else Color(0xFFD9D9D9)
    val borderColor = if (isBlack) Color(0xFF000000) else Color(0xFF9E9E9E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .shadow(if (forExport) 0.dp else 3.dp, CircleShape)
            .clip(CircleShape)
            .then(
                if (forExport) Modifier.background(if (isBlack) Color(0xFF222222) else Color.White)
                else Modifier.background(Brush.radialGradient(colors = listOf(centerColor, edgeColor)))
            )
            .border(1.dp, borderColor, CircleShape)
    )
}
