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

package com.wanbaohe.dsh.wire

import com.wanbaohe.dsh.wire.model.SessionEvent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

/**
 * `/api/remote.mux` 逻辑流传输帧(DSH 0.1.5-rc.2,
 * `packages/api/gateway/src/stream-protocol.ts`)。
 *
 * 一条 WebSocket 复用全部逻辑流:每条流用 `streamId` 关联,客户端发
 * [RemoteStreamClientMessage.Open] / [RemoteStreamClientMessage.Cancel],
 * Host 回 [RemoteStreamServerMessage.Item] / [Error] / [End]。
 * 旧协议里"一条 WS 一个主题"(`/api/events.mux`、`/api/events.host`)的模型已不存在。
 */

/** 上行:开一条逻辑流(`payload` 恒为 `{args:{…}}`) */
@Serializable
data class OpenStreamMessage(
    val type: String = "open",
    val streamId: String,
    val endpoint: String,
    val payload: JsonElement = JsonObject(emptyMap())
)

/** 上行:取消一条逻辑流 */
@Serializable
data class CancelStreamMessage(
    val type: String = "cancel",
    val streamId: String
)

/** 下行:一条流的三类消息(判别字段 `type`) */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class RemoteStreamServerMessage {

    /** 一个流项;`value` 缺席表示空项 */
    @Serializable
    @SerialName("item")
    data class Item(
        val streamId: String,
        val value: JsonElement = JsonNull
    ) : RemoteStreamServerMessage()

    /** 流级失败:该流终止,物理 socket 保持(其他流不受影响) */
    @Serializable
    @SerialName("error")
    data class Error(
        val streamId: String,
        val error: StreamFailure
    ) : RemoteStreamServerMessage()

    /** 流正常结束 */
    @Serializable
    @SerialName("end")
    data class End(
        val streamId: String
    ) : RemoteStreamServerMessage()
}

/** 流级失败体(与一元错误同构,但 details 恒为对象) */
@Serializable
data class StreamFailure(
    val code: String,
    val message: String,
    val details: JsonObject = JsonObject(emptyMap())
)

// ───────────────────── session/follow 帧(每会话一条流) ─────────────────────

/**
 * `session/follow` 的帧(DSH 0.1.5-rc.2 SessionFollowFrame):
 * 首帧恒为 [Snapshot](历史 + 投影 + 压缩后的助手流基线),
 * 之后是 [Event](逐条会话事件)与 [AssistantStream](助手输出增量)。
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class FollowFrame {

    /** 打开快照:开流时的一次性基线 */
    @Serializable
    @SerialName("snapshot")
    data class Snapshot(
        val header: SessionHeader,
        /** 当前日志水位;翻历史页时作为 throughSeq */
        val cursor: Int = 0,
        val records: List<SessionEventEntry> = emptyList(),
        val hasMore: Boolean = false,
        val projections: ProjectionsBlock? = null,
        val assistantStream: JsonElement? = null
    ) : FollowFrame()

    /** 一条新会话事件 */
    @Serializable
    @SerialName("event")
    data class Event(
        val event: SessionEvent
    ) : FollowFrame()

    /** 助手输出流片段(start/chunk/end) */
    @Serializable
    @SerialName("assistant-stream")
    data class AssistantStream(
        val frame: AssistantStreamFrame
    ) : FollowFrame()
}

/** 会话头(follow snapshot.header);cwd 从 host 级下沉到每会话 */
@Serializable
data class SessionHeader(
    val version: Int = 0,
    val id: String,
    val createdAt: Double = 0.0,
    val cwd: String? = null,
    val parentSession: String? = null,
    val isSeeded: Boolean = false,
    val origin: String? = null,
    val delegationDepth: Int? = null,
    val agentPreset: String? = null
)

/** 历史页记录的唯一变体:一条会话事件 */
@Serializable
data class SessionEventEntry(
    val type: String = "event",
    val event: SessionEvent
)

/** 投影水位块(旧名 SessionProjectionsBlock 的 wire 同形) */
@Serializable
data class ProjectionsBlock(
    val asOfSeq: Int = -1,
    val values: Map<String, JsonElement> = emptyMap()
)

/** 助手输出增量帧(进程内状态,不落盘;committed 后由事件补全) */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class AssistantStreamFrame {

    @Serializable
    @SerialName("start")
    data class Start(
        val attemptId: String = "",
        val revision: Int = 0,
        val startedAfterSeq: Int = 0,
        val turn: Int = 0,
        val step: Int = 0
    ) : AssistantStreamFrame()

    /** 增量块;`chunk` 形状由 LLM 适配层决定,客户端按文本块尽力提取 */
    @Serializable
    @SerialName("chunk")
    data class Chunk(
        val attemptId: String = "",
        val revision: Int = 0,
        val index: Int = 0,
        val time: Double = 0.0,
        val chunk: JsonElement = JsonNull
    ) : AssistantStreamFrame()

    @Serializable
    @SerialName("end")
    data class End(
        val attemptId: String = "",
        val revision: Int = 0,
        val index: Int = 0,
        val outcome: JsonElement? = null
    ) : AssistantStreamFrame()
}

/**
 * 从 chunk 里尽力取文本:适配层可能给 `{type:'text',text}`、`{text}`、
 * 字符串本身或 `{delta}` 等形状;取不到返回 null(不抛)。
 */
fun JsonElement.assistantChunkText(): String? {
    (this as? JsonPrimitive)?.let { if (it.isString) return it.contentOrNull }
    val obj = this as? JsonObject ?: return null
    for (key in listOf("text", "delta", "content")) {
        val value = obj[key] as? JsonPrimitive ?: continue
        if (value.isString) return value.contentOrNull
    }
    return null
}

/** 从 assistant-stream end 帧判断是否已提交到某条事件(committed 表示事件会补齐内容) */
fun JsonElement?.assistantStreamCommitted(): Boolean {
    val obj = this as? JsonObject ?: return false
    return (obj["kind"] as? JsonPrimitive)?.contentOrNull == "committed"
}

/** 解析 `{ok:boolean}` 型回执(isTrue = 字段为 true) */
fun JsonObject.booleanField(name: String): Boolean =
    (this[name] as? JsonPrimitive)?.booleanOrNull ?: false
