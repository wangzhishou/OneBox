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
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SankeyIR
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.sankey.SankeyLayout
import com.hrm.diagram.parser.mermaid.MermaidSankeyParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.max

internal class MermaidSankeySubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidSankeyParser()
    private val colors = ThemeResolver.resolveSankey(theme)
    private val layout = SankeyLayout(textMeasurer)
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
    )
    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val labelFont = FontSpec(family = "sans-serif", sizeSp = 11f)

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun render(ir: SankeyIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)
        val bounds = laid.bounds
        val nodeColors = ir.nodes.mapIndexed { index, node -> node.id to palette(index) }.toMap()
        val values = computeNodeValues(ir)
        val outOffset = LinkedHashMap<NodeId, Float>()
        val inOffset = LinkedHashMap<NodeId, Float>()

        out += DrawCommand.FillRect(Rect(Point(0f, 0f), Size(bounds.size.width, bounds.size.height)), colors.background, z = 0)
        val titleRect = laid.nodePositions[NodeId("sankey:title")]
        if (titleRect != null && !ir.title.isNullOrBlank()) {
            out += DrawCommand.DrawText(ir.title!!, Point(titleRect.left, titleRect.top), titleFont, colors.text, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Top, z = 10)
        }

        for (flow in ir.flows) {
            val fromRect = laid.nodePositions[flow.from] ?: continue
            val toRect = laid.nodePositions[flow.to] ?: continue
            val fromScale = fromRect.size.height / max((values[flow.from] ?: 1.0).toFloat(), 1f)
            val toScale = toRect.size.height / max((values[flow.to] ?: 1.0).toFloat(), 1f)
            val thickness = max(6f, flow.value.toFloat() * minOf(fromScale, toScale))
            val fromTop = fromRect.top + (outOffset[flow.from] ?: 0f)
            val toTop = toRect.top + (inOffset[flow.to] ?: 0f)
            outOffset[flow.from] = (outOffset[flow.from] ?: 0f) + thickness
            inOffset[flow.to] = (inOffset[flow.to] ?: 0f) + thickness
            out += DrawCommand.FillPath(
                path = bandPath(
                    from = Point(fromRect.right, fromTop + thickness / 2f),
                    to = Point(toRect.left, toTop + thickness / 2f),
                    thickness = thickness,
                ),
                color = flowColor(nodeColors[flow.from] ?: colors.nodePalette.firstOrNull() ?: colors.text),
                z = 2,
            )
        }

        for ((index, node) in ir.nodes.withIndex()) {
            val rect = laid.nodePositions[node.id] ?: continue
            val labelRect = laid.nodePositions[NodeId("sankey:label:${node.id.value}")]
            val fill = nodeColors[node.id] ?: palette(index)
            out += DrawCommand.FillRect(rect, fill, corner = 6f, z = 4)
            out += DrawCommand.StrokeRect(rect, Stroke(width = 1f), colors.border, corner = 6f, z = 5)
            val label = (node.label as? RichLabel.Plain)?.text.orEmpty()
            if (labelRect != null) {
                out += DrawCommand.DrawText(label, Point(labelRect.left, labelRect.top), labelFont, colors.text, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Top, z = 10)
            }
        }
        return out.entities()
    }

    private fun computeNodeValues(ir: SankeyIR): Map<NodeId, Double> {
        val incoming = LinkedHashMap<NodeId, Double>()
        val outgoing = LinkedHashMap<NodeId, Double>()
        for (flow in ir.flows) {
            outgoing[flow.from] = (outgoing[flow.from] ?: 0.0) + flow.value
            incoming[flow.to] = (incoming[flow.to] ?: 0.0) + flow.value
        }
        return ir.nodes.associate { node -> node.id to maxOf(incoming[node.id] ?: 0.0, outgoing[node.id] ?: 0.0, 1.0) }
    }

    private fun bandPath(from: Point, to: Point, thickness: Float): PathCmd {
        val c1 = Point(from.x + (to.x - from.x) * 0.35f, from.y)
        val c2 = Point(from.x + (to.x - from.x) * 0.65f, to.y)
        val ops = listOf(
            PathOp.MoveTo(Point(from.x, from.y - thickness / 2f)),
            PathOp.CubicTo(Point(c1.x, c1.y - thickness / 2f), Point(c2.x, c2.y - thickness / 2f), Point(to.x, to.y - thickness / 2f)),
            PathOp.LineTo(Point(to.x, to.y + thickness / 2f)),
            PathOp.CubicTo(Point(c2.x, c2.y + thickness / 2f), Point(c1.x, c1.y + thickness / 2f), Point(from.x, from.y + thickness / 2f)),
            PathOp.Close,
        )
        return PathCmd(ops)
    }

    private fun palette(index: Int): Color = listOf(
        *colors.nodePalette.toTypedArray(),
    )[index % colors.nodePalette.size.coerceAtLeast(1)]

    private fun flowColor(color: Color): Color = Color.argb(colors.flowAlpha, color.r, color.g, color.b)

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }
}
