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

package com.wanbaohe.markuplayers.data.image

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.markuplayers.domain.model.BaseTransformSpec
import com.wanbaohe.markuplayers.domain.render.ImageMemoryBudget
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

class BaseImageTransformer @Inject constructor(
    private val memoryBudget: ImageMemoryBudget,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    suspend fun apply(image: Bitmap, spec: BaseTransformSpec): Bitmap = withContext(defaultDispatcher) {
        val crop = spec.cropRect
        require(spec.freeRotation.isFinite() &&
            crop.left.isFinite() && crop.top.isFinite() &&
            crop.right.isFinite() && crop.bottom.isFinite() &&
            crop.left >= 0f && crop.top >= 0f && crop.right <= 1f && crop.bottom <= 1f &&
            crop.width > 0f && crop.height > 0f
        ) { "Invalid crop or rotation" }
        memoryBudget.requireCanRender(image.width, image.height)
        ensureActive()
        val degrees = ((spec.rotationSteps % 4 + 4) % 4) * 90f + spec.freeRotation
        val transformed = if (degrees == 0f && !spec.flipHorizontal && !spec.flipVertical) {
            image
        } else {
            val matrix = Matrix().apply {
                postScale(
                    if (spec.flipHorizontal) -1f else 1f,
                    if (spec.flipVertical) -1f else 1f,
                    image.width / 2f, image.height / 2f
                )
                postRotate(degrees)
            }
            val bounds = RectF(0f, 0f, image.width.toFloat(), image.height.toFloat())
            matrix.mapRect(bounds)
            memoryBudget.requireCanRender(
                bounds.width().roundToInt().coerceAtLeast(1),
                bounds.height().roundToInt().coerceAtLeast(1)
            )
            Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        }
        ensureActive()
        val left = (crop.left * transformed.width).roundToInt().coerceIn(0, transformed.width - 1)
        val top = (crop.top * transformed.height).roundToInt().coerceIn(0, transformed.height - 1)
        val right = (crop.right * transformed.width).roundToInt().coerceIn(left + 1, transformed.width)
        val bottom = (crop.bottom * transformed.height).roundToInt().coerceIn(top + 1, transformed.height)
        Bitmap.createBitmap(transformed, left, top, right - left, bottom - top)
    }
}
