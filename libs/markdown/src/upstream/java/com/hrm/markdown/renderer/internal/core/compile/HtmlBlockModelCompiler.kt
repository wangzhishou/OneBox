package com.hrm.markdown.renderer.internal.core.compile

import com.hrm.markdown.parser.ast.HtmlBlock
import com.hrm.markdown.parser.ast.InlineHtml
import com.hrm.markdown.parser.ast.Node
import com.hrm.markdown.parser.ast.Text
import com.hrm.markdown.parser.core.HtmlEntities
import com.hrm.markdown.parser.html.HtmlFragmentToken
import com.hrm.markdown.parser.html.HtmlFragmentTokenizer
import com.hrm.markdown.parser.html.HtmlTagCategories
import com.hrm.markdown.parser.html.HtmlTagKind
import com.hrm.markdown.parser.html.HtmlTagToken
import com.hrm.markdown.renderer.internal.core.identity.RenderIdentity
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromText
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromValues
import com.hrm.markdown.renderer.internal.core.model.BlockQuoteBlockModel
import com.hrm.markdown.renderer.internal.core.model.BlockTextAlignment
import com.hrm.markdown.renderer.internal.core.model.CodeBlockModel
import com.hrm.markdown.renderer.internal.core.model.CodeBlockWidgetModel
import com.hrm.markdown.renderer.internal.core.model.HeadingBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlContainerBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlParagraphBlockModel
import com.hrm.markdown.renderer.internal.core.model.InlineModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.core.model.ListBlockModel
import com.hrm.markdown.renderer.internal.core.model.ListItemBlockModel
import com.hrm.markdown.renderer.internal.core.model.ThematicBreakBlockModel

/** 将受支持的 HtmlBlock fragment 编译为平台无关 RenderModel。 */
internal object HtmlBlockModelCompiler {
    private sealed interface FragmentNode {
        val sourceRevision: Long
    }

    private data class FragmentText(
        val source: String,
    ) : FragmentNode {
        override val sourceRevision: Long = renderIdentityFromText(source)
    }

    private data class FragmentStandaloneTag(
        val source: String,
        val tag: HtmlTagToken,
    ) : FragmentNode {
        override val sourceRevision: Long = renderIdentityFromText(source)
    }

    private data class FragmentElement(
        val openingSource: String,
        val closingSource: String,
        val tag: HtmlTagToken,
        val children: List<FragmentNode>,
    ) : FragmentNode {
        override val sourceRevision: Long = children.fold(
            renderIdentityFromText(openingSource)
        ) { revision, child ->
            renderIdentityFromValues(revision, child.sourceRevision)
        }
            .let { renderIdentityFromValues(it, renderIdentityFromText(closingSource)) }
    }

    private data class OpenElement(
        val openingSource: String,
        val tag: HtmlTagToken,
        val children: MutableList<FragmentNode> = mutableListOf(),
    )

    private data class CodeBlockExtraction(
        val code: String,
        val language: String,
    )

    private sealed interface InlineParagraphCompileResult {
        data class Content(val model: HtmlParagraphBlockModel) : InlineParagraphCompileResult

        data object Empty : InlineParagraphCompileResult

        data object Unsupported : InlineParagraphCompileResult
    }

    fun compile(node: HtmlBlock, identity: RenderIdentity): InternalRenderBlockModel? {
        when (node.htmlType) {
            RAW_HTML_BLOCK_TYPE,
            HTML_COMMENT_TYPE,
            COMMONMARK_BLOCK_TAG_TYPE,
            COMPLETE_TAG_TYPE -> Unit
            else -> return null
        }
        val tokens = HtmlFragmentTokenizer.tokenize(node.literal) ?: return null
        val tree = buildTree(tokens) ?: return null
        val children = compileFlow(
            nodes = tree,
            inheritedAlignment = BlockTextAlignment.INHERIT,
            parentIdentity = identity,
        ) ?: return null
        return HtmlContainerBlockModel(identity = identity, children = children)
    }

