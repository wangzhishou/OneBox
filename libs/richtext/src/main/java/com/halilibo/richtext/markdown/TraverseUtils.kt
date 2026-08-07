package com.halilibo.richtext.markdown

import com.shifenmiao.model.node.AstCode
import com.shifenmiao.model.node.AstHardLineBreak
import com.shifenmiao.model.node.AstImage
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstNodeType
import com.shifenmiao.model.node.AstSoftLineBreak
import com.shifenmiao.model.node.AstText
import com.shifenmiao.model.node.AstHtmlInline
import com.shifenmiao.model.node.AstJLatexNodeMath

internal fun AstNode.childrenSequence(
  reverse: Boolean = false
): Sequence<AstNode> {
  return if (!reverse) {
    generateSequence(this.links.firstChild) { it.links.next }
  } else {
    generateSequence(this.links.lastChild) { it.links.previous }
  }
}

/**
 * Markdown rendering is susceptible to have assumptions. Hence, some rendering rules
 * may force restrictions on children. So, valid children nodes should be selected
 * before traversing. This function returns a LinkedList of children which conforms to
 * [filter] function.
 *
 * @param filter A lambda to select valid children.
 */
internal fun AstNode.filterChildren(
  reverse: Boolean = false,
  filter: (AstNode) -> Boolean
): Sequence<AstNode> {
  return childrenSequence(reverse).filter(filter)
}

internal inline fun <reified T : AstNodeType> AstNode.filterChildrenType(): Sequence<AstNode> {
  return filterChildren { it.type is T }
}

/**
 * These ASTNode types should never have any children. If any exists, ignore them.
 */
internal fun AstNode.isRichTextTerminal(): Boolean {
  return type is AstText
          || type is AstCode
          || type is AstImage
          || type is AstHtmlInline
          || type is AstJLatexNodeMath
          || type is AstSoftLineBreak
          || type is AstHardLineBreak
}
