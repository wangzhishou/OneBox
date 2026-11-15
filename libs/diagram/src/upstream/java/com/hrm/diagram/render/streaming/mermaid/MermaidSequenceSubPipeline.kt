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
import com.hrm.diagram.core.ir.MessageKind
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SequenceIR
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.sequence.SequenceLayouts
import com.hrm.diagram.parser.mermaid.MermaidSequenceParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.cache.DrawEntityKey
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.sqrt

/**
 * Sub-pipeline that handles `sequenceDiagram` sources. Owns its own parser, layout, and
 * renderer; results funnel back through the same [PipelineAdvance] envelope as the flowchart
 * sub-pipeline.
 */
internal class MermaidSequenceSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {

    private val parser = MermaidSequenceParser()
    private val colors = ThemeResolver.resolveSequence(theme)
    private val layout = SequenceLayouts.forSequence(textMeasurer)
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::renderSequence,
        postLayout = { _, laidOut, seq -> laidOut.copy(seq = seq) },
    )

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

    private fun renderSequence(ir: SequenceIR, laidOut: LaidOutDiagram): List<DrawEntity> {
        val out = ArrayList<DrawEntity>()
        val headerStrokeStyle = Stroke(width = 1.5f)
        val solidStroke = Stroke(width = 1.5f)
        val dashedStroke = Stroke(width = 1.5f, dash = listOf(6f, 4f))
        val labelFont = FontSpec(family = "sans-serif", sizeSp = 13f)
        val msgFont = FontSpec(family = "sans-serif", sizeSp = 11f)

        val bottomY = laidOut.bounds.bottom

        // Headers + lifelines.
        for (p in ir.participants) {
            val r = laidOut.nodePositions[p.id] ?: continue
            val commands = ArrayList<DrawCommand>(4)
            commands += DrawCommand.FillRect(rect = r, color = colors.headerFill, corner = 6f, z = 2)
            commands += DrawCommand.StrokeRect(rect = r, stroke = headerStrokeStyle, color = colors.headerStroke, corner = 6f, z = 3)
            val text = (p.label as? RichLabel.Plain)?.text?.takeIf { it.isNotEmpty() } ?: p.id.value
            val cx = (r.left + r.right) / 2f
            val cy = (r.top + r.bottom) / 2f
            commands += DrawCommand.DrawText(
                text = text,
                origin = Point(cx, cy),
                font = labelFont,
                color = colors.headerText,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 4,
            )
            // Lifeline (dashed) from header bottom to bottom of diagram.
            val lifelinePath = PathCmd(listOf(
                PathOp.MoveTo(Point(cx, r.bottom)),
                PathOp.LineTo(Point(cx, bottomY)),
            ))
            commands += DrawCommand.StrokePath(path = lifelinePath, stroke = dashedStroke, color = colors.lifeline, z = 0)
            out += DrawEntity("${DrawEntityKey.node("mermaid", p.id)}.participant", commands)
        }

        // Activation rects (clusterRects with `#act#` keys) and notes (`note#`) and fragments (`frag#`).
        for ((id, rect) in laidOut.clusterRects) {
            val v = id.value
            val commands = ArrayList<DrawCommand>(2)
            when {
                v.contains("#act#") -> {
                    commands += DrawCommand.FillRect(rect = rect, color = colors.activationFill, corner = 0f, z = 5)
                    commands += DrawCommand.StrokeRect(rect = rect, stroke = solidStroke, color = colors.activationStroke, corner = 0f, z = 6)
                }
                v.startsWith("note#") -> {
                    commands += DrawCommand.FillRect(rect = rect, color = colors.noteFill, corner = 4f, z = 7)
                    commands += DrawCommand.StrokeRect(rect = rect, stroke = solidStroke, color = colors.noteStroke, corner = 4f, z = 8)
                }
                v.startsWith("frag#") -> {
                    commands += DrawCommand.StrokeRect(rect = rect, stroke = solidStroke, color = colors.fragmentStroke, corner = 4f, z = 9)
                }
            }
            if (commands.isNotEmpty()) out += DrawEntity(DrawEntityKey.cluster("mermaid", id), commands)
        }

        // Map note rects to messages by index for label drawing.
        val noteRectsByIdx = HashMap<Int, Rect>()
        var ni = 0
        for ((id, rect) in laidOut.clusterRects) {
            if (id.value.startsWith("note#")) {
                noteRectsByIdx[ni++] = rect
            }
        }

        // Messages (arrows, labels). Iterate in order; index into edgeRoutes for non-note kinds.
        val edgeRoutesById = laidOut.edgeRoutes
        var edgeIdx = 0
        var noteIdx2 = 0
        for ((msgIndex, msg) in ir.messages.withIndex()) {
            when (msg.kind) {
                MessageKind.Note -> {
                    if (msg.activate || msg.deactivate) continue
                    val rect = noteRectsByIdx[noteIdx2++] ?: continue
                    val labelStr = (msg.label as? RichLabel.Plain)?.text ?: ""
                    val commands = ArrayList<DrawCommand>(1)
                    if (labelStr.isNotEmpty()) {
                        val cx = (rect.left + rect.right) / 2f
                        val cy = (rect.top + rect.bottom) / 2f
                        commands += DrawCommand.DrawText(
                            text = labelStr,
                            origin = Point(cx, cy),
                            font = msgFont,
                            color = colors.noteText,
                            anchorX = TextAnchorX.Center,
                            anchorY = TextAnchorY.Middle,
                            z = 10,
                        )
                    }
                    if (commands.isNotEmpty()) out += DrawEntity("mermaid.message.$msgIndex.note", commands)
                }
                else -> {
                    val route = edgeRoutesById.getOrNull(edgeIdx++) ?: continue
                    val pts = route.points
                    val from = pts.first()
                    val to = pts.last()
                    val commands = ArrayList<DrawCommand>(4)
                    val stroke = when (msg.kind) {
                        MessageKind.Reply -> dashedStroke
                        else -> solidStroke
                    }
                    val path = PathCmd(listOf(PathOp.MoveTo(from), PathOp.LineTo(to)))
                    commands += DrawCommand.StrokePath(path = path, stroke = stroke, color = colors.message, z = 1)
                    when (msg.kind) {
                        MessageKind.Async -> commands += openArrowHead(from, to, colors.message)
                        MessageKind.Destroy -> commands += xMark(to, colors.message)
                        else -> commands += filledArrowHead(from, to, colors.message)
                    }
                    val labelStr = (msg.label as? RichLabel.Plain)?.text ?: ""
                    if (labelStr.isNotEmpty()) {
                        val mx = (from.x + to.x) / 2f
                        val my = from.y - 4f
                        commands += DrawCommand.DrawText(
                            text = labelStr,
                            origin = Point(mx, my),
                            font = msgFont,
                            color = colors.messageText,
                            anchorX = TextAnchorX.Center,
                            anchorY = TextAnchorY.Bottom,
                            z = 10,
                        )
                    }
                    out += DrawEntity("mermaid.message.$msgIndex.${msg.from.value}->${msg.to.value}", commands)
                }
            }
        }

        return out
    }

    private fun filledArrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val (p1, p2) = headPoints(from, to, size = 8f)
        val path = PathCmd(listOf(
            PathOp.MoveTo(to), PathOp.LineTo(p1), PathOp.LineTo(p2), PathOp.Close,
        ))
        return DrawCommand.FillPath(path = path, color = color, z = 2)
    }

    private fun openArrowHead(from: Point, to: Point, color: Color): DrawCommand {
        val (p1, p2) = headPoints(from, to, size = 8f)
        val path = PathCmd(listOf(
            PathOp.MoveTo(p1), PathOp.LineTo(to), PathOp.LineTo(p2),
        ))
        return DrawCommand.StrokePath(path = path, stroke = Stroke(width = 1.5f), color = color, z = 2)
    }

    private fun xMark(at: Point, color: Color): DrawCommand {
        val s = 5f
        val ops = listOf(
            PathOp.MoveTo(Point(at.x - s, at.y - s)),
            PathOp.LineTo(Point(at.x + s, at.y + s)),
            PathOp.MoveTo(Point(at.x - s, at.y + s)),
            PathOp.LineTo(Point(at.x + s, at.y - s)),
        )
        return DrawCommand.StrokePath(path = PathCmd(ops), stroke = Stroke(width = 1.5f), color = color, z = 2)
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
}
