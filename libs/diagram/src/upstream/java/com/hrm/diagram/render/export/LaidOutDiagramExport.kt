package com.hrm.diagram.render.export

import com.hrm.diagram.core.DiagramApi
import com.hrm.diagram.core.color.DotColorParser
import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.export.ExportBackground
import com.hrm.diagram.core.export.JpegExportOptions
import com.hrm.diagram.core.export.RasterExportOptions
import com.hrm.diagram.core.export.RenderedDiagram
import com.hrm.diagram.core.export.SvgExportOptions
import com.hrm.diagram.core.export.exportJpeg
import com.hrm.diagram.core.export.exportPng
import com.hrm.diagram.core.export.svg.exportSvg
import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.PieIR
import com.hrm.diagram.core.ir.SequenceIR
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.ir.TimeSeriesIR
import com.hrm.diagram.core.ir.TreeIR
import com.hrm.diagram.core.text.HeuristicTextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.graph.GraphIrRenderer
import com.hrm.diagram.render.graph.GraphRenderStyle
import com.hrm.diagram.render.theme.ThemeResolver

/**
 * Convert a laid out one-shot diagram into the stable export payload.
 *
 * Current implementation supports `GraphIR` sources first; unsupported families return the same
 * bounds with no commands plus an export warning.
 *
 * Minimal usage:
 * ```kotlin
 * val rendered = laidOut.prepareExport()
 * ```
 */
@DiagramApi
fun LaidOutDiagram.prepareExport(
    theme: DiagramTheme = DiagramTheme.Default,
    background: ExportBackground = ExportBackground.Auto,
): RenderedDiagram {
    return when (val source = source) {
        is GraphIR -> renderGraphDiagram(source, this, theme, background)
        is PieIR -> renderFamilyDiagram(this, theme, background, renderPieEntities(source, this, theme))
        is TreeIR -> renderFamilyDiagram(this, theme, background, renderTreeEntities(source, this, theme))
        is SequenceIR -> renderFamilyDiagram(this, theme, background, renderSequenceEntities(source, this, theme))
        is TimeSeriesIR -> renderFamilyDiagram(this, theme, background, renderTimeSeriesEntities(source, this, theme))
        else -> unsupportedRenderedDiagram(this, source, theme, background)
    }
}

/**
 * Convenience one-shot SVG export for fully laid out diagrams.
 *
 * Minimal usage:
 * ```kotlin
 * val svg = laidOut.toSvg()
 * ```
 */
@DiagramApi
fun LaidOutDiagram.toSvg(
    theme: DiagramTheme = DiagramTheme.Default,
    options: SvgExportOptions = SvgExportOptions(),
): String = prepareExport(theme = theme, background = options.background).exportSvg(options).value

@DiagramApi
suspend fun LaidOutDiagram.toPng(
    theme: DiagramTheme = DiagramTheme.Default,
    options: RasterExportOptions = RasterExportOptions(),
): ByteArray = prepareExport(theme = theme, background = options.background).exportPng(options).value

@DiagramApi
suspend fun LaidOutDiagram.toJpeg(
    theme: DiagramTheme = DiagramTheme.Default,
    options: JpegExportOptions = JpegExportOptions(),
): ByteArray = prepareExport(theme = theme, background = options.background).exportJpeg(options).value

private fun renderGraphDiagram(
    graph: GraphIR,
    laidOut: LaidOutDiagram,
    theme: DiagramTheme,
    background: ExportBackground,
): RenderedDiagram {
    val renderer = GraphIrRenderer(
        textMeasurer = HeuristicTextMeasurer(),
        style = graphRenderStyle(theme, graph),
    )
    val drawCommands = renderer.render(graph, laidOut).flatMap { it.commands }
    return RenderedDiagram(
        bounds = laidOut.bounds,
        drawCommands = drawCommands,
        background = resolveBackground(theme, background),
    )
}

private fun renderFamilyDiagram(
    laidOut: LaidOutDiagram,
    theme: DiagramTheme,
    background: ExportBackground,
    entities: List<com.hrm.diagram.render.cache.DrawEntity>,
): RenderedDiagram = RenderedDiagram(
    bounds = laidOut.bounds,
    drawCommands = entities.flatMap { it.commands },
    background = resolveBackground(theme, background),
)

private fun unsupportedRenderedDiagram(
    laidOut: LaidOutDiagram,
    source: DiagramModel,
    theme: DiagramTheme,
    background: ExportBackground,
): RenderedDiagram =
    RenderedDiagram(
        bounds = laidOut.bounds,
        drawCommands = emptyList(),
        background = resolveBackground(theme, background),
    )

