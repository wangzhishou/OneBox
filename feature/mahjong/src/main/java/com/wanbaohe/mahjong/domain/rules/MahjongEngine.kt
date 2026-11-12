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

import com.wanbaohe.mahjong.domain.model.ClaimChoice
import com.wanbaohe.mahjong.domain.model.ClaimSource
import com.wanbaohe.mahjong.domain.model.ClaimType
import com.wanbaohe.mahjong.domain.model.GameEvent
import com.wanbaohe.mahjong.domain.model.GamePhase
import com.wanbaohe.mahjong.domain.model.MahjongAction
import com.wanbaohe.mahjong.domain.model.MahjongState
import com.wanbaohe.mahjong.domain.model.MahjongTransition
import com.wanbaohe.mahjong.domain.model.Meld
import com.wanbaohe.mahjong.domain.model.MeldType
import com.wanbaohe.mahjong.domain.model.PendingClaim
import com.wanbaohe.mahjong.domain.model.PlayerState
import com.wanbaohe.mahjong.domain.model.RoundResult
import com.wanbaohe.mahjong.domain.model.Tiles
import com.wanbaohe.mahjong.domain.model.WinResult
import kotlin.random.Random

object MahjongEngine {

    fun newRound(seed: Long): MahjongState {
        val wall = Tiles.fullWall().shuffled(Random(seed))
        val dealer = ((seed ushr 1) % Tiles.PLAYER_COUNT).toInt()
        val players = List(Tiles.PLAYER_COUNT) { seat ->
            PlayerState(hand = Tiles.sort(wall.subList(seat * 13, seat * 13 + 13)))
        }
        val drawn = wall[52]
        val state = MahjongState(
            players = players,
            wall = wall.drop(53),
            dealerSeat = dealer,
            currentSeat = dealer,
            phase = GamePhase.AWAIT_DISCARD,
            drawnTileId = drawn,
            seed = seed,
        ).updatePlayer(dealer) { it.copy(hand = Tiles.sort(it.hand + drawn)) }
        return state
    }

    fun legalActions(state: MahjongState, seat: Int): List<MahjongAction> {
        if (seat !in 0 until Tiles.PLAYER_COUNT || state.phase == GamePhase.FINISHED) {
            return emptyList()
        }
        return when (state.phase) {
            GamePhase.AWAIT_DISCARD -> turnActions(state, seat)
            GamePhase.AWAIT_CLAIM -> claimActions(state, seat)
            GamePhase.FINISHED -> emptyList()
        }
    }

    fun reduce(state: MahjongState, action: MahjongAction): MahjongTransition? {
        if (action !in legalActions(state, action.seat)) return null
        return when (action) {
            is MahjongAction.Discard -> discard(state, action)
            is MahjongAction.SelfHu -> finishWins(
                state = state,
                winners = listOf(action.seat),
                sourceSeat = null,
                winningTileId = requireNotNull(state.drawnTileId),
                isSelfDraw = true,
                isRobbingKong = false,
            )

            is MahjongAction.ConcealedKong -> concealedKong(state, action)
            is MahjongAction.AddedKong -> beginAddedKong(state, action)
            is MahjongAction.Respond -> respond(state, action)
        }
    }

    private fun turnActions(state: MahjongState, seat: Int): List<MahjongAction> {
        if (seat != state.currentSeat) return emptyList()
        val player = state.players[seat]
        val actions = player.hand.map { MahjongAction.Discard(seat, it) }.toMutableList<MahjongAction>()
        if (state.drawnTileId != null && WinDetector.isWinningHand(player.hand, player.melds)) {
            actions += MahjongAction.SelfHu(seat)
        }
        player.hand.groupingBy(Tiles::kindOf).eachCount()
            .filterValues { it == 4 }
            .keys
            .forEach { actions += MahjongAction.ConcealedKong(seat, it) }
        player.melds.forEachIndexed { index, meld ->
            if (meld.type != MeldType.PONG) return@forEachIndexed
            val kind = Tiles.kindOf(meld.tileIds.first())
            if (player.hand.any { Tiles.kindOf(it) == kind }) {
                actions += MahjongAction.AddedKong(seat, index)
            }
        }
        return actions
    }

    private fun claimActions(state: MahjongState, seat: Int): List<MahjongAction> {
        val pending = state.pending ?: return emptyList()
        if (seat !in pending.responders || seat in pending.choices) return emptyList()
        return positiveClaimChoices(state, pending, seat).map {
            MahjongAction.Respond(seat, it)
        } + MahjongAction.Respond(seat, ClaimChoice(ClaimType.PASS))
    }

