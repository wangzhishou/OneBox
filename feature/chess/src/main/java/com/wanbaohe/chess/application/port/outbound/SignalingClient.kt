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

package com.wanbaohe.chess.application.port.outbound

import com.wanbaohe.chess.domain.model.OnlineMessage
import com.wanbaohe.chess.domain.model.OnlineRoomConfig

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