    private fun buildTree(tokens: List<HtmlFragmentToken>): List<FragmentNode>? {
        val root = mutableListOf<FragmentNode>()
        val frames = ArrayDeque<OpenElement>()

        fun append(node: FragmentNode) {
            if (frames.isEmpty()) root += node else frames.last().children += node
        }

        for (token in tokens) {
            when (token) {
                is HtmlFragmentToken.Text -> append(FragmentText(token.literal))
                is HtmlFragmentToken.Tag -> when (token.tag.kind) {
                    HtmlTagKind.OPENING -> {
                        val name = token.tag.name.orEmpty()
                        if (HtmlTagCategories.isVoidTag(name)) {
                            append(FragmentStandaloneTag(token.literal, token.tag))
                        } else {
                            frames += OpenElement(token.literal, token.tag)
                        }
                    }
                    HtmlTagKind.SELF_CLOSING,
                    HtmlTagKind.COMMENT,
                    HtmlTagKind.PROCESSING_INSTRUCTION,
                    HtmlTagKind.DECLARATION,
                    HtmlTagKind.CDATA -> append(FragmentStandaloneTag(token.literal, token.tag))
                    HtmlTagKind.CLOSING -> {
                        val frame = frames.lastOrNull() ?: return null
                        if (frame.tag.name != token.tag.name) return null
                        frames.removeLast()
                        append(
                            FragmentElement(
                                openingSource = frame.openingSource,
                                closingSource = token.literal,
                                tag = frame.tag,
                                children = frame.children,
                            )
                        )
                    }
                }
            }
        }
        if (frames.isNotEmpty()) return null
        return root
    }

    private fun compileFlow(
        nodes: List<FragmentNode>,
        inheritedAlignment: BlockTextAlignment,
        parentIdentity: RenderIdentity,
    ): List<InternalRenderBlockModel>? {
        val result = mutableListOf<InternalRenderBlockModel>()
        val inlineBuffer = mutableListOf<FragmentNode>()
        var childIndex = 0

        fun flushInline(): Boolean {
            if (inlineBuffer.isEmpty()) return true
            val identity = derivedIdentity(
                parent = parentIdentity,
                role = "html-inline",
                index = childIndex++,
                contentRevision = inlineBuffer.sourceRevision(),
            )
            val paragraph = compileInlineParagraph(inlineBuffer, inheritedAlignment, identity)
            inlineBuffer.clear()
            return when (paragraph) {
                is InlineParagraphCompileResult.Content -> {
                    result += paragraph.model
                    true
                }
                InlineParagraphCompileResult.Empty -> true
                InlineParagraphCompileResult.Unsupported -> false
            }
        }

        for (node in nodes) {
            when (node) {
                is FragmentText -> inlineBuffer += node
                is FragmentStandaloneTag -> {
                    val blockAction = SafeHtmlPolicy.blockAction(node.tag, inheritedAlignment)
                    if (blockAction?.role == SafeBlockHtmlRole.THEMATIC_BREAK) {
                        if (!flushInline()) return null
                        result += ThematicBreakBlockModel(
                            derivedIdentity(
                                parent = parentIdentity,
                                role = "html-hr",
                                index = childIndex++,
                                contentRevision = node.sourceRevision,
                            )
                        )
                    } else {
                        inlineBuffer += node
                    }
                }
                is FragmentElement -> {
                    if (node.isListContainerElement()) {
                        if (!flushInline()) return null
                        val identity = derivedIdentity(
                            parent = parentIdentity,
                            role = "html-${node.tag.name.orEmpty()}",
                            index = childIndex++,
                            contentRevision = node.sourceRevision,
                        )
                        result += compileListElement(
                            element = node,
                            identity = identity,
                            inheritedAlignment = inheritedAlignment,
                        ) ?: return null
                    } else {
                        val blockAction = SafeHtmlPolicy.blockAction(node.tag, inheritedAlignment)
                        if (blockAction == null) {
                            inlineBuffer += node
                        } else {
                            if (!flushInline()) return null
                            val identity = derivedIdentity(
                                parent = parentIdentity,
                                role = "html-${node.tag.name.orEmpty()}",
                                index = childIndex++,
                                contentRevision = node.sourceRevision,
                            )
                            val compiled = when (blockAction.role) {
                                SafeBlockHtmlRole.HEADING -> compileHeadingElement(
                                    element = node,
                                    action = blockAction,
                                    identity = identity,
                                )
                                SafeBlockHtmlRole.BLOCK_QUOTE -> compileBlockQuoteElement(
                                    element = node,
                                    identity = identity,
                                    inheritedAlignment = blockAction.alignment,
                                )
                                SafeBlockHtmlRole.CODE_BLOCK -> compileCodeBlockElement(
                                    element = node,
                                    identity = identity,
                                )
                                SafeBlockHtmlRole.CONTAINER,
                                SafeBlockHtmlRole.PARAGRAPH,
                                SafeBlockHtmlRole.THEMATIC_BREAK -> compileContainerElement(
                                    element = node,
                                    action = blockAction,
                                    identity = identity,
                                )
                            } ?: return null
                            result += compiled
                        }
                    }
                }
            }
        }
        if (!flushInline()) return null
        return result.renderSnapshotList()
    }

