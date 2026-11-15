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
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeStyle
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.StateIR
import com.hrm.diagram.core.ir.StateKind
import com.hrm.diagram.core.ir.StateNode
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.layout.stated.StateDiagramLayout
import com.hrm.diagram.parser.mermaid.MermaidStateParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.cache.DrawEntityKey
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

/** Sub-pipeline for `stateDiagram` / `stateDiagram-v2` Mermaid sources. */
internal class MermaidStateSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {

    private val parser = MermaidStateParser()
    private val colors = ThemeResolver.resolveMermaidState(theme)
    private val layout = StateDiagramLayout(textMeasurer)
    private val styleTransform = MermaidStyleTransformState<StateIR> { model, _ -> model }
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::renderState,
        layoutOptions = { model, isFinal ->
            LayoutOptions(direction = model.styleHints.direction, incremental = !isFinal, allowGlobalReflow = isFinal)
        },
        postLayout = { _, laidOut, seq -> laidOut.copy(seq = seq) },
    )

    override fun updateGraphStyles(styles: MermaidGraphStyleState) {
        // Reuse the same style carrier as GraphIR diagrams: classDef/class/style/::: bindings.
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

    private fun renderState(ir: StateIR, laidOut: LaidOutDiagram): List<DrawEntity> {
        val out = ArrayList<DrawEntity>()
        val boxFill = colors.stateFill
        val boxStroke = colors.stateStroke
        val compositeFill = colors.compositeFill
        val compositeStroke = colors.compositeStroke
        val textColor = colors.text
        val edgeColor = colors.edge
        val noteFill = colors.noteFill
        val noteStroke = colors.noteStroke
        val pseudoFill = colors.pseudoFill

        val solid = Stroke(width = 1.5f)
        val thick = Stroke(width = 4f)
        val nodeFont = FontSpec(family = "sans-serif", sizeSp = 12f)
        val edgeLabelFont = FontSpec(family = "sans-serif", sizeSp = 10f)
        val pseudoFont = FontSpec(family = "sans-serif", sizeSp = 11f)

        val byId = ir.states.associateBy { it.id }
        val styleById = computeStateNodeStyles()

        // Composite states first (lowest z) so children paint on top.
        for (s in ir.states) {
            if (s.kind != StateKind.Composite) continue
            val r = laidOut.nodePositions[s.id] ?: continue
            val commands = ArrayList<DrawCommand>(3)
            val st = styleById[s.id]
            val fill = st?.fill?.let { Color(it.argb) } ?: compositeFill
            val strokeColor = st?.stroke?.let { Color(it.argb) } ?: compositeStroke
            val strokeWidth = st?.strokeWidth ?: solid.width
            commands += DrawCommand.FillRect(rect = r, color = fill, corner = 8f, z = 0)
            commands += DrawCommand.StrokeRect(rect = r, stroke = Stroke(width = strokeWidth), color = strokeColor, corner = 8f, z = 1)
            val title = s.description ?: s.name
            if (title.isNotEmpty()) {
                commands += DrawCommand.DrawText(
                    text = title,
                    origin = Point(r.left + 8f, r.top + 4f),
                    font = nodeFont,
                    color = st?.textColor?.let { Color(it.argb) } ?: textColor,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    z = 2,
                )
            }
            out += DrawEntity(DrawEntityKey.node("mermaid", s.id), commands)
        }

        // Non-composite state shapes (per-kind).
        for (s in ir.states) {
            val r = laidOut.nodePositions[s.id] ?: continue
            val commands = ArrayList<DrawCommand>(4)
            when (s.kind) {
                StateKind.Composite -> { /* already drawn above */ }
                StateKind.Initial -> {
                    val w = r.right - r.left
                    commands += DrawCommand.FillRect(rect = r, color = pseudoFill, corner = w / 2f, z = 4)
                }
                StateKind.Final -> {
                    val w = r.right - r.left
                    commands += DrawCommand.StrokeRect(rect = r, stroke = solid, color = pseudoFill, corner = w / 2f, z = 4)
                    val pad = 4f
                    val inner = Rect.ltrb(r.left + pad, r.top + pad, r.right - pad, r.bottom - pad)
                    val iw = inner.right - inner.left
                    commands += DrawCommand.FillRect(rect = inner, color = pseudoFill, corner = iw / 2f, z = 5)
                }
                StateKind.Choice -> {
                    val cx = (r.left + r.right) / 2f
                    val cy = (r.top + r.bottom) / 2f
                    val path = PathCmd(listOf(
                        PathOp.MoveTo(Point(cx, r.top)),
                        PathOp.LineTo(Point(r.right, cy)),
                        PathOp.LineTo(Point(cx, r.bottom)),
                        PathOp.LineTo(Point(r.left, cy)),
                        PathOp.Close,
                    ))
                    commands += DrawCommand.FillPath(path = path, color = boxFill, z = 4)
                    commands += DrawCommand.StrokePath(path = path, stroke = solid, color = boxStroke, z = 5)
                }
                StateKind.Fork, StateKind.Join -> {
                    commands += DrawCommand.FillRect(rect = r, color = pseudoFill, corner = 2f, z = 4)
                }
                StateKind.History, StateKind.DeepHistory -> {
                    val w = r.right - r.left
                    commands += DrawCommand.FillRect(rect = r, color = boxFill, corner = w / 2f, z = 4)
                    commands += DrawCommand.StrokeRect(rect = r, stroke = solid, color = boxStroke, corner = w / 2f, z = 5)
                    val letter = if (s.kind == StateKind.DeepHistory) "H*" else "H"
                    commands += DrawCommand.DrawText(
                        text = letter,
                        origin = Point((r.left + r.right) / 2f, (r.top + r.bottom) / 2f),
                        font = pseudoFont,
                        color = textColor,
                        anchorX = TextAnchorX.Center,
                        anchorY = TextAnchorY.Middle,
                        z = 6,
                    )
                }
                StateKind.Simple -> {
                    val st = styleById[s.id]
                    val fill = st?.fill?.let { Color(it.argb) } ?: boxFill
                    val strokeColor = st?.stroke?.let { Color(it.argb) } ?: boxStroke
                    val strokeWidth = st?.strokeWidth ?: solid.width
                    val stroke = Stroke(width = strokeWidth)
                    commands += DrawCommand.FillRect(rect = r, color = fill, corner = 8f, z = 4)
                    commands += DrawCommand.StrokeRect(rect = r, stroke = stroke, color = strokeColor, corner = 8f, z = 5)
                    val name = s.description ?: s.name
                    if (name.isNotEmpty()) {
                        commands += DrawCommand.DrawText(
                            text = name,
                            origin = Point((r.left + r.right) / 2f, (r.top + r.bottom) / 2f),
                            font = nodeFont,
                            color = st?.textColor?.let { Color(it.argb) } ?: textColor,
                            anchorX = TextAnchorX.Center,
                            anchorY = TextAnchorY.Middle,
                            z = 6,
                        )
                    }
                }
            }
            if (commands.isNotEmpty()) out += DrawEntity(DrawEntityKey.node("mermaid", s.id), commands)
            // Suppress lint about unused thick stroke when only Fork/Join hit it.
            @Suppress("UNUSED_EXPRESSION") thick
        }

        // Notes.
        for ((id, rect) in laidOut.clusterRects) {
            val v = id.value
            if (!v.startsWith("note#")) continue
            val noteIdx = v.removePrefix("note#").toIntOrNull() ?: continue
            val note = ir.notes.getOrNull(noteIdx) ?: continue
            val commands = ArrayList<DrawCommand>(3)
            commands += DrawCommand.FillRect(rect = rect, color = noteFill, corner = 4f, z = 7)
            commands += DrawCommand.StrokeRect(rect = rect, stroke = solid, color = noteStroke, corner = 4f, z = 8)
            val text = (note.text as? RichLabel.Plain)?.text ?: ""
            if (text.isNotEmpty()) {
                commands += DrawCommand.DrawText(
                    text = text,
                    origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                    font = nodeFont,
                    color = textColor,
                    maxWidth = rect.right - rect.left - 8f,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = 9,
                )
            }
            out += DrawEntity(DrawEntityKey.cluster("mermaid", id), commands)
        }

        // Transitions / edges.
        for ((idx, route) in laidOut.edgeRoutes.withIndex()) {
            val tr = ir.transitions.getOrNull(idx) ?: continue
            val pts = route.points
            if (pts.size < 2) continue
            val from = pts.first()
            val to = pts.last()
            val path = if (route.kind == RouteKind.Bezier && pts.size >= 4 && (pts.size - 1) % 3 == 0) {
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
            val commands = ArrayList<DrawCommand>(3)
            commands += DrawCommand.StrokePath(path = path, stroke = solid, color = edgeColor, z = 3)
            val tangentFrom = if (route.kind == RouteKind.Bezier && pts.size >= 4) pts[pts.size - 2] else from
            commands += openArrowHead(tangentFrom, to, edgeColor)

            val labelText = (tr.label as? RichLabel.Plain)?.text ?: ""
            if (labelText.isNotEmpty()) {
                val (mx, my) = if (route.kind == RouteKind.Bezier && pts.size >= 4) {
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
            out += DrawEntity(DrawEntityKey.edge("mermaid", tr.from, tr.to, idx), commands)
        }

        // Reference unused parameter to avoid lint.
        @Suppress("UNUSED_VARIABLE") val unused = byId
        return out
    }

    private fun computeStateNodeStyles(): Map<NodeId, NodeStyle> {
        val out = HashMap<NodeId, NodeStyle>()
        for (s in parser.snapshot().states) {
            styleTransform.nodeStyleFor(s.id)?.let { out[s.id] = it }
        }
        return out
    }


    private fun openArrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val dx = to.x - from.x; val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(to))),
            stroke = Stroke(width = 1f), color = color, z = 4,
        )
        val ux = dx / len; val uy = dy / len
        val size = 8f
        val baseX = to.x - ux * size; val baseY = to.y - uy * size
        val nx = -uy; val ny = ux
        val p1 = Point(baseX + nx * size * 0.5f, baseY + ny * size * 0.5f)
        val p2 = Point(baseX - nx * size * 0.5f, baseY - ny * size * 0.5f)
        return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(p1), PathOp.LineTo(to), PathOp.LineTo(p2))),
            stroke = Stroke(width = 1.5f), color = color, z = 4,
        )
    }

    @Suppress("unused")
    private fun unusedNode() = StateNode(NodeId("_"), "_")
}
