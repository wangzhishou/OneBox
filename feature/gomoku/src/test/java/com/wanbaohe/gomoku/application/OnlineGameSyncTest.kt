package com.wanbaohe.gomoku.application

import com.wanbaohe.gomoku.application.port.outbound.MoveStore
import com.wanbaohe.gomoku.application.port.outbound.PlyEntity
import com.wanbaohe.gomoku.application.port.outbound.RoomInfo
import com.wanbaohe.gomoku.application.port.outbound.SignalingClient
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.application.usecase.ManageGameUseCase
import com.wanbaohe.gomoku.application.usecase.OnlineGameEvent
import com.wanbaohe.gomoku.application.usecase.OnlinePlayUseCase
import com.wanbaohe.gomoku.application.usecase.PlayMoveUseCase
import com.wanbaohe.gomoku.component.GomokuGameComponent.GomokuOnlineGameSync
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.OnlineMessage
import com.wanbaohe.gomoku.domain.model.OnlineRoomConfig
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OnlineGameSyncTest {
    @Test
    fun queuedStartMoveAndResignPersistInOrderWithoutAnyVisibleGameComponent(): Unit = runBlocking {
        val initialFen = FenCodec.INITIAL_FEN.replace(" b 1", " w 1")
        val fixture = Fixture(fen = initialFen, status = GameStatus.NOT_STARTED)
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true, config = OnlineRoomConfig(initialFen))
            val listener = requireNotNull(client.listener)
            listener.onMessage(OnlineMessage(
                type = "start", roomId = "room", senderSide = Side.WHITE.name, fen = initialFen,
            ))
            listener.onMessage(OnlineMessage(
                type = "move", roomId = "room", senderSide = Side.WHITE.name,
                seq = 2, fromFile = 7, fromRank = 7,
            ))
            listener.onMessage(OnlineMessage(
                type = "resign", roomId = "room", senderSide = Side.WHITE.name,
            ))
            assertEquals(1, client.sent.count { it.type == "ack" })
            val finished = CompletableDeferred<Unit>()
            val statuses = mutableListOf<GameStatus>()
            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                online.opponentEvents("room").collect { event ->
                    val applied = assertIs<GomokuOnlineGameSync.Result.Applied>(fixture.sync.accept("game", event))
                    statuses += applied.detail.status
                    if (event is OnlineGameEvent.Resigned) finished.complete(Unit)
                }
            }
            try {
                withTimeout(3_000) { finished.await() }
                yield()
                assertEquals(listOf(GameStatus.PLAYING, GameStatus.PLAYING, GameStatus.RESIGNED), statuses)
                assertEquals(initialFen, fixture.games.game.initialFen)
                assertEquals(1, fixture.games.game.currentPly)
                assertEquals(Side.BLACK.name, fixture.games.game.winnerSide)
                assertEquals(Side.WHITE, fixture.storedMoves.plies.single().moverSide)
                assertEquals("H8", fixture.storedMoves.plies.single().moveUcci)
                assertEquals(fixture.storedMoves.plies.single().afterFen, fixture.games.game.currentFen)
                assertTrue(fixture.games.game.startedAt > 0)
            } finally {
                collector.cancelAndJoin()
            }
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun receivedMoveWaitsForThePendingHumanCommitAndUsesItsCommittedFen(): Unit = runBlocking {
        val fixture = Fixture()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        fixture.beforeInsert = { ply ->
            if (ply.moverSide == Side.BLACK) {
                entered.complete(Unit)
                release.await()
            }
        }
        val human = async { fixture.humanMove(BoardPoint(7, 7)) }
        withTimeout(3_000) { entered.await() }
        val remote = async {
            fixture.sync.accept("game", OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8), 2))
        }
        yield()
        assertFalse(remote.isCompleted)
        release.complete(Unit)
        withTimeout(3_000) {
            human.await()
            assertIs<GomokuOnlineGameSync.Result.Applied>(remote.await())
        }
        assertEquals(2, fixture.games.game.currentPly)
        assertEquals(listOf(Side.BLACK, Side.WHITE), fixture.storedMoves.plies.map { it.moverSide })
        assertEquals(
            fixture.storedMoves.plies.first().afterFen,
            fixture.storedMoves.plies.last().beforeFen,
        )
        assertEquals(Side.BLACK, FenCodec.parse(fixture.games.game.currentFen).sideToMove)
    }

    @Test
    fun cancellingAnAcceptedOnlineCommitFinishesBothHistoryAndPositionWrites(): Unit = runBlocking {
        val fixture = Fixture()
        fixture.humanMove(BoardPoint(7, 7))
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        fixture.beforeInsert = {
            entered.complete(Unit)
            release.await()
        }
        val remote = async {
            fixture.sync.accept("game", OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8), 2))
        }
        withTimeout(3_000) { entered.await() }
        remote.cancel()
        yield()
        assertFalse(remote.isCompleted)
        release.complete(Unit)
        withTimeout(3_000) { remote.join() }
        assertEquals(2, fixture.games.game.currentPly)
        assertEquals(2, fixture.storedMoves.plies.size)
        assertEquals(fixture.storedMoves.plies.last().afterFen, fixture.games.game.currentFen)
        assertEquals(GameStatus.PLAYING, fixture.games.game.status)
    }

    @Test
    fun cancelledCollectorRetiresTheSavedMoveAndReopeningStillPersistsItsQueuedResignation(): Unit = runBlocking {
        val fixture = Fixture()
        fixture.humanMove(BoardPoint(7, 7))
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        fixture.beforeInsert = {
            entered.complete(Unit)
            release.await()
        }
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            val listener = requireNotNull(client.listener)
            listener.onMessage(OnlineMessage(
                type = "move", roomId = "room", senderSide = Side.WHITE.name,
                seq = 2, fromFile = 7, fromRank = 8,
            ))
            listener.onMessage(OnlineMessage(type = "resign", roomId = "room", senderSide = Side.WHITE.name))
            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                online.opponentEvents("room").collect { event ->
                    withContext(NonCancellable) {
                        assertIs<GomokuOnlineGameSync.Result.Applied>(fixture.sync.accept("game", event))
                        online.completeOpponentEvent(event)
                    }
                }
            }
            try {
                withTimeout(3_000) { entered.await() }
                collector.cancel()
                yield()
                assertFalse(collector.isCompleted)
                release.complete(Unit)
                withTimeout(3_000) { collector.join() }
            } finally {
                release.complete(Unit)
                collector.cancelAndJoin()
            }
            assertEquals(2, fixture.games.game.currentPly)
            val finished = CompletableDeferred<Unit>()
            val restored = mutableListOf<OnlineGameEvent>()
            val reopened = launch(start = CoroutineStart.UNDISPATCHED) {
                online.opponentEvents("room").collect { event ->
                    restored += event
                    assertIs<GomokuOnlineGameSync.Result.Applied>(fixture.sync.accept("game", event))
                    online.completeOpponentEvent(event)
                    finished.complete(Unit)
                }
            }
            try {
                withTimeout(3_000) { finished.await() }
                yield()
                assertEquals(listOf<OnlineGameEvent>(OnlineGameEvent.Resigned("room", Side.BLACK)), restored)
                assertEquals(2, fixture.storedMoves.plies.size)
                assertEquals(GameStatus.RESIGNED, fixture.games.game.status)
                assertEquals(Side.BLACK.name, fixture.games.game.winnerSide)
            } finally {
                reopened.cancelAndJoin()
            }
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun differentRoomSideOrLocalRecordIsRejectedWithoutChangingTheGame(): Unit = runBlocking {
        val fixture = Fixture()
        val before = fixture.games.game
        val foreign = listOf(
            OnlineGameEvent.Started("other", Side.BLACK),
            OnlineGameEvent.Move("other", Side.BLACK, BoardPoint(7, 7)),
            OnlineGameEvent.Resigned("other", Side.BLACK),
            OnlineGameEvent.Started("room", Side.WHITE),
        )
        foreign.forEach { event ->
            assertIs<GomokuOnlineGameSync.Result.Rejected>(fixture.sync.accept("game", event))
        }
        assertEquals(before, fixture.games.game)
        fixture.games.game = before.copy(mode = GameMode.LOCAL_PVP, whitePlayerType = PlayerType.HUMAN)
        val local = fixture.games.game
        listOf(
            OnlineGameEvent.Started("room", Side.BLACK),
            OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 7)),
            OnlineGameEvent.Resigned("room", Side.BLACK),
        ).forEach { event ->
            assertIs<GomokuOnlineGameSync.Result.Rejected>(fixture.sync.accept("game", event))
        }
        assertEquals(local, fixture.games.game)
        assertTrue(fixture.storedMoves.plies.isEmpty())
    }

    @Test
    fun illegalAndOutOfTurnMovesAreReportedWithoutDiscardingTheNextValidMove(): Unit = runBlocking {
        val fixture = Fixture()
        val before = fixture.games.game
        assertIs<GomokuOnlineGameSync.Result.Rejected>(fixture.sync.accept(
            "game", OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8)),
        ))
        assertEquals(before, fixture.games.game)
        fixture.humanMove(BoardPoint(7, 7))
        val afterHuman = fixture.games.game
        listOf(BoardPoint(7, 7), BoardPoint(15, 0), BoardPoint(0, -1)).forEach { point ->
            val rejected = assertIs<GomokuOnlineGameSync.Result.Rejected>(fixture.sync.accept(
                "game", OnlineGameEvent.Move("room", Side.BLACK, point),
            ))
            assertTrue(rejected.reason.isNotBlank())
            assertEquals(afterHuman, fixture.games.game)
        }
        assertIs<GomokuOnlineGameSync.Result.Applied>(fixture.sync.accept(
            "game", OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8)),
        ))
        assertEquals(2, fixture.games.game.currentPly)
    }

    @Test
    fun legacyLocallyPausedOnlineGamesPersistLiveMovesWithoutWaitingForContinue(): Unit = runBlocking {
        val fixture = Fixture()
        fixture.humanMove(BoardPoint(7, 7))
        fixture.manage.pause("game")
        assertEquals(GameStatus.PAUSED, fixture.games.game.status)
        val applied = assertIs<GomokuOnlineGameSync.Result.Applied>(fixture.sync.accept(
            "game", OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8), 2),
        ))
        assertEquals(GameStatus.PLAYING, applied.detail.status)
        assertEquals(2, applied.detail.currentPly)
        assertEquals(2, fixture.storedMoves.plies.size)
        assertEquals("", fixture.games.game.resultText)
    }

    @Test
    fun liveResignationPersistsWhileLocallyPausedAndTerminalEventsAreIdempotent(): Unit = runBlocking {
        val fixture = Fixture()
        fixture.manage.pause("game")
        assertIs<GomokuOnlineGameSync.Result.Applied>(fixture.sync.accept(
            "game", OnlineGameEvent.Resigned("room", Side.BLACK),
        ))
        assertEquals(GameStatus.RESIGNED, fixture.games.game.status)
        assertEquals(Side.BLACK.name, fixture.games.game.winnerSide)
        val terminal = fixture.games.game
        assertIs<GomokuOnlineGameSync.Result.Applied>(fixture.sync.accept(
            "game", OnlineGameEvent.Started("room", Side.BLACK, FenCodec.INITIAL_FEN),
        ))
        assertIs<GomokuOnlineGameSync.Result.Applied>(fixture.sync.accept(
            "game", OnlineGameEvent.Resigned("room", Side.BLACK),
        ))
        assertIs<GomokuOnlineGameSync.Result.Rejected>(fixture.sync.accept(
            "game", OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 7)),
        ))
        assertEquals(terminal, fixture.games.game)
        assertTrue(fixture.storedMoves.plies.isEmpty())
    }

    @Test
    fun receivedStartCannotReplaceTheSavedInitialFenOrStartAnInvalidPosition(): Unit = runBlocking {
        val fixture = Fixture(status = GameStatus.NOT_STARTED)
        val before = fixture.games.game
        listOf("invalid", FenCodec.INITIAL_FEN.replace(" b 1", " w 1")).forEach { fen ->
            assertIs<GomokuOnlineGameSync.Result.Rejected>(fixture.sync.accept(
                "game", OnlineGameEvent.Started("room", Side.BLACK, fen),
            ))
            assertEquals(before, fixture.games.game)
        }
        assertTrue(fixture.storedMoves.plies.isEmpty())
    }

    @Test
    fun deletedGamesAndMissingSavedRoomIdentityNeverFallBackToTheCurrentSession(): Unit = runBlocking {
        val fixture = Fixture()
        val start = OnlineGameEvent.Started("room", Side.BLACK)
        fixture.games.archive("game")
        assertIs<GomokuOnlineGameSync.Result.Rejected>(fixture.sync.accept("game", start))
        val invalid = Fixture()
        invalid.games.game = invalid.games.game.copy(redPlayerConfigJson = "{}")
        val before = invalid.games.game
        assertIs<GomokuOnlineGameSync.Result.Rejected>(invalid.sync.accept("game", start))
        assertEquals(before, invalid.games.game)
        invalid.games.game = before.copy(redPlayerConfigJson = """{"roomId":"room"}""")
        assertIs<GomokuOnlineGameSync.Result.Rejected>(invalid.sync.accept("game", start))
        assertEquals(0, invalid.games.game.currentPly)
    }

    @Test
    fun aWhiteHumanReceivesBlackMovesUsingSavedSideRatherThanLegacyRedColumnNames(): Unit = runBlocking {
        val fixture = Fixture(side = Side.WHITE)
        val applied = assertIs<GomokuOnlineGameSync.Result.Applied>(fixture.sync.accept(
            "game", OnlineGameEvent.Move("room", Side.WHITE, BoardPoint(7, 7), 1),
        ))
        assertEquals(PlayerType.HUMAN, applied.detail.whitePlayerType)
        assertEquals(PlayerType.REMOTE, applied.detail.blackPlayerType)
        assertEquals(Side.BLACK, fixture.storedMoves.plies.single().moverSide)
        assertEquals(Side.WHITE, FenCodec.parse(applied.detail.currentFen).sideToMove)
        assertEquals(1, applied.detail.currentPly)
    }

    private class Fixture(
        side: Side = Side.BLACK,
        fen: String = FenCodec.INITIAL_FEN,
        status: GameStatus = GameStatus.PLAYING,
    ) {
        private val identity = """{"roomId":"room","mySide":"${side.name}"}"""
        val games = MemoryGames(testGame(fen = fen, status = status).copy(
            mode = GameMode.ONLINE_PVP,
            blackPlayerType = if (side == Side.BLACK) PlayerType.HUMAN else PlayerType.REMOTE,
            whitePlayerType = if (side == Side.WHITE) PlayerType.HUMAN else PlayerType.REMOTE,
            redPlayerConfigJson = identity,
            blackPlayerConfigJson = identity,
            startedAt = if (status == GameStatus.NOT_STARTED) 0 else 1,
            lastMoveAt = if (status == GameStatus.NOT_STARTED) 0 else 1,
        ))
        val storedMoves = MemoryMoves()
        var beforeInsert: suspend (PlyEntity) -> Unit = {}
        private val moves = object : MoveStore by storedMoves {
            override suspend fun insert(entity: PlyEntity) {
                beforeInsert(entity)
                storedMoves.insert(entity)
            }
        }
        private val query = GameQueryUseCase(games, moves)
        private val play = PlayMoveUseCase(games, moves, query)
        val manage = ManageGameUseCase(games, moves, query)
        val sync = GomokuOnlineGameSync(query, play, manage)

        suspend fun humanMove(point: BoardPoint) {
            sync.mutate {
                val move = GameArbiter.legalMoves(FenCodec.parse(games.game.currentFen)).first { it.to == point }
                assertIs<PlayMoveUseCase.Result.Success>(play.commit("game", move))
            }
        }
    }

    private class FakeSignalingClient : SignalingClient {
        var listener: SignalingClient.Listener? = null
        val sent = mutableListOf<OnlineMessage>()
        override fun connect(roomId: String, listener: SignalingClient.Listener) { this.listener = listener }
        override fun disconnect() = Unit
        override fun sendMessage(message: OnlineMessage) { sent += message }
        override suspend fun createRoom(
            gameType: String, hostName: String, hostAvatarUrl: String, config: OnlineRoomConfig,
        ): Result<RoomInfo> = Result.failure(UnsupportedOperationException())
        override suspend fun listOpenRooms(gameType: String): Result<List<RoomInfo>> =
            Result.failure(UnsupportedOperationException())
        override suspend fun joinRoom(roomId: String, guestName: String, guestAvatarUrl: String): Result<RoomInfo> =
            Result.failure(UnsupportedOperationException())
        override suspend fun leaveRoom(roomId: String): Result<Unit> = Result.success(Unit)
    }
}