    private fun positiveClaimChoices(
        state: MahjongState,
        pending: PendingClaim,
        seat: Int,
    ): List<ClaimChoice> {
        if (seat == pending.sourceSeat) return emptyList()
        val player = state.players[seat]
        val claimedKind = Tiles.kindOf(pending.tileId)
        val choices = mutableListOf<ClaimChoice>()
        if (WinDetector.isWinningHand(player.hand + pending.tileId, player.melds)) {
            choices += ClaimChoice(ClaimType.HU)
        }
        if (pending.source == ClaimSource.ADDED_KONG) return choices

        val sameKindCount = player.hand.count { Tiles.kindOf(it) == claimedKind }
        if (sameKindCount >= 2) choices += ClaimChoice(ClaimType.PONG)
        if (sameKindCount >= 3) choices += ClaimChoice(ClaimType.KONG)
        if (seat == (pending.sourceSeat + 1) % Tiles.PLAYER_COUNT) {
            chiStarts(player.hand, claimedKind).forEach {
                choices += ClaimChoice(ClaimType.CHI, chiStart = it)
            }
        }
        return choices
    }

    private fun discard(state: MahjongState, action: MahjongAction.Discard): MahjongTransition {
        val discardedState = state.updatePlayer(action.seat) {
            it.copy(
                hand = it.hand.removeOne(action.tileId),
                discards = it.discards + action.tileId,
            )
        }.copy(
            drawnTileId = null,
            phase = GamePhase.AWAIT_CLAIM,
            revision = state.revision + 1,
        )
        val pending = PendingClaim(
            sourceSeat = action.seat,
            tileId = action.tileId,
            source = ClaimSource.DISCARD,
            responders = Tiles.PLAYER_COUNT.let { count ->
                (0 until count).filterTo(mutableSetOf()) { seat ->
                    positiveClaimChoices(discardedState,
                        PendingClaim(action.seat, action.tileId, ClaimSource.DISCARD, emptySet()),
                        seat,
                    ).isNotEmpty()
                }
            },
        )
        val event = GameEvent.Discarded(action.seat, action.tileId)
        if (pending.responders.isEmpty()) {
            return drawTile(
                discardedState.copy(pending = null),
                (action.seat + 1) % Tiles.PLAYER_COUNT,
                extraEvents = listOf(event),
            )
        }
        return MahjongTransition(discardedState.copy(pending = pending), listOf(event))
    }

    private fun respond(state: MahjongState, action: MahjongAction.Respond): MahjongTransition {
        val pending = requireNotNull(state.pending)
        val updated = pending.copy(choices = pending.choices + (action.seat to action.choice))
        val responded = state.copy(pending = updated, revision = state.revision + 1)
        if (updated.choices.size < updated.responders.size) {
            return MahjongTransition(responded)
        }
        return resolveClaim(responded)
    }

    private fun resolveClaim(state: MahjongState): MahjongTransition {
        val pending = requireNotNull(state.pending)
        val winners = pending.choices.filterValues { it.type == ClaimType.HU }.keys.sorted()
        if (winners.isNotEmpty()) {
            return finishWins(
                state = state,
                winners = winners,
                sourceSeat = pending.sourceSeat,
                winningTileId = pending.tileId,
                isSelfDraw = false,
                isRobbingKong = pending.source == ClaimSource.ADDED_KONG,
            )
        }
        if (pending.source == ClaimSource.ADDED_KONG) {
            return completeAddedKong(state)
        }

        val kong = pending.choices.entries.firstOrNull { it.value.type == ClaimType.KONG }
        val pong = pending.choices.entries.firstOrNull { it.value.type == ClaimType.PONG }
        val chi = pending.choices.entries.firstOrNull { it.value.type == ClaimType.CHI }
        return when {
            kong != null -> applyDiscardClaim(state, kong.key, kong.value)
            pong != null -> applyDiscardClaim(state, pong.key, pong.value)
            chi != null -> applyDiscardClaim(state, chi.key, chi.value)
            else -> drawTile(
                state.copy(pending = null),
                (pending.sourceSeat + 1) % Tiles.PLAYER_COUNT,
            )
        }
    }

