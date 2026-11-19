/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.core.ui.widget.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.ink.geometry.AffineTransform
import androidx.ink.geometry.MutableParallelogram
import androidx.ink.geometry.MutableSegment
import androidx.ink.geometry.MutableVec
import androidx.ink.strokes.Stroke
import com.shifenmiao.model.editor.BrushType
import com.shifenmiao.model.editor.DrawStroke

class EditorDrawSessionState {
    var strokes by mutableStateOf<List<DrawStroke>>(emptyList())
        private set
    internal var inkStrokes by mutableStateOf<List<Stroke>>(emptyList())
        private set

    var brush by mutableStateOf(BrushType.Pencil)
    var colorInt by mutableStateOf(DEFAULT_COLOR)
    var widthPx by mutableStateOf(DEFAULT_WIDTH_PX)
    var alpha by mutableStateOf(1f)
    var isEraser by mutableStateOf(false)
    var isPanMode by mutableStateOf(false)
    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set

    private var history = listOf(EditorDrawSnapshot())
    private var historyIndex = 0
    private var previousErasePoint: MutableVec? = null
    private var eraseChanged = false

    fun selectBrush(type: BrushType) {
        if (type == BrushType.Marker && brush != BrushType.Marker) {
            alpha = DEFAULT_HIGHLIGHTER_ALPHA
        } else if (type != BrushType.Marker && brush == BrushType.Marker) {
            alpha = 1f
        }
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

    internal fun currentInkBrush() = createEditorInkBrush(
        type = brush,
        colorInt = colorInt,
        widthPx = widthPx,
        alpha = alpha,
    )

    internal fun addStrokes(
        finishedStrokes: List<Stroke>,
        canvasWidthPx: Float,
        canvasHeightPx: Float,
    ) {
        val added = finishedStrokes.mapNotNull { stroke ->
            stroke.toEditorDrawStroke(brush, canvasWidthPx, canvasHeightPx)?.let { stroke to it }
        }
        if (added.isEmpty()) return
        commit(
            models = strokes + added.map { it.second },
            ink = inkStrokes + added.map { it.first },
        )
    }

    internal fun startErase() {
        previousErasePoint = null
        eraseChanged = false
    }

    internal fun erase(x: Float, y: Float) {
        if (inkStrokes.isEmpty()) return
        val currentPoint = MutableVec(x, y)
        val previousPoint = previousErasePoint
        val padding = (widthPx / 2f).coerceAtLeast(12f)
        val eraser = if (previousPoint == null) {
            MutableParallelogram().populateFromCenterAndDimensions(
                currentPoint,
                padding * 2f,
                padding * 2f,
            )
        } else {
            MutableParallelogram().populateFromSegmentAndPadding(
                MutableSegment(previousPoint, currentPoint),
                padding,
            )
        }
        previousErasePoint = currentPoint
        val retainedIndices = inkStrokes.indices.filterNot { index ->
            inkStrokes[index].shape.computeCoverageIsGreaterThan(
                eraser,
                0f,
                AffineTransform.IDENTITY,
            )
        }
        if (retainedIndices.size == inkStrokes.size) return
        strokes = retainedIndices.map(strokes::get)
        inkStrokes = retainedIndices.map(inkStrokes::get)
        eraseChanged = true
    }

    internal fun endErase() {
        previousErasePoint = null
        if (eraseChanged) {
            eraseChanged = false
            commit(strokes, inkStrokes)
        }
    }

    fun undo() {
        if (!canUndo) return
        historyIndex--
        applyHistory()
    }

    fun redo() {
        if (!canRedo) return
        historyIndex++
        applyHistory()
    }

    private fun commit(models: List<DrawStroke>, ink: List<Stroke>) {
        history = history.take(historyIndex + 1) + EditorDrawSnapshot(models, ink)
        if (history.size > MAX_HISTORY_SIZE) {
            history = history.drop(1)
        } else {
            historyIndex++
        }
        applyHistory()
    }

    private fun applyHistory() {
        val snapshot = history[historyIndex]
        strokes = snapshot.models
        inkStrokes = snapshot.ink
        canUndo = historyIndex > 0
        canRedo = historyIndex < history.lastIndex
        previousErasePoint = null
    }

    private data class EditorDrawSnapshot(
        val models: List<DrawStroke> = emptyList(),
        val ink: List<Stroke> = emptyList(),
    )

    private companion object {
        val DEFAULT_COLOR = 0xFF005FFF.toInt()
        const val DEFAULT_WIDTH_PX = 18f
        const val DEFAULT_HIGHLIGHTER_ALPHA = 0.35f
        const val MAX_HISTORY_SIZE = 50
    }
}