    private fun compileListElement(
        element: FragmentElement,
        identity: RenderIdentity,
        inheritedAlignment: BlockTextAlignment,
    ): ListBlockModel? {
        val name = element.tag.name ?: return null
        val ordered = when (name) {
            "ul" -> false
            "ol" -> true
            else -> return null
        }
        val startNumber = listStartNumber(element.tag, ordered) ?: return null
        val items = mutableListOf<ListItemBlockModel>()

        for (child in element.children) {
            when (child) {
                is FragmentText -> if (!child.source.isBlank()) return null
                is FragmentStandaloneTag -> if (!child.isHiddenComment()) return null
                is FragmentElement -> {
                    if (child.tag.name != "li") return null
                    val itemIdentity = derivedIdentity(
                        parent = identity,
                        role = "html-li",
                        index = items.size,
                        contentRevision = child.sourceRevision,
                    )
                    items += compileListItemElement(
                        element = child,
                        identity = itemIdentity,
                        inheritedAlignment = inheritedAlignment,
                    ) ?: return null
                }
            }
        }

        return ListBlockModel(
            identity = identity,
            ordered = ordered,
            startNumber = startNumber,
            bulletChar = if (ordered) '1' else '-',
            delimiter = '.',
            tight = true,
            items = items.renderSnapshotList(),
        )
    }

    private fun compileListItemElement(
        element: FragmentElement,
        identity: RenderIdentity,
        inheritedAlignment: BlockTextAlignment,
    ): ListItemBlockModel? {
        if (element.tag.name != "li" || element.tag.attributes.isNotEmpty()) return null
        val children = compileFlow(
            nodes = element.children,
            inheritedAlignment = inheritedAlignment,
            parentIdentity = identity,
        ) ?: return null
        return ListItemBlockModel(
            identity = identity,
            taskListItem = false,
            checked = false,
            children = children,
        )
    }

    private fun listStartNumber(tag: HtmlTagToken, ordered: Boolean): Int? {
        if (!ordered) {
            return if (tag.attributes.isEmpty()) 1 else null
        }
        if (tag.attributes.keys.any { it != "start" }) return null
        val start = tag.attributes["start"] ?: return 1
        return start.trim().toIntOrNull()
    }

    private fun compileHeadingElement(
        element: FragmentElement,
        action: SafeBlockHtmlAction,
        identity: RenderIdentity,
    ): HeadingBlockModel? {
        val level = action.headingLevel?.takeIf { it in 1..6 } ?: return null
        return when (val paragraph = compileInlineParagraph(element.children, action.alignment, identity)) {
            is InlineParagraphCompileResult.Content -> HeadingBlockModel(
                identity = identity,
                level = level,
                numbering = null,
                inline = paragraph.model.inline,
            )
            InlineParagraphCompileResult.Empty,
            InlineParagraphCompileResult.Unsupported -> null
        }
    }

