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

import android.graphics.Bitmap
import com.wanbaohe.ocr.domain.PixelPoint
import com.wanbaohe.ocr.domain.PpOcrSpec
import com.wanbaohe.ocr.domain.RecognizedLine
import com.wanbaohe.ocr.domain.orderReadingLines
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.max
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession

@Singleton
class PpOcrV6Engine @Inject constructor(
    private val modelStore: OcrModelStore,
) {
    private val environment = OrtEnvironment.getEnvironment()
    private val lock = Any()
    private var openCvLoaded = false
    private var detection: OrtSession? = null
    private var recognition: OrtSession? = null
    private var orientation: OrtSession? = null
    private var labels: List<String> = emptyList()

    fun recognize(bitmap: Bitmap): List<RecognizedLine> {
        ensureOpenCv()
        val sessions = openSessions()
        val argb = if (bitmap.config == Bitmap.Config.ARGB_8888) {
            bitmap
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        }
        val rgba = Mat()
        val bgr = Mat()
        Utils.bitmapToMat(argb, rgba)
        Imgproc.cvtColor(rgba, bgr, Imgproc.COLOR_RGBA2BGR)
        rgba.release()
        if (argb !== bitmap) argb.recycle()
        return try {
            val found = detect(sessions.detection, bgr)
            val lines = found.mapNotNull { box -> readLine(sessions, bgr, box.corners) }
            orderReadingLines(lines)
        } finally {
            bgr.release()
        }
    }

    private fun detect(session: OrtSession, bgr: Mat): List<DetectedBox> {
        val limit = PpOcrSpec.DET_LIMIT
        val ratio = if (maxSide(bgr.cols(), bgr.rows()) > limit) {
            limit.toDouble() / maxSide(bgr.cols(), bgr.rows())
        } else {
            1.0
        }
        val resizedWidth = roundToAlign(bgr.cols() * ratio, PpOcrSpec.ALIGN)
        val resizedHeight = roundToAlign(bgr.rows() * ratio, PpOcrSpec.ALIGN)
        val resized = Mat()
        Imgproc.resize(bgr, resized, Size(resizedWidth.toDouble(), resizedHeight.toDouble()))
        val tensor = resized.toImageNetTensor()
        resized.release()
        val output = run(session, tensor, longArrayOf(1, CHANNELS, resizedHeight.toLong(), resizedWidth.toLong()))
        val scaleX = resizedWidth.toFloat() / bgr.cols()
        val scaleY = resizedHeight.toFloat() / bgr.rows()
        return DetectionDecoder.boxes(
            probability = output,
            mapWidth = resizedWidth,
            mapHeight = resizedHeight,
            scaleX = scaleX,
            scaleY = scaleY,
            imageWidth = bgr.cols(),
            imageHeight = bgr.rows(),
        )
    }

    private fun readLine(sessions: Sessions, source: Mat, corners: FloatArray): RecognizedLine? {
        val crop = TextLineCropper.crop(source, corners) ?: return null
        return try {
            val oriented = correctOrientation(sessions.orientation, crop)
            try {
                val (text, confidence) = recognizeText(sessions.recognition, oriented)
                if (text.isBlank() || confidence < PpOcrSpec.MIN_CONFIDENCE) {
                    null
                } else {
                    RecognizedLine(
                        text = text,
                        confidence = confidence,
                        corners = List(CORNER_COUNT) { index ->
                            PixelPoint(corners[index * 2], corners[index * 2 + 1])
                        },
                    )
                }
            } finally {
                if (oriented !== crop) oriented.release()
            }
        } finally {
            crop.release()
        }
    }

    private fun correctOrientation(session: OrtSession, crop: Mat): Mat {
        val resized = Mat()
        Imgproc.resize(
            crop,
            resized,
            Size(PpOcrSpec.ORI_WIDTH.toDouble(), PpOcrSpec.ORI_HEIGHT.toDouble()),
        )
        val scores = probabilities(
            run(
                session,
                resized.toImageNetTensor(),
                longArrayOf(1, CHANNELS, PpOcrSpec.ORI_HEIGHT.toLong(), PpOcrSpec.ORI_WIDTH.toLong()),
            ),
        )
        resized.release()
        val flip = scores.size > PpOcrSpec.ORI_FLIP_INDEX &&
            scores[PpOcrSpec.ORI_FLIP_INDEX] > PpOcrSpec.ORI_FLIP_THRESH &&
            scores[PpOcrSpec.ORI_FLIP_INDEX] > scores[0]
        if (!flip) return crop
        val rotated = Mat()
        Core.rotate(crop, rotated, Core.ROTATE_180)
        return rotated
    }

    private fun recognizeText(session: OrtSession, crop: Mat): Pair<String, Float> {
        val width = ceil(PpOcrSpec.REC_HEIGHT * crop.cols() / crop.rows().toFloat()).toInt()
            .coerceIn(PpOcrSpec.REC_MIN_WIDTH, PpOcrSpec.REC_MAX_WIDTH)
        val resized = Mat()
        Imgproc.resize(crop, resized, Size(width.toDouble(), PpOcrSpec.REC_HEIGHT.toDouble()))
        val output = run(
            session,
            resized.toRecognitionTensor(),
            longArrayOf(1, CHANNELS, PpOcrSpec.REC_HEIGHT.toLong(), width.toLong()),
        )
        resized.release()
        return decodeCtc(output, labels)
    }

    private fun ensureOpenCv() {
        if (openCvLoaded) return
        synchronized(lock) {
            if (openCvLoaded) return
            check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
            openCvLoaded = true
        }
    }

    private fun openSessions(): Sessions {
        synchronized(lock) {
            val readyDetection = detection
            val readyRecognition = recognition
            val readyOrientation = orientation
            if (readyDetection != null && readyRecognition != null && readyOrientation != null && labels.isNotEmpty()) {
                return Sessions(readyDetection, readyRecognition, readyOrientation)
            }
            labels = loadLabels()
            detection = createSession(PpOcrSpec.DET_FILE)
            recognition = createSession(PpOcrSpec.REC_FILE)
            orientation = createSession(PpOcrSpec.ORI_FILE)
            return Sessions(
                detection ?: error("detection session missing"),
                recognition ?: error("recognition session missing"),
                orientation ?: error("orientation session missing"),
            )
        }
    }

    private fun createSession(fileName: String): OrtSession {
        return OrtSession.SessionOptions().use { options ->
            options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            options.setIntraOpNumThreads(INTRA_OP_THREADS)
            environment.createSession(modelStore.file(fileName).absolutePath, options)
        }
    }

    private fun loadLabels(): List<String> {
        val lines = modelStore.file(PpOcrSpec.DICT_FILE).readLines()
            .map { it.removeSuffix("\r") }
        return lines + " "
    }

    private fun run(session: OrtSession, data: FloatArray, shape: LongArray): FloatArray {
        val buffer = FloatBuffer.wrap(data)
        OnnxTensor.createTensor(environment, buffer, shape).use { tensor ->
            session.run(mapOf(PpOcrSpec.INPUT_NAME to tensor)).use { result ->
                val output = result.get(0) as OnnxTensor
                val floats = output.floatBuffer
                return FloatArray(floats.remaining()).also { floats.get(it) }
            }
        }
    }

    private data class Sessions(
        val detection: OrtSession,
        val recognition: OrtSession,
        val orientation: OrtSession,
    )

    private companion object {
        const val CHANNELS = 3L
        const val INTRA_OP_THREADS = 2
        const val CORNER_COUNT = 4
    }
}

