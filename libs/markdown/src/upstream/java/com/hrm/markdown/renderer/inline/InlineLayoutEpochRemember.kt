package com.hrm.markdown.renderer.inline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import com.hrm.latex.renderer.measure.LatexMeasurerState
import com.hrm.markdown.renderer.MarkdownRenderOptions
import com.hrm.markdown.renderer.ResolvedMarkdownStyle
import com.hrm.markdown.renderer.internal.layout.inline.InlineLayoutEpoch
import com.hrm.markdown.renderer.internal.layout.inline.inlineLayoutEpoch
import com.hrm.markdown.runtime.MarkdownDirectiveRegistry

/** Compose lifecycle adapter for the pure layout-epoch factory. */
@Composable
internal fun rememberInlineLayoutEpoch(
    theme: ResolvedMarkdownStyle,
    directiveRegistry: MarkdownDirectiveRegistry,
    config: MarkdownRenderOptions?,
    onLinkClick: ((String) -> Unit)?,
    onFootnoteClick: ((String) -> Unit)?,
    density: Density,
    textMeasurer: TextMeasurer,
    latexMeasurer: LatexMeasurerState,
): InlineLayoutEpoch = remember(
    theme,
    directiveRegistry,
    config,
    onLinkClick,
    onFootnoteClick,
    density.density,
    density.fontScale,
    textMeasurer,
    latexMeasurer,
) {
    inlineLayoutEpoch(
        theme = theme,
        directiveRegistry = directiveRegistry,
        config = config,
        onLinkClick = onLinkClick,
        onFootnoteClick = onFootnoteClick,
        density = density,
        textMeasurer = textMeasurer,
        latexMeasurer = latexMeasurer,
    )
}
