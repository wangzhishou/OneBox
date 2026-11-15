package com.hrm.markdown.renderer.internal.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.unit.Constraints
import com.hrm.markdown.renderer.MarkdownInlineDecorationRequest
import com.hrm.markdown.renderer.MarkdownInlineUnderline
import com.hrm.markdown.renderer.internal.LocalMarkdownInteractions
import com.hrm.markdown.renderer.internal.layout.inline.LayoutInlineRunPlacement
import com.hrm.markdown.renderer.internal.layout.inline.runPlacements
import com.hrm.markdown.renderer.internal.layout.inline.textMeasurementStyle
import com.hrm.markdown.renderer.internal.layout.model.LayoutInlineBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTextRun
import com.hrm.markdown.renderer.internal.layout.model.LayoutWidgetRun
import kotlin.math.ceil

internal const val InlineElementRangeEndAnnotationTag = "markdown:inline-element-end"

/** Center of the first painted text line, propagated through Compose containers like FirstBaseline. */
internal val FirstTextLineCenter = HorizontalAlignmentLine { old, new -> minOf(old, new) }

@Composable
internal fun PaintInlineLayoutContent(
    block: LayoutInlineBlockModel,
    modifier: Modifier = Modifier,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    onTextItemLayout: ((List<LayoutInlineRunPlacement>, TextLayoutResult) -> Unit)? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val textPaintStyle = remember(block.style) {
        textMeasurementStyle(block.style)
    }
    val placements = remember(block.lines, block.frame) {
        block.runPlacements()
    }
    val paintItems = remember(placements, block.style, textMeasurer) {
        buildInlinePaintItems(
            block = block,
            placements = placements,
            style = block.style,
            textMeasurer = textMeasurer,
        )
    }
    val alignmentLines = remember(paintItems) {
        val firstText = paintItems.firstOrNull() as? InlineTextPaintItem
        if (firstText == null) emptyMap() else mapOf<AlignmentLine, Int>(
            FirstTextLineCenter to firstText.y + firstText.height / 2,
        )
    }
    Layout(
        // Merge visual text fragments into a reading unit. Native link and widget semantics
        // remain descendants, retaining their own accessibility actions.
        modifier = modifier.semantics(mergeDescendants = LocalMergeInlineSemantics.current) {
            isTraversalGroup = true
            if (block.isHeading) heading()
        },
        content = {
            for ((itemIndex, item) in paintItems.withIndex()) {
                when (item) {
                    is InlineTextPaintItem -> key(
                        item.stableKey,
                        onTextLayout != null || onTextItemLayout != null,
                    ) {
                        val underlines = rememberMarkdownInlineUnderlines(item.text)
                        // painter 占位已经计入 flow 布局；此处只缓存文字装饰线几何。
                        val decorationGeometry = remember(item.text, textPaintStyle, item.width, item.height, LocalDensity.current) {
                            mutableStateOf<TextLayoutResult?>(null)
                        }
                        val needsTextLayout = onTextLayout != null || onTextItemLayout != null || underlines.isNotEmpty()
                        MarkdownInteractiveText(
                            text = item.text,
                            modifier = (if (underlines.isEmpty()) {
                                Modifier
                            } else {
                                Modifier.drawBehind {
                                    val markerLeft = block.contentFrame.left - block.frame.left - item.x
                                    drawInlineUnderlines(
                                        underlines,
                                        decorationGeometry.value,
                                        markerBounds = markerLeft..(markerLeft + block.contentFrame.width),
                                    )
                                }
                            }).clipToBounds().semantics {
                                traversalIndex = itemIndex.toFloat()
                            },
                            // InlineFlowLayout owns line spacing. Paint with the same
                            // line-height-free style used to measure the fixed run bounds.
                            style = textPaintStyle,
                            onTextLayout = if (needsTextLayout) {
                                { result ->
                                    decorationGeometry.value = result
                                    onTextLayout?.invoke(result)
                                    onTextItemLayout?.invoke(item.placements, result)
                                }
                            } else {
                                null
                            },
                        )
                    }

                    is InlineWidgetPaintItem -> key(item.run.identity.stableId) {
                        val run = item.run
                        val payload = block.inlinePayloads[run.id]
                        // 占位宽度被夹到行宽时，widget 仍按自然宽度绘制并要求横向滚动，
                        // 否则超宽内容会在右边界被静默裁掉。
                        val scrollState = if (run.naturalWidthPx > run.frame.width) {
                            rememberScrollState()
                        } else {
                            null
                        }
                        Box(
                            modifier = Modifier.semantics {
                                traversalIndex = itemIndex.toFloat()
                            }.then(
                                if (scrollState != null) Modifier.horizontalScroll(scrollState) else Modifier
                            ),
                        ) {
                            if (scrollState != null) {
                                // Keep the payload's slot finite inside the unbounded scroll content.
                                // Round up like runPlacements so fractional widths do not clip the end.
                                val contentWidth = with(LocalDensity.current) {
                                    ceil(run.naturalWidthPx).toDp()
                                }
                                Box(Modifier.width(contentWidth)) {
                                    payload?.content?.invoke()
                                }
                            } else {
                                payload?.content?.invoke()
                            }
                        }
                    }
                }
            }
        }
    ) { measurables, constraints ->
        val placeables = ArrayList<Placeable>(measurables.size)
        for (index in paintItems.indices) {
            val item = paintItems[index]
            placeables += measurables[index].measure(
                if (item.width == 0 || item.height == 0) {
                    Constraints.fixed(0, 0)
                } else {
                    Constraints.fixed(item.width, item.height)
                }
            )
        }

        val desiredWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            ceil(block.frame.width).toInt().coerceAtLeast(constraints.minWidth)
        }
        val desiredHeight = ceil(block.contentFrame.height).toInt()
            .coerceAtLeast(constraints.minHeight)
            .let { height ->
                if (constraints.hasBoundedHeight) height.coerceAtMost(constraints.maxHeight) else height
            }

        layout(desiredWidth, desiredHeight, alignmentLines = alignmentLines) {
            for (index in paintItems.indices) {
                val item = paintItems[index]
                // LayoutInlineBlockModel owns physical geometry; do not mirror it a second time.
                placeables[index].place(item.x, item.y)
            }
        }
    }
}

