package com.shifenmiao.network.downloader

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

sealed class DownloadState {
    data class Progress(
        val downloadedBytes: Long,
        val totalBytes: Long?
    ) : DownloadState()

    data class Success(val file: File) : DownloadState()
    data class Failed(val throwable: Throwable) : DownloadState()
}

class ResumableFileDownloader(
    private val client: OkHttpClient = OkHttpClient()
) {
    fun download(
        url: String,
        destinationFile: File
    ): Flow<DownloadState> = flow {
        val partFile = File(destinationFile.absolutePath + ".part")
        destinationFile.parentFile?.mkdirs()

        val existingBytes = if (partFile.exists()) partFile.length() else 0L

        val requestBuilder = Request.Builder().url(url)
        if (existingBytes > 0L) {
            requestBuilder.addHeader("Range", "bytes=$existingBytes-")
        }
        val request = requestBuilder.build()

        val call = client.newCall(request)
        val response = call.execute()
        if (!response.isSuccessful) {
            response.close()
            throw IllegalStateException("HTTP ${response.code}")
        }

        val body = response.body

        val append = response.code == 206 && existingBytes > 0L
        if (!append && partFile.exists()) partFile.delete()

        val contentLength = body.contentLength().takeIf { it >= 0 }
        val totalBytes = if (append) {
            contentLength?.let { existingBytes + it }
        } else {
            contentLength
        }

        var downloaded = if (append) existingBytes else 0L
        emit(DownloadState.Progress(downloadedBytes = downloaded, totalBytes = totalBytes))

        try {
            body.byteStream().use { input ->
                FileOutputStream(partFile, append).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var lastEmitAt = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        val now = System.currentTimeMillis()
                        if (now - lastEmitAt >= 200L) {
                            lastEmitAt = now
                            emit(DownloadState.Progress(downloadedBytes = downloaded, totalBytes = totalBytes))
                        }
                    }
                    output.flush()
                }
            }

            if (destinationFile.exists()) destinationFile.delete()
            val moved = partFile.renameTo(destinationFile)
            if (!moved) {
                destinationFile.writeBytes(partFile.readBytes())
                partFile.delete()
            }

            emit(DownloadState.Success(destinationFile))
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            emit(DownloadState.Failed(t))
        } finally {
            response.close()
        }
    }.flowOn(Dispatchers.IO)
}
