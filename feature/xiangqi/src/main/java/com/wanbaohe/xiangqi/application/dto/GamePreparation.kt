package com.wanbaohe.xiangqi.application.dto

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.application.port.outbound.storageKey
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameOrigin
import com.wanbaohe.xiangqi.domain.model.GameSetup
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.PlayerSeat
import com.wanbaohe.xiangqi.domain.model.Side
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class GameAiPlayerConfig(
    val sourceKey: String = "",
    val engineName: String = "",
    val engineProtocol: String = "",
    val engineTitle: String = "",
    val model: AiModel? = null,
) {
    val source: XiangqiAiSource get() = XiangqiAiSource.fromKey(sourceKey)

    fun encode(): String = AppJson.encodeToString(this)

    companion object {
        fun capture(source: XiangqiAiSource, engine: AiEngine): GameAiPlayerConfig =
            if (source == XiangqiAiSource.WorkingModel) {
                GameAiPlayerConfig(
                    sourceKey = source.storageKey(),
                    engineName = engine.name,
                    engineProtocol = engine.requestProtocol.name,
                    engineTitle = engine.title,
                    model = engine.model,
                )
            } else {
                GameAiPlayerConfig(sourceKey = source.storageKey())
            }

        fun decode(json: String): GameAiPlayerConfig? =
            AppJson.decodeFromString<GameAiPlayerConfig>(json)
                .takeIf { it.sourceKey.isNotBlank() }
    }
}

data class GamePreparation(
    val title: String = "",
    val setup: GameSetup = GameSetup.humanVsAi(Side.BLACK),
    val initialFen: String = FenCodec.INITIAL_FEN,
    val redAiConfig: GameAiPlayerConfig? = null,
    val blackAiConfig: GameAiPlayerConfig? = null,
    val origin: GameOrigin? = null,
) {
    fun aiConfigFor(side: Side): GameAiPlayerConfig? =
        if (side == Side.RED) redAiConfig else blackAiConfig

    fun withAiConfig(side: Side, config: GameAiPlayerConfig): GamePreparation =
        if (side == Side.RED) copy(redAiConfig = config) else copy(blackAiConfig = config)
}

fun GameMode.engineSlotFor(side: Side): EngineSlot =
    if (this == GameMode.LLM_VS_LLM) {
        if (side == Side.RED) EngineSlot.DUEL_A else EngineSlot.DUEL_B
    } else {
        EngineSlot.FAST
    }

fun GameDetail.prepareFrom(board: BoardState, sourcePly: Int = currentPly): GamePreparation {
    require(mode != GameMode.ONLINE_PVP)
    val sourceFen = if (sourcePly == 0) initialFen else
        requireNotNull(plies.firstOrNull { it.ply == sourcePly }).afterFen
    return GamePreparation(
        setup = GameSetup(mode, listOf(PlayerSeat(Side.RED, redPlayerType), PlayerSeat(Side.BLACK, blackPlayerType))),
        initialFen = FenCodec.encode(board.copy(halfMoveClock = 0, fullMoveNumber = 1)),
        redAiConfig = redAiConfig,
        blackAiConfig = blackAiConfig,
        origin = GameOrigin(id, sourcePly, sourceFen, title),
    )
}
