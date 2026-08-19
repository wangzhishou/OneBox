package com.wanbaohe.dsh.wire

import com.wanbaohe.dsh.wire.model.AskUserQuestionItem
import com.wanbaohe.dsh.wire.model.QueueItem
import com.wanbaohe.dsh.wire.model.TaskView
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * 下行帧契约模型(DSH-PROTOCOL §4),以 dsh-host-apiproxy 的 events.d.ts 为唯一事实源。
 *
 * 帧是 `{type: ...}` 判别联合:sealed class + [JsonClassDiscriminator]("type")。
 * payload 内未建模的嵌套字段(如 session/event 的 event)用 [JsonObject]/[JsonElement]
 * 占位,P5 再细化;queue/jobs 条目已在 P3 细化(wire/model/Interactor.kt)。
 *
 * 解析纪律:未知 type / 字段畸形抛 SerializationException,
 * 由连接层捕获后走 protocolErrors 上报,不杀 socket。
 */

// ───────────────────────────── MuxFrame(全会话聚合流 /api/events.mux) ─────────────────────────────

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class MuxFrame {

    /** 原始会话事件透传;[view] 是主机算好的工具调用/结果渲染意图(可选,不落盘) */
    @Serializable
    @SerialName("session/event")
    data class SessionEvent(
        val sessionId: String,
        val event: JsonObject,
        val view: JsonElement? = null
    ) : MuxFrame()

    /** 订阅控制帧:流打开时每个 attached 会话一帧,[lastSeq] 为当前水位 */
    @Serializable
    @SerialName("session/subscribed")
    data class SessionSubscribed(
        val sessionId: String,
        val lastSeq: Int
    ) : MuxFrame()

    /** 审批请求(可应答帧,rpcId 在信封层) */
    @Serializable
    @SerialName("approval/requested")
    data class ApprovalRequested(
        val sessionId: String,
        val approvalId: String,
        val toolName: String,
        val callId: String? = null,
        val reason: String? = null
    ) : MuxFrame()

    /** 审批结果;[outcome] 结构随工具而异,占位 JsonElement */
    @Serializable
    @SerialName("approval/resolved")
    data class ApprovalResolved(
        val sessionId: String,
        val approvalId: String,
        val outcome: JsonElement
    ) : MuxFrame()

    /** 结构化问答请求(可应答帧);[questions] 为一批 [AskUserQuestionItem] */
    @Serializable
    @SerialName("question/requested")
    data class QuestionRequested(
        val sessionId: String,
        val questions: List<AskUserQuestionItem>
    ) : MuxFrame()

    /** 问答结果;[questionRpcId] 对应请求帧的 rpcId,[outcome] 占位 */
    @Serializable
    @SerialName("question/resolved")
    data class QuestionResolved(
        val sessionId: String,
        val questionRpcId: String,
        val outcome: JsonElement
    ) : MuxFrame()

    /** 待处理收件箱完整快照(整帧收敛,高水位覆盖低水位);条目模型见 [QueueItem] */
    @Serializable
    @SerialName("session/queue")
    data class SessionQueue(
        val sessionId: String,
        val items: List<QueueItem>
    ) : MuxFrame()

    /** 后台任务完整快照(整帧收敛);条目模型见 [TaskView] */
    @Serializable
    @SerialName("session/jobs")
    data class SessionJobs(
        val sessionId: String,
        val jobs: List<TaskView>
    ) : MuxFrame()

    /** 通用投影单元变更帧:同 [key] 高 [seq] 覆盖低 seq;会话标题走此通道 */
    @Serializable
    @SerialName("session/projection")
    data class SessionProjection(
        val sessionId: String,
        val key: String,
        val value: JsonElement,
        val seq: Int
    ) : MuxFrame()

    /** 流级错误(不杀 socket,业务侧自行处置) */
    @Serializable
    @SerialName("stream/error")
    data class StreamError(
        val error: RpcError
    ) : MuxFrame()
}

/** 结构化问答单题模型已移至 wire/model/Interactor.kt(P3 交互帧域) */

// ───────────────────────────── HostFrame(主机级流 /api/events.host) ─────────────────────────────

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class HostFrame {

    /** 会话创建;[blank] 表示空白会话(尚未首发 prompt) */
    @Serializable
    @SerialName("host/session-added")
    data class SessionAdded(
        val sessionId: String,
        val blank: Boolean,
        val parentSessionId: String? = null,
        val origin: String? = null,
        val cwd: String? = null,
        val agentPreset: String? = null
    ) : HostFrame()

    /** 会话销毁 */
    @Serializable
    @SerialName("host/session-removed")
    data class SessionRemoved(
        val sessionId: String
    ) : HostFrame()

    /** 会话运行状态翻转 */
    @Serializable
    @SerialName("host/session-status")
    data class SessionStatus(
        val sessionId: String,
        val running: Boolean
    ) : HostFrame()

    /** 会话级 agent 错误 */
    @Serializable
    @SerialName("host/agent-error")
    data class AgentError(
        val sessionId: String,
        val message: String
    ) : HostFrame()

    /** workspace 变更;WorkspaceView 结构 P2+ 细化 */
    @Serializable
    @SerialName("host/workspace-changed")
    data class WorkspaceChanged(
        val workspace: JsonObject
    ) : HostFrame()

    /** workspace 移除 */
    @Serializable
    @SerialName("host/workspace-removed")
    data class WorkspaceRemoved(
        val workspaceId: String
    ) : HostFrame()

    /** workspace 排序变更(完整 id 列表,整帧收敛) */
    @Serializable
    @SerialName("host/workspace-order-changed")
    data class WorkspaceOrderChanged(
        val workspaceIds: List<String>
    ) : HostFrame()

    /** 归档会话集变更(完整 id 列表,整帧收敛) */
    @Serializable
    @SerialName("host/archived-sessions-changed")
    data class ArchivedSessionsChanged(
        val archivedSessionIds: List<String>
    ) : HostFrame()

    /** 远程端点转发事件(重连不重放,消费端自行重拉) */
    @Serializable
    @SerialName("host/remote-event")
    data class RemoteEvent(
        val event: String,
        val args: List<JsonElement>
    ) : HostFrame()

    /** 流级错误(不杀 socket) */
    @Serializable
    @SerialName("stream/error")
    data class StreamError(
        val error: RpcError
    ) : HostFrame()
}

/** 带 rpcId 的 mux 帧(rpcId 在信封层,payload 变体里没有):审批/问答 UI 的数据源 */
data class AddressedMuxFrame(
    val rpcId: String,
    val frame: MuxFrame
)
