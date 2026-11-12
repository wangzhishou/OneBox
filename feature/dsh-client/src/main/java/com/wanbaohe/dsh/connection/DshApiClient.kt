/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.dsh.connection

import com.wanbaohe.dsh.wire.ApiTimeoutException
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.ClientRequest
import com.wanbaohe.dsh.wire.DshEndpoints
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.EventResultOutcome
import com.wanbaohe.dsh.wire.EventResultRequest
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.RpcError
import com.wanbaohe.dsh.wire.RpcErrorCodes
import com.wanbaohe.dsh.wire.RpcResult
import com.wanbaohe.dsh.wire.ServerResponse
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
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

/**
 * DSH 一元 RPC 上行客户端(0.1.5-rc.2 的 Connection `/api` 通道)。
 *
 * 契约要点:
 * - 端点名是 `命名空间/方法`(斜杠),0.1.5 起全部端点如此;旧的 `命名空间.方法` 已废弃
 * - body 恒为 `{type:'client-request', rpcId, method:<同一个端点名>, payload:{args:{…}}}`,
 *   必须 `Content-Type: application/json`;路径上的端点名与 method 字段不一致会被服务端拒
 * - 响应为 `{type:'server-response', rpcId, result:{ok:true,value}|{ok:false,error}}`;
 *   value 再由调用方二次 parse
 * - 错误三级折叠:[RpcBusinessException](业务,码原样保留)/ [CarrierException](载波)/ [ApiTimeoutException](超时)
 * - 交互应答(`approval/request`、`user-questions/request`)不走这里:它们是 `$events` 上的
 *   waterfall,应答走 [eventsResult]
 */
