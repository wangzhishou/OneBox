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
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.model.withAnnotations
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
        pageIndices: Set<Int>?,
    ): TextCardExportResult {
        val states = selectStates(document, layout.withAnnotations(document), pageIndices)
        val format = document.exportFormat.toImageFormat()
        val timestamp = System.currentTimeMillis()
        val saved = mutableListOf<SavedTextCardImage>()
        val failed = mutableListOf<Int>()
        states.forEachIndexed { position, indexedState ->
            coroutineContext.ensureActive()
            val index = indexedState.index
            val page = index + 1
            try {
                val rendered = render(document, layout, indexedState)
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
                        sequenceNumber = position.takeIf { states.size > 1 },
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
        return TextCardExportResult(saved = saved, failedPages = failed)
    }

    override suspend fun share(
        document: TextCardDocument,
        layout: TextCardLayout,
        pageIndices: Set<Int>?,
    ) {
        val states = selectStates(document, layout.withAnnotations(document), pageIndices)
        val format = document.exportFormat.toImageFormat()
        val timestamp = System.currentTimeMillis()
        val uris = states.map { indexedState ->
            coroutineContext.ensureActive()
            val rendered = render(document, layout, indexedState)
            shareProvider.cacheImage(
                image = rendered,
                imageInfo = ImageInfo(
                    width = rendered.width,
                    height = rendered.height,
                    quality = Quality.Base(document.exportQuality),
                    imageFormat = format
                ),
                filename = "TextCard_${timestamp}_p${indexedState.index + 1}"
            ) ?: error("Could not cache card for sharing")
        }
        shareProvider.shareUris(uris)
    }

    private suspend fun render(
        document: TextCardDocument,
        layout: TextCardLayout,
        state: IndexedValue<TextCardRenderState>,
    ): Bitmap = if (document.outputMode == TextCardOutputMode.LongImage &&
        document.annotations.isNotEmpty()
    ) {
        renderStitchedPages(layout.pages)
    } else {
        exportRenderer.render(state.value)
    }

    private suspend fun renderStitchedPages(pages: List<TextCardRenderState>): Bitmap {
        val width = pages.first().canvas.width
        val height = pages.sumOf { it.canvas.height }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        var offsetY = 0
        pages.forEach { page ->
            coroutineContext.ensureActive()
            val rendered = exportRenderer.render(page)
            canvas.drawBitmap(rendered, 0f, offsetY.toFloat(), null)
            offsetY += rendered.height
        }
        return bitmap
    }

    private fun selectStates(
        document: TextCardDocument,
        layout: TextCardLayout,
        pageIndices: Set<Int>?,
    ) = when (document.outputMode) {
        TextCardOutputMode.Pages -> layout.pages
        TextCardOutputMode.LongImage -> if (document.annotations.isNotEmpty()) {
            val height = layout.pages.sumOf { it.canvas.height }
            val width = layout.pages.firstOrNull()?.canvas?.width ?: 0
            if (height > TextCardDocument.MAX_LONG_IMAGE_SIDE ||
                width.toLong() * height > TextCardDocument.MAX_LONG_IMAGE_PIXELS
            ) {
                throw TextCardLayoutException.LongImageTooLarge(height)
            }
            layout.pages.take(1)
        } else {
            listOf(
                layout.longImage ?: throw TextCardLayoutException.LongImageTooLarge(
                    layout.longImageRequiredHeight ?: 0
                )
            )
        }
    }.withIndex().filter { indexed ->
        pageIndices == null || indexed.index in pageIndices
    }.also { selected ->
        check(selected.isNotEmpty()) { "There is no card page to export" }
    }
}

private fun TextCardExportFormat.toImageFormat(): ImageFormat = when (this) {
    TextCardExportFormat.Jpg -> ImageFormat.Jpg
    TextCardExportFormat.Png -> ImageFormat.Png.Lossless
    TextCardExportFormat.Webp -> ImageFormat.Webp.Lossy
}
