package com.wanbaohe.ocr.domain

import kotlinx.serialization.Serializable

@Serializable
data class OcrHistoryEntry(
    val id: String,
    val createdAt: Long,
    val imagePath: String,
    val lines: List<RecognizedLine>,
)
