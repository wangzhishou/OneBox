package com.t8rin.imagetoolbox.feature.pdf_tools.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.model.activity.ActivityCategory
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageCompressor
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.ImageTransformer
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.Preset
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.FilenameCreator
import com.t8rin.imagetoolbox.core.domain.saving.model.ImageSaveTarget
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.ExtractPagesAction
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.PdfCreationParams
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.PdfExtractPagesParams
import com.t8rin.imagetoolbox.feature.pdf_tools.service.model.ImagesToPdfStage
import com.t8rin.imagetoolbox.feature.pdf_tools.service.model.PdfPreflight
import com.t8rin.imagetoolbox.feature.pdf_tools.service.model.PdfToImagesStage
import com.t8rin.imagetoolbox.feature.pdf_tools.service.model.SavedFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfConversionService @Inject constructor(
    private val pdfManager: PdfManager,
    private val imageTransformer: ImageTransformer<Bitmap>,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val shareProvider: ImageShareProvider<Bitmap>,
    private val fileController: FileController,
    private val filenameCreator: FilenameCreator,
    private val activityLogRecorder: ActivityLogRecorder,
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatchersHolder,
) {

    suspend fun preflightPdfToImages(
        uri: Uri,
        password: String?
    ): Result<PdfPreflight> = withContext(dispatchers.ioDispatcher) {
        runCatching {
            password?.let { pdfManager.setMasterPassword(it) }
            val pages = pdfManager.getPdfPages(uri.toString())
            val sizes = pdfManager.getPdfPageSizes(uri.toString())
            PdfPreflight(pageCount = pages.size, pageSizes = sizes)
        }
    }

    fun convertPdfToImages(
        uri: Uri,
        password: String?,
        pages: List<Int>?,
        preset: Preset.Percentage,
    ): Flow<PdfToImagesStage> = flow {
        password?.let { pdfManager.setMasterPassword(it) }
        val results = mutableListOf<Bitmap>()
        var total = pages?.size ?: -1
        try {
            pdfManager.extractPages(
                uri = uri.toString(),
                params = PdfExtractPagesParams(pages = pages, preset = preset)
            ).collect { action ->
                when (action) {
                    is ExtractPagesAction.PagesCount -> {
                        total = action.count
                        emit(PdfToImagesStage.Working(done = 0, total = action.count))
                    }
                    is ExtractPagesAction.Progress -> {
                        val bitmap = action.image as? Bitmap ?: return@collect
                        results += bitmap
                        emit(PdfToImagesStage.PageReady(index = action.index, bitmap = bitmap))
                        emit(PdfToImagesStage.Working(done = results.size, total = total))
                    }
                }
            }
            emit(PdfToImagesStage.Done(results.toList()))
        } catch (error: Throwable) {
            emit(PdfToImagesStage.Failed(error))
        }
    }

    suspend fun savePdfImages(
        bitmaps: List<Bitmap>,
        originalUri: Uri,
        oneTimeSaveLocationUri: String?,
        imageInfo: ImageInfo,
        preset: Preset.Percentage,
        screenRoute: String,
    ): Result<List<SavedFile>> = withContext(dispatchers.ioDispatcher) {
        runCatching {
            val saved = mutableListOf<SavedFile>()
            bitmaps.forEachIndexed { index, bitmap ->
                val effectiveInfo = imageTransformer.applyPresetBy(
                    image = bitmap,
                    preset = preset,
                    currentInfo = imageInfo.copy(originalUri = originalUri.toString())
                )
                val data = imageCompressor.compressAndTransform(
                    image = bitmap,
                    imageInfo = effectiveInfo
                )
                val result = fileController.save(
                    saveTarget = ImageSaveTarget(
                        imageInfo = effectiveInfo,
                        metadata = null,
                        originalUri = originalUri.toString(),
                        sequenceNumber = index + 1,
                        data = data
                    ),
                    keepOriginalMetadata = false,
                    oneTimeSaveLocationUri = oneTimeSaveLocationUri
                )
                if (result is SaveResult.Success) {
                    saved += result.toSavedFile().also {
                        recordFileConvert(
                            title = it.fileName,
                            description = "PDF → ${effectiveInfo.imageFormat.title}",
                            screenRoute = screenRoute,
                            fileUri = it.fileUri,
                            fileName = it.fileName,
                            savePath = it.savePath
                        )
                    }
                } else if (result is SaveResult.Error) {
                    throw result.throwable
                }
            }
            saved
        }
    }

    fun convertImagesToPdf(
        imageUris: List<Uri>,
        scaleSmallImagesToLarge: Boolean,
        preset: Preset.Percentage,
        tempFilename: String,
    ): Flow<ImagesToPdfStage> = flow {
        val total = imageUris.size
        try {
            emit(ImagesToPdfStage.Working(done = 0, total = total))
            val tempPath = pdfManager.createPdf(
                imageUris = imageUris.map { it.toString() },
                params = PdfCreationParams(
                    scaleSmallImagesToLarge = scaleSmallImagesToLarge,
                    preset = preset
                )
            )
            emit(ImagesToPdfStage.Done(tempPdfPath = tempPath))
        } catch (error: Throwable) {
            emit(ImagesToPdfStage.Failed(error))
        }
    }

    suspend fun savePdfBytes(
        tempPdfPath: String,
        targetUri: Uri,
        screenRoute: String,
    ): Result<SavedFile> = withContext(dispatchers.ioDispatcher) {
        runCatching {
            val result = fileController.transferBytes(
                fromUri = tempPdfPath,
                toUri = targetUri.toString()
            )
            if (result is SaveResult.Error) throw result.throwable
            val success = result as? SaveResult.Success
                ?: error("Save skipped or invalid")

            val resolvedFileUri = success.fileUri
                ?: SafUriUtils.documentUriToFileUri(targetUri)?.toString()
                ?: targetUri.toString()
            val resolvedSavePath = success.savingPath
                ?: SafUriUtils.documentUriToParentFileUri(targetUri)?.path.orEmpty()
            val resolvedName = success.fileName
                ?: filenameCreator.getFilename(targetUri.toString())

            SavedFile(
                fileName = resolvedName,
                savedFileUri = success.fileUri ?: targetUri.toString(),
                fileUri = resolvedFileUri,
                savePath = resolvedSavePath
            ).also {
                recordFileConvert(
                    title = it.fileName,
                    description = "图片 → PDF",
                    screenRoute = screenRoute,
                    fileUri = it.fileUri,
                    fileName = it.fileName,
                    savePath = it.savePath
                )
            }
        }
    }

    suspend fun savePdfBytesToDownloads(
        tempPdfPath: String,
        fileName: String,
        screenRoute: String,
    ): Result<SavedFile> = withContext(dispatchers.ioDispatcher) {
        runCatching {
            val targetDir = File(context.getExternalFilesDir(null), DOWNLOADS_DIR).apply { mkdirs() }
            val targetFile = uniqueFile(targetDir, fileName)
            openTempPdfInputStream(tempPdfPath).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            val fileUriString = Uri.fromFile(targetFile).toString()
            SavedFile(
                fileName = targetFile.name,
                savedFileUri = fileUriString,
                fileUri = fileUriString,
                savePath = targetFile.parentFile?.absolutePath.orEmpty()
            ).also {
                recordFileConvert(
                    title = it.fileName,
                    description = "图片 → PDF",
                    screenRoute = screenRoute,
                    fileUri = it.fileUri,
                    fileName = it.fileName,
                    savePath = it.savePath
                )
            }
        }
    }

    private fun openTempPdfInputStream(tempPdfPath: String): java.io.InputStream {
        return when {
            tempPdfPath.startsWith("content://") -> {
                context.contentResolver.openInputStream(Uri.parse(tempPdfPath))
                    ?: throw IllegalStateException("Cannot open content URI: $tempPdfPath")
            }
            tempPdfPath.startsWith("file://") -> FileInputStream(File(tempPdfPath.removePrefix("file://")))
            else -> FileInputStream(File(tempPdfPath))
        }
    }

    suspend fun savePdfImagesToDownloads(
        bitmaps: List<Bitmap>,
        baseName: String,
        originalUri: Uri,
        imageInfo: ImageInfo,
        preset: Preset.Percentage,
        screenRoute: String,
    ): Result<List<SavedFile>> = withContext(dispatchers.ioDispatcher) {
        runCatching {
            val targetDir = File(context.getExternalFilesDir(null), DOWNLOADS_DIR).apply { mkdirs() }
            val saved = mutableListOf<SavedFile>()
            bitmaps.forEachIndexed { index, bitmap ->
                val effectiveInfo = imageTransformer.applyPresetBy(
                    image = bitmap,
                    preset = preset,
                    currentInfo = imageInfo.copy(originalUri = originalUri.toString())
                )
                val data = imageCompressor.compressAndTransform(
                    image = bitmap,
                    imageInfo = effectiveInfo
                )
                val extension = effectiveInfo.imageFormat.extension
                val fileName = "${baseName}_${index + 1}.$extension"
                val targetFile = uniqueFile(targetDir, fileName)
                FileOutputStream(targetFile).use { it.write(data) }
                val fileUri = Uri.fromFile(targetFile).toString()
                saved += SavedFile(
                    fileName = targetFile.name,
                    savedFileUri = fileUri,
                    fileUri = fileUri,
                    savePath = targetFile.parentFile?.absolutePath.orEmpty()
                ).also {
                    recordFileConvert(
                        title = it.fileName,
                        description = "PDF → ${effectiveInfo.imageFormat.title}",
                        screenRoute = screenRoute,
                        fileUri = it.fileUri,
                        fileName = it.fileName,
                        savePath = it.savePath
                    )
                }
            }
            saved
        }
    }

    suspend fun shareImages(
        bitmaps: List<Bitmap>,
        originalUri: Uri,
        imageInfo: ImageInfo,
        preset: Preset.Percentage,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ): Unit = withContext(dispatchers.ioDispatcher) {
        runCatching {
            val uris = bitmaps.mapNotNull { bitmap ->
                val effectiveInfo = imageTransformer.applyPresetBy(
                    image = bitmap,
                    preset = preset,
                    currentInfo = imageInfo.copy(originalUri = originalUri.toString())
                )
                shareProvider.cacheImage(image = bitmap, imageInfo = effectiveInfo)
            }
            shareProvider.shareUris(uris)
            onSuccess()
        }.onFailure(onFailure)
    }

    suspend fun sharePdfFromTempPath(
        tempPdfPath: String,
        onSuccess: () -> Unit,
    ) {
        shareProvider.shareUri(
            uri = tempPdfPath,
            onComplete = onSuccess
        )
    }

    suspend fun sharePdfFromUri(
        uri: Uri,
        onSuccess: () -> Unit,
    ) {
        shareProvider.shareData(
            writeData = { writeable ->
                fileController.transferBytes(
                    fromUri = uri.toString(),
                    to = writeable
                )
            },
            filename = filenameCreator.getFilename(uri.toString()),
            onComplete = onSuccess
        )
    }

    private fun SaveResult.Success.toSavedFile(): SavedFile {
        val savingPath = savingPath.orEmpty()
        val fileName = fileName.orEmpty()
        val fileUri = fileUri.orEmpty()
        val resolvedFileUri = fileUri.takeIf { it.isNotEmpty() }
            ?: runCatching { SafUriUtils.documentUriToFileUri(fileUri.toUri())?.toString() }
                .getOrNull()
            ?: fileUri
        return SavedFile(
            fileName = fileName,
            savedFileUri = fileUri,
            fileUri = resolvedFileUri,
            savePath = savingPath
        )
    }

    private suspend fun recordFileConvert(
        title: String,
        description: String,
        screenRoute: String,
        fileUri: String,
        fileName: String,
        savePath: String,
    ) {
        val payload = JSONObject().apply {
            put("fileUri", fileUri)
            put("fileName", fileName)
            put("savePath", savePath)
        }.toString()
        activityLogRecorder.record(
            category = ActivityCategory.FILE_CONVERT,
            title = title.ifEmpty { fileName },
            appTitle = APP_TITLE,
            description = description,
            screenRoute = screenRoute,
            payload = payload,
            thumbnailUri = fileUri.ifEmpty { null },
            dedupKey = "file_convert_${System.currentTimeMillis()}_${fileName.hashCode()}"
        )
    }

    private fun uniqueFile(dir: File, fileName: String): File {
        val candidate = File(dir, fileName)
        if (!candidate.exists()) return candidate
        val dot = fileName.lastIndexOf('.')
        val (base, ext) = if (dot > 0) fileName.substring(0, dot) to fileName.substring(dot) else fileName to ""
        var index = 1
        while (true) {
            val attempt = File(dir, "${base}_$index$ext")
            if (!attempt.exists()) return attempt
            index += 1
        }
    }

    companion object {
        private const val DOWNLOADS_DIR = "downloads"
        private const val APP_TITLE = "PDF 工具"
    }
}
