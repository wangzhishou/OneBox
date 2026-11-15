/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

private const val BOLD_WEIGHT_THRESHOLD = 600

data class RichTextInlineStyle(
    val id: String,
    val label: String,
    val glyph: String,
    val matches: (SpanStyle) -> Boolean,
    val apply: (style: SpanStyle, enabled: Boolean) -> SpanStyle,
    val markdown: RichTextMarkdownInlineRule? = null,
) {
    companion object {
        const val BoldId = "bold"
        const val ItalicId = "italic"
        const val UnderlineId = "underline"
        const val StrikethroughId = "strikethrough"
        const val InlineCodeId = "inline_code"

        fun bold(label: String = "Bold") = RichTextInlineStyle(
            id = BoldId,
            label = label,
            glyph = "B",
            matches = { it.fontWeight?.weight?.let { weight -> weight >= BOLD_WEIGHT_THRESHOLD } == true },
            apply = { style, enabled ->
                style.copy(fontWeight = if (enabled) FontWeight.Bold else null)
            },
            markdown = RichTextMarkdownInlineRule.delimited("**", order = 20),
        )

        fun italic(label: String = "Italic") = RichTextInlineStyle(
            id = ItalicId,
            label = label,
            glyph = "I",
            matches = { it.fontStyle == FontStyle.Italic },
            apply = { style, enabled ->
                style.copy(fontStyle = if (enabled) FontStyle.Italic else null)
            },
            markdown = RichTextMarkdownInlineRule.delimited("_", order = 30),
        )

        fun underline(label: String = "Underline") = RichTextInlineStyle(
            id = UnderlineId,
            label = label,
            glyph = "U",
            matches = { it.textDecoration?.contains(TextDecoration.Underline) == true },
            apply = { style, enabled ->
                style.withDecoration(TextDecoration.Underline, enabled)
            },
            markdown = RichTextMarkdownInlineRule.delimited("<u>", "</u>", order = 0),
        )

        fun strikethrough(label: String = "Strikethrough") = RichTextInlineStyle(
            id = StrikethroughId,
            label = label,
            glyph = "S",
            matches = { it.textDecoration?.contains(TextDecoration.LineThrough) == true },
            apply = { style, enabled ->
                style.withDecoration(TextDecoration.LineThrough, enabled)
            },
            markdown = RichTextMarkdownInlineRule.delimited("~~", order = 10),
        )

        fun inlineCode(
            background: Color,
            label: String = "Inline code",
        ) = RichTextInlineStyle(
            id = InlineCodeId,
            label = label,
            glyph = "</>",
            matches = { it.fontFamily == FontFamily.Monospace },
            apply = { style, enabled ->
                style.copy(
                    fontFamily = if (enabled) FontFamily.Monospace else null,
                    background = if (enabled) background else Color.Unspecified,
                )
            },
            markdown = RichTextMarkdownInlineRule.inlineCode(),
        )
    }
}

private fun SpanStyle.withDecoration(
    target: TextDecoration,
    enabled: Boolean,
): SpanStyle {
    val decorations = buildList {
        if (enabled) add(target)
        textDecoration?.let { current ->
            if (target != TextDecoration.Underline && current.contains(TextDecoration.Underline)) {
                add(TextDecoration.Underline)
            }
            if (target != TextDecoration.LineThrough && current.contains(TextDecoration.LineThrough)) {
                add(TextDecoration.LineThrough)
            }
        }
    }
    return copy(
        textDecoration = decorations.takeIf { it.isNotEmpty() }?.let(TextDecoration::combine),
    )
}
