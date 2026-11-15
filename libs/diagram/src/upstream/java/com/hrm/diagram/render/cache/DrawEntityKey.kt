package com.hrm.diagram.render.cache

import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.layout.EdgeRouteKey

/**
 * Stable entity-key factory for [DrawEntity] streams.
 *
 * Keys are intentionally based on IR/layout identities instead of draw-command position, so
 * coordinate/style updates to an existing entity do not pollute `SessionPatch.addedDrawCommands`.
 */
internal object DrawEntityKey {
    fun graphBackground(prefix: String): String = "${prefix.normalizedPrefix()}.graph.background"

    fun node(prefix: String, id: NodeId): String = "${prefix.normalizedPrefix()}.node.${id.value}"

    fun cluster(prefix: String, id: NodeId): String = "${prefix.normalizedPrefix()}.cluster.${id.value}"

    fun edge(prefix: String, from: NodeId, to: NodeId, ordinal: Int): String =
        "${prefix.normalizedPrefix()}.edge.$ordinal.${from.value}->${to.value}"

    fun edge(prefix: String, key: EdgeRouteKey): String = edge(prefix, key.from, key.to, key.ordinal)

    fun nodeLabel(prefix: String, id: NodeId): String = "${node(prefix, id)}.label"

    fun edgeLabel(prefix: String, from: NodeId, to: NodeId, ordinal: Int): String =
        "${edge(prefix, from, to, ordinal)}.label"

    fun decoration(prefix: String, ownerKey: String, kind: String, ordinal: Int = 0): String =
        "${prefix.normalizedPrefix()}.decoration.$ownerKey.${kind.stableSegment()}.$ordinal"

    private fun String.normalizedPrefix(): String = stableSegment().ifBlank { "diagram" }

    private fun String.stableSegment(): String =
        trim()
            .lowercase()
            .map { ch ->
                when {
                    ch.isLetterOrDigit() -> ch
                    ch == '.' || ch == '-' || ch == '_' -> ch
                    else -> '_'
                }
            }
            .joinToString("")
            .trim('.', '-', '_')
}
