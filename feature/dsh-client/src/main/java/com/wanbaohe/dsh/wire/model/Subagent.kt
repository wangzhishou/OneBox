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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * subagent 域 wire 模型(DSH 0.1.5-rc.2 `packages/subagent/subagent/src`)。
 *
 * 端点已是 `subagents` 命名空间(带 s,斜杠形式),并且签名都变了:
 * - `subagents/list` 的 args 是**裸字符串** parentSessionId(旧版包了一层对象)
 * - `subagents/prompt` 收 `{requestId,parentSessionId,childSessionId,mode:'continuable',
 *   delivery:'queue'|'steer',content:[…],clientTimeZone?}`
 * - `subagents/interruptByParent` 收 `{childSessionId,parentSessionId,mode:'continuable'}`
 * - **子代理历史没有独立端点了**:走 `session/follow` / `session/page`,地址是
 *   [SessionAddress.Subagent]
 */

/** subagents/list 的目录行(判别字段 kind):child 可操作行 / diagnostic 只读诊断行 */
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
        val hasChildren: Boolean = false,
        val label: String? = null
    ) : SubagentListEntry() {
        val isRunning: Boolean get() = activity == ActivityRunning
    }

    /** 诊断行:可读不可操作(目录部分异常时占位) */
    @Serializable
    @SerialName("diagnostic")
    data class Diagnostic(
        val id: String,
        val reason: String? = null
    ) : SubagentListEntry()
}

@Serializable
data class SubagentListValue(
    val entries: List<SubagentListEntry> = emptyList(),
    val parentAvailable: Boolean = false
)

/** subagents/prompt 上行 request */
@Serializable
data class SubagentPromptRequest(
    val requestId: String,
    val parentSessionId: String,
    val childSessionId: String,
    val mode: String = SubagentModeContinuable,
    val delivery: String = SubagentDeliveryQueue,
    val content: List<PromptContentPart> = emptyList(),
    val clientTimeZone: String? = null
)

const val SubagentDeliveryQueue = "queue"
const val SubagentDeliverySteer = "steer"

@Serializable
data class SubagentPromptValue(
    val messageId: String = ""
)

/** subagents/interruptByParent 上行 args */
@Serializable
data class SubagentInterruptRequest(
    val childSessionId: String,
    val parentSessionId: String,
    val mode: String = SubagentModeContinuable
)

@Serializable
data class SubagentInterruptValue(
    val accepted: Boolean = false
)

/** 目录行标题:child 用 label(无则回退 id);诊断行显示原因 */
fun SubagentListEntry.entryTitle(): String = when (this) {
    is SubagentListEntry.Child -> label ?: id
    is SubagentListEntry.Diagnostic -> reason?.let { "diagnostic($id: $it)" } ?: "diagnostic($id)"
}

/** 目录行的会话地址(供 follow/page 使用) */
fun SubagentListEntry.Child.address(parentSessionId: String): SessionAddress =
    SessionAddress.Subagent(parentSessionId = parentSessionId, childSessionId = id, mode = mode)

const val ActivityRunning = "running"
const val SubagentModeContinuable = "continuable"

/** 从子代理目录行的 mode 字段安全取值(未知值按 continuable 处理) */
fun String?.asSubagentMode(): String =
    if (this == SessionAddress.Subagent.MODE_ONE_SHOT) SessionAddress.Subagent.MODE_ONE_SHOT
    else SubagentModeContinuable

/** 子代理显示标签(source.kind == 'subagent' 时用) */
fun JsonPrimitive?.asLabelOrNull(): String? = this?.contentOrNull
