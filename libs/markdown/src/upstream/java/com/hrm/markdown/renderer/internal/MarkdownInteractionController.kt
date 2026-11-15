package com.hrm.markdown.renderer.internal

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import com.hrm.codehigh.ast.CodeToken
import com.hrm.markdown.renderer.*
import com.hrm.markdown.renderer.internal.core.model.*

/** Stable dispatch target. Cached geometry and content never capture host callbacks. */
@Stable
internal class MarkdownInteractionController {
    var citationEnabled by mutableStateOf(false)
        private set

    var imageEnabled by mutableStateOf(false)
        private set
    var codeTokenEnabled by mutableStateOf(false)
        private set
    var inlineElementEnabled by mutableStateOf(false)
        private set
    var decorationEnabled by mutableStateOf(false)
        private set
    private var onCodeTokenClick: ((CodeToken) -> Unit)? = null
    private var onInlineElementClick: ((MarkdownInlineElementClickEvent) -> Unit)? = null
    private var onCitationClick: ((MarkdownCitationClickEvent) -> Unit)? = null
    private var onImageClick: ((MarkdownImageClickEvent) -> Unit)? = null
    private var inlineDecoration: ((MarkdownInlineDecorationRequest) -> MarkdownInlineUnderline?)? = null
    private var document: InternalRenderDocumentModel? = null

    fun update(
        document: InternalRenderDocumentModel,
        handlers: MarkdownInteractions,
    ) {
        this.document = document
        this.onCodeTokenClick = handlers.onCodeTokenClick
        codeTokenEnabled = handlers.onCodeTokenClick != null
        this.onCitationClick = handlers.onCitationClick
        this.onImageClick = handlers.onImageClick
        this.onInlineElementClick = handlers.onInlineElementClick
        this.inlineDecoration = handlers.inlineDecoration
        citationEnabled = handlers.onCitationClick != null
        imageEnabled = handlers.onImageClick != null || handlers.onInlineElementClick != null
        inlineElementEnabled = handlers.onInlineElementClick != null
        decorationEnabled = handlers.inlineDecoration != null
    }

    fun codeToken(token: CodeToken) {
        onCodeTokenClick?.invoke(token)
    }

    fun citation(key: String, id: Long, bounds: Rect?) {
        onCitationClick?.invoke(MarkdownCitationClickEvent(
            key, id, document?.identity?.contentRevision ?: 0L, bounds,
        ))
    }

    fun inlineElement(
        kind: String,
        text: String,
        payload: Map<String, String>,
        attributes: Map<String, String>,
        id: Long,
        bounds: Rect?,
    ) {
        onInlineElementClick?.invoke(MarkdownInlineElementClickEvent(
            kind = kind,
            text = text,
            payload = payload,
            attributes = attributes,
            occurrenceId = id,
            documentRevision = document?.identity?.contentRevision ?: 0L,
            anchorBoundsInWindow = bounds,
        ))
    }

    fun decorate(request: MarkdownInlineDecorationRequest): MarkdownInlineUnderline? =
        inlineDecoration?.invoke(request)

    fun image(data: MarkdownImageData, id: Long, bounds: Rect?) {
        val snapshot = document
        val revision = snapshot?.identity?.contentRevision ?: 0L
        val dedicated = onImageClick
        if (dedicated != null) {
            dedicated(MarkdownImageClickEvent(
                image = data,
                occurrenceId = id,
                documentRevision = revision,
                anchorBoundsInWindow = bounds,
                gallery = {
                    val images = snapshot?.blocks?.imageOccurrences().orEmpty()
                    MarkdownImageGallery(images, images.indexOfFirst { it.occurrenceId == id })
                },
            ))
            return
        }
        // 无专属回调但统一监听存在时，图片点击以 kind="image" 进入统一分发。
        onInlineElementClick?.invoke(MarkdownInlineElementClickEvent(
            kind = InlineElementKinds.Image,
            text = data.altText,
            payload = buildMap {
                data.title?.let { put("title", it) }
                put("url", data.url)
            },
            attributes = data.attributes,
            occurrenceId = id,
            documentRevision = revision,
            anchorBoundsInWindow = bounds,
            image = MarkdownImageClickEvent(
                image = data,
                occurrenceId = id,
                documentRevision = revision,
                anchorBoundsInWindow = bounds,
                gallery = {
                    val images = snapshot?.blocks?.imageOccurrences().orEmpty()
                    MarkdownImageGallery(images, images.indexOfFirst { it.occurrenceId == id })
                },
            ),
        ))
    }
}

internal val LocalMarkdownInteractions = compositionLocalOf<MarkdownInteractionController?> { null }

internal fun List<InternalRenderBlockModel>.imageOccurrences(): List<MarkdownImageOccurrence> = buildList {
    fun inline(model: InlineModel) {
        for (atom in model.atoms) {
            val widget = (atom as? WidgetAtom)?.widget ?: continue
            when (widget) {
                is ImageWidgetModel -> add(MarkdownImageOccurrence(widget.identity.stableId,
                    MarkdownImageData(widget.url, widget.altText, widget.title, widget.width, widget.height, widget.attributes)))
                is SpoilerWidgetModel -> inline(widget.content)
                else -> Unit
            }
        }
    }
    fun visit(block: InternalRenderBlockModel) {
        when (block) {
            is ParagraphBlockModel -> inline(block.inline)
            is HtmlParagraphBlockModel -> inline(block.inline)
            is HeadingBlockModel -> inline(block.inline)
            is TableBlockModel -> block.rows.forEach { row -> row.cells.forEach { inline(it.inline) } }
            is FigureBlockModel -> add(MarkdownImageOccurrence(block.identity.stableId,
                MarkdownImageData(block.imageUrl, block.caption, block.caption, block.imageWidth, block.imageHeight, block.attributes)))
            is DefinitionListBlockModel -> block.items.forEach {
                when (it) {
                    is DefinitionTermBlockModel -> inline(it.inline)
                    is DefinitionDescriptionBlockModel -> it.children.forEach(::visit)
                }
            }
            else -> block.childBlocks().forEach(::visit)
        }
    }
    this@imageOccurrences.forEach(::visit)
}

internal fun InternalRenderBlockModel.childBlocks(): List<InternalRenderBlockModel> = when (this) {
    is BlockQuoteBlockModel -> children
    is AdmonitionBlockModel -> children
    is CustomContainerBlockModel -> children
    is DirectiveBlockModel -> children
    is HtmlContainerBlockModel -> children
    is FallbackContainerBlockModel -> children
    is FootnoteDefinitionBlockModel -> children
    is ListBlockModel -> items.flatMap { it.children }
    is ColumnsLayoutBlockModel -> columns.flatMap { it.children }
    is TabBlockModel -> items.flatMap { it.children }
    is DefinitionListBlockModel -> items.filterIsInstance<DefinitionDescriptionBlockModel>().flatMap { it.children }
    else -> emptyList()
}
