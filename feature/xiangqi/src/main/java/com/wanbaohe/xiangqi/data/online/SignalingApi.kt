package com.wanbaohe.xiangqi.data.online

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * REST API for generic game online room management.
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

data class RoomCreateRequest(
    val gameType: String,
    val hostName: String,
    val hostAvatarUrl: String = "",
    val gameConfig: JsonObject? = null,
)

data class RoomJoinRequest(
    val guestName: String,
    val guestAvatarUrl: String = "",
)

data class RoomResponse(@SerializedName("data") val room: RoomDto?)

data class RoomsListResponse(@SerializedName("data") val rooms: List<RoomDto>?)

data class SuccessResponse(val message: String)

data class RoomDto(
    val id: String,
    val gameType: String,
    val hostId: Int,
    val hostName: String,
    val hostAvatarUrl: String? = null,
    val hostAvatar: String? = null,
    val guestId: Int,
    val guestName: String,
    val guestAvatarUrl: String? = null,
    val guestAvatar: String? = null,
    val status: String,
    val createdAt: Long,
    val gameConfig: JsonObject? = null,
)