private fun graphRenderStyle(
    theme: DiagramTheme,
    graph: GraphIR,
): GraphRenderStyle {
    val typography = theme.typography
    val colors = theme.colors
    val resolvedGraph = ThemeResolver.resolveGraph(theme)
    val nodeFont = typography.bodyFont
    val edgeFont = FontSpec(
        family = typography.bodyFont.family,
        sizeSp = (typography.bodyFont.sizeSp - 2f).coerceAtLeast(10f),
        weight = typography.bodyFont.weight,
        italic = typography.bodyFont.italic,
    )
    val clusterFont = FontSpec(
        family = typography.titleFont.family,
        sizeSp = (typography.titleFont.sizeSp - 4f).coerceAtLeast(12f),
        weight = typography.titleFont.weight,
        italic = typography.titleFont.italic,
    )
    return GraphRenderStyle(
        prefix = exportPrefixOf(graph),
        nodeFont = nodeFont,
        edgeFont = edgeFont,
        clusterFont = clusterFont,
        nodeFill = resolvedGraph.nodeFill,
        nodeStroke = resolvedGraph.nodeStroke,
        nodeText = resolvedGraph.nodeText,
        edgeColor = resolvedGraph.edge,
        edgeLabelText = resolvedGraph.edgeLabelText,
        edgeLabelBg = resolvedGraph.edgeLabelBackground,
        clusterFill = resolvedGraph.clusterFill,
        clusterStroke = resolvedGraph.clusterStroke,
        clusterText = colors.textPrimary,
        nodeFontOf = { node, style -> fontOf(node, style, typography) },
        nodeTextColorOf = { node, _ -> DotColorParser.parseColor(node.payload["dot.node.html.fontcolor"]) },
        edgeLabelColorOf = { edge, prefix, _ ->
            DotColorParser.parseColor(edge.payload["${dotLabelPrefix(prefix)}.html.fontcolor"])
                ?: DotColorParser.parseColor(edge.payload["${dotLabelPrefix(prefix)}.fontcolor"])
        },
        graphBackground = { ir, _ ->
            if (ir.sourceLanguage == SourceLanguage.DOT) {
                DotColorParser.parseColor(ir.styleHints.extras["dot.graph.bgcolor"]) ?: resolvedGraph.background
            } else {
                theme.background
            }
        },
    )
}

private fun fontOf(
    node: Node,
    style: GraphRenderStyle,
    typography: com.hrm.diagram.core.theme.Typography,
): FontSpec {
    val fallback = when (node.shape) {
        NodeShape.Circle, NodeShape.Ellipse -> typography.bodyFont
        else -> style.nodeFont
    }
    val family = node.payload["dot.node.html.fontname"]?.takeIf { it.isNotBlank() }
        ?: node.payload["dot.node.fontname"]?.takeIf { it.isNotBlank() }
        ?: fallback.family
    val size = node.payload["dot.node.html.fontsize"]?.toFloatOrNull()?.coerceIn(6f, 96f)
        ?: node.payload["dot.node.fontsize"]?.toFloatOrNull()?.coerceIn(6f, 96f)
        ?: fallback.sizeSp
    return FontSpec(
        family = family,
        sizeSp = size,
        weight = if (node.payload["dot.node.html.bold"].toBoolean()) 700 else fallback.weight,
        italic = node.payload["dot.node.html.italic"].toBoolean() || fallback.italic,
    )
}

private fun exportPrefixOf(graph: GraphIR): String =
    when (graph.sourceLanguage) {
        com.hrm.diagram.core.ir.SourceLanguage.DOT -> "dot"
        com.hrm.diagram.core.ir.SourceLanguage.MERMAID -> "mermaid"
        com.hrm.diagram.core.ir.SourceLanguage.PLANTUML -> "plantuml"
    }

private fun dotLabelPrefix(prefix: String): String =
    if (prefix == "edge") "dot.edge" else prefix

private fun resolveBackground(
    theme: DiagramTheme,
    background: ExportBackground,
): Color? =
    when (background) {
        ExportBackground.Auto -> theme.background
        ExportBackground.Transparent -> null
        is ExportBackground.Solid -> background.color
    }

private fun Color.copy(alpha: Float): Color {
    val clamped = alpha.coerceIn(0f, 1f)
    val a = (clamped * 255f).toInt().coerceIn(0, 255)
    return Color((argb and 0x00FFFFFF) or (a shl 24))
}
