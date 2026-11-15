package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.PathCmd
import com.hrm.diagram.core.draw.PathOp
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.core.draw.Size
import com.hrm.diagram.core.draw.Stroke
import com.hrm.diagram.core.draw.TextAnchorX
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.ir.Cluster
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.PortId
import com.hrm.diagram.core.ir.PortSide
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.parser.mermaid.C4EdgePresentation
import com.hrm.diagram.parser.mermaid.C4LegendEntry
import com.hrm.diagram.parser.mermaid.MermaidC4Parser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.cache.DrawEntityKey
import com.hrm.diagram.render.graph.GraphMeasurePolicy
import com.hrm.diagram.render.graph.GraphIrRenderer
import com.hrm.diagram.render.graph.GraphRenderStyle
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.streaming.kernel.GraphPipelineProfile
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.min
import kotlin.math.sqrt

internal class MermaidC4SubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidC4Parser()
    private val themeColors = theme.colors
    private val colors = ThemeResolver.resolveGraph(theme)
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f)
    private val clusterFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val stereotypeFont = FontSpec(family = "sans-serif", sizeSp = 10f, weight = 600)
    private val renderer = GraphIrRenderer(
        textMeasurer,
        GraphRenderStyle(
            prefix = "mermaid",
            nodeFont = labelFont,
            edgeFont = edgeLabelFont,
            clusterFont = clusterFont,
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
            customNodeCommands = { node, rect -> nodeCommands(node, rect, parser.nodeLinkSnapshot()) },
            customClusterCommands = { cluster, rect -> clusterCommands(cluster, rect, parser.boundaryLinkSnapshot()) },
            customEdgeCommands = ::edgeCommands,
            edgeEndpointAdjuster = ::adjustAnchors,
            extraEntities = { _, laid ->
                val entries = parser.legendSnapshot()
                if (entries.isEmpty()) emptyList()
                else listOf(DrawEntity(DrawEntityKey.decoration("mermaid", "c4", "legend"), legendCommands(laid.bounds, entries)))
            },
        ),
    )
    private val measurePolicy = GraphMeasurePolicy(
        textMeasurer = textMeasurer,
        defaultSize = Size(188f, 96f),
        maxWidth = 210f,
        minWidth = 144f,
        minHeight = 72f,
        labelOf = ::labelTextOf,
        fontOf = { labelFont },
        customSizeOf = ::measureNodeSize,
    )
    private val profile = GraphPipelineProfile(
        sourceLanguage = SourceLanguage.MERMAID,
        defaultNodeSize = Size(188f, 96f),
        measurePolicy = measurePolicy,
        postLayout = { ir, laid ->
            val clusterRects = LinkedHashMap<NodeId, Rect>()
            for (cluster in ir.clusters) {
                computeClusterRect(cluster, laid.nodePositions, clusterRects)
            }
            laid.copy(
                clusterRects = clusterRects,
                bounds = computeBounds(laid.nodePositions.values + clusterRects.values, parser.legendSnapshot()),
            )
        },
    )
    private val kernel = profile.kernel(
        textMeasurer = textMeasurer,
        renderEntities = { ir, laid -> renderer.render(ir, laid) },
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
    ): PipelineAdvance = graphPipeline.acceptLines(previousSnapshot, lines, seq, isFinal)

    override fun dispose() {
        graphPipeline.clear()
    }

    private fun measureNodeSize(node: Node): Size {
        val metrics = textMeasurer.measure(labelTextOf(node), labelFont, maxWidth = 210f)
        val extraWidth = when (node.shape) {
            is NodeShape.Component -> 18f
            else -> 0f
        }
        return Size(
            width = (metrics.width + 28f + extraWidth).coerceAtLeast(144f),
            height = (metrics.height + 24f).coerceAtLeast(72f),
        )
    }

    private fun computeClusterRect(
        cluster: Cluster,
        nodePositions: Map<NodeId, Rect>,
        out: LinkedHashMap<NodeId, Rect>,
    ): Rect? {
        val childRects = cluster.children.mapNotNull { nodePositions[it] }.toMutableList()
        val nestedRects = cluster.nestedClusters.mapNotNull { computeClusterRect(it, nodePositions, out) }
        childRects += nestedRects
        if (childRects.isEmpty()) return null
        val (type, title) = parseClusterLabel(cluster)
        val titleMetrics = textMeasurer.measure("$title\n$type", clusterFont, maxWidth = 220f)
        val left = childRects.minOf { it.left } - 18f
        val top = childRects.minOf { it.top } - (titleMetrics.height + 24f)
        val right = childRects.maxOf { it.right } + 18f
        val bottom = childRects.maxOf { it.bottom } + 18f
        val rect = Rect.ltrb(left, top, right, bottom)
        out[cluster.id] = rect
        return rect
    }

    private fun computeBounds(rects: Collection<Rect>, legendEntries: List<C4LegendEntry>): Rect {
        if (rects.isEmpty()) return Rect.ltrb(0f, 0f, 400f, 240f)
        val minLeft = rects.minOf { it.left }.coerceAtMost(0f)
        val minTop = rects.minOf { it.top }.coerceAtMost(0f)
        val maxRight = rects.maxOf { it.right }
        val maxBottom = rects.maxOf { it.bottom }
        val base = Rect.ltrb(minLeft, minTop, maxRight + 20f, maxBottom + 20f)
        if (legendEntries.isEmpty()) return base
        val legendRect = legendRect(base, legendEntries)
        return Rect.ltrb(
            minOf(base.left, legendRect.left - 12f),
            minOf(base.top, legendRect.top - 12f),
            maxOf(base.right, legendRect.right + 12f),
            maxOf(base.bottom, legendRect.bottom + 12f),
        )
    }

    private fun render(
        ir: GraphIR,
        laidOut: LaidOutDiagram,
        edgePresentation: Map<Int, C4EdgePresentation>,
        edgeLinks: Map<Int, String>,
        nodeLinks: Map<NodeId, String>,
        boundaryLinks: Map<NodeId, String>,
        legendEntries: List<C4LegendEntry>,
    ): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val bounds = laidOut.bounds
        out += DrawCommand.FillRect(Rect(Point(bounds.left, bounds.top), Size(bounds.size.width, bounds.size.height)), colors.background, z = 0)
        for (cluster in ir.clusters) drawCluster(cluster, laidOut.clusterRects, boundaryLinks, out)
        for (node in ir.nodes) drawNode(node, laidOut, nodeLinks, out)
        for ((index, route) in laidOut.edgeRoutes.withIndex()) {
            val edge = ir.edges.getOrNull(index) ?: continue
            drawEdge(edge, route, laidOut, edgePresentation[index] ?: C4EdgePresentation(), edgeLinks[index], out)
        }
        drawLegend(bounds, legendEntries, out)
        return out
    }

    private fun drawCluster(cluster: Cluster, clusterRects: Map<NodeId, Rect>, boundaryLinks: Map<NodeId, String>, out: MutableList<DrawCommand>) {
        val rect = clusterRects[cluster.id] ?: return
        out += clusterCommands(cluster, rect, boundaryLinks)
        for (nested in cluster.nestedClusters) drawCluster(nested, clusterRects, boundaryLinks, out)
    }

    private fun clusterCommands(cluster: Cluster, rect: Rect, boundaryLinks: Map<NodeId, String>): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val semanticColors = semanticClusterColors(parseClusterLabel(cluster).first)
        val fill = cluster.style.fill?.let { Color(it.argb) } ?: semanticColors.fill
        val strokeColor = cluster.style.stroke?.let { Color(it.argb) } ?: semanticColors.stroke
        val stroke = Stroke(width = cluster.style.strokeWidth ?: 1.5f, dash = listOf(7f, 5f))
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 12f, z = 1)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 12f, z = 2)

        val (type, title) = parseClusterLabel(cluster)
        val chipRect = Rect.ltrb(rect.left + 10f, rect.top + 10f, min(rect.right - 10f, rect.left + 176f), rect.top + 44f)
        out += DrawCommand.FillRect(rect = chipRect, color = colors.edgeLabelBackground, corner = 10f, z = 3)
        out += DrawCommand.StrokeRect(rect = chipRect, stroke = Stroke(width = 1f), color = strokeColor, corner = 10f, z = 4)
        out += DrawCommand.DrawText(
            text = title,
            origin = Point(chipRect.left + 10f, chipRect.top + 13f),
            font = clusterFont,
            color = strokeColor,
            maxWidth = chipRect.size.width - 20f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 5,
        )
        out += DrawCommand.DrawText(
            text = type,
            origin = Point(chipRect.left + 10f, chipRect.top + 28f),
            font = stereotypeFont,
            color = strokeColor,
            maxWidth = chipRect.size.width - 20f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 5,
        )
        boundaryLinks[cluster.id]?.let { out += DrawCommand.Hyperlink(href = it, rect = rect, z = 6) }
        return out
    }

    private fun drawNode(node: Node, laidOut: LaidOutDiagram, nodeLinks: Map<NodeId, String>, out: MutableList<DrawCommand>) {
        val rect = laidOut.nodePositions[node.id] ?: return
        out += nodeCommands(node, rect, nodeLinks)
    }

    private fun nodeCommands(node: Node, rect: Rect, nodeLinks: Map<NodeId, String>): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val semanticColors = semanticNodeColors(node)
        val fill = node.style.fill?.let { Color(it.argb) } ?: semanticColors.fill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: semanticColors.stroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: semanticColors.text
        val external = node.payload[MermaidC4Parser.EXTERNAL_KEY] == "true"
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.5f, dash = if (external) listOf(6f, 4f) else null)

        when (node.shape) {
            is NodeShape.Cylinder -> drawCylinder(rect, fill, strokeColor, stroke, out)
            is NodeShape.Hexagon -> drawHexagon(rect, fill, strokeColor, stroke, out)
            is NodeShape.Component -> drawComponent(rect, fill, strokeColor, stroke, out)
            is NodeShape.Custom -> {
                val customShape = node.shape as NodeShape.Custom
                if (customShape.name == "octagon") {
                    drawOctagon(rect, fill, strokeColor, stroke, out)
                } else {
                    out += DrawCommand.FillRect(rect = rect, color = fill, corner = 12f, z = 6)
                    out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 12f, z = 7)
                }
            }
            else -> {
                val corner = when (node.shape) {
                    is NodeShape.Stadium -> min(rect.size.width, rect.size.height) / 2f
                    else -> 12f
                }
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = corner, z = 6)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = corner, z = 7)
            }
        }

        out += DrawCommand.DrawText(
            text = labelTextOf(node),
            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
            font = labelFont,
            color = textColor,
            maxWidth = rect.size.width - 16f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 8,
        )
        nodeLinks[node.id]?.let { out += DrawCommand.Hyperlink(href = it, rect = rect, z = 9) }
        return out
    }

    private fun semanticNodeColors(node: Node): C4SemanticColors {
        val kind = node.payload[MermaidC4Parser.KIND_KEY].orEmpty()
        val accent = when {
            kind.startsWith("Person") -> themeColors.warning
            "Db" in kind -> themeColors.success
            "Queue" in kind -> themeColors.accentTertiary
            kind.startsWith("Component") -> themeColors.accentSecondary
            else -> themeColors.accent
        }
        val fill = accent.withAlpha(0.12f)
        return C4SemanticColors(
            fill = fill,
            stroke = accent,
            text = readableTextOn(fill),
        )
    }

    private fun semanticClusterColors(type: String): C4SemanticColors {
        val accent = when {
            type.contains("Enterprise", ignoreCase = true) -> themeColors.warning
            type.contains("Deployment", ignoreCase = true) || type == "Node" || type == "Node_L" || type == "Node_R" -> themeColors.textSecondary
            type.contains("Container", ignoreCase = true) -> themeColors.accentSecondary
            type.contains("System", ignoreCase = true) -> themeColors.accent
            else -> themeColors.border
        }
        val fill = accent.withAlpha(0.10f)
        return C4SemanticColors(
            fill = fill,
            stroke = accent,
            text = readableTextOn(fill),
        )
    }

    private fun readableTextOn(background: Color): Color = if (background.isDark()) themeColors.surface else themeColors.textPrimary

    private fun drawCylinder(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 12f, z = 6)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 12f, z = 7)
        val top = rect.top + 10f
        val bottom = rect.bottom - 10f
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(Point(rect.left + 12f, top)),
                    PathOp.CubicTo(Point(rect.left + 32f, rect.top), Point(rect.right - 32f, rect.top), Point(rect.right - 12f, top)),
                ),
            ),
            stroke = Stroke(width = 1f),
            color = strokeColor,
            z = 8,
        )
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(Point(rect.left + 12f, bottom)),
                    PathOp.CubicTo(Point(rect.left + 32f, rect.bottom), Point(rect.right - 32f, rect.bottom), Point(rect.right - 12f, bottom)),
                ),
            ),
            stroke = Stroke(width = 1f),
            color = strokeColor,
            z = 8,
        )
    }

    private fun drawHexagon(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        val dx = rect.size.width * 0.12f
        val path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(rect.left + dx, rect.top)),
                PathOp.LineTo(Point(rect.right - dx, rect.top)),
                PathOp.LineTo(Point(rect.right, (rect.top + rect.bottom) / 2f)),
                PathOp.LineTo(Point(rect.right - dx, rect.bottom)),
                PathOp.LineTo(Point(rect.left + dx, rect.bottom)),
                PathOp.LineTo(Point(rect.left, (rect.top + rect.bottom) / 2f)),
                PathOp.Close,
            ),
        )
        out += DrawCommand.FillPath(path = path, color = fill, z = 6)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 7)
    }

    private fun drawOctagon(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        val dx = rect.size.width * 0.16f
        val dy = rect.size.height * 0.18f
        val path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(rect.left + dx, rect.top)),
                PathOp.LineTo(Point(rect.right - dx, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top + dy)),
                PathOp.LineTo(Point(rect.right, rect.bottom - dy)),
                PathOp.LineTo(Point(rect.right - dx, rect.bottom)),
                PathOp.LineTo(Point(rect.left + dx, rect.bottom)),
                PathOp.LineTo(Point(rect.left, rect.bottom - dy)),
                PathOp.LineTo(Point(rect.left, rect.top + dy)),
                PathOp.Close,
            ),
        )
        out += DrawCommand.FillPath(path = path, color = fill, z = 6)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 7)
    }

    private fun drawComponent(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 10f, z = 6)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 10f, z = 7)
        val tab1 = Rect.ltrb(rect.left + 8f, rect.top + 12f, rect.left + 18f, rect.top + 22f)
        val tab2 = Rect.ltrb(rect.left + 8f, rect.top + 28f, rect.left + 18f, rect.top + 38f)
        out += DrawCommand.FillRect(rect = tab1, color = colors.edgeLabelBackground, corner = 2f, z = 8)
        out += DrawCommand.StrokeRect(rect = tab1, stroke = Stroke(width = 1f), color = strokeColor, corner = 2f, z = 9)
        out += DrawCommand.FillRect(rect = tab2, color = colors.edgeLabelBackground, corner = 2f, z = 8)
        out += DrawCommand.StrokeRect(rect = tab2, stroke = Stroke(width = 1f), color = strokeColor, corner = 2f, z = 9)
    }

    private fun drawEdge(
        edge: com.hrm.diagram.core.ir.Edge,
        route: com.hrm.diagram.layout.EdgeRoute,
        laidOut: LaidOutDiagram,
        presentation: C4EdgePresentation,
        link: String?,
        out: MutableList<DrawCommand>,
    ) {
        val pts = route.points.toMutableList()
        if (pts.size < 2) return
        anchorFor(edge.from, edge.fromPort, laidOut)?.let { pts[0] = it }
        anchorFor(edge.to, edge.toPort, laidOut)?.let { pts[pts.lastIndex] = it }
        val ops = ArrayList<PathOp>(pts.size)
        ops += PathOp.MoveTo(pts[0])
        when (route.kind) {
            RouteKind.Bezier -> {
                var i = 1
                while (i + 2 < pts.size) {
                    ops += PathOp.CubicTo(pts[i], pts[i + 1], pts[i + 2])
                    i += 3
                }
                if (i < pts.size) ops += PathOp.LineTo(pts.last())
            }
            else -> for (k in 1 until pts.size) ops += PathOp.LineTo(pts[k])
        }
        val edgeColor = edge.style.color?.let { Color(it.argb) } ?: colors.edge
        val stroke = Stroke(width = edge.style.width ?: 1.5f, dash = edge.style.dash)
        out += DrawCommand.StrokePath(path = PathCmd(ops), stroke = stroke, color = edgeColor, z = 3)
        val tail = pts[pts.size - 2]
        val head = pts.last()
        val startTail = pts[1]
        val startHead = pts[0]
        when (edge.arrow) {
            com.hrm.diagram.core.ir.ArrowEnds.None -> Unit
            com.hrm.diagram.core.ir.ArrowEnds.ToOnly -> out += arrowHead(tail, head, edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.FromOnly -> out += arrowHead(startTail, startHead, edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.Both -> {
                out += arrowHead(tail, head, edgeColor)
                out += arrowHead(startTail, startHead, edgeColor)
            }
        }
        val text = (edge.label as? RichLabel.Plain)?.text
        if (!text.isNullOrBlank()) {
            val midPoint = pts[pts.size / 2]
            val labelPoint = Point(midPoint.x + presentation.offsetX, midPoint.y + presentation.offsetY)
            val metrics = textMeasurer.measure(text, edgeLabelFont)
            val bgRect = Rect.ltrb(
                labelPoint.x - metrics.width / 2f - 4f,
                labelPoint.y - metrics.height / 2f - 2f,
                labelPoint.x + metrics.width / 2f + 4f,
                labelPoint.y + metrics.height / 2f + 2f,
            )
            out += DrawCommand.FillRect(rect = bgRect, color = edge.style.labelBg?.let { Color(it.argb) } ?: colors.edgeLabelBackground, corner = 3f, z = 9)
            out += DrawCommand.DrawText(
                text = text,
                origin = labelPoint,
                font = edgeLabelFont,
                color = presentation.textColor?.let { Color(it.argb) } ?: colors.edgeLabelText,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 10,
            )
        }
        if (!link.isNullOrBlank()) {
            val left = pts.minOf { it.x } - 8f
            val top = pts.minOf { it.y } - 8f
            val right = pts.maxOf { it.x } + 8f
            val bottom = pts.maxOf { it.y } + 8f
            out += DrawCommand.Hyperlink(href = link, rect = Rect.ltrb(left, top, right, bottom), z = 11)
        }
    }

    private fun edgeCommands(
        edge: com.hrm.diagram.core.ir.Edge,
        index: Int,
        points: List<Point>,
        kind: RouteKind,
        laidOut: LaidOutDiagram,
    ): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val pts = points.toMutableList()
        if (pts.size < 2) return out
        val ops = ArrayList<PathOp>(pts.size)
        ops += PathOp.MoveTo(pts[0])
        when (kind) {
            RouteKind.Bezier -> {
                var i = 1
                while (i + 2 < pts.size) {
                    ops += PathOp.CubicTo(pts[i], pts[i + 1], pts[i + 2])
                    i += 3
                }
                if (i < pts.size) ops += PathOp.LineTo(pts.last())
            }
            else -> for (k in 1 until pts.size) ops += PathOp.LineTo(pts[k])
        }
        val presentation = parser.edgePresentationSnapshot()[index] ?: C4EdgePresentation()
        val link = parser.edgeLinkSnapshot()[index]
        val edgeColor = edge.style.color?.let { Color(it.argb) } ?: colors.edge
        val stroke = Stroke(width = edge.style.width ?: 1.5f, dash = edge.style.dash)
        out += DrawCommand.StrokePath(path = PathCmd(ops), stroke = stroke, color = edgeColor, z = 3)
        val tail = pts[pts.size - 2]
        val head = pts.last()
        val startTail = pts[1]
        val startHead = pts[0]
        when (edge.arrow) {
            com.hrm.diagram.core.ir.ArrowEnds.None -> Unit
            com.hrm.diagram.core.ir.ArrowEnds.ToOnly -> out += arrowHead(tail, head, edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.FromOnly -> out += arrowHead(startTail, startHead, edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.Both -> {
                out += arrowHead(tail, head, edgeColor)
                out += arrowHead(startTail, startHead, edgeColor)
            }
        }
        val text = (edge.label as? RichLabel.Plain)?.text
        if (!text.isNullOrBlank()) {
            val midPoint = pts[pts.size / 2]
            val labelPoint = Point(midPoint.x + presentation.offsetX, midPoint.y + presentation.offsetY)
            val metrics = textMeasurer.measure(text, edgeLabelFont)
            val bgRect = Rect.ltrb(
                labelPoint.x - metrics.width / 2f - 4f,
                labelPoint.y - metrics.height / 2f - 2f,
                labelPoint.x + metrics.width / 2f + 4f,
                labelPoint.y + metrics.height / 2f + 2f,
            )
            out += DrawCommand.FillRect(rect = bgRect, color = edge.style.labelBg?.let { Color(it.argb) } ?: colors.edgeLabelBackground, corner = 3f, z = 9)
            out += DrawCommand.DrawText(
                text = text,
                origin = labelPoint,
                font = edgeLabelFont,
                color = presentation.textColor?.let { Color(it.argb) } ?: colors.edgeLabelText,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 10,
            )
        }
        if (!link.isNullOrBlank()) {
            val left = pts.minOf { it.x } - 8f
            val top = pts.minOf { it.y } - 8f
            val right = pts.maxOf { it.x } + 8f
            val bottom = pts.maxOf { it.y } + 8f
            out += DrawCommand.Hyperlink(href = link, rect = Rect.ltrb(left, top, right, bottom), z = 11)
        }
        return out
    }

    private fun adjustAnchors(
        edge: com.hrm.diagram.core.ir.Edge,
        points: List<Point>,
        laidOut: LaidOutDiagram,
    ): List<Point> {
        if (points.size < 2) return points
        val out = points.toMutableList()
        anchorFor(edge.from, edge.fromPort, laidOut)?.let { out[0] = it }
        anchorFor(edge.to, edge.toPort, laidOut)?.let { out[out.lastIndex] = it }
        return out
    }

    private fun anchorFor(nodeId: NodeId, portId: PortId?, laidOut: LaidOutDiagram): Point? {
        val raw = portId?.value ?: return null
        val side = when (raw) {
            "T" -> PortSide.TOP
            "R" -> PortSide.RIGHT
            "B" -> PortSide.BOTTOM
            "L" -> PortSide.LEFT
            else -> null
        } ?: return null
        val rect = laidOut.nodePositions[nodeId] ?: return null
        return when (side) {
            PortSide.TOP -> Point((rect.left + rect.right) / 2f, rect.top)
            PortSide.RIGHT -> Point(rect.right, (rect.top + rect.bottom) / 2f)
            PortSide.BOTTOM -> Point((rect.left + rect.right) / 2f, rect.bottom)
            PortSide.LEFT -> Point(rect.left, (rect.top + rect.bottom) / 2f)
        }
    }

    private fun parseClusterLabel(cluster: Cluster): Pair<String, String> {
        val raw = (cluster.label as? RichLabel.Plain)?.text.orEmpty()
        return if (raw.startsWith("__type:")) {
            val lineBreak = raw.indexOf('\n')
            if (lineBreak > 0) raw.substring(7, lineBreak) to raw.substring(lineBreak + 1)
            else raw.removePrefix("__type:") to cluster.id.value
        } else {
            "Boundary" to raw.ifBlank { cluster.id.value }
        }
    }

    private fun labelTextOf(node: Node): String =
        (node.label as? RichLabel.Plain)?.text?.takeIf { it.isNotEmpty() } ?: node.id.value

    private fun drawLegend(bounds: Rect, entries: List<C4LegendEntry>, out: MutableList<DrawCommand>) {
        if (entries.isEmpty()) return
        out += legendCommands(bounds, entries)
    }

    private fun legendCommands(bounds: Rect, entries: List<C4LegendEntry>): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val rect = legendRect(bounds, entries)
        out += DrawCommand.FillRect(rect = rect, color = colors.edgeLabelBackground, corner = 10f, z = 20)
        out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = 1f), color = colors.clusterStroke, corner = 10f, z = 21)
        out += DrawCommand.DrawText(
            text = "Legend",
            origin = Point(rect.left + 12f, rect.top + 12f),
            font = clusterFont,
            color = colors.edgeLabelText,
            maxWidth = rect.size.width - 24f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 22,
        )
        var y = rect.top + 42f
        for (entry in entries) {
            val chipRect = Rect.ltrb(rect.left + 12f, y, rect.left + 44f, y + 18f)
            if (entry.kind == "relationship") {
                val midY = (chipRect.top + chipRect.bottom) / 2f
                out += DrawCommand.StrokePath(
                    path = PathCmd(listOf(PathOp.MoveTo(Point(chipRect.left, midY)), PathOp.LineTo(Point(chipRect.right, midY)))),
                    stroke = Stroke(width = 2f),
                    color = entry.stroke?.let { Color(it.argb) } ?: colors.edge,
                    z = 22,
                )
            } else {
                out += DrawCommand.FillRect(rect = chipRect, color = entry.fill?.let { Color(it.argb) } ?: colors.nodeFill, corner = 4f, z = 22)
                out += DrawCommand.StrokeRect(rect = chipRect, stroke = Stroke(width = 1f), color = entry.stroke?.let { Color(it.argb) } ?: colors.nodeStroke, corner = 4f, z = 23)
            }
            out += DrawCommand.DrawText(
                text = entry.text,
                origin = Point(chipRect.right + 10f, chipRect.top + 9f),
                font = edgeLabelFont,
                color = entry.textColor?.let { Color(it.argb) } ?: colors.edgeLabelText,
                maxWidth = rect.right - chipRect.right - 22f,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Middle,
                z = 24,
            )
            y += 24f
        }
        return out
    }

    private fun legendRect(bounds: Rect, entries: List<C4LegendEntry>): Rect {
        val itemWidths = entries.map { textMeasurer.measure(it.text, edgeLabelFont).width }
        val width = maxOf(164f, (itemWidths.maxOrNull() ?: 72f) + 72f)
        val height = 52f + entries.size * 24f
        return Rect.ltrb(bounds.left + 16f, bounds.bottom + 16f, bounds.left + 16f + width, bounds.bottom + 16f + height)
    }

    private fun arrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return DrawCommand.FillPath(
            path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.Close)),
            color = color,
            z = 4,
        )
        val ux = dx / len
        val uy = dy / len
        val size = 8f
        val baseX = to.x - ux * size
        val baseY = to.y - uy * size
        val nx = -uy
        val ny = ux
        val p1 = Point(baseX + nx * size * 0.5f, baseY + ny * size * 0.5f)
        val p2 = Point(baseX - nx * size * 0.5f, baseY - ny * size * 0.5f)
        return DrawCommand.FillPath(
            path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(p1), PathOp.LineTo(p2), PathOp.Close)),
            color = color,
            z = 4,
        )
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = graphPipeline.drawEntities()

}

private data class C4SemanticColors(
    val fill: Color,
    val stroke: Color,
    val text: Color,
)

private fun Color.withAlpha(alpha: Float): Color {
    val clamped = alpha.coerceIn(0f, 1f)
    val a = (clamped * 255f).toInt().coerceIn(0, 255)
    return Color((argb and 0x00FFFFFF) or (a shl 24))
}

private fun Color.isDark(): Boolean {
    val r = (argb shr 16) and 0xFF
    val g = (argb shr 8) and 0xFF
    val b = argb and 0xFF
    val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return luminance < 140f
}
