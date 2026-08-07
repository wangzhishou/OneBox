package com.shifenmiao.ai.upload

import android.content.Context
import android.net.Uri
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AttachedMedia
import com.shifenmiao.model.ai.AttachmentProcessingState
import com.shifenmiao.model.ai.FileUploadStrategy
import com.shifenmiao.model.ai.ProcessingStep
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 上传进度
 */
sealed class UploadProgress {
    /** 开始处理 */
    data class Started(val fileName: String) : UploadProgress()

    /** 处理步骤 */
    data class Step(
        val fileName: String,
        val step: ProcessingStep,
        val detail: String? = null
    ) : UploadProgress()

    /** 处理完成 */
    data class Completed(
        val fileName: String,
        val originalSize: Long,
        val processedSize: Long
    ) : UploadProgress()

    /** 处理失败 */
    data class Error(val fileName: String, val message: String) : UploadProgress()
}

/**
 * 附件处理结果
 */
data class AttachmentProcessResult(
    val original: AttachedMedia,
    val processed: AttachedMedia,
    val success: Boolean,
    val error: String? = null
)

/**
 * 文件上传路由器
 *
 * 根据AI引擎配置的策略选择对应的处理方式：
 * - BASE64: 本地压缩+转WebP+Base64编码
 * - CLOUD: 上传到配置的云存储
 */
