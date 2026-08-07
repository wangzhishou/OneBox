package com.shifenmiao.ai.upload

import android.net.Uri
import com.shifenmiao.model.ai.AttachmentPayloadDto
import com.shifenmiao.model.ai.AttachedMedia
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 附件内容解析器 — 解耦"附件元数据"与"附件实际内容"的关系。
 *
 * 设计目的：
 * 1. 当前会话：localContent 在内存 AttachedMedia 中，直接使用（零 IO）
 * 2. 历史消息：DB 只存 localPath 引用，需从缓存文件读取 base64
 * 3. 缓存丢失：localContent 为 null，发送时由 FileUploadRouter 从 URI 重新处理
 *
 * 数据流：
 * - 选图 → FileAnalyzer 处理 → localContent 留在内存 → 发送时直接用
 * - 序列化到 DB → AttachmentPayloadDto.from() 排除 localContent → 仅存 localPath
 * - 历史加载 → resolveAttachments() 从 localPath 恢复 localContent
 *
 * 线程安全：所有方法均为 suspend，调用方需在 IO 协程中执行。
 */
@Singleton
class AttachmentContentResolver @Inject constructor(
    private val fileAnalyzer: FileAnalyzer,
) {
    /**
     * 将 DTO 列表转换为 AttachedMedia 列表。
     *
     * 优先级：
     * 1. DTO 已有 localContent（当前会话内存态）→ 直接使用
     * 2. localPath 存在 → 从缓存文件读取 base64
     * 3. 均不可用 → localContent 为 null，发送时由 FileUploadRouter 从 URI 重新处理
     */
    suspend fun resolveAttachments(dtos: List<AttachmentPayloadDto>): List<AttachedMedia> {
        return dtos.map { dto -> resolveSingle(dto) }
    }

    /**
     * 单个 DTO 解析为 AttachedMedia。
     */
    suspend fun resolveSingle(dto: AttachmentPayloadDto): AttachedMedia {
        val resolvedContent = dto.localContent
            ?: dto.localPath?.let { path -> fileAnalyzer.readCachedBase64(path) }

        return AttachedMedia(
            uri = Uri.parse(dto.uri),
            name = dto.name,
            mimeType = dto.mimeType,
            size = dto.size,
            localContent = resolvedContent,
            localPath = dto.localPath,
            parseError = dto.parseError,
            url = dto.url,
            isImage = resolvedContent != null || dto.mimeType.startsWith("image/")
        )
    }

    /**
     * 直接从缓存路径读取 base64 内容。
     * 供 AiUtils 等无法注入此类的场景通过 lambda 传递使用。
     *
     * @param localPath 缓存文件绝对路径
     * @return data:image/webp;base64,... 格式字符串，失败返回 null
     */
    suspend fun readContentFromPath(localPath: String): String? {
        return fileAnalyzer.readCachedBase64(localPath)
    }
}
