package com.hrm.markdown.renderer.internal.layout.inline

/**
 * A small multiplatform LRU cache with O(1) lookup, promotion, and eviction.
 *
 * Kotlin common code does not expose the JVM access-order [LinkedHashMap] constructor, so the
 * access order is maintained explicitly with a doubly linked list. The byte limit is deliberately
 * based on an estimator: it is intended to bound retained cache payloads consistently across
 * targets, not to replace a platform heap profiler.
 */
internal class WeightedLruCache<K : Any, V : Any>(
    private val maxEntries: Int,
    private val maxEstimatedBytes: Long,
    private val estimateValueBytes: (V) -> Long,
) {
    init {
        require(maxEntries > 0) { "maxEntries must be positive" }
        require(maxEstimatedBytes > 0) { "maxEstimatedBytes must be positive" }
    }

    private class Entry<K : Any, V : Any>(
        val key: K,
        val value: V,
        val estimatedBytes: Long,
    ) {
        var previous: Entry<K, V>? = null
        var next: Entry<K, V>? = null
    }

    private val entries = mutableMapOf<K, Entry<K, V>>()
    private var leastRecentlyUsed: Entry<K, V>? = null
    private var mostRecentlyUsed: Entry<K, V>? = null
    private var estimatedBytes: Long = 0
    private var hits: Long = 0
    private var misses: Long = 0
    private var evictions: Long = 0

    fun getOrPut(key: K, compute: () -> V): V {
        val cached = entries[key]
        if (cached != null) {
            hits++
            promote(cached)
            return cached.value
        }

        misses++
        val value = compute()
        val entry = Entry(
            key = key,
            value = value,
            estimatedBytes = estimateEntryBytes(value),
        )
        entries[key] = entry
        appendMostRecentlyUsed(entry)
        estimatedBytes = saturatingAdd(estimatedBytes, entry.estimatedBytes)
        trimToLimits()
        return value
    }

    fun clear() {
        entries.clear()
        leastRecentlyUsed = null
        mostRecentlyUsed = null
        estimatedBytes = 0
    }

    fun resetStatistics() {
        hits = 0
        misses = 0
        evictions = 0
    }

    fun snapshot(): LruCacheMetricsSnapshot = LruCacheMetricsSnapshot(
        entryCount = entries.size,
        estimatedBytes = estimatedBytes,
        hits = hits,
        misses = misses,
        evictions = evictions,
    )

    private fun promote(entry: Entry<K, V>) {
        if (entry === mostRecentlyUsed) return
        unlink(entry)
        appendMostRecentlyUsed(entry)
    }

    private fun appendMostRecentlyUsed(entry: Entry<K, V>) {
        entry.previous = mostRecentlyUsed
        entry.next = null
        mostRecentlyUsed?.next = entry
        mostRecentlyUsed = entry
        if (leastRecentlyUsed == null) leastRecentlyUsed = entry
    }

    private fun unlink(entry: Entry<K, V>) {
        val previous = entry.previous
        val next = entry.next
        if (previous == null) leastRecentlyUsed = next else previous.next = next
        if (next == null) mostRecentlyUsed = previous else next.previous = previous
        entry.previous = null
        entry.next = null
    }

    private fun trimToLimits() {
        while (entries.size > maxEntries || estimatedBytes > maxEstimatedBytes) {
            val eldest = leastRecentlyUsed ?: return
            unlink(eldest)
            entries.remove(eldest.key)
            estimatedBytes = (estimatedBytes - eldest.estimatedBytes).coerceAtLeast(0)
            evictions++
        }
    }

    private fun estimateEntryBytes(value: V): Long {
        val valueBytes = estimateValueBytes(value).coerceAtLeast(0)
        return saturatingAdd(valueBytes, ENTRY_OVERHEAD_BYTES)
    }

    private fun saturatingAdd(left: Long, right: Long): Long {
        return if (Long.MAX_VALUE - left < right) Long.MAX_VALUE else left + right
    }

    private companion object {
        /** Map entry, key, and linked-list node bookkeeping; deliberately conservative. */
        const val ENTRY_OVERHEAD_BYTES = 96L
    }
}

internal data class LruCacheMetricsSnapshot(
    val entryCount: Int,
    val estimatedBytes: Long,
    val hits: Long,
    val misses: Long,
    val evictions: Long,
)
