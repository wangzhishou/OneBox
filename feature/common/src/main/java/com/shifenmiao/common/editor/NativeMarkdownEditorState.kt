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

        // 与 libs/markdown 的 MarkdownTypography 默认值一字不差：H1 与页面标题同为 18sp，
        // H2/H3/H4 依次递减。编辑区与预览共用这套字号/行高，切模式时正文不跳、标题不缩水。
        val blockStyles = base.blockStyles.map { style ->
            when (style.id) {
                RichTextBlockStyle.Heading1Id -> RichTextBlockStyle.heading(
                    id = RichTextBlockStyle.Heading1Id,
                    label = style.label,
                    fontSize = HEADING_1_SIZE,
                    lineHeight = HEADING_1_LINE_HEIGHT,
                    markdownPrefix = "# ",
                    fontWeight = FontWeight.Bold,
                )

                RichTextBlockStyle.Heading2Id -> RichTextBlockStyle.heading(
                    id = RichTextBlockStyle.Heading2Id,
                    label = style.label,
                    fontSize = HEADING_2_SIZE,
                    lineHeight = HEADING_2_LINE_HEIGHT,
                    markdownPrefix = "## ",
                    fontWeight = FontWeight.Bold,
                )

                else -> style
            }
        } + listOf(
            RichTextBlockStyle.heading(
                id = NATIVE_EDITOR_HEADING_3_ID,
                label = "H3",
                fontSize = HEADING_3_SIZE,
                lineHeight = HEADING_3_LINE_HEIGHT,
                markdownPrefix = "### ",
            ),
            RichTextBlockStyle.heading(
                id = NATIVE_EDITOR_HEADING_4_ID,
                label = "H4",
                fontSize = HEADING_4_SIZE,
                lineHeight = HEADING_4_LINE_HEIGHT,
                markdownPrefix = "#### ",
                fontWeight = FontWeight.Medium,
            ),
            RichTextBlockStyle.heading(
                id = NATIVE_EDITOR_HEADING_5_ID,
                label = "H5",
                fontSize = HEADING_5_SIZE,
                lineHeight = HEADING_5_LINE_HEIGHT,
                markdownPrefix = "##### ",
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
internal const val NATIVE_EDITOR_HEADING_4_ID = "heading_4"
internal const val NATIVE_EDITOR_HEADING_5_ID = "heading_5"
internal const val NATIVE_EDITOR_QUOTE_ID = "quote"

// 与 libs/markdown MarkdownTypography 的 h1–h5 保持一致（改一处必须同步另一处）。
internal val HEADING_1_SIZE = 18.sp
internal val HEADING_1_LINE_HEIGHT = 26.sp
internal val HEADING_2_SIZE = 17.sp
internal val HEADING_2_LINE_HEIGHT = 25.sp
internal val HEADING_3_SIZE = 16.sp
internal val HEADING_3_LINE_HEIGHT = 24.sp
internal val HEADING_4_SIZE = 15.sp
internal val HEADING_4_LINE_HEIGHT = 22.sp
internal val HEADING_5_SIZE = 14.sp
internal val HEADING_5_LINE_HEIGHT = 20.sp
