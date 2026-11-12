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

package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.sqrt

/** Bounded, processed preview content. Export uses the decoder/applier directly. */
class LayerBitmapLoader @Inject constructor(
    private val contentDecoder: LayerBitmapContentDecoder,
    private val effectsProcessor: LayerEffectsProcessor,
) {
    suspend fun load(
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
    ): Bitmap? {
        require(imageWidth > 0 && imageHeight > 0) { "Preview dimensions must be positive" }
        // A square source on a very wide canvas must not exceed the canvas-based
        // cache budget; bounding only its longest side would not bound its pixels.
        val maxSide = min(
            maxOf(imageWidth, imageHeight),
            sqrt(imageWidth.toDouble() * imageHeight).toInt().coerceAtLeast(1)
        )
        val content = contentDecoder.decode(layer, imageWidth, imageHeight, maxSide)
            ?: return null
        return effectsProcessor.apply(content, layer.effectiveEffects, layer.mask)
    }
}
