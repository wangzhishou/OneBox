package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.port.outbound.GameEntity
import com.wanbaohe.gomoku.application.port.outbound.GameStore
import com.wanbaohe.gomoku.application.port.outbound.MoveStore
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.GameResultResolver
import com.wanbaohe.gomoku.domain.SetupPositionValidator
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.Side
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManageGameUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val moveStore: MoveStore,
    private val query: GameQueryUseCase,
) {

    suspend fun start(gameId: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        // 已终局的对局不允许被"重新开始"成进行中:那会留下
        // "status 进行中 / resultText 认输"的自相矛盾记录。
        if (GameResultResolver.resultText(game.status).isNotEmpty()) return query.getById(gameId)
        val now = System.currentTimeMillis()
        val status = GameArbiter.evaluateStatus(FenCodec.parse(game.currentFen))
        if (status != GameStatus.PLAYING) return query.getById(gameId)
        gameStore.update(
            game.copy(
                status = status,
                // 与 restart() 口径一致:进入可下状态时结果字段同步刷新。
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
        if (game.status != GameStatus.PLAYING) {
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

    suspend fun resumeAfterEditing(gameId: String, editingStartedAt: Long): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        if (game.status != GameStatus.PAUSED) return query.getById(gameId)
        val now = System.currentTimeMillis()
        gameStore.update(
            game.copy(
                status = GameArbiter.evaluateStatus(FenCodec.parse(game.currentFen)),
                lastMoveAt = if (game.lastMoveAt > 0L)
                    game.lastMoveAt + (now - editingStartedAt).coerceAtLeast(0L) else now,
                updatedAt = now,
            ),
        )
        return query.getById(gameId)
    }

    suspend fun updateInitialPosition(gameId: String, board: BoardState): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        check(game.mode != GameMode.ONLINE_PVP)
        check(game.status == GameStatus.NOT_STARTED && game.startedAt == 0L && game.currentPly == 0)
        require(SetupPositionValidator.validate(board) == null)
        val fen = FenCodec.encode(board.copy(moveNumber = 1))
        gameStore.update(game.copy(initialFen = fen, currentFen = fen, updatedAt = System.currentTimeMillis()))
        return query.getById(gameId)
    }

    suspend fun updateAiConfig(gameId: String, side: Side, config: GameAiPlayerConfig): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val playerType = if (side == Side.BLACK) game.blackPlayerType else game.whitePlayerType
        check(game.mode != GameMode.ONLINE_PVP && playerType == PlayerType.LLM)
        require(config.isSupported)
        val next = if (side == Side.WHITE) game.copy(redPlayerConfigJson = config.encode())
            else game.copy(blackPlayerConfigJson = config.encode())
        gameStore.update(next.copy(updatedAt = System.currentTimeMillis()))
        return query.getById(gameId)
    }

    suspend fun undo(gameId: String, steps: Int = 1): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        if (steps <= 0 || game.mode == GameMode.ONLINE_PVP) return query.getById(gameId)
        val targetPly = (game.currentPly - steps).coerceAtLeast(0)
        val targetFen = resolveFenAtPly(game, targetPly)
        updateGameToPly(game, targetPly, targetFen)
        return query.getById(gameId)
    }

    suspend fun redo(gameId: String, steps: Int = 1): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        if (steps <= 0 || game.mode == GameMode.ONLINE_PVP) return query.getById(gameId)
        val plies = moveStore.getByGame(gameId)
        val targetPly = (game.currentPly + steps).coerceAtMost(plies.size)
        val targetFen = if (targetPly == 0) game.initialFen
            else plies.firstOrNull { it.ply == targetPly }?.afterFen ?: game.initialFen
        updateGameToPly(game, targetPly, targetFen)
        return query.getById(gameId)
    }

    /**
     * 重新开局 = 以原局配置(模式/执子方/初始局面/联机房间)**新建**一条对局。
     * 旧局原样保留:终局留在棋库作历史,进行中的局仍是可继续的暂停/进行中记录,
     * 不伪造认输结果。返回新局详情(未开局时重开无意义,直接返回原局)。
     */
    suspend fun restart(gameId: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        if (game.status == GameStatus.NOT_STARTED) return query.getById(gameId)
        val now = System.currentTimeMillis()
        val newGameId = gameStore.insert(
            game.copy(
                id = UUID.randomUUID().toString(),
                currentFen = game.initialFen,
                currentPly = 0,
                status = GameStatus.NOT_STARTED,
                resultText = "",
                winnerSide = "",
                startedAt = 0L,
                lastMoveAt = 0L,
                lastPlayedAt = 0L,
                updatedAt = now,
            ),
        )
        return query.getById(newGameId)
    }

    suspend fun rename(gameId: String, newTitle: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val trimmed = newTitle.trim()
        if (trimmed.isBlank() || trimmed == game.title) return query.getById(gameId)
        gameStore.update(game.copy(title = trimmed, updatedAt = System.currentTimeMillis()))
        return query.getById(gameId)
    }

    suspend fun resign(gameId: String, resigningSide: Side): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        // 幂等闸:与 pause() 同款,缺少它时二次认输会翻转 winnerSide
        if (game.status != GameStatus.PLAYING) {
            return query.getById(gameId)
        }
        val now = System.currentTimeMillis()
        gameStore.update(
            game.copy(
                status = GameStatus.RESIGNED,
                resultText = GameResultResolver.resultText(GameStatus.RESIGNED),
                winnerSide = GameResultResolver.resignWinnerCode(resigningSide),
                updatedAt = now,
            ),
        )
        return query.getById(gameId)
    }

    /**
     * 写入**导入棋谱自带的**终局结果。
     *
     * 认输、和棋这类结果无法从盘面重算,导入时必须从文件恢复,否则"导入一局认输的棋"会退化成
     * 一个无法解释的进行中对局。胜方优先取文件里的 `winnerSide`,缺失时按结果码推导。
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

    private suspend fun resolveFenAtPly(game: GameEntity, targetPly: Int): String =
        if (targetPly == 0) game.initialFen else {
            val stored = moveStore.getByGame(game.id)
            stored.firstOrNull { it.ply == targetPly }?.afterFen ?: game.initialFen
        }

    private suspend fun updateGameToPly(
        game: GameEntity,
        targetPly: Int,
        targetFen: String,
    ) {
        val evaluated = GameArbiter.evaluateStatus(FenCodec.parse(targetFen))
        val now = System.currentTimeMillis()
        // 认输是「非盘面终局」:悔棋/重做只能改变局面,不能推翻"已经认输"这件事。
        // 直接采用 evaluateStatus 会把 RESIGNED 顶成 PLAYING,并把结果字段洗成空。
        val keepExplicitResult = GameResultResolver.isExplicitTerminal(game.status)
        val status = GameResultResolver.statusAfterHistoryChange(game.status, evaluated)
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
                lastMoveAt = now,
            ),
        )
    }
}
