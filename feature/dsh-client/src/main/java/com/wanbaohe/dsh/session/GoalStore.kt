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

package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.wire.DshEndpoints
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.model.GoalCreateRequest
import com.wanbaohe.dsh.wire.model.GoalCreateValue
import com.wanbaohe.dsh.wire.model.GoalEditRequest
import com.wanbaohe.dsh.wire.model.GoalRef
import com.wanbaohe.dsh.wire.model.GoalView
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * goal 域动作面(DSH 0.1.5-rc.2;`goals/create|edit|pause|resume|complete|clear`,命名空间带 s)。
 *
 * 与旧版(<= 0.1.1)的差异:
 * - 端点从 `goal.*` 改为 goals 命名空间(带 s、斜杠形式),并且**统一按 agent 寻址**:args 必带 `agentId`,
 *   值就是 sessionId(harness 里 Agent 的 wire 身份就是 SessionId)
 * - `create` 收 `{agentId, request:{objective, maxGoalRounds?}}` → `{ref}`
 * - `edit` 收 `{agentId, ref, request:{objective?, maxGoalRounds?}}` → 目标视图
 * - `pause|resume|complete` 收 `{agentId, ref}` → 目标视图
 * - `clear` 收 `{agentId, ref}` → 新 revision 的 `GoalRef` 墓碑
 *
 * goal 状态本体走会话 "goal" 投影(经 SessionStore overlay 下发),本类只做动作。
 * 生命周期与 ChatBundle 绑定(dispose 为空操作,保持一致形态)。
 */
class GoalStore(
    private val api: DshApiClient
) {

    /** goals/create:objective 必填,maxGoalRounds 可选(服务端默认) */
    suspend fun create(sessionId: String, objective: String, maxGoalRounds: Int? = null): GoalRef {
        val request = GoalCreateRequest(objective = objective, maxGoalRounds = maxGoalRounds)
        val args = buildJsonObject {
            put("agentId", sessionId)
            put("request", DshJson.encodeToJsonElement(GoalCreateRequest.serializer(), request))
        }
        return DshJson.decodeFromJsonElement<GoalCreateValue>(
            api.callRemote(DshEndpoints.GOALS_CREATE, args)
        ).ref
    }

    /** goals/edit:CAS ref + 可选新 objective / maxGoalRounds */
    suspend fun edit(
        sessionId: String,
        ref: GoalRef,
        objective: String? = null,
        maxGoalRounds: Int? = null
    ): GoalRef {
        val request = GoalEditRequest(objective = objective, maxGoalRounds = maxGoalRounds)
        val args = buildJsonObject {
            put("agentId", sessionId)
            putRef(ref)
            put("request", DshJson.encodeToJsonElement(GoalEditRequest.serializer(), request))
        }
        return DshJson.decodeFromJsonElement<GoalView>(
            api.callRemote(DshEndpoints.GOALS_EDIT, args)
        ).let { GoalRef(it.id, it.revision) }
    }

    suspend fun pause(sessionId: String, ref: GoalRef): GoalRef = refOp(DshEndpoints.GOALS_PAUSE, sessionId, ref)

    suspend fun resume(sessionId: String, ref: GoalRef): GoalRef = refOp(DshEndpoints.GOALS_RESUME, sessionId, ref)

    suspend fun complete(sessionId: String, ref: GoalRef): GoalRef =
        refOp(DshEndpoints.GOALS_COMPLETE, sessionId, ref)

    /** goals/clear:清除当前目标,回带新 revision 的墓碑引用 */
    suspend fun clear(sessionId: String, ref: GoalRef): GoalRef {
        val args = buildJsonObject {
            put("agentId", sessionId)
            putRef(ref)
        }
        return DshJson.decodeFromJsonElement<GoalRef>(
            api.callRemote(DshEndpoints.GOALS_CLEAR, args)
        )
    }

    /** goals/get:读取当前目标视图(无目标返回 null) */
    suspend fun get(sessionId: String): GoalView? {
        val args = buildJsonObject { put("agentId", sessionId) }
        val value = api.callRemote(DshEndpoints.GOALS_GET, args)
        if (value is JsonNull) return null
        return DshJson.decodeFromJsonElement(GoalView.serializer(), value)
    }

    /** pause/resume/complete 公共形态:{agentId, ref} → 回带新 ref 的视图 */
    private suspend fun refOp(endpoint: String, sessionId: String, ref: GoalRef): GoalRef {
        val args = buildJsonObject {
            put("agentId", sessionId)
            putRef(ref)
        }
        return DshJson.decodeFromJsonElement<GoalView>(api.callRemote(endpoint, args))
            .let { GoalRef(it.id, it.revision) }
    }

    /** CAS ref 子对象:{id, revision} */
    private fun JsonObjectBuilder.putRef(ref: GoalRef) {
        putJsonObject("ref") {
            put("id", ref.id)
            put("revision", ref.revision)
        }
    }

    fun dispose() = Unit
}
