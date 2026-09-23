package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.ChessMove

data class InteractionState(
    val selectedPoint: BoardPoint? = null,
    val candidateTargets: Set<BoardPoint> = emptySet(),
    val pendingMove: ChessMove? = null,
    /** 升变待选:同一起止点的 4 个升变候选(后/车/象/马),UI 弹选择器 */
    val pendingPromotionMoves: List<ChessMove> = emptyList(),
)

sealed interface GameAction {
    data class TapCell(val point: BoardPoint) : GameAction
    data object ClearSelection : GameAction
}

/**
 * Pure UI interaction reducer。两步交互:点己方棋子选中(亮候选格),再点候选格生成着法。
 * 升变不直接 pendingMove,先交出 4 个候选由 UI 弹选择器,选完后调用方再提交。
 */
object GameReducer {

    fun reduce(
        boardState: BoardState,
        legalMoves: List<ChessMove>,
        previous: InteractionState,
        action: GameAction,
    ): InteractionState = when (action) {
        GameAction.ClearSelection -> InteractionState()
        is GameAction.TapCell -> onTap(boardState, legalMoves, previous, action.point)
    }

    private fun onTap(
        boardState: BoardState,
        legalMoves: List<ChessMove>,
        previous: InteractionState,
        point: BoardPoint,
    ): InteractionState {
        val selected = previous.selectedPoint
        if (selected != null) {
            val moves = legalMoves.filter { it.from == selected && it.to == point }
            if (moves.isNotEmpty()) {
                // 升变:同一起止点多候选,交给 UI 选择
                if (moves.any { it.promotion != null }) {
                    return InteractionState(
                        selectedPoint = selected,
                        pendingPromotionMoves = moves,
                    )
                }
                return InteractionState(pendingMove = moves.first())
            }
        }
        return selectPoint(boardState, legalMoves, point)
    }

    private fun selectPoint(
        boardState: BoardState,
        legalMoves: List<ChessMove>,
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
