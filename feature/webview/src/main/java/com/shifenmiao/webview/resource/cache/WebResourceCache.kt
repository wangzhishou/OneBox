package com.shifenmiao.webview.resource.cache

import android.content.Context
import android.webkit.WebResourceResponse
import com.shifenmiao.webview.resource.MimeTypeUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebView 远程资源 HTTP 缓存。
 *
 * 设计要点：
 * 1. **磁盘缓存走 OkHttp Cache** — 自动处理 `Cache-Control` / `ETag` / `Last-Modified` / 304，
 *    不再自维护 MD5 文件 + metadata 双目录。
 * 2. **TTL 覆盖** — 调用方传入 [cacheTtlMillis] 时，会在响应中追加
 *    `Cache-Control: max-age=...`，让 OkHttp 后续按该 TTL 判定。
 * 3. **同步 API** — `shouldInterceptRequest` 跑在 WebView 的 Binder 线程上，
 *    直接阻塞 IO 是允许的，**不**做协程切换。
 *
 * 缓存目录：`{cacheDir}/webview_cache/`，默认 50MB。
 * 单例由 Hilt 提供；不要 new 多个。
 */
@Singleton
class WebResourceCache @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val appContext: Context = context.applicationContext

    val client: OkHttpClient = OkHttpClient.Builder()
        .cache(Cache(File(appContext.cacheDir, DISK_CACHE_DIR), DISK_CACHE_SIZE))
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * 同步 GET 资源；命中 OkHttp 磁盘缓存时直接返回（不发网络）。
     *
     * **流式 body**：body 通过 [okhttp3.ResponseBody.byteStream] 直接交给 WebView，**不**在内存中
     * 缓冲整个响应。WebView 读多少 OkHttp 给多少（缓存命中走磁盘、网络命中走 socket）。
     * OkHttp 在流被完全消费后自动释放连接。
     *
     * **生命周期**：返回的 [WebResourceResponse] 持有对 OkHttp body 的 InputStream 引用。
     * **不要在调用方 `.use {}` 包住 OkHttp 响应**——那会在 WebView 还没读完前关掉 stream。
     *
     * @param url 真实资源 URL（非 WebView 看到的虚拟 URL）。
     * @param cacheTtlMillis 非 null 时覆盖响应 `Cache-Control: max-age=...`；
     *                       null 时尊重服务端返回的 Cache-Control。
     * @return 资源响应；网络失败 / 缓存空时返回 null。
     */
    fun fetchBlocking(url: String, cacheTtlMillis: Long? = null): WebResourceResponse? {
        if (url.isBlank()) return null
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return null
            }
            val rawHeader = response.header("Content-Type")
            val mime = MimeTypeUtils.normalizeContentType(rawHeader, url)
            // TTL 覆盖：在内存 response 上追加 Cache-Control。
            // OkHttp 缓存会按修改后的 header 落盘，下次按 max-age 判定。
            val effectiveHeaders = if (cacheTtlMillis != null && cacheTtlMillis > 0) {
                val seconds = TimeUnit.MILLISECONDS.toSeconds(cacheTtlMillis).coerceAtLeast(1L)
                response.headers.newBuilder()
                    .set("Cache-Control", "max-age=$seconds")
                    .build()
            } else {
                response.headers
            }
            // 直接传 byteStream()：WebView 边读 OkHttp 边吐，不在内存中缓冲。
            // response 生命周期交给 OkHttp 内部管理（流消费完后自动释放连接）。
            WebResourceResponse(
                mime,
                MimeTypeUtils.DEFAULT_CHARSET,
                response.code,
                response.message.ifBlank { "OK" },
                effectiveHeaders.toMap(),
                response.body.byteStream(),
            )
        } catch (t: Throwable) {
            // execute() 抛异常（DNS / 连接失败 / 超时）→ 返回 null
            null
        }
    }

    /** 清空磁盘缓存（不影响 OkHttp 客户端本身）。 */
    fun clear() {
        runCatching { client.cache?.evictAll() }
    }

    /** 当前磁盘占用字节数。 */
    fun size(): Long = client.cache?.size() ?: 0L

    companion object {
        const val DISK_CACHE_DIR = "webview_cache"
        const val DISK_CACHE_SIZE = 50L * 1024L * 1024L
        const val CONNECT_TIMEOUT_SECONDS = 10L
        const val READ_TIMEOUT_SECONDS = 15L
    }
}
