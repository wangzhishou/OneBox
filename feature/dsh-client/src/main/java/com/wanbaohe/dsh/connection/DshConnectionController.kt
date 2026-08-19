package com.wanbaohe.dsh.connection

import com.wanbaohe.dsh.wire.AddressedMuxFrame
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.HostFrame
import com.wanbaohe.dsh.wire.MuxFrame
import com.wanbaohe.dsh.wire.model.HostInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.OkHttpClient
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
 * DSH 连接控制器(DSH-PROTOCOL §2,整代重建状态机)。
 *
 * 语义:
 * - 就绪握手 = 两条 WS(/api/events.mux、/api/events.host)都打开 **且** host.describe 成功,
 *   三者并发、缺一不可;任一失败全部拆掉退避重试
 * - 任一 socket 断 → 当前代际失效 → 整代重建(新代两条流 + 重新握手);无 since 续传
 * - 指数退避 300ms → 8s
 * - 畸形帧只上报([protocolErrors])不杀 socket
 * - 网关 401(任一腿 [CarrierException.httpStatus] == 401)→ [authBlocked] = true,
 *   停退避等重新登录,[resume] 恢复
 *
 * 生命周期与组件绑定:每个连接实例独立(不做 @Singleton),由组件层创建并 [dispose]。
 */