    private fun applyDiscardClaim(
        state: MahjongState,
        seat: Int,
        choice: ClaimChoice,
    ): MahjongTransition {
        val pending = requireNotNull(state.pending)
        val player = state.players[seat]
        val claimedKind = Tiles.kindOf(pending.tileId)
        val consumed = when (choice.type) {
            ClaimType.PONG -> player.hand.takeByKinds(listOf(claimedKind, claimedKind))
            ClaimType.KONG -> player.hand.takeByKinds(
                listOf(claimedKind, claimedKind, claimedKind)
            )
            ClaimType.CHI -> player.hand.takeByKinds(
                listOf(choice.chiStart, choice.chiStart + 1, choice.chiStart + 2)
                    .filter { it != claimedKind }
            )
            else -> return MahjongTransition(state)
        }
        val meld = Meld(
            type = when (choice.type) {
                ClaimType.CHI -> MeldType.CHI
                ClaimType.KONG -> MeldType.KONG
                else -> MeldType.PONG
            },
            tileIds = Tiles.sort(consumed + pending.tileId),
            fromSeat = pending.sourceSeat,
        )
        val claimedState = state
            .updatePlayer(pending.sourceSeat) {
                it.copy(discards = it.discards.removeOne(pending.tileId))
            }
            .updatePlayer(seat) {
                it.copy(
                    hand = it.hand.removeAllOnce(consumed),
                    melds = it.melds + meld,
                )
            }
            .copy(
                currentSeat = seat,
                phase = GamePhase.AWAIT_DISCARD,
                drawnTileId = null,
                pending = null,
            )
        val event = GameEvent.MeldFormed(seat, meld)
        if (meld.type == MeldType.KONG) {
            return drawTile(claimedState, seat, extraEvents = listOf(event))
        }
        return MahjongTransition(claimedState, listOf(event))
    }

    private fun concealedKong(
        state: MahjongState,
        action: MahjongAction.ConcealedKong,
    ): MahjongTransition {
        val player = state.players[action.seat]
        val consumed = player.hand.takeByKinds(List(4) { action.kind })
        val meld = Meld(
            type = MeldType.KONG,
            tileIds = Tiles.sort(consumed),
            fromSeat = action.seat,
            concealed = true,
        )
        val next = state.updatePlayer(action.seat) {
            it.copy(
                hand = it.hand.removeAllOnce(consumed),
                melds = it.melds + meld,
            )
        }
        return drawTile(
            next,
            action.seat,
            extraEvents = listOf(GameEvent.KongDeclared(action.seat, action.kind)),
        )
    }

    private fun beginAddedKong(
        state: MahjongState,
        action: MahjongAction.AddedKong,
    ): MahjongTransition {
        val player = state.players[action.seat]
        val meld = player.melds[action.meldIndex]
        val kind = Tiles.kindOf(meld.tileIds.first())
        val tileId = player.hand.first { Tiles.kindOf(it) == kind }
        val pending = PendingClaim(
            sourceSeat = action.seat,
            tileId = tileId,
            source = ClaimSource.ADDED_KONG,
            responders = (0 until Tiles.PLAYER_COUNT).filterTo(mutableSetOf()) { seat ->
                positiveClaimChoices(
                    state,
                    PendingClaim(
                        action.seat,
                        tileId,
                        ClaimSource.ADDED_KONG,
                        emptySet(),
                        addedMeldIndex = action.meldIndex,
                    ),
                    seat,
                ).isNotEmpty()
            },
            addedMeldIndex = action.meldIndex,
        )
        val next = state.copy(
            phase = GamePhase.AWAIT_CLAIM,
            pending = pending,
            revision = state.revision + 1,
        )
        if (pending.responders.isEmpty()) return completeAddedKong(next)
        return MahjongTransition(next)
    }

    private fun completeAddedKong(state: MahjongState): MahjongTransition {
        val pending = requireNotNull(state.pending)
        val player = state.players[pending.sourceSeat]
        val oldMeld = player.melds[pending.addedMeldIndex]
        val meld = oldMeld.copy(
            type = MeldType.KONG,
            tileIds = Tiles.sort(oldMeld.tileIds + pending.tileId),
        )
        val next = state.updatePlayer(pending.sourceSeat) {
            it.copy(
                hand = it.hand.removeOne(pending.tileId),
                melds = it.melds.mapIndexed { index, item ->
                    if (index == pending.addedMeldIndex) meld else item
                },
            )
        }
        return drawTile(
            next,
            pending.sourceSeat,
            extraEvents = listOf(
                GameEvent.KongDeclared(pending.sourceSeat, Tiles.kindOf(pending.tileId))
            ),
        )
    }

