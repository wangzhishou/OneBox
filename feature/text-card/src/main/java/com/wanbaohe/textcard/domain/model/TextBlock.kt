package com.wanbaohe.textcard.domain.model

import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.wanbaohe.textcard.domain.render.CardLayout

/**
 * 卡片上的文字块(标题/正文)。
 *
 * @param sizeScale 字号缩放倍率,作用于各块的基础字号比例(见 CardLayout)
 * @param letterSpacingEm 字间距(em),直接给 TextPaint.letterSpacing / Compose letterSpacing
 * @param lineSpacingMultiplier 行距倍率,StaticLayout setLineSpacing 的 multiplier
 * @param offsetX 相对基准位置的归一化水平偏移(相对画布宽),画布内拖动更新
 * @param offsetY 相对基准位置的归一化垂直偏移(相对画布高)
 */
data class TextBlock(
    val content: String,
    val font: FontType? = null,
    val sizeScale: Float = 1f,
    val letterSpacingEm: Float = 0f,
    val lineSpacingMultiplier: Float = 1.2f,
    val alignment: CardTextAlignment = CardTextAlignment.Left,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val color: Long = 0xFF1A1A1A,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
)

enum class CardTextAlignment {
    Left, Center, Right, Justify
}

/** 文字块身份:标题 / 正文 */
enum class TextBlockId {
    Title, Body
}

/**
 * 装饰层:一个 emoji 贴纸 + 画布内自由拖动位置。
 * offsetX/offsetY 为贴纸左上角的归一化坐标(X 相对画布宽、Y 相对画布高);
 * emojiIndex 为 null 表示无装饰。
 */
data class DecorationSpec(
    val emojiIndex: Int? = null,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
) {
    companion object {
        /** 默认右下角(按画布比例换算归一化 top-left) */
        fun defaultFor(canvas: CanvasSpec): DecorationSpec {
            val fraction = CardLayout.DECORATION_SIZE_RATIO + CardLayout.DECORATION_MARGIN_RATIO
            return DecorationSpec(
                offsetX = 1f - fraction,
                offsetY = 1f - fraction * canvas.aspectRatio
            )
        }
    }
}
