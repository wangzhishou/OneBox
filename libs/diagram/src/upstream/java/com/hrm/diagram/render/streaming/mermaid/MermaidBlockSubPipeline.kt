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
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatch
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.EdgeRoute
import com.hrm.diagram.layout.IncrementalLayout
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.parser.mermaid.BlockCellPlacement
import com.hrm.diagram.parser.mermaid.MermaidBlockParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.streaming.SessionPatch
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

internal class MermaidBlockSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidBlockParser()
    private val themeColors = theme.colors
    private val colors = ThemeResolver.resolveGraph(theme)
    private val layout = BlockLayout(textMeasurer)
    private val styleTransform = MermaidStyleTransformState.graph()
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        transformModel = styleTransform::apply,
        beforeLayout = { layout.updatePlacements(parser.placementSnapshot()) },
        layout = layout::layout,
        renderEntities = ::render,
        postLayout = { _, laidOut, seq -> laidOut.copy(seq = seq) },
        patchFactory = ::blockPatch,
    )
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f)
    private val clusterFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)

    override fun updateGraphStyles(styles: MermaidGraphStyleState) {
        styleTransform.update(styles)
    }

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun blockPatch(
        seq: Long,
        isFinal: Boolean,
        patches: List<IrPatch>,
        diagnostics: List<com.hrm.diagram.core.ir.Diagnostic>,
    ): SessionPatch = SessionPatch(
        seq = seq,
        addedNodes = patches.filterIsInstance<IrPatch.AddNode>().map { it.node.id },
        addedEdges = patches.filterIsInstance<IrPatch.AddEdge>().map { it.edge },
        addedDrawCommands = emptyList(),
        newDiagnostics = diagnostics,
        isFinal = isFinal,
    )

    private fun render(ir: GraphIR, laidOut: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laidOut)
        out += DrawCommand.FillRect(
            rect = Rect(Point(laidOut.bounds.left, laidOut.bounds.top), Size(laidOut.bounds.size.width, laidOut.bounds.size.height)),
            color = colors.background,
            z = 0,
        )
        for (cluster in ir.clusters) drawCluster(cluster, laidOut.clusterRects, out)
        for (node in ir.nodes) drawNode(node, laidOut, out)
        for ((index, route) in laidOut.edgeRoutes.withIndex()) {
            val edge = ir.edges.getOrNull(index) ?: continue
            drawEdge(edge, route, out)
        }
        return out.entities()
    }

    private fun drawCluster(cluster: Cluster, clusterRects: Map<NodeId, Rect>, out: MutableList<DrawCommand>) {
        val rect = clusterRects[cluster.id] ?: return
        val fill = cluster.style.fill?.let { Color(it.argb) } ?: colors.clusterFill
        val strokeColor = cluster.style.stroke?.let { Color(it.argb) } ?: colors.clusterStroke
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 12f, z = 1)
        out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = cluster.style.strokeWidth ?: 1.5f, dash = listOf(7f, 5f)), color = strokeColor, corner = 12f, z = 2)
        val label = (cluster.label as? RichLabel.Plain)?.text.orEmpty().ifBlank { cluster.id.value }
        out += DrawCommand.DrawText(
            text = label,
            origin = Point(rect.left + 10f, rect.top + 12f),
            font = clusterFont,
            color = strokeColor,
            maxWidth = rect.size.width - 20f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 3,
        )
        cluster.nestedClusters.forEach { drawCluster(it, clusterRects, out) }
    }

    private fun drawNode(node: Node, laidOut: LaidOutDiagram, out: MutableList<DrawCommand>) {
        val rect = laidOut.nodePositions[node.id] ?: return
        val semanticColors = semanticNodeColors(node)
        val fill = node.style.fill?.let { Color(it.argb) } ?: semanticColors.fill
        val strokeColor = node.style.stroke?.let { Color(it.argb) } ?: semanticColors.stroke
        val textColor = node.style.textColor?.let { Color(it.argb) } ?: semanticColors.text
        val stroke = Stroke(width = node.style.strokeWidth ?: 1.5f)
        when (node.shape) {
            is NodeShape.Circle -> {
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = min(rect.size.width, rect.size.height) / 2f, z = 4)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = min(rect.size.width, rect.size.height) / 2f, z = 5)
            }
            is NodeShape.EndCircle -> {
                val corner = min(rect.size.width, rect.size.height) / 2f
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = corner, z = 4)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = corner, z = 5)
                val inset = 6f
                out += DrawCommand.StrokeRect(
                    rect = Rect.ltrb(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset),
                    stroke = Stroke(width = 1.2f),
                    color = strokeColor,
                    corner = max(0f, corner - inset),
                    z = 6,
                )
            }
            is NodeShape.RoundedBox -> {
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = 14f, z = 4)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 14f, z = 5)
            }
            is NodeShape.Stadium -> {
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = min(rect.size.width, rect.size.height) / 2f, z = 4)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = min(rect.size.width, rect.size.height) / 2f, z = 5)
            }
            is NodeShape.Diamond -> drawDiamond(rect, fill, strokeColor, stroke, out)
            is NodeShape.Hexagon -> drawHexagon(rect, fill, strokeColor, stroke, out)
            is NodeShape.Cylinder -> drawCylinder(rect, fill, strokeColor, stroke, out)
            is NodeShape.Subroutine -> {
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = 8f, z = 4)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 8f, z = 5)
                val innerL = Rect.ltrb(rect.left + 10f, rect.top + 2f, rect.left + 14f, rect.bottom - 2f)
                val innerR = Rect.ltrb(rect.right - 14f, rect.top + 2f, rect.right - 10f, rect.bottom - 2f)
                out += DrawCommand.FillRect(rect = innerL, color = strokeColor, corner = 0f, z = 6)
                out += DrawCommand.FillRect(rect = innerR, color = strokeColor, corner = 0f, z = 6)
            }
            is NodeShape.Parallelogram -> drawParallelogram(rect, fill, strokeColor, stroke, out)
            is NodeShape.Trapezoid -> drawTrapezoid(rect, fill, strokeColor, stroke, out)
            is NodeShape.Custom -> {
                val customShape = node.shape as NodeShape.Custom
                if (customShape.name == "asymmetric") drawAsymmetric(rect, fill, strokeColor, stroke, out)
                else drawBlockArrow(node, rect, fill, strokeColor, stroke, out)
            }
            else -> {
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = 8f, z = 4)
                out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 8f, z = 5)
            }
        }
        val label = (node.label as? RichLabel.Plain)?.text.orEmpty()
        if (label.isNotBlank()) {
            out += DrawCommand.DrawText(
                text = label,
                origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                font = labelFont,
                color = textColor,
                maxWidth = rect.size.width - 14f,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 7,
            )
        }
    }

    private fun drawEdge(edge: com.hrm.diagram.core.ir.Edge, route: EdgeRoute, out: MutableList<DrawCommand>) {
        val pts = route.points
        if (pts.size < 2) return
        val ops = ArrayList<PathOp>(pts.size)
        ops += PathOp.MoveTo(pts[0])
        for (i in 1 until pts.size) ops += PathOp.LineTo(pts[i])
        val edgeColor = edge.style.color?.let { Color(it.argb) } ?: colors.edge
        out += DrawCommand.StrokePath(path = PathCmd(ops), stroke = Stroke(width = edge.style.width ?: 1.5f, dash = edge.style.dash), color = edgeColor, z = 3)
        when (edge.arrow) {
            com.hrm.diagram.core.ir.ArrowEnds.None -> Unit
            com.hrm.diagram.core.ir.ArrowEnds.ToOnly -> out += arrowHead(pts[pts.size - 2], pts.last(), edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.FromOnly -> out += arrowHead(pts[1], pts[0], edgeColor)
            com.hrm.diagram.core.ir.ArrowEnds.Both -> {
                out += arrowHead(pts[pts.size - 2], pts.last(), edgeColor)
                out += arrowHead(pts[1], pts[0], edgeColor)
            }
        }
        val text = (edge.label as? RichLabel.Plain)?.text.orEmpty()
        if (text.isNotBlank()) {
            val mid = pts[pts.size / 2]
            val metrics = textMeasurer.measure(text, edgeLabelFont)
            val bgRect = Rect.ltrb(mid.x - metrics.width / 2f - 4f, mid.y - metrics.height / 2f - 2f, mid.x + metrics.width / 2f + 4f, mid.y + metrics.height / 2f + 2f)
            out += DrawCommand.FillRect(rect = bgRect, color = edge.style.labelBg?.let { Color(it.argb) } ?: colors.edgeLabelBackground, corner = 3f, z = 8)
            out += DrawCommand.DrawText(text = text, origin = mid, font = edgeLabelFont, color = colors.edgeLabelText, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Middle, z = 9)
        }
    }

    private fun semanticNodeColors(node: Node): BlockSemanticColors {
        val kind = node.payload[MermaidBlockParser.KIND_KEY].orEmpty()
        return when (kind) {
            "arrow" -> {
                val fill = themeColors.success.withAlpha(0.14f)
                BlockSemanticColors(
                    fill = fill,
                    stroke = themeColors.success,
                    text = readableTextOn(fill),
                )
            }
            else -> BlockSemanticColors(
                fill = colors.nodeFill,
                stroke = colors.nodeStroke,
                text = colors.nodeText,
            )
        }
    }

    private fun readableTextOn(background: Color): Color = if (background.isDark()) themeColors.surface else themeColors.textPrimary

    private fun drawDiamond(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        val cx = (rect.left + rect.right) / 2f
        val cy = (rect.top + rect.bottom) / 2f
        val path = PathCmd(listOf(PathOp.MoveTo(Point(cx, rect.top)), PathOp.LineTo(Point(rect.right, cy)), PathOp.LineTo(Point(cx, rect.bottom)), PathOp.LineTo(Point(rect.left, cy)), PathOp.Close))
        out += DrawCommand.FillPath(path = path, color = fill, z = 4)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 5)
    }

    private fun drawHexagon(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        val dx = rect.size.width * 0.12f
        val path = PathCmd(listOf(
            PathOp.MoveTo(Point(rect.left + dx, rect.top)),
            PathOp.LineTo(Point(rect.right - dx, rect.top)),
            PathOp.LineTo(Point(rect.right, (rect.top + rect.bottom) / 2f)),
            PathOp.LineTo(Point(rect.right - dx, rect.bottom)),
            PathOp.LineTo(Point(rect.left + dx, rect.bottom)),
            PathOp.LineTo(Point(rect.left, (rect.top + rect.bottom) / 2f)),
            PathOp.Close,
        ))
        out += DrawCommand.FillPath(path = path, color = fill, z = 4)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 5)
    }

    private fun drawParallelogram(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        val dx = rect.size.width * 0.12f
        val path = PathCmd(listOf(
            PathOp.MoveTo(Point(rect.left + dx, rect.top)),
            PathOp.LineTo(Point(rect.right, rect.top)),
            PathOp.LineTo(Point(rect.right - dx, rect.bottom)),
            PathOp.LineTo(Point(rect.left, rect.bottom)),
            PathOp.Close,
        ))
        out += DrawCommand.FillPath(path = path, color = fill, z = 4)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 5)
    }

    private fun drawCylinder(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 10f, z = 4)
        out += DrawCommand.StrokeRect(rect = rect, stroke = stroke, color = strokeColor, corner = 10f, z = 5)
        out += DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(Point(rect.left + 12f, rect.top + 10f)), PathOp.CubicTo(Point(rect.left + 30f, rect.top), Point(rect.right - 30f, rect.top), Point(rect.right - 12f, rect.top + 10f)))),
            stroke = Stroke(width = 1f),
            color = strokeColor,
            z = 6,
        )
    }

    private fun drawBlockArrow(node: Node, rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        val dir = node.payload[MermaidBlockParser.ARROW_DIR_KEY].orEmpty()
        val path = when (dir.substringBefore(',').trim()) {
            "x" -> PathCmd(listOf(
                PathOp.MoveTo(Point(rect.left + rect.size.width * 0.2f, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top)),
                PathOp.LineTo(Point(rect.right - rect.size.width * 0.2f, (rect.top + rect.bottom) / 2f)),
                PathOp.LineTo(Point(rect.right, rect.bottom)),
                PathOp.LineTo(Point(rect.left + rect.size.width * 0.2f, rect.bottom)),
                PathOp.LineTo(Point(rect.left, (rect.top + rect.bottom) / 2f)),
                PathOp.Close,
            ))
            "y" -> PathCmd(listOf(
                PathOp.MoveTo(Point(rect.left, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top)),
                PathOp.LineTo(Point(rect.right - rect.size.width * 0.25f, rect.top + rect.size.height * 0.45f)),
                PathOp.LineTo(Point((rect.left + rect.right) / 2f, rect.bottom)),
                PathOp.LineTo(Point(rect.left + rect.size.width * 0.25f, rect.top + rect.size.height * 0.45f)),
                PathOp.Close,
            ))
            "left" -> PathCmd(listOf(
                PathOp.MoveTo(Point(rect.left, (rect.top + rect.bottom) / 2f)),
                PathOp.LineTo(Point(rect.left + rect.size.width * 0.35f, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.bottom)),
                PathOp.LineTo(Point(rect.left + rect.size.width * 0.35f, rect.bottom)),
                PathOp.Close,
            ))
            "up" -> PathCmd(listOf(
                PathOp.MoveTo(Point((rect.left + rect.right) / 2f, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top + rect.size.height * 0.35f)),
                PathOp.LineTo(Point(rect.right, rect.bottom)),
                PathOp.LineTo(Point(rect.left, rect.bottom)),
                PathOp.LineTo(Point(rect.left, rect.top + rect.size.height * 0.35f)),
                PathOp.Close,
            ))
            "down" -> PathCmd(listOf(
                PathOp.MoveTo(Point(rect.left, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.top)),
                PathOp.LineTo(Point(rect.right, rect.bottom - rect.size.height * 0.35f)),
                PathOp.LineTo(Point((rect.left + rect.right) / 2f, rect.bottom)),
                PathOp.LineTo(Point(rect.left, rect.bottom - rect.size.height * 0.35f)),
                PathOp.Close,
            ))
            else -> PathCmd(listOf(
                PathOp.MoveTo(Point(rect.left, rect.top)),
                PathOp.LineTo(Point(rect.right - rect.size.width * 0.35f, rect.top)),
                PathOp.LineTo(Point(rect.right, (rect.top + rect.bottom) / 2f)),
                PathOp.LineTo(Point(rect.right - rect.size.width * 0.35f, rect.bottom)),
                PathOp.LineTo(Point(rect.left, rect.bottom)),
                PathOp.Close,
            ))
        }
        out += DrawCommand.FillPath(path = path, color = fill, z = 4)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 5)
    }

    private fun drawTrapezoid(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        val inset = rect.size.width * 0.12f
        val path = PathCmd(listOf(
            PathOp.MoveTo(Point(rect.left + inset, rect.top)),
            PathOp.LineTo(Point(rect.right - inset, rect.top)),
            PathOp.LineTo(Point(rect.right, rect.bottom)),
            PathOp.LineTo(Point(rect.left, rect.bottom)),
            PathOp.Close,
        ))
        out += DrawCommand.FillPath(path = path, color = fill, z = 4)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 5)
    }

    private fun drawAsymmetric(rect: Rect, fill: Color, strokeColor: Color, stroke: Stroke, out: MutableList<DrawCommand>) {
        val inset = rect.size.width * 0.18f
        val path = PathCmd(listOf(
            PathOp.MoveTo(Point(rect.left, rect.top)),
            PathOp.LineTo(Point(rect.right - inset, rect.top)),
            PathOp.LineTo(Point(rect.right, rect.bottom)),
            PathOp.LineTo(Point(rect.left + inset, rect.bottom)),
            PathOp.Close,
        ))
        out += DrawCommand.FillPath(path = path, color = fill, z = 4)
        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 5)
    }

    private fun arrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: 1f
        val ux = dx / len
        val uy = dy / len
        val size = 8f
        val baseX = to.x - ux * size
        val baseY = to.y - uy * size
        val nx = -uy
        val ny = ux
        val p1 = Point(baseX + nx * size * 0.5f, baseY + ny * size * 0.5f)
        val p2 = Point(baseX - nx * size * 0.5f, baseY - ny * size * 0.5f)
        return DrawCommand.FillPath(path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(p1), PathOp.LineTo(p2), PathOp.Close)), color = color, z = 4)
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }

}

