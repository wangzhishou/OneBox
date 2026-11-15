package com.hrm.markdown.renderer.internal.layout.inline

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import kotlin.math.floor

/** Optional work counters for regression tests and the opt-in layout benchmark. */
internal class InlineFlowMeasurementStats {
    var calls: Int = 0
        private set
    var characters: Long = 0
        private set

    internal fun record(length: Int) {
        calls++
        characters += length
    }
}

internal class InlineFlowTextMeasurement(
    private val measurer: TextMeasurer,
    private val style: TextStyle,
    private val stats: InlineFlowMeasurementStats?,
) {
    fun measure(
        text: AnnotatedString,
        width: Int = Constraints.Infinity,
        softWrap: Boolean = false,
    ): TextLayoutResult {
        stats?.record(text.length)
        return measurer.measure(
            text = text,
            style = style,
            constraints = Constraints(maxWidth = width),
            maxLines = if (softWrap) Int.MAX_VALUE else 1,
            softWrap = softWrap,
            // Window and line slices are transient. The flow cache retains the finished layout.
            skipCache = true,
        )
    }

    fun window(text: AnnotatedString, start: Int, width: Float): InlineTextWindow {
        var length = minOf(InitialWindowCharacters, text.length - start)
        while (true) {
            val end = safeTextBreakIndex(text.text, start + length)
                .coerceAtLeast(nextCodePointEnd(text.text, start))
            val slice = text.subSequence(start, end)
            val layout = measure(slice, floor(width).toInt().coerceAtLeast(1), softWrap = true)
            // A window boundary must never become a synthetic wrap. Keep its last unfinished
            // line for the next window; grow geometrically if even the first line is unfinished.
            val completeLines = if (end < text.length &&
                layout.getLineEnd(layout.lineCount - 1, visibleEnd = false) == slice.length
            ) layout.lineCount - 1 else layout.lineCount
            if (completeLines > 0 || end == text.length) {
                return InlineTextWindow(start, layout, completeLines)
            }
            length = (length.toLong() * 2).coerceAtMost((text.length - start).toLong()).toInt()
        }
    }

    companion object {
        private const val InitialWindowCharacters = 1024
    }
}

internal data class InlineTextWindow(
    val sourceStart: Int,
    val layout: TextLayoutResult,
    val completeLines: Int,
)

/** Search raw UTF-16 indices, but measure and return only complete code points. */
internal fun fittingTextPrefixLength(
    text: String,
    maxWidth: Float,
    measurePrefix: (Int) -> Float,
): Int {
    var lo = 1
    var hi = text.length
    var best = 0
    while (lo <= hi) {
        val rawMid = lo + (hi - lo) / 2
        val mid = safeTextBreakIndex(text, rawMid)
        if (mid == 0 || measurePrefix(mid) <= maxWidth) {
            best = mid
            // mid can be lo - 1 at a surrogate boundary; rawMid still guarantees progress.
            lo = rawMid + 1
        } else {
            hi = mid - 1
        }
    }
    return best
}

internal fun safeTextBreakIndex(text: String, index: Int): Int {
    val bounded = index.coerceIn(0, text.length)
    return if (bounded in 1 until text.length &&
        text[bounded - 1].isHighSurrogate() && text[bounded].isLowSurrogate()
    ) bounded - 1 else bounded
}

internal fun nextCodePointEnd(text: String, start: Int): Int =
    if (start + 1 < text.length && text[start].isHighSurrogate() && text[start + 1].isLowSurrogate()) {
        start + 2
    } else {
        (start + 1).coerceAtMost(text.length)
    }
