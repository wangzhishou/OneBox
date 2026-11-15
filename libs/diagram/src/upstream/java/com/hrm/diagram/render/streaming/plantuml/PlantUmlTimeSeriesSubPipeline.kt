package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.ArrowStyle
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
import com.hrm.diagram.core.ir.TimeSeriesIR
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.timeseries.GanttLayout
import com.hrm.diagram.parser.plantuml.PlantUmlGanttParser
import com.hrm.diagram.parser.plantuml.PlantUmlTimingParser
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.theme.ThemeResolver

internal class PlantUmlTimeSeriesSubPipeline(
    private val kind: Kind,
    textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : PlantUmlSubPipeline {
    enum class Kind { Gantt, Timing }

    private companion object {
        const val DAY_MS: Long = 86_400_000L
    }

    private val ganttParser = if (kind == Kind.Gantt) PlantUmlGanttParser() else null
    private val timingParser = if (kind == Kind.Timing) PlantUmlTimingParser() else null
    private val colors = ThemeResolver.resolveTimeSeries(theme)
    private val criticalStrokeColor = Color(0xFFD32F2F.toInt())
    private val layout = GanttLayout(textMeasurer)
    private val kernel = PlantUmlFamilyRenderSubPipelineKernel(
        snapshot = {
            when (kind) {
                Kind.Gantt -> ganttParser!!.snapshot()
                Kind.Timing -> timingParser!!.snapshot()
            }
        },
        diagnostics = {
            when (kind) {
                Kind.Gantt -> ganttParser!!.diagnosticsSnapshot()
                Kind.Timing -> timingParser!!.diagnosticsSnapshot()
            }
        },
        layout = layout::layout,
        renderEntities = ::render,
    )
    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val trackFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val itemFont = FontSpec(family = "sans-serif", sizeSp = 12f)

    override fun acceptLine(line: String): IrPatchBatch =
        when (kind) {
            Kind.Gantt -> ganttParser!!.acceptLine(line)
            Kind.Timing -> timingParser!!.acceptLine(line)
        }

    override fun finish(blockClosed: Boolean): IrPatchBatch =
        when (kind) {
            Kind.Gantt -> ganttParser!!.finish(blockClosed)
            Kind.Timing -> timingParser!!.finish(blockClosed)
        }

    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        kernel.render(previousSnapshot, seq, isFinal)

    private fun render(ir: TimeSeriesIR, laid: LaidOutDiagram): List<com.hrm.diagram.render.cache.DrawEntity> {
        val out = PlantUmlFrameRenderer.sink(model = ir, laidOut = laid)
        out += DrawCommand.FillRect(Rect(Point(0f, 0f), Size(laid.bounds.size.width, laid.bounds.size.height)), colors.slotFill, z = 0)
        val text = colors.labelText
        val isTiming = ir.styleHints.extras["plantuml.timeseries.kind"] == "timing"
        val hideTimingAxis = isTiming && ir.styleHints.extras["timing.hideAxis"] == "true"
        val axis = laid.nodePositions[NodeId("gantt:axis")]
        if (axis != null && !hideTimingAxis) {
            out += DrawCommand.StrokeRect(axis, Stroke(width = 1f), colors.axis, z = 1)
            if (isTiming) drawTimingScale(ir, axis, out) else drawDefaultGrid(axis, out)
            if (!isTiming) drawGanttClosedBands(ir, axis, out)
        }
        laid.nodePositions[NodeId("gantt:title")]?.let { rect ->
            ir.title?.takeIf { it.isNotBlank() }?.let {
                out += DrawCommand.DrawText(it, Point(rect.left, rect.top), titleFont, text, anchorY = TextAnchorY.Top, z = 10)
            }
        }
        for (track in ir.tracks) {
            val r = laid.nodePositions[NodeId("gantt:track:${track.id.value}")] ?: continue
            out += DrawCommand.DrawText(labelText(track.label), Point(r.left, r.top), trackFont, text, anchorY = TextAnchorY.Top, z = 10)
        }
        for (item in ir.items) {
            val bar = laid.nodePositions[NodeId("gantt:item:${item.id.value}")] ?: continue
            val labelRect = laid.nodePositions[NodeId("gantt:itemLabel:${item.id.value}")]
            val timingKind = item.payload["timing.kind"]
            if (isTiming && timingKind == "message") {
                drawTimingMessage(item, bar, laid, out)
                continue
            }
            if (isTiming && timingKind == "constraint") {
                drawTimingConstraint(item, bar, out)
                continue
            }
            if (isTiming && timingKind == "timeLabel") {
                drawTimingTimeLabel(item, bar, laid, out)
                continue
            }
            if (isTiming && item.payload["timing.trackKind"] in setOf("binary", "clock")) {
                drawTimingWaveSegment(item, bar, out)
                continue
            }
            if (isTiming && item.payload["timing.trackKind"] == "robust") {
                drawTimingRobustSegment(item, bar, out)
                continue
            }
            if (isTiming && item.payload["timing.trackKind"] == "concise") {
                drawTimingConciseSegment(item, bar, ir, out)
                continue
            }
            if (!isTiming && item.payload["gantt.kind"] == "milestone") {
                drawGanttMilestone(item, bar, labelRect, out)
                continue
            }
            val color = if (isTiming) stateColor(labelText(item.label)) else itemColor(item.payload["gantt.color"])
            out += DrawCommand.FillRect(bar, color, corner = if (isTiming) 2f else 4f, z = 3)
            if (!isTiming) drawGanttProgress(item, bar, out)
            out += DrawCommand.StrokeRect(bar, ganttStroke(item), ganttStrokeColor(item), corner = if (isTiming) 2f else 4f, z = 4)
            val label = labelText(item.label)
            labelRect?.let {
                out += DrawCommand.DrawText(label, Point(it.left, it.top), itemFont, text, maxWidth = it.size.width, anchorY = TextAnchorY.Top, z = 10)
            }
            if (isTiming) {
                out += DrawCommand.DrawText(
                    label,
                    Point((bar.left + bar.right) / 2f, (bar.top + bar.bottom) / 2f),
                    itemFont,
                    colors.slotFill,
                    maxWidth = bar.size.width - 8f,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Middle,
                    z = 10,
                )
            }
            if (!isTiming) drawGanttNote(item, bar, out)
        }
        return out.entities()
    }

    private fun drawGanttProgress(
        item: com.hrm.diagram.core.ir.TimeItem,
        bar: Rect,
        out: MutableList<DrawCommand>,
    ) {
        val progress = item.payload["gantt.progress"]?.toIntOrNull()?.coerceIn(0, 100) ?: return
        if (progress <= 0) return
        val width = bar.size.width * progress / 100f
        out += DrawCommand.FillRect(Rect(Point(bar.left, bar.top), Size(width, bar.size.height)), colors.doneFill, corner = 4f, z = 5)
        out += DrawCommand.DrawText(
            "$progress%",
            Point((bar.left + bar.right) / 2f, (bar.top + bar.bottom) / 2f),
            itemFont,
            colors.slotFill,
            maxWidth = bar.size.width - 8f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 10,
        )
    }

    private fun drawGanttMilestone(
        item: com.hrm.diagram.core.ir.TimeItem,
        bar: Rect,
        labelRect: Rect?,
        out: MutableList<DrawCommand>,
    ) {
        val center = Point((bar.left + bar.right) / 2f, (bar.top + bar.bottom) / 2f)
        val radius = (minOf(bar.size.height, 18f) / 2f).coerceAtLeast(5f)
        val path = PathCmd(
            listOf(
                PathOp.MoveTo(Point(center.x, center.y - radius)),
                PathOp.LineTo(Point(center.x + radius, center.y)),
                PathOp.LineTo(Point(center.x, center.y + radius)),
                PathOp.LineTo(Point(center.x - radius, center.y)),
                PathOp.Close,
            ),
        )
        val fill = itemColor(item.payload["gantt.color"])
        out += DrawCommand.FillPath(path, fill, z = 4)
        out += DrawCommand.StrokePath(path, ganttStroke(item), ganttStrokeColor(item), z = 5)
        labelRect?.let {
            out += DrawCommand.DrawText(labelText(item.label), Point(it.left, it.top), itemFont, colors.labelText, maxWidth = it.size.width, anchorY = TextAnchorY.Top, z = 10)
        }
        drawGanttNote(item, bar, out)
    }

    private fun drawGanttNote(
        item: com.hrm.diagram.core.ir.TimeItem,
        bar: Rect,
        out: MutableList<DrawCommand>,
    ) {
        val note = item.payload["gantt.note"]?.takeIf { it.isNotBlank() } ?: return
        out += DrawCommand.DrawText(
            note,
            Point(bar.right + 6f, (bar.top + bar.bottom) / 2f),
            itemFont,
            colors.milestoneFill,
            maxWidth = 180f,
            anchorY = TextAnchorY.Middle,
            z = 10,
        )
    }

    private fun ganttStroke(item: com.hrm.diagram.core.ir.TimeItem): Stroke =
        when (item.payload["gantt.style"]?.lowercase()) {
            "dashed" -> Stroke(width = 1.2f, dash = listOf(5f, 3f))
            "bold" -> Stroke(width = 2.2f)
            "critical" -> Stroke(width = 2f)
            else -> Stroke(width = 1f)
        }

    private fun ganttStrokeColor(item: com.hrm.diagram.core.ir.TimeItem): Color =
        if (item.payload["gantt.style"] == "critical") criticalStrokeColor else colors.border

    private fun drawDefaultGrid(axis: Rect, out: MutableList<DrawCommand>) {
        for (i in 0..4) {
            val x = axis.left + axis.size.width * i / 4f
            out += DrawCommand.StrokePath(
                PathCmd(listOf(PathOp.MoveTo(Point(x, axis.top)), PathOp.LineTo(Point(x, axis.bottom)))),
                Stroke.Hairline,
                colors.border,
                z = 1,
            )
        }
    }

    private fun drawGanttClosedBands(ir: TimeSeriesIR, axis: Rect, out: MutableList<DrawCommand>) {
        val ranges = parseClosedRanges(ir.styleHints.extras["gantt.closedRanges"]).toMutableList()
        val closedWeekdays = ir.styleHints.extras["gantt.closedWeekdays"]
            ?.split(',')
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.toSet()
            .orEmpty()
        if (closedWeekdays.isNotEmpty()) {
            var dayStart = floorDay(ir.range.startMs)
            val end = ceilDay(ir.range.endMs)
            var guard = 0
            while (dayStart < end && guard < 4096) {
                if (weekdayIndex(dayStart) in closedWeekdays) {
                    ranges += dayStart to (dayStart + DAY_MS)
                }
                dayStart += DAY_MS
                guard++
            }
        }
        for ((start, end) in ranges) {
            val left = xOf(start.coerceAtLeast(ir.range.startMs), ir, axis)
            val right = xOf(end.coerceAtMost(ir.range.endMs), ir, axis)
            if (right <= left) continue
            out += DrawCommand.FillRect(Rect.ltrb(left, axis.top, right, axis.bottom), Color(0x14FFB74D), z = 1)
            out += DrawCommand.StrokePath(
                PathCmd(listOf(PathOp.MoveTo(Point(left, axis.top)), PathOp.LineTo(Point(left, axis.bottom)))),
                Stroke(width = 1f, dash = listOf(2f, 3f)),
                Color(0x55EF6C00),
                z = 2,
            )
        }
    }

    private fun parseClosedRanges(raw: String?): List<Pair<Long, Long>> =
        raw?.split('|')
            ?.mapNotNull { token ->
                val parts = token.split(':')
                val start = parts.getOrNull(0)?.toLongOrNull()
                val end = parts.getOrNull(1)?.toLongOrNull()
                if (start != null && end != null) start to end else null
            }
            .orEmpty()

    private fun xOf(ms: Long, ir: TimeSeriesIR, axis: Rect): Float {
        val span = (ir.range.endMs - ir.range.startMs).coerceAtLeast(1L).toDouble()
        val t = ((ms - ir.range.startMs).toDouble() / span).coerceIn(0.0, 1.0)
        return (axis.left + axis.size.width * t).toFloat()
    }

    private fun floorDay(ms: Long): Long = floorDiv(ms, DAY_MS) * DAY_MS

    private fun ceilDay(ms: Long): Long {
        val floor = floorDay(ms)
        return if (floor == ms) floor else floor + DAY_MS
    }

    private fun weekdayIndex(dayStartMs: Long): Int {
        val epochDay = floorDiv(dayStartMs, DAY_MS)
        return floorMod(epochDay + 3L, 7L).toInt() + 1
    }

    private fun floorDiv(value: Long, divisor: Long): Long {
        val quotient = value / divisor
        val remainder = value % divisor
        return if (remainder != 0L && (value xor divisor) < 0L) quotient - 1L else quotient
    }

    private fun floorMod(value: Long, divisor: Long): Long {
        val mod = value % divisor
        return if (mod < 0L) mod + divisor else mod
    }

    private fun drawTimingScale(ir: TimeSeriesIR, axis: Rect, out: MutableList<DrawCommand>) {
        val scale = ir.styleHints.extras["timing.scaleMs"]?.toLongOrNull()
        val range = ir.range
        val span = (range.endMs - range.startMs).coerceAtLeast(1L).toDouble()
        fun xOf(ms: Long): Float {
            val t = ((ms - range.startMs).toDouble() / span).coerceIn(0.0, 1.0)
            return (axis.left + axis.size.width * t).toFloat()
        }
        if (scale == null || scale <= 0L) {
            drawDefaultGrid(axis, out)
            return
        }
        val first = ((range.startMs + scale - 1L) / scale) * scale
        var tick = first
        var guard = 0
        while (tick <= range.endMs && guard < 128) {
            val x = xOf(tick)
            out += DrawCommand.StrokePath(
                PathCmd(listOf(PathOp.MoveTo(Point(x, axis.top)), PathOp.LineTo(Point(x, axis.bottom)))),
                Stroke(width = 1f, dash = listOf(3f, 4f)),
                Color(0xFFCFD8DC.toInt()),
                z = 1,
            )
            out += DrawCommand.DrawText(
                text = tickLabel(tick, ir.styleHints.extras["timing.scaleLabel"]),
                origin = Point(x + 4f, axis.top - 16f),
                font = itemFont,
                color = Color(0xFF607D8B.toInt()),
                maxWidth = 90f,
                anchorY = TextAnchorY.Top,
                z = 10,
            )
            tick += scale
            guard++
        }
    }

    private fun tickLabel(tick: Long, scaleLabel: String?): String =
        scaleLabel?.let { "$tick ($it)" } ?: tick.toString()

    private fun drawTimingWaveSegment(
        item: com.hrm.diagram.core.ir.TimeItem,
        bar: Rect,
        out: MutableList<DrawCommand>,
    ) {
        val state = item.payload["timing.state"] ?: labelText(item.label)
        val high = state.lowercase() in setOf("high", "on", "true", "1")
        val yHigh = bar.top + 6f
        val yLow = bar.bottom - 6f
        val y = if (high) yHigh else yLow
        val edgeColor = stateColor(state)
        out += DrawCommand.FillRect(bar, Color(0xFFF5F7FA.toInt()), corner = 2f, z = 2)
        out += DrawCommand.StrokeRect(bar, Stroke.Hairline, Color(0xFFE0E0E0.toInt()), corner = 2f, z = 3)
        out += DrawCommand.StrokePath(
            PathCmd(
                listOf(
                    PathOp.MoveTo(Point(bar.left, if (high) yLow else yHigh)),
                    PathOp.LineTo(Point(bar.left, y)),
                    PathOp.LineTo(Point(bar.right, y)),
                ),
            ),
            Stroke(width = if (item.payload["timing.trackKind"] == "clock") 2f else 1.6f),
            edgeColor,
            z = 8,
        )
    }

    private fun drawTimingConciseSegment(
        item: com.hrm.diagram.core.ir.TimeItem,
        bar: Rect,
        ir: TimeSeriesIR,
        out: MutableList<DrawCommand>,
    ) {
        val state = item.payload["timing.state"] ?: labelText(item.label)
        val display = labelText(item.label)
        val connectedBefore = hasAdjacentConciseSegment(ir, item, before = true)
        val connectedAfter = hasAdjacentConciseSegment(ir, item, before = false)
        val connected = connectedBefore || connectedAfter
        val fill = conciseStateColor(state)
        val stroke = Color(0xFF455A64.toInt())
        out += DrawCommand.FillRect(bar, fill, corner = if (connected) 0f else 6f, z = 3)
        out += DrawCommand.StrokeRect(bar, Stroke(width = 1.1f), stroke, corner = if (connected) 0f else 6f, z = 4)
        if (connectedBefore) {
            boundaryStroke(item)?.let { boundary ->
                out += DrawCommand.StrokePath(
                    PathCmd(listOf(PathOp.MoveTo(Point(bar.left, bar.top + 3f)), PathOp.LineTo(Point(bar.left, bar.bottom - 3f)))),
                    boundary,
                    Color(0xFFFFFFFF.toInt()),
                    z = 5,
                )
            }
        }
        out += DrawCommand.DrawText(
            display,
            Point((bar.left + bar.right) / 2f, (bar.top + bar.bottom) / 2f),
            itemFont,
            Color(0xFFFFFFFF.toInt()),
            maxWidth = bar.size.width - 8f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 10,
        )
    }

    private fun hasAdjacentConciseSegment(
        ir: TimeSeriesIR,
        item: com.hrm.diagram.core.ir.TimeItem,
        before: Boolean,
    ): Boolean =
        ir.items.any {
            it.id != item.id &&
                it.trackId == item.trackId &&
                it.payload["timing.kind"] == null &&
                it.payload["timing.trackKind"] == "concise" &&
                if (before) it.range.endMs == item.range.startMs else it.range.startMs == item.range.endMs
        }

    private fun boundaryStroke(item: com.hrm.diagram.core.ir.TimeItem): Stroke? =
        when (item.payload["timing.boundary"]?.lowercase()) {
            "none" -> null
            "dashed" -> Stroke(width = 1.4f, dash = listOf(3f, 2f))
            "thick" -> Stroke(width = 2.2f)
            else -> Stroke(width = 1.4f)
        }

    private fun drawTimingRobustSegment(
        item: com.hrm.diagram.core.ir.TimeItem,
        bar: Rect,
        out: MutableList<DrawCommand>,
    ) {
        val state = item.payload["timing.state"] ?: labelText(item.label)
        val display = labelText(item.label)
        val accent = robustStateColor(state)
        out += DrawCommand.FillRect(bar, Color(0xFFF3F0FF.toInt()), corner = 8f, z = 3)
        out += DrawCommand.StrokeRect(bar, Stroke(width = 1.8f), accent, corner = 8f, z = 4)
        out += DrawCommand.StrokePath(
            PathCmd(
                listOf(
                    PathOp.MoveTo(Point(bar.left + 5f, bar.top + 4f)),
                    PathOp.LineTo(Point(bar.left + 5f, bar.bottom - 4f)),
                    PathOp.MoveTo(Point(bar.right - 5f, bar.top + 4f)),
                    PathOp.LineTo(Point(bar.right - 5f, bar.bottom - 4f)),
                ),
            ),
            Stroke(width = 1.4f),
            accent,
            z = 5,
        )
        out += DrawCommand.DrawText(
            display,
            Point((bar.left + bar.right) / 2f, (bar.top + bar.bottom) / 2f),
            itemFont,
            accent,
            maxWidth = bar.size.width - 12f,
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Middle,
            z = 10,
        )
    }

    private fun drawTimingMessage(
        item: com.hrm.diagram.core.ir.TimeItem,
        marker: Rect,
        laid: LaidOutDiagram,
        out: MutableList<DrawCommand>,
    ) {
        val fromTrack = item.payload["timing.from"] ?: return
        val toTrack = item.payload["timing.to"] ?: return
        val fromRect = laid.nodePositions[NodeId("gantt:track:timing:track:$fromTrack")]
        val toRect = laid.nodePositions[NodeId("gantt:track:timing:track:$toTrack")]
        val axis = laid.nodePositions[NodeId("gantt:axis")]
        val markerCenterX = (marker.left + marker.right) / 2f
        val markerCenterY = (marker.top + marker.bottom) / 2f
        val startY = fromRect?.let { it.bottom + 14f } ?: markerCenterY
        val endY = toRect?.let { it.bottom + 14f } ?: markerCenterY + 26f
        val x = markerCenterX
        val from = Point(x, startY)
        val to = Point(x, endY)
        out += DrawCommand.DrawArrow(
            from = from,
            to = to,
            style = ArrowStyle(color = Color(0xFF455A64.toInt()), stroke = Stroke(width = 1.2f)),
            z = 8,
        )
        out += DrawCommand.DrawText(
            text = labelText(item.label),
            origin = Point((x + 6f).coerceAtMost((axis?.right ?: x + 120f) - 12f), (startY + endY) / 2f),
            font = itemFont,
            color = Color(0xFF263238.toInt()),
            maxWidth = 140f,
            anchorY = TextAnchorY.Middle,
            z = 10,
        )
    }

    private fun drawTimingConstraint(
        item: com.hrm.diagram.core.ir.TimeItem,
        bar: Rect,
        out: MutableList<DrawCommand>,
    ) {
        val color = Color(0xFF6D4C41.toInt())
        val midY = (bar.top + bar.bottom) / 2f
        out += DrawCommand.StrokePath(
            PathCmd(
                listOf(
                    PathOp.MoveTo(Point(bar.left, midY)),
                    PathOp.LineTo(Point(bar.right, midY)),
                ),
            ),
            Stroke(width = 1.2f, dash = listOf(5f, 4f)),
            color,
            z = 7,
        )
        out += DrawCommand.StrokePath(
            PathCmd(
                listOf(
                    PathOp.MoveTo(Point(bar.left, bar.top + 4f)),
                    PathOp.LineTo(Point(bar.left, bar.bottom - 4f)),
                    PathOp.MoveTo(Point(bar.right, bar.top + 4f)),
                    PathOp.LineTo(Point(bar.right, bar.bottom - 4f)),
                ),
            ),
            Stroke(width = 1.2f),
            color,
            z = 7,
        )
        out += DrawCommand.DrawText(
            labelText(item.label),
            Point((bar.left + bar.right) / 2f, bar.top - 4f),
            itemFont,
            color,
            maxWidth = (bar.size.width - 8f).coerceAtLeast(40f),
            anchorX = TextAnchorX.Center,
            anchorY = TextAnchorY.Bottom,
            z = 10,
        )
    }

    private fun drawTimingTimeLabel(
        item: com.hrm.diagram.core.ir.TimeItem,
        marker: Rect,
        laid: LaidOutDiagram,
        out: MutableList<DrawCommand>,
    ) {
        val axis = laid.nodePositions[NodeId("gantt:axis")]
        val x = (marker.left + marker.right) / 2f
        val top = axis?.top ?: marker.top
        val bottom = axis?.bottom ?: marker.bottom
        val color = Color(0xFF0277BD.toInt())
        out += DrawCommand.StrokePath(
            PathCmd(listOf(PathOp.MoveTo(Point(x, top)), PathOp.LineTo(Point(x, bottom)))),
            Stroke(width = 1.2f, dash = listOf(2f, 3f)),
            color,
            z = 7,
        )
        out += DrawCommand.DrawText(labelText(item.label), Point(x + 4f, top + 4f), itemFont, color, maxWidth = 140f, anchorY = TextAnchorY.Top, z = 10)
    }

    private fun labelText(label: RichLabel): String =
        when (label) {
            is RichLabel.Plain -> label.text
            is RichLabel.Markdown -> label.source
            is RichLabel.Html -> label.html
        }

    private fun stateColor(state: String): Color =
        when (state.lowercase()) {
            "high", "on", "true", "1" -> Color(0xFF43A047.toInt())
            "low", "off", "false", "0" -> Color(0xFFE53935.toInt())
            else -> Color(0xFF5C6BC0.toInt())
        }

    private fun robustStateColor(state: String): Color =
        when (state.lowercase()) {
            "idle", "ready", "available" -> Color(0xFF2E7D32.toInt())
            "busy", "processing", "running" -> Color(0xFF6A1B9A.toInt())
            "error", "failed", "down" -> Color(0xFFC62828.toInt())
            else -> Color(0xFF512DA8.toInt())
        }

    private fun conciseStateColor(state: String): Color {
        val palette = listOf(
            Color(0xFF5C6BC0.toInt()),
            Color(0xFF00897B.toInt()),
            Color(0xFFEF6C00.toInt()),
            Color(0xFF7CB342.toInt()),
            Color(0xFF8E24AA.toInt()),
            Color(0xFF039BE5.toInt()),
        )
        val idx = ((state.lowercase().hashCode() % palette.size) + palette.size) % palette.size
        return palette[idx]
    }

    private fun itemColor(raw: String?): Color {
        val value = raw?.trim().orEmpty()
        if (value.startsWith("#")) {
            val hex = value.drop(1)
            if (hex.length == 6) hex.toLongOrNull(16)?.let { return Color((0xFF000000 or it).toInt()) }
            if (hex.length == 8) hex.toLongOrNull(16)?.let { return Color(it.toInt()) }
        }
        return when (value.lowercase()) {
            "red" -> Color(0xFFE53935.toInt())
            "green", "lime" -> Color(0xFF43A047.toInt())
            "blue" -> Color(0xFF1E88E5.toInt())
            "yellow" -> Color(0xFFFDD835.toInt())
            "orange" -> Color(0xFFFB8C00.toInt())
            "purple" -> Color(0xFF8E24AA.toInt())
            "gray", "grey" -> Color(0xFF78909C.toInt())
            else -> Color(0xFF42A5F5.toInt())
        }
    }
}
