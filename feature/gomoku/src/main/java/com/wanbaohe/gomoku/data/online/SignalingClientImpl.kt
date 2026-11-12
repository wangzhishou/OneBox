/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.gomoku.data.online

import com.shifenmiao.network.NetworkBuilder
import com.wanbaohe.gomoku.application.port.outbound.RoomInfo
import com.wanbaohe.gomoku.application.port.outbound.SignalingClient
import com.wanbaohe.gomoku.domain.model.OnlineMessage
import com.wanbaohe.gomoku.domain.model.OnlineRoomConfig
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
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
import com.wanbaohe.gomoku.R

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
            savedListener?.onError(AppContext.getString(R.string.gomoku_signaling_disconnected))
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

    // 与线上服务协议字段一一对应,Gson 时代产物逐字节等价(字符串/布尔,无数字格式差异)
    private fun OnlineRoomConfig.toJsonObject() = buildJsonObject {
        put("initialFen", initialFen)
        put("hostSide", hostSide.name)
        put("guestSide", guestSide.name)
        put("allowUndo", allowUndo)
    }

    private fun JsonObject?.toRoomConfig(): OnlineRoomConfig {
        if (this == null) return OnlineRoomConfig()
        fun str(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
        return runCatching {
            OnlineRoomConfig(
                initialFen = str("initialFen").orEmpty().ifBlank { OnlineRoomConfig().initialFen },
                hostSide = str("hostSide").toSideOrDefault(Side.BLACK),
                guestSide = str("guestSide").toSideOrDefault(Side.WHITE),
                allowUndo = (this["allowUndo"] as? JsonPrimitive)?.booleanOrNull ?: false,
            )
        }.getOrDefault(OnlineRoomConfig())
    }

    private fun String?.toSideOrDefault(default: Side): Side =
        runCatching { Side.valueOf(this.orEmpty()) }.getOrDefault(default)

    companion object {
        private const val MaxReconnectAttempts = 10
    }
}
