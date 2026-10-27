package com.t8rin.imagetoolbox.core.domain.history

class DocumentHistory<T>(
    private val maxSize: Int = 50,
    private val freeze: (T) -> T,
) {

    init {
        require(maxSize in 1..50) { "History capacity must be between 1 and 50" }
    }

    private val undoStack = ArrayDeque<T>()
    private val redoStack = ArrayDeque<T>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun snapshot(document: T) {
        pushUndo(document)
        redoStack.clear()
    }

    fun replaceLastSnapshot(document: T): Boolean {
        if (undoStack.isEmpty()) return false
        val frozen = freeze(document)
        undoStack.removeLast()
        undoStack.addLast(frozen)
        redoStack.clear()
        return true
    }

    fun undo(current: T): T? {
        if (undoStack.isEmpty()) return null
        redoStack.addLast(freeze(current))
        return undoStack.removeLast()
    }

    fun redo(current: T): T? {
        if (redoStack.isEmpty()) return null
        pushUndo(current)
        return redoStack.removeLast()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    private fun pushUndo(document: T) {
        undoStack.addLast(freeze(document))
        if (undoStack.size > maxSize) undoStack.removeFirst()
    }
}
