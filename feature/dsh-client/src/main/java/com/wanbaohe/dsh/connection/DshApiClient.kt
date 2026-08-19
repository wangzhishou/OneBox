package com.wanbaohe.dsh.connection

import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.ApiTimeoutException
import com.wanbaohe.dsh.wire.ClientRequest
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.RpcError
import com.wanbaohe.dsh.wire.RpcErrorCodes
import com.wanbaohe.dsh.wire.RpcResult
import com.wanbaohe.dsh.wire.ServerResponse
import com.wanbaohe.dsh.wire.model.ClientResponse
import com.wanbaohe.dsh.wire.model.HostInfo
import com.wanbaohe.dsh.wire.model.QueueAction
import com.wanbaohe.dsh.wire.model.RespondReceipt
import com.wanbaohe.dsh.wire.model.SessionCancelValue
import com.wanbaohe.dsh.wire.model.SessionUpdateQueueValue
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.TimeoutCancellationException

/**
 * DSH RPC 上行客户端(DSH-PROTOCOL §1/§2)。
 *
 * 职责:
 * - 信封 wrap:POST {baseUri}/api/<method>,必须 Content-Type: application/json(否则 415)
 * - rpcId mint(UUID)并校验响应回显
 * - 两级解析:先信封,业务 value 由调用方二次 parse
 * - 错误三级折叠:[RpcBusinessException](业务)/ [CarrierException](载波)/ [ApiTimeoutException](超时)
 *
 * unary 默认 30s 超时;host.pickDirectory 等用户节奏方法调用方用 [call] 的 timeout 放宽。
 */
