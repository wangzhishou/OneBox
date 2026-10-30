package com.wanbaohe.mahjong.application.port.outbound

import com.wanbaohe.mahjong.domain.model.MahjongState

data class MahjongStats(
    val games: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val bestScore: Int = 0,
)

interface MahjongStore {
    fun saveSnapshot(state: MahjongState)
    fun loadSnapshot(): MahjongState?
    fun clearSnapshot()
    fun loadStats(): MahjongStats
    fun saveStats(stats: MahjongStats)
}
