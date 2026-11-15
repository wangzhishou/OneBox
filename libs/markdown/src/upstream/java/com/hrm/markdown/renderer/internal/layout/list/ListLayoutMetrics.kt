package com.hrm.markdown.renderer.internal.layout.list

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hrm.markdown.renderer.ResolvedMarkdownStyle
import kotlin.math.ceil

internal val UnorderedListMarkerWidth: Dp = 16.dp
internal val OrderedListMarkerWidth: Dp = 24.dp
internal val TaskListCheckboxSize: Dp = 20.dp
internal val TaskListMarkerGap: Dp = 8.dp
internal val OrderedListMarkerGap: Dp = 4.dp

internal fun orderedListMarkerDigitCount(startNumber: Int, itemCount: Int): Int = maxOf(
    startNumber.toString().length,
    (startNumber.toLong() + (itemCount - 1).coerceAtLeast(0)).toString().length,
)

internal data class OrderedListMarkerMetrics(
    val digitWidthPx: Float,
    val periodWidthPx: Float,
) {
    fun widthPx(digitCount: Int, density: Density): Float = with(density) {
        ceil(maxOf(
            OrderedListMarkerWidth.toPx(),
            digitWidthPx * digitCount + periodWidthPx + OrderedListMarkerGap.toPx(),
        ))
    }
}

/** One short text layout, shared by all ordered lists in the layout environment. */
internal fun measureOrderedListMarkerMetrics(
    style: TextStyle,
    density: Density,
    textMeasurer: TextMeasurer,
): OrderedListMarkerMetrics {
    val result = textMeasurer.measure(
        text = "0123456789.",
        style = style,
        constraints = Constraints(maxWidth = Constraints.Infinity),
        maxLines = 1,
        softWrap = false,
        density = density,
    )
    return OrderedListMarkerMetrics(
        digitWidthPx = (0..9).maxOf { ceil(result.getBoundingBox(it).width) },
        periodWidthPx = ceil(result.getBoundingBox(10).width),
    )
}

internal fun listMarkerWidth(
    taskListItem: Boolean,
    ordered: Boolean,
    orderedMarkerWidth: Dp = OrderedListMarkerWidth,
): Dp = when {
    taskListItem -> TaskListCheckboxSize + TaskListMarkerGap
    ordered -> orderedMarkerWidth
    else -> UnorderedListMarkerWidth
}

internal fun Density.listItemContentIndentPx(
    theme: ResolvedMarkdownStyle,
    taskListItem: Boolean,
    ordered: Boolean,
    orderedMarkerWidth: Dp = OrderedListMarkerWidth,
): Float = theme.layout.listIndent.toPx() + listMarkerWidth(
    taskListItem = taskListItem,
    ordered = ordered,
    orderedMarkerWidth = orderedMarkerWidth,
).toPx()
