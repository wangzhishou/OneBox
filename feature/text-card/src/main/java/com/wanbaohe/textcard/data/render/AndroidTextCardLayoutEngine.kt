package com.wanbaohe.textcard.data.render

import android.text.StaticLayout
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.model.TextCardSegment
import com.wanbaohe.textcard.domain.model.TextCardRole
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.domain.render.TextCardLayout
import com.wanbaohe.textcard.domain.render.TextCardLayoutEngine
import com.wanbaohe.textcard.domain.render.TextCardLayoutException
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class AndroidTextCardLayoutEngine @Inject internal constructor() : TextCardLayoutEngine {

    override fun layout(document: TextCardDocument): TextCardLayout {
        val pages = paginate(document)
        val longImageResult = runCatching { layoutLongImage(document) }
        return TextCardLayout(
            pages = pages,
            longImage = longImageResult.getOrNull(),
            longImageRequiredHeight = (longImageResult.exceptionOrNull() as?
                TextCardLayoutException.LongImageTooLarge)?.requiredHeight
        )
    }

    private fun paginate(document: TextCardDocument): List<TextCardRenderState> {
        val canvas = document.canvas
        val width = canvas.width
        val height = canvas.height
        val padding = width * CardLayout.CONTENT_PADDING_RATIO
        val contentWidth = (width - padding * 2).toInt().coerceAtLeast(1)
        val pageNumberSpace = if (document.showPageNumber) width * 0.045f else 0f
        val top = padding
        val bottom = height - padding - pageNumberSpace
        require(bottom - top > 1f) { "Canvas height is too small for the current width" }
        val pages = mutableListOf<MutableList<TextCardSegment>>()
        var current = mutableListOf<TextCardSegment>()
        var y = top
        var segmentIndex = 0

        fun finishPage() {
            if (current.isNotEmpty()) {
                pages += current
                current = mutableListOf()
            }
            y = top
        }

        fun positioned(
            block: TextBlock,
            role: TextCardRole,
            start: Int,
            end: Int,
            topPx: Float,
        ): TextCardSegment = TextCardSegment(
            block = block.copy(
                id = "${block.id}_page_${pages.size}_segment_${segmentIndex++}",
                baseTopRatio = topPx / width,
                widthRatio = contentWidth.toFloat() / width,
                widthManuallySet = true,
                heightRatio = 0f
            ),
            role = role,
            sourceStart = start,
            sourceEnd = end
        )

        TextCardRole.entries.forEachIndexed { roleIndex, role ->
            val source = document.block(role)
            if (source.content.isBlank()) return@forEachIndexed
            if (current.isNotEmpty()) {
                y += when (roleIndex) {
                    1 -> width * 0.04f
                    2 -> width * 0.055f
                    else -> width * 0.018f
                }
            }
            var start = 0
            while (start < source.content.length) {
                val available = bottom - y
                if (available <= 1f) {
                    check(current.isNotEmpty()) { "No layout progress is possible on this canvas" }
                    finishPage()
                    continue
                }
                var remaining = source.slice(start, source.content.length)
                var layout = remaining.createTextLayout(width, contentWidth)
                var fittingLines = fittingLineCount(layout, available)
                if (fittingLines == 0 && current.isNotEmpty()) {
                    finishPage()
                    continue
                }
                if (fittingLines == 0) {
                    val firstLineHeight = layout.getLineBottom(0).coerceAtLeast(1)
                    val scale = (available / firstLineHeight * 0.92f).coerceIn(0.1f, 1f)
                    remaining = remaining.copy(
                        sizeScale = (remaining.sizeScale * scale).coerceAtLeast(0.1f),
                        lineSpacingMultiplier = remaining.lineSpacingMultiplier.coerceAtMost(2f)
                    )
                    layout = remaining.createTextLayout(width, contentWidth)
                    fittingLines = fittingLineCount(layout, available)
                    check(fittingLines > 0) { "Canvas is too small for the current text style" }
                }
                val end = if (fittingLines == layout.lineCount) {
                    remaining.content.length
                } else {
                    layout.getLineEnd(fittingLines - 1).coerceIn(1, remaining.content.length)
                }
                val segment = remaining.slice(0, end)
                val segmentLayout = if (end == remaining.content.length) {
                    layout
                } else {
                    segment.createTextLayout(width, contentWidth)
                }
                current += positioned(segment, role, start, start + end, y)
                y += segmentLayout.height
                start += end
                if (start < source.content.length) finishPage()
            }
        }
        if (current.isNotEmpty() || pages.isEmpty()) pages += current

        return pages.mapIndexed { index, blocks ->
            val pageNumber = if (document.showPageNumber && pages.size > 1) {
                TextBlock(
                    id = "${TextCardDocument.ROLE_PAGE_NUMBER}_$index",
                    content = "${index + 1} / ${pages.size}",
                    baseSizeRatio = 0.021f,
                    baseTopRatio = (height - padding - width * 0.032f) / width,
                    widthRatio = contentWidth.toFloat() / width,
                    widthManuallySet = true,
                    heightRatio = 0f,
                    alignment = CardTextAlignment.Center,
                    color = document.body.color,
                    font = document.body.font,
                    alpha = 0.62f
                )
            } else null
            document.renderState(canvas, blocks, listOfNotNull(pageNumber))
        }
    }

    private fun layoutLongImage(document: TextCardDocument): TextCardRenderState {
        val canvas = document.canvas
        val width = canvas.width
        val padding = width * CardLayout.CONTENT_PADDING_RATIO
        val contentWidth = (width - padding * 2).toInt().coerceAtLeast(1)
        val blocks = mutableListOf<TextCardSegment>()
        var y = padding
        var segmentIndex = 0

        TextCardRole.entries.forEachIndexed { roleIndex, role ->
            val block = document.block(role)
            if (block.content.isBlank()) return@forEachIndexed
            if (blocks.isNotEmpty()) {
                y += when (roleIndex) {
                    1 -> width * 0.04f
                    2 -> width * 0.055f
                    else -> width * 0.018f
                }
            }
            val laidOut = block.copy(
                widthRatio = contentWidth.toFloat() / width,
                widthManuallySet = true
            )
            val height = laidOut.createTextLayout(width, contentWidth).height
            blocks += TextCardSegment(
                block = laidOut.copy(
                    id = "${block.id}_long_${segmentIndex++}",
                    baseTopRatio = y / width,
                    heightRatio = 0f
                ),
                role = role,
                sourceStart = 0,
                sourceEnd = block.content.length
            )
            y += height
        }

        val requiredHeight = max(canvas.height, ceil(y + padding).toInt())
        val pixels = width.toLong() * requiredHeight
        if (requiredHeight > TextCardDocument.MAX_LONG_IMAGE_SIDE ||
            pixels > TextCardDocument.MAX_LONG_IMAGE_PIXELS
        ) {
            throw TextCardLayoutException.LongImageTooLarge(requiredHeight)
        }
        val outputCanvas = if (requiredHeight == canvas.height) {
            canvas
        } else {
            CanvasSpec.Custom(width, requiredHeight)
        }
        return document.renderState(outputCanvas, blocks)
    }

    private fun fittingLineCount(layout: StaticLayout, availableHeight: Float): Int {
        if (layout.height <= availableHeight) return layout.lineCount
        var low = 0
        var high = layout.lineCount
        while (low < high) {
            val mid = (low + high + 1) ushr 1
            if (layout.getLineBottom(mid - 1) <= availableHeight) low = mid else high = mid - 1
        }
        return low
    }
}

private fun TextCardDocument.renderState(
    canvas: CanvasSpec,
    segments: List<TextCardSegment>,
    additionalBlocks: List<TextBlock> = emptyList(),
): TextCardRenderState = TextCardRenderState(
    canvas = canvas,
    background = background,
    backgroundOpacity = backgroundOpacity,
    textBlocks = segments.map { it.block } + additionalBlocks,
    segments = segments
)

private fun TextBlock.slice(start: Int, end: Int): TextBlock {
    val value = content.substring(start, end)
    val spans = styleSpans.mapNotNull { span ->
        val sliceStart = max(start, span.start)
        val sliceEnd = min(end, span.end)
        if (sliceStart < sliceEnd) {
            span.copy(start = sliceStart - start, end = sliceEnd - start)
        } else null
    }
    val fonts = fontSpans.mapNotNull { span ->
        val sliceStart = max(start, span.start)
        val sliceEnd = min(end, span.end)
        if (sliceStart < sliceEnd) {
            span.copy(start = sliceStart - start, end = sliceEnd - start)
        } else null
    }
    return copy(content = value, styleSpans = spans, fontSpans = fonts)
}
