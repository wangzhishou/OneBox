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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * goal 域 wire 模型(DSH 0.1.5-rc.2 `packages/goal/goal/src`)。
 *
 * 端点已是 goals 命名空间(带 s,斜杠形式),并且**统一按 agent 寻址**:
 * - `goals/get | create | edit | pause | resume | complete | clear` 的 args 都带
 *   `agentId`(= sessionId,lookup 键名就是 agent)
 * - `create` 收 `{agentId, request:{objective, maxGoalRounds?}}` → `{ref}`
 * - `edit` 收 `{agentId, ref, request:{objective?, maxGoalRounds?}}` → `GoalView`
 * - `pause|resume|complete` 收 `{agentId, ref}` → `GoalView`
 * - `clear` 收 `{agentId, ref}` → `GoalRef`(新 revision 墓碑)
 * - `get` 收 `{agentId}` → `GoalView | undefined`
 */

/** goal 引用(id + revision 乐观锁) */
@Serializable
data class GoalRef(
    val id: String,
    val revision: Int
)

/** 目标快照(goals/get 与各变更回值的公共体) */
@Serializable
data class GoalSnapshot(
    val id: String = "",
    val revision: Int = 0,
    val phase: String? = null,
    val objective: String? = null,
    val maxGoalRounds: Int? = null
)

/** goals/get 的回值(operations 视图):快照 + 轮次 + 激活态 */
@Serializable
data class GoalView(
    val id: String = "",
    val revision: Int = 0,
    val phase: String? = null,
    val objective: String? = null,
    val maxGoalRounds: Int? = null,
    val roundsStarted: Int? = null,
    val createdAt: Double? = null,
    val updatedAt: Double? = null,
    val activation: String? = null
) {
    /** 转成只看耐久的快照视图 */
    fun snapshot(): GoalSnapshot = GoalSnapshot(id, revision, phase, objective, maxGoalRounds)
}

/** goals/create 的回值 */
@Serializable
data class GoalCreateValue(
    val ref: GoalRef
)

/** goals/create 上行 request */
@Serializable
data class GoalCreateRequest(
    val objective: String,
    val maxGoalRounds: Int? = null
)

/** goals/edit 上行 request */
@Serializable
data class GoalEditRequest(
    val objective: String? = null,
    val maxGoalRounds: Int? = null
)

/**
 * 会话 goal 投影的解析视图(投影键 "goal",值为对象:
 * goal 子对象 + roundsStarted + 可选 activation)。投影形 Map 为防御式解析:
 * 字段缺席即 null,不抛异常。
 */
data class GoalProjection(
    val ref: GoalRef?,
    val phase: String?,
    val objective: String?,
    val maxGoalRounds: Int?,
    val roundsStarted: Int?,
    val activation: String?
)

/** 从投影值解析 goal 视图;非对象或无 goal 子对象返回 null(无目标) */
fun parseGoalProjection(value: JsonObject?): GoalProjection? {
    val goal = value?.get("goal") as? JsonObject ?: return null
    val id = (goal["id"] as? JsonPrimitive)?.contentOrNull
    val revision = (goal["revision"] as? JsonPrimitive)?.intOrNull
    return GoalProjection(
        ref = if (id != null && revision != null) GoalRef(id, revision) else null,
        phase = (goal["phase"] as? JsonPrimitive)?.contentOrNull,
        objective = (goal["objective"] as? JsonPrimitive)?.contentOrNull,
        maxGoalRounds = (goal["maxGoalRounds"] as? JsonPrimitive)?.intOrNull,
        roundsStarted = (value["roundsStarted"] as? JsonPrimitive)?.intOrNull,
        activation = (value["activation"] as? JsonPrimitive)?.contentOrNull
    )
}
