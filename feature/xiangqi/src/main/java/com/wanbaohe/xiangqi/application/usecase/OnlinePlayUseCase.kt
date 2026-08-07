package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.port.outbound.RoomInfo
import com.wanbaohe.xiangqi.application.port.outbound.SignalingClient
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.ConnectionState
import com.wanbaohe.xiangqi.domain.model.OnlineMessage
import com.wanbaohe.xiangqi.domain.model.OnlineRoomConfig
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates online match-making and in-game peer communication.
 * Game-agnostic; caller provides gameType (e.g. "xiangqi", "gomoku").
 *
 * Session isolation: all mutable per-game state lives inside [OnlineSession].
 * Each [connect] call creates a fresh session atomically, so rapid
 * re-entries or parallel rooms never share stale state.
 *
 * Reliability: every [sendMove] carries a side-scoped monotonic sequence number.
 * The peer must echo an ack message with the same seq.
 * Un-acked messages are retransmitted up to [maxAckRetries] times
 * with [ackTimeoutMs] between attempts.
 */
@Singleton
class OnlinePlayUseCase @Inject constructor(
    private val signalingClient: SignalingClient,
) {

    /** Singleton 生命周期内的托管 scope，App 退出时自动取消。 */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Default game type; override for other games. */
    var gameType: String = "xiangqi"

    /* ───── Session holder ───── */

    @Volatile
    private var session: OnlineSession? = null

    private val sessionFlow: MutableStateFlow<OnlineSession?> =
        MutableStateFlow(null)

    /* ───── Bridged outer flows (delegate to current session) ───── */

    val connectionState: StateFlow<ConnectionState> =
        sessionFlow.flatMapLatest { it?.connectionState ?: emptyFlow() }
            .let { flow ->
                val state = MutableStateFlow(ConnectionState.IDLE)
                // Bridge: collect from flatMapLatest into a stable StateFlow
                scope.launch {
                    flow.collect { state.value = it }
                }
                state.asStateFlow()
            }

    val debugEvents: StateFlow<List<String>> =
        sessionFlow.flatMapLatest { it?.debugEvents ?: emptyFlow() }
            .let { flow ->
                val state = MutableStateFlow<List<String>>(emptyList())
                scope.launch {
                    flow.collect { state.value = it }
                }
                state.asStateFlow()
            }

    private val _opponentMoves = MutableSharedFlow<Pair<BoardPoint, BoardPoint>>(extraBufferCapacity = 16)
    val opponentMoves: SharedFlow<Pair<BoardPoint, BoardPoint>> = _opponentMoves.asSharedFlow()

    private val _opponentStarted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val opponentStarted: SharedFlow<Unit> = _opponentStarted.asSharedFlow()

    private val _opponentResigned = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val opponentResigned: SharedFlow<Unit> = _opponentResigned.asSharedFlow()

    /* ───── Matchmaking (stateless REST) ───── */

    suspend fun createRoom(
        hostName: String,
        initialFen: String = FenCodec.INITIAL_FEN,
        hostAvatarUrl: String = "",
    ): Result<RoomInfo> {
        appendDebug("REST createRoom host=$hostName")
        val config = runCatching { OnlineRoomConfig.fromFen(initialFen) }
            .getOrElse { return Result.failure(it) }
        return signalingClient.createRoom(gameType, hostName, hostAvatarUrl, config)
            .also { appendDebug("REST createRoom ${it.fold({ room -> "ok room=${room.id.takeLast(6)}" }, { e -> "failed ${e.message}" })}") }
    }

    suspend fun listRooms(): Result<List<RoomInfo>> =
        signalingClient.listOpenRooms(gameType)
            .also { appendDebug("REST listRooms ${it.fold({ rooms -> "ok count=${rooms.size}" }, { e -> "failed ${e.message}" })}") }

    suspend fun joinRoom(roomId: String, guestName: String, guestAvatarUrl: String = ""): Result<RoomInfo> =
        signalingClient.joinRoom(roomId, guestName, guestAvatarUrl)
            .also { appendDebug("REST joinRoom room=${roomId.takeLast(6)} ${it.fold({ "ok" }, { e -> "failed ${e.message}" })}") }

    fun useRoom(room: RoomInfo, side: Side, isHost: Boolean) {
        connect(room.id, side, isHost, room.config)
    }

    fun connect(roomId: String, side: Side, isHost: Boolean) {
        connect(roomId, side, isHost, OnlineRoomConfig())
    }

    fun connect(roomId: String, side: Side, isHost: Boolean, config: OnlineRoomConfig) {
        val newSession = OnlineSession(roomId, side, config)
        session = newSession
        sessionFlow.value = newSession

        appendDebug("WS connect room=${roomId.takeLast(6)} side=${side.name} host=$isHost")

        signalingClient.connect(roomId, object : SignalingClient.Listener {
            override fun onRawMessage(raw: String) {
                appendDebug("WS <- raw ${raw.take(DebugMessageLimit)}")
            }

            override fun onMessage(message: OnlineMessage) {
                val s = session ?: return
                appendDebug("WS <- ${message.type} seq=${message.seq} ${message.coordText()}")
                when (message.type.lowercase()) {
                    "ack" -> s.handleAck(message)
                    "move" -> {
                        s.sendAck(message)
                        s.handleOpponentMove(message)
                    }
                    "ready" -> s.handleReady(isHost)
                    "start" -> s.handleStart()
                    "resign" -> _opponentResigned.tryEmit(Unit)
                    "disconnect" ->
                        s._connectionState.value = ConnectionState.OPPONENT_DISCONNECTED
                    "error" ->
                        s._connectionState.value = ConnectionState.ERROR
                }
            }

            override fun onConnected() {
                val s = session ?: return
                s._connectionState.value = ConnectionState.WAITING_FOR_OPPONENT
                appendDebug("WS connected")
                if (!isHost) {
                    s.sendReady()
                }
            }

            override fun onDisconnected() {
                val s = session ?: return
                s._connectionState.value = ConnectionState.OPPONENT_DISCONNECTED
                appendDebug("WS disconnected")
            }

            override fun onError(error: String) {
                val s = session ?: return
                s._connectionState.value = ConnectionState.ERROR
                appendDebug("WS error $error")
            }
        })
    }

    fun sendMove(move: XiangqiMove) {
        val s = session ?: return
        s.sendMove(move)
    }

    fun sendReady() {
        val s = session ?: return
        s.sendReady()
    }

    fun sendStart() {
        val s = session ?: return
        s.sendStart()
    }

    fun sendResign() {
        val s = session ?: return
        s.sendResign()
    }

    fun disconnect() {
        appendDebug("WS disconnect by user")
        signalingClient.disconnect()
        val old = session
        session = null
        sessionFlow.value = null
        old?.dispose()
    }

    val mySide: Side
        get() = session?.mySide ?: Side.RED

    /* ─────────── private helpers ─────────── */

    private fun appendDebug(message: String) {
        val time = DebugTimeFormat.format(Date())
        val s = session
        if (s != null) {
            s._debugEvents.value = (s._debugEvents.value + "$time $message").takeLast(MaxDebugEvents)
        }
    }

    /* ─────────── OnlineSession ─────────── */

    /**
     * Holds all mutable per-connection state.
     * Each [connect] creates a fresh instance; old sessions are disposed.
     */
    private inner class OnlineSession(
        val roomId: String,
        val mySide: Side,
        val config: OnlineRoomConfig,
    ) {
        val _connectionState = MutableStateFlow(ConnectionState.CONNECTING)
        val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

        val _debugEvents = MutableStateFlow<List<String>>(emptyList())
        val debugEvents: StateFlow<List<String>> = _debugEvents.asStateFlow()

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var hasSentReady = false
        private var hasReceivedReady = false
        private var seqCounter = 0
        private val pendingAcks = ConcurrentHashMap<Int, PendingMessage>()
        private val handledMoveSeqs = ConcurrentHashMap.newKeySet<Int>()

        fun sendMove(move: XiangqiMove) {
            val seq = nextOutgoingSeq()
            val msg = OnlineMessage(
                type = "move",
                gameType = gameType,
                roomId = roomId,
                senderSide = mySide.name,
                seq = seq,
                fromFile = move.from.file,
                fromRank = move.from.rank,
                toFile = move.to.file,
                toRank = move.to.rank,
                createdAt = System.currentTimeMillis(),
            )
            pendingAcks[seq] = PendingMessage(msg, retries = 0)
            appendDebug("WS -> move seq=$seq ${msg.coordText()}")
            signalingClient.sendMessage(msg)
            scheduleRetry(seq)
        }

        fun sendReady() {
            if (hasSentReady) return
            hasSentReady = true
            appendDebug("WS -> ready room=${roomId.takeLast(6)}")
            signalingClient.sendMessage(
                OnlineMessage(
                    type = "ready",
                    gameType = gameType,
                    roomId = roomId,
                    senderSide = mySide.name,
                    fen = config.initialFen,
                    createdAt = System.currentTimeMillis(),
                )
            )
            if (hasReceivedReady) {
                _connectionState.value = ConnectionState.READY
            }
        }

        fun sendStart() {
            appendDebug("WS -> start room=${roomId.takeLast(6)}")
            signalingClient.sendMessage(
                OnlineMessage(
                    type = "start",
                    gameType = gameType,
                    roomId = roomId,
                    senderSide = mySide.name,
                    fen = config.initialFen,
                    createdAt = System.currentTimeMillis(),
                )
            )
            _connectionState.value = ConnectionState.PLAYING
        }

        fun sendResign() {
            appendDebug("WS -> resign room=${roomId.takeLast(6)}")
            signalingClient.sendMessage(
                OnlineMessage(
                    type = "resign",
                    gameType = gameType,
                    roomId = roomId,
                    senderSide = mySide.name,
                    createdAt = System.currentTimeMillis(),
                )
            )
        }

        fun sendAck(message: OnlineMessage) {
            if (message.seq <= 0) return
            appendDebug("WS -> ack seq=${message.seq}")
            signalingClient.sendMessage(
                OnlineMessage(type = "ack", gameType = gameType, roomId = roomId, senderSide = mySide.name, seq = message.seq)
            )
        }

        fun handleAck(message: OnlineMessage) {
            appendDebug("ACK seq=${message.seq}")
            pendingAcks.remove(message.seq)
        }

        fun handleReady(isHost: Boolean) {
            hasReceivedReady = true
            if (isHost && !hasSentReady) {
                sendReady()
            }
            if (hasSentReady) {
                _connectionState.value = ConnectionState.READY
            }
            appendDebug("READY sent=$hasSentReady received=$hasReceivedReady")
        }

        fun handleStart() {
            _connectionState.value = ConnectionState.PLAYING
            appendDebug("START received")
            _opponentStarted.tryEmit(Unit)
        }

        fun handleOpponentMove(message: OnlineMessage) {
            if (message.senderSide.equals(mySide.name, ignoreCase = true)) {
                appendDebug("MOVE self echo ignored seq=${message.seq}")
                return
            }
            val dedupeKey = message.dedupeKey()
            if (message.seq > 0 && !handledMoveSeqs.add(dedupeKey)) {
                appendDebug("MOVE duplicate ignored key=$dedupeKey")
                return
            }
            val from = BoardPoint(message.fromFile, message.fromRank)
            val to = BoardPoint(message.toFile, message.toRank)
            appendDebug("MOVE emit ${from.file},${from.rank}->${to.file},${to.rank}")
            _opponentMoves.tryEmit(Pair(from, to))
        }

        fun dispose() {
            scope.cancel()
            pendingAcks.clear()
            handledMoveSeqs.clear()
        }

        private fun scheduleRetry(seq: Int) {
            scope.launch {
                delay(ackTimeoutMs)
                val pending = pendingAcks[seq] ?: return@launch
                if (pending.retries >= maxAckRetries) {
                    pendingAcks.remove(seq)
                    _connectionState.value = ConnectionState.ERROR
                    appendDebug("ACK timeout seq=$seq")
                    return@launch
                }
                pending.retries++
                appendDebug("WS -> retry seq=$seq retry=${pending.retries}")
                signalingClient.sendMessage(pending.message)
                scheduleRetry(seq)
            }
        }

        private fun nextOutgoingSeq(): Int {
            seqCounter += 1
            return when (mySide) {
                Side.RED -> seqCounter * 2 - 1
                Side.BLACK -> seqCounter * 2
            }
        }

        private fun OnlineMessage.dedupeKey(): Int =
            if (senderSide == Side.BLACK.name) -seq else seq
    }

    private fun OnlineMessage.coordText(): String =
        if (type.equals("move", ignoreCase = true)) {
            "$fromFile,$fromRank->$toFile,$toRank side=$senderSide"
        } else {
            ""
        }

    private data class PendingMessage(
        val message: OnlineMessage,
        @Volatile var retries: Int,
    )

    companion object {
        private const val MaxDebugEvents = 80
        private const val DebugMessageLimit = 240
        private val DebugTimeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        private val ackTimeoutMs = 3000L
        private const val maxAckRetries = 3
    }
}
