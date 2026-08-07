package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.dto.GameDetail
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskStore
import com.wanbaohe.xiangqi.application.port.outbound.GameStore
import com.wanbaohe.xiangqi.application.port.outbound.MoveStore
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameResultResolver
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.Side
import com.shifenmiao.database.activity.ActivityLogRecorder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManageGameUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val moveStore: MoveStore,
    private val aiTaskStore: AiTaskStore,
    private val query: GameQueryUseCase,
    private val activityLogRecorder: ActivityLogRecorder,
) {

    suspend fun start(gameId: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val now = System.currentTimeMillis()
        val status = GameArbiter.evaluateStatus(FenCodec.parse(game.currentFen))
        gameStore.update(
            game.copy(
                status = status,
                startedAt = if (game.startedAt == 0L) now else game.startedAt,
                lastMoveAt = now,
                updatedAt = now,
                lastPlayedAt = now,
            ),
        )
        return query.getById(gameId)
    }

    suspend fun pause(gameId: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        if (game.status != GameStatus.PLAYING && game.status != GameStatus.CHECK) {
            return query.getById(gameId)
        }
        gameStore.update(
            game.copy(
                status = GameStatus.PAUSED,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        return query.getById(gameId)
    }

    suspend fun undo(gameId: String, steps: Int = 1): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val targetPly = (game.currentPly - steps).coerceAtLeast(0)
        val targetFen = resolveFenAtPly(game, targetPly)
        updateGameToPly(game, targetPly, targetFen)
        return query.getById(gameId)
    }

    suspend fun redo(gameId: String, steps: Int = 1): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val plies = moveStore.getByGame(gameId)
        val targetPly = (game.currentPly + steps).coerceAtMost(plies.size)
        val targetFen = if (targetPly == 0) game.initialFen
            else plies.firstOrNull { it.ply == targetPly }?.afterFen ?: game.initialFen
        updateGameToPly(game, targetPly, targetFen)
        return query.getById(gameId)
    }

    suspend fun restart(gameId: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val now = System.currentTimeMillis()
        moveStore.deleteByGame(gameId)
        aiTaskStore.deleteByGame(gameId)
        gameStore.update(
            game.copy(
                currentFen = game.initialFen,
                currentPly = 0,
                status = GameStatus.NOT_STARTED,
                resultText = "",
                winnerSide = "",
                startedAt = 0L,
                lastMoveAt = 0L,
                updatedAt = now,
                lastPlayedAt = now,
            ),
        )
        return query.getById(gameId)
    }

    suspend fun rename(gameId: String, newTitle: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val trimmed = newTitle.trim()
        if (trimmed.isBlank() || trimmed == game.title) return query.getById(gameId)
        val oldTitle = game.title
        gameStore.update(game.copy(title = trimmed, updatedAt = System.currentTimeMillis()))

        activityLogRecorder.recordXiangqi(
            gameId = gameId,
            actionType = "RENAME",
            title = "重命名对局: $trimmed",
            description = "旧名称: $oldTitle",
        )

        return query.getById(gameId)
    }

    suspend fun resign(gameId: String, resigningSide: Side): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val now = System.currentTimeMillis()
        val winner = resigningSide.opposite()
        gameStore.update(
            game.copy(
                status = GameStatus.RESIGNED,
                resultText = "RESIGNED",
                winnerSide = winner.name,
                updatedAt = now,
            ),
        )

        activityLogRecorder.recordXiangqi(
            gameId = gameId,
            actionType = "RESIGN",
            title = "对局认输: ${game.title}",
            description = "${resigningSide.name} 方认输，${winner.name} 方获胜",
        )

        return query.getById(gameId)
    }

    private suspend fun resolveFenAtPly(game: com.wanbaohe.xiangqi.application.port.outbound.GameEntity, targetPly: Int): String =
        if (targetPly == 0) game.initialFen else {
            val stored = moveStore.getByGame(game.id)
            stored.firstOrNull { it.ply == targetPly }?.afterFen ?: game.initialFen
        }

    private suspend fun updateGameToPly(
        game: com.wanbaohe.xiangqi.application.port.outbound.GameEntity,
        targetPly: Int,
        targetFen: String,
    ) {
        val status = GameArbiter.evaluateStatus(FenCodec.parse(targetFen))
        val now = System.currentTimeMillis()
        gameStore.update(
            game.copy(
                currentPly = targetPly,
                currentFen = targetFen,
                status = status,
                resultText = GameResultResolver.resultText(status),
                winnerSide = GameResultResolver.winnerSide(status),
                updatedAt = now,
                lastPlayedAt = now,
            ),
        )
    }
}
