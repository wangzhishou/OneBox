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

import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.ControlFrame
import com.wanbaohe.dsh.wire.DshEndpoints
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.FollowFrame
import com.wanbaohe.dsh.wire.RemoteEventFrame
import com.wanbaohe.dsh.wire.WireCompat
import com.wanbaohe.dsh.wire.WorkspaceFollowFrame
import com.wanbaohe.dsh.wire.model.HostInfo
import com.wanbaohe.dsh.wire.model.ModelCatalog
import com.wanbaohe.dsh.wire.model.SessionAddress
import com.wanbaohe.dsh.wire.model.SessionFollowRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** 连接阶段:connecting(握手/退避中)→ ready(就绪)→ down(代际失效,等待重建) */
enum class ConnectionPhase { Connecting, Ready, Down }

/**
 * 代际快照(connecting → ready → down → connecting ...)。
 * [failureReason] 仅 down 阶段携带;[describe] 仅 ready 阶段携带。
 */
data class ConnectionSnapshot(
    val generation: Int,
    val phase: ConnectionPhase,
    val describe: HostInfo? = null,
    val failureReason: String? = null
)

/**
 * 带会话归属的 follow 帧(一条 follow 流 = 一个会话)。
 * 旧协议里帧自带 sessionId,新协议改为"每会话一条流",归属由流决定。
 */
data class SessionFrameEvent(
    val sessionId: String,
    val address: SessionAddress,
    val frame: FollowFrame
)

/** 带会话归属的 control 帧(控制面是全局一条流,帧里带 sessionId) */
data class ControlFrameEvent(
    val sessionId: String,
    val frame: ControlFrame
)

/** 一条流终止的原因(会话/工作区流) */
data class StreamStatus(
    val code: String,
    val message: String
)

/**
 * DSH 连接控制器(DSH 0.1.5-rc.2 协议面)。
 *
 * **0.1.5 的连接模型与旧版完全不同**:
 * - 物理层只有**一条** `/api/remote.mux` WebSocket(旧版是 `/api/events.mux` +
 *   `/api/events.host` 两条只收不发的 WS)
 * - 逻辑层按需开流:`$events`(Host 转发事件 + 就绪信号)、`session/control`(全局控制面)、
 *   `session/follow`(每会话一条)、`workspace/follow`(工作区)
 * - 没有 `host.describe`:就绪判定 = mux 打开 + `$events` 首帧 ready + 一元探针成功;
 *   主机版本号不再上报,UI 上的版本是 App 侧协议锚点([WireCompat])
 *
 * 语义:
 * - 任一必需腿失败 → 当前代际失效 → 整代重建(新 mux + 重新开流 + 重新探针)
 * - 会话流单独断开**不影响**整代:[followStatus] 暴露原因,调用方按需重开
 * - 指数退避 300ms → 8s;网关 401 → [authBlocked] = true,停退避等重新登录,[resume] 恢复
 * - 畸形帧只上报([protocolErrors])不杀连接
 *
 * 生命周期与组件绑定:每个连接实例独立(不做 @Singleton),由组件层创建并 [dispose]。
 */