    private fun compileBlockQuoteElement(
        element: FragmentElement,
        identity: RenderIdentity,
        inheritedAlignment: BlockTextAlignment,
    ): BlockQuoteBlockModel? {
        val children = compileFlow(
            nodes = element.children,
            inheritedAlignment = inheritedAlignment,
            parentIdentity = identity,
        ) ?: return null
        return BlockQuoteBlockModel(identity = identity, children = children)
    }

    private fun compileCodeBlockElement(
        element: FragmentElement,
        identity: RenderIdentity,
    ): CodeBlockModel? {
        val extracted = extractCodeBlock(element) ?: return null
        return CodeBlockModel(
            identity = identity,
            language = extracted.language,
            title = null,
            code = extracted.code,
            showLineNumbers = true,
            startLine = 1,
            highlightedLines = emptySet(),
            widget = CodeBlockWidgetModel(
                identity = identity,
                code = extracted.code,
                language = extracted.language,
                title = null,
            ),
        )
    }

    private fun compileContainerElement(
        element: FragmentElement,
        action: SafeBlockHtmlAction,
        identity: RenderIdentity,
    ): InternalRenderBlockModel? {
        return when (action.role) {
            SafeBlockHtmlRole.PARAGRAPH -> when (
                val paragraph = compileInlineParagraph(element.children, action.alignment, identity)
            ) {
                is InlineParagraphCompileResult.Content -> paragraph.model
                InlineParagraphCompileResult.Empty -> HtmlContainerBlockModel(identity, emptyList())
                InlineParagraphCompileResult.Unsupported -> null
            }
            SafeBlockHtmlRole.CONTAINER -> {
                val children = compileFlow(element.children, action.alignment, identity) ?: return null
                HtmlContainerBlockModel(identity = identity, children = children)
            }
            SafeBlockHtmlRole.HEADING,
            SafeBlockHtmlRole.BLOCK_QUOTE,
            SafeBlockHtmlRole.CODE_BLOCK,
            SafeBlockHtmlRole.THEMATIC_BREAK -> null
        }
    }

    private fun compileInlineParagraph(
        nodes: List<FragmentNode>,
        alignment: BlockTextAlignment,
        identity: RenderIdentity,
    ): InlineParagraphCompileResult {
        if (nodes.any { !it.isSupportedInlineFragment() }) {
            return InlineParagraphCompileResult.Unsupported
        }
        val astNodes = InlineAstBuilder().build(nodes)
        val inline = compileInlineModel(
            nodes = astNodes,
            inlineRevision = nodes.sourceRevision(),
            parentStableId = identity.stableId,
        )
        if (inline.atoms.isEmpty()) return InlineParagraphCompileResult.Empty
        return InlineParagraphCompileResult.Content(
            HtmlParagraphBlockModel(
                identity = identity,
                inline = inline,
                textAlignment = alignment,
            )
        )
    }

    private class InlineAstBuilder {
        private val nodes = mutableListOf<Node>()
        private val text = StringBuilder()
        private var pendingWhitespace = false
        private var hasVisibleContentOnLine = false

        fun build(source: List<FragmentNode>): List<Node> {
            source.forEach(::append)
            flushText()
            return nodes
        }

        private fun append(node: FragmentNode) {
            when (node) {
                is FragmentText -> appendText(HtmlEntities.replaceAll(node.source))
                is FragmentStandaloneTag -> appendTag(node.source, node.tag)
                is FragmentElement -> {
                    appendTag(node.openingSource, node.tag)
                    node.children.forEach(::append)
                    appendTag(
                        node.closingSource,
                        HtmlTagToken(kind = HtmlTagKind.CLOSING, name = node.tag.name),
                    )
                }
            }
        }

        private fun appendText(value: String) {
            for (char in value) {
                if (char.isWhitespace()) {
                    pendingWhitespace = true
                } else {
                    if (pendingWhitespace && hasVisibleContentOnLine) text.append(' ')
                    text.append(char)
                    pendingWhitespace = false
                    hasVisibleContentOnLine = true
                }
            }
        }

