package com.wanbaohe.markuplayers.domain.model

/**
 * 形状图层参数。尺寸相对底图归一化。
 *
 * @param kind 形状种类
 * @param color 颜色(ARGB),填充色或描边色
 * @param filled true=填充,false=描边
 * @param widthRatio 形状外接框宽度,相对底图宽度
 * @param heightRatio 形状外接框高度,相对底图高度
 * @param cornerRadiusRatio 圆角半径(仅矩形),相对底图宽度
 * @param strokeWidthRatio 描边粗细(仅描边模式),相对底图宽度
 */
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
        /** 新形状默认颜色(设计稿示例绿) */
        val DefaultColor: Int = 0xFF4CAF50.toInt()

        /** 各形状的默认参数:宽高能保持形状特征比例(箭头扁、线条细高等) */
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
