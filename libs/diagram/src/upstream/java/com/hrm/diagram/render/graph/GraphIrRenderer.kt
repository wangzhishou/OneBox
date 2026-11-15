package com.hrm.diagram.render.graph

import com.hrm.diagram.core.draw.ArrowHead
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
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.text.TextMetrics
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.cache.DrawEntityKey
import kotlin.math.sqrt

internal data class GraphRenderStyle(
    val prefix: String,
    val nodeFont: FontSpec = FontSpec(family = "sans-serif", sizeSp = 12f),
    val edgeFont: FontSpec = FontSpec(family = "sans-serif", sizeSp = 10f),
    val clusterFont: FontSpec = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600),
    val nodeFill: Color = Color(0xFFF9FAFB.toInt()),
    val nodeStroke: Color = Color(0xFF374151.toInt()),
    val nodeText: Color = Color(0xFF111827.toInt()),
    val edgeColor: Color = Color(0xFF4B5563.toInt()),
    val edgeLabelText: Color = Color(0xFF263238.toInt()),
    val edgeLabelBg: Color = Color(0xF0FFFFFF.toInt()),
    val clusterFill: Color = Color(0xFFF8FAFC.toInt()),
    val clusterStroke: Color = Color(0xFF94A3B8.toInt()),
    val clusterText: Color = Color(0xFF334155.toInt()),
    val nodeCorner: (Node, Rect) -> Float = { node, rect ->
        when (node.shape) {
            NodeShape.Circle, NodeShape.Stadium -> minOf(rect.size.width, rect.size.height) / 2f
            NodeShape.RoundedBox -> 10f
            else -> 4f
        }
    },
    val nodeLabel: (Node) -> String = { graphLabelText(it.label).ifBlank { it.id.value } },
    val nodeFontOf: (Node, GraphRenderStyle) -> FontSpec = { _, style -> style.nodeFont },
    val edgeLabelFontOf: (Edge, String, GraphRenderStyle) -> FontSpec = { _, _, style -> style.edgeFont },
    val nodeTextColorOf: (Node, GraphRenderStyle) -> Color? = { _, _ -> null },
    val edgeLabelColorOf: (Edge, String, GraphRenderStyle) -> Color? = { _, _, _ -> null },
    val arrowHeadOf: (Edge, Boolean) -> ArrowHead = { _, enabled -> if (enabled) ArrowHead.Triangle else ArrowHead.None },
    val edgeEndpointAdjuster: (Edge, List<Point>, LaidOutDiagram) -> List<Point> = { _, points, _ -> points },
    val graphBackground: (GraphIR, LaidOutDiagram) -> Color? = { _, _ -> null },
    val hyperlinkOf: (Node) -> String? = { null },
    val customNodeCommands: ((Node, Rect) -> List<DrawCommand>)? = null,
    val customEdgeCommands: ((Edge, Int, List<Point>, RouteKind, LaidOutDiagram) -> List<DrawCommand>)? = null,
    val customClusterCommands: ((Cluster, Rect) -> List<DrawCommand>)? = null,
    val extraEntities: (GraphIR, LaidOutDiagram) -> List<DrawEntity> = { _, _ -> emptyList() },
)

