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
import com.hrm.diagram.core.ir.PieIR
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.pie.PieLayout
import com.hrm.diagram.parser.mermaid.MermaidPieParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

internal class MermaidPieSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidPieParser()
    private val colors = ThemeResolver.resolvePie(theme)
    private val layout = PieLayout(textMeasurer)
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
    )

    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val legendFont = FontSpec(family = "sans-serif", sizeSp = 12f)

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun render(ir: PieIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)

        // Compute pie geometry from bounds. Layout fixes pie at left, legend at right.
        val pad = 20f
        val radius = 120f
        val diameter = radius * 2f
        val pieTop = laid.nodePositions[NodeId("pie:title")]?.bottom?.plus(10f) ?: pad
        val center = Point(pad + radius, pieTop + radius)

        val total = ir.slices.sumOf { it.value }.takeIf { it > 0.0 } ?: 1.0
        var angle = -PI / 2.0

        val border = Stroke(width = 1f)

        // Title.
        val titleRect = laid.nodePositions[NodeId("pie:title")]
        if (titleRect != null && !ir.title.isNullOrBlank()) {
            out += DrawCommand.DrawText(
                text = ir.title!!,
                origin = Point(titleRect.left, titleRect.top),
                font = titleFont,
                color = colors.titleText,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 10,
            )
        }

        // Wedges.
        for ((i, s) in ir.slices.withIndex()) {
            val sweep = (s.value / total) * 2.0 * PI
            val start = angle
            val end = angle + sweep
            val fill = colors.slices[i % colors.slices.size]
            val path = wedgePath(center, radius, start, end)
            out += DrawCommand.FillPath(path = path, color = fill, z = 1)
            out += DrawCommand.StrokePath(path = path, stroke = border, color = colors.border, z = 2)
            angle = end
        }

        // Legend rows from layout nodePositions.
        for ((i, s) in ir.slices.withIndex()) {
            val row = laid.nodePositions[NodeId("pie:legend:$i")] ?: continue
            val swatch = Rect.ltrb(row.left, row.top + 3f, row.left + 14f, row.bottom - 3f)
            val fill = colors.slices[i % colors.slices.size]
            out += DrawCommand.FillRect(rect = swatch, color = fill, corner = 3f, z = 5)
            out += DrawCommand.StrokeRect(rect = swatch, stroke = Stroke.Hairline, color = colors.border, corner = 3f, z = 6)

            val label = (s.label as? RichLabel.Plain)?.text ?: "slice$i"
            out += DrawCommand.DrawText(
                text = label,
                origin = Point(swatch.right + 8f, (row.top + row.bottom) / 2f),
                font = legendFont,
                color = colors.legendText,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Middle,
                z = 7,
            )
            val valueText = if (s.value % 1.0 == 0.0) s.value.toInt().toString() else s.value.toString()
            out += DrawCommand.DrawText(
                text = valueText,
                origin = Point(row.right, (row.top + row.bottom) / 2f),
                font = legendFont,
                color = colors.legendText,
                anchorX = TextAnchorX.End,
                anchorY = TextAnchorY.Middle,
                z = 7,
            )
        }

        // Overall border (optional).
        out += DrawCommand.StrokeRect(rect = Rect(Point(0f, 0f), Size(laid.bounds.size.width, laid.bounds.size.height)), stroke = Stroke.Hairline, color = Color.argb(26, colors.border.r, colors.border.g, colors.border.b), corner = 0f, z = 0)
        return out.entities()
    }

    private fun wedgePath(center: Point, radius: Float, start: Double, end: Double): PathCmd {
        val ops = ArrayList<PathOp>()
        ops += PathOp.MoveTo(center)
        ops += PathOp.LineTo(polar(center, radius, start))
        arcCubic(ops, center, radius, start, end)
        ops += PathOp.Close
        return PathCmd(ops)
    }

    private fun polar(center: Point, r: Float, a: Double): Point =
        Point(
            (center.x + r * cos(a)).toFloat(),
            (center.y + r * sin(a)).toFloat(),
        )

    private fun arcCubic(ops: MutableList<PathOp>, center: Point, r: Float, start: Double, end: Double) {
        var a0 = start
        val dir = if (end >= start) 1.0 else -1.0
        var remaining = (end - start)
        while (dir * remaining > 1e-6) {
            val step = dir * min(dir * remaining, PI / 2.0) // <= 90deg per segment
            val a1 = a0 + step
            cubicArcSegment(ops, center, r, a0, a1)
            a0 = a1
            remaining = end - a0
        }
    }

    private fun cubicArcSegment(ops: MutableList<PathOp>, center: Point, r: Float, a0: Double, a1: Double) {
        val theta = a1 - a0
        val k = (4.0 / 3.0) * tan(theta / 4.0)
        val p0 = polar(center, r, a0)
        val p3 = polar(center, r, a1)
        val dx0 = (-sin(a0) * k * r).toFloat()
        val dy0 = (cos(a0) * k * r).toFloat()
        val dx1 = (sin(a1) * k * r).toFloat()
        val dy1 = (-cos(a1) * k * r).toFloat()
        val c1 = Point(p0.x + dx0, p0.y + dy0)
        val c2 = Point(p3.x + dx1, p3.y + dy1)
        ops += PathOp.CubicTo(c1, c2, p3)
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }

}
