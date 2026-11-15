/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.internal

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.style.RichTextBlockSnapshot
import com.wanbaohe.richtext.editor.style.RichTextBlockStyle
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

internal data class RichTextSpanSegment(
    val range: TextRange,
    val style: SpanStyle,
)

internal fun richTextSpanSegments(
    range: TextRange,
    styles: List<AnnotatedString.Range<SpanStyle>>,
    reduce: (SpanStyle, SpanStyle) -> SpanStyle,
): List<RichTextSpanSegment> {
    if (range.collapsed) return emptyList()
    val boundaries = sortedSetOf(range.min, range.max)
    styles.forEach { style ->
        if (TextRange(style.start, style.end).overlaps(range)) {
            boundaries += style.start.coerceIn(range.min, range.max)
            boundaries += style.end.coerceIn(range.min, range.max)
        }
    }
    return boundaries.zipWithNext().mapNotNull { (start, end) ->
        if (start == end) return@mapNotNull null
        var effectiveStyle = SpanStyle()
        styles.forEach { style ->
            if (style.start <= start && style.end >= end) {
                effectiveStyle = reduce(effectiveStyle, style.item)
            }
        }
        RichTextSpanSegment(TextRange(start, end), effectiveStyle)
    }
}

internal fun richTextBlockSnapshot(
    source: String,
    paragraph: TextRange,
    snapshot: RichTextSnapshot,
    scheme: RichTextStyleScheme,
): RichTextBlockSnapshot = RichTextBlockSnapshot(
    text = source.substring(paragraph.start, paragraph.end),
    segmentStyles = richTextSpanSegments(
        paragraph,
        snapshot.spanStyles,
        scheme.reduceSpanStyle,
    ).map { it.style },
    paragraphStyles = snapshot.paragraphStyles.filter { style ->
        TextRange(style.start, style.end).overlaps(paragraph)
    },
)

internal fun resolveRichTextBlockStyle(
    block: RichTextBlockSnapshot,
    scheme: RichTextStyleScheme,
): RichTextBlockStyle = scheme.blockStyles.firstOrNull { style ->
    !style.isDefault && style.matches(block)
} ?: scheme.defaultBlockStyle

internal fun clearRichTextBlockSpanStyle(
    style: SpanStyle,
    scheme: RichTextStyleScheme,
): SpanStyle = scheme.blockStyles.fold(style) { result, blockStyle ->
    blockStyle.clearSpan(result)
}

internal fun clearRichTextBlockParagraphStyle(
    style: ParagraphStyle,
    scheme: RichTextStyleScheme,
): ParagraphStyle = scheme.blockStyles.fold(style) { result, blockStyle ->
    blockStyle.clearParagraph(result)
}
