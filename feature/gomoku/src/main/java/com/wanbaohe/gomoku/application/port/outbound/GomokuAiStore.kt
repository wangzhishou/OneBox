package com.wanbaohe.gomoku.application.port.outbound

import kotlinx.coroutines.flow.Flow

/**
 * 五子棋走棋 AI 配置（与聊天工作槽分离）。默认服务端免费引擎 Rapfi（免登录、免积分）。
 */
data class GomokuAiConfig(
    val fastSource: GomokuAiSource = GomokuAiSource.default,
    val duelASource: GomokuAiSource = GomokuAiSource.default,
    val duelBSource: GomokuAiSource = GomokuAiSource.default,
) {
    fun sourceFor(slot: EngineSlot): GomokuAiSource = when (slot) {
        EngineSlot.FAST -> fastSource
        EngineSlot.DUEL_A -> duelASource
        EngineSlot.DUEL_B -> duelBSource
    }

    fun withSource(slot: EngineSlot, source: GomokuAiSource): GomokuAiConfig = when (slot) {
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

interface GomokuAiStore {
    fun observe(): Flow<GomokuAiConfig>
    suspend fun get(): GomokuAiConfig
    suspend fun update(config: GomokuAiConfig)
}
