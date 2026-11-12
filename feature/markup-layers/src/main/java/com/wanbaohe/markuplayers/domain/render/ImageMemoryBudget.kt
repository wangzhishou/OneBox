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
