package com.shifenmiao.ai.agent.tool

import com.google.gson.Gson
import com.shifenmiao.ai.agent.ToolCallTaskManager
import com.shifenmiao.database.ai.entity.ToolCallTaskEntity
import com.t8rin.logger.makeLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交互式工具运行时。
 *
 * 职责很单一：
 * - 持有当前待展示的交互请求
 * - 在工具执行协程与 UI 回调之间传递结果
 * - 持久化 WAITING_INPUT 任务，支持进程恢复
 *
 * 它不负责具体 UI 形式；页面是内嵌卡片还是全局 Dialog，都由上层决定。
 */
@Singleton
class InteractiveToolRuntime @Inject constructor(
    private val taskManager: ToolCallTaskManager,
    private val gson: Gson
) {

    private val _confirmationRequest = MutableStateFlow<ToolConfirmationRequest?>(null)
    val confirmationRequest: StateFlow<ToolConfirmationRequest?> = _confirmationRequest

    private val _questionRequest = MutableStateFlow<AgentUserQuestionRequest?>(null)
    val questionRequest: StateFlow<AgentUserQuestionRequest?> = _questionRequest

    private val _filePickerRequest = MutableStateFlow<FilePickerRequest?>(null)
    val filePickerRequest: StateFlow<FilePickerRequest?> = _filePickerRequest

    private val _folderPickerRequest = MutableStateFlow<FolderPickerRequest?>(null)
    val folderPickerRequest: StateFlow<FolderPickerRequest?> = _folderPickerRequest

    /**
     * 确认链和提问链共享运行时，但各自维护独立队列，避免互相覆盖。
     * 文件/目录选取器同一时间最多只有一个待处理（Android 系统限制），使用单值持有。
     */
    private val pendingConfirmationRequests = linkedMapOf<String, ToolConfirmationRequest>()
    private val pendingConfirmationDeferreds = linkedMapOf<String, CompletableDeferred<String?>>()
    private val pendingQuestionRequests = linkedMapOf<String, AgentUserQuestionRequest>()
    private val pendingQuestionDeferreds = linkedMapOf<String, CompletableDeferred<String?>>()

    @Volatile private var pendingFilePickerDeferred: CompletableDeferred<String?>? = null
    @Volatile private var pendingFolderPickerDeferred: CompletableDeferred<String?>? = null

    suspend fun requestUserQuestion(
        request: AgentUserQuestionRequest
    ): String? {
        val deferred = CompletableDeferred<String?>()

        try {
            taskManager.markWaitingInput(
                request.toolCallId,
                serializeSnapshot(
                    InteractivePendingRequestSnapshot(
                        kind = InteractivePendingRequestSnapshot.KIND_QUESTION,
                        questionRequest = request
                    )
                )
            )
        } catch (e: Exception) {
            "Failed to persist question request: ${e.message}".makeLog("InteractiveToolRuntime")
        }

        val replacedDeferred = synchronized(this) {
            enqueueQuestionLocked(request, deferred)
        }
        replacedDeferred?.takeIf { !it.isCompleted }?.complete(null)

        "Interactive tool '${request.toolName}' waiting for user question input..."
            .makeLog("InteractiveToolRuntime")

        return try {
            deferred.await()
        } finally {
            synchronized(this) {
                removeQuestionLocked(request.toolCallId)
            }
        }
    }

    /**
     * 工具执行前确认。
     *
     * 这类请求本质上也是一种等待用户放行的交互请求，只是 UI 形态通常比表单更轻。
     * 保持独立确认链，方便上层宿主优先渲染确认对话框。
     */
    suspend fun requestConfirmation(
        toolCallId: String,
        toolName: String,
        interactionOwnerId: String? = null,
        dialogTitle: String,
        dialogMessage: String,
        confirmPayload: String,
        dismissPayload: String,
        // 留空时由 UI 层回退到本地化默认文案
        submitButtonText: String = "",
        cancelButtonText: String = ""
    ): String? {
        val deferred = CompletableDeferred<String?>()
        val request = ToolConfirmationRequest(
            toolCallId = toolCallId,
            toolName = toolName,
            interactionOwnerId = interactionOwnerId,
            dialogTitle = dialogTitle,
            dialogMessage = dialogMessage,
            submitButtonText = submitButtonText,
            cancelButtonText = cancelButtonText,
            confirmPayload = confirmPayload,
            dismissPayload = dismissPayload
        )

        try {
            taskManager.markWaitingInput(
                toolCallId,
                serializeSnapshot(
                    InteractivePendingRequestSnapshot(
                        kind = InteractivePendingRequestSnapshot.KIND_CONFIRMATION,
                        confirmationRequest = request
                    )
                )
            )
        } catch (e: Exception) {
            "Failed to persist confirmation request: ${e.message}".makeLog("InteractiveToolRuntime")
        }

        val replacedDeferred = synchronized(this) {
            enqueueConfirmationLocked(request, deferred)
        }
        replacedDeferred?.takeIf { !it.isCompleted }?.complete(null)

        return try {
            deferred.await()
        } finally {
            synchronized(this) {
                removeConfirmationLocked(toolCallId)
            }
        }
    }

    suspend fun restoreWaitingInput(task: ToolCallTaskEntity): RestoredInteractiveRequestResult? {
        if (task.formRequestJson.isNullOrBlank()) {
            "Cannot restore: formRequestJson is empty for task ${task.id}".makeLog("InteractiveToolRuntime")
            return null
        }

        val snapshot = deserializeSnapshot(task.formRequestJson!!)
        if (snapshot != null) {
            return when (snapshot.kind) {
                InteractivePendingRequestSnapshot.KIND_CONFIRMATION -> {
                    val request = snapshot.confirmationRequest?.copy(toolCallId = task.id) ?: return null
                    restoreConfirmationRequest(request)
                }

                InteractivePendingRequestSnapshot.KIND_QUESTION -> {
                    val request = snapshot.questionRequest?.copy(toolCallId = task.id) ?: return null
                    restoreQuestionRequest(request)
                }

                else -> null
            }
        }

        val legacyRequest = deserializeLegacyPendingRequest(task.formRequestJson!!)
        if (legacyRequest == null) {
            "Cannot restore: failed to deserialize interactive request for task ${task.id}"
                .makeLog("InteractiveToolRuntime")
            return null
        }

        return if (legacyRequest.requestType == LegacyPendingRequestType.CONFIRMATION) {
            restoreConfirmationRequest(
                ToolConfirmationRequest(
                    toolCallId = task.id,
                    toolName = legacyRequest.toolName,
                    interactionOwnerId = legacyRequest.interactionOwnerId,
                    dialogTitle = legacyRequest.dialogTitle,
                    dialogMessage = legacyRequest.dialogMessage,
                    submitButtonText = legacyRequest.submitButtonText,
                    cancelButtonText = legacyRequest.cancelButtonText,
                    confirmPayload = legacyRequest.confirmPayload,
                    dismissPayload = legacyRequest.dismissPayload
                )
            )
        } else {
            "Legacy form request is no longer supported for task ${task.id}"
                .makeLog("InteractiveToolRuntime")
            RestoredInteractiveRequestResult(kind = KIND_LEGACY_FORM)
        }
    }

    fun submitConfirmation(payload: String) {
        val deferred = synchronized(this) {
            completeCurrentConfirmationLocked()
        }
        deferred?.complete(payload)
    }

    fun cancelConfirmation() {
        val deferred = synchronized(this) {
            completeCurrentConfirmationLocked()
        }
        deferred?.complete(null)
    }

    fun submitUserQuestion(answersJson: String) {
        val deferred = synchronized(this) {
            completeCurrentQuestionLocked()
        }
        deferred?.complete(answersJson)
    }

    fun cancelUserQuestion() {
        val deferred = synchronized(this) {
            completeCurrentQuestionLocked()
        }
        deferred?.complete(null)
    }

    /**
     * 挂起等待用户通过系统文件选择器选取文件。
     *
     * 同一时间只允许一个文件选取请求。若已有待处理请求，旧请求将被取消。
     * 返回值为用逗号分隔的 content URI 字符串列表，取消时返回 null。
     */
    suspend fun requestFilePicker(request: FilePickerRequest): String? {
        val deferred = CompletableDeferred<String?>()
        val replaced = synchronized(this) {
            val old = pendingFilePickerDeferred
            pendingFilePickerDeferred = deferred
            _filePickerRequest.value = request
            old
        }
        replaced?.takeIf { !it.isCompleted }?.complete(null)

        "Interactive tool '${request.toolName}' waiting for file picker...".makeLog("InteractiveToolRuntime")

        return try {
            deferred.await()
        } finally {
            synchronized(this) {
                if (pendingFilePickerDeferred === deferred) {
                    pendingFilePickerDeferred = null
                    _filePickerRequest.value = null
                }
            }
        }
    }

    /**
     * 挂起等待用户通过系统目录选择器选取目录。
     *
     * 同一时间只允许一个目录选取请求。取消时返回 null。
     */
    suspend fun requestFolderPicker(request: FolderPickerRequest): String? {
        val deferred = CompletableDeferred<String?>()
        val replaced = synchronized(this) {
            val old = pendingFolderPickerDeferred
            pendingFolderPickerDeferred = deferred
            _folderPickerRequest.value = request
            old
        }
        replaced?.takeIf { !it.isCompleted }?.complete(null)

        "Interactive tool '${request.toolName}' waiting for folder picker...".makeLog("InteractiveToolRuntime")

        return try {
            deferred.await()
        } finally {
            synchronized(this) {
                if (pendingFolderPickerDeferred === deferred) {
                    pendingFolderPickerDeferred = null
                    _folderPickerRequest.value = null
                }
            }
        }
    }

    fun submitFilePicker(urisCsv: String) {
        val deferred = synchronized(this) {
            val d = pendingFilePickerDeferred
            pendingFilePickerDeferred = null
            _filePickerRequest.value = null
            d
        }
        deferred?.complete(urisCsv)
    }

    fun cancelFilePicker() {
        val deferred = synchronized(this) {
            val d = pendingFilePickerDeferred
            pendingFilePickerDeferred = null
            _filePickerRequest.value = null
            d
        }
        deferred?.complete(null)
    }

    fun submitFolderPicker(uriString: String) {
        val deferred = synchronized(this) {
            val d = pendingFolderPickerDeferred
            pendingFolderPickerDeferred = null
            _folderPickerRequest.value = null
            d
        }
        deferred?.complete(uriString)
    }

    fun cancelFolderPicker() {
        val deferred = synchronized(this) {
            val d = pendingFolderPickerDeferred
            pendingFolderPickerDeferred = null
            _folderPickerRequest.value = null
            d
        }
        deferred?.complete(null)
    }

    fun reset() {
        val deferreds = synchronized(this) {
            val allDeferreds = pendingConfirmationDeferreds.values.toList() +
                    pendingQuestionDeferreds.values.toList() +
                    listOfNotNull(pendingFilePickerDeferred, pendingFolderPickerDeferred)
            pendingConfirmationRequests.clear()
            pendingConfirmationDeferreds.clear()
            pendingQuestionRequests.clear()
            pendingQuestionDeferreds.clear()
            pendingFilePickerDeferred = null
            pendingFolderPickerDeferred = null
            _filePickerRequest.value = null
            _folderPickerRequest.value = null
            publishCurrentConfirmationLocked()
            publishCurrentQuestionLocked()
            allDeferreds
        }
        deferreds.forEach {
            if (!it.isCompleted) {
                it.complete(null)
            }
        }
    }

    fun hasPendingRequest(): Boolean = synchronized(this) {
        pendingConfirmationDeferreds.values.any { it.isActive } ||
                pendingQuestionDeferreds.values.any { it.isActive } ||
                pendingFilePickerDeferred?.isActive == true ||
                pendingFolderPickerDeferred?.isActive == true
    }

    fun clearPendingRequestOwnedBy(interactionOwnerId: String) {
        "Clearing interactive request owned by $interactionOwnerId".makeLog("InteractiveToolRuntime")
        val deferreds = synchronized(this) {
            val confirmationIds = pendingConfirmationRequests.values
                .filter { it.interactionOwnerId == interactionOwnerId }
                .map { it.toolCallId }
            val questionIds = pendingQuestionRequests.values
                .filter { it.interactionOwnerId == interactionOwnerId }
                .map { it.toolCallId }

            val confirmationDeferreds = confirmationIds.mapNotNull { toolCallId ->
                pendingConfirmationRequests.remove(toolCallId)
                pendingConfirmationDeferreds.remove(toolCallId)
            }
            val questionDeferreds = questionIds.mapNotNull { toolCallId ->
                pendingQuestionRequests.remove(toolCallId)
                pendingQuestionDeferreds.remove(toolCallId)
            }

            // 清理文件/目录选取器（如果属于同一会话）
            val filePickerDeferreds = buildList {
                if (_filePickerRequest.value?.interactionOwnerId == interactionOwnerId) {
                    pendingFilePickerDeferred?.let { add(it) }
                    pendingFilePickerDeferred = null
                    _filePickerRequest.value = null
                }
                if (_folderPickerRequest.value?.interactionOwnerId == interactionOwnerId) {
                    pendingFolderPickerDeferred?.let { add(it) }
                    pendingFolderPickerDeferred = null
                    _folderPickerRequest.value = null
                }
            }

            publishCurrentConfirmationLocked()
            publishCurrentQuestionLocked()
            confirmationDeferreds + questionDeferreds + filePickerDeferreds
        }

        deferreds.forEach {
            if (!it.isCompleted) {
                it.complete(null)
            }
        }
    }

    private suspend fun restoreConfirmationRequest(
        request: ToolConfirmationRequest
    ): RestoredInteractiveRequestResult {
        val deferred = CompletableDeferred<String?>()
        val replacedDeferred = synchronized(this) {
            enqueueConfirmationLocked(request, deferred)
        }
        replacedDeferred?.takeIf { !it.isCompleted }?.complete(null)

        "Restored confirmation for tool '${request.toolName}', waiting for user input..."
            .makeLog("InteractiveToolRuntime")

        return try {
            RestoredInteractiveRequestResult(
                kind = InteractivePendingRequestSnapshot.KIND_CONFIRMATION,
                confirmationRequest = request,
                payload = deferred.await()
            )
        } finally {
            synchronized(this) {
                removeConfirmationLocked(request.toolCallId)
            }
        }
    }

    private suspend fun restoreQuestionRequest(
        request: AgentUserQuestionRequest
    ): RestoredInteractiveRequestResult {
        val deferred = CompletableDeferred<String?>()
        val replacedDeferred = synchronized(this) {
            enqueueQuestionLocked(request, deferred)
        }
        replacedDeferred?.takeIf { !it.isCompleted }?.complete(null)

        "Restored question for tool '${request.toolName}', waiting for user input..."
            .makeLog("InteractiveToolRuntime")

        return try {
            RestoredInteractiveRequestResult(
                kind = InteractivePendingRequestSnapshot.KIND_QUESTION,
                questionRequest = request,
                payload = deferred.await()
            )
        } finally {
            synchronized(this) {
                removeQuestionLocked(request.toolCallId)
            }
        }
    }

    private fun enqueueConfirmationLocked(
        request: ToolConfirmationRequest,
        deferred: CompletableDeferred<String?>
    ): CompletableDeferred<String?>? {
        val replacedDeferred = pendingConfirmationDeferreds.remove(request.toolCallId)
        pendingConfirmationRequests.remove(request.toolCallId)
        pendingConfirmationRequests[request.toolCallId] = request
        pendingConfirmationDeferreds[request.toolCallId] = deferred
        publishCurrentConfirmationLocked()
        return replacedDeferred
    }

    private fun removeConfirmationLocked(toolCallId: String): CompletableDeferred<String?>? {
        pendingConfirmationRequests.remove(toolCallId)
        val deferred = pendingConfirmationDeferreds.remove(toolCallId)
        publishCurrentConfirmationLocked()
        return deferred
    }

    private fun completeCurrentConfirmationLocked(): CompletableDeferred<String?>? {
        val currentToolCallId = pendingConfirmationRequests.keys.firstOrNull() ?: return null
        return removeConfirmationLocked(currentToolCallId)
    }

    private fun publishCurrentConfirmationLocked() {
        _confirmationRequest.value = pendingConfirmationRequests.values.firstOrNull()
    }

    private fun enqueueQuestionLocked(
        request: AgentUserQuestionRequest,
        deferred: CompletableDeferred<String?>
    ): CompletableDeferred<String?>? {
        val replacedDeferred = pendingQuestionDeferreds.remove(request.toolCallId)
        pendingQuestionRequests.remove(request.toolCallId)
        pendingQuestionRequests[request.toolCallId] = request
        pendingQuestionDeferreds[request.toolCallId] = deferred
        publishCurrentQuestionLocked()
        return replacedDeferred
    }

    private fun removeQuestionLocked(toolCallId: String): CompletableDeferred<String?>? {
        pendingQuestionRequests.remove(toolCallId)
        val deferred = pendingQuestionDeferreds.remove(toolCallId)
        publishCurrentQuestionLocked()
        return deferred
    }

    private fun completeCurrentQuestionLocked(): CompletableDeferred<String?>? {
        val currentToolCallId = pendingQuestionRequests.keys.firstOrNull() ?: return null
        return removeQuestionLocked(currentToolCallId)
    }

    private fun publishCurrentQuestionLocked() {
        _questionRequest.value = pendingQuestionRequests.values.firstOrNull()
    }

    private fun serializeSnapshot(snapshot: InteractivePendingRequestSnapshot): String {
        return gson.toJson(snapshot)
    }

    private fun deserializeSnapshot(json: String): InteractivePendingRequestSnapshot? {
        return try {
            gson.fromJson(json, InteractivePendingRequestSnapshot::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun deserializeLegacyPendingRequest(json: String): LegacyPendingRequest? {
        return try {
            gson.fromJson(json, LegacyPendingRequest::class.java)
        } catch (e: Exception) {
            "Failed to deserialize legacy interactive request: ${e.message}"
                .makeLog("InteractiveToolRuntime")
            null
        }
    }

    companion object {
        const val KIND_LEGACY_FORM = "legacy_form"
    }
}

private data class LegacyPendingRequest(
    val toolCallId: String = "",
    val toolName: String = "",
    val uiJson: String = "",
    val interactionOwnerId: String? = null,
    val requestType: LegacyPendingRequestType = LegacyPendingRequestType.FORM,
    val dialogTitle: String = "",
    val dialogMessage: String = "",
    val submitButtonText: String = "",
    val cancelButtonText: String = "",
    val confirmPayload: String? = null,
    val dismissPayload: String? = null
)

private enum class LegacyPendingRequestType {
    FORM,
    CONFIRMATION
}
