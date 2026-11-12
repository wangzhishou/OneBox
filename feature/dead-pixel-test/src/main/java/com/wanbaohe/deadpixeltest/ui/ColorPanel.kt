/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.deadpixeltest.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 全屏纯色面板
 *
 * - 动画平滑过渡到 [color]
 * - 点击 → [onTap]（切换下一色）
 * - 水平拖拽超过 [swipeThreshold] px → 左滑 [onSwipeLeft]，右滑 [onSwipeRight]
 */
@Composable
fun ColorPanel(
    color: Color,
    onTap: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 300),
        label = "panelColor"
    )

    // 累计水平拖拽距离
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 80f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedColor)
            .pointerInput(onTap) {
                detectTapGestures { onTap() }
            }
            .pointerInput(onSwipeLeft, onSwipeRight) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onHorizontalDrag = { _, delta -> dragAccumulator += delta },
                    onDragEnd = {
                        when {
                            dragAccumulator < -swipeThreshold -> onSwipeLeft()
                            dragAccumulator > swipeThreshold  -> onSwipeRight()
                        }
                        dragAccumulator = 0f
                    }
                )
            }
    )
}

