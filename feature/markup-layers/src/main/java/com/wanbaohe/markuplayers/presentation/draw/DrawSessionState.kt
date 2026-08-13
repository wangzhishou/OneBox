package com.wanbaohe.markuplayers.presentation.draw

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wanbaohe.markuplayers.domain.model.BrushType
import com.wanbaohe.markuplayers.domain.model.DrawStroke
import com.wanbaohe.markuplayers.domain.model.StrokePoint

/**
 * 绘画会话状态:只在画笔页内 remember,完成时才落成 Draw 图层,不进 Component。
 * 撤销/重做是笔画级的会话内历史。
 */
internal class DrawSessionState {

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

    /** 生效画笔:橡皮擦优先于当前画笔类型 */
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

    /** 收笔:结算当前笔画(屏幕 px 宽按画布宽换算为 widthRatio),并清空重做栈 */
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

    /** 进行中的笔画(用于画布实时预览),无笔画时返回 null */
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
        /** 默认颜色:预设色板(ColorSelectionRowDefaults)中的蓝色 */
        val DEFAULT_COLOR = 0xFF005FFF.toInt()

        const val DEFAULT_WIDTH_PX = 18f
    }
}
