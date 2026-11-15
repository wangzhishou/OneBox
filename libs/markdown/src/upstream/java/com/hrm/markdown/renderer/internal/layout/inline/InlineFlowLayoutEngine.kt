package com.hrm.markdown.renderer.internal.layout.inline

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

private val WHITESPACE_BOUNDARY_REGEX = Regex("(?<=\\s)|(?=\\s)")

internal fun computeInlineFlowLayout(
    input: InlineFlowInput,
    style: TextStyle,
    density: Density,
    textMeasurer: TextMeasurer,
    maxWidthPx: Float,
    maxLines: Int,
    // Tests/benchmarks explicitly create counters; normal rendering leaves them disabled.
    measurementStats: InlineFlowMeasurementStats? = null,
): InlineFlowLayout {
    if (maxLines <= 0 || maxWidthPx <= 0f) {
        return InlineFlowLayout(
            widthPx = maxWidthPx.coerceAtLeast(0f),
            heightPx = 0f,
            lines = emptyList(),
        )
    }

    val textStyle = textMeasurementStyle(style)
    val measurement = InlineFlowTextMeasurement(textMeasurer, textStyle, measurementStats)
    val baseLineHeightPx = baseLineHeightPx(style, density)
    val baseMetrics = textMeasurer.measure(
        text = "Ag",
        style = textStyle,
        constraints = Constraints(maxWidth = Int.MAX_VALUE),
        maxLines = 1,
        softWrap = false,
    )
    val baseTextHeightPx = baseMetrics.size.height.toFloat()
    val baseTextBaselinePx = baseMetrics.firstBaseline

    data class MeasuredText(val widthPx: Float, val heightPx: Float, val baselinePx: Float)

    fun measureText(a: AnnotatedString): MeasuredText {
        if (a.isEmpty()) return MeasuredText(0f, 0f, 0f)
        val res = measurement.measure(a)
        return MeasuredText(
            widthPx = res.size.width.toFloat(),
            heightPx = res.size.height.toFloat(),
            baselinePx = res.firstBaseline,
        )
    }

    fun inlineSizePx(placeholder: InlinePlaceholderLayoutSpec): Pair<Float, Float> {
        return placeholderSizePx(placeholder)
    }

    val lines = ArrayList<InlineFlowLine>()
    var currentItems = ArrayList<LineItem>()
    var currentWidth = 0f
    var maxItemHeight = 0f
    var lineCount = 0

    fun appendTextItem(
        text: AnnotatedString,
        measured: MeasuredText,
        widthPx: Float = measured.widthPx,
        sourceStart: Int? = null,
    ) {
        currentItems.add(
            LineItem.TextItem(
                text = text,
                widthPx = widthPx.coerceAtLeast(0f),
                heightPx = measured.heightPx,
                baselinePx = measured.baselinePx,
                sourceStart = sourceStart,
                sourceEnd = sourceStart?.plus(text.length),
            )
        )
        currentWidth += widthPx.coerceAtLeast(0f)
        maxItemHeight = maxOf(maxItemHeight, measured.heightPx)
    }

    fun flushLine(force: Boolean = false) {
        if (!force && currentItems.isEmpty()) return
        val textItems = currentItems.filterIsInstance<LineItem.TextItem>()
        val maxTextAscentPx = textItems.maxOfOrNull { it.baselinePx } ?: baseTextBaselinePx
        val maxTextDescentPx = textItems.maxOfOrNull { it.heightPx - it.baselinePx }
            ?: (baseTextHeightPx - baseTextBaselinePx)
        val alignedTextHeightPx = maxTextAscentPx + maxTextDescentPx
        val lineHeightPx = maxOf(baseLineHeightPx, maxItemHeight, alignedTextHeightPx)
        val textLeadingPx = ((lineHeightPx - alignedTextHeightPx) / 2f).coerceAtLeast(0f)
        lines += InlineFlowLine(
            textStyle = textStyle,
            lineWidthPx = currentWidth,
            lineHeightPx = lineHeightPx,
            baselinePx = textLeadingPx + maxTextAscentPx,
            textAlign = style.textAlign,
            items = currentItems.toList(),
        )
        currentItems = ArrayList()
        currentWidth = 0f
        maxItemHeight = 0f
        lineCount++
    }

    for (token in input.segments) {
        if (lineCount >= maxLines) break
        when (token) {
            InlineFlowSegment.Newline -> flushLine(force = true)
            is InlineFlowSegment.InlineRun -> {
                val (w, h) = inlineSizePx(token.placeholder)
                if (w <= 0f || h <= 0f) continue
                if (currentWidth > 0f && currentWidth + w > maxWidthPx) {
                    flushLine(force = true)
                    if (lineCount >= maxLines) break
                }
                // 行内项不可切分：行首单项就比可用行宽还宽时，把占位宽夹到行宽，
                // 真实宽度留给渲染侧做横向滚动，否则超出部分会被右边界直接裁掉。
                val layoutWidthPx = w.coerceAtMost(maxWidthPx.coerceAtLeast(0f))
                currentItems.add(
                    LineItem.InlineItem(
                        id = token.id,
                        widthPx = layoutWidthPx,
                        heightPx = h,
                        alternateText = token.placeholder.alternateText,
                        sourceStart = token.sourceStart,
                        sourceEnd = token.sourceEnd,
                        naturalWidthPx = w,
                    )
                )
                currentWidth += layoutWidthPx
                maxItemHeight = maxOf(maxItemHeight, h)
            }

            is InlineFlowSegment.TextRun -> {
                val text = token.annotated
                var offset = 0
                while (offset < text.length && lineCount < maxLines) {
                    val available = (maxWidthPx - currentWidth).coerceAtLeast(0f)
                    if (available <= 0f && currentItems.isNotEmpty()) {
                        flushLine(force = true)
                        continue
                    }
                    val partialLine = currentItems.isNotEmpty()
                    val window = measurement.window(text, offset, available)
                    for (index in 0 until window.completeLines) {
                        if (lineCount >= maxLines) break
                        val lineEnd = safeTextBreakIndex(
                            text.text,
                            window.sourceStart + window.layout.getLineEnd(index, visibleEnd = false),
                        )
                        val candidate = text.subSequence(offset, lineEnd.coerceAtLeast(offset))
                        var measured = measureText(candidate)
                        val lineAvailable = (maxWidthPx - currentWidth).coerceAtLeast(0f)
                        val fitLength = if (measured.widthPx <= lineAvailable) candidate.length else {
                            fittingTextPrefixLength(candidate.text, lineAvailable) { end ->
                                measureText(candidate.subSequence(0, end)).widthPx
                            }
                        }
                        if (fitLength == 0) {
                            if (currentItems.isNotEmpty()) {
                                flushLine(force = true)
                            } else {
                                // A single code point can exceed the container. Emit it once
                                // without splitting a surrogate pair, then continue on a new line.
                                val end = nextCodePointEnd(text.text, offset)
                                val emergency = text.subSequence(offset, end)
                                appendTextItem(emergency, measureText(emergency), sourceStart = token.sourceStart?.plus(offset))
                                offset = end
                                flushLine(force = true)
                            }
                            break
                        }
                        val fit = if (fitLength == candidate.length) candidate else {
                            candidate.subSequence(0, fitLength).also { measured = measureText(it) }
                        }
                        appendTextItem(fit, measured, sourceStart = token.sourceStart?.plus(offset))
                        offset += fitLength
                        if (offset < text.length) {
                            flushLine(force = true)
                            val beforeSpaces = offset
                            while (offset < text.length && text[offset].isWhitespace() && text[offset] != '\n') offset++
                            // The first line after a widget has a different width. A shortened
                            // line or skipped whitespace also invalidates subsequent native cuts.
                            if (partialLine || fitLength != candidate.length || offset != beforeSpaces) break
                        }
                    }
                }
            }
        }
    }
    if (lineCount < maxLines) flushLine(force = false)

    val totalHeightPx = lines.sumOf { it.lineHeightPx.toDouble() }.toFloat()
    val firstBaselinePx = lines.firstOrNull()?.baselinePx
    val lastBaselinePx = if (lines.isEmpty()) null else {
        var y = 0f
        for (i in 0 until lines.lastIndex) {
            y += lines[i].lineHeightPx
        }
        y + lines.last().baselinePx
    }

    return InlineFlowLayout(
        widthPx = maxWidthPx,
        heightPx = totalHeightPx,
        firstBaselinePx = firstBaselinePx,
        lastBaselinePx = lastBaselinePx,
        lines = lines,
    )
}

