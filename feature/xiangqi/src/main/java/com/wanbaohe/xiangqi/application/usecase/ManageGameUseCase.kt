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
        // 已终局的对局不允许被"重新开始"成进行中：那会留下
        // "status 进行中 / resultText 认输"的自相矛盾记录。
        if (GameResultResolver.resultText(game.status).isNotEmpty()) return query.getById(gameId)
        val now = System.currentTimeMillis()
        val status = GameArbiter.evaluateStatus(FenCodec.parse(game.currentFen))
        gameStore.update(
            game.copy(
                status = status,
                // 与 restart() 口径一致：进入可下状态时结果字段同步刷新。
                resultText = GameResultResolver.resultText(status),
                winnerSide = GameResultResolver.winnerSide(status),
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
        // 幂等闸：与 pause() 同款。缺少它时二次认输会翻转 winnerSide 并追加矛盾审计日志。
        if (game.status != GameStatus.PLAYING && game.status != GameStatus.CHECK) {
            return query.getById(gameId)
        }
        val now = System.currentTimeMillis()
        val winner = resigningSide.opposite()
        gameStore.update(
            game.copy(
                status = GameStatus.RESIGNED,
                resultText = GameResultResolver.resultText(GameStatus.RESIGNED),
                winnerSide = GameResultResolver.resignWinnerCode(resigningSide),
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

    /**
     * 写入**导入棋谱自带的**终局结果。
     *
     * 认输、和棋这类结果无法从盘面重算，导入时必须从文件恢复，否则"导入一局认输的棋"会退化成
     * 一个无法解释的进行中对局。胜方优先取文件里的 `winnerSide`，缺失时按结果码推导。
     */
    suspend fun applyImportedResult(
        gameId: String,
        status: GameStatus,
        winnerSide: String? = null,
    ): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        if (GameResultResolver.resultText(status).isEmpty()) return query.getById(gameId)
        gameStore.update(
            game.copy(
                status = status,
                resultText = GameResultResolver.resultText(status),
                winnerSide = winnerSide ?: GameResultResolver.winnerSide(status),
                updatedAt = System.currentTimeMillis(),
            ),
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
        val evaluated = GameArbiter.evaluateStatus(FenCodec.parse(targetFen))
        val now = System.currentTimeMillis()
        // 认输是「非盘面终局」：悔棋/重做只能改变局面，不能推翻"已经认输"这件事。
        // 直接采用 evaluateStatus 会把 RESIGNED 顶成 PLAYING/CHECK，并把结果字段洗成空。
        val keepExplicitResult = GameResultResolver.isExplicitTerminal(game.status)
        val status = if (keepExplicitResult) game.status else evaluated
        gameStore.update(
            game.copy(
                currentPly = targetPly,
                currentFen = targetFen,
                status = status,
                resultText = if (keepExplicitResult) game.resultText
                else GameResultResolver.resultText(status),
                winnerSide = if (keepExplicitResult) game.winnerSide
                else GameResultResolver.winnerSide(status),
                updatedAt = now,
                lastPlayedAt = now,
            ),
        )
    }
}
