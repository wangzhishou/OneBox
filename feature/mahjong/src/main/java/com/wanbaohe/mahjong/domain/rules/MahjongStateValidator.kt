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

package com.wanbaohe.mahjong.domain.rules

import com.wanbaohe.mahjong.domain.model.GamePhase
import com.wanbaohe.mahjong.domain.model.MahjongState
import com.wanbaohe.mahjong.domain.model.Tiles

object MahjongStateValidator {

    fun isValid(state: MahjongState): Boolean {
        if (state.players.size != Tiles.PLAYER_COUNT) return false
        if (state.dealerSeat !in 0 until Tiles.PLAYER_COUNT ||
            state.currentSeat !in 0 until Tiles.PLAYER_COUNT
        ) return false

        val allTiles = buildList {
            addAll(state.wall)
            state.players.forEach {
                addAll(it.hand)
                addAll(it.discards)
                it.melds.forEach { meld -> addAll(meld.tileIds) }
            }
        }
        if (allTiles.size != Tiles.TILE_COUNT || allTiles.distinct().size != Tiles.TILE_COUNT) {
            return false
        }
        if (allTiles.any { it !in 0 until Tiles.TILE_COUNT }) return false
        if (state.players.any { player ->
                player.melds.any { meld ->
                    meld.fromSeat !in 0 until Tiles.PLAYER_COUNT ||
                        meld.tileIds.size !in 3..4
                }
            }
        ) return false

        return when (state.phase) {
            GamePhase.AWAIT_DISCARD -> state.pending == null && state.result == null
            GamePhase.AWAIT_CLAIM -> state.pending != null && state.result == null &&
                state.pending.choices.keys.all { it in state.pending.responders } &&
                state.pending.choices.size <= state.pending.responders.size
            GamePhase.FINISHED -> state.pending == null && state.result != null
        }
    }
}
