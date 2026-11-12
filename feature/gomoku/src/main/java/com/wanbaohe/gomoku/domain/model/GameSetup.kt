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

package com.wanbaohe.gomoku.domain.model

data class GameSetup(
    val mode: GameMode,
    val seats: List<PlayerSeat>,
) {
    init {
        val sides = seats.map { it.side }.toSet()
        require(sides == Side.entries.toSet()) { "Each side must have exactly one seat" }
        require(seats.size == Side.entries.size) { "Must not duplicate seats" }
        require(mode.isCompatibleWith(seats.map { it.playerType })) {
            "Game mode ${mode.name} is incompatible with seats"
        }
    }

    fun playerTypeFor(side: Side): PlayerType =
        seats.first { it.side == side }.playerType

    companion object {
        fun local(): GameSetup = GameSetup(
            mode = GameMode.LOCAL_PVP,
            seats = listOf(
                PlayerSeat(Side.BLACK, PlayerType.HUMAN),
                PlayerSeat(Side.WHITE, PlayerType.HUMAN),
            ),
        )

        /** 人机对战;五子棋黑先,默认人类执黑 */
        fun humanVsAi(aiSide: Side = Side.WHITE): GameSetup = GameSetup(
            mode = GameMode.HUMAN_VS_LLM,
            seats = listOf(
                PlayerSeat(Side.BLACK, if (aiSide == Side.BLACK) PlayerType.LLM else PlayerType.HUMAN),
                PlayerSeat(Side.WHITE, if (aiSide == Side.WHITE) PlayerType.LLM else PlayerType.HUMAN),
            ),
        )

        fun aiVsAi(): GameSetup = GameSetup(
            mode = GameMode.LLM_VS_LLM,
            seats = listOf(
                PlayerSeat(Side.BLACK, PlayerType.LLM),
                PlayerSeat(Side.WHITE, PlayerType.LLM),
            ),
        )

        /** 在线双人:我方 HUMAN 坐 [mySide],对方 REMOTE 坐对面 */
        fun online(mySide: Side): GameSetup = GameSetup(
            mode = GameMode.ONLINE_PVP,
            seats = listOf(
                PlayerSeat(Side.BLACK, if (mySide == Side.BLACK) PlayerType.HUMAN else PlayerType.REMOTE),
                PlayerSeat(Side.WHITE, if (mySide == Side.WHITE) PlayerType.HUMAN else PlayerType.REMOTE),
            ),
        )
    }
}

private fun GameMode.isCompatibleWith(playerTypes: List<PlayerType>): Boolean = when (this) {
    GameMode.LOCAL_PVP -> playerTypes.all { it == PlayerType.HUMAN }
    GameMode.HUMAN_VS_LLM -> playerTypes.count { it == PlayerType.HUMAN } == 1 &&
        playerTypes.count { it == PlayerType.LLM } == 1
    GameMode.LLM_VS_LLM -> playerTypes.all { it == PlayerType.LLM }
    GameMode.ONLINE_PVP -> playerTypes.count { it == PlayerType.HUMAN } == 1 &&
        playerTypes.count { it == PlayerType.REMOTE } == 1
}
