package com.wanbaohe.xiangqi.data

import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.domain.model.XiangqiMove

internal object HeuristicMoveFallback {

    fun select(legalMoves: List<XiangqiMove>): XiangqiMove? {
        return legalMoves.sortedWith(
            compareByDescending<XiangqiMove> { it.captured != null }
                .thenByDescending { it.notationCn.contains("进") }
        ).firstOrNull()
    }

    fun decision(legalMoves: List<XiangqiMove>): MoveDecision? {
        val move = select(legalMoves) ?: return null
        return MoveDecision(move, "fallback", "fallback", fallbackUsed = true)
    }
}
