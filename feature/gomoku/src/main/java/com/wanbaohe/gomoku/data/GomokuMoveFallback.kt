package com.wanbaohe.gomoku.data

import com.wanbaohe.gomoku.application.port.outbound.MoveDecision
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GomokuMove
import com.wanbaohe.gomoku.domain.model.Side

/**
 * LLM 不可用时的本地启发式兜底:成五 > 堵对方成五 > 连四/活三形状评分,
 * 再按离中心远近微调(五子棋中心子力连接面最大)。
 */
internal object GomokuMoveFallback {

    private val DIRECTIONS = listOf(1 to 0, 0 to 1, 1 to 1, 1 to -1)

    fun decision(
        boardState: BoardState,
        legalMoves: List<GomokuMove>,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null
        val side = boardState.sideToMove
        val opponent = side.opposite()

        // 我方直接成五
        findWinning(boardState, legalMoves, side)?.let { return decision(it, "win-five") }
        // 堵对方成五
        findWinning(boardState, legalMoves, opponent)?.let { return decision(it, "block-five") }

        val best = legalMoves.maxByOrNull { move ->
            scoreShape(boardState, move.to, side) + scoreShape(boardState, move.to, opponent) * 0.9f -
                centerDistance(move.to) * 0.05f
        } ?: return null
        return decision(best, "shape-score")
    }

    private fun findWinning(
        boardState: BoardState,
        legalMoves: List<GomokuMove>,
        side: Side,
    ): GomokuMove? {
        return legalMoves.firstOrNull { move ->
            GameArbiter.hasFive(placeTemp(boardState, move.to, side), move.to, side)
        }
    }

    private fun placeTemp(boardState: BoardState, point: BoardPoint, side: Side): BoardState {
        return boardState.withStonePlaced(GomokuMove(to = point, side = side))
    }

    /** 四个方向上「己方连子长度」加权的形状分;连续 4/3 显著加分 */
    private fun scoreShape(boardState: BoardState, point: BoardPoint, side: Side): Float {
        var score = 0f
        DIRECTIONS.forEach { (df, dr) ->
            val len = 1 +
                countSide(boardState, point, df, dr, side) +
                countSide(boardState, point, -df, -dr, side)
            score += when {
                len >= 4 -> 100f
                len == 3 -> 12f
                len == 2 -> 3f
                else -> 0.1f
            }
        }
        return score
    }

    private fun countSide(
        boardState: BoardState,
        from: BoardPoint,
        df: Int,
        dr: Int,
        side: Side,
    ): Int {
        var count = 0
        var pos = from.offset(df, dr)
        while (pos.isInside() && boardState.stoneAt(pos) == side) {
            count++
            pos = pos.offset(df, dr)
        }
        return count
    }

    private fun centerDistance(point: BoardPoint): Float {
        val center = (BoardPoint.FILE_COUNT - 1) / 2f
        return kotlin.math.abs(point.file - center) + kotlin.math.abs(point.rank - center)
    }

    private fun decision(move: GomokuMove, reason: String): MoveDecision =
        MoveDecision(move, reason, "fallback", fallbackUsed = true)
}
