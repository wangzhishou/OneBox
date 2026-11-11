package com.shifenmiao.model.editor

data class DrawStroke(
    val points: List<StrokePoint>,
    val color: Int,
    val widthRatio: Float,
    val brush: BrushType,
    val alpha: Float = 1f,
)

data class StrokePoint(
    val x: Float,
    val y: Float,
)

enum class BrushType {
    Pencil,
    Pen,
    Brush,
    Marker,
    Eraser,
}
