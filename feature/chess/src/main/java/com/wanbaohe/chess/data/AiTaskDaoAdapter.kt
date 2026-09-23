package com.wanbaohe.chess.data

import com.shifenmiao.database.chess.dao.ChessAiTaskDao
import com.shifenmiao.database.chess.entity.ChessAiTaskEntity
import com.wanbaohe.chess.application.port.outbound.AiTaskEntity
import com.wanbaohe.chess.application.port.outbound.AiTaskStatus
import com.wanbaohe.chess.application.port.outbound.AiTaskStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiTaskDaoAdapter @Inject constructor(
    private val aiTaskDao: ChessAiTaskDao,
) : AiTaskStore {

    override suspend fun getLatestByGame(gameId: String): AiTaskEntity? =
        aiTaskDao.getLatestTask(gameId)?.takeUnless { it.status == "DONE" }?.toEntity()

    override suspend fun upsert(entity: AiTaskEntity) {
        aiTaskDao.upsert(entity.toDbEntity())
    }

    override suspend fun deleteByGame(gameId: String) {
        aiTaskDao.deleteByGameId(gameId)
    }

    private fun ChessAiTaskEntity.toEntity() = AiTaskEntity(
        id = id,
        gameId = gameId,
        targetPly = targetPly,
        requestJson = requestJson,
        status = AiTaskStatus.valueOf(status),
        validatedMove = validatedMove,
        responseJson = responseJson,
        errorMessage = errorMessage,
    )

    private fun AiTaskEntity.toDbEntity() = ChessAiTaskEntity(
        id = id,
        gameId = gameId,
        targetPly = targetPly,
        requestJson = requestJson,
        status = status.name,
        validatedMove = validatedMove,
        responseJson = responseJson,
        errorMessage = errorMessage,
    )
}
