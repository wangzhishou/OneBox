@file:OptIn(ExperimentalFoundationApi::class)

/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.command

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextRange
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.internal.fullParagraphRange
import com.wanbaohe.richtext.editor.internal.lineStart
import com.wanbaohe.richtext.editor.internal.paragraphRanges
import com.wanbaohe.richtext.editor.internal.replaceParagraphStyles
import com.wanbaohe.richtext.editor.style.RichTextListStyle
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

fun RichTextEditorState.isListActive(scheme: RichTextStyleScheme): Boolean {
    val source = text.toString()
    val paragraphs = source.paragraphRanges(selection)
        .filterNot { it.collapsed && text.isNotEmpty() }
    return paragraphs.isNotEmpty() && paragraphs.all { range ->
        scheme.listStyle.hasMarker(source.substring(range.start, range.end))
    }
}

fun RichTextEditorState.toggleList(scheme: RichTextStyleScheme) {
    val source = text.toString()
    val selected = source.paragraphRanges(selection)
    val paragraphs = selected.filter { range -> !range.collapsed || selected.size == 1 }
    if (paragraphs.isEmpty()) return
    val listStyle = scheme.listStyle
    val shouldRemove = paragraphs.all { range ->
        listStyle.hasMarker(source.substring(range.start, range.end))
    }
    val changedParagraphCount = if (shouldRemove) {
        paragraphs.size
    } else {
        paragraphs.count { range ->
            !listStyle.hasMarker(source.substring(range.start, range.end))
        }
    }
    val delta = changedParagraphCount * listStyle.marker.length * if (shouldRemove) -1 else 1
    val blockStart = paragraphs.first().start
    val originalBlockEnd = paragraphs.last().end

    edit {
        paragraphs.asReversed().forEach { paragraph ->
            val hasMarker = listStyle.hasMarker(source.substring(paragraph.start, paragraph.end))
            if (shouldRemove) {
                replace(paragraph.start, paragraph.start + listStyle.marker.length, "")
            } else if (!hasMarker) {
                replace(paragraph.start, paragraph.start, listStyle.marker)
            }
        }

        val updatedSource = toString()
        val updatedBlockEnd = (originalBlockEnd + delta).coerceIn(blockStart, length)
        val updatedParagraphs = updatedSource.paragraphRanges(TextRange(blockStart, updatedBlockEnd))
        updatedParagraphs.forEach { paragraph ->
            val fullRange = updatedSource.fullParagraphRange(paragraph)
            val isListItem = listStyle.hasMarker(
                updatedSource.substring(paragraph.start, paragraph.end),
            )
            replaceParagraphStyles(fullRange) { style ->
                val cleared = if (style.textIndent == listStyle.paragraphStyle.textIndent) {
                    style.copy(textIndent = null)
                } else {
                    style
                }
                if (isListItem) cleared.merge(listStyle.paragraphStyle) else cleared
            }
        }
        selection = TextRange(blockStart, updatedBlockEnd)
    }
}

@Composable
fun rememberRichTextListInputTransformation(
    scheme: RichTextStyleScheme,
): InputTransformation = remember(scheme.listStyle) {
    InputTransformation { autoContinueRichTextList(scheme.listStyle) }
}

private fun TextFieldBuffer.autoContinueRichTextList(listStyle: RichTextListStyle) {
    continueRichTextListOnNewline(listStyle)
    ensureCurrentListIndent(listStyle)
}

private fun TextFieldBuffer.continueRichTextListOnNewline(listStyle: RichTextListStyle) {
    if (changes.changeCount != 1) return
    val insertedRange = changes.getRange(0)
    val replacedRange = changes.getOriginalRange(0)
    val inserted = toString().substring(insertedRange.min, insertedRange.max)
    if (!replacedRange.collapsed || inserted != "\n") return
    val source = toString()
    val previousLineStart = source.lineStart(insertedRange.min)
    val previousLine = source.substring(previousLineStart, insertedRange.min)
    val item = previousLine.removePrefix(listStyle.marker)
    if (item.length != previousLine.length) {
        if (item.isBlank()) {
            replace(previousLineStart, insertedRange.max, "\n")
            selection = TextRange(previousLineStart + 1)
        } else {
            replace(insertedRange.max, insertedRange.max, listStyle.marker)
            selection = TextRange(insertedRange.max + listStyle.marker.length)
        }
    }
}

private fun TextFieldBuffer.ensureCurrentListIndent(listStyle: RichTextListStyle) {
    val source = toString()
    val paragraph = source.paragraphRanges(selection).firstOrNull() ?: return
    val fullRange = source.fullParagraphRange(paragraph)
    val hasMarker = listStyle.hasMarker(source.substring(paragraph.start, paragraph.end))
    if (!hasMarker || fullRange.collapsed) return
    val hasIndent = getParagraphStyles(fullRange).any { range ->
        range.isValid && range.paragraphStyle.textIndent == listStyle.paragraphStyle.textIndent
    }
    if (!hasIndent) addStyle(listStyle.paragraphStyle, fullRange, ExpandPolicy.AtEnd)
}
