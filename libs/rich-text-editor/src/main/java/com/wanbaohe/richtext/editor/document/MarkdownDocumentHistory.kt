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

    // pending 与 current 内容相同（例如空块被删掉又立刻补回）时，flush 会跳过提交，
    // 此时并不存在真正的撤销点，按钮不该亮起，否则点了撤销却毫无反应。
    val canUndo: Boolean
        get() {
            val pendingSnapshot = pending
            return undoStack.isNotEmpty() || (pendingSnapshot != null && !pendingSnapshot.hasSameContent(current))
        }

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
        // 快照按「内容」比对：captureBlocks 每次都新建对象，引用永远不等；而 block id 只是
        // Compose 的列表 key，不该参与判断，否则删掉空块又补回新块这种等价改动仍会留下一个
        // 点了没反应的撤销点。用 === 则会把「内容其实没变」的状态全塞进撤销栈。
        if (snapshot.hasSameContent(current)) return
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

/**
 * 撤销点只认「用户能看见的内容」：block id 只是 Compose 的列表 key，块被删掉再补回来
 * 会拿到一个新 id，但内容其实没变，不该算一次撤销。
 */
private fun MarkdownEditorSnapshot.hasSameContent(other: MarkdownEditorSnapshot?): Boolean = when (this) {
    is MarkdownEditorSnapshot.Source ->
        other is MarkdownEditorSnapshot.Source && markdown == other.markdown

    is MarkdownEditorSnapshot.Blocks ->
        other is MarkdownEditorSnapshot.Blocks &&
            value.size == other.value.size &&
            value.indices.all { index ->
                when (val mine = value[index]) {
                    is MarkdownDocumentBlock.RichText -> {
                        val theirs = other.value[index]
                        theirs is MarkdownDocumentBlock.RichText && mine.snapshot == theirs.snapshot
                    }

                    is MarkdownDocumentBlock.Raw -> {
                        val theirs = other.value[index]
                        theirs is MarkdownDocumentBlock.Raw &&
                            mine.kind == theirs.kind &&
                            mine.markdown == theirs.markdown
                    }
                }
            }
}

private const val EDIT_HISTORY_DELAY_MS = 500L
