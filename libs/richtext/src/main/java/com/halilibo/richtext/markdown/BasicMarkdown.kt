package com.halilibo.richtext.markdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.halilibo.richtext.ui.AsyncFencedCodeBlock
import com.halilibo.richtext.ui.BlockQuote
import com.halilibo.richtext.ui.CodeBlock
import com.halilibo.richtext.ui.FormattedList
import com.halilibo.richtext.ui.Heading
import com.halilibo.richtext.ui.HorizontalRule
import com.halilibo.richtext.ui.ListType.Ordered
import com.halilibo.richtext.ui.ListType.Unordered
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.InlineContent
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.richTextString
import com.shifenmiao.model.node.AstBlockNodeType
import com.shifenmiao.model.node.AstBlockQuote
import com.shifenmiao.model.node.AstDocument
import com.shifenmiao.model.node.AstFencedCodeBlock
import com.shifenmiao.model.node.AstHeading
import com.shifenmiao.model.node.AstHtmlBlock
import com.shifenmiao.model.node.AstImage
import com.shifenmiao.model.node.AstIndentedCodeBlock
import com.shifenmiao.model.node.AstInlineNodeType
import com.shifenmiao.model.node.AstJLatexBlockMath
import com.shifenmiao.model.node.AstLinkReferenceDefinition
import com.shifenmiao.model.node.AstListItem
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstOrderedList
import com.shifenmiao.model.node.AstParagraph
import com.shifenmiao.model.node.AstTableBody
import com.shifenmiao.model.node.AstTableCell
import com.shifenmiao.model.node.AstTableHeader
import com.shifenmiao.model.node.AstTableRoot
import com.shifenmiao.model.node.AstTableRow
import com.shifenmiao.model.node.AstText
import com.shifenmiao.model.node.AstThematicBreak
import com.shifenmiao.model.node.AstUnorderedList
import io.noties.markwon.MarkdownJLatexBlockMath
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener

/**
 * A composable that renders Markdown content pointed by [astNode] into this [RichTextScope].
 * Designed to be a building block that should be wrapped with a specific parser.
 *
 * @param astNode Root node of Markdown tree. This can be obtained via a parser.
 * @param astBlockNodeComposer An interceptor to take control of composing any block type node's
 * rendering. Use it to render images, html text, tables with your own components.
 */
@Composable
fun RichTextScope.BasicMarkdown(
    astNode: AstNode,
    astBlockNodeComposer: AstBlockNodeComposer? = null,
    codeBlockClickListener: CodeBlockClickListener? = null,
    imageConfig: MarkdownImageConfig = MarkdownImageConfig(),
    showCursor: Boolean = false,
    compactBlocks: Boolean = false,
) {
    // Only the last paragraph/heading in the rendered document should blink the cursor.
    val cursorTarget = remember(astNode, showCursor) {
        if (showCursor) astNode.findLastCursorTarget() else null
    }
    RecursiveRenderMarkdownAst(
        astNode,
        astBlockNodeComposer,
        codeBlockClickListener,
        imageConfig,
        showCursor,
        compactBlocks,
        cursorTarget,
    )
}

/**
 * An interface used to intercept block type AstNode rendering logic to inject custom composables
 * for nodes that satisfy [predicate].
 */
interface AstBlockNodeComposer {

    /**
     * Returns true if [Compose] function would handle this [astBlockNodeType].
     */
    fun predicate(
        astBlockNodeType: AstBlockNodeType,
        codeBlockClickListener: CodeBlockClickListener? = null
    ): Boolean

    /**
     * A composable that's responsible for composing the given [astNode] if its [AstNode.type]
     * returned true from [predicate]. This composable should also decide when and where to render
     * its children, then call [visitChildren] with a reference to which node's children to visit.
     * This is not an enforced behavior but unknowingly failing to do so can cause loss of
     * information during rendering.
     */
    @Composable
    fun RichTextScope.Compose(
        astNode: AstNode,
        codeBlockClickListener: CodeBlockClickListener? = null,
        visitChildren: @Composable (AstNode) -> Unit
    )
}

