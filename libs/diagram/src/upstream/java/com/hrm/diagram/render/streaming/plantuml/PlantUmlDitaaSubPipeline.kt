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
import com.hrm.diagram.core.ir.Edge
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
import com.hrm.diagram.parser.plantuml.PlantUmlDitaaParser
import com.hrm.diagram.render.graph.GraphIrRenderer
import com.hrm.diagram.render.graph.GraphRenderStyle
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

internal class PlantUmlDitaaSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private val parser = PlantUmlDitaaParser()
    private val colors = ThemeResolver.resolvePlantUmlDitaa(theme)
    private val labelFont = FontSpec(family = "monospace", sizeSp = 12f, weight = 600)
    private val cellWidth = 12f
    private val cellHeight = 18f
    private val margin = 24f
    private var handwrittenGraph: Boolean = false
    private val renderer = GraphIrRenderer(
        textMeasurer,
        GraphRenderStyle(
            prefix = "plantuml",
            nodeFont = labelFont,
            edgeColor = colors.edge,
            graphBackground = { _, _ -> colors.canvas },
            customNodeCommands = ::nodeCommands,
            customEdgeCommands = ::edgeCommands,
        ),
    )
    private val kernel = PlantUmlRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layoutModel = { it },
        beforeLayout = { ir, _, _ ->
            handwrittenGraph = ir.styleHints.extras[PlantUmlDitaaParser.HANDWRITTEN_KEY] == "true"
        },
        layout = { previous, ir, _, _, isFinal -> layout(ir, previous, !isFinal) },
        renderEntities = { ir, _, laidOut, _ -> renderer.render(ir, laidOut) },
    )

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun layout(ir: GraphIR, previous: LaidOutDiagram?, incremental: Boolean): LaidOutDiagram {
        val positions = LinkedHashMap<NodeId, Rect>()
        var maxRight = 360f
        var maxBottom = 220f
        for (node in ir.nodes) {
            val grid = node.payload[PlantUmlDitaaParser.GRID_KEY]?.split(',')?.mapNotNull { it.toIntOrNull() }
            val fresh = if (grid != null && grid.size == 4) {
                val left = margin + grid[0] * cellWidth
                val top = margin + grid[1] * cellHeight
                val width = ((grid[2] - grid[0] + 1) * cellWidth).coerceAtLeast(96f)
                val height = ((grid[3] - grid[1] + 1) * cellHeight).coerceAtLeast(54f)
                Rect(Point(left, top), Size(width, height))
            } else {
                val idx = positions.size
                Rect(Point(margin + idx * 140f, margin), Size(120f, 60f))
            }
            val rect = if (incremental) previous?.nodePositions?.get(node.id) ?: fresh else fresh
            positions[node.id] = rect
            maxRight = maxOf(maxRight, rect.right + margin)
            maxBottom = maxOf(maxBottom, rect.bottom + margin)
        }
        val routes = ir.edges.mapNotNull { edge ->
            val from = positions[edge.from] ?: return@mapNotNull null
            val to = positions[edge.to] ?: return@mapNotNull null
            val (start, end) = routeEndpoints(from, to)
            val mid = if (kotlin.math.abs(start.x - end.x) > kotlin.math.abs(start.y - end.y)) {
                val x = (start.x + end.x) / 2f
                listOf(start, Point(x, start.y), Point(x, end.y), end)
            } else {
                val y = (start.y + end.y) / 2f
                listOf(start, Point(start.x, y), Point(end.x, y), end)
            }
            EdgeRoute(edge.from, edge.to, mid, RouteKind.Orthogonal)
        }
        return LaidOutDiagram(
            source = ir,
            nodePositions = positions,
            edgeRoutes = routes,
            bounds = Rect.ltrb(0f, 0f, maxRight, maxBottom),
        )
    }

    private fun render(ir: GraphIR, laid: LaidOutDiagram): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        out += DrawCommand.FillRect(laid.bounds, colors.canvas, z = -1)
        val handwritten = ir.styleHints.extras[PlantUmlDitaaParser.HANDWRITTEN_KEY] == "true"
        for ((idx, route) in laid.edgeRoutes.withIndex()) drawEdge(ir.edges.getOrNull(idx), route, out, handwritten)
        for (node in ir.nodes) drawNode(node, laid.nodePositions[node.id] ?: continue, out, handwritten)
        return out
    }

    private fun drawNode(node: Node, rect: Rect, out: MutableList<DrawCommand>, handwrittenGraph: Boolean) {
        out += nodeCommands(node, rect)
    }

    private fun nodeCommands(node: Node, rect: Rect): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val fill = node.style.fill?.let { Color(it.argb) } ?: colors.nodeFill
        val stroke = node.style.stroke?.let { Color(it.argb) } ?: colors.nodeStroke
        val text = node.style.textColor?.let { Color(it.argb) } ?: colors.nodeText
        val rounded = node.payload[PlantUmlDitaaParser.ROUNDED_KEY] == "true"
        val handwritten = handwrittenGraph || node.payload[PlantUmlDitaaParser.HANDWRITTEN_KEY] == "true"
        val corner = if (rounded) 16f else 8f
        val strokeSpec = Stroke(width = node.style.strokeWidth ?: 1.4f)
        drawShape(node, rect, fill, stroke, strokeSpec, corner, handwritten, out)
        out += DrawCommand.DrawText(
            text = labelTextOf(node),
            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
            font = labelFont,
            color = text,
            maxWidth = rect.size.width - 18f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 5,
        )
        return out
    }

    private fun drawShape(
        node: Node,
        rect: Rect,
        fill: Color,
        stroke: Color,
        strokeSpec: Stroke,
        corner: Float,
        handwritten: Boolean,
        out: MutableList<DrawCommand>,
    ) {
        when (node.shape) {
            is NodeShape.Cylinder -> drawCylinder(rect, fill, stroke, strokeSpec, out)
            is NodeShape.Parallelogram -> drawParallelogram(rect, fill, stroke, strokeSpec, out)
            is NodeShape.Diamond -> drawDiamond(rect, fill, stroke, strokeSpec, out)
            is NodeShape.Ellipse -> drawEllipse(rect, fill, stroke, strokeSpec, out)
            is NodeShape.Hexagon -> drawHexagon(rect, fill, stroke, strokeSpec, out)
            is NodeShape.Trapezoid -> drawTrapezoid(rect, fill, stroke, strokeSpec, out)
            is NodeShape.Note -> drawNote(rect, fill, stroke, strokeSpec, out)
            else -> {
                out += DrawCommand.FillRect(rect, fill, corner = corner, z = 3)
                if (handwritten) out += sketchRect(node.id.value, rect, strokeSpec, stroke) else out += DrawCommand.StrokeRect(rect, strokeSpec, stroke, corner = corner, z = 4)
            }
        }
    }

    private fun drawCylinder(rect: Rect, fill: Color, stroke: Color, strokeSpec: Stroke, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect, fill, corner = 10f, z = 3)
        out += DrawCommand.StrokeRect(rect, strokeSpec, stroke, corner = 10f, z = 4)
        out += DrawCommand.StrokeRect(Rect(rect.origin, Size(rect.size.width, 18f)), strokeSpec.copy(width = 1f), stroke, corner = 9f, z = 5)
    }

    private fun drawParallelogram(rect: Rect, fill: Color, stroke: Color, strokeSpec: Stroke, out: MutableList<DrawCommand>) {
        val s = rect.size.height * 0.22f
        drawPolygon(listOf(Point(rect.left + s, rect.top), Point(rect.right, rect.top), Point(rect.right - s, rect.bottom), Point(rect.left, rect.bottom)), fill, stroke, strokeSpec, out)
    }

    private fun drawDiamond(rect: Rect, fill: Color, stroke: Color, strokeSpec: Stroke, out: MutableList<DrawCommand>) {
        drawPolygon(listOf(Point(rect.left + rect.size.width / 2f, rect.top), Point(rect.right, rect.top + rect.size.height / 2f), Point(rect.left + rect.size.width / 2f, rect.bottom), Point(rect.left, rect.top + rect.size.height / 2f)), fill, stroke, strokeSpec, out)
    }

    private fun drawHexagon(rect: Rect, fill: Color, stroke: Color, strokeSpec: Stroke, out: MutableList<DrawCommand>) {
        val s = minOf(rect.size.width, rect.size.height) * 0.18f
        drawPolygon(listOf(Point(rect.left + s, rect.top), Point(rect.right - s, rect.top), Point(rect.right, rect.top + rect.size.height / 2f), Point(rect.right - s, rect.bottom), Point(rect.left + s, rect.bottom), Point(rect.left, rect.top + rect.size.height / 2f)), fill, stroke, strokeSpec, out)
    }

    private fun drawTrapezoid(rect: Rect, fill: Color, stroke: Color, strokeSpec: Stroke, out: MutableList<DrawCommand>) {
        val s = rect.size.width * 0.18f
        drawPolygon(listOf(Point(rect.left + s, rect.top), Point(rect.right - s, rect.top), Point(rect.right, rect.bottom), Point(rect.left, rect.bottom)), fill, stroke, strokeSpec, out)
    }

    private fun drawNote(rect: Rect, fill: Color, stroke: Color, strokeSpec: Stroke, out: MutableList<DrawCommand>) {
        val fold = 14f.coerceAtMost(rect.size.width * 0.18f).coerceAtMost(rect.size.height * 0.25f)
        val path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(rect.left, rect.top)),
                PathOp.LineTo(Point(rect.right - fold, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top + fold)),
                PathOp.LineTo(Point(rect.right, rect.bottom)),
                PathOp.LineTo(Point(rect.left, rect.bottom)),
                PathOp.Close,
            ),
        )
        out += DrawCommand.FillPath(path, fill, z = 3)
        out += DrawCommand.StrokePath(path, strokeSpec, stroke, z = 4)
        out += DrawCommand.StrokePath(PathCmd(listOf(PathOp.MoveTo(Point(rect.right - fold, rect.top)), PathOp.LineTo(Point(rect.right - fold, rect.top + fold)), PathOp.LineTo(Point(rect.right, rect.top + fold)))), strokeSpec.copy(width = 1f), stroke, z = 5)
    }

    private fun drawEllipse(rect: Rect, fill: Color, stroke: Color, strokeSpec: Stroke, out: MutableList<DrawCommand>) {
        val path = ovalPath(rect)
        out += DrawCommand.FillPath(path, fill, z = 3)
        out += DrawCommand.StrokePath(path, strokeSpec, stroke, z = 4)
    }

    private fun drawPolygon(points: List<Point>, fill: Color, stroke: Color, strokeSpec: Stroke, out: MutableList<DrawCommand>) {
        val ops = ArrayList<PathOp>()
        ops += PathOp.MoveTo(points.first())
        for (p in points.drop(1)) ops += PathOp.LineTo(p)
        ops += PathOp.Close
        val path = PathCmd(ops)
        out += DrawCommand.FillPath(path, fill, z = 3)
        out += DrawCommand.StrokePath(path, strokeSpec, stroke, z = 4)
    }

    private fun drawEdge(edge: Edge?, route: EdgeRoute, out: MutableList<DrawCommand>, handwritten: Boolean) {
        out += edgeCommands(edge ?: return, 0, route.points, route.kind, laid = null)
    }

    private fun edgeCommands(
        edge: Edge,
        index: Int,
        points: List<Point>,
        kind: RouteKind,
        laid: LaidOutDiagram?,
    ): List<DrawCommand> {
        val out = ArrayList<DrawCommand>()
        val pts = points
        if (pts.size < 2) return out
        val ops = ArrayList<PathOp>()
        val key = "${edge.from.value}->${edge.to.value}"
        val drawnPoints = if (handwrittenGraph) jitteredPoints(key, pts) else pts
        ops += PathOp.MoveTo(drawnPoints.first())
        for (i in 1 until drawnPoints.size) ops += PathOp.LineTo(drawnPoints[i])
        val color = edge.style.color?.let { Color(it.argb) } ?: colors.edge
        val stroke = Stroke(width = edge.style.width ?: 1.5f, dash = if (handwrittenGraph) listOf(7f, 2f, 2f, 2f) else edge.style.dash)
        out += DrawCommand.StrokePath(PathCmd(ops), stroke, color, z = 1)
        when (edge.arrow) {
            ArrowEnds.ToOnly -> out += openArrowHead(drawnPoints[drawnPoints.size - 2], drawnPoints.last(), color)
            ArrowEnds.FromOnly -> out += openArrowHead(drawnPoints[1], drawnPoints.first(), color)
            ArrowEnds.Both -> {
                out += openArrowHead(drawnPoints[drawnPoints.size - 2], drawnPoints.last(), color)
                out += openArrowHead(drawnPoints[1], drawnPoints.first(), color)
            }
            else -> Unit
        }
        return out
    }

    private fun routeEndpoints(from: Rect, to: Rect): Pair<Point, Point> {
        val fromCx = (from.left + from.right) / 2f
        val fromCy = (from.top + from.bottom) / 2f
        val toCx = (to.left + to.right) / 2f
        val toCy = (to.top + to.bottom) / 2f
        return if (kotlin.math.abs(toCx - fromCx) >= kotlin.math.abs(toCy - fromCy)) {
            if (toCx >= fromCx) {
                Point(from.right, fromCy) to Point(to.left, toCy)
            } else {
                Point(from.left, fromCy) to Point(to.right, toCy)
            }
        } else {
            if (toCy >= fromCy) {
                Point(fromCx, from.bottom) to Point(toCx, to.top)
            } else {
                Point(fromCx, from.top) to Point(toCx, to.bottom)
            }
        }
    }

    private fun sketchRect(key: String, rect: Rect, stroke: Stroke, color: Color): DrawCommand =
        DrawCommand.StrokePath(
            PathCmd(
                listOf(
                    PathOp.MoveTo(jitterPoint(key, 0, Point(rect.left, rect.top))),
                    PathOp.LineTo(jitterPoint(key, 1, Point(rect.right, rect.top))),
                    PathOp.LineTo(jitterPoint(key, 2, Point(rect.right, rect.bottom))),
                    PathOp.LineTo(jitterPoint(key, 3, Point(rect.left, rect.bottom))),
                    PathOp.Close,
                ),
            ),
            stroke.copy(dash = listOf(8f, 2f, 2f, 2f)),
            color,
            z = 4,
        )

    private fun jitteredPoints(key: String, points: List<Point>): List<Point> =
        points.mapIndexed { index, point -> jitterPoint(key, index, point) }

    private fun jitterPoint(key: String, index: Int, point: Point): Point =
        Point(point.x + jitter(key, index, 0), point.y + jitter(key, index, 1))

    private fun jitter(key: String, index: Int, salt: Int): Float {
        val h = key.hashCode() * 31 + index * 131 + salt * 17
        val bucket = ((h % 7) + 7) % 7
        return (bucket - 3) * 0.45f
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
            z = 2,
        )
    }

    private fun ovalPath(rect: Rect): PathCmd {
        val c = 0.5522848f
        val rx = rect.size.width / 2f
        val ry = rect.size.height / 2f
        val cx = rect.left + rx
        val cy = rect.top + ry
        return PathCmd(
            listOf(
                PathOp.MoveTo(Point(cx + rx, cy)),
                PathOp.CubicTo(Point(cx + rx, cy + ry * c), Point(cx + rx * c, cy + ry), Point(cx, cy + ry)),
                PathOp.CubicTo(Point(cx - rx * c, cy + ry), Point(cx - rx, cy + ry * c), Point(cx - rx, cy)),
                PathOp.CubicTo(Point(cx - rx, cy - ry * c), Point(cx - rx * c, cy - ry), Point(cx, cy - ry)),
                PathOp.CubicTo(Point(cx + rx * c, cy - ry), Point(cx + rx, cy - ry * c), Point(cx + rx, cy)),
                PathOp.Close,
            ),
        )
    }

    private fun labelTextOf(node: Node): String =
        when (val label = node.label) {
            is RichLabel.Plain -> label.text.takeIf { it.isNotBlank() } ?: node.id.value
            is RichLabel.Markdown -> label.source.takeIf { it.isNotBlank() } ?: node.id.value
            is RichLabel.Html -> label.html.takeIf { it.isNotBlank() } ?: node.id.value
        }
}
