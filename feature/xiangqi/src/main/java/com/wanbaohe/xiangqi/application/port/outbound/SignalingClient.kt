package com.wanbaohe.xiangqi.application.port.outbound

import com.wanbaohe.xiangqi.domain.model.OnlineMessage
import com.wanbaohe.xiangqi.domain.model.OnlineRoomConfig

/**
 * Outbound port for game-agnostic signaling server communication.
 * Abstracts REST + WebSocket IO from domain logic.
 */
interface SignalingClient {

    suspend fun createRoom(gameType: String, hostName: String, hostAvatarUrl: String, config: OnlineRoomConfig): Result<RoomInfo>
    suspend fun listOpenRooms(gameType: String): Result<List<RoomInfo>>
    suspend fun joinRoom(roomId: String, guestName: String, guestAvatarUrl: String): Result<RoomInfo>
    suspend fun leaveRoom(roomId: String): Result<Unit>

    fun connect(roomId: String, listener: Listener)
    fun disconnect()
    fun sendMessage(message: OnlineMessage)

    interface Listener {
        fun onRawMessage(raw: String) = Unit
        fun onMessage(message: OnlineMessage)
        fun onConnected()
        fun onDisconnected()
        fun onError(error: String)
    }
}

data class RoomInfo(
    val id: String,
    val gameType: String,
    val hostId: Int,
    val hostName: String,
    val hostAvatarUrl: String,
    val guestId: Int,
    val guestName: String,
    val guestAvatarUrl: String,
    val status: String,
    val createdAt: Long,
    val config: OnlineRoomConfig,
)
