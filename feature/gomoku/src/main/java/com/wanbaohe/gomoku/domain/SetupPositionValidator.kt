package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side

sealed interface SetupPositionIssue {
    data class AlreadyWon(val side: Side) : SetupPositionIssue
    data object FullBoard : SetupPositionIssue
}

object SetupPositionValidator {
    fun validate(board: BoardState): SetupPositionIssue? {
        if (board.isFull()) return SetupPositionIssue.FullBoard
        GameArbiter.detectWinner(board)?.let { return SetupPositionIssue.AlreadyWon(it) }
        // Practice positions choose the next side explicitly. Both the FEN request and
        // local search use sideToMove, not historical stone-count parity.
        return null
    }
}
