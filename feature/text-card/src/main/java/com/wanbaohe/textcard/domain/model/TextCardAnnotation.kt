package com.wanbaohe.textcard.domain.model

import com.shifenmiao.model.editor.DrawStroke
import com.shifenmiao.model.editor.LayerTransform
import com.shifenmiao.model.editor.ShapeSpec
import com.wanbaohe.textcard.domain.render.TextCardLayout
import java.util.UUID

data class TextCardAnnotation(
    val id: String = UUID.randomUUID().toString(),
    val pageAnchor: TextCardSourceAnchor = TextCardSourceAnchor(),
    val pageEndAnchor: TextCardSourceAnchor = TextCardSourceAnchor(
        affinity = TextCardAnchorAffinity.Previous
    ),
    val transform: LayerTransform = LayerTransform(),
    val type: TextCardAnnotationType,
)

sealed interface TextCardAnnotationType {
    data class Text(val block: TextBlock) : TextCardAnnotationType
    data class Draw(val strokes: List<DrawStroke>) : TextCardAnnotationType
    data class Shape(val spec: ShapeSpec) : TextCardAnnotationType
}

fun TextCardAnnotation.textBlockOrNull(): TextBlock? =
    (type as? TextCardAnnotationType.Text)?.block

fun TextCardLayout.pageFor(annotation: TextCardAnnotation): Int = pageFor(annotation.pageAnchor)

fun TextCardLayout.needsReview(annotation: TextCardAnnotation): Boolean =
    pageFor(annotation.pageAnchor) != pageFor(annotation.pageEndAnchor)

fun TextCardDocument.annotationsForPage(layout: TextCardLayout, pageIndex: Int): List<TextCardAnnotation> =
    annotations.filter { layout.pageFor(it) == pageIndex }

fun TextCardLayout.withAnnotations(document: TextCardDocument): TextCardLayout {
    val annotatedPages = pages.mapIndexed { index, page ->
        page.copy(annotations = document.annotationsForPage(this, index))
    }
    val hasAnnotations = document.annotations.isNotEmpty()
    return copy(
        pages = annotatedPages,
        longImage = longImage?.takeUnless { hasAnnotations },
        longImageRequiredHeight = if (hasAnnotations) {
            annotatedPages.sumOf { it.canvas.height }
        } else {
            longImageRequiredHeight
        }
    )
}

fun TextCardDocument.adjustAnnotationAnchors(
    role: TextCardRole,
    oldContent: String,
    newContent: String,
): List<TextCardAnnotation> {
    if (oldContent == newContent || annotations.isEmpty()) return annotations
    val prefix = oldContent.commonPrefixWith(newContent).length
    val suffixCapacity = minOf(oldContent.length, newContent.length) - prefix
    val suffix = oldContent.commonSuffixWith(newContent).length.coerceAtMost(suffixCapacity)
    val oldEnd = oldContent.length - suffix
    val newEnd = newContent.length - suffix
    val delta = newEnd - oldEnd

    fun TextCardSourceAnchor.adjust(): TextCardSourceAnchor {
        if (this.role != role) return this
        val updated = when {
            offset < prefix -> offset
            offset >= oldEnd -> offset + delta
            else -> newEnd
        }.coerceIn(0, newContent.length)
        return copy(offset = updated)
    }

    return annotations.map { annotation ->
        annotation.copy(
            pageAnchor = annotation.pageAnchor.adjust(),
            pageEndAnchor = annotation.pageEndAnchor.adjust(),
        )
    }
}
