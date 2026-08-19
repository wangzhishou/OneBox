package com.wanbaohe.dsh.connection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.net.URLDecoder
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** 手机亮码字符集(与网关一致:去 I/L/O 与 0/1) */
private const val CodeAlphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

/** 秘密字符集(43 位字母数字,永不显示) */
private const val SecretAlphabet = "abcdefghijklmnopqrstuvwxyz0123456789"

/** 生成 10 位亮码(与网关字符集一致) */
fun generatePairCode(random: SecureRandom = SecureRandom()): String =
    (1..10).map { CodeAlphabet[random.nextInt(CodeAlphabet.length)] }.joinToString("")

/** 生成 43 位字母数字秘密(永不显示) */
fun generatePairSecret(random: SecureRandom = SecureRandom()): String =
    (1..43).map { SecretAlphabet[random.nextInt(SecretAlphabet.length)] }.joinToString("")

/** 一次配对会话(手机侧材料)。secret 永不显示;code 大字展示给用户 */
data class PairingSession(
    val baseUri: String,
    val pairingId: String,
    val code: String,
    val secret: String,
    val expiresAtEpochSeconds: Long
) {
    /** 展示形态 XXXXX-XXXXX */
    val displayCode: String get() = "${code.substring(0, 5)}-${code.substring(5)}"
}

/** 一条来自某台 Mac 的应约 offer(主机码 = 人工比对凭证) */
data class PairOffer(
    val claimId: String,
    val hostCode: String,
    val hostLabel: String,
    val upstreamPort: Int,
    val expiresAtEpochSeconds: Long
) {
    val displayHostCode: String
        get() = if (hostCode.length == 6) {
            "${hostCode.substring(0, 3)}-${hostCode.substring(3)}"
        } else {
            hostCode
        }
}

enum class PairPollStatus { Waiting, Offers, Confirmed, Expired }

data class PairPollResult(val status: PairPollStatus, val offers: List<PairOffer>)

/** 登录/配对结果(与密码登录令牌同构;绑定隧道端口的 30 天设备令牌) */
data class RemoteLoginSuccess(
    val baseUri: String,
    val token: String,
    /** 来源机器名(配对确认响应回显;密码登录为空)。展示用,非凭证 */
    val hostLabel: String = "",
    /** 来源宿主稳定标识(rust = 隧道端口;CF = 隧道主机名;旧网关/密码登录为空) */
    val hostRef: String = ""
)

/** UI 可本地化的错误类别;[PairingError.message] 为空时按类别取文案 */
enum class PairingErrorKind {
    Network, BadAddress, InvalidInvite, Expired, CodeUsed,
    WrongPassword, RateLimited, PollFailing, MissingToken
}

/** 配对/登录错误:UI 优先按 [kind] 本地化,否则原样展示 [message](网关错误串) */
data class PairingError(val kind: PairingErrorKind? = null, val message: String? = null)

/** 配对失败(网络/协议/被拒);[kind] 供 UI 本地化,[message] 面向用户 */
class PairingFailure(
    message: String,
    val kind: PairingErrorKind? = null
) : Exception(message)

/**
 * 令牌供给(对齐 Flutter MutableTokenProvider):ApiClient/控制器每次请求时读取,
 * 登录成功后原地刷新,无需重建任何 store。
 */
class MutableTokenProvider(initial: String? = null) {
    @Volatile
    var token: String? = initial?.takeIf { it.isNotEmpty() }

    val hasToken: Boolean get() = token != null

    /** WS/HTTP 共用的鉴权头;无令牌返回空 map(不添头) */
    fun authHeaders(): Map<String, String> =
        token?.let { mapOf("Authorization" to "Bearer $it") }.orEmpty()
}

/**
 * 扫码邀请(ADR-0008):QR 内容为网关 /pair 落地页 URL(fragment 携带码),
 * 或裸 10 位码(手抄兜底,需 [parsePairInvite] 提供 fallbackBase)。
 */
