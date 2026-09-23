package com.wanbaohe.chess.data

import com.wanbaohe.chess.application.port.outbound.MoveDecision
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.ChessMove
import com.wanbaohe.chess.domain.model.PieceType

/**
 * LLM 不可用时的本地启发式兜底:优先吃子(按子力价值差),其次升变,
 * 再次向中心靠拢(中心格子力覆盖最大)。
 */
internal object ChessMoveFallback {

    fun decision(
        boardState: BoardState,
        legalMoves: List<ChessMove>,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

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
