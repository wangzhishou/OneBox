package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 会话事件(session/event 帧的 event 载荷、session.history 的事件条目)。
 *
 * 事件日志只增不改,[seq] 是唯一排序/去重键;[data] 保持 JsonObject 原样,
 * 已知类型(user/message、assistant/message)的文本提取见 [extractEventText],
 * 其余类型保留 raw,P5 节点渲染再处理。
 */
@Serializable
data class SessionEvent(
    val type: String,
    val seq: Int,
    val time: Double = 0.0,
    val data: JsonObject = JsonObject(emptyMap()),
    val surfaceOp: String? = null,
    val ignorable: Boolean? = null
)

/** session.history 的事件条目:[view] 为主机算好的工具渲染意图(可选,P5 细化) */
@Serializable
data class HistoryEntry(
    val event: SessionEvent,
    val view: JsonElement? = null
)

/** 投影水位快照(session.list 行内块 / session.history 尾页块);空日志 asOfSeq = -1 */
@Serializable
data class SessionProjectionsBlock(
    val asOfSeq: Int,
    val values: Map<String, JsonElement>
)

/** session.list 的会话摘要行 */
@Serializable
data class SessionSummary(
    val sessionId: String,
    val updatedAt: Double,
    val running: Boolean,
    val blank: Boolean,
    val parentSessionId: String? = null,
    val origin: String? = null,
    val cwd: String? = null,
    val agentPreset: String? = null,
    val projections: SessionProjectionsBlock? = null
)

@Serializable
data class SessionListValue(
    val items: List<SessionSummary>
)

@Serializable
data class SessionHistoryValue(
    val events: List<HistoryEntry>,
    val hasMore: Boolean,
    /** 尾页(beforeSeq 缺席)携带的全量投影水位快照 */
    val projections: SessionProjectionsBlock? = null
)

@Serializable
data class SessionCreateValue(
    val sessionId: String,
    val agentPreset: String? = null
)

/**
 * session.prompt 上行载荷(DSH-PROTOCOL §5):
 * 内容块数组(文本块 + 可选 base64 图片块)+ mode:queue + clientTimeZone(IANA,如 Asia/Shanghai);
 * 非法时区服务端回 invalid-time-zone。内容恰好是单个 "/" 开头文本块 = 斜杠命令。
 */
@Serializable
data class SessionPromptRequest(
    val sessionId: String,
    val mode: String = "queue",
    val content: List<PromptContentPart>,
    val clientTimeZone: String? = null
)

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
    val accepted: Boolean,
    val command: JsonObject? = null
)

/** session.fork 的响应 value:新会话 id(atSeq 锚点映射到其后第一个已闭合 turn/end) */
@Serializable
data class SessionForkValue(
    val sessionId: String
)

/** session.rename 的响应 value:规范化后的标题 + 投影 seq(先落本地格,推送帧高 seq 覆盖) */
@Serializable
data class SessionRenameValue(
    val title: String,
    val seq: Int
)

/** session.search 命中行:sessionId + 命中片段 */
@Serializable
data class SessionSearchItem(
    val sessionId: String,
    val snippet: String
)

@Serializable
data class SessionSearchValue(
    val items: List<SessionSearchItem>,
    val hasMore: Boolean
)

/**
 * imageLimits 投影(主机 per-boot 常量下发,投影键 "imageLimits"):
 * 客户端在附件 intake 前按此本地预拒,省一次上行往返;投影缺席时跳过预检(服务端权威)。
 */
@Serializable
data class ImageLimitsProjection(
    val maxImageBytes: Long,
    val maxImagesPerMessage: Int,
    val maxMessageImageBytes: Long,
    val maxImagePixels: Long,
    val mediaTypes: List<String>
)

/**
 * 事件文本提取(对齐 Flutter event_text.dart):
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

/** 摘要显示标题:投影 title(纯字符串投影),缺席返回 null(UI 回落 cwd 目录名/占位) */
fun SessionSummary.displayTitle(): String? =
    (projections?.values?.get("title") as? JsonPrimitive)
        ?.contentOrNull?.takeIf { it.isNotBlank() }
