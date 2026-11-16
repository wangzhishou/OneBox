package com.hrm.markdown.renderer

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Color-free defaults; explicit colors in a style override MarkdownColors.
 *
 * 标题刻度与 App 页面标题对齐：H1 与 AppBar 标题（titleLarge 18sp）同号，
 * H2–H6 依次递减，阅读界面里标题不再压过页面本身的层级。
 * 编辑器侧 `feature/common` 的 RichTextStyleScheme 必须使用同一组字号/行高。
 */
@Immutable
data class MarkdownTypography(
    val h1: TextStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),
    val h2: TextStyle = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 25.sp),
    val h3: TextStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    val h4: TextStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp),
    val h5: TextStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    val h6: TextStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp),
    val text: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    val footnote: SpanStyle = SpanStyle(fontSize = 12.sp),
    val superscript: SpanStyle = SpanStyle(fontSize = 12.sp),
    val subscript: SpanStyle = SpanStyle(fontSize = 12.sp),
    val kbd: SpanStyle = SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Medium),
    val strikethrough: SpanStyle = SpanStyle(textDecoration = TextDecoration.LineThrough),
    val insertedText: SpanStyle = SpanStyle(textDecoration = TextDecoration.Underline),
    val abbreviation: SpanStyle = SpanStyle(textDecoration = TextDecoration.Underline),
)