class DshConnectionController(
    private val okHttpClient: OkHttpClient,
    private val apiClient: DshApiClient,
    baseUri: String,
    /** 鉴权头钩子(P6 接令牌;远程网关形态两条 WS + 全部 /api HTTP 携带 Authorization) */
    private val authHeaders: (() -> Map<String, String>)? = null,
    /** E2E 载荷加解密(云端中继持有密钥时):/api HTTP 体加解密 + 下行 WS 帧解密 */
    private val payloadCipher: E2ECipher? = null,
    /** 网关令牌形态(已鉴权远程)= true → 特权面可见(DSH-PROTOCOL §6 信任围栏) */
    authenticatedRemote: Boolean = false,
    /**
     * 下行 WS 路径前缀:默认 "/api"(DSH 直连/网关形态,…/api/events.mux);
     * 云端中继(strapi_go dshrelay)的 WS 桥不带 /api 段(…/dsh/relay/events.mux),传 ""。
     */
    private val wsPathPrefix: String = "/api",
    private val initialBackoff: Duration = 300.milliseconds,
    private val maxBackoff: Duration = 8.seconds,
    private val probeTimeout: Duration = 10.seconds
) {

    /** 归一化后的主机地址(含 scheme、无尾斜杠) */
    private val baseUri: String = normalizeBaseUri(baseUri)

    /** 特权面可见性(DSH-PROTOCOL §6):loopback 全开 / LAN 围栏 / 网关已鉴权远程全开 */
    val privilegeScope: PrivilegeScope = PrivilegeScope.of(baseUri, authenticatedRemote)

    init {
        // HTTP 腿(/api/*、respond、export)共用同一令牌钩子;令牌原地刷新即时生效
        apiClient.authHeadersProvider = authHeaders
        // E2E:HTTP 体加解密与 WS 帧解密共用同一密钥(本实例持有,null = 明文)
        apiClient.payloadCipher = payloadCipher
    }

    /** WS 专用 client:协议级 ping 保活 20s(代理对空闲 WS 普遍有读超时) */
    private val wsClient: OkHttpClient =
        okHttpClient.newBuilder().pingInterval(WsPingIntervalSeconds, TimeUnit.SECONDS).build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _snapshots = MutableStateFlow(ConnectionSnapshot(generation = 0, phase = ConnectionPhase.Down))
    val snapshots: StateFlow<ConnectionSnapshot> = _snapshots.asStateFlow()

    private val _muxFrames = MutableSharedFlow<MuxFrame>(
        extraBufferCapacity = FrameBufferCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    /** 当前代际的 mux 帧(重连后自动切换到新一代的帧) */
    val muxFrames: SharedFlow<MuxFrame> = _muxFrames.asSharedFlow()

    private val _hostFrames = MutableSharedFlow<HostFrame>(
        extraBufferCapacity = FrameBufferCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    /** 当前代际的 host 帧 */
    val hostFrames: SharedFlow<HostFrame> = _hostFrames.asSharedFlow()

    private val _addressedMuxFrames = MutableSharedFlow<AddressedMuxFrame>(
        extraBufferCapacity = FrameBufferCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    /** 可应答帧(rpcId 来自信封,payload 为 MuxFrame):审批/问答 UI 的数据源 */
    val addressedMuxFrames: SharedFlow<AddressedMuxFrame> = _addressedMuxFrames.asSharedFlow()

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

    private fun emit(snapshot: ConnectionSnapshot) {
        _snapshots.value = snapshot
    }

    private fun reportProtocolError(error: CarrierException) {
        _protocolErrors.tryEmit(error)
    }

    private fun wsUri(path: String): String {
        val wsBase = when {
            baseUri.startsWith("https://") -> "wss://" + baseUri.removePrefix("https://")
            baseUri.startsWith("http://") -> "ws://" + baseUri.removePrefix("http://")
            else -> "ws://$baseUri"
        }
        return wsBase + path
    }

    /** 起一代:并发 describe + 两条 WS,三路全成才 ready */
    private fun spawnGeneration() {
        if (disposed) return
        generation += 1
        val gen = generation
        emit(ConnectionSnapshot(generation = gen, phase = ConnectionPhase.Connecting))
        val liveGen = LiveGeneration(gen)
        live = liveGen

        scope.launch {
            // 各腿错误旁路登记:先到错会取消其余腿,晚到的 401(如 describe)
            // 会被吞掉 —— 网关 401 判定必须看全部腿
            val legErrors = CopyOnWriteArrayList<Throwable>()
            try {
                val result = coroutineScope {
                    val describeDeferred = async { track(legErrors) { fetchDescribe() } }
                    val muxDeferred = async {
                        track(legErrors) {
                            Downlink.connect(
                                name = "mux",
                                okHttpClient = wsClient,
                                url = wsUri("$wsPathPrefix/events.mux"),
                                headers = authHeaders?.invoke().orEmpty(),
                                cipher = payloadCipher,
                                onProtocolError = ::reportProtocolError
                            )
                        }
                    }
                    val hostDeferred = async {
                        track(legErrors) {
                            Downlink.connect(
                                name = "host",
                                okHttpClient = wsClient,
                                url = wsUri("$wsPathPrefix/events.host"),
                                headers = authHeaders?.invoke().orEmpty(),
                                cipher = payloadCipher,
                                onProtocolError = ::reportProtocolError
                            )
                        }
                    }
                    Triple(describeDeferred.await(), muxDeferred.await(), hostDeferred.await())
                }
                val (describe, mux, host) = result
                if (disposed || generation != gen) {
                    // superseded / disposed:新腿白开了,直接拆掉
                    mux.close()
                    host.close()
                    return@launch
                }
                liveGen.adopt(mux, host)
                attempt = 0
                emit(ConnectionSnapshot(generation = gen, phase = ConnectionPhase.Ready, describe = describe))
                liveGen.watchInvalidations { reason -> invalidate(gen, reason) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                // 握手失败:两条 socket 都可能开了一半,全部拆掉,退避重试
                liveGen.teardown()
                invalidate(gen, "handshake failed: ${e.message}", legErrors)
            }
        }
    }

    private suspend fun fetchDescribe(): HostInfo {
        apiClient.setBaseUri(baseUri)
        val value = apiClient.call(HostDescribeMethod, JsonObject(emptyMap()), timeout = probeTimeout)
        return DshJson.decodeFromJsonElement(HostInfo.serializer(), value)
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
        // 拆掉当前代的残留资源(另一腿可能还开着);异步执行避免自取消
        val old = live
        if (old?.generation == gen) {
            scope.launch { old.teardown() }
        }
        // 网关 401:令牌被拒,退避重试没有意义 —— 停循环等重新登录
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

    /** 下行原始帧 → 信封剥离;信封级畸形只上报,返回 null */
    private fun parseEnvelope(raw: JsonObject): JsonObject? {
        val type = (raw["type"] as? JsonPrimitive)?.contentOrNull
        if (type != ServerRequestType) {
            reportProtocolError(CarrierException("downlink envelope is $type"))
            return null
        }
        val payload = raw["payload"] as? JsonObject
        if (payload == null) {
            reportProtocolError(CarrierException("downlink payload not an object"))
            return null
        }
        return payload
    }

    private fun onMuxRaw(raw: JsonObject) {
        val rpcId = (raw["rpcId"] as? JsonPrimitive)?.contentOrNull.orEmpty()
        val payload = parseEnvelope(raw) ?: return
        try {
            val frame = DshJson.decodeFromJsonElement(MuxFrame.serializer(), payload)
            _muxFrames.tryEmit(frame)
            _addressedMuxFrames.tryEmit(AddressedMuxFrame(rpcId = rpcId, frame = frame))
        } catch (e: Exception) {
            reportProtocolError(CarrierException("mux frame parse: ${e.message}", cause = e))
        }
    }

    private fun onHostRaw(raw: JsonObject) {
        val payload = parseEnvelope(raw) ?: return
        try {
            _hostFrames.tryEmit(DshJson.decodeFromJsonElement(HostFrame.serializer(), payload))
        } catch (e: Exception) {
            reportProtocolError(CarrierException("host frame parse: ${e.message}", cause = e))
        }
    }

    /** 一次代际的活体资源:两条 socket + 帧收集/断线监听 job */
    private inner class LiveGeneration(val generation: Int) {
        private var mux: Downlink? = null
        private var host: Downlink? = null
        private val watchers = mutableListOf<Job>()
        @Volatile private var tearingDown = false
        private var onInvalidated: ((String) -> Unit)? = null

        fun adopt(mux: Downlink, host: Downlink) {
            this.mux = mux
            this.host = host
            watchers += scope.launch { mux.rawFrames.collect(::onMuxRaw) }
            watchers += scope.launch { mux.awaitDone(); notifyInvalid("mux down") }
            watchers += scope.launch { host.rawFrames.collect(::onHostRaw) }
            watchers += scope.launch { host.awaitDone(); notifyInvalid("host down") }
        }

        fun watchInvalidations(callback: (String) -> Unit) {
            onInvalidated = callback
        }

        private fun notifyInvalid(reason: String) {
            if (tearingDown) return
            onInvalidated?.invoke(reason)
        }

        /** 拆代:取消全部收集/监听 job 并关两条 socket(幂等) */
        fun teardown() {
            if (tearingDown) return
            tearingDown = true
            watchers.forEach { it.cancel() }
            watchers.clear()
            mux?.close()
            host?.close()
        }
    }

    companion object {
        private const val HostDescribeMethod = "host.describe"
        private const val ServerRequestType = "server-request"
        private const val HttpUnauthorized = 401
        private const val WsPingIntervalSeconds = 20L
        private const val FrameBufferCapacity = 64
        private const val MaxBackoffShift = 20

        /** 归一化主机地址:可不带 scheme(默认补 http://)、去尾斜杠 */
        fun normalizeBaseUri(address: String): String {
            var normalized = address.trim().trimEnd('/')
            require(normalized.isNotEmpty()) { "empty host address" }
            if (!normalized.contains("://")) normalized = "http://$normalized"
            return normalized
        }
    }
}
