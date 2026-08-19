package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.wire.model.SessionEvent
import com.wanbaohe.dsh.wire.model.extractEventText
import com.wanbaohe.dsh.wire.model.userSourceKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * session/event → 会话流节点提取(对齐 Flutter event_nodes.dart,DSH-PROTOCOL §4)。
 *
 * 把原始事件日志升级为节点流:用户气泡 / 助手消息(markdown)/ think 折叠块 /
 * 工具卡(call+result 配对,优先消费帧 view 渲染意图)/ todo 计划 / 压缩检查点 /
 * 轮末统计行 / 重试行 / 错误行 / 系统短提示 / 未知类型兜底。
 *
 * 不变式(头注释即契约):
 * - 纯函数、可重放:同输入必同输出,无任何外部状态;节点是 UI 层产物,不改事件日志
 * - 按 seq 升序输出(输入乱序也先排,配对按 seq 顺序进行)
 * - 工具卡的本质信息以 event.data 为准,view 只作渲染增强,因此历史回放(无 view)
 *   也能渲染工具卡;view 词表(dsh-tools presentation)只有 card/title/output 等
 *   渲染字段,不含 status/interrupted/ok —— 状态判定以 data.error.code 为权威
 * - 中断/异常落点(dsh 0.1.0-rc.6 权威形状):
 *   tool/result 的 data.error = {name:'AbortError', code:'ABORTED*' /
 *   TOOL_OUTCOME_UNKNOWN / TOOL_NOT_STARTED} → 中断(琥珀);step/end、turn/end
 *   闭合时仍未配到结果的运行卡结算为 interrupted(web interruption() 同款规则);
 *   turn/end 携带 data.reason.kind(completed|aborted|blocked|error|max-tokens|
 *   interrupted),reason≠completed/blocked 各产一行提示
 * - 未知类型兜底收窄(镜像 web fallback.ts):只有 surface 三类型(user/message |
 *   assistant/message | tool/result)且 surfaceOp=='append' 但未被认识的情形才显示
 *   兜底卡;协议管道事件(turn/step 边界、request 系列、hook 系列等)一律不可见
 */

/** 工具卡状态(由数据判定:运行/成功/失败/中断) */
enum class ToolStatus { Running, Success, Failed, Interrupted }

/** 图片附件引用(防御式形状;本部署无视觉模型,按 fixture 形状提取,见 [extractImageRefs]) */
data class ImageAttachmentRef(
    val attachmentId: String,
    val mediaType: String,
    val bytes: Long,
    val width: Int,
    val height: Int,
    val name: String? = null
)

/** todo 计划单项 */
data class TodoItem(
    val title: String,
    val done: Boolean = false
)

/** 会话流节点(sealed)。每个节点携带来源事件的 seq 与原始类型名;[key] 供 LazyColumn 稳定挂载 */
sealed class ChatNode {
    abstract val seq: Int
    abstract val type: String

    /** 来源事件时间(epoch ms;历史旧档/合成节点可能缺失) */
    abstract val time: Double?

    /** LazyColumn item key:同 seq 可产出多节点(assistant/message → think+正文),类型名参与判重 */
    val key: String get() = "$type#$seq"

    /** 用户气泡(右对齐,纯文本;[images] 为防御式提取的图片引用) */
    data class UserMessage(
        override val seq: Int,
        override val type: String,
        val text: String,
        val images: List<ImageAttachmentRef> = emptyList(),
        override val time: Double? = null
    ) : ChatNode()

    /** 助手消息(markdown 渲染;[streaming] = assistant/chunk 折叠出的直播节点) */
    data class AssistantMessage(
        override val seq: Int,
        override val type: String,
        val text: String,
        val images: List<ImageAttachmentRef> = emptyList(),
        val streaming: Boolean = false,
        /** 定稿消息 id(data.id / data.message.id;消息反馈 messageFeedback 的寻址键,缺席不渲染反馈行) */
        val messageId: String? = null,
        override val time: Double? = null
    ) : ChatNode()

    /** think 折叠块(默认收起;[streaming] 时标题行滚动显示最后一行) */
    data class Think(
        override val seq: Int,
        override val type: String,
        val text: String,
        val streaming: Boolean = false,
        override val time: Double? = null
    ) : ChatNode()

    /** 工具卡:call+result 配对后的渲染意图;未配对 call 保持运行中 */
    data class Tool(
        override val seq: Int,
        override val type: String,
        val toolName: String,
        val callId: String? = null,
        val input: JsonElement? = null,
        val output: JsonElement? = null,
        val error: String? = null,
        val summary: String? = null,
        val status: ToolStatus = ToolStatus.Running,
        val callSeq: Int? = null,
        val resultSeq: Int? = null,
        override val time: Double? = null,
        val callTime: Double? = null,
        val resultTime: Double? = null,
        val producedPaths: List<String> = emptyList()
    ) : ChatNode()

    /** todo/write 计划快照(紧凑状态计数卡) */
    data class Todo(
        override val seq: Int,
        override val type: String,
        val items: List<TodoItem>,
        override val time: Double? = null
    ) : ChatNode() {
        val done: Int get() = items.count { it.done }
    }

    /** 压缩检查点(compaction/start|end|summary|prune) */
    data class Compaction(
        override val seq: Int,
        override val type: String,
        val kind: String,
        val summary: String? = null,
        val messages: Int? = null,
        override val time: Double? = null
    ) : ChatNode()

