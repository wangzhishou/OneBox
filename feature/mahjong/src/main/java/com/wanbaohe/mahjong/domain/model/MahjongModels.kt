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

package com.wanbaohe.mahjong.domain.model

enum class TileSuit { CHARACTERS, BAMBOO, DOTS, WINDS, DRAGONS }

object Tiles {
    const val KIND_COUNT = 34
    const val TILE_COUNT = 136
    const val HUMAN_SEAT = 0
    const val PLAYER_COUNT = 4

    fun fullWall(): List<Int> = (0 until TILE_COUNT).toList()

    fun kindOf(tileId: Int): Int = tileId / 4

    fun suitOfKind(kind: Int): TileSuit = when (kind) {
        in 0..8 -> TileSuit.CHARACTERS
        in 9..17 -> TileSuit.BAMBOO
        in 18..26 -> TileSuit.DOTS
        in 27..30 -> TileSuit.WINDS
        else -> TileSuit.DRAGONS
    }

    fun rankOfKind(kind: Int): Int = if (kind in 0..26) kind % 9 + 1 else 0

    fun isNumeric(kind: Int): Boolean = kind in 0..26

    fun sort(tileIds: List<Int>): List<Int> =
        tileIds.sortedWith(compareBy({ kindOf(it) }, { it }))
}

enum class MeldType { CHI, PONG, KONG }

data class Meld(
    val type: MeldType,
    val tileIds: List<Int>,
    val fromSeat: Int,
    val concealed: Boolean = false,
)

data class PlayerState(
    val hand: List<Int>,
    val melds: List<Meld> = emptyList(),
    val discards: List<Int> = emptyList(),
    val score: Int = 1000,
)

enum class GamePhase { AWAIT_DISCARD, AWAIT_CLAIM, FINISHED }

enum class ClaimSource { DISCARD, ADDED_KONG }

enum class ClaimType { PASS, HU, PONG, KONG, CHI }

data class ClaimChoice(
    val type: ClaimType,
    val chiStart: Int = -1,
)

data class PendingClaim(
    val sourceSeat: Int,
    val tileId: Int,
    val source: ClaimSource,
    val responders: Set<Int>,
    val choices: Map<Int, ClaimChoice> = emptyMap(),
    val addedMeldIndex: Int = -1,
)

enum class FanType {
    BASE,
    SEVEN_PAIRS,
    ALL_PUNGS,
    ONE_SUIT,
    SELF_DRAW,
    ROB_KONG,
}

data class WinResult(
    val seat: Int,
    val fans: List<FanType>,
    val points: Int,
)

data class RoundResult(
    val wins: List<WinResult> = emptyList(),
    val isDraw: Boolean = false,
    val winningTileId: Int? = null,
    val sourceSeat: Int? = null,
    val isSelfDraw: Boolean = false,
    val isRobbingKong: Boolean = false,
    val scoreChanges: List<Int> = List(Tiles.PLAYER_COUNT) { 0 },
)

data class MahjongState(
    val players: List<PlayerState>,
    val wall: List<Int>,
    val dealerSeat: Int,
    val currentSeat: Int,
    val phase: GamePhase,
    val drawnTileId: Int? = null,
    val pending: PendingClaim? = null,
    val result: RoundResult? = null,
    val revision: Long = 0,
    val seed: Long = 0,
)

sealed interface MahjongAction {
    val seat: Int

    data class Discard(override val seat: Int, val tileId: Int) : MahjongAction
    data class SelfHu(override val seat: Int) : MahjongAction
    data class ConcealedKong(override val seat: Int, val kind: Int) : MahjongAction
    data class AddedKong(override val seat: Int, val meldIndex: Int) : MahjongAction
    data class Respond(override val seat: Int, val choice: ClaimChoice) : MahjongAction
}

sealed interface GameEvent {
    data class Discarded(val seat: Int, val tileId: Int) : GameEvent
    data class MeldFormed(val seat: Int, val meld: Meld) : GameEvent
    data class KongDeclared(val seat: Int, val kind: Int) : GameEvent
    data class RoundEnded(val result: RoundResult) : GameEvent
}

data class MahjongTransition(
    val state: MahjongState,
    val events: List<GameEvent> = emptyList(),
)
