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
import com.hrm.diagram.core.ir.JourneyIR
import com.hrm.diagram.core.ir.RichLabel
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.layout.journey.JourneyLayout
import com.hrm.diagram.parser.mermaid.MermaidJourneyParser
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.family.FrameEntityRenderer
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.theme.ThemeResolver

internal class MermaidJourneySubPipeline(
    private val textMeasurer: TextMeasurer,
    theme: DiagramTheme,
) : MermaidSubPipeline {
    private val parser = MermaidJourneyParser()
    private val colors = ThemeResolver.resolveJourney(theme)
    private val layout = JourneyLayout(textMeasurer)
    private val kernel = MermaidFamilySubPipelineKernel(
        acceptLine = { parser.acceptLine(it) },
        snapshot = parser::snapshot,
        diagnostics = parser::diagnosticsSnapshot,
        layout = layout::layout,
        renderEntities = ::render,
    )

    private val titleFont = FontSpec(family = "sans-serif", sizeSp = 14f, weight = 600)
    private val stageFont = FontSpec(family = "sans-serif", sizeSp = 12f, weight = 600)
    private val stepFont = FontSpec(family = "sans-serif", sizeSp = 11f, weight = 600)
    private val actorFont = FontSpec(family = "sans-serif", sizeSp = 10f)
    private val axisFont = FontSpec(family = "sans-serif", sizeSp = 10f)

    override fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance = kernel.acceptLines(previousSnapshot, lines, seq, isFinal)

    private fun render(ir: JourneyIR, laid: LaidOutDiagram): List<DrawEntity> {
        val out = FrameEntityRenderer.sink(prefix = "mermaid", model = ir, laidOut = laid)
        val bounds = laid.bounds

        out += DrawCommand.FillRect(Rect(Point(0f, 0f), Size(bounds.size.width, bounds.size.height)), colors.background, z = 0)
        val titleRect = laid.nodePositions[NodeId("journey:title")]
        if (titleRect != null && !ir.title.isNullOrBlank()) {
            out += DrawCommand.DrawText(ir.title!!, Point(titleRect.left, titleRect.top), titleFont, colors.text, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Top, z = 10)
        }

        val plotLeft = 20f
        val plotRight = bounds.right - 20f
        val axisYs = scoreAxisYs(ir, laid)
        for (score in 1..5) {
            val y = axisYs[score] ?: continue
            out += DrawCommand.StrokePath(
                path = PathCmd(listOf(PathOp.MoveTo(Point(plotLeft, y)), PathOp.LineTo(Point(plotRight, y)))),
                stroke = Stroke.Hairline,
                color = colors.axis,
                z = 1,
            )
            out += DrawCommand.DrawText(score.toString(), Point(plotLeft + 2f, y - 4f), axisFont, colors.secondaryText, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Bottom, z = 10)
        }

        val centers = ArrayList<Point>()
        for ((stageIndex, stage) in ir.stages.withIndex()) {
            val stageRect = laid.nodePositions[NodeId("journey:stage:$stageIndex")]
            val stageLabel = (stage.label as? RichLabel.Plain)?.text.orEmpty()
            if (stageRect != null) {
                out += DrawCommand.DrawText(stageLabel, Point(stageRect.left, stageRect.top), stageFont, colors.text, anchorX = TextAnchorX.Start, anchorY = TextAnchorY.Top, z = 10)
            }
            for ((stepIndex, step) in stage.steps.withIndex()) {
                val rect = laid.nodePositions[NodeId("journey:step:$stageIndex:$stepIndex")] ?: continue
                val fill = scoreColor(step.score)
                out += DrawCommand.FillRect(rect = rect, color = fill, corner = 12f, z = 3)
                out += DrawCommand.StrokeRect(rect = rect, stroke = Stroke(width = 1f), color = colors.line, corner = 12f, z = 4)
                val center = Point((rect.left + rect.right) / 2f, (rect.top + rect.bottom) / 2f)
                centers += center
                val label = (step.label as? RichLabel.Plain)?.text.orEmpty()
                val actors = step.actors.mapNotNull { (it as? RichLabel.Plain)?.text }.joinToString(", ")
                out += DrawCommand.DrawText(label, Point(center.x, rect.top + 12f), stepFont, colors.text, maxWidth = rect.size.width - 12f, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Top, z = 10)
                out += DrawCommand.DrawText("score ${step.score}", Point(center.x, center.y), actorFont, colors.secondaryText, maxWidth = rect.size.width - 12f, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Middle, z = 10)
                if (actors.isNotBlank()) {
                    out += DrawCommand.DrawText(actors, Point(center.x, rect.bottom - 12f), actorFont, colors.secondaryText, maxWidth = rect.size.width - 12f, anchorX = TextAnchorX.Center, anchorY = TextAnchorY.Bottom, z = 10)
                }
            }
        }
        if (centers.size >= 2) {
            val ops = ArrayList<PathOp>()
            ops += PathOp.MoveTo(centers.first())
            for (pt in centers.drop(1)) ops += PathOp.LineTo(pt)
            out += DrawCommand.StrokePath(PathCmd(ops), Stroke(width = 2f), colors.line, z = 2)
        }
        return out.entities()
    }

    private fun scoreAxisYs(ir: JourneyIR, laid: LaidOutDiagram): Map<Int, Float> {
        val known = LinkedHashMap<Int, Float>()
        val centers = ArrayList<Pair<Int, Float>>()
        for ((stageIndex, stage) in ir.stages.withIndex()) {
            for ((stepIndex, step) in stage.steps.withIndex()) {
                val rect = laid.nodePositions[NodeId("journey:step:$stageIndex:$stepIndex")] ?: continue
                val centerY = (rect.top + rect.bottom) / 2f
                if (step.score !in known) known[step.score] = centerY
                centers += step.score to centerY
            }
        }
        val stepSize = centers.asSequence()
            .flatMap { a ->
                centers.asSequence().mapNotNull { b ->
                    val scoreDelta = kotlin.math.abs(a.first - b.first)
                    if (scoreDelta == 0) null else kotlin.math.abs(a.second - b.second) / scoreDelta
                }
            }
            .firstOrNull()
            ?: fallbackScoreStep(laid)
        val scoreTop = centers.firstOrNull()?.let { (score, y) -> y - (5 - score) * stepSize }
            ?: fallbackScoreTop(laid)
        return (1..5).associateWith { score -> known[score] ?: scoreTop + (5 - score) * stepSize }
    }

    private fun fallbackScoreStep(laid: LaidOutDiagram): Float {
        val maxCardHeight = laid.nodePositions
            .filterKeys { it.value.startsWith("journey:step:") }
            .values
            .maxOfOrNull { it.size.height }
            ?: 72f
        return maxOf(72f, maxCardHeight + 24f)
    }

    private fun fallbackScoreTop(laid: LaidOutDiagram): Float {
        val maxCardHeight = laid.nodePositions
            .filterKeys { it.value.startsWith("journey:step:") }
            .values
            .maxOfOrNull { it.size.height }
            ?: 72f
        val stageBottom = laid.nodePositions
            .filterKeys { it.value.startsWith("journey:stage:") }
            .values
            .maxOfOrNull { it.bottom }
            ?: (110f - maxCardHeight / 2f)
        return stageBottom + 20f + maxCardHeight / 2f
    }

    private fun scoreColor(score: Int): Color = when (score) {
        5 -> colors.scoreFills.getOrElse(4) { colors.accentPalette.lastOrNull() ?: colors.text }
        4 -> colors.scoreFills.getOrElse(3) { colors.accentPalette.getOrElse(1) { colors.text } }
        3 -> colors.scoreFills.getOrElse(2) { colors.accentPalette.getOrElse(2) { colors.text } }
        2 -> colors.scoreFills.getOrElse(1) { colors.accentPalette.getOrElse(3) { colors.text } }
        else -> colors.scoreFills.firstOrNull() ?: colors.accentPalette.firstOrNull() ?: colors.text
    }

    override fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity> = kernel.drawEntities()

    override fun dispose() {
        kernel.clear()
    }
}
