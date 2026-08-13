package com.wanbaohe.markuplayers.domain.model

import kotlin.math.sin

/**
 * 笔画几何工具:预览(Compose Canvas)与导出(android.graphics.Canvas)共用,
 * 保证两侧笔迹一致。
 */
object DrawStrokeGeometry {

    /** 毛笔两端收尖的最小宽度系数 */
    const val MIN_TAPER_FACTOR = 0.25f

    /**
     * 把采样点平滑成较密折线:以相邻点中点为端点、采样点为控制点的
     * 二次贝塞尔,每条曲线细分为 [subdivisions] 段。供毛笔逐段变宽绘制。
     */
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

    /** 毛笔宽度系数:中段为 1,两端按 sin 曲线衰减到 [MIN_TAPER_FACTOR] */
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
