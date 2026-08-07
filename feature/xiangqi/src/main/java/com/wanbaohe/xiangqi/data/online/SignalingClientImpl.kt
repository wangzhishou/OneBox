package com.wanbaohe.xiangqi.data.online

import com.shifenmiao.network.NetworkBuilder
import com.wanbaohe.xiangqi.application.port.outbound.RoomInfo
import com.wanbaohe.xiangqi.application.port.outbound.SignalingClient
import com.wanbaohe.xiangqi.domain.model.OnlineMessage
import com.wanbaohe.xiangqi.domain.model.OnlineRoomConfig
import com.wanbaohe.xiangqi.domain.model.Side
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.R

/**
 * Production implementation of [SignalingClient].
 *
 * Features:
 * - REST calls through Retrofit
 * - OkHttp WebSocket with **automatic reconnection** (exponential back-off)
 */
@Singleton
class SignalingClientImpl @Inject constructor(
    private val api: SignalingApi,
    @Named("DefaultOkHttpClient") private val okHttpClient: OkHttpClient,
) : SignalingClient {

    private val reconnectScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reconnectJob: Job? = null

    private var webSocket: WebSocket? = null
    private var listener: SignalingClient.Listener? = null

    /* ───── reconnection state ───── */
    private var isUserDisconnect = false
    private var reconnectAttempts = 0
    private var savedRoomId: String = ""
    private var savedListener: SignalingClient.Listener? = null

    override suspend fun createRoom(
        gameType: String,
        hostName: String,
        hostAvatarUrl: String,
        config: OnlineRoomConfig,
    ): Result<RoomInfo> =
        handleApiCall {
            api.createRoom(
                RoomCreateRequest(
                    gameType = gameType,
                    hostName = hostName,
                    hostAvatarUrl = hostAvatarUrl,
                    gameConfig = config.toJsonObject(),
                )
            )
        }

    override suspend fun listOpenRooms(gameType: String): Result<List<RoomInfo>> =
        handleApiListCall {
            api.listRooms(gameType = gameType)
        }

    override suspend fun joinRoom(roomId: String, guestName: String, guestAvatarUrl: String): Result<RoomInfo> =
        handleApiCall {
            api.joinRoom(roomId, RoomJoinRequest(guestName = guestName, guestAvatarUrl = guestAvatarUrl))
        }

    override suspend fun leaveRoom(roomId: String): Result<Unit> =
        try {
            val response = api.leaveRoom(roomId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Leave failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override fun connect(roomId: String, listener: SignalingClient.Listener) {
        webSocket?.cancel()
        reconnectJob?.cancel()
        this.savedRoomId = roomId
        this.savedListener = listener
        this.isUserDisconnect = false
        this.reconnectAttempts = 0
        doConnect(roomId, listener)
    }

    private fun doConnect(roomId: String, listener: SignalingClient.Listener) {
        this.listener = listener
        val base = NetworkBuilder.getBaseUrl()
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .removeSuffix("/")
        val url = "$base/game/signaling?roomId=$roomId"

        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectAttempts = 0
                listener.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    listener.onRawMessage(text)
                    val msg = OnlineMessage.fromJson(text)
                    listener.onMessage(msg)
                } catch (_: Exception) {
                    listener.onError("Invalid message: $text")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (code == 1000) {
                    listener.onDisconnected()
                } else {
                    attemptReconnect()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                listener.onError(t.message ?: "WebSocket failure")
                attemptReconnect()
            }
        })
    }

    override fun disconnect() {
        isUserDisconnect = true
        reconnectJob?.cancel()
        reconnectJob = null
        webSocket?.close(1000, "User disconnect")
        webSocket = null
        listener = null
    }

    override fun sendMessage(message: OnlineMessage) {
        val json = OnlineMessage.toJson(message)
        webSocket?.send(json)
    }

    /* ─────────── private ─────────── */

    private fun attemptReconnect() {
        if (isUserDisconnect) return
        if (reconnectAttempts >= MaxReconnectAttempts) {
            savedListener?.onError(AppContext.getString(R.string.xiangqi_signaling_disconnected))
            return
        }
        reconnectAttempts++
        val delayMs = (1000L * (1 shl (reconnectAttempts - 1).coerceAtMost(4)))
            .coerceAtMost(16000L)
        reconnectJob?.cancel()
        reconnectJob = reconnectScope.launch {
            delay(delayMs)
            savedRoomId.takeIf { it.isNotEmpty() }?.let { roomId ->
                savedListener?.let { listener ->
                    doConnect(roomId, listener)
                }
            }
        }
    }

    private suspend fun handleApiCall(
        call: suspend () -> retrofit2.Response<RoomResponse>,
    ): Result<RoomInfo> = try {
        val response = call()
        val room = response.body()?.room
        if (response.isSuccessful && room != null) {
            Result.success(room.toModel())
        } else {
            Result.failure(Exception(response.errorBody()?.string() ?: "API error"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun handleApiListCall(
        call: suspend () -> retrofit2.Response<RoomsListResponse>,
    ): Result<List<RoomInfo>> = try {
        val response = call()
        val rooms = response.body()?.rooms
        if (response.isSuccessful && rooms != null) {
            Result.success(rooms.map { it.toModel() })
        } else {
            Result.failure(Exception(response.errorBody()?.string() ?: "API error"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun RoomDto.toModel() = RoomInfo(
        id = id,
        gameType = gameType,
        hostId = hostId,
        hostName = hostName,
        hostAvatarUrl = hostAvatarUrl.orEmpty().ifBlank { hostAvatar.orEmpty() },
        guestId = guestId,
        guestName = guestName,
        guestAvatarUrl = guestAvatarUrl.orEmpty().ifBlank { guestAvatar.orEmpty() },
        status = status,
        createdAt = createdAt,
        config = gameConfig.toRoomConfig(),
    )

    private fun OnlineRoomConfig.toJsonObject() = JsonObject().apply {
        addProperty("initialFen", initialFen)
        addProperty("hostSide", hostSide.name)
        addProperty("guestSide", guestSide.name)
        addProperty("allowUndo", allowUndo)
    }

    private fun JsonObject?.toRoomConfig(): OnlineRoomConfig {
        if (this == null) return OnlineRoomConfig()
        return runCatching {
            OnlineRoomConfig(
                initialFen = get("initialFen")?.asString.orEmpty().ifBlank { OnlineRoomConfig().initialFen },
                hostSide = get("hostSide")?.asString.toSideOrDefault(Side.RED),
                guestSide = get("guestSide")?.asString.toSideOrDefault(Side.BLACK),
                allowUndo = get("allowUndo")?.asBoolean ?: false,
            )
        }.getOrDefault(OnlineRoomConfig())
    }

    private fun String?.toSideOrDefault(default: Side): Side =
        runCatching { Side.valueOf(this.orEmpty()) }.getOrDefault(default)

    companion object {
        private const val MaxReconnectAttempts = 10
    }
}