data class PairInvite(
    /** 网关地址(落地页 URL 去掉路径与 fragment) */
    val baseUri: String,
    /** 10 位配对码(已归一化大写) */
    val code: String,
    /** 锚定主机码(6 位;二维码携带,offers 里自动高亮匹配项) */
    val hostCode: String?,
    /** 来源机器名(仅展示) */
    val label: String = ""
) {
    val displayCode: String get() = "${code.substring(0, 5)}-${code.substring(5)}"
}

private fun normalizeCode(raw: String): String =
    raw.uppercase().replace(Regex("[^A-Z0-9]"), "")

private fun isValidInviteCode(code: String): Boolean =
    code.length == 10 && code.all { it in CodeAlphabet }

/**
 * 解析剪贴板/扫码的邀请内容:
 * - `https://host/pair#c=XXXXX&h=YYYYYY&l=label`(落地页「复制」产物/QR 内容)
 * - `XXXXX-XXXXX` / `XXXXXXXXXX`(裸码;此时 baseUri 用 [fallbackBase])
 *
 * 无法解析返回 null(UI 提示格式不对)。
 */
fun parsePairInvite(input: String, fallbackBase: String? = null): PairInvite? {
    val text = input.trim()
    if (text.isEmpty()) return null
    if (text.contains("://")) {
        val uri = runCatching { URI(text) }.getOrNull() ?: return null
        if (uri.scheme.isNullOrEmpty() || uri.host.isNullOrEmpty()) return null
        val frag = uri.rawFragment.orEmpty()
        val query = frag.split('&')
            .mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0) null else {
                    part.substring(0, idx) to runCatching {
                        URLDecoder.decode(part.substring(idx + 1), Charsets.UTF_8)
                    }.getOrDefault("")
                }
            }
            .toMap()
        var code = normalizeCode(query["c"] ?: query["code"] ?: "")
        val host = normalizeCode(query["h"] ?: query["host"] ?: "")
        // 兼容:fragment 本身就是裸码(无键值对)
        if (code.isEmpty() && frag.isNotEmpty() && !frag.contains('=')) {
            code = normalizeCode(frag)
        }
        if (!isValidInviteCode(code)) return null
        // 网关地址 = scheme://host[:port](组件重建,path/query/fragment 全弃)
        val base = buildString {
            append(uri.scheme.lowercase()).append("://").append(uri.host.lowercase())
            if (uri.port > 0) append(':').append(uri.port)
        }
        // 清洗但保留中文机器名(只剥会破坏展示的字符)
        val safeLabel = (query["l"] ?: "")
            .replace(Regex("[^\\p{L}\\p{N} _.\\-]"), "")
            .trim()
            .take(32)
        return PairInvite(
            baseUri = base,
            code = code,
            hostCode = if (host.length >= 6) host.substring(0, 6) else null,
            label = safeLabel
        )
    }
    // 裸码:必须带兜底地址
    val code = normalizeCode(text)
    if (!isValidInviteCode(code)) return null
    val base = fallbackBase ?: return null
    return PairInvite(baseUri = base, code = code, hostCode = null)
}

/** 配对页阶段(对齐 Flutter pairing_page.dart _Stage) */
enum class PairingStage { Url, Waiting, Offers, Confirming, Done }

/** 配对页 UI 状态(状态机输出) */
data class PairingUiState(
    val stage: PairingStage = PairingStage.Url,
    val gatewayAddress: String = "",
    val busy: Boolean = false,
    val error: PairingError? = null,
    val session: PairingSession? = null,
    val offers: List<PairOffer> = emptyList(),
    /** 扫码邀请锚定的主机码(offers 里匹配项高亮;非匹配项需长按) */
    val anchoredHostCode: String? = null,
    val inviteLabel: String = ""
)

