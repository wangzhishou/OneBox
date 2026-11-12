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

import com.wanbaohe.dsh.wire.CancelStreamMessage
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.ControlFrame
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.FollowFrame
import com.wanbaohe.dsh.wire.OpenStreamMessage
import com.wanbaohe.dsh.wire.RemoteEventFrame
import com.wanbaohe.dsh.wire.RemoteStreamServerMessage
import com.wanbaohe.dsh.wire.WorkspaceFollowFrame
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 自建 streamId(端点首段 + UUID),便于日志里一眼看出属于哪条流。
 */
private fun mintStreamId(endpoint: String): String =
    endpoint.substringBefore('/') + "--" + UUID.randomUUID().toString()

/**
 * `/api/remote.mux` 单条物理 WebSocket(DSH 0.1.5-rc.2)。
 *
 * **与旧协议的根本差别**:旧版是"一条 WS 一个主题"(`/api/events.mux` 全会话聚合、
 * `/api/events.host` 主机流),客户端只收不发;新版是**一条 WS 多路复用全部逻辑流**,
 * 客户端用 [openStream] / [cancelStream] 主动开流,下行帧按 `streamId` 分发。
 *
 * - 下行消息只有三类:item / error / end(见 [RemoteStreamServerMessage])
 * - 流级错误只终止该流,物理 socket 保持;物理断开才让整代失效
 * - 畸形帧只经 `onProtocolError` 上报,不杀 socket
 * - E2E(云端中继):持有 [cipher] 时下行文本帧先解密、上行帧先加密
 */
