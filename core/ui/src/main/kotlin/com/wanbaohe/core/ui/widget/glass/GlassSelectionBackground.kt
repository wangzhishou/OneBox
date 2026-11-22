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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanbaohe.core.ui.widget.glass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.glassSelectionBackground(
    selected: Boolean,
    style: GlassStyle,
    shape: Shape,
    color: Color,
    borderWidth: Dp = 0.9.dp,
    enabled: Boolean = true,
): Modifier = glassControlStyle(
    style = style,
    backgroundAlpha = glassSelectionBackgroundAlpha(style, selected),
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    enabled = enabled,
)

internal fun glassSelectionBackgroundAlpha(
    style: GlassStyle,
    selected: Boolean,
): Float = (style.backgroundAlpha + if (selected) 0.12f else 0.05f).coerceAtMost(1f)
