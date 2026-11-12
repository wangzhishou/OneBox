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

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.shifenmiao.model.channel.OnDeviceAiCapabilities
import com.shifenmiao.model.remote.LocalTranslationModel
import com.shifenmiao.network.downloader.BackgroundDownloadRequest
import com.shifenmiao.network.downloader.BackgroundDownloadService
import com.shifenmiao.network.downloader.BackgroundDownloadState
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class DefaultLocalModelRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backgroundDownloadService: BackgroundDownloadService,
    private val dispatchersHolder: DispatchersHolder,
) : LocalModelRepository {
    private val capabilities = OnDeviceAiCapabilities.fromFlavor()
    private val modelsDir: File get() = File(context.filesDir, MODELS_DIR_NAME)

    override val downloads: Flow<Map<String, LocalModelDownloadState>> =
        backgroundDownloadService.downloads.map { states ->
            if (!capabilities.isLocalModelManagementSupported) {
                emptyMap()
            } else {
                states.mapNotNull { (id, state) ->
                    if (!id.startsWith(MODEL_DOWNLOAD_PREFIX)) return@mapNotNull null
                    val download = when (state) {
                        is BackgroundDownloadState.Running -> LocalModelDownloadState.Running(
                            progress = state.progress,
                        )

                        is BackgroundDownloadState.Success -> LocalModelDownloadState.Success
                        is BackgroundDownloadState.Failed -> LocalModelDownloadState.Failed
                    }
                    state.request.destination.name to download
                }.toMap()
            }
        }

    override suspend fun listInstalledModels(): List<LocalModelFileInfo> =
        withContext(dispatchersHolder.ioDispatcher) {
            if (!capabilities.isLocalModelManagementSupported) return@withContext emptyList()
            modelsDir.listFiles()
                ?.filter { it.isFile && it.extension == MODEL_EXTENSION }
                ?.sortedBy { it.name.lowercase() }
                ?.mapNotNull { it.toFileInfo() }
                .orEmpty()
        }

    override fun findInstalledModel(modelName: String): LocalModelFileInfo? {
        if (!capabilities.isLocalModelManagementSupported) return null
        if (modelName.contains('/') || modelName.contains('\\')) return null
        return File(modelsDir, "$modelName.$MODEL_EXTENSION").toFileInfo()
    }

    override fun startDownload(fileName: String, defaultUrl: String, title: String) {
        if (!capabilities.isLocalModelManagementSupported) return
        backgroundDownloadService.start(
            BackgroundDownloadRequest(
                id = modelDownloadId(fileName),
                url = resolveDownloadUrl(fileName, defaultUrl),
                destination = File(modelsDir, fileName),
                title = title,
            )
        )
    }

    override fun cancelDownload(fileName: String) {
        backgroundDownloadService.cancel(modelDownloadId(fileName))
    }

    override suspend fun importModel(uri: Uri, overwrite: Boolean): LocalModelImportResult =
        withContext(dispatchersHolder.ioDispatcher) {
            if (!capabilities.isLocalModelManagementSupported) {
                return@withContext LocalModelImportResult.Failed
            }
            try {
                val fileName = resolveDisplayName(uri)
                    ?: return@withContext LocalModelImportResult.Failed
                val destination = File(modelsDir, fileName)
                if (destination.exists() && !overwrite) {
                    return@withContext LocalModelImportResult.AlreadyExists(fileName)
                }
                modelsDir.mkdirs()
                val input = context.contentResolver.openInputStream(uri)
                    ?: return@withContext LocalModelImportResult.Failed
                input.use { source ->
                    destination.outputStream().use { target -> source.copyTo(target) }
                }
                LocalModelImportResult.Success
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                LocalModelImportResult.Failed
            }
        }

    override suspend fun deleteModel(fileName: String): Boolean =
        withContext(dispatchersHolder.ioDispatcher) {
            if (!capabilities.isLocalModelManagementSupported) return@withContext false
            val deleted = File(modelsDir, fileName).delete()
            if (deleted) clearInferenceCaches(fileName)
            deleted
        }

    private fun resolveDownloadUrl(fileName: String, defaultUrl: String): String {
        val config = RemoteConfigStorage.getRemoteConfig()
        val configuredUrl = when (fileName) {
            "gemma3-1b-it-int4.litertlm" -> config.localChatGemma3ModelUrl
            "Qwen3-0.6B.litertlm" -> config.localChatQwen3ModelUrl
            LocalTranslationModel.FILE_NAME -> config.localTranslationModelUrl
            else -> null
        }
        return configuredUrl?.takeIf { it.isNotBlank() } ?: defaultUrl
    }

    private fun clearInferenceCaches(fileName: String) {
        val dirs = listOf(File(context.filesDir, INFERENCE_CACHE_DIR_NAME), context.cacheDir)
        dirs.forEach { dir ->
            dir.listFiles { file -> file.name.startsWith("${fileName}_") }
                ?.forEach { file -> runCatching { file.delete() } }
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        val fromProvider = runCatching {
            context.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            }
        }.getOrNull()
        return (fromProvider ?: uri.lastPathSegment)
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.takeIf { it.isNotBlank() }
    }

    private fun File.toFileInfo(): LocalModelFileInfo? =
        takeIf { it.isFile }?.let {
            LocalModelFileInfo(
                fileName = it.name,
                sizeBytes = it.length(),
                path = it.absolutePath,
            )
        }

    private fun modelDownloadId(fileName: String) = "$MODEL_DOWNLOAD_PREFIX$fileName"

    private companion object {
        const val MODEL_DOWNLOAD_PREFIX = "local-model:"
        const val MODELS_DIR_NAME = "local_models"
        const val MODEL_EXTENSION = "litertlm"
        const val INFERENCE_CACHE_DIR_NAME = "litertlm_cache"
    }
}
