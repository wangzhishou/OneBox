package com.hrm.markdown.renderer.internal.selection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.hrm.markdown.renderer.LocalMarkdownStyle
import kotlin.math.roundToInt

internal const val SelectionStartHandleDescription = "Markdown selection start handle"
internal const val SelectionEndHandleDescription = "Markdown selection end handle"

private val HandleTouchSize = 40.dp
private val HandleRadius = 6.dp
private val HandleStemWidth = 2.dp

@Composable
internal fun SelectionHandlesHost(controller: MarkdownSelectionController) {
    val range = controller.state.range ?: return
    if (range.start == range.end) return
    val activeHandle = controller.state.activeHandle
    if (activeHandle != SelectionActiveHandle.None && !controller.state.isHandleDrag) return

    SelectionHandlePopup(controller, SelectionActiveHandle.Start)
    SelectionHandlePopup(controller, SelectionActiveHandle.End)
}

@Composable
private fun SelectionHandlePopup(
    controller: MarkdownSelectionController,
    handle: SelectionActiveHandle,
) {
    val position = controller.selectionHandlePositionInRoot(handle) ?: return
    val density = LocalDensity.current
    val touchSizePx = with(density) { HandleTouchSize.toPx() }
    val latestPosition by rememberUpdatedState(position.positionInRoot)
    val popupOffset = IntOffset(
        x = (position.positionInRoot.x - touchSizePx / 2f).roundToInt(),
        y = (position.positionInRoot.y - touchSizePx / 2f).roundToInt(),
    )
    val description = if (handle == SelectionActiveHandle.Start) {
        SelectionStartHandleDescription
    } else {
        SelectionEndHandleDescription
    }
    val color = LocalMarkdownStyle.current.colors.link

    Popup(
        alignment = Alignment.TopStart,
        offset = popupOffset,
        properties = PopupProperties(
            focusable = false,
            clippingEnabled = false,
        ),
    ) {
        Canvas(
            modifier = Modifier
                .size(HandleTouchSize)
                .semantics { contentDescription = description }
                .pointerInput(controller, handle) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var draggedPosition = latestPosition
                        controller.beginSelectionHandleDrag(handle)
                        down.consume()
                        try {
                            drag(down.id) { change ->
                                draggedPosition += change.positionChange()
                                controller.dragSelectionHandleToRootLocal(draggedPosition)
                                change.consume()
                            }
                        } finally {
                            controller.finishSelectionHandleDrag()
                        }
                    }
                }
        ) {
            val radiusPx = HandleRadius.toPx()
            val stemWidthPx = HandleStemWidth.toPx()
            val anchor = center
            drawLine(
                color = color,
                start = anchor,
                end = Offset(anchor.x, anchor.y + radiusPx),
                strokeWidth = stemWidthPx,
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = color,
                radius = radiusPx,
                center = Offset(anchor.x, anchor.y + radiusPx * 1.75f),
            )
        }
    }
}
