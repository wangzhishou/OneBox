package com.wanbaohe.chess.application

import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.application.usecase.GameMutationLock
import com.wanbaohe.chess.application.usecase.ManageGameUseCase
import com.wanbaohe.chess.application.usecase.PlayMoveUseCase
import com.wanbaohe.chess.domain.BoardSetupDraft
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.GameResultResolver
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.yield
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ManageGameUseCaseTest {
    private val games = MemoryGames(GameEntity(
        id = "game", title = "Game", mode = GameMode.LOCAL_PVP,
        whitePlayerType = PlayerType.HUMAN, blackPlayerType = PlayerType.HUMAN,
        redPlayerConfigJson = "{}", blackPlayerConfigJson = "{}",
        initialFen = FenCodec.INITIAL_FEN, currentFen = FenCodec.INITIAL_FEN, currentPly = 0,
        status = GameStatus.PLAYING, resultText = "", winnerSide = "",
        startedAt = 1, lastMoveAt = 1, lastPlayedAt = 100, updatedAt = 100,
    ))
    private val moves = MemoryMoves()
    private val query = GameQueryUseCase(games, moves)
    private val mutationLock = GameMutationLock()
    private val manage = ManageGameUseCase(games, moves, query, mutationLock)
    private val play = PlayMoveUseCase(games, moves, query, mutationLock)

    @Test
    fun pausedUndoRedoNeverResumeOrAlterActualPlayTime() = runBlocking {
        val move = GameArbiter.legalMoves(FenCodec.parse(games.game.currentFen)).first()
        assertIs<PlayMoveUseCase.Result.Success>(play.commit("game", move))
        val playedAt = games.game.lastPlayedAt
        manage.pause("game")
        assertEquals(GameStatus.PAUSED, manage.undo("game")?.status)
        assertEquals(playedAt, games.game.lastPlayedAt)
        assertEquals(GameStatus.PAUSED, manage.redo("game")?.status)
        assertEquals(1, games.game.currentPly)
    }

    @Test
    fun editingAnUnstartedRecordOnlyChangesItsInitialPosition() = runBlocking {
        games.game = games.game.copy(status = GameStatus.NOT_STARTED, startedAt = 0, lastPlayedAt = 0)
        val draft = BoardSetupDraft(FenCodec.parse(FenCodec.INITIAL_FEN))
            .move(com.wanbaohe.chess.domain.model.BoardPoint(4, 1), com.wanbaohe.chess.domain.model.BoardPoint(4, 3))
        val result = manage.updateInitialPosition("game", draft.startPosition())
        assertEquals(GameStatus.NOT_STARTED, result?.status)
        assertEquals(result?.initialFen, result?.currentFen)
        assertEquals(0, result?.currentPly)
        assertEquals(0L, games.game.lastPlayedAt)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun cancelEditingResumesOnlyPausedRecordAndExcludesEditingTime() = runBlocking {
        games.game = games.game.copy(status = GameStatus.PAUSED, lastMoveAt = System.currentTimeMillis() - 20_000)
        val before = games.game.lastMoveAt
        val editingStart = System.currentTimeMillis() - 10_000
        manage.resumeAfterEditing("game", editingStart)
        assertEquals(GameStatus.PLAYING, games.game.status)
        assertTrue(games.game.lastMoveAt >= before + 10_000)
        assertEquals(100L, games.game.lastPlayedAt)
    }

    @Test
    fun normalMovesCannotCommitToPausedOrUnstartedRecords() = runBlocking {
        val move = GameArbiter.legalMoves(FenCodec.parse(FenCodec.INITIAL_FEN)).first()
        games.game = games.game.copy(status = GameStatus.PAUSED)
        assertIs<PlayMoveUseCase.Result.Rejected>(play.commit("game", move))
        games.game = games.game.copy(status = GameStatus.NOT_STARTED)
        assertIs<PlayMoveUseCase.Result.Rejected>(play.commit("game", move))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun explicitResultsAndPauseArePreservedWhenHistoryChanges() {
        assertEquals(GameStatus.PAUSED, GameResultResolver.statusAfterHistoryChange(GameStatus.PAUSED, GameStatus.CHECK))
        assertEquals(GameStatus.RESIGNED, GameResultResolver.statusAfterHistoryChange(GameStatus.RESIGNED, GameStatus.PLAYING))
        assertEquals(GameStatus.CHECK, GameResultResolver.statusAfterHistoryChange(GameStatus.WHITE_WINS, GameStatus.CHECK))
        assertFalse(GameResultResolver.isExplicitTerminal(GameStatus.PAUSED))
    }

    @Test
    fun noOpHistoryChangesAndDuplicateStartDoNotResetClocksOrActivity() = runBlocking {
        val before = games.game
        manage.undo("game", 0)
        manage.redo("game", 0)
        manage.start("game")
        assertEquals(before, games.game)
    }

    @Test
    fun pauseWaitsForAnAlreadyAcceptedCommitEvenAfterCancellation() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        moves.beforeInsert = { entered.complete(Unit); release.await() }
        val move = GameArbiter.legalMoves(FenCodec.parse(games.game.currentFen)).first()
        val commit = async { play.commit("game", move) }
        entered.await()
        val pause = async { manage.pause("game") }
        yield()
        assertEquals(GameStatus.PLAYING, games.game.status)
        commit.cancel()
        release.complete(Unit)
        commit.join()
        pause.await()
        assertEquals(GameStatus.PAUSED, games.game.status)
        assertEquals(1, games.game.currentPly)
        assertEquals(1, moves.plies.size)
    }
}
