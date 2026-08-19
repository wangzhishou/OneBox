package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.HostFrame
import com.wanbaohe.dsh.wire.MuxFrame
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.model.HistoryEntry
import com.wanbaohe.dsh.wire.model.ImageLimitsProjection
import com.wanbaohe.dsh.wire.model.SessionCreateValue
import com.wanbaohe.dsh.wire.model.SessionEvent
import com.wanbaohe.dsh.wire.model.SessionForkValue
import com.wanbaohe.dsh.wire.model.SessionHistoryValue
import com.wanbaohe.dsh.wire.model.SessionListValue
import com.wanbaohe.dsh.wire.model.SessionModelsValue
import com.wanbaohe.dsh.wire.model.SessionProjectionsBlock
import com.wanbaohe.dsh.wire.model.SessionPromptRequest
import com.wanbaohe.dsh.wire.model.SessionPromptValue
import com.wanbaohe.dsh.wire.model.SessionRenameValue
import com.wanbaohe.dsh.wire.model.SessionSearchValue
import com.wanbaohe.dsh.wire.model.SessionSelectModelValue
import com.wanbaohe.dsh.wire.model.SessionSummary
import com.wanbaohe.dsh.wire.model.userSourceKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.util.TimeZone
import kotlin.time.Duration.Companion.seconds

/**
 * 单会话事件日志(对齐 Flutter SessionLog):
 * seq 去重有序插入(重连重放天然安全)+ 投影水位 + hasOlder 翻页锚点。
 */
class SessionLog(val sessionId: String) {

    private val _events = MutableStateFlow<List<SessionEvent>>(emptyList())
    /** 当前日志快照流(seq 升序) */
    val events: StateFlow<List<SessionEvent>> = _events.asStateFlow()

    private val seenSeqs = HashSet<Int>()
    private val ordered = ArrayList<SessionEvent>()

    /** 主机算好的工具渲染意图(seq → view;实时 mux 帧 / 历史页条目携带,不落盘) */
    private val viewBySeq = HashMap<Int, JsonElement>()

    /** 取某事件的渲染意图(缺席返回 null,节点提取退化为防御式 data 提取) */
    fun viewFor(seq: Int): JsonElement? = viewBySeq[seq]

    /** 投影单元值(高 seq 覆盖低 seq;标题走此通道) */
    val projections = mutableMapOf<String, JsonElement>()

    /** 投影水位(日志级;overlay 侧另有 per-key seq) */
    var projectionWatermark = -1

    private val _hasOlder = MutableStateFlow(false)
    /** 服务端还有更早历史(loadOlder 入口) */
    val hasOlder: StateFlow<Boolean> = _hasOlder.asStateFlow()

    /** 已装载的最早 seq(loadOlder 的 beforeSeq 锚点) */
    val earliestLoadedSeq: Int? get() = ordered.firstOrNull()?.seq

    /** 按 seq 去重追加;返回是否真追加 */
    fun append(event: SessionEvent, view: JsonElement? = null): Boolean {
        if (view != null) viewBySeq[event.seq] = view
        if (!seenSeqs.add(event.seq)) return false
        insertOrdered(event)
        _events.value = ordered.toList()
        return true
    }

    /** 批量追加(历史页装载):seq 去重后只发一次快照,避免逐条全屏重组 */
    fun appendAll(entries: List<HistoryEntry>): Int {
        var added = 0
        for (entry in entries) {
            entry.view?.let { viewBySeq[entry.event.seq] = it }
            if (!seenSeqs.add(entry.event.seq)) continue
            insertOrdered(entry.event)
            added++
        }
        if (added > 0) _events.value = ordered.toList()
        return added
    }

    private fun insertOrdered(event: SessionEvent) {
        var at = ordered.size
        while (at > 0 && ordered[at - 1].seq > event.seq) at--
        ordered.add(at, event)
    }

    /** 投影覆盖:高 seq 赢,低/同 seq 丢弃 */
    fun applyProjection(key: String, value: JsonElement, seq: Int) {
        if (seq < projectionWatermark) return
        projectionWatermark = seq
        projections[key] = value
    }

    fun setHasOlder(value: Boolean) {
        _hasOlder.value = value
    }
}

