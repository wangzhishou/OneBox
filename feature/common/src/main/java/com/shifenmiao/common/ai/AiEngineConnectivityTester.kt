package com.shifenmiao.common.ai

import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.AiRequestUrlResolver.normalizeBaseUrl
import com.shifenmiao.network.NetworkBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

/** 连通性测试失败的归因分类, UI 按类别给用户可懂的排查提示 */
enum class AiEngineTestFailureKind {
    /** 连不上服务器(检查 URL/网络) */
    CONNECT_FAILED,

    /** 服务器已连接但响应超时 */
    RESPONSE_TIMEOUT,

    /** 密钥无效(HTTP 401/403) */
    AUTH_INVALID,

    /** 模型或请求路径不存在(HTTP 404) */
    MODEL_NOT_FOUND,

    /** 限流或额度不足(HTTP 429) */
    RATE_LIMITED,

    /** 服务商服务器故障(HTTP 5xx) */
    SERVER_ERROR,

    /** 其它未归类失败 */
    UNKNOWN,
}

data class AiEngineTestResult(
    val success: Boolean,
    val failureKind: AiEngineTestFailureKind? = null,
    val errorMessage: String? = null,
    val httpLogs: String? = null,
)

/**
 * AI 引擎连通性测试(真实发一条 "Say this is a test!" 流式请求)。
 *
 * 从 AiEngineTestDialog 抽出的可复用实现, 设置页手动测试与保存后的静默复查共用。
 * 分阶段超时:
 * - 连接建立 10s(connectTimeout);
 * - 首个响应/鉴权 20s(readTimeout, 覆盖到响应头返回为止);
 * - 响应头拿到后(network interceptor)把流式读取放宽到每次 60s;
 *   HTTP/2 下 soTimeout 不生效, 退回 OkHttp 每次读取 20s, 对持续输出的流同样够用。
 *
 * 注意: 本类的请求构造(URL/鉴权头/body 形状)必须与聊天链路
 * AIPromptClient + AiRequestUrlResolver + AnthropicCompatibleService 保持一致,
 * 否则会出现"测试通过但聊天失败"(或反向)。已对齐的点: 各协议默认 path、
 * Anthropic 的 anthropic-version 头、URL join 走 AiRequestUrlResolver.joinUrl。
 * 已知差异(有意保留): 测试恒走直连/流式请求, 代理路由与非流式端点不在测试覆盖内。
 */
object AiEngineConnectivityTester {

    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val FIRST_RESPONSE_TIMEOUT_SECONDS = 20L
    private const val STREAM_READ_TIMEOUT_SECONDS = 60L

    // 共享 base client: 每次测试用 newBuilder() 派生, 复用连接池与调度线程, 不每次新建
    private val baseClient = OkHttpClient()

    suspend fun test(aiEngine: AiEngine): AiEngineTestResult = withContext(Dispatchers.IO) {
        executeTest(aiEngine)
    }

