package com.shifenmiao.ai.component

import android.net.Uri
import com.shifenmiao.ai.logic.ChatInputComponent
import com.shifenmiao.ai.upload.AttachmentContentResolver
import com.shifenmiao.ai.upload.FileUploadRouter
import com.shifenmiao.ai.upload.UploadProgress
import com.shifenmiao.ai.utils.AttachmentPayloadUtils
import com.shifenmiao.database.image.dao.ImageDao
import com.shifenmiao.database.image.entity.ImageEntity
import com.shifenmiao.model.ai.AttachedMedia
import com.shifenmiao.model.ai.AttachmentProcessingState
import com.google.gson.Gson
import com.t8rin.logger.makeLog
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 附件处理编排器 —— 负责附件的上传/压缩/序列化和图片持久化。
 *
 * 从 [AIChatComponent] 中抽离，职责边界：
 * 1. 附件上传（Base64/云存储）与进度追踪
 * 2. 附件 JSON 序列化/反序列化
 * 3. 图片附件持久化到 ImageEntity
 * 4. 待持久化图片的临时缓存管理
 *
 * 线程安全：所有 suspend 方法由调用方在 IO 协程中执行；
 * [pendingImageAttachments] 仅在同一切换点读写（executeStreamingChat 的 IO 协程）。
 */
class AttachmentUploadCoordinator(
    private val fileUploadRouter: FileUploadRouter,
    private val attachmentContentResolver: AttachmentContentResolver,
    private val imageDao: ImageDao,
    private val gson: Gson,
    private val sharedState: ChatSharedState,
    private val chatInputComponent: ChatInputComponent,
    private val onProgressStateChanged: (Uri, AttachmentProcessingState) -> Unit,
) {
    companion object {
        /** 附件处理超时：3 分钟 */
        const val ATTACHMENT_PROCESSING_TIMEOUT_MS = 180_000L
    }

    /** 当前轮次待持久化的图片附件（含 localContent） */
    var pendingImageAttachments: List<AttachedMedia> = emptyList()
        private set

    /**
     * 处理附件列表：上传/压缩 + 进度收集。
     *
     * @return 处理后的附件列表；失败附件的 url/localContent 为 null
     * @throws kotlinx.coroutines.TimeoutCancellationException 如果整体处理超时
     */
    suspend fun processAttachments(
        attachments: List<AttachedMedia>,
        enableCompression: Boolean = true,
    ): List<AttachedMedia> {
        if (attachments.isEmpty()) return emptyList()

        val engine = sharedState.conversation.value.engine

        val (progressFlow, resultDeferred) = fileUploadRouter.processAttachmentsWithResult(
            attachments = attachments,
            engine = engine,
            stateUpdater = onProgressStateChanged,
            enableCompression = enableCompression
        )

        // 并行收集进度 + 等待结果，带超时保护
        val processResults = withTimeoutOrNull(ATTACHMENT_PROCESSING_TIMEOUT_MS) {
            progressFlow.collect { progress ->
                when (progress) {
                    is UploadProgress.Started -> {
                        "Processing started: ${progress.fileName}".makeLog("FileUpload")
                    }
                    is UploadProgress.Step -> {
                        "Processing step: ${progress.fileName} - ${progress.step.displayName}: ${progress.detail}".makeLog("FileUpload")
                    }
                    is UploadProgress.Completed -> {
                        "Processing completed: ${progress.fileName} (${progress.originalSize} -> ${progress.processedSize})".makeLog("FileUpload")
                    }
                    is UploadProgress.Error -> {
                        "Processing error: ${progress.fileName} - ${progress.message}".makeLog("FileUpload")
                    }
                }
            }
            resultDeferred.await()
        } ?: run {
            // 超时：尽量获取已处理的结果
            resultDeferred.await()
        }

        val failedResults = processResults.filterNot { result ->
            result.success && result.processed.canBeDeliveredToModel()
        }
        if (failedResults.isNotEmpty()) {
            pendingImageAttachments = emptyList()
            val message = failedResults.joinToString(separator = "；") { result ->
                val name = result.original.name.ifBlank { "file" }
                val reason = result.error
                    ?: result.processed.parseError
                    ?: "附件处理完成，但未生成可发送给模型的内容"
                "$name：$reason"
            }
            throw IllegalStateException(message)
        }

        val finalAttachments = processResults.map { it.processed }

        // 保存待持久化的图片附件
        pendingImageAttachments = finalAttachments.filter { it.isImage && it.localContent != null }

        return finalAttachments
    }

    /**
     * 将当前轮次的图片附件持久化到 ImageEntity 表。
     * 在消息写入 DB 之后调用，此时 questionMessageEntity.id 已有正确的自增主键。
     */
    suspend fun saveImagesToEntity() {
        val attachments = pendingImageAttachments
        if (attachments.isEmpty()) return
        pendingImageAttachments = emptyList()

        val questionId = sharedState.questionMessageEntity.value.id
        if (questionId <= 0) return

        val imageEntities = attachments.map { attachment ->
            ImageEntity(
                uri = attachment.uri.toString(),
                format = attachment.mimeType.substringAfter('/', "webp"),
                base64Data = attachment.localContent!!,
                messageId = questionId,
                conversationId = sharedState.conversation.value.id,
                localPath = attachment.localPath,
                thumbnailBase64 = attachment.thumbnailBase64
            )
        }
        runCatching { imageDao.insertImages(imageEntities) }
            .onFailure { "saveImagesToEntity failed: ${it.message}".makeLog("AttachmentUploadCoordinator") }
    }

    /** 清理 pending 图片缓存（取消/销毁时调用） */
    fun clearPendingImageCaches() {
        pendingImageAttachments.forEach { it.localPath?.let { path -> chatInputComponent.deleteCachedFile(path) } }
        pendingImageAttachments = emptyList()
    }

    /**
     * 序列化附件列表为 JSON。
     *
     * @param stripLocalContent true 时排除 localContent（DB 持久化），
     *                          false 时保留 localContent（API 请求构建）。
     */
    fun serializeAttachmentsJson(
        attachments: List<AttachedMedia>,
        stripLocalContent: Boolean = true
    ): String {
        return AttachmentPayloadUtils.serialize(
            attachments = attachments,
            gson = gson,
            stripLocalContent = stripLocalContent
        )
    }

    /**
     * 从 attachmentsJson 反序列化并通过 resolver 恢复 localContent。
     * DB 中的 attachmentsJson 不含 localContent（避免 DB 膨胀），
     * 通过 AttachmentContentResolver 从缓存文件按需读取。
     */
    suspend fun resolveAttachmentsFromJson(json: String): List<AttachedMedia> {
        if (json.isBlank()) return emptyList()
        val dtos = runCatching {
            AttachmentPayloadUtils.deserialize(json, gson)
        }.getOrNull() ?: return emptyList()
        return attachmentContentResolver.resolveAttachments(dtos)
    }
}

private fun AttachedMedia.canBeDeliveredToModel(): Boolean {
    return when {
        url != null -> true
        localContent != null -> true
        localPath != null -> true
        else -> false
    }
}

