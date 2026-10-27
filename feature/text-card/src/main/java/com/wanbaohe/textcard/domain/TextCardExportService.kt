package com.wanbaohe.textcard.domain

import com.wanbaohe.textcard.domain.model.TextCardDocument
import com.wanbaohe.textcard.domain.render.TextCardLayout

data class SavedTextCardImage(
    val page: Int,
    val uri: String,
    val fileName: String,
    val savingPath: String,
)

data class TextCardExportResult(
    val saved: List<SavedTextCardImage>,
    val failedPages: List<Int>,
    val shareFailed: Boolean = false,
)

interface TextCardExportService {
    suspend fun export(
        document: TextCardDocument,
        layout: TextCardLayout,
    ): TextCardExportResult
}
