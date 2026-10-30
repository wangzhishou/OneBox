package com.wanbaohe.ocr.data

import com.wanbaohe.ocr.domain.PpOcrSpec
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.geometry.Geometry
import org.opencv.imgproc.Imgproc
import kotlin.math.max
import kotlin.math.sqrt

internal data class DetectedBox(
    val corners: FloatArray,
)

internal object DetectionDecoder {
    fun boxes(
        probability: FloatArray,
        mapWidth: Int,
        mapHeight: Int,
        scaleX: Float,
        scaleY: Float,
        imageWidth: Int,
        imageHeight: Int,
    ): List<DetectedBox> {
        val prob = Mat(mapHeight, mapWidth, CvType.CV_32FC1)
        prob.put(0, 0, probability)
        val binary = Mat()
        val binary8 = Mat()
        val hierarchy = Mat()
        val contours = ArrayList<MatOfPoint>()
        return try {
            Imgproc.threshold(prob, binary, PpOcrSpec.BIN_THRESH, FILLED, Imgproc.THRESH_BINARY)
            binary.convertTo(binary8, CvType.CV_8UC1)
            Imgproc.findContours(
                binary8,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE,
            )
            contours.mapNotNull { contour ->
                toBox(contour, prob, scaleX, scaleY, imageWidth, imageHeight)
            }.sortedByDescending { scoreOf(it) }.take(PpOcrSpec.MAX_BOXES)
        } finally {
            contours.forEach { it.release() }
            hierarchy.release()
            binary8.release()
            binary.release()
            prob.release()
        }
    }

    private fun toBox(
        contour: MatOfPoint,
        probability: Mat,
        scaleX: Float,
        scaleY: Float,
        imageWidth: Int,
        imageHeight: Int,
    ): DetectedBox? {
        val contour2f = MatOfPoint2f(*contour.toArray())
        if (Geometry.contourArea(contour) < PpOcrSpec.MIN_CONTOUR_AREA) {
            contour2f.release()
            return null
        }
        val mask = Mat.zeros(probability.size(), CvType.CV_8UC1)
        Imgproc.drawContours(mask, listOf(contour), DRAW_ALL, Scalar(FILLED), Imgproc.FILLED)
        val score = Core.mean(probability, mask).`val`[0]
        mask.release()
        if (score < PpOcrSpec.BOX_THRESH) {
            contour2f.release()
            return null
        }
        val expanded = unclip(contour, probability.rows(), probability.cols()) ?: contour2f
        val ownedExpanded = expanded !== contour2f
        val rect = Geometry.minAreaRect(expanded)
        val box = MatOfPoint2f()
        Geometry.boxPoints(rect, box)
        val ordered = orderClockwise(box.toArray())
        box.release()
        if (ownedExpanded) expanded.release()
        contour2f.release()
        if (ordered == null) return null
        val corners = FloatArray(ordered.size * 2)
        ordered.forEachIndexed { index, point ->
            val x = (point.x / scaleX).coerceIn(0.0, (imageWidth - 1).toDouble())
            val y = (point.y / scaleY).coerceIn(0.0, (imageHeight - 1).toDouble())
            corners[index * 2] = x.toFloat()
            corners[index * 2 + 1] = y.toFloat()
        }
        return DetectedBox(corners)
    }

    private fun unclip(contour: MatOfPoint, rows: Int, cols: Int): MatOfPoint2f? {
        val contour2f = MatOfPoint2f(*contour.toArray())
        val area = Geometry.contourArea(contour)
        val perimeter = Geometry.arcLength(contour2f, true)
        contour2f.release()
        if (perimeter <= 0.0) return null
        val distance = ((area * PpOcrSpec.UNCLIP_RATIO) / perimeter).toInt()
            .coerceIn(1, PpOcrSpec.MAX_UNCLIP)
        val kernelSize = distance * 2 + 1
        val mask = Mat.zeros(rows, cols, CvType.CV_8UC1)
        Imgproc.drawContours(mask, listOf(contour), DRAW_ALL, Scalar(FILLED), Imgproc.FILLED)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(kernelSize.toDouble(), kernelSize.toDouble()))
        val dilated = Mat()
        Imgproc.dilate(mask, dilated, kernel)
        val found = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(dilated, found, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        val largest = found.maxByOrNull { Geometry.contourArea(it) }
        val result = largest?.let { MatOfPoint2f(*it.toArray()) }
        found.forEach { it.release() }
        hierarchy.release()
        dilated.release()
        kernel.release()
        mask.release()
        return result
    }

    private fun orderClockwise(points: Array<Point>): List<Point>? {
        if (points.size < 4) return null
        val sum = points.map { it.x + it.y }
        val diff = points.map { it.y - it.x }
        val topLeft = points[sum.indices.minBy { sum[it] }]
        val bottomRight = points[sum.indices.maxBy { sum[it] }]
        val topRight = points[diff.indices.minBy { diff[it] }]
        val bottomLeft = points[diff.indices.maxBy { diff[it] }]
        return listOf(topLeft, topRight, bottomRight, bottomLeft)
    }

    private fun scoreOf(box: DetectedBox): Float {
        val width = distance(box.corners, 0, 1)
        val height = distance(box.corners, 0, 3)
        return width * height
    }

    private fun distance(corners: FloatArray, from: Int, to: Int): Float {
        val dx = corners[from * 2] - corners[to * 2]
        val dy = corners[from * 2 + 1] - corners[to * 2 + 1]
        return sqrt(dx * dx + dy * dy)
    }

    private const val FILLED = 255.0
    private const val DRAW_ALL = -1
}

internal fun maxSide(width: Int, height: Int): Int = max(width, height)
