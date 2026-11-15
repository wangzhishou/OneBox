package com.hrm.markdown.renderer.inline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hrm.markdown.parser.SourceRange
import com.hrm.markdown.parser.ast.ContainerNode
import com.hrm.markdown.parser.ast.Node
import com.hrm.markdown.renderer.LocalMarkdownRenderOptions
import com.hrm.markdown.renderer.internal.core.compile.compileInlineModel
import com.hrm.markdown.renderer.internal.core.compile.computeSemanticRevision
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromText
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromValues
import com.hrm.markdown.renderer.internal.core.model.InlineModel

private const val INLINE_REVISION_OFFSET_BASIS = -3750763034362895579L
private const val INLINE_REVISION_FNV_PRIME = 1099511628211L

@Composable
internal fun rememberInlineModel(parent: ContainerNode): InlineModel {
    val inlineRevision = remember(
        parent.contentHash,
        parent.lineRange.startLine,
        parent.lineRange.endLine,
        parent.childCount(),
    ) {
        inlineNodesRevision(parent.children)
    }
    return rememberInlineModel(
        nodes = parent.children,
        inlineRevision = inlineRevision,
        parentStableId = legacyInlineParentStableId(parent),
    )
}

@Composable
internal fun rememberInlineModel(
    nodes: List<Node>,
    inlineRevision: Long,
    parentStableId: Long,
): InlineModel {
    val softBreakPolicy = LocalMarkdownRenderOptions.current.softBreakPolicy
    return remember(inlineRevision, parentStableId, softBreakPolicy) {
        compileInlineModel(
            nodes = nodes,
            inlineRevision = inlineRevision,
            parentStableId = parentStableId,
            softBreakPolicy = softBreakPolicy,
        )
    }
}

internal fun legacyInlineParentStableId(parent: ContainerNode): Long {
    val typeId = renderIdentityFromText(parent::class.simpleName ?: "inline-parent")
    return if (parent.sourceRange != SourceRange.EMPTY) {
        renderIdentityFromValues(
            typeId,
            parent.sourceRange.start.offset.toLong(),
            parent.lineRange.startLine.toLong(),
        )
    } else {
        renderIdentityFromValues(typeId, parent.stableKey.toLong())
    }
}

internal fun inlineNodesRevision(nodes: List<Node>): Long {
    var acc = INLINE_REVISION_OFFSET_BASIS
    for (node in nodes) {
        acc = inlineNodeRevision(acc, node)
    }
    return acc
}

private fun inlineNodeRevision(acc: Long, node: Node): Long {
    return inlineMixRevision(acc, computeSemanticRevision(node))
}

private fun inlineMixRevision(acc: Long, value: Long): Long = (acc xor value) * INLINE_REVISION_FNV_PRIME
