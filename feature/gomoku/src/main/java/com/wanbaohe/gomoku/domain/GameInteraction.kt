package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GomokuMove

data class InteractionState(
    val selectedPoint: BoardPoint? = null,
    val candidateTargets: Set<BoardPoint> = emptySet(),
    val pendingMove: GomokuMove? = null,
)

sealed interface GameAction {
    data class TapCell(val point: BoardPoint) : GameAction
    data object ClearSelection : GameAction
}

/**
 * Pure UI interaction reducer。五子棋无"选中棋子再走位"两步交互:
 * 点击合法空位即直接生成 pendingMove;点击其他位置清除选中态。
 */
object GameReducer {

    fun reduce(
        boardState: BoardState,
        legalMoves: List<GomokuMove>,
        previous: InteractionState,
        action: GameAction,
    ): InteractionState = when (action) {
        GameAction.ClearSelection -> InteractionState()
        is GameAction.TapCell -> onTap(boardState, legalMoves, previous, action.point)
    }

    private fun onTap(
        boardState: BoardState,
        legalMoves: List<GomokuMove>,
        previous: InteractionState,
        point: BoardPoint,
    ): InteractionState {
        if (!point.isInside() || !boardState.isEmpty(point)) return InteractionState()
        val move = legalMoves.find { it.to == point } ?: return InteractionState()
        return InteractionState(
            selectedPoint = point,
            pendingMove = move,
        )
    }
}
