package com.shifenmiao.base.ui.shapes

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * 气泡形状，支持箭头方向和水平对齐方式。
 *
 * @param arrowSize     箭头的大小（三角形的半宽和高度）
 * @param arrowDirection 箭头方向：Top（向上）或 Bottom（向下）
 * @param arrowAlignment 箭头水平对齐方式：Start / Center / End
 * @param arrowOffset    箭头距离对齐边缘的偏移量（仅 Start / End 时生效）
 * @param cornerRadius   圆角半径
 */
class BubbleShape(
    private val arrowSize: Dp = 12.dp,
    private val arrowDirection: ArrowDirection = ArrowDirection.Bottom,
    private val arrowAlignment: ArrowAlignment = ArrowAlignment.Center,
    private val arrowOffset: Dp = 24.dp,
    private val cornerRadius: Dp = 16.dp
) : Shape {

    enum class ArrowDirection { Top, Bottom }
    enum class ArrowAlignment { Start, Center, End }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arrowPx = with(density) { arrowSize.toPx() }
        val cornerPx = with(density) { cornerRadius.toPx() }
        val offsetPx = with(density) { arrowOffset.toPx() }

        // 计算箭头顶点的水平中心位置
        val arrowCenterX = when (arrowAlignment) {
            ArrowAlignment.Start -> offsetPx
            ArrowAlignment.Center -> size.width / 2f
            ArrowAlignment.End -> size.width - offsetPx
        }

        return Outline.Generic(Path().apply {
            when (arrowDirection) {
                ArrowDirection.Top -> {
                    // 矩形主体，顶部留出箭头空间
                    addRoundRect(
                        roundRect = RoundRect(
                            left = 0f,
                            top = arrowPx,
                            right = size.width,
                            bottom = size.height,
                            cornerRadius = CornerRadius(cornerPx, cornerPx)
                        )
                    )
                    // 顶部三角形箭头
                    moveTo(arrowCenterX - arrowPx, arrowPx)
                    lineTo(arrowCenterX, 0f)
                    lineTo(arrowCenterX + arrowPx, arrowPx)
                    close()
                }

                ArrowDirection.Bottom -> {
                    // 矩形主体，底部留出箭头空间
                    addRoundRect(
                        roundRect = RoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height - arrowPx,
                            cornerRadius = CornerRadius(cornerPx, cornerPx)
                        )
                    )
                    // 底部三角形箭头
                    moveTo(arrowCenterX - arrowPx, size.height - arrowPx)
                    lineTo(arrowCenterX, size.height)
                    lineTo(arrowCenterX + arrowPx, size.height - arrowPx)
                    close()
                }
            }
        })
    }
}