private fun Mat.toImageNetTensor(): FloatArray = fillChw { channel, value ->
    val mean = when (channel) {
        0 -> PpOcrSpec.MEAN_B
        1 -> PpOcrSpec.MEAN_G
        else -> PpOcrSpec.MEAN_R
    }
    val std = when (channel) {
        0 -> PpOcrSpec.STD_B
        1 -> PpOcrSpec.STD_G
        else -> PpOcrSpec.STD_R
    }
    (value - mean) / std
}

private fun Mat.toRecognitionTensor(): FloatArray = fillChw { _, value ->
    (value - PpOcrSpec.REC_SHIFT) / PpOcrSpec.REC_SHIFT
}

private fun Mat.fillChw(normalize: (channel: Int, value: Float) -> Float): FloatArray {
    val height = rows()
    val width = cols()
    val plane = height * width
    val data = FloatArray(CHANNELS_INT * plane)
    val row = ByteArray(width * CHANNELS_INT)
    for (y in 0 until height) {
        get(y, 0, row)
        for (x in 0 until width) {
            val offset = y * width + x
            for (channel in 0 until CHANNELS_INT) {
                val sample = (row[x * CHANNELS_INT + channel].toInt() and 0xFF) / PpOcrSpec.BYTE_SCALE
                data[channel * plane + offset] = normalize(channel, sample)
            }
        }
    }
    return data
}

private fun decodeCtc(output: FloatArray, labels: List<String>): Pair<String, Float> {
    if (labels.isEmpty() || output.isEmpty()) return "" to 0f
    val classes = labels.size + 1
    if (output.size % classes != 0) return "" to 0f
    val steps = output.size / classes
    val text = StringBuilder()
    var confidenceSum = 0f
    var kept = 0
    var previous = -1
    for (step in 0 until steps) {
        val offset = step * classes
        var best = 0
        var bestValue = Float.NEGATIVE_INFINITY
        for (index in 0 until classes) {
            val value = output[offset + index]
            if (value > bestValue) {
                bestValue = value
                best = index
            }
        }
        if (best == previous) continue
        previous = best
        if (best == PpOcrSpec.BLANK_INDEX) continue
        val label = labels.getOrNull(best - 1) ?: continue
        text.append(label)
        confidenceSum += bestValue
        kept += 1
    }
    val confidence = if (kept == 0) 0f else confidenceSum / kept
    return text.toString() to confidence
}

private fun probabilities(values: FloatArray): FloatArray {
    if (values.isEmpty()) return values
    val sum = values.sum()
    val already = values.all { it in 0f..1f } && kotlin.math.abs(sum - 1f) < PROBABILITY_TOLERANCE
    if (already) return values
    val peak = values.max()
    val shifted = FloatArray(values.size) { exp(values[it] - peak) }
    val normalizer = shifted.sum().coerceAtLeast(Float.MIN_VALUE)
    return FloatArray(values.size) { shifted[it] / normalizer }
}

private const val CHANNELS_INT = 3
private const val PROBABILITY_TOLERANCE = 0.05f