@Singleton
class FileUploadRouter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileAnalyzer: FileAnalyzer,
    private val cloudFileUploadHandler: CloudFileUploadHandler,
) {
    /**
     * 处理附件列表，返回带进度的Flow和处理结果
     *
     * @param attachments 附件列表
     * @param engine AI引擎配置
     * @param stateUpdater 更新附件状态的回调
     * @param enableCompression 是否启用压缩
     * @return Pair<Flow<UploadProgress>, Deferred<List<AttachmentProcessResult>>>
     */
    fun processAttachmentsWithResult(
        attachments: List<AttachedMedia>,
        engine: AiEngine,
        stateUpdater: (Uri, AttachmentProcessingState) -> Unit = { _, _ -> },
        enableCompression: Boolean = true
    ): Pair<Flow<UploadProgress>, kotlinx.coroutines.Deferred<List<AttachmentProcessResult>>> {
        val deferred = kotlinx.coroutines.CompletableDeferred<List<AttachmentProcessResult>>()
        
        val flow = flow<UploadProgress> {
            if (attachments.isEmpty()) {
                deferred.complete(emptyList())
                return@flow
            }

            val results = mutableListOf<AttachmentProcessResult>()
            
            when (engine.fileUploadStrategy) {
                FileUploadStrategy.BASE64 -> {
                    processWithBase64AndCollect(attachments, stateUpdater, results, enableCompression)
                }
                FileUploadStrategy.CLOUD -> {
                    processWithCloudAndCollect(attachments, engine, stateUpdater, results)
                }
            }
            
            deferred.complete(results)
        }.flowOn(Dispatchers.IO)
        
        return Pair(flow, deferred)
    }

    /**
     * 处理附件列表，返回带进度的Flow（兼容旧接口）
     */
    fun processAttachments(
        attachments: List<AttachedMedia>,
        engine: AiEngine,
        stateUpdater: (Uri, AttachmentProcessingState) -> Unit = { _, _ -> }
    ): Flow<UploadProgress> = flow {
        if (attachments.isEmpty()) return@flow

        when (engine.fileUploadStrategy) {
            FileUploadStrategy.BASE64 -> {
                processWithBase64(attachments, stateUpdater)
            }
            FileUploadStrategy.CLOUD -> {
                processWithCloud(attachments, engine, stateUpdater)
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Base64模式处理（旧接口，不收集结果）
     */
    private suspend fun kotlinx.coroutines.flow.FlowCollector<UploadProgress>.processWithBase64(
        attachments: List<AttachedMedia>,
        stateUpdater: (Uri, AttachmentProcessingState) -> Unit
    ) {
        for (media in attachments) {
            // 跳过已处理的
            if (media.localContent != null || media.url != null) {
                emit(UploadProgress.Completed(
                    fileName = media.name,
                    originalSize = media.size,
                    processedSize = media.size
                ))
                continue
            }

            emit(UploadProgress.Started(media.name))
            stateUpdater(media.uri, AttachmentProcessingState.PROCESSING(ProcessingStep.CHECKING))

            try {
                fileAnalyzer.analyzeWithProgress(media.uri).collect { progress ->
                    when (progress) {
                        is AnalyzerProgress.Step -> {
                            emit(UploadProgress.Step(
                                fileName = media.name,
                                step = progress.step,
                                detail = progress.detail
                            ))
                            stateUpdater(media.uri, AttachmentProcessingState.PROCESSING(progress.step, progress.detail))
                        }
                        is AnalyzerProgress.Completed -> {
                            emit(UploadProgress.Completed(
                                fileName = media.name,
                                originalSize = progress.result.originalSize,
                                processedSize = progress.result.processedSize
                            ))
                            stateUpdater(media.uri, AttachmentProcessingState.COMPLETED(
                                originalSize = progress.result.originalSize,
                                processedSize = progress.result.processedSize,
                                format = if (progress.result.mimeType.equals("image/webp", ignoreCase = true)) {
                                    "webp"
                                } else {
                                    "original"
                                }
                            ))
                        }
                        is AnalyzerProgress.Error -> {
                            emit(UploadProgress.Error(media.name, progress.message))
                            stateUpdater(media.uri, AttachmentProcessingState.ERROR(progress.message))
                        }
                    }
                }
            } catch (e: Exception) {
                emit(UploadProgress.Error(media.name, "${context.getString(R.string.attachment_process_failed)}: ${e.message}"))
                stateUpdater(media.uri, AttachmentProcessingState.ERROR("${context.getString(R.string.attachment_process_failed)}: ${e.message}"))
            }
        }
    }

    /**
     * Base64模式处理（新接口，收集结果）
     */
    private suspend fun processWithBase64AndCollect(
        attachments: List<AttachedMedia>,
        stateUpdater: (Uri, AttachmentProcessingState) -> Unit,
        results: MutableList<AttachmentProcessResult>,
        enableCompression: Boolean = true
    ) {
        for (media in attachments) {
            // 跳过已处理的
            if (media.localContent != null || media.url != null) {
                results.add(AttachmentProcessResult(
                    original = media,
                    processed = media,
                    success = true
                ))
                continue
            }

            // 如果不启用压缩，直接编码为Base64
            if (!enableCompression) {
                stateUpdater(media.uri, AttachmentProcessingState.PROCESSING(ProcessingStep.ENCODING))
                try {
                    val base64 = encodeToBase64(media.uri)
                    val size = getFileSize(media.uri)
                    stateUpdater(media.uri, AttachmentProcessingState.COMPLETED(
                        originalSize = size,
                        processedSize = size,
                        format = "original"
                    ))
                    results.add(AttachmentProcessResult(
                        original = media,
                        processed = media.copy(
                            localContent = base64,
                            size = size,
                            isImage = true
                        ),
                        success = true
                    ))
                } catch (e: Exception) {
                    stateUpdater(media.uri, AttachmentProcessingState.ERROR("${context.getString(R.string.attachment_encode_failed)}: ${e.message}"))
                    results.add(AttachmentProcessResult(
                        original = media,
                        processed = media,
                        success = false,
                        error = e.message
                    ))
                }
                continue
            }

            stateUpdater(media.uri, AttachmentProcessingState.PROCESSING(ProcessingStep.CHECKING))

            try {
                var processingResult: ImageProcessingResult? = null
                var processingError: String? = null
                fileAnalyzer.analyzeWithProgress(media.uri).collect { progress ->
                    when (progress) {
                        is AnalyzerProgress.Step -> {
                            stateUpdater(media.uri, AttachmentProcessingState.PROCESSING(progress.step, progress.detail))
                        }
                        is AnalyzerProgress.Completed -> {
                            processingResult = progress.result
                            stateUpdater(media.uri, AttachmentProcessingState.COMPLETED(
                                originalSize = progress.result.originalSize,
                                processedSize = progress.result.processedSize,
                                format = if (progress.result.mimeType.equals("image/webp", ignoreCase = true)) {
                                    "webp"
                                } else {
                                    "original"
                                }
                            ))
                        }
                        is AnalyzerProgress.Error -> {
                            processingError = progress.message
                            stateUpdater(media.uri, AttachmentProcessingState.ERROR(progress.message))
                        }
                    }
                }
                // 返回处理结果，包含缓存文件路径
                processingResult?.let { result ->
                    results.add(AttachmentProcessResult(
                        original = media,
                        processed = media.copy(
                            localContent = result.base64,
                            localPath = result.cachedFilePath,
                            size = result.processedSize,
                            isImage = true,
                            mimeType = result.mimeType,
                            thumbnailBase64 = result.thumbnailBase64
                        ),
                        success = true
                    ))
                } ?: results.add(AttachmentProcessResult(
                    original = media,
                    processed = media,
                    success = false,
                    error = processingError ?: context.getString(R.string.attachment_process_failed)
                ))
            } catch (e: Exception) {
                val detail = e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                stateUpdater(media.uri, AttachmentProcessingState.ERROR("${context.getString(R.string.attachment_process_failed)}: $detail"))
                results.add(AttachmentProcessResult(
                    original = media,
                    processed = media,
                    success = false,
                    error = "${context.getString(R.string.attachment_process_failed)}: $detail"
                ))
            }
        }
    }

    /**
     * 直接编码为Base64（不压缩）
     */
    private fun encodeToBase64(uri: Uri): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException(context.getString(R.string.attachment_read_failed))
        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        return "data:$mimeType;base64,$base64"
    }

    /**
     * 获取文件大小
     */
    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                it.length
            } ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * 云存储模式处理（新接口，收集结果）
     */
    private suspend fun processWithCloudAndCollect(
        attachments: List<AttachedMedia>,
        engine: AiEngine,
        stateUpdater: (Uri, AttachmentProcessingState) -> Unit,
        results: MutableList<AttachmentProcessResult>
    ) {
        val connectionId = engine.cloudStorageConnectionId
        val bucket = engine.cloudStorageBucket

        if (connectionId.isNullOrBlank() || bucket.isNullOrBlank()) {
            for (media in attachments) {
                results.add(AttachmentProcessResult(
                    original = media,
                    processed = media,
                    success = false,
                    error = context.getString(R.string.cloud_storage_config_invalid)
                ))
            }
            return
        }

        for (media in attachments) {
            // 跳过已上传的
            if (media.url != null) {
                results.add(AttachmentProcessResult(
                    original = media,
                    processed = media,
                    success = true
                ))
                continue
            }

            stateUpdater(media.uri, AttachmentProcessingState.PROCESSING(ProcessingStep.UPLOADING))

            try {
                val result = cloudFileUploadHandler.upload(
                    uri = media.uri,
                    connectionId = connectionId,
                    bucket = bucket,
                    prefix = engine.cloudStoragePrefix,
                    contentType = media.mimeType
                )

                result.fold(
                    onSuccess = { uploadResult ->
                        stateUpdater(media.uri, AttachmentProcessingState.COMPLETED(
                            originalSize = media.size,
                            processedSize = media.size,
                            format = "cloud"
                        ))
                        results.add(AttachmentProcessResult(
                            original = media,
                            processed = media.copy(url = uploadResult.url),
                            success = true
                        ))
                    },
                    onFailure = { e ->
                        stateUpdater(media.uri, AttachmentProcessingState.ERROR("${context.getString(R.string.attachment_upload_failed)}: ${e.message}"))
                        results.add(AttachmentProcessResult(
                            original = media,
                            processed = media,
                            success = false,
                            error = e.message
                        ))
                    }
                )
            } catch (e: Exception) {
                stateUpdater(media.uri, AttachmentProcessingState.ERROR("${context.getString(R.string.attachment_upload_failed)}: ${e.message}"))
                results.add(AttachmentProcessResult(
                    original = media,
                    processed = media,
                    success = false,
                    error = e.message
                ))
            }
        }
    }

    /**
     * 云存储模式处理
     */
    private suspend fun kotlinx.coroutines.flow.FlowCollector<UploadProgress>.processWithCloud(
        attachments: List<AttachedMedia>,
        engine: AiEngine,
        stateUpdater: (Uri, AttachmentProcessingState) -> Unit
    ) {
        val connectionId = engine.cloudStorageConnectionId
        val bucket = engine.cloudStorageBucket

        if (connectionId.isNullOrBlank() || bucket.isNullOrBlank()) {
            for (media in attachments) {
                emit(UploadProgress.Error(media.name, context.getString(R.string.cloud_storage_config_invalid)))
                stateUpdater(media.uri, AttachmentProcessingState.ERROR(context.getString(R.string.cloud_storage_config_invalid)))
            }
            return
        }

        for (media in attachments) {
            // 跳过已上传的
            if (media.url != null) {
                emit(UploadProgress.Completed(
                    fileName = media.name,
                    originalSize = media.size,
                    processedSize = media.size
                ))
                continue
            }

            emit(UploadProgress.Started(media.name))
            stateUpdater(media.uri, AttachmentProcessingState.PROCESSING(ProcessingStep.UPLOADING))

            try {
                val result = cloudFileUploadHandler.upload(
                    uri = media.uri,
                    connectionId = connectionId,
                    bucket = bucket,
                    prefix = engine.cloudStoragePrefix,
                    contentType = media.mimeType
                )

                result.fold(
                    onSuccess = { uploadResult ->
                        emit(UploadProgress.Completed(
                            fileName = media.name,
                            originalSize = media.size,
                            processedSize = media.size
                        ))
                        stateUpdater(media.uri, AttachmentProcessingState.COMPLETED(
                            originalSize = media.size,
                            processedSize = media.size,
                            format = "cloud"
                        ))
                    },
                    onFailure = { e ->
                        emit(UploadProgress.Error(media.name, "${context.getString(R.string.attachment_upload_failed)}: ${e.message}"))
                        stateUpdater(media.uri, AttachmentProcessingState.ERROR("${context.getString(R.string.attachment_upload_failed)}: ${e.message}"))
                    }
                )
            } catch (e: Exception) {
                emit(UploadProgress.Error(media.name, "${context.getString(R.string.attachment_upload_failed)}: ${e.message}"))
                stateUpdater(media.uri, AttachmentProcessingState.ERROR("${context.getString(R.string.attachment_upload_failed)}: ${e.message}"))
            }
        }
    }

    /**
     * 批量处理附件（简化版本，不带进度）
     *
     * @param attachments 附件列表
     * @param engine AI引擎配置
     * @return 处理后的附件列表
     */
    suspend fun processAttachmentsSimple(
        attachments: List<AttachedMedia>,
        engine: AiEngine,
    ): List<AttachedMedia> {
        if (attachments.isEmpty()) return attachments

        return when (engine.fileUploadStrategy) {
            FileUploadStrategy.BASE64 -> {
                processWithBase64Simple(attachments)
            }
            FileUploadStrategy.CLOUD -> {
                processWithCloudSimple(attachments, engine)
            }
        }
    }

    /**
     * Base64模式处理（简化版本）
     */
    private suspend fun processWithBase64Simple(
        attachments: List<AttachedMedia>
    ): List<AttachedMedia> = coroutineScope {
        attachments.map { media ->
            async {
                if (media.localContent != null || media.url != null) {
                    media
                } else {
                    try {
                        val result = fileAnalyzer.analyze(media.uri)
                        media.copy(
                            localContent = result.base64,
                            mimeType = result.mimeType,
                            size = result.size,
                            isImage = true,
                            parseError = result.error
                        )
                    } catch (e: Exception) {
                        media.copy(parseError = "${context.getString(R.string.attachment_process_failed)}: ${e.message}")
                    }
                }
            }
        }.awaitAll()
    }

    /**
     * 云存储模式处理（简化版本）
     */
    private suspend fun processWithCloudSimple(
        attachments: List<AttachedMedia>,
        engine: AiEngine,
    ): List<AttachedMedia> = coroutineScope {
        val connectionId = engine.cloudStorageConnectionId
        val bucket = engine.cloudStorageBucket

        if (connectionId.isNullOrBlank() || bucket.isNullOrBlank()) {
            return@coroutineScope attachments.map {
                it.copy(parseError = context.getString(R.string.cloud_storage_config_invalid))
            }
        }

        attachments.map { media ->
            async {
                if (media.url != null) {
                    media
                } else {
                    try {
                        val result = cloudFileUploadHandler.upload(
                            uri = media.uri,
                            connectionId = connectionId,
                            bucket = bucket,
                            prefix = engine.cloudStoragePrefix,
                            contentType = media.mimeType
                        )
                        result.fold(
                            onSuccess = { uploadResult ->
                                media.copy(url = uploadResult.url)
                            },
                            onFailure = { e ->
                                media.copy(parseError = "${context.getString(R.string.attachment_upload_failed)}: ${e.message}")
                            }
                        )
                    } catch (e: Exception) {
                        media.copy(parseError = "${context.getString(R.string.attachment_upload_failed)}: ${e.message}")
                    }
                }
            }
        }.awaitAll()
    }

    /**
     * 检查云存储配置是否完整
     */
    fun isCloudConfigValid(engine: AiEngine): Boolean {
        return engine.fileUploadStrategy == FileUploadStrategy.CLOUD &&
                !engine.cloudStorageConnectionId.isNullOrBlank() &&
                !engine.cloudStorageBucket.isNullOrBlank()
    }

    /**
     * 获取云存储连接列表
     */
    fun getCloudConnections() = cloudFileUploadHandler.getConnections()

    /**
     * 获取默认云存储连接
     */
    fun getDefaultCloudConnection() = cloudFileUploadHandler.getDefaultConnection()
}
