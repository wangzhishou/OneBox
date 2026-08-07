package com.wanbaohe.game2048.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.wanbaohe.game2048.component.Direction
import kotlin.math.abs

/** 最小拖动距离，低于此阈值不触发移动 */
private const val MIN_SWIPE_DISTANCE = 40f

/**
 * 4×4 游戏棋盘
 *
 * 功能：
 * 1. 以正方形容器渲染 4×4 方块网格
 * 2. 检测上下左右滑动手势，回调 [onSwipe]
 * 3. 外层使用 [GlassSurface] 容器，支持毛玻璃模式
 *
 * @param grid    4×4 棋盘数据，0 表示空格
 * @param onSwipe 滑动方向回调
 */
@Composable
fun GameBoard(
    grid: List<List<Int>>,
    onSwipe: (Direction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 记录拖动偏移量
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragX = 0f
                        dragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount.x
                        dragY += dragAmount.y
                    },
                    onDragEnd = {
                        // 根据累积偏移判断方向
                        val absX = abs(dragX)
                        val absY = abs(dragY)
                        if (absX > MIN_SWIPE_DISTANCE || absY > MIN_SWIPE_DISTANCE) {
                            val direction = if (absX > absY) {
                                if (dragX > 0) Direction.Right else Direction.Left
                            } else {
                                if (dragY > 0) Direction.Down else Direction.Up
                            }
                            onSwipe(direction)
                        }
                        dragX = 0f
                        dragY = 0f
                    },
                    onDragCancel = {
                        dragX = 0f
                        dragY = 0f
                    }
                )
            },
        style = GlassStyle.Medium,
        shape = RoundedCornerShape(20.dp),
        borderWidth = 0.dp,
    ) {
        // 4×4 网格
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (row in grid.indices) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (col in grid[row].indices) {
                        Box(modifier = Modifier.weight(1f)) {
                            TileView(
                                value = grid[row][col],
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}

