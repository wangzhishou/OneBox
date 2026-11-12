/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.chess.application.dto

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.storageKey
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameOrigin
import com.wanbaohe.chess.domain.model.GameSetup
import com.wanbaohe.chess.domain.model.PlayerSeat
import com.wanbaohe.chess.domain.model.Side
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
    val source: ChessAiSource get() = ChessAiSource.fromKey(sourceKey)

    val isSupported: Boolean
        get() = sourceKey.isNotBlank() && when (val source = source) {
            ChessAiSource.WorkingModel -> engineName.isNotBlank() && model?.name?.isNotBlank() == true &&
                AiRequestProtocol.entries.firstOrNull { it.name == engineProtocol }
                    ?.let { !it.isNonChat && it != AiRequestProtocol.LOCAL_ON_DEVICE } == true
            is ChessAiSource.RemoteEngine -> source in ChessAiSource.RemoteEngine.presets
        }

    fun encode(): String = AppJson.encodeToString(this)

    companion object {
        fun capture(source: ChessAiSource, engine: AiEngine): GameAiPlayerConfig =
            if (source == ChessAiSource.WorkingModel) GameAiPlayerConfig(
                sourceKey = source.storageKey(),
                engineName = engine.name,
                engineProtocol = engine.requestProtocol.name,
                engineTitle = engine.title,
                model = engine.model,
            ) else GameAiPlayerConfig(sourceKey = source.storageKey())

        fun decode(json: String): GameAiPlayerConfig? =
            runCatching { AppJson.decodeFromString<GameAiPlayerConfig>(json) }
                .getOrNull()?.takeIf { it.sourceKey.isNotBlank() }

        fun isLegacyEmpty(json: String): Boolean = json.isBlank() ||
            runCatching { (AppJson.parseToJsonElement(json) as? JsonObject)?.isEmpty() == true }.getOrDefault(false)
    }
}

data class GamePreparation(
    val title: String = "",
    val setup: GameSetup = GameSetup.humanVsAi(Side.BLACK),
    val initialFen: String = FenCodec.INITIAL_FEN,
    val whiteAiConfig: GameAiPlayerConfig? = null,
    val blackAiConfig: GameAiPlayerConfig? = null,
    val origin: GameOrigin? = null,
) {
    fun aiConfigFor(side: Side): GameAiPlayerConfig? =
        if (side == Side.WHITE) whiteAiConfig else blackAiConfig

    fun withAiConfig(side: Side, config: GameAiPlayerConfig): GamePreparation =
        if (side == Side.WHITE) copy(whiteAiConfig = config) else copy(blackAiConfig = config)
}

fun GameMode.engineSlotFor(side: Side): EngineSlot =
    if (this == GameMode.LLM_VS_LLM) {
        if (side == Side.WHITE) EngineSlot.DUEL_A else EngineSlot.DUEL_B
    } else EngineSlot.FAST

fun GameDetail.aiConfigFor(side: Side): GameAiPlayerConfig? =
    if (side == Side.WHITE) whiteAiConfig else blackAiConfig

fun GameDetail.prepareFrom(board: BoardState, sourcePly: Int = currentPly): GamePreparation {
    require(mode != GameMode.ONLINE_PVP)
    val sourceFen = if (sourcePly == 0) initialFen else
        requireNotNull(plies.firstOrNull { it.ply == sourcePly }).afterFen
    return GamePreparation(
        setup = GameSetup(mode, listOf(
            PlayerSeat(Side.WHITE, whitePlayerType), PlayerSeat(Side.BLACK, blackPlayerType),
        )),
        initialFen = FenCodec.encode(board.copy(halfMoveClock = 0, fullMoveNumber = 1)),
        whiteAiConfig = whiteAiConfig,
        blackAiConfig = blackAiConfig,
        origin = GameOrigin(id, sourcePly, sourceFen, title),
    )
}

fun GameDetail.prepareRestart(): GamePreparation =
    prepareFrom(FenCodec.parse(initialFen), 0).copy(title = title, origin = origin)

fun GameDetail.prepareStandardOpening(): GamePreparation =
    prepareFrom(FenCodec.parse(FenCodec.INITIAL_FEN), 0).copy(origin = null)
