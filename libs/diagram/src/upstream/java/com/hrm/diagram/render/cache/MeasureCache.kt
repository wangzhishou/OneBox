package com.hrm.diagram.render.cache

import com.hrm.diagram.core.draw.FontSpec

/**
 * Bounded LRU cache for text-measurement results.
 *
 * Per `docs/streaming.md` §3.5: text measurement is one of the hottest ops on the streaming path,
 * and is keyed deterministically by `(text, FontSpec, maxWidth)`. This cache stores the
 * already-measured value (typically a `Size` or platform `TextLayoutResult`) under that key,
 * evicting least-recently-used entries when [maxEntries] is exceeded.
 *
 * The cache is generic over [V] so platform-specific renderers can store either:
 * - common-side `com.hrm.diagram.core.draw.Size` (for SVG/PNG export pipelines), or
 * - Compose `TextLayoutResult` (for the on-screen DiagramView).
 *
 * **Single-threaded.** Compose's `TextMeasurer` is touched only on the UI thread; if you need
 * cross-thread caching, wrap with a `Mutex`. We do not add the lock here to keep the hot path
 * allocation-free.
 */
internal class MeasureCache<V : Any>(
    val maxEntries: Int = DEFAULT_MAX_ENTRIES,
) {
    init { require(maxEntries > 0) { "maxEntries must be positive, got $maxEntries" } }

    private val map = HashMap<MeasureKey, Node<V>>(maxEntries.coerceAtMost(64))
    private var head: Node<V>? = null  // most-recently-used
    private var tail: Node<V>? = null  // least-recently-used

    val size: Int get() = map.size

    /** Counters useful in benchmarks and CI assertions. */
    var hits: Long = 0
        private set
    var misses: Long = 0
        private set

    /**
     * Look up the value for [key]. Returns `null` on miss; on hit, marks the entry as most-recently-used.
     */
    fun get(key: MeasureKey): V? {
        val n = map[key] ?: return null.also { misses++ }
        moveToHead(n)
        hits++
        return n.value
    }

    /** Insert / overwrite. May evict the LRU entry. */
    fun put(key: MeasureKey, value: V) {
        val existing = map[key]
        if (existing != null) {
            existing.value = value
            moveToHead(existing)
            return
        }
        val node = Node(key, value)
        map[key] = node
        addToHead(node)
        if (map.size > maxEntries) {
            evictTail()
        }
    }

    /**
     * Get-or-compute. The supplied [compute] runs only on a miss. The computed value is cached
     * and returned. This is the recommended API on the hot path.
     */
    inline fun getOrPut(key: MeasureKey, compute: (MeasureKey) -> V): V {
        val cached = get(key)
        if (cached != null) return cached
        val v = compute(key)
        put(key, v)
        return v
    }

    fun clear() {
        map.clear()
        head = null
        tail = null
        hits = 0
        misses = 0
    }

    // --- LRU plumbing ---

    private fun addToHead(n: Node<V>) {
        val h = head
        n.prev = null
        n.next = h
        h?.prev = n
        head = n
        if (tail == null) tail = n
    }

    private fun unlink(n: Node<V>) {
        val p = n.prev; val nx = n.next
        if (p != null) p.next = nx else head = nx
        if (nx != null) nx.prev = p else tail = p
        n.prev = null; n.next = null
    }

    private fun moveToHead(n: Node<V>) {
        if (head === n) return
        unlink(n)
        addToHead(n)
    }

    private fun evictTail() {
        val t = tail ?: return
        unlink(t)
        map.remove(t.key)
    }

    private class Node<V : Any>(
        val key: MeasureKey,
        var value: V,
        var prev: Node<V>? = null,
        var next: Node<V>? = null,
    )

    companion object {
        const val DEFAULT_MAX_ENTRIES: Int = 512
    }
}

/**
 * Stable cache key for text measurement. Equality is value-based on the triple
 * `(text, font, maxWidth)`. `text` is held as [String] (NOT [CharSequence]) so that hash/equals
 * are O(content) and predictable across CharSequence implementations.
 */
internal data class MeasureKey(
    val text: String,
    val font: FontSpec,
    /** `null` means unconstrained width. */
    val maxWidth: Float? = null,
)
