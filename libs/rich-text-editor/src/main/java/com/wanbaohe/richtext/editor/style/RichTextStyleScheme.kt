/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.style

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp

data class RichTextListStyle(
    val marker: String = "• ",
    val paragraphStyle: ParagraphStyle = ParagraphStyle(
        // 与预览的列表内容列对齐：listIndent(24dp) + 无序列表 marker 列(16dp)。
        // 编辑区整体再通过 LIST_INDENT 补 24dp，所以这里只保留 16sp 悬挂缩进。
        textIndent = TextIndent(firstLine = 0.sp, restLine = 16.sp),
    ),
    val markdownPrefix: String = "- ",
) {
    fun hasMarker(text: String): Boolean = text.startsWith(marker)
}

data class RichTextStyleScheme(
    val inlineStyles: List<RichTextInlineStyle>,
    val blockStyles: List<RichTextBlockStyle>,
    val listStyle: RichTextListStyle = RichTextListStyle(),
    val reduceSpanStyle: (SpanStyle, SpanStyle) -> SpanStyle = ::mergeRichTextSpanStyles,
) {
    val defaultBlockStyle: RichTextBlockStyle
        get() = blockStyles.firstOrNull { it.isDefault }
            ?: blockStyles.firstOrNull { it.id == RichTextBlockStyle.BodyId }
            ?: error("RichTextStyleScheme requires a default block style")

    fun inlineStyle(id: String): RichTextInlineStyle? = inlineStyles.firstOrNull { it.id == id }

    fun blockStyle(id: String): RichTextBlockStyle? = blockStyles.firstOrNull { it.id == id }

    init {
        require(inlineStyles.map { it.id }.distinct().size == inlineStyles.size) {
            "Inline style IDs must be unique"
        }
        require(blockStyles.map { it.id }.distinct().size == blockStyles.size) {
            "Block style IDs must be unique"
        }
        require(blockStyles.count { it.isDefault } <= 1) {
            "Only one default block style is allowed"
        }
        require(blockStyles.any { it.isDefault || it.id == RichTextBlockStyle.BodyId }) {
            "RichTextStyleScheme requires a default block style"
        }
    }
}

fun mergeRichTextSpanStyles(first: SpanStyle, second: SpanStyle): SpanStyle {
    val merged = first.merge(second)
    val decorations = buildList {
        if (first.textDecoration?.contains(TextDecoration.Underline) == true ||
            second.textDecoration?.contains(TextDecoration.Underline) == true
        ) {
            add(TextDecoration.Underline)
        }
        if (first.textDecoration?.contains(TextDecoration.LineThrough) == true ||
            second.textDecoration?.contains(TextDecoration.LineThrough) == true
        ) {
            add(TextDecoration.LineThrough)
        }
    }
    return if (decorations.isEmpty()) {
        merged
    } else {
        merged.copy(textDecoration = TextDecoration.combine(decorations))
    }
}
