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
import com.hrm.diagram.core.ir.Direction
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.ir.TimeSeriesIR
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.timeseries.TimelineLayout
import com.hrm.diagram.parser.mermaid.MermaidTimelineParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ThemeResolver

internal class MermaidTimelineSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private var styleExtras: Map<String, String> = emptyMap()
    private val colors = ThemeResolver.resolveTimeSeries(theme)
    private val categoricalPalette = ThemeResolver.categoricalPalette(theme)
    private val secondaryText = theme.colors.textSecondary
    private var timelinePalette = categoricalPalette

    override fun updateStyleExtras(extras: Map<String, String>) {
        styleExtras = extras
        val themeRaw = MermaidRenderThemeUtils.decodeRawThemeTokens(styleExtras["mermaid.themeTokens"])
        timelinePalette = categoricalPalette.mapIndexed { idx, fallback ->
            MermaidRenderThemeUtils.parseThemeColor(themeRaw["cScale$idx"]) ?: fallback
        }
    }

    private val parser = MermaidTimelineParser()
    private val layout = TimelineLayout(textMeasurer)
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
    private val headerFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val itemFont = FontSpec(family = "sans-serif", sizeSp = 12f)

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun render(ir: TimeSeriesIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)
        val bounds = laid.bounds

        val textColor = colors.labelText
        val axisColor = colors.axis
        val cardStroke = colors.slotStroke
        val multicolor = styleExtras["mermaid.config.timeline.disableMulticolor"]?.lowercase() != "true" &&
            ir.styleHints.extras["timeline.disableMulticolor"]?.lowercase() != "true"
        val palette = timelinePalette

        out += DrawCommand.FillRect(
            rect = Rect(Point(0f, 0f), Size(bounds.size.width, bounds.size.height)),
            color = colors.slotFill,
            corner = 0f,
            z = 0,
        )

        // Title.
        val titleRect = laid.nodePositions[NodeId("timeline:title")]
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

        val dir = ir.styleHints.direction ?: Direction.LR

        // Per track.
        for (track in ir.tracks) {
            val headerId = NodeId("timeline:track:${track.id.value}")
            val headerRect = laid.nodePositions[headerId]
            if (headerRect != null) {
                val headerText = (track.label as? RichLabel.Plain)?.text ?: track.id.value
                out += DrawCommand.DrawText(
                    text = headerText,
                    origin = Point(headerRect.left, headerRect.top),
                    font = headerFont,
                    color = textColor,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    z = 10,
                )
            }

            val slotIds = laid.nodePositions.keys
                .filter { it.value.startsWith("timeline:slot:${track.id.value}:") }
                .sortedBy { it.value.substringAfterLast(':').toLongOrNull() ?: 0L }

            // Axis for the track: goes through the middle of slots.
            if (slotIds.isNotEmpty()) {
                if (dir == Direction.TB) {
                    val x = laid.nodePositions[slotIds.first()]!!.left + 14f
                    val top = laid.nodePositions[slotIds.first()]!!.top + 8f
                    val bottom = laid.nodePositions[slotIds.last()]!!.bottom - 8f
                    out += DrawCommand.StrokePath(
                        path = PathCmd(listOf(PathOp.MoveTo(Point(x, top)), PathOp.LineTo(Point(x, bottom)))),
                        stroke = Stroke(width = 2f),
                        color = axisColor,
                        z = 1,
                    )
                } else {
                    val y = laid.nodePositions[slotIds.first()]!!.top + 14f
                    val left = laid.nodePositions[slotIds.first()]!!.left + 8f
                    val right = laid.nodePositions[slotIds.last()]!!.right - 8f
                    out += DrawCommand.StrokePath(
                        path = PathCmd(listOf(PathOp.MoveTo(Point(left, y)), PathOp.LineTo(Point(right, y)))),
                        stroke = Stroke(width = 2f),
                        color = axisColor,
                        z = 1,
                    )
                }
            }

            for (slotId in slotIds) {
                val slot = laid.nodePositions[slotId] ?: continue
                // Marker.
                val marker = Rect.ltrb(slot.left + 10f, slot.top + 10f, slot.left + 18f, slot.top + 18f)
                out += DrawCommand.FillRect(rect = marker, color = axisColor, corner = 4f, z = 2)

                // Items in this slot: the layout created item rects based on ir.items ids.
                val slotMs = slotId.value.substringAfterLast(':').toLongOrNull()
                val slotItems = ir.items.filter { it.trackId == track.id && it.range.startMs == slotMs }
                for (it in slotItems) {
                    val r = laid.nodePositions[NodeId("timeline:item:${it.id.value}")] ?: continue
                    val cardFill = if (multicolor) {
                        palette[((slotMs?.div(1000L) ?: 0L).toInt()).mod(palette.size)]
                    } else {
                        colors.itemFill
                    }
                    out += DrawCommand.FillRect(rect = r, color = cardFill, corner = 6f, z = 3)
                    out += DrawCommand.StrokeRect(rect = r, stroke = Stroke(width = 1f), color = cardStroke, corner = 6f, z = 4)

                    val label = (it.payload["event"] ?: (it.label as? RichLabel.Plain)?.text ?: it.id.value)
                    out += DrawCommand.DrawText(
                        text = label,
                        origin = Point(r.left + 8f, r.top + 6f),
                        font = itemFont,
                        color = textColor,
                        anchorX = TextAnchorX.Start,
                        anchorY = TextAnchorY.Top,
                        z = 5,
                        maxWidth = r.size.width - 16f,
                    )

                    // Period label as a small caption.
                    it.payload["period"]?.let { p ->
                        out += DrawCommand.DrawText(
                            text = p,
                            origin = Point(r.left + 8f, r.bottom - 4f),
                            font = FontSpec(family = "sans-serif", sizeSp = 10f, weight = 600),
                            color = secondaryText,
                            anchorX = TextAnchorX.Start,
                            anchorY = TextAnchorY.Bottom,
                            z = 5,
                            maxWidth = r.size.width - 16f,
                        )
                    }
                }
            }
        }

        return out.entities()
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }

}
