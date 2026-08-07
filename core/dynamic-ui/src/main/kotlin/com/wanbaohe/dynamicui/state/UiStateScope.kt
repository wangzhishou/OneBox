package com.wanbaohe.dynamicui.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateMap

/**
 * UiStateScope – the reactive state container for a single [DynamicUi] instance.
 *
 * Backed by Compose [SnapshotStateMap], so any write automatically schedules
 * recomposition of the Composables that read the affected keys.
 */
@Stable
class UiStateScope(initialValues: Map<String, Any?> = emptyMap()) {

    private val _state: SnapshotStateMap<String, Any?> = mutableStateMapOf<String, Any?>()
        .also { it.putAll(initialValues) }

    /** Read any state value by its dot-path key (e.g. "user.name" or "count"). */
    operator fun get(key: String): Any? = getByPath(key)

    /** Write a single state key. Triggers recomposition of nodes that read it. */
    operator fun set(key: String, value: Any?) {
        setByPath(key, value)
    }

    /** Reads a nested value such as `user.name` from the reactive state tree. */
    fun getByPath(path: String): Any? {
        if (path.isBlank()) return null
        
        // Fast path: check flat key first (supports fine-grained reactive bindings)
        if (_state.containsKey(path)) return _state[path]
        
        if (!path.contains('.')) return null

        val segments = path.split('.')
        val root = _state[segments.first()] ?: return null
        return navigate(root, segments.drop(1))
    }

    /**
     * Writes a nested value such as `dialog.visible`.
     * Also writes the flat path to ensure exact key observers recompose without
     * needing the whole root object to recompose.
     */
    fun setByPath(path: String, value: Any?) {
        if (path.isBlank()) return
        
        // Store flat key for fine-grained reactivity
        _state[path] = value
        
        if (!path.contains('.')) {
            return
        }

        val segments = path.split('.')
        val rootKey = segments.first()
        val updatedRoot = putNestedValue(_state[rootKey], segments.drop(1), value)
        _state[rootKey] = updatedRoot
    }

    /** Bulk update – all changes are applied atomically inside one snapshot. */
    fun patch(updates: Map<String, Any?>) {
        Snapshot.withMutableSnapshot {
            updates.forEach { (key, value) -> setByPath(key, value) }
        }
    }

    /** Reset to a new initial state (replaces all current keys). */
    fun reset(newValues: Map<String, Any?> = emptyMap()) {
        Snapshot.withMutableSnapshot {
            _state.clear()
            _state.putAll(newValues)
        }
    }

    /** Expose a read-only snapshot for the resolver. */
    fun snapshot(): Map<String, Any?> = _state

    fun contains(key: String): Boolean = getByPath(key) != null

    /** Convenience for boolean flags */
    fun isTrue(key: String): Boolean = getByPath(key) == true || getByPath(key) == "true"

    /** Convenience: append an item to a list-valued key. */
    @Suppress("UNCHECKED_CAST")
    fun appendToList(key: String, item: Any?) {
        val existing = getByPath(key)
        // Ensure we create a new list reference so Compose observes the change
        val updated = (existing as? List<Any?>)?.toMutableList() ?: mutableListOf()
        updated.add(item)
        setByPath(key, updated.toList())
    }

    private fun navigate(root: Any?, segments: List<String>): Any? {
        var current: Any? = root
        for (segment in segments) {
            current = when (current) {
                is Map<*, *> -> current[segment]
                is List<*> -> segment.toIntOrNull()?.let(current::getOrNull)
                else -> return null
            }
        }
        return current
    }

    @Suppress("UNCHECKED_CAST")
    private fun putNestedValue(root: Any?, segments: List<String>, value: Any?): Any? {
        if (segments.isEmpty()) return value

        val current = (root as? Map<String, Any?>)?.toMutableMap() ?: linkedMapOf()
        val head = segments.first()
        current[head] = putNestedValue(current[head], segments.drop(1), value)
        return current.toMap()
    }
}
