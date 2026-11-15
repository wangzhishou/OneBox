package com.hrm.markdown.renderer.internal.core.compile

import com.hrm.markdown.parser.ast.Abbreviation
import com.hrm.markdown.parser.ast.Autolink
import com.hrm.markdown.parser.ast.CitationReference
import com.hrm.markdown.parser.ast.ContainerNode
import com.hrm.markdown.parser.ast.DirectiveInline
import com.hrm.markdown.parser.ast.Emoji
import com.hrm.markdown.parser.ast.Emphasis
import com.hrm.markdown.parser.ast.EscapedChar
import com.hrm.markdown.parser.ast.FootnoteReference
import com.hrm.markdown.parser.ast.HardLineBreak
import com.hrm.markdown.parser.ast.Highlight
import com.hrm.markdown.parser.ast.HtmlEntity
import com.hrm.markdown.parser.ast.Image
import com.hrm.markdown.parser.ast.InlineCode
import com.hrm.markdown.parser.ast.InlineHtml
import com.hrm.markdown.parser.ast.InlineMath
import com.hrm.markdown.parser.ast.InsertedText
import com.hrm.markdown.parser.ast.KeyboardInput
import com.hrm.markdown.parser.ast.LeafNode
import com.hrm.markdown.parser.ast.Link
import com.hrm.markdown.parser.ast.Node
import com.hrm.markdown.parser.ast.RubyText
import com.hrm.markdown.parser.ast.SoftLineBreak
import com.hrm.markdown.parser.ast.Spoiler
import com.hrm.markdown.parser.ast.Strikethrough
import com.hrm.markdown.parser.ast.StrongEmphasis
import com.hrm.markdown.parser.ast.StyledText
import com.hrm.markdown.parser.ast.Subscript
import com.hrm.markdown.parser.ast.Superscript
import com.hrm.markdown.parser.ast.Text
import com.hrm.markdown.parser.ast.WikiLink
import com.hrm.markdown.renderer.MarkdownSoftBreakPolicy
import com.hrm.markdown.renderer.internal.core.identity.RenderIdentity
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromText
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromValues
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityMix
import com.hrm.markdown.renderer.internal.core.model.DirectiveInlineWidgetModel
import com.hrm.markdown.renderer.internal.core.model.ImageWidgetModel
import com.hrm.markdown.renderer.internal.core.model.InlineAtom
import com.hrm.markdown.renderer.internal.core.model.InlineCodeWidgetModel
import com.hrm.markdown.renderer.internal.core.model.InlineMathWidgetModel
import com.hrm.markdown.renderer.internal.core.model.InlineModel
import com.hrm.markdown.renderer.internal.core.model.RubyTextWidgetModel
import com.hrm.markdown.renderer.internal.core.model.SpanMark
import com.hrm.markdown.renderer.internal.core.model.SpoilerWidgetModel
import com.hrm.markdown.renderer.internal.core.model.TextAtom
import com.hrm.markdown.renderer.internal.core.model.WidgetAtom

internal fun compileInlineModel(
    nodes: List<Node>,
    inlineRevision: Long,
    parentStableId: Long,
    softBreakPolicy: MarkdownSoftBreakPolicy = MarkdownSoftBreakPolicy.Space,
): InlineModel = when (softBreakPolicy) {
    MarkdownSoftBreakPolicy.Space -> spaceInlineCompiler
    MarkdownSoftBreakPolicy.LineBreak -> lineBreakInlineCompiler
}.compileInlineModel(nodes, inlineRevision, parentStableId)

private val spaceInlineCompiler = InlineCompiler(MarkdownSoftBreakPolicy.Space)
private val lineBreakInlineCompiler = InlineCompiler(MarkdownSoftBreakPolicy.LineBreak)

