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

package com.shifenmiao.model.editor

import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object ShapeGeometry {

    fun trianglePoints(width: Float, height: Float): List<Pair<Float, Float>> {
        val side = min(width, height * 2f / sqrt(3f))
        val triHeight = side * sqrt(3f) / 2f
        val left = (width - side) / 2f
        val top = (height - triHeight) / 2f
        return listOf(
            (width / 2f) to top,
            (left + side) to (top + triHeight),
            left to (top + triHeight)
        )
    }

    fun arrowPoints(width: Float, height: Float): List<Pair<Float, Float>> {
        val shaftX = width * 0.55f
        return listOf(
            0f to height * 0.3f,
            shaftX to height * 0.3f,
            shaftX to 0f,
            width to height / 2f,
            shaftX to height,
            shaftX to height * 0.7f,
            0f to height * 0.7f
        )
    }

    fun starPoints(width: Float, height: Float): List<Pair<Float, Float>> {
        val outerRadius = min(width, height) / 2f
        val innerRadius = outerRadius * 0.382f
        val centerX = width / 2f
        val centerY = height / 2f
        return (0 until 10).map { i ->
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = Math.toRadians((i * 36 - 90).toDouble())
            (centerX + radius * cos(angle).toFloat()) to (centerY + radius * sin(angle).toFloat())
        }
    }

    fun polygonPoints(
        kind: ShapeKind,
        width: Float,
        height: Float
    ): List<Pair<Float, Float>> = when (kind) {
        ShapeKind.Triangle -> trianglePoints(width, height)
        ShapeKind.Arrow -> arrowPoints(width, height)
        ShapeKind.Star -> starPoints(width, height)
        else -> emptyList()
    }
}