    /** 轮末统计行(runMs / ttftMs / tok-s;turn/end 时由轮内记账产出,仅完成轮有值) */
    data class Stats(
        override val seq: Int,
        override val type: String,
        val runMs: Double,
        val ttftMs: Double? = null,
        val tokensPerSecond: Double? = null,
        val outputTokens: Int = 0,
        override val time: Double? = null
    ) : ChatNode()

    /** 轮末产出文件行(turn/end 时结转:本轮成功修改调用的 producedPaths,首见去重) */
    data class Deliverables(
        override val seq: Int,
        override val type: String,
        val paths: List<String>,
        override val time: Double? = null
    ) : ChatNode()

    /** llm/retry 重试行(内联细行) */
    data class Retry(
        override val seq: Int,
        override val type: String,
        val reason: String? = null,
        val attempt: Int? = null,
        val maxRetries: Int? = null,
        override val time: Double? = null
    ) : ChatNode()

    /** 错误行(turn/error,或 turn/end reason.kind==error) */
    data class Error(
        override val seq: Int,
        override val type: String,
        val message: String,
        override val time: Double? = null
    ) : ChatNode()

    /** 系统短提示(已知但不应直接暴露协议名的事件,人类语言整理后一行交代) */
    data class Notice(
        override val seq: Int,
        override val type: String,
        val title: String,
        val detail: String? = null,
        override val time: Double? = null
    ) : ChatNode()

    /** 未知类型兜底(类型名 + 原始 data 折叠展示) */
    data class Unknown(
        override val seq: Int,
        override val type: String,
        val data: JsonObject,
        override val time: Double? = null
    ) : ChatNode()
}

/**
 * 提取输入对:事件 + 主机渲染意图(view 可选,仅作渲染增强)。
 * 历史回放(无 view)直接 [EventNodeInput](event) 即可。
 */
class EventNodeInput(
    val event: SessionEvent,
    view: JsonElement? = null
) {
    /** view 的判别字段('call' | 'result';缺席/畸形为 null) */
    val viewFor: String? = (view as? JsonObject)?.str("for")

    /** view 的渲染字段载荷(缺席/畸形为 null) */
    val viewMap: JsonObject? = (view as? JsonObject)?.get("view") as? JsonObject
}

/** 便捷入口:事件日志 + view 查找(SessionLog.viewFor)→ 节点列表(按 seq 升序,可重放) */
fun extractNodes(
    events: List<SessionEvent>,
    viewFor: (Int) -> JsonElement? = { null }
): List<ChatNode> =
    extractNodes(events.map { EventNodeInput(it, viewFor(it.seq)) })

/** 提取器:List<[EventNodeInput]> → List<[ChatNode]>(按 seq 升序,可重放) */
fun extractNodes(inputs: List<EventNodeInput>): List<ChatNode> {
    val sorted = inputs.sortedBy { it.event.seq }
    val result = ArrayList<ChatNode>()
    // 未配对 call 的占位:记录它在 result 中的下标,结果事件到达时回填合并
    val pending = ArrayList<PendingCall>()
    // 轮级指标记账(turn/start → turn/end):轮末产出统计行
    val turnMetrics = TurnMetrics()
    // 轮末产出:本轮成功修改调用累计的 producedPaths(首见去重,turn/end 时结转)
    var turnProduced = ArrayList<String>()
    for (input in sorted) {
        val event = input.event
        if (event.type == EventAssistantChunk && chunkOf(event.data) != null) {
            continue // 折叠态由 foldChunks 统一产出
        }
        // 轮内记账:边界/首帧/用量喂给 turnMetrics;turn/end 时产出统计行
        if (event.type == EventTurnEnd) {
            turnMetrics.buildStats(event)?.let(result::add)
            // 轮末产出结转:只有成功结算的修改意图才计入(统计行之后,同 seq 依附)
            if (turnProduced.isNotEmpty()) {
                result.add(
                    ChatNode.Deliverables(
                        seq = event.seq,
                        type = "turn/deliverables",
                        paths = turnProduced,
                        time = event.time
                    )
                )
                turnProduced = ArrayList()
            }
        }
        turnMetrics.observe(event)
        when (toolKindOf(event, input)) {
            ToolKind.Call -> {
                val node = buildToolCall(event, input.viewMap)
                pending.add(
                    PendingCall(
                        index = result.size,
                        callId = node.callId,
                        turn = event.data.intOf("turn"),
                        step = event.data.intOf("step")
                    )
                )
                result.add(node)
            }

            ToolKind.Result -> {
                val node = buildToolResult(event, input.viewMap)
                val idx = matchPending(pending, node.callId)
                if (idx >= 0) {
                    val p = pending.removeAt(idx)
                    val merged = mergeCallResult(result[p.index] as ChatNode.Tool, node)
                    result[p.index] = merged
                    // web producedPaths:只有成功结算的修改意图才计入产出(首见去重)
                    if (merged.status == ToolStatus.Success && merged.producedPaths.isNotEmpty()) {
                        for (path in merged.producedPaths) {
                            if (path !in turnProduced) turnProduced.add(path)
                        }
                    }
                } else {
                    result.add(node) // 无配对结果的独立卡(按数据判定状态)
                }
            }

            ToolKind.None -> {
                // step/turn 闭合:范围内仍无结果的运行卡结算为中断
                // (web interruption() 同款规则 —— 宿主在关闭前必已提交结果,
                // 关闭时仍缺 = 永不再来,消灭「中断后永远转圈」)
                if (event.type == EventStepEnd) {
                    val t = event.data.intOf("turn")
                    val s = event.data.intOf("step")
                    // 字段齐全才做精确匹配;缺字段的 step 边界不结算(留给 turn 边界兜底)
                    if (t != null && s != null) {
                        settlePending(pending, result, turn = t, step = s, seq = event.seq)
                    }
                } else if (event.type == EventTurnEnd) {
                    // turn 边界落定一切:seq 序保证此前事件属于已闭合范围
                    settlePending(pending, result, seq = event.seq)
                }
                result.addAll(nodesFor(event))
            }
        }
    }
    // 流式折叠节点(无定稿 message 的 chunk 游)按 seq 归位插入
    val folded = foldChunks(sorted)
    if (folded.isNotEmpty()) {
        result.addAll(folded)
        result.sortBy { it.seq }
    }
    return result
}

