package com.hrm.markdown.renderer

import androidx.compose.runtime.Immutable
import com.hrm.latex.renderer.model.LatexConfig

/** Rendering-only snapshot for internal cache keys, not a public configuration container. */
@Immutable
internal data class MarkdownRenderOptions(
    val enableHeadingNumbering: Boolean = false,
    val latexConfig: LatexConfig = LatexConfig(),
    val softBreakPolicy: MarkdownSoftBreakPolicy = MarkdownSoftBreakPolicy.Space,
) {
    companion object { val Default = MarkdownRenderOptions() }
}

enum class MarkdownSoftBreakPolicy { Space, LineBreak }
