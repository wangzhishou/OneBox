/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
        ): TextCardSegment {
            val blockWidth = (width * block.widthRatio).toInt().coerceIn(1, contentWidth)
            return TextCardSegment(
                block = block.copy(
                    id = "${block.id}_page_${pages.size}_segment_${segmentIndex++}",
                    baseTopRatio = topPx / width,
                    widthRatio = blockWidth.toFloat() / width,
                    widthManuallySet = true,
                    heightRatio = 0f
                ),
                role = role,
                sourceStart = start,
                sourceEnd = end
            )
        }

        TextCardRole.entries.forEachIndexed { roleIndex, role ->
            val source = document.block(role)
            if (source.content.isBlank()) return@forEachIndexed
            val roleContentWidth = (width * source.widthRatio).toInt().coerceIn(1, contentWidth)
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
                val chunkEnd = source.content.indexOf(TextCardDocument.MANUAL_PAGE_BREAK, start)
                    .takeIf { it >= 0 } ?: source.content.length
                if (chunkEnd == start) {
                    finishPage()
                    start++
                    continue
                }
                var remaining = source.slice(start, chunkEnd)
                var layout = remaining.createTextLayout(width, roleContentWidth)
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
                    layout = remaining.createTextLayout(width, roleContentWidth)
                    fittingLines = fittingLineCount(layout, available)
                    check(fittingLines > 0) { "Canvas is too small for the current text style" }
                }
                val end = if (fittingLines == layout.lineCount) {
                    remaining.content.length
                } else {
                    val visibleLines = if (layout.lineCount - fittingLines == 1 && fittingLines > 2) {
                        fittingLines - 1
                    } else fittingLines
                    val lineEnd = layout.getLineEnd(visibleLines - 1)
                        .coerceIn(1, remaining.content.length)
                    preferredBreakEnd(remaining.content, lineEnd)
                }
                val segment = remaining.slice(0, end)
                val segmentLayout = if (end == remaining.content.length) {
                    layout
                } else {
                    segment.createTextLayout(width, roleContentWidth)
                }
                current += positioned(segment, role, start, start + end, y)
                y += segmentLayout.height
                start += end
                if (start < chunkEnd) {
                    finishPage()
                } else if (chunkEnd < source.content.length) {
                    finishPage()
                    start = chunkEnd + 1
                }
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
            val block = document.block(role).let { source ->
                if (source.content.contains(TextCardDocument.MANUAL_PAGE_BREAK)) {
                    source.copy(
                        content = source.content.replace(TextCardDocument.MANUAL_PAGE_BREAK, '\n')
                    )
                } else source
            }
            if (block.content.isBlank()) return@forEachIndexed
            if (blocks.isNotEmpty()) {
                y += when (roleIndex) {
                    1 -> width * 0.04f
                    2 -> width * 0.055f
                    else -> width * 0.018f
                }
            }
            val blockWidth = (width * block.widthRatio).toInt().coerceIn(1, contentWidth)
            val laidOut = block.copy(
                widthRatio = blockWidth.toFloat() / width,
                widthManuallySet = true
            )
            val height = laidOut.createTextLayout(width, blockWidth).height
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

    private fun preferredBreakEnd(text: String, lineEnd: Int): Int {
        if (lineEnd <= 0 || lineEnd >= text.length) return lineEnd
        val searchEnd = lineEnd - 1
        val minimum = (lineEnd * 0.55f).toInt()
        val paragraph = text.lastIndexOf("\n\n", startIndex = searchEnd)
            .takeIf { it >= minimum }?.plus(2)?.coerceAtMost(lineEnd) ?: 0
        val newline = text.lastIndexOf('\n', startIndex = searchEnd)
            .takeIf { it >= minimum }?.plus(1) ?: 0
        val sentence = text.lastIndexOfAny(
            chars = charArrayOf('。', '！', '？', '!', '?', '；', ';', '.'),
            startIndex = searchEnd
        ).takeIf { it >= minimum }?.plus(1) ?: 0
        return maxOf(paragraph, newline, sentence).takeIf { it >= minimum } ?: lineEnd
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