private enum class ToolKind { None, Call, Result }

// ───────────────────────────── 轮级指标(turn/start → turn/end 统计行) ─────────────────────────────

/** 单轮指标:runMs(turn/start→turn/end)、ttftMs(首个文本 chunk)、decode tok/s(usage outputTokens / 首 chunk→轮末) */
private class TurnMetrics {
    private var turnStart: Double? = null
    private var firstChunk: Double? = null
    private var outputTokens = 0

    fun observe(event: SessionEvent) {
        when (event.type) {
            EventTurnStart -> reset(event.time)
            EventAssistantChunk -> {
                val chunk = chunkOf(event.data) ?: return
                val hasText = (chunk.str("type") == "text-delta" &&
                    !chunk.str("text").isNullOrBlank()) || chunk.str("type") == "block-end"
                if (hasText && firstChunk == null) firstChunk = event.time
            }

            EventAssistantMessage -> {
                val usage = event.data["usage"] as? JsonObject
                outputTokens += usage?.intOf("outputTokens") ?: 0
            }
        }
    }

    private fun reset(time: Double) {
        turnStart = time
        firstChunk = null
        outputTokens = 0
    }

    /** turn/end:产出本轮统计行(无 turn/start 的防御形状不出行) */
    fun buildStats(end: SessionEvent): ChatNode.Stats? {
        val start = turnStart ?: return null
        val runMs = (end.time - start).coerceAtLeast(0.0)
        val first = firstChunk
        val ttft = first?.let { (it - start).coerceAtLeast(0.0) }
        val tps = if (first != null && outputTokens > 0) {
            outputTokens / ((end.time - first).coerceAtLeast(1.0) / 1000.0)
        } else {
            null
        }
        reset(end.time)
        return ChatNode.Stats(
            seq = end.seq,
            type = "turn/stats",
            runMs = runMs,
            ttftMs = ttft,
            tokensPerSecond = tps,
            outputTokens = outputTokens,
            time = end.time
        )
    }
}

// ───────────────────────────── 工具卡配对 ─────────────────────────────

/** 工具事件判定:优先信 view(主机渲染意图),其次按类型名猜测。
 * code-dispatch 是 run_code 的内部派发子调用(父卡已汇总输出),不算工具卡 */
private fun toolKindOf(event: SessionEvent, input: EventNodeInput): ToolKind {
    val type = event.type
    if (type == "tool/code-dispatch" || type == "tool/code-dispatch-start") {
        return ToolKind.None
    }
    when (input.viewFor) {
        "call" -> return ToolKind.Call
        "result" -> return ToolKind.Result
    }
    if (type.startsWith("tool/") || type.startsWith("tool:")) {
        if (type.contains("result")) return ToolKind.Result
        if (type.contains("error")) return ToolKind.Result // tool/error 视作失败结果
        return ToolKind.Call
    }
    return ToolKind.None
}

private class PendingCall(
    val index: Int,
    val callId: String?,
    val turn: Int?,
    val step: Int?
)

/** 闭合结算:turn/step 均给定时只精确命中同范围的调用,均缺省时结算全部(turn 边界语义)。
 * 倒序遍历安全移除;卡的 seq 保持 call 位(key 稳定) */
private fun settlePending(
    pending: MutableList<PendingCall>,
    result: MutableList<ChatNode>,
    turn: Int? = null,
    step: Int? = null,
    seq: Int
) {
    if (pending.isEmpty()) return
    val precise = turn != null && step != null
    for (i in pending.indices.reversed()) {
        val p = pending[i]
        if (precise && (p.turn != turn || p.step != step)) continue
        pending.removeAt(i)
        result[p.index] = interruptedCall(result[p.index] as ChatNode.Tool, seq)
    }
}

/** 运行卡 → 中断卡(闭合时无结果):状态与 resultSeq 取结算事件;
 * 输出/错误不合成文本 —— 中断原因由紧随的轮次级提示节点交代 */
private fun interruptedCall(call: ChatNode.Tool, seq: Int): ChatNode.Tool =
    call.copy(
        status = ToolStatus.Interrupted,
        resultSeq = seq,
        callSeq = call.callSeq ?: call.seq,
        callTime = call.callTime ?: call.time
    )

/** 配对:先按 callId 精确匹配,否则取最近(最后加入)的未配对 call */
private fun matchPending(pending: List<PendingCall>, callId: String?): Int {
    if (callId != null) {
        for (i in pending.indices.reversed()) {
            if (pending[i].callId == callId) return i
        }
    }
    return pending.size - 1
}