private sealed interface InlinePaintItem {
    val x: Int
    val y: Int
    val width: Int
    val height: Int
}

private data class InlineTextPaintItem(
    val placements: List<LayoutInlineRunPlacement>,
    val text: AnnotatedString,
    override val x: Int,
    override val y: Int,
    override val width: Int,
    override val height: Int,
) : InlinePaintItem {
    val stableKey: Long = placements.fold(0L) { acc, placement ->
        acc * 31 + placement.run.identity.stableId
    }
}

private data class InlineWidgetPaintItem(
    val placement: LayoutInlineRunPlacement,
) : InlinePaintItem {
    val run: LayoutWidgetRun = placement.run as LayoutWidgetRun
    override val x: Int get() = placement.x
    override val y: Int get() = placement.y
    override val width: Int get() = placement.width
    override val height: Int get() = placement.height
}

private fun buildInlinePaintItems(
    block: LayoutInlineBlockModel,
    placements: List<LayoutInlineRunPlacement>,
    style: TextStyle,
    textMeasurer: TextMeasurer,
): List<InlinePaintItem> {
    val items = ArrayList<InlinePaintItem>()
    val textPlacements = ArrayList<LayoutInlineRunPlacement>()

    fun flushTextPlacements() {
        if (textPlacements.isEmpty()) return
        items += buildInlineTextPaintItems(
            placements = textPlacements,
            style = style,
            textMeasurer = textMeasurer,
        )
        textPlacements.clear()
    }

    for (placement in placements) {
        when (placement.run) {
            is LayoutTextRun -> {
                val previous = textPlacements.lastOrNull()
                if (previous != null && !canMergeTextPlacements(previous, placement)) {
                    flushTextPlacements()
                }
                textPlacements += placement
            }

            is LayoutWidgetRun -> {
                flushTextPlacements()
                items += InlineWidgetPaintItem(placement)
            }
        }
    }
    flushTextPlacements()
    return items
}

