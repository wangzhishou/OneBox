package com.wanbaohe.setting.local.download

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 本地模型管理页私有的简单下载器:OkHttp GET → `<name>.download` 临时文件,
 * 完成后 rename 为目标文件;取消/失败都会删除临时文件。不做断点续传。
 */
internal class LocalModelDownloader {

    sealed interface Result {
        data class Success(val file: File) : Result
        data object Cancelled : Result
        data class Failed(val cause: Throwable) : Result
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val activeCalls = ConcurrentHashMap<String, Call>()

    suspend fun download(
        url: String,
        destination: File,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): Result = withContext(Dispatchers.IO) {
        val key = destination.name
        val tempFile = File(destination.parentFile, "${destination.name}.download")
        destination.parentFile?.mkdirs()
        val call = client.newCall(Request.Builder().url(url).get().build())
        activeCalls[key] = call
        // 页面销毁导致协程取消时,同步中断阻塞中的 OkHttp 调用
        val job = currentCoroutineContext().job
        val cancellationHandle = job.invokeOnCompletion { if (!job.isActive) call.cancel() }
        try {
            call.execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                val body = response.body ?: throw IOException("Empty response body")
                val totalBytes = body.contentLength()
                body.byteStream().use { input ->
                    tempFile.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var downloadedBytes = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloadedBytes += read
                            onProgress(downloadedBytes, totalBytes)
                        }
                        output.flush()
                    }
                }
            }
            if (call.isCanceled()) throw CancellationException("Download cancelled")
            if (destination.exists()) destination.delete()
            if (!tempFile.renameTo(destination)) throw IOException("Failed to move downloaded file")
            Result.Success(destination)
        } catch (e: Exception) {
            tempFile.delete()
            if (call.isCanceled() || e is CancellationException) {
                Result.Cancelled
            } else {
                Result.Failed(e)
            }
        } finally {
            cancellationHandle.dispose()
            activeCalls.remove(key)
        }
    }

    fun cancel(fileName: String) {
        activeCalls[fileName]?.cancel()
    }
}
