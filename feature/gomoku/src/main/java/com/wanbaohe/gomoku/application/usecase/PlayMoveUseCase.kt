package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.port.outbound.GameEntity
import com.wanbaohe.gomoku.application.port.outbound.GameStore
import com.wanbaohe.gomoku.application.port.outbound.MoveStore
import com.wanbaohe.gomoku.application.port.outbound.PlyEntity
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.GameResultResolver
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GomokuMove
import com.wanbaohe.gomoku.domain.model.GameStatus
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
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
        expectedGame: GameDetail? = null,
    ): Result {
        val game = gameStore.getById(gameId) ?: return Result.Rejected("Game not found")
        if (game.status != GameStatus.PLAYING && game.status != GameStatus.NOT_STARTED) {
            return Result.Rejected("Game is not playing")
        }
        if (expectedGame != null && (
                game.currentFen != expectedGame.currentFen || game.currentPly != expectedGame.currentPly ||
                    game.status != GameStatus.PLAYING || game.mode != expectedGame.mode ||
                    game.blackPlayerType != expectedGame.blackPlayerType || game.whitePlayerType != expectedGame.whitePlayerType ||
                    GameAiPlayerConfig.decode(game.blackPlayerConfigJson) != expectedGame.blackAiConfig ||
                    GameAiPlayerConfig.decode(game.redPlayerConfigJson) != expectedGame.whiteAiConfig ||
                    game.blackPlayerConfigJson != expectedGame.blackPlayerConfigJson ||
                    game.redPlayerConfigJson != expectedGame.whitePlayerConfigJson
                )
        ) return Result.Rejected("AI request changed")
        val before = FenCodec.parse(game.currentFen)
        if (move.side != before.sideToMove) return Result.Rejected("Not your turn")

        val legalMove = findLegalMove(before, move)
            ?: return Result.Rejected("Illegal move: ${move.to}")

        val after = before.withStonePlaced(legalMove)
        val afterFen = FenCodec.encode(after)
        val status = GameArbiter.evaluateStatus(after, legalMove.to)
        val nextPly = game.currentPly + 1
        val now = System.currentTimeMillis()
        val thinkDuration = computeThinkDuration(game, now)

        currentCoroutineContext().ensureActive()
        // Once accepted, finish both history and position writes before cancellation
        // cleanup can pause the game; never leave a half-written ply.
        return withContext(NonCancellable) {
            moveStore.deleteAfterPly(gameId, game.currentPly)
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

            Result.Success(requireNotNull(query.getById(gameId)))
        }
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