private class InlineCompiler(private val softBreakPolicy: MarkdownSoftBreakPolicy) {
    fun compileInlineModel(
        nodes: List<Node>,
        inlineRevision: Long,
        parentStableId: Long,
    ): InlineModel {
        val inlineStableId = stableInlineListId(parentStableId)
        val revision = if (softBreakPolicy == MarkdownSoftBreakPolicy.Space) inlineRevision
            else renderIdentityFromValues(inlineRevision, softBreakPolicy.ordinal.toLong())
        return InlineModel(
            identity = RenderIdentity(
                stableId = inlineStableId,
                contentRevision = revision,
                layoutRevision = revision,
                paintRevision = 0L,
            ),
            atoms = buildList {
                compileInlineNodes(
                    nodes = nodes,
                    activeMarks = emptyList(),
                    sink = this,
                    parentStableId = inlineStableId,
                )
            }
        )
    }

    private fun compileInlineNodes(
        nodes: List<Node>,
        activeMarks: List<SpanMark>,
        sink: MutableList<InlineAtom>,
        parentStableId: Long,
    ) {
        compileNormalizedInlineSources(
            sources = InlineHtmlNormalizer.normalize(nodes),
            activeMarks = activeMarks,
            sink = sink,
            parentStableId = parentStableId,
        )
    }

    private fun compileNormalizedInlineSources(
        sources: List<NormalizedInlineSource>,
        activeMarks: List<SpanMark>,
        sink: MutableList<InlineAtom>,
        parentStableId: Long,
    ) {
        sources.forEachIndexed { index, source ->
            val stableId = stableInlineNodeId(source.anchor, parentStableId, index)
            val identity = nodeIdentity(source.anchor, stableId)
            when (source) {
                is AstInlineSource -> compileInlineNode(
                    node = source.anchor,
                    activeMarks = activeMarks,
                    sink = sink,
                    stableId = stableId,
                )
                is HtmlSpanInlineSource -> {
                    val firstAtom = sink.size
                    compileNormalizedInlineSources(
                        sources = source.children,
                        activeMarks = activeMarks + source.marks.map { it.copy(isRangeEnd = false) },
                        sink = sink,
                        parentStableId = stableId,
                    )
                    markInlineRangeEnd(sink, firstAtom, activeMarks.size, source.marks.size)
                }
                is HtmlLineBreakInlineSource -> sink += snapshotTextAtom(identity, "\n", activeMarks)
                is HtmlImageInlineSource -> sink += WidgetAtom(
                    identity = identity,
                    widget = ImageWidgetModel(
                        identity = identity,
                        url = source.source,
                        altText = source.altText,
                        title = source.title,
                        width = source.width,
                        height = source.height,
                        attributes = source.attributes.renderSnapshotMap(),
                    ),
                )
                is HiddenHtmlInlineSource -> Unit
            }
        }
    }

