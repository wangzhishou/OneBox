package com.halilibo.richtext.markdown

import com.shifenmiao.model.node.AstBlockQuote
import com.shifenmiao.model.node.AstDocument
import com.shifenmiao.model.node.AstFencedCodeBlock
import com.shifenmiao.model.node.AstHeading
import com.shifenmiao.model.node.AstIndentedCodeBlock
import com.shifenmiao.model.node.AstJLatexBlockMath
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstOrderedList
import com.shifenmiao.model.node.AstParagraph
import com.shifenmiao.model.node.AstTableRoot
import com.shifenmiao.model.node.AstThematicBreak
import com.shifenmiao.model.node.AstUnorderedList

/**
 * 将 Markdown AST 拆分为顶层 Block 节点列表。
 *
 * 与 [MessageUiModel.MarkdownBlock.splitIntoBlocks] 的区别：
 * - 不依赖 MessageEntity，返回纯 [AstNode] 列表，更通用。
 * - 不生成 UI 模型，仅做树拆分。
 *
 * @param rootNode Document 根节点或任意 AstNode
 * @return 顶层 block 节点列表（顺序与原文档一致）
 */
fun splitMarkdownBlocks(rootNode: AstNode?): List<AstNode> {
    rootNode ?: return emptyList()

    val blocks = mutableListOf<AstNode>()

    fun processNode(node: AstNode?) {
        node ?: return
        when (node.type) {
            is AstHeading,
            is AstParagraph,
            is AstFencedCodeBlock,
            is AstIndentedCodeBlock,
            is AstBlockQuote,
            is AstOrderedList,
            is AstUnorderedList,
            is AstTableRoot,
            is AstThematicBreak,
            is AstJLatexBlockMath -> {
                blocks.add(node)
                return
            }

            is AstDocument -> {
                var child = node.links.firstChild
                while (child != null) {
                    processNode(child)
                    child = child.links.next
                }
            }

            else -> {
                // Unknown / non-top-level block types are skipped.
                // Do NOT walk `node.links.next` here — Document already iterates siblings,
                // and doing so again would double-process (or worse, infinite-loop) them.
            }
        }
    }

    processNode(rootNode)
    return blocks
}
