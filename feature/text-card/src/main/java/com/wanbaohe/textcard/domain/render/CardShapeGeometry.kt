package com.wanbaohe.textcard.domain.render

import com.wanbaohe.textcard.domain.model.CardShapeKind
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 形状几何(自 markup-layers ShapeGeometry 移植):预览(Compose Path)与导出
 * (android.graphics.Path)共用同一套顶点计算,保证两侧图形一致。
 * 返回点为外接框内像素坐标(原点 = 外接框左上)。
 */
object CardShapeGeometry {

    /** 等边三角形:边长取外接框能容纳的最大值,整体居中 */
    fun trianglePoints(width: Float, height: Float): List<Pair<Float, Float>> {
        val side = min(width, height * 2f / sqrt(3f))
        val triHeight = side * sqrt(3f) / 2f
        val left = (width - side) / 2f
        val top = (height - triHeight) / 2f
        return listOf(
            (width / 2f) to top,
            (left + side) to (top + triHeight),
            left to (top + triHeight)
        )
    }

    /** 右向箭头:箭杆占左 55%、高 40%,箭头占右 45%,按外接框比例 */
    fun arrowPoints(width: Float, height: Float): List<Pair<Float, Float>> {
        val shaftX = width * 0.55f
        return listOf(
            0f to height * 0.3f,
            shaftX to height * 0.3f,
            shaftX to 0f,
            width to height / 2f,
            shaftX to height,
            shaftX to height * 0.7f,
            0f to height * 0.7f
        )
    }

    /** 五角星:外接圆半径取外接框短边一半,内半径系数 0.382,起点在正上方 */
    fun starPoints(width: Float, height: Float): List<Pair<Float, Float>> {
        val outerRadius = min(width, height) / 2f
        val innerRadius = outerRadius * 0.382f
        val centerX = width / 2f
        val centerY = height / 2f
        return (0 until 10).map { i ->
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = Math.toRadians((i * 36 - 90).toDouble())
            (centerX + radius * cos(angle).toFloat()) to (centerY + radius * sin(angle).toFloat())
        }
    }

    /** 多边形类形状(三角形/箭头/五角星)的顶点,其余形状返回空 */
    fun polygonPoints(
        kind: CardShapeKind,
        width: Float,
        height: Float
    ): List<Pair<Float, Float>> = when (kind) {
        CardShapeKind.Triangle -> trianglePoints(width, height)
        CardShapeKind.Arrow -> arrowPoints(width, height)
        CardShapeKind.Star -> starPoints(width, height)
        else -> emptyList()
    }
}