private data class BlockSemanticColors(
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

private class BlockLayout(
    private val textMeasurer: TextMeasurer,
) : IncrementalLayout<GraphIR> {
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f)
    private val clusterFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private var placements: Map<NodeId, BlockCellPlacement> = emptyMap()

    fun updatePlacements(placements: Map<NodeId, BlockCellPlacement>) {
        this.placements = placements
    }

    override fun layout(previous: LaidOutDiagram?, model: GraphIR, options: LayoutOptions): LaidOutDiagram {
        val ir = model
        val placements = this.placements
        val nodePositions = LinkedHashMap<NodeId, Rect>()
        val clusterRects = LinkedHashMap<NodeId, Rect>()
        val edgeRoutes = ArrayList<EdgeRoute>()
        val colWidths = LinkedHashMap<Pair<NodeId?, Int>, Float>()
        val rowHeights = LinkedHashMap<Pair<NodeId?, Int>, Float>()
        val parentColumns = HashMap<NodeId?, Int>()
        parentColumns[null] = ir.styleHints.extras["block.columns.root"]?.toIntOrNull() ?: 1
        for (cluster in ir.clusters) collectColumns(cluster, ir.styleHints.extras, parentColumns)

        for (node in ir.nodes) {
            val place = placements[node.id] ?: continue
            val metrics = textMeasurer.measure((node.label as? RichLabel.Plain)?.text ?: node.id.value, labelFont, maxWidth = 220f)
            val desiredWidth = (metrics.width + 28f).coerceAtLeast(defaultWidthFor(node))
            val desiredHeight = (metrics.height + 24f).coerceAtLeast(defaultHeightFor(node))
            val perColumn = desiredWidth / place.span
            for (c in place.col until place.col + place.span) {
                val key = place.parent to c
                colWidths[key] = max(colWidths[key] ?: 0f, perColumn)
            }
            val rowKey = place.parent to place.row
            rowHeights[rowKey] = max(rowHeights[rowKey] ?: 0f, desiredHeight)
        }

        placeNodesForParent(parent = null, ir = ir, placements = placements, colWidths = colWidths, rowHeights = rowHeights, parentColumns = parentColumns, originX = 24f, originY = 24f, nodePositions = nodePositions, clusterRects = clusterRects)

        for (edge in ir.edges) {
            val fromRect = nodePositions[edge.from] ?: continue
            val toRect = nodePositions[edge.to] ?: continue
            val from = Point(fromRect.right, (fromRect.top + fromRect.bottom) / 2f)
            val to = Point(toRect.left, (toRect.top + toRect.bottom) / 2f)
            val midX = (from.x + to.x) / 2f
            edgeRoutes += EdgeRoute(edge.from, edge.to, listOf(from, Point(midX, from.y), Point(midX, to.y), to), kind = RouteKind.Orthogonal)
        }

        val allRects = nodePositions.values + clusterRects.values
        val bounds = if (allRects.isEmpty()) Rect.ltrb(0f, 0f, 400f, 240f) else Rect.ltrb(0f, 0f, allRects.maxOf { it.right } + 24f, allRects.maxOf { it.bottom } + 24f)
        return LaidOutDiagram(source = ir, nodePositions = nodePositions, edgeRoutes = edgeRoutes, clusterRects = clusterRects, bounds = bounds)
    }

    private fun collectColumns(cluster: Cluster, extras: Map<String, String>, out: MutableMap<NodeId?, Int>) {
        out[cluster.id] = extras["block.columns.${cluster.id.value}"]?.toIntOrNull() ?: 1
        cluster.nestedClusters.forEach { collectColumns(it, extras, out) }
    }

    private fun placeNodesForParent(
        parent: NodeId?,
        ir: GraphIR,
        placements: Map<NodeId, BlockCellPlacement>,
        colWidths: Map<Pair<NodeId?, Int>, Float>,
        rowHeights: Map<Pair<NodeId?, Int>, Float>,
        parentColumns: Map<NodeId?, Int>,
        originX: Float,
        originY: Float,
        nodePositions: MutableMap<NodeId, Rect>,
        clusterRects: MutableMap<NodeId, Rect>,
    ): Rect {
        val cols = parentColumns[parent] ?: 1
        val colGap = 18f
        val rowGap = 16f
        val lefts = FloatArray(cols)
        var cursor = originX
        for (c in 0 until cols) {
            lefts[c] = cursor
            cursor += (colWidths[parent to c] ?: 140f) + colGap
        }
        val rows = placements.values.filter { it.parent == parent }.maxOfOrNull { it.row + 1 } ?: 0
        val tops = FloatArray(rows.coerceAtLeast(1))
        var topCursor = originY
        for (r in 0 until rows) {
            tops[r] = topCursor
            topCursor += (rowHeights[parent to r] ?: 72f) + rowGap
        }

        val directNodes = ir.nodes.filter { placements[it.id]?.parent == parent }
        for (node in directNodes) {
            val place = placements[node.id] ?: continue
            val left = lefts[place.col]
            val width = (place.col until place.col + place.span).sumOf { ((colWidths[parent to it] ?: 140f) + if (it == place.col + place.span - 1) 0f else colGap).toDouble() }.toFloat()
            val height = rowHeights[parent to place.row] ?: 72f
            nodePositions[node.id] = Rect.ltrb(left, tops[place.row], left + width, tops[place.row] + height)
        }

        val directClusters = if (parent == null) ir.clusters else findClusters(parent, ir.clusters)
        for (cluster in directClusters) {
            val place = placements[cluster.id] ?: continue
            val baseLeft = lefts[place.col]
            val baseTop = tops[place.row]
            val inner = placeNodesForParent(cluster.id, ir, placements, colWidths, rowHeights, parentColumns, baseLeft + 16f, baseTop + 40f, nodePositions, clusterRects)
            val labelHeight = textMeasurer.measure((cluster.label as? RichLabel.Plain)?.text.orEmpty(), clusterFont, maxWidth = 220f).height
            val rect = Rect.ltrb(baseLeft, baseTop, max(inner.right + 16f, baseLeft + 160f), max(inner.bottom + 16f, baseTop + labelHeight + 64f))
            clusterRects[cluster.id] = rect
        }

        val all = mutableListOf<Rect>()
        all += directNodes.mapNotNull { nodePositions[it.id] }
        all += directClusters.mapNotNull { clusterRects[it.id] }
        return if (all.isEmpty()) Rect.ltrb(originX, originY, originX + 120f, originY + 80f) else Rect.ltrb(all.minOf { it.left }, all.minOf { it.top }, all.maxOf { it.right }, all.maxOf { it.bottom })
    }

    private fun findClusters(parent: NodeId, roots: List<Cluster>): List<Cluster> {
        for (cluster in roots) {
            if (cluster.id == parent) return cluster.nestedClusters
            val nested = findClusters(parent, cluster.nestedClusters)
            if (nested.isNotEmpty()) return nested
        }
        return emptyList()
    }

    private fun defaultWidthFor(node: Node): Float = when (node.shape) {
        is NodeShape.Circle -> 82f
        is NodeShape.Custom -> 92f
        else -> 128f
    }

    private fun defaultHeightFor(node: Node): Float = when (node.shape) {
        is NodeShape.Circle -> 82f
        else -> 64f
    }
}
