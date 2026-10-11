package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.transformation.EmptyTransformation
import com.t8rin.imagetoolbox.core.filters.domain.FilterProvider
import com.wanbaohe.markuplayers.domain.model.LayerEffect
import com.wanbaohe.markuplayers.domain.model.LayerMask
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Shared preview/export bitmap pipeline, without a persistent bitmap cache.
 *
 * Filters run in order at the current content size. Each strength mixes the
 * result with that effect's input in premultiplied RGBA, including alpha-removal
 * filters. A mask clips the final content alpha: there is no unchanged image
 * outside the selection. Geometry-changing filters are rejected, rather than
 * stretching the original or silently omitting an effect.
 */
class LayerEffectsProcessor @Inject constructor(
    private val filterProvider: FilterProvider<Bitmap>,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    suspend fun apply(
        image: Bitmap,
        effects: List<LayerEffect>,
        mask: LayerMask? = null,
    ): Bitmap = withContext(defaultDispatcher) {
        check(!image.isRecycled) { "Cannot process a recycled layer bitmap" }
        currentCoroutineContext().ensureActive()
        if (effects.isEmpty() && mask == null) return@withContext image

        var current = image
        var ownsCurrent = false
        try {
            effects.forEach { effect ->
                currentCoroutineContext().ensureActive()
                // Keep the original independent even if a transformation mutates its input.
                val input = checkNotNull(current.copy(Bitmap.Config.ARGB_8888, true)) {
                    "Cannot copy layer content for an effect"
                }
                var filtered: Bitmap? = null
                try {
                    val transformation = filterProvider.filterToTransformation(effect.filter)
                    check(transformation !is EmptyTransformation<*> || !effect.filter.isVisible) {
                        "Unsupported layer filter: ${effect.filter::class.simpleName}"
                    }
                    filtered = transformation.transform(
                        input,
                        IntegerSize(input.width, input.height)
                    )
                    currentCoroutineContext().ensureActive()
                    check(!filtered.isRecycled) { "Filter returned a recycled bitmap" }
                    check(filtered.width == current.width && filtered.height == current.height) {
                        "Layer effects must preserve content dimensions"
                    }
                    if (Build.VERSION.SDK_INT >= 26 && filtered.config == Bitmap.Config.HARDWARE) {
                        val software = checkNotNull(filtered.copy(Bitmap.Config.ARGB_8888, false)) {
                            "Cannot read hardware filter output"
                        }
                        recycleIntermediate(filtered, input, image)
                        filtered = software
                    }
                    val next = if (effect.strength == 1f) {
                        filtered
                    } else {
                        blendStrength(current, filtered, effect.strength)
                    }
                    if (ownsCurrent) recycleIntermediate(current, next, image)
                    current = next
                    ownsCurrent = current !== image
                } finally {
                    recycleIntermediate(input, current, image)
                    recycleIntermediate(filtered, current, image)
                }
            }

            if (mask != null) {
                val next = clipAlpha(current, mask)
                if (ownsCurrent) recycleIntermediate(current, next, image)
                current = next
                ownsCurrent = true
            }
            currentCoroutineContext().ensureActive()
            current.also { ownsCurrent = false }
        } finally {
            if (ownsCurrent) recycleIntermediate(current, image, image)
        }
    }

    /**
     * 蒙版选区预览:输出透明白色位图,alpha = 选区不透明度(含反选与羽化),
     * 供蒙版工具 sheet 叠加显示(与 [clipAlpha] 同一套栅格化语义)。
     */
    suspend fun maskAlphaPreview(
        mask: LayerMask,
        width: Int,
        height: Int,
    ): Bitmap = withContext(defaultDispatcher) {
        require(width > 0 && height > 0) { "Mask preview dimensions must be positive" }
        val alpha = rasterMask(mask, width, height)
        if (mask.feather > 0f) {
            val radius = (mask.feather.coerceAtMost(LayerMask.MaxFeather) * min(width, height))
                .roundToInt()
            if (radius > 0) {
                featherLayerMaskAlpha(alpha, width, height, radius)
            }
        }
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val row = IntArray(width)
        for (y in 0 until height) {
            currentCoroutineContext().ensureActive()
            val rowOffset = y * width
            for (x in 0 until width) {
                row[x] = (alpha[rowOffset + x].toInt() and 255) shl 24 or 0x00FFFFFF
            }
            output.setPixels(row, 0, width, 0, y, width, 1)
        }
        output
    }

    private suspend fun blendStrength(
        original: Bitmap,
        filtered: Bitmap,
        strength: Float,
    ): Bitmap {
        val output = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val before = IntArray(original.width)
        val after = IntArray(original.width)
        var completed = false
        try {
            for (y in 0 until original.height) {
                currentCoroutineContext().ensureActive()
                original.getPixels(before, 0, original.width, 0, y, original.width, 1)
                filtered.getPixels(after, 0, original.width, 0, y, original.width, 1)
                for (x in before.indices) {
                    before[x] = interpolateEffectPixel(before[x], after[x], strength)
                }
                output.setPixels(before, 0, original.width, 0, y, original.width, 1)
            }
            completed = true
            return output
        } finally {
            if (!completed) output.recycle()
        }
    }

    private suspend fun clipAlpha(image: Bitmap, mask: LayerMask): Bitmap {
        val width = image.width
        val height = image.height
        require(width.toLong() * height <= Int.MAX_VALUE) { "Layer mask dimensions are too large" }
        val alpha = rasterMask(mask, width, height)
        if (mask.feather > 0f) {
            val radius = (mask.feather.coerceAtMost(LayerMask.MaxFeather) * min(width, height))
                .roundToInt()
            if (radius > 0) {
                featherLayerMaskAlpha(alpha, width, height, radius)
            }
        }
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width)
        val software = if (Build.VERSION.SDK_INT >= 26 && image.config == Bitmap.Config.HARDWARE) {
            checkNotNull(image.copy(Bitmap.Config.ARGB_8888, false)) {
                "Cannot read hardware layer content"
            }
        } else image
        var completed = false
        try {
            for (y in 0 until height) {
                currentCoroutineContext().ensureActive()
                software.getPixels(pixels, 0, width, 0, y, width, 1)
                val rowOffset = y * width
                for (x in pixels.indices) {
                    pixels[x] = applyMaskPixel(pixels[x], alpha[rowOffset + x].toInt() and 255)
                }
                output.setPixels(pixels, 0, width, 0, y, width, 1)
            }
            completed = true
            return output
        } finally {
            if (software !== image) software.recycle()
            if (!completed) output.recycle()
        }
    }

    private suspend fun rasterMask(mask: LayerMask, width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        try {
            val rect = mask.rect
            if (rect != null) {
                val left = rect.left * width
                val top = rect.top * height
                val right = rect.right * width
                val bottom = rect.bottom * height
                require(left.isFinite() && top.isFinite() && right.isFinite() && bottom.isFinite()) {
                    "Mask rectangle exceeds pixel coordinate range"
                }
                canvas.drawRect(left, top, right, bottom, paint)
            } else if (mask.strokes.none { !it.erase }) {
                canvas.drawColor(Color.WHITE)
            }

            mask.strokes.forEach { stroke ->
                currentCoroutineContext().ensureActive()
                val points = stroke.points
                if (points.isEmpty()) return@forEach
                val strokeWidth = stroke.widthRatio * width
                require(strokeWidth.isFinite()) { "Mask stroke exceeds pixel width range" }
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeJoin = Paint.Join.ROUND
                paint.xfermode = if (stroke.erase) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
                val path = Path()
                points.forEachIndexed { index, point ->
                    if (index % 1024 == 0) currentCoroutineContext().ensureActive()
                    val x = point.x * width
                    val y = point.y * height
                    require(x.isFinite() && y.isFinite()) { "Mask point exceeds pixel coordinate range" }
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                if (stroke.widthRatio == 0f) {
                    path.close()
                    paint.style = Paint.Style.FILL
                    canvas.drawPath(path, paint)
                } else if (points.size == 1) {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(points[0].x * width, points[0].y * height, strokeWidth / 2f, paint)
                } else {
                    canvas.drawPath(path, paint)
                }
            }

            val alpha = ByteArray(width * height)
            val row = IntArray(width)
            for (y in 0 until height) {
                currentCoroutineContext().ensureActive()
                bitmap.getPixels(row, 0, width, 0, y, width, 1)
                for (x in row.indices) {
                    val value = Color.alpha(row[x])
                    alpha[y * width + x] = (if (mask.inverted) 255 - value else value).toByte()
                }
            }
            return alpha
        } finally {
            bitmap.recycle()
        }
    }

    private fun recycleIntermediate(bitmap: Bitmap?, retained: Bitmap, original: Bitmap) {
        if (bitmap != null && bitmap !== retained && bitmap !== original && !bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}

internal fun interpolateEffectPixel(before: Int, after: Int, strength: Float): Int {
    val beforeWeight = (before ushr 24) * (1f - strength)
    val afterWeight = (after ushr 24) * strength
    val alpha = beforeWeight + afterWeight
    val roundedAlpha = alpha.roundToInt().coerceIn(0, 255)
    if (roundedAlpha == 0) return 0

    fun channel(shift: Int): Int = (
        (((before ushr shift) and 255) * beforeWeight +
            ((after ushr shift) and 255) * afterWeight) / alpha
        ).roundToInt().coerceIn(0, 255)

    return (roundedAlpha shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
}

internal fun applyMaskPixel(pixel: Int, maskAlpha: Int): Int {
    val alpha = ((pixel ushr 24) * maskAlpha + 127) / 255
    return if (alpha == 0) 0 else (pixel and 0x00FFFFFF) or (alpha shl 24)
}

// Clamped edges keep a feathered full selection opaque, without a transparent frame.
internal suspend fun featherLayerMaskAlpha(
    alpha: ByteArray,
    width: Int,
    height: Int,
    radius: Int,
) {
    require(width > 0 && height > 0 && width.toLong() * height == alpha.size.toLong()) {
        "Mask alpha dimensions must match its pixels"
    }
    require(radius in 0..maxOf(1, (min(width, height) * LayerMask.MaxFeather).roundToInt())) {
        "Mask feather radius exceeds its bound"
    }
    currentCoroutineContext().ensureActive()
    if (radius == 0) return
    val scratch = ByteArray(alpha.size)
    repeat(3) {
        blurMaskHorizontal(alpha, scratch, width, height, radius)
        blurMaskVertical(scratch, alpha, width, height, radius)
    }
}

private suspend fun blurMaskHorizontal(
    input: ByteArray,
    output: ByteArray,
    width: Int,
    height: Int,
    radius: Int,
) {
    val count = radius * 2 + 1
    for (y in 0 until height) {
        currentCoroutineContext().ensureActive()
        val offset = y * width
        fun at(x: Int): Int = input[offset + x.coerceIn(0, width - 1)].toInt() and 255
        var sum = at(0) * (radius + 1)
        for (x in 1..radius) sum += at(x)
        for (x in 0 until width) {
            output[offset + x] = ((sum + count / 2) / count).toByte()
            sum += at(x + radius + 1) - at(x - radius)
        }
    }
}

private suspend fun blurMaskVertical(
    input: ByteArray,
    output: ByteArray,
    width: Int,
    height: Int,
    radius: Int,
) {
    val count = radius * 2 + 1
    for (x in 0 until width) {
        currentCoroutineContext().ensureActive()
        fun at(y: Int): Int = input[y.coerceIn(0, height - 1) * width + x].toInt() and 255
        var sum = at(0) * (radius + 1)
        for (y in 1..radius) sum += at(y)
        for (y in 0 until height) {
            output[y * width + x] = ((sum + count / 2) / count).toByte()
            sum += at(y + radius + 1) - at(y - radius)
        }
    }
}
