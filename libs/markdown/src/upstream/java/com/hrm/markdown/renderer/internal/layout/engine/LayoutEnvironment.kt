package com.hrm.markdown.renderer.internal.layout.engine

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.hrm.latex.renderer.measure.LatexMeasurerState
import com.hrm.latex.renderer.model.LatexConfig
import com.hrm.markdown.renderer.DiagramHostRegistry
import com.hrm.markdown.renderer.ResolvedMarkdownStyle
import com.hrm.markdown.runtime.MarkdownDirectiveRegistry
import com.hrm.markdown.renderer.internal.layout.inline.InlineLayoutEpoch
import com.hrm.markdown.renderer.internal.layout.inline.InlineLayoutRuntime
import com.hrm.markdown.renderer.internal.layout.list.measureOrderedListMarkerMetrics

internal data class LayoutEnvironment(
    val viewportWidth: Float,
    val blockSpacing: Float = 0f,
    val markdownTheme: ResolvedMarkdownStyle,
    val onLinkClick: ((String) -> Unit)? = null,
    val onFootnoteClick: ((String) -> Unit)? = null,
    val density: Density,
    val layoutDirection: LayoutDirection,
    val textMeasurer: TextMeasurer,
    val latexMeasurer: LatexMeasurerState,
    val latexConfig: LatexConfig,
    val directiveRegistry: MarkdownDirectiveRegistry,
    val diagramHostRegistry: DiagramHostRegistry,
    val inlineLayoutRuntime: InlineLayoutRuntime,
    val inlineLayoutEpoch: InlineLayoutEpoch,
    val codeBlockHeaderHeightPx: Float = 0f,
    val tableToolbarHeightPx: Float = 0f,
) {
    val orderedListMarkerMetrics by lazy(LazyThreadSafetyMode.NONE) {
        measureOrderedListMarkerMetrics(markdownTheme.bodyStyle, density, textMeasurer)
    }
}
