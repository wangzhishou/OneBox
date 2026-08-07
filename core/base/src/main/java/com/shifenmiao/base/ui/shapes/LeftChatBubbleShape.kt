package com.shifenmiao.base.ui.shapes

import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class LeftChatBubbleShape(
    private val arrowSize: Dp = 4.dp,
    private val arrowPosition: Dp = 16.dp,
    private val cornerSize: Dp = 16.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arrowSizePx = with(density) { arrowSize.toPx() }
        val arrowPositionPx = with(density) { arrowPosition.toPx() }
        val cornerSizePx = cornerSize.value
        return Outline.Generic(Path().apply {
            // Draw a rectangle for the body of the bubble
            addRoundRect(
                roundRect = RoundRect(
                    left = arrowSizePx, // leave space for the arrow on the left
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    radiusX = cornerSizePx, // 使用计算出的圆角像素值
                    radiusY = cornerSizePx  // 使用计算出的圆角像素值
                )
            )

            // Draw an equilateral triangle for the arrow of the bubble on the left
            moveTo(arrowSizePx, arrowPositionPx)
            lineTo(0f, arrowPositionPx + arrowSizePx)
            lineTo(arrowSizePx, arrowPositionPx + arrowSizePx * 2)
            close()
        })
    }
}