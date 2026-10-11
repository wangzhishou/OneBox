package com.wanbaohe.markuplayers.data.recipe

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageScaler
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageScaleMode
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.wanbaohe.markuplayers.domain.recipe.ExportEncoded
import com.wanbaohe.markuplayers.domain.recipe.ExportSizeLimitException
import com.wanbaohe.markuplayers.domain.render.ImageMemoryBudget
import com.wanbaohe.markuplayers.presentation.export.ExportSettings
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MarkupExportEncoder @Inject constructor(
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val imageScaler: ImageScaler<Bitmap>,
    private val memoryBudget: ImageMemoryBudget,
    dispatchersHolder: DispatchersHolder
) : DispatchersHolder by dispatchersHolder {

    /** Does not consume or recycle [image]. Limits are checked on the final encoded bytes. */
    suspend fun encodeForExport(
        image: Bitmap,
        settings: ExportSettings
    ): ExportEncoded = withContext(defaultDispatcher) {
        require(!image.isRecycled) { "The image is no longer available." }
        val targetInfo = settings.toImageInfo(image.width, image.height)
        memoryBudget.requireCanRender(targetInfo.width, targetInfo.height)
        currentCoroutineContext().ensureActive()

        var scaled: Bitmap? = null
        var flattened: Bitmap? = null
        var software: Bitmap? = null
        try {
            val resized = if (targetInfo.width == image.width && targetInfo.height == image.height) {
                image
            } else {
                imageScaler.scaleImage(
                    image = image,
                    width = targetInfo.width,
                    height = targetInfo.height,
                    imageScaleMode = ImageScaleMode.Base
                ).also {
                    if (it !== image) scaled = it
                }
            }
            check(resized.width == targetInfo.width && resized.height == targetInfo.height) {
                "The image could not be resized to the requested export dimensions."
            }

            val prepared = if (settings.format == ImageFormat.Jpg) {
                val drawable = if (Build.VERSION.SDK_INT >= 26 &&
                    resized.config == Bitmap.Config.HARDWARE
                ) {
                    checkNotNull(resized.copy(Bitmap.Config.ARGB_8888, false)) {
                        "The image could not be prepared for JPG export."
                    }.also { software = it }
                } else resized
                Bitmap.createBitmap(drawable.width, drawable.height, Bitmap.Config.ARGB_8888).apply {
                    flattened = this
                    Canvas(this).apply {
                        drawColor(settings.jpegBackgroundColor)
                        drawBitmap(drawable, 0f, 0f, null)
                    }
                    setHasAlpha(false)
                }
            } else resized

            suspend fun encode(quality: Int): ExportEncoded {
                currentCoroutineContext().ensureActive()
                val info = targetInfo.copy(
                    quality = Quality.Base(quality),
                    originalUri = null
                )
                val data = imageCompressor.compressAndTransform(
                    image = prepared,
                    imageInfo = info,
                    applyImageTransformations = false
                )
                currentCoroutineContext().ensureActive()
                check(data.isNotEmpty()) { "The image encoder returned an empty file." }
                return ExportEncoded(info.copy(sizeInBytes = data.size), data)
            }

            val requested = encode(settings.quality)
            val limit = settings.maxFileSizeBytes ?: return@withContext requested
            if (requested.data.size.toLong() <= limit) return@withContext requested
            if (settings.format is ImageFormat.Png) {
                throw ExportSizeLimitException(limit, requested.data.size.toLong(), settings.format)
            }

            var best = if (settings.quality == ExportSettings.MIN_QUALITY) requested
            else encode(ExportSettings.MIN_QUALITY)
            if (best.data.size.toLong() > limit) {
                throw ExportSizeLimitException(limit, best.data.size.toLong(), settings.format)
            }
            var low = ExportSettings.MIN_QUALITY + 1
            var high = settings.quality - 1
            while (low <= high) {
                val quality = low + (high - low) / 2
                val encoded = encode(quality)
                if (encoded.data.size.toLong() <= limit) {
                    best = encoded
                    low = quality + 1
                } else {
                    high = quality - 1
                }
            }
            check(best.data.size.toLong() <= limit)
            best
        } finally {
            flattened?.recycle()
            software?.recycle()
            scaled?.recycle()
        }
    }
}
