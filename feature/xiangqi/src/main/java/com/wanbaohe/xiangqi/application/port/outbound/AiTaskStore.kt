package com.wanbaohe.xiangqi.application.port.outbound

interface AiTaskStore {
    suspend fun getLatestByGame(gameId: String): AiTaskEntity?
    suspend fun upsert(entity: AiTaskEntity)
    suspend fun deleteByGame(gameId: String)
}

data class AiTaskEntity(
    val id: String,
    val gameId: String,
    val targetPly: Int,
    val requestJson: String,
    val status: AiTaskStatus,
    val validatedMove: String = "",
    val responseJson: String = "",
    val errorMessage: String = "",
)

enum class AiTaskStatus { RUNNING, DONE, FAILED }
