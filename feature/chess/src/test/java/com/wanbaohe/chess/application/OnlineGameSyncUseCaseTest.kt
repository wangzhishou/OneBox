package com.wanbaohe.chess.application

import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.usecase.GameMutationLock
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.application.usecase.ManageGameUseCase
import com.wanbaohe.chess.application.usecase.OnlineGameEvent
import com.wanbaohe.chess.application.usecase.OnlineGameSyncUseCase
import com.wanbaohe.chess.application.usecase.PlayMoveUseCase
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnlineGameSyncUseCaseTest {
    private val games = MemoryGames(GameEntity(
        id = "game", title = "Online", mode = GameMode.ONLINE_PVP,
        whitePlayerType = PlayerType.HUMAN, blackPlayerType = PlayerType.REMOTE,
        redPlayerConfigJson = """{"roomId":"room","mySide":"WHITE"}""",
        blackPlayerConfigJson = """{"roomId":"room","mySide":"WHITE"}""",
        initialFen = FenCodec.INITIAL_FEN, currentFen = FenCodec.INITIAL_FEN, currentPly = 0,
        status = GameStatus.PLAYING, resultText = "", winnerSide = "",
        startedAt = 1, lastMoveAt = 1, lastPlayedAt = 10, updatedAt = 10,
    ))
    private val moves = MemoryMoves()
    private val query = GameQueryUseCase(games, moves)
    private val lock = GameMutationLock()
    private val play = PlayMoveUseCase(games, moves, query, lock)
    private val manage = ManageGameUseCase(games, moves, query, lock)
    private val sync = OnlineGameSyncUseCase(play, manage)
    private val remoteMove = OnlineGameEvent.Move("room", BoardPoint(4, 6), BoardPoint(4, 4))

    @Test
    fun acknowledgedMovePersistsWithoutAnActiveVisibleGameUi() = runBlocking {
        commitHumanOpening()
        val detail = assertNotNull(sync.accept("game", remoteMove))
        assertEquals(2, detail.currentPly)
        assertEquals(Side.WHITE, FenCodec.parse(detail.currentFen).sideToMove)
        assertEquals(listOf("e2e4", "e7e5"), moves.plies.map { it.moveUcci })
        assertEquals(GameStatus.PLAYING, games.game.status)
    }

    @Test
    fun receivedMoveWaitsForTheAcceptedHumanCommitAndReadsTheCommittedPosition() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        moves.beforeInsert = { entered.complete(Unit); release.await() }
        val human = async { commitHumanOpening() }
        entered.await()
        val remote = async { sync.accept("game", remoteMove) }
        yield()
        assertFalse(remote.isCompleted)
        release.complete(Unit)
        human.await()
        assertNotNull(remote.await())
        assertEquals(2, games.game.currentPly)
        assertEquals(listOf("e2e4", "e7e5"), moves.plies.map { it.moveUcci })
    }

    @Test
    fun legacyLocallyPausedOnlineGamesKeepReceivedMovesAndTheirPausedState() = runBlocking {
        commitHumanOpening()
        manage.pause("game")
        val detail = assertNotNull(sync.accept("game", remoteMove))
        assertEquals(GameStatus.PAUSED, detail.status)
        assertEquals(2, detail.currentPly)
        assertEquals(Side.WHITE, FenCodec.parse(detail.currentFen).sideToMove)
        assertEquals("", detail.resultText)
        assertTrue(games.game.lastPlayedAt > 10)
    }

    @Test
    fun receivedStartIsPersistedWhileHiddenAndIsIdempotent() = runBlocking {
        games.game = games.game.copy(status = GameStatus.NOT_STARTED, startedAt = 0, lastMoveAt = 0)
        assertEquals(GameStatus.PLAYING, sync.accept("game", OnlineGameEvent.Started("room"))?.status)
        val started = games.game
        assertTrue(started.startedAt > 0)
        sync.accept("game", OnlineGameEvent.Started("room"))
        assertEquals(started, games.game)
    }

    @Test
    fun receivedResignIsPersistedWhilePausedAndPreservesAnAlreadyTerminalResult() = runBlocking {
        manage.pause("game")
        assertEquals(GameStatus.RESIGNED, sync.accept("game", OnlineGameEvent.Resigned("room"))?.status)
        assertEquals(Side.WHITE.name, games.game.winnerSide)
        val resigned = games.game
        sync.accept("game", OnlineGameEvent.Started("room"))
        sync.accept("game", OnlineGameEvent.Resigned("room"))
        assertEquals(resigned, games.game)
    }

    @Test
    fun remoteCheckmateWhileLocallyPausedPersistsTheTerminalResultRatherThanHidingIt() = runBlocking {
        commitHumanMove("f2f3")
        assertNotNull(sync.accept("game", remoteMove))
        commitHumanMove("g2g4")
        manage.pause("game")
        val detail = assertNotNull(sync.accept(
            "game", OnlineGameEvent.Move("room", BoardPoint(3, 7), BoardPoint(7, 3)),
        ))
        assertEquals(4, detail.currentPly)
        assertEquals(GameStatus.BLACK_WINS, detail.status)
        assertEquals("BLACK", detail.resultText)
        assertEquals(Side.BLACK.name, detail.winnerSide)
        assertTrue(moves.plies.last().isCheckmate)
    }

    @Test
    fun remoteEventsFromAnotherRoomOrForALocalRecordCannotMutateThisGame() = runBlocking {
        commitHumanOpening()
        val before = games.game
        assertNull(sync.accept("game", remoteMove.copy(roomId = "another-room")))
        assertNull(sync.accept("game", OnlineGameEvent.Started("another-room")))
        assertNull(sync.accept("game", OnlineGameEvent.Resigned("another-room")))
        assertEquals(before, games.game)
        games.game = before.copy(mode = GameMode.LOCAL_PVP, blackPlayerType = PlayerType.HUMAN)
        val local = games.game
        assertNull(sync.accept("game", remoteMove))
        assertNull(sync.accept("game", OnlineGameEvent.Started("room")))
        assertNull(sync.accept("game", OnlineGameEvent.Resigned("room")))
        assertEquals(local, games.game)
    }

    @Test
    fun invalidOrOutOfTurnRemoteMovesDoNotOverwriteTheCurrentBoard() = runBlocking {
        val before = games.game
        assertNull(sync.accept("game", remoteMove))
        assertEquals(before, games.game)
        commitHumanOpening()
        val afterHuman = games.game
        assertNull(sync.accept("game", remoteMove.copy(to = BoardPoint(4, 3))))
        assertEquals(afterHuman, games.game)
        assertEquals(1, moves.plies.size)
    }

    @Test
    fun analysisEntryPauseExemptsOnlineWithoutMutatingTheRecord(): Unit = runBlocking {
        commitHumanOpening()
        val before = games.game
        val history = moves.plies.toList()
        assertEquals(GameStatus.PLAYING, manage.pauseOffline("game")?.status)
        assertEquals(before, games.game)
        assertEquals(history, moves.plies)
        assertNotNull(sync.accept("game", remoteMove))
    }

    @Test
    fun practiceFromAnOnlineReplayIsRejectedBeforeAnyPauseOrMutation() = runBlocking {
        commitHumanOpening()
        val source = assertNotNull(query.getById("game"))
        val before = games.game
        val history = moves.plies.toList()
        assertFailsWith<IllegalArgumentException> {
            manage.preparePractice(source, FenCodec.parse(source.initialFen), 0)
        }
        assertEquals(before, games.game)
        assertEquals(history, moves.plies)
    }

    private suspend fun commitHumanOpening() {
        commitHumanMove("e2e4")
    }

    private suspend fun commitHumanMove(notation: String) {
        val move = GameArbiter.legalMoves(FenCodec.parse(games.game.currentFen)).first { it.notationUcci == notation }
        assertIs<PlayMoveUseCase.Result.Success>(play.commit("game", move))
    }
}