    private fun compileInlineNode(
        node: Node,
        activeMarks: List<SpanMark>,
        sink: MutableList<InlineAtom>,
        stableId: Long,
    ) {
        val identity = nodeIdentity(node, stableId)
        when (node) {
            is Text -> sink += snapshotTextAtom(identity, node.literal, activeMarks)
            is SoftLineBreak -> sink += snapshotTextAtom(identity, if (softBreakPolicy == MarkdownSoftBreakPolicy.LineBreak) "\n" else " ", activeMarks)
            is HardLineBreak -> sink += snapshotTextAtom(identity, "\n", activeMarks)
            is Emphasis -> compileInlineNodes(node.children, activeMarks + snapshotSpanMark("emphasis"), sink, stableId)
            is StrongEmphasis -> compileInlineNodes(node.children, activeMarks + snapshotSpanMark("strong"), sink, stableId)
            is Strikethrough -> compileInlineNodes(node.children, activeMarks + snapshotSpanMark("strikethrough"), sink, stableId)
            is Highlight -> {
                val firstAtom = sink.size
                compileInlineNodes(
                    node.children,
                    activeMarks + snapshotSpanMark("highlight").copy(attributes = node.attributes, isRangeEnd = false),
                    sink,
                    stableId,
                )
                markInlineRangeEnd(sink, firstAtom, activeMarks.size, 1)
            }
            is Superscript -> compileInlineNodes(
                node.children,
                activeMarks + snapshotSpanMark("superscript").copy(attributes = node.attributes),
                sink,
                stableId,
            )
            is Subscript -> compileInlineNodes(
                node.children,
                activeMarks + snapshotSpanMark("subscript").copy(attributes = node.attributes),
                sink,
                stableId,
            )
            is InsertedText -> compileInlineNodes(
                node.children,
                activeMarks + snapshotSpanMark("inserted").copy(attributes = node.attributes),
                sink,
                stableId,
            )
            is StyledText -> {
                compileInlineNodes(
                    node.children,
                    activeMarks + snapshotSpanMark(
                        kind = "styled",
                        payload = buildMap {
                            node.style?.let { put("style", it) }
                            if (node.cssClasses.isNotEmpty()) {
                                put("class", node.cssClasses.joinToString(" "))
                            }
                        }
                    ),
                    sink,
                    stableId,
                )
            }

            is Link -> {
                compileInlineNodes(
                    node.children,
                    activeMarks + snapshotSpanMark(
                        kind = "link",
                        payload = mapOf(
                            "target" to node.destination,
                            "tag" to "link",
                        )
                    ),
                    sink,
                    stableId,
                )
            }

            is Autolink -> {
                sink += snapshotTextAtom(
                    identity = identity,
                    text = node.destination,
                    marks = activeMarks + snapshotSpanMark(
                        kind = "link",
                        payload = mapOf(
                            "target" to node.destination,
                            "tag" to "link",
                        )
                    ),
                )
            }

            is WikiLink -> {
                sink += snapshotTextAtom(
                    identity = identity,
                    text = node.label ?: node.target,
                    marks = activeMarks + snapshotSpanMark(
                        kind = "link",
                        payload = mapOf(
                            "target" to node.target,
                            "tag" to "wikilink",
                        )
                    ),
                )
            }

            is InlineCode -> {
                sink += WidgetAtom(
                    identity = identity,
                    widget = InlineCodeWidgetModel(
                        identity = identity,
                        code = node.literal,
                    )
                )
            }

            is Image -> {
                sink += WidgetAtom(
                    identity = identity,
                    widget = ImageWidgetModel(
                        identity = identity,
                        url = node.destination,
                        altText = extractPlainText(node),
                        title = node.title,
                        width = node.imageWidth,
                        height = node.imageHeight,
                        attributes = node.attributes.renderSnapshotMap(),
                    )
                )
            }

            is InlineMath -> {
                sink += WidgetAtom(
                    identity = identity,
                    widget = InlineMathWidgetModel(
                        identity = identity,
                        latex = node.literal,
                    )
                )
            }

            is FootnoteReference -> {
                sink += snapshotTextAtom(
                    identity = identity,
                    text = "[${node.index}]",
                    marks = activeMarks + snapshotSpanMark(
                        kind = "footnote",
                        payload = mapOf(
                            "label" to node.label,
                            "index" to node.index.toString(),
                        )
                    ),
                )
            }

            is CitationReference -> {
                sink += snapshotTextAtom(
                    identity = identity,
                    text = "[${node.key}]",
                    marks = activeMarks + snapshotSpanMark(
                        kind = "citation",
                        payload = mapOf("key" to node.key),
                    ),
                )
            }

            is InlineHtml -> sink += snapshotTextAtom(identity, node.literal, activeMarks + snapshotSpanMark("inline_html"))
            is HtmlEntity -> sink += snapshotTextAtom(identity, node.resolved.ifEmpty { node.literal }, activeMarks)
            is EscapedChar -> sink += snapshotTextAtom(identity, node.literal, activeMarks)
            is Emoji -> sink += snapshotTextAtom(identity, node.unicode ?: node.literal.ifEmpty { ":${node.shortcode}:" }, activeMarks)
            is Abbreviation -> {
                sink += snapshotTextAtom(
                    identity = identity,
                    text = node.abbreviation,
                    marks = activeMarks + snapshotSpanMark(
                        kind = "abbreviation",
                        payload = mapOf("fullText" to node.fullText),
                    ),
                )
            }

            is KeyboardInput -> sink += snapshotTextAtom(identity, node.literal, activeMarks + snapshotSpanMark("kbd"))
            is Spoiler -> {
                sink += WidgetAtom(
                    identity = identity,
                    widget = SpoilerWidgetModel(
                        identity = identity,
                        content = compileInlineModel(
                            nodes = node.children,
                            inlineRevision = computeSemanticRevision(node),
                            parentStableId = stableId,
                        ),
                        alternateText = extractPlainText(node),
                    ),
                )
            }

            is DirectiveInline -> {
                sink += WidgetAtom(
                    identity = identity,
                    widget = DirectiveInlineWidgetModel(
                        identity = identity,
                        tagName = node.tagName,
                        args = node.args.renderSnapshotMap(),
                        alternateText = buildInlineDirectiveFallbackText(node),
                    ),
                )
            }

            is RubyText -> {
                sink += WidgetAtom(
                    identity = identity,
                    widget = RubyTextWidgetModel(
                        identity = identity,
                        base = node.base,
                        annotation = node.annotation,
                    ),
                )
            }

            else -> {
                if (node is ContainerNode) {
                    compileInlineNodes(node.children, activeMarks, sink, stableId)
                }
            }
        }
    }

