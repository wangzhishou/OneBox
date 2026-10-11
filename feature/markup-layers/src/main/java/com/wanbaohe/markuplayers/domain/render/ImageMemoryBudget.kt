package com.wanbaohe.markuplayers.domain.render

import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt

class ImageMemoryBudget @Inject constructor() {

    val maxWorkingPixels: Long
        get() = min(Runtime.getRuntime().maxMemory() / 32L, 24_000_000L)

    fun requireCanRender(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Image dimensions must be positive" }
        val pixels = width.toLong() * height
        if (pixels > maxWorkingPixels || maxOf(width, height) > MAX_RENDER_SIDE) {
            throw ImageRenderLimitException(width, height, maxWorkingPixels)
        }
    }

    fun previewSize(width: Int, height: Int): IntegerSize {
        require(width > 0 && height > 0) { "Image dimensions must be positive" }
        val scale = min(1.0, PREVIEW_SIDE.toDouble() / maxOf(width, height))
        return IntegerSize(
            width = (width * scale).roundToInt().coerceAtLeast(1),
            height = (height * scale).roundToInt().coerceAtLeast(1)
        )
    }

    companion object {
        private const val PREVIEW_SIDE = 2048
        private const val MAX_RENDER_SIDE = 16_384
    }
}

class ImageRenderLimitException(
    val width: Int,
    val height: Int,
    val maxPixels: Long,
) : IllegalArgumentException(
    "The ${width}x$height image exceeds this device's processing budget. " +
        "Choose a smaller export resolution."
)
