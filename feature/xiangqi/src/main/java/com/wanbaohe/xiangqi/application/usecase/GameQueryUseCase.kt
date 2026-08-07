package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.dto.GameDetail
import com.wanbaohe.xiangqi.application.dto.GameSummary
import com.wanbaohe.xiangqi.application.dto.OnlineGameMetadata
import com.wanbaohe.xiangqi.application.dto.PlyRecord
import com.wanbaohe.xiangqi.application.port.outbound.GameStore
import com.wanbaohe.xiangqi.application.port.outbound.MoveStore
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.Side
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameQueryUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val moveStore: MoveStore,
) {

    fun observeAll(): Flow<List<GameSummary>> = gameStore.observeAll()
        .combine(flowOf(Unit)) { games, _ ->
            games.map { it.toSummary() }
        }

    fun observeById(gameId: String): Flow<GameDetail?> = combine(
        gameStore.observeById(gameId),
        moveStore.observeByGame(gameId),
    ) { game, plies ->
        game?.toDetail(plies)
    }

    suspend fun getById(gameId: String): GameDetail? {
        val game = gameStore.getById(gameId) ?: return null
        return game.toDetail(moveStore.getByGame(gameId))
    }

    private fun com.wanbaohe.xiangqi.application.port.outbound.GameSummaryEntity.toSummary() = GameSummary(
        id = id,
        title = title,
        mode = mode,
        redPlayerType = redPlayerType,
        blackPlayerType = blackPlayerType,
        status = status,
        resultText = resultText,
        updatedAt = updatedAt,
    )

    private fun com.wanbaohe.xiangqi.application.port.outbound.GameEntity.toDetail(
        plies: List<com.wanbaohe.xiangqi.application.port.outbound.PlyEntity>,
    ) = GameDetail(
        id = id,
        title = title,
        mode = mode,
        redPlayerType = redPlayerType,
        blackPlayerType = blackPlayerType,
        initialFen = initialFen,
        currentFen = currentFen,
        currentPly = currentPly,
        status = status,
        startedAt = startedAt,
        lastMoveAt = lastMoveAt,
        plies = plies.map { it.toRecord() },
        onlineMetadata = toOnlineMetadata(),
    )

    private fun com.wanbaohe.xiangqi.application.port.outbound.GameEntity.toOnlineMetadata(): OnlineGameMetadata {
        if (mode != GameMode.ONLINE_PVP) return OnlineGameMetadata()
        val json = runCatching { JSONObject(redPlayerConfigJson.ifBlank { blackPlayerConfigJson }) }
            .getOrNull()
            ?: return OnlineGameMetadata(initialFen = initialFen)
        return OnlineGameMetadata(
            roomId = json.optString("roomId"),
            mySide = runCatching { Side.valueOf(json.optString("mySide")) }.getOrDefault(Side.RED),
            opponentName = json.optString("opponentName"),
            opponentAvatarUrl = json.optString("opponentAvatarUrl"),
            initialFen = json.optString("initialFen", initialFen),
        )
    }

    private fun com.wanbaohe.xiangqi.application.port.outbound.PlyEntity.toRecord() = PlyRecord(
        ply = ply,
        moveUcci = moveUcci,
        moveCn = moveCn,
        beforeFen = beforeFen,
        afterFen = afterFen,
        aiReason = aiReason,
        aiRawResponse = aiRawResponse,
        thinkDurationMs = thinkDurationMs,
    )
}
