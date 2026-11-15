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
import com.hrm.diagram.core.ir.KanbanIR
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.kanban.KanbanLayout
import com.hrm.diagram.parser.mermaid.MermaidKanbanParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ThemeResolver

internal class MermaidKanbanSubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private var styleExtras: Map<String, String> = emptyMap()
    private val colors = ThemeResolver.resolveKanban(theme)
    private val metaMeasurements: MutableMap<NodeId, List<MetaLineMeasurement>> = HashMap()

    override fun updateStyleExtras(extras: Map<String, String>) {
        styleExtras = extras
    }

    private val parser = MermaidKanbanParser()
    private val layout = KanbanLayout(textMeasurer)
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        beforeLayout = ::prepareMetaMeasurements,
        layout = layout::layout,
        renderEntities = ::render,
    )

    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val headerFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val cardFont = FontSpec(family = "sans-serif", sizeSp = 12f)
    private val metaFont = FontSpec(family = "sans-serif", sizeSp = 10f)

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun render(ir: KanbanIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)
        val bounds = laid.bounds
        val ticketBaseUrl = styleExtras["mermaid.config.kanban.ticketBaseUrl"]

        out += DrawCommand.FillRect(rect = Rect(Point(0f, 0f), Size(bounds.size.width, bounds.size.height)), color = colors.background, corner = 0f, z = 0)

        val titleRect = laid.nodePositions[NodeId("kanban:title")]
        if (titleRect != null && !ir.title.isNullOrBlank()) {
            out += DrawCommand.DrawText(
                text = ir.title!!,
                origin = Point(titleRect.left, titleRect.top),
                font = titleFont,
                color = colors.text,
                anchorX = TextAnchorX.Start,
                anchorY = TextAnchorY.Top,
                z = 10,
            )
        }

        for (col in ir.columns) {
            val colRect = laid.nodePositions[NodeId("kanban:column:${col.id.value}")] ?: continue
            val headerRect = laid.nodePositions[NodeId("kanban:columnHeader:${col.id.value}")] ?: continue

            out += DrawCommand.FillRect(rect = colRect, color = colors.columnFill, corner = 10f, z = 1)
            out += DrawCommand.StrokeRect(rect = colRect, stroke = Stroke(width = 1f), color = colors.columnStroke, corner = 10f, z = 2)
            out += DrawCommand.FillRect(rect = headerRect, color = colors.headerFill, corner = 10f, z = 3)

            val headerText = (col.label as? RichLabel.Plain)?.text ?: col.id.value
            out += DrawCommand.DrawText(
                text = headerText,
                origin = Point((headerRect.left + headerRect.right) / 2f, (headerRect.top + headerRect.bottom) / 2f),
                font = headerFont,
                color = colors.text,
                anchorX = TextAnchorX.Center,
                anchorY = TextAnchorY.Middle,
                z = 4,
            )

            for (card in col.cards) {
                val cardRect = laid.nodePositions[NodeId("kanban:card:${card.id.value}")] ?: continue
                out += DrawCommand.FillRect(rect = cardRect, color = colors.cardFill, corner = 8f, z = 5)
                out += DrawCommand.StrokeRect(rect = cardRect, stroke = Stroke(width = 1f), color = colors.cardStroke, corner = 8f, z = 6)

                val label = (card.label as? RichLabel.Plain)?.text ?: card.id.value
                out += DrawCommand.DrawText(
                    text = label,
                    origin = Point(cardRect.left + 10f, cardRect.top + 10f),
                    font = cardFont,
                    color = colors.text,
                    anchorX = TextAnchorX.Start,
                    anchorY = TextAnchorY.Top,
                    maxWidth = cardRect.size.width - 20f,
                    z = 7,
                )

                card.payload["priority"]?.let { priority ->
                    val badgeText = priority
                    val badgeFill = when (priority.lowercase()) {
                        "very high" -> colors.priorityVeryHigh
                        "high" -> colors.priorityHigh
                        "low" -> colors.priorityLow
                        "very low" -> colors.priorityVeryLow
                        else -> colors.priorityDefault
                    }
                    val badgeRect = Rect.ltrb(cardRect.right - 82f, cardRect.top + 8f, cardRect.right - 8f, cardRect.top + 26f)
                    out += DrawCommand.FillRect(rect = badgeRect, color = badgeFill, corner = 9f, z = 7)
                    out += DrawCommand.DrawText(
                        text = badgeText,
                        origin = Point((badgeRect.left + badgeRect.right) / 2f, (badgeRect.top + badgeRect.bottom) / 2f),
                        font = metaFont,
                        color = colors.badgeText,
                        anchorX = TextAnchorX.Center,
                        anchorY = TextAnchorY.Middle,
                        maxWidth = badgeRect.size.width - 8f,
                        z = 8,
                    )
                }

                val cardId = NodeId("kanban:card:${card.id.value}")
                val metaLines = metaMeasurements[cardId].orEmpty()
                var metaY = cardRect.bottom - 10f
                for (i in metaLines.indices.reversed()) {
                    val meta = metaLines[i]
                    out += DrawCommand.DrawText(
                        text = meta.text,
                        origin = Point(cardRect.left + 10f, metaY),
                        font = metaFont,
                        color = colors.metaText,
                        anchorX = TextAnchorX.Start,
                        anchorY = TextAnchorY.Bottom,
                        maxWidth = cardRect.size.width - 20f,
                        z = 7,
                    )
                    if (ticketBaseUrl != null && meta.isTicket) {
                        val href = ticketBaseUrl.replace("#TICKET#", meta.text)
                        val linkRect = Rect.ltrb(cardRect.left + 10f, metaY - meta.height, cardRect.left + 10f + meta.width, metaY)
                        out += DrawCommand.Hyperlink(href = href, rect = linkRect, z = 9)
                    }
                    metaY -= meta.height + 4f
                }
            }
        }
        return out.entities()
    }

    private fun prepareMetaMeasurements(ir: KanbanIR) {
        metaMeasurements.clear()
        for (column in ir.columns) {
            for (card in column.cards) {
                val cardId = NodeId("kanban:card:${card.id.value}")
                metaMeasurements[cardId] = buildMetaMeasurements(card.payload)
            }
        }
    }

    private fun buildMetaLines(payload: Map<String, String>): List<String> {
        val out = ArrayList<String>()
        payload["ticket"]?.let { out += it }
        payload["assigned"]?.let { out += it }
        payload["priority"]?.let { out += it }
        return out
    }

    private fun buildMetaMeasurements(payload: Map<String, String>): List<MetaLineMeasurement> {
        val ticket = payload["ticket"]
        return buildMetaLines(payload).map { line ->
            val metrics = textMeasurer.measure(line, metaFont, maxWidth = KANBAN_META_MAX_WIDTH)
            MetaLineMeasurement(
                text = line,
                width = metrics.width,
                height = metrics.height,
                isTicket = ticket == line,
            )
        }
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
        metaMeasurements.clear()
    }

    private data class MetaLineMeasurement(
        val text: String,
        val width: Float,
        val height: Float,
        val isTicket: Boolean,
    )

    private companion object {
        const val KANBAN_META_MAX_WIDTH = 220f
    }

}
