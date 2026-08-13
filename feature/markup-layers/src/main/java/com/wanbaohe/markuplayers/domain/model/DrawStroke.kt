package com.wanbaohe.markuplayers.domain.model

/**
 * 画笔图层的一次笔画。坐标与宽度全部相对底图归一化。
 *
 * @param points 笔画采样点,x/y 均为相对底图宽高的 0..1 归一化值
 * @param color 笔画颜色(ARGB)
 * @param widthRatio 笔画粗细,相对底图宽度的比例(如 0.02 = 底图宽度的 2%)
 * @param brush 画笔类型
 * @param alpha 笔画不透明度 0..1
 */
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

/** 画笔类型,对应设计稿的 铅笔/钢笔/毛笔/马克笔 + 橡皮擦 */
enum class BrushType {
    Pencil,
    Pen,
    Brush,
    Marker,
    Eraser,
}
