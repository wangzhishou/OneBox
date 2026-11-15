/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.internal

import androidx.compose.ui.text.TextRange

internal fun String.paragraphRanges(selection: TextRange): List<TextRange> {
    if (isEmpty()) return listOf(TextRange.Zero)
    val ranges = mutableListOf<TextRange>()
    var start = 0
    while (start <= length) {
        val newline = indexOf('\n', start)
        val end = if (newline < 0) length else newline
        ranges += TextRange(start, end)
        if (newline < 0) break
        start = newline + 1
    }
    return if (ranges.size == 1) {
        ranges
    } else {
        val adjustedEnd = if (!selection.collapsed && selection.max > 0 &&
            getOrNull(selection.max - 1) == '\n'
        ) {
            selection.max - 1
        } else {
            selection.max
        }
        val blockStart = lineStart(selection.min)
        val blockEnd = lineEnd(adjustedEnd)
        ranges.filter { it.end >= blockStart && it.start <= blockEnd }
    }
}

internal fun String.fullParagraphRange(range: TextRange): TextRange = TextRange(
    start = range.start,
    end = if (range.end < length && this[range.end] == '\n') range.end + 1 else range.end,
)

internal fun String.lineStart(offset: Int): Int {
    if (offset <= 0) return 0
    val previousNewline = lastIndexOf('\n', (offset - 1).coerceAtMost(lastIndex))
    return if (previousNewline < 0) 0 else previousNewline + 1
}

internal fun String.lineEnd(offset: Int): Int {
    val nextNewline = indexOf('\n', offset.coerceIn(0, length))
    return if (nextNewline < 0) length else nextNewline
}

internal fun TextRange.intersection(other: TextRange): TextRange? {
    val start = maxOf(min, other.min)
    val end = minOf(max, other.max)
    return if (start < end) TextRange(start, end) else null
}

internal fun TextRange.overlaps(other: TextRange): Boolean = intersection(other) != null
