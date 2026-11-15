package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.Size
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.parser.mermaid.MermaidFlowchartParser
import com.hrm.diagram.parser.mermaid.MermaidFrontend
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.graph.GraphIrRenderer
import com.hrm.diagram.render.graph.GraphMeasurePolicy
import com.hrm.diagram.render.graph.GraphRenderStyle
import com.hrm.diagram.render.graph.graphLabelText
import com.hrm.diagram.render.streaming.BatchLineStreamingSubPipeline
import com.hrm.diagram.render.streaming.DrawEntitySnapshotProvider
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.streaming.kernel.GraphPipelineProfile
import com.hrm.diagram.render.theme.ThemeResolver

/**
 * Sub-pipeline for Mermaid flowchart. Syntax and style adaptation stay here; GraphIR
 * measuring/layout/rendering is delegated to the shared graph modules.
 */
internal class MermaidFlowchartSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val colors = ThemeResolver.resolveMermaidFlowchart(theme)
    private val parser = MermaidFlowchartParser()
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val measurePolicy = GraphMeasurePolicy(
        textMeasurer = textMeasurer,
        defaultSize = Size(120f, 48f),
        maxWidth = 220f,
        minWidth = 64f,
        minHeight = 36f,
        labelOf = ::labelTextOf,
        fontOf = { labelFont },
        paddingOf = { node ->
            when (node.shape) {
                NodeShape.Circle, NodeShape.Diamond -> 28f to 18f
                NodeShape.Stadium, NodeShape.RoundedBox -> 18f to 12f
                else -> 14f to 10f
            }
        },
    )
    private val renderer = GraphIrRenderer(
        textMeasurer,
        GraphRenderStyle(
            prefix = "mermaid",
            nodeFont = labelFont,
            edgeFont = edgeLabelFont,
            nodeFill = colors.nodeFill,
            nodeStroke = colors.nodeStroke,
            nodeText = colors.nodeText,
            edgeColor = colors.edge,
            edgeLabelText = colors.edgeLabelText,
            edgeLabelBg = colors.edgeLabelBackground,
            clusterFill = colors.clusterFill,
            clusterStroke = colors.clusterStroke,
            clusterText = colors.nodeText,
            graphBackground = { _, _ -> colors.background },
            nodeCorner = { node, rect ->
                when (node.shape) {
                    NodeShape.Circle, NodeShape.Stadium -> minOf(rect.size.width, rect.size.height) / 2f
                    NodeShape.RoundedBox -> 14f
                    else -> 4f
                }
            },
            nodeLabel = ::labelTextOf,
            nodeFontOf = { _, _ -> labelFont },
        ),
    )
    private val profile = GraphPipelineProfile(
        sourceLanguage = SourceLanguage.MERMAID,
        defaultNodeSize = Size(120f, 48f),
        measurePolicy = measurePolicy,
    )
    private val kernel = profile.kernel(
        textMeasurer = textMeasurer,
        renderEntities = { graph, laid -> renderer.render(graph, laid) },
    )
    private val graphPipeline = MermaidGraphSubPipelineKernel(
        acceptLine = parser::acceptLine,
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        graphKernel = kernel,
    )

    override fun updateGraphStyles(styles: MermaidGraphStyleState) {
        graphPipeline.updateGraphStyles(styles)
    }

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = graphPipeline.acceptLines(
        previousSnapshot = previousSnapshot,
        lines = expandMermaidFlowchartLines(lines),
        seq = seq,
        isFinal = isFinal,
    )

    override fun dispose() {
        graphPipeline.clear()
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = graphPipeline.drawEntities()

    private fun labelTextOf(node: Node): String =
        graphLabelText(node.label).ifBlank { node.id.value }
}

/** Internal SPI used by the dispatcher to delegate per-line work. */
internal interface MermaidSubPipeline : DrawEntitySnapshotProvider, BatchLineStreamingSubPipeline<List<Token>, PipelineAdvance> {
    /** Optional hook for Mermaid GraphIR-based styling (flowchart/erDiagram). */
    fun updateGraphStyles(styles: MermaidGraphStyleState) {}

    /** Optional hook for non-GraphIR frontmatter/style extras needed during render. */
    fun updateStyleExtras(extras: Map<String, String>) {}

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance

}

/** Helper kept here to avoid duplicating across sub-pipelines. */
internal fun isLineNonBlank(line: List<Token>): Boolean =
    line.any { !MermaidFrontend.isCommentToken(it) && !MermaidFrontend.isNewlineToken(it) }