/**
 * 会话领域状态(对齐 Flutter session_store.dart,DSH-PROTOCOL §5)。
 *
 * - 代际 ready → 全量重取 session.list(无 since 续传);已积累日志按 seq 去重保留
 * - 同一时刻只允许一次 session.list 在飞(并发合并);拉取期间到达的变更帧登记重放
 * - 投影 overlay 四路汇入(list 行内基线 / history 尾页块 / projection 推送帧),
 *   单一规则「高 seq 覆盖低 seq」;list 行块是部分基线,缺席键不清 overlay
 * - 日志懒注册:只向「已打开」的会话日志投递 mux 事件(防内存无界增长)
 * - host/session-added、session-removed、session-status 折叠进摘要列表
 *
 * 生命周期与连接实例绑定:由组件层创建并 [dispose](不做 @Singleton)。
 */
class SessionStore(
    private val api: DshApiClient,
    private val connection: DshConnectionController,
    parentScope: CoroutineScope
) {

    /** 子 scope:dispose 只取消自己,不动组件 scope */
    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )

    private val _summaries = MutableStateFlow<List<SessionSummary>>(emptyList())
    /** 会话摘要列表(已合并投影 overlay,标题为最新值) */
    val summaries: StateFlow<List<SessionSummary>> = _summaries.asStateFlow()

    private var rawSummaries: List<SessionSummary> = emptyList()
    private val logs = mutableMapOf<String, SessionLog>()

    // 会话投影 overlay:list 行内块 / history 尾页块 / 推送帧四路汇入,高 seq 覆盖低 seq
    private val projectionValues = mutableMapOf<String, MutableMap<String, JsonElement>>()
    private val projectionSeqs = mutableMapOf<String, MutableMap<String, Int>>()

    @Volatile
    private var disposed = false
    private var started = false
    private var lastReadyGeneration = 0

    /** 在飞的 session.list(并发合并:后续 refresh 共享同一次往返) */
    private var refreshJob: Job? = null

    /** 拉取在飞期间到达的变更(响应落地后按序重放,防快照盖回新状态) */
    private val pendingMutations = mutableListOf<() -> Unit>()

    fun start() {
        if (started) return
        started = true
        scope.launch {
            connection.snapshots.collect { snapshot ->
                // 重连 = 全量重取(StateFlow 回放在此被代际号去重);失败由下一代际重试
                if (!disposed &&
                    snapshot.phase == ConnectionPhase.Ready &&
                    snapshot.generation > lastReadyGeneration
                ) {
                    lastReadyGeneration = snapshot.generation
                    refresh()
                }
            }
        }
        scope.launch { connection.muxFrames.collect(::onMuxFrame) }
        scope.launch { connection.hostFrames.collect(::onHostFrame) }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 取(或建)某会话的日志;UI 打开会话时调用即完成「懒注册」 */
    fun logFor(sessionId: String): SessionLog =
        logs.getOrPut(sessionId) { SessionLog(sessionId) }

    /** 全量重取会话列表;在飞时返回同一个 Job(共享往返) */
    fun refresh(): Job? {
        if (disposed) return null
        refreshJob?.let { if (it.isActive) return it }
        val job = scope.launch {
            try {
                doRefresh()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                // 失败的拉取不落地基线:登记变更作废(帧到达时已直接折叠),下一代际重试
                pendingMutations.clear()
            } finally {
                refreshJob = null
            }
        }
        refreshJob = job
        return job
    }

    private suspend fun doRefresh() {
        val value = api.call(RpcSessionList, buildJsonObject {})
        val parsed = DshJson.decodeFromJsonElement<SessionListValue>(value)
        if (disposed) return
        rawSummaries = parsed.items
        // 行内投影基线 seed 进 overlay(部分基线:可能滞后但不错,asOfSeq 标明多旧)
        val alive = HashSet<String>()
        for (summary in parsed.items) {
            alive.add(summary.sessionId)
            val block = summary.projections ?: continue
            for ((key, item) in block.values) {
                applyProjectionValue(summary.sessionId, key, item, block.asOfSeq)
            }
        }
        // 已消失会话的 overlay 行回收(防长期增长)
        projectionValues.keys.retainAll(alive)
        projectionSeqs.keys.retainAll(alive)
        // 基线落地 → 重放拉取期间到达的变更;先清在飞标记,重放不得二次登记
        refreshJob = null
        val pending = pendingMutations.toList()
        pendingMutations.clear()
        pending.forEach { it.invoke() }
        emitSummaries()
    }

    /** 装载历史尾页(beforeSeq 缺席 = 最新一页,附 projections 水位快照);幂等 */
    suspend fun loadHistory(sessionId: String, maxMessages: Int = DefaultPageSize) {
        fetchPage(sessionId, logFor(sessionId), maxMessages, beforeSeq = null)
    }

    /** 向前补一页更早历史(无更早时 no-op;幂等) */
    suspend fun loadOlder(sessionId: String, maxMessages: Int = DefaultPageSize) {
        val log = logFor(sessionId)
        val earliest = log.earliestLoadedSeq
        if (!log.hasOlder.value || earliest == null) return
        fetchPage(sessionId, log, maxMessages, beforeSeq = earliest)
    }

    /**
     * 拉单页并落地,返回服务端 hasMore(空页视作无更多)。
     * 超时/载波故障退避重试(共 3 次);业务错误(session-not-found 等)直接上抛。
     */
    private suspend fun fetchPage(
        sessionId: String,
        log: SessionLog,
        maxMessages: Int,
        beforeSeq: Int?
    ): Boolean {
        val payload = buildJsonObject {
            put("sessionId", sessionId)
            put("maxMessages", maxMessages)
            beforeSeq?.let { put("beforeSeq", it) }
        }
        var lastError: Throwable? = null
        for (attempt in 0 until HistoryAttempts) {
            try {
                val value = api.call(RpcSessionHistory, payload, timeout = HistoryTimeout)
                val parsed = DshJson.decodeFromJsonElement<SessionHistoryValue>(value)
                log.appendAll(parsed.events)
                val block = parsed.projections
                var overlayChanged = false
                if (block != null) {
                    for ((key, item) in block.values) {
                        log.projections[key] = item
                        // 尾页块同样 seed overlay:打开冷会话,侧栏标题对齐持久化缓存值
                        if (applyProjectionValue(sessionId, key, item, block.asOfSeq)) {
                            overlayChanged = true
                        }
                    }
                    // 尾页块是全量基线:块中缺席且 seq 不高于切面的 overlay 键清除(防幻影键)
                    val valuesMap = projectionValues[sessionId]
                    val seqsMap = projectionSeqs[sessionId]
                    if (valuesMap != null && seqsMap != null) {
                        val dead = valuesMap.keys.filter { key ->
                            !block.values.containsKey(key) && (seqsMap[key] ?: -1) <= block.asOfSeq
                        }
                        for (key in dead) {
                            valuesMap.remove(key)
                            seqsMap.remove(key)
                            overlayChanged = true
                        }
                    }
                    if (block.asOfSeq > log.projectionWatermark) {
                        log.projectionWatermark = block.asOfSeq
                    }
                }
                if (overlayChanged) emitSummaries()
                val hasOlder = parsed.hasMore && parsed.events.isNotEmpty()
                log.setHasOlder(hasOlder)
                return hasOlder
            } catch (e: CancellationException) {
                throw e
            } catch (e: RpcBusinessException) {
                throw e
            } catch (e: Throwable) {
                lastError = e
            }
            if (attempt < HistoryAttempts - 1) delay(HistoryBackoffMs[attempt])
        }
        throw lastError ?: CarrierException("session.history 重试耗尽")
    }

    /**
     * session.create:workspaceId 与 cwd 至多一个(双侧都发服务端会拒)。
     * 创建后登记日志 + 合成 upsert(在飞拉取快照可能早于本次创建),并触发 refresh。
     */
    suspend fun createSession(
        workspaceId: String? = null,
        cwd: String? = null,
        agentPreset: String? = null
    ): SessionCreateValue {
        val payload = buildJsonObject {
            workspaceId?.let { put("workspaceId", it) }
            cwd?.let { put("cwd", it) }
            agentPreset?.let { put("agentPreset", it) }
        }
        val value = DshJson.decodeFromJsonElement<SessionCreateValue>(
            api.call(RpcSessionCreate, payload)
        )
        logFor(value.sessionId)
        recordMutation {
            mergeAddedFields(
                sessionId = value.sessionId,
                blank = true,
                cwd = cwd,
                agentPreset = value.agentPreset
            )
        }
        refresh()
        return value
    }

    /**
     * 发送 prompt([mode]:queue 排队 / steer 插话进运行中轮次;clientTimeZone IANA;
     * rpcId 会进入 user/message 事件)。带图片时先做本地预拒(imageLimits 投影缺席
     * 则跳过预检,服务端权威),content 为文本块 + base64 图片块(DSH-PROTOCOL §7)。
     * steer 是 UI 前置语义,服务端仍可拒(steer-unavailable / agent-busy 上抛)。
     */
    suspend fun prompt(
        sessionId: String,
        text: String,
        images: List<PendingImage> = emptyList(),
        mode: String = PromptModeQueue
    ): SessionPromptValue {
        if (images.isNotEmpty()) {
            attachmentLimitsFor(sessionId)?.let { limits ->
                validateImages(images, limits)?.let { throw AttachmentRejectException(it) }
            }
        }
        val request = SessionPromptRequest(
            sessionId = sessionId,
            mode = mode,
            content = buildPromptContent(text, images),
            clientTimeZone = TimeZone.getDefault().id
        )
        val payload = DshJson.encodeToJsonElement(request).jsonObject
        return DshJson.decodeFromJsonElement(api.call(RpcSessionPrompt, payload))
    }

    /** session.models:目录 + 当前选择 + routable(prompt 前不可路由 → model-unavailable) */
    suspend fun sessionModels(sessionId: String): SessionModelsValue {
        val payload = buildJsonObject { put("sessionId", sessionId) }
        return DshJson.decodeFromJsonElement(api.call(RpcSessionModels, payload))
    }

    /** session.selectModel:选择可与目录成员无关(服务端语义) */
    suspend fun selectModel(
        sessionId: String,
        provider: String,
        model: String,
        reasoningEffort: String? = null
    ): SessionSelectModelValue {
        val payload = buildJsonObject {
            put("sessionId", sessionId)
            put("provider", provider)
            put("model", model)
            reasoningEffort?.let { put("reasoningEffort", it) }
        }
        return DshJson.decodeFromJsonElement(api.call(RpcSessionSelectModel, payload))
    }

    /** session.search:侧栏搜索(query ≤500 字符,分页 hasMore) */
    suspend fun search(query: String): SessionSearchValue {
        val payload = buildJsonObject { put("query", query) }
        return DshJson.decodeFromJsonElement(api.call(RpcSessionSearch, payload))
    }

    /**
     * session.fork:atSeq 锚点映射到其后第一个 turn/end(turn 未闭合 → fork-unavailable)。
     * fork 后登记日志 + 合成 upsert,并触发 refresh(新会话入列)。
     */
    suspend fun fork(sessionId: String, atSeq: Int? = null): SessionForkValue {
        val payload = buildJsonObject {
            put("sessionId", sessionId)
            atSeq?.let { put("atSeq", it) }
        }
        val value = DshJson.decodeFromJsonElement<SessionForkValue>(
            api.call(RpcSessionFork, payload)
        )
        logFor(value.sessionId)
        recordMutation {
            mergeAddedFields(sessionId = value.sessionId, blank = true)
        }
        refresh()
        return value
    }

    /**
     * session.rename:响应回带的规范化 title+seq 先落本地格,
     * 推送 session/projection 帧高 seq 覆盖(乱序安全,DSH-PROTOCOL §5/§7)。
     */
    suspend fun rename(sessionId: String, title: String): SessionRenameValue {
        val payload = buildJsonObject {
            put("sessionId", sessionId)
            put("title", title)
        }
        val value = DshJson.decodeFromJsonElement<SessionRenameValue>(
            api.call(RpcSessionRename, payload)
        )
        // 显式用户动作 → 登记日志(懒注册例外);title 投影值是纯字符串
        val projected = JsonPrimitive(value.title)
        logFor(sessionId).applyProjection("title", projected, value.seq)
        if (applyProjectionValue(sessionId, "title", projected, value.seq)) {
            emitSummaries()
        }
        refresh()
        return value
    }

    /** 从日志投影水位取 imageLimits(未装载历史时为 null —— 预拒退化为服务端权威) */
    fun attachmentLimitsFor(sessionId: String): ImageLimitsProjection? {
        val raw = logs[sessionId]?.projections?.get("imageLimits") ?: return null
        return runCatching {
            DshJson.decodeFromJsonElement(ImageLimitsProjection.serializer(), raw)
        }.getOrNull()
    }

    // ───────────────────────────── 帧折叠 ─────────────────────────────

    private fun onMuxFrame(frame: MuxFrame) {
        when (frame) {
            is MuxFrame.SessionEvent -> {
                val event = runCatching {
                    DshJson.decodeFromJsonElement(SessionEvent.serializer(), frame.event)
                }.getOrNull() ?: return
                // 懒注册:只向已打开的日志投递;未打开会话的历史打开时按页拉取
                logs[frame.sessionId]?.append(event, frame.view)
                // 活动折叠:任何客户端直发的用户消息推进摘要 updatedAt(侧栏时间数据源)
                if (event.type == EventTypeUserMessage &&
                    event.data.userSourceKind() == SourceKindUser
                ) {
                    bumpActivity(frame.sessionId, event.time)
                }
            }

            is MuxFrame.SessionProjection -> {
                logs[frame.sessionId]?.applyProjection(frame.key, frame.value, frame.seq)
                // overlay 无条件落地:标题投影对整个列表生效(未打开的会话也要演进)
                if (applyProjectionValue(frame.sessionId, frame.key, frame.value, frame.seq)) {
                    emitSummaries()
                }
            }

            // subscribed 水位本阶段不折叠;queue/jobs 快照由 QueueStore 消费(P3)
            else -> Unit
        }
    }

    private fun onHostFrame(frame: HostFrame) {
        when (frame) {
            is HostFrame.SessionStatus -> applyStatusFlip(frame.sessionId, frame.running)
            is HostFrame.SessionAdded -> {
                recordMutation {
                    mergeAddedFields(
                        sessionId = frame.sessionId,
                        blank = frame.blank,
                        parentSessionId = frame.parentSessionId,
                        origin = frame.origin,
                        cwd = frame.cwd,
                        agentPreset = frame.agentPreset
                    )
                }
                mergeAddedFields(
                    sessionId = frame.sessionId,
                    blank = frame.blank,
                    parentSessionId = frame.parentSessionId,
                    origin = frame.origin,
                    cwd = frame.cwd,
                    agentPreset = frame.agentPreset
                )
            }

            is HostFrame.SessionRemoved -> removeSummary(frame.sessionId)
            else -> Unit
        }
    }

    /** running 翻转:running=true 清 blank(首 turn 开跑即非空);同值重放零副作用 */
    private fun applyStatusFlip(sessionId: String, running: Boolean) {
        recordMutation { applyStatusFlip(sessionId, running) }
        val idx = rawSummaries.indexOfFirst { it.sessionId == sessionId }
        if (idx < 0) return
        val old = rawSummaries[idx]
        if (old.running == running && !(running && old.blank)) return
        val next = rawSummaries.toMutableList()
        next[idx] = old.copy(running = running, blank = old.blank && !running)
        rawSummaries = next
        emitSummaries()
    }

    /** added/upsert 折叠:新会话入列;已存在的行只补缺失字段,绝不覆盖 refresh 数据 */
    private fun mergeAddedFields(
        sessionId: String,
        blank: Boolean,
        parentSessionId: String? = null,
        origin: String? = null,
        cwd: String? = null,
        agentPreset: String? = null
    ) {
        val idx = rawSummaries.indexOfFirst { it.sessionId == sessionId }
        if (idx < 0) {
            rawSummaries = listOf(
                SessionSummary(
                    sessionId = sessionId,
                    updatedAt = System.currentTimeMillis().toDouble(),
                    running = false,
                    blank = blank,
                    parentSessionId = parentSessionId,
                    origin = origin,
                    cwd = cwd,
                    agentPreset = agentPreset
                )
            ) + rawSummaries
            emitSummaries()
            return
        }
        val old = rawSummaries[idx]
        val merged = old.copy(
            blank = old.blank && blank,
            parentSessionId = old.parentSessionId ?: parentSessionId,
            origin = old.origin ?: origin,
            cwd = old.cwd ?: cwd,
            agentPreset = old.agentPreset ?: agentPreset
        )
        if (merged == old) return // 竞态后到帧无新信息:零副作用
        val next = rawSummaries.toMutableList()
        next[idx] = merged
        rawSummaries = next
        emitSummaries()
    }

    /** session-removed:subagent 可 resume 只折 running;普通会话移出并回收 overlay */
    private fun removeSummary(sessionId: String) {
        recordMutation { removeSummary(sessionId) }
        val idx = rawSummaries.indexOfFirst { it.sessionId == sessionId }
        if (idx < 0) return
        if (rawSummaries[idx].origin == OriginSubagent) {
            applyStatusFlip(sessionId, false)
            return
        }
        rawSummaries = rawSummaries.toMutableList().also { it.removeAt(idx) }
        projectionValues.remove(sessionId)
        projectionSeqs.remove(sessionId)
        emitSummaries()
    }

    /** 活动时间推进:只前进不回退,重放零副作用 */
    private fun bumpActivity(sessionId: String, time: Double) {
        recordMutation { bumpActivity(sessionId, time) }
        val idx = rawSummaries.indexOfFirst { it.sessionId == sessionId }
        if (idx < 0) return
        val old = rawSummaries[idx]
        if (time <= old.updatedAt) return
        val next = rawSummaries.toMutableList()
        next[idx] = old.copy(updatedAt = time)
        rawSummaries = next
        emitSummaries()
    }

    // ───────────────────────────── 投影 overlay ─────────────────────────────

    /** 变更帧在拉取在飞时登记重放(HTTP 响应慢于 WS 帧时,快照里是旧值) */
    private fun recordMutation(replay: () -> Unit) {
        if (refreshJob?.isActive == true) pendingMutations.add(replay)
    }

    /** 投影单键落地(高 seq 覆盖低 seq);返回是否有键值更新 */
    private fun applyProjectionValue(
        sessionId: String,
        key: String,
        value: JsonElement,
        seq: Int
    ): Boolean {
        val seqs = projectionSeqs.getOrPut(sessionId) { mutableMapOf() }
        if ((seqs[key] ?: -1) >= seq) return false
        seqs[key] = seq
        projectionValues.getOrPut(sessionId) { mutableMapOf() }[key] = value
        return true
    }

    /** 把 overlay 合并进摘要列表(overlay 键无条件胜出行块值,行块是部分基线) */
    private fun projected(items: List<SessionSummary>): List<SessionSummary> {
        if (projectionValues.isEmpty()) return items
        return items.map(::mergeOne)
    }

    private fun mergeOne(summary: SessionSummary): SessionSummary {
        val overlay = projectionValues[summary.sessionId]
        if (overlay.isNullOrEmpty()) return summary
        val seqs = projectionSeqs[summary.sessionId].orEmpty()
        val merged = summary.projections?.values.orEmpty() + overlay
        var asOf = summary.projections?.asOfSeq ?: -1
        for (key in overlay.keys) {
            val seq = seqs[key] ?: -1
            if (seq > asOf) asOf = seq
        }
        return summary.copy(
            projections = SessionProjectionsBlock(asOfSeq = asOf, values = merged)
        )
    }

    private fun emitSummaries() {
        _summaries.value = projected(rawSummaries)
    }

    companion object {
        private const val RpcSessionList = "session.list"
        private const val RpcSessionHistory = "session.history"
        private const val RpcSessionCreate = "session.create"
        private const val RpcSessionPrompt = "session.prompt"
        private const val RpcSessionModels = "session.models"
        private const val RpcSessionSelectModel = "session.selectModel"
        private const val RpcSessionSearch = "session.search"
        private const val RpcSessionFork = "session.fork"
        private const val RpcSessionRename = "session.rename"
        private const val EventTypeUserMessage = "user/message"
        private const val SourceKindUser = "user"
        private const val OriginSubagent = "subagent"
        private const val DefaultPageSize = 50
        private const val HistoryAttempts = 3

        /** session.prompt 的 mode 枚举(DSH-PROTOCOL §9 zod 实证):queue 排队 / steer 插话 */
        const val PromptModeQueue = "queue"
        const val PromptModeSteer = "steer"

        /** 历史装载放宽到 45s(主机事件日志回放,大日志/冷启动可超 30s 默认超时) */
        private val HistoryTimeout = 45.seconds
        private val HistoryBackoffMs = longArrayOf(500L, 1500L)
    }
}
