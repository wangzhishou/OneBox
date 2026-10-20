/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.zip.presentation.screenLogic

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ShareProvider
import com.t8rin.imagetoolbox.core.domain.utils.runSuspendCatching
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.imagetoolbox.feature.zip.domain.ZipManager
import com.t8rin.imagetoolbox.feature.zip.domain.ZipProgress
import com.t8rin.imagetoolbox.feature.zip.domain.ZipSourceFile
import com.t8rin.logger.makeLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ZipComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUris: List<Uri>?,
    @Assisted val onGoBack: () -> Unit,
    private val zipManager: ZipManager,
    private val shareProvider: ShareProvider,
    private val activityLogRecorder: ActivityLogRecorder,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    init {
        debounce {
            initialUris?.let(::setUris)
        }
    }

    private val _uris = mutableStateOf<List<Uri>>(emptyList())
    val uris by _uris

    private val _sourceFiles = mutableStateOf<List<ZipSourceFile>>(emptyList())
    val sourceFiles by _sourceFiles

    val totalSize: Long
        get() = sourceFiles.sumOf { it.size }

    private val _savedArchiveUri = mutableStateOf<Uri?>(null)
    val savedArchiveUri by _savedArchiveUri

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving by _isSaving

    private val _progress = mutableStateOf(ZipProgress())
    val progress by _progress

    private var describeJob: Job? by smartJob()

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
        _progress.update { ZipProgress() }
    }

    fun setUris(newUris: List<Uri>) {
        _uris.update { newUris.distinct() }
        _savedArchiveUri.update { null }
        refreshSourceFiles()
    }

    fun addUris(list: List<Uri>) = setUris(uris + list)

    fun removeUri(uri: Uri) {
        _uris.update { it - uri }
        _savedArchiveUri.update { null }
        refreshSourceFiles()
    }

    /**
     * 解析文件元数据（真实文件名、大小、类型）。先放一组占位数据，
     * 避免列表在查询期间空一拍。
     */
    private fun refreshSourceFiles() {
        val snapshot = _uris.value
        _sourceFiles.value = snapshot.map { uri ->
            val fallbackName = uri.lastPathSegment
                ?.substringAfterLast('/')
                ?.substringAfterLast(':')
                ?.takeIf { it.isNotBlank() }
                ?: uri.toString()
            ZipSourceFile(
                uri = uri.toString(),
                name = fallbackName,
                size = 0L,
                extension = fallbackName.substringAfterLast('.', "").lowercase(),
                mimeType = null
            )
        }
        if (snapshot.isEmpty()) return

        describeJob = componentScope.launch {
            val described = zipManager.describeSources(snapshot.map { it.toString() })
            if (_uris.value == snapshot) {
                _sourceFiles.value = described
            }
        }
    }

    /**
     * 一步保存：用户先在系统保存对话框里选好位置，压缩结果直接写进去。
     */
    fun saveArchive(
        destinationUri: Uri,
        onResult: (Result<Uri>) -> Unit
    ) {
        if (_uris.value.isEmpty()) return

        savingJob = componentScope.launch {
            _isSaving.value = true
            _progress.value = ZipProgress(
                totalBytes = totalSize,
                totalFiles = _uris.value.size
            )

            val result = runSuspendCatching {
                zipManager.compress(
                    files = _uris.value.map(Uri::toString),
                    destinationUri = destinationUri.toString(),
                    onProgress = ::publishProgress
                )
                destinationUri
            }

            _isSaving.value = false
            result.onSuccess { savedUri ->
                _savedArchiveUri.value = savedUri
                recordArchiveCreated(savedUri)
            }
            onResult(result)
        }
    }

    /**
     * 写操作记录（操作记录页可点回压缩包）。失败只打日志，绝不能影响保存成功提示。
     */
    private suspend fun recordArchiveCreated(archiveUri: Uri) {
        runCatching {
            activityLogRecorder.recordArchiveCreated(
                archiveName = zipManager.displayName(archiveUri.toString()).orEmpty(),
                fileCount = _uris.value.size,
                archiveUri = archiveUri.toString()
            )
        }.onFailure { it.makeLog("ZipComponent") }
    }

    fun shareSavedArchive(onComplete: () -> Unit) {
        val uri = _savedArchiveUri.value ?: return
        savingJob = componentScope.launch {
            _isSaving.value = true
            _progress.value = ZipProgress()
            runSuspendCatching {
                shareProvider.shareUri(
                    uri = uri.toString(),
                    onComplete = {
                        _isSaving.value = false
                        onComplete()
                    }
                )
            }.onFailure {
                _isSaving.value = false
            }
        }
    }

    private fun publishProgress(progress: ZipProgress) {
        // 进度回调在 IO 线程，统一回到主线程写 Compose state
        componentScope.launch { _progress.value = progress }
    }

    fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.value = false
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUris: List<Uri>?,
            onGoBack: () -> Unit,
        ): ZipComponent
    }
}