@Singleton
class DshApiClient @Inject constructor(
    @Named("DshOkHttpClient") private val okHttpClient: OkHttpClient
) {

    /** 当前主机地址(已归一化,含 scheme、无尾斜杠) */
    @Volatile
    private var baseUri: String? = null

    /**
     * 鉴权头钩子(P6):远程网关形态所有 HTTP 请求(/api 各方法、/api/respond、
     * session.export 下载)携带 Authorization: Bearer;无令牌返回空 map。
     * 由连接控制器按当前连接形态注入;令牌原地刷新(重登/重配)后无需重建本 client。
     */
    @Volatile
    var authHeadersProvider: (() -> Map<String, String>)? = null

    /**
     * E2E 载荷加解密(云端中继):持有密钥时 /api 各方法与 /api/respond 的
     * 请求体加密、响应体解密(网关只见密文);null = 明文,行为同旧版。
     * 由连接控制器按当前连接条目注入;session.export 下载不加密(不在契约范围)。
     */
    @Volatile
    var payloadCipher: E2ECipher? = null

    /** 设置主机地址;输入可不带 scheme,默认补 http:// */
    fun setBaseUri(address: String) {
        baseUri = normalizeBaseUri(address)
    }

    /**
     * 单方法调用:信封 wrap → POST → 信封 unwrap → 返回原始 result.value。
     * value 缺席(ok:true 无 value)时返回 JsonNull。
     */
    suspend fun call(
        method: String,
        payload: JsonObject,
        timeout: Duration? = null
    ): JsonElement {
        val base = baseUri ?: throw CarrierException("baseUri 未设置")
        return roundTrip(base, method, payload, timeout ?: DefaultUnaryTimeout)
    }

    /**
     * 远程端点调用(typert gateway,DSH-PROTOCOL §9):
     * 斜杠命名端点(commands/list、commands/execute 等),payload 必须恰好是
     * `{args: {...}}`(args 字段名即端点签名参数名;缺字段服务端点名报错)。
     *
     * 信封层数按端点而异,按形状剥离:result.value 是 Map 且含 ok 键 → 视为
     * TS RemoteResult 内层信封,ok:true 取内层 value、ok:false 折叠为
     * [RpcBusinessException](错误码取内层 error.code **原样保留** —— 远程端点是
     * typert 自己的错误空间,如 messageFeedback 的 version-conflict / note-too-large,
     * 不走 RpcErrorDetailsMap 封闭集归一化,调用方按原码分流);
     * 其余形状(裸数组 / 普通 Map)原样返回。
     */
    suspend fun callRemote(
        name: String,
        args: JsonObject,
        timeout: Duration? = null
    ): JsonElement {
        val payload = buildJsonObject { put("args", args) }
        val value = call(name, payload, timeout)
        if (value is JsonObject && value.containsKey("ok")) {
            val ok = (value["ok"] as? JsonPrimitive)?.contentOrNull
            if (ok == "true") return value["value"] ?: JsonNull
            val errorJson = value["error"] as? JsonObject
            val code = (errorJson?.get("code") as? JsonPrimitive)?.contentOrNull
                ?: RpcErrorCodes.Internal
            val message = (errorJson?.get("message") as? JsonPrimitive)?.contentOrNull
                ?: "remote error"
            // 原样保留内层错误码(远程端点独立错误空间,不做封闭集归一化)
            throw RpcBusinessException(RpcError(code, message))
        }
        return value
    }

    /** 便捷方法:设置地址并调用 host.describe(就绪探针 + 能力面) */
    suspend fun hostDescribe(baseUri: String): HostInfo {
        setBaseUri(baseUri)
        val value = call(HostDescribeMethod, buildJsonObject {})
        return DshJson.decodeFromJsonElement(value)
    }

    /**
     * 应答可应答帧(DSH-PROTOCOL §1):POST {base}/api/respond,
     * body 为 client-response 信封(rpcId 原样回显帧的,value 装 result.value 槽)。
     *
     * 响应体两种形态都受理:server-response 信封(校验 rpcId 回显 + RpcResult 分流,
     * 回执在 result.value)或裸回执 JSON `{accepted, reason?}`(Flutter 实测形态)。
     * 第一个到达的应答占有请求:迟到者 not-pending,畸形者 bad-response。
     */
    suspend fun respond(rpcId: String, value: JsonObject): RespondReceipt {
        val base = baseUri ?: throw CarrierException("baseUri 未设置")
        val envelope = ClientResponse.mint(rpcId, value)
        val body = try {
            withTimeout(DefaultUnaryTimeout.inWholeMilliseconds) {
                postRespond(base, envelope)
            }
        } catch (e: TimeoutCancellationException) {
            throw ApiTimeoutException(RespondPath, DefaultUnaryTimeout)
        } catch (e: RpcBusinessException) {
            throw e
        } catch (e: CarrierException) {
            throw e
        } catch (e: IOException) {
            throw CarrierException("socket: ${e.message}", cause = e)
        }
        return parseReceipt(body, rpcId)
    }

    /** session.updateQueue:按 MessageId 寻址的 splice(action 体见 [QueueAction]) */
    suspend fun sessionUpdateQueue(
        sessionId: String,
        itemId: String,
        action: QueueAction
    ): SessionUpdateQueueValue {
        val payload = buildJsonObject {
            put("sessionId", sessionId)
            put("itemId", itemId)
            put("action", DshJson.encodeToJsonElement(action))
        }
        return DshJson.decodeFromJsonElement(call(RpcSessionUpdateQueue, payload))
    }

    /** session.cancel:只中止当前 turn,保留 pending inbox(客户端永不重发/提升排队消息) */
    suspend fun sessionCancel(sessionId: String): SessionCancelValue {
        val payload = buildJsonObject { put("sessionId", sessionId) }
        return DshJson.decodeFromJsonElement(call(RpcSessionCancel, payload))
    }

    /**
     * 会话导出(非 RPC 下载面,DSH-PROTOCOL §3):
     * GET {base}/api/session.export?sessionId=…&includeDescendants=…,流式 ZIP 落盘。
     * 流式写文件:不整读进内存;HTTP 非 200 折叠为 [CarrierException]。
     */
    suspend fun sessionExport(
        sessionId: String,
        destination: File,
        includeDescendants: Boolean = true
    ) {
        val base = baseUri ?: throw CarrierException("baseUri 未设置")
        val url = ("$base/api/session.export").toHttpUrl().newBuilder()
            .addQueryParameter("sessionId", sessionId)
            .addQueryParameter("includeDescendants", includeDescendants.toString())
            .build()
        downloadTo(url, destination)
    }

    /** GET 流式下载到文件;OkHttp 回调转协程,取消时中断底层 Call 并删半成品文件 */
    private suspend fun downloadTo(
        url: HttpUrl,
        destination: File
    ): Unit = suspendCancellableCoroutine { cont ->
        val request = Request.Builder().url(url).get().withAuth().build()
        val call = okHttpClient.newCall(request)
        cont.invokeOnCancellation {
            call.cancel()
            destination.delete()
        }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                destination.delete()
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
                        // 在 OkHttp 派发线程上流式落盘(读超时按次计,不受文件大小影响)
                        destination.parentFile?.mkdirs()
                        it.body.byteStream().use { input ->
                            destination.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
                outcome.onFailure { destination.delete() }
                if (cont.isActive) {
                    outcome.fold(
                        onSuccess = { cont.resume(Unit) },
                        onFailure = cont::resumeWithException
                    )
                }
            }
        })
    }

    /** respond 回执解析:server-response 信封与裸回执 JSON 两形态 */
    private fun parseReceipt(body: JsonObject, sentRpcId: String): RespondReceipt {
        val type = (body["type"] as? JsonPrimitive)?.contentOrNull
        if (type == "server-response") {
            if ((body["rpcId"] as? JsonPrimitive)?.contentOrNull != sentRpcId) {
                throw CarrierException("respond rpcId mismatch")
            }
            val result = body["result"] as? JsonObject
                ?: throw CarrierException("respond result not an object")
            return when (val outcome = RpcResult.parse(result)) {
                is RpcResult.Err -> throw RpcBusinessException(outcome.error)
                is RpcResult.Ok -> decodeReceipt(outcome.value)
            }
        }
        return decodeReceipt(body)
    }

    /** 回执对象解码;非对象(如 ok:true 无 value)视为已接受 */
    private fun decodeReceipt(element: JsonElement): RespondReceipt {
        if (element !is JsonObject) return RespondReceipt(accepted = true)
        return DshJson.decodeFromJsonElement(RespondReceipt.serializer(), element)
    }

    /** 发起 /api/respond POST 并返回响应体 JsonObject;OkHttp 回调转协程,取消时中断底层 Call */
    private suspend fun postRespond(
        base: String,
        envelope: ClientResponse
    ): JsonObject = suspendCancellableCoroutine { cont ->
        val bodyText = DshJson.encodeToString(ClientResponse.serializer(), envelope)
        val body = (payloadCipher?.encryptText(bodyText) ?: bodyText)
            .toRequestBody(JsonMediaType)
        val request = Request.Builder()
            .url("$base$RespondPath")
            .post(body)
            .withAuth()
            .build()
        val call = okHttpClient.newCall(request)
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
                        val raw = it.body.string()
                        val element = DshJson.parseToJsonElement(
                            payloadCipher?.decryptText(raw) ?: raw
                        )
                        element as? JsonObject
                            ?: throw CarrierException("respond receipt not an object")
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

    /** 带超时的完整回环;把各类失败折叠为三级异常 */
    private suspend fun roundTrip(
        base: String,
        method: String,
        payload: JsonObject,
        limit: Duration
    ): JsonElement {
        try {
            return withTimeout(limit.inWholeMilliseconds) {
                execute(base, method, payload)
            }
        } catch (e: TimeoutCancellationException) {
            throw ApiTimeoutException(method, limit)
        } catch (e: RpcBusinessException) {
            throw e
        } catch (e: CarrierException) {
            throw e
        } catch (e: IOException) {
            throw CarrierException("socket: ${e.message}", cause = e)
        } catch (e: IllegalArgumentException) {
            // URL 非法 / JSON 解析失败等
            throw CarrierException("malformed: ${e.message}", cause = e)
        }
    }

    /** 发起一次 POST 并解析到 result.value;OkHttp 回调转协程,取消时中断底层 Call */
    private suspend fun execute(
        base: String,
        method: String,
        payload: JsonObject
    ): JsonElement = suspendCancellableCoroutine { cont ->
        val envelope = ClientRequest.mint(method, payload)
        val bodyText = DshJson.encodeToString(ClientRequest.serializer(), envelope)
        val body = (payloadCipher?.encryptText(bodyText) ?: bodyText)
            .toRequestBody(JsonMediaType)
        val request = Request.Builder()
            .url("$base/api/$method")
            .post(body)
            .withAuth()
            .build()
        val call = okHttpClient.newCall(request)
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
                    response.use { parseResponse(it, envelope.rpcId) }
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

    /** 响应解析:HTTP 状态 → 信封 → rpcId 回显校验 → RpcResult 分流 */
    private fun parseResponse(response: Response, sentRpcId: String): JsonElement {
        if (response.code != 200) {
            throw CarrierException("http ${response.code}", httpStatus = response.code)
        }
        val raw = response.body.string()
        val text = payloadCipher?.decryptText(raw) ?: raw
        val envelope = try {
            DshJson.decodeFromString(ServerResponse.serializer(), text)
        } catch (e: Exception) {
            throw CarrierException("envelope parse: ${e.message}", cause = e)
        }
        if (envelope.type != "server-response") {
            throw CarrierException("expected server-response, got ${envelope.type}")
        }
        if (envelope.rpcId != sentRpcId) {
            throw CarrierException("rpcId mismatch: sent $sentRpcId, got ${envelope.rpcId}")
        }
        return when (val result = RpcResult.parse(envelope.result)) {
            is RpcResult.Ok -> result.value
            is RpcResult.Err -> throw RpcBusinessException(result.error)
        }
    }

    /** 附带当前鉴权头(P6 网关令牌;无钩子/无令牌时不添头) */
    private fun Request.Builder.withAuth(): Request.Builder {
        authHeadersProvider?.invoke().orEmpty().forEach { (k, v) -> header(k, v) }
        return this
    }

    private fun normalizeBaseUri(address: String): String {
        var normalized = address.trim().trimEnd('/')
        require(normalized.isNotEmpty()) { "empty host address" }
        if (!normalized.contains("://")) normalized = "http://$normalized"
        return normalized
    }

    companion object {
        private const val HostDescribeMethod = "host.describe"
        private const val RpcSessionUpdateQueue = "session.updateQueue"
        private const val RpcSessionCancel = "session.cancel"
        private const val RespondPath = "/api/respond"
        private val DefaultUnaryTimeout = 30.seconds
        private val JsonMediaType = "application/json".toMediaType()
    }
}
