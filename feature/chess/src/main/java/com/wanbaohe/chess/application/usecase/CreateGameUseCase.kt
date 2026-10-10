package com.wanbaohe.chess.application.usecase

import com.shifenmiao.common.manager.AIEngineManager
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.dto.GamePreparation
import com.wanbaohe.chess.application.dto.engineSlotFor
import com.wanbaohe.chess.application.port.outbound.ChessAiStore
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.port.outbound.GameStore
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.model.GameSetup
import com.wanbaohe.chess.domain.model.GameOrigin
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateGameUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val chessAiStore: ChessAiStore,
    private val aiEngineManager: AIEngineManager,
) {

    suspend fun createLocal(title: String): String =
        create(title, GameSetup.local(), FenCodec.INITIAL_FEN)

    suspend fun createHumanVsAi(title: String, aiSide: Side): String =
        create(title, GameSetup.humanVsAi(aiSide), FenCodec.INITIAL_FEN)

    suspend fun createAiVsAi(title: String): String =
        create(title, GameSetup.aiVsAi(), FenCodec.INITIAL_FEN)

    suspend fun resolvePreparation(preparation: GamePreparation): GamePreparation {
        val defaults = chessAiStore.get()
        var resolved = preparation
        for (side in Side.entries) {
            if (preparation.setup.playerTypeFor(side) != PlayerType.LLM || preparation.aiConfigFor(side) != null) continue
            val slot = preparation.setup.mode.engineSlotFor(side)
            val engine = when (slot) {
                EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
                EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
                EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
            }
            resolved = resolved.withAiConfig(side, GameAiPlayerConfig.capture(defaults.sourceFor(slot), engine))
        }
        return resolved
    }

    suspend fun createPrepared(preparation: GamePreparation): String {
        val resolved = resolvePreparation(preparation)
        return create(
            title = resolved.title,
            setup = resolved.setup,
            initialFen = resolved.initialFen,
            redPlayerConfigJson = resolved.whiteAiConfig?.encode() ?: "{}",
            blackPlayerConfigJson = resolved.blackAiConfig?.encode() ?: "{}",
            origin = resolved.origin,
        )
    }

    suspend fun createOnline(
        title: String,
        mySide: Side,
        initialFen: String = FenCodec.INITIAL_FEN,
        roomId: String = "",
        opponentName: String = "",
        opponentAvatarUrl: String = "",
    ): String {
        val normalizedFen = FenCodec.encode(FenCodec.parse(initialFen))
        val configJson = onlineConfigJson(roomId, mySide, opponentName, opponentAvatarUrl, normalizedFen)
        return create(
            title = title,
            setup = GameSetup.online(mySide),
            initialFen = normalizedFen,
            redPlayerConfigJson = configJson,
            blackPlayerConfigJson = configJson,
        )
    }

    suspend fun create(
        title: String,
        setup: GameSetup,
        initialFen: String,
        redPlayerConfigJson: String = "{}",
        blackPlayerConfigJson: String = "{}",
        origin: GameOrigin? = null,
    ): String {
        val gameId = UUID.randomUUID().toString()
        val normalizedFen = FenCodec.encode(FenCodec.parse(initialFen))
        val aiConfig = resolvePreparation(GamePreparation(
            title = title,
            setup = setup,
            initialFen = normalizedFen,
            whiteAiConfig = if (setup.playerTypeFor(Side.WHITE) == PlayerType.LLM)
                GameAiPlayerConfig.decode(redPlayerConfigJson) else null,
            blackAiConfig = if (setup.playerTypeFor(Side.BLACK) == PlayerType.LLM)
                GameAiPlayerConfig.decode(blackPlayerConfigJson) else null,
        ))
        val now = System.currentTimeMillis()
        gameStore.insert(
            GameEntity(
                id = gameId,
                title = title,
                mode = setup.mode,
                whitePlayerType = setup.playerTypeFor(Side.WHITE),
                blackPlayerType = setup.playerTypeFor(Side.BLACK),
                redPlayerConfigJson = if (setup.playerTypeFor(Side.WHITE) == PlayerType.LLM)
                    requireNotNull(aiConfig.whiteAiConfig).encode() else redPlayerConfigJson,
                blackPlayerConfigJson = if (setup.playerTypeFor(Side.BLACK) == PlayerType.LLM)
                    requireNotNull(aiConfig.blackAiConfig).encode() else blackPlayerConfigJson,
                initialFen = normalizedFen,
                currentFen = normalizedFen,
                currentPly = 0,
                status = GameStatus.NOT_STARTED,
                resultText = "",
                winnerSide = "",
                startedAt = 0L,
                lastMoveAt = 0L,
                lastPlayedAt = 0L,
                updatedAt = now,
                origin = origin,
            ),
        )

        return gameId
    }

    private fun onlineConfigJson(
        roomId: String,
        mySide: Side,
        opponentName: String,
        opponentAvatarUrl: String,
        initialFen: String,
    ): String = JSONObject()
        .put("roomId", roomId)
        .put("mySide", mySide.name)
        .put("opponentName", opponentName)
        .put("opponentAvatarUrl", opponentAvatarUrl)
        .put("initialFen", initialFen)
        .toString()
}
