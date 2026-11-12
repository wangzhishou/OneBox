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

package com.wanbaohe.mahjong.application.usecase

import com.wanbaohe.mahjong.application.port.outbound.AiObservation
import com.wanbaohe.mahjong.application.port.outbound.MahjongAi
import com.wanbaohe.mahjong.application.port.outbound.SeedProvider
import com.wanbaohe.mahjong.domain.model.MahjongAction
import com.wanbaohe.mahjong.domain.model.MahjongState
import com.wanbaohe.mahjong.domain.model.MahjongTransition
import com.wanbaohe.mahjong.domain.model.Tiles
import com.wanbaohe.mahjong.domain.rules.MahjongEngine
import javax.inject.Inject

class NewRoundUseCase @Inject constructor(
    private val seedProvider: SeedProvider,
) {
    operator fun invoke(): MahjongState = MahjongEngine.newRound(seedProvider.nextSeed())
}

class PlayActionUseCase @Inject constructor() {
    operator fun invoke(state: MahjongState, action: MahjongAction): MahjongTransition? =
        MahjongEngine.reduce(state, action)
}

class RunAiTurnUseCase @Inject constructor(
    private val ai: MahjongAi,
) {
    operator fun invoke(state: MahjongState, seat: Int): MahjongAction {
        val visibleCounts = IntArray(Tiles.KIND_COUNT)
        state.players.forEach { player ->
            player.discards.forEach { visibleCounts[Tiles.kindOf(it)]++ }
            player.melds.flatMap { it.tileIds }.forEach { visibleCounts[Tiles.kindOf(it)]++ }
        }
        val player = state.players[seat]
        player.hand.forEach { visibleCounts[Tiles.kindOf(it)]++ }
        return ai.chooseAction(
            AiObservation(
                seat = seat,
                hand = player.hand,
                melds = player.melds,
                visibleCounts = visibleCounts,
                pendingTileId = state.pending?.tileId,
                legalActions = MahjongEngine.legalActions(state, seat),
            )
        )
    }
}
