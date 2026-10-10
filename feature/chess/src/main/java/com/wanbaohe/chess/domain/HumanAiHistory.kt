package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.Side

object HumanAiHistory {
    fun undoSteps(moverSides: List<Side>, currentPly: Int, humanSide: Side): Int {
        val lastHuman = moverSides.take(currentPly).indexOfLast { it == humanSide }
        return if (lastHuman < 0) 0 else currentPly - lastHuman
    }

    fun redoSteps(moverSides: List<Side>, currentPly: Int, humanSide: Side): Int {
        val remaining = moverSides.drop(currentPly)
        val response = remaining.indexOfFirst { it != humanSide }
        return if (response >= 0) response + 1 else remaining.size
    }
}
