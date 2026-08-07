package com.shifenmiao.model.ai

import android.net.Uri

data class ChatInputState(
    val inputText: String = "",
    /** 光标/选区起点（字符 offset） */
    val cursorStart: Int = 0,
    /** 光标/选区终点（字符 offset），与 [cursorStart] 相等表示无选区，仅光标 */
    val cursorEnd: Int = 0,
    var isExpanded: Boolean = false,
    var inputMore: Boolean = false,
    var showPromptPicker: Boolean = false,
    var showRecentPicker: Boolean = false,
    var showModelPicker: Boolean = false,
    val attachedMedia: List<AttachedMedia> = emptyList(),
    val isUploading: Boolean = false,
    /** 已选中作为系统提示词的标题（用于 UI 显示） */
    val selectedSystemPromptTitle: String? = null,
    /** 已选中作为系统提示词的内容（发送时注入） */
    val selectedSystemPromptText: String? = null,
    /** 是否启用图片压缩 */
    val enableImageCompression: Boolean = true,
)

data class AttachedMedia(
    val uri: Uri,
    val url: String? = null,
    val name: String = "",
    val mimeType: String = "",
    /** 本地解析的文本内容或 Base64 编码 */
    val localContent: String? = null,
    /** 本地文件路径 */
    val localPath: String? = null,
    /** 是否为图片 */
    val isImage: Boolean = false,
    /** 文件大小（字节） */
    val size: Long = 0,
    /** 解析错误信息 */
    val parseError: String? = null,
    /** 处理状态 */
    val processingState: AttachmentProcessingState = AttachmentProcessingState.IDLE,
    /** 缩略图 Base64（约 5-10KB，用于 ImageEntity 持久化） */
    val thumbnailBase64: String? = null,
)

/**
 * 附件处理状态
 */
sealed class AttachmentProcessingState {
    /** 空闲 */
    object IDLE : AttachmentProcessingState()

    /** 处理中 */
    data class PROCESSING(
        val step: ProcessingStep,
        val detail: String? = null
    ) : AttachmentProcessingState()

    /** 处理完成 */
    data class COMPLETED(
        val originalSize: Long,
        val processedSize: Long,
        val format: String = "webp"
    ) : AttachmentProcessingState()

    /** 处理失败 */
    data class ERROR(val message: String) : AttachmentProcessingState()
}

/**
 * 处理步骤
 */
enum class ProcessingStep(val displayName: String) {
    CHECKING("检查图片"),
    RESIZING("调整尺寸"),
    CONVERTING("转换格式"),
    COMPRESSING("压缩大小"),
    ENCODING("编码处理"),
    UPLOADING("上传中")
}