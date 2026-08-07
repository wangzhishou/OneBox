package com.shifenmiao.ai.upload

import android.content.Context
import android.net.Uri
import com.wanbaohe.cloud.storage.data.CloudStorageRepository
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 云存储上传结果
 */
data class CloudUploadResult(
    val url: String,
    val bucket: String,
    val key: String,
    val connectionId: String,
)

/**
 * 云存储上传处理器
 *
 * 支持上传到：
 * - 阿里云 OSS
 * - 腾讯 COS
 * - AWS S3
 * - 华为 OBS
 */
@Singleton
class CloudFileUploadHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cloudStorageRepository: CloudStorageRepository,
) {
    /**
     * 上传文件到云存储
     *
     * @param uri 文件URI
     * @param connectionId 云存储连接ID
     * @param bucket Bucket名称
     * @param prefix 上传前缀（如 "ai-uploads/"）
     * @param contentType 文件类型
     * @return 上传结果，包含公开访问URL
     */
    suspend fun upload(
        uri: Uri,
        connectionId: String,
        bucket: String,
        prefix: String = "ai-uploads/",
        contentType: String? = null,
    ): Result<CloudUploadResult> = withContext(Dispatchers.IO) {
        try {
            // 1. 获取连接配置
            val connection = cloudStorageRepository.getConnections()
                .find { it.id == connectionId }
                ?: return@withContext Result.failure(
                    IllegalStateException("云存储配置不存在: $connectionId")
                )

            // 2. 读取文件内容
            val contentResolver = context.contentResolver
            val mimeType = contentType ?: contentResolver.getType(uri) ?: "application/octet-stream"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(
                    IllegalStateException("无法读取文件: $uri")
                )

            // 3. 生成对象Key
            val fileName = getFileName(uri)
            val extension = fileName.substringAfterLast('.', "").let {
                if (it.isNotBlank()) ".$it" else ""
            }
            val key = "${prefix.trimEnd('/')}/${UUID.randomUUID()}$extension"

            // 4. 上传到云存储
            cloudStorageRepository.uploadObject(
                connection = connection,
                bucket = bucket,
                key = key,
                body = bytes,
                contentType = mimeType,
            ).fold(
                onSuccess = {
                    // 5. 生成公开访问URL
                    val url = cloudStorageRepository.buildSignedGetUrl(
                        connection = connection,
                        bucket = bucket,
                        key = key,
                        expiresInSeconds = 3600 * 24 * 7 // 7天有效期
                    ) ?: return@fold Result.failure(IllegalStateException("协议 ${connection.protocol} 不支持生成下载链接"))
                    Result.success(
                        CloudUploadResult(
                            url = url,
                            bucket = bucket,
                            key = key,
                            connectionId = connectionId,
                        )
                    )
                },
                onFailure = { e ->
                    Result.failure(Exception("上传失败: ${e.message}"))
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取默认连接
     */
    fun getDefaultConnection(): CloudStorageConnection? {
        return cloudStorageRepository.getConnections().firstOrNull { it.isDefault }
            ?: cloudStorageRepository.getConnections().firstOrNull()
    }

    /**
     * 获取所有连接
     */
    fun getConnections(): List<CloudStorageConnection> {
        return cloudStorageRepository.getConnections()
    }

    /**
     * 获取文件名
     */
    private fun getFileName(uri: Uri): String {
        val lastPathSegment = uri.lastPathSegment
        if (lastPathSegment != null) {
            val fileName = lastPathSegment.substringAfterLast("/")
            if (fileName.isNotBlank()) {
                return fileName
            }
        }

        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        cursor.getString(nameIndex) ?: "unknown"
                    } else {
                        "unknown"
                    }
                } else {
                    "unknown"
                }
            } ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
