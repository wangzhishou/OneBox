package com.hrm.diagram.render.export

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
import com.hrm.diagram.core.ir.MessageKind
import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.PieIR
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.ir.SequenceIR
import com.hrm.diagram.core.ir.TimeSeriesIR
import com.hrm.diagram.core.ir.TreeIR
import com.hrm.diagram.core.ir.TreeNode
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private fun buildPieFrame(
    ir: PieIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawCommand> {
    val colors = ThemeResolver.resolvePie(theme)
    val titleFont = theme.typography.titleFont.copy(sizeSp = 14f)
    val legendFont = theme.typography.bodyFont.copy(sizeSp = 12f)
    val border = Stroke(width = 1f)
    val out = ArrayList<DrawCommand>()
    val pieRect = laid.nodePositions[NodeId("pie:plot")] ?: Rect(Point(20f, 20f), Size(240f, 240f))
    val center = Point((pieRect.left + pieRect.right) / 2f, (pieRect.top + pieRect.bottom) / 2f)
    val radius = min(pieRect.size.width, pieRect.size.height) / 2f
    val total = ir.slices.sumOf { it.value }.takeIf { it > 0.0 } ?: 1.0
    var angle = -PI / 2.0

    laid.nodePositions[NodeId("pie:title")]?.let { titleRect ->
        ir.title?.takeIf { it.isNotBlank() }?.let { title ->
            out += DrawCommand.DrawText(
                text = title,
                origin = Point(titleRect.left, titleRect.top),
                font = titleFont,
                color = colors.titleText,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 10,
                measuredBounds = titleRect,
            )
        }
    }

    for ((index, slice) in ir.slices.withIndex()) {
        val sweep = (slice.value / total) * 2.0 * PI
        val start = angle
        val end = angle + sweep
        val path = wedgePath(center, radius, start, end)
        val fill = colors.slices[index % colors.slices.size]
        out += DrawCommand.FillPath(path = path, color = fill, z = 1)
        out += DrawCommand.StrokePath(path = path, stroke = border, color = colors.border, z = 2)
        angle = end
    }

    for ((index, slice) in ir.slices.withIndex()) {
        val row = laid.nodePositions[NodeId("pie:legend:$index")] ?: continue
        val swatch = Rect.ltrb(row.left, row.top + 3f, row.left + 14f, row.bottom - 3f)
        out += DrawCommand.FillRect(rect = swatch, color = colors.slices[index % colors.slices.size], corner = 3f, z = 5)
        out += DrawCommand.StrokeRect(rect = swatch, stroke = Stroke.Hairline, color = colors.border, corner = 3f, z = 6)
        val label = slice.label.plainText("slice$index")
        out += DrawCommand.DrawText(
            text = label,
            origin = Point(swatch.right + 8f, (row.top + row.bottom) / 2f),
            font = legendFont,
            color = colors.legendText,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Middle,
            z = 7,
        )
        out += DrawCommand.DrawText(
            text = formatNumber(slice.value),
            origin = Point(row.right, (row.top + row.bottom) / 2f),
            font = legendFont,
            color = colors.legendText,
            anchorX = TextAnchorX.End,
            anchorY = TextAnchorY.Middle,
            z = 7,
        )
    }
    return out
}

internal fun renderPieEntities(
    ir: PieIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawEntity> = oneShotEntities(ir, laid, buildPieFrame(ir, laid, theme))

private fun buildTreeFrame(
    ir: TreeIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawCommand> {
    val out = ArrayList<DrawCommand>()
    val colors = ThemeResolver.resolveTree(theme)
    val font = theme.typography.bodyFont.copy(sizeSp = 12f)

    fun drawEdges(parent: TreeNode) {
        val pr = laid.nodePositions[parent.id] ?: return
        for (child in parent.children) {
            val cr = laid.nodePositions[child.id] ?: continue
            val childOnRight = cr.left >= pr.left
            val from = if (childOnRight) Point(pr.right, (pr.top + pr.bottom) / 2f) else Point(pr.left, (pr.top + pr.bottom) / 2f)
            val to = if (childOnRight) Point(cr.left, (cr.top + cr.bottom) / 2f) else Point(cr.right, (cr.top + cr.bottom) / 2f)
            val midX = (from.x + to.x) / 2f
            val path = PathCmd(
                listOf(
                    PathOp.MoveTo(from),
                    PathOp.CubicTo(Point(midX, from.y), Point(midX, to.y), to),
                ),
            )
            out += DrawCommand.StrokePath(path = path, stroke = Stroke(width = 1.5f), color = colors.edge, z = 0)
            drawEdges(child)
        }
    }

    fun drawNode(node: TreeNode, isRoot: Boolean) {
        val rect = laid.nodePositions[node.id] ?: return
        val fill = node.style.fill?.let { Color(it.argb) } ?: if (isRoot) colors.rootFill else colors.nodeFill
        val stroke = node.style.stroke?.let { Color(it.argb) } ?: if (isRoot) colors.rootStroke else colors.nodeStroke
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = if (isRoot) 14f else 10f, z = 1)
        out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = if (isRoot) 2f else 1.5f), color = stroke, corner = if (isRoot) 14f else 10f, z = 2)
        out += DrawCommand.DrawText(
            text = node.label.plainText(node.id.value),
            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
            font = font,
            color = node.style.textColor?.let { Color(it.argb) } ?: if (isRoot) colors.rootText else colors.nodeText,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            maxWidth = rect.size.width - 12f,
            z = 3,
            measuredBounds = rect,
        )
        node.children.forEach { drawNode(it, false) }
    }

    drawEdges(ir.root)
    drawNode(ir.root, true)
    return out
}

