package com.hrm.markdown.renderer.internal.core.compile

import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel

/**
 * Persistent root slots. Invisible AST blocks retain a slot while [size] and [get] expose only
 * rendered blocks. Splicing never copies a stable prefix or suffix, and old models remain valid.
 */
internal class RenderBlockSlots private constructor(private val root: SlotTree?) :
    AbstractList<InternalRenderBlockModel>() {
    val slotCount: Int get() = root.slotCount
    override val size: Int get() = root.renderCount

    override fun get(index: Int): InternalRenderBlockModel {
        checkElementIndex(index)
        var remaining = index
        var current = root!!
        while (current is SlotBranch) {
            val leftCount = current.left.renderCount
            if (remaining < leftCount) current = current.left
            else {
                remaining -= leftCount
                current = current.right
            }
        }
        return (current as SlotLeaf).model!!
    }

    override fun iterator(): Iterator<InternalRenderBlockModel> = object : Iterator<InternalRenderBlockModel> {
        private val pending = ArrayDeque<SlotTree>()
        private var nextModel: InternalRenderBlockModel? = null

        init {
            root?.let { pending.addLast(it) }
            advance()
        }

        private fun advance() {
            nextModel = null
            while (pending.isNotEmpty()) {
                when (val tree = pending.removeLast()) {
                    is SlotBranch -> {
                        if (tree.right.renderCount > 0) pending.addLast(tree.right)
                        if (tree.left.renderCount > 0) pending.addLast(tree.left)
                    }
                    is SlotLeaf -> if (tree.model != null) {
                        nextModel = tree.model
                        return
                    }
                }
            }
        }

        override fun hasNext(): Boolean = nextModel != null
        override fun next(): InternalRenderBlockModel {
            val result = nextModel ?: throw NoSuchElementException()
            advance()
            return result
        }
    }

    fun splice(start: Int, removedCount: Int, inserted: List<InternalRenderBlockModel?>): RenderBlockSlots {
        require(start >= 0 && removedCount >= 0 && start <= slotCount - removedCount)
        val (prefix, rest) = split(root, start)
        val (_, suffix) = split(rest, removedCount)
        return RenderBlockSlots(join(join(prefix, build(inserted, 0, inserted.size)), suffix))
    }

    private fun checkElementIndex(index: Int) {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("index=$index, size=$size")
    }

    companion object {
        fun from(slots: List<InternalRenderBlockModel?>): RenderBlockSlots =
            RenderBlockSlots(build(slots, 0, slots.size))
    }
}

private sealed interface SlotTree {
    val height: Int
    val slotCount: Int
    val renderCount: Int
}

private class SlotLeaf(val model: InternalRenderBlockModel?) : SlotTree {
    override val height: Int get() = 1
    override val slotCount: Int get() = 1
    override val renderCount: Int get() = if (model == null) 0 else 1
}

private class SlotBranch(val left: SlotTree, val right: SlotTree) : SlotTree {
    override val height: Int = maxOf(left.height, right.height) + 1
    override val slotCount: Int = left.slotCount + right.slotCount
    override val renderCount: Int = left.renderCount + right.renderCount
}

private val SlotTree?.slotCount: Int get() = this?.slotCount ?: 0
private val SlotTree?.renderCount: Int get() = this?.renderCount ?: 0

private fun build(slots: List<InternalRenderBlockModel?>, start: Int, end: Int): SlotTree? {
    if (start == end) return null
    if (end - start == 1) return SlotLeaf(slots[start])
    val middle = (start + end) ushr 1
    return SlotBranch(build(slots, start, middle)!!, build(slots, middle, end)!!)
}

private fun join(left: SlotTree?, right: SlotTree?): SlotTree? {
    if (left == null) return right
    if (right == null) return left
    return when {
        left.height > right.height + 1 -> {
            left as SlotBranch
            balance(left.left, join(left.right, right)!!)
        }
        right.height > left.height + 1 -> {
            right as SlotBranch
            balance(join(left, right.left)!!, right.right)
        }
        else -> SlotBranch(left, right)
    }
}

private fun balance(left: SlotTree, right: SlotTree): SlotTree = when {
    left.height > right.height + 1 -> {
        left as SlotBranch
        if (left.left.height >= left.right.height) {
            SlotBranch(left.left, SlotBranch(left.right, right))
        } else {
            val pivot = left.right as SlotBranch
            SlotBranch(SlotBranch(left.left, pivot.left), SlotBranch(pivot.right, right))
        }
    }
    right.height > left.height + 1 -> {
        right as SlotBranch
        if (right.right.height >= right.left.height) {
            SlotBranch(SlotBranch(left, right.left), right.right)
        } else {
            val pivot = right.left as SlotBranch
            SlotBranch(SlotBranch(left, pivot.left), SlotBranch(pivot.right, right.right))
        }
    }
    else -> SlotBranch(left, right)
}

private fun split(tree: SlotTree?, index: Int): Pair<SlotTree?, SlotTree?> {
    if (tree == null || index == 0) return null to tree
    if (index == tree.slotCount) return tree to null
    tree as SlotBranch
    return if (index < tree.left.slotCount) {
        val (prefix, rest) = split(tree.left, index)
        prefix to join(rest, tree.right)
    } else {
        val (prefix, rest) = split(tree.right, index - tree.left.slotCount)
        join(tree.left, prefix) to rest
    }
}
