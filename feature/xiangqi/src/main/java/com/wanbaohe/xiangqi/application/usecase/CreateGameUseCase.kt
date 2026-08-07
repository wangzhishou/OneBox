package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.port.outbound.GameEntity
import com.wanbaohe.xiangqi.application.port.outbound.GameStore
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.model.GameSetup
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.Side
import com.shifenmiao.database.activity.ActivityLogRecorder
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateGameUseCase @Inject constructor(
    private val gameStore: GameStore,
    private val activityLogRecorder: ActivityLogRecorder,
) {

    suspend fun createLocal(title: String): String =
        create(title, GameSetup.local(), FenCodec.INITIAL_FEN)

    suspend fun createHumanVsAi(title: String, aiSide: Side): String =
        create(title, GameSetup.humanVsAi(aiSide), FenCodec.INITIAL_FEN)

    suspend fun createAiVsAi(title: String): String =
        create(title, GameSetup.aiVsAi(), FenCodec.INITIAL_FEN)

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
    ): String {
        val gameId = UUID.randomUUID().toString()
        val normalizedFen = FenCodec.encode(FenCodec.parse(initialFen))
        val now = System.currentTimeMillis()
        gameStore.insert(
            GameEntity(
                id = gameId,
                title = title,
                mode = setup.mode,
                redPlayerType = setup.playerTypeFor(Side.RED),
                blackPlayerType = setup.playerTypeFor(Side.BLACK),
                redPlayerConfigJson = redPlayerConfigJson,
                blackPlayerConfigJson = blackPlayerConfigJson,
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
            ),
        )

        activityLogRecorder.recordXiangqi(
            gameId = gameId,
            actionType = "CREATE",
            title = "新建对局: $title",
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
