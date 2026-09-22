package com.wanbaohe.xiangqi.application.port.outbound

import kotlinx.coroutines.flow.Flow

/**
 * 象棋专用走棋 AI 配置（与聊天工作槽分离）。
 */
data class XiangqiAiConfig(
    val fastSource: XiangqiAiSource = XiangqiAiSource.WorkingModel,
    val duelASource: XiangqiAiSource = XiangqiAiSource.WorkingModel,
    val duelBSource: XiangqiAiSource = XiangqiAiSource.WorkingModel,
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
}

interface XiangqiAiStore {
    fun observe(): Flow<XiangqiAiConfig>
    suspend fun get(): XiangqiAiConfig
    suspend fun update(config: XiangqiAiConfig)
}
