package com.wanbaohe.chess.application.usecase

import com.wanbaohe.chess.application.port.outbound.AiTaskStore
import com.wanbaohe.chess.application.port.outbound.GameStore
import com.wanbaohe.chess.application.port.outbound.MoveStore
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
