package com.t8rin.imagetoolbox.feature.pdf_tools.service.model

import android.graphics.Bitmap
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize

data class PdfPreflight(
    val pageCount: Int,
    val pageSizes: List<IntegerSize>
)

sealed interface PdfToImagesStage {
    data class Working(val done: Int, val total: Int) : PdfToImagesStage
    data class PageReady(val index: Int, val bitmap: Bitmap) : PdfToImagesStage
    data class Done(val bitmaps: List<Bitmap>) : PdfToImagesStage
    data class Failed(val cause: Throwable) : PdfToImagesStage
}

sealed interface ImagesToPdfStage {
    data class Working(val done: Int, val total: Int) : ImagesToPdfStage
    data class Done(val tempPdfPath: String) : ImagesToPdfStage
    data class Failed(val cause: Throwable) : ImagesToPdfStage
}

data class SavedFile(
    val fileName: String,
    val savedFileUri: String,
    val fileUri: String,
    val savePath: String,
)
