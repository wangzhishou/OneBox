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

package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.DshEndpoints
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.FollowFrame
import com.wanbaohe.dsh.wire.RemoteEventFrame
import com.wanbaohe.dsh.wire.RemoteEventNames
import com.wanbaohe.dsh.wire.model.ActivityRunning
import com.wanbaohe.dsh.wire.model.PromptContentPart
import com.wanbaohe.dsh.wire.model.SessionAddress
import com.wanbaohe.dsh.wire.model.SessionEvent
import com.wanbaohe.dsh.wire.model.SessionSummary
import com.wanbaohe.dsh.wire.model.SubagentDeliveryQueue
import com.wanbaohe.dsh.wire.model.SubagentInterruptRequest
import com.wanbaohe.dsh.wire.model.SubagentInterruptValue
import com.wanbaohe.dsh.wire.model.SubagentListEntry
import com.wanbaohe.dsh.wire.model.SubagentListValue
import com.wanbaohe.dsh.wire.model.SubagentModeContinuable
import com.wanbaohe.dsh.wire.model.SubagentPromptRequest
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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import java.util.TimeZone
import java.util.UUID

/**
 * subagent 域状态(DSH 0.1.5-rc.2)。
 *
 * 与旧版(<= 0.1.1)的差异:
 * - 端点命名空间带 s:`subagents/list`、`subagents/prompt`、`subagents/interruptByParent`
 * - `subagents/list` 的 args 是**裸字符串** parentSessionId(旧版包了一层对象)
 * - `subagents/prompt` 的 request 新增必填 `requestId` 与 `delivery`,content 复用会话的
 *   [PromptContentPart]
 * - **子代理历史没有独立端点**:走 `session/follow` / `session/page`,
 *   地址是 [SessionAddress.Subagent](父 + 子 + mode),因此 transcript 实际由
 *   [SessionStore] 的同一条会话流管线供给
 * - 目录失效信号来自 `$events` 的 `api-session/added|removed|status`(旧版是 host 流帧)
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

/**
 * 单个子会话的只读事件日志视图。
 *
 * 0.1.5 起子会话与主会话共用同一条 follow/page 管线,数据实体在
 * [SessionStore.logFor] 的 [SessionLog] 里;本类只是一个**只读门面**,
 * 让 UI 继续按"子代理 transcript"的语义取事件(不复制数据)。
 */
