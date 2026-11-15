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
import com.hrm.diagram.core.ir.Cluster
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.EdgeRoute
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.parser.plantuml.PlantUmlNetworkParser
import com.hrm.diagram.render.graph.GraphIrRenderer
import com.hrm.diagram.render.graph.GraphRenderStyle
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.abs
import kotlin.math.sqrt

internal class PlantUmlNetworkSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private val parser = PlantUmlNetworkParser()
    private val colors = ThemeResolver.resolvePlantUmlComponent(theme)
    private val secondaryText = theme.colors.textSecondary
    private val nodeSizes: MutableMap<NodeId, Size> = HashMap()
    private val nodeFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val detailFont = FontSpec(family = "monospace", sizeSp = 10f)
    private val clusterFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val edgeFont = FontSpec(family = "sans-serif", sizeSp = 10f)
    private val defaultNodeSize = Size(132f, 58f)
    private val renderer = GraphIrRenderer(
        textMeasurer,
        GraphRenderStyle(
            prefix = "plantuml",
            nodeFont = nodeFont,
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
            customNodeCommands = ::nodeCommands,
            customClusterCommands = ::clusterCommands,
            customEdgeCommands = ::edgeCommands,
        ),
    )
    private val kernel = PlantUmlRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layoutModel = { it },
        beforeLayout = { ir, _, _ -> measureNodes(ir) },
        layout = { previous, ir, _, seq, isFinal ->
            layoutNetwork(ir = ir, previous = previous, seq = seq, incremental = !isFinal)
        },
        renderEntities = { ir, _, laidOut, _ -> renderer.render(ir, laidOut) },
        postLayout = { _, _, laidOut, _, _ -> laidOut },
    )

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun measureNodes(ir: GraphIR) {
        for (node in ir.nodes) {
            val text = labelTextOf(node)
            val metrics = textMeasurer.measure(text, nodeFont, maxWidth = 180f)
            nodeSizes[node.id] = Size(
                width = (metrics.width + 32f).coerceAtLeast(118f),
                height = (metrics.height + 24f).coerceAtLeast(52f),
            )
        }
    }

    private fun layoutNetwork(ir: GraphIR, previous: LaidOutDiagram?, seq: Long, incremental: Boolean): LaidOutDiagram {
        val nodeById = ir.nodes.associateBy { it.id }
        val columnNames = orderedNodeNames(ir)
        val columnWidths = columnNames.associateWith { name ->
            ir.nodes
                .filter { nodeNameOf(it) == name }
                .maxOfOrNull { nodeSizes[it.id]?.width ?: defaultNodeSize.width }
                ?: defaultNodeSize.width
        }
        val columnX = LinkedHashMap<String, Float>()
        var x = 48f
        for (name in columnNames) {
            columnX[name] = x
            x += (columnWidths[name] ?: defaultNodeSize.width) + 46f
        }
        val laneRight = (x - 46f + 48f).coerceAtLeast(360f)
        val nodePositions = LinkedHashMap<NodeId, Rect>()
        val clusterRects = LinkedHashMap<NodeId, Rect>()
        var y = 32f
        for (cluster in ir.clusters) {
            val clusterNodes = clusterNodeIds(cluster).mapNotNull { nodeById[it] }
            val contentHeight = clusterNodes.maxOfOrNull { nodeSizes[it.id]?.height ?: defaultNodeSize.height } ?: defaultNodeSize.height
            val laneHeight = (64f + contentHeight + 28f).coerceAtLeast(128f)
            val laneRect = Rect.ltrb(24f, y, laneRight, y + laneHeight)
            clusterRects[cluster.id] = laneRect
            val nodeTop = laneRect.top + 58f + ((laneHeight - 58f - 24f - contentHeight) / 2f).coerceAtLeast(0f)
            for (node in clusterNodes) {
                val size = nodeSizes[node.id] ?: defaultNodeSize
                val name = nodeNameOf(node)
                val colLeft = columnX[name] ?: 48f
                val colWidth = columnWidths[name] ?: size.width
                val fresh = Rect(Point(colLeft + (colWidth - size.width) / 2f, nodeTop), size)
                nodePositions[node.id] = if (incremental) previous?.nodePositions?.get(node.id) ?: fresh else fresh
            }
            for (nested in cluster.nestedClusters) {
                computeNestedClusterRect(nested, nodePositions, clusterRects, laneRect)
            }
            y += laneHeight + 24f
        }
        for (node in ir.nodes) {
            if (node.id in nodePositions) continue
            val size = nodeSizes[node.id] ?: defaultNodeSize
            val fresh = Rect(Point(48f, y), size)
            nodePositions[node.id] = if (incremental) previous?.nodePositions?.get(node.id) ?: fresh else fresh
            y += size.height + 24f
        }
        val edgeRoutes = ir.edges.mapNotNull { edge ->
            routeEdge(edge.from, edge.to, nodePositions)
        }
        return LaidOutDiagram(
            source = ir,
            nodePositions = nodePositions,
            edgeRoutes = edgeRoutes,
            clusterRects = clusterRects,
            bounds = computeBounds(nodePositions.values + clusterRects.values),
            seq = seq,
        )
    }

    private fun orderedNodeNames(ir: GraphIR): List<String> {
        val out = LinkedHashSet<String>()
        for (cluster in ir.clusters) {
            for (id in clusterNodeIds(cluster)) {
                ir.nodes.firstOrNull { it.id == id }?.let { out += nodeNameOf(it) }
            }
        }
        for (node in ir.nodes) out += nodeNameOf(node)
        return out.toList()
    }

    private fun clusterNodeIds(cluster: Cluster): List<NodeId> =
        buildList {
            addAll(cluster.children)
            for (nested in cluster.nestedClusters) addAll(clusterNodeIds(nested))
        }

    private fun computeNestedClusterRect(cluster: Cluster, nodePositions: Map<NodeId, Rect>, out: MutableMap<NodeId, Rect>, laneRect: Rect): Rect? {
        val childRects = cluster.children.mapNotNull { nodePositions[it] }.toMutableList()
        childRects += cluster.nestedClusters.mapNotNull { computeNestedClusterRect(it, nodePositions, out, laneRect) }
        if (childRects.isEmpty()) return null
        val rect = Rect.ltrb(
            childRects.minOf { it.left } - 16f,
            (childRects.minOf { it.top } - 34f).coerceAtLeast(laneRect.top + 44f),
            childRects.maxOf { it.right } + 16f,
            childRects.maxOf { it.bottom } + 16f,
        )
        out[cluster.id] = rect
        return rect
    }

    private fun routeEdge(from: NodeId, to: NodeId, nodePositions: Map<NodeId, Rect>): EdgeRoute? {
        val a = nodePositions[from] ?: return null
        val b = nodePositions[to] ?: return null
        val ac = centerOf(a)
        val bc = centerOf(b)
        val points = when {
            abs(ac.x - bc.x) < 1f -> verticalAnchors(a, b)
            abs(ac.y - bc.y) < 1f -> horizontalAnchors(a, b)
            else -> {
                val (start, end) = boundaryAnchors(a, b)
                val midX = (start.x + end.x) / 2f
                listOf(start, Point(midX, start.y), Point(midX, end.y), end)
            }
        }
        return EdgeRoute(from = from, to = to, points = points, kind = RouteKind.Orthogonal)
    }

    private fun horizontalAnchors(a: Rect, b: Rect): List<Point> =
        if (centerOf(a).x <= centerOf(b).x) {
            listOf(Point(a.right, centerOf(a).y), Point(b.left, centerOf(b).y))
        } else {
            listOf(Point(a.left, centerOf(a).y), Point(b.right, centerOf(b).y))
        }

    private fun verticalAnchors(a: Rect, b: Rect): List<Point> =
        if (centerOf(a).y <= centerOf(b).y) {
            listOf(Point(centerOf(a).x, a.bottom), Point(centerOf(b).x, b.top))
        } else {
            listOf(Point(centerOf(a).x, a.top), Point(centerOf(b).x, b.bottom))
        }

    private fun boundaryAnchors(a: Rect, b: Rect): Pair<Point, Point> {
        val ac = centerOf(a)
        val bc = centerOf(b)
        return if (abs(ac.x - bc.x) > abs(ac.y - bc.y)) {
            if (ac.x <= bc.x) Point(a.right, ac.y) to Point(b.left, bc.y) else Point(a.left, ac.y) to Point(b.right, bc.y)
        } else {
            if (ac.y <= bc.y) Point(ac.x, a.bottom) to Point(bc.x, b.top) else Point(ac.x, a.top) to Point(bc.x, b.bottom)
        }
    }

    private fun computeBounds(rects: Collection<Rect>): Rect {
        if (rects.isEmpty()) return Rect.ltrb(0f, 0f, 360f, 220f)
        return Rect.ltrb(
            left = rects.minOf { it.left }.coerceAtMost(0f),
            top = rects.minOf { it.top }.coerceAtMost(0f),
            right = rects.maxOf { it.right } + 24f,
            bottom = rects.maxOf { it.bottom } + 24f,
        )
    }

    private fun render(ir: GraphIR, laid: LaidOutDiagram): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        out += DrawCommand.FillRect(laid.bounds, colors.canvas, z = -1)
        for (cluster in ir.clusters) drawCluster(cluster, laid.clusterRects, out)
        for (node in ir.nodes) drawNode(node, laid.nodePositions[node.id] ?: continue, out)
        for ((idx, route) in laid.edgeRoutes.withIndex()) {
            drawEdge(ir.edges.getOrNull(idx), route, out)
        }
        return out
    }

    private fun drawCluster(cluster: Cluster, rects: Map<NodeId, Rect>, out: MutableList<DrawCommand>) {
        val rect = rects[cluster.id] ?: return
        out += clusterCommands(cluster, rect)
        for (nested in cluster.nestedClusters) drawCluster(nested, rects, out)
    }

    private fun clusterCommands(cluster: Cluster, rect: Rect): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val fill = cluster.style.fill?.let { Color(it.argb) } ?: colors.clusterFill
        val strokeColor = cluster.style.stroke?.let { Color(it.argb) } ?: colors.clusterStroke
        out += DrawCommand.FillRect(rect, fill, corner = 14f, z = 0)
        out += DrawCommand.StrokeRect(rect, Stroke(width = cluster.style.strokeWidth ?: 1.4f, dash = listOf(8f, 5f)), strokeColor, corner = 14f, z = 1)
        val chip = Rect.ltrb(rect.left + 12f, rect.top + 10f, rect.right - 12f, rect.top + 36f)
        out += DrawCommand.FillRect(chip, colors.clusterChipFill, corner = 13f, z = 2)
        out += DrawCommand.DrawText(
            text = clusterTitle(cluster),
            origin = Point(chip.left + 12f, chip.top + chip.size.height / 2f),
            font = clusterFont,
            color = colors.clusterText,
            maxWidth = chip.size.width - 24f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Middle,
            z = 3,
        )
        return out
    }

    private fun drawNode(node: Node, rect: Rect, out: MutableList<DrawCommand>) {
        out += nodeCommands(node, rect)
    }

    private fun nodeCommands(node: Node, rect: Rect): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val fill = node.style.fill?.let { Color(it.argb) } ?: colors.nodeFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: colors.nodeStroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: colors.nodeText
        when (node.shape) {
            is NodeShape.Cloud -> drawCloudNode(rect, fill, strokeColor, out)
            is NodeShape.Cylinder -> drawCylinderNode(rect, fill, strokeColor, out)
            is NodeShape.Hexagon -> drawHexagonNode(rect, fill, strokeColor, out)
            is NodeShape.Actor -> drawActorNode(rect, fill, strokeColor, out)
            is NodeShape.Component -> drawComponentNode(rect, fill, strokeColor, out)
            is NodeShape.Box -> {
                out += DrawCommand.FillRect(rect, fill, corner = 2f, z = 4)
                out += DrawCommand.StrokeRect(rect, Stroke(width = node.style.strokeWidth ?: 1.5f), strokeColor, corner = 2f, z = 5)
            }
            else -> {
                out += DrawCommand.FillRect(rect, fill, corner = 10f, z = 4)
                out += DrawCommand.StrokeRect(rect, Stroke(width = node.style.strokeWidth ?: 1.5f), strokeColor, corner = 10f, z = 5)
            }
        }
        val lines = labelTextOf(node).split('\n')
        out += DrawCommand.DrawText(
            text = lines.firstOrNull().orEmpty(),
            origin = Point(rect.left + 14f, rect.top + 10f),
            font = nodeFont,
            color = textColor,
            maxWidth = rect.size.width - 28f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 6,
        )
        val details = lines.drop(1).joinToString("\n")
        if (details.isNotBlank()) {
            out += DrawCommand.DrawText(
                text = details,
                origin = Point(rect.left + 14f, rect.top + 30f),
                font = detailFont,
                color = secondaryText,
                maxWidth = rect.size.width - 28f,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 6,
            )
        }
        return out
    }

    private fun drawCylinderNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect, fill, corner = 10f, z = 4)
        out += DrawCommand.StrokeRect(rect, Stroke(width = 1.5f), strokeColor, corner = 10f, z = 5)
        out += DrawCommand.StrokeRect(Rect(rect.origin, Size(rect.size.width, 18f)), Stroke(width = 1f), strokeColor, corner = 9f, z = 6)
    }

    private fun drawHexagonNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        val inset = minOf(rect.size.width, rect.size.height) * 0.18f
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
        out += DrawCommand.StrokePath(path, Stroke(width = 1.5f), strokeColor, z = 5)
    }

    private fun drawActorNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect, fill, corner = rect.size.height / 2f, z = 4)
        out += DrawCommand.StrokeRect(rect, Stroke(width = 1.5f), strokeColor, corner = rect.size.height / 2f, z = 5)
    }

    private fun drawComponentNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect, fill, corner = 8f, z = 4)
        out += DrawCommand.StrokeRect(rect, Stroke(width = 1.5f), strokeColor, corner = 8f, z = 5)
        val tab = Rect.ltrb(rect.right - 26f, rect.top + 10f, rect.right - 8f, rect.top + 28f)
        out += DrawCommand.StrokeRect(tab, Stroke(width = 1f), strokeColor, corner = 3f, z = 6)
    }

    private fun drawCloudNode(rect: Rect, fill: Color, strokeColor: Color, out: MutableList<DrawCommand>) {
        val path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(rect.left + rect.size.width * 0.18f, rect.bottom - rect.size.height * 0.28f)),
                PathOp.CubicTo(
                    Point(rect.left, rect.bottom - rect.size.height * 0.38f),
                    Point(rect.left + rect.size.width * 0.08f, rect.top + rect.size.height * 0.24f),
                    Point(rect.left + rect.size.width * 0.30f, rect.top + rect.size.height * 0.34f),
                ),
                PathOp.CubicTo(
                    Point(rect.left + rect.size.width * 0.36f, rect.top + rect.size.height * 0.08f),
                    Point(rect.left + rect.size.width * 0.66f, rect.top + rect.size.height * 0.08f),
                    Point(rect.left + rect.size.width * 0.72f, rect.top + rect.size.height * 0.34f),
                ),
                PathOp.CubicTo(
                    Point(rect.right, rect.top + rect.size.height * 0.28f),
                    Point(rect.right, rect.bottom - rect.size.height * 0.22f),
                    Point(rect.left + rect.size.width * 0.76f, rect.bottom - rect.size.height * 0.22f),
                ),
                PathOp.LineTo(Point(rect.left + rect.size.width * 0.18f, rect.bottom - rect.size.height * 0.28f)),
                PathOp.Close,
            ),
        )
        out += DrawCommand.FillPath(path, fill, z = 4)
        out += DrawCommand.StrokePath(path, Stroke(width = 1.5f), strokeColor, z = 5)
    }

    private fun drawEdge(edge: com.hrm.diagram.core.ir.Edge?, route: EdgeRoute, out: MutableList<DrawCommand>) {
        out += edgeCommands(edge ?: return, 0, route.points, route.kind, laid = null)
    }

    private fun edgeCommands(
        edge: com.hrm.diagram.core.ir.Edge,
        index: Int,
        points: List<Point>,
        kind: RouteKind,
        laid: LaidOutDiagram?,
    ): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val pts = points
        if (pts.size < 2) return out
        val ops = ArrayList<PathOp>(pts.size)
        ops += PathOp.MoveTo(pts.first())
        when (kind) {
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
        val color = edge.style.color?.let { Color(it.argb) } ?: colors.edgeColor
        out += DrawCommand.StrokePath(PathCmd(ops), Stroke(width = edge.style.width ?: 1.4f, dash = edge.style.dash), color, z = 2)
        when (edge.arrow) {
            com.hrm.diagram.core.ir.ArrowEnds.ToOnly -> out += openArrowHead(pts[pts.size - 2], pts.last(), color)
            com.hrm.diagram.core.ir.ArrowEnds.FromOnly -> out += openArrowHead(pts[1], pts.first(), color)
            com.hrm.diagram.core.ir.ArrowEnds.Both -> {
                out += openArrowHead(pts[pts.size - 2], pts.last(), color)
                out += openArrowHead(pts[1], pts.first(), color)
            }
            else -> Unit
        }
        val label = edge.label?.let(::labelTextOf).orEmpty()
        if (label.isNotBlank()) {
            val mid = routeMidpoint(pts)
            out += DrawCommand.FillRect(Rect(Point(mid.x - 42f, mid.y - 10f), Size(84f, 20f)), colors.clusterChipFill, corner = 4f, z = 3)
            out += DrawCommand.DrawText(label, mid, edgeFont, colors.edgeLabelText, maxWidth = 80f, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Middle, z = 4)
        }
        return out
    }

    private fun routeMidpoint(points: List<Point>): Point {
        if (points.size == 2) {
            val a = points[0]
            val b = points[1]
            return Point((a.x + b.x) / 2f, (a.y + b.y) / 2f)
        }
        val lengths = points.zipWithNext { a, b ->
            sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y))
        }
        val half = lengths.sum() / 2f
        var walked = 0f
        for (i in lengths.indices) {
            val len = lengths[i]
            if (walked + len >= half) {
                val t = if (len > 0f) (half - walked) / len else 0f
                val a = points[i]
                val b = points[i + 1]
                return Point(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
            }
            walked += len
        }
        return points[points.size / 2]
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
            PathCmd(
                listOf(
                    PathOp.MoveTo(Point(bx + nx * size * 0.5f, by + ny * size * 0.5f)),
                    PathOp.LineTo(to),
                    PathOp.LineTo(Point(bx - nx * size * 0.5f, by - ny * size * 0.5f)),
                ),
            ),
            Stroke(width = 1.4f),
            color,
            z = 3,
        )
    }

    private fun labelTextOf(node: Node): String =
        when (val label = node.label) {
            is RichLabel.Plain -> label.text.takeIf { it.isNotBlank() } ?: node.id.value
            is RichLabel.Markdown -> label.source.takeIf { it.isNotBlank() } ?: node.id.value
            is RichLabel.Html -> label.html.takeIf { it.isNotBlank() } ?: node.id.value
        }

    private fun labelTextOf(label: RichLabel): String =
        when (label) {
            is RichLabel.Plain -> label.text
            is RichLabel.Markdown -> label.source
            is RichLabel.Html -> label.html
        }

    private fun nodeNameOf(node: Node): String =
        labelTextOf(node).lineSequence().firstOrNull()?.takeIf { it.isNotBlank() } ?: node.id.value

    private fun centerOf(rect: Rect): Point =
        Point(rect.left + rect.size.width / 2f, rect.top + rect.size.height / 2f)

    private fun clusterTitle(cluster: Cluster): String {
        val label = (cluster.label as? RichLabel.Plain)?.text ?: cluster.id.value
        val lines = label.split('\n')
        return when {
            lines.size >= 3 -> "${lines[1]}  ${lines[2]}"
            lines.size >= 2 -> lines[1]
            else -> label
        }
    }
}
