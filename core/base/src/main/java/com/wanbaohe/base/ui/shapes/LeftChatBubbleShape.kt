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

package com.wanbaohe.base.ui.shapes

import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class LeftChatBubbleShape(
    private val arrowSize: Dp = 4.dp,
    private val arrowPosition: Dp = 16.dp,
    private val cornerSize: Dp = 16.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arrowSizePx = with(density) { arrowSize.toPx() }
        val arrowPositionPx = with(density) { arrowPosition.toPx() }
        val cornerSizePx = cornerSize.value
        return Outline.Generic(Path().apply {
            // Draw a rectangle for the body of the bubble
            addRoundRect(
                roundRect = RoundRect(
                    left = arrowSizePx, // leave space for the arrow on the left
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    radiusX = cornerSizePx, // 使用计算出的圆角像素值
                    radiusY = cornerSizePx  // 使用计算出的圆角像素值
                )
            )

            // Draw an equilateral triangle for the arrow of the bubble on the left
            moveTo(arrowSizePx, arrowPositionPx)
            lineTo(0f, arrowPositionPx + arrowSizePx)
            lineTo(arrowSizePx, arrowPositionPx + arrowSizePx * 2)
            close()
        })
    }
}