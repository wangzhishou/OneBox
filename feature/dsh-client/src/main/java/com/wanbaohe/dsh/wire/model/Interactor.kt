package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/**
 * P3 交互帧域 wire 模型(DSH-PROTOCOL §1/§4/§5):
 * 审批/问答载荷、respond 信封与回执、队列快照条目、updateQueue/cancel 载荷、jobs 快照条目。
 *
 * 契约要点:
 * - approval/question 是可应答 ServerRequest:应答 = POST /api/respond,
 *   body 为 client-response 信封,rpcId 原样回显;第一个到达的应答占有请求
 * - approval 应答值:{sessionId, approvalId, outcome: allowed-once|rejected}
 * - question 应答值:{sessionId, answer:{answers:[{id, selected, custom?}]}},
 *   label 必须精确匹配请求里的;空 custom 拒(缺席与空串不同,空串一律不发送)
 * - respond 回执:{accepted} 或 {accepted:false, reason: not-pending|bad-response}
 * - session/queue 与 session/jobs 均为完整快照(整帧收敛,直接替换)
 */

// ───────────────────────────── 问答载荷 ─────────────────────────────

/** 问答选项(label 是必须精确回传的字符串) */
@Serializable
data class QuestionOption(
    val label: String,
    val description: String? = null
)

/** 结构化问答单题(question/requested 帧的 questions 元素) */
@Serializable
data class AskUserQuestionItem(
    val id: String,
    val question: String,
    val header: String? = null,
    val detail: String? = null,
    val options: List<QuestionOption>? = null,
    val multiSelect: Boolean? = null,
    val intent: JsonElement? = null
)

// ───────────────────────────── respond 信封与回执 ─────────────────────────────

/**
 * 上行应答信封(ClientResponse):`{type:'client-response', rpcId, result}`。
 * rpcId 不是 mint 的 —— 逐字回显可应答帧信封上的 rpcId(重连重放同 rpcId,天然幂等)。
 */
@Serializable
data class ClientResponse(
    val type: String = "client-response",
    val rpcId: String,
    val result: JsonObject
) {
    companion object {
        /** 以帧的 rpcId 构造应答信封,业务值装 result.value 槽 */
        fun mint(rpcId: String, value: JsonObject): ClientResponse = ClientResponse(
            rpcId = rpcId,
            result = buildJsonObject {
                put("ok", true)
                put("value", value)
            }
        )
    }
}

/** respond 回执折叠:{accepted} | {accepted:false, reason} */
@Serializable
data class RespondReceipt(
    val accepted: Boolean,
    val reason: String? = null
) {
    /** not-pending:host 侧已 resolved(另一端先答/turn 取消)—— 权威清场信号 */
    val isLate: Boolean get() = !accepted && reason == ReasonNotPending

    /** bad-response:应答畸形被服务端权威拒绝(本地预校验的兜底) */
    val isMalformed: Boolean get() = !accepted && reason == ReasonBadResponse

    companion object {
        const val ReasonNotPending = "not-pending"
        const val ReasonBadResponse = "bad-response"
    }
}

// ───────────────────────────── 队列 / 任务快照 ─────────────────────────────

/**
 * 队列快照条目(session/queue 帧的 items 元素)。
 * 字段全可选:单条畸形不拖垮整帧(快照收敛语义下丢整帧比丢单条更糟)。
 */
@Serializable
data class QueueItem(
    val id: String? = null,
    val placement: String? = null,
    val message: JsonObject? = null
) {
    /** 待处理输入(placement == 'queued';steering/context 项不是排队消息) */
    val isQueued: Boolean get() = placement == PlacementQueued

    companion object {
        const val PlacementQueued = "queued"
    }
}

/** 取队列项的首个文本块作预览;无文本块返回 null(由 UI 决定占位文案) */
fun QueueItem.textPreview(): String? {
    val content = message?.get("content") as? JsonArray ?: return null
    for (block in content) {
        val obj = block as? JsonObject ?: continue
        if ((obj["type"] as? JsonPrimitive)?.contentOrNull != "text") continue
        val text = (obj["text"] as? JsonPrimitive)?.contentOrNull
        if (!text.isNullOrEmpty()) return text
    }
    return null
}

/**
 * 后台任务快照条目(session/jobs 帧的 jobs 元素,对齐 Flutter TaskView)。
 * 缺省值防御:单条畸形不拖垮整帧。
 */
@Serializable
data class TaskView(
    val id: String = "",
    val kind: String = "",
    val label: String = "",
    val status: String? = null,
    val detail: String? = null,
    val startedAt: Long = 0,
    val finishedAt: Long? = null
) {
    /** 活跃(running/stopping)判定;非字符串一律按终态(防御 wire 变化) */
    val isActive: Boolean get() = status == StatusRunning || status == StatusStopping

    companion object {
        const val StatusRunning = "running"
        const val StatusStopping = "stopping"
    }
}

// ───────────────────────────── updateQueue / cancel 载荷 ─────────────────────────────

/** session.updateQueue 的 action 体:splice 语义,kind: remove(按 MessageId 寻址删除) */
@Serializable
data class QueueAction(val kind: String) {
    companion object {
        val Remove = QueueAction("remove")
    }
}

/** session.updateQueue 回值(accepted 缺席视为未接受) */
@Serializable
data class SessionUpdateQueueValue(val accepted: Boolean = false)

/** session.cancel 回值:只中止当前 turn,保留 pending inbox */
@Serializable
data class SessionCancelValue(val accepted: Boolean = false)
