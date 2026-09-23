package com.wanbaohe.chess.data

import android.content.Context
import android.content.SharedPreferences
import com.wanbaohe.chess.application.port.outbound.ChessAiConfig
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.application.port.outbound.ChessAiStore
import com.wanbaohe.chess.application.port.outbound.storageKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChessAiPrefsAdapter @Inject constructor(
    @ApplicationContext context: Context,
) : ChessAiStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun observe(): Flow<ChessAiConfig> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(load()) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(load())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun get(): ChessAiConfig = load()

    override suspend fun update(config: ChessAiConfig) {
        prefs.edit()
            .putString(KEY_FAST, config.fastSource.storageKey())
            .putString(KEY_DUEL_A, config.duelASource.storageKey())
            .putString(KEY_DUEL_B, config.duelBSource.storageKey())
            .apply()
    }

    private fun load(): ChessAiConfig = ChessAiConfig(
        fastSource = ChessAiSource.fromKey(prefs.getString(KEY_FAST, null)),
        duelASource = ChessAiSource.fromKey(prefs.getString(KEY_DUEL_A, null)),
        duelBSource = ChessAiSource.fromKey(prefs.getString(KEY_DUEL_B, null)),
    )

    companion object {
        private const val PREFS_NAME = "chess_ai_source"
        private const val KEY_FAST = "fast_source"
        private const val KEY_DUEL_A = "duel_a_source"
        private const val KEY_DUEL_B = "duel_b_source"
    }
}
