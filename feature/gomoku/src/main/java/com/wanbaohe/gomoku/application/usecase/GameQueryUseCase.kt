package com.wanbaohe.gomoku.application.usecase

import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.dto.GameSummary
import com.wanbaohe.gomoku.application.dto.OnlineGameMetadata
import com.wanbaohe.gomoku.application.dto.PlyRecord
import com.wanbaohe.gomoku.application.port.outbound.GameEntity
import com.wanbaohe.gomoku.application.port.outbound.GameStore
import com.wanbaohe.gomoku.application.port.outbound.GameSummaryEntity
import com.wanbaohe.gomoku.application.port.outbound.MoveStore
import com.wanbaohe.gomoku.application.port.outbound.PlyEntity
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameQueryUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val moveStore: MoveStore,
) {
    companion object {
        fun mostRecentUnfinishedHumanAiGame(games: List<GameSummary>): GameSummary? =
            games.filter {
                it.mode == GameMode.HUMAN_VS_LLM && it.lastPlayedAt > 0L &&
                    it.status in setOf(GameStatus.PLAYING, GameStatus.PAUSED)
            }.maxByOrNull { it.lastPlayedAt }
    }

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
        lastPlayedAt = lastPlayedAt,
        blackAiConfig = if (blackPlayerType == PlayerType.LLM) decodeAiConfig(blackPlayerConfigJson) else null,
        whiteAiConfig = if (whitePlayerType == PlayerType.LLM) decodeAiConfig(redPlayerConfigJson) else null,
        origin = origin,
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
        plies = plies.sortedBy { it.ply }.map { it.toRecord() },
        onlineMetadata = toOnlineMetadata(),
        blackPlayerConfigJson = blackPlayerConfigJson,
        whitePlayerConfigJson = redPlayerConfigJson,
        blackAiConfig = if (blackPlayerType == PlayerType.LLM) decodeAiConfig(blackPlayerConfigJson) else null,
        whiteAiConfig = if (whitePlayerType == PlayerType.LLM) decodeAiConfig(redPlayerConfigJson) else null,
        origin = origin,
    )

    private fun decodeAiConfig(json: String): GameAiPlayerConfig? =
        GameAiPlayerConfig.decode(json) ?: if (GameAiPlayerConfig.isLegacyEmpty(json)) null else GameAiPlayerConfig()

    private fun GameEntity.toOnlineMetadata(): OnlineGameMetadata {
        if (mode != GameMode.ONLINE_PVP) return OnlineGameMetadata()
        val json = runCatching {
            AppJson.parseToJsonElement(redPlayerConfigJson.ifBlank { blackPlayerConfigJson }) as? JsonObject
        }
            .getOrNull()
            ?: return OnlineGameMetadata(initialFen = initialFen)
        fun value(key: String): String = (json[key] as? JsonPrimitive)?.content.orEmpty()
        return OnlineGameMetadata(
            roomId = value("roomId"),
            mySide = runCatching { Side.valueOf(value("mySide")) }.getOrDefault(Side.BLACK),
            opponentName = value("opponentName"),
            opponentAvatarUrl = value("opponentAvatarUrl"),
            initialFen = value("initialFen").ifBlank { initialFen },
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
