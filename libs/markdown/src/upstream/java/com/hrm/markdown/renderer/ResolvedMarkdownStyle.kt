package com.hrm.markdown.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import com.hrm.codehigh.renderer.CodeColors
import com.hrm.codehigh.renderer.CodeDefaults
import com.hrm.codehigh.renderer.CodeLayout
import com.hrm.codehigh.renderer.CodeTypography
import com.hrm.diagram.core.theme.DiagramTheme

private val DefaultTypography = MarkdownTypography()
private val DefaultCodeTypography = CodeTypography()
private val DefaultHeadingStyles = DefaultTypography.let { listOf(it.h1, it.h2, it.h3, it.h4, it.h5, it.h6) }

/** Internal resolved snapshot shared by layout and paint; never a second public settings API. */
@Immutable
internal data class ResolvedMarkdownStyle(
    val colors: MarkdownColors = MarkdownColors(),
    val typography: MarkdownTypography = MarkdownTypography(),
    val layout: MarkdownLayout = MarkdownLayout(),
    val codeColors: CodeColors = CodeDefaults.darkColors(),
    val codeTypography: CodeTypography = DefaultMarkdownCodeTypography,
    val codeLayout: CodeLayout = DefaultMarkdownCodeLayout,
    val diagramTheme: DiagramTheme = DiagramTheme.Default,
) {
    val bodyStyle = TextStyle(color = colors.text).merge(DefaultTypography.text).merge(typography.text)
    val headingStyles = listOf(
        typography.h1, typography.h2, typography.h3, typography.h4, typography.h5, typography.h6,
    ).mapIndexed { index, value ->
        TextStyle(color = colors.heading).merge(DefaultHeadingStyles[index]).merge(value)
    }
    val codeBlockStyle get() = TextStyle(color = colors.text).merge(DefaultCodeTypography.text).merge(codeTypography.text)
    val inlineCodeStyle get() = TextStyle(color = codeColors.inlineText)
        .merge(DefaultCodeTypography.inlineCode).merge(codeTypography.inlineCode).toSpanStyle()
    val inlineCodeBackground get() = codeColors.inlineBackground
    val footnoteStyle get() = SpanStyle(color = colors.footnote).merge(typography.footnote)
    val superscriptStyle get() = typography.superscript
    val subscriptStyle get() = typography.subscript
    val kbdStyle get() = SpanStyle(background = colors.kbdBackground).merge(typography.kbd)
    val strikethroughStyle get() = typography.strikethrough
    val insertedTextStyle get() = typography.insertedText
    val abbreviationStyle get() = typography.abbreviation
}

internal val LocalMarkdownStyle = compositionLocalOf { ResolvedMarkdownStyle() }
@Composable
internal fun ProvideMarkdownStyle(style: ResolvedMarkdownStyle, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMarkdownStyle provides style, content = content)
}
