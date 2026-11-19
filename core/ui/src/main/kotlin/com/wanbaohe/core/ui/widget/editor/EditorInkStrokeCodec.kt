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

import android.util.Base64
import androidx.ink.brush.compose.composeColor
import androidx.ink.storage.decode
import androidx.ink.storage.encode
import androidx.ink.strokes.Stroke
import androidx.ink.strokes.StrokeInputBatch
import com.shifenmiao.model.editor.BrushType
import com.shifenmiao.model.editor.DrawStroke
import com.shifenmiao.model.editor.InkStrokeSnapshot
import com.shifenmiao.model.editor.StrokePoint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

internal fun Stroke.toEditorDrawStroke(
    type: BrushType,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
): DrawStroke? {
    if (inputs.isEmpty() || canvasWidthPx <= 0f || canvasHeightPx <= 0f) return null
    val encodedInputs = runCatching {
        ByteArrayOutputStream().use { output ->
            inputs.encode(output)
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
    }.getOrNull()
    val snapshot = encodedInputs?.let {
        InkStrokeSnapshot(
            encodedInputs = it,
            brushSize = brush.size,
            epsilon = brush.epsilon,
            canvasWidth = canvasWidthPx,
            canvasHeight = canvasHeightPx,
        )
    }
    return DrawStroke(
        points = List(inputs.size) { index ->
            val input = inputs[index]
            StrokePoint(
                x = input.x / canvasWidthPx,
                y = input.y / canvasHeightPx,
            )
        },
        color = brush.colorIntArgb,
        widthRatio = brush.size / canvasWidthPx,
        brush = type,
        alpha = brush.composeColor.alpha,
        ink = snapshot,
    )
}

internal fun DrawStroke.toInkStrokeOrNull(): Stroke? {
    val snapshot = ink ?: return null
    if (brush == BrushType.Eraser) return null
    return runCatching {
        val inputBytes = Base64.decode(snapshot.encodedInputs, Base64.NO_WRAP)
        val inputs = ByteArrayInputStream(inputBytes).use { input ->
            StrokeInputBatch.decode(input)
        }
        Stroke(
            brush = createEditorInkBrush(
                type = brush,
                colorInt = color,
                widthPx = snapshot.brushSize,
                alpha = alpha,
            ).copy(epsilon = snapshot.epsilon),
            inputs = inputs,
        )
    }.getOrNull()
}