internal fun textMeasurementStyle(style: TextStyle): TextStyle {
    return if (style.lineHeight.value.isNaN()) style else style.copy(lineHeight = TextUnit.Unspecified)
}

internal fun baseLineHeightPx(style: TextStyle, density: Density): Float = with(density) {
    val lh = style.lineHeight.value.takeUnless { it.isNaN() }
        ?: style.fontSize.value.takeUnless { it.isNaN() }?.times(1.5f)
        ?: 0f
    lh.sp.toPx()
}.coerceAtLeast(0f)

internal fun placeholderSizePx(
    placeholder: InlinePlaceholderLayoutSpec,
): Pair<Float, Float> {
    return placeholder.widthPx to placeholder.heightPx
}

internal fun computeMaxIntrinsicWidthPx(
    input: InlineFlowInput,
    style: TextStyle,
    textMeasurer: TextMeasurer,
): Int {
    val textStyle = textMeasurementStyle(style)
    var lineWidth = 0f
    var maxLineWidth = 0f
    for (token in input.segments) {
        when (token) {
            InlineFlowSegment.Newline -> {
                maxLineWidth = maxOf(maxLineWidth, lineWidth)
                lineWidth = 0f
            }

            is InlineFlowSegment.InlineRun -> {
                lineWidth += placeholderSizePx(token.placeholder).first
            }

            is InlineFlowSegment.TextRun -> {
                if (token.annotated.isEmpty()) continue
                val width = textMeasurer.measure(
                    text = token.annotated,
                    style = textStyle,
                    constraints = Constraints(maxWidth = Int.MAX_VALUE),
                    maxLines = 1,
                    softWrap = false,
                ).size.width.toFloat()
                lineWidth += width
            }
        }
    }
    maxLineWidth = maxOf(maxLineWidth, lineWidth)
    return ceil(maxLineWidth).toInt()
}