    private fun executeTest(aiEngine: AiEngine): AiEngineTestResult {
        val logBuffer = StringBuilder()
        return try {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                logBuffer.append(message).append("\n")
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
                // 日志会原样展示给用户, 鉴权头一律脱敏, 防截图求助时泄露
                redactHeader("Authorization")
                redactHeader("x-api-key")
                redactHeader("api-key")
            }

            val client = baseClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(FIRST_RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(FIRST_RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addNetworkInterceptor { chain ->
                    val response = chain.proceed(chain.request())
                    // 响应头已返回(连接/鉴权阶段通过), 后续流式读取放宽到完整流式预算
                    chain.connection()?.socket()?.soTimeout =
                        (STREAM_READ_TIMEOUT_SECONDS * 1000).toInt()
                    response
                }
                .build()

            // use 保证 response/连接归还连接池; SSE 分支在读完流特征后才出块
            client.newCall(buildRequest(aiEngine)).execute().use { response ->
                if (response.isSuccessful) {
                    // Jev 的 systemone 非流式接口, HTTP 200 即通过; 其余协议仍需流式响应特征
                    val isJev = aiEngine.requestProtocol == AiRequestProtocol.JEV
                    if (isJev || response.looksLikeStreamingResponse()) {
                        AiEngineTestResult(success = true, httpLogs = logBuffer.toString())
                    } else {
                        AiEngineTestResult(
                            success = false,
                            failureKind = AiEngineTestFailureKind.UNKNOWN,
                            errorMessage = buildString {
                                append("Response received but does not appear to be a stream response")
                                response.body.contentType().toString().takeIf { it.isNotBlank() }
                                    ?.let {
                                        append(" (content-type=")
                                        append(it)
                                        append(')')
                                    }
                            },
                            httpLogs = logBuffer.toString()
                        )
                    }
                } else {
                    AiEngineTestResult(
                        success = false,
                        failureKind = failureKindForHttpCode(response.code),
                        errorMessage = "HTTP ${response.code}: ${response.message}",
                        httpLogs = logBuffer.toString()
                    )
                }
            }
        } catch (e: Exception) {
            AiEngineTestResult(
                success = false,
                failureKind = failureKindForException(e),
                errorMessage = e.message ?: "Unknown error",
                httpLogs = logBuffer.toString().takeIf { it.isNotBlank() }
            )
        }
    }

    private fun failureKindForHttpCode(code: Int): AiEngineTestFailureKind {
        return when (code) {
            401, 403 -> AiEngineTestFailureKind.AUTH_INVALID
            404 -> AiEngineTestFailureKind.MODEL_NOT_FOUND
            429 -> AiEngineTestFailureKind.RATE_LIMITED
            in 500..599 -> AiEngineTestFailureKind.SERVER_ERROR
            else -> AiEngineTestFailureKind.UNKNOWN
        }
    }

    private fun failureKindForException(e: Exception): AiEngineTestFailureKind {
        return when (e) {
            // OkHttp 连接阶段超时的报文以 "failed to connect" 开头, 据此区分连接/读取阶段
            is SocketTimeoutException ->
                if (e.message?.contains("failed to connect") == true) {
                    AiEngineTestFailureKind.CONNECT_FAILED
                } else {
                    AiEngineTestFailureKind.RESPONSE_TIMEOUT
                }

            is UnknownHostException, is ConnectException, is NoRouteToHostException, is SSLException ->
                AiEngineTestFailureKind.CONNECT_FAILED

            is IOException -> AiEngineTestFailureKind.CONNECT_FAILED
            else -> AiEngineTestFailureKind.UNKNOWN
        }
    }

    private fun buildRequest(aiEngine: AiEngine): Request {
        val isJev = aiEngine.requestProtocol == AiRequestProtocol.JEV
        val isAnthropic = aiEngine.requestProtocol == AiRequestProtocol.ANTHROPIC_COMPATIBLE

        // 与 AiRequestUrlResolver 保持同一套默认路径, 避免"聊天能通、测试 404"
        val requestPath = when {
            isJev -> StringUtils.normalizeApiPath(aiEngine.requestPath)
                .ifBlank { UrlConstants.JEV_SYSTEMONE_ENDPOINT }
            isAnthropic -> StringUtils.normalizeApiPath(aiEngine.requestPath)
                .ifBlank { "/v1/messages" }
            aiEngine.requestProtocol == AiRequestProtocol.RESPONSES_COMPATIBLE ->
                StringUtils.normalizeApiPath(aiEngine.requestPath)
                    .ifBlank { "/v1/responses" }
            else -> StringUtils.normalizeApiPath(aiEngine.requestPath)
                .ifBlank { "/v1/chat/completions" }
        }

        val url = AiRequestUrlResolver.joinUrl(
            baseUrl = normalizeBaseUrl(
                baseUrl = StringUtils.normalizeApiBaseUrl(aiEngine.requestUrl),
                fallbackBaseUrl = NetworkBuilder.ensureValidBaseUrl(aiEngine)
            ),
            path = requestPath
        )

        val jsonBody = if (isJev) {
            JSONObject().apply {
                put("state", "ping")
                put("model", aiEngine.model.name.takeIf { it.startsWith("jev") } ?: "jev-latest")
                put("questions", JSONObject().apply {
                    put("ping", JSONObject().apply {
                        put("type", "noul")
                        put("instructions", "Is this a connectivity test?")
                    })
                })
            }
        } else if (aiEngine.requestProtocol == AiRequestProtocol.RESPONSES_COMPATIBLE) {
            JSONObject().apply {
                put("model", aiEngine.model.name)
                put("input", JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "message")
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "input_text")
                                put("text", "Say this is a test!")
                            })
                        })
                    })
                })
                put("stream", true)
            }
        } else {
            JSONObject().apply {
                put("model", aiEngine.model.name)
                // Anthropic Messages 必填 max_tokens; OpenAI 兼容端点多会忽略多余字段
                if (isAnthropic) {
                    put("max_tokens", aiEngine.model.maxTokens.coerceIn(1, 8192))
                }
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Say this is a test!")
                    })
                })
                put("stream", true)
            }
        }

        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")

        if (aiEngine.authorizationCode.isNotBlank()) {
            when {
                aiEngine.requestProtocol == AiRequestProtocol.ANTHROPIC_COMPATIBLE -> {
                    requestBuilder.addHeader("x-api-key", aiEngine.authorizationCode)
                    // 与 AnthropicCompatibleService 对齐, 缺这个头真实端点会 400/401
                    requestBuilder.addHeader("anthropic-version", "2023-06-01")
                }
                aiEngine.authType == AuthType.API_KEY -> {
                    requestBuilder.addHeader("api-key", aiEngine.authorizationCode)
                }
                aiEngine.requestProtocol != AiRequestProtocol.OWN_PROXY -> {
                    requestBuilder.addHeader("Authorization", "Bearer ${aiEngine.authorizationCode}")
                }
            }
        }

        return requestBuilder
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun Response.looksLikeStreamingResponse(): Boolean {
        val responseBody = body
        val contentType = responseBody.contentType().toString()
        if (contentType.contains("text/event-stream", ignoreCase = true)) {
            return true
        }

        if (header("x-dashscope-finished")?.equals("false", ignoreCase = true) == true) {
            return true
        }

        val bodyString = runCatching { responseBody.string() }.getOrDefault("")

        return bodyString.contains("data: ") ||
            bodyString.contains("\"delta\"") ||
            bodyString.contains("\"chunk\"") ||
            bodyString.contains("\"response.output_text.delta\"")
    }
}
