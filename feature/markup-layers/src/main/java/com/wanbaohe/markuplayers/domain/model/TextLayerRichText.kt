package com.wanbaohe.markuplayers.domain.model

import androidx.compose.ui.text.AnnotatedString
import com.shifenmiao.common.components.text.RichTextStyleSpan
import com.shifenmiao.common.components.text.buildRichText

/**
 * 文字图层富文本(局部样式 span)的读取与判定。
 *
 * 与图文卡片 TextBlock 的 styleSpans 同一套语义:
 * - null 字段继承图层级样式(图层粗体时 bold=false 表示该段反粗);
 * - 同属性区间互斥由画布选区工具条保证(见 common TextSelectionSupport);
 * - 预览/导出共用 [buildAnnotatedText]。
 */
fun LayerType.Text.buildAnnotatedText(): AnnotatedString =
    buildRichText(text, styleSpans)

/** 选区当前有效加粗态(图层级为底,区间覆盖) */
fun LayerType.Text.isRangeEffectivelyBold(start: Int, end: Int): Boolean =
    isRangeEffectivelyStyled(start, end, layerLevel = isBold(), field = { it.bold })

/** 选区当前有效斜体态 */
fun LayerType.Text.isRangeEffectivelyItalic(start: Int, end: Int): Boolean =
    isRangeEffectivelyStyled(start, end, layerLevel = isItalic(), field = { it.italic })

/** 选区当前有效颜色(最后一个覆盖选区且带颜色的 span,否则图层颜色) */
fun LayerType.Text.effectiveColorAt(start: Int, end: Int): Int =
    styleSpans.lastOrNull { it.color != null && it.start < end && it.end > start }
        ?.color?.toInt() ?: color

/** 选区当前有效字号倍率(最后一个覆盖选区且带倍率的 span,否则 1) */
fun LayerType.Text.effectiveSizeScaleAt(start: Int, end: Int): Float =
    styleSpans.lastOrNull { it.sizeScale != null && it.start < end && it.end > start }
        ?.sizeScale ?: 1f

internal fun LayerType.Text.isBold(): Boolean =
    LayerType.Text.Decoration.Bold in decorations

internal fun LayerType.Text.isItalic(): Boolean =
    LayerType.Text.Decoration.Italic in decorations

private fun LayerType.Text.isRangeEffectivelyStyled(
    start: Int,
    end: Int,
    layerLevel: Boolean,
    field: (RichTextStyleSpan) -> Boolean?,
): Boolean {
    if (start >= end) return layerLevel
    val lastCovering = styleSpans.lastOrNull { it.start <= start && it.end >= end }
        ?: styleSpans.lastOrNull { it.start < end && it.end > start }
    return lastCovering?.let(field) ?: layerLevel
}
