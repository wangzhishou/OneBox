package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.domain.model.GomokuMove

/**
 * 五子棋走法生成:所有空位均可落子,无吃子/阻挡概念。
 */
object MoveGenerator {

    fun pseudoLegalMoves(boardState: BoardState, side: Side = boardState.sideToMove): List<GomokuMove> {
        return buildList {
            for (rank in 0 until BoardPoint.RANK_COUNT) {
                for (file in 0 until BoardPoint.FILE_COUNT) {
                    val point = BoardPoint(file, rank)
                    if (boardState.isEmpty(point)) {
                        add(GomokuMove(to = point, side = side))
                    }
                }
            }
        }
    }
}
