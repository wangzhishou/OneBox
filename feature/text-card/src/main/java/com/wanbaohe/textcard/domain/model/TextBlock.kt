package com.wanbaohe.textcard.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import com.shifenmiao.common.components.text.RichTextStyleSpan
import com.shifenmiao.common.components.text.adjustForTrim as adjustRichTextForTrim
import com.shifenmiao.common.components.text.buildRichText
import com.shifenmiao.common.components.text.toRichTextStyleSpan
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
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
 * 文字角色及分页片段的样式模型。源文档使用稳定的角色 id,分页片段由排版引擎生成。
 *
 * @param id 源文字角色或分页片段的稳定 id
 * @param baseTopRatio 当前渲染片段的 top(相对画布宽),由排版引擎计算
 * @param sizeScale 字号缩放倍率,作用于块的基础字号比例 [baseSizeRatio]
 * @param widthRatio 文字框宽(相对画布宽),文字在框内折行
 * [widthManuallySet]=false 时它是框宽上限,框随文字宽度收缩;正式排版使用固定内容宽
 * @param widthManuallySet 是否使用固定 [widthRatio] 宽
 * @param heightRatio 文字框最小高(相对画布高),0 = 随内容自适应;实际框高 = max(内容高, 设定高)
 * @param letterSpacingEm 字间距(em),直接给 TextPaint.letterSpacing / Compose letterSpacing
 * @param lineSpacingMultiplier 行距倍率,StaticLayout setLineSpacing 的 multiplier
 * @param offsetX 相对基准位置的归一化水平偏移(相对画布宽)
 * @param offsetY 相对基准位置的归一化垂直偏移(相对画布高)
 * @param scale 手势整体缩放(绕内容中心)
 * @param rotation 手势旋转(度,绕内容中心)
 * @param alpha 元素级不透明度(0..1),与颜色自身 alpha 叠乘
 * @param isUnderline 下划线(块级;选区级局部样式不含它,选区激活时面板置灰)
 * @param isStrikethrough 删除线(块级,同上)
 * @param backgroundColor 文字底色(逐行高亮,0 = 无背景;预览 TextStyle.background,导出 BackgroundColorSpan)
 * @param outlineColor 描边颜色([outlineWidthRatio] > 0 时生效)
 * @param outlineWidthRatio 描边粗细(相对画布宽的比例),0 = 无描边;
 * 预览经 OutlinedText 双线叠绘(描边层垫底),导出经 STROKE 画笔二遍绘制
 * @param styleSpans 块内局部样式区间(字符下标;字段 null = 继承块级样式),
 * 由文字设置面板的选区作用域产生;编辑中文字增删由 Compose TrackedRange 跟随迁移。
 * 不变量:同属性区间互斥(写入侧 TextSelectionSupport 保证)——em 字号在 Android 端
 * 转 RelativeSizeSpan,重叠区间会相乘而非覆盖,互斥是防止字号爆炸的前提
 */
data class TextBlock(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val baseSizeRatio: Float = CardLayout.BODY_BASE_SIZE_RATIO,
    val baseTopRatio: Float = CardLayout.BODY_BASE_TOP_RATIO,
    val font: FontType? = null,
    val sizeScale: Float = 1f,
    val widthRatio: Float = CardLayout.DEFAULT_TEXT_WIDTH_RATIO,
    val widthManuallySet: Boolean = false,
    val heightRatio: Float = 0f,
    val letterSpacingEm: Float = 0f,
    val lineSpacingMultiplier: Float = 1.2f,
    val alignment: CardTextAlignment = CardTextAlignment.Left,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val color: Long = 0xFF1A1A1A,
    val alpha: Float = 1f,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val backgroundColor: Long = 0,
    val outlineColor: Long = 0xFFFFFFFF,
    val outlineWidthRatio: Float = 0f,
    val styleSpans: List<TextStyleSpan> = emptyList(),
    val fontSpans: List<TextCardFontSpan> = emptyList(),
    override val offsetX: Float = 0f,
    override val offsetY: Float = 0f,
    override val scale: Float = 1f,
    override val rotation: Float = 0f,
) : ElementTransform

