package com.hrm.markdown.renderer.internal.core.compile

/** Detached collections whose runtime type and entries expose no mutable operations. */
internal fun <T> List<T>.renderSnapshotList(): List<T> = when {
    isEmpty() -> emptyList()
    this is RenderSnapshotList<*> -> this
    else -> RenderSnapshotList(this)
}

internal fun <K, V> Map<K, V>.renderSnapshotMap(): Map<K, V> = when {
    isEmpty() -> emptyMap()
    this is RenderSnapshotMap<*, *> -> this
    else -> RenderSnapshotMap(this)
}

private class RenderSnapshotList<T>(source: List<T>) : AbstractList<T>() {
    private val values = source.toList()
    override val size: Int get() = values.size
    override fun get(index: Int): T = values[index]
}

private class RenderSnapshotMap<K, V>(source: Map<K, V>) : AbstractMap<K, V>() {
    private val backing = source.toMap()
    override fun get(key: K): V? = backing[key]
    override fun containsKey(key: K): Boolean = backing.containsKey(key)
    override val entries: Set<Map.Entry<K, V>> = object : AbstractSet<Map.Entry<K, V>>() {
        override val size: Int get() = backing.size
        override fun iterator(): Iterator<Map.Entry<K, V>> {
            val sourceIterator = backing.entries.iterator()
            return object : Iterator<Map.Entry<K, V>> {
                override fun hasNext(): Boolean = sourceIterator.hasNext()
                override fun next(): Map.Entry<K, V> = sourceIterator.next().let { SnapshotEntry(it.key, it.value) }
            }
        }
    }
}

private class SnapshotEntry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V> {
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && key == other.key && value == other.value
    override fun hashCode(): Int = (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)
    override fun toString(): String = "$key=$value"
}
