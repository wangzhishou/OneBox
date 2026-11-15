package com.hrm.markdown.renderer

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme

/** Colors of native Markdown content. Code, LaTeX and diagram palettes use their native APIs. */
@Immutable
data class MarkdownColors(
    val text: Color = Color(0xFF1F2328),
    val heading: Color = text,
    val link: Color = Color(0xFF0969DA),
    val blockQuoteBorder: Color = Color(0xFFD0D7DE),
    val blockQuoteText: Color = Color(0xFF656D76),
    val divider: Color = Color(0xFFD0D7DE),
    val listBullet: Color = text,
    val tableBorder: Color = Color(0xFFD0D7DE),
    val tableHeaderBackground: Color = Color(0xFFF6F8FA),
    val highlight: Color = Color(0xFFFFF3B0),
    val taskChecked: Color = Color(0xFF1A7F37),
    val taskUnchecked: Color = Color(0xFFD0D7DE),
    val footnote: Color = link,
    val kbdBackground: Color = Color(0xFFEFF1F3),
    val spoiler: Color = Color(0xFF3A3A3A),
    val containerBackground: Color = Color(0xFFF6F8FA),
    val containerHeaderBackground: Color = Color(0xFFEBEDF0),
    val admonitions: Map<String, MarkdownAdmonitionColors> = defaultAdmonitionColors(),
)

@Immutable
data class MarkdownAdmonitionColors(val borderColor: Color, val backgroundColor: Color, val titleColor: Color)

internal fun defaultAdmonitionColors(): Map<String, MarkdownAdmonitionColors> = mapOf(
    "NOTE" to MarkdownAdmonitionColors(
        borderColor = Color(0xFF0969DA),
        backgroundColor = Color(0xFFDDF4FF),
        titleColor = Color(0xFF0969DA),
    ),
    "TIP" to MarkdownAdmonitionColors(
        borderColor = Color(0xFF1A7F37),
        backgroundColor = Color(0xFFDCFFE4),
        titleColor = Color(0xFF1A7F37),
    ),
    "IMPORTANT" to MarkdownAdmonitionColors(
        borderColor = Color(0xFF8250DF),
        backgroundColor = Color(0xFFFBEFFF),
        titleColor = Color(0xFF8250DF),
    ),
    "WARNING" to MarkdownAdmonitionColors(
        borderColor = Color(0xFFBF8700),
        backgroundColor = Color(0xFFFFF8C5),
        titleColor = Color(0xFFBF8700),
    ),
    "CAUTION" to MarkdownAdmonitionColors(
        borderColor = Color(0xFFCF222E),
        backgroundColor = Color(0xFFFFEBE9),
        titleColor = Color(0xFFCF222E),
    ),
)

internal fun darkAdmonitionColors(): Map<String, MarkdownAdmonitionColors> = mapOf(
    "NOTE" to MarkdownAdmonitionColors(
        borderColor = Color(0xFF4493F8),
        backgroundColor = Color(0xFF0D1D30),
        titleColor = Color(0xFF4493F8),
    ),
    "TIP" to MarkdownAdmonitionColors(
        borderColor = Color(0xFF3FB950),
        backgroundColor = Color(0xFF0D2818),
        titleColor = Color(0xFF3FB950),
    ),
    "IMPORTANT" to MarkdownAdmonitionColors(
        borderColor = Color(0xFFAB7DF8),
        backgroundColor = Color(0xFF1B1030),
        titleColor = Color(0xFFAB7DF8),
    ),
    "WARNING" to MarkdownAdmonitionColors(
        borderColor = Color(0xFFD29922),
        backgroundColor = Color(0xFF2A1F00),
        titleColor = Color(0xFFD29922),
    ),
    "CAUTION" to MarkdownAdmonitionColors(
        borderColor = Color(0xFFF85149),
        backgroundColor = Color(0xFF300C0C),
        titleColor = Color(0xFFF85149),
    ),
)

internal fun material3AdmonitionColors(colorScheme: ColorScheme): Map<String, MarkdownAdmonitionColors> = mapOf(
    "NOTE" to MarkdownAdmonitionColors(
        borderColor = colorScheme.primary,
        backgroundColor = colorScheme.primaryContainer,
        titleColor = colorScheme.onPrimaryContainer,
    ),
    "TIP" to MarkdownAdmonitionColors(
        borderColor = colorScheme.secondary,
        backgroundColor = colorScheme.secondaryContainer,
        titleColor = colorScheme.onSecondaryContainer,
    ),
    "IMPORTANT" to MarkdownAdmonitionColors(
        borderColor = colorScheme.tertiary,
        backgroundColor = colorScheme.tertiaryContainer,
        titleColor = colorScheme.onTertiaryContainer,
    ),
    "WARNING" to MarkdownAdmonitionColors(
        borderColor = colorScheme.error,
        backgroundColor = colorScheme.errorContainer,
        titleColor = colorScheme.onErrorContainer,
    ),
    "CAUTION" to MarkdownAdmonitionColors(
        borderColor = colorScheme.error,
        backgroundColor = colorScheme.errorContainer,
        titleColor = colorScheme.onErrorContainer,
    ),
)
