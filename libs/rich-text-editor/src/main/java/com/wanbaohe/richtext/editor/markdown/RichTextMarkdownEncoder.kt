/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 * Licensed under the Apache License, Version 2.0
 */

package com.wanbaohe.richtext.editor.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextRange
import com.wanbaohe.richtext.editor.RichTextEditorState
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.internal.clearRichTextBlockSpanStyle
import com.wanbaohe.richtext.editor.internal.paragraphRanges
import com.wanbaohe.richtext.editor.internal.resolveRichTextBlockStyle
import com.wanbaohe.richtext.editor.internal.richTextBlockSnapshot
import com.wanbaohe.richtext.editor.internal.richTextSpanSegments
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

class RichTextMarkdownEncoder(
    private val scheme: RichTextStyleScheme,
    private val escapePlainText: (String) -> String = ::escapeRichTextMarkdownText,
) {
    fun encode(snapshot: RichTextSnapshot): String {
        val source = snapshot.text
        return source.paragraphRanges(TextRange(0, source.length)).joinToString("\n") { paragraph ->
            encodeParagraph(source, paragraph, snapshot)
        }
    }

    private fun encodeParagraph(
        source: String,
        paragraph: TextRange,
        snapshot: RichTextSnapshot,
    ): String {
        if (paragraph.collapsed) return ""
        val blockSnapshot = richTextBlockSnapshot(source, paragraph, snapshot, scheme)
        val blockStyle = resolveRichTextBlockStyle(blockSnapshot, scheme)
        val isListItem = scheme.listStyle.hasMarker(blockSnapshot.text)
        val contentStart = paragraph.start + if (isListItem) scheme.listStyle.marker.length else 0
        val content = encodeInlineContent(
            source = source,
            range = TextRange(contentStart, paragraph.end),
            snapshot = snapshot,
        )
        val prefix = blockStyle.markdownPrefix.ifEmpty {
            if (isListItem) scheme.listStyle.markdownPrefix else ""
        }
        return prefix + content
    }

    private fun encodeInlineContent(
        source: String,
        range: TextRange,
        snapshot: RichTextSnapshot,
    ): String {
        if (range.collapsed) return ""
        val segments = richTextSpanSegments(range, snapshot.spanStyles, scheme.reduceSpanStyle)
        return buildString {
            val openFrames = mutableListOf<RichTextMarkdownFrame>()
            segments.forEach { segment ->
                val inlineStyle = clearRichTextBlockSpanStyle(segment.style, scheme)
                val frames = scheme.inlineStyles.mapNotNull { definition ->
                    val rule = definition.markdown ?: return@mapNotNull null
                    if (definition.matches(inlineStyle)) {
                        RichTextMarkdownFrame(definition.id, rule)
                    } else {
                        null
                    }
                }
                val text = source.substring(segment.range.start, segment.range.end)
                val exclusive = frames.firstOrNull { it.rule.exclusive }
                if (exclusive != null) {
                    closeMarkdownFrames(openFrames, 0)
                    append(exclusive.rule.wrap(text))
                    return@forEach
                }

                val nextFrames = frames.sortedByDescending { it.rule.order }
                if (nextFrames.any { it.rule.prefix == null || it.rule.suffix == null }) {
                    closeMarkdownFrames(openFrames, 0)
                    var result = escapePlainText(text)
                    nextFrames.sortedBy { it.rule.order }.forEach { frame ->
                        result = frame.rule.wrap(result)
                    }
                    append(result)
                    return@forEach
                }

                var commonSize = 0
                while (commonSize < openFrames.size && commonSize < nextFrames.size &&
                    openFrames[commonSize].styleId == nextFrames[commonSize].styleId
                ) {
                    commonSize++
                }
                closeMarkdownFrames(openFrames, commonSize)
                nextFrames.subList(commonSize, nextFrames.size).forEach { frame ->
                    append(frame.rule.prefix)
                    openFrames += frame
                }
                append(escapePlainText(text))
            }
            closeMarkdownFrames(openFrames, 0)
        }
    }

    private fun StringBuilder.closeMarkdownFrames(
        frames: MutableList<RichTextMarkdownFrame>,
        targetSize: Int,
    ) {
        for (index in frames.lastIndex downTo targetSize) {
            append(frames[index].rule.suffix)
            frames.removeAt(index)
        }
    }

    private data class RichTextMarkdownFrame(
        val styleId: String,
        val rule: com.wanbaohe.richtext.editor.style.RichTextMarkdownInlineRule,
    )
}

fun RichTextEditorState.exportMarkdown(encoder: RichTextMarkdownEncoder): String =
    encoder.encode(snapshot())

@Composable
fun rememberRichTextMarkdownEncoder(
    scheme: RichTextStyleScheme,
): RichTextMarkdownEncoder = remember(scheme) { RichTextMarkdownEncoder(scheme) }

fun escapeRichTextMarkdownText(text: String): String = buildString(text.length) {
    text.forEach { char ->
        if (char in "\\`*_{}[]<>#~") append('\\')
        append(char)
    }
}
