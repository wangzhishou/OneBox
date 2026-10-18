package com.shifenmiao.ai.error

import androidx.annotation.StringRes
import com.shifenmiao.base.utils.NetWorkUtils
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ModelProvider.AppJson
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * AI 聊天失败的语义分类。
 *
 * 只描述"用户该知道什么 / 该做什么"，不携带任何 provider 细节；
 * 具体文案统一走 [titleRes] / [descriptionRes]，保证 14 种语言的卡片结构一致。
 */
enum class AiChatErrorKind(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
) {
    OFFLINE(R.string.ai_chat_error_title_offline, R.string.ai_chat_error_desc_offline),
    NETWORK(R.string.ai_chat_error_title_network, R.string.ai_chat_error_desc_network),
    TIMEOUT(R.string.ai_chat_error_title_timeout, R.string.ai_chat_error_desc_timeout),
    AUTH(R.string.ai_chat_error_title_auth, R.string.ai_chat_error_desc_auth),
    POINTS(R.string.ai_chat_error_title_points, R.string.ai_chat_error_desc_points),
    PAYMENT(R.string.ai_chat_error_title_payment, R.string.ai_chat_error_desc_payment),
    FORBIDDEN(R.string.ai_chat_error_title_forbidden, R.string.ai_chat_error_desc_forbidden),
    RATE_LIMITED(R.string.ai_chat_error_title_rate_limited, R.string.ai_chat_error_desc_rate_limited),
    CONTENT_FILTERED(
        R.string.ai_chat_error_title_content_filtered,
        R.string.ai_chat_error_desc_content_filtered
    ),
    SERVER(R.string.ai_chat_error_title_server, R.string.ai_chat_error_desc_server),
    CLIENT(R.string.ai_chat_error_title_client, R.string.ai_chat_error_desc_client),
    EMPTY_RESPONSE(R.string.ai_chat_error_title_empty, R.string.ai_chat_error_desc_empty),
    INTERRUPTED(R.string.ai_chat_error_title_interrupted, R.string.ai_chat_error_desc_interrupted),
    UNKNOWN(R.string.ai_chat_error_title_unknown, R.string.ai_chat_error_desc_unknown),
    ;

    /** 这些类型值得在服务端文案之外再补一句"接下来怎么办"。 */
    val showsHint: Boolean
        get() = this in setOf(
            OFFLINE, NETWORK, TIMEOUT, AUTH, POINTS, PAYMENT,
            FORBIDDEN, RATE_LIMITED, SERVER, CLIENT, UNKNOWN
        )
}

/**
 * 一条可以直接渲染的聊天错误。
 *
 * [message] 优先保留后台返回的原文（这是用户最需要看到的信息），
 * [hint] 是本地的"下一步"提示；[technicalDetail] 只放在可折叠的详情里。
 */
data class AiChatError(
    val kind: AiChatErrorKind,
    val message: String,
    val hint: String = "",
    val statusCode: Int? = null,
    val code: String? = null,
    val technicalDetail: String = "",
) {
    /**
     * 落库到 `message.finish_reason` 的轻量元数据。
     * 正常回答的 finish_reason 是 stop/length 等，前置 `error:` 避免语义冲突。
     */
    val meta: String
        get() = "error:${statusCode ?: 0}:${code.orEmpty()}"
}

/**
 * 把 HTTP 响应体 / 异常 / 历史消息统一翻译成 [AiChatError]。
 *
 * 设计要点：
 * - 后台 `error.code` 是稳定契约，优先按 code 分类，其次才看状态码；
 * - 后台 `error.message` 直接展示，但过滤掉 "Payment Required" 这类纯 HTTP 短语；
 * - 断网 / DNS / 超时在客户端就地判定，不再把 `Unable to resolve host` 抛给用户；
 * - 历史消息没有元数据时回退解析旧格式（`HTTP 402 ... URL ... Error: ...`）。
 */
object AiChatErrorFactory {

