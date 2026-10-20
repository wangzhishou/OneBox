/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.feature.zip.presentation.screenLogic

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import androidx.core.net.toUri
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.utils.runSuspendCatching
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.settings.domain.SettingsProvider
import com.wanbaohe.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.feature.zip.domain.ArchiveUnavailableException
import com.t8rin.imagetoolbox.feature.zip.domain.EncryptedArchiveException
import com.t8rin.imagetoolbox.feature.zip.domain.UnsupportedArchiveFormatException
import com.t8rin.imagetoolbox.feature.zip.domain.ZipArchive
import com.t8rin.imagetoolbox.feature.zip.domain.ZipArchiveEntry
import com.t8rin.imagetoolbox.feature.zip.domain.ZipManager
import com.t8rin.imagetoolbox.feature.zip.domain.ZipProgress
import com.t8rin.imagetoolbox.feature.zip.data.ZipExtractPrefs
import com.t8rin.imagetoolbox.feature.zip.data.resolveExtractStartUri
import com.t8rin.logger.makeLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ZipBrowserComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val sourceUri: Uri,
    @Assisted val onGoBack: () -> Unit,
    private val zipManager: ZipManager,
    private val extractPrefs: ZipExtractPrefs,
    private val activityLogRecorder: ActivityLogRecorder,
    settingsProvider: SettingsProvider,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    /** App 工作目录（用户在设置里授权的保存文件夹），解压选择器的默认落点之一 */
    private val workspaceUri = settingsProvider.settingsState.value.saveFolderUri?.toUri()

    sealed interface State {
        data object Loading : State
        data object Ready : State

        data class Error(
            val isUnsupportedFormat: Boolean,
            val isEncrypted: Boolean = false
        ) : State
    }

    private val _state = mutableStateOf<State>(State.Loading)
    val state by _state

    private val _archiveName = mutableStateOf("")
    val archiveName by _archiveName

    private val _archiveSize = mutableLongStateOf(0L)
    val archiveSize by _archiveSize

    private val _entries = mutableStateOf<List<ZipArchiveEntry>>(emptyList())

    private val _totalSize = mutableLongStateOf(0L)
    val totalSize by _totalSize

    private val _totalCompressedSize = mutableLongStateOf(0L)
    val totalCompressedSize by _totalCompressedSize

    val fileCount: Int
        get() = _entries.value.count { !it.isDirectory && !it.path.endsWith('/') }

    /**
     * 压缩率：压缩后比原始小多少（0..99）。不好算时返回 null，不显示这一项。
     */
    val compressionPercent: Int?
        get() {
            val original = _totalSize.value
            val compressed = _totalCompressedSize.value
            if (original <= 0L || compressed <= 0L || compressed >= original) return null
            return (((original - compressed) * 100.0) / original)
                .roundToInt()
                .coerceIn(1, 99)
        }

    private val _currentFolder = mutableStateOf<String?>(null)
    val currentFolder by _currentFolder

    private val _visibleItems = mutableStateOf<List<ZipBrowserItem>>(emptyList())
    val visibleItems by _visibleItems

    private val _isExtracting = mutableStateOf(false)
    val isExtracting by _isExtracting

    private val _extractProgress = mutableStateOf(ZipProgress())
    val extractProgress by _extractProgress

    private val _loadProgress = mutableStateOf(ZipProgress())
    val loadProgress by _loadProgress

    private var archive: ZipArchive? = null
    private var isReleased = false
    private var isArchiveClosed = false

    private var loadJob: Job? by smartJob()

    private var extractJob: Job? by smartJob {
        _isExtracting.value = false
        _extractProgress.value = ZipProgress()
    }

    init {
        // 缓存/压缩包归组件生命周期：旋转、切语言、暗色切换时组合会重建，
        // 但 retainedComponent 还活着，不能在 onDispose 里关掉。
        componentContext.lifecycle.doOnDestroy { release() }
        loadArchive()
    }

    fun retry() = loadArchive()

    private fun loadArchive() {
        loadJob = componentScope.launch {
            _state.value = State.Loading
            _loadProgress.value = ZipProgress()
            _archiveName.value = zipManager.displayName(sourceUri.toString())
                ?.takeIf { it.isNotBlank() }
                ?: _archiveName.value
            runSuspendCatching {
                zipManager.openArchive(sourceUri.toString()) { progress ->
                    publishLoadProgress(progress)
                }
            }.onSuccess { opened ->
                if (isReleased) {
                    runCatching { opened.close() }
                    return@onSuccess
                }
                archive?.close()
                archive = opened
                _archiveName.value = opened.name
                _archiveSize.value = opened.archiveSize
                _entries.value = opened.entries
                _totalSize.value = opened.totalSize
                _totalCompressedSize.value = opened.totalCompressedSize
                _currentFolder.value = null
                rebuildItems()
                _state.value = State.Ready
            }.onFailure { throwable ->
                _state.value = State.Error(
                    isUnsupportedFormat = throwable is UnsupportedArchiveFormatException,
                    isEncrypted = throwable is EncryptedArchiveException
                )
            }
        }
    }

    fun openFolder(folderPath: String) {
        _currentFolder.value = folderPath.trimEnd('/')
        rebuildItems()
    }

    /**
     * @return true 表示这次返回被消费（回到上一级目录），false 交给页面关闭。
     */
    fun navigateUp(): Boolean {
        val current = _currentFolder.value ?: return false
        _currentFolder.value = current.substringBeforeLast('/', "").ifBlank { null }
        rebuildItems()
        return true
    }

    /**
     * 目录选择器该从哪儿打开。
     *
     * 系统默认从存储根目录打开，而根目录与 Download 根目录都不可写；
     * 这里优先上次选过的目录，其次 App 工作目录，最后 Download（让用户选子目录）。
     */
    fun extractStartUri(): Uri? = resolveExtractStartUri(
        lastDestination = extractPrefs.lastDestination(),
        workspaceUri = workspaceUri,
    )

    /** 记住这次选的目录，下次解压直接从这里开始。 */
    fun rememberExtractDestination(uri: Uri) = extractPrefs.saveLastDestination(uri)

    fun extractAll(
        destinationTreeUri: Uri,
        onResult: (Result<Uri>) -> Unit
    ) {
        val openedArchive = archive
        if (openedArchive == null) {
            onResult(Result.failure(ArchiveUnavailableException()))
            return
        }
        extractJob = componentScope.launch {
            _isExtracting.value = true
            _extractProgress.value = ZipProgress(
                totalBytes = openedArchive.totalSize,
                totalFiles = fileCount
            )

            val result = runSuspendCatching {
                openedArchive.extractAll(destinationTreeUri.toString()) { progress ->
                    publishExtractProgress(progress)
                }.let(Uri::parse)
            }

            _isExtracting.value = false
            result.onSuccess { folderUri ->
                recordArchiveExtracted(folderUri)
            }
            onResult(result)
        }
    }

    fun extractEntry(
        entry: ZipArchiveEntry,
        destinationUri: Uri,
        onResult: (Result<Uri>) -> Unit
    ) {
        val openedArchive = archive
        if (openedArchive == null) {
            onResult(Result.failure(ArchiveUnavailableException()))
            return
        }
        extractJob = componentScope.launch {
            _isExtracting.value = true
            _extractProgress.value = ZipProgress(
                totalBytes = entry.size.coerceAtLeast(0L),
                totalFiles = 1
            )

            val result = runSuspendCatching {
                openedArchive.extractEntry(
                    entry = entry,
                    destinationUri = destinationUri.toString()
                ) { progress ->
                    publishExtractProgress(progress)
                }
                destinationUri
            }

            _isExtracting.value = false
            result.onSuccess { fileUri ->
                recordArchiveEntryExtracted(entry.name, fileUri)
            }
            onResult(result)
        }
    }

    /**
     * 写操作记录（操作记录页可点回解压出的文件夹）。失败只打日志，
     * 绝不能影响解压成功提示。
     */
    private suspend fun recordArchiveExtracted(folderUri: Uri) {
        runCatching {
            activityLogRecorder.recordArchiveExtracted(
                archiveName = _archiveName.value,
                fileCount = fileCount,
                folderName = zipManager.displayName(folderUri.toString()).orEmpty(),
                archiveUri = sourceUri.toString(),
                folderUri = folderUri.toString()
            )
        }.onFailure { it.makeLog("ZipBrowserComponent") }
    }

    private suspend fun recordArchiveEntryExtracted(fileName: String, fileUri: Uri) {
        runCatching {
            activityLogRecorder.recordArchiveEntryExtracted(
                fileName = fileName,
                // 单文件解压拿不到父目录显示名，记录里用「所选文件夹」兜底
                folderName = "",
                archiveUri = sourceUri.toString(),
                fileUri = fileUri.toString()
            )
        }.onFailure { it.makeLog("ZipBrowserComponent") }
    }

    fun cancelExtraction() {
        // 保留 extractJob 引用，等它真正结束后 release 再关 ZipFile
        extractJob?.cancel()
        _isExtracting.value = false
        _extractProgress.value = ZipProgress()
    }

    private fun publishLoadProgress(progress: ZipProgress) {
        componentScope.launch {
            if (!isReleased) _loadProgress.value = progress
        }
    }

    private fun publishExtractProgress(progress: ZipProgress) {
        componentScope.launch {
            if (!isReleased) _extractProgress.value = progress
        }
    }

    private fun release() {
        if (isReleased) return
        isReleased = true

        loadJob?.cancel()

        val runningExtract = extractJob
        if (runningExtract != null && !runningExtract.isCompleted) {
            // 读线程还在用 ZipFile，等它退出/取消完成后再删缓存
            runningExtract.invokeOnCompletion { closeArchive() }
            runningExtract.cancel()
        } else {
            closeArchive()
        }
    }

    private fun closeArchive() {
        if (isArchiveClosed) return
        isArchiveClosed = true
        val opened = archive
        archive = null
        runCatching { opened?.close() }
    }

    private fun rebuildItems() {
        val files = _entries.value.filter { !it.isDirectory && !it.path.endsWith('/') }
        val folderPaths = mutableSetOf<String>()

        fun registerAncestors(path: String) {
            var parent = path
            while (parent.isNotBlank()) {
                folderPaths.add(parent)
                parent = parent.substringBeforeLast('/', "")
            }
        }

        files.forEach { registerAncestors(it.parentPath) }
        _entries.value
            .filter { it.isDirectory || it.path.endsWith('/') }
            .forEach { registerAncestors(it.path.trimEnd('/')) }

        val current = _currentFolder.value.orEmpty()

        val folders = folderPaths
            .filter { it.substringBeforeLast('/', "") == current }
            .sortedBy { it.substringAfterLast('/').lowercase() }
            .map { folderPath ->
                val prefix = "$folderPath/"
                ZipBrowserItem.Folder(
                    path = folderPath,
                    name = folderPath.substringAfterLast('/'),
                    fileCount = files.count { it.path.startsWith(prefix) },
                    totalSize = files.filter { it.path.startsWith(prefix) }.sumOf { it.size }
                )
            }

        val currentFiles = files
            .filter { it.parentPath == current }
            .sortedBy { it.name.lowercase() }
            .map(ZipBrowserItem::File)

        _visibleItems.value = folders + currentFiles
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            sourceUri: Uri,
            onGoBack: () -> Unit,
        ): ZipBrowserComponent
    }
}
