package com.wanbaohe.markuplayers.domain.model

import com.shifenmiao.common.components.text.RichTextStyleSpan
import com.t8rin.imagetoolbox.core.domain.model.Outline
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerSource

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
        /**
         * 文字内局部样式区间(富文本):颜色 / 粗体 / 斜体 / 字号倍率,
         * 字段 null = 继承图层级样式。由画布选区工具条写入,文字增删由
         * Compose TrackedRange 跟随迁移;不变量见 common 的 TextSelectionSupport。
         */
        val styleSpans: List<RichTextStyleSpan> = emptyList(),
        /**
         * 框宽(相对底图宽),配合 [widthManuallySet]:
         * [widthManuallySet]=false(默认)= 自适应收紧:框随文字内容收缩(选框始终贴合文字,
         * 缩放后手柄不飞出画布),[widthRatio] 此时只是折行上限,null = 画布宽;
         * [widthManuallySet]=true = 固定框宽(拖边/角改框后落此态,文字在框内重排)
         */
        val widthRatio: Float? = null,
        /** 是否被框尺寸手柄手动拖过:true = 固定框宽;false = 框随文字收缩(对齐图文卡片) */
        val widthManuallySet: Boolean = false,
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

/**
 * 是否可直接应用位图滤镜(UiFilter)。位图类图层(图片/贴纸/涂鸦)直接支持;
 * 文字/形状为矢量图层,需先光栅化为图片图层才能用滤镜(调节不受限,均支持)。
 */
val LayerType.isBitmapFilterable: Boolean
    get() = this is LayerType.Image || this is LayerType.Sticker || this is LayerType.Draw
