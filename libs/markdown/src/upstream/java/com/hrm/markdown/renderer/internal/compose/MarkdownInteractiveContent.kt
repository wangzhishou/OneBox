package com.hrm.markdown.renderer.internal.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import com.hrm.markdown.renderer.DefaultMarkdownImage
import com.hrm.markdown.renderer.LocalImageRenderer
import com.hrm.markdown.renderer.MarkdownImageData
import com.hrm.markdown.renderer.internal.LocalMarkdownInteractions

internal const val CitationAnnotationTag = "markdown:citation"
internal const val InlineElementAnnotationTag = "markdown:inline-element"

/** Geometry is owned only by the visible text item and is read when an action is invoked. */
private class CitationGeometry {
    var coordinates: LayoutCoordinates? = null
    var layout: TextLayoutResult? = null

    fun bounds(start: Int, end: Int): Rect? {
        val coordinates = coordinates?.takeIf { it.isAttached } ?: return null
        val result = layout ?: return null
        if (start >= end || end > result.layoutInput.text.length) return null
        var bounds = result.getBoundingBox(start)
        for (offset in start + 1 until end) {
            val next = result.getBoundingBox(offset)
            bounds = Rect(minOf(bounds.left, next.left), minOf(bounds.top, next.top),
                maxOf(bounds.right, next.right), maxOf(bounds.bottom, next.bottom))
        }
        val corners = listOf(
            coordinates.localToWindow(Offset(bounds.left, bounds.top)),
            coordinates.localToWindow(Offset(bounds.right, bounds.top)),
            coordinates.localToWindow(Offset(bounds.left, bounds.bottom)),
            coordinates.localToWindow(Offset(bounds.right, bounds.bottom)),
        )
        return Rect(corners.minOf { it.x }, corners.minOf { it.y },
            corners.maxOf { it.x }, corners.maxOf { it.y })
    }
}

/** Bind actions after layout. Callback updates never invalidate measured inline models. */
@Composable
internal fun MarkdownInteractiveText(
    text: AnnotatedString,
    modifier: Modifier,
    style: TextStyle,
    onTextLayout: ((TextLayoutResult) -> Unit)?,
) {
    val interactions = LocalMarkdownInteractions.current
    val citations = if (interactions?.citationEnabled == true) {
        remember(text) { text.getStringAnnotations(CitationAnnotationTag, 0, text.length) }
    } else emptyList()
    val inlineElements = if (interactions?.inlineElementEnabled == true) {
        remember(text) { text.getStringAnnotations(InlineElementAnnotationTag, 0, text.length) }
    } else emptyList()
    if (citations.isEmpty() && inlineElements.isEmpty()) {
        BasicText(text, modifier, style, maxLines = 1, softWrap = false, onTextLayout = onTextLayout)
        return
    }
    val geometry = remember { CitationGeometry() }
    val interactiveText = remember(text, citations, inlineElements, interactions) {
        AnnotatedString.Builder(text).apply {
            for (citation in citations) {
                val id = citation.item.substringBefore(':').toLong()
                val key = citation.item.substringAfter(':')
                addLink(LinkAnnotation.Clickable(
                    tag = "citation",
                    linkInteractionListener = {
                        interactions?.citation(key, id, geometry.bounds(citation.start, citation.end))
                    },
                ), citation.start, citation.end)
            }
            for (element in inlineElements) {
                // 注解值：occurrenceId|kind|payload|attributes，后两段各自用 \u001f 分隔键值对
                val parts = element.item.split('|')
                val id = parts.getOrNull(0)?.toLongOrNull() ?: continue
                val kind = parts.getOrNull(1).orEmpty()
                val payload = decodePairs(parts.getOrNull(2))
                val attributes = decodePairs(parts.getOrNull(3))
                val originalLink = text.getLinkAnnotations(element.start, element.end)
                    .firstOrNull { it.start <= element.start && it.end >= element.end }?.item
                addLink(LinkAnnotation.Clickable(
                    tag = kind.ifEmpty { "inline-element" },
                    styles = originalLink?.styles,
                    linkInteractionListener = {
                        interactions?.inlineElement(
                            kind,
                            text.subSequence(element.start, element.end).toString(),
                            payload,
                            attributes,
                            id,
                            geometry.bounds(element.start, element.end),
                        )
                        originalLink?.linkInteractionListener?.onClick(originalLink)
                    },
                ), element.start, element.end)
            }
        }.toAnnotatedString()
    }
    BasicText(
        text = interactiveText,
        modifier = modifier.onGloballyPositioned { geometry.coordinates = it },
        style = style,
        maxLines = 1,
        softWrap = false,
        onTextLayout = {
            geometry.layout = it
            onTextLayout?.invoke(it)
        },
    )
}

/** The same wrapper serves inline images, HTML images and figures, including custom loaders. */
@Composable
internal fun RenderInteractiveMarkdownImage(data: MarkdownImageData, occurrenceId: Long) {
    val interactions = LocalMarkdownInteractions.current
    val renderer = LocalImageRenderer.current
    val content: @Composable () -> Unit = {
        if (renderer == null) DefaultMarkdownImage(data) else renderer(data, Modifier)
    }
    if (interactions?.imageEnabled != true) {
        content()
        return
    }
    // imageEnabled 现已涵盖统一监听；controller.image 负责在两级回调间分发。
    val geometry = remember { ImageGeometry() }
    Box(Modifier
        .onGloballyPositioned { geometry.coordinates = it }
        .clickable(onClickLabel = "Open image") {
            interactions.image(data, occurrenceId,
                geometry.coordinates?.takeIf { it.isAttached }?.boundsInWindow())
        }) { content() }
}

private class ImageGeometry {
    var coordinates: LayoutCoordinates? = null
}

internal fun decodePairs(segment: String?): Map<String, String> =
    segment.orEmpty().split('').mapNotNull { entry ->
        val eq = entry.indexOf('=')
        if (eq <= 0) null else entry.take(eq) to entry.substring(eq + 1)
    }.toMap()