/**
 * When parsed, markdown content or any other rich text can be represented as a tree.
 * The default markdown parser that is used in this project `common-markdown` also
 * utilizes the said approach. Although there are ways to iteratively traverse a tree,
 * it is more readable and compose-friendly to do it recursively.
 *
 * This function basically receives a node from the tree, root or any node, and then
 * recursively travels along the nodes while spitting out or wrapping composables around
 * the content. RichText API is highly compatible with this method.
 *
 * However, there are multiple assumptions to increase predictability. Despite the fact
 * that every [AstNode] can have another [AstNode] as a child, it should not be that
 * generic in Markdown content. For example, a Text node must not have any other children.
 * That's why this function does not have 100% coverage for all [AstNode] types.
 *
 * Heading, Paragraph are considered to be main text containers. Their content is regarded
 * as one block and children traversal happens separately.
 *
 * FormattedList, OrderedList are also content blocks. Their children are filtered before
 * being traversed. Only ListItems are accepted as valid children for these blocks.
 *
 * For now, only tables are rendered from CustomBlock or CustomNode.
 *
 * @param astNode Root node to start rendering.
 */
@Composable
internal fun RichTextScope.RecursiveRenderMarkdownAst(
    astNode: AstNode?,
    astNodeComposer: AstBlockNodeComposer?,
    codeBlockClickListener: CodeBlockClickListener? = null,
    imageConfig: MarkdownImageConfig = MarkdownImageConfig(),
    showCursor: Boolean = false,
    compactBlocks: Boolean = false,
    cursorTarget: AstNode? = null,
) {
    astNode ?: return

    if (astNodeComposer != null &&
        astNode.type is AstBlockNodeType &&
        astNodeComposer.predicate(
            astBlockNodeType = astNode.type as AstBlockNodeType,
            codeBlockClickListener = codeBlockClickListener
        )
    ) {
        with(astNodeComposer) {
            Compose(
                astNode = astNode,
                codeBlockClickListener = codeBlockClickListener
            ) {
                RenderChildren(
                    it,
                    astNodeComposer = astNodeComposer,
                    codeBlockClickListener = codeBlockClickListener,
                    imageConfig = imageConfig,
                    showCursor = showCursor,
                    compactBlocks = compactBlocks,
                    cursorTarget = cursorTarget,
                )
            }
        }
    } else {
        with(DefaultAstNodeComposer) {
            Compose(
                astNode = astNode,
                codeBlockClickListener = codeBlockClickListener,
                imageConfig = imageConfig,
                showCursor = showCursor,
                compactBlocks = compactBlocks,
                cursorTarget = cursorTarget,
            ) {
                RenderChildren(
                    it,
                    astNodeComposer = astNodeComposer,
                    codeBlockClickListener = codeBlockClickListener,
                    imageConfig = imageConfig,
                    showCursor = showCursor,
                    compactBlocks = compactBlocks,
                    cursorTarget = cursorTarget,
                )
            }
        }
    }
}

