package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.application.port.outbound.AiTaskStore
import com.wanbaohe.gomoku.application.port.outbound.GameStore
import com.wanbaohe.gomoku.application.port.outbound.MoveStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteGameUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val moveStore: MoveStore,
    private val aiTaskStore: AiTaskStore,
) {
    suspend fun delete(gameId: String) {
        moveStore.deleteByGame(gameId)
        aiTaskStore.deleteByGame(gameId)
        gameStore.archive(gameId)
    }
}
