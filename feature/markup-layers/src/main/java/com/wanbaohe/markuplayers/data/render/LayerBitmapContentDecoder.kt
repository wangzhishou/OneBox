package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import android.os.Build
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Reuses the image IO and draw/sticker renderers for untransformed local content. */
class LayerBitmapContentDecoder @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
    private val stickerRenderer: StickerLayerExportRenderer,
    private val drawRenderer: DrawLayerExportRenderer,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    /**
     * [maxDecodeSize] bounds the longest preview-content side; null requests export
     * content (original image, export-sized sticker, or full canvas draw bitmap).
     * A decode failure is null, never a substitute image or an omitted effect.
     */
    suspend fun decode(
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
        maxDecodeSize: Int? = null,
    ): Bitmap? = withContext(defaultDispatcher) {
        require(imageWidth > 0 && imageHeight > 0) { "Layer canvas dimensions must be positive" }
        require(maxDecodeSize == null || maxDecodeSize > 0) { "Preview decode size must be positive" }
        currentCoroutineContext().ensureActive()
        val decoded = when (val type = layer.type) {
            is LayerType.Image -> if (maxDecodeSize == null) {
                imageGetter.getImage(data = type.imageData)
            } else {
                imageGetter.getImage(data = type.imageData, size = maxDecodeSize)
            }

            is LayerType.Sticker -> stickerRenderer.decodeBitmap(
                type = type,
                baseSizePx = min(
                    imageWidth * STICKER_EXPORT_BASE_RATIO,
                    maxDecodeSize?.toFloat() ?: Float.MAX_VALUE
                )
            )

            is LayerType.Draw -> {
                val scale = maxDecodeSize?.let {
                    min(1f, it.toFloat() / max(imageWidth, imageHeight))
                } ?: 1f
                drawRenderer.renderStrokesBitmap(
                    strokes = type.strokes,
                    imageWidth = (imageWidth * scale).roundToInt().coerceAtLeast(1),
                    imageHeight = (imageHeight * scale).roundToInt().coerceAtLeast(1)
                )
            }

            else -> error("Text and shape layers must be rasterized before bitmap processing")
        }
        currentCoroutineContext().ensureActive()
        if (Build.VERSION.SDK_INT >= 26 && decoded?.config == Bitmap.Config.HARDWARE) {
            checkNotNull(decoded.copy(Bitmap.Config.ARGB_8888, false)) {
                "Cannot convert decoded layer content to software"
            }
        } else decoded
    }
}
