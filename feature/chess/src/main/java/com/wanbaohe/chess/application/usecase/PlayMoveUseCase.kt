package com.wanbaohe.chess.application.usecase

import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.port.outbound.GameStore
import com.wanbaohe.chess.application.port.outbound.MoveStore
import com.wanbaohe.chess.application.port.outbound.PlyEntity
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.GameResultResolver
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.ChessMove
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

@Singleton
class PlayMoveUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val moveStore: MoveStore,
    private val query: GameQueryUseCase,
    private val mutationLock: GameMutationLock,
) {

    sealed interface Result {
        data class Success(val detail: GameDetail) : Result
        data class Rejected(val reason: String) : Result
    }

    suspend fun commit(
        gameId: String,
        move: ChessMove,
        aiReason: String = "",
        aiRawResponse: String = "",
        expected: GameDetail? = null,
        importing: Boolean = false,
    ): Result {
        return mutationLock.withGame(gameId) {
            currentCoroutineContext().ensureActive()
            withContext(NonCancellable) {
                commitInternal(gameId, move, aiReason, aiRawResponse, expected, importing)
            }
        }
    }

    suspend fun commitOnline(gameId: String, roomId: String, from: BoardPoint, to: BoardPoint): Result =
        mutationLock.withGame(gameId) {
            currentCoroutineContext().ensureActive()
            withContext(NonCancellable) {
                val detail = query.getById(gameId) ?: return@withContext Result.Rejected("Game not found")
                if (detail.mode != GameMode.ONLINE_PVP || roomId.isBlank() ||
                    detail.onlineMetadata.roomId != roomId || detail.resultText.isNotEmpty() ||
                    detail.status !in setOf(GameStatus.NOT_STARTED, GameStatus.PLAYING, GameStatus.CHECK, GameStatus.PAUSED)
                ) return@withContext Result.Rejected("Online session is not active")
                val board = FenCodec.parse(detail.currentFen)
                val player = if (board.sideToMove == Side.WHITE) detail.whitePlayerType else detail.blackPlayerType
                if (board.sideToMove == detail.onlineMetadata.mySide || player != PlayerType.REMOTE) {
                    return@withContext Result.Rejected("Not the remote player's turn")
                }
                val candidates = GameArbiter.legalMoves(board).filter { it.from == from && it.to == to }
                val move = candidates.firstOrNull { it.promotion == PieceType.QUEEN } ?: candidates.firstOrNull()
                    ?: return@withContext Result.Rejected("Illegal online move")
                commitInternal(gameId, move, "", "", null, importing = false, synchronizingOnline = true)
            }
        }

    private suspend fun commitInternal(
        gameId: String,
        move: ChessMove,
        aiReason: String,
        aiRawResponse: String,
        expected: GameDetail?,
        importing: Boolean,
        synchronizingOnline: Boolean = false,
    ): Result {
        val game = gameStore.getById(gameId) ?: return Result.Rejected("Game not found")
        if (!importing && !synchronizingOnline && game.status != GameStatus.PLAYING && game.status != GameStatus.CHECK) {
            return Result.Rejected("Game is not playing")
        }
        if (expected != null && !AiOrchestrationUseCase.isConsistent(expected, query.getById(gameId))) {
            return Result.Rejected("Position changed")
        }
        val before = FenCodec.parse(game.currentFen)

        val legalMove = findLegalMove(before, move)
            ?: return Result.Rejected("Illegal move: ${move.from} -> ${move.to}")

        moveStore.deleteAfterPly(gameId, game.currentPly)
        val after = before.withPieceMoved(legalMove)
        val afterFen = FenCodec.encode(after)
        val evaluated = GameArbiter.evaluateStatus(after)
        val status = if (synchronizingOnline && game.status == GameStatus.PAUSED) {
            GameResultResolver.statusAfterHistoryChange(game.status, evaluated)
        } else evaluated
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
                isCheck = evaluated == GameStatus.CHECK,
                isCheckmate = evaluated == GameStatus.WHITE_WINS || evaluated == GameStatus.BLACK_WINS,
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
                lastPlayedAt = if (importing) game.lastPlayedAt else now,
                lastMoveAt = now,
                startedAt = if (synchronizingOnline && game.startedAt == 0L) now else game.startedAt,
            ),
        )

        return Result.Success(query.getById(gameId)!!)
    }

    private fun findLegalMove(before: BoardState, desired: ChessMove): ChessMove? {
        // GameArbiter 出口已补全记谱字段(UCI 坐标);升变需匹配 promotion
        return GameArbiter.legalMoves(before, before.sideToMove)
            .firstOrNull { it.from == desired.from && it.to == desired.to && it.promotion == desired.promotion }
    }

    private fun computeThinkDuration(game: GameEntity, now: Long): Long {
        val base = if (game.lastMoveAt > 0L) game.lastMoveAt else game.startedAt
        return if (base > 0L) (now - base).coerceAtLeast(0L) else 0L
    }
}
