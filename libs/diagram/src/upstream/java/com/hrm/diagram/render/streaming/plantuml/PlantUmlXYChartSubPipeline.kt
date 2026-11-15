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
import com.hrm.diagram.core.ir.AxisKind
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SeriesKind
import com.hrm.diagram.core.ir.XYChartIR
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.xy.XYChartLayout
import com.hrm.diagram.parser.plantuml.PlantUmlXYChartParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.round

internal class PlantUmlXYChartSubPipeline(
    defaultKind: SeriesKind,
    textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    private val parser = PlantUmlXYChartParser(defaultKind)
    private val layout = XYChartLayout(textMeasurer)
    private val baseColors = ThemeResolver.resolveXYChart(theme)
    private var cachedExtras: Map<String, String> = emptyMap()
    private var renderConfig: RenderConfig = RenderConfig.from(emptyMap(), baseColors)
    private val kernel = PlantUmlFamilyRenderSubPipelineKernel(
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
    )
    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val axisTitleFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val axisLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)

    override fun acceptLine(line: String): IrPatchBatch = parser.acceptLine(line)

    override fun finish(blockClosed: Boolean): IrPatchBatch = parser.finish(blockClosed)

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun render(ir: XYChartIR, laid: LaidOutDiagram): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laid)
        val bounds = laid.bounds
        val plot = laid.nodePositions[NodeId("xychart:plot")] ?: return emptyList()
        val config = resolveConfig(ir)

        out += DrawCommand.FillRect(Rect(Point(0f, 0f), Size(bounds.size.width, bounds.size.height)), config.background, z = 0)
        drawTitles(ir, laid, out, config.text)
        drawAxes(plot, out, config.axis, config.lineWidth)

        val yMin = ir.yAxis.min ?: 0.0
        val yMax = ir.yAxis.max ?: 1.0
        val yTicks = 5
        for (i in 0..yTicks) {
            val t = i.toFloat() / yTicks.toFloat()
            val y = plot.bottom - plot.size.height * t
            out += DrawCommand.StrokePath(
                PathCmd(listOf(PathOp.MoveTo(Point(plot.left - 4f, y)), PathOp.LineTo(Point(plot.right, y)))),
                Stroke(width = if (i == 0) config.lineWidth else 0.75f, dash = if (i == 0) null else listOf(4f, 4f)),
                config.axis,
                z = 1,
            )
            laid.nodePositions[NodeId("xychart:yLabel:$i")]?.let { labelRect ->
                val value = yMin + (yMax - yMin) * t.toDouble()
                out += DrawCommand.DrawText(formatTick(value), Point(labelRect.left, y), axisLabelFont, config.text, anchorY = TextAnchorY.Middle, z = 10)
            }
        }

        val itemCount = when {
            ir.xAxis.kind == AxisKind.Category -> ir.xAxis.categories.size
            else -> (ir.series.maxOfOrNull { it.ys.size } ?: 0).coerceAtLeast(2)
        }
        if (itemCount <= 0) return out.entities()
        val slot = plot.size.width / itemCount.toFloat()
        for (i in 0 until itemCount) {
            val x = plot.left + slot * (i + 0.5f)
            ir.xAxis.categories.getOrNull(i)?.let { label ->
                out += DrawCommand.DrawText(label, Point(x, plot.bottom + 18f), axisLabelFont, config.text, maxWidth = slot - 8f, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Top, z = 10)
            }
        }

        fun yOf(value: Double): Float {
            val denom = (yMax - yMin).takeIf { it != 0.0 } ?: 1.0
            val t = ((value - yMin) / denom).coerceIn(0.0, 1.0)
            return (plot.bottom - plot.size.height * t).toFloat()
        }

        fun pointAt(index: Int, value: Double): Point =
            Point(plot.left + slot * (index + 0.5f), yOf(value))

        fun xOf(value: Double, fallbackIndex: Int): Float {
            if (ir.xAxis.kind != AxisKind.Linear) return pointAt(fallbackIndex, 0.0).x
            val min = ir.xAxis.min ?: 0.0
            val max = ir.xAxis.max ?: 1.0
            val denom = (max - min).takeIf { it != 0.0 } ?: 1.0
            val t = ((value - min) / denom).coerceIn(0.0, 1.0)
            return (plot.left + plot.size.width * t).toFloat()
        }

        fun pointOf(index: Int, x: Double, y: Double): Point =
            if (ir.xAxis.kind == AxisKind.Linear) Point(xOf(x, index), yOf(y)) else pointAt(index, y)

        ir.series.forEachIndexed { seriesIndex, series ->
            val color = config.palette[seriesIndex % config.palette.size.coerceAtLeast(1)]
            when (series.kind) {
                SeriesKind.Bar -> {
                    val barW = slot / (ir.series.size + 1f)
                    for (i in series.ys.indices) {
                        val x0 = plot.left + slot * i + barW * seriesIndex + 6f
                        val y = yOf(series.ys[i])
                        out += DrawCommand.FillRect(Rect.ltrb(x0, y, x0 + barW, plot.bottom), color, corner = 3f, z = 3)
                    }
                }
                SeriesKind.Line -> {
                    if (series.ys.isEmpty()) return@forEachIndexed
                    val ops = ArrayList<PathOp>()
                    ops += PathOp.MoveTo(pointOf(0, series.xs[0], series.ys[0]))
                    for (i in 1 until series.ys.size) ops += PathOp.LineTo(pointOf(i, series.xs[i], series.ys[i]))
                    out += DrawCommand.StrokePath(PathCmd(ops), Stroke(width = (config.lineWidth + 0.5f).coerceAtLeast(2f)), color, z = 4)
                    for (i in series.ys.indices) {
                        val point = pointOf(i, series.xs[i], series.ys[i])
                        out += DrawCommand.FillRect(Rect.ltrb(point.x - 3f, point.y - 3f, point.x + 3f, point.y + 3f), color, corner = 3f, z = 5)
                    }
                }
                SeriesKind.Scatter -> {
                    for (i in series.ys.indices) {
                        val point = pointOf(i, series.xs[i], series.ys[i])
                        out += DrawCommand.FillRect(
                            Rect.ltrb(point.x - 4f, point.y - 4f, point.x + 4f, point.y + 4f),
                            color,
                            corner = 4f,
                            z = 5,
                        )
                    }
                }
                SeriesKind.Area,
                -> Unit
            }
        }
        if (config.legend != "none") drawLegend(ir, out, plot, config.palette, config.text)
        return out.entities()
    }

    private fun drawTitles(ir: XYChartIR, laid: LaidOutDiagram, out: MutableList<DrawCommand>, text: Color) {
        laid.nodePositions[NodeId("xychart:title")]?.let { rect ->
            if (!ir.title.isNullOrBlank()) {
                out += DrawCommand.DrawText(ir.title!!, Point(rect.left, rect.top), titleFont, text, anchorY = TextAnchorY.Top, z = 10)
            }
        }
        laid.nodePositions[NodeId("xychart:xTitle")]?.let { rect ->
            val title = (ir.xAxis.title as? RichLabel.Plain)?.text ?: return@let
            out += DrawCommand.DrawText(title, Point(rect.left, rect.top), axisTitleFont, text, anchorY = TextAnchorY.Top, z = 10)
        }
        laid.nodePositions[NodeId("xychart:yTitle")]?.let { rect ->
            val title = (ir.yAxis.title as? RichLabel.Plain)?.text ?: return@let
            out += DrawCommand.DrawText(title, Point(rect.left, rect.top), axisTitleFont, text, anchorY = TextAnchorY.Top, z = 10)
        }
    }

    private fun drawAxes(plot: Rect, out: MutableList<DrawCommand>, axis: Color, lineWidth: Float) {
        out += DrawCommand.StrokePath(
            PathCmd(listOf(PathOp.MoveTo(Point(plot.left, plot.bottom)), PathOp.LineTo(Point(plot.right, plot.bottom)))),
            Stroke(width = lineWidth),
            axis,
            z = 1,
        )
        out += DrawCommand.StrokePath(
            PathCmd(listOf(PathOp.MoveTo(Point(plot.left, plot.top)), PathOp.LineTo(Point(plot.left, plot.bottom)))),
            Stroke(width = lineWidth),
            axis,
            z = 1,
        )
    }

    private fun formatTick(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else (round(value * 100.0) / 100.0).toString()

    private fun drawLegend(ir: XYChartIR, out: MutableList<DrawCommand>, plot: Rect, palette: List<Color>, text: Color) {
        if (ir.series.size <= 1 && ir.series.firstOrNull()?.name?.startsWith("series") == true) return
        val position = ir.styleHints.extras[PlantUmlXYChartParser.STYLE_LEGEND_KEY] ?: "right"
        val origin = when (position) {
            "left" -> Point(12f, plot.top)
            "top" -> Point(plot.left, plot.top - 28f)
            "bottom" -> Point(plot.left, plot.bottom + 42f)
            else -> Point(plot.right + 10f, plot.top)
        }
        var y = origin.y
        for ((idx, series) in ir.series.withIndex()) {
            val swatch = Rect.ltrb(origin.x, y + 2f, origin.x + 14f, y + 14f)
            out += DrawCommand.FillRect(swatch, palette[idx % palette.size], corner = 3f, z = 8)
            out += DrawCommand.DrawText(series.name, Point(swatch.right + 8f, y), axisLabelFont, text, anchorY = TextAnchorY.Top, z = 9)
            y += 20f
        }
    }

    private fun resolveConfig(ir: XYChartIR): RenderConfig {
        val extras = ir.styleHints.extras
        if (cachedExtras != extras) {
            cachedExtras = extras.toMap()
            renderConfig = RenderConfig.from(cachedExtras, baseColors)
        }
        return renderConfig
    }

    private data class RenderConfig(
        val background: Color,
        val axis: Color,
        val text: Color,
        val lineWidth: Float,
        val palette: List<Color>,
        val legend: String,
    ) {
        companion object {
            fun from(extras: Map<String, String>, baseColors: com.hrm.diagram.render.theme.ResolvedXYChartColors): RenderConfig {
                val palette = extras.keys
                    .filter { it.startsWith(PlantUmlXYChartParser.STYLE_SERIES_COLOR_PREFIX) }
                    .sorted()
                    .mapNotNull { PlantUmlTreeRenderSupport.parsePlantUmlColor(extras[it].orEmpty()) }
                return RenderConfig(
                    background = PlantUmlTreeRenderSupport.parsePlantUmlColor(extras[PlantUmlXYChartParser.STYLE_BACKGROUND_KEY].orEmpty()) ?: baseColors.background,
                    axis = PlantUmlTreeRenderSupport.parsePlantUmlColor(extras[PlantUmlXYChartParser.STYLE_AXIS_COLOR_KEY].orEmpty()) ?: baseColors.xAxis,
                    text = PlantUmlTreeRenderSupport.parsePlantUmlColor(extras[PlantUmlXYChartParser.STYLE_TEXT_KEY].orEmpty()) ?: baseColors.text,
                    lineWidth = extras[PlantUmlXYChartParser.STYLE_LINE_THICKNESS_KEY]?.toFloatOrNull() ?: 1.5f,
                    palette = if (palette.isEmpty()) baseColors.plotPalette else palette,
                    legend = extras[PlantUmlXYChartParser.STYLE_LEGEND_KEY] ?: "right",
                )
            }
        }
    }
}