private val DefaultAstNodeComposer = object : AstBlockNodeComposer {

    override fun predicate(
        astBlockNodeType: AstBlockNodeType,
        codeBlockClickListener: CodeBlockClickListener?
    ): Boolean = true

    @Composable
    fun RichTextScope.Compose(
        astNode: AstNode,
        codeBlockClickListener: CodeBlockClickListener?,
        imageConfig: MarkdownImageConfig,
        showCursor: Boolean = false,
        compactBlocks: Boolean = false,
        cursorTarget: AstNode? = null,
        visitChildren: @Composable (AstNode) -> Unit
    ) {
        // Only the actual last paragraph/heading target should render the inline cursor.
        val showCursorHere = showCursor && astNode === cursorTarget
        when (val astNodeType = astNode.type) {
            is AstDocument -> visitChildren(astNode)
            is AstBlockQuote -> {
                BlockQuote {
                    visitChildren(astNode)
                }
            }

            is AstUnorderedList -> {
                FormattedList(
                    listType = Unordered,
                    items = astNode.filterChildrenType<AstListItem>().toList()
                ) { astListItem ->
                    // if this list item has no child, it should at least emit a single pixel layout.
                    if (astListItem.links.firstChild == null) {
                        BasicText("")
                    } else {
                        visitChildren(astListItem)
                    }
                }
            }

            is AstOrderedList -> {
                FormattedList(
                    listType = Ordered,
                    items = astNode.filterChildrenType<AstListItem>().toList(),
                    startIndex = astNodeType.startNumber - 1,
                ) { astListItem ->
                    // if this list item has no child, it should at least emit a single pixel layout.
                    if (astListItem.links.firstChild == null) {
                        BasicText("")
                    } else {
                        visitChildren(astListItem)
                    }
                }
            }

            is AstThematicBreak -> {
                if (!compactBlocks) {
                    HorizontalRule()
                }
            }

            is AstHeading -> {
                if (compactBlocks) {
                    MarkdownRichText(astNode, imageConfig = imageConfig, showCursor = showCursorHere)
                } else {
                    Heading(level = astNodeType.level) {
                        MarkdownRichText(astNode, Modifier.semantics { heading() }, imageConfig, showCursorHere)
                    }
                }
            }

            is AstIndentedCodeBlock -> {
                CodeBlock(
                    text = astNodeType.literal.trim()
                )
            }

            is AstFencedCodeBlock -> {
                AsyncFencedCodeBlock(
                    astNodeType,
                    enableAsyncHighlight = codeBlockClickListener?.isHighlighted == true,
                    codeBlockClickListener = codeBlockClickListener
                )
            }

            is AstHtmlBlock -> {
                Text(text = richTextString {
                    appendInlineContent(content = InlineContent {
                        HtmlBlock(astNodeType.literal)
                    })
                })
            }

            is AstLinkReferenceDefinition -> {
                // Link reference definitions are metadata and should not be rendered.
                // no-op
            }

            is AstParagraph -> {
                MarkdownRichText(astNode, imageConfig = imageConfig, showCursor = showCursorHere)
            }

            is AstImage -> {
                // Handle block-level images (e.g. when image is the primary node in a paragraph).
                // Image fills the full width of the parent container.
                MarkdownImage(
                    url = astNodeType.destination,
                    title = astNodeType.title,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = imageConfig.contentScale,
                    onClick = imageConfig.onImageClick?.let { cb ->
                        { cb(astNodeType.destination, astNodeType.title) }
                    },
                    onLongClick = imageConfig.onImageLongClick?.let { cb ->
                        { cb(astNodeType.destination, astNodeType.title) }
                    },
                    loading = imageConfig.loading?.let { loadingContent ->
                        { loadingContent() }
                    },
                    error = imageConfig.error?.let { errorContent ->
                        { errorContent() }
                    }
                )
            }

            is AstTableRoot -> {
                RenderTable(astNode)
            }
            // This should almost never happen. All the possible text
            // nodes must be under either Heading, Paragraph or CustomNode
            // In any case, we should include it here to prevent any
            // non-rendered text problems.
            is AstText -> {
                Text(richTextString { append(astNodeType.literal) })
            }

            is AstListItem -> {
                // Should not happen at this level; list items are rendered under Ast(Un)OrderedList.
                // no-op
            }

            is AstJLatexBlockMath -> {
                astNodeType.latex?.let { latex ->
                    MarkdownJLatexBlockMath(
                        latex = latex
                    )
                }
            }

            is AstInlineNodeType -> {
                // Inline nodes are rendered by MarkdownRichText under Heading/Paragraph.
                // no-op
            }

            AstTableBody,
            AstTableHeader,
            AstTableRow,
            is AstTableCell -> {
                // Table internal nodes are rendered by RenderTable.
                // no-op
            }

            // Exhaustive when: no else branch needed.
        }
    }

    @Composable
    override fun RichTextScope.Compose(
        astNode: AstNode,
        codeBlockClickListener: CodeBlockClickListener?,
        visitChildren: @Composable (AstNode) -> Unit
    ) {
        // Delegate to internal overload with a default config.
        Compose(astNode, codeBlockClickListener, MarkdownImageConfig(), false, false, null, visitChildren)
    }
}

/**
 * Visit and render children from first to last.
 *
 * @param node Root ASTNode whose children will be visited.
 */
@Composable
internal fun RichTextScope.RenderChildren(
    node: AstNode?,
    astNodeComposer: AstBlockNodeComposer?,
    codeBlockClickListener: CodeBlockClickListener? = null,
    imageConfig: MarkdownImageConfig = MarkdownImageConfig(),
    showCursor: Boolean = false,
    compactBlocks: Boolean = false,
    cursorTarget: AstNode? = null,
) {
    node?.childrenSequence()?.forEach {
        RecursiveRenderMarkdownAst(
            astNode = it,
            astNodeComposer = astNodeComposer,
            codeBlockClickListener = codeBlockClickListener,
            imageConfig = imageConfig,
            showCursor = showCursor,
            compactBlocks = compactBlocks,
            cursorTarget = cursorTarget,
        )
    }
}

/**
 * Returns the last [AstParagraph] or [AstHeading] in document order under this node.
 * If the node itself is a paragraph or heading, it is returned directly.
 * This is used to place the streaming cursor at the end of the last text block only.
 *
 * Walks from the last child backwards so that it can return as soon as the last
 * text block is found, without traversing the whole tree.
 */
private fun AstNode.findLastCursorTarget(): AstNode? {
    if (type is AstParagraph || type is AstHeading) return this

    var child = links.lastChild
    while (child != null) {
        child.findLastCursorTarget()?.let { return it }
        child = child.links.previous
    }
    return null
}