private fun buildToolCall(event: SessionEvent, viewMap: JsonObject?): ChatNode.Tool {
    val name = pick(event.data, viewMap, "toolName", "name", "tool", "tool_name") ?: "tool"
    val callId = pick(event.data, viewMap, "callId", "call_id", "id")
    val rawInput = pickValue(event.data, viewMap, "input", "args", "arguments")
    // 线上 arguments 是 JSON 字符串:能解析就解码成结构(展示/摘要都更友好)
    val input = maybeDecodeJson(rawInput)
    val summary = viewMap.pickString("summary", "title", "label")
        ?: preview(summarySeed(name, input), SummaryPreviewMax)
    return ChatNode.Tool(
        seq = event.seq,
        type = event.type,
        toolName = name,
        callId = callId,
        input = input,
        summary = summary,
        status = ToolStatus.Running,
        callSeq = event.seq,
        time = event.time,
        callTime = event.time,
        producedPaths = producedPathsOfView(viewMap)
    )
}

/** 产出文件路径(web producedPaths):call view 的渲染意图 —— card='diff' 或
 * card='generic' 且 kind='edit' 时,locations[].path 即产出文件 */
private fun producedPathsOfView(viewMap: JsonObject?): List<String> {
    if (viewMap == null) return emptyList()
    val card = viewMap.str("card")
    val isMutation = card == "diff" || (card == "generic" && viewMap.str("kind") == "edit")
    if (!isMutation) return emptyList()
    val locations = viewMap["locations"] as? JsonArray ?: return emptyList()
    return locations.mapNotNull { (it as? JsonObject)?.str("path")?.takeIf(String::isNotEmpty) }
}

private fun buildToolResult(event: SessionEvent, viewMap: JsonObject?): ChatNode.Tool {
    val name = pick(event.data, viewMap, "toolName", "name", "tool", "tool_name") ?: "tool"
    val callId = pick(event.data, viewMap, "callId", "call_id", "id")
        ?: resultCallId(event.data)
    val output = pickValue(event.data, viewMap, "output", "result", "value", "data")
        ?: resultText(event.data)?.let(::JsonPrimitive)
    val isError = resultIsError(event.data)
    val code = errorCodeOf(event.data, viewMap)
    // error 文本:message 优先;isError 的输出提升仅限非中断结果(中断卡不显示
    // 红错误框,输出文本原样留在输出区 —— 对齐 web stopped 语义)
    val error = errorOf(event.data, viewMap)
        ?: if (isError && !isInterruptCode(code)) {
            (output as? JsonPrimitive)?.contentOrNull?.takeIf(String::isNotEmpty)
        } else {
            null
        }
    val status = statusOf(code, error)
    val summary = viewMap.pickString("summary", "title", "label")
        ?: preview(output ?: error?.let(::JsonPrimitive), SummaryPreviewMax)
    return ChatNode.Tool(
        seq = event.seq,
        type = event.type,
        toolName = name,
        callId = callId,
        output = output,
        error = error,
        summary = summary,
        status = status,
        resultSeq = event.seq,
        time = event.time,
        resultTime = event.time
    )
}

/** 合并配对:卡出现在 call 位置(seq=call),状态与输出取结果侧;
 * 摘要优先保留 call 侧(命令/参数预览比输出首行更稳定可读) */
private fun mergeCallResult(call: ChatNode.Tool, result: ChatNode.Tool): ChatNode.Tool =
    call.copy(
        toolName = if (result.toolName != "tool") result.toolName else call.toolName,
        callId = call.callId ?: result.callId,
        output = result.output,
        error = result.error,
        summary = call.summary ?: result.summary,
        status = result.status,
        callSeq = call.callSeq ?: call.seq,
        resultSeq = result.resultSeq ?: result.seq,
        callTime = call.callTime ?: call.time,
        resultTime = result.resultTime
    )

// ───────────────────────────── assistant/chunk 流式折叠 ─────────────────────────────

private fun chunkOf(data: JsonObject): JsonObject? = data["chunk"] as? JsonObject

private class FoldedBlock(var kind: String) {
    val text = StringBuilder()
}

private class ChunkGroupMeta(val firstSeq: Int, var lastSeq: Int, var time: Double? = null)

/**
 * 事件序后的 chunk 游 → 直播节点。同 (turn,step) 已有 assistant/message 定稿的游
 * 直接丢弃(定稿渲染更完整);否则产出 streaming 节点:若该步之后出现过任何定界事件
 * (turn/end、step/end、user/message、llm/retry…),说明这一步被中断/翻页,
 * 标记为非流式(静态残留)。
 */
