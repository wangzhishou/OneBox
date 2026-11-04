package com.shifenmiao.common.localmodel

import android.net.Uri
import kotlinx.coroutines.flow.Flow

data class LocalModelFileInfo(
    val fileName: String,
    val sizeBytes: Long,
    val path: String,
)

sealed interface LocalModelDownloadState {
    data class Running(val progress: Float?) : LocalModelDownloadState
    data object Success : LocalModelDownloadState
    data object Failed : LocalModelDownloadState
}

sealed interface LocalModelImportResult {
    data object Success : LocalModelImportResult
    data class AlreadyExists(val fileName: String) : LocalModelImportResult
    data object Failed : LocalModelImportResult
}

interface LocalModelRepository {
    val downloads: Flow<Map<String, LocalModelDownloadState>>

    suspend fun listInstalledModels(): List<LocalModelFileInfo>

    fun findInstalledModel(modelName: String): LocalModelFileInfo?

    fun startDownload(fileName: String, defaultUrl: String, title: String)

    fun cancelDownload(fileName: String)

    suspend fun importModel(uri: Uri, overwrite: Boolean): LocalModelImportResult

    suspend fun deleteModel(fileName: String): Boolean
}
