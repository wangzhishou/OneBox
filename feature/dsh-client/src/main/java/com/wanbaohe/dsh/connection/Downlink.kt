package com.wanbaohe.dsh.connection

import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.DshJson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 单条下行 WebSocket 封装(DSH-PROTOCOL §2):打开后只收不发。
 *
 * - [rawFrames]:文本帧解析为 [JsonObject] 的流(server-request 信封原样,帧 union 解析在控制器)。
 *   带 replay 缓冲:真 host 在 mux open 瞬间同步推基线重放帧(still-pending 审批/问答 +
 *   队列/任务快照),而客户端要等三路握手全成才挂收集 —— 无缓冲会丢这批帧。
 * - E2E(云端中继):持有 [cipher] 时下行文本帧先解密再解析;无 "e2e" 标记的帧
 *   按明文原样受理(兼容旧插件)。
 * - [awaitDone]:对端关闭或出错时返回;随后看 [failure] 区分正常/异常。
 * - 畸形帧(非 JSON / 非 object)只经 [onProtocolError] 上报,不杀 socket。
 * - 客户端在这两条 socket 上不发送任何应用数据(协议不变式)。
 */
class Downlink private constructor(
    val name: String,
    private val webSocket: WebSocket,
    private val cipher: E2ECipher?
) {

    private val _rawFrames = MutableSharedFlow<JsonObject>(
        replay = ReplayBufferSize,
        extraBufferCapacity = ReplayBufferSize,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val rawFrames: SharedFlow<JsonObject> = _rawFrames.asSharedFlow()

    private val done = CompletableDeferred<Unit>()

    /** 非 null 表示因错误断开(而非对端正常关闭或本端主动 close) */
    @Volatile
    var failure: CarrierException? = null
        private set

    private val closed = AtomicBoolean(false)

    /** 对端关闭或出错时完成(含本端 close) */
    suspend fun awaitDone() = done.await()

    /** 主动关闭(整代重建时由控制器调;幂等、非阻塞) */
    fun close() {
        if (closed.compareAndSet(false, true)) {
            // OkHttp close 为异步优雅关闭,立即返回;不等对端回执
            webSocket.close(NormalCloseCode, "client closing")
            if (!done.isCompleted) done.complete(Unit)
        }
    }

    internal fun onText(text: String, onProtocolError: (CarrierException) -> Unit) {
        // E2E:先解密(无明文标记的帧原样);解密失败按畸形帧上报,不杀 socket
        val plain = runCatching { cipher?.decryptText(text) ?: text }.getOrElse {
            onProtocolError(
                it as? CarrierException
                    ?: CarrierException("e2e decrypt on $name: ${it.message}", cause = it)
            )
            return
        }
        val element = runCatching { DshJson.parseToJsonElement(plain) }.getOrElse {
            onProtocolError(CarrierException("non-json frame on $name: ${it.message}", cause = it))
            return
        }
        if (element is JsonObject) {
            _rawFrames.tryEmit(element)
        } else {
            onProtocolError(CarrierException("frame on $name not an object"))
        }
    }

    internal fun onClosed(code: Int) {
        if (closed.compareAndSet(false, true)) {
            failure = CarrierException("socket closed with code $code")
            if (!done.isCompleted) done.complete(Unit)
        }
    }

    internal fun onFailed(t: Throwable, response: Response?) {
        if (closed.compareAndSet(false, true)) {
            failure = CarrierException(
                "socket failure: ${t.message}",
                httpStatus = response?.code,
                cause = t
            )
            if (!done.isCompleted) done.complete(Unit)
        }
    }

    companion object {
        private const val NormalCloseCode = 1000
        private const val ReplayBufferSize = 64

        /**
         * 建立连接:onOpen 才算成功;upgrade 失败(含网关 401)抛 [CarrierException],
         * httpStatus 取自 upgrade 响应(WS 腿 401 判定的依据)。
         */
        suspend fun connect(
            name: String,
            okHttpClient: OkHttpClient,
            url: String,
            headers: Map<String, String> = emptyMap(),
            cipher: E2ECipher? = null,
            onProtocolError: (CarrierException) -> Unit = {}
        ): Downlink = suspendCancellableCoroutine { cont ->
            val requestBuilder = Request.Builder().url(url)
            headers.forEach { (k, v) -> requestBuilder.addHeader(k, v) }

            var downlink: Downlink? = null
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    val link = downlink ?: return
                    if (cont.isActive) cont.resume(link)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    downlink?.onText(text, onProtocolError)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    // 协议只定义 UTF-8 JSON 文本帧;二进制帧按畸形上报,不杀 socket
                    onProtocolError(CarrierException("binary frame on $name"))
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    // 回执对端的关闭帧,让 OkHttp 走完关闭流程
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    downlink?.onClosed(code)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    val link = downlink
                    if (link == null) {
                        // 极早期失败(回调先于构造返回):直接以异常完成挂起
                        if (cont.isActive) {
                            cont.resumeWithException(
                                CarrierException("connect failed: ${t.message}", httpStatus = response?.code, cause = t)
                            )
                        }
                        return
                    }
                    link.onFailed(t, response)
                    if (cont.isActive) {
                        cont.resumeWithException(
                            link.failure ?: CarrierException("connect failed: ${t.message}", cause = t)
                        )
                    }
                }
            }

            val webSocket = okHttpClient.newWebSocket(requestBuilder.build(), listener)
            downlink = Downlink(name, webSocket, cipher)
            cont.invokeOnCancellation { webSocket.cancel() }
        }
    }
}
