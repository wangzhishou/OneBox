package com.hrm.markdown.renderer.internal.core.compile

import com.hrm.markdown.parser.ast.*
import com.hrm.markdown.renderer.internal.core.identity.RenderIdentity
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromText
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromValues
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityMix

/** Identities are source independent: published root identity plus structural child paths. */
internal class RenderIdentityProvider(
    private val document: Document,
    rootNodes: List<Node> = document.children,
    firstRootIndex: Int = 0,
) {
    internal var visitedNodes: Int = 1
        private set

    private val stableIds = mutableMapOf<Node, Long>()
    private val semanticRevisions = mutableMapOf<Node, Long>()

    init {
        val documentId = documentStableId()
        stableIds[document] = documentId
        rootNodes.forEachIndexed { index, child ->
            val typeId = renderIdentityFromText(child::class.simpleName ?: "node")
            val rootId = if (child.persistentId != 0L) {
                renderIdentityFromValues(documentId, typeId, 0L, child.persistentId)
            } else {
                renderIdentityFromValues(documentId, typeId, 1L, (firstRootIndex + index).toLong())
            }
            assignStableIds(child, rootId)
        }
    }

    fun identity(node: Node): RenderIdentity {
        if (node === document && document.publication != null) return publishedDocumentIdentity(document)
        val stableId = stableId(node)
        val revision = renderIdentityFromValues(stableId, semanticRevision(node))
        return RenderIdentity(stableId, revision, revision, 0L)
    }

    fun stableId(node: Node): Long = stableIds[node]
        ?: error("Node is outside the compilation scope: ${node::class.simpleName}")

    fun semanticRevision(node: Node): Long = semanticRevisions.getOrPut(node) {
        computeSemanticRevision(node) { child -> semanticRevision(child) }
    }

    private fun assignStableIds(node: Node, stableId: Long) {
        visitedNodes++
        stableIds[node] = stableId
        if (node is ContainerNode) {
            node.children.forEachIndexed { index, child ->
                assignStableIds(
                    child,
                    renderIdentityFromValues(
                        stableId,
                        renderIdentityFromText(child::class.simpleName ?: "node"),
                        index.toLong(),
                    ),
                )
            }
        }
    }
}

private fun documentStableId(): Long = renderIdentityFromValues(
    renderIdentityFromText("render-document-root"),
    renderIdentityFromText("Document"),
)

internal fun publishedDocumentIdentity(document: Document): RenderIdentity {
    val publication = requireNotNull(document.publication) { "A render session requires a published document" }
    require(publication.isCurrent) { "An expired document view cannot provide a current render identity" }
    val stableId = renderIdentityFromValues(documentStableId(), publication.lineageId.identityKey)
    val revision = renderIdentityFromValues(stableId, publication.revision, document.contentHash)
    return RenderIdentity(stableId, revision, revision, 0L)
}

/** Canonical fallback revision for custom/synthetic AST nodes whose contentHash is zero. */
internal fun computeSemanticRevision(node: Node): Long =
    computeSemanticRevision(node) { child -> computeSemanticRevision(child) }

