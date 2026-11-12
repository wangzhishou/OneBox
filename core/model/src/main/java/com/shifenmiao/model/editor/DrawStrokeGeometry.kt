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

import kotlin.math.sin

object DrawStrokeGeometry {

    const val MIN_TAPER_FACTOR = 0.25f

    fun smoothed(
        points: List<StrokePoint>,
        subdivisions: Int = 4
    ): List<StrokePoint> {
        if (points.size < 3) return points
        val result = ArrayList<StrokePoint>(points.size * subdivisions)
        var start = points.first()
        result += start
        for (i in 1 until points.size - 1) {
            val control = points[i]
            val end = StrokePoint(
                x = (points[i].x + points[i + 1].x) / 2f,
                y = (points[i].y + points[i + 1].y) / 2f
            )
            for (step in 1..subdivisions) {
                val t = step / subdivisions.toFloat()
                result += quadratic(start, control, end, t)
            }
            start = end
        }
        result += points.last()
        return result
    }

    fun taperFactor(index: Int, lastIndex: Int): Float {
        if (lastIndex <= 0) return 1f
        val t = index.toFloat() / lastIndex
        val curve = sin(Math.PI * t).toFloat()
        return MIN_TAPER_FACTOR + (1f - MIN_TAPER_FACTOR) * curve
    }

    private fun quadratic(
        start: StrokePoint,
        control: StrokePoint,
        end: StrokePoint,
        t: Float
    ): StrokePoint {
        val u = 1f - t
        return StrokePoint(
            x = u * u * start.x + 2 * u * t * control.x + t * t * end.x,
            y = u * u * start.y + 2 * u * t * control.y + t * t * end.y
        )
    }
}
