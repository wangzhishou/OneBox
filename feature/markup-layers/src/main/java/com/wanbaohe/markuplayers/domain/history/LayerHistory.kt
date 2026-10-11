package com.wanbaohe.markuplayers.domain.history

import com.t8rin.imagetoolbox.core.filters.presentation.serialization.UiFilterCodec
import com.wanbaohe.markuplayers.domain.model.LayerEffect
import com.wanbaohe.markuplayers.domain.model.LayerMask
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.project.MarkupDocument

/**
 * Snapshot the complete document before an edit, including base edits and export preferences.
 * A gesture can take one pre-edit snapshot and apply any number of transient updates.
 */
class LayerHistory(
    private val maxSize: Int = 50,
) {

    init {
        require(maxSize in 1..50) { "History capacity must be between 1 and 50" }
    }

    private val undoStack = ArrayDeque<MarkupDocument>()
    private val redoStack = ArrayDeque<MarkupDocument>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun snapshot(document: MarkupDocument) {
        pushUndo(document)
        redoStack.clear()
    }

    /** Replace a coalesced edit's pre-edit snapshot; an empty history is left unchanged. */
    fun replaceLastSnapshot(document: MarkupDocument): Boolean {
        if (undoStack.isEmpty()) return false
        val frozen = document.frozen()
        undoStack.removeLast()
        undoStack.addLast(frozen)
        redoStack.clear()
        return true
    }

    fun undo(current: MarkupDocument): MarkupDocument? {
        if (undoStack.isEmpty()) return null
        redoStack.addLast(current.frozen())
        return undoStack.removeLast()
    }

    fun redo(current: MarkupDocument): MarkupDocument? {
        if (redoStack.isEmpty()) return null
        pushUndo(current)
        return redoStack.removeLast()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    private fun pushUndo(document: MarkupDocument) {
        undoStack.addLast(document.frozen())
        if (undoStack.size > maxSize) undoStack.removeFirst()
    }

    private fun MarkupDocument.frozen(): MarkupDocument = copy(
        baseEffects = baseEffects.frozenEffects(),
        baseMask = baseMask?.frozen(),
        layers = layers.map { layer ->
            layer.copy(
                type = when (val type = layer.type) {
                    is LayerType.Draw -> type.copy(
                        strokes = type.strokes.map { it.copy(points = it.points.toList()) }
                    )
                    is LayerType.Text -> type.copy(decorations = type.decorations.toSet())
                    else -> type
                },
                filter = layer.filter?.let(UiFilterCodec::snapshot),
                effects = layer.effects.frozenEffects(),
                mask = layer.mask?.frozen(),
                imageHistory = layer.imageHistory.toList(),
            )
        },
    )

    private fun List<LayerEffect>.frozenEffects(): List<LayerEffect> =
        map { it.copy(filter = UiFilterCodec.snapshot(it.filter)) }

    private fun LayerMask.frozen(): LayerMask =
        copy(strokes = strokes.map { it.copy(points = it.points.toList()) })
}
