package com.hrm.diagram.render.streaming.plantuml

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
import com.hrm.diagram.core.ir.ArgbColor
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.StateIR
import com.hrm.diagram.core.ir.StateKind
import com.hrm.diagram.core.ir.StateNode
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.RouteKind
import com.hrm.diagram.layout.stated.StateDiagramLayout
import com.hrm.diagram.parser.plantuml.PlantUmlStateParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

internal class PlantUmlStateSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private data class StatePalette(
        val stateFill: ArgbColor?,
        val stateStroke: ArgbColor?,
        val stateText: ArgbColor?,
        val stateFontSize: Float?,
        val stateFontName: String?,
        val stateLineThickness: Float?,
        val stateShadowing: Boolean?,
        val noteFill: ArgbColor?,
        val noteStroke: ArgbColor?,
        val noteText: ArgbColor?,
        val noteFontSize: Float?,
        val noteFontName: String?,
        val noteLineThickness: Float?,
        val noteShadowing: Boolean?,
        val compositeFill: ArgbColor?,
        val compositeStroke: ArgbColor?,
        val edgeColor: ArgbColor?,
    )

    private companion object {
        const val REGION_PREFIX = "__plantuml_state_region__#"
    }

    private val parser = PlantUmlStateParser()
    private val colors = ThemeResolver.resolvePlantUmlState(theme)
    private val shadowTint = PlantUmlTreeRenderSupport.themedShadowTint(theme.colors.border)
    private var cachedPaletteExtras: Map<String, String> = emptyMap()
    private var cachedPalette: StatePalette = paletteOf(emptyMap())
    private val layout = StateDiagramLayout(textMeasurer)
    private val kernel = PlantUmlFamilyRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::renderState,
        layoutOptions = { model, isFinal ->
            LayoutOptions(direction = model.styleHints.direction, incremental = !isFinal, allowGlobalReflow = isFinal)
        },
    )

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun renderState(ir: StateIR, laidOut: LaidOutDiagram): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laidOut)
        val palette = resolvePalette(ir)
        val boxFill = Color((palette.stateFill ?: ArgbColor(colors.stateFill.argb)).argb)
        val boxStroke = Color((palette.stateStroke ?: ArgbColor(colors.stateStroke.argb)).argb)
        val compositeFill = Color((palette.compositeFill ?: palette.stateFill ?: ArgbColor(colors.compositeFill.argb)).argb)
        val compositeStroke = Color((palette.compositeStroke ?: palette.stateStroke ?: ArgbColor(colors.compositeStroke.argb)).argb)
        val textColor = Color((palette.stateText ?: ArgbColor(colors.stateText.argb)).argb)
        val edgeColor = Color((palette.edgeColor ?: ArgbColor(colors.edgeColor.argb)).argb)
        val noteFill = Color((palette.noteFill ?: ArgbColor(colors.noteFill.argb)).argb)
        val noteStroke = Color((palette.noteStroke ?: ArgbColor(colors.noteStroke.argb)).argb)
        val noteTextColor = Color((palette.noteText ?: palette.stateText ?: ArgbColor(colors.noteText.argb)).argb)
        val pseudoFill = colors.pseudoFill

        val solid = Stroke(width = palette.stateLineThickness ?: 1.5f)
        val noteSolid = Stroke(width = palette.noteLineThickness ?: 1.25f)
        val nodeFont = PlantUmlTreeRenderSupport.resolveFontSpec(
            FontSpec(family = "sans-serif", sizeSp = 12f),
            palette.stateFontName,
            palette.stateFontSize?.toString(),
        )
        val noteFont = PlantUmlTreeRenderSupport.resolveFontSpec(
            nodeFont,
            palette.noteFontName,
            palette.noteFontSize?.toString(),
        )
        val edgeLabelFont = nodeFont.copy(sizeSp = (nodeFont.sizeSp - 2f).coerceAtLeast(10f))
        val pseudoFont = nodeFont.copy(sizeSp = (nodeFont.sizeSp - 1f).coerceAtLeast(11f))

        for (s in ir.states) {
            if (s.kind != StateKind.Composite) continue
            if (isRegionState(s)) continue
            val r = laidOut.nodePositions[s.id] ?: continue
            if (palette.stateShadowing == true) {
                out += DrawCommand.FillRect(
                    rect = PlantUmlTreeRenderSupport.offsetRect(r, 4f, 4f),
                    color = shadowTint,
                    corner = 8f,
                    z = 0,
                )
            }
            out += DrawCommand.FillRect(rect = r, color = compositeFill, corner = 8f, z = 0)
            out += DrawCommand.StrokeRect(rect = r, stroke = solid, color = compositeStroke, corner = 8f, z = 1)
            val title = s.description ?: s.name
            if (title.isNotEmpty()) {
                out += DrawCommand.DrawText(
                    text = title,
                    origin = Point(r.left + 8f, r.top + 4f),
                    font = nodeFont,
                    color = compositeStroke,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    z = 2,
                )
            }
        }

        drawRegionSeparators(ir, laidOut, compositeStroke, solid.width, out)

        for (s in ir.states) {
            val r = laidOut.nodePositions[s.id] ?: continue
            when (s.kind) {
                StateKind.Composite -> {}
                StateKind.Initial -> {
                    val w = r.right - r.left
                    if (palette.stateShadowing == true) {
                        out += DrawCommand.FillRect(
                            rect = PlantUmlTreeRenderSupport.offsetRect(r, 4f, 4f),
                            color = shadowTint,
                            corner = w / 2f,
                            z = 4,
                        )
                    }
                    out += DrawCommand.FillRect(rect = r, color = pseudoFill, corner = w / 2f, z = 4)
                }
                StateKind.Final -> {
                    val w = r.right - r.left
                    if (palette.stateShadowing == true) {
                        out += DrawCommand.FillRect(
                            rect = PlantUmlTreeRenderSupport.offsetRect(r, 4f, 4f),
                            color = shadowTint,
                            corner = w / 2f,
                            z = 4,
                        )
                    }
                    out += DrawCommand.StrokeRect(rect = r, stroke = solid, color = pseudoFill, corner = w / 2f, z = 4)
                    val pad = 4f
                    val inner = Rect.ltrb(r.left + pad, r.top + pad, r.right - pad, r.bottom - pad)
                    val iw = inner.right - inner.left
                    out += DrawCommand.FillRect(rect = inner, color = pseudoFill, corner = iw / 2f, z = 5)
                }
                StateKind.Choice -> {
                    val cx = (r.left + r.right) / 2f
                    val cy = (r.top + r.bottom) / 2f
                    val path = PathCmd(
                        listOf(
                            PathOp.MoveTo(Point(cx, r.top)),
                            PathOp.LineTo(Point(r.right, cy)),
                            PathOp.LineTo(Point(cx, r.bottom)),
                            PathOp.LineTo(Point(r.left, cy)),
                            PathOp.Close,
                        ),
                    )
                    if (palette.stateShadowing == true) {
                        out += DrawCommand.FillPath(
                            path = PathCmd(
                                listOf(
                                    PathOp.MoveTo(Point(cx + 4f, r.top + 4f)),
                                    PathOp.LineTo(Point(r.right + 4f, cy + 4f)),
                                    PathOp.LineTo(Point(cx + 4f, r.bottom + 4f)),
                                    PathOp.LineTo(Point(r.left + 4f, cy + 4f)),
                                    PathOp.Close,
                                ),
                            ),
                            color = shadowTint,
                            z = 4,
                        )
                    }
                    out += DrawCommand.FillPath(path = path, color = boxFill, z = 4)
                    out += DrawCommand.StrokePath(path = path, stroke = solid, color = boxStroke, z = 5)
                }
                StateKind.Fork, StateKind.Join -> {
                    if (palette.stateShadowing == true) {
                        out += DrawCommand.FillRect(
                            rect = PlantUmlTreeRenderSupport.offsetRect(r, 4f, 4f),
                            color = shadowTint,
                            corner = 2f,
                            z = 4,
                        )
                    }
                    out += DrawCommand.FillRect(rect = r, color = pseudoFill, corner = 2f, z = 4)
                }
                StateKind.History, StateKind.DeepHistory -> {
                    val w = r.right - r.left
                    if (palette.stateShadowing == true) {
                        out += DrawCommand.FillRect(
                            rect = PlantUmlTreeRenderSupport.offsetRect(r, 4f, 4f),
                            color = shadowTint,
                            corner = w / 2f,
                            z = 4,
                        )
                    }
                    out += DrawCommand.FillRect(rect = r, color = boxFill, corner = w / 2f, z = 4)
                    out += DrawCommand.StrokeRect(rect = r, stroke = solid, color = boxStroke, corner = w / 2f, z = 5)
                    out += DrawCommand.DrawText(
                        text = if (s.kind == StateKind.DeepHistory) "H*" else "H",
                        origin = Point((r.left + r.right) / 2f, (r.top + r.bottom) / 2f),
                        font = pseudoFont,
                        color = textColor,
                        anchorX = TextAnchorX.Center,
                        anchorY = TextAnchorY.Middle,
                        z = 6,
                    )
                }
                StateKind.Simple -> {
                    if (palette.stateShadowing == true) {
                        out += DrawCommand.FillRect(
                            rect = PlantUmlTreeRenderSupport.offsetRect(r, 4f, 4f),
                            color = shadowTint,
                            corner = 8f,
                            z = 4,
                        )
                    }
                    out += DrawCommand.FillRect(rect = r, color = boxFill, corner = 8f, z = 4)
                    out += DrawCommand.StrokeRect(rect = r, stroke = solid, color = boxStroke, corner = 8f, z = 5)
                    val name = s.description ?: s.name
                    if (name.isNotEmpty()) {
                        out += DrawCommand.DrawText(
                            text = name,
                            origin = Point((r.left + r.right) / 2f, (r.top + r.bottom) / 2f),
                            font = nodeFont,
                            color = textColor,
                            anchorX = TextAnchorX.Center,
                            anchorY = TextAnchorY.Middle,
                            z = 6,
                        )
                    }
                }
            }
        }

        for ((id, rect) in laidOut.clusterRects) {
            if (!id.value.startsWith("note#")) continue
            val noteIdx = id.value.removePrefix("note#").toIntOrNull() ?: continue
            val note = ir.notes.getOrNull(noteIdx) ?: continue
            if (palette.noteShadowing == true) {
                out += DrawCommand.FillRect(
                    rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                    color = shadowTint,
                    corner = 4f,
                    z = 7,
                )
            }
            out += DrawCommand.FillRect(rect = rect, color = noteFill, corner = 4f, z = 7)
            out += DrawCommand.StrokeRect(rect = rect, stroke = noteSolid, color = noteStroke, corner = 4f, z = 8)
            val text = (note.text as? RichLabel.Plain)?.text.orEmpty()
            if (text.isNotEmpty()) {
                out += DrawCommand.DrawText(
                    text = text,
                    origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                    font = noteFont,
                    color = noteTextColor,
                    maxWidth = rect.right - rect.left - 8f,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = 9,
                )
            }
        }

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
            out += DrawCommand.StrokePath(path = path, stroke = solid, color = edgeColor, z = 3)
            val tangentFrom = if (route.kind == RouteKind.Bezier && pts.size >= 4) pts[pts.size - 2] else from
            out += openArrowHead(tangentFrom, to, edgeColor, solid.width)
            val labelText = (tr.label as? RichLabel.Plain)?.text.orEmpty()
            if (labelText.isNotEmpty()) {
                val labelPoint = if (route.kind == RouteKind.Bezier && pts.size >= 4) {
                    cubicMidPoint(pts[0], pts[1], pts[2], pts[3])
                } else {
                    Point((from.x + to.x) / 2f, (from.y + to.y) / 2f)
                }
                out += DrawCommand.DrawText(
                    text = labelText,
                    origin = Point(labelPoint.x, labelPoint.y - 4f),
                    font = edgeLabelFont,
                    color = textColor,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Bottom,
                    z = 9,
                )
            }
        }

        return out.entities()
    }

    private fun cubicMidPoint(p0: Point, p1: Point, p2: Point, p3: Point): Point {
        val t = 0.5f
        val mt = 1f - t
        return Point(
            mt * mt * mt * p0.x + 3f * mt * mt * t * p1.x + 3f * mt * t * t * p2.x + t * t * t * p3.x,
            mt * mt * mt * p0.y + 3f * mt * mt * t * p1.y + 3f * mt * t * t * p2.y + t * t * t * p3.y,
        )
    }

    private fun openArrowHead(from: Point, to: Point, color: Color, width: Float): DrawCommand {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val len = sqrt(dx * dx + dy * dy).takeIf { it > 0.0001f } ?: return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(to))),
            stroke = Stroke(width = 1f),
            color = color,
            z = 4,
        )
        val ux = dx / len
        val uy = dy / len
        val size = 8f
        val baseX = to.x - ux * size
        val baseY = to.y - uy * size
        val nx = -uy
        val ny = ux
        val p1 = Point(baseX + nx * size * 0.5f, baseY + ny * size * 0.5f)
        val p2 = Point(baseX - nx * size * 0.5f, baseY - ny * size * 0.5f)
        return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(p1), PathOp.LineTo(to), PathOp.LineTo(p2))),
            stroke = Stroke(width = width),
            color = color,
            z = 4,
        )
    }

    private fun drawRegionSeparators(
        ir: StateIR,
        laidOut: LaidOutDiagram,
        color: Color,
        strokeWidth: Float,
        out: MutableList<DrawCommand>,
    ) {
        val byId = ir.states.associateBy { it.id }
        val stroke = Stroke(width = strokeWidth.coerceAtLeast(1.25f))
        for (state in ir.states) {
            if (state.kind != StateKind.Composite) continue
            val regions = state.children.mapNotNull { childId ->
                val child = byId[childId]
                val rect = laidOut.nodePositions[childId]
                if (child != null && rect != null && isRegionState(child)) childId to rect else null
            }
            if (regions.size < 2) continue
            val parentRect = laidOut.nodePositions[state.id] ?: continue
            for (index in 1 until regions.size) {
                val previous = regions[index - 1].second
                val current = regions[index].second
                val y = (previous.bottom + current.top) / 2f
                out += DrawCommand.StrokePath(
                    path = PathCmd(
                        listOf(
                            PathOp.MoveTo(Point(parentRect.left + 10f, y)),
                            PathOp.LineTo(Point(parentRect.right - 10f, y)),
                        ),
                    ),
                    stroke = stroke,
                    color = color,
                    z = 3,
                )
            }
        }
    }

    private fun resolvePalette(ir: StateIR): StatePalette {
        val extras = ir.styleHints.extras
        if (cachedPaletteExtras != extras) {
            cachedPaletteExtras = extras.toMap()
            cachedPalette = paletteOf(cachedPaletteExtras)
        }
        return cachedPalette
    }

    private fun paletteOf(extras: Map<String, String>): StatePalette {
        fun c(key: String): ArgbColor? =
            extras[key]?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)?.let { ArgbColor(it.argb) }
        fun f(key: String): Float? = PlantUmlTreeRenderSupport.parsePlantUmlFloat(extras[key])
        fun s(key: String): String? = PlantUmlTreeRenderSupport.parsePlantUmlFontFamily(extras[key])
        fun b(key: String): Boolean? = PlantUmlTreeRenderSupport.parsePlantUmlBoolean(extras[key])
        return StatePalette(
            stateFill = c(PlantUmlStateParser.STYLE_STATE_FILL_KEY),
            stateStroke = c(PlantUmlStateParser.STYLE_STATE_STROKE_KEY),
            stateText = c(PlantUmlStateParser.STYLE_STATE_TEXT_KEY),
            stateFontSize = f(PlantUmlStateParser.STYLE_STATE_FONT_SIZE_KEY),
            stateFontName = s(PlantUmlStateParser.STYLE_STATE_FONT_NAME_KEY),
            stateLineThickness = f(PlantUmlStateParser.STYLE_STATE_LINE_THICKNESS_KEY),
            stateShadowing = b(PlantUmlStateParser.STYLE_STATE_SHADOWING_KEY),
            noteFill = c(PlantUmlStateParser.STYLE_NOTE_FILL_KEY),
            noteStroke = c(PlantUmlStateParser.STYLE_NOTE_STROKE_KEY),
            noteText = c(PlantUmlStateParser.STYLE_NOTE_TEXT_KEY),
            noteFontSize = f(PlantUmlStateParser.STYLE_NOTE_FONT_SIZE_KEY),
            noteFontName = s(PlantUmlStateParser.STYLE_NOTE_FONT_NAME_KEY),
            noteLineThickness = f(PlantUmlStateParser.STYLE_NOTE_LINE_THICKNESS_KEY),
            noteShadowing = b(PlantUmlStateParser.STYLE_NOTE_SHADOWING_KEY),
            compositeFill = c(PlantUmlStateParser.STYLE_COMPOSITE_FILL_KEY),
            compositeStroke = c(PlantUmlStateParser.STYLE_COMPOSITE_STROKE_KEY),
            edgeColor = c(PlantUmlStateParser.STYLE_EDGE_COLOR_KEY),
        )
    }

    private fun isRegionState(state: StateNode): Boolean = state.id.value.startsWith(REGION_PREFIX)
}
