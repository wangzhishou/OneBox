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
import androidx.compose.ui.text.SpanStyle
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.internal.replaceSpanStyles
import com.wanbaohe.richtext.editor.internal.richTextSpanSegments
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

fun RichTextEditorState.isInlineStyleActive(
    styleId: String,
    scheme: RichTextStyleScheme,
): Boolean {
    val definition = scheme.inlineStyle(styleId) ?: return false
    return !selection.collapsed && richTextSpanSegments(
        selection,
        textFieldState.textStyles.getSpanStyles(selection),
        scheme.reduceSpanStyle,
    ).let { segments ->
        segments.isNotEmpty() && segments.all { definition.matches(it.style) }
    }
}

fun RichTextEditorState.toggleInlineStyle(
    styleId: String,
    scheme: RichTextStyleScheme,
) {
    val definition = scheme.inlineStyle(styleId) ?: return
    val range = selection
    if (range.collapsed) return
    val enabled = !isInlineStyleActive(styleId, scheme)
    edit {
        replaceSpanStyles(range, scheme) { style ->
            definition.apply(style, enabled)
        }
        selection = range
    }
}

fun RichTextEditorState.clearInlineFormatting(
    scheme: RichTextStyleScheme,
) {
    val range = selection
    if (range.collapsed) return
    edit {
        replaceSpanStyles(range, scheme) { SpanStyle() }
        selection = range
    }
}
