package com.wanbaohe.gomoku.application

import com.wanbaohe.gomoku.application.port.outbound.RoomInfo
import com.wanbaohe.gomoku.application.port.outbound.SignalingClient
import com.wanbaohe.gomoku.application.usecase.OnlineGameEvent
import com.wanbaohe.gomoku.application.usecase.OnlinePlayUseCase
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.ConnectionState
import com.wanbaohe.gomoku.domain.model.OnlineMessage
import com.wanbaohe.gomoku.domain.model.OnlineRoomConfig
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OnlinePlayUseCaseTest {
    @Test
    fun acknowledgedEventsWaitForTheGameSubscriberAndKeepStartMoveResignOrder(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            val listener = client.listeners.single()
            listener.onMessage(start())
            val move = move()
            listener.onMessage(move)
            listener.onMessage(move)
            listener.onMessage(resign())
            assertEquals(2, client.sent.count { it.type == "ack" })

            assertEquals(
                listOf(
                    OnlineGameEvent.Started("room", Side.BLACK, FenCodec.INITIAL_FEN),
                    OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8), 2),
                    OnlineGameEvent.Resigned("room", Side.BLACK),
                ),
                readEvents(online, "room", 3),
            )
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun hiddenUpdatingHandlerKeepsAllLaterAcknowledgedMovesAndResignation(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            val finished = CompletableDeferred<Unit>()
            val events = mutableListOf<OnlineGameEvent>()
            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                online.opponentEvents("room").collect { event ->
                    if (event is OnlineGameEvent.Started) {
                        entered.complete(Unit)
                        release.await()
                    }
                    events += event
                    if (event is OnlineGameEvent.Resigned) finished.complete(Unit)
                }
            }
            try {
                val listener = client.listeners.single()
                listener.onMessage(start())
                withTimeout(3_000) { entered.await() }
                repeat(48) { index ->
                    listener.onMessage(move().copy(
                        seq = (index + 1) * 2,
                        fromFile = index % 15,
                        fromRank = index / 15,
                    ))
                }
                listener.onMessage(resign())
                assertEquals(48, client.sent.count { it.type == "ack" })
                assertTrue(events.isEmpty())
                release.complete(Unit)
                withTimeout(3_000) { finished.await() }
                yield()
                assertEquals(50, events.size)
                assertTrue(events.first() is OnlineGameEvent.Started)
                assertEquals(
                    (1..48).map { it * 2 },
                    events.filterIsInstance<OnlineGameEvent.Move>().map { it.seq },
                )
                assertTrue(events.last() is OnlineGameEvent.Resigned)
            } finally {
                collector.cancelAndJoin()
            }
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun differentRoomsKeepSourceTagsAndOldListenersCannotAffectTheNewSession(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("old-room", Side.BLACK, isHost = true)
            val old = client.listeners.last()
            old.onMessage(start().copy(roomId = "old-room"))
            old.onMessage(move().copy(roomId = "old-room"))
            online.connect("new-room", Side.WHITE, isHost = false)
            val current = client.listeners.last()
            current.onConnected()
            withTimeout(3_000) {
                online.connectionState.first { it == ConnectionState.WAITING_FOR_OPPONENT }
            }
            old.onConnected()
            old.onDisconnected()
            old.onError("old-session error")
            old.onRawMessage("old-session raw message")
            old.onMessage(resign().copy(roomId = "old-room"))
            current.onMessage(resign().copy(roomId = "old-room"))
            current.onMessage(start().copy(roomId = "new-room", senderSide = Side.BLACK.name))
            current.onMessage(move().copy(roomId = "new-room", senderSide = Side.BLACK.name, seq = 1))

            assertEquals(
                listOf(
                    OnlineGameEvent.Started("new-room", Side.WHITE, FenCodec.INITIAL_FEN),
                    OnlineGameEvent.Move("new-room", Side.WHITE, BoardPoint(7, 8), 1),
                ),
                readEvents(online, "new-room", 2),
            )
            assertEquals(
                listOf(
                    OnlineGameEvent.Started("old-room", Side.BLACK, FenCodec.INITIAL_FEN),
                    OnlineGameEvent.Move("old-room", Side.BLACK, BoardPoint(7, 8), 2),
                ),
                readEvents(online, "old-room", 2),
            )
            assertEquals("new-room", online.currentRoomId)
            assertEquals(Side.WHITE, online.mySide)
            assertEquals(2, client.sent.count { it.type == "ack" })
            assertTrue(online.debugEvents.value.none { it.contains("old-session") })
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun callbacksAfterDisconnectCannotReviveOrEnqueueAnOldSession(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        online.connect("room", Side.BLACK, isHost = true)
        val old = client.listeners.single()
        online.disconnect()
        old.onConnected()
        old.onError("late error")
        old.onMessage(start())
        old.onMessage(move())
        old.onMessage(resign())
        withTimeout(3_000) { online.connectionState.first { it == ConnectionState.IDLE } }
        assertEquals("", online.currentRoomId)
        assertTrue(client.sent.isEmpty())
        assertNull(withTimeoutOrNull(50) { online.opponentEvents("room").first() })
    }

    @Test
    fun selfEchoesAndForeignRoomOrGameMessagesDoNotEnterTheOpponentQueue(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            val listener = client.listeners.single()
            listener.onMessage(start().copy(senderSide = Side.BLACK.name))
            listener.onMessage(move().copy(senderSide = Side.BLACK.name))
            listener.onMessage(resign().copy(senderSide = Side.BLACK.name))
            listener.onMessage(move().copy(roomId = "other"))
            listener.onMessage(move().copy(gameType = "chess"))
            listener.onMessage(start())
            assertEquals(
                listOf(OnlineGameEvent.Started("room", Side.BLACK, FenCodec.INITIAL_FEN)),
                readEvents(online, "room", 1),
            )
            assertTrue(client.sent.none { it.type == "ack" })
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun legacyBlankRoomAndSenderFieldsUseTheCapturedSessionIdentity(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.WHITE, isHost = false)
            val listener = client.listeners.single()
            listener.onMessage(OnlineMessage(type = "start"))
            listener.onMessage(OnlineMessage(type = "move", seq = 1, fromFile = 4, fromRank = 5))
            listener.onMessage(OnlineMessage(type = "resign"))
            assertEquals(
                listOf(
                    OnlineGameEvent.Started("room", Side.WHITE),
                    OnlineGameEvent.Move("room", Side.WHITE, BoardPoint(4, 5), 1),
                    OnlineGameEvent.Resigned("room", Side.WHITE),
                ),
                readEvents(online, "room", 3),
            )
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun cancellationDuringDeliveryRetainsTheHeadAndFollowingEventsForReopening(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            val listener = client.listeners.single()
            listener.onMessage(start())
            listener.onMessage(move())
            listener.onMessage(resign())
            val entered = CompletableDeferred<Unit>()
            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                online.opponentEvents("room").collect {
                    entered.complete(Unit)
                    awaitCancellation()
                }
            }
            withTimeout(3_000) { entered.await() }
            collector.cancelAndJoin()
            assertEquals(
                listOf(
                    OnlineGameEvent.Started("room", Side.BLACK, FenCodec.INITIAL_FEN),
                    OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8), 2),
                    OnlineGameEvent.Resigned("room", Side.BLACK),
                ),
                readEvents(online, "room", 3),
            )
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun failedPersistenceDoesNotRetireAnAcknowledgedMove(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            client.listeners.single().onMessage(move())
            assertFailsWith<IllegalStateException> {
                withTimeout(3_000) {
                    online.opponentEvents("room").collect { throw IllegalStateException("write failed") }
                }
            }
            assertEquals(
                listOf(OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8), 2)),
                readEvents(online, "room", 1),
            )
            assertEquals(1, client.sent.count { it.type == "ack" })
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun successfullyHandledEventsAreRetiredRatherThanReplayedOnReturn(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            val listener = client.listeners.single()
            listener.onMessage(start())
            listener.onMessage(move())
            listener.onMessage(resign())
            assertEquals(3, readEvents(online, "room", 3).size)
            assertNull(withTimeoutOrNull(50) { online.opponentEvents("room").first() })
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun gomokuKeepsFromCoordinateSlotsAndTheExistingMirroredPairFlow(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            val localMove = GameArbiter.legalMoves(FenCodec.parse(FenCodec.INITIAL_FEN))
                .first { it.to == BoardPoint(14, 14) }
            online.sendMove(localMove)
            val sent = client.sent.single { it.type == "move" }
            assertEquals(14, sent.fromFile)
            assertEquals(14, sent.fromRank)
            assertEquals(0, sent.toFile)
            assertEquals(0, sent.toRank)
            assertEquals(1, sent.seq)
            val legacy = async(start = CoroutineStart.UNDISPATCHED) { online.opponentMoves.first() }
            client.listeners.single().onMessage(move().copy(toFile = 1, toRank = 2))
            assertEquals(
                BoardPoint(7, 8) to BoardPoint(7, 8),
                withTimeout(3_000) { legacy.await() },
            )
            assertEquals(
                listOf(OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8), 2)),
                readEvents(online, "room", 1),
            )
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun invalidSenderIsSurfacedWithoutAcknowledgingOrMisclassifyingItsMove(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            val listener = client.listeners.single()
            listener.onMessage(move().copy(senderSide = "invalid-side"))
            withTimeout(3_000) { online.connectionState.first { it == ConnectionState.ERROR } }
            withTimeout(3_000) {
                online.debugEvents.first { events -> events.any { it.contains("Invalid sender side") } }
            }
            assertTrue(client.sent.none { it.type == "ack" })
            listener.onMessage(start())
            assertEquals(
                listOf(OnlineGameEvent.Started("room", Side.BLACK, FenCodec.INITIAL_FEN)),
                readEvents(online, "room", 1),
            )
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun moveRetriesDedupeAcrossSenderCaseAndLegacyBlankSenderFields(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.BLACK, isHost = true)
            val listener = client.listeners.single()
            listener.onMessage(move().copy(senderSide = "white"))
            listener.onMessage(move().copy(senderSide = ""))
            listener.onMessage(move())
            assertEquals(3, client.sent.count { it.type == "ack" })
            assertEquals(
                listOf(OnlineGameEvent.Move("room", Side.BLACK, BoardPoint(7, 8), 2)),
                readEvents(online, "room", 1),
            )
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun savedRoomAndSideAreCheckedAtomicallyBeforeSendingLocalActions(): Unit = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            val localMove = GameArbiter.legalMoves(FenCodec.parse(FenCodec.INITIAL_FEN)).first()
            online.connect("old-room", Side.BLACK, isHost = true)
            online.connect("new-room", Side.WHITE, isHost = false)
            assertEquals(false, online.sendMove(localMove, "old-room", Side.BLACK))
            assertEquals(false, online.sendStart("old-room", Side.BLACK))
            assertEquals(false, online.sendResign("old-room", Side.BLACK))
            assertEquals(false, online.sendStart("new-room", Side.BLACK))
            assertTrue(client.sent.isEmpty())
            assertEquals(true, online.sendStart("new-room", Side.WHITE))
            assertEquals("new-room", client.sent.single().roomId)
            assertEquals(Side.WHITE.name, client.sent.single().senderSide)
        } finally {
            online.disconnect()
        }
    }

    private fun start() = OnlineMessage(
        type = "start", roomId = "room", senderSide = Side.WHITE.name, fen = FenCodec.INITIAL_FEN,
    )

    private fun move() = OnlineMessage(
        type = "move", roomId = "room", senderSide = Side.WHITE.name, seq = 2,
        fromFile = 7, fromRank = 8,
    )

    private fun resign() = OnlineMessage(
        type = "resign", roomId = "room", senderSide = Side.WHITE.name,
    )

    private suspend fun readEvents(
        online: OnlinePlayUseCase,
        roomId: String,
        count: Int,
    ): List<OnlineGameEvent> = withTimeout(3_000) {
        coroutineScope {
            val received = mutableListOf<OnlineGameEvent>()
            val finished = CompletableDeferred<Unit>()
            val collector = launch(start = CoroutineStart.UNDISPATCHED) {
                online.opponentEvents(roomId).collect {
                    received += it
                    if (received.size == count) finished.complete(Unit)
                }
            }
            try {
                finished.await()
                yield()
                received.toList()
            } finally {
                collector.cancelAndJoin()
            }
        }
    }

    private class FakeSignalingClient : SignalingClient {
        val listeners = mutableListOf<SignalingClient.Listener>()
        val sent = mutableListOf<OnlineMessage>()
        override fun connect(roomId: String, listener: SignalingClient.Listener) { listeners += listener }
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
