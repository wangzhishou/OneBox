package com.hrm.diagram.render.streaming.plantuml

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
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.pie.PieLayout
import com.hrm.diagram.parser.plantuml.PlantUmlPieParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

internal class PlantUmlPieSubPipeline(
    textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private val parser = PlantUmlPieParser()
    private val colors = ThemeResolver.resolvePie(theme)
    private val layout = PieLayout(textMeasurer)
    private val kernel = PlantUmlFamilyRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
    )
    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val legendFont = FontSpec(family = "sans-serif", sizeSp = 12f)

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun render(ir: PieIR, laid: LaidOutDiagram): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laid)
        val pad = 20f
        val pieRect = laid.nodePositions[NodeId("pie:plot")] ?: Rect(Point(pad, laid.nodePositions[NodeId("pie:title")]?.bottom?.plus(10f) ?: pad), Size(240f, 240f))
        val radius = pieRect.size.width.coerceAtMost(pieRect.size.height) / 2f
        val center = Point(pieRect.left + radius, pieRect.top + radius)
        val total = ir.slices.sumOf { it.value }.takeIf { it > 0.0 } ?: 1.0
        var angle = -PI / 2.0
        val palette = ir.slices.indices.map { idx ->
            parseColor(ir.styleHints.extras["${PlantUmlPieParser.STYLE_SLICE_COLOR_PREFIX}$idx"])
        }.mapIndexed { index, color ->
            color ?: colors.slices[index % colors.slices.size]
        }
        val border = Stroke(width = ir.styleHints.extras[PlantUmlPieParser.STYLE_LINE_THICKNESS_KEY]?.toFloatOrNull() ?: 1f)
        val borderColor = parseColor(ir.styleHints.extras[PlantUmlPieParser.STYLE_BORDER_KEY]) ?: colors.border
        val textColor = parseColor(ir.styleHints.extras[PlantUmlPieParser.STYLE_TEXT_KEY]) ?: colors.titleText
        parseColor(ir.styleHints.extras[PlantUmlPieParser.STYLE_BACKGROUND_KEY])?.let {
            out += DrawCommand.FillRect(Rect(Point(0f, 0f), Size(laid.bounds.size.width, laid.bounds.size.height)), it, z = -1)
        }
        val showShadow = ir.styleHints.extras[PlantUmlPieParser.STYLE_SHADOWING_KEY]?.lowercase() in setOf("true", "yes", "on", "1")
        if (showShadow) {
            out += DrawCommand.FillPath(path = wedgePath(Point(center.x + 4f, center.y + 5f), radius, 0.0, 2.0 * PI), color = Color(0x22000000), z = 0)
        }

        val titleRect = laid.nodePositions[NodeId("pie:title")]
        if (titleRect != null && !ir.title.isNullOrBlank()) {
            out += DrawCommand.DrawText(
                text = ir.title!!,
                origin = Point(titleRect.left, titleRect.top),
                font = titleFont,
                color = textColor,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 10,
            )
        }

        for ((index, slice) in ir.slices.withIndex()) {
            val sweep = (slice.value / total) * 2.0 * PI
            val start = angle
            val end = angle + sweep
            val path = wedgePath(center, radius, start, end)
            val fill = palette[index % palette.size]
            out += DrawCommand.FillPath(path = path, color = fill, z = 1)
            out += DrawCommand.StrokePath(path = path, stroke = border, color = borderColor, z = 2)
            angle = end
        }

        if (ir.styleHints.extras[PlantUmlPieParser.STYLE_LEGEND_KEY] == "none") return out.entities()

        for ((index, slice) in ir.slices.withIndex()) {
            val row = laid.nodePositions[NodeId("pie:legend:$index")] ?: continue
            val swatch = Rect.ltrb(row.left, row.top + 3f, row.left + 14f, row.bottom - 3f)
            val fill = palette[index % palette.size]
            out += DrawCommand.FillRect(rect = swatch, color = fill, corner = 3f, z = 5)
            out += DrawCommand.StrokeRect(rect = swatch, stroke = Stroke.Hairline, color = borderColor, corner = 3f, z = 6)
            out += DrawCommand.DrawText(
                text = labelOf(slice.label, index),
                origin = Point(swatch.right + 8f, (row.top + row.bottom) / 2f),
                font = legendFont,
                color = textColor,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Middle,
                z = 7,
            )
            out += DrawCommand.DrawText(
                text = formatValue(slice.value),
                origin = Point(row.right, (row.top + row.bottom) / 2f),
                font = legendFont,
                color = textColor,
                anchorX = TextAnchorX.End,
                anchorY = TextAnchorY.Middle,
                z = 7,
            )
        }

        out += DrawCommand.StrokeRect(
            rect = Rect(Point(0f, 0f), Size(laid.bounds.size.width, laid.bounds.size.height)),
            stroke = Stroke.Hairline,
            color = Color(0x1A000000),
            z = 0,
        )
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

    private fun polar(center: Point, radius: Float, angle: Double): Point =
        Point(
            (center.x + radius * cos(angle)).toFloat(),
            (center.y + radius * sin(angle)).toFloat(),
        )

    private fun arcCubic(ops: MutableList<PathOp>, center: Point, radius: Float, start: Double, end: Double) {
        var a0 = start
        val dir = if (end >= start) 1.0 else -1.0
        var remaining = end - start
        while (dir * remaining > 1e-6) {
            val step = dir * min(dir * remaining, PI / 2.0)
            val a1 = a0 + step
            cubicArcSegment(ops, center, radius, a0, a1)
            a0 = a1
            remaining = end - a0
        }
    }

    private fun cubicArcSegment(ops: MutableList<PathOp>, center: Point, radius: Float, a0: Double, a1: Double) {
        val theta = a1 - a0
        val k = (4.0 / 3.0) * tan(theta / 4.0)
        val p0 = polar(center, radius, a0)
        val p3 = polar(center, radius, a1)
        val dx0 = (-sin(a0) * k * radius).toFloat()
        val dy0 = (cos(a0) * k * radius).toFloat()
        val dx1 = (sin(a1) * k * radius).toFloat()
        val dy1 = (-cos(a1) * k * radius).toFloat()
        ops += PathOp.CubicTo(Point(p0.x + dx0, p0.y + dy0), Point(p3.x + dx1, p3.y + dy1), p3)
    }

    private fun labelOf(label: RichLabel, index: Int): String =
        when (label) {
            is RichLabel.Plain -> label.text
            is RichLabel.Markdown -> label.source
            is RichLabel.Html -> label.html
        }.ifBlank { "slice$index" }

    private fun formatValue(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

    private fun parseColor(raw: String?): Color? {
        val s = raw?.trim()?.lowercase() ?: return null
        if (s.isEmpty()) return null
        if (s.startsWith("#")) {
            val hex = s.removePrefix("#")
            return when (hex.length) {
                3 -> Color(0xFF000000.toInt() or expand3(hex))
                6 -> hex.toIntOrNull(16)?.let { Color(0xFF000000.toInt() or it) }
                8 -> hex.toLongOrNull(16)?.toInt()?.let { Color(it) }
                else -> null
            }
        }
        return when (s) {
            "white" -> Color(0xFFFFFFFF.toInt())
            "black" -> Color(0xFF000000.toInt())
            "red" -> Color(0xFFE53935.toInt())
            "green", "lime" -> Color(0xFF43A047.toInt())
            "blue" -> Color(0xFF1E88E5.toInt())
            "yellow" -> Color(0xFFFDD835.toInt())
            "orange" -> Color(0xFFFB8C00.toInt())
            "purple" -> Color(0xFF8E24AA.toInt())
            "gray", "grey" -> Color(0xFF78909C.toInt())
            else -> null
        }
    }

    private fun expand3(hex: String): Int =
        ("${hex[0]}${hex[0]}".toInt(16) shl 16) or
            ("${hex[1]}${hex[1]}".toInt(16) shl 8) or
            "${hex[2]}${hex[2]}".toInt(16)
}
