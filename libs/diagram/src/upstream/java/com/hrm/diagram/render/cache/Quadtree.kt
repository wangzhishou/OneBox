package com.hrm.diagram.render.cache

import com.hrm.diagram.core.draw.Rect

/**
 * Region quadtree over axis-aligned [Rect] entries.
 *
 * Used by the renderer to viewport-cull `DrawCommand`s in O(log n) time so that 60fps stays
 * achievable on the "10k node" budget from `docs/streaming.md` §4.
 *
 * Design:
 * - Insert assigns each value to **one** leaf — the smallest quadrant that fully contains its
 *   bounds. Values straddling the parent split lines stay at the parent (no duplication).
 * - [query] returns every value whose bounds intersect the viewport, walking only the
 *   subtrees whose own bounds intersect.
 * - Re-balancing on insert: when a leaf exceeds [bucketSize] AND its depth < [maxDepth] it
 *   subdivides; existing entries that fit a child move down, the rest stay.
 *
 * This implementation is **append-only / clearable** (matches our streaming model — we never
 * remove individual draw commands; we either grow or `clear()` and rebuild).
 */
internal class Quadtree<T>(
    val bounds: Rect,
    val bucketSize: Int = 16,
    val maxDepth: Int = 8,
) {
    private val root: Node<T> = Node(bounds, depth = 0)

    val size: Int get() = root.totalSize()

    fun insert(rect: Rect, value: T) {
        require(intersects(bounds, rect)) {
            "rect $rect lies outside quadtree bounds $bounds"
        }
        root.insert(rect, value, bucketSize, maxDepth)
    }

    fun query(viewport: Rect): List<T> {
        val out = ArrayList<T>()
        root.query(viewport, out)
        return out
    }

    fun clear() {
        root.clear()
    }

    private class Node<T>(
        val bounds: Rect,
        val depth: Int,
    ) {
        private val entries: MutableList<Entry<T>> = ArrayList()
        private var children: Array<Node<T>>? = null

        data class Entry<T>(val rect: Rect, val value: T)

        fun totalSize(): Int {
            var n = entries.size
            children?.forEach { n += it.totalSize() }
            return n
        }

        fun insert(rect: Rect, value: T, bucketSize: Int, maxDepth: Int) {
            // If subdivided, try to push down; otherwise keep here.
            children?.let { kids ->
                val target = kids.firstOrNull { contains(it.bounds, rect) }
                if (target != null) {
                    target.insert(rect, value, bucketSize, maxDepth)
                    return
                }
                entries.add(Entry(rect, value))
                return
            }
            entries.add(Entry(rect, value))
            if (entries.size > bucketSize && depth < maxDepth) {
                subdivide(bucketSize, maxDepth)
            }
        }

        private fun subdivide(bucketSize: Int, maxDepth: Int) {
            val midX = (bounds.left + bounds.right) / 2f
            val midY = (bounds.top + bounds.bottom) / 2f
            val kids = arrayOf(
                Node(Rect.ltrb(bounds.left, bounds.top, midX, midY), depth + 1),
                Node(Rect.ltrb(midX, bounds.top, bounds.right, midY), depth + 1),
                Node(Rect.ltrb(bounds.left, midY, midX, bounds.bottom), depth + 1),
                Node<T>(Rect.ltrb(midX, midY, bounds.right, bounds.bottom), depth + 1),
            )
            children = kids
            val staying = ArrayList<Entry<T>>(entries.size)
            for (e in entries) {
                val target = kids.firstOrNull { contains(it.bounds, e.rect) }
                if (target != null) target.entries.add(e) else staying.add(e)
            }
            entries.clear()
            entries.addAll(staying)
        }

        fun query(viewport: Rect, out: MutableList<T>) {
            if (!intersects(bounds, viewport)) return
            for (e in entries) {
                if (intersects(e.rect, viewport)) out.add(e.value)
            }
            children?.forEach { it.query(viewport, out) }
        }

        fun clear() {
            entries.clear()
            children = null
        }
    }

    private companion object {
        fun intersects(a: Rect, b: Rect): Boolean =
            a.left < b.right && a.right > b.left && a.top < b.bottom && a.bottom > b.top

        fun contains(outer: Rect, inner: Rect): Boolean =
            outer.left <= inner.left && outer.right >= inner.right &&
                outer.top <= inner.top && outer.bottom >= inner.bottom
    }
}
