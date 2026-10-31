package com.wanbaohe.ocr.domain

import android.graphics.Bitmap
import kotlinx.coroutines.flow.StateFlow

interface OcrHistoryRepository {
    val records: StateFlow<List<OcrHistoryEntry>>

    suspend fun save(
        bitmap: Bitmap,
        lines: List<RecognizedLine>,
        existingId: String? = null,
    ): OcrHistoryEntry

    suspend fun loadImage(record: OcrHistoryEntry): Bitmap

    suspend fun delete(id: String)

    suspend fun clear()
}
