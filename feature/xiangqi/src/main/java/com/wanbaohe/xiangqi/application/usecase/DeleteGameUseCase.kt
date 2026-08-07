package com.wanbaohe.xiangqi.application.usecase

import com.shifenmiao.database.activity.ActivityLogRecorder
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskStore
import com.wanbaohe.xiangqi.application.port.outbound.GameStore
import com.wanbaohe.xiangqi.application.port.outbound.MoveStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteGameUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val moveStore: MoveStore,
    private val aiTaskStore: AiTaskStore,
    private val activityLogRecorder: ActivityLogRecorder,
) {
    suspend fun delete(gameId: String) {
        val game = gameStore.getById(gameId)
        moveStore.deleteByGame(gameId)
        aiTaskStore.deleteByGame(gameId)
        gameStore.archive(gameId)

        activityLogRecorder.recordXiangqi(
            gameId = gameId,
            actionType = "DELETE",
            title = "删除对局: ${game?.title ?: gameId}",
            description = "",
        )
    }
}
