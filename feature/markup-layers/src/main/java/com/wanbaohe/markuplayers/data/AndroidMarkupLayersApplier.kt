package com.wanbaohe.markuplayers.data

import android.graphics.Bitmap
import android.graphics.Canvas
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.markuplayers.data.render.LayerExportDispatcher
import com.wanbaohe.markuplayers.domain.MarkupLayersApplier
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 把图层列表按 z 序逐层重绘到原图的可变副本上,输出与原图同分辨率的位图。
 */
internal class AndroidMarkupLayersApplier @Inject constructor(
    private val dispatcher: LayerExportDispatcher,
    dispatchersHolder: DispatchersHolder,
) : MarkupLayersApplier<Bitmap>, DispatchersHolder by dispatchersHolder {

    override suspend fun applyToImage(
        image: Bitmap,
        layers: List<MarkupLayer>,
    ): Bitmap = withContext(defaultDispatcher) {
        val result = image.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        layers.forEach { layer ->
            dispatcher.draw(canvas, layer, result.width, result.height)
        }
        result
    }
}
