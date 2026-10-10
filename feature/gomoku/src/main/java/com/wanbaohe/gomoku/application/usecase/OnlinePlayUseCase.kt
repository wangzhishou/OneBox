package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.application.port.outbound.RoomInfo
import com.wanbaohe.gomoku.application.port.outbound.SignalingClient
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.ConnectionState
import com.wanbaohe.gomoku.domain.model.OnlineMessage
import com.wanbaohe.gomoku.domain.model.OnlineRoomConfig
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.domain.model.GomokuMove
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed interface OnlineGameEvent {
    val roomId: String
    val mySide: Side

    data class Started(
        override val roomId: String,
        override val mySide: Side,
        val initialFen: String = "",
    ) : OnlineGameEvent

    data class Move(
        override val roomId: String,
        override val mySide: Side,
        val point: BoardPoint,
        val seq: Int = 0,
    ) : OnlineGameEvent

    data class Resigned(
        override val roomId: String,
        override val mySide: Side,
    ) : OnlineGameEvent
}

/**
 * Orchestrates online match-making and in-game peer communication.
 * Game-agnostic; caller provides gameType (五子棋固定 "gomoku").
 *
 * Session isolation: all mutable per-game state lives inside [OnlineSession].
 * Each [connect] call creates a fresh session atomically, so rapid
 * re-entries or parallel rooms never share stale state.
 *
 * Reliability: every [sendMove] carries a side-scoped monotonic sequence number.
 * The peer must echo an ack message with the same seq.
 * Un-acked messages are retransmitted up to [maxAckRetries] times
 * with [ackTimeoutMs] between attempts.
 *
 * 五子棋走子载荷:落点写 fromFile/fromRank 槽位(等价坐标 "H8" 的数值形态),
 * toFile/toRank 不用;[opponentMoves] 只消费 first。
 */
