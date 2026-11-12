package com.halilibo.richtext.commonmark

import com.shifenmiao.model.node.AstBlockQuote
import com.shifenmiao.model.node.AstCode
import com.shifenmiao.model.node.AstDocument
import com.shifenmiao.model.node.AstEmphasis
import com.shifenmiao.model.node.AstFencedCodeBlock
import com.shifenmiao.model.node.AstHardLineBreak
import com.shifenmiao.model.node.AstHeading
import com.shifenmiao.model.node.AstHtmlBlock
import com.shifenmiao.model.node.AstHtmlInline
import com.shifenmiao.model.node.AstImage
import com.shifenmiao.model.node.AstIndentedCodeBlock
import com.shifenmiao.model.node.AstJLatexBlockMath
import com.shifenmiao.model.node.AstJLatexNodeMath
import com.shifenmiao.model.node.AstLink
import com.shifenmiao.model.node.AstLinkReferenceDefinition
import com.shifenmiao.model.node.AstListItem
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstNodeLinks
import com.shifenmiao.model.node.AstNodeType
import com.shifenmiao.model.node.AstOrderedList
import com.shifenmiao.model.node.AstParagraph
import com.shifenmiao.model.node.AstSoftLineBreak
import com.shifenmiao.model.node.AstStrikethrough
import com.shifenmiao.model.node.AstStrongEmphasis
import com.shifenmiao.model.node.AstTableBody
import com.shifenmiao.model.node.AstTableCell
import com.shifenmiao.model.node.AstTableCellAlignment
import com.shifenmiao.model.node.AstTableHeader
import com.shifenmiao.model.node.AstTableRoot
import com.shifenmiao.model.node.AstTableRow
import com.shifenmiao.model.node.AstText
import com.shifenmiao.model.node.AstThematicBreak
import com.shifenmiao.model.node.AstUnorderedList
import io.noties.markwon.ext.latex.JLatexMathBlock
import io.noties.markwon.ext.latex.JLatexMathNode
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableCell.Alignment.CENTER
import org.commonmark.ext.gfm.tables.TableCell.Alignment.LEFT
import org.commonmark.ext.gfm.tables.TableCell.Alignment.RIGHT
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.CustomBlock
import org.commonmark.node.CustomNode
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak

private class ConvertWorkItem(
    val startNode: Node,
    val parentNode: AstNode?,
    val initialPreviousNode: AstNode?,
    val onFirstCreated: (AstNode?) -> Unit
)

/**
 * Converts the CommonMark tree iteratively so long sibling chains and deep nesting cannot overflow
 * the Android thread stack.
 */
internal fun convert(
    node: Node?,
    parentNode: AstNode? = null,
    previousNode: AstNode? = null,
): AstNode? {
    node ?: return null

    var result: AstNode? = null
    val pending = ArrayDeque<ConvertWorkItem>()
    pending.addLast(ConvertWorkItem(node, parentNode, previousNode) { result = it })

    while (pending.isNotEmpty()) {
        val work = pending.removeLast()
        var currentNode: Node? = work.startNode
        var previousAstNode = work.initialPreviousNode
        var firstAstNode: AstNode? = null

        while (currentNode != null) {
            val sourceNode = currentNode
            val converted = sourceNode.toAstNodes(work.parentNode, previousAstNode)
            if (converted == null) {
                currentNode = sourceNode.next
                continue
            }

            val (head, tail) = converted
            if (firstAstNode == null) firstAstNode = head
            previousAstNode?.links?.next = head
            head.links.previous = previousAstNode

            sourceNode.firstChild?.let { child ->
                pending.addLast(ConvertWorkItem(child, head, null) { head.links.firstChild = it })
            }

            previousAstNode = tail
            currentNode = sourceNode.next
        }

        work.parentNode?.links?.lastChild = previousAstNode
        work.onFirstCreated(firstAstNode)
    }

    return result
}

private fun Node.toAstNodes(
    parentNode: AstNode?,
    previousNode: AstNode?
): Pair<AstNode, AstNode>? {
    if (this is Text) {
        splitInlineMathNodes(literal, parentNode, previousNode)?.let { return it }
    }
    val type = toAstNodeType() ?: return null
    val newNode = AstNode(type, AstNodeLinks(parent = parentNode, previous = previousNode))
    return newNode to newNode
}

