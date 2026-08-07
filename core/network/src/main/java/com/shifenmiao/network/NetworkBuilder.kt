package com.shifenmiao.network

import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.core.constants.UrlConstants.OPENAI_BASE_URL
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.storage.RemoteConfigStorage
import okhttp3.Cache
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

/**
 * 参考使用例子
 * viewModelScope.launch {
 *     // 同时发起两个请求
 *     val request1 = async { RetrofitClient.apiService.fetchData() }
 *     val request2 = async { RetrofitClient.apiService.fetchData() }
 *
 *     // 等待两个请求都完成
 *     val response1 = request1.await()
 *     val response2 = request2.await()
 *
 *     // 处理响应
 *     if (response1.isSuccessful && response2.isSuccessful) {
 *         // Combine or process the data from both responses
 *     } else {
 *         // Handle error
 *     }
 * }
 */
object NetworkBuilder {

    // 设置缓存目录和大小
    private var cacheDirectory: File =
        File(AppContext.getContext().cacheDir, "http_cache")
    var cacheSize = 100 * 1024 * 1024 // 100 MiB

    var cache: Cache = Cache(cacheDirectory, cacheSize.toLong())

    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (CoreUtils.isOneBoxDebug() || CoreUtils.isGoogleDebug()) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
            redactHeader("api-key")
            redactHeader("x-api-key")
        }
    }

    private var baseUrl = UrlConstants.RELEASE_URL

    fun setBaseUrl(url: String) {
        baseUrl = url
    }

    fun getBaseUrl(): String {
        // 未注入域名时(如开源构建)回退到本地占位地址: 请求立即失败,
        // 不触达任何真实服务器, 也避免 Retrofit 因空 baseUrl 抛异常
        return baseUrl.ifBlank { "http://localhost/" }
    }

    fun isDebug(): Boolean {
        // 空域名按 release 语义处理 (开源构建无 debug 后端)
        return baseUrl.isNotBlank() && baseUrl == UrlConstants.DEBUG_URL
    }

    fun getTimeOut(): Long {
        return RemoteConfigStorage.getRemoteConfig().timeOut ?: 3L
    }

    /**
     * Ensures a valid base URL for Retrofit
     *
     * @param engine The AiEngine to extract requestUrl from
     * @return A properly formatted URL with http/https scheme
     */
    fun ensureValidBaseUrl(engine: AiEngine): String {
        return engine.requestUrl.let {
            when {
                it.isBlank() -> OPENAI_BASE_URL // Fallback URL
                !it.startsWith("http://") && !it.startsWith("https://") -> "https://$it" // Add https:// prefix
                else -> it
            }
        }
    }
}