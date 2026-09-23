package com.wanbaohe.gomoku.data

import android.content.Context
import android.content.SharedPreferences
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiConfig
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiStore
import com.wanbaohe.gomoku.application.port.outbound.storageKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GomokuAiPrefsAdapter @Inject constructor(
    @ApplicationContext context: Context,
) : GomokuAiStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun observe(): Flow<GomokuAiConfig> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(load()) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(load())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun get(): GomokuAiConfig = load()

    override suspend fun update(config: GomokuAiConfig) {
        prefs.edit()
            .putString(KEY_FAST, config.fastSource.storageKey())
            .putString(KEY_DUEL_A, config.duelASource.storageKey())
            .putString(KEY_DUEL_B, config.duelBSource.storageKey())
            .apply()
    }

    private fun load(): GomokuAiConfig = GomokuAiConfig(
        fastSource = GomokuAiSource.fromKey(prefs.getString(KEY_FAST, null)),
        duelASource = GomokuAiSource.fromKey(prefs.getString(KEY_DUEL_A, null)),
        duelBSource = GomokuAiSource.fromKey(prefs.getString(KEY_DUEL_B, null)),
    )

    companion object {
        private const val PREFS_NAME = "gomoku_ai_source"
        private const val KEY_FAST = "fast_source"
        private const val KEY_DUEL_A = "duel_a_source"
        private const val KEY_DUEL_B = "duel_b_source"
    }
}
