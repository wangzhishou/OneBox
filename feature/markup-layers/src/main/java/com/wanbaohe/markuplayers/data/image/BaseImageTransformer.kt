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
