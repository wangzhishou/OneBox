package com.wanbaohe.chess.application.usecase

import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.domain.model.BoardPoint
import javax.inject.Inject
import javax.inject.Singleton

sealed interface OnlineGameEvent {
    val roomId: String

    data class Move(
        override val roomId: String,
        val from: BoardPoint,
        val to: BoardPoint,
    ) : OnlineGameEvent

    data class Started(override val roomId: String) : OnlineGameEvent
    data class Resigned(override val roomId: String) : OnlineGameEvent
}

@Singleton
class OnlineGameSyncUseCase @Inject constructor(
    private val playMove: PlayMoveUseCase,
    private val manageGame: ManageGameUseCase,
) {
    suspend fun accept(gameId: String, event: OnlineGameEvent): GameDetail? = when (event) {
        is OnlineGameEvent.Move -> when (
            val result = playMove.commitOnline(gameId, event.roomId, event.from, event.to)
        ) {
            is PlayMoveUseCase.Result.Success -> result.detail
            is PlayMoveUseCase.Result.Rejected -> null
        }
        is OnlineGameEvent.Started -> manageGame.startOnline(gameId, event.roomId)
        is OnlineGameEvent.Resigned -> manageGame.resignOnline(gameId, event.roomId)
    }
}
