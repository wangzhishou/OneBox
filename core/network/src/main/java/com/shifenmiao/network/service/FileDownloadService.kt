package com.shifenmiao.network.service

import android.content.Context
import android.os.Environment
import com.shifenmiao.network.downloader.DownloadState
import com.shifenmiao.network.downloader.ResumableFileDownloader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文件下载服务
 *
 * 提供文件下载功能，支持进度回调、断点续传、自定义保存路径
 */
@Singleton
class FileDownloadService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val DEFAULT_DOWNLOAD_DIR = "downloads"
    }

    /**
     * 下载结果
     */
    data class DownloadResult(
        val file: File,
        val fileName: String,
        val fileSize: Long,
        val downloadUrl: String,
        val savedPath: String
    )

    /**
     * 下载文件
     *
     * @param url 文件下载 URL
     * @param fileName 保存的文件名（可选，默认从 URL 或 Content-Disposition 提取）
     * @param savePath 保存路径（相对路径，默认 downloads/）
     * @param overwrite 是否覆盖已存在的文件
     * @return 下载结果的 Flow，包含进度和最终结果
     */
    fun download(
        url: String,
        fileName: String? = null,
        savePath: String? = null,
        overwrite: Boolean = false
    ): Flow<DownloadState> {
        val resolvedFileName = fileName ?: extractFileName(url)
        val resolvedSavePath = savePath ?: DEFAULT_DOWNLOAD_DIR
        
        val downloadDir = File(context.getExternalFilesDir(null), resolvedSavePath)
        downloadDir.mkdirs()
        
        val destinationFile = File(downloadDir, resolvedFileName)
        
        // 如果文件已存在且不覆盖，直接返回成功
        if (destinationFile.exists() && !overwrite) {
            return kotlinx.coroutines.flow.flow {
                emit(DownloadState.Success(destinationFile))
            }.flowOn(Dispatchers.IO)
        }

        val downloader = ResumableFileDownloader(
            OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()
        )

        return downloader.download(url, destinationFile)
    }

    /**
     * 同步下载文件（等待完成）
     *
     * @param url 文件下载 URL
     * @param fileName 保存的文件名
     * @param savePath 保存路径
     * @param overwrite 是否覆盖已存在的文件
     * @return 下载结果
     */
    suspend fun downloadSync(
        url: String,
        fileName: String? = null,
        savePath: String? = null,
        overwrite: Boolean = false
    ): Result<DownloadResult> = withContext(Dispatchers.IO) {
        try {
            val resolvedFileName = fileName ?: extractFileName(url)
            val resolvedSavePath = savePath ?: DEFAULT_DOWNLOAD_DIR
            
            val downloadDir = File(context.getExternalFilesDir(null), resolvedSavePath)
            downloadDir.mkdirs()
            
            val destinationFile = File(downloadDir, resolvedFileName)
            
            // 如果文件已存在且不覆盖，直接返回
            if (destinationFile.exists() && !overwrite) {
                return@withContext Result.success(
                    DownloadResult(
                        file = destinationFile,
                        fileName = resolvedFileName,
                        fileSize = destinationFile.length(),
                        downloadUrl = url,
                        savedPath = destinationFile.absolutePath
                    )
                )
            }

            val downloader = ResumableFileDownloader(
                OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
            )

            var lastState: DownloadState? = null
            downloader.download(url, destinationFile).collect { state ->
                lastState = state
            }

            when (val finalState = lastState) {
                is DownloadState.Success -> {
                    Result.success(
                        DownloadResult(
                            file = finalState.file,
                            fileName = resolvedFileName,
                            fileSize = finalState.file.length(),
                            downloadUrl = url,
                            savedPath = finalState.file.absolutePath
                        )
                    )
                }
                is DownloadState.Failed -> {
                    Result.failure(finalState.throwable)
                }
                else -> {
                    Result.failure(Exception("Download incomplete"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从 URL 提取文件名
     */
    private fun extractFileName(url: String): String {
        return try {
            val path = java.net.URL(url).path
            val fileName = path.substringAfterLast("/").substringBefore("?")
            if (fileName.isNotBlank() && fileName.contains(".")) {
                fileName
            } else {
                "download_${UUID.randomUUID().toString().take(8)}"
            }
        } catch (e: Exception) {
            "download_${UUID.randomUUID().toString().take(8)}"
        }
    }

    /**
     * 获取下载目录
     */
    fun getDownloadDirectory(): File {
        return File(context.getExternalFilesDir(null), DEFAULT_DOWNLOAD_DIR).apply {
            mkdirs()
        }
    }

    /**
     * 列出已下载的文件
     */
    fun listDownloadedFiles(): List<File> {
        val downloadDir = getDownloadDirectory()
        return downloadDir.listFiles()?.filter { it.isFile && !it.name.endsWith(".part") } ?: emptyList()
    }

    /**
     * 删除下载的文件
     */
    fun deleteDownloadedFile(fileName: String): Boolean {
        val file = File(getDownloadDirectory(), fileName)
        return file.delete()
    }
}
