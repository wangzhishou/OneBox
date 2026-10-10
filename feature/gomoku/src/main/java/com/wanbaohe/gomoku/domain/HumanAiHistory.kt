package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.Side

object HumanAiHistory {
    fun undoSteps(moverSides: List<Side>, currentPly: Int, humanSide: Side): Int {
        val activePly = currentPly.coerceIn(0, moverSides.size)
        val lastHuman = moverSides.take(activePly).indexOfLast { it == humanSide }
        return if (lastHuman < 0) 0 else activePly - lastHuman
    }

    fun redoSteps(moverSides: List<Side>, currentPly: Int, humanSide: Side): Int {
        val remaining = moverSides.drop(currentPly.coerceIn(0, moverSides.size))
        val response = remaining.indexOfFirst { it != humanSide }
        return if (response >= 0) response + 1 else remaining.size
    }
}
