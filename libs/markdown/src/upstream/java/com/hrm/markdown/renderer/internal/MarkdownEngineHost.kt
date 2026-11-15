package com.hrm.markdown.renderer.internal

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.hrm.latex.renderer.measure.LatexMeasurerState
import com.hrm.markdown.parser.ast.Document
import com.hrm.markdown.renderer.DiagramHostRegistry
import com.hrm.markdown.renderer.MarkdownComponents
import com.hrm.markdown.renderer.ResolvedMarkdownStyle
import com.hrm.markdown.renderer.internal.compose.DefaultMarkdownComposePainter
import com.hrm.markdown.renderer.internal.compose.MarkdownComposePainter
import com.hrm.markdown.renderer.internal.core.compile.RenderCompileSession
import com.hrm.markdown.renderer.internal.core.compile.RenderModelCompiler
import com.hrm.markdown.renderer.internal.core.compile.RenderCompileEnvironment
import com.hrm.markdown.renderer.internal.core.model.InternalRenderDocumentModel
import com.hrm.markdown.renderer.internal.layout.engine.DefaultMarkdownLayoutEngine
import com.hrm.markdown.renderer.internal.layout.engine.LayoutEnvironment
import com.hrm.markdown.renderer.internal.layout.engine.MarkdownBlockLayoutCache
import com.hrm.markdown.renderer.internal.layout.engine.MarkdownLayoutEngine
import com.hrm.markdown.renderer.internal.layout.engine.MarkdownLayoutSession
import com.hrm.markdown.renderer.internal.layout.inline.InlineLayoutMetricsSnapshot
import com.hrm.markdown.renderer.internal.layout.inline.InlineLayoutRuntime
import com.hrm.markdown.renderer.internal.layout.inline.inlineLayoutEpoch
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutDocumentModel

internal class MarkdownEngineHost(
    private val compiler: RenderModelCompiler = RenderCompileSession(),
    private val layoutEngine: MarkdownLayoutEngine = DefaultMarkdownLayoutEngine,
    val composePainter: MarkdownComposePainter = DefaultMarkdownComposePainter,
) {
    private val inlineLayoutRuntime = InlineLayoutRuntime()
    private val blockLayoutCache = MarkdownBlockLayoutCache()

    internal fun inlineLayoutMetrics(): InlineLayoutMetricsSnapshot =
        inlineLayoutRuntime.metricsSnapshot()

    internal fun resetInlineLayoutMetrics() {
        inlineLayoutRuntime.resetMetrics()
    }

    fun compile(
        document: Document,
        facadeState: RendererFacadeState,
    ): InternalRenderDocumentModel {
        return compiler.compile(
            document = document,
            environment = facadeState.toCompileEnvironment(),
        )
    }

    fun layout(
        renderDocument: InternalRenderDocumentModel,
        facadeState: RendererFacadeState,
        viewportWidth: Float,
        blockSpacing: Float = 0f,
        onLinkClick: ((String) -> Unit)? = null,
        onFootnoteClick: ((String) -> Unit)? = null,
        density: Density,
        textMeasurer: TextMeasurer,
        latexMeasurer: LatexMeasurerState,
        diagramHostRegistry: DiagramHostRegistry,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        components: MarkdownComponents = MarkdownComponents.Default,
    ): InternalLayoutDocumentModel {
        return layoutEngine.layout(
            document = renderDocument,
            environment = createLayoutEnvironment(
                facadeState = facadeState,
                viewportWidth = viewportWidth,
                blockSpacing = blockSpacing,
                onLinkClick = onLinkClick,
                onFootnoteClick = onFootnoteClick,
                density = density,
                textMeasurer = textMeasurer,
                latexMeasurer = latexMeasurer,
                diagramHostRegistry = diagramHostRegistry,
                layoutDirection = layoutDirection,
                components = components,
            ),
        )
    }

    fun layoutSession(
        renderDocument: InternalRenderDocumentModel,
        facadeState: RendererFacadeState,
        viewportWidth: Float,
        blockSpacing: Float = 0f,
        onLinkClick: ((String) -> Unit)? = null,
        onFootnoteClick: ((String) -> Unit)? = null,
        density: Density,
        textMeasurer: TextMeasurer,
        latexMeasurer: LatexMeasurerState,
        diagramHostRegistry: DiagramHostRegistry,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        components: MarkdownComponents = MarkdownComponents.Default,
    ): MarkdownLayoutSession = MarkdownLayoutSession(
        document = renderDocument,
        environment = createLayoutEnvironment(
            facadeState = facadeState,
            viewportWidth = viewportWidth,
            blockSpacing = blockSpacing,
            onLinkClick = onLinkClick,
            onFootnoteClick = onFootnoteClick,
            density = density,
            textMeasurer = textMeasurer,
            latexMeasurer = latexMeasurer,
            diagramHostRegistry = diagramHostRegistry,
            layoutDirection = layoutDirection,
            components = components,
        ),
        engine = layoutEngine,
        sharedBlockCache = blockLayoutCache,
    )

    private fun createLayoutEnvironment(
        facadeState: RendererFacadeState,
        viewportWidth: Float,
        blockSpacing: Float,
        onLinkClick: ((String) -> Unit)?,
        onFootnoteClick: ((String) -> Unit)?,
        density: Density,
        textMeasurer: TextMeasurer,
        latexMeasurer: LatexMeasurerState,
        diagramHostRegistry: DiagramHostRegistry,
        layoutDirection: LayoutDirection,
        components: MarkdownComponents,
    ): LayoutEnvironment = LayoutEnvironment(
        viewportWidth = viewportWidth,
        blockSpacing = blockSpacing,
        markdownTheme = facadeState.theme,
        onLinkClick = onLinkClick,
        onFootnoteClick = onFootnoteClick,
        density = density,
        layoutDirection = layoutDirection,
        textMeasurer = textMeasurer,
        latexMeasurer = latexMeasurer,
        directiveRegistry = facadeState.directiveRegistry,
        latexConfig = facadeState.config.latexConfig,
        diagramHostRegistry = diagramHostRegistry,
        codeBlockHeaderHeightPx = with(density) { components.codeBlockHeader?.height?.roundToPx()?.toFloat() ?: 0f },
        tableToolbarHeightPx = with(density) { components.tableToolbar?.height?.roundToPx()?.toFloat() ?: 0f },
        inlineLayoutRuntime = inlineLayoutRuntime,
        inlineLayoutEpoch = inlineLayoutEpoch(
            theme = facadeState.theme,
            directiveRegistry = facadeState.directiveRegistry,
            config = facadeState.config,
            onLinkClick = onLinkClick,
            onFootnoteClick = onFootnoteClick,
            density = density,
            textMeasurer = textMeasurer,
            latexMeasurer = latexMeasurer,
        ),
    )
}

internal fun RendererFacadeState.toCompileEnvironment(): RenderCompileEnvironment = RenderCompileEnvironment(
    enableHeadingNumbering = config.enableHeadingNumbering,
    softBreakPolicy = config.softBreakPolicy,
)
