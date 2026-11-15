/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle

@Immutable
data class RichTextSnapshot(
    val text: String = "",
    val spanStyles: List<AnnotatedString.Range<SpanStyle>> = emptyList(),
    val paragraphStyles: List<AnnotatedString.Range<ParagraphStyle>> = emptyList(),
) {
    companion object {
        val Empty = RichTextSnapshot()
    }

    class Builder internal constructor(private val text: String) {
        private val spanStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()
        private val paragraphStyles = mutableListOf<AnnotatedString.Range<ParagraphStyle>>()

        fun addSpanStyle(style: SpanStyle, start: Int, end: Int) {
            if (start >= 0 && start < end && end <= text.length) {
                spanStyles += AnnotatedString.Range(style, start, end)
            }
        }

        fun addParagraphStyle(style: ParagraphStyle, start: Int, end: Int) {
            if (start >= 0 && start < end && end <= text.length) {
                paragraphStyles += AnnotatedString.Range(style, start, end)
            }
        }

        fun build(): RichTextSnapshot = RichTextSnapshot(
            text = text,
            spanStyles = spanStyles.toList(),
            paragraphStyles = paragraphStyles.toList(),
        )
    }
}

fun richTextSnapshot(
    text: String,
    block: RichTextSnapshot.Builder.() -> Unit = {},
): RichTextSnapshot = RichTextSnapshot.Builder(text).apply(block).build()
