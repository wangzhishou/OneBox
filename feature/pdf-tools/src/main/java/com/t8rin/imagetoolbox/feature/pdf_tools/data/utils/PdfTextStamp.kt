/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.pdf_tools.data.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A block of text rasterized with the system font.
 *
 * PDFBox can only embed TrueType fonts, while the system CJK font on Android is an
 * OpenType/CFF collection, so text that may contain arbitrary scripts is drawn onto a
 * bitmap with [Typeface.DEFAULT_BOLD] and embedded as an image. It looks exactly like the
 * on-screen preview (same typeface, same metrics) and needs no bundled font.
 */
internal data class PdfTextStamp(
    val bitmap: Bitmap,
    val widthPt: Float,
    val heightPt: Float,
) {
    val aspectRatio: Float get() = heightPt / widthPt
}

/** Rasterization resolution. 4 px per PDF point is roughly 288 dpi. */
private const val STAMP_PIXELS_PER_POINT = 4f
private const val STAMP_MAX_BITMAP_SIDE = 4096
private const val STAMP_MIN_BITMAP_WIDTH = 16
private const val STAMP_CROP_PADDING_PX = 2

/** Base size used to measure the text before it is scaled to the target width. */
private const val STAMP_BASE_TEXT_SIZE = 100f

/**
 * Draws [lines] centered on a transparent bitmap and crops it to the glyph bounds.
 *
 * The widest line is scaled so that its advance width matches [targetWidthPt], which is
 * the same rule the Compose previews use, so preview and output stay consistent.
 */
internal fun rasterizePdfText(
    lines: List<String>,
    targetWidthPt: Float,
    color: Int,
): PdfTextStamp {
    val visible = lines.filter { it.isNotBlank() }
    require(visible.isNotEmpty()) { "No text to rasterize" }
    require(targetWidthPt.isFinite() && targetWidthPt > 0f) { "Invalid stamp width" }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.DEFAULT_BOLD
        textSize = STAMP_BASE_TEXT_SIZE
        this.color = color
    }

    var widestAdvance = 1f
    visible.forEach { line ->
        widestAdvance = max(widestAdvance, paint.measureText(line))
    }

    var widthPx = (targetWidthPt * STAMP_PIXELS_PER_POINT)
        .roundToInt()
        .coerceIn(STAMP_MIN_BITMAP_WIDTH, STAMP_MAX_BITMAP_SIDE)
    var layout = paint.layout(visible, widthPx, widestAdvance)

    if (layout.heightPx > STAMP_MAX_BITMAP_SIDE) {
        widthPx = (widthPx.toFloat() * STAMP_MAX_BITMAP_SIDE / layout.heightPx)
            .roundToInt()
            .coerceAtLeast(STAMP_MIN_BITMAP_WIDTH)
        layout = paint.layout(visible, widthPx, widestAdvance)
    }

    val full = Bitmap.createBitmap(widthPx, layout.heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(full)
    val bounds = Rect()
    var left = Int.MAX_VALUE
    var top = Int.MAX_VALUE
    var right = Int.MIN_VALUE
    var bottom = Int.MIN_VALUE

    visible.forEachIndexed { index, line ->
        val x = (widthPx - paint.measureText(line)) / 2f
        val baseline = index * layout.lineHeight - layout.ascent
        canvas.drawText(line, x, baseline, paint)
        paint.getTextBounds(line, 0, line.length, bounds)
        left = minOf(left, bounds.left + x.roundToInt())
        top = minOf(top, bounds.top + baseline.roundToInt())
        right = maxOf(right, bounds.right + x.roundToInt())
        bottom = maxOf(bottom, bounds.bottom + baseline.roundToInt())
    }

    val cropLeft = (left - STAMP_CROP_PADDING_PX).coerceIn(0, widthPx - 1)
    val cropTop = (top - STAMP_CROP_PADDING_PX).coerceIn(0, layout.heightPx - 1)
    val cropRight = (right + STAMP_CROP_PADDING_PX).coerceIn(cropLeft + 1, widthPx)
    val cropBottom = (bottom + STAMP_CROP_PADDING_PX).coerceIn(cropTop + 1, layout.heightPx)
    val cropped = Bitmap.createBitmap(
        full,
        cropLeft,
        cropTop,
        cropRight - cropLeft,
        cropBottom - cropTop,
    )

    // Glyph bounds are slightly narrower than the advance width the preview uses to
    // normalize against, so keep the same ratio to stay pixel-consistent with it.
    val widthPt = targetWidthPt * cropped.width / widthPx

    return PdfTextStamp(
        bitmap = cropped,
        widthPt = widthPt,
        heightPt = widthPt * cropped.height / cropped.width,
    )
}

private class TextLayout(
    val ascent: Float,
    val lineHeight: Float,
    val heightPx: Int,
)

/** Re-applies the text size that maps [widthPx] onto [widestAdvance] and re-measures. */
private fun Paint.layout(
    lines: List<String>,
    widthPx: Int,
    widestAdvance: Float,
): TextLayout {
    textSize = STAMP_BASE_TEXT_SIZE * widthPx / widestAdvance
    val metrics = fontMetrics
    val lineHeight = (metrics.descent - metrics.ascent + metrics.leading)
        .coerceAtLeast(1f)
    return TextLayout(
        ascent = metrics.ascent,
        lineHeight = lineHeight,
        heightPx = ceil(lineHeight * lines.size).toInt().coerceAtLeast(1),
    )
}
