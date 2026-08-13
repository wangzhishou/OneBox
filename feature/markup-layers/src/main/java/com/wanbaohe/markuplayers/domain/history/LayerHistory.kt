package com.wanbaohe.markuplayers.domain.history

import com.wanbaohe.markuplayers.domain.model.MarkupLayer

/**
 * 快照式 undo/redo:每次图层操作(增/删/改/排序)前先 [snapshot] 记录当前状态。
 * 纯 JVM 实现,与 Compose 解耦。
 */
class LayerHistory(
    private val maxSize: Int = 50,
) {

    private val undoStack = ArrayDeque<List<MarkupLayer>>()
    private val redoStack = ArrayDeque<List<MarkupLayer>>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** 在即将修改图层列表前调用,记录修改前的快照 */
    fun snapshot(layers: List<MarkupLayer>) {
        undoStack.addLast(layers)
        if (undoStack.size > maxSize) undoStack.removeFirst()
        redoStack.clear()
    }

    /**
     * 撤销:返回上一个快照;[current] 为当前图层列表(压入 redo 栈)。
     * 无可撤销时返回 null。
     */
    fun undo(current: List<MarkupLayer>): List<MarkupLayer>? {
        if (undoStack.isEmpty()) return null
        redoStack.addLast(current)
        return undoStack.removeLast()
    }

    /** 重做:返回下一个快照;无可重做时返回 null */
    fun redo(current: List<MarkupLayer>): List<MarkupLayer>? {
        if (redoStack.isEmpty()) return null
        undoStack.addLast(current)
        return redoStack.removeLast()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
