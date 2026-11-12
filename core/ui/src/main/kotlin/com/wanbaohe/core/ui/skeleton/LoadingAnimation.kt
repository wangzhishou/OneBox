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

package com.wanbaohe.core.ui.skeleton

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.shifenmiao.theme.AppTheme

@Composable
fun Modifier.shimmerLoading(
    isActive: Boolean = true,
    isLightMode: Boolean = !isSystemInDarkTheme(),
    widthOfShadowBrush: Int = 500,
    durationMillis: Int = 1500
): Modifier {
    if (!isActive) {
        return this
    } else {
        return composed {
            val shimmerColors = remember { getColors(isLightMode) }
            val transition = rememberInfiniteTransition(label = "")
            val offsetX by transition.animateFloat(
                initialValue = 0f,
                targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ShimmerLoadingAnimation"
            )

            this.background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(x = offsetX - widthOfShadowBrush, y = 0f),
                    end = Offset(x = offsetX, y = 0f)
                )
            )
        }
    }
}

private fun getColors(isLightMode: Boolean): List<Color> {
    return if (isLightMode) {
        val color = AppTheme.colorScheme.surfaceContainerLow
        listOf(
            color.copy(0.4f),
            color.copy(1f),
            color.copy(0.4f)
        )
    } else {
        val color = AppTheme.colorScheme.surfaceContainer
        listOf(
            color.copy(0.0f),
            color.copy(0.3f),
            color.copy(0.5f),
            color.copy(0.3f),
            color.copy(0.0f),
        )
    }
}