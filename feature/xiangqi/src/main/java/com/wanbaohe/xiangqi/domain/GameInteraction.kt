package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove

data class InteractionState(
    val selectedPoint: BoardPoint? = null,
    val candidateTargets: Set<BoardPoint> = emptySet(),
    val pendingMove: XiangqiMove? = null,
)

sealed interface GameAction {
    data class TapCell(val point: BoardPoint) : GameAction
    data object ClearSelection : GameAction
}

/**
 * Pure UI interaction reducer. Derives selection state and pending moves from
 * immutable board state plus legal moves. Never mutates game truth.
 */
object GameReducer {

    fun reduce(
        boardState: BoardState,
        legalMoves: List<XiangqiMove>,
        previous: InteractionState,
        action: GameAction,
    ): InteractionState = when (action) {
        GameAction.ClearSelection -> InteractionState()
        is GameAction.TapCell -> onTap(boardState, legalMoves, previous, action.point)
    }

    private fun onTap(
        boardState: BoardState,
        legalMoves: List<XiangqiMove>,
        previous: InteractionState,
        point: BoardPoint,
    ): InteractionState {
        val selected = previous.selectedPoint
        val move = selected?.let { from ->
            legalMoves.find { it.from == from && it.to == point }
        }
        if (move != null) return InteractionState(pendingMove = move)
        return selectPoint(boardState, legalMoves, point)
    }

    private fun selectPoint(
        boardState: BoardState,
        legalMoves: List<XiangqiMove>,
        point: BoardPoint,
    ): InteractionState {
        val piece = boardState.pieceAt(point)
        if (piece?.side != boardState.sideToMove) return InteractionState()
        return InteractionState(
            selectedPoint = point,
            candidateTargets = legalMoves.asSequence()
                .filter { it.from == point }
                .map { it.to }
                .toSet(),
        )
    }
}
