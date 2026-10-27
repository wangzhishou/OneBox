package com.wanbaohe.textcard.domain.model

fun TextBlock.replaceSourceText(
    start: Int,
    end: Int,
    replacement: String,
    insertedStyles: List<TextStyleSpan>,
    insertedFonts: List<TextCardFontSpan>,
): TextBlock {
    requireValidRange(start, end)
    val delta = replacement.length - (end - start)
    return copy(
        content = content.replaceRange(start, end, replacement),
        styleSpans = styleSpans.replaceSpans(
            start = start,
            end = end,
            insertedLength = replacement.length,
            inserted = insertedStyles,
            delta = delta,
            bounds = { it.start to it.end },
            rebuild = { span, newStart, newEnd -> span.copy(start = newStart, end = newEnd) }
        ),
        fontSpans = fontSpans.replaceSpans(
            start = start,
            end = end,
            insertedLength = replacement.length,
            inserted = insertedFonts,
            delta = delta,
            bounds = { it.start to it.end },
            rebuild = { span, newStart, newEnd -> span.copy(start = newStart, end = newEnd) }
        )
    )
}

fun TextBlock.replaceSourceStyles(
    start: Int,
    end: Int,
    inserted: List<TextStyleSpan>,
): TextBlock {
    requireValidRange(start, end)
    return copy(
        styleSpans = styleSpans.replaceSpans(
            start = start,
            end = end,
            insertedLength = end - start,
            inserted = inserted,
            delta = 0,
            bounds = { it.start to it.end },
            rebuild = { span, newStart, newEnd -> span.copy(start = newStart, end = newEnd) }
        )
    )
}

fun TextBlock.replaceSourceFonts(
    start: Int,
    end: Int,
    inserted: List<TextCardFontSpan>,
): TextBlock {
    requireValidRange(start, end)
    return copy(
        fontSpans = fontSpans.replaceSpans(
            start = start,
            end = end,
            insertedLength = end - start,
            inserted = inserted,
            delta = 0,
            bounds = { it.start to it.end },
            rebuild = { span, newStart, newEnd -> span.copy(start = newStart, end = newEnd) }
        )
    )
}

fun TextBlock.clearSourceRangeStyles(start: Int, end: Int): TextBlock =
    replaceSourceStyles(start, end, emptyList()).replaceSourceFonts(start, end, emptyList())

private fun TextBlock.requireValidRange(start: Int, end: Int) {
    require(start >= 0 && start <= end && end <= content.length) {
        "Invalid source range: $start..$end for content length ${content.length}"
    }
}

private inline fun <T> List<T>.replaceSpans(
    start: Int,
    end: Int,
    insertedLength: Int,
    inserted: List<T>,
    delta: Int,
    crossinline bounds: (T) -> Pair<Int, Int>,
    crossinline rebuild: (T, Int, Int) -> T,
): List<T> = buildList {
    this@replaceSpans.forEach { item ->
        val (itemStart, itemEnd) = bounds(item)
        when {
            itemEnd <= start -> add(item)
            itemStart >= end -> add(rebuild(item, itemStart + delta, itemEnd + delta))
            else -> {
                if (itemStart < start) add(rebuild(item, itemStart, start))
                if (itemEnd > end) {
                    add(rebuild(item, start + insertedLength, itemEnd + delta))
                }
            }
        }
    }
    addAll(inserted)
}.sortedBy { bounds(it).first }
