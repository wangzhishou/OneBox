package com.hrm.diagram.render.family

import com.hrm.diagram.core.ir.ActivityBlock
import com.hrm.diagram.core.ir.ActivityIR
import com.hrm.diagram.core.ir.StructIR
import com.hrm.diagram.core.ir.StructNode
import com.hrm.diagram.core.ir.WireBox
import com.hrm.diagram.core.ir.WireframeIR

internal object StructuralEntityKeys {
    fun activityKeys(prefix: String, model: ActivityIR): List<String> =
        buildList {
            model.blocks.forEachIndexed { index, block -> addActivityBlockKeys(this, prefix, "activity.$index", block) }
        }

    fun wireframeKeys(prefix: String, model: WireframeIR): List<String> =
        buildList { addWireBoxKeys(this, prefix, "wireframe.root", model.root) }

    fun structKeys(prefix: String, model: StructIR): List<String> =
        buildList { addStructKeys(this, prefix, "struct.root", model.root) }

    private fun addActivityBlockKeys(keys: MutableList<String>, prefix: String, path: String, block: ActivityBlock) {
        keys += "$prefix.${stableSegment(path)}.${stableSegment(block::class.simpleName.orEmpty())}"
        when (block) {
            is ActivityBlock.IfElse -> {
                block.thenBranch.forEachIndexed { index, child -> addActivityBlockKeys(keys, prefix, "$path.then.$index", child) }
                block.elseBranch.forEachIndexed { index, child -> addActivityBlockKeys(keys, prefix, "$path.else.$index", child) }
            }
            is ActivityBlock.While -> block.body.forEachIndexed { index, child -> addActivityBlockKeys(keys, prefix, "$path.body.$index", child) }
            is ActivityBlock.ForkJoin -> block.branches.forEachIndexed { branchIndex, branch ->
                branch.forEachIndexed { index, child -> addActivityBlockKeys(keys, prefix, "$path.branch.$branchIndex.$index", child) }
            }
            is ActivityBlock.Action, is ActivityBlock.Note -> Unit
        }
    }

    private fun addWireBoxKeys(keys: MutableList<String>, prefix: String, path: String, box: WireBox) {
        keys += "$prefix.${stableSegment(path)}.${stableSegment(box::class.simpleName.orEmpty())}"
        when (box) {
            is WireBox.Plain -> box.children.forEachIndexed { index, child -> addWireBoxKeys(keys, prefix, "$path.child.$index", child) }
            is WireBox.TabbedGroup -> box.tabs.forEachIndexed { index, child -> addWireBoxKeys(keys, prefix, "$path.tab.$index", child) }
            is WireBox.Button, is WireBox.Image, is WireBox.Input -> Unit
        }
    }

    private fun addStructKeys(keys: MutableList<String>, prefix: String, path: String, node: StructNode) {
        val keySegment = stableSegment(node.key ?: path)
        keys += "$prefix.${stableSegment(path)}.$keySegment"
        when (node) {
            is StructNode.ObjectNode -> node.entries.forEachIndexed { index, child -> addStructKeys(keys, prefix, "$path.object.$index.${child.key.orEmpty()}", child) }
            is StructNode.ArrayNode -> node.items.forEachIndexed { index, child -> addStructKeys(keys, prefix, "$path.array.$index.${child.key.orEmpty()}", child) }
            is StructNode.Scalar -> Unit
        }
    }
}