    private fun markInlineRangeEnd(
        sink: MutableList<InlineAtom>,
        firstAtom: Int,
        firstMark: Int,
        markCount: Int,
    ) {
        if (markCount == 0) return
        for (index in sink.lastIndex downTo firstAtom) {
            val atom = sink[index] as? TextAtom ?: continue
            if (atom.text.none { it != '\n' }) continue
            sink[index] = atom.copy(marks = atom.marks.mapIndexed { markIndex, mark ->
                if (markIndex in firstMark until firstMark + markCount) mark.copy(isRangeEnd = true) else mark
            }.renderSnapshotList())
            return
        }
    }

    private fun nodeIdentity(node: Node, stableId: Long): RenderIdentity {
        val revision = if (node.contentHash != 0L) {
            node.contentHash
        } else {
            renderIdentityFromValues(stableId, inlineContentFingerprint(node))
        }
        return RenderIdentity(
            stableId = stableId,
            contentRevision = revision,
            layoutRevision = revision,
            paintRevision = 0L,
        )
    }

    private fun stableInlineListId(parentStableId: Long): Long = renderIdentityFromValues(
        renderIdentityFromText("inline-list"),
        parentStableId,
    )

    private fun stableInlineNodeId(node: Node, parentStableId: Long, siblingIndex: Int): Long {
        val typeId = renderIdentityFromText(node::class.simpleName ?: "inline")
        return renderIdentityFromValues(parentStableId, typeId, siblingIndex.toLong())
    }

    private fun inlineContentFingerprint(node: Node): Long {
        return computeSemanticRevision(node)
    }

    private fun buildInlineDirectiveFallbackText(node: DirectiveInline): String {
        val argsText = if (node.args.isNotEmpty()) {
            " " + node.args.entries.joinToString(" ") { (key, value) ->
                if (key.startsWith("_")) value else "$key=$value"
            }
        } else {
            ""
        }
        return "{% ${node.tagName}$argsText %}"
    }

    private fun extractPlainText(node: Node): String = buildString {
        when (node) {
            is Text -> append(node.literal)
            is InlineCode -> append(node.literal)
            is Image -> node.children.forEach { append(extractPlainText(it)) }
            is ContainerNode -> node.children.forEach { append(extractPlainText(it)) }
            else -> Unit
        }
    }

}

private fun snapshotTextAtom(identity: RenderIdentity, text: String, marks: List<SpanMark> = emptyList()): TextAtom =
    TextAtom(identity, text, if (marks.isEmpty()) emptyList() else marks.map { mark ->
        val payload = mark.payload.renderSnapshotMap()
        if (payload === mark.payload) mark else mark.copy(payload = payload)
    }.renderSnapshotList())

private fun snapshotSpanMark(kind: String, payload: Map<String, String> = emptyMap()): SpanMark =
    SpanMark(kind, payload.renderSnapshotMap())
