package com.wanbaohe.richtext.editor.document

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wanbaohe.richtext.editor.markdown.MarkdownDocumentBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal sealed interface MarkdownEditorSnapshot {
    data class Blocks(val value: List<MarkdownDocumentBlock>) : MarkdownEditorSnapshot

    data class Source(val markdown: String) : MarkdownEditorSnapshot
}

internal class MarkdownDocumentHistory(
    private val scope: CoroutineScope,
    private val maxEntries: Int = 100,
) {
    private val undoStack = mutableStateListOf<MarkdownEditorSnapshot>()
    private val redoStack = mutableStateListOf<MarkdownEditorSnapshot>()
    private var current: MarkdownEditorSnapshot? = null
    private var pending by mutableStateOf<MarkdownEditorSnapshot?>(null)
    private var recordJob: Job? = null

    val canUndo: Boolean
        get() = pending != null || undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = pending == null && redoStack.isNotEmpty()

    fun reset(snapshot: MarkdownEditorSnapshot) {
        recordJob?.cancel()
        recordJob = null
        pending = null
        current = snapshot
        undoStack.clear()
        redoStack.clear()
    }

    fun record(snapshot: MarkdownEditorSnapshot) {
        pending = snapshot
        recordJob?.cancel()
        recordJob = scope.launch {
            delay(EDIT_HISTORY_DELAY_MS)
            flush()
        }
    }

    fun flush() {
        recordJob?.cancel()
        recordJob = null
        val snapshot = pending ?: return
        pending = null
        if (snapshot === current) return
        current?.let(undoStack::add)
        while (undoStack.size >= maxEntries) undoStack.removeAt(0)
        current = snapshot
        redoStack.clear()
    }

    fun undo(): MarkdownEditorSnapshot? {
        flush()
        if (undoStack.isEmpty()) return null
        current?.let(redoStack::add)
        return undoStack.removeAt(undoStack.lastIndex).also { current = it }
    }

    fun redo(): MarkdownEditorSnapshot? {
        flush()
        if (redoStack.isEmpty()) return null
        current?.let(undoStack::add)
        return redoStack.removeAt(redoStack.lastIndex).also { current = it }
    }
}

private const val EDIT_HISTORY_DELAY_MS = 500L
