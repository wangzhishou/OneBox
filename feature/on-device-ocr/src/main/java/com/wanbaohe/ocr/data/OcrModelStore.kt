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

package com.wanbaohe.ocr.data

import android.content.Context
import com.shifenmiao.storage.RemoteConfigStorage
import com.wanbaohe.ocr.domain.PpOcrSpec
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class OcrModelStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val root = File(context.filesDir, "ocr_models/${PpOcrSpec.PACK_ID}")
    private val cacheZip = File(context.cacheDir, "${PpOcrSpec.PACK_ID}.zip")

    fun isReady(): Boolean = PpOcrSpec.files.all { name ->
        file(name).isFile && file(name).length() > MIN_FILE_BYTES
    }

    fun file(name: String): File = File(root, name)

    suspend fun ensureDownloaded(onProgress: (Float?) -> Unit) {
        if (isReady()) {
            onProgress(1f)
            return
        }
        withContext(Dispatchers.IO) {
            root.deleteRecursively()
            root.mkdirs()
            try {
                downloadZip(onProgress)
                check(sha256(cacheZip) == PpOcrSpec.PACK_SHA256) { "model pack checksum mismatch" }
                unzip(cacheZip, root)
                check(isReady()) { "model pack is incomplete" }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                root.deleteRecursively()
                throw cancelled
            } catch (error: Exception) {
                root.deleteRecursively()
                throw error
            } finally {
                cacheZip.delete()
            }
        }
    }

    private suspend fun downloadZip(onProgress: (Float?) -> Unit) {
        val downloadUrl = RemoteConfigStorage.getRemoteConfig().ocrModelPackUrl
            ?.takeIf { it.isNotBlank() }
            ?: PpOcrSpec.DEFAULT_PACK_URL
        val connection = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            instanceFollowRedirects = true
        }
        try {
            connection.connect()
            check(connection.responseCode in HTTP_OK_RANGE) {
                "download failed: ${connection.responseCode}"
            }
            val total = connection.contentLengthLong.takeIf { it > 0L }
            connection.inputStream.use { input ->
                FileOutputStream(cacheZip).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var downloaded = 0L
                    var lastEmit = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        downloaded += count
                        val now = System.currentTimeMillis()
                        if (now - lastEmit >= PROGRESS_INTERVAL_MS) {
                            lastEmit = now
                            onProgress(total?.let { downloaded.toFloat() / it })
                        }
                    }
                }
            }
            onProgress(1f)
        } finally {
            connection.disconnect()
        }
    }

    private fun unzip(zip: File, destination: File) {
        ZipInputStream(zip.inputStream()).use { archive ->
            while (true) {
                val entry = archive.nextEntry ?: break
                val name = File(entry.name).name
                if (entry.isDirectory || name !in PpOcrSpec.files) {
                    archive.closeEntry()
                    continue
                }
                File(destination, name).outputStream().use { output ->
                    archive.copyTo(output)
                }
                archive.closeEntry()
            }
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte.toInt() and 0xFF)
        }
    }

    private companion object {
        const val MIN_FILE_BYTES = 8_000L
        const val BUFFER_SIZE = 8_192
        const val CONNECT_TIMEOUT_MS = 20_000
        const val READ_TIMEOUT_MS = 60_000
        const val PROGRESS_INTERVAL_MS = 80L
        val HTTP_OK_RANGE = 200..299
    }
}
