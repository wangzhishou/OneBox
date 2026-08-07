package com.wanbaohe.xiangqi.application.port.outbound

import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove

interface MoveChooser {
    suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
    ): MoveDecision?
}

enum class EngineSlot { FAST, DUEL_A, DUEL_B }

data class MoveDecision(
    val move: XiangqiMove,
    val reason: String,
    val rawResponse: String,
    val fallbackUsed: Boolean,
)
