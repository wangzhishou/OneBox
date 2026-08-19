package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.HostFrame
import com.wanbaohe.dsh.wire.MuxFrame
import com.wanbaohe.dsh.wire.model.ActivityRunning
import com.wanbaohe.dsh.wire.model.SessionEvent
import com.wanbaohe.dsh.wire.model.SessionSummary
import com.wanbaohe.dsh.wire.model.SubagentHistoryValue
import com.wanbaohe.dsh.wire.model.SubagentInterruptValue
import com.wanbaohe.dsh.wire.model.SubagentListEntry
import com.wanbaohe.dsh.wire.model.SubagentListValue
import com.wanbaohe.dsh.wire.model.SubagentModeContinuable
import com.wanbaohe.dsh.wire.model.SubagentPromptValue
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.util.TimeZone

/**
 * subagent 域状态(对齐 Flutter subagent_store.dart,DSH-PROTOCOL §3 subagent 组)。
 *
 * - subagent.list({parentSessionId}) → {entries, parentAvailable};目录按 parent 缓存,
 *   失效点 = 代际 ready(重连 = 全量重取);transcript 跨代际保留(seq 去重幂等补齐)
 * - 目录状态机:per-parent 三态 loading/ready/error;错误保留旧 entries;
 *   单飞复用(同一 parent 并发刷新共享一次往返)
 * - host/session-status → child 行 activity 行内翻转(零 RPC);
 *   host/session-added(origin=subagent) → 子行 hasChildren 正提示 + 防抖重拉其父目录;
 *   host/session-removed → 行内折 activity;该会话作为目录 owner 时 parentAvailable 置 false
 * - 后代聚合:origin=='subagent' 的行沿 parentSessionId 链向上累计 count/runningCount
 *   (普通 fork 断链 —— 每个可见会话只拥有不间断 subagent 血统的后代)
 * - subagent.prompt / interrupt 的 mode 恒为 'continuable';续聊入口仅当目录行
 *   parentAvailable==true 时暴露(store 不复查,服务端仍权威)
 *
 * 生命周期与连接实例绑定:由组件层创建并 [dispose]。
 */

/** 目录装载状态(对齐 web runtime refreshSubagents 三态) */
enum class SubagentCatalogPhase { Loading, Ready, Error }

/** 一个 parent 的子代理目录快照 + 装载状态(error/loading 态保留旧 entries) */
data class SubagentCatalogState(
    val entries: List<SubagentListEntry>,
    val parentAvailable: Boolean,
    val phase: SubagentCatalogPhase,
    val error: Throwable? = null
)

/** 某会话的「不间断 subagent 血统」后代计数 */
data class SubagentDescendants(
    val count: Int,
    val runningCount: Int
)

/**
 * 纯聚合:每个 origin=='subagent' 的会话沿 parentId 链向上累计,直到链断
 * (父不是 subagent origin / 父不在列表 / 环;环容错 seen 集)。
 */
fun indexSubagentDescendants(
    summaries: List<SessionSummary>
): Map<String, SubagentDescendants> {
    val byId = summaries.associateBy { it.sessionId }
    val counts = HashMap<String, Int>()
    val runningCounts = HashMap<String, Int>()
    for (descendant in summaries) {
        if (descendant.origin != OriginSubagent) continue
        var current = descendant
        val seen = HashSet<String>()
        while (current.parentSessionId != null &&
            current.origin == OriginSubagent &&
            seen.add(current.sessionId)
        ) {
            val parent = byId[current.parentSessionId] ?: break
            val key = current.parentSessionId!!
            counts[key] = (counts[key] ?: 0) + 1
            if (descendant.running) runningCounts[key] = (runningCounts[key] ?: 0) + 1
            current = parent
        }
    }
    return counts.keys.associateWith { key ->
        SubagentDescendants(
            count = counts[key] ?: 0,
            runningCount = runningCounts[key] ?: 0
        )
    }
}

private const val OriginSubagent = "subagent"

/** 单个子会话的只读事件日志(seq 去重追加,与 SessionLog 同语义) */
class SubagentTranscript(val childSessionId: String) {

    private val _events = MutableStateFlow<List<SessionEvent>>(emptyList())
    /** 当前事件快照流(seq 升序) */
    val events: StateFlow<List<SessionEvent>> = _events.asStateFlow()

    private val seenSeqs = HashSet<Int>()
    private val ordered = ArrayList<SessionEvent>()

