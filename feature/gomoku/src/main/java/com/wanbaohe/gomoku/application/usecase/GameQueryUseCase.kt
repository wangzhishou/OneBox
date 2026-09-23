package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.dto.GameSummary
import com.wanbaohe.gomoku.application.dto.OnlineGameMetadata
import com.wanbaohe.gomoku.application.dto.PlyRecord
import com.wanbaohe.gomoku.application.port.outbound.GameEntity
import com.wanbaohe.gomoku.application.port.outbound.GameStore
import com.wanbaohe.gomoku.application.port.outbound.GameSummaryEntity
import com.wanbaohe.gomoku.application.port.outbound.MoveStore
import com.wanbaohe.gomoku.application.port.outbound.PlyEntity
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.Side
import org.json.JSONObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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

    private fun GameSummaryEntity.toSummary() = GameSummary(
        id = id,
        title = title,
        mode = mode,
        blackPlayerType = blackPlayerType,
        whitePlayerType = whitePlayerType,
        status = status,
        resultText = resultText,
        updatedAt = updatedAt,
        plyCount = plyCount,
    )

    private fun GameEntity.toDetail(plies: List<PlyEntity>) = GameDetail(
        id = id,
        title = title,
        mode = mode,
        blackPlayerType = blackPlayerType,
        whitePlayerType = whitePlayerType,
        initialFen = initialFen,
        currentFen = currentFen,
        currentPly = currentPly,
        status = status,
        resultText = resultText,
        winnerSide = winnerSide,
        startedAt = startedAt,
        lastMoveAt = lastMoveAt,
        plies = plies.map { it.toRecord() },
        onlineMetadata = toOnlineMetadata(),
    )

    private fun GameEntity.toOnlineMetadata(): OnlineGameMetadata {
        if (mode != GameMode.ONLINE_PVP) return OnlineGameMetadata()
        val json = runCatching { JSONObject(redPlayerConfigJson.ifBlank { blackPlayerConfigJson }) }
            .getOrNull()
            ?: return OnlineGameMetadata(initialFen = initialFen)
        return OnlineGameMetadata(
            roomId = json.optString("roomId"),
            mySide = runCatching { Side.valueOf(json.optString("mySide")) }.getOrDefault(Side.BLACK),
            opponentName = json.optString("opponentName"),
            opponentAvatarUrl = json.optString("opponentAvatarUrl"),
            initialFen = json.optString("initialFen", initialFen),
        )
    }

    private fun PlyEntity.toRecord() = PlyRecord(
        ply = ply,
        moveUcci = moveUcci,
        moveCn = moveCn,
        moverSide = moverSide,
        beforeFen = beforeFen,
        afterFen = afterFen,
        aiReason = aiReason,
        aiRawResponse = aiRawResponse,
        thinkDurationMs = thinkDurationMs,
    )
}