    private fun drawTile(
        state: MahjongState,
        seat: Int,
        extraEvents: List<GameEvent> = emptyList(),
    ): MahjongTransition {
        if (state.wall.isEmpty()) return finishDraw(state, extraEvents)
        val tileId = state.wall.last()
        val next = state.copy(
            wall = state.wall.dropLast(1),
            currentSeat = seat,
            phase = GamePhase.AWAIT_DISCARD,
            drawnTileId = tileId,
            pending = null,
        ).updatePlayer(seat) { it.copy(hand = Tiles.sort(it.hand + tileId)) }
        return MahjongTransition(next, extraEvents)
    }

    private fun finishWins(
        state: MahjongState,
        winners: List<Int>,
        sourceSeat: Int?,
        winningTileId: Int,
        isSelfDraw: Boolean,
        isRobbingKong: Boolean,
    ): MahjongTransition {
        val scoreChanges = MutableList(Tiles.PLAYER_COUNT) { 0 }
        val wins = winners.map { seat ->
            val player = state.players[seat]
            val winningHand = if (isSelfDraw) player.hand else player.hand + winningTileId
            val fans = WinDetector.evaluate(
                hand = winningHand,
                melds = player.melds,
                isSelfDraw = isSelfDraw,
                isRobbingKong = isRobbingKong,
            )
            val points = WinDetector.pointsFor(fans)
            if (isSelfDraw) {
                scoreChanges[seat] += points * 3
                (0 until Tiles.PLAYER_COUNT).filter { it != seat }.forEach {
                    scoreChanges[it] -= points
                }
            } else {
                scoreChanges[seat] += points
                scoreChanges[requireNotNull(sourceSeat)] -= points
            }
            WinResult(seat, fans, points)
        }
        val result = RoundResult(
            wins = wins,
            winningTileId = winningTileId,
            sourceSeat = sourceSeat,
            isSelfDraw = isSelfDraw,
            isRobbingKong = isRobbingKong,
            scoreChanges = scoreChanges,
        )
        val next = state.copy(
            players = state.players.mapIndexed { index, player ->
                player.copy(score = player.score + scoreChanges[index])
            },
            phase = GamePhase.FINISHED,
            pending = null,
            result = result,
            revision = state.revision + 1,
        )
        return MahjongTransition(next, listOf(GameEvent.RoundEnded(result)))
    }

    private fun finishDraw(state: MahjongState, events: List<GameEvent>): MahjongTransition {
        val result = RoundResult(isDraw = true)
        val next = state.copy(
            phase = GamePhase.FINISHED,
            pending = null,
            result = result,
            revision = state.revision + 1,
        )
        return MahjongTransition(next, events + GameEvent.RoundEnded(result))
    }

    private fun chiStarts(hand: List<Int>, claimedKind: Int): List<Int> {
        if (!Tiles.isNumeric(claimedKind)) return emptyList()
        val kinds = hand.map(Tiles::kindOf).toSet()
        return (claimedKind - 2..claimedKind).filter { start ->
            Tiles.isNumeric(start) &&
                Tiles.rankOfKind(start) in 1..7 &&
                Tiles.suitOfKind(start) == Tiles.suitOfKind(claimedKind) &&
                (start..start + 2).filter { it != claimedKind }.all(kinds::contains)
        }
    }
}

private fun MahjongState.updatePlayer(seat: Int, update: (PlayerState) -> PlayerState): MahjongState =
    copy(players = players.mapIndexed { index, player -> if (index == seat) update(player) else player })

private fun List<Int>.removeOne(value: Int): List<Int> {
    val index = indexOf(value)
    if (index < 0) return this
    return toMutableList().apply { removeAt(index) }
}

private fun List<Int>.takeByKinds(kinds: List<Int>): List<Int> {
    val remaining = toMutableList()
    return kinds.map { kind ->
        val index = remaining.indexOfFirst { Tiles.kindOf(it) == kind }
        require(index >= 0) { "Missing tile kind $kind" }
        remaining.removeAt(index)
    }
}

private fun List<Int>.removeAllOnce(values: List<Int>): List<Int> {
    val remaining = toMutableList()
    values.forEach {
        val index = remaining.indexOf(it)
        require(index >= 0) { "Missing tile $it" }
        remaining.removeAt(index)
    }
    return remaining
}
