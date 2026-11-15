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
import com.hrm.diagram.core.ir.MessageKind
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SequenceIR
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.sequence.SequenceLayouts
import com.hrm.diagram.parser.plantuml.PlantUmlParsing
import com.hrm.diagram.parser.plantuml.PlantUmlParsingFactory
import com.hrm.diagram.parser.plantuml.PlantUmlSequenceHints
import com.hrm.diagram.parser.plantuml.PlantUmlSequenceParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

internal class PlantUmlSequenceSubPipeline(
    private val textMeasurer: TextMeasurer,
    private val theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private data class MessageDecoration(
        val tail: String? = null,
        val head: String? = null,
        val headStyle: String? = null,
    )

    private data class ScopeStyle(
        val fill: ArgbColor?,
        val stroke: ArgbColor?,
        val text: ArgbColor?,
        val fontSize: Float?,
        val fontName: String?,
        val lineThickness: Float?,
        val shadowing: Boolean?,
    )

    private data class SequencePalette(
        val sequence: ScopeStyle,
        val participant: ScopeStyle,
        val actor: ScopeStyle,
        val boundary: ScopeStyle,
        val control: ScopeStyle,
        val entity: ScopeStyle,
        val database: ScopeStyle,
        val collections: ScopeStyle,
        val queue: ScopeStyle,
        val note: ScopeStyle,
        val box: ScopeStyle,
        val edgeColor: ArgbColor?,
    )

    private val parser: PlantUmlParsing<SequenceIR> = PlantUmlParsingFactory.sequence()
    private val resolvedColors = ThemeResolver.resolvePlantUmlSequence(theme)
    private val shadowTint = PlantUmlTreeRenderSupport.themedShadowTint(theme.colors.border)
    private val layout = SequenceLayouts.forSequence(textMeasurer)
    private val kernel = PlantUmlFamilyRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::renderSequence,
    )

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun renderSequence(ir: SequenceIR, laidOut: LaidOutDiagram): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laidOut)
        val palette = paletteOf(ir)
        val colors = resolvedColors
        val headerFillDefault = colors.headerFill
        val headerStrokeDefault = colors.headerStroke
        val headerTextDefault = colors.headerText
        val lifelineColor = palette.sequence.stroke?.let { Color(it.argb) } ?: palette.edgeColorOrNull() ?: colors.lifeline
        val msgColor = palette.edgeColorOrNull() ?: palette.sequence.stroke?.let { Color(it.argb) } ?: colors.message
        val msgText = palette.sequence.text?.let { Color(it.argb) } ?: colors.messageText
        val noteFill = palette.note.fill?.let { Color(it.argb) } ?: colors.noteFill
        val noteStroke = palette.note.stroke?.let { Color(it.argb) } ?: colors.noteStroke
        val noteText = palette.note.text?.let { Color(it.argb) } ?: colors.noteText
        val activationFill = colors.activationFill
        val activationStroke = palette.sequence.stroke?.let { Color(it.argb) } ?: colors.activationStroke
        val fragStroke = palette.sequence.stroke?.let { Color(it.argb) } ?: colors.fragmentStroke
        val decorations = parseDecorations(ir)

        val messageStrokeWidth = palette.sequence.lineThickness ?: 1.5f
        val headerStrokeStyle = Stroke(width = palette.participant.lineThickness ?: 1.5f)
        val solidStroke = Stroke(width = messageStrokeWidth)
        val dashedStroke = Stroke(width = messageStrokeWidth, dash = listOf(6f, 4f))
        val msgFont = scopedFont(palette.sequence, FontSpec(family = "sans-serif", sizeSp = 11f))
        val noteFont = scopedFont(palette.note, msgFont)
        val boxFont = scopedFont(palette.box, FontSpec(family = "sans-serif", sizeSp = 11f, weight = 600))
        val bottomY = laidOut.bounds.bottom

        palette.sequence.fill?.let {
            out += DrawCommand.FillRect(rect = laidOut.bounds, color = Color(it.argb), z = -5)
        }
        drawBoxes(ir, laidOut, out, palette, boxFont, colors)

        for (p in ir.participants) {
            val r = laidOut.nodePositions[p.id] ?: continue
            val scope = participantScopeFor(p.kind.name.lowercase(), palette)
            val headerFill = scope.fill?.let { Color(it.argb) } ?: headerFillDefault
            val headerStroke = scope.stroke?.let { Color(it.argb) } ?: headerStrokeDefault
            val headerText = scope.text?.let { Color(it.argb) } ?: headerTextDefault
            val participantFont = scopedFont(scope, FontSpec(family = "sans-serif", sizeSp = 13f))
            val participantStroke = Stroke(width = scope.lineThickness ?: palette.participant.lineThickness ?: headerStrokeStyle.width)
            if (scope.shadowing == true) {
                out += DrawCommand.FillRect(
                    rect = PlantUmlTreeRenderSupport.offsetRect(r, 4f, 4f),
                    color = shadowTint,
                    corner = 6f,
                    z = 1,
                )
            }
            out += DrawCommand.FillRect(rect = r, color = headerFill, corner = 6f, z = 2)
            out += DrawCommand.StrokeRect(rect = r, stroke = participantStroke, color = headerStroke, corner = 6f, z = 3)
            val text = (p.label as? RichLabel.Plain)?.text?.takeIf { it.isNotEmpty() } ?: p.id.value
            val cx = (r.left + r.right) / 2f
            val cy = (r.top + r.bottom) / 2f
            out += DrawCommand.DrawText(
                text = text,
                origin = Point(cx, cy),
                font = participantFont,
                color = headerText,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 4,
            )
            out += DrawCommand.StrokePath(
                path = PathCmd(listOf(PathOp.MoveTo(Point(cx, r.bottom)), PathOp.LineTo(Point(cx, bottomY)))),
                stroke = dashedStroke,
                color = lifelineColor,
                z = 0,
            )
        }

        val noteMessages = ir.messages.filter { it.kind == MessageKind.Note && !(it.activate || it.deactivate) }
        var noteStyleIndex = 0
        for ((id, rect) in laidOut.clusterRects) {
            when {
                id.value.contains("#act#") -> {
                    if (palette.sequence.shadowing == true) {
                        out += DrawCommand.FillRect(
                            rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                            color = shadowTint,
                            corner = 0f,
                            z = 4,
                        )
                    }
                    out += DrawCommand.FillRect(rect = rect, color = activationFill, corner = 0f, z = 5)
                    out += DrawCommand.StrokeRect(rect = rect, stroke = solidStroke, color = activationStroke, corner = 0f, z = 6)
                }
                id.value.startsWith("note#") -> {
                    val noteMessage = noteMessages.getOrNull(noteStyleIndex++)
                    val isRef = (noteMessage?.label as? RichLabel.Plain)?.text?.let(PlantUmlSequenceHints::isReferenceLabel) == true
                    val fill = palette.note.fill?.let { Color(it.argb) } ?: if (isRef) colors.referenceNoteFill else noteFill
                    val stroke = palette.note.stroke?.let { Color(it.argb) } ?: if (isRef) colors.referenceNoteStroke else noteStroke
                    if (palette.note.shadowing == true) {
                        out += DrawCommand.FillRect(
                            rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                            color = shadowTint,
                            corner = 4f,
                            z = 6,
                        )
                    }
                    out += DrawCommand.FillRect(rect = rect, color = fill, corner = 4f, z = 7)
                    out += DrawCommand.StrokeRect(
                        rect = rect,
                        stroke = Stroke(width = palette.note.lineThickness ?: messageStrokeWidth),
                        color = stroke,
                        corner = 4f,
                        z = 8,
                    )
                }
                id.value.startsWith("frag#") -> {
                    if (palette.sequence.shadowing == true) {
                        out += DrawCommand.FillRect(
                            rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                            color = shadowTint,
                            corner = 4f,
                            z = 8,
                        )
                    }
                    out += DrawCommand.StrokeRect(rect = rect, stroke = solidStroke, color = fragStroke, corner = 4f, z = 9)
                }
            }
        }

        val noteRectsByIndex = HashMap<Int, Rect>()
        var noteIndex = 0
        for ((id, rect) in laidOut.clusterRects) {
            if (id.value.startsWith("note#")) noteRectsByIndex[noteIndex++] = rect
        }

        var edgeIndex = 0
        var noteDrawIndex = 0
        for ((messageIndex, msg) in ir.messages.withIndex()) {
            when (msg.kind) {
                MessageKind.Note -> {
                    if (msg.activate || msg.deactivate) continue
                    val rect = noteRectsByIndex[noteDrawIndex++] ?: continue
                    val rawText = (msg.label as? RichLabel.Plain)?.text.orEmpty()
                    val labelText = PlantUmlSequenceHints.stripReferenceLabel(rawText)
                    if (labelText.isNotEmpty()) {
                        out += DrawCommand.DrawText(
                            text = labelText,
                            origin = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f),
                            font = noteFont,
                            color = noteText,
                            anchorX = TextAnchorX.Center,
                            anchorY = TextAnchorY.Middle,
                            z = 10,
                        )
                    }
                }
                else -> {
                    val route = laidOut.edgeRoutes.getOrNull(edgeIndex++) ?: continue
                    val from = route.points.first()
                    val to = route.points.last()
                    val decoration = decorations[messageIndex]
                    val stroke = if (msg.kind == MessageKind.Reply) dashedStroke else solidStroke
                    out += DrawCommand.StrokePath(
                        path = PathCmd(listOf(PathOp.MoveTo(from), PathOp.LineTo(to))),
                        stroke = stroke,
                        color = msgColor,
                        z = 1,
                    )
                    decoration?.tail?.let { out += terminalMarker(from, to, it, msgColor, messageStrokeWidth) }
                    when {
                        msg.kind == MessageKind.Destroy -> out += xMark(to, msgColor, messageStrokeWidth)
                        decoration?.head != null -> out += terminalMarker(to, from, decoration.head, msgColor, messageStrokeWidth)
                        decoration?.headStyle == "open" -> out += openArrowHead(from, to, msgColor, messageStrokeWidth)
                        msg.kind == MessageKind.Async -> out += openArrowHead(from, to, msgColor, messageStrokeWidth)
                        else -> out += filledArrowHead(from, to, msgColor)
                    }
                    val labelText = (msg.label as? RichLabel.Plain)?.text.orEmpty()
                    if (labelText.isNotEmpty()) {
                        out += DrawCommand.DrawText(
                            text = labelText,
                            origin = Point((from.x + to.x) / 2f, from.y - 4f),
                            font = msgFont,
                            color = msgText,
                            anchorX = TextAnchorX.Center,
                            anchorY = TextAnchorY.Bottom,
                            z = 10,
                        )
                    }
                }
            }
        }

        return out.entities()
    }

    private fun parseDecorations(ir: SequenceIR): Map<Int, MessageDecoration> {
        val raw = ir.styleHints.extras[PlantUmlSequenceParser.DECORATIONS_KEY].orEmpty()
        if (raw.isEmpty()) return emptyMap()
        val out = LinkedHashMap<Int, MessageDecoration>()
        for (match in Regex("""(\d+)\|([^|]*)\|([^|]*)\|([^|]*)""").findAll(raw)) {
            val index = match.groupValues[1].toInt()
            out[index] = MessageDecoration(
                tail = match.groupValues[2].ifEmpty { null },
                head = match.groupValues[3].ifEmpty { null },
                headStyle = match.groupValues[4].ifEmpty { null },
            )
        }
        return out
    }

    private fun drawBoxes(
        ir: SequenceIR,
        laidOut: LaidOutDiagram,
        out: MutableList<DrawCommand>,
        palette: SequencePalette,
        boxFont: FontSpec,
        colors: com.hrm.diagram.render.theme.ResolvedPlantUmlSequenceColors,
    ) {
        val spec = ir.styleHints.extras[PlantUmlSequenceParser.BOXES_KEY].orEmpty()
        if (spec.isEmpty()) return
        val laneRects = ir.participants.mapNotNull { participant ->
            laidOut.nodePositions[participant.id]?.let { participant.id to it }
        }.toMap()
        for (entry in spec.split("||")) {
            if (entry.isEmpty()) continue
            val parts = entry.split("|", limit = 3)
            val title = parts.getOrNull(0)?.replace("\\|", "|").orEmpty()
            val inlineColor = PlantUmlTreeRenderSupport.parsePlantUmlColor(parts.getOrNull(1).orEmpty())
            val fill = palette.box.fill?.let { Color(it.argb) } ?: inlineColor ?: colors.boxFill
            val strokeColor = palette.box.stroke?.let { Color(it.argb) } ?: inlineColor ?: colors.boxStroke
            val textColor = palette.box.text?.let { Color(it.argb) } ?: colors.boxText
            val ids = parts.getOrNull(2).orEmpty().split(',').filter { it.isNotEmpty() }.map { NodeId(it) }
            val rects = ids.mapNotNull { laneRects[it] }
            if (rects.isEmpty()) continue
            val left = rects.minOf { it.left } - 14f
            val right = rects.maxOf { it.right } + 14f
            val rect = Rect.ltrb(left, 8f, right, laidOut.bounds.bottom - 8f)
            if (palette.box.shadowing == true) {
                out += DrawCommand.FillRect(
                    rect = PlantUmlTreeRenderSupport.offsetRect(rect, 4f, 4f),
                    color = shadowTint,
                    corner = 10f,
                    z = -3,
                )
            }
            out += DrawCommand.FillRect(rect = rect, color = fill.copyAlpha(0.18f), corner = 10f, z = -2)
            out += DrawCommand.StrokeRect(
                rect = rect,
                stroke = Stroke(width = palette.box.lineThickness ?: palette.sequence.lineThickness ?: 1.2f),
                color = strokeColor,
                corner = 10f,
                z = -1,
            )
            if (title.isNotEmpty()) {
                out += DrawCommand.DrawText(
                    text = title,
                    origin = Point(rect.left + 10f, rect.top + 8f),
                    font = boxFont,
                    color = textColor,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    z = 0,
                )
            }
        }
    }

    private fun filledArrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val (p1, p2) = headPoints(from, to, 8f)
        return DrawCommand.FillPath(
            path = PathCmd(listOf(PathOp.MoveTo(to), PathOp.LineTo(p1), PathOp.LineTo(p2), PathOp.Close)),
            color = color,
            z = 2,
        )
    }

    private fun openArrowHead(from: Point, to: Point, color: Color, width: Float): DrawCommand {
        val (p1, p2) = headPoints(from, to, 8f)
        return DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(p1), PathOp.LineTo(to), PathOp.LineTo(p2))),
            stroke = Stroke(width = width),
            color = color,
            z = 2,
        )
    }

    private fun xMark(at: Point, color: Color, width: Float): DrawCommand {
        val size = 5f
        return DrawCommand.StrokePath(
            path = PathCmd(
                listOf(
                    PathOp.MoveTo(Point(at.x - size, at.y - size)),
                    PathOp.LineTo(Point(at.x + size, at.y + size)),
                    PathOp.MoveTo(Point(at.x - size, at.y + size)),
                    PathOp.LineTo(Point(at.x + size, at.y - size)),
                ),
            ),
            stroke = Stroke(width = width),
            color = color,
            z = 2,
        )
    }

    private fun terminalMarker(at: Point, toward: Point, marker: String, color: Color, width: Float): DrawCommand =
        when (marker.lowercase()) {
            "o" -> openCircle(at, color, width)
            "x" -> xMark(at, color, width)
            else -> filledArrowHead(toward, at, color)
        }

    private fun openCircle(at: Point, color: Color, width: Float): DrawCommand {
        val radius = 5f
        return DrawCommand.StrokeRect(
            rect = Rect.ltrb(at.x - radius, at.y - radius, at.x + radius, at.y + radius),
            stroke = Stroke(width = width),
            color = color,
            corner = radius,
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
        return Point(baseX + nx * size * 0.5f, baseY + ny * size * 0.5f) to
            Point(baseX - nx * size * 0.5f, baseY - ny * size * 0.5f)
    }

    private fun paletteOf(ir: SequenceIR): SequencePalette {
        val extras = ir.styleHints.extras
        fun scope(name: String): ScopeStyle = ScopeStyle(
            fill = extras["plantuml.sequence.style.$name.fill"]?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)?.let { ArgbColor(it.argb) },
            stroke = extras["plantuml.sequence.style.$name.stroke"]?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)?.let { ArgbColor(it.argb) },
            text = extras["plantuml.sequence.style.$name.text"]?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)?.let { ArgbColor(it.argb) },
            fontSize = PlantUmlTreeRenderSupport.parsePlantUmlFloat(extras["plantuml.sequence.style.$name.fontSize"]),
            fontName = PlantUmlTreeRenderSupport.parsePlantUmlFontFamily(extras["plantuml.sequence.style.$name.fontName"]),
            lineThickness = PlantUmlTreeRenderSupport.parsePlantUmlFloat(extras["plantuml.sequence.style.$name.lineThickness"]),
            shadowing = PlantUmlTreeRenderSupport.parsePlantUmlBoolean(extras["plantuml.sequence.style.$name.shadowing"]),
        )
        return SequencePalette(
            sequence = scope("sequence"),
            participant = scope("participant"),
            actor = scope("actor"),
            boundary = scope("boundary"),
            control = scope("control"),
            entity = scope("entity"),
            database = scope("database"),
            collections = scope("collections"),
            queue = scope("queue"),
            note = scope("note"),
            box = scope("box"),
            edgeColor = extras[PlantUmlSequenceParser.STYLE_EDGE_COLOR_KEY]
                ?.let(PlantUmlTreeRenderSupport::parsePlantUmlColor)
                ?.let { ArgbColor(it.argb) },
        )
    }

    private fun SequencePalette.edgeColorOrNull(): Color? =
        edgeColor?.let { Color(it.argb) }

    private fun participantScopeFor(kind: String, palette: SequencePalette): ScopeStyle =
        when (kind) {
            "actor" -> mergeScope(palette.participant, palette.actor)
            "boundary" -> mergeScope(palette.participant, palette.boundary)
            "control" -> mergeScope(palette.participant, palette.control)
            "entity" -> mergeScope(palette.participant, palette.entity)
            "database" -> mergeScope(palette.participant, palette.database)
            "collections" -> mergeScope(palette.participant, palette.collections)
            "queue" -> mergeScope(palette.participant, palette.queue)
            else -> palette.participant
        }

    private fun mergeScope(base: ScopeStyle, override: ScopeStyle): ScopeStyle =
        ScopeStyle(
            fill = override.fill ?: base.fill,
            stroke = override.stroke ?: base.stroke,
            text = override.text ?: base.text,
            fontSize = override.fontSize ?: base.fontSize,
            fontName = override.fontName ?: base.fontName,
            lineThickness = override.lineThickness ?: base.lineThickness,
            shadowing = override.shadowing ?: base.shadowing,
        )

    private fun scopedFont(scope: ScopeStyle, base: FontSpec): FontSpec =
        PlantUmlTreeRenderSupport.resolveFontSpec(base, scope.fontName, scope.fontSize?.toString())

    private fun Color.copyAlpha(alpha: Float): Color {
        val a = (alpha.coerceIn(0f, 1f) * 255f).toInt()
        return Color((argb and 0x00FFFFFF) or (a shl 24))
    }
}
