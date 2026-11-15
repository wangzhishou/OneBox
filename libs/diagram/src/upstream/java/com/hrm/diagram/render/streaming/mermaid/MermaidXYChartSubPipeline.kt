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
import com.hrm.diagram.core.draw.Transform
import com.hrm.diagram.core.ir.Direction
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.SeriesKind
import com.hrm.diagram.core.ir.XYChartIR
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.xy.XYChartLayout
import com.hrm.diagram.parser.mermaid.MermaidXYChartParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ResolvedXYChartColors
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.truncate

internal class MermaidXYChartSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private var styleExtras: Map<String, String> = emptyMap()
    private val baseColors = ThemeResolver.resolveXYChart(theme)
    private var renderConfig = RenderConfig.from(baseColors, emptyMap(), emptyMap())

    override fun updateStyleExtras(extras: Map<String, String>) {
        styleExtras = extras
        renderConfig = RenderConfig.from(baseColors, styleExtras, emptyMap())
    }

    private val parser = MermaidXYChartParser()
    private val layout = XYChartLayout(textMeasurer)
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
        layoutOptions = { model, isFinal ->
            LayoutOptions(direction = model.styleHints.direction, incremental = !isFinal, allowGlobalReflow = isFinal)
        },
    )
    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val axisTitleFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val axisLabelFont = FontSpec(family = "sans-serif", sizeSp = 11f)

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun render(ir: XYChartIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)
        val bounds = laid.bounds
        val plot = laid.nodePositions[NodeId("xychart:plot")] ?: return emptyList()
        val horizontal = ir.styleHints.extras["xyChart.orientation"] == "horizontal" || ir.styleHints.direction == Direction.LR
        val config = renderConfig.mergeIr(ir.styleHints.extras)

        out += DrawCommand.FillRect(rect = Rect(Point(0f, 0f), Size(bounds.size.width, bounds.size.height)), color = config.background, corner = 0f, z = 0)

        laid.nodePositions[NodeId("xychart:title")]?.let { r ->
            if (!ir.title.isNullOrBlank()) {
                out += DrawCommand.DrawText(ir.title!!, Point(r.left, r.top), titleFont, config.text, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Top, z = 10)
            }
        }
        laid.nodePositions[NodeId("xychart:xTitle")]?.let { r ->
            val t = (ir.xAxis.title as? RichLabel.Plain)?.text ?: return@let
            out += DrawCommand.DrawText(t, Point(r.left, r.top), axisTitleFont, config.xAxisTitle, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Top, z = 10)
        }
        laid.nodePositions[NodeId("xychart:yTitle")]?.let { r ->
            val t = (ir.yAxis.title as? RichLabel.Plain)?.text ?: return@let
            out += DrawCommand.Group(
                transform = Transform(
                    translate = Point((r.left + r.right) / 2f, (r.top + r.bottom) / 2f),
                    rotateDeg = -90f,
                ),
                children = listOf(
                    DrawCommand.DrawText(
                        t,
                        Point(0f, 0f),
                        axisTitleFont,
                        config.yAxisTitle,
                        anchorX = TextAnchorX.Center,
                        anchorY = TextAnchorY.Middle,
                        z = 10,
                    ),
                ),
                z = 10,
            )
        }

        // Axes.
        out += DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(Point(plot.left, plot.bottom)), PathOp.LineTo(Point(plot.right, plot.bottom)))),
            stroke = Stroke(width = 1.5f),
            color = config.xAxis,
            z = 1,
        )
        out += DrawCommand.StrokePath(
            path = PathCmd(listOf(PathOp.MoveTo(Point(plot.left, plot.top)), PathOp.LineTo(Point(plot.left, plot.bottom)))),
            stroke = Stroke(width = 1.5f),
            color = config.yAxis,
            z = 1,
        )

        // Y ticks / labels.
        val yMin = ir.yAxis.min ?: 0.0
        val yMax = ir.yAxis.max ?: 1.0
        val yTicks = 5
        for (i in 0..yTicks) {
            val t = i.toFloat() / yTicks.toFloat()
            val y = plot.bottom - (plot.size.height * t)
            out += DrawCommand.StrokePath(
                path = PathCmd(listOf(PathOp.MoveTo(Point(plot.left - 4f, y)), PathOp.LineTo(Point(plot.right, y)))),
                stroke = Stroke(width = if (i == 0) 1.5f else 0.75f, dash = if (i == 0) null else listOf(4f, 4f)),
                color = config.yAxis,
                z = 1,
            )
            val v = yMin + (yMax - yMin) * t.toDouble()
            val txt = formatTick(v)
            val labelRect = laid.nodePositions[NodeId("xychart:yLabel:$i")]
            if (labelRect != null) {
                out += DrawCommand.DrawText(txt, Point(labelRect.left, y), axisLabelFont, config.yAxisLabel, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Middle, z = 10)
            }
        }

        val itemCount = when {
            ir.xAxis.kind == com.hrm.diagram.core.ir.AxisKind.Category -> ir.xAxis.categories.size
            else -> ir.series.maxOfOrNull { it.ys.size } ?: 0
        }
        if (itemCount <= 0) return out.entities()

        val slot = plot.size.width / itemCount.toFloat()
        for (i in 0 until itemCount) {
            val x = plot.left + slot * (i + 0.5f)
            val label = ir.xAxis.categories.getOrNull(i)
            if (label != null) {
                out += DrawCommand.DrawText(label, Point(x, plot.bottom + 18f), axisLabelFont, config.xAxisLabel, maxWidth = slot - 8f, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Top, z = 10)
            }
        }

        fun yOf(v: Double): Float {
            val denom = ((yMax - yMin).takeIf { it != 0.0 } ?: 1.0)
            val t = ((v - yMin) / denom).coerceIn(0.0, 1.0)
            return (plot.bottom - plot.size.height * t).toFloat()
        }

        fun pointAt(i: Int, v: Double): Point {
            val x = plot.left + slot * (i + 0.5f)
            val y = yOf(v)
            return if (!horizontal) Point(x, y) else Point(plot.left + (plot.size.width * ((v - yMin) / ((yMax - yMin).takeIf { it != 0.0 } ?: 1.0))).toFloat(), plot.top + (plot.size.height / itemCount.toFloat()) * (i + 0.5f))
        }

        ir.series.forEachIndexed { sIdx, s ->
            val color = config.plotPalette[sIdx % config.plotPalette.size.coerceAtLeast(1)]
            when (s.kind) {
                SeriesKind.Bar -> {
                    val barW = slot / (ir.series.size + 1f)
                    for (i in s.ys.indices) {
                        val x0 = plot.left + slot * i + barW * sIdx + 6f
                        val y = yOf(s.ys[i])
                        val rect = if (!horizontal) Rect.ltrb(x0, y, x0 + barW, plot.bottom) else Rect.ltrb(plot.left, plot.top + (plot.size.height / itemCount.toFloat()) * i + 6f, pointAt(i, s.ys[i]).x, plot.top + (plot.size.height / itemCount.toFloat()) * (i + 1) - 6f)
                        out += DrawCommand.FillRect(rect = rect, color = color, corner = 3f, z = 3)
                        if (config.showDataLabel) {
                            val anchorY = if (!horizontal && config.showDataLabelOutsideBar) rect.top - 4f else (rect.top + rect.bottom) / 2f
                            val textAnchorY = if (!horizontal && config.showDataLabelOutsideBar) TextAnchorY.Bottom else TextAnchorY.Middle
                            out += DrawCommand.DrawText(
                                text = formatTick(s.ys[i]),
                                origin = Point((rect.left + rect.right) / 2f, anchorY),
                                font = axisLabelFont,
                                color = config.dataLabel,
                                anchorX = TextAnchorX.Center,
                                anchorY = textAnchorY,
                                z = 10,
                            )
                        }
                    }
                }
                SeriesKind.Line -> {
                    if (s.ys.isEmpty()) return@forEachIndexed
                    val ops = ArrayList<PathOp>()
                    ops += PathOp.MoveTo(pointAt(0, s.ys[0]))
                    for (i in 1 until s.ys.size) ops += PathOp.LineTo(pointAt(i, s.ys[i]))
                    out += DrawCommand.StrokePath(path = PathCmd(ops), stroke = Stroke(width = 2f), color = color, z = 4)
                    if (config.showDataLabel) {
                        for (i in s.ys.indices) {
                            val p = pointAt(i, s.ys[i])
                            out += DrawCommand.DrawText(
                                text = formatTick(s.ys[i]),
                                origin = Point(p.x, p.y - 6f),
                                font = axisLabelFont,
                                color = config.dataLabel,
                                anchorX = TextAnchorX.Center,
                                anchorY = TextAnchorY.Bottom,
                                z = 10,
                            )
                        }
                    }
                }
                SeriesKind.Scatter -> {
                    for (i in s.ys.indices) {
                        val p = pointAt(i, s.ys[i])
                        val r = Rect.ltrb(p.x - 4f, p.y - 4f, p.x + 4f, p.y + 4f)
                        out += DrawCommand.FillRect(rect = r, color = color, corner = 4f, z = 5)
                        if (config.showDataLabel) {
                            out += DrawCommand.DrawText(
                                text = formatTick(s.ys[i]),
                                origin = Point(p.x, p.y - 6f),
                                font = axisLabelFont,
                                color = config.dataLabel,
                                anchorX = TextAnchorX.Center,
                                anchorY = TextAnchorY.Bottom,
                                z = 10,
                            )
                        }
                    }
                }
                SeriesKind.Area -> {
                    if (s.ys.isEmpty()) return@forEachIndexed
                    val ops = ArrayList<PathOp>()
                    val start = pointAt(0, s.ys[0])
                    ops += PathOp.MoveTo(Point(start.x, plot.bottom))
                    ops += PathOp.LineTo(start)
                    for (i in 1 until s.ys.size) ops += PathOp.LineTo(pointAt(i, s.ys[i]))
                    val end = pointAt(s.ys.lastIndex, s.ys.last())
                    ops += PathOp.LineTo(Point(end.x, plot.bottom))
                    ops += PathOp.Close
                    out += DrawCommand.FillPath(path = PathCmd(ops), color = Color.argb(config.areaAlpha, color.r, color.g, color.b), z = 2)
                }
            }
        }
        return out.entities()
    }

    private fun formatTick(v: Double): String {
        val rounded = truncate(v + if (v >= 0.0) 0.5 else -0.5)
        if (abs(v - rounded) < 1e-4) return rounded.toInt().toString()
        val scaled = truncate(v * 100.0 + if (v >= 0.0) 0.5 else -0.5) / 100.0
        return scaled.toString()
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }

    private data class RenderConfig(
        val background: Color,
        val xAxis: Color,
        val yAxis: Color,
        val text: Color,
        val xAxisLabel: Color,
        val yAxisLabel: Color,
        val xAxisTitle: Color,
        val yAxisTitle: Color,
        val dataLabel: Color,
        val plotPalette: List<Color>,
        val areaAlpha: Int,
        val showDataLabel: Boolean,
        val showDataLabelOutsideBar: Boolean,
    ) {
        fun mergeIr(irExtras: Map<String, String>): RenderConfig = copy(
            showDataLabel = showDataLabel || irExtras["xyChart.showDataLabel"]?.lowercase() == "true",
            showDataLabelOutsideBar = showDataLabelOutsideBar || irExtras["xyChart.showDataLabelOutsideBar"]?.lowercase() == "true",
        )

        companion object {
            fun from(
                base: ResolvedXYChartColors,
                styleExtras: Map<String, String>,
                irExtras: Map<String, String>,
            ): RenderConfig {
                val themeRaw = MermaidRenderThemeUtils.decodeRawThemeTokens(styleExtras["mermaid.themeTokens"])
                val customPalette = themeRaw["plotColorPalette"]
                    ?.split(',')
                    ?.mapNotNull { MermaidRenderThemeUtils.parseThemeColor(it.trim()) }
                    .orEmpty()
                return RenderConfig(
                    background = MermaidRenderThemeUtils.parseThemeColor(themeRaw["backgroundColor"]) ?: base.background,
                    xAxis = MermaidRenderThemeUtils.parseThemeColor(themeRaw["xAxisLineColor"]) ?: base.xAxis,
                    yAxis = MermaidRenderThemeUtils.parseThemeColor(themeRaw["yAxisLineColor"]) ?: base.yAxis,
                    text = MermaidRenderThemeUtils.parseThemeColor(themeRaw["titleColor"]) ?: base.text,
                    xAxisLabel = MermaidRenderThemeUtils.parseThemeColor(themeRaw["xAxisLabelColor"]) ?: base.xAxisLabel,
                    yAxisLabel = MermaidRenderThemeUtils.parseThemeColor(themeRaw["yAxisLabelColor"]) ?: base.yAxisLabel,
                    xAxisTitle = MermaidRenderThemeUtils.parseThemeColor(themeRaw["xAxisTitleColor"]) ?: base.xAxisTitle,
                    yAxisTitle = MermaidRenderThemeUtils.parseThemeColor(themeRaw["yAxisTitleColor"]) ?: base.yAxisTitle,
                    dataLabel = MermaidRenderThemeUtils.parseThemeColor(themeRaw["dataLabelColor"]) ?: base.dataLabel,
                    plotPalette = if (customPalette.isEmpty()) base.plotPalette else customPalette,
                    areaAlpha = base.areaAlpha,
                    showDataLabel = styleExtras["mermaid.config.xyChart.showDataLabel"]?.lowercase() == "true" ||
                        irExtras["xyChart.showDataLabel"]?.lowercase() == "true",
                    showDataLabelOutsideBar = styleExtras["mermaid.config.xyChart.showDataLabelOutsideBar"]?.lowercase() == "true" ||
                        irExtras["xyChart.showDataLabelOutsideBar"]?.lowercase() == "true",
                )
            }
        }
    }
}
