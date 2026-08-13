package com.wanbaohe.markuplayers.domain.model

import com.t8rin.imagetoolbox.core.domain.model.Outline
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType

/**
 * 图层类型。新增图层类型的步骤:
 * 1. 这里加一个 data class
 * 2. 在 render 注册表登记对应的预览渲染器与导出渲染器
 * 3. (可选)在工具注册表登记创建该图层的工具
 */
sealed interface LayerType {

    /**
     * 文字图层。
     * @param fontSizeRatio 字号,相对底图宽度的比例(如 0.05 = 底图宽度的 5%)
     * @param lineHeight 行距倍数(1.0 起)
     * @param letterSpacingEm 字间距(em 单位)
     */
    data class Text(
        val text: String,
        val color: Int,
        val fontSizeRatio: Float,
        val font: FontType? = null,
        val backgroundColor: Int = 0,
        val decorations: Set<Decoration> = emptySet(),
        val outline: Outline? = null,
        val alignment: TextAlignment = TextAlignment.Left,
        val lineHeight: Float = 1.2f,
        val letterSpacingEm: Float = 0f,
    ) : LayerType {
        enum class Decoration {
            Bold, Italic, Underline, LineThrough
        }

        enum class TextAlignment {
            Left, Center, Right, Justify
        }

        companion object {
            val Default = Text(
                text = "Text",
                color = 0xFF000000.toInt(),
                fontSizeRatio = 0.05f,
            )
        }
    }

    /** 画笔图层:一次绘画会话的全部笔画集合 */
    data class Draw(
        val strokes: List<DrawStroke>,
    ) : LayerType

    /** 形状图层 */
    data class Shape(
        val spec: ShapeSpec,
    ) : LayerType

    /** 贴纸图层 */
    data class Sticker(
        val source: StickerSource,
    ) : LayerType

    /** 图片图层,imageData 为 Uri/String 等图片引用(与项目现有约定一致) */
    data class Image(
        val imageData: Any,
    ) : LayerType
}
