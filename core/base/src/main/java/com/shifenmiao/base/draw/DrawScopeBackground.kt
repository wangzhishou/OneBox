package com.shifenmiao.base.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.random.Random

fun DrawScope.drawCloudySkyWithBirds(
    size: Size,
    colors: List<Color>,
    cloudColor: Color
) {
    val gradientBrush = Brush.verticalGradient(
        colors = colors,
        startY = 0f,
        endY = 135.dp.toPx()
    )
    drawRect(brush = gradientBrush, size = size)
}

fun DrawScope.drawSciFiBackground(size: Size, colors: List<Color>, rotation: Float) {
    // Gradient Background
    val gradientBrush = Brush.linearGradient(
        colors = colors,
        start = Offset.Zero,
        end = Offset(size.width, size.height)
    )
    drawRect(brush = gradientBrush, size = size)

    // Draw Circles
    drawCircle(
        color = colors[0].copy(alpha = 0.3f),
        center = Offset(size.width * 0.3f, size.height * 0.3f),
        radius = size.minDimension * 0.2f
    )
    drawCircle(
        color = colors[1].copy(alpha = 0.3f),
        center = Offset(size.width * 0.7f, size.height * 0.7f),
        radius = size.minDimension * 0.15f
    )

    // Draw Rotating Lines
    rotate(rotation) {
        drawLine(
            color = colors[2].copy(alpha = 0.5f),
            start = Offset(size.width * 0.1f, size.height * 0.1f),
            end = Offset(size.width * 0.9f, size.height * 0.9f),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = colors[2].copy(alpha = 0.5f),
            start = Offset(size.width * 0.9f, size.height * 0.1f),
            end = Offset(size.width * 0.1f, size.height * 0.9f),
            strokeWidth = 4.dp.toPx()
        )
    }

    // Draw Random Cubes
    drawRandomCube(size, 100, 200f, 300f, colors)
    drawRandomCube(size, 100, 600f, 50f, colors)
    drawRandomCube(size, 100, 450f, 450f, colors)
    drawRandomCube(size, 100, 80f, 150f, colors)
}

fun DrawScope.drawProfileBackground(
    size: Size,
    linearGradientColors: List<Color> = listOf(
        Color.Cyan, Color.Cyan, Color.Cyan
    ),
    circleColors: List<Color> = listOf(
        Color.White, Color.White, Color.White
    )
) {
    val rectBrush = Brush.linearGradient(
        0.0f to linearGradientColors[0],
        1.0f to linearGradientColors[1],
        start = Offset(0.0f, size.height / 2),
        end = Offset(size.width, 100.0f)
    )
    drawRect(
        brush = rectBrush,
        topLeft = Offset.Zero,
        size = size
    )
    drawCircle(
        color = circleColors[0].copy(0.1F),
        center = Offset(x = 30F, y = 30F),
        radius = size.width / 2
    )
    drawCircle(
        color = circleColors[1].copy(0.2F),
        center = Offset(x = 15F, y = 15F),
        radius = size.width / 4
    )
    drawCircle(
        color = circleColors[2].copy(0.1F),
        center = Offset(size.width, y = 0F),
        radius = size.width / 5
    )
}


fun DrawScope.drawAppBackground(
    size: Size,
    linearGradientColors: List<Color>,
    maskColor: Color = Color.White
) {
    val updatedLinearGradientColors = linearGradientColors.map { it.copy(alpha = 0.1F) }
    val topGradient = Brush.sweepGradient(
        colors = updatedLinearGradientColors,
        center = Offset(size.width, 300.dp.toPx()),
    )
    drawRect(topGradient)
    // New white gradient overlay
    val whiteGradient = Brush.verticalGradient(
        colors = listOf(
            maskColor.copy(alpha = 0.5f),
            maskColor.copy(alpha = 0.0f)
        ),
        startY = 0f,
        endY = 150.dp.toPx()
    )
    drawRect(brush = whiteGradient, size = size)

    drawRandomCube(size, 200, 300f, 200f, linearGradientColors)
    drawRandomCube(size, 100, 600f, 50f, linearGradientColors)
    drawRandomCube(size, 100, 450f, 450f, linearGradientColors)
    drawRandomCube(size, 100, 80f, 150f, linearGradientColors)
}

private fun DrawScope.drawRandomCube(
    size: Size,
    cubeSize: Int = 100,
    x: Float = 0f,
    y: Float = 0f,
    linearGradientColors: List<Color> = listOf(
        Color.Cyan, Color.Cyan, Color.Cyan
    )
) {
    val topLeft = Offset(
        x = x,
        y = y
    ) // Fixed position for the cube

    // Define points for the cube
    val points = listOf(
        Offset(topLeft.x, topLeft.y), // 0
        Offset(topLeft.x + cubeSize, topLeft.y), // 1
        Offset(topLeft.x + cubeSize, topLeft.y + cubeSize), // 2
        Offset(topLeft.x, topLeft.y + cubeSize), // 3
        Offset(topLeft.x + cubeSize / 2, topLeft.y - cubeSize / 2), // 4
        Offset(topLeft.x + cubeSize + cubeSize / 2, topLeft.y - cubeSize / 2), // 5
        Offset(topLeft.x + cubeSize + cubeSize / 2, topLeft.y + cubeSize / 2), // 6
        Offset(topLeft.x + cubeSize / 2, topLeft.y + cubeSize / 2) // 7
    )

    val color = linearGradientColors[Random.nextInt(linearGradientColors.size)].copy(0.3f)

    // Draw the back face
    drawLine(color, points[0], points[1])
    drawLine(color, points[1], points[2])
    drawLine(color, points[2], points[3])
    drawLine(color, points[3], points[0])

    // Draw the side faces
    drawLine(color, points[0], points[4])
    drawLine(color, points[4], points[5])
    drawLine(color, points[5], points[1])
    drawLine(color, points[1], points[0])

    drawLine(color, points[3], points[7])
    drawLine(color, points[7], points[6])
    drawLine(color, points[6], points[2])
    drawLine(color, points[2], points[3])

    // Draw the front face
    drawLine(color, points[4], points[7])
    drawLine(color, points[7], points[6])
    drawLine(color, points[6], points[5])
    drawLine(color, points[5], points[4])
}