internal fun renderTreeEntities(
    ir: TreeIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawEntity> = oneShotEntities(ir, laid, buildTreeFrame(ir, laid, theme))

private fun buildSequenceFrame(
    ir: SequenceIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawCommand> {
    val out = ArrayList<DrawCommand>()
    val colors = ThemeResolver.resolveSequence(theme)
    val labelFont = theme.typography.bodyFont.copy(sizeSp = 13f)
    val msgFont = theme.typography.bodyFont.copy(sizeSp = 11f)
    val bottomY = laid.bounds.bottom
    val dashedStroke = Stroke(width = 1.5f, dash = listOf(6f, 4f))
    val solidStroke = Stroke(width = 1.5f)

    for (participant in ir.participants) {
        val rect = laid.nodePositions[participant.id] ?: continue
        val cx = (rect.left + rect.right) / 2f
        val cy = (rect.top + rect.bottom) / 2f
        out += DrawCommand.FillRect(rect = rect, color = colors.headerFill, corner = 6f, z = 2)
        out += DrawCommand.StrokeRect(rect = rect, stroke = solidStroke, color = colors.headerStroke, corner = 6f, z = 3)
        out += DrawCommand.DrawText(
            text = participant.label.plainText(participant.id.value),
            origin = Point(cx, cy),
            font = labelFont,
            color = colors.headerText,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 4,
            measuredBounds = rect,
        )
        out += DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(Point(cx, rect.bottom)), PathOp.LineTo(Point(cx, bottomY)))),
            stroke = dashedStroke,
            color = colors.lifeline,
            z = 0,
        )
    }

    for ((id, rect) in laid.clusterRects) {
        val key = id.value
        when {
            key.contains("#act#") -> {
                out += DrawCommand.FillRect(rect = rect, color = colors.activationFill, z = 5)
                out += DrawCommand.StrokeRect(rect = rect, stroke = solidStroke, color = colors.activationStroke, z = 6)
            }
            key.startsWith("note#") -> {
                out += DrawCommand.FillRect(rect = rect, color = colors.noteFill, corner = 4f, z = 7)
                out += DrawCommand.StrokeRect(rect = rect, stroke = solidStroke, color = colors.noteStroke, corner = 4f, z = 8)
            }
            key.startsWith("frag#") -> {
                out += DrawCommand.StrokeRect(rect = rect, stroke = solidStroke, color = colors.fragmentStroke, corner = 4f, z = 9)
            }
        }
    }

    val noteRects = laid.clusterRects.filterKeys { it.value.startsWith("note#") }.values.toList()
    var noteIndex = 0
    var edgeIndex = 0
    for ((msgIndex, message) in ir.messages.withIndex()) {
        when (message.kind) {
            MessageKind.Note -> {
                if (message.activate || message.deactivate) continue
                val rect = noteRects.getOrNull(noteIndex++) ?: continue
                val label = message.label.plainText("")
                if (label.isNotEmpty()) {
                    out += DrawCommand.DrawText(
                        text = label,
                        origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                        font = msgFont,
                        color = colors.noteText,
                        anchorX = TextAnchorX.Center,
                        anchorY = TextAnchorY.Middle,
                        z = 10,
                        measuredBounds = rect,
                    )
                }
            }
            else -> {
                val route = laid.edgeRoutes.getOrNull(edgeIndex++) ?: continue
                val points = route.points
                val from = points.firstOrNull() ?: continue
                val to = points.lastOrNull() ?: continue
                val stroke = if (message.kind == MessageKind.Reply) dashedStroke else solidStroke
                out += DrawCommand.StrokePath(
                    path = PathCmd(listOf(PathOp.MoveTo(from), PathOp.LineTo(to))),
                    stroke = stroke,
                    color = colors.message,
                    z = 1,
                )
                out += when (message.kind) {
                    MessageKind.Async -> openArrowHead(from, to, colors.message)
                    MessageKind.Destroy -> xMark(to, colors.message)
                    else -> filledArrowHead(from, to, colors.message)
                }
                val label = message.label.plainText("")
                if (label.isNotEmpty()) {
                    out += DrawCommand.DrawText(
                        text = label,
                        origin = Point((from.x + to.x) / 2f, from.y - 4f),
                        font = msgFont,
                        color = colors.messageText,
                        anchorX = TextAnchorX.Center,
                        anchorY = TextAnchorY.Bottom,
                        z = 10,
                    )
                }
            }
        }
    }
    return out
}

