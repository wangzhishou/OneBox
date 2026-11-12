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
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

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
        val closed = Mat()
        val hierarchy = Mat()
        val contours = ArrayList<MatOfPoint>()
        return try {
            Imgproc.threshold(prob, binary, PpOcrSpec.BIN_THRESH, FILLED, Imgproc.THRESH_BINARY)
            binary.convertTo(binary8, CvType.CV_8UC1)
            val closeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(CLOSE_SIZE, CLOSE_SIZE))
            Imgproc.morphologyEx(binary8, closed, Imgproc.MORPH_CLOSE, closeKernel)
            closeKernel.release()
            Imgproc.findContours(
                closed,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE,
            )
            contours.mapNotNull { contour ->
                toBox(contour, prob, scaleX, scaleY, imageWidth, imageHeight)
            }.sortedByDescending { scoreOf(it) }.take(PpOcrSpec.MAX_BOXES)
        } finally {
            contours.forEach { it.release() }
            closed.release()
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
        val rect = Geometry.minAreaRect(contour2f)
        val box = Array(CORNER_COUNT) { Point() }
        rect.points(box)
        val ordered = orderClockwise(box)
        contour2f.release()
        if (ordered == null) return null
        val shortSide = min(span(ordered[0], ordered[1]), span(ordered[0], ordered[3]))
        val longSide = max(span(ordered[0], ordered[1]), span(ordered[0], ordered[3]))
        if (shortSide < MIN_SHORT_SIDE || longSide < MIN_LONG_SIDE) return null
        val expanded = expandQuad(ordered, unclipDistance(ordered, shortSide))
        val corners = FloatArray(expanded.size * 2)
        expanded.forEachIndexed { index, point ->
            val x = (point.x / scaleX).coerceIn(0.0, (imageWidth - 1).toDouble())
            val y = (point.y / scaleY).coerceIn(0.0, (imageHeight - 1).toDouble())
            corners[index * 2] = x.toFloat()
            corners[index * 2 + 1] = y.toFloat()
        }
        return DetectedBox(corners)
    }

    private fun unclipDistance(points: List<Point>, shortSide: Double): Double {
        val area = abs(polygonArea(points))
        val perimeter = (0 until points.size).sumOf { index ->
            span(points[index], points[(index + 1) % points.size])
        }
        if (perimeter <= 0.0) return 1.0
        return (area * PpOcrSpec.UNCLIP_RATIO / perimeter)
            .coerceIn(1.0, shortSide * MAX_UNCLIP_OF_SHORT)
            .coerceAtMost(PpOcrSpec.MAX_UNCLIP.toDouble())
    }

    private fun expandQuad(points: List<Point>, distance: Double): List<Point> {
        val count = points.size
        return List(count) { index ->
            val previous = points[(index + count - 1) % count]
            val current = points[index]
            val next = points[(index + 1) % count]
            val first = outward(current.x - previous.x, current.y - previous.y)
            val second = outward(next.x - current.x, next.y - current.y)
            val bisectorX = first.x + second.x
            val bisectorY = first.y + second.y
            val lengthSquared = bisectorX * bisectorX + bisectorY * bisectorY
            if (lengthSquared < 1e-6) {
                current
            } else {
                val scale = 2.0 * distance / lengthSquared
                Point(current.x + bisectorX * scale, current.y + bisectorY * scale)
            }
        }
    }

    private fun outward(dx: Double, dy: Double): Point {
        val length = hypot(dx, dy).coerceAtLeast(1e-6)
        return Point(dy / length, -dx / length)
    }

    private fun polygonArea(points: List<Point>): Double {
        var area = 0.0
        for (index in points.indices) {
            val next = points[(index + 1) % points.size]
            area += points[index].x * next.y - next.x * points[index].y
        }
        return area / 2.0
    }

    private fun span(start: Point, end: Point): Double {
        return hypot(start.x - end.x, start.y - end.y)
    }

    private fun orderClockwise(points: Array<Point>): List<Point>? {
        if (points.size < 4) return null
        val centerX = points.map { it.x }.average()
        val centerY = points.map { it.y }.average()
        val clockwise = points.sortedBy { point ->
            kotlin.math.atan2(point.y - centerY, point.x - centerX)
        }
        val start = clockwise.indices.minWith(
            compareBy(
                { index -> clockwise[index].x + clockwise[index].y },
                { index -> clockwise[index].y },
            ),
        )
        return List(CORNER_COUNT) { offset -> clockwise[(start + offset) % CORNER_COUNT] }
    }

    private fun scoreOf(box: DetectedBox): Float {
        val width = distance(box.corners, 0, 1)
        val height = distance(box.corners, 0, 3)
        return width * height
    }

    private fun distance(corners: FloatArray, from: Int, to: Int): Float {
        val dx = corners[from * 2] - corners[to * 2]
        val dy = corners[from * 2 + 1] - corners[to * 2 + 1]
        return hypot(dx, dy)
    }

    private const val FILLED = 255.0
    private const val DRAW_ALL = -1
    private const val CLOSE_SIZE = 3.0
    private const val MIN_SHORT_SIDE = 6.0
    private const val MIN_LONG_SIDE = 10.0
    private const val MAX_UNCLIP_OF_SHORT = 0.45
    private const val CORNER_COUNT = 4
}

internal fun maxSide(width: Int, height: Int): Int = max(width, height)
