package com.hrm.markdown.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.hrm.codehigh.renderer.CodeColors
import com.hrm.codehigh.renderer.CodeLayout
import com.hrm.codehigh.renderer.CodeTypography

enum class MarkdownBlockSurfaceKind {
    Code,
    Quote,
    Admonition,
    Table,
    CustomContainer,
    Tabs,
    Bibliography,
    Diagram,
}

@Immutable
data class MarkdownInlineCodeInfo(
    val text: String,
    val colors: CodeColors,
    val typography: CodeTypography,
    val layout: CodeLayout,
)

typealias MarkdownBlockSurface = @Composable (
    MarkdownBlockSurfaceKind,
    @Composable () -> Unit,
) -> Unit

typealias MarkdownInlineCodeRenderer = @Composable (MarkdownInlineCodeInfo) -> Unit
