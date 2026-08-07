package com.wanbaohe.dynamicui.ir

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Universal, immutable IR node that represents any UI component.
 *
 * Design rationale:
 * - Single generic data class (vs. sealed subclasses per component) keeps the registry
 *   open for any custom component without touching IR.
 * - All fields are `val` + primitives/immutable collections → Compose treats this as
 *   @Immutable and skips recomposition when the node reference hasn't changed.
 * - [props] holds component-specific parameters, including nested node parameters.
 * - [actions] maps event names ("onClick", "onValueChange") to [ActionSpec].
 */
@Immutable
data class UiNode(
    val type: String,
    val id: String? = null,
    val prompt: String? = null,
    val modifier: List<String> = emptyList(),
    val visibleExpr: String? = null,
    val children: List<UiNode> = emptyList(),
    val props: Map<String, Any?> = emptyMap(),
    val listConfig: ListConfig? = null,
    val actions: Map<String, ActionSpec> = emptyMap(),
)

/**
 * Configuration for list/grid rendering nodes (LazyColumn, LazyRow, LazyVerticalGrid, ForEach).
 */
@Immutable
data class ListConfig(
    /** Expression resolving to a List<*> in state scope, e.g. "\${state.items}" */
    val dataExpr: String,
    /** Field name of each item used as Compose `key(…)`, e.g. "id" */
    val itemKey: String? = null,
    /** Template rendered for each item; \${item.xxx} refs are resolved per element */
    val itemTemplate: UiNode,
    /** >1 → LazyVerticalGrid with this many columns */
    val columns: Int = 1,
)

/**
 * Describes a side-effect to trigger on a UI event.
 *
 * Actions are composable via [onSuccess] / [onError] chains, enabling patterns like:
 * http → setState on success → toast on error.
 */
@Immutable
data class ActionSpec(
    val type: String,
    val params: Map<String, String> = emptyMap(),
    val onSuccess: ActionSpec? = null,
    val onError: ActionSpec? = null,
    val onConfirm: ActionSpec? = null,   // used by "dialog" action
)

// ─── Companion helpers ────────────────────────────────────────────────────────

/** Shorthand to read a required prop or throw a descriptive error during render. */
fun UiNode.requireProp(key: String): Any =
    props[key] ?: error("UiNode(type=$type) is missing required prop '$key'")

/** Shorthand to read an optional prop with a fallback value. */
fun UiNode.prop(key: String, default: String = ""): Any? = props[key] ?: default

/** Convenience: read a prop as String, coercing null to empty string. */
fun UiNode.propString(key: String, default: String = ""): String =
    props[key]?.toString() ?: default

/** Convenience: read a prop as Int. */
fun UiNode.propInt(key: String, default: Int = 0): Int =
    (props[key] as? Number)?.toInt() ?: default

/** Convenience: read a prop as Float. */
fun UiNode.propFloat(key: String, default: Float = 0f): Float =
    (props[key] as? Number)?.toFloat() ?: default

/** Convenience: read a prop as Boolean. */
fun UiNode.propBool(key: String, default: Boolean = false): Boolean =
    (props[key] as? Boolean) ?: default

/** Read a prop as a child UiNode if the value is a node-shaped parameter. */
fun UiNode.propNode(key: String): UiNode? = props[key] as? UiNode

/** Read a prop as a list of child UiNodes if the value is a node-array parameter. */
@Suppress("UNCHECKED_CAST")
fun UiNode.propNodeList(key: String): List<UiNode> = when (val value = props[key]) {
    is List<*> -> value.filterIsInstance<UiNode>()
    else -> emptyList()
}

/** Check if this node has an action registered for the given event. */
fun UiNode.hasAction(event: String): Boolean = actions.containsKey(event)
