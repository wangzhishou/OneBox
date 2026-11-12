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

import com.wanbaohe.dsh.wire.SessionEventEntry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * 会话域 wire 模型(DSH 0.1.5-rc.2 `packages/api/session-controller/src/types.ts`)。
 *
 * 与旧版(<= 0.1.1)的差异:
 * - `session.list` → `session/list`;摘要行**不再带 projections/agentPreset**,
 *   `cwd` 仍在摘要行;标题等投影要在 `session/follow` 的 snapshot 里取
 * - `session.history` → `session/page`(游标从"beforeSeq 倒序翻页"改为
 *   `throughSeq` 冻结切口 + `beforeSeq` 回退)
 * - `session.prompt` 的 request 新增必填 `requestId` 与 `mode`
 * - `session.models` → `session/modelCatalog`
 */

/**
 * 会话事件(follow 的 event 帧、历史页 records 的 event)。
 *
 * 事件日志只增不改,[seq] 是唯一排序/去重键;[data] 是新协议的 `JsonValue`
 * (不保证是对象,旧版硬编码 JsonObject 会直接反序列化失败),消费方按
 * [dataObject] 取对象视图;已知类型(user/message、assistant/message)的文本
 * 提取见 [extractEventText]。
 */
@Serializable
data class SessionEvent(
    val type: String,
    val seq: Int,
    val time: Double = 0.0,
    val data: JsonElement = JsonObject(emptyMap()),
    val surfaceOp: JsonElement? = null,
    val ignorable: Boolean? = null,
    val sourceEventSeqs: JsonElement? = null
) {
    /** data 的对象视图;非对象(字符串/数组)时为空对象 */
    val dataObject: JsonObject get() = data as? JsonObject ?: JsonObject(emptyMap())
}

/**
 * 会话摘要行(session/list 的 items 元素)。
 *
 * 0.1.5 的列表行**带投影提示**(projections.values:title/goal/tokenUsage/imageLimits…,
 * 值可能滞后但不错,asOfSeq 标明多旧),冷会话也能拿到标题等;
 * 打开会话后同一批投影由 follow 快照给出,control baseline 还会推送增量。
 *
 * [title] 是本模块的**客户端侧字段**(wire 上不存在),由 SessionStore 把投影 overlay
 * 合并进摘要后供列表/顶栏直接读取,省掉 UI 每次查表。
 */
@Serializable
data class SessionSummary(
    val sessionId: String,
    val updatedAt: Double,
    val running: Boolean,
    val blank: Boolean = false,
    val parentSessionId: String? = null,
    val origin: String? = null,
    val cwd: String? = null,
    /** 列表行投影提示(部分基线,可能滞后) */
    val projections: SessionProjectionsBlock? = null,
    /** 客户端侧:合并后的 title 投影(不参与 wire) */
    val title: String? = null
)

@Serializable
data class SessionListValue(
    val items: List<SessionSummary> = emptyList()
)

@Serializable
data class SessionListRequest(
    val cursor: String? = null
)

/**
 * session/page 的响应 value(DSH 0.1.5-rc.2 SessionPage):
 * 一页历史记录 + 是否还有更早的页 + 冻结切口处的投影整量。
 */
@Serializable
data class SessionPageValue(
    val records: List<SessionEventEntry> = emptyList(),
    val hasMore: Boolean = false,
    val projections: SessionProjectionsBlock? = null
)

/** session/page 上行:`throughSeq` 是 follow snapshot 给的切口(必填) */
@Serializable
data class SessionPageRequest(
    val address: SessionAddress,
    val throughSeq: Int,
    val beforeSeq: Int? = null,
    val maxMessages: Int? = null
)

/** session/follow 上行 */
@Serializable
data class SessionFollowRequest(
    val address: SessionAddress,
    val maxMessages: Int? = null,
    val assistantStream: Boolean? = null
)

/** 会话地址:主会话 / 子代理会话(子代理不再有独立的 history 端点) */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed class SessionAddress {

    @Serializable
    @SerialName("session")
    data class Main(val sessionId: String) : SessionAddress()

    @Serializable
    @SerialName("subagent")
    data class Subagent(
        val parentSessionId: String,
        val childSessionId: String,
        val mode: String = SubagentModeContinuable
    ) : SessionAddress() {
        companion object {
            const val MODE_ONE_SHOT = "one-shot"
            const val MODE_CONTINUABLE = SubagentModeContinuable
        }
    }
}

