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
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatch
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.core.text.TextMetrics
import com.hrm.diagram.layout.IncrementalLayout
import com.hrm.diagram.layout.EdgeRoute
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.layout.sugiyama.SugiyamaLayouts
import com.hrm.diagram.parser.plantuml.PlantUmlErdParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

internal class PlantUmlErdSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private val parser = PlantUmlErdParser()
    private val colors = ThemeResolver.resolvePlantUmlComponent(theme)
    private val themeColors = theme.colors
    private val secondaryText = theme.colors.textSecondary
    private val entityFont = FontSpec(family = "sans-serif", sizeSp = 13f, weight = 600)
    private val attributeFont = FontSpec(family = "sans-serif", sizeSp = 12f)
    private val flagFont = FontSpec(family = "sans-serif", sizeSp = 10f, weight = 600)
    private val relationFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val noteFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val nodeSizes: MutableMap<NodeId, Size> = HashMap()
    private val attrBadge: MutableMap<NodeId, BadgeLayout?> = HashMap()
    private val relBadge: MutableMap<Int, RelationshipBadgeLayout?> = HashMap()
    private val entityEmbedded: MutableMap<NodeId, EntityEmbeddedLayout?> = HashMap()
    private val layout: IncrementalLayout<GraphIR> = SugiyamaLayouts.forGraph(
        defaultNodeSize = Size(140f, 56f),
        nodeSizeOf = { id -> nodeSizes[id] ?: Size(140f, 56f) },
    )
    private val kernel = PlantUmlRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layoutModel = { it },
        beforeLayout = { ir, _, isFinal -> prepareLayout(ir, isFinal) },
        layout = { previous, ir, options, _, _ -> layout.layout(previous, ir, options) },
        layoutOptions = { _, ir, isFinal ->
            LayoutOptions(direction = ir.styleHints.direction, incremental = !isFinal, allowGlobalReflow = isFinal)
        },
        postLayout = { ir, _, laidOut, seq, _ -> applyAnchoredNotes(ir, laidOut).copy(seq = seq) },
        renderEntities = { ir, _, laidOut, isFinal -> renderDraw(ir, laidOut, isFinal) },
    )

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    override fun dispose() {
        nodeSizes.clear()
        attrBadge.clear()
        relBadge.clear()
        entityEmbedded.clear()
    }

    private fun prepareLayout(ir: GraphIR, isFinal: Boolean) {
        val needRemeasure = isFinal
        val attrById = ir.nodes.asSequence().filter { isAttributeNode(it) }.associateBy { it.id }
        val attrEdgesByEntity = ir.edges
            .asSequence()
            .filter { it.label == null }
            .mapNotNull { e -> attrById[e.to]?.let { e.from to it } }
            .groupBy({ it.first }, { it.second })

        if (needRemeasure) {
            attrBadge.clear()
            relBadge.clear()
            entityEmbedded.clear()
        }
        for (n in ir.nodes) {
            if (!isAttributeNode(n)) continue
            if (!needRemeasure && n.id in attrBadge) continue
            val flags = attributeFlagsOf(n)
            attrBadge[n.id] = if (flags.isEmpty()) null else BadgeLayout(
                flags.joinToString(" / "),
                textMeasurer.measure(flags.joinToString(" / "), flagFont),
            )
        }
        for ((idx, e) in ir.edges.withIndex()) {
            if (e.label == null) continue
            if (!needRemeasure && idx in relBadge) continue
            relBadge[idx] = relationshipBadgeLayoutOf(e)
        }
        for (n in ir.nodes) {
            if (!needRemeasure && n.id in nodeSizes) continue
            val size = measureNode(n, isFinal, attrEdgesByEntity[n.id].orEmpty())
            nodeSizes[n.id] = size
        }
    }

    private fun measureNode(n: Node, isFinal: Boolean, attrs: List<Node>): Size {
        val text = labelTextOf(n)
        val maxWrap = 260f
        val raw = textMeasurer.measure(text, fontForNode(n), maxWidth = maxWrap)
        if (isNoteNode(n)) {
            return Size(
                (raw.width + 24f).coerceAtLeast(120f),
                (raw.height + 20f).coerceAtLeast(48f),
            )
        }
        if (isFinal && isEntityNode(n) && attrs.isNotEmpty()) {
            val embedded = buildEmbeddedLayout(text, raw, attrs, maxWrap)
            entityEmbedded[n.id] = embedded
            return embedded.size
        }
        val padX = if (isAttributeNode(n)) 22f else 18f
        val padY = if (isAttributeNode(n)) 14f else 14f
        val minW = if (isAttributeNode(n)) 120f else 104f
        val minH = if (isAttributeNode(n)) 44f else 48f
        return Size((raw.width + 2 * padX).coerceAtLeast(minW), (raw.height + 2 * padY).coerceAtLeast(minH))
    }

    private fun renderDraw(ir: GraphIR, laidOut: LaidOutDiagram, isFinal: Boolean): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laidOut)
        val nodeById = ir.nodes.associateBy { it.id }
        val attributeLinkStroke = Stroke(width = 1f, dash = listOf(5f, 5f))
        for (n in ir.nodes) {
            if (isFinal && isAttributeNode(n)) continue
            val r = laidOut.nodePositions[n.id] ?: continue
            val defaultFill = if (isNoteNode(n)) colors.noteFill else colors.nodeFill
            val defaultStroke = if (isNoteNode(n)) colors.noteStroke else colors.nodeStroke
            val defaultText = if (isNoteNode(n)) colors.noteText else colors.nodeText
            val flags = attributeFlagsOf(n)
            val fill = colorOf(n.style.fill, attributeFill(flags, defaultFill))
            val strokeColor = colorOf(n.style.stroke, attributeStroke(flags, defaultStroke))
            val textColor = colorOf(n.style.textColor, attributeText(flags, defaultText))
            val strokeWidth = n.style.strokeWidth ?: if (isAttributeNode(n)) 1.25f else 1.5f
            val corner = if (isAttributeNode(n)) minOf(r.size.height / 2f, 16f) else 4f
            val embedded = if (isFinal) entityEmbedded[n.id] else null
            when {
                isNoteNode(n) -> drawNoteNode(out, n, r, fill, strokeColor, textColor, strokeWidth)
                embedded != null -> drawEntityWithEmbeddedAttributes(out, r, fill, strokeColor, textColor, embedded)
                isAttributeNode(n) -> {
                    out += DrawCommand.FillRect(rect = r, color = fill, corner = corner, z = 1)
                    out += DrawCommand.StrokeRect(rect = r, stroke = Stroke(width = strokeWidth), color = strokeColor, corner = corner, z = 2)
                    drawAttributeNode(out, n, r, fill, strokeColor, textColor, attrBadge[n.id])
                }
                else -> {
                    out += DrawCommand.FillRect(rect = r, color = fill, corner = corner, z = 1)
                    out += DrawCommand.StrokeRect(rect = r, stroke = Stroke(width = strokeWidth), color = strokeColor, corner = corner, z = 2)
                    out += DrawCommand.DrawText(
                        text = labelTextOf(n),
                        origin = Point((r.left + r.right) / 2f, (r.top + r.bottom) / 2f),
                        font = entityFont,
                        color = textColor,
                        maxWidth = r.size.width - 16f,
                        anchorX = TextAnchorX.Center,
                        anchorY = TextAnchorY.Middle,
                        z = 3,
                    )
                }
            }
        }
        for ((idx, route) in laidOut.edgeRoutes.withIndex()) {
            val pts = route.points
            if (pts.size < 2) continue
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
            val path = PathCmd(ops)
            val edge = ir.edges.getOrNull(idx) ?: continue
            val fromNode = nodeById[edge.from]
            val toNode = nodeById[edge.to]
            val isAttributeLink = edge.label == null && (fromNode?.let(::isAttributeNode) == true || toNode?.let(::isAttributeNode) == true)
            val isNoteLink = edge.label == null && (fromNode?.let(::isNoteNode) == true || toNode?.let(::isNoteNode) == true)
            if (isFinal && isAttributeLink) continue
            val edgeColor = colorOf(edge.style.color, colors.edgeColor)
            val edgeStroke = when {
                isAttributeLink -> attributeLinkStroke
                isNoteLink -> Stroke(width = edge.style.width ?: 1f, dash = edge.style.dash ?: listOf(4f, 4f))
                else -> Stroke(width = edge.style.width ?: 1.5f, dash = edge.style.dash)
            }
            out += DrawCommand.StrokePath(path = path, stroke = edgeStroke, color = edgeColor, z = 0)
            if (!isAttributeLink && !isNoteLink) {
                drawRelationshipEndpointMarkers(out, edge, pts, edgeColor)
            }
            if (edge.arrow != ArrowEnds.None) {
                val endTail = pts[pts.size - 2]
                val endHead = pts.last()
                val startTail = pts[1]
                val startHead = pts[0]
                when (edge.arrow) {
                    ArrowEnds.None -> Unit
                    ArrowEnds.ToOnly -> out += arrowHead(endTail, endHead, edgeColor)
                    ArrowEnds.FromOnly -> out += arrowHead(startTail, startHead, edgeColor)
                    ArrowEnds.Both -> {
                        out += arrowHead(endTail, endHead, edgeColor)
                        out += arrowHead(startTail, startHead, edgeColor)
                    }
                }
            }
            if (isAttributeLink || isNoteLink) continue
            val labelBg = edge.style.labelBg?.let { Color(it.argb) } ?: colors.clusterChipFill
            drawRelationshipBadge(out, relBadge[idx], pts[pts.size / 2], labelBg, secondaryText, edgeColor)
        }
        return out.entities()
    }

    private fun drawRelationshipEndpointMarkers(
        out: MutableList<DrawCommand>,
        edge: Edge,
        pts: List<Point>,
        color: Color,
    ) {
        if (pts.size < 2) return
        val left = edge.payload[PlantUmlErdParser.ER_RELATION_LEFT_KEY].orEmpty()
        val right = edge.payload[PlantUmlErdParser.ER_RELATION_RIGHT_KEY].orEmpty()
        if (left.isNotBlank()) {
            drawEndpointMarker(out, endpoint = pts.first(), inside = pts[1], spec = left, reverse = false, color = color)
        }
        if (right.isNotBlank()) {
            drawEndpointMarker(out, endpoint = pts.last(), inside = pts[pts.size - 2], spec = right, reverse = true, color = color)
        }
    }

    private fun drawEndpointMarker(
        out: MutableList<DrawCommand>,
        endpoint: Point,
        inside: Point,
        spec: String,
        reverse: Boolean,
        color: Color,
    ) {
        val dx = inside.x - endpoint.x
        val dy = inside.y - endpoint.y
        val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
        val ux = dx / len
        val uy = dy / len
        val nx = -uy
        val ny = ux
        var offset = 4f
        val tokens = if (reverse) spec.reversed() else spec
        for (token in tokens) {
            when (token) {
                '|' -> {
                    drawCardinalityBar(out, endpoint, ux, uy, nx, ny, offset, color)
                    offset += 5f
                }
                'o' -> {
                    drawOptionalCircle(out, endpoint, ux, uy, offset + 4f, color)
                    offset += 11f
                }
                '{', '}' -> {
                    drawCrowfoot(out, endpoint, ux, uy, nx, ny, offset, color)
                    offset += 12f
                }
            }
        }
    }

    private fun drawCardinalityBar(
        out: MutableList<DrawCommand>,
        endpoint: Point,
        ux: Float,
        uy: Float,
        nx: Float,
        ny: Float,
        offset: Float,
        color: Color,
    ) {
        val half = 6f
        val base = Point(endpoint.x + ux * offset, endpoint.y + uy * offset)
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(Point(base.x - nx * half, base.y - ny * half)),
                    PathOp.LineTo(Point(base.x + nx * half, base.y + ny * half)),
                ),
            ),
            stroke = Stroke(width = 1.5f),
            color = color,
            z = 2,
        )
    }

    private fun drawOptionalCircle(
        out: MutableList<DrawCommand>,
        endpoint: Point,
        ux: Float,
        uy: Float,
        offset: Float,
        color: Color,
    ) {
        val radius = 4.5f
        val center = Point(endpoint.x + ux * offset, endpoint.y + uy * offset)
        out += DrawCommand.StrokePath(
            path = circlePath(center, radius),
            stroke = Stroke(width = 1.5f),
            color = color,
            z = 2,
        )
    }

    private fun drawCrowfoot(
        out: MutableList<DrawCommand>,
        endpoint: Point,
        ux: Float,
        uy: Float,
        nx: Float,
        ny: Float,
        offset: Float,
        color: Color,
    ) {
        val spread = 7f
        val length = 12f
        val base = Point(endpoint.x + ux * offset, endpoint.y + uy * offset)
        val upper = Point(base.x + ux * length + nx * spread, base.y + uy * length + ny * spread)
        val lower = Point(base.x + ux * length - nx * spread, base.y + uy * length - ny * spread)
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(base),
                    PathOp.LineTo(upper),
                    PathOp.MoveTo(base),
                    PathOp.LineTo(lower),
                ),
            ),
            stroke = Stroke(width = 1.5f),
            color = color,
            z = 2,
        )
    }

    private fun circlePath(center: Point, radius: Float): PathCmd {
        val k = 0.55228475f * radius
        return PathCmd(
            listOf(
                PathOp.MoveTo(Point(center.x + radius, center.y)),
                PathOp.CubicTo(Point(center.x + radius, center.y + k), Point(center.x + k, center.y + radius), Point(center.x, center.y + radius)),
                PathOp.CubicTo(Point(center.x - k, center.y + radius), Point(center.x - radius, center.y + k), Point(center.x - radius, center.y)),
                PathOp.CubicTo(Point(center.x - radius, center.y - k), Point(center.x - k, center.y - radius), Point(center.x, center.y - radius)),
                PathOp.CubicTo(Point(center.x + k, center.y - radius), Point(center.x + radius, center.y - k), Point(center.x + radius, center.y)),
                PathOp.Close,
            ),
        )
    }

    private fun applyAnchoredNotes(ir: GraphIR, laidOut: LaidOutDiagram): LaidOutDiagram {
        val noteNodes = ir.nodes.filter(::isNoteNode)
        if (noteNodes.isEmpty()) return laidOut
        val nodePositions = LinkedHashMap(laidOut.nodePositions)
        val edgeRoutes = laidOut.edgeRoutes.toMutableList()
        for (note in noteNodes) {
            val target = note.payload[PlantUmlErdParser.ER_NOTE_TARGET_KEY]?.let(::NodeId) ?: continue
            val placement = note.payload[PlantUmlErdParser.ER_NOTE_PLACEMENT_KEY].orEmpty()
            val targetRect = nodePositions[target] ?: continue
            val noteRect = nodePositions[note.id] ?: continue
            val anchored = anchoredNoteRect(noteRect.size, targetRect, placement)
            nodePositions[note.id] = anchored
            val edgeIndex = ir.edges.indexOfFirst { it.from == note.id && it.to == target }
            if (edgeIndex >= 0) {
                val route = EdgeRoute(
                    from = note.id,
                    to = target,
                    points = anchoredNoteRoute(anchored, targetRect, placement),
                    kind = RouteKind.Polyline,
                )
                if (edgeIndex < edgeRoutes.size) edgeRoutes[edgeIndex] = route else edgeRoutes += route
            }
        }
        val boundsLeft = minOf(laidOut.bounds.left, nodePositions.values.minOfOrNull { it.left } ?: laidOut.bounds.left)
        val boundsTop = minOf(laidOut.bounds.top, nodePositions.values.minOfOrNull { it.top } ?: laidOut.bounds.top)
        val boundsRight = maxOf(laidOut.bounds.right, nodePositions.values.maxOfOrNull { it.right } ?: laidOut.bounds.right)
        val boundsBottom = maxOf(laidOut.bounds.bottom, nodePositions.values.maxOfOrNull { it.bottom } ?: laidOut.bounds.bottom)
        return laidOut.copy(
            nodePositions = nodePositions,
            edgeRoutes = edgeRoutes,
            bounds = Rect.ltrb(boundsLeft, boundsTop, boundsRight, boundsBottom),
        )
    }

    private fun anchoredNoteRect(size: Size, targetRect: Rect, placement: String): Rect {
        val gap = 18f
        return when (placement.lowercase()) {
            "left" -> Rect(Point(targetRect.left - size.width - gap, targetRect.top + (targetRect.size.height - size.height) / 2f), size)
            "top" -> Rect(Point(targetRect.left + (targetRect.size.width - size.width) / 2f, targetRect.top - size.height - gap), size)
            "bottom" -> Rect(Point(targetRect.left + (targetRect.size.width - size.width) / 2f, targetRect.bottom + gap), size)
            else -> Rect(Point(targetRect.right + gap, targetRect.top + (targetRect.size.height - size.height) / 2f), size)
        }
    }

    private fun anchoredNoteRoute(noteRect: Rect, targetRect: Rect, placement: String): List<Point> {
        return when (placement.lowercase()) {
            "left" -> listOf(
                Point(noteRect.right, (noteRect.top + noteRect.bottom) / 2f),
                Point(targetRect.left, (targetRect.top + targetRect.bottom) / 2f),
            )
            "top" -> listOf(
                Point((noteRect.left + noteRect.right) / 2f, noteRect.bottom),
                Point((targetRect.left + targetRect.right) / 2f, targetRect.top),
            )
            "bottom" -> listOf(
                Point((noteRect.left + noteRect.right) / 2f, noteRect.top),
                Point((targetRect.left + targetRect.right) / 2f, targetRect.bottom),
            )
            else -> listOf(
                Point(noteRect.left, (noteRect.top + noteRect.bottom) / 2f),
                Point(targetRect.right, (targetRect.top + targetRect.bottom) / 2f),
            )
        }
    }

    private fun drawAttributeNode(
        out: MutableList<DrawCommand>,
        node: Node,
        rect: Rect,
        fill: Color,
        strokeColor: Color,
        textColor: Color,
        badge: BadgeLayout?,
    ) {
        if (badge != null) {
            val badgeRect = Rect.ltrb(rect.left + 8f, rect.top + 6f, rect.left + 8f + badge.metrics.width + 10f, rect.top + 6f + badge.metrics.height + 6f)
            out += DrawCommand.FillRect(rect = badgeRect, color = strokeColor, corner = 8f, z = 3)
            out += DrawCommand.DrawText(
                text = badge.text,
                origin = Point((badgeRect.left + badgeRect.right) / 2f, (badgeRect.top + badgeRect.bottom) / 2f),
                font = flagFont,
                color = fill,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 4,
            )
        }
        val name = node.payload[PlantUmlErdParser.ER_ATTRIBUTE_NAME_KEY].orEmpty().ifEmpty { labelTextOf(node).substringBefore(':').trim() }
        val type = node.payload[PlantUmlErdParser.ER_ATTRIBUTE_TYPE_KEY]
        val valueText = buildString {
            append(name)
            if (!type.isNullOrBlank()) {
                append("\n")
                append(type)
            }
        }
        out += DrawCommand.DrawText(
            text = valueText,
            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f + if (badge != null) 6f else 0f),
            font = attributeFont,
            color = textColor,
            maxWidth = rect.size.width - 16f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 4,
        )
    }

    private fun drawNoteNode(
        out: MutableList<DrawCommand>,
        node: Node,
        rect: Rect,
        fill: Color,
        strokeColor: Color,
        textColor: Color,
        strokeWidth: Float,
    ) {
        val fold = minOf(14f, rect.size.width * 0.18f, rect.size.height * 0.28f)
        out += DrawCommand.FillRect(rect = rect, color = fill, corner = 8f, z = 1)
        out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = strokeWidth), color = strokeColor, corner = 8f, z = 2)
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(Point(rect.right - fold, rect.top)),
                    PathOp.LineTo(Point(rect.right - fold, rect.top + fold)),
                    PathOp.LineTo(Point(rect.right, rect.top + fold)),
                ),
            ),
            stroke = Stroke(width = 1f),
            color = strokeColor,
            z = 3,
        )
        out += DrawCommand.DrawText(
            text = labelTextOf(node),
            origin = Point(rect.left + 12f, rect.top + 10f),
            font = noteFont,
            color = textColor,
            maxWidth = rect.size.width - 24f,
            anchorX = TextAnchorX.Start,
            anchorY = TextAnchorY.Top,
            z = 4,
        )
    }

    private fun drawRelationshipBadge(
        out: MutableList<DrawCommand>,
        layout: RelationshipBadgeLayout?,
        midPoint: Point,
        bgColor: Color,
        textColor: Color,
        borderColor: Color,
    ) {
        val l = layout ?: return
        val padX = 6f
        val padY = 4f
        val width = l.metrics.width + 2 * padX
        val height = l.metrics.height + 2 * padY
        val bgRect = Rect.ltrb(midPoint.x - width / 2f, midPoint.y - height / 2f, midPoint.x + width / 2f, midPoint.y + height / 2f)
        out += DrawCommand.FillRect(rect = bgRect, color = bgColor, corner = 6f, z = 4)
        out += DrawCommand.StrokeRect(rect = bgRect, stroke = Stroke.Hairline, color = borderColor, corner = 6f, z = 5)
        out += DrawCommand.DrawText(
            text = l.text,
            origin = midPoint,
            font = relationFont,
            color = textColor,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 6,
        )
    }

    private fun relationshipBadgeLayoutOf(edge: Edge): RelationshipBadgeLayout? {
        val raw = (edge.label as? RichLabel.Plain)?.text ?: return null
        if (raw.isBlank()) return null
        return RelationshipBadgeLayout(
            text = raw,
            metrics = textMeasurer.measure(raw, relationFont),
        )
    }

    private fun buildEmbeddedLayout(entityLabel: String, entityMetrics: TextMetrics, attrs: List<Node>, maxWrap: Float): EntityEmbeddedLayout {
        val headerPadX = 18f
        val headerPadTop = 12f
        val headerPadBottom = 10f
        val bodyPadX = 12f
        val rowGap = 6f
        val topDividerGap = 6f
        val badgePadX = 6f
        val badgePadY = 3f
        val badgeGap = 8f
        val rows = attrs.map { attr ->
            val type = attr.payload[PlantUmlErdParser.ER_ATTRIBUTE_TYPE_KEY]
            val name = attr.payload[PlantUmlErdParser.ER_ATTRIBUTE_NAME_KEY].orEmpty().ifEmpty { labelTextOf(attr).substringBefore(':').trim() }
            val flags = attributeFlagsOf(attr)
            val badge = if (flags.isEmpty()) null else BadgeLayout(flags.joinToString(" / "), textMeasurer.measure(flags.joinToString(" / "), flagFont))
            val rowText = if (!type.isNullOrBlank()) "$name: $type" else name
            EmbeddedAttrRow(rowText, textMeasurer.measure(rowText, attributeFont, maxWidth = maxWrap), badge)
        }
        val headerW = entityMetrics.width + 2 * headerPadX
        val bodyW = rows.maxOfOrNull { row ->
            val badgeW = row.badge?.let { it.metrics.width + 2 * badgePadX } ?: 0f
            val contentW = row.metrics.width
            (badgeW.takeIf { it > 0f }?.plus(badgeGap) ?: 0f) + contentW + 2 * bodyPadX
        } ?: (2 * bodyPadX)
        val w = maxOf(headerW, bodyW, 140f)
        val bodyH = if (rows.isEmpty()) 0f else rows.sumOf { it.metrics.height.toDouble() }.toFloat() + (rows.size - 1) * rowGap + topDividerGap + 10f
        val h = maxOf(entityMetrics.height + headerPadTop + headerPadBottom + bodyH, 56f)
        return EntityEmbeddedLayout(Size(w, h), entityLabel, entityMetrics, rows, headerPadTop, headerPadBottom, headerPadX, bodyPadX, rowGap, topDividerGap, badgePadX, badgePadY, badgeGap)
    }

    private fun drawEntityWithEmbeddedAttributes(
        out: MutableList<DrawCommand>,
        rect: Rect,
        fill: Color,
        strokeColor: Color,
        textColor: Color,
        embedded: EntityEmbeddedLayout,
    ) {
        val cx = (rect.left + rect.right) / 2f
        val headerTop = rect.top + embedded.headerPadTop
        val headerCenterY = headerTop + embedded.headerMetrics.height / 2f
        out += DrawCommand.DrawText(
            text = embedded.headerLabel,
            origin = Point(cx, headerCenterY),
            font = entityFont,
            color = textColor,
            maxWidth = rect.size.width - 2 * embedded.headerPadX,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 3,
        )
        if (embedded.rows.isEmpty()) return
        val dividerY = rect.top + embedded.headerPadTop + embedded.headerMetrics.height + embedded.headerPadBottom / 2f
        out += DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(Point(rect.left + 10f, dividerY)), PathOp.LineTo(Point(rect.right - 10f, dividerY)))),
            stroke = Stroke.Hairline,
            color = strokeColor,
            z = 3,
        )
        var y = dividerY + embedded.topDividerGap
        for (row in embedded.rows) {
            var x = rect.left + embedded.bodyPadX
            row.badge?.let { badge ->
                val badgeRect = Rect.ltrb(x, y, x + badge.metrics.width + 2 * embedded.badgePadX, y + badge.metrics.height + 2 * embedded.badgePadY)
                out += DrawCommand.FillRect(rect = badgeRect, color = strokeColor, corner = 8f, z = 3)
                out += DrawCommand.DrawText(
                    text = badge.text,
                    origin = Point((badgeRect.left + badgeRect.right) / 2f, (badgeRect.top + badgeRect.bottom) / 2f),
                    font = flagFont,
                    color = fill,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = 4,
                )
                x = badgeRect.right + embedded.badgeGap
            }
            out += DrawCommand.DrawText(
                text = row.text,
                origin = Point(x, y),
                font = attributeFont,
                color = textColor,
                maxWidth = rect.right - x - embedded.bodyPadX,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 4,
            )
            y += row.metrics.height + embedded.rowGap
        }
    }

    private fun labelTextOf(n: Node): String {
        return when (val label = n.label) {
            is RichLabel.Plain -> label.text.takeIf { it.isNotEmpty() } ?: n.id.value
            is RichLabel.Markdown -> label.source.takeIf { it.isNotEmpty() } ?: n.id.value
            is RichLabel.Html -> label.html.takeIf { it.isNotEmpty() } ?: n.id.value
        }
    }

    private fun fontForNode(node: Node): FontSpec = if (isAttributeNode(node)) attributeFont else entityFont

    private fun isAttributeNode(node: Node): Boolean =
        node.payload[PlantUmlErdParser.ER_KIND_KEY] == PlantUmlErdParser.ER_ATTRIBUTE_KIND

    private fun isEntityNode(node: Node): Boolean =
        node.payload[PlantUmlErdParser.ER_KIND_KEY] == PlantUmlErdParser.ER_ENTITY_KIND

    private fun isNoteNode(node: Node): Boolean =
        node.payload[PlantUmlErdParser.ER_KIND_KEY] == PlantUmlErdParser.ER_NOTE_KIND

    private fun attributeFlagsOf(node: Node): List<String> =
        node.payload[PlantUmlErdParser.ER_ATTRIBUTE_FLAGS_KEY]?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()

    private fun attributeFill(flags: List<String>, fallback: Color): Color = when {
        "PK" in flags -> alpha(themeColors.warning, 28)
        "FK" in flags -> alpha(themeColors.accent, 28)
        "UK" in flags -> alpha(themeColors.accentSecondary, 28)
        flags.isNotEmpty() -> alpha(themeColors.success, 24)
        else -> fallback
    }

    private fun attributeStroke(flags: List<String>, fallback: Color): Color = when {
        "PK" in flags -> themeColors.warning
        "FK" in flags -> themeColors.accent
        "UK" in flags -> themeColors.accentSecondary
        flags.isNotEmpty() -> themeColors.success
        else -> fallback
    }

    private fun attributeText(flags: List<String>, fallback: Color): Color = if (flags.isEmpty()) fallback else colors.nodeText

    private fun alpha(color: Color, alpha: Int): Color =
        Color.argb(alpha.coerceIn(0, 255), color.r, color.g, color.b)

    private fun colorOf(value: com.hrm.diagram.core.ir.ArgbColor?, fallback: Color): Color =
        value?.let { Color(it.argb) } ?: fallback

    private fun arrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
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
        return DrawCommand.FillPath(path = path, color = color, z = 1)
    }

    private data class BadgeLayout(val text: String, val metrics: TextMetrics)
    private data class RelationshipBadgeLayout(val text: String, val metrics: TextMetrics)
    private data class EmbeddedAttrRow(val text: String, val metrics: TextMetrics, val badge: BadgeLayout?)
    private data class EntityEmbeddedLayout(
        val size: Size,
        val headerLabel: String,
        val headerMetrics: TextMetrics,
        val rows: List<EmbeddedAttrRow>,
        val headerPadTop: Float,
        val headerPadBottom: Float,
        val headerPadX: Float,
        val bodyPadX: Float,
        val rowGap: Float,
        val topDividerGap: Float,
        val badgePadX: Float,
        val badgePadY: Float,
        val badgeGap: Float,
    )
}
