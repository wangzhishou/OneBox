package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.port.outbound.GameEntity
import com.wanbaohe.xiangqi.application.port.outbound.GameStore
import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.application.dto.GamePreparation
import com.wanbaohe.xiangqi.application.dto.engineSlotFor
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiStore
import com.shifenmiao.common.manager.AIEngineManager
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.model.GameSetup
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.GameOrigin
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.R
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateGameUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val activityLogRecorder: ActivityLogRecorder,
    private val xiangqiAiStore: XiangqiAiStore,
    private val aiEngineManager: AIEngineManager,
) {

    suspend fun createLocal(title: String): String =
        create(title, GameSetup.local(), FenCodec.INITIAL_FEN)

    suspend fun createHumanVsAi(title: String, aiSide: Side): String =
        create(title, GameSetup.humanVsAi(aiSide), FenCodec.INITIAL_FEN)

    suspend fun createAiVsAi(title: String): String =
        create(title, GameSetup.aiVsAi(), FenCodec.INITIAL_FEN)

    suspend fun resolvePreparation(preparation: GamePreparation): GamePreparation {
        if (Side.entries.none {
                preparation.setup.playerTypeFor(it) == PlayerType.LLM && preparation.aiConfigFor(it) == null
            }
        ) return preparation
        val defaults = xiangqiAiStore.get()
        var resolved = preparation
        for (side in Side.entries) {
            if (preparation.setup.playerTypeFor(side) != PlayerType.LLM ||
                preparation.aiConfigFor(side) != null
            ) continue
            val slot = preparation.setup.mode.engineSlotFor(side)
            val engine = when (slot) {
                EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
                EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
                EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
            }
            resolved = resolved.withAiConfig(
                side,
                GameAiPlayerConfig.capture(defaults.sourceFor(slot), engine),
            )
        }
        return resolved
    }

    suspend fun createPrepared(preparation: GamePreparation): String {
        val resolved = resolvePreparation(preparation)
        return create(
            title = resolved.title,
            setup = resolved.setup,
            initialFen = resolved.initialFen,
            redPlayerConfigJson = resolved.redAiConfig?.encode() ?: "{}",
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
            title, setup, normalizedFen,
            redAiConfig = if (setup.playerTypeFor(Side.RED) == PlayerType.LLM)
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
                redPlayerType = setup.playerTypeFor(Side.RED),
                blackPlayerType = setup.playerTypeFor(Side.BLACK),
                redPlayerConfigJson = if (redPlayerConfigJson == "{}" && setup.playerTypeFor(Side.RED) == PlayerType.LLM)
                    requireNotNull(aiConfig.redAiConfig).encode() else redPlayerConfigJson,
                blackPlayerConfigJson = if (blackPlayerConfigJson == "{}" && setup.playerTypeFor(Side.BLACK) == PlayerType.LLM)
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

        activityLogRecorder.recordXiangqi(
            gameId = gameId,
            actionType = "CREATE",
            title = AppContext.getString(R.string.xiangqi_log_game_created, title),
            description = "模式: ${setup.mode.name}",
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