        private fun appendTag(literal: String, tag: HtmlTagToken) {
            flushText()
            nodes += InlineHtml(literal)
            when (tag.name) {
                "br" -> {
                    pendingWhitespace = false
                    hasVisibleContentOnLine = false
                }
                "img" -> {
                    pendingWhitespace = false
                    hasVisibleContentOnLine = true
                }
            }
        }

        private fun flushText() {
            if (text.isEmpty()) return
            nodes += Text(text.toString())
            text.clear()
        }
    }

    private fun FragmentNode.isSupportedInlineFragment(): Boolean = when (this) {
        is FragmentText -> true
        is FragmentStandaloneTag -> SafeHtmlPolicy.inlineAction(tag) != null
        is FragmentElement -> {
            SafeHtmlPolicy.inlineAction(tag) is SafeInlineHtmlAction.Span &&
                children.all { it.isSupportedInlineFragment() }
        }
    }

    private fun FragmentElement.isListContainerElement(): Boolean = tag.name == "ul" || tag.name == "ol"

    private fun FragmentStandaloneTag.isHiddenComment(): Boolean =
        SafeHtmlPolicy.inlineAction(tag) == SafeInlineHtmlAction.Hidden

    private fun extractCodeBlock(element: FragmentElement): CodeBlockExtraction? {
        if (element.tag.name != "pre") return null
        if (element.tag.attributes.isNotEmpty()) return null

        val visibleChildren = element.children.filterNot { child ->
            child is FragmentStandaloneTag && child.isHiddenComment()
        }
        val codeElement = visibleChildren.filterIsInstance<FragmentElement>()
            .singleOrNull { it.tag.name == "code" }
        if (codeElement != null) {
            if (visibleChildren.any { child ->
                    child !== codeElement && (child !is FragmentText || child.source.isNotBlank())
                }
            ) {
                return null
            }
            val language = codeLanguage(codeElement.tag) ?: return null
            val code = collectCodeText(codeElement.children) ?: return null
            return CodeBlockExtraction(code = code, language = language)
        }

        val code = collectCodeText(element.children) ?: return null
        return CodeBlockExtraction(code = code, language = "")
    }

    private fun collectCodeText(nodes: List<FragmentNode>): String? = buildString {
        for (node in nodes) {
            when (node) {
                is FragmentText -> append(HtmlEntities.replaceAll(node.source))
                is FragmentStandaloneTag -> if (!node.isHiddenComment()) return null
                is FragmentElement -> return null
            }
        }
    }

    private fun codeLanguage(tag: HtmlTagToken): String? {
        if (tag.name != "code") return null
        if (tag.attributes.isEmpty()) return ""
        if (tag.attributes.keys.any { it != "class" }) return null
        val classes = tag.attributes["class"].orEmpty()
            .splitToSequence(' ', '\t', '\n', '\r')
            .filter { it.isNotBlank() }
            .toList()
        if (classes.size != 1) return null
        val className = classes.single()
        val language = when {
            className.startsWith("language-") -> className.removePrefix("language-")
            className.startsWith("lang-") -> className.removePrefix("lang-")
            else -> return null
        }
        return language.takeIf { it.isNotBlank() && it.none(Char::isWhitespace) }
    }

    private fun List<FragmentNode>.sourceRevision(): Long = fold(0L) { revision, node ->
        renderIdentityFromValues(revision, node.sourceRevision)
    }

    private fun derivedIdentity(
        parent: RenderIdentity,
        role: String,
        index: Int,
        contentRevision: Long,
    ): RenderIdentity {
        val stableId = renderIdentityFromValues(
            parent.stableId,
            renderIdentityFromText(role),
            index.toLong(),
        )
        val revision = renderIdentityFromValues(parent.contentRevision, contentRevision)
        return RenderIdentity(
            stableId = stableId,
            contentRevision = revision,
            layoutRevision = revision,
            paintRevision = 0L,
        )
    }

    private const val RAW_HTML_BLOCK_TYPE = 1
    private const val HTML_COMMENT_TYPE = 2
    private const val COMMONMARK_BLOCK_TAG_TYPE = 6
    private const val COMPLETE_TAG_TYPE = 7
}