private fun foldChunks(sorted: List<EventNodeInput>): List<ChatNode> {
    val groups = LinkedHashMap<String, MutableList<FoldedBlock>>()
    val metas = HashMap<String, ChunkGroupMeta>()
    for (input in sorted) {
        val event = input.event
        if (event.type != EventAssistantChunk) continue
        val chunk = chunkOf(event.data) ?: continue
        val key = stepKeyOf(event.data) ?: continue
        val blocks = groups.getOrPut(key) { mutableListOf() }
        val meta = metas.getOrPut(key) { ChunkGroupMeta(event.seq, event.seq) }
        meta.lastSeq = event.seq
        if (meta.time == null) meta.time = event.time
        val index = chunk.intOf("index") ?: blocks.size
        when (chunk.str("type")) {
            "block-start" -> {
                while (blocks.size <= index) blocks.add(FoldedBlock(""))
                blocks[index] = FoldedBlock(chunk.str("blockType") ?: "text")
            }

            "text-delta", "reasoning-delta" -> {
                while (blocks.size <= index) blocks.add(FoldedBlock(""))
                if (blocks[index].kind.isEmpty()) {
                    blocks[index] = FoldedBlock(
                        if (chunk.str("type") == "reasoning-delta") "reasoning" else "text"
                    )
                }
                chunk.str("text")?.let(blocks[index].text::append)
            }

            "block-end" -> {
                // block-end 携带整块定稿文本(权威);有机会就替换累计值
                val block = chunk["block"] as? JsonObject
                val text = block?.str("text")
                if (!text.isNullOrEmpty()) {
                    while (blocks.size <= index) blocks.add(FoldedBlock(""))
                    blocks[index].text.clear()
                    blocks[index].text.append(text)
                }
            }
            // tool-call-delta:文本流不消费(后续 tool/call 事件自成卡片)
        }
    }
    if (groups.isEmpty()) return emptyList()
    // 定稿集合 + 定界事件最大 seq
    val finalized = HashSet<String>()
    var maxSettleSeq = -1
    for (input in sorted) {
        val event = input.event
        if (event.type == EventAssistantMessage) {
            stepKeyOf(event.data)?.let(finalized::add)
            maxSettleSeq = event.seq
        } else if (event.type in SettleTypes) {
            maxSettleSeq = event.seq
        }
    }
    val nodes = ArrayList<ChatNode>()
    for ((key, blocks) in groups) {
        if (key in finalized) continue
        val meta = metas.getValue(key)
        val streaming = meta.lastSeq >= maxSettleSeq
        // seq 用 firstSeq(块首事件):流式期间每个 delta 都会把 lastSeq 推高,
        // 若节点 seq 跟着变,列表 key 每帧一换 → item 状态全量销毁重建
        // (think 展开态丢失、尾随滚动重置);firstSeq 在块生命周期内不变,排序仍单调
        val reasoning = blocks
            .filter { it.kind == "reasoning" && it.text.isNotEmpty() }
            .joinToString("\n") { it.text.toString() }
        val text = blocks
            .filter { it.kind == "text" && it.text.isNotEmpty() }
            .joinToString("\n") { it.text.toString() }
        if (reasoning.isNotEmpty()) {
            nodes.add(
                ChatNode.Think(
                    seq = meta.firstSeq,
                    type = "assistant/chunk/reasoning",
                    text = reasoning,
                    streaming = streaming,
                    time = meta.time
                )
            )
        }
        if (text.isNotEmpty()) {
            nodes.add(
                ChatNode.AssistantMessage(
                    seq = meta.firstSeq,
                    type = "assistant/chunk",
                    text = text,
                    streaming = streaming,
                    time = meta.time
                )
            )
        }
    }
    return nodes
}

private val SettleTypes = setOf("turn/end", "step/end", "user/message", "llm/retry")

private fun stepKeyOf(data: JsonObject): String? {
    val turn = data.intOf("turn") ?: return null
    val step = data.intOf("step") ?: return null
    return "$turn:$step"
}

// ───────────────────────────── tool/result 嵌套形状提取(真实日志:data.message.content[].tool-result) ─────────────────────────────

private fun resultCallId(data: JsonObject): String? {
    val message = data["message"] as? JsonObject ?: return null
    (message["source"] as? JsonObject)?.str("callId")?.takeIf(String::isNotEmpty)
        ?.let { return it }
    val content = message["content"] as? JsonArray ?: return null
    for (block in content) {
        (block as? JsonObject)?.str("toolCallId")?.takeIf(String::isNotEmpty)
            ?.let { return it }
    }
    return null
}

/** tool-result 内容文本:content[].content[] 的 text 块拼接 */
private fun resultText(data: JsonObject): String? {
    val message = data["message"] as? JsonObject ?: return null
    val content = message["content"] as? JsonArray ?: return null
    val parts = ArrayList<String>()
    for (block in content) {
        val obj = block as? JsonObject ?: continue
        if (obj.str("type") != "tool-result") continue
        val inner = obj["content"] as? JsonArray ?: continue
        for (piece in inner) {
            val p = piece as? JsonObject ?: continue
            if (p.str("type") != "text") continue
            p.str("text")?.takeIf(String::isNotEmpty)?.let(parts::add)
        }
    }
    return if (parts.isEmpty()) null else parts.joinToString("\n")
}

private fun resultIsError(data: JsonObject): Boolean {
    val message = data["message"] as? JsonObject ?: return false
    val content = message["content"] as? JsonArray ?: return false
    return content.any { block ->
        val obj = block as? JsonObject
        obj?.str("type") == "tool-result" &&
            (obj.get("isError") as? JsonPrimitive)?.booleanOrNull == true
    }
}
/** JSON 字符串 → 结构(解析失败原样返回;数字/布尔等标量不受影响) */
private fun maybeDecodeJson(value: JsonElement?): JsonElement? {
    val raw = (value as? JsonPrimitive)?.contentOrNull ?: return value
    val s = raw.trim()
    if (s.isEmpty() || (s[0] != '{' && s[0] != '[')) return value
    return runCatching { Json.parseToJsonElement(s) }.getOrDefault(value)
}