internal fun renderSequenceEntities(
    ir: SequenceIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawEntity> = oneShotEntities(ir, laid, buildSequenceFrame(ir, laid, theme))

private fun buildTimeSeriesFrame(
    ir: TimeSeriesIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawCommand> =
    if (laid.nodePositions.keys.any { it.value.startsWith("timeline:") }) {
        buildTimelineFrame(ir, laid, theme)
    } else {
        buildGanttFrame(ir, laid, theme)
    }

private fun buildGanttFrame(
    ir: TimeSeriesIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawCommand> {
    val out = ArrayList<DrawCommand>()
    val colors = ThemeResolver.resolveTimeSeries(theme)
    val titleFont = theme.typography.titleFont.copy(sizeSp = 14f)
    val trackFont = theme.typography.bodyFont.copy(sizeSp = 12f, weight = 600)
    val itemFont = theme.typography.bodyFont.copy(sizeSp = 12f)
    val axisFont = theme.typography.bodyFont.copy(sizeSp = 11f)

    laid.nodePositions[NodeId("gantt:title")]?.let { titleRect ->
        ir.title?.takeIf { it.isNotBlank() }?.let { title ->
            out += DrawCommand.DrawText(
                text = title,
                origin = Point(titleRect.left, titleRect.top),
                font = titleFont,
                color = colors.titleText,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 10,
                measuredBounds = titleRect,
            )
        }
    }
    laid.nodePositions[NodeId("gantt:axis")]?.let { axis ->
        out += DrawCommand.StrokeRect(rect = axis, stroke = Stroke(width = 1f), color = colors.axis, z = 1)
    }

    for ((trackIndex, track) in ir.tracks.withIndex()) {
        val headerRect = laid.nodePositions[NodeId("gantt:track:${track.id.value}")]
        headerRect?.let {
            out += DrawCommand.DrawText(
                text = track.label.plainText(track.id.value),
                origin = Point(it.left, it.top),
                font = trackFont,
                color = colors.labelText,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 10,
                measuredBounds = it,
            )
        }

        val items = ir.items.filter { it.trackId == track.id && it.payload["gantt.kind"] != "vert" }
        for ((itemIndex, item) in items.withIndex()) {
            val barRect = laid.nodePositions[NodeId("gantt:item:${item.id.value}")] ?: continue
            val labelRect = laid.nodePositions[NodeId("gantt:itemLabel:${item.id.value}")]
            if ((trackIndex + itemIndex) % 2 == 1) {
                out += DrawCommand.FillRect(
                    rect = Rect.ltrb((labelRect?.left ?: barRect.left) - 6f, min(labelRect?.top ?: barRect.top, barRect.top) - 4f, laid.nodePositions[NodeId("gantt:axis")]?.right ?: barRect.right, max(labelRect?.bottom ?: barRect.bottom, barRect.bottom) + 4f),
                    color = colors.alternateRowBackground,
                    z = 0,
                )
            }
            val tags = item.payload["gantt.tags"]?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet().orEmpty()
            val isMilestone = item.payload["gantt.kind"] == "milestone" || ("milestone" in tags)
            val fill = when {
                "crit" in tags -> colors.criticalFill
                "active" in tags -> colors.activeFill
                "done" in tags -> colors.doneFill
                isMilestone -> colors.milestoneFill
                else -> colors.normalFill
            }
            val corner = if (isMilestone) min(barRect.size.width, barRect.size.height) / 2f else 6f
            out += DrawCommand.FillRect(rect = barRect, color = fill, corner = corner, z = 2)
            out += DrawCommand.StrokeRect(rect = barRect, stroke = Stroke(width = 1f), color = colors.border, corner = corner, z = 3)
            labelRect?.let {
                out += DrawCommand.DrawText(
                    text = item.label.plainText(item.id.value),
                    origin = Point(it.left, (it.top + it.bottom) / 2f),
                    font = itemFont,
                    color = colors.labelText,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Middle,
                    z = 10,
                    measuredBounds = it,
                )
            }
        }
    }
    return out
}

internal fun renderTimeSeriesEntities(
    ir: TimeSeriesIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawEntity> = oneShotEntities(ir, laid, buildTimeSeriesFrame(ir, laid, theme))

private fun oneShotEntities(
    model: DiagramModel,
    laid: LaidOutDiagram,
    commands: List<DrawCommand>,
): List<DrawEntity> {
    val out = FrameEntityRenderer.sink(prefix = model.sourceLanguage.entityPrefix, model = model, laidOut = laid)
    out.addAll(commands)
    return out.entities()
}

private val SourceLanguage.entityPrefix: String
    get() = when (this) {
        SourceLanguage.MERMAID -> "mermaid"
        SourceLanguage.PLANTUML -> "plantuml"
        SourceLanguage.DOT -> "dot"
    }

private fun buildTimelineFrame(
    ir: TimeSeriesIR,
    laid: LaidOutDiagram,
    theme: DiagramTheme,
): List<DrawCommand> {
    val out = ArrayList<DrawCommand>()
    val colors = ThemeResolver.resolveTimeSeries(theme)
    val titleFont = theme.typography.titleFont.copy(sizeSp = 14f)
    val headerFont = theme.typography.bodyFont.copy(sizeSp = 12f, weight = 600)
    val itemFont = theme.typography.bodyFont.copy(sizeSp = 12f)

    laid.nodePositions[NodeId("timeline:title")]?.let { rect ->
        ir.title?.takeIf { it.isNotBlank() }?.let { title ->
            out += DrawCommand.DrawText(
                text = title,
                origin = Point(rect.left, rect.top),
                font = titleFont,
                color = colors.titleText,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 10,
                measuredBounds = rect,
            )
        }
    }

    for (track in ir.tracks) {
        laid.nodePositions[NodeId("timeline:track:${track.id.value}")]?.let { rect ->
            out += DrawCommand.DrawText(
                text = track.label.plainText(track.id.value),
                origin = Point(rect.left, rect.top),
                font = headerFont,
                color = colors.labelText,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 10,
                measuredBounds = rect,
            )
        }
        laid.nodePositions[NodeId("timeline:trackBox:${track.id.value}")]?.let { rect ->
            out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = 1f), color = colors.slotStroke, corner = 8f, z = 1)
        }
    }

    for ((id, rect) in laid.nodePositions) {
        when {
            id.value.startsWith("timeline:slot:") -> {
                out += DrawCommand.FillRect(rect = rect, color = colors.slotFill, corner = 8f, z = 1)
                out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = 1f), color = colors.slotStroke, corner = 8f, z = 2)
            }
            id.value.startsWith("timeline:item:") -> {
                out += DrawCommand.FillRect(rect = rect, color = colors.itemFill, corner = 6f, z = 3)
                out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = 1f), color = colors.itemStroke, corner = 6f, z = 4)
                val itemId = id.value.removePrefix("timeline:item:")
                val item = ir.items.firstOrNull { it.id.value == itemId } ?: continue
                out += DrawCommand.DrawText(
                    text = item.payload["event"] ?: item.label.plainText(item.id.value),
                    origin = Point(rect.left + 8f, rect.top + 8f),
                    font = itemFont,
                    color = colors.labelText,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    maxWidth = rect.size.width - 16f,
                    z = 5,
                    measuredBounds = rect,
                )
            }
        }
    }
    return out
}

