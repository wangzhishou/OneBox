package com.wanbaohe.gomoku.application.dto

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.port.outbound.storageKey
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.domain.model.GameSetup
import com.wanbaohe.gomoku.domain.model.PlayerSeat
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject

@Serializable
data class GameAiPlayerConfig(
    val sourceKey: String = "",
    val engineName: String = "",
    val engineProtocol: String = "",
    val engineTitle: String = "",
    val model: AiModel? = null,
) {
    val source: GomokuAiSource get() = GomokuAiSource.fromKey(sourceKey)

    val isSupported: Boolean
        get() = sourceKey.isNotBlank() && when (val source = source) {
            GomokuAiSource.WorkingModel -> engineName.isNotBlank() && model?.name?.isNotBlank() == true &&
                engineProtocol.isNotBlank() &&
                AiRequestProtocol.entries.firstOrNull { it.name.equals(engineProtocol, ignoreCase = true) }
                    ?.let { !it.isNonChat && it != AiRequestProtocol.LOCAL_ON_DEVICE } == true
            is GomokuAiSource.RemoteEngine -> source in GomokuAiSource.RemoteEngine.presets
        }

    fun encode(): String = AppJson.encodeToString(this)

    companion object {
        fun capture(source: GomokuAiSource, engine: AiEngine): GameAiPlayerConfig =
            if (source == GomokuAiSource.WorkingModel) {
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
            runCatching { AppJson.decodeFromString<GameAiPlayerConfig>(json) }
                .getOrNull()?.takeIf { it.sourceKey.isNotBlank() }

        fun isLegacyEmpty(json: String): Boolean = json.isBlank() ||
            runCatching { (AppJson.parseToJsonElement(json) as? JsonObject)?.isEmpty() == true }.getOrDefault(false)
    }
}

data class GamePreparation(
    val title: String = "",
    val setup: GameSetup = GameSetup.humanVsAi(Side.WHITE),
    val initialFen: String = FenCodec.INITIAL_FEN,
    val blackAiConfig: GameAiPlayerConfig? = null,
    val whiteAiConfig: GameAiPlayerConfig? = null,
    val origin: GameOrigin? = null,
) {
    fun aiConfigFor(side: Side): GameAiPlayerConfig? =
        if (side == Side.BLACK) blackAiConfig else whiteAiConfig

    fun withAiConfig(side: Side, config: GameAiPlayerConfig): GamePreparation =
        if (side == Side.BLACK) copy(blackAiConfig = config) else copy(whiteAiConfig = config)
}

fun GameDetail.aiConfigFor(side: Side): GameAiPlayerConfig? =
    if (side == Side.BLACK) blackAiConfig else whiteAiConfig

fun GameMode.engineSlotFor(side: Side): EngineSlot =
    if (this == GameMode.LLM_VS_LLM) {
        if (side == Side.BLACK) EngineSlot.DUEL_A else EngineSlot.DUEL_B
    } else EngineSlot.FAST

fun GameDetail.fenAtPly(ply: Int): String =
    if (ply == 0) initialFen else requireNotNull(plies.firstOrNull { it.ply == ply }).afterFen

fun GameDetail.prepareFrom(board: BoardState, sourcePly: Int = currentPly): GamePreparation {
    return GamePreparation(
        setup = if (mode == GameMode.ONLINE_PVP) GameSetup.humanVsAi(board.sideToMove.opposite())
            else GameSetup(mode, listOf(PlayerSeat(Side.BLACK, blackPlayerType), PlayerSeat(Side.WHITE, whitePlayerType))),
        initialFen = FenCodec.encode(board.copy(board = board.board.toList(), moveNumber = 1)),
        blackAiConfig = blackAiConfig,
        whiteAiConfig = whiteAiConfig,
        origin = GameOrigin(id, sourcePly, fenAtPly(sourcePly), title),
    )
}