/** 投影水位快照(follow snapshot / page 尾页 / control baseline) */
@Serializable
data class SessionProjectionsBlock(
    val asOfSeq: Int = -1,
    val values: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class SessionCreateValue(
    val sessionId: String,
    val agentPreset: String? = null
)

/**
 * session/create 上行:显式 sessionId 可"认领"一个既有会话;
 * `cwd` 与 `workspaceId` 二选一(缺省用 Host 默认工作目录)。
 */
@Serializable
data class SessionCreateRequest(
    val workspaceId: String? = null,
    val cwd: String? = null,
    val sessionId: String? = null,
    val agentPreset: String? = null
)

/**
 * session/prompt 上行载荷(DSH 0.1.5-rc.2):
 * `requestId` 由客户端生成并被持久化在接受的用户消息上(事件 source.rpcId 回显);
 * `mode` 决定排队还是插队;content 至少一个非空白文本块或附件。
 */
@Serializable
data class SessionPromptRequest(
    val requestId: String,
    val sessionId: String,
    val mode: String = PromptModeQueue,
    val content: List<PromptContentPart>,
    val clientTimeZone: String? = null
)

const val PromptModeQueue = "queue"
const val PromptModeSteer = "steer"

/** prompt 内容块联合(判别字段 type):text 文本块 / image base64 图片块 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class PromptContentPart {

    @Serializable
    @SerialName("text")
    data class Text(
        val text: String
    ) : PromptContentPart()

    /** 图片块:base64 上行,主机把字节提升为持久引用;mediaType 为闭合枚举 */
    @Serializable
    @SerialName("image")
    data class Image(
        val mediaType: String,
        val data: String,
        val name: String? = null
    ) : PromptContentPart()
}

@Serializable
data class SessionPromptValue(
    val accepted: Boolean = false
)

/** session/rename 的响应 value:规范化后的标题 + 投影 seq(先落本地格,推送帧高 seq 覆盖) */
@Serializable
data class SessionRenameValue(
    val title: String,
    val seq: Int = -1
)

/** session/fork 的响应 value:新会话 id */
@Serializable
data class SessionForkValue(
    val sessionId: String
)

/** session/search 命中行:sessionId + 命中片段 */
@Serializable
data class SessionSearchItem(
    val sessionId: String,
    val snippet: String
)

@Serializable
data class SessionSearchValue(
    val items: List<SessionSearchItem> = emptyList(),
    val hasMore: Boolean = false
)

/** session/search 上行 */
@Serializable
data class SessionSearchRequest(val query: String)

// ─────────────────── 模型目录(session/modelCatalog) ───────────────────
// 目录类型(ModelCatalog / ModelProviderGroup / ModelCatalogModel …)定义在 Llm.kt,
// 这里只放本域的请求/回值,避免同名重复声明。

/** session/selectModel 上行 */
@Serializable
data class SessionSelectModelRequest(
    val sessionId: String,
    val provider: String,
    val model: String,
    val reasoningEffort: String? = null
)

/**
 * imageLimits 投影(主机 per-boot 常量下发,投影键 "imageLimits"):
 * 客户端在附件 intake 前按此本地预拒,省一次上行往返;投影缺席时跳过预检(服务端权威)。
 */
@Serializable
data class ImageLimitsProjection(
    val maxImageBytes: Long = 0,
    val maxImagesPerMessage: Int = 0,
    val maxMessageImageBytes: Long = 0,
    val maxImagePixels: Long = 0,
    val maxImageDimension: Int = 0,
    val mediaTypes: List<String> = emptyList()
)

/**
 * 事件文本提取(对齐 web 客户端的事件渲染):
 * data.content / data.message.content 块数组中 type=="text" 的 text 以换行拼接;
 * 无块数组时兜底 data.text 纯串;都没有返回空串。
 */
fun JsonObject.extractEventText(): String {
    var content = this["content"]
    if (content !is JsonArray) {
        content = (this["message"] as? JsonObject)?.get("content")
    }
    if (content !is JsonArray) {
        return (this["text"] as? JsonPrimitive)?.contentOrNull.orEmpty()
    }
    return content.mapNotNull { block ->
        val obj = block as? JsonObject ?: return@mapNotNull null
        if ((obj["type"] as? JsonPrimitive)?.contentOrNull != "text") return@mapNotNull null
        (obj["text"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotEmpty() }
    }.joinToString("\n")
}

/** user/message 的 source.kind(source 缺席返回 null,视为用户直发) */
fun JsonObject.userSourceKind(): String? =
    ((this["source"] as? JsonObject)?.get("kind") as? JsonPrimitive)?.contentOrNull

/** user/message 的 source.rpcId(prompt 的 requestId 回显;用于退掉本地回执) */
fun JsonObject.userSourceRpcId(): String? =
    ((this["source"] as? JsonObject)?.get("rpcId") as? JsonPrimitive)?.contentOrNull

/**
 * 摘要行的显示标题:[title](客户端侧合并的 title 投影)优先,
 * 缺席时回落会话 cwd 的目录名(0.1.5 的列表行只有 cwd)。
 */
fun SessionSummary.displayTitle(): String? =
    title?.takeIf { it.isNotBlank() }
        ?: cwd?.trimEnd('/')?.substringAfterLast('/')?.takeIf { it.isNotBlank() }

/** 投影值里的纯字符串投影(标题等);缺席/畸形返回 null */
fun Map<String, JsonElement>.stringProjection(key: String): String? =
    (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

/** 投影值里的整数投影 */
fun Map<String, JsonElement>.intProjection(key: String): Int? =
    (this[key] as? JsonPrimitive)?.intOrNull

/** 投影值里的布尔投影 */
fun Map<String, JsonElement>.booleanProjection(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.booleanOrNull
