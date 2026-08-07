package com.shifenmiao.network.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HTTP 请求服务
 *
 * 提供通用的 HTTP 请求功能，支持自定义方法、头部、请求体
 */
@Singleton
class HttpRequestService @Inject constructor() {

    companion object {
        private const val DEFAULT_TIMEOUT_SECONDS = 30L
        private const val MAX_RESPONSE_BODY_LENGTH = 10 * 1024 // 10KB
    }

    /**
     * HTTP 方法
     */
    enum class HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
    }

    /**
     * 请求配置
     */
    data class RequestConfig(
        val url: String,
        val method: HttpMethod = HttpMethod.GET,
        val headers: Map<String, String> = emptyMap(),
        val body: String? = null,
        val contentType: String? = null,
        val timeoutSeconds: Long = DEFAULT_TIMEOUT_SECONDS,
        val followRedirects: Boolean = true
    )

    /**
     * 响应结果
     */
    data class HttpResponse(
        val statusCode: Int,
        val statusMessage: String,
        val headers: Map<String, List<String>>,
        val body: String?,
        val contentType: String?,
        val contentLength: Long?,
        val responseTimeMs: Long,
        val isSuccessful: Boolean
    )

    /**
     * 发送 HTTP 请求
     *
     * @param config 请求配置
     * @return 响应结果
     */
    suspend fun request(config: RequestConfig): Result<HttpResponse> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
                .followRedirects(config.followRedirects)
                .build()

            val requestBuilder = Request.Builder()
                .url(config.url)

            // 设置请求头
            config.headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }

            // 设置请求体
            when (config.method) {
                HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS -> {
                    // 这些方法通常没有请求体
                }
                HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH -> {
                    if (config.body != null) {
                        val mediaType = config.contentType?.toMediaTypeOrNull()
                        val requestBody = config.body.toRequestBody(mediaType)
                        when (config.method) {
                            HttpMethod.POST -> requestBuilder.post(requestBody)
                            HttpMethod.PUT -> requestBuilder.put(requestBody)
                            HttpMethod.DELETE -> requestBuilder.delete(requestBody)
                            HttpMethod.PATCH -> requestBuilder.patch(requestBody)
                        }
                    } else {
                        when (config.method) {
                            HttpMethod.POST -> requestBuilder.post("".toRequestBody(null))
                            HttpMethod.PUT -> requestBuilder.put("".toRequestBody(null))
                            HttpMethod.DELETE -> requestBuilder.delete()
                            HttpMethod.PATCH -> requestBuilder.patch("".toRequestBody(null))
                        }
                    }
                }
            }

            val startTime = System.currentTimeMillis()
            val response = client.newCall(requestBuilder.build()).execute()
            val responseTimeMs = System.currentTimeMillis() - startTime

            // 读取响应体（限制大小）
            val responseBody = response.body.let { body ->
                val bytes = body.bytes()
                if (bytes.size > MAX_RESPONSE_BODY_LENGTH) {
                    String(bytes, 0, MAX_RESPONSE_BODY_LENGTH) + "\n...(响应体已截断，共${bytes.size}字节)"
                } else {
                    String(bytes)
                }
            }

            // 转换响应头
            val responseHeaders = mutableMapOf<String, List<String>>()
            response.headers.forEach { (name, value) ->
                responseHeaders.getOrPut(name) { mutableListOf() }.let {
                    (it as MutableList).add(value)
                }
            }

            Result.success(
                HttpResponse(
                    statusCode = response.code,
                    statusMessage = response.message,
                    headers = responseHeaders,
                    body = responseBody,
                    contentType = response.header("Content-Type"),
                    contentLength = response.header("Content-Length")?.toLongOrNull(),
                    responseTimeMs = responseTimeMs,
                    isSuccessful = response.isSuccessful
                )
            )

        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 发送 GET 请求
     */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        timeoutSeconds: Long = DEFAULT_TIMEOUT_SECONDS
    ): Result<HttpResponse> {
        return request(
            RequestConfig(
                url = url,
                method = HttpMethod.GET,
                headers = headers,
                timeoutSeconds = timeoutSeconds
            )
        )
    }

    /**
     * 发送 POST 请求
     */
    suspend fun post(
        url: String,
        body: String,
        contentType: String = "application/json",
        headers: Map<String, String> = emptyMap(),
        timeoutSeconds: Long = DEFAULT_TIMEOUT_SECONDS
    ): Result<HttpResponse> {
        return request(
            RequestConfig(
                url = url,
                method = HttpMethod.POST,
                headers = headers,
                body = body,
                contentType = contentType,
                timeoutSeconds = timeoutSeconds
            )
        )
    }
}
