package com.wanbaohe.dsh.wire.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * goal 域 wire 模型(goal.create/edit/pause/resume/complete/clear 六方法)。
 *
 * goal 是目标对象的引用操作面:[GoalRef] = id + revision 乐观锁(CAS),
 * create/edit/pause/resume/complete 各回新 ref(revision 递增);clear 无引用。
 * payload 形状以生成模型 goals.dart 为准(Flutter 旧 goal_store.dart 的扁平
 * 字段是过时形态,不采用)。
 */

/** goal 引用(id + revision 乐观锁) */
@Serializable
data class GoalRef(
    val id: String,
    val revision: Int
)

/** create/edit/pause/resume/complete 的响应 value 统一为回带新 ref */
@Serializable
data class GoalRefValue(
    val ref: GoalRef
)

/** goal.clear 的响应 value */
@Serializable
data class GoalClearValue(
    val cleared: Boolean
)

/**
 * 会话 goal 投影的解析视图(投影键 "goal",形如 {goal:{id,revision,phase,objective,…}, roundsStarted})。
 * 投影形 Map 为防御式解析:字段缺席即 null,不抛异常。
 */
data class GoalProjection(
    val ref: GoalRef?,
    val phase: String?,
    val objective: String?,
    val roundsStarted: Int?
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
        roundsStarted = (value["roundsStarted"] as? JsonPrimitive)?.intOrNull
    )
}
