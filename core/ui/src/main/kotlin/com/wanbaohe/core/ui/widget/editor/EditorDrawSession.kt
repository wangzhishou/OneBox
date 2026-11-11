package com.wanbaohe.core.ui.widget.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import com.shifenmiao.model.editor.BrushType
import com.shifenmiao.model.editor.DrawStroke
import com.shifenmiao.model.editor.StrokePoint

class EditorDrawSessionState {
    var strokes by mutableStateOf<List<DrawStroke>>(emptyList())
        private set
    var redoStack by mutableStateOf<List<DrawStroke>>(emptyList())
        private set
    var currentPoints by mutableStateOf<List<StrokePoint>>(emptyList())
        private set

    var brush by mutableStateOf(BrushType.Pencil)
    var colorInt by mutableStateOf(DEFAULT_COLOR)
    var widthPx by mutableStateOf(DEFAULT_WIDTH_PX)
    var alpha by mutableStateOf(1f)
    var isEraser by mutableStateOf(false)
    var isPanMode by mutableStateOf(false)

    val canUndo: Boolean get() = strokes.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    val effectiveBrush: BrushType get() = if (isEraser) BrushType.Eraser else brush

    fun selectBrush(type: BrushType) {
        brush = type
        isEraser = false
        isPanMode = false
    }

    fun selectColor(argb: Int) {
        colorInt = argb
        isEraser = false
        isPanMode = false
    }

    fun enableEraser() {
        isEraser = true
        isPanMode = false
    }

    fun enableBrush() {
        isEraser = false
        isPanMode = false
    }

    fun togglePanMode() {
        isPanMode = !isPanMode
    }

    fun beginStroke() {
        currentPoints = emptyList()
    }

    fun appendPoint(point: StrokePoint) {
        currentPoints = currentPoints + point
    }

    fun finishStroke(canvasWidthPx: Float) {
        val points = currentPoints
        currentPoints = emptyList()
        if (points.isEmpty() || canvasWidthPx <= 0f) return
        strokes = strokes + DrawStroke(
            points = points,
            color = colorInt,
            widthRatio = widthPx / canvasWidthPx,
            brush = effectiveBrush,
            alpha = alpha
        )
        redoStack = emptyList()
    }

    fun inProgressStroke(canvasWidthPx: Float): DrawStroke? {
        val points = currentPoints
        if (points.isEmpty() || canvasWidthPx <= 0f) return null
        return DrawStroke(
            points = points,
            color = colorInt,
            widthRatio = widthPx / canvasWidthPx,
            brush = effectiveBrush,
            alpha = alpha
        )
    }

    fun undo() {
        val last = strokes.lastOrNull() ?: return
        strokes = strokes.dropLast(1)
        redoStack = redoStack + last
    }

    fun redo() {
        val next = redoStack.lastOrNull() ?: return
        redoStack = redoStack.dropLast(1)
        strokes = strokes + next
    }

    private companion object {
        val DEFAULT_COLOR = 0xFF005FFF.toInt()
        const val DEFAULT_WIDTH_PX = 18f
    }
}

@Composable
fun EditorDrawOverlay(
    session: EditorDrawSessionState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    Canvas(
        modifier = modifier
            .size(
                width = with(density) { canvasWidthPx.toDp() },
                height = with(density) { canvasHeightPx.toDp() }
            )
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawGesture(session, canvasWidthPx, canvasHeightPx)
    ) {
        session.strokes.forEach { drawEditorStroke(it, size.width, size.height) }
        session.inProgressStroke(size.width)?.let {
            drawEditorStroke(it, size.width, size.height)
        }
    }
}

private fun Modifier.drawGesture(
    session: EditorDrawSessionState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
): Modifier = pointerInput(canvasWidthPx, canvasHeightPx, session.isPanMode) {
    if (session.isPanMode) return@pointerInput
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (down.isConsumed) return@awaitEachGesture
        session.beginStroke()
        session.appendPoint(down.position.normalized(canvasWidthPx, canvasHeightPx))
        try {
            while (true) {
                val event = awaitPointerEvent()
                if (event.changes.count { it.pressed } > 1) break
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                if (change.positionChanged()) {
                    session.appendPoint(change.position.normalized(canvasWidthPx, canvasHeightPx))
                    change.consume()
                }
            }
        } finally {
            session.finishStroke(canvasWidthPx)
        }
    }
}

private fun Offset.normalized(width: Float, height: Float) = StrokePoint(x / width, y / height)