/** 摘要种子:bash 类工具取 command、read 类取 path,其余原样交给 [preview] 格式化 */
private fun summarySeed(name: String, input: JsonElement?): JsonElement? {
    val obj = input as? JsonObject ?: return input
    for (key in listOf("command", "cmd", "script")) {
        obj.str(key)?.takeIf(String::isNotEmpty)?.let { return JsonPrimitive(it) }
    }
    val n = name.lowercase()
    if ("read" in n || "glob" in n || "grep" in n) {
        for (key in listOf("path", "pattern", "file_path", "filePath")) {
            obj.str(key)?.takeIf(String::isNotEmpty)?.let { return JsonPrimitive(it) }
        }
    }
    return input
}

// ───────────────────────────── 普通事件 → 节点 ─────────────────────────────

private fun nodesFor(event: SessionEvent): List<ChatNode> {
    val type = event.type
    val data = event.data
    if (type == EventUserMessage) {
        // source.kind != 'user' 的注入上下文不进主聊天流(与 P2 行为一致;
        // Flutter 的 ContextRow 展示属后续阶段)
        val kind = data.userSourceKind()
        if (kind != null && kind != "user") return emptyList()
        val text = data.extractEventText()
        val images = extractImageRefs(data)
        if (text.isEmpty() && images.isEmpty()) return emptyList()
        return listOf(
            ChatNode.UserMessage(seq = event.seq, type = type, text = text, images = images, time = event.time)
        )
    }
    if (type == EventAssistantMessage) {
        val text = data.extractEventText()
        val think = reasoningText(data)
        val images = extractImageRefs(data)
        // 消息反馈寻址键:data.id 优先,data.message.id 兜底(防御两种形状)
        val messageId = data.str("id") ?: (data["message"] as? JsonObject)?.str("id")
        val nodes = ArrayList<ChatNode>(2)
        if (!think.isNullOrEmpty()) {
            nodes.add(
                ChatNode.Think(seq = event.seq, type = "$type/reasoning", text = think, time = event.time)
            )
        }
        if (text.isNotEmpty() || images.isNotEmpty()) {
            nodes.add(
                ChatNode.AssistantMessage(
                    seq = event.seq,
                    type = type,
                    text = text,
                    images = images,
                    messageId = messageId,
                    time = event.time
                )
            )
        }
        return nodes
    }
    if (type == EventAssistantChunk) {
        // 无 data.chunk 的防御形状:按文本渲染为普通助手消息
        val text = data.extractEventText()
        if (text.isEmpty()) return emptyList()
        return listOf(
            ChatNode.AssistantMessage(seq = event.seq, type = type, text = text, time = event.time)
        )
    }
    if ((type.startsWith("assistant/") && ("reasoning" in type || "think" in type)) ||
        type == "think" || type.startsWith("think/")
    ) {
        val text = eventTextOf(data)
        if (text.isNullOrEmpty()) return emptyList()
        return listOf(ChatNode.Think(seq = event.seq, type = type, text = text, time = event.time))
    }
    if (type.startsWith("todo")) {
        val items = todoItems(data)
        if (items.isEmpty()) return emptyList()
        return listOf(ChatNode.Todo(seq = event.seq, type = type, items = items, time = event.time))
    }
    if (type.startsWith("compaction")) {
        return listOf(
            ChatNode.Compaction(
                seq = event.seq,
                type = type,
                kind = type.substringAfterLast('/'),
                summary = pick(data, null, "summary", "text", "message"),
                messages = data.intOf("messages") ?: data.intOf("messageCount"),
                time = event.time
            )
        )
    }
    if (type == "llm/retry" || type.startsWith("llm/retry")) {
        if (type == "llm/retry-started") return emptyList() // 与 llm/retry 重复,只留一行
        return listOf(
            ChatNode.Retry(
                seq = event.seq,
                type = type,
                reason = retryReason(data),
                attempt = data.intOf("attempt") ?: data.intOf("retryCount") ?: data.intOf("retry"),
                maxRetries = data.intOf("maxRetries") ?: data.intOf("max_retries"),
                time = event.time
            )
        )
    }
    if (type == EventTurnEnd) {
        return turnEndNodes(event)
    }
    if (type == "turn/error" || type.startsWith("turn/error")) {
        val message = pick(data, null, "message", "error", "text") ?: type
        return listOf(ChatNode.Error(seq = event.seq, type = type, message = message, time = event.time))
    }
    // 未知兜底收窄(镜像 web fallback.ts):只有 surface 三类型且
    // surfaceOp=='append' 但未被本提取器认识的情形,才显示兜底卡;
    // 其余未知类型一律不可见(web:未注册节点的类型根本不进时间线)
    if (type == EventUserMessage || type == EventAssistantMessage || type == "tool/result") {
        if (event.surfaceOp == "append" && data.isNotEmpty()) {
            return listOf(ChatNode.Unknown(seq = event.seq, type = type, data = data, time = event.time))
        }
        return emptyList()
    }
    // 协议分隔符/内部管道事件不在主聊天流占位;其余未知事件同样不可见(log-only)
    return emptyList()
}

/** llm/retry 失败原因:顶层 reason/message/error,或 failure.message(线上形状) */
private fun retryReason(data: JsonObject): String? {
    pick(data, null, "reason", "message", "error")?.let { return it }
    return (data["failure"] as? JsonObject)?.str("message")?.takeIf(String::isNotEmpty)
}

