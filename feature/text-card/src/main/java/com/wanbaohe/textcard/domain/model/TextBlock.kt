package com.wanbaohe.textcard.domain.model

import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.wanbaohe.textcard.domain.render.CardLayout
import java.util.UUID

/** 元素整体变换:归一化偏移 + 缩放 + 旋转(度),与字号等基础样式叠乘,互不冲突 */
interface ElementTransform {
    val offsetX: Float
    val offsetY: Float
    val scale: Float
    val rotation: Float
}

/**
 * 卡片上的文字块(任意多条,默认标题 + 正文两块;每条一个图层)。
 *
 * @param id 稳定 id,图层/选中/编辑/拖动都按它寻址
 * @param baseTopRatio 基准 top(相对画布宽),新块在上一块基础上递增
 * @param sizeScale 字号缩放倍率,作用于块的基础字号比例 [baseSizeRatio](与手势 scale 叠乘)
 * @param widthRatio 文字框宽(相对画布宽),文字在框内折行;拖框手柄改它,字号不变
 * @param heightRatio 文字框最小高(相对画布高),0 = 随内容自适应;实际框高 = max(内容高, 设定高)
 * @param letterSpacingEm 字间距(em),直接给 TextPaint.letterSpacing / Compose letterSpacing
 * @param lineSpacingMultiplier 行距倍率,StaticLayout setLineSpacing 的 multiplier
 * @param offsetX 相对基准位置的归一化水平偏移(相对画布宽)
 * @param offsetY 相对基准位置的归一化垂直偏移(相对画布高)
 * @param scale 手势整体缩放(绕内容中心)
 * @param rotation 手势旋转(度,绕内容中心)
 * @param alpha 元素级不透明度(0..1),与颜色自身 alpha 叠乘
 */
data class TextBlock(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val baseSizeRatio: Float = CardLayout.BODY_BASE_SIZE_RATIO,
    val baseTopRatio: Float = CardLayout.BODY_BASE_TOP_RATIO,
    val font: FontType? = null,
    val sizeScale: Float = 1f,
    val widthRatio: Float = CardLayout.DEFAULT_TEXT_WIDTH_RATIO,
    val heightRatio: Float = 0f,
    val letterSpacingEm: Float = 0f,
    val lineSpacingMultiplier: Float = 1.2f,
    val alignment: CardTextAlignment = CardTextAlignment.Left,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val color: Long = 0xFF1A1A1A,
    val alpha: Float = 1f,
    override val offsetX: Float = 0f,
    override val offsetY: Float = 0f,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
) : ElementTransform

enum class CardTextAlignment {
    Left, Center, Right, Justify
}

/**
 * 装饰贴纸(支持多个;每个装饰一个图层,可独立拖动/缩放/旋转)。
 * offsetX/offsetY 为贴纸左上角的归一化坐标(X 相对画布宽、Y 相对画布高)。
 * alpha 为元素级不透明度(0..1)。
 */
data class DecorationSpec(
    val id: String = UUID.randomUUID().toString(),
    val emojiIndex: Int,
    val alpha: Float = 1f,
    override val offsetX: Float = 0f,
    override val offsetY: Float = 0f,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
) : ElementTransform {
    companion object {
        /** 新装饰默认落左上角(留 DECORATION_MARGIN_RATIO 边距,不贴边) */
        fun defaultPositionFor(canvas: CanvasSpec, emojiIndex: Int): DecorationSpec {
            val margin = CardLayout.DECORATION_MARGIN_RATIO
            return DecorationSpec(
                emojiIndex = emojiIndex,
                offsetX = margin,
                offsetY = margin * canvas.aspectRatio
            )
        }
    }
}

/**
 * AI 生成图片元素(支持多个;每个一个图层,可独立拖动/缩放/旋转,同装饰)。
 * offsetX/offsetY 为元素左上角的归一化坐标(X 相对画布宽、Y 相对画布高),
 * 图片在 IMAGE_ELEMENT_SIZE_RATIO 正方形框内 fit 居中。
 */
data class ImageElementSpec(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val alpha: Float = 1f,
    override val offsetX: Float = 0f,
    override val offsetY: Float = 0f,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
) : ElementTransform {
    companion object {
        /** 新图片元素默认落画布中心 */
        fun defaultPositionFor(canvas: CanvasSpec, uri: String): ImageElementSpec {
            val size = CardLayout.IMAGE_ELEMENT_SIZE_RATIO
            return ImageElementSpec(
                uri = uri,
                offsetX = 0.5f - size / 2,
                offsetY = 0.5f - size * canvas.aspectRatio / 2
            )
        }
    }
}
