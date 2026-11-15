package com.wanbaohe.richtext.editor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange

@Stable
class RichTextSelectionAnchorTracker {
    var anchorBounds by mutableStateOf(Rect.Zero)
        private set

    private var layoutResult: TextLayoutResult? = null
    private var coordinates: LayoutCoordinates? = null

    fun updateLayout(
        result: TextLayoutResult,
        coordinates: LayoutCoordinates?,
        selection: TextRange,
        scrollOffset: Int,
    ) {
        layoutResult = result
        this.coordinates = coordinates
        update(selection, scrollOffset)
    }

    fun updateCoordinates(
        coordinates: LayoutCoordinates,
        selection: TextRange,
        scrollOffset: Int,
    ) {
        this.coordinates = coordinates
        update(selection, scrollOffset)
    }

    fun update(selection: TextRange, scrollOffset: Int) {
        val result = layoutResult
        val coordinates = coordinates?.takeIf { it.isAttached }
        if (selection.collapsed) {
            anchorBounds = Rect.Zero
            return
        }
        if (result == null || coordinates == null || selection.max > result.layoutInput.text.length) {
            anchorBounds = Rect.Zero
            return
        }
        val pathBounds = result.getPathForRange(selection.min, selection.max).getBounds()
        val origin = coordinates.localToWindow(Offset.Zero)
        anchorBounds = pathBounds.translate(origin.x, origin.y - scrollOffset)
    }
}

@Composable
fun rememberRichTextSelectionAnchorTracker(): RichTextSelectionAnchorTracker = remember {
    RichTextSelectionAnchorTracker()
}
