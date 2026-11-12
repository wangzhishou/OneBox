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

package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 交互帧域 wire 模型(DSH 0.1.5-rc.2):问答载荷、队列/任务条目、queue/cancel 回值。
 *
 * **审批与问答不再走 `POST /api/respond`**:它们是 `$events` 流上的 waterfall
 * 帧([com.wanbaohe.dsh.wire.RemoteEventFrame.Waterfall]),应答经
 * `POST /api/$events/result`([com.wanbaohe.dsh.wire.EventResultRequest])。
 * 本文件只保留纯数据结构。
 */

// ───────────────────────────── 问答载荷 ─────────────────────────────

/** 问答选项(label 是必须精确回传的字符串) */
@Serializable
data class QuestionOption(
    val label: String,
    val description: String? = null
)

/** 结构化问答单题(waterfall request.questions 的元素) */
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

/** 单题应答(label 必须与请求里的选项逐字一致;空 custom 不发送) */
@Serializable
data class AskUserQuestionAnswerItem(
    val id: String,
    val selected: List<String> = emptyList(),
    val custom: String? = null
)

/** 问答应答整体(waterfall 的 outcome value) */
@Serializable
data class AskUserQuestionAnswer(
    val answers: List<AskUserQuestionAnswerItem> = emptyList()
)

// ───────────────────────────── 队列 / 任务条目 ─────────────────────────────

/**
 * 队列快照条目(session/control 的 queue 帧 items 元素)。
 * 新协议条目是 `{id, placement, rpcId?, message:{id, content}}`。
 */
@Serializable
data class QueueItem(
    val id: String? = null,
    val placement: String? = null,
    val rpcId: String? = null,
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

/** 队列项的稳定 id(message.id 优先,回退条目 id) */
fun QueueItem.stableId(): String =
    ((message?.get("id") as? JsonPrimitive)?.contentOrNull) ?: id.orEmpty()

/** 后台任务条目(session/control 的 jobs 帧元素);缺省值防御:单条畸形不拖垮整帧 */
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

/** session/updateQueue 的 action 体:splice 语义,kind: remove(按 MessageId 寻址删除) */
@Serializable
data class QueueAction(val kind: String) {
    companion object {
        val Remove = QueueAction("remove")
    }
}

/** session/updateQueue 回值(accepted 缺席视为未接受) */
@Serializable
data class SessionUpdateQueueValue(val accepted: Boolean = false)

/** session/cancel 回值:只中止当前 turn,保留 pending inbox */
@Serializable
data class SessionCancelValue(val accepted: Boolean = false)

/** session/updateQueue 上行 request */
@Serializable
data class SessionUpdateQueueRequest(
    val sessionId: String,
    val itemId: String,
    val action: QueueAction
)

/** session/cancel 上行 request */
@Serializable
data class SessionCancelRequest(val sessionId: String)
