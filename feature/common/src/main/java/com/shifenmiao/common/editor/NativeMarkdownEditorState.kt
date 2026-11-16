package com.shifenmiao.common.editor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
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
    val bodyStyle = MaterialTheme.typography.bodyLarge
    val quoteColor = MaterialTheme.colorScheme.onSurfaceVariant
    val quoteLabel = stringResource(R.string.native_editor_style_quote)
    val highlightColor = MaterialTheme.colorScheme.tertiaryContainer
    val highlightLabel = stringResource(R.string.native_editor_style_highlight)
    val inlineCodeBackground = MaterialTheme.colorScheme.surfaceContainer
    return remember(
        base,
        bodyStyle.fontSize,
        bodyStyle.lineHeight,
        quoteColor,
        quoteLabel,
        highlightColor,
        highlightLabel,
        inlineCodeBackground,
    ) {
        val inlineStyles = base.inlineStyles.map { style ->
            if (style.id == RichTextInlineStyle.InlineCodeId) {
                RichTextInlineStyle.inlineCode(
                    background = inlineCodeBackground,
                    label = style.label,
                )
            } else {
                style
            }
        } + RichTextInlineStyle(
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
        )

        // 与 libs/markdown 的 MarkdownTypography 默认值对齐：H1 32/40、H2 24/32、H3 20/28。
        // 编辑区用同一套字号/行高，切到预览时正文不会跳字号，也不会整体位移。
        val blockStyles = base.blockStyles.map { style ->
            when (style.id) {
                RichTextBlockStyle.Heading1Id -> RichTextBlockStyle.heading(
                    id = RichTextBlockStyle.Heading1Id,
                    label = style.label,
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    markdownPrefix = "# ",
                    fontWeight = FontWeight.Bold,
                )

                RichTextBlockStyle.Heading2Id -> RichTextBlockStyle.heading(
                    id = RichTextBlockStyle.Heading2Id,
                    label = style.label,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    markdownPrefix = "## ",
                    fontWeight = FontWeight.Bold,
                )

                else -> style
            }
        } + listOf(
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
                spanStyle = SpanStyle(color = quoteColor, fontSize = bodyStyle.fontSize),
                paragraphStyle = ParagraphStyle(lineHeight = bodyStyle.lineHeight),
                markdownPrefix = "> ",
                matches = { block ->
                    block.segmentStyles.isNotEmpty() && block.segmentStyles.all { style ->
                        style.color == quoteColor && style.fontSize == bodyStyle.fontSize
                    }
                },
                clearSpan = { style ->
                    style.copy(
                        color = if (style.color == quoteColor) Color.Unspecified else style.color,
                        fontSize = if (style.fontSize == bodyStyle.fontSize) {
                            bodyStyle.fontSize
                        } else {
                            style.fontSize
                        },
                    )
                },
                clearParagraph = { style ->
                    if (style.lineHeight == bodyStyle.lineHeight) {
                        style.copy(lineHeight = bodyStyle.lineHeight)
                    } else {
                        style
                    }
                },
            ),
        )

        base.copy(
            inlineStyles = inlineStyles,
            blockStyles = blockStyles,
        )
    }
}

internal const val NATIVE_EDITOR_HEADING_3_ID = "heading_3"
internal const val NATIVE_EDITOR_QUOTE_ID = "quote"
