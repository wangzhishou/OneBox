package com.wanbaohe.xiangqi.application.port.outbound

import kotlinx.coroutines.flow.Flow

/**
 * 象棋专用走棋 AI 配置（与聊天工作槽分离）。默认 Pikafish。
 */
data class XiangqiAiConfig(
    val fastSource: XiangqiAiSource = XiangqiAiSource.default,
    val duelASource: XiangqiAiSource = XiangqiAiSource.default,
    val duelBSource: XiangqiAiSource = XiangqiAiSource.default,
) {
    fun sourceFor(slot: EngineSlot): XiangqiAiSource = when (slot) {
        EngineSlot.FAST -> fastSource
        EngineSlot.DUEL_A -> duelASource
        EngineSlot.DUEL_B -> duelBSource
    }

    fun withSource(slot: EngineSlot, source: XiangqiAiSource): XiangqiAiConfig = when (slot) {
        EngineSlot.FAST -> copy(fastSource = source)
        EngineSlot.DUEL_A -> copy(duelASource = source)
        EngineSlot.DUEL_B -> copy(duelBSource = source)
    }

    /** 人机对弈只用 FAST 槽 */
    fun requiresLoginForHumanVsAi(): Boolean = fastSource.requiresLogin

    /** AI 对战两侧任一需要登录则要登录 */
    fun requiresLoginForAiVsAi(): Boolean =
        duelASource.requiresLogin || duelBSource.requiresLogin

    /** 开局积分门槛（取两侧较高者；免费引擎为 0） */
    fun startPointsForHumanVsAi(): Int = fastSource.startPoints

    fun startPointsForAiVsAi(): Int =
        maxOf(duelASource.startPoints, duelBSource.startPoints)
}

interface XiangqiAiStore {
    fun observe(): Flow<XiangqiAiConfig>
    suspend fun get(): XiangqiAiConfig
    suspend fun update(config: XiangqiAiConfig)
}
