package com.wanbaohe.gomoku.application

import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.application.usecase.ManageGameUseCase
import com.wanbaohe.gomoku.domain.BoardSetupDraft
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ManageGameUseCaseTest {
    private val games = MemoryGames(testGame())
    private val moves = MemoryMoves()
    private val query = GameQueryUseCase(games, moves)
    private val manage = ManageGameUseCase(games, moves, query)

    @Test
    fun pausedUndoAndRedoStayPausedAndDoNotLoseTheRedoHistory() = runBlocking {
        val first = moves.addMove("game", 1, games.game.initialFen, BoardPoint(7, 7))
        val second = moves.addMove("game", 2, first, BoardPoint(7, 8))
        games.game = games.game.copy(currentFen = second, currentPly = 2)
        assertEquals(GameStatus.PAUSED, manage.undo("game", 2)?.status)
        assertEquals(0, games.game.currentPly)
        assertEquals(2, moves.plies.size)
        assertEquals(GameStatus.PAUSED, manage.redo("game", 2)?.status)
        assertEquals(second, games.game.currentFen)
    }

    @Test
    fun editingANotStartedGameUpdatesOnlyItsInitialPositionWithNoFakeMoves() = runBlocking {
        games.game = games.game.copy(status = GameStatus.NOT_STARTED, startedAt = 0, lastPlayedAt = 0)
        val draft = BoardSetupDraft(BoardState.empty()).selectStone(Side.BLACK).tap(BoardPoint(1, 1)).setSideToMove(Side.WHITE)
        assertNotNull(manage.updateInitialPosition("game", draft.startPosition()))
        assertEquals(games.game.initialFen, games.game.currentFen)
        assertEquals(1, FenCodec.parse(games.game.currentFen).moveNumber)
        assertEquals(0, games.game.currentPly)
        assertEquals(0L, games.game.startedAt)
        assertEquals(0L, games.game.lastPlayedAt)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun startedOrOnlineGamesCannotHaveTheirInitialPositionOverwritten(): Unit = runBlocking {
        val original = games.game
        assertFailsWith<IllegalStateException> { manage.updateInitialPosition("game", BoardState.empty(Side.WHITE)) }
        assertEquals(original, games.game)
        games.game = games.game.copy(mode = GameMode.ONLINE_PVP, status = GameStatus.NOT_STARTED, startedAt = 0)
        assertFailsWith<IllegalStateException> { manage.updateInitialPosition("game", BoardState.empty(Side.WHITE)) }
    }

    @Test
    fun aTerminalSetupCannotBeStartedEvenThroughTheUseCase() = runBlocking {
        val cells = MutableList<Side?>(225) { null }
        repeat(6) { cells[BoardPoint(it, 0).index] = Side.BLACK }
        val terminal = FenCodec.encode(BoardState(cells, Side.WHITE))
        games.game = games.game.copy(initialFen = terminal, currentFen = terminal,
            status = GameStatus.NOT_STARTED, startedAt = 0, lastPlayedAt = 0)
        val original = games.game
        manage.start("game")
        assertEquals(original, games.game)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun cancellingAnActiveEditorExcludesItsTimeFromTheNextMoveClock() = runBlocking {
        games.game = games.game.copy(lastMoveAt = 100, lastPlayedAt = 50)
        val resumed = assertNotNull(manage.resumeAfterEditing("game", System.currentTimeMillis() - 5_000))
        assertEquals(GameStatus.PLAYING, resumed.status)
        assertTrue(resumed.lastMoveAt >= 5_100)
        assertEquals(50L, games.game.lastPlayedAt)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun savingWhiteOpponentUsesRedColumnAndDoesNotChangeLastPlayedTime() = runBlocking {
        val blackConfig = games.game.blackPlayerConfigJson
        val white = GameAiPlayerConfig(sourceKey = "rapfi", engineTitle = "Snapshot")
        manage.updateAiConfig("game", Side.WHITE, white)
        assertEquals(white.encode(), games.game.redPlayerConfigJson)
        assertEquals(blackConfig, games.game.blackPlayerConfigJson)
        assertEquals(1L, games.game.lastPlayedAt)
    }

    @Test
    fun restartCreatesAnIndependentEmptyHistoryAndKeepsTheOriginalAndOrigin() = runBlocking {
        val custom = FenCodec.INITIAL_FEN.replace(" b 1", " w 4")
        val source = GameOrigin("earlier-game", 0, custom, "Earlier game")
        games.game = games.game.copy(initialFen = custom, currentFen = custom, origin = source)
        val old = games.game
        val restarted = assertNotNull(manage.restart("game"))
        assertNotEquals(old.id, restarted.id)
        assertEquals(old, games.game)
        assertEquals(custom, restarted.initialFen)
        assertEquals(custom, restarted.currentFen)
        assertEquals(source, restarted.origin)
        assertEquals(GameStatus.NOT_STARTED, restarted.status)
        assertEquals(0, restarted.currentPly)
        assertTrue(restarted.plies.isEmpty())
    }
}
