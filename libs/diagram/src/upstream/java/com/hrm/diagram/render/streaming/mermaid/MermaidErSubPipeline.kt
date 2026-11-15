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
import com.hrm.diagram.core.ir.ArrowEnds
import com.hrm.diagram.core.ir.Edge
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.text.TextMetrics
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.parser.mermaid.MermaidErParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.graph.GraphMeasurePolicy
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.streaming.kernel.GraphPipelineProfile
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

/** Sub-pipeline for Mermaid `erDiagram` sources (Phase 1 subset). */
internal class MermaidErSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidErParser()
    private val colors = ThemeResolver.resolveGraph(theme)
    private val themeColors = theme.colors
    private val entityFont = FontSpec(family = "sans-serif", sizeSp = 13f, weight = 600)
    private val attributeFont = FontSpec(family = "sans-serif", sizeSp = 12f)
    private val flagFont = FontSpec(family = "sans-serif", sizeSp = 10f, weight = 600)
    private val relationFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val nodeMetrics: MutableMap<NodeId, TextMetrics> = HashMap()
    private val attrBadge: MutableMap<NodeId, BadgeLayout?> = HashMap()
    private val relBadge: MutableMap<Int, RelationshipBadgeLayout?> = HashMap()
    private val entityEmbedded: MutableMap<NodeId, EntityEmbeddedLayout?> = HashMap()
    private var currentAttrEdgesByEntity: Map<NodeId, List<Node>> = emptyMap()
    private var currentIsFinal: Boolean = false
    private val measurePolicy = GraphMeasurePolicy(
        textMeasurer = textMeasurer,
        defaultSize = Size(140f, 56f),
        maxWidth = 260f,
        minWidth = 104f,
        minHeight = 48f,
        labelOf = ::labelTextOf,
        fontOf = ::fontForNode,
        customSizeOf = { node ->
            val (size, metrics) = measureNode(node, isFinal = currentIsFinal, attrs = currentAttrEdgesByEntity[node.id].orEmpty())
            nodeMetrics[node.id] = metrics
            size
        },
    )
    private val profile = GraphPipelineProfile(
        sourceLanguage = SourceLanguage.MERMAID,
        defaultNodeSize = Size(140f, 56f),
        measurePolicy = measurePolicy,
    )
    private val kernel = profile.kernel(
        textMeasurer = textMeasurer,
        renderEntities = { graph, laid -> renderDraw(graph, laid, isFinal = currentIsFinal) },
    )
    private val graphPipeline = MermaidGraphSubPipelineKernel(
        acceptLine = parser::acceptLine,
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        graphKernel = kernel,
        beforeAdvance = ::prepareErLayout,
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

    private fun prepareErLayout(ir: GraphIR, isFinal: Boolean) {
        val needRemeasure = isFinal
        currentIsFinal = isFinal

        // Precompute attribute groupings for embedded rendering.
        val attrById: Map<NodeId, Node> = ir.nodes
            .asSequence()
            .filter { isAttributeNode(it) }
            .associateBy { it.id }
        val attrEdgesByEntity: Map<NodeId, List<Node>> = ir.edges
            .asSequence()
            .withIndex()
            .filter { it.value.label == null } // attribute helper edges
            .mapNotNull { (_, e) -> attrById[e.to]?.let { e.from to it } }
            .groupBy({ it.first }, { it.second })
        currentAttrEdgesByEntity = attrEdgesByEntity

        // Precompute badge layouts (layout-stage measurement) so render never measures.
        // - Attribute flag badge (PK/FK/UK)
        // - Relationship badge (cardinality + optional relation name)
        // These are safe to recompute on finish; otherwise only compute missing entries.
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
                text = flags.joinToString(" / "),
                metrics = textMeasurer.measure(flags.joinToString(" / "), flagFont),
            )
        }
        for ((idx, e) in ir.edges.withIndex()) {
            if (e.label == null) continue
            if (!needRemeasure && idx in relBadge) continue
            relBadge[idx] = relationshipBadgeLayoutOf(e)
        }
    }

    override fun dispose() {
        nodeMetrics.clear()
        attrBadge.clear()
        relBadge.clear()
        entityEmbedded.clear()
        currentAttrEdgesByEntity = emptyMap()
        currentIsFinal = false
        graphPipeline.clear()
    }

    private fun labelTextOf(n: Node): String =
        (n.label as? RichLabel.Plain)?.text?.takeIf { it.isNotEmpty() } ?: n.id.value

    private fun measureNode(
        n: Node,
        isFinal: Boolean,
        attrs: List<Node>,
    ): Pair<Size, TextMetrics> {
        val text = labelTextOf(n)
        val maxWrap = 260f
        val raw = textMeasurer.measure(text, fontForNode(n), maxWidth = maxWrap)

        if (isFinal && !isAttributeNode(n) && attrs.isNotEmpty()) {
            val embedded = buildEmbeddedLayout(entityLabel = text, entityMetrics = raw, attrs = attrs, maxWrap = maxWrap)
            entityEmbedded[n.id] = embedded
            return embedded.size to raw
        }

        val (padX, padY) = when {
            isAttributeNode(n) -> 22f to 14f
            n.shape is NodeShape.RoundedBox -> 18f to 12f
            else -> 18f to 14f
        }
        val minW = if (isAttributeNode(n)) 120f else 104f
        val minH = if (isAttributeNode(n)) 44f else 48f
        val w = (raw.width + 2 * padX).coerceAtLeast(minW)
        val h = (raw.height + 2 * padY).coerceAtLeast(minH)
        return Size(w, h) to raw
    }

    private fun renderDraw(ir: GraphIR, laidOut: LaidOutDiagram, isFinal: Boolean): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laidOut)
        val fallbackEntityFill = colors.nodeFill
        val fallbackEntityStroke = colors.nodeStroke
        val fallbackEntityText = colors.nodeText
        val relationLabelText = colors.edgeLabelText
        val fallbackRelationLabelBg = colors.edgeLabelBackground
        val attributeLinkStroke = Stroke(width = 1f, dash = listOf(5f, 5f))
        val relationStroke = Stroke(width = 1.5f)
        val relationBadgePadX = 6f
        val relationBadgePadY = 4f

        for (n in ir.nodes) {
            val embedAttributes = isFinal
            if (embedAttributes && isAttributeNode(n)) continue
            val r = laidOut.nodePositions[n.id] ?: continue
            val fill = colorOf(n.style.fill, attributeFill(attributeFlagsOf(n), fallbackEntityFill))
            val strokeColor = colorOf(n.style.stroke, attributeStroke(attributeFlagsOf(n), fallbackEntityStroke))
            val textColor = colorOf(n.style.textColor, attributeText(attributeFlagsOf(n), fallbackEntityText))
            val strokeWidth = n.style.strokeWidth ?: if (isAttributeNode(n)) 1.25f else 1.5f
            val stroke = Stroke(width = strokeWidth)
            val corner = when {
                isAttributeNode(n) -> minOf(r.size.height / 2f, 16f)
                n.shape is NodeShape.RoundedBox -> 12f
                else -> 4f
            }
            out += DrawCommand.FillRect(rect = r, color = fill, corner = corner, z = 1)
            out += DrawCommand.StrokeRect(rect = r, stroke = stroke, color = strokeColor, corner = corner, z = 2)
            val cx = (r.left + r.right) / 2f
            val cy = (r.top + r.bottom) / 2f
            val embedded = if (isFinal) entityEmbedded[n.id] else null
            if (embedded != null) {
                drawEntityWithEmbeddedAttributes(out, n, r, fill, strokeColor, textColor, embedded)
            } else if (isAttributeNode(n)) {
                drawAttributeNode(out, n, r, fill, strokeColor, textColor, attrBadge[n.id])
            } else {
                out += DrawCommand.DrawText(
                    text = labelTextOf(n),
                    origin = Point(cx, cy),
                    font = entityFont,
                    color = textColor,
                    maxWidth = r.size.width - 16f,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = 3,
                )
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
            val isAttributeLink = edge.label == null
            if (isFinal && isAttributeLink) continue
            val edgeColor = colorOf(edge.style.color, colors.edge)
            val edgeStroke = if (isAttributeLink) {
                attributeLinkStroke
            } else {
                Stroke(width = edge.style.width ?: relationStroke.width, dash = edge.style.dash)
            }
            out += DrawCommand.StrokePath(path = path, stroke = edgeStroke, color = edgeColor, z = 0)
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

            if (isAttributeLink) continue
            val labelBg = edge.style.labelBg?.let { Color(it.argb) } ?: fallbackRelationLabelBg
            drawRelationshipBadge(
                out = out,
                edge = edge,
                layout = relBadge[idx],
                midPoint = pts[pts.size / 2],
                bgColor = labelBg,
                textColor = relationLabelText,
                borderColor = edgeColor,
                padX = relationBadgePadX,
                padY = relationBadgePadY,
            )
        }

        return out.entities()
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
            val badgeRect = Rect.ltrb(
                rect.left + 8f,
                rect.top + 6f,
                rect.left + 8f + badge.metrics.width + 10f,
                rect.top + 6f + badge.metrics.height + 6f,
            )
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

        val label = labelTextOf(node)
        val type = node.payload[MermaidErParser.ER_ATTRIBUTE_TYPE_KEY]
        val name = label.substringBefore(':').trim()
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

    private fun drawRelationshipBadge(
        out: MutableList<DrawCommand>,
        edge: Edge,
        layout: RelationshipBadgeLayout?,
        midPoint: Point,
        bgColor: Color,
        textColor: Color,
        borderColor: Color,
        padX: Float,
        padY: Float,
    ) {
        val l = layout ?: return
        val width = maxOf(l.cardMetrics.width + 2 * padX, (l.relationMetrics?.width ?: 0f) + 2 * padX)
        val height = l.cardMetrics.height + 2 * padY + (l.relationMetrics?.let { it.height + 2f } ?: 0f)
        val bgRect = Rect.ltrb(
            midPoint.x - width / 2f,
            midPoint.y - height / 2f,
            midPoint.x + width / 2f,
            midPoint.y + height / 2f,
        )
        out += DrawCommand.FillRect(rect = bgRect, color = bgColor, corner = 6f, z = 4)
        out += DrawCommand.StrokeRect(rect = bgRect, stroke = Stroke.Hairline, color = borderColor, corner = 6f, z = 5)
        out += DrawCommand.DrawText(
            text = l.cardinality,
            origin = Point(midPoint.x, bgRect.top + padY),
            font = flagFont,
            color = textColor,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Top,
            z = 6,
        )
        if (l.relationMetrics != null && !l.relation.isNullOrBlank()) {
            out += DrawCommand.DrawText(
                text = l.relation,
                origin = Point(midPoint.x, bgRect.top + padY + l.cardMetrics.height + 2f),
                font = relationFont,
                color = textColor,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Top,
                z = 6,
            )
        }
    }

    private fun relationshipBadgeLayoutOf(edge: Edge): RelationshipBadgeLayout? {
        val raw = (edge.label as? RichLabel.Plain)?.text ?: return null
        if (raw.isBlank()) return null
        val cardinality = raw.substringBefore(' ').trim()
        val relation = raw.substringAfter(' ', "").trim().takeIf { it.isNotEmpty() }
        val cardMetrics = textMeasurer.measure(cardinality, flagFont)
        val relationMetrics = relation?.let { textMeasurer.measure(it, relationFont) }
        return RelationshipBadgeLayout(cardinality, relation, cardMetrics, relationMetrics)
    }

    private fun buildEmbeddedLayout(
        entityLabel: String,
        entityMetrics: TextMetrics,
        attrs: List<Node>,
        maxWrap: Float,
    ): EntityEmbeddedLayout {
        // Layout constants for "entity box with embedded rows".
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
            val type = attr.payload[MermaidErParser.ER_ATTRIBUTE_TYPE_KEY]
            val name = labelTextOf(attr).substringBefore(':').trim()
            val flags = attributeFlagsOf(attr)
            val badge = if (flags.isEmpty()) null else {
                val t = flags.joinToString(" / ")
                BadgeLayout(t, textMeasurer.measure(t, flagFont))
            }
            val rowText = if (!type.isNullOrBlank()) "$name: $type" else name
            val rowMetrics = textMeasurer.measure(rowText, attributeFont, maxWidth = maxWrap)
            EmbeddedAttrRow(
                text = rowText,
                metrics = rowMetrics,
                badge = badge,
            )
        }

        val headerW = entityMetrics.width + 2 * headerPadX
        val headerH = entityMetrics.height + headerPadTop + headerPadBottom
        val bodyW = rows.maxOfOrNull { row ->
            val badgeW = row.badge?.let { it.metrics.width + 2 * badgePadX } ?: 0f
            val contentW = row.metrics.width
            (badgeW.takeIf { it > 0f }?.plus(badgeGap) ?: 0f) + contentW + 2 * bodyPadX
        } ?: (2 * bodyPadX)
        val w = maxOf(headerW, bodyW, 140f)

        val bodyH = if (rows.isEmpty()) 0f else {
            val rowsH = rows.sumOf { it.metrics.height.toDouble() }.toFloat()
            rowsH + (rows.size - 1) * rowGap + topDividerGap + 10f
        }
        val h = maxOf(headerH + bodyH, 56f)

        return EntityEmbeddedLayout(
            size = Size(w, h),
            headerLabel = entityLabel,
            headerMetrics = entityMetrics,
            rows = rows,
            headerPadTop = headerPadTop,
            headerPadBottom = headerPadBottom,
            headerPadX = headerPadX,
            bodyPadX = bodyPadX,
            rowGap = rowGap,
            topDividerGap = topDividerGap,
            badgePadX = badgePadX,
            badgePadY = badgePadY,
            badgeGap = badgeGap,
        )
    }

    private fun drawEntityWithEmbeddedAttributes(
        out: MutableList<DrawCommand>,
        node: Node,
        rect: Rect,
        fill: Color,
        strokeColor: Color,
        textColor: Color,
        embedded: EntityEmbeddedLayout,
    ) {
        // Header: centered at the top section.
        val headerTop = rect.top + embedded.headerPadTop
        val headerCenterY = headerTop + embedded.headerMetrics.height / 2f
        val cx = (rect.left + rect.right) / 2f

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

        // Divider line.
        val dividerY = rect.top + embedded.headerPadTop + embedded.headerMetrics.height + embedded.headerPadBottom / 2f
        out += DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(Point(rect.left + 10f, dividerY)),
                    PathOp.LineTo(Point(rect.right - 10f, dividerY)),
                ),
            ),
            stroke = Stroke.Hairline,
            color = strokeColor,
            z = 3,
        )

        // Body rows.
        var y = dividerY + embedded.topDividerGap
        for (row in embedded.rows) {
            val rowTop = y
            val textTop = rowTop
            var x = rect.left + embedded.bodyPadX
            val badge = row.badge
            if (badge != null) {
                val badgeRect = Rect.ltrb(
                    x,
                    rowTop,
                    x + badge.metrics.width + 2 * embedded.badgePadX,
                    rowTop + badge.metrics.height + 2 * embedded.badgePadY,
                )
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
                origin = Point(x, textTop),
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

    private data class BadgeLayout(
        val text: String,
        val metrics: TextMetrics,
    )

    private data class RelationshipBadgeLayout(
        val cardinality: String,
        val relation: String?,
        val cardMetrics: TextMetrics,
        val relationMetrics: TextMetrics?,
    )

    private data class EmbeddedAttrRow(
        val text: String,
        val metrics: TextMetrics,
        val badge: BadgeLayout?,
    )

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

    private fun fontForNode(node: Node): FontSpec = if (isAttributeNode(node)) attributeFont else entityFont

    private fun isAttributeNode(node: Node): Boolean =
        node.payload[MermaidErParser.ER_KIND_KEY] == MermaidErParser.ER_ATTRIBUTE_KIND

    private fun attributeFlagsOf(node: Node): List<String> =
        node.payload[MermaidErParser.ER_ATTRIBUTE_FLAGS_KEY]
            ?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()

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
        val ux = dx / len; val uy = dy / len
        val size = 8f
        val baseX = to.x - ux * size; val baseY = to.y - uy * size
        val nx = -uy; val ny = ux
        val p1 = Point(baseX + nx * size * 0.5f, baseY + ny * size * 0.5f)
        val p2 = Point(baseX - nx * size * 0.5f, baseY - ny * size * 0.5f)
        val path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(p1), PathOp.LineTo(p2), PathOp.Close))
        return DrawCommand.FillPath(path = path, color = color, z = 1)
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = graphPipeline.drawEntities()

}