/**
 * turn/end → 提示节点(dsh 0.1.0-rc.6 权威形状:data.reason.kind ∈
 * completed|aborted|blocked|error|max-tokens|interrupted)。
 * completed/blocked 不占位;其余终态各一行,给中断/异常轮次一个明确交代。
 * 文案不本地化(提取器是纯函数层):title 用语义键,UI 层映射 string resources。
 */
private fun turnEndNodes(event: SessionEvent): List<ChatNode> {
    val reason = event.data["reason"] as? JsonObject
    return when (reason?.str("kind")) {
        "aborted" -> listOf(
            ChatNode.Notice(
                seq = event.seq,
                type = event.type,
                title = NoticeTurnStopped,
                detail = abortedDetail(reason),
                time = event.time
            )
        )

        "error" -> listOf(
            ChatNode.Error(
                seq = event.seq,
                type = event.type,
                message = failureText(reason["error"]),
                time = event.time
            )
        )

        "max-tokens" -> listOf(
            ChatNode.Notice(
                seq = event.seq,
                type = event.type,
                title = NoticeMaxTokens,
                time = event.time
            )
        )

        "interrupted" -> listOf(
            ChatNode.Notice(
                seq = event.seq,
                type = event.type,
                title = NoticeSessionInterrupted,
                detail = NoticeSessionInterruptedDetail,
                time = event.time
            )
        )

        else -> emptyList() // completed / blocked / 未知变体:不占位
    }
}

/** 提示节点 title/detail 的语义键(UI 层映射本地化文案;提取器不持 Android 资源) */
const val NoticeTurnStopped = "turn_stopped"
const val NoticeMaxTokens = "max_tokens"
const val NoticeSessionInterrupted = "session_interrupted"
const val NoticeSessionInterruptedDetail = "session_interrupted_detail"

/** 轮次失败兜底语义键(failureText 无文本可用时返回;UI 层映射本地化文案) */
const val FailureTurnFailed = "turn_failed"

/** aborted 终止原因(TurnEndCancelCause:user|parent|hook|disposed|legacy) */
private fun abortedDetail(reason: JsonObject): String? {
    val cause = reason["reason"] as? JsonObject ?: return null
    return when (cause.str("kind")) {
        "user" -> "user"
        "parent" -> "parent"
        "hook" -> cause.str("reason")?.takeIf(String::isNotEmpty)?.let { "hook:$it" } ?: "hook"
        "disposed" -> "disposed"
        else -> null // legacy 等粗粒度记录
    }
}

/** 轮次失败文本(reason.error = LlmFailure {message, code};code 为 UNKNOWN 时不拼,避免噪音) */
private fun failureText(failure: JsonElement?): String {
    val obj = failure as? JsonObject
    if (obj != null) {
        val message = obj.str("message")?.takeIf(String::isNotEmpty)
        val code = obj.str("code")?.takeIf(String::isNotEmpty)
        if (message != null) {
            return if (code != null && code != "UNKNOWN") "$message ($code)" else message
        }
        if (code != null) return code
    }
    return FailureTurnFailed
}

/** 提取 assistant/message 中 reasoning 块的文本(拆成 think 节点) */
private fun reasoningText(data: JsonObject): String? {
    val parts = ArrayList<String>()
    for (block in contentBlocksOf(data)) {
        val obj = block as? JsonObject ?: continue
        if (obj.str("type") != "reasoning") continue
        val text = obj.str("text") ?: obj.str("summary") ?: obj.str("content")
        text?.takeIf(String::isNotEmpty)?.let(parts::add)
    }
    return if (parts.isEmpty()) null else parts.joinToString("\n")
}

private fun contentBlocksOf(data: JsonObject): List<JsonElement> {
    var content = data["content"]
    if (content !is JsonArray) {
        content = (data["message"] as? JsonObject)?.get("content")
    }
    return (content as? JsonArray)?.toList().orEmpty()
}

/** 独立 think 事件的文本(弹性键:text/summary/content) */
private fun eventTextOf(data: JsonObject): String? {
    for (key in listOf("text", "summary", "content")) {
        data.str(key)?.takeIf(String::isNotEmpty)?.let { return it }
    }
    return null
}

private fun todoItems(data: JsonObject): List<TodoItem> {
    val raw = (data["items"] ?: data["todos"]) as? JsonArray ?: return emptyList()
    val items = ArrayList<TodoItem>()
    for (e in raw) {
        if (e is JsonPrimitive && e.isString && e.content.isNotEmpty()) {
            items.add(TodoItem(title = e.content))
        } else if (e is JsonObject) {
            // 线上形状:{content, status: pending|in_progress|completed}
            val title = e.str("title") ?: e.str("text") ?: e.str("content") ?: e.str("id") ?: "(untitled)"
            val status = e.str("status") ?: e.str("state")
            val done = e["done"]?.let { (it as? JsonPrimitive)?.booleanOrNull } == true ||
                e["completed"]?.let { (it as? JsonPrimitive)?.booleanOrNull } == true ||
                status == "done" || status == "completed"
            items.add(TodoItem(title = title, done = done))
        }
    }
    return items
}

/** 防御式图片引用提取:data.content[](或 data.images[])中 type=='image' 的块。
 * 字段不齐(attachmentId/mediaType/bytes/width/height 任一缺席)整块跳过 */
