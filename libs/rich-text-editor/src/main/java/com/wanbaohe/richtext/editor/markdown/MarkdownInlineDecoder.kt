package com.wanbaohe.richtext.editor.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.wanbaohe.richtext.editor.style.RichTextInlineStyle
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme

internal data class DecodedInlineContent(
    val text: String,
    val styles: List<AnnotatedString.Range<SpanStyle>>,
)

internal class MarkdownInlineDecoder(
    private val scheme: RichTextStyleScheme,
) {
    fun decode(source: String): DecodedInlineContent =
        DecodedInlineBuilder(scheme).append(source).content()
}

private class DecodedInlineBuilder(
    private val scheme: RichTextStyleScheme,
) {
    private val text = StringBuilder()
    private val styles = mutableListOf<AnnotatedString.Range<SpanStyle>>()

    fun append(source: String): DecodedInlineBuilder {
        var index = 0
        while (index < source.length) {
            index += consume(source, index)
        }
        return this
    }

    fun content() = DecodedInlineContent(text.toString(), styles)

    private fun consume(source: String, index: Int): Int {
        if (source[index] == '\\' && index + 1 < source.length) {
            text.append(source[index + 1])
            return 2
        }
        val delimiter = InlineDelimiter.entries.firstOrNull { candidate ->
            source.startsWith(candidate.open, index) && candidate.isValidStart(source, index)
        }
        val closing = delimiter?.let { findClosing(source, index + it.open.length, it) } ?: -1
        val content = delimiter?.takeIf { closing >= 0 }?.let {
            source.substring(index + it.open.length, closing)
        }
        return if (delimiter == null || content == null || !delimiter.canWrap(content)) {
            text.append(source[index])
            1
        } else {
            val start = text.length
            if (delimiter == InlineDelimiter.Code) {
                text.append(content)
            } else {
                append(content)
            }
            val style = delimiter.styleIds.fold(SpanStyle()) { current, id ->
                scheme.inlineStyle(id)?.apply?.invoke(current, true) ?: current
            }
            styles += AnnotatedString.Range(style, start, text.length)
            closing + delimiter.close.length - index
        }
    }

    private fun findClosing(
        source: String,
        start: Int,
        delimiter: InlineDelimiter,
    ): Int {
        var index = start
        var closing = -1
        while (index < source.length && closing < 0) {
            val candidate = source.indexOf(delimiter.close, index)
            if (candidate < 0) return -1
            if ((candidate == 0 || source[candidate - 1] != '\\') &&
                delimiter.isValidEnd(source, candidate)
            ) {
                closing = candidate
            }
            index = candidate + delimiter.close.length
        }
        return closing
    }
}

private enum class InlineDelimiter(
    val open: String,
    val close: String,
    val styleIds: Set<String>,
) {
    Code("`", "`", setOf(RichTextInlineStyle.InlineCodeId)),
    BoldItalic("***", "***", setOf(RichTextInlineStyle.BoldId, RichTextInlineStyle.ItalicId)),
    BoldItalicUnderscore("___", "___", setOf(RichTextInlineStyle.BoldId, RichTextInlineStyle.ItalicId)),
    Bold("**", "**", setOf(RichTextInlineStyle.BoldId)),
    BoldUnderscore("__", "__", setOf(RichTextInlineStyle.BoldId)),
    Italic("*", "*", setOf(RichTextInlineStyle.ItalicId)),
    ItalicUnderscore("_", "_", setOf(RichTextInlineStyle.ItalicId)),
    Strikethrough("~~", "~~", setOf(RichTextInlineStyle.StrikethroughId)),
    Underline("<u>", "</u>", setOf(RichTextInlineStyle.UnderlineId)),
    Highlight("==", "==", setOf("highlight"));

    fun canWrap(content: String): Boolean =
        content.isNotEmpty() && (this != Code || !content.contains('\n'))

    fun isValidStart(source: String, index: Int): Boolean =
        !usesUnderscoreBoundary || index == 0 || !source[index - 1].isLetterOrDigit()

    fun isValidEnd(source: String, index: Int): Boolean {
        val end = index + close.length
        return !usesUnderscoreBoundary || end >= source.length || !source[end].isLetterOrDigit()
    }

    private val usesUnderscoreBoundary: Boolean
        get() = open.startsWith("_")
}
