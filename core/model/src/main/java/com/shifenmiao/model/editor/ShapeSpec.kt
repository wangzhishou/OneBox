package com.shifenmiao.model.editor

data class ShapeSpec(
    val kind: ShapeKind,
    val color: Int,
    val filled: Boolean = true,
    val widthRatio: Float = 0.3f,
    val heightRatio: Float = 0.3f,
    val cornerRadiusRatio: Float = 0f,
    val strokeWidthRatio: Float = 0.01f,
) {
    companion object {
        val DefaultColor: Int = 0xFF4CAF50.toInt()

        fun default(kind: ShapeKind): ShapeSpec = when (kind) {
            ShapeKind.Rectangle -> ShapeSpec(
                kind = kind,
                color = DefaultColor,
                cornerRadiusRatio = 0.01f
            )
            ShapeKind.Arrow -> ShapeSpec(
                kind = kind,
                color = DefaultColor,
                heightRatio = 0.18f
            )
            ShapeKind.Line -> ShapeSpec(
                kind = kind,
                color = DefaultColor,
                filled = false,
                heightRatio = 0.05f,
                strokeWidthRatio = 0.008f
            )
            ShapeKind.Circle, ShapeKind.Triangle, ShapeKind.Star -> ShapeSpec(
                kind = kind,
                color = DefaultColor
            )
        }
    }
}

enum class ShapeKind {
    Rectangle,
    Circle,
    Triangle,
    Arrow,
    Line,
    Star,
}
