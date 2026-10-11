package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import androidx.annotation.RequiresApi
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.Q)
fun LayerBlendMode.toAndroidBlendMode(): BlendMode = when (this) {
    LayerBlendMode.Normal -> BlendMode.SRC_OVER
    LayerBlendMode.Multiply -> BlendMode.MULTIPLY
    LayerBlendMode.Screen -> BlendMode.SCREEN
    LayerBlendMode.Overlay -> BlendMode.OVERLAY
    LayerBlendMode.Darken -> BlendMode.DARKEN
    LayerBlendMode.Lighten -> BlendMode.LIGHTEN
}

fun LayerBlendMode.toPorterDuffMode(): PorterDuff.Mode = when (this) {
    LayerBlendMode.Normal -> PorterDuff.Mode.SRC_OVER
    // PorterDuff.MULTIPLY is RGBA modulate, not source-over color multiply.
    LayerBlendMode.Multiply -> error("Multiply requires BlendMode or the software layer compositor")
    LayerBlendMode.Screen -> PorterDuff.Mode.SCREEN
    LayerBlendMode.Overlay -> PorterDuff.Mode.OVERLAY
    LayerBlendMode.Darken -> PorterDuff.Mode.DARKEN
    LayerBlendMode.Lighten -> PorterDuff.Mode.LIGHTEN
}

internal fun Paint.setLayerBlendMode(mode: LayerBlendMode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        blendMode = mode.toAndroidBlendMode()
    } else {
        xfermode = PorterDuffXfermode(mode.toPorterDuffMode())
    }
}

internal suspend fun blendBitmapOnto(
    destination: Bitmap,
    source: Bitmap,
    mode: LayerBlendMode,
) {
    require(destination.isMutable && destination.width == source.width && destination.height == source.height) {
        "Software layer blending requires equal dimensions and a mutable destination"
    }
    val width = destination.width
    val below = IntArray(width)
    val above = IntArray(width)
    for (y in 0 until destination.height) {
        currentCoroutineContext().ensureActive()
        destination.getPixels(below, 0, width, 0, y, width, 1)
        source.getPixels(above, 0, width, 0, y, width, 1)
        for (x in 0 until width) below[x] = blendLayerPixel(below[x], above[x], mode)
        destination.setPixels(below, 0, width, 0, y, width, 1)
    }
}

/** Source-over separable blending in premultiplied RGBA, also on transparent bases. */
internal fun blendLayerPixel(destination: Int, source: Int, mode: LayerBlendMode): Int {
    val sourceAlpha = (source ushr 24) / 255f
    if (sourceAlpha == 0f) return destination
    val destinationAlpha = (destination ushr 24) / 255f
    if (destinationAlpha == 0f) return source
    val alpha = sourceAlpha + destinationAlpha * (1f - sourceAlpha)

    fun channel(shift: Int): Int {
        val src = ((source ushr shift) and 255).toFloat()
        val dst = ((destination ushr shift) and 255).toFloat()
        val mixed = when (mode) {
            LayerBlendMode.Normal -> src
            LayerBlendMode.Multiply -> src * dst / 255f
            LayerBlendMode.Screen -> src + dst - src * dst / 255f
            LayerBlendMode.Overlay -> if (dst <= 127.5f) {
                2f * src * dst / 255f
            } else {
                255f - 2f * (255f - src) * (255f - dst) / 255f
            }
            LayerBlendMode.Darken -> minOf(src, dst)
            LayerBlendMode.Lighten -> maxOf(src, dst)
        }
        val premultiplied = dst * destinationAlpha * (1f - sourceAlpha) +
            src * sourceAlpha * (1f - destinationAlpha) +
            mixed * sourceAlpha * destinationAlpha
        return (premultiplied / alpha).roundToInt().coerceIn(0, 255)
    }

    return ((alpha * 255).roundToInt().coerceIn(0, 255) shl 24) or
        (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
}