private fun wedgePath(center: Point, radius: Float, start: Double, end: Double): PathCmd {
    val ops = ArrayList<PathOp>()
    ops += PathOp.MoveTo(center)
    ops += PathOp.LineTo(polar(center, radius, start))
    arcCubic(ops, center, radius, start, end)
    ops += PathOp.Close
    return PathCmd(ops)
}

private fun polar(center: Point, r: Float, a: Double): Point =
    Point((center.x + r * cos(a)).toFloat(), (center.y + r * sin(a)).toFloat())

private fun arcCubic(ops: MutableList<PathOp>, center: Point, r: Float, start: Double, end: Double) {
    var a0 = start
    val dir = if (end >= start) 1.0 else -1.0
    var remaining = end - start
    while (dir * remaining > 1e-6) {
        val step = dir * min(dir * remaining, PI / 2.0)
        val a1 = a0 + step
        cubicArcSegment(ops, center, r, a0, a1)
        a0 = a1
        remaining = end - a0
    }
}

private fun cubicArcSegment(ops: MutableList<PathOp>, center: Point, r: Float, a0: Double, a1: Double) {
    val theta = a1 - a0
    val k = (4.0 / 3.0) * tan(theta / 4.0)
    val p0 = polar(center, r, a0)
    val p3 = polar(center, r, a1)
    val dx0 = (-sin(a0) * k * r).toFloat()
    val dy0 = (cos(a0) * k * r).toFloat()
    val dx1 = (sin(a1) * k * r).toFloat()
    val dy1 = (-cos(a1) * k * r).toFloat()
    ops += PathOp.CubicTo(Point(p0.x + dx0, p0.y + dy0), Point(p3.x + dx1, p3.y + dy1), p3)
}