class SubagentTranscript internal constructor(
    val childSessionId: String,
    private val log: SessionLog
) {
    /** 当前事件快照流(seq 升序) */
    val events: StateFlow<List<SessionEvent>> get() = log.events

    /** 已装载的最早 seq(loadOlder 的 beforeSeq 锚点) */
    val earliestLoadedSeq: Int? get() = log.earliestLoadedSeq

    /** 服务端还有更早历史(loadOlderTranscript 入口) */
    val hasOlder: StateFlow<Boolean> get() = log.hasOlder

    /** 按 seq 去重追加(实时增量);返回是否真追加 */
    fun append(event: SessionEvent): Boolean = log.append(event)
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
                // 重连 = 全量重取:目录缓存清空;transcript 走会话日志,seq 去重天然幂等
                if (_catalogs.value.isNotEmpty()) _catalogs.value = emptyMap()
            }
        }
        // 子会话的实时事件:同一套 follow 流,归属由 SessionFrameEvent.address 给出
        scope.launch { connection.sessionFrames.collect(::onSessionFrame) }
        // 目录失效信号(子会话增删/运行态翻转)来自 $events
        scope.launch { connection.eventFrames.collect(::onRemoteEvent) }
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

    /** 取(或建)某子会话的只读日志门面(懒登记) */
    fun transcriptFor(childSessionId: String): SubagentTranscript =
        transcripts.getOrPut(childSessionId) { SubagentTranscript(childSessionId, sessionStore.logFor(childSessionId)) }

    /**
     * 拉取(或命中缓存)某 parent 的直接 child 目录。
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
        _catalogs.value = _catalogs.value + (
            parentSessionId to SubagentCatalogState(
                entries = previous?.entries.orEmpty(),
                parentAvailable = previous?.parentAvailable ?: false,
                phase = SubagentCatalogPhase.Loading
            )
            )
        val job = scope.launch { fetchCatalog(parentSessionId) }
        catalogInflight[parentSessionId] = job
        try {
            job.join()
        } finally {
            catalogInflight.remove(parentSessionId)
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
            // 0.1.5:args 是裸字符串 parentSessionId
            val value = DshJson.decodeFromJsonElement<SubagentListValue>(
                api.callRemote(DshEndpoints.SUBAGENTS_LIST, buildJsonObject { put("parentSessionId", parentSessionId) })
            )
            SubagentCatalogState(
                entries = value.entries,
                parentAvailable = value.parentAvailable,
                phase = SubagentCatalogPhase.Ready
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
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

    /** 子会话地址(父 + 子 + mode;mode 来自目录行) */
    fun addressFor(parentSessionId: String, childSessionId: String, mode: String): SessionAddress =
        SessionAddress.Subagent(
            parentSessionId = parentSessionId,
            childSessionId = childSessionId,
            mode = mode
        )

    /**
     * 打开子会话(0.1.5:开一条 `session/follow`,地址是 subagent)。
     * 打开后快照即灌入 [transcriptFor]
     */
    fun openChild(parentSessionId: String, childSessionId: String, mode: String): Boolean {
        transcriptFor(childSessionId)
        return connection.followSession(addressFor(parentSessionId, childSessionId, mode)) != null
    }

    /** 关闭子会话流 */
    fun closeChild(parentSessionId: String, childSessionId: String, mode: String) {
        connection.unfollowSession(addressFor(parentSessionId, childSessionId, mode))
    }

    /** 装载子会话 transcript 尾页(默认 50 条;幂等,seq 去重) */
    suspend fun readTranscript(
        parentSessionId: String,
        childSessionId: String,
        mode: String,
        maxMessages: Int = TranscriptPageSize
    ) {
        openChild(parentSessionId, childSessionId, mode)
    }

    /** 向前补一页更早历史(走统一 `session/page`;无更早时 no-op) */
    suspend fun loadOlderTranscript(
        parentSessionId: String,
        childSessionId: String,
        mode: String,
        maxMessages: Int = TranscriptPageSize
    ) {
        sessionStore.loadOlderFor(addressFor(parentSessionId, childSessionId, mode), maxMessages)
    }

    /** 续聊:mode 恒 'continuable';入口暴露条件由 UI 按目录行判定(服务端仍权威) */
    suspend fun promptChild(
        parentSessionId: String,
        childSessionId: String,
        text: String
    ): SubagentPromptValue {
        val request = SubagentPromptRequest(
            requestId = UUID.randomUUID().toString(),
            parentSessionId = parentSessionId,
            childSessionId = childSessionId,
            mode = SubagentModeContinuable,
            delivery = SubagentDeliveryQueue,
            content = listOf(PromptContentPart.Text(text)),
            clientTimeZone = TimeZone.getDefault().id
        )
        val args = buildJsonObject {
            put("request", DshJson.encodeToJsonElement(SubagentPromptRequest.serializer(), request))
        }
        return DshJson.decodeFromJsonElement(api.callRemote(DshEndpoints.SUBAGENTS_PROMPT, args))
    }

    /** 中断运行中的可继续子会话(`subagents/interruptByParent`) */
    suspend fun interruptChild(
        parentSessionId: String,
        childSessionId: String
    ): SubagentInterruptValue {
        val args = DshJson.encodeToJsonElement(
            SubagentInterruptRequest.serializer(),
            SubagentInterruptRequest(
                childSessionId = childSessionId,
                parentSessionId = parentSessionId,
                mode = SubagentModeContinuable
            )
        ) as kotlinx.serialization.json.JsonObject
        return DshJson.decodeFromJsonElement(api.callRemote(DshEndpoints.SUBAGENTS_INTERRUPT_BY_PARENT, args))
    }

    // ───────────────────────────── 帧折叠 ─────────────────────────────

    /** 子会话实时事件(仅已登记的 transcript;未打开过的 child 不预登记) */
    private fun onSessionFrame(event: com.wanbaohe.dsh.connection.SessionFrameEvent) {
        if (disposed) return
        val frame = event.frame
        if (frame !is FollowFrame.Event) return
        val transcript = transcripts[event.sessionId] ?: return
        transcript.append(frame.event)
    }

    /** `$events`:子会话增删与运行态翻转驱动目录行更新 */
    private fun onRemoteEvent(frame: RemoteEventFrame) {
        if (disposed) return
        if (frame !is RemoteEventFrame.Emit) return
        when (frame.event) {
            RemoteEventNames.SESSION_STATUS -> {
                val sessionId = (frame.args.getOrNull(0) as? JsonPrimitive)?.content ?: return
                val running = (frame.args.getOrNull(1) as? JsonPrimitive)?.content == "true"
                applyActivity(sessionId, if (running) ActivityRunning else ActivityInactive)
            }

            RemoteEventNames.SESSION_ADDED -> {
                val arg = frame.args.firstOrNull() ?: return
                val summary = runCatching {
                    DshJson.decodeFromJsonElement(SessionSummary.serializer(), arg)
                }.getOrNull() ?: return
                if (summary.origin == OriginSubagent && summary.parentSessionId != null) {
                    markExpandable(summary.parentSessionId)
                    scheduleCatalogRefresh(summary.parentSessionId)
                }
            }

            RemoteEventNames.SESSION_REMOVED -> {
                val sessionId = (frame.args.firstOrNull() as? JsonPrimitive)?.content ?: return
                applyActivity(sessionId, ActivityInactive)
                val owned = _catalogs.value[sessionId]
                if (owned != null && owned.parentAvailable) {
                    _catalogs.value = _catalogs.value + (sessionId to owned.copy(parentAvailable = false))
                }
            }
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
        private const val ActivityInactive = "inactive"
        private const val TranscriptPageSize = 50
        private const val CatalogRefreshDebounceMs = 50L
    }
}
