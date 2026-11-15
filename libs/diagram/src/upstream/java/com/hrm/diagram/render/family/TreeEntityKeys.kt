package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.TreeIR
import com.hrm.diagram.core.ir.TreeNode

internal object TreeEntityKeys {
    fun keys(prefix: String, model: TreeIR): List<String> {
        val keys = ArrayList<String>()
        fun visit(node: TreeNode) {
            keys += "$prefix.tree.node.${stableSegment(node.id.value)}"
            node.children.forEach { child ->
                keys += "$prefix.tree.edge.${stableSegment(node.id.value)}-${stableSegment(child.id.value)}"
                visit(child)
            }
        }
        visit(model.root)
        return keys
    }
}
