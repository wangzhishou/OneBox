package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.dto.GameDetail
import com.wanbaohe.xiangqi.application.port.outbound.GameEntity
import com.wanbaohe.xiangqi.application.port.outbound.GameStore
import com.wanbaohe.xiangqi.application.port.outbound.MoveStore
import com.wanbaohe.xiangqi.application.port.outbound.PlyEntity
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.GameResultResolver
import com.wanbaohe.xiangqi.domain.UcciNotation
import com.wanbaohe.xiangqi.domain.ChineseNotationFormatter
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayMoveUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val moveStore: MoveStore,
    private val query: GameQueryUseCase,
) {

    sealed interface Result {
        data class Success(val detail: GameDetail) : Result
        data class Rejected(val reason: String) : Result
    }

    suspend fun commit(
        gameId: String,
        move: XiangqiMove,
        aiReason: String = "",
        aiRawResponse: String = "",
    ): Result {
        val game = gameStore.getById(gameId) ?: return Result.Rejected("Game not found")
        val before = FenCodec.parse(game.currentFen)

        val legalMove = findLegalMove(before, move)
            ?: return Result.Rejected("Illegal move: ${move.from} -> ${move.to}")

        moveStore.deleteAfterPly(gameId, game.currentPly)
        val after = before.withPieceMoved(legalMove)
        val afterFen = FenCodec.encode(after)
        val status = GameArbiter.evaluateStatus(after)
        val nextPly = game.currentPly + 1
        val now = System.currentTimeMillis()
        val thinkDuration = computeThinkDuration(game, now)

        moveStore.insert(
            PlyEntity(
                id = "$gameId:$nextPly",
                gameId = gameId,
                ply = nextPly,
                moveUcci = legalMove.notationUcci,
                moveCn = legalMove.notationCn,
                moverSide = before.sideToMove,
                beforeFen = game.currentFen,
                afterFen = afterFen,
                isCapture = legalMove.captured != null,
                isCheck = status == GameStatus.CHECK,
                isCheckmate = status == GameStatus.RED_WINS || status == GameStatus.BLACK_WINS,
                aiReason = aiReason,
                aiRawResponse = aiRawResponse,
                thinkDurationMs = thinkDuration,
            ),
        )

        gameStore.update(
            game.copy(
                currentFen = afterFen,
                currentPly = nextPly,
                status = status,
                resultText = GameResultResolver.resultText(status),
                winnerSide = GameResultResolver.winnerSide(status),
                updatedAt = now,
                lastPlayedAt = now,
                lastMoveAt = now,
            ),
        )

        return Result.Success(query.getById(gameId)!!)
    }

    private fun findLegalMove(before: com.wanbaohe.xiangqi.domain.model.BoardState, desired: XiangqiMove): XiangqiMove? {
        val legalMoves = GameArbiter.legalMoves(before, before.sideToMove)
        val matched = legalMoves.firstOrNull { it.from == desired.from && it.to == desired.to }
            ?: return null
        val enriched = matched.copy(
            notationUcci = if (matched.notationUcci.isBlank())
                UcciNotation.format(matched.from, matched.to) else matched.notationUcci,
            notationCn = if (matched.notationCn.isBlank())
                ChineseNotationFormatter.format(matched) else matched.notationCn,
        )
        return enriched
    }

    private fun computeThinkDuration(game: GameEntity, now: Long): Long {
        val base = if (game.lastMoveAt > 0L) game.lastMoveAt else game.startedAt
        return if (base > 0L) (now - base).coerceAtLeast(0L) else 0L
    }
}
