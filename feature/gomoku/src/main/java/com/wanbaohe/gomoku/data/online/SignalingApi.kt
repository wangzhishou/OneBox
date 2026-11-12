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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * REST API for generic game online room management.
 * 服务端按 gameType 隔离房间存储,五子棋(固定传 "gomoku")固定传 "gomoku"。
 */
interface SignalingApi {

    @POST("game/rooms")
    suspend fun createRoom(@Body request: RoomCreateRequest): Response<RoomResponse>

    @GET("game/rooms")
    suspend fun listRooms(
        @Query("gameType") gameType: String,
    ): Response<RoomsListResponse>

    @POST("game/rooms/{roomId}/join")
    suspend fun joinRoom(
        @Path("roomId") roomId: String,
        @Body request: RoomJoinRequest,
    ): Response<RoomResponse>

    @DELETE("game/rooms/{roomId}/leave")
    suspend fun leaveRoom(@Path("roomId") roomId: String): Response<SuccessResponse>
}

@Serializable
data class RoomCreateRequest(
    val gameType: String,
    val hostName: String,
    val hostAvatarUrl: String = "",
    val gameConfig: JsonObject? = null,
)

@Serializable
data class RoomJoinRequest(
    val guestName: String,
    val guestAvatarUrl: String = "",
)

@Serializable
data class RoomResponse(@SerialName("data") val room: RoomDto? = null)

@Serializable
data class RoomsListResponse(@SerialName("data") val rooms: List<RoomDto>? = null)

@Serializable
data class SuccessResponse(val message: String = "")

@Serializable
data class RoomDto(
    // 默认值对齐 Gson 容错:服务端(go-proxy game 模块)字段稳定,缺字段时不至于整包解析失败
    val id: String = "",
    val gameType: String = "",
    val hostId: Int = 0,
    val hostName: String = "",
    val hostAvatarUrl: String? = null,
    val hostAvatar: String? = null,
    val guestId: Int = 0,
    val guestName: String = "",
    val guestAvatarUrl: String? = null,
    val guestAvatar: String? = null,
    val status: String = "",
    val createdAt: Long = 0L,
    val gameConfig: JsonObject? = null,
)
