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

import com.wanbaohe.dsh.wire.model.QueueItem
import com.wanbaohe.dsh.wire.model.TaskView
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * `session/control` 帧(DSH 0.1.5-rc.2 SessionControlFrame)。
 *
 * 这是**取代旧 `/api/events.host` 主机流**的全局控制面:一条流给出全 Host 的
 * 队列 / 后台任务 / 投影水位,首帧恒为 [Baseline](整量),之后是增量帧。
 * 队列与任务是"整快照收敛"语义,直接替换本地对应会话的集合。
 *
 * 端点 `session/control` 的 args 为空对象,流与会话无关(全 Host 共用一条)。
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class ControlFrame {

    /** 每世代一次的整量基线 */
    @Serializable
    @SerialName("baseline")
    data class Baseline(
        val value: ControlBaseline = ControlBaseline()
    ) : ControlFrame()

    /** 某会话的待处理收件箱整量快照(高水位覆盖低水位) */
    @Serializable
    @SerialName("queue")
    data class Queue(
        val sessionId: String,
        val items: List<QueueItem> = emptyList()
    ) : ControlFrame()

    /** 某会话的后台任务整量快照 */
    @Serializable
    @SerialName("jobs")
    data class Jobs(
        val sessionId: String,
        val jobs: List<TaskView> = emptyList()
    ) : ControlFrame()

    /** 单个投影单元变更:同 key 高 seq 覆盖低 seq */
    @Serializable
    @SerialName("projection")
    data class Projection(
        val sessionId: String,
        val key: String,
        val value: JsonElement? = null,
        val seq: Int = -1
    ) : ControlFrame()
}

/** 控制面整量基线:会话 id → 队列/任务/投影 */
@Serializable
data class ControlBaseline(
    val queues: Map<String, List<QueueItem>> = emptyMap(),
    val jobs: Map<String, List<TaskView>> = emptyMap(),
    val projections: Map<String, ProjectionsBlock> = emptyMap()
)

/**
 * workspace/follow 的帧(DSH 0.1.5-rc.2 WorkspaceFollowFrame):
 * 首帧恒为 baseline(`value:{items,archivedSessionIds}`),之后是
 * upsert / remove / order / archived 增量。
 */
@Serializable
data class WorkspaceFollowFrame(
    val type: String = "",
    val value: WorkspaceBaseline? = null,
    val workspace: JsonObject? = null,
    val workspaceId: String? = null,
    val workspaceIds: List<String>? = null,
    val archivedSessionIds: List<String>? = null
)

/** workspace/follow 的基线整量 */
@Serializable
data class WorkspaceBaseline(
    val items: List<JsonObject> = emptyList(),
    val archivedSessionIds: List<String> = emptyList()
)
