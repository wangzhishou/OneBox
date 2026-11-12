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
import com.wanbaohe.dsh.connection.SessionFrameEvent
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.ControlFrame
import com.wanbaohe.dsh.wire.DshEndpoints
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.FollowFrame
import com.wanbaohe.dsh.wire.RemoteEventFrame
import com.wanbaohe.dsh.wire.RemoteEventNames
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.model.ImageLimitsProjection
import com.wanbaohe.dsh.wire.model.ModelCatalog
import com.wanbaohe.dsh.wire.model.SessionAddress
import com.wanbaohe.dsh.wire.model.SessionCreateRequest
import com.wanbaohe.dsh.wire.model.SessionCreateValue
import com.wanbaohe.dsh.wire.SessionEventEntry
import com.wanbaohe.dsh.wire.model.SessionEvent
import com.wanbaohe.dsh.wire.model.SessionForkValue
import com.wanbaohe.dsh.wire.model.SessionListValue
import com.wanbaohe.dsh.wire.model.SessionPageRequest
import com.wanbaohe.dsh.wire.model.SessionPageValue
import com.wanbaohe.dsh.wire.model.SessionProjectionsBlock
import com.wanbaohe.dsh.wire.model.SessionPromptRequest
import com.wanbaohe.dsh.wire.model.SessionPromptValue
import com.wanbaohe.dsh.wire.model.SessionRenameValue
import com.wanbaohe.dsh.wire.model.SessionSearchRequest
import com.wanbaohe.dsh.wire.model.SessionSearchValue
import com.wanbaohe.dsh.wire.model.SessionSelectModelRequest
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
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.util.UUID
import java.util.TimeZone
import kotlin.time.Duration.Companion.seconds

/**
 * 单会话事件日志(对齐 web 客户端的 Session journal):
 * seq 去重有序插入(重连重放天然安全)+ 投影水位 + hasOlder 翻页锚点。
 */
class SessionLog(val sessionId: String) {

    private val _events = MutableStateFlow<List<SessionEvent>>(emptyList())

    /** 当前日志快照流(seq 升序) */
    val events: StateFlow<List<SessionEvent>> = _events.asStateFlow()

    private val seenSeqs = HashSet<Int>()
    private val ordered = ArrayList<SessionEvent>()

    /** 投影单元值(高 seq 覆盖低 seq;标题走此通道) */
    val projections = mutableMapOf<String, JsonElement>()

    /** 投影水位(日志级) */
    var projectionWatermark = -1

    /**
     * follow 快照给出的日志切口:翻历史页时作为 `throughSeq` 必填参数。
     * 0 表示尚未收到快照(此时不能翻页)。
     */
    @Volatile
    var throughSeq: Int = 0

    private val _hasOlder = MutableStateFlow(false)

    /** 服务端还有更早历史(loadOlder 入口) */
    val hasOlder: StateFlow<Boolean> = _hasOlder.asStateFlow()

    /** 已装载的最早 seq(loadOlder 的 beforeSeq 锚点) */
    val earliestLoadedSeq: Int? get() = ordered.firstOrNull()?.seq

    /** 按 seq 去重追加;返回是否真追加 */
    fun append(event: SessionEvent): Boolean {
        if (!seenSeqs.add(event.seq)) return false
        insertOrdered(event)
        _events.value = ordered.toList()
        return true
    }

