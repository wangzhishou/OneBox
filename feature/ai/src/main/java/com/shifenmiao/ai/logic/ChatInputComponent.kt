package com.shifenmiao.ai.logic

import android.content.Intent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.shifenmiao.ai.upload.FileAnalyzer
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.model.ai.AttachedMedia
import com.shifenmiao.model.ai.AttachmentProcessingState
import com.shifenmiao.model.ai.ChatInputState
import com.shifenmiao.network.api.ApiService
import android.net.Uri
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.provider.OpenableColumns
import androidx.core.net.toUri
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * 用于在 Activity 重建时保持输入状态的 Holder
 */
private class InputStateHolder : InstanceKeeper.Instance {
    var inputText: String = ""
    var isExpanded: Boolean = false
    var inputMore: Boolean = false
}

class ChatInputComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @ApplicationContext private val context: Context,
    settingsManager: SettingsManager,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    apiService: ApiService,
    fileController: FileController,
    private val fileAnalyzer: FileAnalyzer,
) : CommonComponent(
    settingsManager,
    dispatchersHolder,
    componentContext,
    appDatabase,
    apiService,
    fileController
) {

    // 使用 InstanceKeeper 保持输入状态，确保跳转第三方返回后输入内容不丢失
    private val inputStateHolder: InputStateHolder =
        instanceKeeper.getOrCreate("ChatInputStateHolder") { InputStateHolder() }

    private val _inputChatInputState = MutableStateFlow(
        ChatInputState(
            inputText = inputStateHolder.inputText,
            isExpanded = inputStateHolder.isExpanded,
            inputMore = inputStateHolder.inputMore,
        )
    )
    val chatInputState: StateFlow<ChatInputState> = _inputChatInputState

    private val _recognizedFinished = MutableStateFlow(false)
    private val attachmentProcessingJobs = mutableMapOf<Uri, Job>()

    // 提示词相关 — 使用 Category + ChatPromptEntity (item联表查询)
    private val chatPromptDao = appDatabase.chatPromptDao()
    private val categoryDao = appDatabase.categoryDao()
    private val defaultCategory = Category(id = 0, name = "全部", canEdit = false)

    private val _promptCategories = MutableStateFlow(listOf(defaultCategory))
    val promptCategories: StateFlow<List<Category>> = _promptCategories

    fun promptListFlow(categoryId: Int): Flow<List<PromptEntity>> = if (categoryId == 0) {
        chatPromptDao.getAllPromptsFlow()
    } else {
        chatPromptDao.getPromptsByCategoryId(categoryId, com.shifenmiao.model.ListItemType.PROMPT.id)
    }

    // 最近问题相关
    private val messageDao = appDatabase.messageDao()
    private val _recentQuestions = MutableStateFlow<List<String>>(emptyList())
    val recentQuestions: StateFlow<List<String>> = _recentQuestions

    init {
        initCallBack()
        loadPromptCategories()
        loadRecentQuestions()
    }

    private fun initCallBack() {
        componentScope.launch(ioDispatcher) {
            // 初始化语音识别回调

        }
    }

    private fun loadPromptCategories() {
        componentScope.launch(ioDispatcher) {
            categoryDao.getAllCategories().collectLatest { categories ->
                _promptCategories.value = listOf(defaultCategory) + categories
            }
        }
    }

    private fun loadRecentQuestions() {
        componentScope.launch(ioDispatcher) {
            messageDao.getRecentDistinctQuestions().collectLatest { questions ->
                _recentQuestions.value = questions
            }
        }
    }

    fun onDispose() {
        cancelAllAttachmentProcessingJobs()
        componentScope.cancel()
        // 清空 holder 状态
        inputStateHolder.inputText = ""
        inputStateHolder.isExpanded = false
        inputStateHolder.inputMore = false
        _inputChatInputState.value = _inputChatInputState.value.copy(
            inputText = "",
            isExpanded = false,
            selectedSystemPromptTitle = null,
            selectedSystemPromptText = null,
        )
        _recognizedFinished.value = false
    }

    fun clearInputText() {
        inputStateHolder.inputText = ""
        inputStateHolder.isExpanded = false
        _inputChatInputState.value = _inputChatInputState.value.copy(
            inputText = "",
            cursorStart = 0,
            cursorEnd = 0,
            isExpanded = false,
            selectedSystemPromptTitle = null,
            selectedSystemPromptText = null,
        )
    }

    fun selectSystemPrompt(title: String?, promptText: String?) {
        _inputChatInputState.value = _inputChatInputState.value.copy(
            selectedSystemPromptTitle = title,
            selectedSystemPromptText = promptText,
        )
    }

    fun clearSystemPrompt() {
        _inputChatInputState.value = _inputChatInputState.value.copy(
            selectedSystemPromptTitle = null,
            selectedSystemPromptText = null,
        )
    }

    fun addAttachment(uri: Uri, mimeType: String) {
        val current = _inputChatInputState.value.attachedMedia.toMutableList()
        if (current.any { it.uri == uri }) return
        if (current.size >= 6) {
            ActionUtils.showToast(R.string.ai_attachment_limit_reached)
            return
        }
        persistReadPermissionIfPossible(uri)
        val name = getFileName(uri)
        val size = getFileSize(uri)
        val isImage = mimeType.startsWith("image/")
        val media = AttachedMedia(
            uri = uri,
            name = name,
            mimeType = mimeType,
            size = size,
            isImage = isImage,
            processingState = if (isImage) {
                AttachmentProcessingState.PROCESSING(com.shifenmiao.model.ai.ProcessingStep.CHECKING)
            } else {
                AttachmentProcessingState.IDLE
            }
        )
        current.add(media)
        _inputChatInputState.value = _inputChatInputState.value.copy(attachedMedia = current)

        if (media.isImage) {
            startImagePreprocessing(media, enableCompression = _inputChatInputState.value.enableImageCompression)
        }
    }

    /**
     * 恢复已有附件（编辑消息时复用已有数据，跳过预览生成）。
     */
    fun restoreAttachment(media: AttachedMedia) {
        val current = _inputChatInputState.value.attachedMedia.toMutableList()
        if (current.any { it.uri == media.uri }) return
        if (current.size >= 6) {
            ActionUtils.showToast(R.string.ai_attachment_limit_reached)
            return
        }
        current.add(media)
        _inputChatInputState.value = _inputChatInputState.value.copy(attachedMedia = current)
    }

    private fun getFileName(uri: Uri): String {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }
                } else {
                    null
                }
            }
        }.getOrNull()
            ?: uri.lastPathSegment
                ?.substringAfterLast("/")
                ?.takeIf { it.isNotBlank() }
            ?: "file"
    }

    private fun persistReadPermissionIfPossible(uri: Uri) {
        if (uri.scheme != android.content.ContentResolver.SCHEME_CONTENT) return
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                it.length
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun removeAttachment(uri: Uri) {
        cancelAttachmentProcessing(uri)
        val toRemove = _inputChatInputState.value.attachedMedia.find { it.uri == uri }
        toRemove?.localPath?.let { fileAnalyzer.deleteCachedFile(it) }
        val current = _inputChatInputState.value.attachedMedia.filter { it.uri != uri }
        _inputChatInputState.value = _inputChatInputState.value.copy(attachedMedia = current)
    }

    /**
     * 更新附件处理状态
     */
    fun updateAttachmentState(uri: Uri, state: AttachmentProcessingState) {
        val current = _inputChatInputState.value.attachedMedia.toMutableList()
        val index = current.indexOfFirst { it.uri == uri }
        if (index >= 0) {
            current[index] = current[index].copy(processingState = state)
            _inputChatInputState.value = _inputChatInputState.value.copy(attachedMedia = current)
        }
    }

    /**
     * 更新附件数据（处理完成后）
     */
    fun updateAttachment(uri: Uri, updatedMedia: AttachedMedia) {
        val current = _inputChatInputState.value.attachedMedia.toMutableList()
        val index = current.indexOfFirst { it.uri == uri }
        if (index >= 0) {
            current[index] = updatedMedia
            _inputChatInputState.value = _inputChatInputState.value.copy(attachedMedia = current)
        }
    }

    fun clearAttachments() {
        cancelAllAttachmentProcessingJobs()
        _inputChatInputState.value.attachedMedia.forEach {
            it.localPath?.let { path -> fileAnalyzer.deleteCachedFile(path) }
        }
        _inputChatInputState.value = _inputChatInputState.value.copy(attachedMedia = emptyList())
    }

    fun getPreviewUri(media: AttachedMedia): Uri {
        return media.localPath
            ?.takeIf { java.io.File(it).exists() }
            ?.toUri()
            ?: media.uri
    }

    /**
     * 删除指定缓存文件。
     * 供 AIChatComponent 在取消/编辑等场景清理磁盘残留。
     */
    fun deleteCachedFile(localPath: String?): Boolean {
        return fileAnalyzer.deleteCachedFile(localPath)
    }

    /**
     * 切换图片压缩开关
     */
    fun toggleImageCompression() {
        val enabled = !_inputChatInputState.value.enableImageCompression
        _inputChatInputState.value = _inputChatInputState.value.copy(enableImageCompression = enabled)
        reprocessSelectedImages(enableCompression = enabled)
    }

    /**
     * 检查附件是否都处理完成（可以发送）
     */
    fun areAttachmentsReady(): Boolean {
        val attachments = _inputChatInputState.value.attachedMedia
        if (attachments.isEmpty()) return true
        return attachments.all { media ->
            when (media.processingState) {
                is AttachmentProcessingState.IDLE -> true // 允许发送，处理会在发送时进行
                is AttachmentProcessingState.PROCESSING -> false // 处理中，不允许发送
                is AttachmentProcessingState.COMPLETED -> true
                is AttachmentProcessingState.ERROR -> false
            }
        }
    }

    private fun startImagePreprocessing(media: AttachedMedia, enableCompression: Boolean) {
        cancelAttachmentProcessing(media.uri)
        attachmentProcessingJobs[media.uri] = componentScope.launch(ioDispatcher) {
            try {
                fileAnalyzer.preparePreview(media.uri)?.let { thumbnail ->
                    mergeAttachment(media.uri) { current ->
                        current.copy(thumbnailBase64 = thumbnail)
                    }
                }

                val progressFlow = if (enableCompression) {
                    fileAnalyzer.analyzeWithProgress(media.uri)
                } else {
                    fileAnalyzer.encodeOriginalWithProgress(media.uri)
                }

                progressFlow.collectLatest { progress ->
                    when (progress) {
                        is com.shifenmiao.ai.upload.AnalyzerProgress.Step -> {
                            updateAttachmentState(
                                media.uri,
                                AttachmentProcessingState.PROCESSING(progress.step, progress.detail)
                            )
                        }

                        is com.shifenmiao.ai.upload.AnalyzerProgress.Completed -> {
                            val result = progress.result
                            mergeAttachment(media.uri) { current ->
                                current.copy(
                                    mimeType = result.mimeType,
                                    localContent = result.base64,
                                    localPath = result.cachedFilePath,
                                    size = result.processedSize,
                                    isImage = true,
                                    parseError = null,
                                    thumbnailBase64 = result.thumbnailBase64 ?: current.thumbnailBase64,
                                    processingState = AttachmentProcessingState.COMPLETED(
                                        originalSize = result.originalSize,
                                        processedSize = result.processedSize,
                                        format = if (result.mimeType.equals("image/webp", ignoreCase = true)) {
                                            "webp"
                                        } else {
                                            "original"
                                        }
                                    )
                                )
                            }
                        }

                        is com.shifenmiao.ai.upload.AnalyzerProgress.Error -> {
                            mergeAttachment(media.uri) { current ->
                                current.copy(
                                    parseError = progress.message,
                                    processingState = AttachmentProcessingState.ERROR(progress.message)
                                )
                            }
                        }
                    }
                }
            } catch (throwable: Throwable) {
                val detail = throwable.message?.takeIf { it.isNotBlank() }
                    ?: throwable.javaClass.simpleName
                mergeAttachment(media.uri) { current ->
                    current.copy(
                        parseError = detail,
                        processingState = AttachmentProcessingState.ERROR(
                            "${context.getString(R.string.attachment_process_failed)}: $detail"
                        )
                    )
                }
            } finally {
                attachmentProcessingJobs.remove(media.uri)
            }
        }
    }

    private fun reprocessSelectedImages(enableCompression: Boolean) {
        _inputChatInputState.value.attachedMedia
            .filter { it.isImage || it.mimeType.startsWith("image/") }
            .forEach { media ->
                media.localPath?.let { fileAnalyzer.deleteCachedFile(it) }
                mergeAttachment(media.uri) { current ->
                    current.copy(
                        localContent = null,
                        localPath = null,
                        parseError = null,
                        processingState = AttachmentProcessingState.PROCESSING(
                            com.shifenmiao.model.ai.ProcessingStep.CHECKING
                        )
                    )
                }
                startImagePreprocessing(media.copy(localContent = null, localPath = null), enableCompression)
            }
    }

    private fun mergeAttachment(uri: Uri, transform: (AttachedMedia) -> AttachedMedia) {
        val current = _inputChatInputState.value.attachedMedia.toMutableList()
        val index = current.indexOfFirst { it.uri == uri }
        if (index < 0) return
        current[index] = transform(current[index])
        _inputChatInputState.value = _inputChatInputState.value.copy(attachedMedia = current)
    }

    private fun cancelAttachmentProcessing(uri: Uri) {
        attachmentProcessingJobs.remove(uri)?.cancel()
    }

    private fun cancelAllAttachmentProcessingJobs() {
        attachmentProcessingJobs.values.forEach { it.cancel() }
        attachmentProcessingJobs.clear()
    }

    fun onInputTextChange(it: String) {
        inputStateHolder.inputText = it
        // 文本通过外部修改（如附件触发清空、引用粘贴）时，把光标置于末尾，
        // 这样无论 inline 还是全屏，再次进入都不会出现光标错乱。
        _inputChatInputState.value = _inputChatInputState.value.copy(
            inputText = it,
            cursorStart = it.length,
            cursorEnd = it.length,
        )
    }

    /**
     * 同时更新文本与光标/选区。inline 输入框与全屏写作态都通过此方法上报，
     * 由此实现两种形态切换时光标位置保持一致。
     */
    fun onInputValueChange(text: String, selectionStart: Int, selectionEnd: Int) {
        inputStateHolder.inputText = text
        _inputChatInputState.value = _inputChatInputState.value.copy(
            inputText = text,
            cursorStart = selectionStart.coerceIn(0, text.length),
            cursorEnd = selectionEnd.coerceIn(0, text.length),
        )
    }

    fun toggleExpand(bool: Boolean) {
        inputStateHolder.isExpanded = bool
        // 展开文本区时，关闭底部面板（互斥）
        if (bool) {
            inputStateHolder.inputMore = false
        }
        _inputChatInputState.value = _inputChatInputState.value.copy(
            isExpanded = bool,
            inputMore = if (bool) false else _inputChatInputState.value.inputMore,
        )
    }

    fun hideInputMore() {
        inputStateHolder.inputMore = false
        _inputChatInputState.value = _inputChatInputState.value.copy(
            inputMore = false,
        )
    }

    fun toggleInputMore() {
        val newValue = !_inputChatInputState.value.inputMore
        inputStateHolder.inputMore = newValue
        // 展开底部面板时，关闭文本展开（互斥）
        if (newValue) {
            inputStateHolder.isExpanded = false
        }
        _inputChatInputState.value = _inputChatInputState.value.copy(
            inputMore = newValue,
            isExpanded = if (newValue) false else _inputChatInputState.value.isExpanded,
        )
    }

    fun showPromptPicker() {
        _inputChatInputState.value = _inputChatInputState.value.copy(
            showPromptPicker = true,
        )
    }

    fun hidePromptPicker() {
        _inputChatInputState.value = _inputChatInputState.value.copy(
            showPromptPicker = false,
        )
    }

    fun showModelPicker() {
        _inputChatInputState.value = _inputChatInputState.value.copy(
            showModelPicker = true,
        )
    }

    fun hideModelPicker() {
        _inputChatInputState.value = _inputChatInputState.value.copy(
            showModelPicker = false,
        )
    }

    fun showRecentPicker() {
        _inputChatInputState.value = _inputChatInputState.value.copy(
            showRecentPicker = true,
        )
    }

    fun hideRecentPicker() {
        _inputChatInputState.value = _inputChatInputState.value.copy(
            showRecentPicker = false,
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): ChatInputComponent
    }
}
