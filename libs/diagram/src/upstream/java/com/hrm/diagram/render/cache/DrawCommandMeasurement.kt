package com.hrm.diagram.render.cache

import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.core.draw.Size
import com.hrm.diagram.core.draw.TextAnchorX
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.text.TextMetrics

/**
 * Measure-stage normalization for draw commands.
 *
 * All default Mermaid / PlantUML / DOT pipelines pass their completed command stream through this
 * before publishing a snapshot, so every DrawText has true measured bounds without culling/render
 * code re-measuring or guessing.
 */
internal fun List<DrawCommand>.withMeasuredTextBounds(textMeasurer: TextMeasurer): List<DrawCommand> =
    map { it.withMeasuredTextBounds(textMeasurer) }

internal fun List<DrawEntity>.withMeasuredEntityTextBounds(textMeasurer: TextMeasurer): List<DrawEntity> =
    map { entity -> entity.copy(commands = entity.commands.withMeasuredTextBounds(textMeasurer)) }

private fun DrawCommand.withMeasuredTextBounds(textMeasurer: TextMeasurer): DrawCommand =
    when (this) {
        is DrawCommand.DrawText -> withMeasuredBounds(textMeasurer)
        is DrawCommand.Group -> copy(children = children.withMeasuredTextBounds(textMeasurer))
        is DrawCommand.Clip -> copy(children = children.withMeasuredTextBounds(textMeasurer))
        else -> this
    }

private fun DrawCommand.DrawText.withMeasuredBounds(textMeasurer: TextMeasurer): DrawCommand.DrawText {
    if (measuredBounds != null) return this
    val metrics = textMeasurer.measure(text, font, maxWidth)
    return copy(measuredBounds = textBounds(origin, metrics, anchorX, anchorY))
}

private fun textBounds(
    origin: Point,
    metrics: TextMetrics,
    anchorX: TextAnchorX,
    anchorY: TextAnchorY,
): Rect {
    val left = when (anchorX) {
        TextAnchorX.Start -> origin.x
        TextAnchorX.Center -> origin.x - metrics.width / 2f
        TextAnchorX.End -> origin.x - metrics.width
    }
    val top = when (anchorY) {
        TextAnchorY.Top -> origin.y
        TextAnchorY.Middle -> origin.y - metrics.height / 2f
        TextAnchorY.Baseline -> origin.y - metrics.ascent
        TextAnchorY.Bottom -> origin.y - metrics.height
    }
    return Rect(Point(left, top), Size(metrics.width, metrics.height))
}
