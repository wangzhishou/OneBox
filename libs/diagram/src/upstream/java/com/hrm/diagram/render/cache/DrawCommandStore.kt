package com.hrm.diagram.render.cache

import com.hrm.diagram.core.draw.DrawCommand

/**
 * Session-local store for draw commands.
 *
 * The public snapshot still carries a complete draw list for repaint-friendly UI backends, while
 * SessionPatch.addedDrawCommands must only contain commands that entered the stream in this
 * advance. Renderers submit stable [DrawEntity] keys so updates to an existing node/edge do not
 * look like newly appended commands.
 */
internal class DrawCommandStore {
    private val entityCommands: LinkedHashMap<String, List<DrawCommand>> = LinkedHashMap()
    private var latestFullFrame: List<DrawCommand> = emptyList()

    val snapshot: List<DrawCommand>
        get() = latestFullFrame

    /** New entity keys produce added commands; existing keys update the full-frame snapshot. */
    fun updateEntities(entities: List<DrawEntity>): DrawCommandDelta {
        val added = ArrayList<DrawCommand>()
        val next = LinkedHashMap<String, List<DrawCommand>>(entities.size)
        for (entity in entities) {
            if (entity.key !in entityCommands) added += entity.commands
            next[entity.key] = entity.commands
        }
        entityCommands.clear()
        entityCommands.putAll(next)
        latestFullFrame = entities.flatMap { it.commands }
        return DrawCommandDelta(fullFrame = latestFullFrame, addedCommands = added)
    }

    fun clear() {
        entityCommands.clear()
        latestFullFrame = emptyList()
    }
}

internal data class DrawEntity(
    val key: String,
    val commands: List<DrawCommand>,
)

internal data class DrawCommandDelta(
    val fullFrame: List<DrawCommand>,
    val addedCommands: List<DrawCommand>,
)
