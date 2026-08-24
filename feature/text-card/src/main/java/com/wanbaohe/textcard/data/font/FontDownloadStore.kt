package com.wanbaohe.textcard.data.font

import android.content.Context
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.wanbaohe.textcard.domain.FontCatalog
import com.wanbaohe.textcard.domain.model.DownloadableFont
import com.wanbaohe.textcard.domain.model.DownloadableFonts
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FontCatalog 实现:下载到 filesDir/textcard/fonts/,已下载清单持久化为 JSON 文件。
 *
 * 下载器为独立最小 OkHttpClient(不挂 core/network 的鉴权拦截器):
 * 字体地址是第三方公共 CDN,绝不能把 App 登录 token 带过去。
 */
@Singleton
internal class FontDownloadStore @Inject constructor(
    @ApplicationContext context: Context,
    private val dispatchersHolder: DispatchersHolder,
) : FontCatalog {

    private val fontsDir = File(context.filesDir, "textcard/fonts")
    private val indexFile = File(fontsDir, "downloaded.json")

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override val fonts: List<DownloadableFont> = DownloadableFonts.all

    override fun downloadedFont(font: DownloadableFont): FontType.File? {
        if (font.id !in readDownloadedIds()) return null
        val file = File(fontsDir, font.fileName)
        if (file.isFile && file.length() > 0) return FontType.File(file.absolutePath)
        // 记录残留但文件丢失:清理记录,回到未下载态
        removeDownloadedId(font.id)
        return null
    }

    override suspend fun download(
        font: DownloadableFont,
        onProgress: (Float) -> Unit,
    ): Result<FontType.File> = withContext(dispatchersHolder.ioDispatcher) {
        runCatching {
            onProgress(0f)
            fontsDir.mkdirs()
            val target = File(fontsDir, font.fileName)
            val temp = File(fontsDir, "${font.fileName}.part")

            client.newCall(Request.Builder().url(font.url).build()).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
                val body = response.body
                val total = body.contentLength()
                body.byteStream().use { input ->
                    temp.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var copied = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            copied += read
                            if (total > 0) onProgress(copied.toFloat() / total)
                        }
                    }
                }
            }
            check(temp.length() > 0) { "empty font file" }
            if (target.exists()) target.delete()
            check(temp.renameTo(target)) { "rename failed" }

            addDownloadedId(font.id)
            onProgress(1f)
            FontType.File(target.absolutePath)
        }.onFailure {
            File(fontsDir, "${font.fileName}.part").delete()
        }
    }

    // ---------------- 已下载清单(JSON 文件持久化) ----------------

    @Synchronized
    private fun readDownloadedIds(): Set<String> {
        if (!indexFile.isFile) return emptySet()
        return runCatching {
            val array = JSONArray(indexFile.readText())
            (0 until array.length()).mapTo(mutableSetOf()) { array.getString(it) }
        }.getOrDefault(emptySet())
    }

    @Synchronized
    private fun addDownloadedId(id: String) {
        writeDownloadedIds(readDownloadedIds() + id)
    }

    @Synchronized
    private fun removeDownloadedId(id: String) {
        writeDownloadedIds(readDownloadedIds() - id)
    }

    private fun writeDownloadedIds(ids: Set<String>) {
        fontsDir.mkdirs()
        runCatching { indexFile.writeText(JSONArray(ids.toList()).toString()) }
    }
}
