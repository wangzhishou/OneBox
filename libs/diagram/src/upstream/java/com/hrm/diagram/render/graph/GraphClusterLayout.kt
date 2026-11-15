package com.hrm.diagram.render.graph

import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.core.draw.Size
import com.hrm.diagram.core.ir.Cluster
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.layout.LaidOutDiagram
import kotlin.math.max
import kotlin.math.min

internal object GraphClusterLayout {
    fun withClusterRects(
        ir: GraphIR,
        laid: LaidOutDiagram,
        textMeasurer: TextMeasurer,
        labelFont: FontSpec,
        horizontalPadding: Float = 24f,
        bottomPadding: Float = 20f,
        maxLabelWidth: Float = 260f,
    ): LaidOutDiagram {
        val rects = LinkedHashMap<NodeId, Rect>()
        for (cluster in ir.clusters) {
            computeClusterRect(cluster, laid.nodePositions, rects, textMeasurer, labelFont, horizontalPadding, bottomPadding, maxLabelWidth)
        }
        val bounds = computeBounds(laid.nodePositions.values + rects.values)
        return laid.copy(clusterRects = rects, bounds = bounds)
    }

    private fun computeClusterRect(
        cluster: Cluster,
        nodePositions: Map<NodeId, Rect>,
        out: MutableMap<NodeId, Rect>,
        textMeasurer: TextMeasurer,
        labelFont: FontSpec,
        horizontalPadding: Float,
        bottomPadding: Float,
        maxLabelWidth: Float,
    ): Rect? {
        val rects = ArrayList<Rect>()
        cluster.children.mapNotNullTo(rects) { nodePositions[it] }
        cluster.nestedClusters.mapNotNullTo(rects) {
            computeClusterRect(it, nodePositions, out, textMeasurer, labelFont, horizontalPadding, bottomPadding, maxLabelWidth)
        }
        val base = rects.takeIf { it.isNotEmpty() }?.let(::union) ?: return null
        val labelHeight = graphLabelText(cluster.label).takeIf { it.isNotBlank() }?.let {
            textMeasurer.measure(it, labelFont, maxWidth = maxLabelWidth).height + 18f
        } ?: 18f
        val rect = Rect.ltrb(
            base.left - horizontalPadding,
            base.top - labelHeight - 16f,
            base.right + horizontalPadding,
            base.bottom + bottomPadding,
        )
        out[cluster.id] = rect
        return rect
    }

    fun computeBounds(rects: Collection<Rect>, padding: Float = 24f): Rect {
        if (rects.isEmpty()) return Rect(Point.Zero, Size.Zero)
        var l = Float.POSITIVE_INFINITY
        var t = Float.POSITIVE_INFINITY
        var r = Float.NEGATIVE_INFINITY
        var b = Float.NEGATIVE_INFINITY
        for (rect in rects) {
            l = min(l, rect.left)
            t = min(t, rect.top)
            r = max(r, rect.right)
            b = max(b, rect.bottom)
        }
        return Rect.ltrb(l, t, r + padding, b + padding)
    }

    private fun union(rects: Collection<Rect>): Rect {
        var l = Float.POSITIVE_INFINITY
        var t = Float.POSITIVE_INFINITY
        var r = Float.NEGATIVE_INFINITY
        var b = Float.NEGATIVE_INFINITY
        for (rect in rects) {
            l = min(l, rect.left)
            t = min(t, rect.top)
            r = max(r, rect.right)
            b = max(b, rect.bottom)
        }
        return Rect.ltrb(l, t, r, b)
    }
}
