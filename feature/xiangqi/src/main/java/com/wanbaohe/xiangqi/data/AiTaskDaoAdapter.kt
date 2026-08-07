package com.wanbaohe.xiangqi.data

import com.shifenmiao.database.xiangqi.dao.XiangqiAiTaskDao
import com.shifenmiao.database.xiangqi.entity.XiangqiAiTaskEntity
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskEntity
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskStatus
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiTaskDaoAdapter @Inject constructor(
    private val aiTaskDao: XiangqiAiTaskDao,
) : AiTaskStore {

    override suspend fun getLatestByGame(gameId: String): AiTaskEntity? =
        aiTaskDao.getLatestTask(gameId)?.takeUnless { it.status == "DONE" }?.toEntity()

    override suspend fun upsert(entity: AiTaskEntity) {
        aiTaskDao.upsert(entity.toDbEntity())
    }

    override suspend fun deleteByGame(gameId: String) {
        aiTaskDao.deleteByGameId(gameId)
    }

    private fun XiangqiAiTaskEntity.toEntity() = AiTaskEntity(
        id = id,
        gameId = gameId,
        targetPly = targetPly,
        requestJson = requestJson,
        status = AiTaskStatus.valueOf(status),
        validatedMove = validatedMove,
        responseJson = responseJson,
        errorMessage = errorMessage,
    )

    private fun AiTaskEntity.toDbEntity() = XiangqiAiTaskEntity(
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
