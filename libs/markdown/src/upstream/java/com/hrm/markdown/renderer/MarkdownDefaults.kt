package com.hrm.markdown.renderer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.codehigh.renderer.CodeColors
import com.hrm.codehigh.renderer.CodeDefaults
import com.hrm.codehigh.renderer.CodeLayout
import com.hrm.codehigh.renderer.CodeTypography

internal val LocalMarkdownColors = compositionLocalOf<MarkdownColors?> { null }
internal val LocalMarkdownTypography = compositionLocalOf { MarkdownTypography() }
internal val LocalMarkdownLayout = compositionLocalOf { MarkdownLayout() }

// Keep Markdown's original code metrics; the native component retains its own defaults.
internal val DefaultMarkdownCodeTypography = CodeTypography().let {
    it.copy(
        text = it.text.copy(fontSize = 14.sp),
        inlineCode = it.inlineCode.copy(fontSize = 14.sp),
        title = it.title.copy(fontSize = 13.sp),
    )
}
internal val DefaultMarkdownCodeLayout = CodeLayout(contentPadding = PaddingValues(12.dp), shape = RoundedCornerShape(8.dp))
private val LocalMarkdownCodeColors = compositionLocalOf<CodeColors?> { null }
private val LocalMarkdownCodeTypography = compositionLocalOf { DefaultMarkdownCodeTypography }
private val LocalMarkdownCodeLayout = compositionLocalOf { DefaultMarkdownCodeLayout }

object MarkdownDefaults {
    fun lightColors(): MarkdownColors = MarkdownColors()
    fun darkColors(): MarkdownColors = MarkdownColors(
        text = Color(0xFFE6EDF3), link = Color(0xFF4493F8),
        blockQuoteBorder = Color(0xFF3D444D), blockQuoteText = Color(0xFF9198A1), divider = Color(0xFF3D444D),
        tableBorder = Color(0xFF3D444D), tableHeaderBackground = Color(0xFF161B22), highlight = Color(0xFF5C4B00),
        taskChecked = Color(0xFF3FB950), taskUnchecked = Color(0xFF3D444D), kbdBackground = Color(0xFF343942),
        spoiler = Color(0xFF3D444D), containerBackground = Color(0xFF161B22), containerHeaderBackground = Color(0xFF21262D),
        admonitions = darkAdmonitionColors(),
    )

    /** Inherits the nearest provider, otherwise follows the system. Named overrides preserve the palette. */
    @Composable
    fun colors(
        text: Color? = null,
        heading: Color? = null,
        link: Color? = null,
        blockQuoteBorder: Color? = null,
        blockQuoteText: Color? = null,
        divider: Color? = null,
        listBullet: Color? = null,
        tableBorder: Color? = null,
        tableHeaderBackground: Color? = null,
        highlight: Color? = null,
        taskChecked: Color? = null,
        taskUnchecked: Color? = null,
        footnote: Color? = null,
        kbdBackground: Color? = null,
        spoiler: Color? = null,
        containerBackground: Color? = null,
        containerHeaderBackground: Color? = null,
        admonitions: Map<String, MarkdownAdmonitionColors>? = null,
    ): MarkdownColors {
        val base = LocalMarkdownColors.current ?: if (isSystemInDarkTheme()) darkColors() else lightColors()
        return base.copy(
            text = text ?: base.text,
            heading = heading ?: base.heading,
            link = link ?: base.link,
            blockQuoteBorder = blockQuoteBorder ?: base.blockQuoteBorder,
            blockQuoteText = blockQuoteText ?: base.blockQuoteText,
            divider = divider ?: base.divider,
            listBullet = listBullet ?: base.listBullet,
            tableBorder = tableBorder ?: base.tableBorder,
            tableHeaderBackground = tableHeaderBackground ?: base.tableHeaderBackground,
            highlight = highlight ?: base.highlight,
            taskChecked = taskChecked ?: base.taskChecked,
            taskUnchecked = taskUnchecked ?: base.taskUnchecked,
            footnote = footnote ?: base.footnote,
            kbdBackground = kbdBackground ?: base.kbdBackground,
            spoiler = spoiler ?: base.spoiler,
            containerBackground = containerBackground ?: base.containerBackground,
            containerHeaderBackground = containerHeaderBackground ?: base.containerHeaderBackground,
            admonitions = admonitions ?: base.admonitions,
        )
    }

    @Composable fun typography(): MarkdownTypography = LocalMarkdownTypography.current
    @Composable fun layout(): MarkdownLayout = LocalMarkdownLayout.current
    @Composable fun codeColors(): CodeColors = LocalMarkdownCodeColors.current ?: CodeDefaults.colors()
    @Composable fun codeTypography(): CodeTypography = LocalMarkdownCodeTypography.current
    @Composable fun codeLayout(): CodeLayout = LocalMarkdownCodeLayout.current
    @Composable fun material3Colors(): MarkdownColors = material3Colors(MaterialTheme.colorScheme)
    fun material3Colors(scheme: ColorScheme): MarkdownColors = MarkdownColors(
        text = scheme.onSurface, link = scheme.primary,
        blockQuoteBorder = scheme.outline, blockQuoteText = scheme.onSurfaceVariant, divider = scheme.outlineVariant,
        tableBorder = scheme.outlineVariant, tableHeaderBackground = scheme.surfaceContainerHighest,
        highlight = scheme.tertiaryContainer, taskChecked = scheme.primary, taskUnchecked = scheme.outline,
        footnote = scheme.onSurfaceVariant, kbdBackground = scheme.surfaceVariant, spoiler = scheme.onSurfaceVariant,
        containerBackground = scheme.surfaceVariant, containerHeaderBackground = scheme.surfaceContainerHighest,
        admonitions = material3AdmonitionColors(scheme),
    )
}

/** Optional app-level defaults. Instance parameters always override these values. */
@Composable
fun ProvideMarkdownDefaults(
    colors: MarkdownColors = MarkdownDefaults.colors(),
    typography: MarkdownTypography = MarkdownDefaults.typography(),
    layout: MarkdownLayout = MarkdownDefaults.layout(),
    codeColors: CodeColors = MarkdownDefaults.codeColors(),
    codeTypography: CodeTypography = MarkdownDefaults.codeTypography(),
    codeLayout: CodeLayout = MarkdownDefaults.codeLayout(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMarkdownColors provides colors,
        LocalMarkdownTypography provides typography,
        LocalMarkdownLayout provides layout,
        LocalMarkdownCodeColors provides codeColors,
        LocalMarkdownCodeTypography provides codeTypography,
        LocalMarkdownCodeLayout provides codeLayout,
        content = content,
    )
}
