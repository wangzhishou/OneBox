package com.shifenmiao.common.editor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.R
import com.wanbaohe.richtext.editor.document.MarkdownDocumentEditorState
import com.wanbaohe.richtext.editor.document.MarkdownEditorBlock
import com.wanbaohe.richtext.editor.document.MarkdownEditorMode
import com.wanbaohe.richtext.editor.document.rememberMarkdownDocumentEditorState
import com.wanbaohe.richtext.editor.style.RichTextBlockStyle
import com.wanbaohe.richtext.editor.style.RichTextInlineStyle
import com.wanbaohe.richtext.editor.style.RichTextMarkdownInlineRule
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme
import com.wanbaohe.richtext.editor.style.rememberRichTextStyleScheme

typealias NativeMarkdownEditorState = MarkdownDocumentEditorState
typealias NativeEditorBlock = MarkdownEditorBlock
typealias NativeMarkdownEditorMode = MarkdownEditorMode

@Composable
fun rememberNativeMarkdownEditorState(
    initialMarkdown: String,
): NativeMarkdownEditorState = rememberMarkdownDocumentEditorState(
    initialMarkdown = initialMarkdown,
    scheme = rememberNativeMarkdownStyleScheme(),
)

@Composable
internal fun rememberNativeMarkdownStyleScheme(): RichTextStyleScheme {
    val base = rememberRichTextStyleScheme()
    val quoteColor = MaterialTheme.colorScheme.onSecondaryContainer
    val quoteLabel = stringResource(R.string.native_editor_style_quote)
    val highlightColor = MaterialTheme.colorScheme.secondaryContainer
    val highlightLabel = stringResource(R.string.native_editor_style_highlight)
    return remember(base, quoteColor, quoteLabel, highlightColor, highlightLabel) {
        base.copy(
            inlineStyles = base.inlineStyles + RichTextInlineStyle(
                id = "highlight",
                label = highlightLabel,
                glyph = "H",
                matches = { it.background == highlightColor },
                apply = { style, enabled ->
                    style.copy(
                        background = if (enabled) highlightColor else Color.Unspecified,
                    )
                },
                markdown = RichTextMarkdownInlineRule.delimited("==", order = 15),
            ),
            blockStyles = base.blockStyles + listOf(
                RichTextBlockStyle.heading(
                    id = NATIVE_EDITOR_HEADING_3_ID,
                    label = "H3",
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    markdownPrefix = "### ",
                ),
                RichTextBlockStyle(
                    id = NATIVE_EDITOR_QUOTE_ID,
                    label = quoteLabel,
                    spanStyle = SpanStyle(color = quoteColor, fontSize = 16.sp),
                    paragraphStyle = ParagraphStyle(lineHeight = 26.sp),
                    markdownPrefix = "> ",
                    matches = { block ->
                        block.segmentStyles.isNotEmpty() && block.segmentStyles.all { style ->
                            style.color == quoteColor && style.fontSize == 16.sp
                        }
                    },
                    clearSpan = { style ->
                        style.copy(
                            color = if (style.color == quoteColor) Color.Unspecified else style.color,
                            fontSize = if (style.fontSize == 16.sp) 18.sp else style.fontSize,
                        )
                    },
                    clearParagraph = { style ->
                        if (style.lineHeight == 26.sp) style.copy(lineHeight = 18.sp) else style
                    },
                ),
            ),
        )
    }
}

internal const val NATIVE_EDITOR_HEADING_3_ID = "heading_3"
internal const val NATIVE_EDITOR_QUOTE_ID = "quote"