private fun canMergeTextPlacements(
    previous: LayoutInlineRunPlacement,
    next: LayoutInlineRunPlacement,
): Boolean {
    val previousRight = previous.x + previous.width
    return previous.lineIndex == next.lineIndex &&
        previous.runIndex + 1 == next.runIndex &&
        previous.y == next.y &&
        kotlin.math.abs(next.x - previousRight) <= 1
}

private fun buildInlineTextPaintItems(
    placements: List<LayoutInlineRunPlacement>,
    style: TextStyle,
    textMeasurer: TextMeasurer,
): List<InlineTextPaintItem> {
    val merged = buildInlineTextPaintItem(placements)
    if (placements.size == 1) {
        return listOf(merged)
    }

    // A single run was already measured by InlineFlow. Only merged runs need this guard because
    // shaping across their boundary can produce a different width from the sum of both runs.
    val measured = textMeasurer.measure(
        text = merged.text,
        style = textMeasurementStyle(style),
        constraints = Constraints(maxWidth = Int.MAX_VALUE),
        maxLines = 1,
        softWrap = false,
    )
    // Use the fractional advance in both directions: ceil(size.width) can hide a narrower merged run.
    val measuredWidth = measured.getLineRight(0) - measured.getLineLeft(0)
    if (kotlin.math.abs(measuredWidth - merged.width) <= 1f && measured.size.height <= merged.height + 1) {
        return listOf(merged)
    }
    return placements.map { placement -> buildInlineTextPaintItem(listOf(placement)) }
}

private fun buildInlineTextPaintItem(
    placements: List<LayoutInlineRunPlacement>,
): InlineTextPaintItem {
    val first = placements.first()
    val mergedText = placements.joinText()
    var right = first.x + first.width
    var bottom = first.y + first.height
    for (i in 1 until placements.size) {
        val placement = placements[i]
        right = maxOf(right, placement.x + placement.width)
        bottom = maxOf(bottom, placement.y + placement.height)
    }
    return InlineTextPaintItem(
        placements = placements.toList(),
        text = mergedText,
        x = first.x,
        y = first.y,
        width = (right - first.x).coerceAtLeast(0),
        height = (bottom - first.y).coerceAtLeast(0),
    )
}

private fun List<LayoutInlineRunPlacement>.joinText(): AnnotatedString {
    if (size == 1) {
        return (first().run as LayoutTextRun).text
    }
    return buildAnnotatedString {
        for (placement in this@joinText) {
            append((placement.run as LayoutTextRun).text)
        }
    }
}

private class InlineUnderlineRange(
    val start: Int,
    val end: Int,
    val underline: MarkdownInlineUnderline,
    val isRangeEnd: Boolean,
)

/**
 * 收集该行内文本项的装饰请求。注解列表按文本缓存，装饰回调在每次重组时重新执行——
 * 宿主回调内部读取的状态（如点赞态）变化会触发重组并刷新装饰。
 */
@Composable
private fun rememberMarkdownInlineUnderlines(text: AnnotatedString): List<InlineUnderlineRange> {
    val interactions = LocalMarkdownInteractions.current
    if (interactions?.decorationEnabled != true) return emptyList()
    val annotations = remember(text) {
        text.getStringAnnotations(InlineElementAnnotationTag, 0, text.length)
    }
    val rangeEnds = remember(text) {
        text.getStringAnnotations(InlineElementRangeEndAnnotationTag, 0, text.length)
    }
    return annotations.mapNotNull { annotation ->
        val parts = annotation.item.split('|')
        val kind = parts.getOrNull(1).orEmpty()
        if (kind.isEmpty()) return@mapNotNull null
        val underline = interactions.decorate(
            MarkdownInlineDecorationRequest(
                kind = kind,
                text = text.subSequence(annotation.start, annotation.end).toString(),
                payload = decodePairs(parts.getOrNull(2)),
                attributes = decodePairs(parts.getOrNull(3)),
            ),
        ) ?: return@mapNotNull null
        InlineUnderlineRange(
            annotation.start,
            annotation.end,
            underline,
            isRangeEnd = rangeEnds.any { it.start < it.end && it.end == annotation.end && it.item == annotation.item },
        )
    }
}

