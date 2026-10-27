package com.wanbaohe.textcard.domain

import android.graphics.Bitmap
import com.wanbaohe.textcard.domain.model.TextCardDocument
import kotlinx.coroutines.flow.StateFlow

data class TextCardProjectSummary(
    val id: String,
    val name: String,
    val updatedAt: Long,
    val thumbnailPath: String?,
    val pageCount: Int,
)

data class StoredTextCardProject(
    val summary: TextCardProjectSummary,
    val document: TextCardDocument,
)

interface TextCardProjectStore {
    val projects: StateFlow<List<TextCardProjectSummary>>

    suspend fun refresh()

    suspend fun save(
        id: String,
        name: String,
        document: TextCardDocument,
        pageCount: Int,
        thumbnail: Bitmap? = null,
    ): TextCardDocument

    suspend fun load(id: String): StoredTextCardProject

    suspend fun rename(id: String, name: String)

    suspend fun delete(id: String)
}
