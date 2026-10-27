package com.wanbaohe.textcard.data.export

import android.graphics.Bitmap
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.wanbaohe.textcard.domain.SavedTextCardImage
import com.wanbaohe.textcard.domain.TextCardExportResult
import com.wanbaohe.textcard.domain.TextCardExportService
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.model.TextCardExportFormat
import com.wanbaohe.textcard.domain.model.TextCardOutputMode
import com.wanbaohe.textcard.domain.render.TextCardLayout
import com.wanbaohe.textcard.domain.render.TextCardLayoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class AndroidTextCardExportService @Inject internal constructor(
    private val exportRenderer: TextCardExportRenderer,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val fileController: FileController,
    private val shareProvider: ImageShareProvider<Bitmap>,
) : TextCardExportService {

    override suspend fun export(
        document: TextCardDocument,
        layout: TextCardLayout,
    ): TextCardExportResult {
        val states = when (document.outputMode) {
            TextCardOutputMode.Pages -> layout.pages
            TextCardOutputMode.LongImage -> listOf(
                layout.longImage ?: throw TextCardLayoutException.LongImageTooLarge(
                    layout.longImageRequiredHeight ?: 0
                )
            )
        }
        check(states.isNotEmpty()) { "There is no card page to export" }
        val format = document.exportFormat.toImageFormat()
        val timestamp = System.currentTimeMillis()
        val saved = mutableListOf<SavedTextCardImage>()
        val failed = mutableListOf<Int>()
        states.forEachIndexed { index, state ->
            coroutineContext.ensureActive()
            val page = index + 1
            try {
                val rendered = exportRenderer.render(state)
                val imageInfo = ImageInfo(
                    width = rendered.width,
                    height = rendered.height,
                    quality = Quality.Base(document.exportQuality),
                    imageFormat = format
                )
                val bytes = imageCompressor.compressAndTransform(rendered, imageInfo)
                val result = fileController.save(
                    saveTarget = ImageSaveTarget(
                        imageInfo = imageInfo,
                        originalUri = "text_card",
                        sequenceNumber = index.takeIf { states.size > 1 },
                        filename = "TextCard_${timestamp}_p${page.toString().padStart(2, '0')}",
                        data = bytes
                    ),
                    keepOriginalMetadata = false,
                    oneTimeSaveLocationUri = null
                )
                if (result is SaveResult.Success) {
                    saved += SavedTextCardImage(
                        page = page,
                        uri = result.fileUri.orEmpty(),
                        fileName = result.fileName.orEmpty(),
                        savingPath = result.savingPath.orEmpty()
                    )
                } else {
                    failed += page
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                failed += page
            }
        }
        var shareFailed = false
        if (document.shareAfterSave && saved.isNotEmpty() && failed.isEmpty()) {
            try {
                shareProvider.shareUris(saved.mapNotNull { it.uri.takeIf(String::isNotBlank) })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                shareFailed = true
            }
        }
        return TextCardExportResult(
            saved = saved,
            failedPages = failed,
            shareFailed = shareFailed
        )
    }
}

private fun TextCardExportFormat.toImageFormat(): ImageFormat = when (this) {
    TextCardExportFormat.Jpg -> ImageFormat.Jpg
    TextCardExportFormat.Png -> ImageFormat.Png.Lossless
    TextCardExportFormat.Webp -> ImageFormat.Webp.Lossy
}
