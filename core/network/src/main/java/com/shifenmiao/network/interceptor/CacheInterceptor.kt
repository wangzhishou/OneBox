package com.shifenmiao.network.interceptor

import com.shifenmiao.base.utils.NetWorkUtils
import com.shifenmiao.core.constants.Constants.NET_CACHE_TIME
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.storage.RemoteConfigStorage
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * 全局 HTTP 缓存拦截器。
 *
 * - `/api/remote-configs`：永不缓存，强制走网络，避免配置不生效。
 * - 带 `X-Force-Refresh` 头的请求：强制走网络，用于手动刷新或同步间隔到期。
 * - `/api/items/sync`、`/api/categories/sync` 等其它接口：按 [RemoteConfig.networkCache]
 *   与 [RemoteConfig.cacheTimeout] 决定是否缓存。
 */
class CacheInterceptor : Interceptor {

    /**
     * 这些路径必须实时，不经过 OkHttp 缓存。
     */
    private val noCachePaths = setOf("/api/remote-configs")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val encodedPath = url.encodedPath

        // 1. RemoteConfig 等实时接口：强制走网络，响应标记 no-store
        if (encodedPath in noCachePaths) {
            val noCacheRequest = request.newBuilder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build()
            return chain.proceed(noCacheRequest)
                .newBuilder()
                .header("Cache-Control", "no-store")
                .build()
        }

        // 2. 强制刷新：手动下拉 或 同步间隔到期
        if (request.header(FORCE_REFRESH_HEADER) == "true") {
            return chain.proceed(
                request.newBuilder()
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build()
            )
        }

        // 3. 常规接口按 RemoteConfig 缓存策略处理
        val needCacheValue = url.queryParameter(UrlConstants.NEED_CACHE_PARAM_NAME)
        val needCache = RemoteConfigStorage.getRemoteConfig().networkCache == true
        val cacheTimeoutSeconds = RemoteConfigStorage.getRemoteConfig().cacheTimeout
            ?: (NET_CACHE_TIME * 60)

        val cleanedRequest = if (needCacheValue != null) {
            val newUrl = url.newBuilder()
                .removeAllQueryParameters(UrlConstants.NEED_CACHE_PARAM_NAME)
                .build()
            request.newBuilder().url(newUrl).build()
        } else {
            request
        }

        val cacheControl = when {
            needCacheValue != null -> CacheControl.Builder()
                .maxAge(needCacheValue.toInt(), TimeUnit.HOURS)
                .build()
            needCache -> CacheControl.Builder()
                .maxAge(cacheTimeoutSeconds, TimeUnit.SECONDS)
                .build()
            else -> CacheControl.FORCE_NETWORK
        }

        val networkAvailable = NetWorkUtils.isNetworkAvailable()
        val finalRequest = cleanedRequest.newBuilder().apply {
            if (networkAvailable) {
                cacheControl(cacheControl)
            } else {
                cacheControl(CacheControl.FORCE_CACHE)
            }
        }.build()

        val response = chain.proceed(finalRequest)

        return if (needCache && networkAvailable) {
            response.newBuilder()
                .header("Cache-Control", "public, max-age=$cacheTimeoutSeconds")
                .build()
        } else {
            response
        }
    }

    companion object {
        const val FORCE_REFRESH_HEADER = "X-Force-Refresh"
    }
}
