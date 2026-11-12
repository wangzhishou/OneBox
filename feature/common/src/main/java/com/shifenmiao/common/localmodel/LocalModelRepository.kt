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