private fun computeSemanticRevision(
    node: Node,
    childRevision: (Node) -> Long,
): Long {
    var acc = renderIdentityFromText(node::class.simpleName ?: "node")
    acc = renderIdentityMix(acc, node.contentHash)
    if (node is LeafNode) acc = acc.mixText(node.literal)

    acc = when (node) {
        is Document -> acc
            .mixNodeMap(node.linkDefinitions) { definition ->
                listOf(definition.label, definition.destination, definition.title.orEmpty())
            }
            .mixNodeMap(node.footnoteDefinitions) { definition ->
                listOf(definition.label, definition.index.toString())
            }
            .mixNodeMap(node.abbreviationDefinitions) { definition ->
                listOf(definition.abbreviation, definition.fullText)
            }
        is Heading -> acc.mixInt(node.level).mixText(node.customId).mixText(node.autoId)
            .mixMap(node.blockAttributes)
        is SetextHeading -> acc.mixInt(node.level).mixText(node.autoId).mixMap(node.blockAttributes)
        is Paragraph -> acc.mixMap(node.blockAttributes)
        is ThematicBreak -> acc.mixInt(node.char.code)
        is FencedCodeBlock -> acc
            .mixText(node.info)
            .mixText(node.language)
            .mixInt(node.fenceChar.code)
            .mixInt(node.fenceLength)
            .mixInt(node.fenceIndent)
            .mixMap(node.attributes.toMap())
            .mixRanges(node.highlightLines)
            .mixBoolean(node.showLineNumbers)
            .mixInt(node.startLineNumber)
        is BlockQuote -> acc.mixMap(node.blockAttributes)
        is ListBlock -> acc
            .mixBoolean(node.ordered)
            .mixInt(node.startNumber)
            .mixInt(node.bulletChar.code)
            .mixInt(node.delimiter.code)
            .mixBoolean(node.tight)
            .mixMap(node.blockAttributes)
        is ListItem -> acc
            .mixInt(node.markerIndent)
            .mixInt(node.contentIndent)
            .mixBoolean(node.taskListItem)
            .mixBoolean(node.checked)
            .mixBoolean(node.containsBlankLine)
        is HtmlBlock -> acc.mixInt(node.htmlType)
        is LinkReferenceDefinition -> acc.mixText(node.label).mixText(node.destination).mixText(node.title)
        is Table -> acc
            .mixStrings(node.columnAlignments.map { it.name })
            .mixMap(node.blockAttributes)
        is TableCell -> acc.mixText(node.alignment.name).mixBoolean(node.isHeader).mixText(node.rawContent)
        is FootnoteDefinition -> acc.mixText(node.label).mixInt(node.index)
        is Admonition -> acc.mixText(node.type).mixText(node.title)
        is FrontMatter -> acc.mixText(node.format)
        is TocPlaceholder -> acc
            .mixInt(node.minDepth)
            .mixInt(node.maxDepth)
            .mixStrings(node.excludeIds)
            .mixText(node.order)
        is AbbreviationDefinition -> acc.mixText(node.abbreviation).mixText(node.fullText)
        is CustomContainer -> acc
            .mixText(node.type)
            .mixText(node.title)
            .mixStrings(node.cssClasses)
            .mixText(node.cssId)
        is DiagramBlock -> acc.mixText(node.diagramType)
        is ColumnItem -> acc.mixText(node.width)
        is DirectiveBlock -> acc.mixText(node.tagName).mixMap(node.args)
        is TabItem -> acc.mixText(node.title)
        is BibliographyDefinition -> acc.mixEntries(node.entries)
        is Figure -> acc
            .mixText(node.imageUrl)
            .mixText(node.caption)
            .mixNullableInt(node.imageWidth)
            .mixNullableInt(node.imageHeight)
            .mixMap(node.attributes)
        is Emphasis -> acc.mixInt(node.delimiter.code)
        is StrongEmphasis -> acc.mixInt(node.delimiter.code)
        is Link -> acc.mixText(node.destination).mixText(node.title).mixMap(node.attributes)
        is Image -> acc
            .mixText(node.destination)
            .mixText(node.title)
            .mixNullableInt(node.imageWidth)
            .mixNullableInt(node.imageHeight)
            .mixMap(node.attributes)
        is Autolink -> acc.mixText(node.destination).mixBoolean(node.isEmail).mixText(node.rawText)
        is HtmlEntity -> acc.mixText(node.resolved)
        is FootnoteReference -> acc.mixText(node.label).mixInt(node.index)
        is Emoji -> acc.mixText(node.shortcode).mixText(node.unicode)
        is StyledText -> acc.mixMap(node.attributes)
        is Abbreviation -> acc.mixText(node.abbreviation).mixText(node.fullText)
        is DirectiveInline -> acc.mixText(node.tagName).mixOrderedMap(node.args)
        is CitationReference -> acc.mixText(node.key)
        is WikiLink -> acc.mixText(node.target).mixText(node.label)
        is RubyText -> acc.mixText(node.base).mixText(node.annotation)
        else -> acc
    }

    if (node is ContainerNode) {
        acc = renderIdentityMix(acc, node.childCount().toLong())
        for (child in node.children) {
            acc = renderIdentityMix(acc, childRevision(child))
        }
    }
    return acc
}

private fun Long.mixBoolean(value: Boolean): Long = renderIdentityMix(this, if (value) 1L else 0L)

private fun Long.mixInt(value: Int): Long = renderIdentityMix(this, value.toLong())

private fun Long.mixNullableInt(value: Int?): Long = if (value == null) {
    renderIdentityMix(this, 0L)
} else {
    renderIdentityMix(renderIdentityMix(this, 1L), value.toLong())
}

private fun Long.mixText(value: String?): Long = if (value == null) {
    renderIdentityMix(this, 0L)
} else {
    renderIdentityFromText(value, renderIdentityMix(this, 1L))
}

private fun Long.mixStrings(values: List<String>): Long {
    var next = renderIdentityMix(this, values.size.toLong())
    for (value in values) next = next.mixText(value)
    return next
}

private fun Long.mixMap(values: Map<String, String>): Long {
    var next = renderIdentityMix(this, values.size.toLong())
    for (key in values.keys.sorted()) {
        next = next.mixText(key).mixText(values.getValue(key))
    }
    return next
}

private fun Long.mixRanges(values: List<IntRange>): Long {
    var next = renderIdentityMix(this, values.size.toLong())
    for (range in values) next = next.mixInt(range.first).mixInt(range.last)
    return next
}

private fun Long.mixEntries(values: Map<String, BibEntry>): Long {
    var next = renderIdentityMix(this, values.size.toLong())
    for ((key, entry) in values) {
        next = next.mixText(key).mixText(entry.key).mixText(entry.content)
    }
    return next
}

private fun Long.mixOrderedMap(values: Map<String, String>): Long {
    var next = renderIdentityMix(this, values.size.toLong())
    for ((key, value) in values) next = next.mixText(key).mixText(value)
    return next
}

private fun <T : Node> Long.mixNodeMap(
    values: Map<String, T>,
    fields: (T) -> List<String>,
): Long {
    var next = renderIdentityMix(this, values.size.toLong())
    for (key in values.keys.sorted()) {
        val value = values.getValue(key)
        next = next.mixText(key).mixStrings(fields(value))
    }
    return next
}