internal fun computeMinIntrinsicWidthPx(
    input: InlineFlowInput,
    style: TextStyle,
    textMeasurer: TextMeasurer,
): Int {
    val textStyle = textMeasurementStyle(style)
    var maxPieceWidth = 0f
    for (token in input.segments) {
        when (token) {
            InlineFlowSegment.Newline -> Unit
            is InlineFlowSegment.InlineRun -> {
                maxPieceWidth =
                    maxOf(maxPieceWidth, placeholderSizePx(token.placeholder).first)
            }

            is InlineFlowSegment.TextRun -> {
                val pieces = token.annotated.text.split(WHITESPACE_BOUNDARY_REGEX)
                var cursor = 0
                for (piece in pieces) {
                    if (piece.isEmpty()) continue
                    val end = cursor + piece.length
                    val sub = token.annotated.subSequence(cursor, end)
                    val width = textMeasurer.measure(
                        text = sub,
                        style = textStyle,
                        constraints = Constraints(maxWidth = Int.MAX_VALUE),
                        maxLines = 1,
                        softWrap = false,
                    ).size.width.toFloat()
                    maxPieceWidth = maxOf(maxPieceWidth, width)
                    cursor = end
                }
            }
        }
    }
    return ceil(maxPieceWidth).toInt()
}