    /** 批量追加(历史页装载):seq 去重后只发一次快照,避免逐条全屏重组 */
    fun appendAll(entries: List<SessionEventEntry>): Int {
        var added = 0
        for (entry in entries) {
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
 * 会话领域状态(DSH 0.1.5-rc.2 协议面)。
 *
 * 与旧版(<= 0.1.1)的差异:
 * - 事件不再来自全局 mux 流,而是**每会话一条 `session/follow` 流**;
 *   打开会话 = [follow](开流,首帧 snapshot 一次性给出历史 + 投影)
 * - 历史分页从 `session.history(sessionId,beforeSeq)` 改为
 *   `session/page(address,throughSeq,beforeSeq)`,`throughSeq` 是快照切口
 * - 摘要行不再带投影;`cwd` 仍在行内,标题等投影要打开会话(或读 control baseline)才有
 * - 会话列表增量走 `$events` 的 `api-session/added|removed|status`
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

    /** 会话投影 overlay:follow 快照 / control baseline / projection 帧汇入,高 seq 覆盖低 seq */
    private val projectionValues = mutableMapOf<String, MutableMap<String, JsonElement>>()
    private val projectionSeqs = mutableMapOf<String, MutableMap<String, Int>>()

    @Volatile
    private var disposed = false
    private var started = false
    private var lastReadyGeneration = 0

    /** 在飞的 session/list(并发合并:后续 refresh 共享同一次往返) */
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
        scope.launch { connection.sessionFrames.collect(::onSessionFrame) }
        scope.launch { connection.controlFrames.collect { event -> onControlProjection(event.sessionId, event.frame) } }
        scope.launch { connection.eventFrames.collect(::onRemoteEvent) }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 取(或建)某会话的日志;UI 打开会话时调用即完成「懒注册」 */
    fun logFor(sessionId: String): SessionLog =
        logs.getOrPut(sessionId) { SessionLog(sessionId) }

    /**
     * 打开会话流(0.1.5 的"进入会话"动作):开 `session/follow`,
     * 首帧 snapshot 会把历史与投影一次性灌进日志。
     * @return 是否成功开流(连接未就绪时为 false,走 [summaries] 的空态)
     */
    fun follow(sessionId: String): Boolean {
        logFor(sessionId)
        return connection.followSession(SessionAddress.Main(sessionId)) != null
    }

    /** 关闭会话流(离开会话时调用) */
    fun unfollow(sessionId: String) {
        connection.unfollowSession(SessionAddress.Main(sessionId))
    }

    /** 全量重取会话列表;在飞时返回同一个 Job(共享往返) */
    fun refresh(): Job? {
        if (disposed) return null
        refreshJob?.let { if (it.isActive) return it }
        val job = scope.launch {
            try {
                refreshList()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                pendingMutations.clear()
            } finally {
                refreshJob = null
            }
        }
        refreshJob = job
        return job
    }

    private suspend fun refreshList() {
        // 注意:session/list 的 wire 字段名是 **_request**(harness 端 list 的形参是
        // 解构默认值,typert 记成 _request),传 {} 或 request 都会被判参数不匹配。
        val value = api.callRemote(
            DshEndpoints.SESSION_LIST,
            buildJsonObject { put("_request", buildJsonObject {}) }
        )
        val parsed = DshJson.decodeFromJsonElement<SessionListValue>(value)
        if (disposed) return
        rawSummaries = parsed.items
        val alive = parsed.items.mapTo(HashSet()) { it.sessionId }
        // 列表行自带的投影提示 seed 进 overlay(标题/goal/imageLimits 等冷会话也有;
        // 是部分基线,值可能滞后但不错,asOfSeq 标明多旧 —— 高 seq 推送会覆盖)
        for (summary in parsed.items) {
            val block = summary.projections ?: continue
            for ((key, item) in block.values) {
                applyProjectionValue(summary.sessionId, key, item, block.asOfSeq)
            }
        }
        // overlay 行回收(防长期增长);日志保留(已打开会话仍可看)
        projectionValues.keys.retainAll(alive)
        projectionSeqs.keys.retainAll(alive)
        refreshJob = null
        val pending = pendingMutations.toList()
        pendingMutations.clear()
        pending.forEach { it.invoke() }
        emitSummaries()
    }

    /**
     * 向前补一页更早历史(open 后调用;throughSeq 尚未到手时 no-op)。
     * 0.1.5 的 `session/page` 要求 `throughSeq`(snapshot 切口)与可选 `beforeSeq`。
     */
    suspend fun loadOlder(sessionId: String, maxMessages: Int = DefaultPageSize) {
        val log = logFor(sessionId)
        val earliest = log.earliestLoadedSeq
        if (!log.hasOlder.value || earliest == null || log.throughSeq <= 0) return
        fetchPage(SessionAddress.Main(sessionId), log, maxMessages, beforeSeq = earliest)
    }

    /** 子代理历史补页(地址是 parent+child) */
    suspend fun loadOlderFor(address: SessionAddress, maxMessages: Int = DefaultPageSize) {
        val log = logFor(addressId(address))
        val earliest = log.earliestLoadedSeq
        if (!log.hasOlder.value || earliest == null || log.throughSeq <= 0) return
        fetchPage(address, log, maxMessages, beforeSeq = earliest)
    }

    /** 打开一个子代理会话流(与主会话同形,地址不同) */
    fun followAddress(address: SessionAddress): Boolean =
        connection.followSession(address) != null

    /**
     * 拉一页历史并落地。超时/载波故障退避重试(共 3 次);业务错误直接上抛。
     */
    private suspend fun fetchPage(
        address: SessionAddress,
        log: SessionLog,
        maxMessages: Int,
        beforeSeq: Int?
    ): Boolean {
        val request = SessionPageRequest(
            address = address,
            throughSeq = log.throughSeq,
            beforeSeq = beforeSeq,
            maxMessages = maxMessages
        )
        val args = buildJsonObject {
            put("request", DshJson.encodeToJsonElement(SessionPageRequest.serializer(), request))
        }
        var lastError: Throwable? = null
        for (attempt in 0 until HistoryAttempts) {
            try {
                val value = api.callRemote(DshEndpoints.SESSION_PAGE, args, timeout = HistoryTimeout)
                val parsed = DshJson.decodeFromJsonElement<SessionPageValue>(value)
                log.appendAll(parsed.records)
                parsed.projections?.let { block ->
                    var overlayChanged = false
                    for ((key, item) in block.values) {
                        log.projections[key] = item
                        if (applyProjectionValue(projectionKey(address), key, item, block.asOfSeq)) {
                            overlayChanged = true
                        }
                    }
                    if (overlayChanged) emitSummaries()
                    if (block.asOfSeq > log.projectionWatermark) log.projectionWatermark = block.asOfSeq
                }
                val hasOlder = parsed.hasMore && parsed.records.isNotEmpty()
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
        throw lastError ?: CarrierException("session/page 重试耗尽")
    }

    /**
     * session/create:workspaceId 与 cwd 至多一个(双侧都发服务端会拒)。
     * 创建后登记日志 + 合成 upsert,并触发 refresh。
     */
    suspend fun createSession(
        workspaceId: String? = null,
        cwd: String? = null,
        agentPreset: String? = null
    ): SessionCreateValue {
        val request = SessionCreateRequest(
            workspaceId = workspaceId,
            cwd = cwd,
            agentPreset = agentPreset
        )
        val args = buildJsonObject {
            put("request", DshJson.encodeToJsonElement(SessionCreateRequest.serializer(), request))
        }
        val value = DshJson.decodeFromJsonElement<SessionCreateValue>(
            api.callRemote(DshEndpoints.SESSION_CREATE, args)
        )
        logFor(value.sessionId)
        recordMutation { mergeAddedFields(value.sessionId, blank = true, cwd = cwd) }
        mergeAddedFields(value.sessionId, blank = true, cwd = cwd)
        refresh()
        return value
    }

    /**
     * 发送 prompt(0.1.5 契约):`requestId` 必填(客户端 mint,用户消息的
     * `source.rpcId` 会回显它),[`mode`] queue 排队 / steer 插话。
     * 带图片时先做本地预拒(imageLimits 投影缺席则跳过预检,服务端权威)。
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
            requestId = UUID.randomUUID().toString(),
            sessionId = sessionId,
            mode = mode,
            content = buildPromptContent(text, images),
            clientTimeZone = TimeZone.getDefault().id
        )
        val args = buildJsonObject {
            put("request", DshJson.encodeToJsonElement(SessionPromptRequest.serializer(), request))
        }
        return DshJson.decodeFromJsonElement(api.callRemote(DshEndpoints.SESSION_PROMPT, args))
    }

    /** session/modelCatalog:目录 + 默认选择(旧 `session.models` 已合并到目录面) */
    suspend fun modelCatalog(sessionId: String? = null): ModelCatalog {
        val value = api.callRemote(DshEndpoints.SESSION_MODEL_CATALOG)
        return DshJson.decodeFromJsonElement(ModelCatalog.serializer(), value)
    }

    /** session/selectModel:选择可与目录成员无关(服务端语义) */
    suspend fun selectModel(
        sessionId: String,
        provider: String,
        model: String,
        reasoningEffort: String? = null
    ): SessionSelectModelValue {
        val request = SessionSelectModelRequest(
            sessionId = sessionId,
            provider = provider,
            model = model,
            reasoningEffort = reasoningEffort
        )
        val args = buildJsonObject {
            put("request", DshJson.encodeToJsonElement(SessionSelectModelRequest.serializer(), request))
        }
        return DshJson.decodeFromJsonElement(api.callRemote(DshEndpoints.SESSION_SELECT_MODEL, args))
    }

    /** session/search:侧栏搜索(query ≤500 字符,分页 hasMore) */
    suspend fun search(query: String): SessionSearchValue {
        val request = SessionSearchRequest(query = query)
        val args = buildJsonObject {
            put("request", DshJson.encodeToJsonElement(SessionSearchRequest.serializer(), request))
        }
        return DshJson.decodeFromJsonElement(api.callRemote(DshEndpoints.SESSION_SEARCH, args))
    }

    /**
     * session/fork:atSeq 锚点映射到其后第一个 turn/end(turn 未闭合 → fork-unavailable)。
     * fork 后登记日志 + 合成 upsert,并触发 refresh(新会话入列)。
     */
    suspend fun fork(sessionId: String, atSeq: Int? = null): SessionForkValue {
        val args = buildJsonObject {
            putJsonObject("request") {
                put("sessionId", sessionId)
                atSeq?.let { put("atSeq", it) }
            }
        }
        val value = DshJson.decodeFromJsonElement<SessionForkValue>(
            api.callRemote(DshEndpoints.SESSION_FORK, args)
        )
        logFor(value.sessionId)
        recordMutation { mergeAddedFields(value.sessionId, blank = true) }
        mergeAddedFields(value.sessionId, blank = true)
        refresh()
        return value
    }

    /**
     * session/rename:响应回带的规范化 title+seq 先落本地格,
     * 推送 projection 帧高 seq 覆盖(乱序安全)。
     */
    suspend fun rename(sessionId: String, title: String): SessionRenameValue {
        val args = buildJsonObject {
            putJsonObject("request") {
                put("sessionId", sessionId)
                put("title", title)
            }
        }
        val value = DshJson.decodeFromJsonElement<SessionRenameValue>(
            api.callRemote(DshEndpoints.SESSION_RENAME, args)
        )
        val projected = JsonPrimitive(value.title)
        logFor(sessionId).applyProjection("title", projected, value.seq)
        if (applyProjectionValue(sessionId, "title", projected, value.seq)) emitSummaries()
        refresh()
        return value
    }

    /**
     * 会话标题投影(0.1.5 的摘要行不再带投影)。
     *
     * 标题有两条来源:
     * 1. control baseline / projection 推送帧 → 这里落进 overlay,**不必打开会话**就能拿到
     *    (会话列表因此能显示 LLM 生成的标题,而不是 cwd 目录名)
     * 2. 打开会话(follow 快照)→ 落在该会话的 [SessionLog].projections
     */
    fun titleOf(sessionId: String): String? {
        val overlay = (projectionValues[sessionId]?.get("title") as? JsonPrimitive)?.contentOrNull
        if (!overlay.isNullOrBlank()) return overlay
        return (logs[sessionId]?.projections?.get("title") as? JsonPrimitive)
            ?.contentOrNull
            ?.takeIf { it.isNotBlank() }
    }

    /** 会话标题(同上;供 UI 顶层调用) */
    fun titleFor(sessionId: String): String? = titleOf(sessionId)

    /** 从日志投影水位取 imageLimits(未打开会话时为 null —— 预拒退化为服务端权威) */
    fun attachmentLimitsFor(sessionId: String): ImageLimitsProjection? {
        val raw = logs[sessionId]?.projections?.get("imageLimits") ?: return null
        return runCatching {
            DshJson.decodeFromJsonElement(ImageLimitsProjection.serializer(), raw)
        }.getOrNull()
    }

    // ───────────────────────────── 帧折叠 ─────────────────────────────

    /** follow 帧(每会话一条流;归属由 [SessionFrameEvent] 给出) */
    private fun onSessionFrame(event: SessionFrameEvent) {
        when (val frame = event.frame) {
            is FollowFrame.Snapshot -> {
                val log = logFor(event.sessionId)
                log.throughSeq = frame.cursor
                log.appendAll(frame.records)
                frame.projections?.let { block ->
                    for ((key, item) in block.values) {
                        log.projections[key] = item
                        if (applyProjectionValue(event.sessionId, key, item, block.asOfSeq)) emitSummaries()
                    }
                    if (block.asOfSeq > log.projectionWatermark) log.projectionWatermark = block.asOfSeq
                }
                log.setHasOlder(frame.hasMore)
                // 摘要行补齐会话头事实(摘要行本身不带 cwd 之外的头部字段)
                mergeAddedFields(event.sessionId, blank = frame.header.isSeeded.not(), cwd = frame.header.cwd)
            }

            is FollowFrame.Event -> {
                // 懒注册:只向已打开的日志投递;未打开会话的历史在打开时由快照给出
                logs[event.sessionId]?.append(frame.event)
                if (frame.event.type == EventTypeUserMessage &&
                    frame.event.dataObject.userSourceKind() == SourceKindUser
                ) {
                    bumpActivity(event.sessionId, frame.event.time)
                }
            }

            // 助手输出增量(进程内状态):由 Chat 层按需消费,不落日志
            is FollowFrame.AssistantStream -> Unit
        }
    }

    /** 控制面帧里的投影增量(全局一条 control 流) */
    private fun onControlProjection(sessionId: String, frame: ControlFrame) {
        if (frame !is ControlFrame.Projection) return
        val value = frame.value ?: return
        logs[sessionId]?.applyProjection(frame.key, value, frame.seq)
        if (applyProjectionValue(sessionId, frame.key, value, frame.seq)) emitSummaries()
    }

    /** `$events` 转发事件:会话列表增量在此折叠 */
    private fun onRemoteEvent(frame: RemoteEventFrame) {
        if (frame !is RemoteEventFrame.Emit) return
        when (frame.event) {
            RemoteEventNames.SESSION_ADDED -> {
                val arg = frame.args.firstOrNull() ?: return
                val summary = runCatching {
                    DshJson.decodeFromJsonElement(SessionSummary.serializer(), arg)
                }.getOrNull() ?: return
                recordMutation { upsertSummary(summary) }
                upsertSummary(summary)
            }

            RemoteEventNames.SESSION_REMOVED -> {
                val arg = frame.args.firstOrNull() as? JsonPrimitive ?: return
                val sessionId = arg.content
                recordMutation { removeSummary(sessionId) }
                removeSummary(sessionId)
            }

            RemoteEventNames.SESSION_STATUS -> {
                val sessionId = (frame.args.getOrNull(0) as? JsonPrimitive)?.content ?: return
                val running = (frame.args.getOrNull(1) as? JsonPrimitive)?.content == "true"
                recordMutation { applyStatusFlip(sessionId, running) }
                applyStatusFlip(sessionId, running)
            }

            RemoteEventNames.SESSION_ERROR -> Unit
        }
    }

    /** running 翻转:running=true 清 blank(首 turn 开跑即非空);同值重放零副作用 */
    private fun applyStatusFlip(sessionId: String, running: Boolean) {
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
    private fun upsertSummary(summary: SessionSummary) {
        val idx = rawSummaries.indexOfFirst { it.sessionId == summary.sessionId }
        if (idx < 0) {
            rawSummaries = listOf(summary) + rawSummaries
            emitSummaries()
            return
        }
        val old = rawSummaries[idx]
        val merged = old.copy(
            updatedAt = maxOf(old.updatedAt, summary.updatedAt),
            running = summary.running,
            blank = old.blank && summary.blank,
            parentSessionId = old.parentSessionId ?: summary.parentSessionId,
            origin = old.origin ?: summary.origin,
            cwd = old.cwd ?: summary.cwd
        )
        if (merged == old) return
        val next = rawSummaries.toMutableList()
        next[idx] = merged
        rawSummaries = next
        emitSummaries()
    }

    /** mergeAddedFields:本地合成的会话行(create/fork 后立即入列) */
    private fun mergeAddedFields(
        sessionId: String,
        blank: Boolean,
        parentSessionId: String? = null,
        origin: String? = null,
        cwd: String? = null
    ) {
        upsertSummary(
            SessionSummary(
                sessionId = sessionId,
                updatedAt = System.currentTimeMillis().toDouble(),
                running = false,
                blank = blank,
                parentSessionId = parentSessionId,
                origin = origin,
                cwd = cwd
            )
        )
    }

    /** session-removed:subagent 可 resume 只折 running;普通会话移出并回收 overlay */
    private fun removeSummary(sessionId: String) {
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

    private fun projectionKey(address: SessionAddress): String = addressId(address)

    private fun addressId(address: SessionAddress): String = when (address) {
        is SessionAddress.Main -> address.sessionId
        is SessionAddress.Subagent -> address.childSessionId
    }

    /**
     * 发布摘要列表:把 title 投影(overlay / 会话日志)合并进行。
     * 0.1.5 的列表行不带投影,标题来自 control baseline 或 follow 快照,
     * 不合并的话列表只能显示 cwd 目录名。
     */
    private fun emitSummaries() {
        _summaries.value = rawSummaries.map { summary ->
            val title = titleOf(summary.sessionId)
            if (title == null || title == summary.title) summary else summary.copy(title = title)
        }
    }

    companion object {
        private const val EventTypeUserMessage = "user/message"
        private const val SourceKindUser = "user"
        private const val OriginSubagent = "subagent"
        private const val DefaultPageSize = 50
        private const val HistoryAttempts = 3

        /** prompt 的 mode 枚举:queue 排队 / steer 插话 */
        const val PromptModeQueue = "queue"
        const val PromptModeSteer = "steer"

        /** 历史装载放宽到 45s(主机事件日志回放,大日志/冷启动可超 30s 默认超时) */
        private val HistoryTimeout = 45.seconds
        private val HistoryBackoffMs = longArrayOf(500L, 1500L)
    }
}
