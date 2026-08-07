package com.wanbaohe.xiangqi.application.port.outbound

import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import kotlinx.coroutines.flow.Flow

interface GameStore {
    fun observeAll(): Flow<List<GameSummaryEntity>>
    fun observeById(gameId: String): Flow<GameEntity?>
    suspend fun getById(gameId: String): GameEntity?
    suspend fun insert(entity: GameEntity): String
    suspend fun update(entity: GameEntity)
    suspend fun archive(gameId: String)
}

data class GameEntity(
    val id: String,
    val title: String,
    val mode: GameMode,
    val redPlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val redPlayerConfigJson: String,
    val blackPlayerConfigJson: String,
    val initialFen: String,
    val currentFen: String,
    val currentPly: Int,
    val status: GameStatus,
    val resultText: String,
    val winnerSide: String,
    val startedAt: Long,
    val lastMoveAt: Long,
    val lastPlayedAt: Long,
    val updatedAt: Long,
)

data class GameSummaryEntity(
    val id: String,
    val title: String,
    val mode: GameMode,
    val redPlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val status: GameStatus,
    val resultText: String,
    val updatedAt: Long,
)
