package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.application.port.outbound.RoomInfo
import com.wanbaohe.xiangqi.application.port.outbound.SignalingClient
import com.wanbaohe.xiangqi.application.usecase.OnlinePlayUseCase
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.OnlineMessage
import com.wanbaohe.xiangqi.domain.model.OnlineRoomConfig
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnlinePlayUseCaseTest {

    @Test
    fun onlineMessageRoundTripPreservesFenAndRoomId() {
        val message = OnlineMessage(
            type = "ready",
            gameType = "xiangqi",
            roomId = "room-1",
            fen = FenCodec.INITIAL_FEN,
            createdAt = 1L,
        )

        val decoded = OnlineMessage.fromJson(OnlineMessage.toJson(message))

        assertEquals("room-1", decoded.roomId)
        assertEquals(FenCodec.INITIAL_FEN, decoded.fen)
        assertEquals(1L, decoded.createdAt)
    }

    @Test
    fun onlineMessageParsesNestedRelayPayload() {
        val raw = """
            {"type":"message","data":{"type":"move","roomId":"room-1","seq":3,"fromFile":1,"fromRank":2,"toFile":1,"toRank":3}}
        """.trimIndent()

        val decoded = OnlineMessage.fromJson(raw)

        assertEquals("move", decoded.type)
        assertEquals("room-1", decoded.roomId)
        assertEquals(3, decoded.seq)
        assertEquals(1, decoded.fromFile)
        assertEquals(3, decoded.toRank)
    }

    @Test
    fun roomConfigNormalizesFen() {
        val config = OnlineRoomConfig.fromFen(FenCodec.INITIAL_FEN)

        assertEquals(FenCodec.INITIAL_FEN, config.initialFen)
        assertEquals(Side.RED, config.hostSide)
        assertEquals(Side.BLACK, config.guestSide)
    }

    @Test
    fun opponentMoveIsAckedAndDuplicateSeqIsIgnored() = runBlocking {
        val client = FakeSignalingClient()
        val useCase = OnlinePlayUseCase(client)
        useCase.connect("room-1", Side.RED, isHost = true)

        val move = CompletableDeferred<Pair<BoardPoint, BoardPoint>>()
        val job = launch {
            useCase.opponentMoves.collect { move.complete(it) }
        }
        yield()

        client.receiveMove(seq = 7)
        client.receiveMove(seq = 7)

        assertEquals(BoardPoint(0, 0) to BoardPoint(0, 1), withTimeout(1_000L) { move.await() })
        assertEquals(2, client.sent.count { it.type == "ack" && it.seq == 7 })
        job.cancelAndJoin()
    }

    @Test
    fun uppercaseMoveTypeIsAccepted() = runBlocking {
        val client = FakeSignalingClient()
        val useCase = OnlinePlayUseCase(client)
        useCase.connect("room-1", Side.RED, isHost = true)

        val move = CompletableDeferred<Pair<BoardPoint, BoardPoint>>()
        val job = launch { useCase.opponentMoves.collect { move.complete(it) } }
        yield()

        client.receiveMove(seq = 8, type = "MOVE")

        assertEquals(BoardPoint(0, 0) to BoardPoint(0, 1), withTimeout(1_000L) { move.await() })
        job.cancelAndJoin()
    }

    @Test
    fun redAndBlackOutgoingMoveSeqDoNotCollide() {
        val redClient = FakeSignalingClient()
        val blackClient = FakeSignalingClient()
        val red = OnlinePlayUseCase(redClient)
        val black = OnlinePlayUseCase(blackClient)

        red.connect("room-1", Side.RED, isHost = true)
        black.connect("room-1", Side.BLACK, isHost = false)
        red.sendMove(sampleMove(Side.RED))
        black.sendMove(sampleMove(Side.BLACK))

        val redMove = redClient.sent.first { it.type == "move" }
        val blackMove = blackClient.sent.first { it.type == "move" }
        assertEquals(1, redMove.seq)
        assertEquals(2, blackMove.seq)
        assertEquals(Side.RED.name, redMove.senderSide)
        assertEquals(Side.BLACK.name, blackMove.senderSide)
    }

    @Test
    fun createRoomPassesFenConfigToClient() = runBlocking {
        val client = FakeSignalingClient()
        val useCase = OnlinePlayUseCase(client)

        val result = useCase.createRoom("host", FenCodec.INITIAL_FEN)

        assertTrue(result.isSuccess)
        assertEquals(FenCodec.INITIAL_FEN, client.createdConfig.initialFen)
    }

    private fun sampleMove(side: Side) = XiangqiMove(
        from = BoardPoint(0, 0),
        to = BoardPoint(0, 1),
        piece = Piece(side, PieceType.ROOK),
    )

    private class FakeSignalingClient : SignalingClient {
        var createdConfig: OnlineRoomConfig = OnlineRoomConfig()
        val sent = mutableListOf<OnlineMessage>()
        private var listener: SignalingClient.Listener? = null

        override suspend fun createRoom(
            gameType: String,
            hostName: String,
            hostAvatarUrl: String,
            config: OnlineRoomConfig,
        ): Result<RoomInfo> {
            createdConfig = config
            return Result.success(room(config = config, hostName = hostName, hostAvatarUrl = hostAvatarUrl))
        }

        override suspend fun listOpenRooms(gameType: String): Result<List<RoomInfo>> =
            Result.success(listOf(room()))

        override suspend fun joinRoom(roomId: String, guestName: String, guestAvatarUrl: String): Result<RoomInfo> =
            Result.success(room(id = roomId, guestName = guestName, guestAvatarUrl = guestAvatarUrl))

        override suspend fun leaveRoom(roomId: String): Result<Unit> = Result.success(Unit)

        override fun connect(roomId: String, listener: SignalingClient.Listener) {
            this.listener = listener
            listener.onConnected()
        }

        override fun disconnect() {
            listener = null
        }

        override fun sendMessage(message: OnlineMessage) {
            sent += message
        }

        fun receiveMove(seq: Int, type: String = "move") {
            listener?.onMessage(
                OnlineMessage(
                    type = type,
                    roomId = "room-1",
                    seq = seq,
                    fromFile = 0,
                    fromRank = 0,
                    toFile = 0,
                    toRank = 1,
                )
            )
        }


        private fun room(
            id: String = "room-1",
            hostName: String = "host",
            hostAvatarUrl: String = "",
            guestName: String = "guest",
            guestAvatarUrl: String = "",
            config: OnlineRoomConfig = OnlineRoomConfig(),
        ) = RoomInfo(
            id = id,
            gameType = "xiangqi",
            hostId = 1,
            hostName = hostName,
            hostAvatarUrl = hostAvatarUrl,
            guestId = 2,
            guestName = guestName,
            guestAvatarUrl = guestAvatarUrl,
            status = "WAITING",
            createdAt = 1L,
            config = config,
        )
    }
}


