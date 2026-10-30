package com.wanbaohe.mahjong.data.local

import com.tencent.mmkv.MMKV
import com.wanbaohe.mahjong.application.port.outbound.MahjongStats
import com.wanbaohe.mahjong.application.port.outbound.MahjongStore
import com.wanbaohe.mahjong.domain.model.GamePhase
import com.wanbaohe.mahjong.domain.model.MahjongState
import com.wanbaohe.mahjong.domain.rules.MahjongStateValidator
import javax.inject.Inject

class MmkvMahjongStore @Inject constructor() : MahjongStore {
    private val mmkv: MMKV = MMKV.mmkvWithID("mahjong")

    override fun saveSnapshot(state: MahjongState) {
        if (state.phase == GamePhase.FINISHED) {
            clearSnapshot()
            return
        }
        mmkv.encode(KEY_SNAPSHOT, MahjongSnapshotCodec.encode(state))
    }

    override fun loadSnapshot(): MahjongState? {
        val json = mmkv.decodeString(KEY_SNAPSHOT) ?: return null
        val state = runCatching { MahjongSnapshotCodec.decode(json) }.getOrNull()
        if (state == null || state.phase == GamePhase.FINISHED || !MahjongStateValidator.isValid(state)) {
            clearSnapshot()
            return null
        }
        return state
    }

    override fun clearSnapshot() {
        mmkv.remove(KEY_SNAPSHOT)
    }

    override fun loadStats(): MahjongStats = MahjongStats(
        games = mmkv.decodeInt(KEY_GAMES, 0),
        wins = mmkv.decodeInt(KEY_WINS, 0),
        draws = mmkv.decodeInt(KEY_DRAWS, 0),
        bestScore = mmkv.decodeInt(KEY_BEST_SCORE, 0),
    )

    override fun saveStats(stats: MahjongStats) {
        mmkv.encode(KEY_GAMES, stats.games)
        mmkv.encode(KEY_WINS, stats.wins)
        mmkv.encode(KEY_DRAWS, stats.draws)
        mmkv.encode(KEY_BEST_SCORE, stats.bestScore)
    }

    private companion object {
        const val KEY_SNAPSHOT = "snapshot_v1"
        const val KEY_GAMES = "stats_games"
        const val KEY_WINS = "stats_wins"
        const val KEY_DRAWS = "stats_draws"
        const val KEY_BEST_SCORE = "stats_best_score"
    }
}
