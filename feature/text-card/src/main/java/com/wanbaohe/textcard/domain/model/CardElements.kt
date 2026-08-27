package com.wanbaohe.textcard.domain.model

import java.util.UUID

/**
 * 形状元素(类型/几何/默认参数对照 markup-layers 图片创作的形状工具)。
 * 每个形状一个图层,可独立拖动/缩放/旋转/进图层面板。
 * offsetX/offsetY 为外接框左上角归一化坐标(X 相对画布宽、Y 相对画布高);
 * widthRatio 相对画布宽、heightRatio 相对画布高(与 markup ShapeSpec 一致)。
 */
data class ShapeElementSpec(
    val id: String = UUID.randomUUID().toString(),
    val kind: CardShapeKind,
    val colorArgb: Long = DEFAULT_SHAPE_COLOR,
    val filled: Boolean = true,
    val widthRatio: Float,
    val heightRatio: Float,
    val cornerRadiusRatio: Float = 0f,
    val strokeWidthRatio: Float = 0.01f,
    val alpha: Float = 1f,
    override val offsetX: Float = 0f,
    override val offsetY: Float = 0f,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
) : ElementTransform {
    companion object {
        /** 新形状默认颜色(与 markup ShapeSpec 同款示例绿) */
        const val DEFAULT_SHAPE_COLOR = 0xFF4CAF50L

        /** 各形状默认参数(宽高保持形状特征比例),默认落画布中心 */
        fun defaultFor(kind: CardShapeKind): ShapeElementSpec {
            val base = when (kind) {
                CardShapeKind.Rectangle -> ShapeElementSpec(
                    kind = kind,
                    widthRatio = 0.3f,
                    heightRatio = 0.22f,
                    cornerRadiusRatio = 0.01f
                )

                CardShapeKind.Arrow -> ShapeElementSpec(
                    kind = kind,
                    widthRatio = 0.3f,
                    heightRatio = 0.18f
                )

                CardShapeKind.Line -> ShapeElementSpec(
                    kind = kind,
                    filled = false,
                    widthRatio = 0.3f,
                    heightRatio = 0.05f,
                    strokeWidthRatio = 0.008f
                )

                CardShapeKind.Circle, CardShapeKind.Triangle, CardShapeKind.Star ->
                    ShapeElementSpec(kind = kind, widthRatio = 0.3f, heightRatio = 0.3f)
            }
            return base.copy(
                offsetX = 0.5f - base.widthRatio / 2,
                offsetY = 0.5f - base.heightRatio / 2
            )
        }
    }
}

enum class CardShapeKind {
    Rectangle, Circle, Triangle, Arrow, Line, Star
}

/**
 * 画笔元素:一次绘制会话的笔画集合,坐标相对画布归一化(x 相对宽、y 相对高)。
 * 元素外接框 = 整张画布(offset 0 起点),变换作用于整个画布框。
 */
data class DrawElementSpec(
    val id: String = UUID.randomUUID().toString(),
    val strokes: List<CardDrawStroke>,
    val alpha: Float = 1f,
    override val offsetX: Float = 0f,
    override val offsetY: Float = 0f,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
) : ElementTransform

/** 一次笔画:采样点归一化 + 颜色 + 粗细(相对画布宽) */
data class CardDrawStroke(
    val points: List<CardStrokePoint>,
    val colorArgb: Long,
    val widthRatio: Float,
)

data class CardStrokePoint(
    val x: Float,
    val y: Float,
)

/** 画笔默认参数(与 markup DrawSessionState 同款:预设蓝 + 18px/画布宽) */
const val DEFAULT_DRAW_COLOR = 0xFF005FFFL
const val DEFAULT_DRAW_WIDTH_RATIO = 0.018f

/** 画笔内容包围盒(归一化):所有笔画点 min/max + 最粗笔画半宽外扩 */
data class CardStrokeBounds(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
)

/** 元素内容(全部笔画)的归一化包围盒;无笔画返回 null */
fun DrawElementSpec.contentBounds(): CardStrokeBounds? {
    val all = strokes.flatMap { it.points }
    if (all.isEmpty()) return null
    val inflate = (strokes.maxOf { it.widthRatio } / 2f)
    val left = (all.minOf { it.x } - inflate).coerceIn(0f, 1f)
    val top = (all.minOf { it.y } - inflate).coerceIn(0f, 1f)
    val right = (all.maxOf { it.x } + inflate).coerceIn(0f, 1f)
    val bottom = (all.maxOf { it.y } + inflate).coerceIn(0f, 1f)
    return CardStrokeBounds(
        left = left,
        top = top,
        width = (right - left).coerceAtLeast(0.01f),
        height = (bottom - top).coerceAtLeast(0.01f)
    )
}