private fun extractImageRefs(data: JsonObject): List<ImageAttachmentRef> {
    val candidates = ArrayList<JsonElement>()
    (data["content"] as? JsonArray)?.let(candidates::addAll)
    (data["images"] as? JsonArray)?.let(candidates::addAll)
    if (candidates.isEmpty()) return emptyList()
    val refs = ArrayList<ImageAttachmentRef>()
    for (c in candidates) {
        val obj = c as? JsonObject ?: continue
        val attachmentId = obj.str("attachmentId")
        val isImage = obj.str("type") == "image" || attachmentId != null
        if (!isImage || attachmentId == null) continue
        val mediaType = obj.str("mediaType") ?: continue
        val bytes = obj.longOf("bytes") ?: continue
        val width = obj.intOf("width") ?: continue
        val height = obj.intOf("height") ?: continue
        refs.add(
            ImageAttachmentRef(
                attachmentId = attachmentId,
                mediaType = mediaType,
                bytes = bytes,
                width = width,
                height = height,
                name = obj.str("name")
            )
        )
    }
    return refs
}

// ───────────────────────────── 状态判定与弹性取值 ─────────────────────────────

/** 中断类错误码全集(dsh 0.1.0-rc.6):运行中被取消 / 派发前被跳过 /
 * 崩溃修复补记的两种未知结局。web 客户端合成的 'interrupted' 同义 */
private val InterruptCodes = setOf(
    "ABORTED",
    "ABORTED_BEFORE_DISPATCH",
    "TOOL_OUTCOME_UNKNOWN",
    "TOOL_NOT_STARTED",
    "INTERRUPTED"
)

private fun isInterruptCode(code: String?): Boolean =
    code != null && code.uppercase() in InterruptCodes

/** 状态判定:data.error.code 权威 —— 中断类 → interrupted,其余 code → failed;
 * 无 code 时 error 文本 → failed,否则 success。
 * view 词表(dsh-tools presentation)不含 status/interrupted/ok 字段,不参与判定 */
private fun statusOf(code: String?, error: String?): ToolStatus = when {
    code != null -> if (isInterruptCode(code)) ToolStatus.Interrupted else ToolStatus.Failed
    error != null -> ToolStatus.Failed
    else -> ToolStatus.Success
}

/** 错误文本:error 字段可能是字符串或 {message}(只认 message —— {name, code}
 * 形状是中断类错误,不进红错误框) */
private fun errorOf(data: JsonObject, viewMap: JsonObject?): String? {
    for (src in listOf(viewMap, data)) {
        val error = src?.get("error") ?: continue
        if (error is JsonPrimitive) {
            error.contentOrNull?.takeIf(String::isNotEmpty)?.let { return it }
        } else if (error is JsonObject) {
            error.str("message")?.takeIf(String::isNotEmpty)?.let { return it }
        }
    }
    return null
}

/** 错误码提取:tool/result 的 data.error = {name, code}(dsh 权威形状) */
private fun errorCodeOf(data: JsonObject, viewMap: JsonObject?): String? {
    for (src in listOf(data, viewMap)) {
        val error = src?.get("error") ?: continue
        if (error is JsonObject) {
            error.str("code")?.takeIf(String::isNotEmpty)?.let { return it }
        } else if (error is JsonPrimitive) {
            error.contentOrNull?.takeIf(String::isNotEmpty)?.let { return it }
        }
    }
    return null
}

/** 弹性取字符串:工具卡本质信息以 event.data 为准,view 仅兜底(渲染增强) */
private fun pick(data: JsonObject, viewMap: JsonObject?, vararg keys: String): String? {
    for (key in keys) {
        data.str(key)?.takeIf(String::isNotEmpty)?.let { return it }
    }
    if (viewMap != null) {
        for (key in keys) {
            viewMap.str(key)?.takeIf(String::isNotEmpty)?.let { return it }
        }
    }
    return null
}

/** 弹性取值:同上,data 优先、view 兜底 */
private fun pickValue(data: JsonObject, viewMap: JsonObject?, vararg keys: String): JsonElement? {
    for (key in keys) {
        data[key]?.let { return it }
    }
    if (viewMap != null) {
        for (key in keys) {
            viewMap[key]?.let { return it }
        }
    }
    return null
}

private fun JsonObject?.pickString(vararg keys: String): String? {
    if (this == null) return null
    for (key in keys) {
        str(key)?.takeIf(String::isNotEmpty)?.let { return it }
    }
    return null
}

private fun JsonObject.str(key: String): String? =
    (this[key] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.intOf(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull

private fun JsonObject.longOf(key: String): Long? =
    (this[key] as? JsonPrimitive)?.contentOrNull?.toLongOrNull()

/** 摘要预览(截断防爆行) */
private fun preview(value: JsonElement?, max: Int): String? {
    if (value == null) return null
    val s = formatNodeValue(value)
    return if (s.length <= max) s else s.take(max) + "…"
}

private const val SummaryPreviewMax = 60

private const val EventUserMessage = "user/message"
private const val EventAssistantMessage = "assistant/message"
private const val EventAssistantChunk = "assistant/chunk"
private const val EventTurnStart = "turn/start"
private const val EventTurnEnd = "turn/end"
private const val EventStepEnd = "step/end"

private val PrettyJson = Json { prettyPrint = true }

/** 值 → 可读文本(字符串直出,标量 toString,结构走缩进 JSON);工具卡输入/输出展示用 */
fun formatNodeValue(value: JsonElement): String {
    if (value is JsonPrimitive) {
        if (value.isString) return value.content
        return value.content
    }
    return runCatching { PrettyJson.encodeToString(JsonElement.serializer(), value) }
        .getOrDefault(value.toString())
}
