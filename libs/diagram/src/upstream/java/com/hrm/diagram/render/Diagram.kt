package com.hrm.diagram.render

import com.hrm.diagram.core.DiagramApi
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.text.HeuristicTextMeasurer
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.render.cache.cached
import com.hrm.diagram.render.streaming.DiagramSession
import com.hrm.diagram.render.streaming.SessionPipeline
import com.hrm.diagram.render.streaming.dot.DotSessionPipeline
import com.hrm.diagram.render.streaming.mermaid.MermaidSessionPipeline
import com.hrm.diagram.render.streaming.plantuml.PlantUmlSessionPipeline

/**
 * Top-level entry point for the diagram framework. Verbose API surface lives in subpackages
 * (`com.hrm.diagram.render.streaming`, etc.); this object exists purely as a discoverable
 * facade so callers can write `Diagram.session(...)` / `Diagram.parse(...)`.
 *
 * See `docs/api.md` for the full surface contract.
 */
@DiagramApi
object Diagram {
    /**
     * Detect whether a Markdown block or streaming source prefix should be routed to diagram
     * rendering.
     *
     * Pass a Markdown code-fence info string such as `"mermaid"`, `"plantuml"`, or `"dot"` as
     * [hint] when available; otherwise the detector falls back to syntax headers.
     */
    fun detectSource(source: CharSequence, hint: String? = null): DiagramSourceDetection =
        DiagramSourceDetector.detect(source = source, hint = hint)

    /**
     * Open a streaming session — the primary use case for LLM-driven incremental rendering.
     * See `docs/streaming.md`.
     *
     * Dispatches to the best registered pipeline for [language]:
     * - [SourceLanguage.MERMAID] → [MermaidSessionPipeline] multi-family Mermaid pipeline.
     * - [SourceLanguage.PLANTUML] → [PlantUmlSessionPipeline] multi-family PlantUML pipeline.
     * - [SourceLanguage.DOT]      → [DotSessionPipeline] Graphviz DOT pipeline.
     */
    @DiagramApi
    fun session(
        language: SourceLanguage,
        theme: DiagramTheme = DiagramTheme.Default,
        layoutOptions: LayoutOptions = LayoutOptions(),
        textMeasurer: TextMeasurer = HeuristicTextMeasurer(),
    ): DiagramSession = session(
        language = language,
        theme = theme,
        layoutOptions = layoutOptions,
        textMeasurer = textMeasurer,
        pipeline = defaultPipelineFor(language, textMeasurer, theme),
    )

    internal fun session(
        language: SourceLanguage,
        theme: DiagramTheme = DiagramTheme.Default,
        layoutOptions: LayoutOptions = LayoutOptions(),
        textMeasurer: TextMeasurer = HeuristicTextMeasurer(),
        pipeline: SessionPipeline = defaultPipelineFor(language, textMeasurer, theme),
    ): DiagramSession = DiagramSession.create(language, theme, layoutOptions, pipeline)

    private fun defaultPipelineFor(
        language: SourceLanguage,
        textMeasurer: TextMeasurer,
        theme: DiagramTheme,
    ): SessionPipeline {
        val cachedMeasurer = textMeasurer.cached()
        return when (language) {
            SourceLanguage.MERMAID -> MermaidSessionPipeline(textMeasurer = cachedMeasurer, theme = theme)
            SourceLanguage.PLANTUML -> PlantUmlSessionPipeline(textMeasurer = cachedMeasurer, theme = theme)
            SourceLanguage.DOT -> DotSessionPipeline(textMeasurer = cachedMeasurer, theme = theme)
        }
    }
}
