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
import androidx.compose.ui.text.TextRange
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.internal.clearRichTextBlockParagraphStyle
import com.wanbaohe.richtext.editor.internal.clearRichTextBlockSpanStyle
import com.wanbaohe.richtext.editor.internal.fullParagraphRange
import com.wanbaohe.richtext.editor.internal.paragraphRanges
import com.wanbaohe.richtext.editor.internal.replaceParagraphStyles
import com.wanbaohe.richtext.editor.internal.replaceSpanStyles
import com.wanbaohe.richtext.editor.internal.resolveRichTextBlockStyle
import com.wanbaohe.richtext.editor.internal.richTextBlockSnapshot
import com.wanbaohe.richtext.editor.style.RichTextBlockStyle
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

fun RichTextEditorState.currentBlockStyle(
    scheme: RichTextStyleScheme,
): RichTextBlockStyle {
    val source = text.toString()
    val paragraph = source.paragraphRanges(selection).firstOrNull()
        ?: return scheme.defaultBlockStyle
    val block = richTextBlockSnapshot(source, paragraph, snapshot(), scheme)
    return resolveRichTextBlockStyle(block, scheme)
}

fun RichTextEditorState.applyBlockStyle(
    styleId: String,
    scheme: RichTextStyleScheme,
) {
    val definition = scheme.blockStyle(styleId) ?: return
    val source = text.toString()
    val paragraphs = source.paragraphRanges(selection)
    if (paragraphs.isEmpty()) return
    edit {
        paragraphs.forEach { paragraph ->
            if (!paragraph.collapsed) {
                replaceSpanStyles(paragraph, scheme) { style ->
                    val cleared = clearRichTextBlockSpanStyle(style, scheme)
                    scheme.reduceSpanStyle(cleared, definition.spanStyle)
                }
            }
            val fullRange = source.fullParagraphRange(paragraph)
            replaceParagraphStyles(fullRange) { style ->
                clearRichTextBlockParagraphStyle(style, scheme).merge(definition.paragraphStyle)
            }
        }
        selection = TextRange(paragraphs.first().start, paragraphs.last().end)
    }
}
