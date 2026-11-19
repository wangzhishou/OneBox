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

import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.graphics.withSaveLayer
import androidx.core.graphics.withSave
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.Stroke
import com.shifenmiao.model.editor.BrushType

internal class EditorInkStrokeRenderer {
    private val renderer = CanvasStrokeRenderer.create()
    private val srcOverPaint = Paint().apply { blendMode = BlendMode.SrcOver }
    private val multiplyPaint = Paint().apply { blendMode = BlendMode.Multiply }
    private val nativeMultiplyPaint = android.graphics.Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
    }

    fun draw(
        scope: DrawScope,
        stroke: Stroke,
        transform: android.graphics.Matrix,
    ) {
        val paint = if (stroke.isHighlighter()) multiplyPaint else srcOverPaint
        val nativeCanvas = scope.drawContext.canvas.nativeCanvas
        scope.drawContext.canvas.withSaveLayer(scope.drawContext.size.toRect(), paint) {
            nativeCanvas.withSave {
                renderer.draw(
                    canvas = this,
                    stroke = stroke,
                    strokeToScreenTransform = transform,
                )
            }
        }
    }

    fun draw(
        canvas: android.graphics.Canvas,
        stroke: Stroke,
        transform: android.graphics.Matrix,
        width: Float,
        height: Float,
    ) {
        if (stroke.isHighlighter()) {
            val saveCount = canvas.saveLayer(0f, 0f, width, height, nativeMultiplyPaint)
            renderer.draw(canvas, stroke, transform)
            canvas.restoreToCount(saveCount)
        } else {
            canvas.withSave {
                renderer.draw(this, stroke, transform)
            }
        }
    }

    private fun Stroke.isHighlighter(): Boolean =
        brush.family == editorInkBrushFamily(BrushType.Marker)
}
