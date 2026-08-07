package com.wanbaohe.xiangqi.application.port.outbound

import com.wanbaohe.xiangqi.domain.model.Side
import kotlinx.coroutines.flow.Flow

interface MoveStore {
    fun observeByGame(gameId: String): Flow<List<PlyEntity>>
    suspend fun getByGame(gameId: String): List<PlyEntity>
    suspend fun getPly(gameId: String, plyNumber: Int): PlyEntity?
    suspend fun insert(entity: PlyEntity)
    suspend fun deleteAfterPly(gameId: String, ply: Int)
    suspend fun deleteByGame(gameId: String)
}

data class PlyEntity(
    val id: String,
    val gameId: String,
    val ply: Int,
    val moveUcci: String,
    val moveCn: String,
    val moverSide: Side,
    val beforeFen: String,
    val afterFen: String,
    val isCapture: Boolean,
    val isCheck: Boolean,
    val isCheckmate: Boolean,
    val aiReason: String,
    val aiRawResponse: String,
    val thinkDurationMs: Long,
)
