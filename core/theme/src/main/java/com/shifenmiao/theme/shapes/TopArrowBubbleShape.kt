package com.shifenmiao.theme.shapes

import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class TopArrowBubbleShape(
    private val arrowSize: Dp = 24.dp,
    private val arrowOffset: Dp = 64.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arrowSizePx = arrowSize.value
        val arrowOffsetPx = arrowOffset.value
        return Outline.Generic(Path().apply {
            // Draw a rectangle for the body of the bubble
            addRoundRect(
                roundRect = RoundRect(
                    left = 0f,
                    top = arrowSizePx, // leave space for the arrow at the top
                    right = size.width,
                    bottom = size.height,
                    radiusX = arrowSizePx,
                    radiusY = arrowSizePx
                )
            )

            // Draw a triangle for the arrow of the bubble at the top
            moveTo(size.width - arrowOffsetPx - arrowSizePx, arrowSizePx)
            lineTo(size.width - arrowOffsetPx, 0f)
            lineTo(size.width - arrowOffsetPx + arrowSizePx, arrowSizePx)
            close()
        })
    }
}