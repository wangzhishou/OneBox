package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.model.GoalClearValue
import com.wanbaohe.dsh.wire.model.GoalRef
import com.wanbaohe.dsh.wire.model.GoalRefValue
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * goal 域动作面(goal.create/edit/pause/resume/complete/clear 六方法)。
 *
 * goal 是目标对象的引用操作面:[GoalRef] = id + revision 乐观锁(CAS),
 * create/edit/pause/resume/complete 各回新 ref(revision 递增);clear 无引用。
 * goal 状态本体走会话 "goal" 投影(经 SessionStore 投影 overlay 下发),本类只做动作,
 * 不持状态;生命周期与 ChatBundle 绑定(dispose 为空操作,保持一致形态)。
 */
class GoalStore(
    private val api: DshApiClient
) {

    /** goal.create:objective 必填,maxGoalRounds 可选(服务端默认) */
    suspend fun create(sessionId: String, objective: String, maxGoalRounds: Int? = null): GoalRef {
        val payload = buildJsonObject {
            put("sessionId", sessionId)
            put("objective", objective)
            maxGoalRounds?.let { put("maxGoalRounds", it) }
        }
        return DshJson.decodeFromJsonElement<GoalRefValue>(api.call(RpcGoalCreate, payload)).ref
    }

    /** goal.edit:CAS ref + 可选新 objective / maxGoalRounds */
    suspend fun edit(
        sessionId: String,
        ref: GoalRef,
        objective: String? = null,
        maxGoalRounds: Int? = null
    ): GoalRef {
        val payload = buildJsonObject {
            put("sessionId", sessionId)
            putRef(ref)
            objective?.let { put("objective", it) }
            maxGoalRounds?.let { put("maxGoalRounds", it) }
        }
        return DshJson.decodeFromJsonElement<GoalRefValue>(api.call(RpcGoalEdit, payload)).ref
    }

    suspend fun pause(sessionId: String, ref: GoalRef): GoalRef = refOp(RpcGoalPause, sessionId, ref)

    suspend fun resume(sessionId: String, ref: GoalRef): GoalRef = refOp(RpcGoalResume, sessionId, ref)

    suspend fun complete(sessionId: String, ref: GoalRef): GoalRef = refOp(RpcGoalComplete, sessionId, ref)

    /** goal.clear:清除当前目标(无引用操作) */
    suspend fun clear(sessionId: String, ref: GoalRef): Boolean {
        val payload = buildJsonObject {
            put("sessionId", sessionId)
            putRef(ref)
        }
        return DshJson.decodeFromJsonElement<GoalClearValue>(api.call(RpcGoalClear, payload)).cleared
    }

    /** pause/resume/complete 公共形态:{sessionId, ref} → 回带新 ref */
    private suspend fun refOp(method: String, sessionId: String, ref: GoalRef): GoalRef {
        val payload = buildJsonObject {
            put("sessionId", sessionId)
            putRef(ref)
        }
        return DshJson.decodeFromJsonElement<GoalRefValue>(api.call(method, payload)).ref
    }

    /** CAS ref 子对象:{id, revision} */
    private fun kotlinx.serialization.json.JsonObjectBuilder.putRef(ref: GoalRef) {
        putJsonObject("ref") {
            put("id", ref.id)
            put("revision", ref.revision)
        }
    }

    fun dispose() = Unit

    companion object {
        private const val RpcGoalCreate = "goal.create"
        private const val RpcGoalEdit = "goal.edit"
        private const val RpcGoalPause = "goal.pause"
        private const val RpcGoalResume = "goal.resume"
        private const val RpcGoalComplete = "goal.complete"
        private const val RpcGoalClear = "goal.clear"
    }
}
