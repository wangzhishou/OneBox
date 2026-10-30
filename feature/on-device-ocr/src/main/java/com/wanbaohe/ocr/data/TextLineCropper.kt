package com.wanbaohe.ocr.data

import com.wanbaohe.ocr.domain.PpOcrSpec
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.geometry.Geometry
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.sqrt

internal object TextLineCropper {
    fun crop(source: Mat, corners: FloatArray): Mat? {
        if (corners.size != CORNER_STRIDE) return null
        val points = Array(CORNER_COUNT) { index ->
            Point(corners[index * 2].toDouble(), corners[index * 2 + 1].toDouble())
        }
        val width = max(span(points[0], points[1]), span(points[2], points[3])).toInt().coerceAtLeast(MIN_CROP)
        val height = max(span(points[0], points[3]), span(points[1], points[2])).toInt().coerceAtLeast(MIN_CROP)
        val src = MatOfPoint2f(*points)
        val dst = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(width.toDouble(), 0.0),
            Point(width.toDouble(), height.toDouble()),
            Point(0.0, height.toDouble()),
        )
        val transform = Geometry.getPerspectiveTransform(src, dst)
        val cropped = Mat()
        Imgproc.warpPerspective(
            source,
            cropped,
            transform,
            Size(width.toDouble(), height.toDouble()),
            Imgproc.INTER_CUBIC,
            Core.BORDER_REPLICATE,
            Scalar(0.0, 0.0, 0.0),
        )
        src.release()
        dst.release()
        transform.release()
        if (cropped.empty()) {
            cropped.release()
            return null
        }
        if (height.toFloat() / width < PpOcrSpec.VERTICAL_RATIO) return cropped
        val rotated = Mat()
        Core.rotate(cropped, rotated, Core.ROTATE_90_COUNTERCLOCKWISE)
        cropped.release()
        return rotated
    }

    private fun span(start: Point, end: Point): Double {
        val dx = start.x - end.x
        val dy = start.y - end.y
        return sqrt(dx * dx + dy * dy)
    }

    private const val CORNER_COUNT = 4
    private const val CORNER_STRIDE = 8
    private const val MIN_CROP = 2
}
