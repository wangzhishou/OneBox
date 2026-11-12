/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.network.downloader

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.job
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

        val call = client.newCall(requestBuilder.build())
        val job = currentCoroutineContext().job
        val cancellationHandle = job.invokeOnCompletion {
            if (!job.isActive) call.cancel()
        }

        try {
            call.execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}")
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

                if (totalBytes != null && downloaded != totalBytes) {
                    throw java.io.IOException("Incomplete download: $downloaded / $totalBytes bytes")
                }
                if (destinationFile.exists()) destinationFile.delete()
                if (!partFile.renameTo(destinationFile)) {
                    partFile.copyTo(destinationFile, overwrite = true)
                    partFile.delete()
                }

                emit(DownloadState.Success(destinationFile))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            currentCoroutineContext().ensureActive()
            emit(DownloadState.Failed(t))
        } finally {
            cancellationHandle.dispose()
        }
    }.flowOn(Dispatchers.IO)
}
