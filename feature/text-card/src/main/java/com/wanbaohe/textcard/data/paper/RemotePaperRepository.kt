package com.wanbaohe.textcard.data.paper

import android.content.Context
import com.shifenmiao.network.NetworkBuilder
import com.shifenmiao.network.api.ApiService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.textcard.domain.TextCardPaperRepository
import com.wanbaohe.textcard.domain.model.RemotePaper
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TextCardPaperRepository 实现:ApiService 拉列表 → 图片下载到
 * filesDir/textcard/papers/(按 URL 哈希命名,已存在不重复下载)。
 * 任一环节失败返回空表,由调用方静默降级。
 *
 * 下载用独立最小 OkHttpClient(与 FontDownloadStore 同理):
 * 图片地址可能拼的是绝对 CDN/后台地址,不挂 App 鉴权拦截器。
 */
@Singleton
internal class RemotePaperRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val apiService: ApiService,
    private val dispatchersHolder: DispatchersHolder,
) : TextCardPaperRepository {

    private val papersDir = File(context.filesDir, "textcard/papers")

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun loadLocalPapers(): List<RemotePaper> =
        withContext(dispatchersHolder.ioDispatcher) {
            runCatching {
                val response = apiService.fetchTextCardPapers()
                if (!response.isSuccessful) return@runCatching emptyList()
                response.body()?.data.orEmpty().mapNotNull { paper ->
                    val url = resolveUrl(paper.image?.url) ?: return@mapNotNull null
                    val path = download(url) ?: return@mapNotNull null
                    RemotePaper(title = paper.title, localPath = path)
                }
            }.onFailure { it.makeLog("TextCardPapers") }
                .getOrDefault(emptyList())
        }

    /** 相对路径拼 baseUrl;已是 http(s) 绝对地址直接用 */
    private fun resolveUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        if (url.startsWith("http")) return url
        val base = NetworkBuilder.getBaseUrl().trimEnd('/')
        if (base.isBlank()) return null
        return base + if (url.startsWith("/")) url else "/$url"
    }

    /** 按 URL 哈希命名落盘;已存在且非空直接复用 */
    private fun download(url: String): String? {
        papersDir.mkdirs()
        val file = File(papersDir, url.md5() + ".img")
        if (file.isFile && file.length() > 0) return file.absolutePath
        return runCatching {
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body
                file.outputStream().use { output ->
                    body.byteStream().copyTo(output)
                }
            }
            file.takeIf { it.length() > 0 }?.absolutePath
        }.onFailure {
            it.makeLog("TextCardPaperDownload")
            file.delete()
        }.getOrNull()
    }

    private fun String.md5(): String {
        val digest = MessageDigest.getInstance("MD5").digest(toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
