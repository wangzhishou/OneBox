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

class DownArrowBubbleShape(private val arrowSize: Dp = 16.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arrowSizePx = with(density) { arrowSize.toPx() }
        return Outline.Generic(Path().apply {
            // Draw a rectangle for the body of the bubble
            addRoundRect(
                roundRect = RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height - arrowSizePx, // leave space for the arrow
                    radiusX = arrowSizePx,
                    radiusY = arrowSizePx
                )
            )

            // Draw a triangle for the arrow of the bubble
            val arrowPosition = size.width * 4 / 6
            moveTo(arrowPosition - arrowSizePx, size.height - arrowSizePx)
            lineTo(arrowPosition, size.height)
            lineTo(arrowPosition + arrowSizePx, size.height - arrowSizePx)
            close()
        })
    }
}