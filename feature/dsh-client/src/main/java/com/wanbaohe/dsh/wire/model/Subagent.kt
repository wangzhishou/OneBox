package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * subagent 域 wire 模型(DSH-PROTOCOL §3 subagent 组,对齐 Flutter subagents.dart)。
 *
 * - subagent.list({parentSessionId}) → {entries, parentAvailable}
 * - subagent.history({parentSessionId, childSessionId, mode, beforeSeq?, maxMessages?})
 *   → {events, hasMore, projections?};mode 取自目录行('one-shot'|'continuable')
 * - subagent.prompt / subagent.interrupt 的 mode 恒为 'continuable'
 */

/** subagent.list 的目录行(判别字段 kind):child 可操作行 / diagnostic 只读诊断行 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed class SubagentListEntry {

    /** 子会话行;[activity] 为 'running'|'inactive'(wire 为字符串,未知值按非运行处理) */
    @Serializable
    @SerialName("child")
    data class Child(
        val id: String,
        val mode: String,
        val activity: String,
        val hasChildren: Boolean,
        val label: String? = null
    ) : SubagentListEntry() {
        val isRunning: Boolean get() = activity == ActivityRunning
    }

    /** 诊断行:可读不可操作(目录部分异常时占位) */
    @Serializable
    @SerialName("diagnostic")
    data class Diagnostic(
        val id: String,
        val reason: JsonElement = JsonNull
    ) : SubagentListEntry()
}

@Serializable
data class SubagentListValue(
    val entries: List<SubagentListEntry>,
    val parentAvailable: Boolean
)

/** subagent.history 的响应 value:与 session.history 同构(分页 + 尾页投影块) */
@Serializable
data class SubagentHistoryValue(
    val events: List<HistoryEntry>,
    val hasMore: Boolean,
    val projections: SessionProjectionsBlock? = null
)

@Serializable
data class SubagentPromptValue(
    val messageId: String
)

@Serializable
data class SubagentInterruptValue(
    val accepted: Boolean
)

/** 目录行标题:child 用 label(无则回退 id);诊断行显示原因 */
fun SubagentListEntry.entryTitle(): String = when (this) {
    is SubagentListEntry.Child -> label ?: id
    is SubagentListEntry.Diagnostic -> "diagnostic($id)"
}

const val ActivityRunning = "running"
const val SubagentModeContinuable = "continuable"
