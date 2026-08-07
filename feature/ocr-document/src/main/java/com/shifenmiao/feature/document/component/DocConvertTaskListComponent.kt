package com.shifenmiao.feature.document.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.constants.Constants.FILE_POINTS_CONSUME_NUM
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.database.docconvert.entity.DocConvertTaskEntity
import com.shifenmiao.feature.document.domain.OCR_TASK_POLL_INTERVAL_MS
import com.shifenmiao.feature.document.domain.computeRequiredPoints
import com.shifenmiao.feature.document.domain.shouldPoll
import com.shifenmiao.feature.document.repository.DocConvertRepository
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainClickEventFrom
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.model.ocr.OcrTaskStatus
import com.shifenmiao.network.downloader.DownloadState
import com.shifenmiao.network.downloader.ResumableFileDownloader
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import java.io.File

class DocConvertTaskListComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    private val fileController: FileController,
    private val repository: DocConvertRepository,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private enum class ConvertFileType { WORD, EXCEL }

    private data class DownloadRequest(val taskId: String, val type: ConvertFileType)

    val tasks: StateFlow<List<DocConvertTaskEntity>> = repository.getAllTasksFromDb()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val accessTokenState = MutableStateFlow("")
    private var pendingPollingJob: Job? = null

    private val _uiEvents = MutableSharedFlow<DocConvertTaskListUiEvent>(extraBufferCapacity = 8)
    val uiEvents = _uiEvents.asSharedFlow()

    private var submitJob: Job? = null
    private var appContext: Context? = null

    private val downloadQueue = Channel<DownloadRequest>(capacity = Channel.BUFFERED)
    private var downloadWorkerJob: Job? = null

    private val _downloadingTaskIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingTaskIds: StateFlow<Set<String>> = _downloadingTaskIds
    private val queuedDownloadKeys = MutableStateFlow<Set<String>>(emptySet())

    private val downloader = ResumableFileDownloader()

    fun getAccessToken(): String = UrlConstants.ACCESS_TOKEN

    fun startPendingPolling(context: Context, accessToken: String = getAccessToken()) {
        appContext = context.applicationContext
        accessTokenState.value = accessToken
        if (pendingPollingJob != null) return
        if (downloadWorkerJob == null) startDownloadWorker()

        tasks.value
            .asSequence()
            .filter { it.status == OcrTaskStatus.SUCCESS.value }
            .filter { it.localWordPath.isNullOrBlank() && it.localExcelPath.isNullOrBlank() }
            .forEach { enqueueDownloadsForTaskId(it.taskId) }

        pendingPollingJob = componentScope.launch {
            while (isActive) {
                val token = accessTokenState.value
                if (token.isBlank()) {
                    delay(1000)
                    continue
                }

                val now = System.currentTimeMillis()
                val active = tasks.value.filter {
                    it.status == OcrTaskStatus.PENDING.value || it.status == OcrTaskStatus.PROCESSING.value
                }
                if (active.isEmpty()) {
                    delay(1000)
                    continue
                }

                val nextDueAt = active.minOf { it.updatedAt + OCR_TASK_POLL_INTERVAL_MS }
                val waitMs = maxOf(0, nextDueAt - now)
                if (waitMs > 0) {
                    delay(minOf(1000L, waitMs))
                    continue
                }

                val dueTask = active
                    .asSequence()
                    .filter { shouldPoll(it.updatedAt, now) }
                    .minByOrNull { it.updatedAt }
                if (dueTask == null) {
                    delay(200)
                    continue
                }

                repository.touchTaskUpdatedAt(dueTask.taskId, System.currentTimeMillis())
                repository.syncTaskStatus(token, dueTask.taskId)
                    .onSuccess { result ->
                        if (result.retCode == 3) {
                            enqueueDownloadsForTaskId(dueTask.taskId)
                        }
                    }
                    .onFailure { repository.setTaskPollError(dueTask.taskId, it.message) }
            }
        }
    }

    fun stopPendingPolling() {
        pendingPollingJob?.cancel()
        pendingPollingJob = null
        downloadWorkerJob?.cancel()
        downloadWorkerJob = null
    }

    fun cancelSubmitting(context: Context) {
        submitJob?.cancel()
        submitJob = null
        _isSubmitting.value = false
        _uiEvents.tryEmit(DocConvertTaskListUiEvent.Toast(context.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_cancelled)))
    }

    fun cancelAllDownloads(context: Context) {
        downloadWorkerJob?.cancel()
        downloadWorkerJob = null
        _downloadingTaskIds.value = emptySet()
        _uiEvents.tryEmit(DocConvertTaskListUiEvent.Toast(context.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_cancelled)))
    }

    fun submitTask(
        context: Context,
        fileUri: Uri,
        isPdf: Boolean,
        fileSizeBytes: Long = 0,
        accessToken: String = getAccessToken()
    ) {
        if (accessToken.isBlank()) {
            _uiEvents.tryEmit(
                DocConvertTaskListUiEvent.Toast(
                    context.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_submit_missing_token)
                )
            )
            return
        }

        val resolvedFileSizeBytes = resolveFileSizeBytes(context, fileUri, fileSizeBytes)
        val requiredPoints = computeRequiredPoints(resolvedFileSizeBytes)

        ActionUtils.checkPointsAndDo(
            point = requiredPoints,
            onFailure = {
                MainClickEvent(
                    from = MainClickEventFrom.OCR_TASK_LIST,
                    type = MainShowType.BUY_COFFEE
                ).also { AppEventBus.emit(it) }
            }
        ) {
            _isSubmitting.value = true
            submitJob?.cancel()
            submitJob = componentScope.launch {
                try {
                    val fileName =
                        getFileName(context, fileUri) ?: "unknown_${System.currentTimeMillis()}"
                    val inputStream = context.contentResolver.openInputStream(fileUri)
                        ?: run {
                            _uiEvents.tryEmit(
                                DocConvertTaskListUiEvent.Toast(
                                    context.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_submit_open_file_failed)
                                )
                            )
                            return@launch
                        }

                    inputStream.use { stream ->
                        repository.submitFile(accessToken, stream, fileName, isPdf)
                            .onSuccess { taskId ->
                                val newTask = DocConvertTaskEntity(
                                    taskId = taskId,
                                    fileName = fileName,
                                    fileSizeBytes = resolvedFileSizeBytes,
                                    sourcePath = fileUri.toString(),
                                    status = OcrTaskStatus.PENDING.value
                                )
                                repository.insertTask(newTask)
                                BaseUtils.consumePoints(requiredPoints)
                                _uiEvents.tryEmit(
                                    DocConvertTaskListUiEvent.Toast(
                                        context.getString(
                                            com.shifenmiao.feature.ocr.document.R.string.ocr_submit_success,
                                            fileName,
                                            requiredPoints
                                        )
                                    )
                                )
                            }
                            .onFailure { e ->
                                val msg = e.message?.takeIf { it.isNotBlank() }
                                    ?: context.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_submit_failed_unknown)
                                _uiEvents.tryEmit(
                                    DocConvertTaskListUiEvent.Toast(
                                        context.getString(
                                            com.shifenmiao.feature.ocr.document.R.string.ocr_submit_failed,
                                            msg
                                        )
                                    )
                                )
                            }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val msg = e.message?.takeIf { it.isNotBlank() }
                        ?: context.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_submit_failed_unknown)
                    _uiEvents.tryEmit(
                        DocConvertTaskListUiEvent.Toast(
                            context.getString(
                                com.shifenmiao.feature.ocr.document.R.string.ocr_submit_failed,
                                msg
                            )
                        )
                    )
                } finally {
                    _isSubmitting.value = false
                }
            }
        }
    }

    fun downloadDefaultResult(context: Context, task: DocConvertTaskEntity) {
        appContext = context.applicationContext
        enqueueDownloadsForTaskId(task.taskId)
    }

    fun downloadWord(context: Context, task: DocConvertTaskEntity) {
        appContext = context.applicationContext
        enqueueDownloadIfNeeded(task.taskId, ConvertFileType.WORD)
    }

    fun downloadExcel(context: Context, task: DocConvertTaskEntity) {
        appContext = context.applicationContext
        enqueueDownloadIfNeeded(task.taskId, ConvertFileType.EXCEL)
    }

    fun onHttpDownload(context: Context, task: DocConvertTaskEntity, preferWord: Boolean) {
        val url = if (preferWord) task.wordUrl else task.excelUrl
        val path = normalizeDownloadUrl(url) ?: return
        try {
            ActionUtils.openWebBrowser(context, path)
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = e.message?.takeIf { it.isNotBlank() }
                ?: context.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_share_failed_unknown)
            _uiEvents.tryEmit(
                DocConvertTaskListUiEvent.Toast(
                    context.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_download_failed, msg)
                )
            )
        }
    }

    fun copyRemoteUrl(context: Context, task: DocConvertTaskEntity, preferWord: Boolean) {
        val url = if (preferWord) task.wordUrl else task.excelUrl
        val text = normalizeDownloadUrl(url) ?: return
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(task.fileName, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                context,
                context.getString(com.shifenmiao.feature.ocr.document.R.string.copied_to_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteTask(task: DocConvertTaskEntity) {
        componentScope.launch {
            runCatching { task.localWordPath?.takeIf { it.isNotBlank() }?.let { File(it).delete() } }
            runCatching { task.localExcelPath?.takeIf { it.isNotBlank() }?.let { File(it).delete() } }
            repository.deleteTask(task)
        }
    }

    private fun startDownloadWorker() {
        downloadWorkerJob = componentScope.launch {
            while (isActive) {
                val request = downloadQueue.receive()
                queuedDownloadKeys.value = queuedDownloadKeys.value - "${request.taskId}:${request.type.name}"
                val ctx = appContext ?: continue
                val entity = repository.getTaskByTaskId(request.taskId) ?: continue
                val url = normalizeDownloadUrl(
                    when (request.type) {
                    ConvertFileType.WORD -> entity.wordUrl
                    ConvertFileType.EXCEL -> entity.excelUrl
                    }
                ) ?: continue

                val alreadyDownloaded = when (request.type) {
                    ConvertFileType.WORD -> !entity.localWordPath.isNullOrBlank()
                    ConvertFileType.EXCEL -> !entity.localExcelPath.isNullOrBlank()
                }
                if (alreadyDownloaded) continue

                _downloadingTaskIds.value = _downloadingTaskIds.value + request.taskId
                val extension = when (request.type) {
                    ConvertFileType.WORD -> "docx"
                    ConvertFileType.EXCEL -> "xlsx"
                }
                val outputFile = File(ctx.cacheDir, "${entity.fileName}_${entity.taskId}.$extension")

                downloader.download(url, outputFile).collect { state ->
                    when (state) {
                        is DownloadState.Progress -> Unit
                        is DownloadState.Success -> {
                            when (request.type) {
                                ConvertFileType.WORD -> repository.updateLocalWordPath(request.taskId, state.file.absolutePath)
                                ConvertFileType.EXCEL -> repository.updateLocalExcelPath(request.taskId, state.file.absolutePath)
                            }
                            _uiEvents.tryEmit(
                                DocConvertTaskListUiEvent.Toast(
                                    ctx.getString(com.shifenmiao.feature.ocr.document.R.string.doc_convert_file_downloaded)
                                )
                            )
                        }

                        is DownloadState.Failed -> {
                            val msg = state.throwable.message?.takeIf { it.isNotBlank() }
                                ?: ctx.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_download_failed_unknown)
                            _uiEvents.tryEmit(
                                DocConvertTaskListUiEvent.Toast(
                                    ctx.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_download_failed, msg)
                                )
                            )
                        }
                    }
                }

                _downloadingTaskIds.value = _downloadingTaskIds.value - request.taskId
            }
        }
    }

    private fun enqueueDownloadsForTaskId(taskId: String) {
        componentScope.launch {
            val entity = repository.getTaskByTaskId(taskId) ?: return@launch
            if (entity.status != OcrTaskStatus.SUCCESS.value) return@launch

            normalizeDownloadUrl(entity.wordUrl)
                ?.takeIf { entity.localWordPath.isNullOrBlank() }
                ?.let { enqueueDownloadIfNeeded(taskId, ConvertFileType.WORD) }

            normalizeDownloadUrl(entity.excelUrl)
                ?.takeIf { entity.localExcelPath.isNullOrBlank() }
                ?.let { enqueueDownloadIfNeeded(taskId, ConvertFileType.EXCEL) }
        }
    }

    private fun enqueueDownloadIfNeeded(taskId: String, type: ConvertFileType) {
        val key = "$taskId:${type.name}"
        if (queuedDownloadKeys.value.contains(key)) return
        queuedDownloadKeys.value = queuedDownloadKeys.value + key
        val result = downloadQueue.trySend(DownloadRequest(taskId, type))
        if (result.isFailure) {
            queuedDownloadKeys.value = queuedDownloadKeys.value - key
        }
    }

    private fun normalizeDownloadUrl(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return null
        if (value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)) {
            return value
        }
        if (value.startsWith("//")) return "https:$value"
        if (value.startsWith("/")) return "https://aip.baidubce.com$value"
        if (value.contains('.') && !value.contains("://")) return "https://$value"
        return null
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            val path = uri.path
            if (path != null) {
                val cut = path.lastIndexOf('/')
                result = if (cut != -1) path.substring(cut + 1) else path
            }
        }
        return result
    }

    private fun resolveFileSizeBytes(context: Context, uri: Uri, fallback: Long): Long {
        if (fallback > 0) return fallback
        if (uri.scheme != "content") return 0

        val cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.SIZE)
                if (index >= 0) {
                    val size = it.getLong(index)
                    if (size > 0) return size
                }
            }
        }
        return 0
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): DocConvertTaskListComponent
    }
}
