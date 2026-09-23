package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.domain.model.GomokuMove

private val FIVE_DIRECTIONS = listOf(
    1 to 0,   // 横
    0 to 1,   // 竖
    1 to 1,   // 斜 /
    1 to -1,  // 斜 \
)

/**
 * Pure function arbiter。给定局面与目标落子,产出新局面与对局状态,或拒绝该手。
 * 落子后检查横/竖/双斜五连胜,满盘和棋;无将军概念(休闲规则,不做禁手)。
 */
object GameArbiter {

    fun legalMoves(boardState: BoardState, side: Side = boardState.sideToMove): List<GomokuMove> =
        MoveGenerator.pseudoLegalMoves(boardState, side)
            .map { it.copy(notationUcci = it.to.toCoordinate(), notationCn = it.to.toCoordinate()) }

    fun applyMove(boardState: BoardState, move: GomokuMove): MoveOutcome {
        if (move.side != boardState.sideToMove) {
            return MoveOutcome.Rejected("Not your turn")
        }
        if (!move.to.isInside() || !boardState.isEmpty(move.to)) {
            return MoveOutcome.Rejected("Cell is not empty")
        }

        val nextState = boardState.withStonePlaced(move)
        val status = evaluateStatus(nextState, move.to)
        return MoveOutcome.Applied(nextState, move, status)
    }

    /** [lastMoveAt] 为刚落下的位置;判胜只需从该子沿四个方向数连子。为空时全盘面扫描。 */
    fun evaluateStatus(boardState: BoardState, lastMoveAt: BoardPoint? = null): GameStatus {
        val winner = if (lastMoveAt != null) {
            boardState.stoneAt(lastMoveAt)
                ?.takeIf { hasFive(boardState, lastMoveAt, it) }
        } else {
            detectWinner(boardState)
        }
        if (winner != null) {
            return if (winner == Side.BLACK) GameStatus.BLACK_WINS else GameStatus.WHITE_WINS
        }
        return if (boardState.isFull()) GameStatus.DRAW else GameStatus.PLAYING
    }

    /** 全盘面扫描判胜(回放/终局状态重建等没有「刚落的子」场景用) */
    fun detectWinner(boardState: BoardState): Side? {
        for (rank in 0 until BoardPoint.RANK_COUNT) {
            for (file in 0 until BoardPoint.FILE_COUNT) {
                val point = BoardPoint(file, rank)
                val side = boardState.stoneAt(point) ?: continue
                if (hasFive(boardState, point, side)) return side
            }
        }
        return null
    }

    fun hasFive(boardState: BoardState, point: BoardPoint, side: Side): Boolean =
        FIVE_DIRECTIONS.any { (df, dr) ->
            1 + countStones(boardState, point, df, dr, side) +
                countStones(boardState, point, -df, -dr, side) >= 5
        }

    private fun countStones(
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
}

sealed interface MoveOutcome {
    data class Applied(
        val boardState: BoardState,
        val move: GomokuMove,
        val status: GameStatus,
    ) : MoveOutcome

    data class Rejected(val reason: String) : MoveOutcome
}