internal class GraphIrRenderer(
    private val textMeasurer: TextMeasurer,
    private val style: GraphRenderStyle,
) {
    fun render(ir: GraphIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = ArrayList<DrawEntity>()
        style.graphBackground(ir, laid)?.let { bg ->
            out += DrawEntity(DrawEntityKey.graphBackground(style.prefix), listOf(DrawCommand.FillRect(laid.bounds, bg, z = -10)))
        }
        renderClusters(ir.clusters, laid, out)
        val routeByEndpoints = laid.edgeRoutes.associateBy { it.from to it.to }
        for ((edgeIndex, edge) in ir.edges.withIndex()) {
            if (edge.kind == EdgeKind.Invisible) continue
            val route = routeByEndpoints[edge.from to edge.to]
            val rawPoints = route?.points ?: fallbackRoute(edge.from, edge.to, laid.nodePositions) ?: continue
            val points = style.edgeEndpointAdjuster(edge, rawPoints, laid)
            val kind = route?.kind ?: RouteKind.Polyline
            val commands = style.customEdgeCommands?.invoke(edge, edgeIndex, points, kind, laid)
                ?: defaultEdgeCommands(edge, points, kind)
            out += DrawEntity(DrawEntityKey.edge(style.prefix, edge.from, edge.to, edgeIndex), commands)
        }
        for (node in ir.nodes) {
            out += renderNode(node, laid.nodePositions[node.id] ?: continue)
        }
        out += style.extraEntities(ir, laid)
        return out
    }

    private fun renderClusters(clusters: List<Cluster>, laid: LaidOutDiagram, out: MutableList<DrawEntity>) {
        for (cluster in clusters) {
            val rect = laid.clusterRects[cluster.id]
            if (rect != null) {
                val commands = style.customClusterCommands?.invoke(cluster, rect)
                    ?: defaultClusterCommands(cluster, rect)
                out += DrawEntity(DrawEntityKey.cluster(style.prefix, cluster.id), commands)
            }
            renderClusters(cluster.nestedClusters, laid, out)
        }
    }

    private fun renderNode(node: Node, rect: Rect): DrawEntity {
        style.customNodeCommands?.let { return DrawEntity(DrawEntityKey.node(style.prefix, node.id), it(node, rect)) }
        val out = ArrayList<DrawCommand>()
        val fill = node.style.fill?.let { Color(it.argb) } ?: style.nodeFill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: style.nodeStroke
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.2f)
        when (node.shape) {
            NodeShape.Circle, NodeShape.Ellipse -> {
                val path = ellipsePath(rect)
                out += DrawCommand.FillPath(path, fill, z = 7)
                out += DrawCommand.StrokePath(path, stroke, strokeColor, z = 8)
            }
            NodeShape.Diamond -> {
                val cx = (rect.left + rect.right) / 2f
                val cy = (rect.top + rect.bottom) / 2f
                val path = PathCmd(listOf(PathOp.MoveTo(Point(cx, rect.top)), PathOp.LineTo(Point(rect.right, cy)), PathOp.LineTo(Point(cx, rect.bottom)), PathOp.LineTo(Point(rect.left, cy)), PathOp.Close))
                out += DrawCommand.FillPath(path, fill, z = 7)
                out += DrawCommand.StrokePath(path, stroke, strokeColor, z = 8)
            }
            else -> {
                val corner = style.nodeCorner(node, rect)
                out += DrawCommand.FillRect(rect, fill, corner = corner, z = 7)
                out += DrawCommand.StrokeRect(rect, stroke, strokeColor, corner = corner, z = 8)
            }
        }
        out += textCommand(style.nodeLabel(node), Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f), style.nodeFontOf(node, style), style.nodeTextColorOf(node, style) ?: node.style.textColor?.let { Color(it.argb) } ?: style.nodeText, rect.size.width - 16f, TextAnchorX.Center, TextAnchorY.Middle, 9)
        style.hyperlinkOf(node)?.takeIf { it.isNotBlank() }?.let { out += DrawCommand.Hyperlink(href = it, rect = rect, z = 10) }
        return DrawEntity(DrawEntityKey.node(style.prefix, node.id), out)
    }

    private fun defaultClusterCommands(cluster: Cluster, rect: Rect): List<DrawCommand> {
        val commands = ArrayList<DrawCommand>()
        commands += DrawCommand.FillRect(rect, cluster.style.fill?.let { Color(it.argb) } ?: style.clusterFill, corner = 12f, z = 0)
        commands += DrawCommand.StrokeRect(rect, Stroke(width = cluster.style.strokeWidth ?: 1.2f), cluster.style.stroke?.let { Color(it.argb) } ?: style.clusterStroke, corner = 12f, z = 1)
        graphLabelText(cluster.label).takeIf { it.isNotBlank() }?.let {
            commands += textCommand(it, Point(rect.left + 12f, rect.top + 10f), style.clusterFont, style.clusterText, rect.size.width - 24f, TextAnchorX.Start, TextAnchorY.Top, 2)
        }
        return commands
    }

    private fun defaultEdgeCommands(edge: Edge, points: List<Point>, kind: RouteKind): List<DrawCommand> {
        val color = edge.style.color?.let { Color(it.argb) } ?: style.edgeColor
        val stroke = Stroke(width = edge.style.width ?: if (edge.kind == EdgeKind.Thick) 2.2f else 1.5f, dash = edge.style.dash)
        val commands = ArrayList<DrawCommand>()
        commands += DrawCommand.StrokePath(path = pathOf(points, kind), stroke = stroke, color = color, z = 3)
        renderArrowHeads(edge, points, kind, color, stroke, commands)
        graphLabelText(edge.label).takeIf { it.isNotBlank() }?.let { label ->
            val mid = points[points.size / 2]
            val metrics = textMeasurer.measure(label, style.edgeLabelFontOf(edge, "edge", style), maxWidth = 160f)
            val bgRect = Rect(
                Point(mid.x - metrics.width / 2f - 4f, mid.y - metrics.height / 2f - 2f),
                Size(metrics.width + 8f, metrics.height + 4f),
            )
            commands += DrawCommand.FillRect(
                rect = bgRect,
                color = edge.style.labelBg?.let { Color(it.argb) } ?: style.edgeLabelBg,
                corner = 4f,
                z = 5,
            )
            commands += textCommand(label, mid, style.edgeLabelFontOf(edge, "edge", style), style.edgeLabelColorOf(edge, "edge", style) ?: style.edgeLabelText, 160f, TextAnchorX.Center, TextAnchorY.Middle, 6)
        }
        renderEndpointLabel(edge.payload["dot.edge.headlabel"], points.last(), color, commands, edge, "dot.edge.head")
        renderEndpointLabel(edge.payload["dot.edge.taillabel"], points.first(), color, commands, edge, "dot.edge.tail")
        return commands
    }

    private fun renderEndpointLabel(label: String?, point: Point, color: Color, out: MutableList<DrawCommand>, edge: Edge, prefix: String) {
        if (label.isNullOrBlank()) return
        val font = style.edgeLabelFontOf(edge, prefix, style)
        val metrics = textMeasurer.measure(label, font, maxWidth = 120f)
        out += DrawCommand.FillRect(Rect(Point(point.x - metrics.width / 2f - 4f, point.y - metrics.height / 2f - 2f), Size(metrics.width + 8f, metrics.height + 4f)), style.edgeLabelBg, corner = 4f, z = 5)
        out += textCommand(label, point, font, style.edgeLabelColorOf(edge, prefix, style) ?: color, 120f, TextAnchorX.Center, TextAnchorY.Middle, 6)
    }

    private fun textCommand(text: String, origin: Point, font: FontSpec, color: Color, maxWidth: Float?, anchorX: TextAnchorX, anchorY: TextAnchorY, z: Int): DrawCommand.DrawText {
        val metrics = textMeasurer.measure(text, font, maxWidth)
        return DrawCommand.DrawText(text = text, origin = origin, font = font, color = color, maxWidth = maxWidth, anchorX = anchorX, anchorY = anchorY, measuredBounds = textBounds(origin, metrics, anchorX, anchorY), z = z)
    }

    private fun textBounds(origin: Point, metrics: TextMetrics, anchorX: TextAnchorX, anchorY: TextAnchorY): Rect {
        val left = when (anchorX) {
            TextAnchorX.Start -> origin.x
            TextAnchorX.Center -> origin.x - metrics.width / 2f
            TextAnchorX.End -> origin.x - metrics.width
        }
        val top = when (anchorY) {
            TextAnchorY.Top -> origin.y
            TextAnchorY.Middle -> origin.y - metrics.height / 2f
            TextAnchorY.Baseline -> origin.y - metrics.ascent
            TextAnchorY.Bottom -> origin.y - metrics.height
        }
        return Rect(Point(left, top), Size(metrics.width, metrics.height))
    }

    private fun fallbackRoute(from: NodeId, to: NodeId, nodes: Map<NodeId, Rect>): List<Point>? {
        val a = nodes[from] ?: return null
        val b = nodes[to] ?: return null
        return listOf(Point(a.right, (a.top + a.bottom) / 2f), Point(b.left, (b.top + b.bottom) / 2f))
    }

    private fun renderArrowHeads(edge: Edge, points: List<Point>, routeKind: RouteKind, color: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        if (points.size < 2) return
        val head = style.arrowHeadOf(edge, edge.arrow == ArrowEnds.ToOnly || edge.arrow == ArrowEnds.Both)
        val tail = style.arrowHeadOf(edge, edge.arrow == ArrowEnds.FromOnly || edge.arrow == ArrowEnds.Both)
        arrowCommand(head, tangentBeforeEnd(points, routeKind), points.last(), color, stroke)?.let { out += it }
        arrowCommand(tail, tangentAfterStart(points, routeKind), points.first(), color, stroke)?.let { out += it }
    }

    private fun arrowCommand(head: ArrowHead, direction: Point, tip: Point, color: Color, stroke: Stroke): DrawCommand? {
        if (head == ArrowHead.None) return null
        val len = sqrt(direction.x * direction.x + direction.y * direction.y)
        if (len <= 0.0001f) return null
        val ux = direction.x / len
        val uy = direction.y / len
        val size = 8f * stroke.width.coerceAtLeast(1f)
        val nx = -uy
        val ny = ux
        fun p(back: Float, side: Float) = Point(tip.x - ux * back + nx * side, tip.y - uy * back + ny * side)
        return when (head) {
            ArrowHead.Triangle -> DrawCommand.FillPath(PathCmd(listOf(PathOp.MoveTo(tip), PathOp.LineTo(p(size, -size / 2f)), PathOp.LineTo(p(size, size / 2f)), PathOp.Close)), color = color, z = 10)
            ArrowHead.OpenTriangle -> DrawCommand.StrokePath(PathCmd(listOf(PathOp.MoveTo(p(size, -size / 2f)), PathOp.LineTo(tip), PathOp.LineTo(p(size, size / 2f)))), stroke = stroke, color = color, z = 10)
            ArrowHead.Bar -> DrawCommand.StrokePath(PathCmd(listOf(PathOp.MoveTo(p(0f, -size / 2f)), PathOp.LineTo(p(0f, size / 2f)))), stroke = stroke, color = color, z = 10)
            ArrowHead.Cross -> DrawCommand.StrokePath(PathCmd(listOf(PathOp.MoveTo(p(size / 2f, -size / 2f)), PathOp.LineTo(p(-size / 2f, size / 2f)), PathOp.MoveTo(p(size / 2f, size / 2f)), PathOp.LineTo(p(-size / 2f, -size / 2f)))), stroke = stroke, color = color, z = 10)
            ArrowHead.Diamond, ArrowHead.OpenDiamond -> {
                val path = PathCmd(listOf(PathOp.MoveTo(tip), PathOp.LineTo(p(size / 2f, -size / 3f)), PathOp.LineTo(p(size, 0f)), PathOp.LineTo(p(size / 2f, size / 3f)), PathOp.Close))
                if (head == ArrowHead.Diamond) DrawCommand.FillPath(path, color = color, z = 10) else DrawCommand.StrokePath(path, stroke = stroke, color = color, z = 10)
            }
            ArrowHead.Circle, ArrowHead.OpenCircle -> {
                val center = p(size / 2f, 0f)
                val path = circlePath(center, radius = size / 2f)
                if (head == ArrowHead.Circle) DrawCommand.FillPath(path, color = color, z = 10) else DrawCommand.StrokePath(path, stroke = stroke, color = color, z = 10)
            }
            ArrowHead.None -> null
        }
    }

    private fun tangentBeforeEnd(points: List<Point>, routeKind: RouteKind): Point =
        if (routeKind == RouteKind.Bezier && points.size >= 4) Point(points.last().x - points[points.lastIndex - 1].x, points.last().y - points[points.lastIndex - 1].y)
        else Point(points.last().x - points[points.lastIndex - 1].x, points.last().y - points[points.lastIndex - 1].y)

    private fun tangentAfterStart(points: List<Point>, routeKind: RouteKind): Point =
        if (routeKind == RouteKind.Bezier && points.size >= 4) Point(points.first().x - points[1].x, points.first().y - points[1].y)
        else Point(points.first().x - points[1].x, points.first().y - points[1].y)

    private fun pathOf(points: List<Point>, kind: RouteKind): PathCmd {
        val ops = ArrayList<PathOp>(points.size)
        ops += PathOp.MoveTo(points.first())
        if (kind == RouteKind.Bezier) {
            var i = 1
            while (i + 2 < points.size) {
                ops += PathOp.CubicTo(points[i], points[i + 1], points[i + 2])
                i += 3
            }
            while (i < points.size) ops += PathOp.LineTo(points[i++])
        } else {
            points.drop(1).forEach { ops += PathOp.LineTo(it) }
        }
        return PathCmd(ops)
    }

    private fun ellipsePath(rect: Rect): PathCmd {
        val cx = (rect.left + rect.right) / 2f
        val cy = (rect.top + rect.bottom) / 2f
        val rx = rect.size.width / 2f
        val ry = rect.size.height / 2f
        return PathCmd(listOf(
            PathOp.MoveTo(Point(cx, rect.top)),
            PathOp.CubicTo(Point(cx + rx * 0.552f, rect.top), Point(rect.right, cy - ry * 0.552f), Point(rect.right, cy)),
            PathOp.CubicTo(Point(rect.right, cy + ry * 0.552f), Point(cx + rx * 0.552f, rect.bottom), Point(cx, rect.bottom)),
            PathOp.CubicTo(Point(cx - rx * 0.552f, rect.bottom), Point(rect.left, cy + ry * 0.552f), Point(rect.left, cy)),
            PathOp.CubicTo(Point(rect.left, cy - ry * 0.552f), Point(cx - rx * 0.552f, rect.top), Point(cx, rect.top)),
            PathOp.Close,
        ))
    }

    private fun circlePath(center: Point, radius: Float): PathCmd {
        val k = radius * 0.5522848f
        return PathCmd(listOf(
            PathOp.MoveTo(Point(center.x + radius, center.y)),
            PathOp.CubicTo(Point(center.x + radius, center.y + k), Point(center.x + k, center.y + radius), Point(center.x, center.y + radius)),
            PathOp.CubicTo(Point(center.x - k, center.y + radius), Point(center.x - radius, center.y + k), Point(center.x - radius, center.y)),
            PathOp.CubicTo(Point(center.x - radius, center.y - k), Point(center.x - k, center.y - radius), Point(center.x, center.y - radius)),
            PathOp.CubicTo(Point(center.x + k, center.y - radius), Point(center.x + radius, center.y - k), Point(center.x + radius, center.y)),
            PathOp.Close,
        ))
    }
}
