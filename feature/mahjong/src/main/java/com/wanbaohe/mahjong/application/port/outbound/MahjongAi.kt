package com.wanbaohe.mahjong.application.port.outbound

import com.wanbaohe.mahjong.domain.model.MahjongAction
import com.wanbaohe.mahjong.domain.model.Meld

data class AiObservation(
    val seat: Int,
    val hand: List<Int>,
    val melds: List<Meld>,
    val visibleCounts: IntArray,
    val pendingTileId: Int?,
    val legalActions: List<MahjongAction>,
)

interface MahjongAi {
    fun chooseAction(observation: AiObservation): MahjongAction
}
