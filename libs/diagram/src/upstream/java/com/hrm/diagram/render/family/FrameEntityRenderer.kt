package com.hrm.diagram.render.family

import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.cache.DrawEntityKey

/**
 * Entity sink for diagram-family renderers that emit commands while rendering.
 *
 * Commands are assigned to stable semantic entities as they are produced, so
 * callers do not need to build a full flat DrawCommand frame before grouping.
 */
internal object FrameEntityRenderer {
    fun sink(
        prefix: String,
        model: DiagramModel?,
        laidOut: LaidOutDiagram? = null,
    ): EntitySink {
        val keys = FamilyEntityKeyRegistry.semanticKeys(prefix, model).ifEmpty {
            listOf(DrawEntityKey.decoration(prefix, FamilyEntityKeyRegistry.familyKey(model), "frame"))
        }
        return EntitySink(keys = keys, anchors = entityAnchors(prefix, laidOut))
    }

    internal class EntitySink internal constructor(
        private val keys: List<String>,
        private val anchors: List<EntityAnchor>,
    ) : AbstractMutableList<DrawCommand>() {
        private val buckets = LinkedHashMap<String, MutableList<DrawCommand>>()
        private var commandIndex = 0

        operator fun plusAssign(command: DrawCommand) {
            add(command)
        }

        fun emit(key: String, commands: List<DrawCommand>) {
            if (commands.isEmpty()) return
            buckets.getOrPut(key) { ArrayList() } += commands
            commandIndex += commands.size
        }

        fun entities(): List<DrawEntity> {
            val entities = ArrayList<DrawEntity>(keys.size + buckets.size)
            keys.forEach { key -> entities += DrawEntity(key, buckets[key].orEmpty()) }
            buckets.forEach { (key, groupedCommands) ->
                if (key !in keys) entities += DrawEntity(key, groupedCommands)
            }
            return entities
        }

        override val size: Int
            get() = commandIndex

        override fun add(index: Int, element: DrawCommand) {
            val key = anchors.bestKeyFor(element) ?: keys.first()
            buckets.getOrPut(key) { ArrayList() } += element
            commandIndex += 1
        }

        override fun get(index: Int): DrawCommand =
            throw UnsupportedOperationException("EntitySink does not expose a flat command list")

        override fun removeAt(index: Int): DrawCommand =
            throw UnsupportedOperationException("EntitySink does not support removing emitted commands")

        override fun set(index: Int, element: DrawCommand): DrawCommand =
            throw UnsupportedOperationException("EntitySink does not support replacing emitted commands")
    }

    internal data class EntityAnchor(
        val key: String,
        val bounds: Rect,
        val priority: Int,
    )

    private fun entityAnchors(prefix: String, laidOut: LaidOutDiagram?): List<EntityAnchor> {
        if (laidOut == null) return emptyList()
        val p = normalizedEntityPrefix(prefix)
        val anchors = ArrayList<EntityAnchor>()
        laidOut.clusterRects.forEach { (id, rect) ->
            anchors += EntityAnchor(DrawEntityKey.cluster(p, id), rect, priority = 0)
        }
        laidOut.layoutState.edgeRoutesByKey.forEach { (key, route) ->
            if (route.points.isNotEmpty()) {
                anchors += EntityAnchor(DrawEntityKey.edge(p, key), route.points.bounds().expand(6f), priority = 1)
            }
        }
        laidOut.nodePositions.forEach { (id, rect) ->
            anchors += EntityAnchor(DrawEntityKey.node(p, id), rect, priority = 2)
        }
        return anchors
    }

    private fun List<EntityAnchor>.bestKeyFor(command: DrawCommand): String? {
        val bounds = commandBounds(command) ?: return null
        var best: EntityAnchor? = null
        var bestScore = 0f
        for (anchor in this) {
            val score = bounds.overlapArea(anchor.bounds) * 10f + if (anchor.bounds.contains(bounds.center)) 1f else 0f
            if (score > bestScore || (score == bestScore && best != null && anchor.priority > best.priority)) {
                best = anchor
                bestScore = score
            }
        }
        return best?.takeIf { bestScore > 0f }?.key
    }

    private fun commandBounds(command: DrawCommand): Rect? =
        when (command) {
            is DrawCommand.FillRect -> command.rect
            is DrawCommand.StrokeRect -> command.rect
            is DrawCommand.DrawText -> command.measuredBounds
            is DrawCommand.DrawIcon -> command.rect
            is DrawCommand.Hyperlink -> command.rect
            is DrawCommand.FillPath -> command.path.ops.points().pointBoundsOrNull()
            is DrawCommand.StrokePath -> command.path.ops.points().pointBoundsOrNull()
            is DrawCommand.DrawArrow -> listOf(command.from, command.to).bounds().expand(4f)
            is DrawCommand.Group -> command.children.mapNotNull(::commandBounds).rectBoundsOrNull()
            is DrawCommand.Clip -> command.rect
        }

    private fun List<com.hrm.diagram.core.draw.PathOp>.points(): List<Point> =
        flatMap { op ->
            when (op) {
                is com.hrm.diagram.core.draw.PathOp.MoveTo -> listOf(op.p)
                is com.hrm.diagram.core.draw.PathOp.LineTo -> listOf(op.p)
                is com.hrm.diagram.core.draw.PathOp.QuadTo -> listOf(op.ctrl, op.end)
                is com.hrm.diagram.core.draw.PathOp.CubicTo -> listOf(op.c1, op.c2, op.end)
                com.hrm.diagram.core.draw.PathOp.Close -> emptyList()
            }
        }

    private fun List<Point>.pointBoundsOrNull(): Rect? =
        if (isEmpty()) null else bounds()

    private fun List<Point>.bounds(): Rect {
        var minX = first().x
        var maxX = first().x
        var minY = first().y
        var maxY = first().y
        for (point in drop(1)) {
            if (point.x < minX) minX = point.x
            if (point.x > maxX) maxX = point.x
            if (point.y < minY) minY = point.y
            if (point.y > maxY) maxY = point.y
        }
        return Rect.ltrb(minX, minY, maxX, maxY)
    }

    private fun List<Rect>.rectBoundsOrNull(): Rect? {
        if (isEmpty()) return null
        return Rect.ltrb(minOf { it.left }, minOf { it.top }, maxOf { it.right }, maxOf { it.bottom })
    }

    private val Rect.center: Point
        get() = Point((left + right) / 2f, (top + bottom) / 2f)

    private fun Rect.expand(padding: Float): Rect =
        Rect.ltrb(left - padding, top - padding, right + padding, bottom + padding)

    private fun Rect.overlapArea(other: Rect): Float {
        val overlapLeft = maxOf(left, other.left)
        val overlapTop = maxOf(top, other.top)
        val overlapRight = minOf(right, other.right)
        val overlapBottom = minOf(bottom, other.bottom)
        val width = overlapRight - overlapLeft
        val height = overlapBottom - overlapTop
        return if (width > 0f && height > 0f) width * height else 0f
    }

}