/**
 * 配对状态机(P6,对齐 Flutter pairing.dart + pairing_page.dart)。
 *
 * 协议(网关公开面,无鉴权;字段名与 pairing.dart 一致):
 * - POST /pair/start   {code, secret, device}            → {pairing_id, expires_at}
 *   409(码被存活 pending 占用,error 含 "already in use")自动换码,最多 3 次;
 *   扫码邀请码不换码(换了就与 Mac 侧锚定的码对不上)
 * - POST /pair/poll    {pairing_id, secret}              → {status, offers[]}
 *   2s 轮询;单次失败静默重试,持续失败(>3 次)才报错;expired → 回首页;
 *   confirmed(状态残留,令牌只经 confirm 发放)→ 提示已被使用
 * - POST /pair/confirm {pairing_id, secret, claim_id, host_code} → {token, host_label?, host_ref?}
 *
 * 密码登录兜底:POST /auth/login {password, device} → {token, expires_at};
 * 401 = 密码不正确,409 = 限速。密码永不落盘。
 *
 * 生命周期与组件绑定:由组件层创建并 [dispose];成功经 [onSuccess] 回调交出令牌。
 */
class PairingManager(
    okHttpClient: OkHttpClient,
    private val scope: CoroutineScope,
    /** 上报给网关的 device 字段(设备名,调用时读取) */
    private val deviceNameProvider: () -> String,
    /** 配对/登录成功(组件层接管教条:adopt 凭证 → 连接/原地刷新) */
    private val onSuccess: suspend (RemoteLoginSuccess) -> Unit
) {

    /** 配对/登录专用 client:15s 整Call超时(对齐 Flutter) */
    private val client: OkHttpClient = okHttpClient.newBuilder()
        .callTimeout(CallTimeoutSeconds, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow(PairingUiState())
    val state = _state.asStateFlow()

    private var pollJob: Job? = null
    private var pollCount = 0

    fun onGatewayAddressChange(value: String) {
        _state.value = _state.value.copy(gatewayAddress = value)
    }

    /** 归一化网关地址(去尾斜杠);非法返回 null(UI 提示格式) */
    private fun normalizedBase(): String? {
        val text = _state.value.gatewayAddress.trim().trimEnd('/')
        if (text.isEmpty()) return null
        val uri = runCatching { URI(text) }.getOrNull() ?: return null
        if (uri.scheme.isNullOrEmpty() || uri.host.isNullOrEmpty()) return null
        return text
    }

    /** 手动配对:以地址栏网关发起(自生成码;裸 10 位码粘贴到地址栏不适用 —— 走 [applyInvite]) */
    fun start(code: String? = null, anchor: String? = null, label: String = "") {
        val base = normalizedBase()
        if (base == null) {
            _state.value = _state.value.copy(error = PairingError(PairingErrorKind.BadAddress))
            return
        }
        if (_state.value.busy) return
        _state.value = _state.value.copy(
            busy = true,
            error = null,
            anchoredHostCode = anchor
        )
        scope.launch {
            try {
                val session = pairStart(base, code = code)
                pollCount = 0
                _state.value = _state.value.copy(
                    busy = false,
                    stage = PairingStage.Waiting,
                    session = session,
                    offers = emptyList(),
                    inviteLabel = label.ifEmpty { _state.value.inviteLabel }
                )
                startPolling()
            } catch (e: PairingFailure) {
                _state.value = _state.value.copy(
                    busy = false,
                    error = PairingError(e.kind, e.message)
                )
            } catch (e: Throwable) {
                _state.value = _state.value.copy(
                    busy = false,
                    error = PairingError(PairingErrorKind.Network, e.message)
                )
            }
        }
    }

    /**
     * 应用邀请文本(扫码结果或剪贴板):合法邀请直接以邀请码发起(不换码,
     * 主机码锚定);地址栏被邀请自带地址覆盖(防「手机等 A 网关、Mac 去 B 网关」)。
     */
    fun applyInvite(text: String) {
        val fallback = normalizedBase()
        val invite = parsePairInvite(text, fallbackBase = fallback)
        if (invite == null) {
            _state.value = _state.value.copy(error = PairingError(PairingErrorKind.InvalidInvite))
            return
        }
        _state.value = _state.value.copy(gatewayAddress = invite.baseUri)
        start(code = invite.code, anchor = invite.hostCode, label = invite.label)
    }

    /** 取消/返回地址页:停轮询、清会话材料(secret 随 session 一并丢弃) */
    fun cancel() {
        stopPolling()
        _state.value = PairingUiState(gatewayAddress = _state.value.gatewayAddress)
    }

    /** 密码登录兜底(密码只在本函数调用期内存在,永不落盘) */
    fun loginWithPassword(password: String) {
        val base = normalizedBase()
        if (base == null) {
            _state.value = _state.value.copy(error = PairingError(PairingErrorKind.BadAddress))
            return
        }
        if (_state.value.busy) return
        _state.value = _state.value.copy(busy = true, error = null)
        scope.launch {
            try {
                val success = passwordLogin(base, password)
                _state.value = _state.value.copy(busy = false, stage = PairingStage.Done)
                onSuccess(success)
            } catch (e: PairingFailure) {
                _state.value = _state.value.copy(
                    busy = false,
                    error = PairingError(e.kind, e.message)
                )
            } catch (e: Throwable) {
                _state.value = _state.value.copy(
                    busy = false,
                    error = PairingError(PairingErrorKind.Network, e.message)
                )
            }
        }
    }

    /** 用户人工比对主机码后点选 offer → confirm 换令牌 */
    fun confirm(offer: PairOffer) {
        val session = _state.value.session ?: return
        stopPolling()
        _state.value = _state.value.copy(stage = PairingStage.Confirming, error = null)
        scope.launch {
            try {
                val success = pairConfirm(session, offer)
                _state.value = _state.value.copy(stage = PairingStage.Done)
                onSuccess(success)
            } catch (e: PairingFailure) {
                // 确认失败回 offers 列表(可换一条再试),轮询恢复
                _state.value = _state.value.copy(
                    stage = PairingStage.Offers,
                    error = PairingError(e.kind, e.message)
                )
                startPolling()
            } catch (e: Throwable) {
                _state.value = _state.value.copy(
                    stage = PairingStage.Offers,
                    error = PairingError(PairingErrorKind.Network, e.message)
                )
                startPolling()
            }
        }
    }

    /** 释放:停轮询(组件销毁/离开配对页时调;幂等) */
    fun dispose() = stopPolling()

    // ───────────────────────────── 轮询 ─────────────────────────────

    private fun startPolling() {
        stopPolling()
        pollJob = scope.launch {
            while (isActive) {
                pollOnce()
                delay(PollIntervalMillis)
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private suspend fun pollOnce() {
        val session = _state.value.session ?: return
        val stage = _state.value.stage
        if (stage == PairingStage.Confirming || stage == PairingStage.Done) return
        try {
            val result = pairPoll(session)
            pollCount += 1
            when (result.status) {
                PairPollStatus.Waiting ->
                    if (_state.value.stage == PairingStage.Offers) {
                        _state.value = _state.value.copy(stage = PairingStage.Waiting)
                    }

                PairPollStatus.Offers ->
                    _state.value = _state.value.copy(
                        stage = PairingStage.Offers,
                        offers = result.offers
                    )

                PairPollStatus.Confirmed -> {
                    // 令牌只经 confirm 发放;这里的 confirmed 是状态残留,提示重试
                    stopPolling()
                    _state.value = _state.value.copy(error = PairingError(PairingErrorKind.CodeUsed))
                }

                PairPollStatus.Expired -> {
                    stopPolling()
                    _state.value = _state.value.copy(
                        stage = PairingStage.Url,
                        session = null,
                        anchoredHostCode = null,
                        error = PairingError(PairingErrorKind.Expired)
                    )
                }
            }
        } catch (e: Throwable) {
            // 单次轮询失败不打断流程(网络抖动);失败也计数 —— 只计成功的话
            // 网关不可达时永远到不了阈值,手机静默停在「等待电脑应约」
            pollCount += 1
            if (pollCount > PollFailureThreshold && _state.value.error == null) {
                _state.value = _state.value.copy(
                    error = PairingError(PairingErrorKind.PollFailing, e.message)
                )
            }
        }
    }

    // ───────────────────────────── 协议端点 ─────────────────────────────

    /** 非 200 折叠:优先取响应体 error 字段(409 "already in use" 判定依赖原文) */
    private suspend fun post(baseUri: String, path: String, body: JsonObject): JsonObject {
        val request = Request.Builder()
            .url("$baseUri$path")
            .post(body.toString().toRequestBody(JsonMediaType))
            .build()
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val call = client.newCall(request)
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    if (cont.isActive) {
                        cont.resumeWithException(
                            PairingFailure("connect failed: ${e.message}", PairingErrorKind.Network)
                        )
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val outcome = runCatching {
                        response.use {
                            val text = it.body.string()
                            val element = runCatching { com.wanbaohe.dsh.wire.DshJson.parseToJsonElement(text) }
                                .getOrNull()
                            if (it.code != 200) {
                                val serverError = (element as? JsonObject)
                                    ?.get("error") as? JsonPrimitive
                                throw PairingFailure(serverError?.contentOrNull ?: "HTTP ${it.code}")
                            }
                            element as? JsonObject
                                ?: throw PairingFailure("gateway response is not a JSON object")
                        }
                    }
                    if (cont.isActive) {
                        outcome.fold(
                            onSuccess = { cont.resume(it) },
                            onFailure = cont::resumeWithException
                        )
                    }
                }
            })
        }
    }

    /** POST /pair/start;409(码被占)自动换码重试,最多 3 次(邀请码不换) */
    private suspend fun pairStart(baseUri: String, code: String? = null): PairingSession {
        var lastError: PairingFailure? = null
        // 外部邀请码归一化(去分隔符大写,与网关规则一致);自生成码本就纯净
        val normalized = code?.uppercase()?.replace(Regex("[^A-Z0-9]"), "")
        repeat(CodeConflictMaxAttempts) { attempt ->
            val effective = normalized ?: generatePairCode()
            val secret = generatePairSecret()
            try {
                val resp = post(
                    baseUri,
                    "/pair/start",
                    buildJsonObject {
                        put("code", effective)
                        put("secret", secret)
                        put("device", deviceNameProvider())
                    }
                )
                return PairingSession(
                    baseUri = baseUri,
                    pairingId = (resp["pairing_id"] as? JsonPrimitive)?.contentOrNull
                        ?: throw PairingFailure("gateway response missing pairing_id"),
                    code = effective,
                    secret = secret,
                    expiresAtEpochSeconds = (resp["expires_at"] as? JsonPrimitive)?.longOrNull ?: 0
                )
            } catch (e: PairingFailure) {
                if (e.message?.contains("already in use") == true &&
                    normalized == null &&
                    attempt < CodeConflictMaxAttempts - 1
                ) {
                    lastError = e
                    return@repeat
                }
                throw e
            }
        }
        throw lastError ?: PairingFailure("pair code conflict, please retry")
    }

    /** POST /pair/poll → 状态 + offers 列表 */
    private suspend fun pairPoll(session: PairingSession): PairPollResult {
        val resp = post(
            session.baseUri,
            "/pair/poll",
            buildJsonObject {
                put("pairing_id", session.pairingId)
                put("secret", session.secret)
            }
        )
        val status = when ((resp["status"] as? JsonPrimitive)?.contentOrNull) {
            "offers" -> PairPollStatus.Offers
            "confirmed" -> PairPollStatus.Confirmed
            "expired" -> PairPollStatus.Expired
            else -> PairPollStatus.Waiting
        }
        val offers = (resp["offers"] as? kotlinx.serialization.json.JsonArray)
            ?.mapNotNull { (it as? JsonObject)?.let(::parseOffer) }
            .orEmpty()
        return PairPollResult(status, offers)
    }

    private fun parseOffer(json: JsonObject): PairOffer? {
        val claimId = (json["claim_id"] as? JsonPrimitive)?.contentOrNull ?: return null
        val hostCode = (json["host_code"] as? JsonPrimitive)?.contentOrNull ?: return null
        return PairOffer(
            claimId = claimId,
            hostCode = hostCode,
            hostLabel = (json["host_label"] as? JsonPrimitive)?.contentOrNull.orEmpty(),
            upstreamPort = (json["upstream_port"] as? JsonPrimitive)?.intOrNull ?: 0,
            expiresAtEpochSeconds = (json["expires_at"] as? JsonPrimitive)?.longOrNull ?: 0
        )
    }

    /** POST /pair/confirm → 30 天设备令牌(绑定该 claim 的隧道端口) */
    private suspend fun pairConfirm(session: PairingSession, offer: PairOffer): RemoteLoginSuccess {
        val resp = post(
            session.baseUri,
            "/pair/confirm",
            buildJsonObject {
                put("pairing_id", session.pairingId)
                put("secret", session.secret)
                put("claim_id", offer.claimId)
                put("host_code", offer.hostCode)
            }
        )
        val token = (resp["token"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotEmpty() }
            ?: throw PairingFailure("gateway response missing token", PairingErrorKind.MissingToken)
        return RemoteLoginSuccess(
            baseUri = session.baseUri,
            token = token,
            hostLabel = (resp["host_label"] as? JsonPrimitive)?.contentOrNull.orEmpty(),
            hostRef = (resp["host_ref"] as? JsonPrimitive)?.contentOrNull.orEmpty()
        )
    }

    /**
     * POST /auth/login {password, device} → {token, expires_at}(密码登录兜底)。
     * 401 = 密码不正确;409 = 限速;密码不落盘。
     */
    private suspend fun passwordLogin(baseUri: String, password: String): RemoteLoginSuccess {
        val body = buildJsonObject {
            put("password", password)
            put("device", deviceNameProvider())
        }
        val request = Request.Builder()
            .url("$baseUri/auth/login")
            .post(body.toString().toRequestBody(JsonMediaType))
            .build()
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val call = client.newCall(request)
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    if (cont.isActive) {
                        cont.resumeWithException(
                            PairingFailure("connect failed: ${e.message}", PairingErrorKind.Network)
                        )
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val outcome = runCatching {
                        response.use {
                            when (it.code) {
                                401 -> throw PairingFailure("wrong password", PairingErrorKind.WrongPassword)
                                409 -> throw PairingFailure("rate limited", PairingErrorKind.RateLimited)
                            }
                            if (it.code != 200) throw PairingFailure("HTTP ${it.code}")
                            val element = com.wanbaohe.dsh.wire.DshJson.parseToJsonElement(it.body.string())
                            val token = ((element as? JsonObject)?.get("token") as? JsonPrimitive)
                                ?.contentOrNull
                                ?: throw PairingFailure("gateway response missing token", PairingErrorKind.MissingToken)
                            RemoteLoginSuccess(baseUri = baseUri, token = token)
                        }
                    }
                    if (cont.isActive) {
                        outcome.fold(
                            onSuccess = { cont.resume(it) },
                            onFailure = cont::resumeWithException
                        )
                    }
                }
            })
        }
    }

    private companion object {
        const val CallTimeoutSeconds = 15L
        const val PollIntervalMillis = 2_000L
        const val PollFailureThreshold = 3
        const val CodeConflictMaxAttempts = 3
        val JsonMediaType = "application/json".toMediaType()
    }
}
