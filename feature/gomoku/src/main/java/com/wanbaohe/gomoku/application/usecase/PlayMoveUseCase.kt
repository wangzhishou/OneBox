package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.port.outbound.GameEntity
import com.wanbaohe.gomoku.application.port.outbound.GameStore
import com.wanbaohe.gomoku.application.port.outbound.MoveStore
import com.wanbaohe.gomoku.application.port.outbound.PlyEntity
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.GameResultResolver
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GomokuMove
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
        move: GomokuMove,
        aiReason: String = "",
        aiRawResponse: String = "",
    ): Result {
        val game = gameStore.getById(gameId) ?: return Result.Rejected("Game not found")
        val before = FenCodec.parse(game.currentFen)

        val legalMove = findLegalMove(before, move)
            ?: return Result.Rejected("Illegal move: ${move.to}")

        moveStore.deleteAfterPly(gameId, game.currentPly)
        val after = before.withStonePlaced(legalMove)
        val afterFen = FenCodec.encode(after)
        val status = GameArbiter.evaluateStatus(after, legalMove.to)
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
                isCapture = false,
                isCheck = false,
                isCheckmate = false,
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

    private fun findLegalMove(before: BoardState, desired: GomokuMove): GomokuMove? {
        // GameArbiter 出口已补全记谱字段(坐标格式)
        return GameArbiter.legalMoves(before, before.sideToMove)
            .firstOrNull { it.to == desired.to }
    }

    private fun computeThinkDuration(game: GameEntity, now: Long): Long {
        val base = if (game.lastMoveAt > 0L) game.lastMoveAt else game.startedAt
        return if (base > 0L) (now - base).coerceAtLeast(0L) else 0L
    }
}
