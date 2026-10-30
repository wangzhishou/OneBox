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