class DshConnectionController(
    private val okHttpClient: OkHttpClient,
    private val apiClient: DshApiClient,
    baseUri: String,
    /** 鉴权头钩子(网关令牌;mux 与全部 /api HTTP 携带) */
    private val authHeaders: (() -> Map<String, String>)? = null,
    /** E2E 载荷加解密(云端中继持有密钥时):/api HTTP 体 + mux 双向帧 */
    private val payloadCipher: E2ECipher? = null,
    /** 网关令牌形态(已鉴权远程)= true → 特权面可见 */
    authenticatedRemote: Boolean = false,
    private val initialBackoff: Duration = 300.milliseconds,
    private val maxBackoff: Duration = 8.seconds,
    private val probeTimeout: Duration = 10.seconds
) {

    /** 归一化后的主机地址(含 scheme、无尾斜杠) */
    private val baseUri: String = normalizeBaseUri(baseUri)

    /** 特权面可见性:loopback 全开 / LAN 围栏 / 网关已鉴权远程全开 */
    val privilegeScope: PrivilegeScope = PrivilegeScope.of(baseUri, authenticatedRemote)

    init {
        apiClient.authHeadersProvider = authHeaders
        apiClient.payloadCipher = payloadCipher
    }

    /** mux 专用 client:协议级 ping 保活 20s(代理对空闲 WS 普遍有读超时) */
    private val wsClient: OkHttpClient =
        okHttpClient.newBuilder().pingInterval(WsPingIntervalSeconds, TimeUnit.SECONDS).build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _snapshots = MutableStateFlow(ConnectionSnapshot(generation = 0, phase = ConnectionPhase.Down))
    val snapshots: StateFlow<ConnectionSnapshot> = _snapshots.asStateFlow()

    // ── 下行帧流(会话归属见 SessionFrameEvent / ControlFrameEvent) ──

    private val _sessionFrames = MutableSharedFlow<SessionFrameEvent>(
        extraBufferCapacity = FrameBufferCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** 当前代际的会话帧(follow 流;重连后由新一代重新打开的流续上) */
    val sessionFrames: SharedFlow<SessionFrameEvent> = _sessionFrames.asSharedFlow()

    private val _controlFrames = MutableSharedFlow<ControlFrameEvent>(
        extraBufferCapacity = FrameBufferCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** 当前代际的控制面帧(queue / jobs / projection,baseeline 已展开) */
    val controlFrames: SharedFlow<ControlFrameEvent> = _controlFrames.asSharedFlow()

    private val _workspaceFrames = MutableSharedFlow<WorkspaceFollowFrame>(
        extraBufferCapacity = FrameBufferCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** 当前代际的工作区帧 */
    val workspaceFrames: SharedFlow<WorkspaceFollowFrame> = _workspaceFrames.asSharedFlow()

    private val _eventFrames = MutableSharedFlow<RemoteEventFrame>(
        extraBufferCapacity = FrameBufferCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** `$events` 转发事件(审批/问答 waterfall 与单向 emit) */
    val eventFrames: SharedFlow<RemoteEventFrame> = _eventFrames.asSharedFlow()

    private val _protocolErrors = MutableSharedFlow<CarrierException>(
        extraBufferCapacity = FrameBufferCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** 协议级畸形帧上报(不杀连接) */
    val protocolErrors: SharedFlow<CarrierException> = _protocolErrors.asSharedFlow()

    private val _authBlocked = MutableStateFlow(false)

    /** 网关已拒绝当前令牌(401);为 true 时重试循环已停,重新登录后调 [resume] */
    val authBlocked: StateFlow<Boolean> = _authBlocked.asStateFlow()

    private var generation = 0
    private var attempt = 0

    @Volatile private var disposed = false

    @Volatile private var started = false
    private var live: LiveGeneration? = null

    /** 当前代际身份(应答 waterfall 用);null 表示尚未 ready */
    val eventClientId: String? get() = live?.events?.clientId

    /** Host home 目录(ready 帧给出;缩写路径用) */
    val hostHome: String? get() = live?.events?.home

    /** 启动连接(幂等;已 start 或已 dispose 时为空操作) */
    fun start() {
        if (started || disposed) return
        started = true
        spawnGeneration()
    }

    /** 鉴权恢复后重新启动连接(与 start 等价;语义显式) */
    fun resume() {
        if (disposed) return
        started = false
        _authBlocked.value = false
        start()
    }

    /** 释放全部资源:当前代际拆掉、退避取消、内部 scope 取消(幂等) */
    fun dispose() {
        if (disposed) return
        disposed = true
        live?.teardown()
        scope.cancel()
    }

    // ───────────────────────── 逻辑流:开 / 关 ─────────────────────────

    /**
     * 打开(或复用)某会话的 follow 流。
     *
     * 幂等:同地址的活跃流直接复用;已终止的流会被替换。流一旦打开,
     * 帧会持续进入 [sessionFrames](带会话归属),调用方不必自己收。
     * @return 流句柄(null = 当前没有活跃代际);[DshStream.failure] 非 null 即已终止,
     *   重新调用本方法即可重开
     */
    fun followSession(address: SessionAddress, maxMessages: Int? = null): FollowStream? {
        val gen = live ?: return null
        val key = addressKey(address)
        gen.followStreams[key]?.let { handle ->
            if (handle.stream.failure.value == null) return handle.stream
            handle.pumpJob.cancel()
            handle.collectorJob.cancel()
            gen.followStreams.remove(key)
        }
        val request = SessionFollowRequest(address = address, maxMessages = maxMessages, assistantStream = true)
        val args = buildJsonObject {
            put("request", DshJson.encodeToJsonElement(SessionFollowRequest.serializer(), request))
        }
        val stream = gen.openFollow(args) ?: return null
        val pumpJob = scope.launch {
            try {
                stream.pump()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                reportProtocolError(CarrierException("follow pump failed: " + e.message.orEmpty(), cause = e))
            }
        }
        val collectorJob = scope.launch {
            stream.frames.collect { frame ->
                _sessionFrames.tryEmit(SessionFrameEvent(sessionIdOf(address), address, frame))
            }
        }
        gen.followStreams[key] = FollowHandle(stream, pumpJob, collectorJob)
        return stream
    }

    /** 关闭某会话的 follow 流(切会话时调用;幂等) */
    fun unfollowSession(address: SessionAddress) {
        val gen = live ?: return
        gen.followStreams.remove(addressKey(address))?.let { handle ->
            handle.pumpJob.cancel()
            handle.collectorJob.cancel()
            handle.stream.close()
        }
    }

    /** 该地址的流状态:null 表示从未打开或仍活跃;非 null 表示已终止及原因 */
    fun followStatus(address: SessionAddress): StreamStatus? {
        val gen = live ?: return StreamStatus("down", "connection down")
        val handle = gen.followStreams[addressKey(address)] ?: return null
        val failure = handle.stream.failure.value ?: return null
        return StreamStatus(failure.code, failure.message)
    }

    // ───────────────────────── 代际握手 ─────────────────────────

    private fun wsUri(path: String): String {
        val wsBase = when {
            baseUri.startsWith("https://") -> "wss://" + baseUri.removePrefix("https://")
            baseUri.startsWith("http://") -> "ws://" + baseUri.removePrefix("http://")
            else -> "ws://$baseUri"
        }
        return wsBase + path
    }

    /**
     * 起一代:mux 打开 + `$events` 收到 ready + 一元探针成功,三者齐备才算 ready。
     * 旧版的 `host.describe` 探针换成 `session/modelCatalog`(顺带拿到默认 provider/model)。
     */
    private fun spawnGeneration() {
        if (disposed) return
        generation += 1
        val gen = generation
        emit(ConnectionSnapshot(generation = gen, phase = ConnectionPhase.Connecting))
        val liveGen = LiveGeneration(gen)
        live = liveGen

        scope.launch {
            val legErrors = CopyOnWriteArrayList<Throwable>()
            try {
                val mux = track(legErrors) {
                    Downlink.connect(
                        name = "mux",
                        okHttpClient = wsClient,
                        url = wsUri(DshEndpoints.MUX_PATH),
                        headers = authHeaders?.invoke().orEmpty(),
                        cipher = payloadCipher,
                        onProtocolError = ::reportProtocolError
                    )
                }
                if (disposed || generation != gen) {
                    mux.close()
                    return@launch
                }
                liveGen.adopt(mux)

                // 就绪握手:$events ready
                track(legErrors) { liveGen.installEvents(mux) }
                // 一元探针(顺带取默认模型)
                val catalog = track(legErrors) { fetchModelCatalog() }

                if (disposed || generation != gen) {
                    mux.close()
                    return@launch
                }
                liveGen.openControlAndWorkspace(::reportProtocolError)
                attempt = 0
                emit(
                    ConnectionSnapshot(
                        generation = gen,
                        phase = ConnectionPhase.Ready,
                        describe = HostInfo(
                            version = WireCompat.VERIFIED_HARNESS_VERSION,
                            home = liveGen.events?.home,
                            provider = catalog.default?.provider,
                            model = catalog.default?.model,
                            canOpenPath = liveGen.events?.home != null
                        )
                    )
                )
                liveGen.watchInvalidations { reason -> invalidate(gen, reason) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                liveGen.teardown()
                invalidate(gen, "handshake failed: " + e.message.orEmpty(), legErrors)
            }
        }
    }

    /** 一元探针:主机是否真正可用(顺带取默认模型与可路由 provider) */
    private suspend fun fetchModelCatalog(): ModelCatalog {
        apiClient.setBaseUri(baseUri)
        val value = apiClient.callRemote(DshEndpoints.SESSION_MODEL_CATALOG, JsonObject(emptyMap()), probeTimeout)
        return DshJson.decodeFromJsonElement(ModelCatalog.serializer(), value)
    }

    private suspend fun <T> track(legErrors: MutableList<Throwable>, block: suspend () -> T): T {
        try {
            return block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            legErrors.add(e)
            throw e
        }
    }

    /** 代际失效:拆残留、判 401、发 down、退避重建 */
    private fun invalidate(gen: Int, reason: String, legErrors: List<Throwable> = emptyList()) {
        if (disposed || generation != gen) return
        val old = live
        if (old?.generation == gen) {
            scope.launch { old.teardown() }
        }
        if (isAuthRejection(reason, legErrors)) {
            _authBlocked.value = true
            emit(ConnectionSnapshot(generation = gen, phase = ConnectionPhase.Down, failureReason = "unauthorized"))
            return
        }
        emit(ConnectionSnapshot(generation = gen, phase = ConnectionPhase.Down, failureReason = reason))
        val backoff = nextBackoff()
        scope.launch {
            delay(backoff)
            spawnGeneration()
        }
    }

    /** 网关 401 判定:任一腿的 CarrierException 带 httpStatus 401,或失败串含 401 */
    private fun isAuthRejection(reason: String, legErrors: List<Throwable>): Boolean {
        for (e in legErrors) {
            if (e is CarrierException && e.httpStatus == HttpUnauthorized) return true
        }
        return reason.contains("http $HttpUnauthorized")
    }

    private fun nextBackoff(): Duration {
        if (attempt > MaxBackoffShift) attempt = MaxBackoffShift
        val ms = initialBackoff.inWholeMilliseconds * 2.0.pow(attempt).toLong()
        attempt += 1
        return min(ms, maxBackoff.inWholeMilliseconds).milliseconds
    }

    private fun emit(snapshot: ConnectionSnapshot) {
        _snapshots.value = snapshot
    }

    private fun reportProtocolError(error: CarrierException) {
        _protocolErrors.tryEmit(error)
    }

    private fun sessionIdOf(address: SessionAddress): String = when (address) {
        is SessionAddress.Main -> address.sessionId
        is SessionAddress.Subagent -> address.childSessionId
    }

    private fun addressKey(address: SessionAddress): String = when (address) {
        is SessionAddress.Main -> "session:" + address.sessionId
        is SessionAddress.Subagent -> "subagent:" + address.parentSessionId + ":" + address.childSessionId
    }

    /** 当前代际持有的全部流与收集协程(整代重建时统一拆掉) */
    private inner class LiveGeneration(val generation: Int) {
        private var mux: Downlink? = null
        private val collectors = CopyOnWriteArrayList<Job>()
        val followStreams = ConcurrentHashMap<String, FollowHandle>()

        @Volatile var events: EventStream? = null
            private set

        private var controlStream: ControlStream? = null
        private var workspaceStream: WorkspaceStream? = null

        fun adopt(link: Downlink) {
            mux = link
        }

        fun muxOrNull(): Downlink? = mux

        /** 建 `$events` 流并等首帧 ready(0.1.5 的就绪握手核心) */
        suspend fun installEvents(link: Downlink) {
            val stream = EventStream(link, link.openStream(DshEndpoints.EVENT_STREAM), ::reportProtocolError)
            events = stream
            val ready = CompletableDeferred<Unit>()
            collectors.add(
                scope.launch {
                    stream.frames.collect { frame ->
                        if (frame is RemoteEventFrame.Ready && !ready.isCompleted) ready.complete(Unit)
                        _eventFrames.tryEmit(frame)
                    }
                }
            )
            collectors.add(scope.launch { stream.pump() })
            collectors.add(
                scope.launch {
                    stream.failure.collect { failure ->
                        if (failure != null && !ready.isCompleted) {
                            ready.completeExceptionally(
                                CarrierException("events stream failed: " + failure.code + " " + failure.message)
                            )
                        }
                    }
                }
            )
            try {
                withTimeout(ProbeTimeoutMs) { ready.await() }
            } catch (e: TimeoutCancellationException) {
                throw CarrierException("timeout waiting for events ready")
            }
        }

        fun openFollow(args: JsonObject): FollowStream? {
            val link = mux ?: return null
            return FollowStream(link, link.openStream(DshEndpoints.SESSION_FOLLOW, args), ::reportProtocolError)
        }

        /** 控制面与工作区流:整代各开一次;baseline 展开成等价的增量帧 */
        fun openControlAndWorkspace(onProtocolError: (CarrierException) -> Unit) {
            val link = mux ?: return
            val control = ControlStream(link, link.openStream(DshEndpoints.SESSION_CONTROL), onProtocolError)
            controlStream = control
            collectors.add(scope.launch { control.pump() })
            collectors.add(
                scope.launch {
                    control.frames.collect { frame ->
                        when (frame) {
                            is ControlFrame.Baseline -> {
                                frame.value.queues.forEach { (sid, items) ->
                                    _controlFrames.tryEmit(ControlFrameEvent(sid, ControlFrame.Queue(sid, items)))
                                }
                                frame.value.jobs.forEach { (sid, jobs) ->
                                    _controlFrames.tryEmit(ControlFrameEvent(sid, ControlFrame.Jobs(sid, jobs)))
                                }
                                frame.value.projections.forEach { (sid, block) ->
                                    block.values.forEach { (key, value) ->
                                        _controlFrames.tryEmit(
                                            ControlFrameEvent(
                                                sid,
                                                ControlFrame.Projection(sid, key, value, block.asOfSeq)
                                            )
                                        )
                                    }
                                }
                            }
                            is ControlFrame.Queue ->
                                _controlFrames.tryEmit(ControlFrameEvent(frame.sessionId, frame))

                            is ControlFrame.Jobs ->
                                _controlFrames.tryEmit(ControlFrameEvent(frame.sessionId, frame))

                            is ControlFrame.Projection ->
                                _controlFrames.tryEmit(ControlFrameEvent(frame.sessionId, frame))
                        }
                    }
                }
            )
            val workspace = WorkspaceStream(link, link.openStream(DshEndpoints.WORKSPACE_FOLLOW), onProtocolError)
            workspaceStream = workspace
            collectors.add(scope.launch { workspace.pump() })
            collectors.add(scope.launch { workspace.frames.collect { _workspaceFrames.tryEmit(it) } })
        }

        /** 物理连接断开即整代失效 */
        fun watchInvalidations(onInvalid: (String) -> Unit) {
            val link = mux ?: return
            collectors.add(
                scope.launch {
                    link.awaitDone()
                    onInvalid(link.failure?.reason ?: "mux socket closed")
                }
            )
        }

        fun teardown() {
            collectors.forEach { it.cancel() }
            collectors.clear()
            followStreams.values.forEach {
                it.pumpJob.cancel()
                it.collectorJob.cancel()
            }
            followStreams.clear()
            controlStream?.close()
            workspaceStream?.close()
            events?.close()
            mux?.close()
            mux = null
        }
    }

    /** 一条会话流的句柄:流对象 + pump/收集协程 */
    private class FollowHandle(
        val stream: FollowStream,
        val pumpJob: Job,
        val collectorJob: Job
    )

    companion object {
        /**
         * 主机地址归一化:去空白/尾斜杠,缺 scheme 补 http://。
         * 供组件层在起连接前算出稳定 baseUri(主机簿 id 也用它做键)。
         */
        fun normalizeBaseUri(address: String): String {
            var normalized = address.trim().trimEnd('/')
            require(normalized.isNotEmpty()) { "empty host address" }
            if (!normalized.contains("://")) normalized = "http://" + normalized
            return normalized
        }

        private const val WsPingIntervalSeconds = 20L
        private const val FrameBufferCapacity = 256
        private const val MaxBackoffShift = 6
        private const val HttpUnauthorized = 401
        private const val ProbeTimeoutMs = 10_000L
    }
}
