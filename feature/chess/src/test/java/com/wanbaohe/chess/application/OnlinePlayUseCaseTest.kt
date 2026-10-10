package com.wanbaohe.chess.application

import com.wanbaohe.chess.application.port.outbound.RoomInfo
import com.wanbaohe.chess.application.port.outbound.SignalingClient
import com.wanbaohe.chess.application.usecase.OnlineGameEvent
import com.wanbaohe.chess.application.usecase.OnlinePlayUseCase
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.ChessMove
import com.wanbaohe.chess.domain.model.ConnectionState
import com.wanbaohe.chess.domain.model.OnlineMessage
import com.wanbaohe.chess.domain.model.OnlineRoomConfig
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnlinePlayUseCaseTest {
    @Test
    fun disconnectResetsStableRoomConnectionAndDebugFlows(): Unit = runBlocking {
        val online = OnlinePlayUseCase(FakeSignalingClient())
        online.connect("room", Side.WHITE, isHost = true)
        try {
            online.sendStart("room", Side.WHITE)
            withTimeout(3_000) {
                online.connectionState.first { it == ConnectionState.PLAYING }
                online.debugEvents.first { it.isNotEmpty() }
            }
        } finally {
            online.disconnect()
        }
        withTimeout(3_000) {
            online.connectionState.first { it == ConnectionState.IDLE }
            online.debugEvents.first { it.isEmpty() }
        }
        assertEquals("", online.currentRoomId)
    }

    @Test
    fun retainedGameActionsCannotSendIntoAnotherRoomOrSide() {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        val move = ChessMove(BoardPoint(4, 6), BoardPoint(4, 4), Piece(Side.BLACK, PieceType.PAWN))
        try {
            online.connect("old-room", Side.WHITE, isHost = true)
            online.connect("new-room", Side.BLACK, isHost = false)
            assertFalse(online.ownsSession("old-room", Side.WHITE))
            assertFalse(online.sendMove(move, "old-room", Side.WHITE))
            assertFalse(online.sendStart("old-room", Side.WHITE))
            assertFalse(online.sendResign("old-room", Side.WHITE))
            assertFalse(online.sendStart("new-room", Side.WHITE))
            assertFalse(online.sendStart("", Side.BLACK))
            assertTrue(client.sent.isEmpty())
            assertTrue(online.ownsSession("new-room", Side.BLACK))
            assertTrue(online.sendStart("new-room", Side.BLACK))
            assertTrue(online.sendMove(move, "new-room", Side.BLACK))
            assertTrue(online.sendResign("new-room", Side.BLACK))
            assertEquals(listOf("start", "move", "resign"), client.sent.map { it.type })
            assertTrue(client.sent.all { it.roomId == "new-room" && it.senderSide == Side.BLACK.name })
        } finally {
            online.disconnect()
        }
        assertFalse(online.ownsSession("new-room", Side.BLACK))
        assertFalse(online.sendResign("new-room", Side.BLACK))
    }

    @Test
    fun rejectedSyncEventsAreVisibleInTheRoomDebugLog(): Unit = runBlocking {
        val online = OnlinePlayUseCase(FakeSignalingClient())
        try {
            online.connect("room", Side.WHITE, isHost = true)
            online.reportSyncFailure(OnlineGameEvent.Move("room", BoardPoint(4, 6), BoardPoint(4, 3)))
            val events = withTimeout(3_000) { online.debugEvents.first { log -> log.any { it.contains("SYNC rejected") } } }
            assertTrue(events.last().contains("room=room"))
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun ackedEventsWaitForTheGameSubscriberAndKeepStartMoveResignOrder() = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("room", Side.WHITE, isHost = true)
            val listener = client.listeners.single()
            listener.onMessage(OnlineMessage(type = "start", roomId = "room"))
            val move = OnlineMessage(
                type = "move", roomId = "room", senderSide = Side.BLACK.name, seq = 2,
                fromFile = 4, fromRank = 6, toFile = 4, toRank = 4,
            )
            listener.onMessage(move)
            listener.onMessage(move)
            listener.onMessage(OnlineMessage(type = "resign", roomId = "room", senderSide = Side.BLACK.name))
            assertEquals(2, client.sent.count { it.type == "ack" })

            val events = withTimeout(3_000) { online.opponentEvents("room").take(3).toList() }
            assertEquals(listOf(
                OnlineGameEvent.Started("room"),
                OnlineGameEvent.Move("room", BoardPoint(4, 6), BoardPoint(4, 4)),
                OnlineGameEvent.Resigned("room"),
            ), events)
        } finally {
            online.disconnect()
        }
    }

    @Test
    fun retainedAnotherRoomCannotConsumeOrBlockNewRoomEventsAndOldListenersAreIgnored() = runBlocking {
        val client = FakeSignalingClient()
        val online = OnlinePlayUseCase(client)
        try {
            online.connect("old-room", Side.WHITE, isHost = true)
            val old = client.listeners.last()
            old.onMessage(OnlineMessage(type = "start", roomId = "old-room"))
            online.connect("new-room", Side.WHITE, isHost = true)
            old.onMessage(OnlineMessage(type = "resign", roomId = "old-room", senderSide = Side.BLACK.name))
            val current = client.listeners.last()
            current.onMessage(OnlineMessage(type = "start", roomId = "new-room"))
            current.onMessage(OnlineMessage(type = "resign", roomId = "old-room", senderSide = Side.BLACK.name))
            current.onMessage(OnlineMessage(type = "resign", roomId = "new-room", senderSide = Side.WHITE.name))
            current.onMessage(OnlineMessage(
                type = "move", roomId = "new-room", senderSide = Side.BLACK.name, seq = 2,
                fromFile = 4, fromRank = 6, toFile = 4, toRank = 4,
            ))

            val events = withTimeout(3_000) { online.opponentEvents("new-room").take(2).toList() }
            assertEquals(listOf(
                OnlineGameEvent.Started("new-room"),
                OnlineGameEvent.Move("new-room", BoardPoint(4, 6), BoardPoint(4, 4)),
            ), events)
            assertEquals(
                OnlineGameEvent.Started("old-room"),
                withTimeout(3_000) { online.opponentEvents("old-room").take(1).toList() }.single(),
            )
            assertEquals(1, client.sent.count { it.type == "ack" })
        } finally {
            online.disconnect()
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
