package com.wanbaohe.chess.application.usecase

import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.dto.GamePreparation
import com.wanbaohe.chess.application.dto.prepareFrom
import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.port.outbound.GameStore
import com.wanbaohe.chess.application.port.outbound.MoveStore
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.GameResultResolver
import com.wanbaohe.chess.domain.SetupPositionValidator
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.Side
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

@Singleton
class ManageGameUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val moveStore: MoveStore,
    private val query: GameQueryUseCase,
    private val mutationLock: GameMutationLock,
) {

    suspend fun start(gameId: String): GameDetail? = mutationLock.withGame(gameId) {
        currentCoroutineContext().ensureActive()
        withContext(NonCancellable) { startInternal(gameId) }
    }

    private suspend fun startInternal(gameId: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        if (game.status == GameStatus.PLAYING || game.status == GameStatus.CHECK) return query.getById(gameId)
        // 已终局的对局不允许被"重新开始"成进行中:那会留下
        // "status 进行中 / resultText 认输"的自相矛盾记录。
        if (GameResultResolver.resultText(game.status).isNotEmpty()) return query.getById(gameId)
        val now = System.currentTimeMillis()
        val status = GameArbiter.evaluateStatus(FenCodec.parse(game.currentFen))
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

    suspend fun pause(gameId: String): GameDetail? = mutationLock.withGame(gameId) { pauseInternal(gameId) }

    private suspend fun pauseInternal(gameId: String): GameDetail? {
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

    suspend fun pauseOffline(gameId: String): GameDetail? = mutationLock.withGame(gameId) {
        val game = gameStore.getById(gameId) ?: return@withGame null
        if (game.mode == GameMode.ONLINE_PVP) query.getById(gameId) else pauseInternal(gameId)
    }

    suspend fun pauseImported(gameId: String, wasStarted: Boolean): GameDetail? = mutationLock.withGame(gameId) {
        val game = gameStore.getById(gameId) ?: return@withGame null
        if (game.status == GameStatus.NOT_STARTED && wasStarted) {
            gameStore.update(game.copy(status = GameStatus.PAUSED))
            query.getById(gameId)
        } else pauseInternal(gameId)
    }

    suspend fun startOnline(gameId: String, roomId: String): GameDetail? = mutationLock.withGame(gameId) {
        val detail = query.getById(gameId) ?: return@withGame null
        if (detail.mode != GameMode.ONLINE_PVP || roomId.isBlank() || detail.onlineMetadata.roomId != roomId) {
            return@withGame null
        }
        currentCoroutineContext().ensureActive()
        withContext(NonCancellable) { startInternal(gameId) }
    }

    suspend fun resignOnline(gameId: String, roomId: String): GameDetail? = mutationLock.withGame(gameId) {
        val detail = query.getById(gameId) ?: return@withGame null
        if (detail.mode != GameMode.ONLINE_PVP || roomId.isBlank() || detail.onlineMetadata.roomId != roomId) {
            return@withGame null
        }
        resignInternal(gameId, detail.onlineMetadata.mySide.opposite(), synchronizingOnline = true)
    }

    suspend fun preparePractice(source: GameDetail, board: BoardState, sourcePly: Int): GamePreparation {
        val preparation = source.prepareFrom(board, sourcePly)
        pauseOffline(source.id)
        return preparation
    }

    suspend fun undo(gameId: String, steps: Int = 1): GameDetail? =
        mutationLock.withGame(gameId) { undoInternal(gameId, steps) }

    private suspend fun undoInternal(gameId: String, steps: Int): GameDetail? {
        require(steps >= 0)
        val game = gameStore.getById(gameId) ?: return null
        val targetPly = (game.currentPly - steps).coerceAtLeast(0)
        if (targetPly == game.currentPly) return query.getById(gameId)
        val targetFen = resolveFenAtPly(game, targetPly)
        updateGameToPly(game, targetPly, targetFen)
        return query.getById(gameId)
    }

    suspend fun redo(gameId: String, steps: Int = 1): GameDetail? =
        mutationLock.withGame(gameId) { redoInternal(gameId, steps) }

    private suspend fun redoInternal(gameId: String, steps: Int): GameDetail? {
        require(steps >= 0)
        val game = gameStore.getById(gameId) ?: return null
        val plies = moveStore.getByGame(gameId)
        val targetPly = (game.currentPly + steps).coerceAtMost(plies.size)
        if (targetPly == game.currentPly) return query.getById(gameId)
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
    suspend fun restart(gameId: String): GameDetail? = mutationLock.withGame(gameId) { restartInternal(gameId) }

    private suspend fun restartInternal(gameId: String): GameDetail? {
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

    suspend fun rename(gameId: String, newTitle: String): GameDetail? =
        mutationLock.withGame(gameId) { renameInternal(gameId, newTitle) }

    private suspend fun renameInternal(gameId: String, newTitle: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val trimmed = newTitle.trim()
        if (trimmed.isBlank() || trimmed == game.title) return query.getById(gameId)
        gameStore.update(game.copy(title = trimmed, updatedAt = System.currentTimeMillis()))
        return query.getById(gameId)
    }

    suspend fun resign(gameId: String, resigningSide: Side): GameDetail? =
        mutationLock.withGame(gameId) { resignInternal(gameId, resigningSide) }

    private suspend fun resignInternal(
        gameId: String,
        resigningSide: Side,
        synchronizingOnline: Boolean = false,
    ): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        // 幂等闸:与 pause() 同款,缺少它时二次认输会翻转 winnerSide
        if (game.status != GameStatus.PLAYING && game.status != GameStatus.CHECK &&
            !(synchronizingOnline && game.status in setOf(GameStatus.PAUSED, GameStatus.NOT_STARTED))
        ) {
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
    ): GameDetail? = mutationLock.withGame(gameId) { applyImportedResultInternal(gameId, status, winnerSide) }

    private suspend fun applyImportedResultInternal(
        gameId: String,
        status: GameStatus,
        winnerSide: String?,
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
                lastPlayedAt = game.lastPlayedAt,
                lastMoveAt = now,
            ),
        )
    }

    suspend fun resumeAfterEditing(gameId: String, editingStartedAt: Long): GameDetail? =
        mutationLock.withGame(gameId) { resumeAfterEditingInternal(gameId, editingStartedAt) }

    private suspend fun resumeAfterEditingInternal(gameId: String, editingStartedAt: Long): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        if (game.status != GameStatus.PAUSED) return query.getById(gameId)
        val now = System.currentTimeMillis()
        gameStore.update(game.copy(
            status = GameArbiter.evaluateStatus(FenCodec.parse(game.currentFen)),
            lastMoveAt = if (game.lastMoveAt > 0L)
                game.lastMoveAt + (now - editingStartedAt).coerceAtLeast(0L) else now,
            updatedAt = now,
        ))
        return query.getById(gameId)
    }

    suspend fun updateInitialPosition(gameId: String, board: BoardState): GameDetail? =
        mutationLock.withGame(gameId) { updateInitialPositionInternal(gameId, board) }

    private suspend fun updateInitialPositionInternal(gameId: String, board: BoardState): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        check(game.mode != GameMode.ONLINE_PVP && game.status == GameStatus.NOT_STARTED &&
            game.startedAt == 0L && game.currentPly == 0)
        val normalized = board.copy(halfMoveClock = 0, fullMoveNumber = 1)
        require(SetupPositionValidator.validate(normalized) == null)
        val fen = FenCodec.encode(normalized)
        gameStore.update(game.copy(initialFen = fen, currentFen = fen, updatedAt = System.currentTimeMillis()))
        return query.getById(gameId)
    }

    suspend fun updateAiConfig(gameId: String, side: Side, config: GameAiPlayerConfig): GameDetail? =
        mutationLock.withGame(gameId) { updateAiConfigInternal(gameId, side, config) }

    private suspend fun updateAiConfigInternal(gameId: String, side: Side, config: GameAiPlayerConfig): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        val next = if (side == Side.WHITE) game.copy(redPlayerConfigJson = config.encode())
            else game.copy(blackPlayerConfigJson = config.encode())
        gameStore.update(next.copy(updatedAt = System.currentTimeMillis()))
        return query.getById(gameId)
    }
}
