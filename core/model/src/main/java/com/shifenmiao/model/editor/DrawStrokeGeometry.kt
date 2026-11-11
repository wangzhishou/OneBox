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
