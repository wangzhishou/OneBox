package com.wanbaohe.markuplayers.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.markuplayers.data.render.LayerBitmapContentDecoder
import com.wanbaohe.markuplayers.data.render.LayerEffectsProcessor
import com.wanbaohe.markuplayers.data.render.LayerExportDispatcher
import com.wanbaohe.markuplayers.data.render.blendBitmapOnto
import com.wanbaohe.markuplayers.domain.MarkupLayersApplier
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.isBitmapFilterable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 把图层列表按 z 序逐层重绘到原图的可变副本上,输出与原图同分辨率的位图。
 */
internal class AndroidMarkupLayersApplier @Inject constructor(
    private val dispatcher: LayerExportDispatcher,
    private val contentDecoder: LayerBitmapContentDecoder,
    private val effectsProcessor: LayerEffectsProcessor,
    dispatchersHolder: DispatchersHolder,
) : MarkupLayersApplier<Bitmap>, DispatchersHolder by dispatchersHolder {

    override suspend fun applyToImage(
        image: Bitmap,
        layers: List<MarkupLayer>,
        filteredLayers: Map<String, Bitmap>,
    ): Bitmap = withContext(defaultDispatcher) {
        currentCoroutineContext().ensureActive()
        val result = checkNotNull(image.copy(Bitmap.Config.ARGB_8888, true)) {
            "Cannot copy the export canvas"
        }
        val canvas = Canvas(result)
        var completed = false
        try {
            layers.forEach { layer ->
                currentCoroutineContext().ensureActive()
                if (!layer.transform.visible || layer.transform.alpha == 0f) return@forEach
                val provided = filteredLayers[layer.id]
                val content = if (layer.type.isBitmapFilterable) {
                    provided ?: checkNotNull(
                        contentDecoder.decode(layer, result.width, result.height)
                    ) { "Cannot decode export content for layer ${layer.id}" }
                } else {
                    check(!layer.requiresBitmapProcessing) {
                        "Layer ${layer.id} must be rasterized before applying effects or masks"
                    }
                    null
                }
                val processed = if (provided == null && content != null && layer.requiresBitmapProcessing) {
                    effectsProcessor.apply(content, layer.effectiveEffects, layer.mask)
                } else content
                try {
                    val localContent = processed?.let { mapOf(layer.id to it) }.orEmpty()
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                        layer.transform.blendMode == LayerBlendMode.Multiply
                    ) {
                        val overlay = Bitmap.createBitmap(result.width, result.height, Bitmap.Config.ARGB_8888)
                        try {
                            dispatcher.draw(
                                canvas = Canvas(overlay),
                                layer = layer.copy(transform = layer.transform.copy(blendMode = LayerBlendMode.Normal)),
                                imageWidth = result.width,
                                imageHeight = result.height,
                                filteredLayers = localContent
                            )
                            blendBitmapOnto(result, overlay, LayerBlendMode.Multiply)
                        } finally {
                            overlay.recycle()
                        }
                    } else {
                        dispatcher.draw(
                            canvas = canvas,
                            layer = layer,
                            imageWidth = result.width,
                            imageHeight = result.height,
                            filteredLayers = localContent
                        )
                    }
                } finally {
                    // Caller-supplied content and decoder/image-loader caches remain caller-owned.
                    if (provided == null && processed != null && processed !== content) {
                        processed.recycle()
                    }
                }
            }
            currentCoroutineContext().ensureActive()
            completed = true
            result
        } finally {
            if (!completed) result.recycle()
        }
    }
}
