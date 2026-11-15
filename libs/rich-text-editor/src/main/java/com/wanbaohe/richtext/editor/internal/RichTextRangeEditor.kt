@file:OptIn(ExperimentalFoundationApi::class)

/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.internal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TrackedRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

internal fun TextFieldBuffer.replaceSpanStyles(
    target: TextRange,
    scheme: RichTextStyleScheme,
    transform: (SpanStyle) -> SpanStyle,
) {
    if (target.collapsed) return
    val tracked = getSpanStyles(target).mapNotNull { range ->
        if (!range.isValid) return@mapNotNull null
        TrackedSpanStyle(range, range.textRange, range.spanStyle)
    }
    val immutableStyles = tracked.map {
        AnnotatedString.Range(it.style, it.range.start, it.range.end)
    }
    val replacements = richTextSpanSegments(target, immutableStyles, scheme.reduceSpanStyle)
        .map { segment -> segment.range to transform(segment.style) }

    tracked.forEach { style -> trimSpanStyle(style, target) }
    replacements.forEach { (range, style) ->
        if (!range.collapsed && style != SpanStyle()) {
            addStyle(style, range, ExpandPolicy.AtEnd)
        }
    }
}

internal fun TextFieldBuffer.replaceParagraphStyles(
    target: TextRange,
    transform: (ParagraphStyle) -> ParagraphStyle,
) {
    if (target.collapsed) return
    val tracked = getParagraphStyles(target).mapNotNull { range ->
        if (!range.isValid) return@mapNotNull null
        TrackedParagraphStyle(range, range.textRange, range.paragraphStyle)
    }
    var effectiveStyle = ParagraphStyle()
    tracked.forEach { style ->
        effectiveStyle = effectiveStyle.merge(style.style)
    }
    val replacement = transform(effectiveStyle)
    tracked.forEach { style -> trimParagraphStyle(style, target) }
    if (replacement != ParagraphStyle()) addStyle(replacement, target, ExpandPolicy.AtEnd)
}

private data class TrackedSpanStyle(
    val tracked: TrackedRange<SpanStyle>,
    val range: TextRange,
    val style: SpanStyle,
)

private data class TrackedParagraphStyle(
    val tracked: TrackedRange<ParagraphStyle>,
    val range: TextRange,
    val style: ParagraphStyle,
)

private fun TextFieldBuffer.trimSpanStyle(style: TrackedSpanStyle, target: TextRange) {
    val intersection = style.range.intersection(target) ?: return
    val before = TextRange(style.range.start, intersection.start).takeUnless { it.collapsed }
    val after = TextRange(intersection.end, style.range.end).takeUnless { it.collapsed }
    when {
        before == null && after == null -> removeStyle(style.tracked)
        before == null -> style.tracked.textRange = after!!
        after == null -> style.tracked.textRange = before
        else -> {
            style.tracked.textRange = before
            addStyle(style.style, after, ExpandPolicy.AtEnd)
        }
    }
}

private fun TextFieldBuffer.trimParagraphStyle(
    style: TrackedParagraphStyle,
    target: TextRange,
) {
    val intersection = style.range.intersection(target) ?: return
    val before = TextRange(style.range.start, intersection.start).takeUnless { it.collapsed }
    val after = TextRange(intersection.end, style.range.end).takeUnless { it.collapsed }
    when {
        before == null && after == null -> removeStyle(style.tracked)
        before == null -> style.tracked.textRange = after!!
        after == null -> style.tracked.textRange = before
        else -> {
            style.tracked.textRange = before
            addStyle(style.style, after, ExpandPolicy.AtEnd)
        }
    }
}
