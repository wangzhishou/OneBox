package com.wanbaohe.dsh.connection

import com.shifenmiao.network.NetworkBuilder
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.TokenStorage
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.DshJson
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.URI
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 云端中继(P7):不经 LAN、不配对第三方网关,直接用 App 自己的后端
 *(api.wanbaohe.com,nginx → Go 网关)+ App 登录态 JWT 连接自己 Mac 上的 DSH。
 *
 * 契约(strapi_go dshrelay 模块):
 * - POST {backend}/dsh/bind-codes(Authorization: Bearer <App JWT>)
 *   → `{code:"123456", expiresIn:600}`,6 位一次性绑定码,
 *   用户把它输进 Mac 上的 dsh-connector CLI 完成绑定
 * - 中继端点:DSH 的 baseUri 设为 {backend}/dsh/relay 后,现有协议栈原样工作
 *   (POST {backend}/dsh/relay/api/<method>、WS …/api/events.mux|events.host,
 *   均由服务端隧道转到用户 Mac 的 dsh;agent 不在线 → 503)
 *
 * App JWT 不落盘进主机簿:每次请求时经 [currentAppToken] 现取
 * (登录态 token 优先,缺失时兜底游客 token)。
 */
@Singleton
class CloudRelayApi @Inject constructor(
    @Named("DshOkHttpClient") okHttpClient: OkHttpClient
) {

    /** 绑定码申请专用 client:15s 整 Call 超时(与配对链路一致) */
    private val client: OkHttpClient = okHttpClient.newBuilder()
        .callTimeout(CallTimeoutSeconds, TimeUnit.SECONDS)
        .build()

    /**
     * 申请一次性绑定码:POST {backend}/dsh/bind-codes(空 JSON 体,Bearer 鉴权)。
     * 非 200 折叠为 [CarrierException](httpStatus 保留,401 = App 登录过期)。
     */
    suspend fun requestBindCode(token: String): BindCodeResponse =
        suspendCancellableCoroutine { cont ->
            val request = Request.Builder()
                .url("${backendBase()}$BindCodesPath")
                .header("Authorization", "Bearer $token")
                .post(EmptyJsonBody)
                .build()
            val call = client.newCall(request)
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (cont.isActive) {
                        cont.resumeWithException(
                            CarrierException("connect failed: ${e.message}", cause = e)
                        )
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val outcome = runCatching {
                        response.use {
                            if (it.code != 200) {
                                throw CarrierException("http ${it.code}", httpStatus = it.code)
                            }
                            DshJson.decodeFromString(
                                BindCodeResponse.serializer(),
                                it.body.string()
                            )
                        }
                    }
                    if (cont.isActive) {
                        outcome.fold(
                            onSuccess = cont::resume,
                            onFailure = cont::resumeWithException
                        )
                    }
                }
            })
        }

