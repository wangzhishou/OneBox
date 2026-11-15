package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.draw.Color
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.core.draw.Size
import com.hrm.diagram.core.draw.Stroke
import com.hrm.diagram.core.draw.TextAnchorX
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.TimeSeriesIR
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.timeseries.GanttAxisSupport
import com.hrm.diagram.layout.timeseries.GanttLayout
import com.hrm.diagram.parser.mermaid.MermaidGanttParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ThemeResolver
import kotlin.math.abs

internal class MermaidGanttSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidGanttParser()
    private val colors = ThemeResolver.resolveTimeSeries(theme)
    private val layout = GanttLayout(textMeasurer)
    private var styleExtras: Map<String, String> = emptyMap()
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        transformModel = ::applyStyleExtras,
        layout = layout::layout,
        renderEntities = ::render,
    )

    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val trackFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val itemFont = FontSpec(family = "sans-serif", sizeSp = 12f)
    private val axisFont = FontSpec(family = "sans-serif", sizeSp = 11f)

    override fun updateStyleExtras(extras: Map<String, String>) {
        styleExtras = extras
    }

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun applyStyleExtras(baseIr: TimeSeriesIR): TimeSeriesIR {
        val mergedExtras = baseIr.styleHints.extras + styleExtras
            .filterKeys { it.startsWith("mermaid.config.gantt.") }
            .mapKeys { it.key.removePrefix("mermaid.config.") }
        return baseIr.copy(styleHints = baseIr.styleHints.copy(extras = mergedExtras))
    }

    private fun render(ir: TimeSeriesIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)
        val bounds = laid.bounds

        val bg = colors.slotFill
        val text = colors.labelText
        val axisStroke = colors.axis
        val barStroke = colors.border
        val doneFill = colors.doneFill
        val activeFill = colors.activeFill
        val critFill = colors.criticalFill
        val normalFill = colors.normalFill
        val milestoneFill = colors.milestoneFill
        val gridColor = colors.border
        val majorGridColor = colors.axis
        val scaleBg = colors.slotFill
        val sectionBg = colors.alternateRowBackground
        val rowAltBg = colors.alternateRowBackground

        out += DrawCommand.FillRect(
            rect = Rect(Point(0f, 0f), Size(bounds.size.width, bounds.size.height)),
            color = bg,
            corner = 0f,
            z = 0,
        )

        // Title.
        val titleRect = laid.nodePositions[NodeId("gantt:title")]
        if (titleRect != null && !ir.title.isNullOrBlank()) {
            out += DrawCommand.DrawText(
                text = ir.title!!,
                origin = Point(titleRect.left, titleRect.top),
                font = titleFont,
                color = text,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 10,
            )
        }

        // Axis frame.
        val axis = laid.nodePositions[NodeId("gantt:axis")]
        if (axis != null) {
            out += DrawCommand.StrokeRect(rect = axis, stroke = Stroke(width = 1f), color = axisStroke, corner = 0f, z = 1)
            val axisFormat = ir.styleHints.extras["gantt.axisFormat"] ?: "%Y-%m-%d"
            val tickInterval = GanttAxisSupport.parseTickInterval(ir.styleHints.extras["gantt.tickInterval"], ir.range.endMs - ir.range.startMs)
            val ticks = GanttAxisSupport.buildTicks(ir.range.startMs, ir.range.endMs, tickInterval)
            val span = (ir.range.endMs - ir.range.startMs).coerceAtLeast(1L).toDouble()
            fun xOf(ms: Long): Float {
                val t = ((ms - ir.range.startMs).toDouble() / span).coerceIn(0.0, 1.0)
                return (axis.left + axis.size.width * t).toFloat()
            }
            val scaleTop = axis.top - 40f
            val scaleSplitY = axis.top - 20f
            out += DrawCommand.FillRect(
                rect = Rect.ltrb(axis.left, scaleTop, axis.right, axis.top),
                color = scaleBg,
                z = 1,
            )
            out += DrawCommand.StrokePath(
                path = com.hrm.diagram.core.draw.PathCmd(
                    listOf(
                        com.hrm.diagram.core.draw.PathOp.MoveTo(Point(axis.left, scaleSplitY)),
                        com.hrm.diagram.core.draw.PathOp.LineTo(Point(axis.right, scaleSplitY)),
                    ),
                ),
                stroke = Stroke.Hairline,
                color = gridColor,
                z = 2,
            )
            out += DrawCommand.StrokePath(
                path = com.hrm.diagram.core.draw.PathCmd(
                    listOf(
                        com.hrm.diagram.core.draw.PathOp.MoveTo(Point(axis.left, axis.top)),
                        com.hrm.diagram.core.draw.PathOp.LineTo(Point(axis.right, axis.top)),
                    ),
                ),
                stroke = Stroke(width = 1f),
                color = majorGridColor,
                z = 2,
            )
            for ((index, tick) in ticks.withIndex()) {
                val x = xOf(tick)
                val labelRect = laid.nodePositions[NodeId("gantt:axisTickLabel:$tick")]
                out += DrawCommand.StrokePath(
                    path = com.hrm.diagram.core.draw.PathCmd(
                        listOf(
                            com.hrm.diagram.core.draw.PathOp.MoveTo(Point(x, if (labelRect != null) scaleTop else axis.top)),
                            com.hrm.diagram.core.draw.PathOp.LineTo(Point(x, axis.bottom)),
                        ),
                    ),
                    stroke = if (labelRect != null) Stroke(width = 1f) else Stroke.Hairline,
                    color = if (labelRect != null) majorGridColor else gridColor,
                    z = 1,
                )
                if (labelRect != null) {
                    out += DrawCommand.DrawText(
                        text = GanttAxisSupport.formatMinorTick(tick, axisFormat, tickInterval),
                        origin = Point((labelRect.left + labelRect.right) / 2f, labelRect.bottom),
                        font = axisFont,
                        color = text,
                        anchorX = TextAnchorX.Center,
                        anchorY = TextAnchorY.Bottom,
                        z = 10,
                    )
                }
                if (index == ticks.lastIndex) {
                    out += DrawCommand.StrokePath(
                        path = com.hrm.diagram.core.draw.PathCmd(
                            listOf(
                                com.hrm.diagram.core.draw.PathOp.MoveTo(Point(x, scaleTop)),
                                com.hrm.diagram.core.draw.PathOp.LineTo(Point(x, axis.bottom)),
                            ),
                        ),
                        stroke = Stroke(width = 1f),
                        color = majorGridColor,
                        z = 2,
                    )
                }
            }
            for (label in axisMajorLabels(ticks, tickInterval)) {
                val labelRect = laid.nodePositions[label.first] ?: continue
                out += DrawCommand.DrawText(
                    text = label.second,
                    origin = Point((labelRect.left + labelRect.right) / 2f, labelRect.bottom),
                    font = axisFont,
                    color = text,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Bottom,
                    z = 10,
                )
            }
        }

        // Track headers + items.
        for ((trackIndex, track) in ir.tracks.withIndex()) {
            val headerId = NodeId("gantt:track:${track.id.value}")
            val headerRect = laid.nodePositions[headerId]
            if (headerRect != null) {
                axis?.let {
                    out += DrawCommand.FillRect(
                        rect = Rect.ltrb(headerRect.left - 6f, headerRect.top - 5f, it.right, headerRect.bottom + 5f),
                        color = sectionBg,
                        z = 0,
                    )
                }
                val headerText = (track.label as? RichLabel.Plain)?.text ?: track.id.value
                out += DrawCommand.DrawText(
                    text = headerText,
                    origin = Point(headerRect.left, headerRect.top),
                    font = trackFont,
                    color = text,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    z = 10,
                )
            }

            val items = ir.items.filter { it.trackId == track.id }
            for ((itemIndex, it) in items.filter { it.payload["gantt.kind"] != "vert" }.withIndex()) {
                val barRect = laid.nodePositions[NodeId("gantt:item:${it.id.value}")] ?: continue
                val labelRect = laid.nodePositions[NodeId("gantt:itemLabel:${it.id.value}")]
                if ((trackIndex + itemIndex) % 2 == 1) {
                    val rowTop = kotlin.math.min(labelRect?.top ?: barRect.top, barRect.top) - 4f
                    val rowBottom = kotlin.math.max(labelRect?.bottom ?: barRect.bottom, barRect.bottom) + 4f
                    out += DrawCommand.FillRect(
                        rect = Rect.ltrb((labelRect?.left ?: barRect.left) - 6f, rowTop, axis?.right ?: barRect.right, rowBottom),
                        color = rowAltBg,
                        z = 0,
                    )
                }

                val tags = it.payload["gantt.tags"]?.split(',')?.map { s -> s.trim() }?.filter { s -> s.isNotEmpty() }?.toSet().orEmpty()
                val isMilestone = it.payload["gantt.kind"] == "milestone" || ("milestone" in tags)
                val fill = when {
                    "crit" in tags -> critFill
                    "active" in tags -> activeFill
                    "done" in tags -> doneFill
                    else -> if (isMilestone) milestoneFill else normalFill
                }

                if (isMilestone) {
                    // Render milestone as a small diamond centered on barRect.
                    val cx = (barRect.left + barRect.right) / 2f
                    val cy = (barRect.top + barRect.bottom) / 2f
                    val r = 7f
                    val path = com.hrm.diagram.core.draw.PathCmd(
                        listOf(
                            com.hrm.diagram.core.draw.PathOp.MoveTo(Point(cx, cy - r)),
                            com.hrm.diagram.core.draw.PathOp.LineTo(Point(cx + r, cy)),
                            com.hrm.diagram.core.draw.PathOp.LineTo(Point(cx, cy + r)),
                            com.hrm.diagram.core.draw.PathOp.LineTo(Point(cx - r, cy)),
                            com.hrm.diagram.core.draw.PathOp.Close,
                        ),
                    )
                    out += DrawCommand.FillPath(path = path, color = fill, z = 3)
                    out += DrawCommand.StrokePath(path = path, stroke = Stroke(width = 1f), color = barStroke, z = 4)
                } else {
                    out += DrawCommand.FillRect(rect = barRect, color = fill, corner = 4f, z = 3)
                    out += DrawCommand.StrokeRect(rect = barRect, stroke = Stroke(width = 1f), color = barStroke, corner = 4f, z = 4)
                }
                it.payload["gantt.href"]?.takeIf { href -> href.isNotBlank() }?.let { href ->
                    out += DrawCommand.Hyperlink(href = href, rect = barRect, z = 5)
                }

                val labelText = (it.label as? RichLabel.Plain)?.text ?: it.id.value
                if (labelRect != null) {
                    out += DrawCommand.DrawText(
                        text = labelText,
                        origin = Point(labelRect.left, labelRect.top),
                        font = itemFont,
                        color = text,
                        anchorX = TextAnchorX.Start,
                        anchorY = TextAnchorY.Top,
                        z = 10,
                        maxWidth = labelRect.size.width,
                    )
                }
            }
        }

        axis?.let {
            out += DrawCommand.StrokePath(
                path = com.hrm.diagram.core.draw.PathCmd(
                    listOf(
                        com.hrm.diagram.core.draw.PathOp.MoveTo(Point(it.left, scaleBgTop(it))),
                        com.hrm.diagram.core.draw.PathOp.LineTo(Point(it.left, it.bottom)),
                    ),
                ),
                stroke = Stroke(width = 1f),
                color = majorGridColor,
                z = 5,
            )
        }

        for (item in ir.items.filter { it.payload["gantt.kind"] == "vert" }) {
            val markerRect = laid.nodePositions[NodeId("gantt:vert:${item.id.value}")] ?: continue
            val labelRect = laid.nodePositions[NodeId("gantt:vertLabel:${item.id.value}")]
            val centerX = (markerRect.left + markerRect.right) / 2f
            out += DrawCommand.StrokePath(
                path = com.hrm.diagram.core.draw.PathCmd(
                    listOf(
                        com.hrm.diagram.core.draw.PathOp.MoveTo(Point(centerX, markerRect.top)),
                        com.hrm.diagram.core.draw.PathOp.LineTo(Point(centerX, markerRect.bottom)),
                    ),
                ),
                stroke = Stroke(width = 1.5f, dash = listOf(6f, 4f)),
                color = colors.criticalFill,
                z = 6,
            )
            if (labelRect != null) {
                val labelText = (item.label as? RichLabel.Plain)?.text ?: item.id.value
                val bubble = Rect.ltrb(labelRect.left - 5f, labelRect.top - 3f, labelRect.right + 5f, labelRect.bottom + 3f)
                out += DrawCommand.FillRect(
                    rect = bubble,
                    color = colors.slotFill,
                    corner = 4f,
                    z = 8,
                )
                out += DrawCommand.StrokeRect(
                    rect = bubble,
                    stroke = Stroke(width = 1f),
                    color = colors.criticalFill,
                    corner = 4f,
                    z = 9,
                )
                out += DrawCommand.DrawText(
                    text = labelText,
                    origin = Point((labelRect.left + labelRect.right) / 2f, labelRect.top),
                    font = itemFont,
                    color = colors.criticalFill,
                    anchorX = TextAnchorX.Center,
                    anchorY = TextAnchorY.Top,
                    z = 10,
                    maxWidth = labelRect.size.width,
                )
            }
        }

        return out.entities()
    }

    private fun scaleBgTop(axis: Rect): Float = axis.top - 40f

    private fun axisMajorLabels(
        ticks: List<Long>,
        tickInterval: Long,
    ): List<Pair<NodeId, String>> {
        if (ticks.size < 2) return emptyList()
        val out = ArrayList<Pair<NodeId, String>>()
        var groupStartIndex = 0
        var groupKey = GanttAxisSupport.majorTickKey(ticks.first(), tickInterval)
        for (i in 1 until ticks.size) {
            val key = if (i < ticks.lastIndex) GanttAxisSupport.majorTickKey(ticks[i], tickInterval) else null
            if (key == groupKey) continue
            val start = ticks[groupStartIndex]
            val id = NodeId("gantt:axisMajorLabel:${groupKey}:$start")
            out += id to GanttAxisSupport.formatMajorTick(start, tickInterval)
            groupStartIndex = i
            groupKey = key ?: groupKey
        }
        return out
    }

    private fun parseTickInterval(raw: String?, spanMs: Long): Long {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) return defaultTickInterval(spanMs)
        val digits = trimmed.takeWhile { it.isDigit() }
        val unit = trimmed.drop(digits.length).lowercase()
        val count = digits.toLongOrNull() ?: return defaultTickInterval(spanMs)
        val base = when (unit) {
            "millisecond" -> 1L
            "second" -> 1_000L
            "minute" -> 60_000L
            "hour" -> 3_600_000L
            "day" -> 86_400_000L
            "week" -> 7 * 86_400_000L
            "month" -> 30 * 86_400_000L
            else -> return defaultTickInterval(spanMs)
        }
        return (count * base).coerceAtLeast(1L)
    }

    private fun defaultTickInterval(spanMs: Long): Long {
        return when {
            spanMs <= 12 * 3_600_000L -> 3_600_000L
            spanMs <= 21 * 86_400_000L -> 86_400_000L
            spanMs <= 140 * 86_400_000L -> 7 * 86_400_000L
            else -> 30 * 86_400_000L
        }
    }

    private fun buildTicks(startMs: Long, endMs: Long, stepMs: Long): List<Long> {
        if (stepMs <= 0L) return emptyList()
        val out = ArrayList<Long>()
        var cur = startMs
        while (cur <= endMs) {
            out += cur
            val next = cur + stepMs
            if (next <= cur) break
            cur = next
        }
        if (out.isEmpty() || abs(out.last() - endMs) > stepMs / 3) out += endMs
        return out.distinct()
    }

    private fun formatAxisTick(epochMs: Long, pattern: String): String {
        val parts = epochToParts(epochMs)
        val shortWeekdays = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val longWeekdays = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val shortMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val longMonths = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        return buildString {
            var i = 0
            while (i < pattern.length) {
                if (pattern[i] != '%' || i == pattern.lastIndex) {
                    append(pattern[i]); i++; continue
                }
                when (pattern[i + 1]) {
                    '%' -> append('%')
                    'Y' -> append(parts.year.pad(4))
                    'y' -> append((parts.year % 100).pad(2))
                    'm' -> append(parts.month.pad(2))
                    'd' -> append(parts.day.pad(2))
                    'e' -> append(parts.day.toString())
                    'H' -> append(parts.hour.pad(2))
                    'M' -> append(parts.minute.pad(2))
                    'S' -> append(parts.second.pad(2))
                    'L' -> append(parts.millis.pad(3))
                    'b' -> append(shortMonths[parts.month - 1])
                    'B' -> append(longMonths[parts.month - 1])
                    'a' -> append(shortWeekdays[parts.weekday])
                    'A' -> append(longWeekdays[parts.weekday])
                    'w' -> append(parts.weekday)
                    's' -> append(epochMs / 1000L)
                    else -> {
                        append('%')
                        append(pattern[i + 1])
                    }
                }
                i += 2
            }
        }
    }

    private data class DateParts(
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int,
        val second: Int,
        val millis: Int,
        val weekday: Int,
    )

    private fun epochToParts(epochMs: Long): DateParts {
        val msPerDay = 86_400_000L
        val epochDay = floorDiv(epochMs, msPerDay)
        val dayMs = epochMs - epochDay * msPerDay
        val civil = epochDayToCivil(epochDay)
        val hour = (dayMs / 3_600_000L).toInt()
        val minute = ((dayMs % 3_600_000L) / 60_000L).toInt()
        val second = ((dayMs % 60_000L) / 1000L).toInt()
        val millis = (dayMs % 1000L).toInt()
        val weekday = (4 + epochDay).mod(7)
        return DateParts(civil.first, civil.second, civil.third, hour, minute, second, millis, weekday)
    }

    private fun epochDayToCivil(epochDay: Long): Triple<Int, Int, Int> {
        var z = epochDay + 719468L
        val era = if (z >= 0) z / 146097L else (z - 146096L) / 146097L
        val doe = (z - era * 146097L).toInt()
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        var y = yoe + era.toInt() * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = mp + if (mp < 10) 3 else -9
        y += if (m <= 2) 1 else 0
        return Triple(y, m, d)
    }

    private fun floorDiv(a: Long, b: Long): Long {
        var q = a / b
        val r = a % b
        if (r != 0L && ((r > 0) != (b > 0))) q -= 1
        return q
    }

    private fun Int.pad(width: Int): String = toString().padStart(width, '0')
    private fun Long.pad(width: Int): String = toString().padStart(width, '0')

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }

}
