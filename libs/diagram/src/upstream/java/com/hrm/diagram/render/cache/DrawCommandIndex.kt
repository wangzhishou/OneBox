package com.hrm.diagram.render.cache

import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.PathOp
import com.hrm.diagram.core.draw.Point
import com.hrm.diagram.core.draw.Rect
import kotlin.math.max
import kotlin.math.min

/**
 * Spatial index for viewport-culling draw commands.
 *
 * Commands with conservative bounds are indexed in a [Quadtree]. Commands that cannot be bounded
 * safely stay in [alwaysVisible], so culling never drops content incorrectly.
 */
internal class DrawCommandIndex private constructor(
    private val commands: List<DrawCommand>,
    private val indexed: Quadtree<Int>?,
    private val alwaysVisible: List<Int>,
) {
    fun query(viewport: Rect): List<DrawCommand> {
        val indices = LinkedHashSet<Int>()
        for (index in alwaysVisible) indices += index
        indexed?.query(viewport)?.forEach { indices += it }
        return indices
            .map { commands[it] }
            .sortedBy { it.z }
    }

    companion object {
        fun from(commands: List<DrawCommand>, worldBounds: Rect?): DrawCommandIndex {
            if (commands.isEmpty() || worldBounds == null || worldBounds.size.width <= 0f || worldBounds.size.height <= 0f) {
                return DrawCommandIndex(commands, indexed = null, alwaysVisible = commands.indices.toList())
            }
            val tree = Quadtree<Int>(worldBounds.expand(1f, 1f))
            val always = ArrayList<Int>()
            for ((index, command) in commands.withIndex()) {
                val bounds = commandBounds(command)
                if (bounds == null || bounds.size.width <= 0f || bounds.size.height <= 0f || !bounds.intersects(worldBounds)) {
                    always += index
                } else {
                    tree.insert(bounds, index)
                }
            }
            return DrawCommandIndex(commands, tree, always)
        }
    }
}

internal fun commandBounds(command: DrawCommand): Rect? =
    when (command) {
        is DrawCommand.FillRect -> command.rect
        is DrawCommand.StrokeRect -> command.rect.expand(command.stroke.width, command.stroke.width)
        is DrawCommand.FillPath -> pathBounds(command.path.ops)
        is DrawCommand.StrokePath -> pathBounds(command.path.ops)?.expand(command.stroke.width, command.stroke.width)
        is DrawCommand.DrawText -> command.measuredBounds
        is DrawCommand.DrawArrow -> lineBounds(command.from, command.to).expand(12f, 12f)
        is DrawCommand.DrawIcon -> command.rect
        is DrawCommand.Hyperlink -> command.rect
        is DrawCommand.Clip -> command.rect
        is DrawCommand.Group -> null
    }

private fun pathBounds(ops: List<PathOp>): Rect? {
    val points = ArrayList<Point>()
    for (op in ops) {
        when (op) {
            is PathOp.MoveTo -> points += op.p
            is PathOp.LineTo -> points += op.p
            is PathOp.QuadTo -> {
                points += op.ctrl
                points += op.end
            }
            is PathOp.CubicTo -> {
                points += op.c1
                points += op.c2
                points += op.end
            }
            PathOp.Close -> Unit
        }
    }
    if (points.isEmpty()) return null
    var left = points.first().x
    var right = left
    var top = points.first().y
    var bottom = top
    for (p in points.drop(1)) {
        left = min(left, p.x)
        right = max(right, p.x)
        top = min(top, p.y)
        bottom = max(bottom, p.y)
    }
    return Rect.ltrb(left, top, right, bottom)
}

private fun lineBounds(a: Point, b: Point): Rect =
    Rect.ltrb(min(a.x, b.x), min(a.y, b.y), max(a.x, b.x), max(a.y, b.y))

private fun Rect.expand(dx: Float, dy: Float): Rect =
    Rect.ltrb(left - dx, top - dy, right + dx, bottom + dy)

private fun Rect.intersects(other: Rect): Boolean =
    left < other.right && right > other.left && top < other.bottom && bottom > other.top