private fun filledArrowHead(from: Point, to: Point, color: Color): DrawCommand {
    val (p1, p2) = headPoints(from, to, 8f)
    return DrawCommand.FillPath(
        path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(p1), PathOp.LineTo(p2), PathOp.Close)),
        color = color,
        z = 2,
    )
}

private fun openArrowHead(from: Point, to: Point, color: Color): DrawCommand {
    val (p1, p2) = headPoints(from, to, 8f)
    return DrawCommand.StrokePath(
        path = PathCmd(listOf(PathOp.MoveTo(p1), PathOp.LineTo(to), PathOp.LineTo(p2))),
        stroke = Stroke(width = 1.5f),
        color = color,
        z = 2,
    )
}

private fun xMark(at: Point, color: Color): DrawCommand {
    val s = 5f
    return DrawCommand.StrokePath(
        path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(at.x - s, at.y - s)),
                PathOp.LineTo(Point(at.x + s, at.y + s)),
                PathOp.MoveTo(Point(at.x - s, at.y + s)),
                PathOp.LineTo(Point(at.x + s, at.y - s)),
            ),
        ),
        stroke = Stroke(width = 1.5f),
        color = color,
        z = 2,
    )
}

private fun headPoints(from: Point, to: Point, size: Float): Pair<Point, Point> {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return Point(to.x, to.y) to Point(to.x, to.y)
    val ux = dx / len
    val uy = dy / len
    val baseX = to.x - ux * size
    val baseY = to.y - uy * size
    val nx = -uy
    val ny = ux
    return Point(baseX + nx * size * 0.5f, baseY + ny * size * 0.5f) to Point(baseX - nx * size * 0.5f, baseY - ny * size * 0.5f)
}

private fun RichLabel.plainText(fallback: String): String =
    (this as? RichLabel.Plain)?.text?.takeIf { it.isNotBlank() } ?: fallback

private fun formatNumber(value: Double): String =
    if (abs(value % 1.0) < 1e-9) value.toInt().toString() else value.toString()