    /** 已装载的最早 seq(loadOlder 的 beforeSeq 锚点) */
    val earliestLoadedSeq: Int? get() = ordered.firstOrNull()?.seq

    private val _hasOlder = MutableStateFlow(false)
    /** 服务端还有更早历史(loadOlderTranscript 入口) */
    val hasOlder: StateFlow<Boolean> = _hasOlder.asStateFlow()

    fun setHasOlder(value: Boolean) {
        _hasOlder.value = value
    }

    /** 按 seq 去重追加(mux 增量;重连重放安全);返回是否真追加 */
    fun append(event: SessionEvent): Boolean {
        if (!seenSeqs.add(event.seq)) return false
        insertOrdered(event)
        _events.value = ordered.toList()
        return true
    }

    /** 批量追加(历史页装载):seq 去重后只发一次快照 */
    fun appendAll(events: List<SessionEvent>): Int {
        var added = 0
        for (event in events) {
            if (!seenSeqs.add(event.seq)) continue
            insertOrdered(event)
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
}

class SubagentStore(
    private val api: DshApiClient,
    private val connection: DshConnectionController,
    private val sessionStore: SessionStore,
    parentScope: CoroutineScope
) {

    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )

    private val _catalogs = MutableStateFlow<Map<String, SubagentCatalogState>>(emptyMap())
    /** 目录快照流(parentSessionId → 装载状态) */
    val catalogs: StateFlow<Map<String, SubagentCatalogState>> = _catalogs.asStateFlow()

    private val _descendants = MutableStateFlow<Map<String, SubagentDescendants>>(emptyMap())
    /** 后代聚合流(会话摘要每快照一评估,等值不重发) */
    val descendants: StateFlow<Map<String, SubagentDescendants>> = _descendants.asStateFlow()

    private val transcripts = HashMap<String, SubagentTranscript>()
    private val catalogInflight = HashMap<String, Job>()
    private val catalogStale = HashSet<String>()
    private val catalogDebounce = HashMap<String, Job>()

    @Volatile
    private var disposed = false
    private var started = false
    private var lastReadyGeneration = 0

    fun start() {
        if (started) return
        started = true
        scope.launch {
            connection.snapshots.collect { snapshot ->
                if (disposed || snapshot.phase != ConnectionPhase.Ready) return@collect
                if (snapshot.generation <= lastReadyGeneration) return@collect
                lastReadyGeneration = snapshot.generation
                // 重连 = 全量重取:目录缓存清空;transcript 保留(seq 去重,幂等补齐)
                if (_catalogs.value.isNotEmpty()) _catalogs.value = emptyMap()
            }
        }
        scope.launch { connection.muxFrames.collect(::onMuxFrame) }
        scope.launch { connection.hostFrames.collect(::onHostFrame) }
        scope.launch {
            sessionStore.summaries.collect { summaries ->
                if (disposed) return@collect
                val next = indexSubagentDescendants(summaries)
                if (next != _descendants.value) _descendants.value = next
            }
        }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 当前目录快照(未装载为 null) */
    fun catalogFor(parentSessionId: String): SubagentCatalogState? =
        _catalogs.value[parentSessionId]

    /** 取(或建)某子会话的只读日志(懒登记;mux 增量只投已缓存的) */
    fun transcriptFor(childSessionId: String): SubagentTranscript =
        transcripts.getOrPut(childSessionId) { SubagentTranscript(childSessionId) }

    /**
     * 拉取(或命中缓存)某 parent 的直接 child 目录。
     * 缓存命中即返回([force] 跳过);未命中/force 走 RPC 推进 loading→ready/error。
     * 单飞:同一 parent 并发刷新共享一次往返(在飞时后来者 join 同一个 Job)。
     */
    suspend fun listChildren(
        parentSessionId: String,
        force: Boolean = false
    ): SubagentCatalogState {
        if (!force) {
            catalogInflight[parentSessionId]?.let { it.join(); return catalogFor(parentSessionId)!! }
            val cached = _catalogs.value[parentSessionId]
            if (cached != null && cached.phase == SubagentCatalogPhase.Ready) return cached
        }
        val previous = _catalogs.value[parentSessionId]
        _catalogs.value = _catalogs.value + (parentSessionId to SubagentCatalogState(
            entries = previous?.entries.orEmpty(),
            parentAvailable = previous?.parentAvailable ?: false,
            phase = SubagentCatalogPhase.Loading
        ))
        val job = scope.launch { fetchCatalog(parentSessionId) }
        catalogInflight[parentSessionId] = job
        try {
            job.join()
        } finally {
            catalogInflight.remove(parentSessionId)
            // 在飞响应早于触发 stale 的帧:settle 后补一拉才能收敛到最新
            if (catalogStale.remove(parentSessionId)) {
                scope.launch { listChildren(parentSessionId, force = true) }
            }
        }
        return _catalogs.value[parentSessionId]!!
    }

    /** 显式重拉某 parent 的目录(UI 主动刷新入口) */
    suspend fun invalidateChildren(parentSessionId: String) =
        listChildren(parentSessionId, force = true)

    private suspend fun fetchCatalog(parentSessionId: String) {
        val next = try {
            val value = DshJson.decodeFromJsonElement<SubagentListValue>(
                api.call(RpcSubagentList, buildJsonObject {
                    put("parentSessionId", parentSessionId)
                })
            )
            SubagentCatalogState(
                entries = value.entries,
                parentAvailable = value.parentAvailable,
                phase = SubagentCatalogPhase.Ready
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            // 错误保留旧 entries(UI 旧数据可用 + 可重试)
            val previous = _catalogs.value[parentSessionId]
            SubagentCatalogState(
                entries = previous?.entries.orEmpty(),
                parentAvailable = previous?.parentAvailable ?: false,
                phase = SubagentCatalogPhase.Error,
                error = e
            )
        }
        if (!disposed) _catalogs.value = _catalogs.value + (parentSessionId to next)
    }

    /** 装载子会话 transcript 尾页(默认 50 条;幂等,seq 去重)。[mode] 来自目录行 */
    suspend fun readTranscript(
        parentSessionId: String,
        childSessionId: String,
        mode: String,
        maxMessages: Int = TranscriptPageSize
    ) {
        fetchTranscriptPage(parentSessionId, childSessionId, mode, maxMessages, beforeSeq = null)
    }

    /** 向前补一页更早历史(无更早时 no-op;幂等) */
    suspend fun loadOlderTranscript(
        parentSessionId: String,
        childSessionId: String,
        mode: String,
        maxMessages: Int = TranscriptPageSize
    ) {
        val transcript = transcriptFor(childSessionId)
        val earliest = transcript.earliestLoadedSeq
        if (!transcript.hasOlder.value || earliest == null) return
        fetchTranscriptPage(parentSessionId, childSessionId, mode, maxMessages, beforeSeq = earliest)
    }

    private suspend fun fetchTranscriptPage(
        parentSessionId: String,
        childSessionId: String,
        mode: String,
        maxMessages: Int,
        beforeSeq: Int?
    ) {
        val value = DshJson.decodeFromJsonElement<SubagentHistoryValue>(
            api.call(RpcSubagentHistory, buildJsonObject {
                put("parentSessionId", parentSessionId)
                put("childSessionId", childSessionId)
                put("mode", mode)
                put("maxMessages", maxMessages)
                beforeSeq?.let { put("beforeSeq", it) }
            })
        )
        val transcript = transcriptFor(childSessionId)
        transcript.appendAll(value.events.map { it.event })
        transcript.setHasOlder(value.hasMore && value.events.isNotEmpty())
    }

    /** 续聊:mode 恒 'continuable';入口暴露条件由 UI 按目录行判定(服务端仍权威) */
    suspend fun promptChild(
        parentSessionId: String,
        childSessionId: String,
        text: String
    ): SubagentPromptValue {
        return DshJson.decodeFromJsonElement(
            api.call(RpcSubagentPrompt, buildJsonObject {
                put("parentSessionId", parentSessionId)
                put("childSessionId", childSessionId)
                put("mode", SubagentModeContinuable)
                putJsonArray("content") {
                    addJsonObject {
                        put("type", "text")
                        put("text", text)
                    }
                }
                put("clientTimeZone", TimeZone.getDefault().id)
            })
        )
    }

    /** 中断运行中的可继续子会话 */
    suspend fun interruptChild(
        parentSessionId: String,
        childSessionId: String
    ): SubagentInterruptValue {
        return DshJson.decodeFromJsonElement(
            api.call(RpcSubagentInterrupt, buildJsonObject {
                put("parentSessionId", parentSessionId)
                put("childSessionId", childSessionId)
                put("mode", SubagentModeContinuable)
            })
        )
    }

    // ───────────────────────────── 帧折叠 ─────────────────────────────

    private fun onMuxFrame(frame: MuxFrame) {
        if (disposed) return
        // 子会话事件实时增量(仅已缓存 transcript;未打开过的 child 不预登记)
        if (frame is MuxFrame.SessionEvent) {
            val transcript = transcripts[frame.sessionId] ?: return
            val event = runCatching {
                DshJson.decodeFromJsonElement(SessionEvent.serializer(), frame.event)
            }.getOrNull() ?: return
            transcript.append(event)
        }
    }

    private fun onHostFrame(frame: HostFrame) {
        if (disposed) return
        when (frame) {
            is HostFrame.SessionStatus -> applyActivity(
                frame.sessionId,
                if (frame.running) ActivityRunning else ActivityInactive
            )

            is HostFrame.SessionAdded -> {
                if (frame.origin == OriginSubagent && frame.parentSessionId != null) {
                    // 孙出生:子行 hasChildren 正提示 + 防抖重拉其父目录
                    markExpandable(frame.parentSessionId)
                    scheduleCatalogRefresh(frame.parentSessionId)
                }
            }

            is HostFrame.SessionRemoved -> {
                // 行内折 activity(Activation 脱离 ≠ 删除 durable 子代,行保留)
                applyActivity(frame.sessionId, ActivityInactive)
                // 被移除的会话不再是任何目录的投递属主:parentAvailable 即时置 false
                val owned = _catalogs.value[frame.sessionId]
                if (owned != null && owned.parentAvailable) {
                    _catalogs.value = _catalogs.value + (frame.sessionId to owned.copy(
                        parentAvailable = false
                    ))
                }
            }

            else -> Unit
        }
    }

    /** 行内翻转某 child 在所有已装目录里的 activity(零 RPC) */
    private fun applyActivity(sessionId: String, activity: String) {
        var changed = false
        val next = _catalogs.value.toMutableMap()
        for ((key, catalog) in _catalogs.value) {
            var rowChanged = false
            val entries = catalog.entries.map { entry ->
                if (entry is SubagentListEntry.Child &&
                    entry.id == sessionId &&
                    entry.activity != activity
                ) {
                    rowChanged = true
                    entry.copy(activity = activity)
                } else {
                    entry
                }
            }
            if (rowChanged) {
                next[key] = catalog.copy(entries = entries)
                changed = true
            }
        }
        if (changed) _catalogs.value = next
    }

    /** 把所有已装目录里 id==childSessionId 的行标成可展开(hasChildren=true) */
    private fun markExpandable(childSessionId: String) {
        var changed = false
        val next = _catalogs.value.toMutableMap()
        for ((key, catalog) in _catalogs.value) {
            var rowChanged = false
            val entries = catalog.entries.map { entry ->
                if (entry is SubagentListEntry.Child &&
                    entry.id == childSessionId &&
                    !entry.hasChildren
                ) {
                    rowChanged = true
                    entry.copy(hasChildren = true)
                } else {
                    entry
                }
            }
            if (rowChanged) {
                next[key] = catalog.copy(entries = entries)
                changed = true
            }
        }
        if (changed) _catalogs.value = next
    }

    /** 防抖重拉某 parent 的目录(50ms;在飞响应早于触发帧时标 stale,settle 后补拉) */
    private fun scheduleCatalogRefresh(parentSessionId: String) {
        if (!_catalogs.value.containsKey(parentSessionId)) return
        if (catalogDebounce.containsKey(parentSessionId)) return
        catalogDebounce[parentSessionId] = scope.launch {
            delay(CatalogRefreshDebounceMs)
            catalogDebounce.remove(parentSessionId)
            if (catalogInflight.containsKey(parentSessionId)) {
                catalogStale.add(parentSessionId)
                return@launch
            }
            listChildren(parentSessionId, force = true)
        }
    }

    companion object {
        private const val RpcSubagentList = "subagent.list"
        private const val RpcSubagentHistory = "subagent.history"
        private const val RpcSubagentPrompt = "subagent.prompt"
        private const val RpcSubagentInterrupt = "subagent.interrupt"
        private const val ActivityInactive = "inactive"
        private const val TranscriptPageSize = 50
        private const val CatalogRefreshDebounceMs = 50L
    }
}
