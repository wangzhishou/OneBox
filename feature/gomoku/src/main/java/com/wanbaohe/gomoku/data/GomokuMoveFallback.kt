package com.wanbaohe.gomoku.data

import com.wanbaohe.gomoku.application.port.outbound.MoveDecision
import com.wanbaohe.gomoku.data.search.GomokuSearch
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GomokuMove
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.CancellationException

/**
 * LLM / 服务端引擎全链路失败时的本地兜底。
 *
 * 主路径是 [GomokuSearch] 的浅层搜索(候选点裁剪 + 迭代加深 alpha-beta),能看见活三/冲四
 * 这类两步手段;搜索万一抛异常(理论上不该发生)再退到「成五 > 堵五 > 连子形状评分」的
 * 单层启发式,保证任何情况下都能给出合法着法。
 */
internal object GomokuMoveFallback {

    private val DIRECTIONS = listOf(1 to 0, 0 to 1, 1 to 1, 1 to -1)

    suspend fun decision(
        boardState: BoardState,
        legalMoves: List<GomokuMove>,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val searched = try {
            GomokuSearch.findBestMove(boardState, legalMoves)
        } catch (cancellation: CancellationException) {
            // 协程取消必须原样上抛,否则离开对局页后兜底还在后台空转
            throw cancellation
        } catch (_: Throwable) {
            null
        }

        if (searched != null) {
            return MoveDecision(
                move = searched.move,
                reason = "local-search d=${searched.depth} score=${searched.score} " +
                    "n=${searched.nodes} t=${searched.elapsedMs}ms",
                rawResponse = "local-search",
                fallbackUsed = true,
            )
        }
        return greedy(boardState, legalMoves)
    }

    /** 最后的保命路径:单层启发式,不搜索 */
    private fun greedy(
        boardState: BoardState,
        legalMoves: List<GomokuMove>,
    ): MoveDecision? {
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