private fun DrawScope.drawInlineUnderlines(
    underlines: List<InlineUnderlineRange>,
    layout: TextLayoutResult?,
    markerBounds: ClosedFloatingPointRange<Float>,
) {
    if (layout == null || underlines.isEmpty()) return
    val textLength = layout.layoutInput.text.length
    for (range in underlines) {
        val start = range.start.coerceIn(0, textLength)
        val end = range.end.coerceIn(0, textLength)
        if (end <= start) continue
        val underline = range.underline
        val strokeWidth = underline.strokeWidth.toPx()
        val dashWidth = underline.dashWidth?.toPx()
        val gapWidth = underline.gapWidth?.toPx()
        val dotDiameter = underline.dotDiameter?.toPx()
        val dotGap = underline.dotGap.toPx()
        val offset = underline.offset.toPx()
        val startLine = layout.getLineForOffset(start)
        val endLine = layout.getLineForOffset((end - 1).coerceAtLeast(start))
        for (line in startLine..endLine) {
            val lineStart = if (line == startLine) start else layout.getLineStart(line)
            val lineEnd = if (line == endLine) end else layout.getLineEnd(line)
            if (lineEnd <= lineStart) continue
            val left = layout.getHorizontalPosition(lineStart, usePrimaryDirection = true)
            val right = layout.getHorizontalPosition(lineEnd, usePrimaryDirection = true)
            val x1 = minOf(left, right)
            val x2 = maxOf(left, right)
            if (x2 - x1 <= 0f) continue
            val y = layout.getLineBottom(line) - offset
            val isLastLine = range.isRangeEnd && line == endLine
            when {
                dotDiameter != null && dotDiameter > 0f -> {
                    // 点线：沿行内字符区间等距铺圆点，两端各留半个点，避免首尾被裁。
                    val step = dotDiameter + dotGap.coerceAtLeast(0f)
                    var x = x1 + dotDiameter / 2f
                    while (x <= x2 - dotDiameter / 2f + 0.01f) {
                        drawCircle(
                            color = underline.color,
                            radius = dotDiameter / 2f,
                            center = Offset(x, y),
                        )
                        x += step
                    }
                }

                dashWidth != null && gapWidth != null -> {
                    drawPath(
                        path = Path().apply {
                            moveTo(x1, y)
                            lineTo(x2, y)
                        },
                        color = underline.color,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, gapWidth)),
                        ),
                    )
                }

                else -> {
                    drawLine(
                        color = underline.color,
                        start = Offset(x1, y),
                        end = Offset(x2, y),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round,
                    )
                }
            }
            if (isLastLine) {
                val lastBox = layout.getBoundingBox(end - 1)
                val isRtl = layout.getBidiRunDirection(end - 1) == ResolvedTextDirection.Rtl
                drawEndMarker(underline, if (isRtl) lastBox.left else lastBox.right, y, isRtl, markerBounds)
            }
        }
    }
}

/** 在最后一个文字端点绘制，不改变正文测量、断行或可选内容。 */
private fun DrawScope.drawEndMarker(
    underline: MarkdownInlineUnderline,
    endX: Float,
    baselineY: Float,
    isRtl: Boolean,
    markerBounds: ClosedFloatingPointRange<Float>,
) {
    val marker = underline.endMarker ?: return
    if (marker.painter != null) return // Painter belongs to the measured flow widget.
    val diameter = marker.diameter.toPx().coerceAtMost(markerBounds.endInclusive - markerBounds.start)
    if (!diameter.isFinite() || diameter <= 0f) return
    val radius = diameter / 2f
    val preferredLeft = if (isRtl) endX - marker.gap.toPx() - diameter else endX + marker.gap.toPx()
    val left = preferredLeft.coerceIn(markerBounds.start, markerBounds.endInclusive - diameter)
    // 使用正文栏目边界而非单个文本 run 的宽度；放不下时贴末字下方。
    val top = if (left == preferredLeft) baselineY - radius else baselineY
    drawCircle(
        color = marker.color,
        radius = radius,
        center = Offset(left + radius, top + radius),
    )
}
