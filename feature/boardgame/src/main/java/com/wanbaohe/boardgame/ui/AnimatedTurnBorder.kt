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

package com.wanbaohe.boardgame.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.animatedTurnBorder(
    shape: Shape = RoundedCornerShape(14.dp),
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    width: Dp = 1.dp,
): Modifier {
    val transition = rememberInfiniteTransition(label = "Active player border")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "Border progress",
    )

    return drawWithCache {
        val strokeWidth = width.toPx()
        val outline = shape.createOutline(
            size = Size(
                (size.width - strokeWidth).coerceAtLeast(0f),
                (size.height - strokeWidth).coerceAtLeast(0f),
            ),
            layoutDirection = layoutDirection,
            density = this,
        )
        val border = Path().apply { addOutline(outline) }
        val measure = PathMeasure().apply { setPath(border, forceClosed = true) }
        val indicator = Path()
        val length = measure.length
        val indicatorLength = length * 0.25f

        onDrawWithContent {
            drawContent()
            withTransform({ translate(strokeWidth / 2, strokeWidth / 2) }) {
                drawPath(border, color.copy(alpha = color.alpha * 0.35f), style = Stroke(strokeWidth))
                val start = progress.value * length
                val end = start + indicatorLength
                indicator.reset()
                measure.getSegment(start, minOf(end, length), indicator)
                if (end > length) measure.getSegment(0f, end - length, indicator)
                drawPath(indicator, color, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            }
        }
    }
}
