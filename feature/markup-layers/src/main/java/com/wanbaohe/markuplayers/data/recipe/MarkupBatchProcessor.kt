package com.wanbaohe.markuplayers.data.recipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.wanbaohe.markuplayers.data.render.LayerEffectsProcessor
import com.wanbaohe.markuplayers.domain.MarkupLayersApplier
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.domain.recipe.BatchFailure
import com.wanbaohe.markuplayers.domain.recipe.BatchProgress
import com.wanbaohe.markuplayers.domain.recipe.BatchReport
import com.wanbaohe.markuplayers.domain.recipe.ExportEncoded
import com.wanbaohe.markuplayers.domain.recipe.MarkupBatchCancelledException
import com.wanbaohe.markuplayers.domain.recipe.MarkupRecipe
import com.wanbaohe.markuplayers.domain.render.ImageMemoryBudget
import com.wanbaohe.markuplayers.presentation.export.ExportResolution
import com.wanbaohe.markuplayers.presentation.tools.adjust.BaseAdjustments
import com.wanbaohe.markuplayers.presentation.tools.adjust.toColorMatrixValues
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

class MarkupBatchProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageGetter: ImageGetter<Bitmap>,
    private val markupLayersApplier: MarkupLayersApplier<Bitmap>,
    private val effectsProcessor: LayerEffectsProcessor,
    private val exportEncoder: MarkupExportEncoder,
    private val fileController: FileController,
    private val memoryBudget: ImageMemoryBudget,
    dispatchersHolder: DispatchersHolder
) : DispatchersHolder by dispatchersHolder {

    suspend fun process(
        recipe: MarkupRecipe,
        uris: List<String>,
        onProgress: (BatchProgress) -> Unit
    ): BatchReport {
        recipe.validate()
        val inputs = uris.toList()
        val successes = mutableListOf<SaveResult.Success>()
        val failures = mutableListOf<BatchFailure>()
        fun report(cancelled: Boolean = false) = BatchReport(
            successes = successes.toList(),
            failures = failures.toList(),
            total = inputs.size,
            cancelled = cancelled
        )

        try {
            onProgress(BatchProgress(0, inputs.size))
            inputs.forEachIndexed { index, uri ->
                currentCoroutineContext().ensureActive()
                val name = withContext(ioDispatcher) { displayName(uri) }
                onProgress(BatchProgress(successes.size + failures.size, inputs.size, name))
                try {
                    when (val result = processImage(recipe, uri, index + 1)) {
                        is SaveResult.Success -> {
                            if (result.fileUri.isNullOrBlank()) {
                                failures += BatchFailure(
                                    uri, "The image was not saved to a file. Enable file saving instead of clipboard-only mode."
                                )
                            } else if (result.isOverwritten) {
                                failures += BatchFailure(uri, "The output was not saved as a separate copy.")
                            } else {
                                successes += result
                            }
                        }
                        is SaveResult.Error -> {
                            if (result.throwable is CancellationException) throw result.throwable
                            failures += BatchFailure(
                                uri, result.throwable.message ?: "The image could not be saved."
                            )
                        }
                        SaveResult.Skipped -> failures += BatchFailure(uri, "The image was skipped and not saved.")
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    failures += BatchFailure(uri, error.message ?: "The image could not be processed.")
                }
                currentCoroutineContext().ensureActive()
                onProgress(BatchProgress(successes.size + failures.size, inputs.size))
            }
        } catch (cancelled: CancellationException) {
            throw MarkupBatchCancelledException(report(cancelled = true), cancelled)
        }
        return report()
    }

    private suspend fun processImage(
        recipe: MarkupRecipe,
        uri: String,
        sequenceNumber: Int
    ): SaveResult {
        val encoded = renderForExport(recipe, uri)
        currentCoroutineContext().ensureActive()
        // Finish one committed file before stopping, so a cancellation report includes that file.
        return withContext(NonCancellable) {
            fileController.save(
                saveTarget = ImageSaveTarget(
                    imageInfo = encoded.imageInfo,
                    originalUri = uri,
                    sequenceNumber = sequenceNumber,
                    data = encoded.data,
                    overwriteOriginal = false
                ),
                keepOriginalMetadata = false
            )
        }
    }

    private suspend fun renderForExport(
        recipe: MarkupRecipe,
        uri: String
    ): ExportEncoded = withContext(defaultDispatcher) {
        val sourceSize = readSourceSize(uri)
        val sourceCrop = pixelCrop(recipe.cropRect, sourceSize.width, sourceSize.height)
        val settings = recipe.exportSettings.copy(saveCopy = true, shareAfterSave = false)
        val targetInfo = settings.toImageInfo(sourceCrop.width, sourceCrop.height)
        memoryBudget.requireCanRender(targetInfo.width, targetInfo.height)

        val decodeScale = minOf(
            1.0,
            maxOf(
                targetInfo.width.toDouble() / sourceCrop.width,
                targetInfo.height.toDouble() / sourceCrop.height
            )
        )
        val decodeSize = IntegerSize(
            (sourceSize.width * decodeScale).roundToInt().coerceAtLeast(1),
            (sourceSize.height * decodeScale).roundToInt().coerceAtLeast(1)
        )
        // A small crop may still need a larger source decode. Reject it before allocation.
        memoryBudget.requireCanRender(decodeSize.width, decodeSize.height)
        currentCoroutineContext().ensureActive()
        val decoded = checkNotNull(imageGetter.getImage(data = uri, size = decodeSize)) {
            "The source image could not be decoded."
        }
        memoryBudget.requireCanRender(decoded.width, decoded.height)
        check(decoded.width + 1L >= decodeSize.width && decoded.height + 1L >= decodeSize.height) {
            "The decoder cannot provide the requested resolution. Choose a smaller export size."
        }
        if (settings.resolution == ExportResolution.Original) {
            check(decoded.width == sourceSize.width && decoded.height == sourceSize.height) {
                "The decoder cannot provide the original resolution. Choose a smaller export size."
            }
        }

        var current = decoded
        var ownsCurrent = false
        fun replace(next: Bitmap) {
            if (next === current) return
            if (ownsCurrent) current.recycle()
            current = next
            ownsCurrent = true
        }

        try {
            if (Build.VERSION.SDK_INT >= 26 && current.config == Bitmap.Config.HARDWARE) {
                replace(checkNotNull(current.copy(Bitmap.Config.ARGB_8888, false)) {
                    "The source image could not be prepared for rendering."
                })
            }
            val crop = pixelCrop(recipe.cropRect, current.width, current.height)
            if (crop.left != 0 || crop.top != 0 || crop.width != current.width || crop.height != current.height) {
                replace(Bitmap.createBitmap(current, crop.left, crop.top, crop.width, crop.height))
            }
            currentCoroutineContext().ensureActive()
            if (!recipe.adjustments.isNeutral) {
                replace(applyAdjustments(current, recipe.adjustments))
            }
            replace(effectsProcessor.apply(current, recipe.effects, recipe.mask))
            currentCoroutineContext().ensureActive()
            replace(markupLayersApplier.applyToImage(current, recipe.instantiateLayers()))
            currentCoroutineContext().ensureActive()

            exportEncoder.encodeForExport(
                image = current,
                settings = settings.copy(
                    resolution = ExportResolution.Custom,
                    customWidth = targetInfo.width,
                    customHeight = targetInfo.height
                )
            )
        } finally {
            // ImageGetter can return a shared Coil-cache bitmap; only intermediates belong to us.
            if (ownsCurrent) current.recycle()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun readSourceSize(source: String): IntegerSize = withContext(ioDispatcher) {
        val uri = Uri.parse(source)
        require(uri.scheme == "content" || uri.scheme == "file") {
            "Batch images must be local content:// or file:// images."
        }
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        checkNotNull(context.contentResolver.openInputStream(uri)) {
            "The source image could not be opened."
        }.use { BitmapFactory.decodeStream(it, null, options) }
        require(options.outWidth > 0 && options.outHeight > 0) {
            "The source format does not expose safe raster dimensions for batch processing."
        }
        currentCoroutineContext().ensureActive()
        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val rotated = orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
            orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
            orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
            orientation == ExifInterface.ORIENTATION_TRANSVERSE
        if (rotated) IntegerSize(options.outHeight, options.outWidth)
        else IntegerSize(options.outWidth, options.outHeight)
    }

    private fun applyAdjustments(image: Bitmap, adjustments: BaseAdjustments): Bitmap {
        val output = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        try {
            Canvas(output).drawBitmap(
                image, 0f, 0f,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(ColorMatrix(adjustments.toColorMatrixValues()))
                }
            )
            return output
        } catch (error: Throwable) {
            output.recycle()
            throw error
        }
    }

    private fun displayName(source: String): String {
        val uri = Uri.parse(source)
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(
                    uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index >= 0) cursor.getString(index)?.takeIf { it.isNotBlank() }?.let { return it }
                    }
                }
            } catch (_: Exception) {
                // A readable image need not expose its display name.
            }
        }
        return uri.lastPathSegment?.takeIf { it.isNotBlank() } ?: source
    }

    private fun pixelCrop(rect: NormalizedRect?, width: Int, height: Int): PixelCrop {
        if (rect == null) return PixelCrop(0, 0, width, height)
        val left = (rect.left * width).roundToInt().coerceIn(0, width - 1)
        val top = (rect.top * height).roundToInt().coerceIn(0, height - 1)
        val right = (rect.right * width).roundToInt().coerceIn(left + 1, width)
        val bottom = (rect.bottom * height).roundToInt().coerceIn(top + 1, height)
        return PixelCrop(left, top, right - left, bottom - top)
    }

    private data class PixelCrop(val left: Int, val top: Int, val width: Int, val height: Int)
}
