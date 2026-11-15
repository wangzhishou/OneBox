package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.PathCmd
import com.hrm.diagram.core.draw.PathOp
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.core.draw.Stroke
import com.hrm.diagram.core.draw.TextAnchorX
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.ir.ClassIR
import com.hrm.diagram.core.ir.ClassMember
import com.hrm.diagram.core.ir.ClassNode
import com.hrm.diagram.core.ir.ClassRelation
import com.hrm.diagram.core.ir.ClassRelationKind
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeStyle
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.Visibility
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.classd.ClassDiagramLayout
import com.hrm.diagram.parser.mermaid.MermaidClassParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.cache.DrawEntityKey
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

/** Sub-pipeline for `classDiagram` Mermaid sources. */
internal class MermaidClassSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private data class ClassRenderMeasurements(
        val headerHeight: Float,
        val attributeHeights: List<Float>,
        val methodHeights: List<Float>,
    )

    private val parser = MermaidClassParser()
    private val colors = ThemeResolver.resolveMermaidClass(theme)
    private val layout = ClassDiagramLayout(textMeasurer)
    private val headerFont = FontSpec(family = "sans-serif", sizeSp = 13f)
    private val memberFont = FontSpec(family = "sans-serif", sizeSp = 11f)
    private val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 10f)
    private val styleTransform = MermaidStyleTransformState<ClassIR> { model, _ -> model }
    private var renderMeasurements: Map<NodeId, ClassRenderMeasurements> = emptyMap()
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        beforeLayout = ::prepareRenderMeasurements,
        layout = layout::layout,
        renderEntities = ::renderClass,
        layoutOptions = { model, isFinal ->
            LayoutOptions(direction = model.styleHints.direction, incremental = !isFinal, allowGlobalReflow = isFinal)
        },
        postLayout = { _, laidOut, seq -> laidOut.copy(seq = seq) },
    )

    override fun updateGraphStyles(styles: MermaidGraphStyleState) {
        styleTransform.update(styles)
    }

    override fun acceptLines(
        previousSnapshot: com.hrm.diagram.render.streaming.DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): com.hrm.diagram.render.streaming.PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    override fun dispose() {
        kernel.clear()
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    private fun renderClass(ir: ClassIR, laidOut: LaidOutDiagram): List<DrawEntity> {
        val out = ArrayList<DrawEntity>()
        val boxFill = colors.default.fill
        val boxStroke = colors.default.stroke
        val headerFill = colors.default.header
        val textColor = colors.default.text
        val edgeColor = colors.edge
        val noteFill = colors.noteFill
        val noteStroke = colors.noteStroke
        val nsStroke = colors.namespaceStroke

        // Map every class id → its resolved cssClass name (if any). Both inline `:::name`
        // (stored on ClassNode.cssClass) and bulk `cssClass "A,B" styleName` (stored in
        // ClassIR.cssClasses) feed this map; bulk assignments win since they typically
        // appear later in the source.
        val classStyleByName = HashMap<NodeId, String>()
        for (c in ir.classes) c.cssClass?.let { classStyleByName[c.id] = it }
        for (def in ir.cssClasses) {
            for (raw in def.name.split(',')) {
                val key = raw.trim()
                if (key.isNotEmpty()) classStyleByName[NodeId(key)] = def.style
            }
        }
        val resolvedNodeStyleById = computeClassNodeStyles(ir, classStyleByName)

        val solid = Stroke(width = 1.5f)
        val dashed = Stroke(width = 1.5f, dash = listOf(6f, 4f))

        // Padding constants must mirror ClassDiagramLayout.measureClass so layout-computed
        // box dimensions and render-drawn content always agree (no clipped names, no orphan
        // horizontal lines below the box).
        val sectionPad = 8f
        val rowGap = 2f

        // Namespace clusters first (lowest z).
        for ((id, rect) in laidOut.clusterRects) {
            val v = id.value
            if (v.startsWith("ns#")) {
                val commands = ArrayList<DrawCommand>(2)
                commands += DrawCommand.StrokeRect(rect = rect, stroke = dashed, color = nsStroke, corner = 6f, z = 0)
                val title = v.removePrefix("ns#")
                commands += DrawCommand.DrawText(
                    text = title,
                    origin = Point(rect.left + 8f, rect.top + 4f),
                    font = memberFont,
                    color = nsStroke,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    z = 1,
                )
                out += DrawEntity(DrawEntityKey.cluster("mermaid", id), commands)
            }
        }

        // Class boxes. We measure each text fragment with the same TextMeasurer the layout
        // used so divider positions track real glyph heights at any density.
        for (c in ir.classes) {
            val r = laidOut.nodePositions[c.id] ?: continue
            val commands = ArrayList<DrawCommand>(8 + c.members.size)
            val st = resolvedNodeStyleById[c.id]
            val palette = paletteFor(classStyleByName[c.id])
            val cBoxFill = st?.fill?.let { Color(it.argb) } ?: (palette?.fill ?: boxFill)
            val cBoxStroke = st?.stroke?.let { Color(it.argb) } ?: (palette?.stroke ?: boxStroke)
            val cHeaderFill = st?.fill?.let { Color(it.argb) } ?: (palette?.header ?: headerFill)
            val cTextColor = st?.textColor?.let { Color(it.argb) } ?: (palette?.text ?: textColor)
            val strokeWidth = st?.strokeWidth ?: solid.width
            val borderStroke = Stroke(width = strokeWidth)
            val measurements = renderMeasurements[c.id] ?: measureClassSections(c)
            val headerText = buildString {
                c.stereotype?.let { append("«").append(it).append("»\n") }
                append(c.name)
                c.generics?.let { append("~").append(it).append("~") }
            }
            val headerH = measurements.headerHeight

            commands += DrawCommand.FillRect(rect = r, color = cBoxFill, corner = 4f, z = 2)
            commands += DrawCommand.StrokeRect(rect = r, stroke = borderStroke, color = cBoxStroke, corner = 4f, z = 4)

            val headerRect = Rect.ltrb(r.left, r.top, r.right, r.top + headerH)
            commands += DrawCommand.FillRect(rect = headerRect, color = cHeaderFill, corner = 4f, z = 3)

            val cx = (r.left + r.right) / 2f
            commands += DrawCommand.DrawText(
                text = headerText,
                origin = Point(cx, r.top + sectionPad / 2f),
                font = headerFont,
                color = cTextColor,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Top,
                z = 5,
            )

            val attrs = c.members.filter { !it.isMethod }
            val methods = c.members.filter { it.isMethod }
            val hasAttrs = attrs.isNotEmpty()
            val hasMethods = methods.isNotEmpty()

            // Header divider only if there's any body content; otherwise the box stays a
            // single header chip and we don't draw a stray horizontal line.
            if (hasAttrs || hasMethods) {
                val divY1 = r.top + headerH
                commands += DrawCommand.StrokePath(
                    path = PathCmd(listOf(PathOp.MoveTo(Point(r.left, divY1)), PathOp.LineTo(Point(r.right, divY1)))),
                    stroke = borderStroke, color = cBoxStroke, z = 4,
                )
                var y = divY1 + rowGap
                for ((index, a) in attrs.withIndex()) {
                    val line = renderMemberLine(a)
                    commands += DrawCommand.DrawText(
                        text = line,
                        origin = Point(r.left + 6f, y),
                        font = memberFont,
                        color = cTextColor,
                        anchorX = TextAnchorX.Start,
                        anchorY = TextAnchorY.Top,
                        z = 5,
                    )
                    y += measurements.attributeHeights.getOrElse(index) { 0f }
                }
                if (hasAttrs && hasMethods) {
                    commands += DrawCommand.StrokePath(
                        path = PathCmd(listOf(PathOp.MoveTo(Point(r.left, y + rowGap)), PathOp.LineTo(Point(r.right, y + rowGap)))),
                        stroke = borderStroke, color = cBoxStroke, z = 4,
                    )
                    y += rowGap * 2 + 2f
                }
                for ((index, m) in methods.withIndex()) {
                    val line = renderMemberLine(m)
                    commands += DrawCommand.DrawText(
                        text = line,
                        origin = Point(r.left + 6f, y),
                        font = memberFont,
                        color = cTextColor,
                        anchorX = TextAnchorX.Start,
                        anchorY = TextAnchorY.Top,
                        z = 5,
                    )
                    y += measurements.methodHeights.getOrElse(index) { 0f }
                }
            }
            out += DrawEntity("mermaid.class.node.${c.id.value}", commands)
        }

        // Notes.
        for ((id, rect) in laidOut.clusterRects) {
            val v = id.value
            if (!v.startsWith("note#")) continue
            val noteIdx = v.removePrefix("note#").toIntOrNull() ?: continue
            val note = ir.notes.getOrNull(noteIdx) ?: continue
            val commands = ArrayList<DrawCommand>(3)
            commands += DrawCommand.FillRect(rect = rect, color = noteFill, corner = 4f, z = 6)
            commands += DrawCommand.StrokeRect(rect = rect, stroke = solid, color = noteStroke, corner = 4f, z = 7)
            val text = (note.text as? RichLabel.Plain)?.text ?: ""
            if (text.isNotEmpty()) {
                commands += DrawCommand.DrawText(
                    text = text,
                    origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                    font = memberFont,
                    color = textColor,
                    maxWidth = rect.right - rect.left - 8f,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = 8,
                )
            }
            out += DrawEntity(DrawEntityKey.cluster("mermaid", id), commands)
        }

        // Relations / edges.
        for ((idx, route) in laidOut.edgeRoutes.withIndex()) {
            val rel = ir.relations.getOrNull(idx) ?: continue
            val pts = route.points
            if (pts.size < 2) continue
            val from = pts.first()
            val to = pts.last()
            val stroke = if (rel.kind == ClassRelationKind.Dependency || rel.kind == ClassRelationKind.Realization || rel.kind == ClassRelationKind.LinkDashed) dashed else solid
            val path = if (route.kind == com.hrm.diagram.layout.RouteKind.Bezier && pts.size >= 4 && (pts.size - 1) % 3 == 0) {
                val ops = ArrayList<PathOp>(1 + (pts.size - 1) / 3)
                ops += PathOp.MoveTo(pts[0])
                var i = 1
                while (i + 2 <= pts.size - 1) {
                    ops += PathOp.CubicTo(pts[i], pts[i + 1], pts[i + 2])
                    i += 3
                }
                PathCmd(ops)
            } else {
                PathCmd(listOf(PathOp.MoveTo(from), PathOp.LineTo(to)))
            }
            val commands = ArrayList<DrawCommand>(6)
            commands += DrawCommand.StrokePath(path = path, stroke = stroke, color = edgeColor, z = 1)
            // Arrowhead direction uses the last segment's tangent so heads stay aligned with
            // the curve, not with the straight from→to line.
            val tangentFrom = if (route.kind == com.hrm.diagram.layout.RouteKind.Bezier && pts.size >= 4) pts[pts.size - 2] else from
            // Arrowheads at the `to` end.
            when (rel.kind) {
                ClassRelationKind.Inheritance, ClassRelationKind.Realization ->
                    commands += hollowTriangle(tangentFrom, to, edgeColor)
                ClassRelationKind.Composition ->
                    commands += filledDiamond(tangentFrom, to, edgeColor)
                ClassRelationKind.Aggregation ->
                    commands += hollowDiamond(tangentFrom, to, edgeColor)
                ClassRelationKind.Association, ClassRelationKind.Dependency ->
                    commands += openArrowHead(tangentFrom, to, edgeColor)
                ClassRelationKind.Link, ClassRelationKind.LinkDashed -> { /* no head */ }
            }

            // Cardinality labels.
            rel.fromCardinality?.let {
                if (it.isNotEmpty()) commands += cardinalityText(from, pts.getOrElse(1) { to }, it, edgeLabelFont, textColor, atStart = true)
            }
            rel.toCardinality?.let {
                if (it.isNotEmpty()) commands += cardinalityText(tangentFrom, to, it, edgeLabelFont, textColor, atStart = false)
            }
            // Edge label at the geometric midpoint of the curve (or straight line).
            val labelText = (rel.label as? RichLabel.Plain)?.text ?: ""
            if (labelText.isNotEmpty()) {
                val (mx, my) = if (route.kind == com.hrm.diagram.layout.RouteKind.Bezier && pts.size >= 4) {
                    val p0 = pts[0]; val p1 = pts[1]; val p2 = pts[2]; val p3 = pts[3]
                    val t = 0.5f
                    val mt = 1f - t
                    val x = mt * mt * mt * p0.x + 3f * mt * mt * t * p1.x + 3f * mt * t * t * p2.x + t * t * t * p3.x
                    val y = mt * mt * mt * p0.y + 3f * mt * mt * t * p1.y + 3f * mt * t * t * p2.y + t * t * t * p3.y
                    x to y
                } else {
                    ((from.x + to.x) / 2f) to ((from.y + to.y) / 2f)
                }
                commands += DrawCommand.DrawText(
                    text = labelText,
                    origin = Point(mx, my - 4f),
                    font = edgeLabelFont,
                    color = textColor,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Bottom,
                    z = 9,
                )
            }
            out += DrawEntity("mermaid.class.relation.$idx.${rel.from.value}->${rel.to.value}", commands)
        }

        return out
    }

    private fun prepareRenderMeasurements(ir: ClassIR) {
        renderMeasurements = ir.classes.associate { it.id to measureClassSections(it) }
    }

    private fun computeClassNodeStyles(
        ir: ClassIR,
        classStyleByName: Map<NodeId, String>,
    ): Map<NodeId, NodeStyle> {
        val out = HashMap<NodeId, NodeStyle>()
        for (c in ir.classes) {
            val id = c.id
            // `:::someclass` and bulk `cssClass "A,B" someclass` point to a named classDef.
            val classNames = listOfNotNull(classStyleByName[id]?.takeIf { it.isNotBlank() })
            styleTransform.nodeStyleFor(id, classNames)?.let { out[id] = it }
        }
        return out
    }

    private fun measureClassSections(c: ClassNode): ClassRenderMeasurements {
        val sectionPad = 8f
        val attrs = c.members.filter { !it.isMethod }
        val methods = c.members.filter { it.isMethod }
        val headerText = buildString {
            c.stereotype?.let { append("«").append(it).append("»\n") }
            append(c.name)
            c.generics?.let { append("~").append(it).append("~") }
        }
        return ClassRenderMeasurements(
            headerHeight = textMeasurer.measure(headerText, headerFont).height + sectionPad,
            attributeHeights = attrs.map { textMeasurer.measure(renderMemberLine(it), memberFont).height },
            methodHeights = methods.map { textMeasurer.measure(renderMemberLine(it), memberFont).height },
        )
    }

    private fun renderMemberLine(m: ClassMember): String {
        val sb = StringBuilder()
        sb.append(when (m.visibility) {
            Visibility.PUBLIC -> "+"
            Visibility.PRIVATE -> "-"
            Visibility.PROTECTED -> "#"
            Visibility.PACKAGE -> "~"
        })
        sb.append(m.name)
        if (m.isMethod) {
            sb.append('(')
            sb.append(m.params.joinToString(", ") { p -> p.type?.let { "${p.name}: $it" } ?: p.name })
            sb.append(')')
            m.type?.let { sb.append(" : ").append(it) }
        } else {
            m.type?.let { sb.append(" : ").append(it) }
        }
        if (m.isStatic) sb.append('$')
        if (m.isAbstract) sb.append('*')
        return sb.toString()
    }

    private fun cardinalityText(from: Point, to: Point, text: String, font: FontSpec, color: Color, atStart: Boolean): DrawCommand {
        val t = if (atStart) 0.15f else 0.85f
        val x = from.x + (to.x - from.x) * t
        val y = from.y + (to.y - from.y) * t
        return DrawCommand.DrawText(
            text = text,
            origin = Point(x, y - 4f),
            font = font,
            color = color,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Bottom,
            z = 9,
        )
    }

    // ---- Arrowhead helpers ----

    private fun openArrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val (p1, p2) = headPoints(from, to, size = 8f)
        return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(p1), PathOp.LineTo(to), PathOp.LineTo(p2))),
            stroke = Stroke(width = 1.5f), color = color, z = 2,
        )
    }

    private fun hollowTriangle(from: Point, to: Point, color: Color): DrawCommand {
        val (p1, p2) = headPoints(from, to, size = 12f)
        val path = PathCmd(listOf(
            PathOp.MoveTo(to), PathOp.LineTo(p1), PathOp.LineTo(p2), PathOp.Close,
        ))
        return DrawCommand.StrokePath(path = path, stroke = Stroke(width = 1.5f), color = color, z = 2)
    }

    private fun filledDiamond(from: Point, to: Point, color: Color): DrawCommand {
        val path = diamondPath(from, to, size = 10f)
        return DrawCommand.FillPath(path = path, color = color, z = 2)
    }

    private fun hollowDiamond(from: Point, to: Point, color: Color): DrawCommand {
        val path = diamondPath(from, to, size = 10f)
        return DrawCommand.StrokePath(path = path, stroke = Stroke(width = 1.5f), color = color, z = 2)
    }

    private fun diamondPath(from: Point, to: Point, size: Float): PathCmd {
        val dx = to.x - from.x; val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return PathCmd(listOf(PathOp.MoveTo(to), PathOp.Close))
        val ux = dx / len; val uy = dy / len
        val nx = -uy; val ny = ux
        val tip = to
        val mid = Point(to.x - ux * size, to.y - uy * size)
        val side1 = Point(mid.x + nx * size * 0.5f, mid.y + ny * size * 0.5f)
        val side2 = Point(mid.x - nx * size * 0.5f, mid.y - ny * size * 0.5f)
        val tail = Point(to.x - ux * size * 2f, to.y - uy * size * 2f)
        return PathCmd(listOf(
            PathOp.MoveTo(tip),
            PathOp.LineTo(side1),
            PathOp.LineTo(tail),
            PathOp.LineTo(side2),
            PathOp.Close,
        ))
    }

    private fun headPoints(from: Point, to: Point, size: Float): Pair<Point, Point> {
        val dx = to.x - from.x; val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return Point(to.x, to.y) to Point(to.x, to.y)
        val ux = dx / len; val uy = dy / len
        val baseX = to.x - ux * size; val baseY = to.y - uy * size
        val nx = -uy; val ny = ux
        val p1 = Point(baseX + nx * size * 0.5f, baseY + ny * size * 0.5f)
        val p2 = Point(baseX - nx * size * 0.5f, baseY - ny * size * 0.5f)
        return p1 to p2
    }

    @Suppress("unused")
    private fun unusedNodeId() = NodeId("_")

    @Suppress("unused")
    private fun unusedRel() = ClassRelation(NodeId("a"), NodeId("b"), ClassRelationKind.Link)

    /**
     * Resolves a cssClass name to a built-in palette. Names are case-insensitive. Returns null
     * for unknown / null input so the caller falls back to the default amber palette.
     *
     * Built-in names: red, orange, yellow, green, cyan, blue, indigo, purple, pink, gray.
     */
    private fun paletteFor(name: String?): com.hrm.diagram.render.theme.MermaidClassPalette? {
        if (name.isNullOrBlank()) return null
        return colors.cssPalettes[name.lowercase()]
    }
}