@Singleton
class OnlinePlayUseCase @Inject constructor(
    private val signalingClient: SignalingClient,
) {

    /** Singleton 生命周期内的托管 scope，App 退出时自动取消。 */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Default game type; override for other games. */
    var gameType: String = "gomoku"

    /* ───── Session holder ───── */

    @Volatile
    private var session: OnlineSession? = null

    private val sessionLock = Any()
    private val sessionFlow: MutableStateFlow<OnlineSession?> =
        MutableStateFlow(null)

    /* ───── Bridged outer flows (delegate to current session) ───── */

    val connectionState: StateFlow<ConnectionState> =
        sessionFlow.flatMapLatest { it?.connectionState ?: flowOf(ConnectionState.IDLE) }
            .let { flow ->
                val state = MutableStateFlow(ConnectionState.IDLE)
                // Bridge: collect from flatMapLatest into a stable StateFlow
                scope.launch {
                    flow.collect { state.value = it }
                }
                state.asStateFlow()
            }

    val debugEvents: StateFlow<List<String>> =
        sessionFlow.flatMapLatest { it?.debugEvents ?: flowOf(emptyList()) }
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

    private class OpponentMailbox {
        private val pending = ArrayDeque<OnlineGameEvent>()
        private val changes = MutableStateFlow(0L)
        private val deliveryMutex = Mutex()

        fun enqueue(event: OnlineGameEvent) = synchronized(pending) {
            pending.addLast(event)
            changes.value += 1
        }

        private fun firstPending(): OnlineGameEvent? = synchronized(pending) { pending.firstOrNull() }

        fun complete(event: OnlineGameEvent) = synchronized(pending) {
            if (pending.firstOrNull() === event) pending.removeFirst()
        }

        val events: Flow<OnlineGameEvent> = flow {
            deliveryMutex.withLock {
                while (true) {
                    currentCoroutineContext().ensureActive()
                    changes.first { firstPending() != null }
                    val event = requireNotNull(firstPending())
                    // Keep the head until the collector finishes persistence. Cancellation
                    // or a failed write leaves it available for the next subscription.
                    emit(event)
                    complete(event)
                }
            }
        }
    }

    private val opponentMailboxes = ConcurrentHashMap<String, OpponentMailbox>()

    private fun mailbox(roomId: String): OpponentMailbox =
        opponentMailboxes.computeIfAbsent(roomId) { OpponentMailbox() }

    /**
     * Ordered, buffered, at-least-once delivery for one room. Collectors must persist
     * each event before returning; do not buffer or launch-and-forget the handler.
     */
    fun opponentEvents(roomId: String): Flow<OnlineGameEvent> {
        require(roomId.isNotBlank()) { "An online room is required" }
        return mailbox(roomId).events
    }

    // Complete inside accepted NonCancellable persistence so cancellation when
    // returning to the collector cannot replay a move already saved to the board.
    internal fun completeOpponentEvent(event: OnlineGameEvent) {
        mailbox(event.roomId).complete(event)
    }

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
        synchronized(sessionLock) {
            val newSession = OnlineSession(roomId, side, config, gameType)
            val oldSession = session
            session = newSession
            oldSession?.dispose()
            sessionFlow.value = newSession

            appendDebug("WS connect room=${roomId.takeLast(6)} side=${side.name} host=$isHost")

            signalingClient.connect(roomId, object : SignalingClient.Listener {
                override fun onRawMessage(raw: String) {
                    synchronized(sessionLock) {
                        if (session !== newSession) return
                        appendDebug("WS <- raw ${raw.take(DebugMessageLimit)}")
                    }
                }

                override fun onMessage(message: OnlineMessage) {
                    synchronized(sessionLock) {
                        if (session !== newSession ||
                            (message.roomId.isNotBlank() && message.roomId != newSession.roomId) ||
                            (message.gameType.isNotBlank() && message.gameType != newSession.gameType)
                        ) return
                        val s = newSession
                        appendDebug("WS <- ${message.type} seq=${message.seq} ${message.coordText()}")
                        when (message.type.lowercase()) {
                            "ack" -> s.handleAck(message)
                            "move" -> s.handleOpponentMove(message)
                            "ready" -> s.handleReady(isHost)
                            "start" -> if (s.isOpponent(message)) s.handleStart(message.fen)
                            "resign" -> if (s.isOpponent(message)) s.handleResign()
                            "disconnect" ->
                                s._connectionState.value = ConnectionState.OPPONENT_DISCONNECTED
                            "error" ->
                                s._connectionState.value = ConnectionState.ERROR
                        }
                    }
                }

                override fun onConnected() {
                    synchronized(sessionLock) {
                        if (session !== newSession) return
                        val s = newSession
                        s._connectionState.value = ConnectionState.WAITING_FOR_OPPONENT
                        appendDebug("WS connected")
                        if (!isHost) s.sendReady()
                    }
                }

                override fun onDisconnected() {
                    synchronized(sessionLock) {
                        if (session !== newSession) return
                        newSession._connectionState.value = ConnectionState.OPPONENT_DISCONNECTED
                        appendDebug("WS disconnected")
                    }
                }

                override fun onError(error: String) {
                    synchronized(sessionLock) {
                        if (session !== newSession) return
                        newSession._connectionState.value = ConnectionState.ERROR
                        appendDebug("WS error $error")
                    }
                }
            })
        }
    }

    fun sendMove(move: GomokuMove) {
        synchronized(sessionLock) { session?.sendMove(move) }
    }

    fun sendMove(move: GomokuMove, roomId: String, side: Side): Boolean =
        sendToRoom(roomId, side) { it.sendMove(move) }

    fun sendReady() {
        synchronized(sessionLock) { session?.sendReady() }
    }

    fun sendStart() {
        synchronized(sessionLock) { session?.sendStart() }
    }

    fun sendStart(roomId: String, side: Side): Boolean =
        sendToRoom(roomId, side) { it.sendStart() }

    fun sendResign() {
        synchronized(sessionLock) { session?.sendResign() }
    }

    fun sendResign(roomId: String, side: Side): Boolean =
        sendToRoom(roomId, side) { it.sendResign() }

    private fun sendToRoom(roomId: String, side: Side, send: (OnlineSession) -> Unit): Boolean =
        synchronized(sessionLock) {
            val current = session ?: return@synchronized false
            if (current.roomId != roomId || current.mySide != side) return@synchronized false
            send(current)
            true
        }

    fun disconnect() {
        synchronized(sessionLock) {
            appendDebug("WS disconnect by user")
            val old = session
            session = null
            sessionFlow.value = null
            old?.dispose()
            signalingClient.disconnect()
        }
    }

    val mySide: Side
        get() = session?.mySide ?: Side.BLACK

    val currentRoomId: String
        get() = session?.roomId.orEmpty()

    /* ─────────── private helpers ─────────── */

    private fun appendDebug(message: String) {
        synchronized(sessionLock) {
            val time = synchronized(DebugTimeFormat) { DebugTimeFormat.format(Date()) }
            val s = session
            if (s != null) {
                s._debugEvents.value = (s._debugEvents.value + "$time $message").takeLast(MaxDebugEvents)
            }
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
        val gameType: String,
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

        fun sendMove(move: GomokuMove) {
            val seq = nextOutgoingSeq()
            val msg = OnlineMessage(
                type = "move",
                gameType = gameType,
                roomId = roomId,
                senderSide = mySide.name,
                seq = seq,
                fromFile = move.to.file,
                fromRank = move.to.rank,
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

        fun handleStart(initialFen: String) {
            _connectionState.value = ConnectionState.PLAYING
            appendDebug("START received")
            mailbox(roomId).enqueue(OnlineGameEvent.Started(roomId, mySide, initialFen))
            _opponentStarted.tryEmit(Unit)
        }

        fun handleResign() {
            mailbox(roomId).enqueue(OnlineGameEvent.Resigned(roomId, mySide))
            _opponentResigned.tryEmit(Unit)
        }

        fun isOpponent(message: OnlineMessage): Boolean {
            if (message.senderSide.isBlank()) return true
            if (message.senderSide.equals(mySide.name, ignoreCase = true)) return false
            if (message.senderSide.equals(mySide.opposite().name, ignoreCase = true)) return true
            _connectionState.value = ConnectionState.ERROR
            appendDebug("Invalid sender side: ${message.senderSide}")
            return false
        }

        fun handleOpponentMove(message: OnlineMessage) {
            if (!isOpponent(message)) {
                appendDebug("MOVE ignored sender=${message.senderSide} seq=${message.seq}")
                return
            }
            if (message.seq > 0 && handledMoveSeqs.contains(message.seq)) {
                appendDebug("MOVE duplicate ignored key=${message.seq}")
                sendAck(message)
                return
            }
            // 五子棋只用落点(from* 槽位),to* 回填同值保持 Pair 形态
            val point = BoardPoint(message.fromFile, message.fromRank)
            appendDebug("MOVE emit ${point.file},${point.rank}")
            mailbox(roomId).enqueue(OnlineGameEvent.Move(roomId, mySide, point, message.seq))
            if (message.seq > 0) handledMoveSeqs.add(message.seq)
            _opponentMoves.tryEmit(Pair(point, point))
            // ACK only after the ordered mailbox owns the message.
            sendAck(message)
        }

        fun dispose() {
            scope.cancel()
            pendingAcks.clear()
            handledMoveSeqs.clear()
        }

        private fun scheduleRetry(seq: Int) {
            scope.launch {
                delay(ackTimeoutMs)
                synchronized(sessionLock) {
                    if (session !== this@OnlineSession) return@launch
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
        }

        // 黑方(先手/房主侧)奇数 seq,白方偶数 seq,两端命名空间隔离
        private fun nextOutgoingSeq(): Int {
            seqCounter += 1
            return when (mySide) {
                Side.BLACK -> seqCounter * 2 - 1
                Side.WHITE -> seqCounter * 2
            }
        }
    }

    private fun OnlineMessage.coordText(): String =
        if (type.equals("move", ignoreCase = true)) {
            "$fromFile,$fromRank side=$senderSide"
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
