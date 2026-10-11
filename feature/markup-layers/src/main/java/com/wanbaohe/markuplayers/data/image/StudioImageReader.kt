package com.wanbaohe.markuplayers.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.model.MetadataTag
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.wanbaohe.markuplayers.domain.render.ImageMemoryBudget
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

class StudioImageReader @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
    private val fileController: FileController,
    private val memoryBudget: ImageMemoryBudget,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    suspend fun preview(path: String): Pair<IntegerSize, Bitmap> {
        val dimensions = dimensions(path)
        val preview = load(path, memoryBudget.previewSize(dimensions.width, dimensions.height))
        val normalError = abs(dimensions.aspectRatio - preview.width.toFloat() / preview.height)
        val swappedError = abs(1f / dimensions.aspectRatio - preview.width.toFloat() / preview.height)
        val orientedSize = if (swappedError + 0.01f < normalError) {
            IntegerSize(dimensions.height, dimensions.width)
        } else {
            dimensions
        }
        return orientedSize to preview
    }

    suspend fun load(path: String, size: IntegerSize): Bitmap {
        memoryBudget.requireCanRender(size.width, size.height)
        coroutineContext.ensureActive()
        val bitmap = imageGetter.getImage(data = path, size = size)
            ?: error("Could not decode image")
        coroutineContext.ensureActive()
        return if (Build.VERSION.SDK_INT >= 26 && bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
                ?: error("Could not create a software image")
        } else {
            bitmap
        }
    }

    suspend fun dimensions(path: String): IntegerSize = withContext(ioDispatcher) {
        val file = File(path)
        require(file.isFile) { "Image asset does not exist" }
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        if (options.outWidth > 0 && options.outHeight > 0) {
            return@withContext IntegerSize(options.outWidth, options.outHeight)
        }
        val metadata = fileController.readMetadata(file.toURI().toString())
        val width = metadata?.getAttribute(MetadataTag.PixelXDimension)?.toIntOrNull()
        val height = metadata?.getAttribute(MetadataTag.PixelYDimension)?.toIntOrNull()
        if (width != null && height != null && width > 0 && height > 0) {
            return@withContext IntegerSize(width, height)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            var result: IntegerSize? = null
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
                result = IntegerSize(info.size.width, info.size.height)
                decoder.setTargetSize(1, 1)
            }.recycle()
            return@withContext result ?: error("Could not read image dimensions")
        }
        error("Could not determine the original image dimensions")
    }
}
