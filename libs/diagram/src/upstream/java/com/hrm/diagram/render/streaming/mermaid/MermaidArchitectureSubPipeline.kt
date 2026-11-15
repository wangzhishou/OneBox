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
import com.hrm.diagram.parser.mermaid.MermaidArchitectureParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.graph.GraphMeasurePolicy
import com.hrm.diagram.render.graph.GraphIrRenderer
import com.hrm.diagram.render.graph.GraphRenderStyle
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.streaming.kernel.GraphPipelineProfile
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

internal class MermaidArchitectureSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidArchitectureParser()
    private val colors = ThemeResolver.resolveGraph(theme)
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f, weight = 600)
    private val groupFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val iconFallbackFont = FontSpec(family = "sans-serif", sizeSp = 10f, weight = 600)
    private val renderer = GraphIrRenderer(
        textMeasurer,
        GraphRenderStyle(
            prefix = "mermaid",
            nodeFont = labelFont,
            edgeFont = edgeLabelFont,
            clusterFont = groupFont,
            nodeFill = colors.nodeFill,
            nodeStroke = colors.nodeStroke,
            nodeText = colors.nodeText,
            edgeColor = colors.edge,
            edgeLabelText = colors.edgeLabelText,
            edgeLabelBg = colors.edgeLabelBackground,
            clusterFill = colors.clusterFill,
            clusterStroke = colors.clusterStroke,
            clusterText = colors.nodeText,
            graphBackground = { _, laid -> colors.background.takeIf { laid.bounds.size.width >= 0f } },
            customNodeCommands = ::nodeCommands,
            customClusterCommands = ::clusterCommands,
            edgeEndpointAdjuster = ::adjustAnchors,
        ),
    )
    private val measurePolicy = GraphMeasurePolicy(
        textMeasurer = textMeasurer,
        defaultSize = Size(172f, 72f),
        maxWidth = 160f,
        minWidth = 128f,
        minHeight = 54f,
        labelOf = ::labelTextOf,
        fontOf = { labelFont },
        customSizeOf = ::measureNodeSize,
    )
    private val profile = GraphPipelineProfile(
        sourceLanguage = SourceLanguage.MERMAID,
        defaultNodeSize = Size(172f, 72f),
        measurePolicy = measurePolicy,
        postLayout = { ir, laid ->
            val clusterRects = LinkedHashMap<NodeId, Rect>()
            for (cluster in ir.clusters) {
                computeClusterRect(cluster, laid.nodePositions, clusterRects)
            }
            normalizeVisibleArea(laid, clusterRects, laid.seq)
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
        val kind = node.payload[MermaidArchitectureParser.KIND_KEY]
        return when (kind) {
            "junction" -> Size(18f, 18f)
            else -> {
                val metrics = textMeasurer.measure(labelTextOf(node), labelFont, maxWidth = 160f)
                val hasIcon = node.payload[MermaidArchitectureParser.ICON_KEY] != null
                val left = if (hasIcon) 56f else 18f
                Size(
                    width = (metrics.width + left + 18f).coerceAtLeast(128f),
                    height = (metrics.height + 26f).coerceAtLeast(54f),
                )
            }
        }
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
        val title = parseClusterLabel(cluster).second
        val titleMetrics = textMeasurer.measure(title.ifBlank { cluster.id.value }, groupFont, maxWidth = 220f)
        val left = childRects.minOf { it.left } - 20f
        val top = childRects.minOf { it.top } - (titleMetrics.height + 26f)
        val right = childRects.maxOf { it.right } + 20f
        val bottom = childRects.maxOf { it.bottom } + 18f
        val rect = Rect.ltrb(left, top, right, bottom)
        out[cluster.id] = rect
        return rect
    }

    private fun normalizeVisibleArea(
        baseLaid: LaidOutDiagram,
        clusterRects: Map<NodeId, Rect>,
        seq: Long,
    ): LaidOutDiagram {
        val allRects = baseLaid.nodePositions.values + clusterRects.values
        val minLeft = allRects.minOfOrNull { it.left } ?: 0f
        val minTop = allRects.minOfOrNull { it.top } ?: 0f
        val dx = if (minLeft < 12f) 12f - minLeft else 0f
        val dy = if (minTop < 12f) 12f - minTop else 0f
        val shiftedNodes = if (dx != 0f || dy != 0f) {
            baseLaid.nodePositions.mapValues { (_, rect) -> rect.shift(dx, dy) }
        } else {
            baseLaid.nodePositions
        }
        val shiftedClusters = if (dx != 0f || dy != 0f) {
            clusterRects.mapValues { (_, rect) -> rect.shift(dx, dy) }
        } else {
            clusterRects
        }
        val shiftedRoutes = if (dx != 0f || dy != 0f) {
            baseLaid.edgeRoutes.map { route ->
                route.copy(points = route.points.map { point -> point.shift(dx, dy) })
            }
        } else {
            baseLaid.edgeRoutes
        }
        val bounds = computeBounds(shiftedNodes.values + shiftedClusters.values)
        return baseLaid.copy(
            nodePositions = shiftedNodes,
            edgeRoutes = shiftedRoutes,
            clusterRects = shiftedClusters,
            bounds = bounds,
            seq = seq,
        )
    }

    private fun computeBounds(rects: Collection<Rect>): Rect {
        if (rects.isEmpty()) return Rect.ltrb(0f, 0f, 400f, 240f)
        val maxRight = rects.maxOf { it.right }
        val maxBottom = rects.maxOf { it.bottom }
        return Rect.ltrb(0f, 0f, maxRight + 20f, maxBottom + 20f)
    }

    private fun Rect.shift(dx: Float, dy: Float): Rect =
        Rect(Point(left + dx, top + dy), size)

    private fun Point.shift(dx: Float, dy: Float): Point =
        Point(x + dx, y + dy)

    private fun render(ir: GraphIR, laidOut: LaidOutDiagram): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val bounds = laidOut.bounds
        val bg = colors.background
        out += DrawCommand.FillRect(Rect(Point(bounds.left, bounds.top), Size(bounds.size.width, bounds.size.height)), bg, z = 0)

        for (cluster in ir.clusters) drawCluster(cluster, laidOut.clusterRects, out)

        for (node in ir.nodes) drawNode(node, laidOut, out)

        for ((index, route) in laidOut.edgeRoutes.withIndex()) {
            val edge = ir.edges.getOrNull(index) ?: continue
            drawEdge(edge, route, laidOut, out)
        }
        return out
    }

    private fun drawCluster(cluster: Cluster, clusterRects: Map<NodeId, Rect>, out: MutableList<DrawCommand>) {
        val rect = clusterRects[cluster.id] ?: return
        out += clusterCommands(cluster, rect)
        for (nested in cluster.nestedClusters) drawCluster(nested, clusterRects, out)
    }

    private fun clusterCommands(cluster: Cluster, rect: Rect): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val fill = cluster.style.fill?.let { Color(it.argb) } ?: colors.clusterFill
        val strokeColor = cluster.style.stroke?.let { Color(it.argb) } ?: colors.clusterStroke
        val stroke = Stroke(width = cluster.style.strokeWidth ?: 1.5f, dash = listOf(7f, 5f))
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 14f, z = 0)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 14f, z = 1)

        val (icon, title) = parseClusterLabel(cluster)
        val chipHeight = 24f
        val chipRect = Rect.ltrb(rect.left + 12f, rect.top + 10f, rect.left + 12f + 120f.coerceAtMost(rect.size.width - 24f), rect.top + 10f + chipHeight)
        out += DrawCommand.FillRect(rect = chipRect, color = colors.edgeLabelBackground, corner = 12f, z = 2)
        out += DrawCommand.StrokeRect(rect = chipRect, stroke = Stroke(width = 1f), color = strokeColor, corner = 12f, z = 3)
        if (icon.isNotBlank()) {
            val iconRect = Rect.ltrb(chipRect.left + 6f, chipRect.top + 4f, chipRect.left + 22f, chipRect.bottom - 4f)
            out += DrawCommand.DrawIcon(name = icon, rect = iconRect, z = 4)
            out += DrawCommand.DrawText(
                text = iconFallbackLabel(icon),
                origin = Point((iconRect.left + iconRect.right) / 2f, (iconRect.top + iconRect.bottom) / 2f),
                font = iconFallbackFont,
                color = strokeColor,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 5,
            )
            out += DrawCommand.DrawText(
                text = title.ifBlank { cluster.id.value },
                origin = Point(iconRect.right + 6f, (chipRect.top + chipRect.bottom) / 2f),
                font = groupFont,
                color = strokeColor,
                maxWidth = chipRect.right - iconRect.right - 12f,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Middle,
                z = 5,
            )
        } else {
            out += DrawCommand.DrawText(
                text = title.ifBlank { cluster.id.value },
                origin = Point(chipRect.left + 10f, (chipRect.top + chipRect.bottom) / 2f),
                font = groupFont,
                color = strokeColor,
                maxWidth = chipRect.size.width - 20f,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Middle,
                z = 5,
            )
        }
        return out
    }

    private fun drawNode(node: Node, laidOut: LaidOutDiagram, out: MutableList<DrawCommand>) {
        val rect = laidOut.nodePositions[node.id] ?: return
        out += nodeCommands(node, rect)
    }

    private fun nodeCommands(node: Node, rect: Rect): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val fill = node.style.fill?.let { Color(it.argb) } ?: colors.nodeFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: colors.nodeStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: colors.nodeText
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.5f)
        val kind = node.payload[MermaidArchitectureParser.KIND_KEY]
        if (kind == "junction") {
            out += DrawCommand.FillRect(rect = rect, color = fill, corner = rect.size.width / 2f, z = 4)
            out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = rect.size.width / 2f, z = 5)
            return out
        }

        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 12f, z = 4)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 12f, z = 5)
        val iconName = node.payload[MermaidArchitectureParser.ICON_KEY]
        val label = labelTextOf(node)
        if (!iconName.isNullOrBlank()) {
            val iconRect = Rect.ltrb(rect.left + 10f, rect.top + 10f, rect.left + 34f, rect.top + 34f)
            out += DrawCommand.DrawIcon(name = iconName, rect = iconRect, z = 6)
            out += DrawCommand.DrawText(
                text = iconFallbackLabel(iconName),
                origin = Point((iconRect.left + iconRect.right) / 2f, (iconRect.top + iconRect.bottom) / 2f),
                font = iconFallbackFont,
                color = strokeColor,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 7,
            )
            out += DrawCommand.DrawText(
                text = label,
                origin = Point(iconRect.right + 10f, (rect.top + rect.bottom) / 2f),
                font = labelFont,
                color = textColor,
                maxWidth = rect.right - iconRect.right - 18f,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Middle,
                z = 7,
            )
        } else {
            out += DrawCommand.DrawText(
                text = label,
                origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                font = labelFont,
                color = textColor,
                maxWidth = rect.size.width - 16f,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 7,
            )
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

    private fun drawEdge(
        edge: com.hrm.diagram.core.ir.Edge,
        route: com.hrm.diagram.layout.EdgeRoute,
        laidOut: LaidOutDiagram,
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
        out += DrawCommand.StrokePath(path = PathCmd(ops), stroke = stroke, color = edgeColor, z = 2)
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
            val metrics = textMeasurer.measure(text, edgeLabelFont)
            val bgRect = Rect.ltrb(
                midPoint.x - metrics.width / 2f - 4f,
                midPoint.y - metrics.height / 2f - 2f,
                midPoint.x + metrics.width / 2f + 4f,
                midPoint.y + metrics.height / 2f + 2f,
            )
            out += DrawCommand.FillRect(rect = bgRect, color = edge.style.labelBg?.let { Color(it.argb) } ?: colors.edgeLabelBackground, corner = 3f, z = 8)
            out += DrawCommand.DrawText(
                text = text,
                origin = midPoint,
                font = edgeLabelFont,
                color = colors.edgeLabelText,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 9,
            )
        }
    }

    private fun anchorFor(nodeId: NodeId, portId: PortId?, laidOut: LaidOutDiagram): Point? {
        val token = portId?.value ?: return null
        return when {
            token.startsWith("GROUP@") -> {
                val rest = token.removePrefix("GROUP@")
                val sep = rest.lastIndexOf('@')
                if (sep <= 0) return null
                val groupId = NodeId(rest.substring(0, sep))
                val side = parseSideToken(rest.substring(sep + 1)) ?: return null
                pointOnRect(laidOut.clusterRects[groupId] ?: return null, side)
            }
            token.startsWith("NODE@") -> {
                val side = parseSideToken(token.removePrefix("NODE@")) ?: return null
                pointOnRect(laidOut.nodePositions[nodeId] ?: return null, side)
            }
            else -> null
        }
    }

    private fun parseSideToken(raw: String): PortSide? = when (raw) {
        "T" -> PortSide.TOP
        "R" -> PortSide.RIGHT
        "B" -> PortSide.BOTTOM
        "L" -> PortSide.LEFT
        else -> null
    }

    private fun pointOnRect(rect: Rect, side: PortSide): Point = when (side) {
        PortSide.TOP -> Point((rect.left + rect.right) / 2f, rect.top)
        PortSide.RIGHT -> Point(rect.right, (rect.top + rect.bottom) / 2f)
        PortSide.BOTTOM -> Point((rect.left + rect.right) / 2f, rect.bottom)
        PortSide.LEFT -> Point(rect.left, (rect.top + rect.bottom) / 2f)
    }

    private fun parseClusterLabel(cluster: Cluster): Pair<String, String> {
        val raw = (cluster.label as? RichLabel.Plain)?.text.orEmpty()
        return if (raw.startsWith("__icon:")) {
            val lineBreak = raw.indexOf('\n')
            if (lineBreak > 0) raw.substring(7, lineBreak) to raw.substring(lineBreak + 1)
            else raw.removePrefix("__icon:") to cluster.id.value
        } else {
            "" to raw.ifBlank { cluster.id.value }
        }
    }

    private fun labelTextOf(node: Node): String =
        (node.label as? RichLabel.Plain)?.text?.takeIf { it.isNotEmpty() } ?: node.id.value

    private fun iconFallbackLabel(iconName: String): String {
        val lastToken = iconName.split(':', '-', '_').map { it.trim() }.filter { it.isNotEmpty() }.lastOrNull().orEmpty()
        return when {
            lastToken.isEmpty() -> "I"
            lastToken.length == 1 -> lastToken.uppercase()
            else -> lastToken.take(2).uppercase()
        }
    }

    private fun arrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return DrawCommand.FillPath(
            path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.Close)),
            color = color,
            z = 3,
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
        val path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(p1), PathOp.LineTo(p2), PathOp.Close))
        return DrawCommand.FillPath(path = path, color = color, z = 3)
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = graphPipeline.drawEntities()

}
