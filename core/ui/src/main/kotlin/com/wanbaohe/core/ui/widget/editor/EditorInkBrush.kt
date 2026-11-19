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

import androidx.compose.ui.graphics.Color
import androidx.ink.brush.Brush
import androidx.ink.brush.BrushFamily
import androidx.ink.brush.SelfOverlap
import androidx.ink.brush.StockBrushes
import androidx.ink.brush.compose.createWithComposeColor
import com.shifenmiao.model.editor.BrushType

internal fun editorInkBrushFamily(type: BrushType): BrushFamily = when (type) {
    BrushType.Pencil, BrushType.Pen -> StockBrushes.pressurePen()
    BrushType.Brush -> StockBrushes.marker()
    BrushType.Marker -> StockBrushes.highlighter(selfOverlap = SelfOverlap.DISCARD)
    BrushType.Eraser -> StockBrushes.marker()
}

internal fun createEditorInkBrush(
    type: BrushType,
    colorInt: Int,
    widthPx: Float,
    alpha: Float,
): Brush = Brush.createWithComposeColor(
    family = editorInkBrushFamily(type),
    color = Color(colorInt).copy(alpha = alpha.coerceIn(0.05f, 1f)),
    size = widthPx.coerceAtLeast(2f),
    epsilon = 0.1f,
)
