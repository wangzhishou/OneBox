/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.style

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

@Immutable
data class RichTextBlockSnapshot(
    val text: String,
    val segmentStyles: List<SpanStyle>,
    val paragraphStyles: List<AnnotatedString.Range<ParagraphStyle>>,
)

data class RichTextBlockStyle(
    val id: String,
    val label: String,
    val spanStyle: SpanStyle = SpanStyle(),
    val paragraphStyle: ParagraphStyle = ParagraphStyle(),
    val markdownPrefix: String = "",
    val isDefault: Boolean = false,
    val matches: (RichTextBlockSnapshot) -> Boolean,
    val clearSpan: (SpanStyle) -> SpanStyle,
    val clearParagraph: (ParagraphStyle) -> ParagraphStyle,
) {
    companion object {
        const val BodyId = "body"
        const val Heading1Id = "heading_1"
        const val Heading2Id = "heading_2"

        fun body(label: String = "Body") = RichTextBlockStyle(
            id = BodyId,
            label = label,
            isDefault = true,
            matches = { false },
            clearSpan = { it },
            clearParagraph = { it },
        )

        fun heading(
            id: String,
            label: String,
            fontSize: TextUnit,
            lineHeight: TextUnit,
            markdownPrefix: String,
        ) = RichTextBlockStyle(
            id = id,
            label = label,
            spanStyle = SpanStyle(fontSize = fontSize, fontWeight = FontWeight.SemiBold),
            paragraphStyle = ParagraphStyle(lineHeight = lineHeight),
            markdownPrefix = markdownPrefix,
            matches = { block ->
                block.segmentStyles.isNotEmpty() && block.segmentStyles.all { style ->
                    style.fontSize == fontSize
                }
            },
            clearSpan = { style ->
                if (style.fontSize == fontSize) {
                    style.copy(
                        fontSize = TextUnit.Unspecified,
                        fontWeight = if (style.fontWeight == FontWeight.SemiBold) {
                            null
                        } else {
                            style.fontWeight
                        },
                    )
                } else {
                    style
                }
            },
            clearParagraph = { style ->
                if (style.lineHeight == lineHeight) {
                    style.copy(lineHeight = TextUnit.Unspecified)
                } else {
                    style
                }
            },
        )

        fun heading1(
            fontSize: TextUnit,
            lineHeight: TextUnit,
            label: String = "H1",
        ) = heading(Heading1Id, label, fontSize, lineHeight, "# ")

        fun heading2(
            fontSize: TextUnit,
            lineHeight: TextUnit,
            label: String = "H2",
        ) = heading(Heading2Id, label, fontSize, lineHeight, "## ")
    }
}