/** 绑定码申请响应(code 为 6 位数字串,expiresIn 单位秒) */
    @Serializable
    data class BindCodeResponse(
        val code: String,
        val expiresIn: Int
    )

    /**
     * 扫码配对邀请(P8,onebox-dsh-bridge 插件页面二维码内容):
     * `oneboxdsh://pair?v=1&g=<gatewayBase>&p=<pairingId>&s=<secret>[&k=<e2eKey>]`。
     *
     * @property gatewayBase 网关(App 后端)根地址,已去尾斜杠;必须是 https(防御降级 http 注入)
     * @property pairingId 配对会话 id(claim 路径段)
     * @property secret 配对秘密(≥32 字符;claim body,永不显示)
     * @property key E2E 密钥 base64url(无填充,解码 32 字节);null = 旧插件明文形态
     */
    data class CloudPairInvite(
        val gatewayBase: String,
        val pairingId: String,
        val secret: String,
        val key: String?
    )

    /**
     * 认领扫码配对会话:POST {gatewayBase}/dsh/pair-sessions/{pairingId}/claim,
     * body `{"secret": ...}`,Bearer App JWT。200({ok:true})= 认领成功;
     * 非 200 折叠为 [CarrierException](httpStatus 保留:409 已被认领 / 410 已过期 / 401 登录过期)。
     */
    suspend fun claimPairSession(
        invite: CloudPairInvite,
        token: String
    ): Unit = suspendCancellableCoroutine { cont ->
        val body = buildJsonObject { put("secret", invite.secret) }
            .toString().toRequestBody(JsonMediaType)
        val request = Request.Builder()
            .url("${invite.gatewayBase}/dsh/pair-sessions/${invite.pairingId}/claim")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()
        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) {
                    cont.resumeWithException(
                        CarrierException("connect failed: ${e.message}", cause = e)
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (cont.isActive) {
                    response.use {
                        if (it.code == 200) {
                            cont.resume(Unit)
                        } else {
                            cont.resumeWithException(
                                CarrierException("http ${it.code}", httpStatus = it.code)
                            )
                        }
                    }
                }
            }
        })
    }

    companion object {
        private const val BindCodesPath = "/dsh/bind-codes"
        private const val RelayPath = "/dsh/relay"
        private const val CallTimeoutSeconds = 15L
        private const val InviteScheme = "oneboxdsh"
        private const val InviteVersion = "1"
        private const val MinSecretLength = 32
        private val EmptyJsonBody = "{}".toRequestBody("application/json".toMediaType())
        private val JsonMediaType = "application/json".toMediaType()

        /**
         * 解析扫码结果(onebox-dsh-bridge 插件页面二维码):
         * `oneboxdsh://pair?v=1&g=<gatewayBase>&p=<pairingId>&s=<secret>[&k=<e2eKey>]`。
         * 校验:scheme 必须 oneboxdsh、v/g/p/s 齐全、secret ≥32 字符、g 必须 https(防御);
         * k 可缺席(旧插件明文形态 → key = null),存在则必须 base64url 解码正好 32 字节,
         * 非法 k 同样整个邀请返回 null(UI 提示无效二维码)。
         */
        fun parseCloudPairInvite(raw: String): CloudPairInvite? {
            val text = raw.trim()
            if (text.isEmpty()) return null
            val uri = runCatching { URI(text) }.getOrNull() ?: return null
            if (!uri.scheme.equals(InviteScheme, ignoreCase = true)) return null
            // query 值逐个 URL 解码(g 是整段 URL,几乎必然带 percent-encoding)
            val query = uri.rawQuery.orEmpty().split('&')
                .mapNotNull { part ->
                    val idx = part.indexOf('=')
                    if (idx <= 0) null else {
                        part.substring(0, idx) to runCatching {
                            URLDecoder.decode(part.substring(idx + 1), Charsets.UTF_8)
                        }.getOrDefault("")
                    }
                }
                .toMap()
            if (query["v"] != InviteVersion) return null
            val gateway = query["g"].orEmpty().trim().trimEnd('/')
            if (!gateway.startsWith("https://")) return null
            val pairingId = query["p"].orEmpty().trim()
            val secret = query["s"].orEmpty().trim()
            if (pairingId.isEmpty() || secret.length < MinSecretLength) return null
            // E2E 密钥:缺席 = 旧插件明文形态;存在但非法(解码非 32 字节)= 无效二维码
            val key = query["k"]?.trim()?.takeIf { it.isNotEmpty() }
            if (key != null && E2ECipher.fromBase64Url(key) == null) return null
            return CloudPairInvite(
                gatewayBase = gateway,
                pairingId = pairingId,
                secret = secret,
                key = key
            )
        }

        /** App 后端根地址(去尾斜杠;开源构建为本地占位,请求会立即失败) */
        fun backendBase(): String = NetworkBuilder.getBaseUrl().trim().trimEnd('/')

        /** 云端中继 baseUri:直接作为 DSH 主机地址交给现有协议栈(wss 由 Downlink 处理) */
        fun relayBaseUri(): String = backendBase() + RelayPath

        /**
         * 当前 App 登录态 JWT:登录 token 优先,缺失时兜底游客 token
         * (RemoteConfig.accessToken);两者皆空 = 未登录。
         * 每次调用现取 —— 登录/登出/换号即时生效,无需重建连接材料。
         * 仅用于已建立连接的鉴权头([appTokenHeaders] 链路);云端中继入口
         * 门控走 [currentLoginToken](登录用户专属,游客不可用)。
         */
        fun currentAppToken(): String? =
            TokenStorage.getTokenFromLocalStorage()?.takeIf { it.isNotEmpty() }
                ?: RemoteConfigStorage.getRemoteConfig().accessToken
                    ?.takeIf { it.isNotBlank() }

        /**
         * 当前 App 登录用户 JWT(只看登录态,不看游客 token):
         * 云端中继的入口门控(可用性/绑定码/扫码认领/连接)一律用它 ——
         * 云端中继为登录用户专属功能,游客 token 不再兜底。
         */
        fun currentLoginToken(): String? =
            TokenStorage.getTokenFromLocalStorage()?.takeIf { it.isNotEmpty() }
    }
}
