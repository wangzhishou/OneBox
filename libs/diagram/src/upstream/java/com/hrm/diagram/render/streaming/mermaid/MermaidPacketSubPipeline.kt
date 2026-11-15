package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.PathCmd
import com.hrm.diagram.core.draw.PathOp
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Stroke
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.StructIR
import com.hrm.diagram.core.ir.StructNode
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.struct.StructLayout
import com.hrm.diagram.parser.mermaid.MermaidPacketParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ThemeResolver

internal class MermaidPacketSubPipeline(
    textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidPacketParser()
    private val layout = StructLayout(textMeasurer)
    private val colors = ThemeResolver.resolvePacket(theme)
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
        postLayout = { _, laidOut, seq -> laidOut.copy(seq = seq) },
    )
    private val font = FontSpec(family = "monospace", sizeSp = 12f)
    private val rootFont = font.copy(weight = 700)

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun render(ir: StructIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)
        for (route in laid.edgeRoutes) {
            out += DrawCommand.StrokePath(
                PathCmd(route.points.mapIndexed { index, point -> if (index == 0) PathOp.MoveTo(point) else PathOp.LineTo(point) }),
                Stroke(width = 1.2f),
                colors.edge,
                z = 0,
            )
        }
        fun drawNode(node: StructNode, path: String, isRoot: Boolean) {
            val id = NodeId("struct_$path")
            val rect = laid.nodePositions[id] ?: return
            out += DrawCommand.FillRect(rect, if (isRoot) colors.rootFill else colors.fill, corner = 7f, z = 1)
            out += DrawCommand.StrokeRect(rect, Stroke(width = if (isRoot) 1.8f else 1f), if (isRoot) colors.rootStroke else colors.stroke, corner = 7f, z = 2)
            out += DrawCommand.DrawText(
                text = labelFor(node),
                origin = Point(rect.left + 12f, rect.top + rect.size.height / 2f),
                font = if (isRoot) rootFont else font,
                color = colors.text,
                maxWidth = rect.size.width - 24f,
                anchorY = TextAnchorY.Middle,
                z = 3,
            )
            when (node) {
                is StructNode.ArrayNode -> node.items.forEachIndexed { index, child -> drawNode(child, "$path.$index", false) }
                is StructNode.ObjectNode -> node.entries.forEachIndexed { index, child -> drawNode(child, "$path.$index", false) }
                is StructNode.Scalar -> Unit
            }
        }
        drawNode(ir.root, "root", true)
        return out.entities()
    }

    private fun labelFor(node: StructNode): String {
        val prefix = node.key?.let { "$it: " }.orEmpty()
        return when (node) {
            is StructNode.ArrayNode -> "$prefix[${node.items.size}]"
            is StructNode.ObjectNode -> "$prefix{${node.entries.size}}"
            is StructNode.Scalar -> "$prefix${node.value}"
        }
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }
}