private fun Node.toAstNodeType(): AstNodeType? = when (this) {
    is BlockQuote -> AstBlockQuote
    is BulletList -> AstUnorderedList(bulletMarker = marker?.firstOrNull() ?: '\u0000')
    is Code -> AstCode(literal = literal)
    is Document -> AstDocument
    is Emphasis -> AstEmphasis(delimiter = openingDelimiter)
    is FencedCodeBlock -> AstFencedCodeBlock(
        literal = literal,
        fenceChar = fenceCharacter?.firstOrNull() ?: '\u0000',
        fenceIndent = fenceIndent,
        fenceLength = openingFenceLength ?: 0,
        info = info
    )

    is HardLineBreak -> AstHardLineBreak
    is Heading -> AstHeading(
        // commonmark 的 level 从 1 开始,Heading 样式表以 0 为最高级别
        level = level - 1
    )

    is ThematicBreak -> AstThematicBreak
    is HtmlInline -> AstHtmlInline(literal = literal)
    is HtmlBlock -> AstHtmlBlock(literal = literal)
    is Image -> destination?.let {
        AstImage(title = title ?: "", destination = it)
    }

    is IndentedCodeBlock -> AstIndentedCodeBlock(literal = literal)
    is Link -> AstLink(title = title ?: "", destination = destination)
    is ListItem -> AstListItem
    is OrderedList -> AstOrderedList(
        startNumber = markerStartNumber ?: 0,
        delimiter = markerDelimiter?.firstOrNull() ?: '\u0000'
    )
    is Paragraph -> AstParagraph
    is SoftLineBreak -> AstSoftLineBreak
    is StrongEmphasis -> AstStrongEmphasis(delimiter = openingDelimiter)
    is Text -> AstText(literal = literal)
    is LinkReferenceDefinition -> AstLinkReferenceDefinition(
        title = title ?: "",
        destination = destination,
        label = label
    )

    is TableBlock -> AstTableRoot
    is TableHead -> AstTableHeader
    is TableBody -> AstTableBody
    is TableRow -> AstTableRow
    is TableCell -> AstTableCell(
        header = isHeader,
        alignment = when (alignment) {
            LEFT -> AstTableCellAlignment.LEFT
            CENTER -> AstTableCellAlignment.CENTER
            RIGHT -> AstTableCellAlignment.RIGHT
            null -> AstTableCellAlignment.LEFT
        }
    )

    is Strikethrough -> AstStrikethrough(openingDelimiter)
    is JLatexMathNode -> AstJLatexNodeMath(latex = latex(), spanned = null)
    is JLatexMathBlock -> AstJLatexBlockMath(latex = latex(), spanned = null)
    is CustomNode -> null
    is CustomBlock -> null
    else -> null
}

private val InlineDoubleDollarMathRegex = Regex("""(?<!\\)\$\$(.+?)(?<!\\)\$\$""")
private val InlineDollarMathRegex = Regex("""(?<![\\$])\$(?!\$)(.+?)(?<![\\$])\$(?!\$)""")
private val InlineParenMathRegex = Regex("""\\\((.+?)\\\)""")

private fun splitInlineMathNodes(
    text: String,
    parentNode: AstNode?,
    previousNode: AstNode?,
): Pair<AstNode, AstNode>? {
    if (!text.contains('$') && !text.contains("\\(")) return null
    val hasInline = InlineDoubleDollarMathRegex.containsMatchIn(text) ||
        InlineDollarMathRegex.containsMatchIn(text) ||
        InlineParenMathRegex.containsMatchIn(text)
    if (!hasInline) return null

    var index = 0
    var head: AstNode? = null
    var prev: AstNode? = previousNode

    fun appendNode(node: AstNode) {
        if (head == null) {
            head = node
        }
        prev?.links?.next = node
        node.links.previous = prev
        prev = node
    }

    while (index < text.length) {
        val doubleDollarMatch = InlineDoubleDollarMathRegex.find(text, index)
        val dollarMatch = InlineDollarMathRegex.find(text, index)
        val parenMatch = InlineParenMathRegex.find(text, index)
        val nextMatch = listOfNotNull(doubleDollarMatch, dollarMatch, parenMatch)
            .minByOrNull { it.range.first }

        if (nextMatch == null) {
            if (index < text.length) {
                val literal = text.substring(index)
                appendNode(
                    AstNode(
                        AstText(literal = literal),
                        AstNodeLinks(parent = parentNode, previous = prev)
                    )
                )
            }
            break
        }

        if (nextMatch.range.first > index) {
            val literal = text.substring(index, nextMatch.range.first)
            appendNode(
                AstNode(
                    AstText(literal = literal),
                    AstNodeLinks(parent = parentNode, previous = prev)
                )
            )
        }

        val latexContent = nextMatch.groupValues.getOrNull(1).orEmpty()
        val hasLineBreak = latexContent.contains('\n') || latexContent.contains('\r')
        if (latexContent.isBlank() || hasLineBreak) {
            val literal = text.substring(nextMatch.range.first, nextMatch.range.last + 1)
            appendNode(
                AstNode(
                    AstText(literal = literal),
                    AstNodeLinks(parent = parentNode, previous = prev)
                )
            )
        } else {
            appendNode(
                AstNode(
                    AstJLatexNodeMath(
                        latex = latexContent,
                        spanned = null
                    ),
                    AstNodeLinks(parent = parentNode, previous = prev)
                )
            )
        }

        index = nextMatch.range.last + 1
    }

    val headNode = head ?: return null
    val tailNode = prev ?: headNode
    previousNode?.links?.next = headNode

    return headNode to tailNode
}