@Singleton
class DshApiClient @Inject constructor(
    @Named("DshOkHttpClient") private val okHttpClient: OkHttpClient
) {

    /** 当前主机地址(已归一化,含 scheme、无尾斜杠) */
    @Volatile
    private var baseUri: String? = null

    /**
     * 鉴权头钩子:远程网关形态所有 HTTP 请求携带 Authorization: Bearer;无令牌返回空 map。
     * 由连接控制器按当前连接形态注入;令牌原地刷新(重登/重配)后无需重建本 client。
     */
    @Volatile
    var authHeadersProvider: (() -> Map<String, String>)? = null

    /**
     * E2E 载荷加解密(云端中继):持有密钥时 /api 各方法与 $events/result 的
     * 请求体加密、响应体解密(网关只见密文);null = 明文。
     */
    @Volatile
    var payloadCipher: E2ECipher? = null

    /** 设置主机地址;输入可不带 scheme,默认补 http:// */
    fun setBaseUri(address: String) {
        baseUri = normalizeBaseUri(address)
    }

    /**
     * 单方法调用:信封 wrap → POST → 信封 unwrap → 返回原始 result.value。
     * args 缺席时按空对象发送(0.1.5 所有端点都要求恰好一个 args 字段)。
     */
    suspend fun call(
        endpoint: String,
        args: JsonObject = JsonObject(emptyMap()),
        timeout: Duration? = null
    ): JsonElement {
        val base = baseUri ?: throw CarrierException("baseUri 未设置")
        return roundTrip(base, endpoint, args, timeout ?: DefaultUnaryTimeout)
    }

    /**
     * 调用并展开 RemoteResult:0.1.5 的一元端点回值恒为
     * `{ok:true,value}` / `{ok:false,error}`;后者折叠为 [RpcBusinessException]
     * (错误码原样保留,不做封闭集归一化)。
     */
    suspend fun callRemote(
        endpoint: String,
        args: JsonObject = JsonObject(emptyMap()),
        timeout: Duration? = null
    ): JsonElement {
        val value = call(endpoint, args, timeout)
        return unwrapRemoteResult(value)
    }

    /** 展开 `{ok,value|error}` 内层信封;非该形状(裸值)原样返回 */
    fun unwrapRemoteResult(value: JsonElement): JsonElement {
        if (value !is JsonObject || !value.containsKey("ok")) return value
        val ok = (value["ok"] as? JsonPrimitive)?.contentOrNull
        if (ok == "true") return value["value"] ?: JsonNull
        val errorJson = value["error"] as? JsonObject
        val code = (errorJson?.get("code") as? JsonPrimitive)?.contentOrNull
            ?: RpcErrorCodes.GatewayInternal
        val message = (errorJson?.get("message") as? JsonPrimitive)?.contentOrNull
            ?: "remote error"
        val details = errorJson?.get("details") as? JsonObject
        throw RpcBusinessException(RpcError.of(code, message, details))
    }

    /**
     * 应答 `$events` 上的 waterfall 帧(审批 / 结构化问答)。
     * body 为 `{args:{clientId,eventId,outcome}}`;首个到达的应答占有请求,
     * 迟到者被服务端忽略(不报错)。
     */
    suspend fun eventsResult(
        clientId: String,
        eventId: String,
        outcome: EventResultOutcome,
        timeout: Duration? = null
    ) {
        val args = DshJson.encodeToJsonElement(
            EventResultRequest.serializer(),
            EventResultRequest(clientId = clientId, eventId = eventId, outcome = outcome)
        ) as JsonObject
        call(DshEndpoints.EVENT_RESULT, args, timeout)
    }

    /** 便捷:应答一个"结果值"型 outcome */
    suspend fun eventsResultValue(clientId: String, eventId: String, value: JsonElement) {
        eventsResult(clientId, eventId, EventResultOutcome(EventResultOutcome.KIND_RESULT, value))
    }

    /**
     * 会话导出(非 RPC 下载面):GET `/api/session.export?sessionId=…`,流式 ZIP 落盘。
     * 注意:该路由由主机侧 `session-log-export` 装载才存在,404 表示主机未启用。
     */
    suspend fun sessionExport(
        sessionId: String,
        destination: File,
        includeDescendants: Boolean = true
    ) {
        val base = baseUri ?: throw CarrierException("baseUri 未设置")
        val url = ("$base" + DshEndpoints.SESSION_EXPORT).toHttpUrl().newBuilder()
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
                        CarrierException("connect failed: " + e.message.orEmpty(), cause = e)
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val outcome = runCatching {
                    response.use {
                        if (it.code != 200) {
                            throw CarrierException("http " + it.code, httpStatus = it.code)
                        }
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

    /** 带超时的完整回环;把各类失败折叠为三级异常 */
    private suspend fun roundTrip(
        base: String,
        endpoint: String,
        args: JsonObject,
        limit: Duration
    ): JsonElement {
        try {
            return withTimeout(limit.inWholeMilliseconds) {
                execute(base, endpoint, args)
            }
        } catch (e: TimeoutCancellationException) {
            throw ApiTimeoutException(endpoint, limit)
        } catch (e: RpcBusinessException) {
            throw e
        } catch (e: CarrierException) {
            throw e
        } catch (e: IOException) {
            throw CarrierException("socket: " + e.message.orEmpty(), cause = e)
        } catch (e: IllegalArgumentException) {
            throw CarrierException("malformed: " + e.message.orEmpty(), cause = e)
        }
    }

    /** 发起一次 POST 并解析到 result.value;OkHttp 回调转协程,取消时中断底层 Call */
    private suspend fun execute(
        base: String,
        endpoint: String,
        args: JsonObject
    ): JsonElement = suspendCancellableCoroutine { cont ->
        val envelope = ClientRequest.mint(
            method = endpoint,
            payload = buildJsonObject { put("args", args) }
        )
        val bodyText = DshJson.encodeToString(ClientRequest.serializer(), envelope)
        val body = (payloadCipher?.encryptText(bodyText) ?: bodyText)
            .toRequestBody(JsonMediaType)
        val request = Request.Builder()
            .url("$base/api/$endpoint")
            .post(body)
            .withAuth()
            .build()
        val call = okHttpClient.newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) {
                    cont.resumeWithException(
                        CarrierException("connect failed: " + e.message.orEmpty(), cause = e)
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
            throw CarrierException("http " + response.code, httpStatus = response.code)
        }
        val raw = response.body.string()
        val text = payloadCipher?.decryptText(raw) ?: raw
        val envelope = try {
            DshJson.decodeFromString(ServerResponse.serializer(), text)
        } catch (e: Exception) {
            throw CarrierException("envelope parse: " + e.message.orEmpty(), cause = e)
        }
        if (envelope.type != "server-response") {
            throw CarrierException("expected server-response, got " + envelope.type)
        }
        if (envelope.rpcId != sentRpcId) {
            throw CarrierException("rpcId mismatch: sent $sentRpcId, got " + envelope.rpcId)
        }
        return when (val result = RpcResult.parse(envelope.result)) {
            is RpcResult.Ok -> result.value
            is RpcResult.Err -> throw RpcBusinessException(result.error)
        }
    }

    /** 附带当前鉴权头(网关令牌;无钩子/无令牌时不添头) */
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
        private val DefaultUnaryTimeout = 30.seconds
        private val JsonMediaType = "application/json".toMediaType()
    }
}
