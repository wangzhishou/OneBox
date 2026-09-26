package com.wanbaohe.chess.data

import com.wanbaohe.chess.application.port.outbound.MoveDecision
import com.wanbaohe.chess.data.search.ChessSearch
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.ChessMove
import com.wanbaohe.chess.domain.model.PieceType
import kotlinx.coroutines.CancellationException

/**
 * LLM / 服务端引擎全链路失败时的本地兜底。
 *
 * 主路径是 [ChessSearch] 的浅层搜索(迭代加深 alpha-beta + 战术静态搜索),离线时能正常对弈;
 * 搜索万一抛异常(理论上不该发生)再退到「优先吃子、其次升变、再次向中心靠拢」的贪心挑法,
 * 保证任何情况下都给出合法着法。
 */
internal object ChessMoveFallback {

    suspend fun decision(
        boardState: BoardState,
        legalMoves: List<ChessMove>,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val searched = try {
            ChessSearch.findBestMove(boardState, legalMoves)
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
        return greedy(legalMoves)
    }

    /** 最后的保命路径:不搜索,直接按吃子价值 / 升变 / 向中心挑一手 */
    private fun greedy(legalMoves: List<ChessMove>): MoveDecision? {
        // 吃子:被吃子价值 - 攻击子价值*0.1(小换大优先)
        val capture = legalMoves.filter { it.captured != null }
            .maxByOrNull { (it.captured?.value ?: 0) * 10 - it.piece.value }
        if (capture != null && (capture.captured?.value ?: 0) >= 3) {
            return decision(capture, "capture-gain")
        }

        // 升变
        legalMoves.firstOrNull { it.promotion == PieceType.QUEEN }
            ?.let { return decision(it, "promote-queen") }

        // 有价值的吃子(兵换兵等)
        if (capture != null) return decision(capture, "capture")

        // 向中心靠拢
        val best = legalMoves.maxByOrNull { move ->
            val centerScore = 8f - centerDistance(move.to)
            val developBonus = if (move.piece.type == PieceType.KNIGHT || move.piece.type == PieceType.BISHOP) 1f else 0f
            centerScore + developBonus
        } ?: return null
        return decision(best, "center-shape")
    }

    private fun centerDistance(point: BoardPoint): Float {
        val center = (BoardPoint.FILE_COUNT - 1) / 2f
        return kotlin.math.abs(point.file - center) + kotlin.math.abs(point.rank - center)
    }

    private fun decision(move: ChessMove, reason: String): MoveDecision =
        MoveDecision(move, reason, "fallback", fallbackUsed = true)
}
