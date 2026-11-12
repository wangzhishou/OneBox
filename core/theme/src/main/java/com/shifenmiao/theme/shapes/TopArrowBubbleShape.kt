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

package com.shifenmiao.theme.shapes

import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class TopArrowBubbleShape(
    private val arrowSize: Dp = 24.dp,
    private val arrowOffset: Dp = 64.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arrowSizePx = arrowSize.value
        val arrowOffsetPx = arrowOffset.value
        return Outline.Generic(Path().apply {
            // Draw a rectangle for the body of the bubble
            addRoundRect(
                roundRect = RoundRect(
                    left = 0f,
                    top = arrowSizePx, // leave space for the arrow at the top
                    right = size.width,
                    bottom = size.height,
                    radiusX = arrowSizePx,
                    radiusY = arrowSizePx
                )
            )

            // Draw a triangle for the arrow of the bubble at the top
            moveTo(size.width - arrowOffsetPx - arrowSizePx, arrowSizePx)
            lineTo(size.width - arrowOffsetPx, 0f)
            lineTo(size.width - arrowOffsetPx + arrowSizePx, arrowSizePx)
            close()
        })
    }
}