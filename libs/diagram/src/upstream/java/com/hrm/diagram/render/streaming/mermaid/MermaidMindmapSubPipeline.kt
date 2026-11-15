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
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.TreeIR
import com.hrm.diagram.core.ir.TreeNode
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.tree.MindmapLayout
import com.hrm.diagram.parser.mermaid.MermaidMindmapParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.min

internal class MermaidMindmapSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidMindmapParser()
    private val colors = ThemeResolver.resolveMermaidMindmap(theme)
    private val layout = MindmapLayout(textMeasurer)
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = { model, laidOut -> render(model, laidOut, parser.snapshotNodeShapes()) },
    )
    private val font = FontSpec(family = "sans-serif", sizeSp = 12f)

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun render(ir: TreeIR, laid: LaidOutDiagram, shapes: Map<NodeId, NodeShape>): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)
        val icons = parser.snapshotNodeIcons()
        fun drawEdges(parent: TreeNode) {
            val pr = laid.nodePositions[parent.id] ?: return
            for (c in parent.children) {
                val cr = laid.nodePositions[c.id] ?: continue
                val childOnRight = cr.left >= pr.left
                val from = if (childOnRight) {
                    Point(pr.right, (pr.top + pr.bottom) / 2f)
                } else {
                    Point(pr.left, (pr.top + pr.bottom) / 2f)
                }
                val to = if (childOnRight) {
                    Point(cr.left, (cr.top + cr.bottom) / 2f)
                } else {
                    Point(cr.right, (cr.top + cr.bottom) / 2f)
                }
                val midX = (from.x + to.x) / 2f
                val path = PathCmd(
                    listOf(
                        PathOp.MoveTo(from),
                        PathOp.CubicTo(Point(midX, from.y), Point(midX, to.y), to),
                    ),
                )
                out += DrawCommand.StrokePath(path = path, stroke = Stroke(width = 1.5f), color = colors.edge, z = 0)
                drawEdges(c)
            }
        }

        fun drawNode(n: TreeNode, isRoot: Boolean) {
            val r = laid.nodePositions[n.id] ?: return
            val shape = shapes[n.id] ?: NodeShape.Box
            val fill = if (isRoot) colors.rootFill else colors.nodeFill
            val strokeColor = if (isRoot) colors.rootStroke else colors.nodeStroke
            val stroke = Stroke(width = if (isRoot) 2f else 1.5f)
            val iconName = icons[n.id]

            when (shape) {
                is NodeShape.Circle -> {
                    val corner = min(r.size.width, r.size.height) / 2f
                    out += DrawCommand.FillRect(rect = r, color = fill, corner = corner, z = 1)
                    out += DrawCommand.StrokeRect(rect = r, stroke = stroke, color = strokeColor, corner = corner, z = 2)
                }
                is NodeShape.RoundedBox -> {
                    out += DrawCommand.FillRect(rect = r, color = fill, corner = 14f, z = 1)
                    out += DrawCommand.StrokeRect(rect = r, stroke = stroke, color = strokeColor, corner = 14f, z = 2)
                }
                is NodeShape.Hexagon -> {
                    val dx = r.size.width * 0.16f
                    val cy = (r.top + r.bottom) / 2f
                    val path = PathCmd(
                        listOf(
                            PathOp.MoveTo(Point(r.left + dx, r.top)),
                            PathOp.LineTo(Point(r.right - dx, r.top)),
                            PathOp.LineTo(Point(r.right, cy)),
                            PathOp.LineTo(Point(r.right - dx, r.bottom)),
                            PathOp.LineTo(Point(r.left + dx, r.bottom)),
                            PathOp.LineTo(Point(r.left, cy)),
                            PathOp.Close,
                        ),
                    )
                    out += DrawCommand.FillPath(path = path, color = fill, z = 1)
                    out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 2)
                }
                is NodeShape.Cloud -> {
                    val l = r.left
                    val t = r.top
                    val rr = r.right
                    val b = r.bottom
                    val w = r.size.width
                    val h = r.size.height
                    val path = PathCmd(
                        listOf(
                            PathOp.MoveTo(Point(l + w * 0.18f, b - h * 0.18f)),
                            PathOp.CubicTo(Point(l - w * 0.02f, b - h * 0.26f), Point(l + w * 0.02f, t + h * 0.38f), Point(l + w * 0.20f, t + h * 0.38f)),
                            PathOp.CubicTo(Point(l + w * 0.20f, t + h * 0.08f), Point(l + w * 0.42f, t + h * 0.02f), Point(l + w * 0.54f, t + h * 0.20f)),
                            PathOp.CubicTo(Point(l + w * 0.64f, t - h * 0.02f), Point(rr - w * 0.10f, t + h * 0.06f), Point(rr - w * 0.12f, t + h * 0.32f)),
                            PathOp.CubicTo(Point(rr + w * 0.02f, t + h * 0.36f), Point(rr + w * 0.02f, b - h * 0.12f), Point(rr - w * 0.12f, b - h * 0.12f)),
                            PathOp.CubicTo(Point(rr - w * 0.18f, b + h * 0.04f), Point(l + w * 0.34f, b + h * 0.04f), Point(l + w * 0.18f, b - h * 0.18f)),
                            PathOp.Close,
                        ),
                    )
                    out += DrawCommand.FillPath(path = path, color = fill, z = 1)
                    out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 2)
                }
                is NodeShape.Custom -> {
                    if (shape.name.equals("bang", ignoreCase = true)) {
                        val cx = (r.left + r.right) / 2f
                        val cy = (r.top + r.bottom) / 2f
                        val w = r.size.width / 2f
                        val h = r.size.height / 2f
                        val path = PathCmd(
                            listOf(
                                PathOp.MoveTo(Point(cx, r.top)),
                                PathOp.LineTo(Point(cx + w * 0.36f, cy - h * 0.42f)),
                                PathOp.LineTo(Point(r.right, cy - h * 0.14f)),
                                PathOp.LineTo(Point(cx + w * 0.52f, cy + h * 0.12f)),
                                PathOp.LineTo(Point(cx + w * 0.68f, r.bottom)),
                                PathOp.LineTo(Point(cx, cy + h * 0.42f)),
                                PathOp.LineTo(Point(cx - w * 0.68f, r.bottom)),
                                PathOp.LineTo(Point(cx - w * 0.52f, cy + h * 0.12f)),
                                PathOp.LineTo(Point(r.left, cy - h * 0.14f)),
                                PathOp.LineTo(Point(cx - w * 0.36f, cy - h * 0.42f)),
                                PathOp.Close,
                            ),
                        )
                        out += DrawCommand.FillPath(path = path, color = fill, z = 1)
                        out += DrawCommand.StrokePath(path = path, stroke = stroke, color = strokeColor, z = 2)
                    } else {
                        out += DrawCommand.FillRect(rect = r, color = fill, corner = 4f, z = 1)
                        out += DrawCommand.StrokeRect(rect = r, stroke = stroke, color = strokeColor, corner = 4f, z = 2)
                    }
                }
                else -> {
                    out += DrawCommand.FillRect(rect = r, color = fill, corner = 4f, z = 1)
                    out += DrawCommand.StrokeRect(rect = r, stroke = stroke, color = strokeColor, corner = 4f, z = 2)
                }
            }

            val label = (n.label as? RichLabel.Plain)?.text ?: ""
            if (iconName != null) {
                val iconRect = Rect.ltrb(r.left + 10f, r.top + 10f, r.right - 10f, r.bottom - 10f)
                out += DrawCommand.DrawIcon(name = iconName, rect = iconRect, z = 3)
                out += DrawCommand.DrawText(
                    text = iconFallbackLabel(iconName),
                    origin = Point((r.left + r.right) / 2f, (r.top + r.bottom) / 2f),
                    font = font.copy(weight = 600),
                color = if (isRoot) colors.rootText else colors.nodeText,
                    maxWidth = r.size.width - 12f,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = 4,
                )
            } else {
                out += DrawCommand.DrawText(
                    text = label,
                    origin = Point((r.left + r.right) / 2f, (r.top + r.bottom) / 2f),
                    font = font,
                    color = if (isRoot) colors.rootText else colors.nodeText,
                    maxWidth = r.size.width - 12f,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = 3,
                )
            }
            n.children.forEach { drawNode(it, false) }
        }

        drawEdges(ir.root)
        drawNode(ir.root, true)
        return out.entities()
    }

    private fun iconFallbackLabel(iconName: String): String {
        val lastToken = iconName.split(' ').map { it.trim() }.filter { it.isNotEmpty() }.lastOrNull().orEmpty()
        val compact = lastToken.removePrefix("fa-").removePrefix("mdi-").replace('-', ' ').trim()
        val parts = compact.split(' ').filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> "I"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }

}
