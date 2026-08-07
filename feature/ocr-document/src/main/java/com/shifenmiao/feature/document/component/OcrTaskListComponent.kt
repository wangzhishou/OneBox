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
import com.shifenmiao.database.ocr.entity.PaddleOcrTaskEntity
import com.shifenmiao.feature.document.domain.OCR_TASK_POLL_INTERVAL_MS
import com.shifenmiao.feature.document.domain.shouldPoll
import com.shifenmiao.feature.ocr.document.R
import com.shifenmiao.model.ocr.OcrTaskStatus
import com.shifenmiao.network.downloader.DownloadState
import com.shifenmiao.network.downloader.ResumableFileDownloader
import com.shifenmiao.feature.document.repository.PaddleOcrRepository
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainClickEventFrom
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.webview.HtmlMarkdownConverter
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import com.shifenmiao.model.event.AppEventBus
import java.io.File
import kotlin.math.ceil
import kotlin.math.min

class OcrTaskListComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    private val fileController: FileController,
    private val repository: PaddleOcrRepository,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val tasks: StateFlow<List<PaddleOcrTaskEntity>> = repository.getAllTasksFromDb()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting
    private val accessTokenState = MutableStateFlow("")
    private var pendingPollingJob: Job? = null
    private val _uiEvents = MutableSharedFlow<OcrTaskListUiEvent>(extraBufferCapacity = 8)
    val uiEvents = _uiEvents.asSharedFlow()
    private var submitJob: Job? = null
    private var appContext: Context? = null
    private val downloadQueue = Channel<String>(capacity = Channel.BUFFERED)
    private var downloadWorkerJob: Job? = null
    private val _downloadingTaskIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingTaskIds: StateFlow<Set<String>> = _downloadingTaskIds
    private val downloader = ResumableFileDownloader()

    fun getAccessToken(): String = "1sd@#%@3s;ds1a-v5"

    fun startPendingPolling(context: Context, accessToken: String = getAccessToken()) {
        appContext = context.applicationContext
        accessTokenState.value = accessToken
        if (pendingPollingJob != null) return
        if (downloadWorkerJob == null) startDownloadWorker()
        tasks.value
            .asSequence()
            .filter { it.status == OcrTaskStatus.SUCCESS.value }
            .filter { it.localPath.isNullOrBlank() }
            .forEach { enqueueDownloadIfNeeded(it.taskId) }

        pendingPollingJob = componentScope.launch {
            while (isActive) {
                val token = accessTokenState.value
                if (token.isBlank()) {
                    delay(1000)
                    continue
                }

                val now = System.currentTimeMillis()
                val pendingTasks = tasks.value.filter { it.status == OcrTaskStatus.PENDING.value }
                if (pendingTasks.isEmpty()) {
                    delay(1000)
                    continue
                }

                val nextDueAt = pendingTasks.minOf { it.updatedAt + OCR_TASK_POLL_INTERVAL_MS }
                val waitMs = maxOf(0, nextDueAt - now)
                if (waitMs > 0) {
                    delay(min(1000L, waitMs))
                    continue
                }

                val dueTask = pendingTasks
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
                        if (result.status == OcrTaskStatus.SUCCESS.value) {
                            enqueueDownloadIfNeeded(dueTask.taskId)
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
        _uiEvents.tryEmit(OcrTaskListUiEvent.Toast(context.getString(R.string.ocr_cancelled)))
    }

    fun cancelAllDownloads(context: Context) {
        downloadWorkerJob?.cancel()
        downloadWorkerJob = null
        _downloadingTaskIds.value = emptySet()
        _uiEvents.tryEmit(OcrTaskListUiEvent.Toast(context.getString(R.string.ocr_cancelled)))
    }

    fun submitTask(
        context: Context,
        fileUri: Uri,
        isPdf: Boolean,
        fileSizeBytes: Long = 0,
        accessToken: String = getAccessToken()
    ) {
        if (accessToken.isBlank()) {
            _uiEvents.tryEmit(OcrTaskListUiEvent.Toast(context.getString(R.string.ocr_submit_missing_token)))
            return
        }

        val resolvedFileSizeBytes = resolveFileSizeBytes(context, fileUri, fileSizeBytes)
        val requiredPoints = computeRequiredPoints(resolvedFileSizeBytes)

        ActionUtils.checkPointsAndDo(
            point = requiredPoints,
            onFailure = {
                AppEventBus.emit(
                    MainClickEvent(
                        from = MainClickEventFrom.OCR_TASK_LIST,
                        type = MainShowType.BUY_COFFEE
                    )
                )
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
                            _uiEvents.tryEmit(OcrTaskListUiEvent.Toast(context.getString(R.string.ocr_submit_open_file_failed)))
                            return@launch
                        }

                    inputStream.use { stream ->
                        repository.submitFileDetailed(accessToken, stream, fileName, null)
                            .onSuccess { taskId ->
                                val newTask = PaddleOcrTaskEntity(
                                    taskId = taskId,
                                    fileName = fileName,
                                    fileSizeBytes = resolvedFileSizeBytes,
                                    sourcePath = fileUri.toString(),
                                    status = OcrTaskStatus.PENDING.value
                                )
                                repository.insertTask(newTask)
                                BaseUtils.consumePoints(requiredPoints)
                                _uiEvents.tryEmit(
                                    OcrTaskListUiEvent.Toast(
                                        context.getString(
                                            R.string.ocr_submit_success,
                                            fileName,
                                            requiredPoints
                                        )
                                    )
                                )
                            }
                            .onFailure { e ->
                                val msg = e.message?.takeIf { it.isNotBlank() }
                                    ?: context.getString(R.string.ocr_submit_failed_unknown)
                                _uiEvents.tryEmit(
                                    OcrTaskListUiEvent.Toast(
                                        context.getString(R.string.ocr_submit_failed, msg)
                                    )
                                )
                            }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val msg = e.message?.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.ocr_submit_failed_unknown)
                    _uiEvents.tryEmit(
                        OcrTaskListUiEvent.Toast(
                            context.getString(
                                R.string.ocr_submit_failed,
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

    private fun computeRequiredPoints(fileSizeBytes: Long): Int {
        if (fileSizeBytes <= 0) return FILE_POINTS_CONSUME_NUM
        val mbUnits = ceil(fileSizeBytes.toDouble() / (1024.0 * 1024.0))
            .toLong()
            .coerceAtLeast(1L)
        val points = mbUnits * FILE_POINTS_CONSUME_NUM.toLong()
        return points.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun syncActiveTasks(accessToken: String) {
        componentScope.launch {
            tasks.value.filter {
                it.status == OcrTaskStatus.PENDING.value ||
                        it.status == OcrTaskStatus.PROCESSING.value
            }.forEach { task ->
                repository.syncTaskStatus(accessToken, task.taskId)
            }
        }
    }

    fun downloadMarkdown(context: Context, task: PaddleOcrTaskEntity) {
        appContext = context.applicationContext
        enqueueDownloadIfNeeded(task.taskId)
    }

    private fun startDownloadWorker() {
        downloadWorkerJob = componentScope.launch {
            while (isActive) {
                val taskId = downloadQueue.receive()
                val ctx = appContext ?: continue
                val entity = repository.getTaskByTaskId(taskId) ?: continue
                val url = entity.markdownUrl ?: continue
                if (!entity.localPath.isNullOrBlank()) continue

                _downloadingTaskIds.value = _downloadingTaskIds.value + taskId
                val rawFile = File(ctx.cacheDir, "${entity.fileName}_${taskId}_raw.md")
                val convertedFile = File(ctx.cacheDir, "${entity.fileName}_${taskId}.md")
                downloader.download(url, rawFile).collect { state ->
                    when (state) {
                        is DownloadState.Progress -> Unit
                        is DownloadState.Success -> {
                            val raw = runCatching { state.file.readText() }.getOrDefault("")
                            val converted = runCatching {
                                HtmlMarkdownConverter.convertHtmlTablesToGfm(raw)
                            }.getOrElse { raw }
                            runCatching { convertedFile.writeText(converted) }

                            repository.updateRawDownloadedPath(taskId, rawFile.absolutePath)
                            repository.updateLocalPath(taskId, convertedFile.absolutePath)
                            _uiEvents.tryEmit(
                                OcrTaskListUiEvent.Toast(
                                    ctx.getString(R.string.ocr_markdown_downloaded)
                                )
                            )
                        }

                        is DownloadState.Failed -> {
                            val msg = state.throwable.message?.takeIf { it.isNotBlank() }
                                ?: ctx.getString(R.string.ocr_download_failed_unknown)
                            _uiEvents.tryEmit(
                                OcrTaskListUiEvent.Toast(
                                    ctx.getString(R.string.ocr_download_failed, msg)
                                )
                            )
                        }
                    }
                }

                _downloadingTaskIds.value = _downloadingTaskIds.value - taskId
            }
        }
    }

    private fun enqueueDownloadIfNeeded(taskId: String) {
        if (_downloadingTaskIds.value.contains(taskId)) return
        downloadQueue.trySend(taskId)
    }

    fun onHttpDownload(context: Context, task: PaddleOcrTaskEntity) {
        val path = task.parseResultUrl ?: return
        try {
            ActionUtils.openWebBrowser(context, path)
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = e.message?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.ocr_share_failed_unknown)
            _uiEvents.tryEmit(
                OcrTaskListUiEvent.Toast(
                    context.getString(
                        R.string.ocr_download_failed,
                        msg
                    )
                )
            )
        }
    }

    fun copyMarkdownContent(context: Context, task: PaddleOcrTaskEntity) {
        val path = task.localPath ?: return
        val file = File(path)
        if (!file.exists()) return

        try {
            val content = file.readText()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(task.fileName, content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                context,
                context.getString(R.string.copied_to_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyRemoteMarkdownUrl(context: Context, task: PaddleOcrTaskEntity) {
        val url = task.markdownUrl?.takeIf { it.isNotBlank() } ?: return
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(task.fileName, url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                context,
                context.getString(R.string.copied_to_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteTask(task: PaddleOcrTaskEntity) {
        componentScope.launch {
            runCatching {
                task.localPath?.takeIf { it.isNotBlank() }?.let { File(it).delete() }
            }
            runCatching {
                task.rawDownloadedPath?.takeIf { it.isNotBlank() }?.let { File(it).delete() }
            }
            repository.deleteTask(task)
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): OcrTaskListComponent
    }
}
