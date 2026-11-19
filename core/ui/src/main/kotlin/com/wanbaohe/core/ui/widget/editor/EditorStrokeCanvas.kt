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

import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.ink.strokes.Stroke
import com.shifenmiao.model.editor.DrawStroke

@Composable
fun EditorStrokeCanvas(
    strokes: List<DrawStroke>,
    modifier: Modifier = Modifier,
) {
    val renderer = remember { EditorInkStrokeRenderer() }
    val inkStrokes = remember(strokes) { strokes.map { it.toInkStrokeOrNull() } }
    val transform = remember { Matrix() }
    Canvas(modifier = modifier) {
        strokes.forEachIndexed { index, stroke ->
            val inkStroke = inkStrokes[index]
            val snapshot = stroke.ink
            if (inkStroke == null || snapshot == null) {
                drawLegacyEditorStroke(stroke, size.width, size.height)
            } else {
                transform.setScale(
                    size.width / snapshot.canvasWidth,
                    size.height / snapshot.canvasHeight,
                )
                renderer.draw(this, inkStroke, transform)
            }
        }
    }
}

@Composable
internal fun EditorInkStrokeCanvas(
    strokes: List<Stroke>,
    modifier: Modifier = Modifier,
) {
    val renderer = remember { EditorInkStrokeRenderer() }
    val transform = remember { Matrix() }
    Canvas(modifier = modifier) {
        strokes.forEach { stroke -> renderer.draw(this, stroke, transform) }
    }
}