    private const val MAX_BACKEND_MESSAGE = 300
    private const val MAX_DETAIL = 800
    private const val ERROR_CODE_CONTENT_FILTER = -100
    private const val CODE_NETWORK_OFFLINE = "NETWORK_OFFLINE"
    private const val CODE_NETWORK_TIMEOUT = "NETWORK_TIMEOUT"
    private const val CODE_NETWORK_ERROR = "NETWORK_ERROR"
    private const val CODE_CONTENT_FILTERED = "CONTENT_FILTERED"
    private const val CODE_INTERRUPTED = "INTERRUPTED"
    private const val CODE_EMPTY_RESPONSE = "EMPTY_RESPONSE"
    private const val CODE_STREAM_TIMEOUT = "STREAM_TIMEOUT"

    private val HTTP_STATUS_REGEX = Regex("""HTTP\s+(\d{3})""")
    private val CODE_TOKEN_REGEX = Regex("""\b([A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+)\b""")
    private val ERROR_LINE_REGEX = Regex("""(?im)^\s*(?:error|message|msg)\s*:\s*(.+)$""")
    private val JSON_MESSAGE_REGEX = Regex(""""message"\s*:\s*"((?:\\.|[^"\\])*)"""")
    private val GENERIC_HTTP_MESSAGES = setOf(
        "bad request", "unauthorized", "payment required", "forbidden", "not found",
        "method not allowed", "request timeout", "conflict", "gone", "payload too large",
        "too many requests", "internal server error", "bad gateway",
        "service unavailable", "gateway timeout"
    )

    fun fromHttpResponse(
        statusCode: Int,
        statusMessage: String,
        method: String,
        url: String,
        body: String,
        cacheOnlyRequest: Boolean = false,
    ): AiChatError {
        // 全局 CacheInterceptor 在无网时会把请求改成 FORCE_CACHE; OkHttp 找不到缓存时
        // 会合成一个 `HTTP 504 Unsatisfiable Request (only-if-cached)` 响应(没有响应体)。
        // 这不是服务端错误, 必须按"客户端断网"提示, 否则用户会看到莫名其妙的 504。
        if (isSyntheticOfflineResponse(statusCode, statusMessage, cacheOnlyRequest)) {
            val kind = if (!hasNetwork()) AiChatErrorKind.OFFLINE else AiChatErrorKind.NETWORK
            val code = if (kind == AiChatErrorKind.OFFLINE) CODE_NETWORK_OFFLINE else CODE_NETWORK_ERROR
            return AiChatError(
                kind = kind,
                message = string(kind.descriptionRes),
                code = code,
                // 合成的 504 是 OkHttp 缓存层的占位响应, 不作为服务端状态展示
                statusCode = null,
                technicalDetail = "code=$code\n$method ${url.substringBefore('?')}".trim(),
            )
        }
        val parsed = parseBackendError(body)
        val code = parsed.code?.trim()?.takeIf { it.isNotEmpty() }
        val backendMessage = parsed.message
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.takeIf { !isGenericHttpMessage(it, statusMessage) }
            ?.take(MAX_BACKEND_MESSAGE)
        val kind = resolveKind(statusCode, code, backendMessage)
        val description = string(kind.descriptionRes)
        val message = backendMessage ?: description
        val hint = if (backendMessage != null && kind.showsHint && backendMessage != description) {
            description
        } else {
            ""
        }
        val detail = buildString {
            append("HTTP ").append(statusCode)
            if (statusMessage.isNotBlank()) append(' ').append(statusMessage)
            append('\n').append(method).append(' ').append(url.substringBefore('?'))
            if (!code.isNullOrBlank()) append("\ncode=").append(code)
            val trimmedBody = body.trim()
            if (trimmedBody.isNotEmpty()) append("\n\n").append(trimmedBody.take(MAX_DETAIL))
        }
        return AiChatError(kind, message, hint, statusCode, code, detail)
    }

    fun fromThrowable(error: Throwable): AiChatError {
        val kind = when (error) {
            is UnknownHostException -> if (!hasNetwork()) AiChatErrorKind.OFFLINE else AiChatErrorKind.NETWORK
            is SocketTimeoutException -> AiChatErrorKind.TIMEOUT
            is ConnectException -> AiChatErrorKind.NETWORK
            is SSLException -> AiChatErrorKind.NETWORK
            is IOException -> AiChatErrorKind.NETWORK
            else -> AiChatErrorKind.UNKNOWN
        }
        val code = when (kind) {
            AiChatErrorKind.OFFLINE -> CODE_NETWORK_OFFLINE
            AiChatErrorKind.TIMEOUT -> CODE_NETWORK_TIMEOUT
            AiChatErrorKind.NETWORK -> CODE_NETWORK_ERROR
            else -> "UNKNOWN_ERROR"
        }
        val detail = buildString {
            append(error.javaClass.simpleName)
            error.message?.takeIf { it.isNotBlank() }?.let { append(": ").append(it.take(400)) }
        }
        return AiChatError(
            kind = kind,
            message = string(kind.descriptionRes),
            statusCode = null,
            code = code,
            technicalDetail = detail,
        )
    }

    fun fromStreamError(errorCode: Int, errorCodeName: String?, errorMessage: String): AiChatError {
        val rawMessage = errorMessage.trim()
        val code = errorCodeName?.trim()?.takeIf { it.isNotEmpty() }
        if (errorCode == ERROR_CODE_CONTENT_FILTER ||
            code == CODE_CONTENT_FILTERED ||
            looksLikeContentFilter(rawMessage)
        ) {
            return contentFiltered()
        }
        // 网络类错误码即使带着 504 也是缓存层占位, 不当作服务端状态
        val status = errorCode.takeIf {
            it in 100..599 && code?.startsWith("NETWORK_") != true
        }
        val kind = resolveKind(status, code, rawMessage)
        val description = string(kind.descriptionRes)
        val display = when {
            rawMessage.isEmpty() || rawMessage == "Unknown error" -> description
            isTechnicalText(rawMessage) -> description
            else -> rawMessage.take(MAX_BACKEND_MESSAGE)
        }
        val hint = if (kind.showsHint && display != description) description else ""
        val detail = buildString {
            status?.let { append("HTTP ").append(it) }
            if (!code.isNullOrBlank()) {
                if (isNotEmpty()) append("\n")
                append("code=").append(code)
            }
            if (rawMessage.isNotEmpty()) {
                if (isNotEmpty()) append("\n\n")
                append(rawMessage)
            }
        }
        return AiChatError(kind, display, hint, status, code, detail)
    }

    fun fromStoredMessage(message: String, meta: String?): AiChatError {
        val text = message.trim()
        val decoded = decodeMeta(meta)
        if (decoded != null) {
            val (status, code) = decoded
            val kind = resolveKind(status, code, text)
            val description = string(kind.descriptionRes)
            val display = text.ifBlank { description }
            val hint = if (kind.showsHint && display != description) description else ""
            val detail = buildString {
                status?.let { append("HTTP ").append(it) }
                if (!code.isNullOrBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(code)
                }
                if (display != description) {
                    if (isNotEmpty()) append('\n')
                    append(display)
                }
            }
            return AiChatError(kind, display, hint, status, code, detail)
        }
        return parseLegacyMessage(text)
    }

    fun timeout(message: String): AiChatError = AiChatError(
        kind = AiChatErrorKind.TIMEOUT,
        message = message,
        hint = string(AiChatErrorKind.TIMEOUT.descriptionRes),
        code = CODE_STREAM_TIMEOUT,
    )

    fun interrupted(message: String): AiChatError = AiChatError(
        kind = AiChatErrorKind.INTERRUPTED,
        message = message,
        code = CODE_INTERRUPTED,
    )

    fun emptyResponse(message: String): AiChatError = AiChatError(
        kind = AiChatErrorKind.EMPTY_RESPONSE,
        message = message,
        code = CODE_EMPTY_RESPONSE,
    )

    fun contentFiltered(): AiChatError {
        val description = string(AiChatErrorKind.CONTENT_FILTERED.descriptionRes)
        return AiChatError(
            kind = AiChatErrorKind.CONTENT_FILTERED,
            message = description,
            code = CODE_CONTENT_FILTERED,
        )
    }

    /**
     * 调用方已识别出联系方式验证门控时使用。
     */
    fun contactVerificationRequired(statusCode: Int, message: String): AiChatError = AiChatError(
        kind = AiChatErrorKind.FORBIDDEN,
        message = message,
        hint = string(AiChatErrorKind.FORBIDDEN.descriptionRes),
        statusCode = statusCode,
        code = "CONTACT_VERIFICATION_REQUIRED",
    )

    // ── 解析 ──

    private data class BackendError(
        val message: String?,
        val code: String?,
    )

    private fun parseBackendError(body: String): BackendError {
        val text = body.trim()
        if (text.isEmpty()) return BackendError(null, null)
        val json = runCatching {
            AppJson.parseToJsonElement(text) as? JsonObject
        }.getOrNull()
        if (json != null) {
            val nested = json["error"] as? JsonObject
            val message = nested?.str("message")
                ?: json.str("message")
                ?: json.str("msg")
            val code = nested?.str("code")
                ?: json.str("code")
            return BackendError(message, code)
        }
        if (text.startsWith("<") || text.length > MAX_BACKEND_MESSAGE) {
            return BackendError(null, null)
        }
        return BackendError(text, null)
    }

    private fun JsonObject.str(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    private fun parseLegacyMessage(text: String): AiChatError {
        if (text.isEmpty()) {
            return AiChatError(
                kind = AiChatErrorKind.UNKNOWN,
                message = string(AiChatErrorKind.UNKNOWN.descriptionRes),
                hint = string(AiChatErrorKind.UNKNOWN.descriptionRes),
                code = "UNKNOWN_ERROR",
            )
        }
        val status = HTTP_STATUS_REGEX.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val token = CODE_TOKEN_REGEX.find(text)?.groupValues?.getOrNull(1)
        val backendMessage = extractBackendMessage(text)
        val kind = resolveKind(status, token, backendMessage ?: text)
        val description = string(kind.descriptionRes)
        val display = when {
            backendMessage != null &&
                !isTechnicalText(backendMessage) &&
                !isGenericHttpMessage(backendMessage, "") ->
                backendMessage.take(MAX_BACKEND_MESSAGE)
            isTechnicalText(text) -> description
            else -> text.take(MAX_BACKEND_MESSAGE)
        }
        val hint = if (kind.showsHint && display != description) description else ""
        return AiChatError(kind, display, hint, status, token, text.take(MAX_DETAIL))
    }

    private fun extractBackendMessage(text: String): String? {
        ERROR_LINE_REGEX.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
            ?.let { return it }
        JSON_MESSAGE_REGEX.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
            ?.let { return it }
        return null
    }

    private fun decodeMeta(meta: String?): Pair<Int?, String?>? {
        val value = meta?.trim().orEmpty()
        if (!value.startsWith("error:")) return null
        val rest = value.removePrefix("error:")
        val separator = rest.indexOf(':')
        val statusPart = if (separator >= 0) rest.substring(0, separator) else rest
        val codePart = if (separator >= 0) rest.substring(separator + 1) else ""
        val status = statusPart.toIntOrNull()?.takeIf { it > 0 }
        val code = codePart.trim().takeIf { it.isNotEmpty() }
        return status to code
    }

    /**
     * 402 有两种语义：我方网关的"积分不足"(可充值)和上游服务商的"额度用尽"(改 billing)。
     * 网关的 code 是 POINTS_INSUFFICIENT；老数据/无 code 时按后台文案里是否出现"积分"兜底。
     */
    private fun resolveKind(status: Int?, code: String?, message: String?): AiChatErrorKind {
        val text = message.orEmpty()
        if (status == 402 && (code == "POINTS_INSUFFICIENT" || text.contains("积分"))) {
            return AiChatErrorKind.POINTS
        }
        return kindFor(status, code) ?: inferKindFromText(text)
    }

    private fun kindFor(status: Int?, code: String?): AiChatErrorKind? = when {
        code == CODE_NETWORK_OFFLINE -> AiChatErrorKind.OFFLINE
        code == CODE_NETWORK_TIMEOUT || code == CODE_STREAM_TIMEOUT || code == "UPSTREAM_TIMEOUT" ->
            AiChatErrorKind.TIMEOUT
        code == CODE_NETWORK_ERROR -> AiChatErrorKind.NETWORK
        code == CODE_INTERRUPTED -> AiChatErrorKind.INTERRUPTED
        code == CODE_EMPTY_RESPONSE -> AiChatErrorKind.EMPTY_RESPONSE
        code == CODE_CONTENT_FILTERED -> AiChatErrorKind.CONTENT_FILTERED
        code == "POINTS_INSUFFICIENT" -> AiChatErrorKind.POINTS
        code == "CONTACT_VERIFICATION_REQUIRED" -> AiChatErrorKind.FORBIDDEN
        code == "RATE_LIMITED" -> AiChatErrorKind.RATE_LIMITED
        code != null && code.startsWith("AUTH_") -> AiChatErrorKind.AUTH
        code == "AUTH_REQUIRED" -> AiChatErrorKind.AUTH
        code != null && code.startsWith("UPSTREAM_") -> AiChatErrorKind.SERVER
        code == "SERVICE_UNAVAILABLE" || code == "INTERNAL_ERROR" -> AiChatErrorKind.SERVER
        status == 401 -> AiChatErrorKind.AUTH
        status == 402 -> AiChatErrorKind.PAYMENT
        status == 403 -> AiChatErrorKind.FORBIDDEN
        status == 404 -> AiChatErrorKind.CLIENT
        status == 429 -> AiChatErrorKind.RATE_LIMITED
        status != null && status in 400..499 -> AiChatErrorKind.CLIENT
        status != null && status in 500..599 -> AiChatErrorKind.SERVER
        else -> null
    }

    private fun inferKindFromText(text: String): AiChatErrorKind {
        val lower = text.lowercase()
        return when {
            text.contains("积分不足") || lower.contains("points_insufficient") ||
                lower.contains("insufficient points") -> AiChatErrorKind.POINTS
            looksLikeContentFilter(text) -> AiChatErrorKind.CONTENT_FILTERED
            lower.contains("unable to resolve host") || lower.contains("no address associated") ||
                lower.contains("failed to connect") || lower.contains("network is unreachable") ||
                lower.contains("connection refused") || lower.contains("connectexception") ->
                AiChatErrorKind.NETWORK
            lower.contains("timeout") || lower.contains("timed out") ->
                AiChatErrorKind.TIMEOUT
            lower.contains("interrupted") || text.contains("连接中断") ->
                AiChatErrorKind.INTERRUPTED
            lower.contains("unauthorized") || lower.contains("session expired") ||
                text.contains("请重新登录") || text.contains("登录已过期") ->
                AiChatErrorKind.AUTH
            lower.contains("too many requests") || lower.contains("rate limit") ||
                text.contains("过于频繁") -> AiChatErrorKind.RATE_LIMITED
            lower.contains("no response") || lower.contains("returned nothing") ||
                text.contains("没有返回内容") -> AiChatErrorKind.EMPTY_RESPONSE
            else -> AiChatErrorKind.UNKNOWN
        }
    }

    private fun looksLikeContentFilter(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("content_filter") ||
            lower.contains("content filter") ||
            lower.contains("considered high risk") ||
            lower.contains("high risk")
    }

    private fun isTechnicalText(text: String): Boolean {
        val value = text.trim()
        if (value.isEmpty()) return true
        if (value.startsWith("HTTP ")) return true
        if (value.contains("\nURL:")) return true
        if (value.startsWith("IO error") || value.startsWith("Failed to")) return true
        if (value.contains("Unable to resolve host") || value.contains("Failed to connect")) return true
        if (value.contains("java.") && value.contains("Exception")) return true
        return false
    }

    private fun isGenericHttpMessage(message: String, statusMessage: String): Boolean {
        if (statusMessage.isNotBlank() && message.equals(statusMessage, ignoreCase = true)) return true
        return message.lowercase().trim() in GENERIC_HTTP_MESSAGES
    }

    private fun hasNetwork(): Boolean = runCatching {
        NetWorkUtils.isNetworkAvailable(AppContext.getContext())
    }.getOrDefault(false)

    /**
     * OkHttp 的 `FORCE_CACHE` + 缓存未命中会合成 504, 响应行特征稳定。
     */
    private fun isSyntheticOfflineResponse(
        statusCode: Int,
        statusMessage: String,
        cacheOnlyRequest: Boolean,
    ): Boolean {
        if (statusCode != 504) return false
        if (cacheOnlyRequest) return true
        val message = statusMessage.lowercase()
        return message.contains("only-if-cached") || message.contains("unsatisfiable request")
    }

    private fun string(@StringRes res: Int): String = AppContext.getString(res)
}