class Downlink private constructor(
    val name: String,
    private val webSocket: WebSocket,
    private val cipher: E2ECipher?
) {

    private val _messages = MutableSharedFlow<StreamMessage>(
        extraBufferCapacity = BufferCapacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val _streamFailures = MutableSharedFlow<StreamFailure>(
        extraBufferCapacity = BufferCapacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** 全部逻辑流的下行项(按 [StreamMessage.streamId] 分发) */
    val messages: SharedFlow<StreamMessage> = _messages.asSharedFlow()

    /** 流级失败(单条流终止,物理连接不受影响) */
    val streamFailures: SharedFlow<StreamFailure> = _streamFailures.asSharedFlow()

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
            webSocket.close(NormalCloseCode, "client closing")
            if (!done.isCompleted) done.complete(Unit)
        }
    }

    /**
     * 开一条逻辑流:`payload` 恒为 `{args: {…}}`(0.1.5 所有端点统一形状)。
     * @return 本流的 streamId,用于 [cancelStream] 与帧过滤
     */
    fun openStream(endpoint: String, args: JsonObject = JsonObject(emptyMap())): String {
        val streamId = mintStreamId(endpoint)
        val message = OpenStreamMessage(
            streamId = streamId,
            endpoint = endpoint,
            payload = buildJsonObject { put("args", args) }
        )
        send(DshJson.encodeToString(OpenStreamMessage.serializer(), message))
        return streamId
    }

    /** 取消一条逻辑流(Host 侧随之停止推送) */
    fun cancelStream(streamId: String) {
        val message = CancelStreamMessage(streamId = streamId)
        send(DshJson.encodeToString(CancelStreamMessage.serializer(), message))
    }

    /** 上行发送:持有 cipher 时先加密(与网关 E2E 帧格式一致) */
    private fun send(json: String) {
        if (closed.get()) return
        webSocket.send(cipher?.encryptText(json) ?: json)
    }

    internal fun onText(text: String, onProtocolError: (CarrierException) -> Unit) {
        val plain = runCatching { cipher?.decryptText(text) ?: text }.getOrElse {
            onProtocolError(
                it as? CarrierException
                    ?: CarrierException("e2e decrypt on " + name + ": " + it.message.orEmpty(), cause = it)
            )
            return
        }
        val element = runCatching { DshJson.parseToJsonElement(plain) }.getOrElse {
            onProtocolError(CarrierException("non-json frame on " + name + ": " + it.message.orEmpty(), cause = it))
            return
        }
        if (element !is JsonObject) {
            onProtocolError(CarrierException("frame on " + name + " not an object"))
            return
        }
        val message = runCatching {
            DshJson.decodeFromJsonElement(RemoteStreamServerMessage.serializer(), element)
        }.getOrElse {
            onProtocolError(CarrierException("stream frame on " + name + ": " + it.message.orEmpty(), cause = it))
            return
        }
        when (message) {
            is RemoteStreamServerMessage.Item ->
                _messages.tryEmit(StreamMessage(message.streamId, message.value))
            is RemoteStreamServerMessage.Error ->
                _streamFailures.tryEmit(
                    StreamFailure(
                        streamId = message.streamId,
                        code = message.error.code,
                        message = message.error.message,
                        details = message.error.details
                    )
                )
            is RemoteStreamServerMessage.End ->
                _streamFailures.tryEmit(
                    StreamFailure(
                        streamId = message.streamId,
                        code = StreamFailure.CodeEnded,
                        message = "stream ended"
                    )
                )
        }
    }

    internal fun onClosed(code: Int) {
        if (closed.compareAndSet(false, true)) {
            failure = CarrierException("socket closed with code " + code)
            if (!done.isCompleted) done.complete(Unit)
        }
    }

    internal fun onFailed(t: Throwable, response: Response?) {
        if (closed.compareAndSet(false, true)) {
            failure = CarrierException(
                "socket failure: " + t.message.orEmpty(),
                httpStatus = response?.code,
                cause = t
            )
            if (!done.isCompleted) done.complete(Unit)
        }
    }

    companion object {
        private const val NormalCloseCode = 1000
        private const val BufferCapacity = 256

        /**
         * 建立物理连接:onOpen 才算成功;upgrade 失败(含 401)抛 [CarrierException],
         * httpStatus 取自 upgrade 响应(401 判定依据)。
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
                    onProtocolError(CarrierException("binary frame on " + name))
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    downlink?.onClosed(code)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    val link = downlink
                    if (link == null) {
                        if (cont.isActive) {
                            cont.resumeWithException(
                                CarrierException(
                                    "connect failed: " + t.message.orEmpty(),
                                    httpStatus = response?.code,
                                    cause = t
                                )
                            )
                        }
                        return
                    }
                    link.onFailed(t, response)
                    if (cont.isActive) {
                        cont.resumeWithException(
                            link.failure
                                ?: CarrierException("connect failed: " + t.message.orEmpty(), cause = t)
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

/** 一条逻辑流的下行项 */
data class StreamMessage(
    val streamId: String,
    val value: JsonElement
)

/** 流级失败(含 Host 的 error 帧与流正常结束) */
data class StreamFailure(
    val streamId: String,
    val code: String,
    val message: String,
    val details: JsonObject = JsonObject(emptyMap())
) {
    /** 是否只是"流正常结束"(不是错误) */
    val isEnded: Boolean get() = code == CodeEnded

    companion object {
        const val CodeEnded = "stream/ended"
    }
}

// ───────────────────────────── 逻辑流封装 ─────────────────────────────

/**
 * 一条逻辑流的句柄:按 streamId 过滤 [Downlink.messages],把帧解析成调用方要的类型,
 * 并把流级失败暴露为 [failure](非 null 即已终止)。
 */
sealed class DshStream(
    private val downlink: Downlink,
    protected val streamId: String
) {
    private val _failure = MutableStateFlow<StreamFailure?>(null)

    /** 本流是否已终止(正常结束或失败) */
    val failure: StateFlow<StreamFailure?> = _failure.asStateFlow()

    val isClosed: Boolean get() = _failure.value != null

    protected fun markFailure(failure: StreamFailure) {
        if (_failure.value == null) _failure.value = failure
    }

    /** 取消本流(幂等) */
    fun close() {
        downlink.cancelStream(streamId)
        markFailure(StreamFailure(streamId, StreamFailure.CodeEnded, "client closed"))
    }

    protected suspend fun collectRaw(block: suspend (JsonElement) -> Unit) {
        downlink.messages.collect { message ->
            if (message.streamId == streamId) block(message.value)
        }
    }

    protected suspend fun observeFailures() {
        downlink.streamFailures.collect { failure ->
            if (failure.streamId == streamId) markFailure(failure)
        }
    }
}

/** `$events` 转发事件流(替代旧 `/api/events.host`) */
class EventStream internal constructor(
    downlink: Downlink,
    streamId: String,
    private val onProtocolError: (CarrierException) -> Unit
) : DshStream(downlink, streamId) {

    private val _frames = MutableSharedFlow<RemoteEventFrame>(
        extraBufferCapacity = 128,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val frames: SharedFlow<RemoteEventFrame> = _frames.asSharedFlow()

    /** 首帧 ready 给出的世代身份;应答 waterfall 时必须回带 */
    @Volatile
    var clientId: String? = null
        private set

    /** ready 帧里的 Host home(缩写路径用) */
    @Volatile
    var home: String? = null
        private set

    internal suspend fun pump(): Unit = coroutineScope {
        launch { observeFailures() }
        collectRaw { value ->
            val frame = runCatching {
                DshJson.decodeFromJsonElement(RemoteEventFrame.serializer(), value)
            }.getOrElse {
                onProtocolError(CarrierException("events frame: " + it.message.orEmpty(), cause = it))
                return@collectRaw
            }
            if (frame is RemoteEventFrame.Ready) {
                clientId = frame.clientId
                home = frame.host.home
            }
            _frames.tryEmit(frame)
        }
    }
}

/** `session/follow` 流(每会话一条) */
class FollowStream internal constructor(
    downlink: Downlink,
    streamId: String,
    private val onProtocolError: (CarrierException) -> Unit
) : DshStream(downlink, streamId) {

    private val _frames = MutableSharedFlow<FollowFrame>(
        extraBufferCapacity = 512,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val frames: SharedFlow<FollowFrame> = _frames.asSharedFlow()

    internal suspend fun pump(): Unit = coroutineScope {
        launch { observeFailures() }
        collectRaw { value ->
            val frame = runCatching {
                DshJson.decodeFromJsonElement(FollowFrame.serializer(), value)
            }.getOrElse {
                onProtocolError(CarrierException("follow frame: " + it.message.orEmpty(), cause = it))
                return@collectRaw
            }
            _frames.tryEmit(frame)
        }
    }
}

/** `session/control` 流(全 Host 一条) */
class ControlStream internal constructor(
    downlink: Downlink,
    streamId: String,
    private val onProtocolError: (CarrierException) -> Unit
) : DshStream(downlink, streamId) {

    private val _frames = MutableSharedFlow<ControlFrame>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val frames: SharedFlow<ControlFrame> = _frames.asSharedFlow()

    internal suspend fun pump(): Unit = coroutineScope {
        launch { observeFailures() }
        collectRaw { value ->
            val frame = runCatching {
                DshJson.decodeFromJsonElement(ControlFrame.serializer(), value)
            }.getOrElse {
                onProtocolError(CarrierException("control frame: " + it.message.orEmpty(), cause = it))
                return@collectRaw
            }
            _frames.tryEmit(frame)
        }
    }
}

/** `workspace/follow` 流 */
class WorkspaceStream internal constructor(
    downlink: Downlink,
    streamId: String,
    private val onProtocolError: (CarrierException) -> Unit
) : DshStream(downlink, streamId) {

    private val _frames = MutableSharedFlow<WorkspaceFollowFrame>(
        extraBufferCapacity = 128,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val frames: SharedFlow<WorkspaceFollowFrame> = _frames.asSharedFlow()

    internal suspend fun pump(): Unit = coroutineScope {
        launch { observeFailures() }
        collectRaw { value ->
            val frame = runCatching {
                DshJson.decodeFromJsonElement(WorkspaceFollowFrame.serializer(), value)
            }.getOrElse {
                onProtocolError(CarrierException("workspace frame: " + it.message.orEmpty(), cause = it))
                return@collectRaw
            }
            _frames.tryEmit(frame)
        }
    }
}
