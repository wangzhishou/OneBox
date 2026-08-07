package com.shifenmiao.network.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetAddress
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * URL 检查服务
 *
 * 检查 URL 的可访问性、响应状态、重定向链等信息
 */
@Singleton
class UrlCheckService @Inject constructor() {

    companion object {
        private const val DEFAULT_TIMEOUT_SECONDS = 10L
    }

    /**
     * URL 检查结果
     */
    data class UrlCheckResult(
        val url: String,
        val isAccessible: Boolean,
        val statusCode: Int?,
        val statusMessage: String?,
        val responseTimeMs: Long,
        val redirectChain: List<String>,
        val finalUrl: String?,
        val contentType: String?,
        val contentLength: Long?,
        val serverHeader: String?,
        val ipAddress: String?,
        val error: String? = null
    )

    /**
     * 检查 URL 状态
     *
     * @param url 要检查的 URL
     * @param timeoutSeconds 超时时间（秒）
     * @param followRedirects 是否跟踪重定向
     * @return 检查结果
     */
    suspend fun check(
        url: String,
        timeoutSeconds: Long = DEFAULT_TIMEOUT_SECONDS,
        followRedirects: Boolean = true
    ): Result<UrlCheckResult> = withContext(Dispatchers.IO) {
        try {
            val normalizedUrl = normalizeUrl(url)
            val redirectChain = mutableListOf<String>()

            val client = OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .followRedirects(false) // 手动处理重定向以记录链
                .build()

            val startTime = System.currentTimeMillis()
            var currentUrl = normalizedUrl
            var statusCode: Int? = null
            var statusMessage: String? = null
            var contentType: String? = null
            var contentLength: Long? = null
            var serverHeader: String? = null
            var isAccessible = false
            var error: String? = null

            // 手动跟踪重定向
            var redirectCount = 0
            val maxRedirects = 10

            while (redirectCount < maxRedirects) {
                val request = Request.Builder()
                    .url(currentUrl)
                    .head() // 使用 HEAD 请求节省带宽
                    .header("User-Agent", "Mozilla/5.0 (compatible; UrlChecker/1.0)")
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    statusCode = response.code
                    statusMessage = response.message
                    contentType = response.header("Content-Type")
                    contentLength = response.header("Content-Length")?.toLongOrNull()
                    serverHeader = response.header("Server")

                    if (response.isSuccessful) {
                        isAccessible = true
                        break
                    }

                    // 处理重定向
                    if (statusCode in 301..308 && followRedirects) {
                        val location = response.header("Location")
                        if (location != null) {
                            redirectChain.add(currentUrl)
                            currentUrl = if (location.startsWith("http")) {
                                location
                            } else {
                                URI(currentUrl).resolve(location).toString()
                            }
                            redirectCount++
                            continue
                        }
                    }

                    // 非重定向的非成功状态码
                    isAccessible = false
                    error = "HTTP $statusCode: $statusMessage"
                    break

                } catch (e: IOException) {
                    isAccessible = false
                    error = e.message ?: "Unknown error"
                    break
                }
            }

            if (redirectCount >= maxRedirects) {
                error = "Too many redirects (max: $maxRedirects)"
            }

            val responseTimeMs = System.currentTimeMillis() - startTime

            // 解析 IP 地址
            val ipAddress = try {
                val host = java.net.URL(normalizedUrl).host
                InetAddress.getByName(host).hostAddress
            } catch (e: Exception) {
                null
            }

            Result.success(
                UrlCheckResult(
                    url = normalizedUrl,
                    isAccessible = isAccessible,
                    statusCode = statusCode,
                    statusMessage = statusMessage,
                    responseTimeMs = responseTimeMs,
                    redirectChain = redirectChain,
                    finalUrl = if (redirectChain.isNotEmpty()) currentUrl else null,
                    contentType = contentType,
                    contentLength = contentLength,
                    serverHeader = serverHeader,
                    ipAddress = ipAddress,
                    error = error
                )
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 规范化 URL
     */
    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            else -> "https://$trimmed"
        }
    }
}
