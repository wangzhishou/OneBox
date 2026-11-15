package com.hrm.diagram.render.streaming.plantuml

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
import com.hrm.diagram.core.ir.ArrowEnds
import com.hrm.diagram.core.ir.Cluster
import com.hrm.diagram.core.ir.Edge
import com.hrm.diagram.core.ir.EdgeKind
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.EdgeRoute
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.parser.plantuml.PlantUmlC4Parser
import com.hrm.diagram.render.graph.GraphMeasurePolicy
import com.hrm.diagram.render.graph.GraphIrRenderer
import com.hrm.diagram.render.graph.GraphRenderStyle
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.kernel.GraphPipelineProfile
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.min
import kotlin.math.sqrt

internal class PlantUmlC4SubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private val parser = PlantUmlC4Parser()
    private val colors = ThemeResolver.resolvePlantUmlComponent(theme)
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f)
    private val clusterFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val edgeFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val stereoFont = FontSpec(family = "sans-serif", sizeSp = 10f, weight = 600)
    private val renderer = GraphIrRenderer(
        textMeasurer,
        GraphRenderStyle(
            prefix = "plantuml",
            nodeFont = labelFont,
            edgeFont = edgeFont,
            clusterFont = clusterFont,
            nodeFill = colors.nodeFill,
            nodeStroke = colors.nodeStroke,
            nodeText = colors.nodeText,
            edgeColor = colors.edgeColor,
            edgeLabelText = colors.edgeLabelText,
            edgeLabelBg = colors.clusterChipFill,
            clusterFill = colors.clusterFill,
            clusterStroke = colors.clusterStroke,
            clusterText = colors.clusterText,
            graphBackground = { _, _ -> colors.canvas },
            customNodeCommands = { node, rect -> nodeCommands(node, rect, parser.nodeLinkSnapshot()[node.id]) },
            customClusterCommands = { cluster, rect -> clusterCommands(cluster, rect, parser.nodeLinkSnapshot()) },
            customEdgeCommands = ::edgeCommands,
        ),
    )
    private val measurePolicy = GraphMeasurePolicy(
        textMeasurer = textMeasurer,
        defaultSize = Size(188f, 96f),
        maxWidth = 220f,
        minWidth = 150f,
        minHeight = 78f,
        labelOf = ::labelTextOf,
        fontOf = { labelFont },
        customSizeOf = ::measureNodeSize,
    )
    private val profile = GraphPipelineProfile(
        sourceLanguage = SourceLanguage.PLANTUML,
        defaultNodeSize = Size(188f, 96f),
        measurePolicy = measurePolicy,
        postLayout = { ir, base ->
            val clusterRects = LinkedHashMap<NodeId, Rect>()
            for (cluster in ir.clusters) computeClusterRect(cluster, base.nodePositions, clusterRects)
            val clusterTitleRects = clusterTitleRects(clusterRects.values)
            val edgeLabelRects = computeEdgeLabelRects(ir, base.edgeRoutes, base.nodePositions.values, clusterTitleRects)
            base.copy(
                clusterRects = clusterRects,
                bounds = computeBounds(base.nodePositions.values + clusterRects.values + edgeLabelRects.values),
            )
        },
    )
    private val kernel = profile.kernel(
        textMeasurer = textMeasurer,
        renderEntities = { ir, laid -> renderer.render(ir, laid) },
    )

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState {
        val ir = parser.snapshot()
        return kernel.advanceRendered(
            previousSnapshot = previousSnapshot,
            seq = seq,
            isFinal = isFinal,
            ir = ir,
            diagnostics = parser.diagnosticsSnapshot(),
        )
    }

    override fun dispose() {
        kernel.clear()
    }

    private fun measureNodeSize(node: Node): Size {
        val metrics = textMeasurer.measure(labelTextOf(node), labelFont, maxWidth = 220f)
        val extraWidth = if (node.shape is NodeShape.Component) 18f else 0f
        return Size(
            width = (metrics.width + 32f + extraWidth).coerceAtLeast(150f),
            height = (metrics.height + 26f).coerceAtLeast(78f),
        )
    }

    private fun computeClusterRect(cluster: Cluster, nodePositions: Map<NodeId, Rect>, out: MutableMap<NodeId, Rect>): Rect? {
        val childRects = cluster.children.mapNotNull { nodePositions[it] }.toMutableList()
        childRects += cluster.nestedClusters.mapNotNull { computeClusterRect(it, nodePositions, out) }
        if (childRects.isEmpty()) return null
        val title = clusterTitle(cluster)
        val titleMetrics = textMeasurer.measure(title, clusterFont, maxWidth = 220f)
        val rect = Rect.ltrb(
            childRects.minOf { it.left } - 20f,
            childRects.minOf { it.top } - titleMetrics.height - 28f,
            childRects.maxOf { it.right } + 20f,
            childRects.maxOf { it.bottom } + 20f,
        )
        out[cluster.id] = rect
        return rect
    }

    private fun computeBounds(rects: Collection<Rect>): Rect {
        if (rects.isEmpty()) return Rect.ltrb(0f, 0f, 420f, 260f)
        return Rect.ltrb(
            rects.minOf { it.left }.coerceAtMost(0f),
            rects.minOf { it.top }.coerceAtMost(0f),
            rects.maxOf { it.right } + 24f,
            rects.maxOf { it.bottom } + 24f,
        )
    }

    private fun render(ir: GraphIR, laid: LaidOutDiagram): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val clusterTitleRects = clusterTitleRects(laid.clusterRects.values)
        val edgeLabelRects = computeEdgeLabelRects(ir, laid.edgeRoutes, laid.nodePositions.values, clusterTitleRects)
        out += DrawCommand.FillRect(laid.bounds, colors.canvas, z = -1)
        val nodeLinks = parser.nodeLinkSnapshot()
        val edgeLinks = parser.edgeLinkSnapshot()
        val edgePresentation = parser.edgePresentationSnapshot()
        for (cluster in ir.clusters) drawCluster(cluster, laid.clusterRects, nodeLinks, out)
        for ((idx, route) in laid.edgeRoutes.withIndex()) {
            drawEdge(ir.edges.getOrNull(idx), presentation = edgePresentation[idx], link = edgeLinks[idx], route = route, labelRect = edgeLabelRects[idx], out = out)
        }
        for (node in ir.nodes) drawNode(node, laid.nodePositions[node.id] ?: continue, nodeLinks[node.id], out)
        return out
    }

    private fun clusterTitleRects(clusterRects: Collection<Rect>): List<Rect> =
        clusterRects.map { rect ->
            Rect.ltrb(
                left = rect.left + 8f,
                top = rect.top + 8f,
                right = min(rect.right - 8f, rect.left + 198f),
                bottom = rect.top + 44f,
            )
        }

    private fun computeEdgeLabelRects(
        ir: GraphIR,
        routes: List<EdgeRoute>,
        nodeRects: Collection<Rect>,
        clusterTitleRects: Collection<Rect>,
    ): Map<Int, Rect> {
        val out = LinkedHashMap<Int, Rect>()
        val occupied = ArrayList<Rect>()
        val edgePresentation = parser.edgePresentationSnapshot()
        for ((index, route) in routes.withIndex()) {
            val edge = ir.edges.getOrNull(index) ?: continue
            val rect = edgeLabelRect(
                edge = edge,
                route = route,
                presentation = edgePresentation[index],
                nodeRects = nodeRects,
                clusterTitleRects = clusterTitleRects,
                occupiedLabelRects = occupied,
            ) ?: continue
            out[index] = rect
            occupied += rect
        }
        return out
    }

    private fun edgeLabelRect(
        edge: Edge,
        route: EdgeRoute,
        presentation: PlantUmlC4Parser.C4EdgePresentation?,
        nodeRects: Collection<Rect>,
        clusterTitleRects: Collection<Rect>,
        occupiedLabelRects: Collection<Rect>,
    ): Rect? {
        if (edge.kind == EdgeKind.Invisible) return null
        val label = edge.label?.let(::labelTextOf).orEmpty()
        if (label.isBlank()) return null
        val metrics = textMeasurer.measure(label, edgeFont, maxWidth = EDGE_LABEL_MAX_WIDTH)
        val width = (metrics.width + EDGE_LABEL_PADDING_X * 2f).coerceIn(EDGE_LABEL_MIN_WIDTH, EDGE_LABEL_MAX_WIDTH + EDGE_LABEL_PADDING_X * 2f)
        val height = metrics.height + EDGE_LABEL_PADDING_Y * 2f
        val anchor = routeLabelPoint(route, presentation)
        val obstacles = nodeRects.map { inflate(it, EDGE_LABEL_NODE_CLEARANCE) } +
            clusterTitleRects.map { inflate(it, EDGE_LABEL_CLUSTER_TITLE_CLEARANCE) } +
            occupiedLabelRects.map { inflate(it, EDGE_LABEL_LABEL_CLEARANCE) }
        return edgeLabelCandidates(route, anchor, width, height)
            .minWithOrNull(
                compareBy<Rect> { rect -> obstacles.count { overlaps(rect, it) } }
                    .thenBy { rect -> distanceSquared(centerOf(rect), anchor) },
            )
    }

    private fun routeLabelPoint(route: EdgeRoute, presentation: PlantUmlC4Parser.C4EdgePresentation?): Point {
        val pts = route.points
        if (pts.size < 2) return Point(0f, 0f)
        val base = if (route.kind == RouteKind.Bezier && pts.size >= 4) {
            cubicPoint(pts[0], pts[1], pts[2], pts[3], t = 0.5f)
        } else {
            val left = pts[(pts.size / 2 - 1).coerceAtLeast(0)]
            val right = pts[(pts.size / 2).coerceAtMost(pts.lastIndex)]
            Point((left.x + right.x) / 2f, (left.y + right.y) / 2f)
        }
        return Point(base.x + (presentation?.offsetX ?: 0f), base.y + (presentation?.offsetY ?: 0f))
    }

    private fun edgeLabelCandidates(route: EdgeRoute, anchor: Point, width: Float, height: Float): List<Rect> {
        val candidates = ArrayList<Rect>()
        candidates += centeredRect(anchor, width, height)
        val pts = route.points
        if (route.kind == RouteKind.Bezier && pts.size >= 4) {
            val samples = listOf(0.5f, 0.35f, 0.65f, 0.25f, 0.75f)
            for (t in samples) {
                val p = cubicPoint(pts[0], pts[1], pts[2], pts[3], t)
                val tangent = cubicTangent(pts[0], pts[1], pts[2], pts[3], t)
                val len = sqrt(tangent.x * tangent.x + tangent.y * tangent.y).takeIf { it > 0.001f } ?: 1f
                val nx = -tangent.y / len
                val ny = tangent.x / len
                for (offset in EDGE_LABEL_OFFSETS) {
                    candidates += centeredRect(Point(p.x + nx * offset, p.y + ny * offset), width, height)
                }
            }
        } else {
            val left = pts[(pts.size / 2 - 1).coerceAtLeast(0)]
            val right = pts[(pts.size / 2).coerceAtMost(pts.lastIndex)]
            val dx = right.x - left.x
            val dy = right.y - left.y
            val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.001f } ?: 1f
            val nx = -dy / len
            val ny = dx / len
            for (offset in EDGE_LABEL_OFFSETS) {
                candidates += centeredRect(Point(anchor.x + nx * offset, anchor.y + ny * offset), width, height)
            }
        }
        for ((dx, dy) in EDGE_LABEL_FALLBACK_OFFSETS) {
            candidates += centeredRect(Point(anchor.x + dx, anchor.y + dy), width, height)
        }
        return candidates
    }

    private fun drawCluster(cluster: Cluster, rects: Map<NodeId, Rect>, links: Map<NodeId, String>, out: MutableList<DrawCommand>) {
        val rect = rects[cluster.id] ?: return
        out += clusterCommands(cluster, rect, links)
    }

    private fun clusterCommands(cluster: Cluster, rect: Rect, links: Map<NodeId, String>): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val strokeColor = cluster.style.stroke?.let { Color(it.argb) } ?: colors.clusterStroke
        out += DrawCommand.FillRect(rect, cluster.style.fill?.let { Color(it.argb) } ?: colors.clusterFill, corner = 14f, z = 0)
        out += DrawCommand.StrokeRect(rect, Stroke(width = cluster.style.strokeWidth ?: 1.4f, dash = listOf(8f, 5f)), strokeColor, corner = 14f, z = 1)
        val chip = Rect.ltrb(rect.left + 10f, rect.top + 10f, min(rect.right - 10f, rect.left + 190f), rect.top + 42f)
        out += DrawCommand.FillRect(chip, colors.clusterChipFill, corner = 10f, z = 2)
        out += DrawCommand.StrokeRect(chip, Stroke(width = 1f), strokeColor, corner = 10f, z = 3)
        out += DrawCommand.DrawText(clusterTitle(cluster), Point(chip.left + 10f, chip.top + 10f), clusterFont, colors.clusterText, maxWidth = chip.size.width - 20f, anchorY = TextAnchorY.Top, z = 4)
        links[cluster.id]?.let { href ->
            out += DrawCommand.Hyperlink(href, rect, z = 9)
            drawLinkBadge(rect, strokeColor, out)
        }
        return out
    }

    private fun drawNode(node: Node, rect: Rect, link: String?, out: MutableList<DrawCommand>) {
        out += nodeCommands(node, rect, link)
    }

    private fun nodeCommands(node: Node, rect: Rect, link: String?): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val fill = node.style.fill?.let { Color(it.argb) } ?: colors.nodeFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: colors.nodeStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: colors.nodeText
        val shape = node.shape
        when (shape) {
            is NodeShape.Actor, is NodeShape.Stadium -> drawActorNode(rect, fill, strokeColor, out)
            is NodeShape.Cylinder -> drawCylinderNode(rect, fill, strokeColor, out)
            is NodeShape.Hexagon -> drawHexagonNode(rect, fill, strokeColor, out)
            is NodeShape.Component -> drawComponentNode(rect, fill, strokeColor, out)
            is NodeShape.Package -> drawDeploymentNode(rect, fill, strokeColor, out)
            is NodeShape.Custom -> if (shape.name == "octagon") {
                drawOctagonNode(rect, fill, strokeColor, out)
            } else {
                out += DrawCommand.FillRect(rect, fill, corner = 12f, z = 4)
                out += DrawCommand.StrokeRect(rect, Stroke(width = node.style.strokeWidth ?: 1.4f), strokeColor, corner = 12f, z = 5)
            }
            else -> {
                out += DrawCommand.FillRect(rect, fill, corner = 12f, z = 4)
                out += DrawCommand.StrokeRect(rect, Stroke(width = node.style.strokeWidth ?: 1.4f), strokeColor, corner = 12f, z = 5)
            }
        }
        if (!link.isNullOrBlank() || node.payload[PlantUmlC4Parser.LINK_KEY]?.isNotBlank() == true) {
            drawLinkBadge(rect, strokeColor, out)
            out += DrawCommand.Hyperlink(link ?: node.payload[PlantUmlC4Parser.LINK_KEY].orEmpty(), rect, z = 10)
        }
        val lines = labelTextOf(node).lines()
        val stereo = lines.firstOrNull().orEmpty()
        val body = lines.drop(1).joinToString("\n")
        out += DrawCommand.DrawText(stereo, Point(rect.left + 14f, rect.top + 10f), stereoFont, strokeColor, maxWidth = rect.size.width - 28f, anchorY = TextAnchorY.Top, z = 6)
        out += DrawCommand.DrawText(body, Point(rect.left + 14f, rect.top + 30f), labelFont, textColor, maxWidth = rect.size.width - 28f, anchorY = TextAnchorY.Top, z = 6)
        return out
    }

    private fun drawDeploymentNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect, fill, corner = 8f, z = 4)
        out += DrawCommand.StrokeRect(rect, Stroke(width = 1.4f), strokeColor, corner = 8f, z = 5)
        val tab = Rect.ltrb(rect.left + 10f, rect.top - 8f, rect.left + 72f, rect.top + 12f)
        out += DrawCommand.FillRect(tab, fill, corner = 5f, z = 4)
        out += DrawCommand.StrokeRect(tab, Stroke(width = 1.2f), strokeColor, corner = 5f, z = 5)
    }

    private fun drawLinkBadge(rect: Rect, strokeColor: Color, out: MutableList<DrawCommand>) {
        val badge = Rect.ltrb(rect.right - 28f, rect.bottom - 24f, rect.right - 8f, rect.bottom - 6f)
        out += DrawCommand.FillRect(badge, colors.clusterChipFill, corner = 9f, z = 7)
        out += DrawCommand.StrokeRect(badge, Stroke(width = 1f), strokeColor, corner = 9f, z = 8)
        out += DrawCommand.DrawText("L", Point((badge.left + badge.right) / 2f, (badge.top + badge.bottom) / 2f), stereoFont, strokeColor, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Middle, z = 9)
    }

    private fun drawActorNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect, fill, corner = 16f, z = 4)
        out += DrawCommand.StrokeRect(rect, Stroke(width = 1.4f), strokeColor, corner = 16f, z = 5)
    }

    private fun drawHexagonNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        val inset = min(rect.size.width, rect.size.height) * 0.18f
        val path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(rect.left + inset, rect.top)),
                PathOp.LineTo(Point(rect.right - inset, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top + rect.size.height / 2f)),
                PathOp.LineTo(Point(rect.right - inset, rect.bottom)),
                PathOp.LineTo(Point(rect.left + inset, rect.bottom)),
                PathOp.LineTo(Point(rect.left, rect.top + rect.size.height / 2f)),
                PathOp.Close,
            ),
        )
        out += DrawCommand.FillPath(path, fill, z = 4)
        out += DrawCommand.StrokePath(path, Stroke(width = 1.4f), strokeColor, z = 5)
    }

    private fun drawOctagonNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        val inset = min(rect.size.width, rect.size.height) * 0.18f
        val path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(rect.left + inset, rect.top)),
                PathOp.LineTo(Point(rect.right - inset, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top + inset)),
                PathOp.LineTo(Point(rect.right, rect.bottom - inset)),
                PathOp.LineTo(Point(rect.right - inset, rect.bottom)),
                PathOp.LineTo(Point(rect.left + inset, rect.bottom)),
                PathOp.LineTo(Point(rect.left, rect.bottom - inset)),
                PathOp.LineTo(Point(rect.left, rect.top + inset)),
                PathOp.Close,
            ),
        )
        out += DrawCommand.FillPath(path, fill, z = 4)
        out += DrawCommand.StrokePath(path, Stroke(width = 1.4f), strokeColor, z = 5)
    }

    private fun drawCylinderNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect, fill, corner = 10f, z = 4)
        out += DrawCommand.StrokeRect(rect, Stroke(width = 1.4f), strokeColor, corner = 10f, z = 5)
        out += DrawCommand.StrokeRect(Rect(rect.origin, Size(rect.size.width, 18f)), Stroke(width = 1f), strokeColor, corner = 9f, z = 6)
    }

    private fun drawComponentNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect, fill, corner = 10f, z = 4)
        out += DrawCommand.StrokeRect(rect, Stroke(width = 1.4f), strokeColor, corner = 10f, z = 5)
        val tab = Rect.ltrb(rect.right - 30f, rect.top + 12f, rect.right - 10f, rect.top + 30f)
        out += DrawCommand.StrokeRect(tab, Stroke(width = 1f), strokeColor, corner = 3f, z = 6)
    }

    private fun drawEdge(
        edge: Edge?,
        presentation: PlantUmlC4Parser.C4EdgePresentation?,
        link: String?,
        route: EdgeRoute,
        labelRect: Rect?,
        out: MutableList<DrawCommand>,
    ) {
        if (edge?.kind == EdgeKind.Invisible) return
        val pts = route.points
        if (pts.size < 2) return
        val ops = ArrayList<PathOp>()
        ops += PathOp.MoveTo(pts.first())
        when (route.kind) {
            RouteKind.Bezier -> {
                var i = 1
                while (i + 2 < pts.size) {
                    ops += PathOp.CubicTo(pts[i], pts[i + 1], pts[i + 2])
                    i += 3
                }
                if (i < pts.size) ops += PathOp.LineTo(pts.last())
            }
            else -> for (i in 1 until pts.size) ops += PathOp.LineTo(pts[i])
        }
        val color = edge?.style?.color?.let { Color(it.argb) } ?: colors.edgeColor
        out += DrawCommand.StrokePath(PathCmd(ops), Stroke(width = edge?.style?.width ?: 1.5f, dash = edge?.style?.dash), color, z = 2)
        if (edge?.arrow == ArrowEnds.ToOnly || edge?.arrow == ArrowEnds.Both) out += openArrowHead(pts[pts.size - 2], pts.last(), color)
        if (edge?.arrow == ArrowEnds.Both) out += openArrowHead(pts[1], pts.first(), color)
        val label = edge?.label?.let(::labelTextOf).orEmpty()
        if (label.isNotBlank() && labelRect != null) {
            val origin = Point((labelRect.left + labelRect.right) / 2f, (labelRect.top + labelRect.bottom) / 2f)
            out += DrawCommand.FillRect(labelRect, colors.clusterChipFill, corner = 4f, z = 3)
            val labelColor = presentation?.textColor?.let { Color(it.argb) } ?: colors.edgeLabelText
            out += DrawCommand.DrawText(
                label,
                origin,
                edgeFont,
                labelColor,
                maxWidth = labelRect.size.width - EDGE_LABEL_PADDING_X * 2f,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 4,
            )
            link?.let {
                out += DrawCommand.Hyperlink(it, labelRect, z = 10)
                out += DrawCommand.DrawText("L", Point(labelRect.right - 8f, labelRect.top + 8f), stereoFont, color, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Middle, z = 5)
            }
        }
    }

    private fun edgeCommands(
        edge: Edge,
        index: Int,
        points: List<Point>,
        kind: RouteKind,
        laid: LaidOutDiagram,
    ): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val ir = laid.source as? GraphIR ?: return out
        val route = EdgeRoute(edge.from, edge.to, points, kind)
        val labelRect = computeEdgeLabelRects(
            ir = ir,
            routes = laid.edgeRoutes,
            nodeRects = laid.nodePositions.values,
            clusterTitleRects = clusterTitleRects(laid.clusterRects.values),
        )[index]
        drawEdge(
            edge = edge,
            presentation = parser.edgePresentationSnapshot()[index],
            link = parser.edgeLinkSnapshot()[index],
            route = route,
            labelRect = labelRect,
            out = out,
        )
        return out
    }

    private fun openArrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.001f } ?: 1f
        val ux = dx / len
        val uy = dy / len
        val size = 8f
        val bx = to.x - ux * size
        val by = to.y - uy * size
        val nx = -uy
        val ny = ux
        return DrawCommand.StrokePath(
            PathCmd(listOf(PathOp.MoveTo(Point(bx + nx * size * 0.5f, by + ny * size * 0.5f)), PathOp.LineTo(to), PathOp.LineTo(Point(bx - nx * size * 0.5f, by - ny * size * 0.5f)))),
            Stroke(width = 1.5f),
            color,
            z = 3,
        )
    }

    private fun centeredRect(center: Point, width: Float, height: Float): Rect =
        Rect.ltrb(
            left = center.x - width / 2f,
            top = center.y - height / 2f,
            right = center.x + width / 2f,
            bottom = center.y + height / 2f,
        )

    private fun inflate(rect: Rect, delta: Float): Rect =
        Rect.ltrb(
            left = rect.left - delta,
            top = rect.top - delta,
            right = rect.right + delta,
            bottom = rect.bottom + delta,
        )

    private fun overlaps(a: Rect, b: Rect): Boolean =
        a.left < b.right && a.right > b.left && a.top < b.bottom && a.bottom > b.top

    private fun centerOf(rect: Rect): Point = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f)

    private fun distanceSquared(a: Point, b: Point): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return dx * dx + dy * dy
    }

    private fun cubicPoint(p0: Point, p1: Point, p2: Point, p3: Point, t: Float): Point {
        val u = 1f - t
        val tt = t * t
        val uu = u * u
        val uuu = uu * u
        val ttt = tt * t
        return Point(
            x = uuu * p0.x + 3f * uu * t * p1.x + 3f * u * tt * p2.x + ttt * p3.x,
            y = uuu * p0.y + 3f * uu * t * p1.y + 3f * u * tt * p2.y + ttt * p3.y,
        )
    }

    private fun cubicTangent(p0: Point, p1: Point, p2: Point, p3: Point, t: Float): Point {
        val u = 1f - t
        return Point(
            x = 3f * u * u * (p1.x - p0.x) + 6f * u * t * (p2.x - p1.x) + 3f * t * t * (p3.x - p2.x),
            y = 3f * u * u * (p1.y - p0.y) + 6f * u * t * (p2.y - p1.y) + 3f * t * t * (p3.y - p2.y),
        )
    }

    private fun labelTextOf(node: Node): String = labelTextOf(node.label).ifBlank { node.id.value }

    private fun labelTextOf(label: RichLabel): String = when (label) {
        is RichLabel.Plain -> label.text
        is RichLabel.Markdown -> label.source
        is RichLabel.Html -> label.html
    }

    private fun clusterTitle(cluster: Cluster): String {
        val text = (cluster.label as? RichLabel.Plain)?.text ?: cluster.id.value
        val lines = text.lines()
        return if (lines.size >= 2) "${lines[1]}\n[${lines[0]}]" else text
    }

    private companion object {
        const val EDGE_LABEL_MIN_WIDTH = 90f
        const val EDGE_LABEL_MAX_WIDTH = 180f
        const val EDGE_LABEL_PADDING_X = 10f
        const val EDGE_LABEL_PADDING_Y = 5f
        const val EDGE_LABEL_NODE_CLEARANCE = 8f
        const val EDGE_LABEL_CLUSTER_TITLE_CLEARANCE = 8f
        const val EDGE_LABEL_LABEL_CLEARANCE = 6f
        val EDGE_LABEL_OFFSETS = listOf(-26f, 26f, -38f, 38f, -52f, 52f, -88f, 88f, -128f, 128f, -168f, 168f)
        val EDGE_LABEL_FALLBACK_OFFSETS = listOf(
            -168f to 0f,
            168f to 0f,
            -128f to 0f,
            128f to 0f,
            -90f to 0f,
            90f to 0f,
            -60f to -24f,
            60f to -24f,
            -60f to 24f,
            60f to 24f,
            0f to -42f,
            0f to 42f,
        )
    }
}
