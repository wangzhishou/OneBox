package com.hrm.diagram.render.graph

import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.Size
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeShape
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.text.TextMetrics
import kotlin.math.max

internal class GraphMeasurePolicy(
    private val textMeasurer: TextMeasurer,
    private val defaultSize: Size,
    private val maxWidth: Float,
    private val minWidth: Float,
    private val minHeight: Float,
    private val labelOf: (Node) -> String = { graphLabelText(it.label).ifBlank { it.id.value } },
    private val fontOf: (Node) -> FontSpec,
    private val paddingOf: (Node) -> Pair<Float, Float> = ::defaultPadding,
    private val customSizeOf: (Node) -> Size? = { null },
) {
    private val sizes: MutableMap<NodeId, Size> = LinkedHashMap()
    private val metrics: MutableMap<NodeId, TextMetrics> = LinkedHashMap()

    fun sizeOf(id: NodeId): Size = sizes[id] ?: defaultSize

    fun metricOf(id: NodeId): TextMetrics? = metrics[id]

    fun measure(ir: GraphIR, remeasure: Boolean) {
        for (node in ir.nodes) {
            if (!remeasure && node.id in sizes) continue
            val customSize = customSizeOf(node)
            if (customSize != null) {
                sizes[node.id] = customSize
                continue
            }
            val raw = textMeasurer.measure(labelOf(node), fontOf(node), maxWidth = maxWidth)
            val (padX, padY) = paddingOf(node)
            val width = (raw.width + padX * 2f).coerceAtLeast(minWidth)
            val height = (raw.height + padY * 2f).coerceAtLeast(minHeight)
            val side = max(width, height)
            sizes[node.id] = when (node.shape) {
                NodeShape.Circle -> Size(side, side)
                NodeShape.Diamond -> Size(width * 1.35f, height * 1.35f)
                else -> Size(width, height)
            }
            metrics[node.id] = raw
        }
    }

    fun clear() {
        sizes.clear()
        metrics.clear()
    }
}

private fun defaultPadding(node: Node): Pair<Float, Float> =
    when (node.shape) {
        NodeShape.Diamond -> 30f to 18f
        NodeShape.Circle, NodeShape.Ellipse -> 24f to 12f
        NodeShape.Stadium, NodeShape.RoundedBox -> 18f to 12f
        else -> 18f to 12f
    }