/** 块内局部样式区间(富文本);模型与图片创作共用 [RichTextStyleSpan] */
typealias TextStyleSpan = RichTextStyleSpan

data class TextCardFontSpan(
    val start: Int,
    val end: Int,
    val font: FontType?,
) {
    fun clamped(length: Int): TextCardFontSpan? {
        val safeStart = start.coerceIn(0, length)
        val safeEnd = end.coerceIn(0, length)
        return if (safeStart < safeEnd) {
            copy(start = safeStart, end = safeEnd)
        } else null
    }
}

/** 编辑器样式层读回(getSpanStyles)→ 模型 span;四字段全空(未设置)或空区间返回 null */
fun AnnotatedString.Range<SpanStyle>.toTextStyleSpan(): TextStyleSpan? =
    toRichTextStyleSpan()

@Composable
fun TextBlock.buildAnnotatedContent(
    highlightRange: TextRange? = null,
    highlightColor: Color? = null,
): AnnotatedString {
    val baseContent = buildRichText(content, styleSpans)
    val fontFamilies = fontSpans.map { it.font.toUiFont().fontFamily }
    return buildAnnotatedString {
        append(baseContent)
        fontSpans.forEachIndexed { index, span ->
            span.clamped(content.length)?.let { clamped ->
                addStyle(
                    SpanStyle(fontFamily = fontFamilies[index]),
                    clamped.start,
                    clamped.end
                )
            }
        }
        if (highlightRange != null && highlightColor != null) {
            val start = highlightRange.min.coerceIn(0, content.length)
            val end = highlightRange.max.coerceIn(0, content.length)
            if (start < end) addStyle(SpanStyle(background = highlightColor), start, end)
        }
    }
}

/** endTextEdit trim 时同步调整局部样式区间(实现在 common,图片创作同一份逻辑) */
fun List<TextStyleSpan>.adjustForTrim(
    original: String,
    trimmed: String,
): List<TextStyleSpan> = adjustRichTextForTrim(original, trimmed)

/** 选区当前有效加粗态(块级为底,区间覆盖):工具栏 B 切换判定。选区非法/塌陷时回退块级 */
fun TextBlock.isRangeEffectivelyBold(start: Int, end: Int): Boolean =
    isRangeEffectivelyStyled(start, end, blockLevel = isBold) { it.bold }

/** 选区当前有效斜体态,同 [isRangeEffectivelyBold] */
fun TextBlock.isRangeEffectivelyItalic(start: Int, end: Int): Boolean =
    isRangeEffectivelyStyled(start, end, blockLevel = isItalic) { it.italic }

/** 选区当前有效颜色(最后一个覆盖选区且带颜色的 span,否则块级颜色):工具栏色板当前值 */
fun TextBlock.effectiveColorAt(start: Int, end: Int): Long =
    styleSpans.lastOrNull { it.color != null && it.start < end && it.end > start }?.color ?: color

/** 选区当前有效字号倍率(最后一个覆盖选区且带倍率的 span,否则 1):工具栏字号步进基准 */
fun TextBlock.effectiveSizeScaleAt(start: Int, end: Int): Float =
    styleSpans.lastOrNull { it.sizeScale != null && it.start < end && it.end > start }
        ?.sizeScale ?: 1f

/** 有效样式判定:以覆盖选区的最后一个 span 为准(追加式语义,后加覆盖先加) */
private fun TextBlock.isRangeEffectivelyStyled(
    start: Int,
    end: Int,
    blockLevel: Boolean,
    field: (TextStyleSpan) -> Boolean?,
): Boolean {
    if (start >= end) return blockLevel
    val lastCovering = styleSpans.lastOrNull { it.start <= start && it.end >= end }
        ?: styleSpans.lastOrNull { it.start < end && it.end > start }
    return lastCovering?.let(field) ?: blockLevel
}

enum class CardTextAlignment {
    Left, Center, Right, Justify
}
