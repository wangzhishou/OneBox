package com.halilibo.richtext.commonmark

import com.halilibo.richtext.markwon.MarkdownAstNodeParser
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

/**
 * Converts common-markdown tree to AstNode tree in a recursive fashion.
 */
internal fun convert(
    node: Node?,
    parentNode: AstNode? = null,
    previousNode: AstNode? = null,
    markdownAstNodeParser: MarkdownAstNodeParser,
): AstNode? {
    node ?: return null

    val nodeLinks = AstNodeLinks(
        parent = parentNode,
        previous = previousNode,
    )

    val newNodeType: AstNodeType? = when (node) {
        is BlockQuote -> AstBlockQuote
        is BulletList -> AstUnorderedList(bulletMarker = node.bulletMarker)
        is Code -> AstCode(literal = node.literal)
        is Document -> AstDocument
        is Emphasis -> AstEmphasis(delimiter = node.openingDelimiter)
        is FencedCodeBlock -> AstFencedCodeBlock(
            literal = node.literal,
            fenceChar = node.fenceChar,
            fenceIndent = node.fenceIndent,
            fenceLength = node.fenceLength,
            info = node.info
        )

        is HardLineBreak -> AstHardLineBreak
        is Heading -> AstHeading(
            level = node.level
        )

        is ThematicBreak -> AstThematicBreak
        is HtmlInline -> AstHtmlInline(
            literal = node.literal
        )

        is HtmlBlock -> AstHtmlBlock(
            literal = node.literal
        )

        is Image -> {
            if (node.destination == null) {
                null
            } else {
                AstImage(
                    title = node.title ?: "",
                    destination = node.destination
                )
            }
        }

        is IndentedCodeBlock -> AstIndentedCodeBlock(
            literal = node.literal
        )

        is Link -> AstLink(
            title = node.title ?: "",
            destination = node.destination
        )

        is ListItem -> AstListItem
        is OrderedList -> AstOrderedList(
            startNumber = node.startNumber,
            delimiter = node.delimiter
        )

        is Paragraph -> AstParagraph
        is SoftLineBreak -> AstSoftLineBreak
        is StrongEmphasis -> AstStrongEmphasis(
            delimiter = node.openingDelimiter
        )

        is Text -> {
            val splitNodes = splitInlineMathNodes(
                text = node.literal,
                parentNode = parentNode,
                previousNode = previousNode
            )
            if (splitNodes != null) {
                val (head, tail) = splitNodes
                val nextNode = convert(
                    node.next,
                    parentNode = parentNode,
                    previousNode = tail,
                    markdownAstNodeParser = markdownAstNodeParser
                )
                tail.links.next = nextNode
                nextNode?.links?.previous = tail
                if (node.next == null) {
                    parentNode?.links?.lastChild = tail
                }
                return head
            }
            AstText(
                literal = node.literal
            )
        }

        is LinkReferenceDefinition -> AstLinkReferenceDefinition(
            title = node.title ?: "",
            destination = node.destination,
            label = node.label
        )

        is TableBlock -> AstTableRoot
        is TableHead -> AstTableHeader
        is TableBody -> AstTableBody
        is TableRow -> AstTableRow
        is TableCell -> AstTableCell(
            header = node.isHeader,
            alignment = when (node.alignment) {
                LEFT -> AstTableCellAlignment.LEFT
                CENTER -> AstTableCellAlignment.CENTER
                RIGHT -> AstTableCellAlignment.RIGHT
                null -> AstTableCellAlignment.LEFT
            }
        )

        is Strikethrough -> AstStrikethrough(
            node.openingDelimiter
        )

        is JLatexMathNode -> {
            AstJLatexNodeMath(
                latex = node.latex(),
                spanned = markdownAstNodeParser.markdown?.render(node)
            )
        }

        is JLatexMathBlock -> AstJLatexBlockMath(
            latex = node.latex(),
            spanned = markdownAstNodeParser.markdown?.render(node)
        )

        is CustomNode -> null
        is CustomBlock -> null
        else -> null
    }

    val newNode = newNodeType?.let {
        AstNode(newNodeType, nodeLinks)
    }

    // Unsupported nodes (CustomNode/CustomBlock/null-destination Image, etc.) must not
    // drop the rest of the sibling chain — continue converting `node.next`.
    if (newNode == null) {
        val nextAst = convert(
            node.next,
            parentNode = parentNode,
            previousNode = previousNode,
            markdownAstNodeParser = markdownAstNodeParser
        )
        if (node.next == null) {
            parentNode?.links?.lastChild = previousNode
        }
        return nextAst
    }

    newNode.links.firstChild = convert(
        node.firstChild,
        parentNode = newNode,
        previousNode = null,
        markdownAstNodeParser
    )
    newNode.links.next = convert(
        node.next,
        parentNode = parentNode,
        previousNode = newNode,
        markdownAstNodeParser
    )

    if (node.next == null) {
        parentNode?.links?.lastChild = newNode
    }

    return newNode
}

private val InlineDollarMathRegex = Regex("(?<!\\\\)\\$(.+?)(?<!\\\\)\\$")
private val InlineParenMathRegex = Regex("""\\\((.+?)\\\)""")

private fun splitInlineMathNodes(
    text: String,
    parentNode: AstNode?,
    previousNode: AstNode?,
): Pair<AstNode, AstNode>? {
    if (!text.contains('$') && !text.contains("\\(")) return null
    val hasInline = InlineDollarMathRegex.containsMatchIn(text) || InlineParenMathRegex.containsMatchIn(text)
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
        val dollarMatch = InlineDollarMathRegex.find(text, index)
        val parenMatch = InlineParenMathRegex.find(text, index)
        val nextMatch = listOfNotNull(dollarMatch, parenMatch)
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

