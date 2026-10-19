package com.shifenmiao.common.components.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em

/**
 * 块内局部样式区间(富文本):[start, end) 为文字内容字符下标。
 *
 * 所有字段可空,null = 继承块级/图层级样式。区间互斥由写入侧保证
 * (同一属性不重叠),原因见 [TextSelectionSupport] 的样式重建逻辑。
 */
data class RichTextStyleSpan(
    val start: Int,
    val end: Int,
    val color: Long? = null,
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val sizeScale: Float? = null,
) {
    /** 钳制到 [length] 内;退化为空区间返回 null(调用方过滤) */
    fun clamped(length: Int): RichTextStyleSpan? {
        val s = start.coerceIn(0, length)
        val e = end.coerceIn(0, length)
        return if (s < e) copy(start = s, end = e) else null
    }

    /** 转 Compose SpanStyle(仅设置非空字段;字号倍率用 em,相对块级字号) */
    fun toSpanStyle(): SpanStyle = SpanStyle(
        color = color?.let(::Color) ?: Color.Unspecified,
        fontSize = sizeScale?.em ?: TextUnit.Unspecified,
        fontWeight = bold?.let { if (it) FontWeight.Bold else FontWeight.Normal },
        fontStyle = italic?.let { if (it) FontStyle.Italic else FontStyle.Normal }
    )
}

/** 编辑器样式层读回(getSpanStyles)→ 模型 span;四字段全空(未设置)或空区间返回 null */
fun AnnotatedString.Range<SpanStyle>.toRichTextStyleSpan(): RichTextStyleSpan? {
    if (start >= end) return null
    val spanColor = item.color
        .takeIf { it != Color.Unspecified }
        ?.let { it.toArgb().toLong() and 0xFFFF_FFFFL }
    val spanSize = item.fontSize.takeIf { it.isEm }?.value
    val spanBold = when (item.fontWeight) {
        FontWeight.Bold -> true
        FontWeight.Normal -> false
        else -> null
    }
    val spanItalic = when (item.fontStyle) {
        FontStyle.Italic -> true
        FontStyle.Normal -> false
        else -> null
    }
    if (spanColor == null && spanSize == null && spanBold == null && spanItalic == null) {
        return null
    }
    return RichTextStyleSpan(
        start = start,
        end = end,
        color = spanColor,
        bold = spanBold,
        italic = spanItalic,
        sizeScale = spanSize
    )
}

/** 预览/编辑共用:content + 局部样式叠加成 AnnotatedString(块级粗斜由 TextStyle 承担) */
fun buildRichText(
    content: String,
    spans: List<RichTextStyleSpan>,
): AnnotatedString = buildAnnotatedString {
    append(content)
    spans.forEach { span ->
        span.clamped(content.length)?.let { clamped ->
            addStyle(clamped.toSpanStyle(), clamped.start, clamped.end)
        }
    }
}

/**
 * endTextEdit trim 时同步调整局部样式区间:平移前导空白,钳制到新长度,丢弃空区间;
 * 内容被替换成占位文案(trimmed 与原内容无关联)时直接清空。
 */
fun List<RichTextStyleSpan>.adjustForTrim(
    original: String,
    trimmed: String,
): List<RichTextStyleSpan> {
    if (isEmpty()) return this
    if (trimmed.isEmpty() || !original.contains(trimmed)) return emptyList()
    val lead = original.indexOf(trimmed)
    return mapNotNull { span ->
        span.copy(start = span.start - lead, end = span.end - lead).clamped(trimmed.length)
    }
}

/** 覆盖整段文字的颜色区间(最后一个),用于整块/整层颜色的回显 */
fun List<RichTextStyleSpan>.fullRangeColor(length: Int): Long? =
    lastOrNull { it.color != null && it.start <= 0 && it.end >= length }?.color

/**
 * 去掉覆盖整段文字的颜色覆盖,保留其它属性(粗/斜/字号)。
 * 整块/整层 Color 控件改的是底层颜色,若全段仍压着旧颜色 span,
 * 视觉上就会"怎么点都不变色";只清全段 span,局部选区颜色保留。
 */
fun List<RichTextStyleSpan>.withoutFullRangeColor(length: Int): List<RichTextStyleSpan> =
    mapNotNull { span ->
        if (span.color == null || span.start > 0 || span.end < length) {
            span
        } else {
            span.copy(color = null).takeIf {
                it.bold != null || it.italic != null || it.sizeScale != null
            }
        }
    }
