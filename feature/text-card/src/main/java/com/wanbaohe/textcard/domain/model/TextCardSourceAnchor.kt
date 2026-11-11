package com.wanbaohe.textcard.domain.model

import com.wanbaohe.textcard.domain.render.TextCardLayout

data class TextCardSourceAnchor(
    val role: TextCardRole = TextCardRole.Body,
    val offset: Int = 0,
    val affinity: TextCardAnchorAffinity = TextCardAnchorAffinity.Next,
)

enum class TextCardAnchorAffinity {
    Previous, Next
}

fun TextCardDocument.normalizedAnchor(anchor: TextCardSourceAnchor): TextCardSourceAnchor =
    anchor.copy(offset = anchor.offset.coerceIn(0, block(anchor.role).content.length))

fun TextCardLayout.pageFor(anchor: TextCardSourceAnchor): Int {
    var closestPage = 0
    var closestDistance = Int.MAX_VALUE
    pages.forEachIndexed { pageIndex, page ->
        page.segments.forEach { segment ->
            if (segment.role != anchor.role) return@forEach
            val contains = anchor.offset in segment.sourceStart until segment.sourceEnd
            val atStart = anchor.offset == segment.sourceStart &&
                anchor.affinity == TextCardAnchorAffinity.Next
            val atEnd = anchor.offset == segment.sourceEnd &&
                anchor.affinity == TextCardAnchorAffinity.Previous
            if (contains || atStart || atEnd) return pageIndex
            val distance = when {
                anchor.offset < segment.sourceStart -> segment.sourceStart - anchor.offset
                anchor.offset >= segment.sourceEnd -> anchor.offset - segment.sourceEnd + 1
                else -> 0
            }
            if (distance < closestDistance) {
                closestDistance = distance
                closestPage = pageIndex
            }
        }
    }
    return closestPage.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
}

data class TextCardManualPageBreak(
    val offset: Int,
    val before: String,
    val after: String,
)

fun String.manualPageBreaks(): List<TextCardManualPageBreak> = buildList {
    var start = 0
    while (start < length) {
        val offset = indexOf(TextCardDocument.MANUAL_PAGE_BREAK, start)
        if (offset < 0) break
        add(
            TextCardManualPageBreak(
                offset = offset,
                before = substring(0, offset).trimEnd().takeLast(24),
                after = substring(offset + 1).trimStart().take(24),
            )
        )
        start = offset + 1
    }
}

fun TextCardRenderState.startsAtManualPageBreak(document: TextCardDocument): Boolean {
    val segment = segments.firstOrNull() ?: return false
    return document.block(segment.role).content.getOrNull(segment.sourceStart - 1) ==
        TextCardDocument.MANUAL_PAGE_BREAK
}
