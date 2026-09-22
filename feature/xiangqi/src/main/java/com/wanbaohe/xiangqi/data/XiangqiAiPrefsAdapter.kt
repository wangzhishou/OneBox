package com.wanbaohe.xiangqi.data

import android.content.Context
import android.content.SharedPreferences
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiConfig
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiStore
import com.wanbaohe.xiangqi.application.port.outbound.storageKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XiangqiAiPrefsAdapter @Inject constructor(
    @ApplicationContext context: Context,
) : XiangqiAiStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun observe(): Flow<XiangqiAiConfig> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(load()) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(load())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun get(): XiangqiAiConfig = load()

    override suspend fun update(config: XiangqiAiConfig) {
        prefs.edit()
            .putString(KEY_FAST, config.fastSource.storageKey())
            .putString(KEY_DUEL_A, config.duelASource.storageKey())
            .putString(KEY_DUEL_B, config.duelBSource.storageKey())
            .apply()
    }

    private fun load(): XiangqiAiConfig = XiangqiAiConfig(
        fastSource = XiangqiAiSource.fromKey(prefs.getString(KEY_FAST, null)),
        duelASource = XiangqiAiSource.fromKey(prefs.getString(KEY_DUEL_A, null)),
        duelBSource = XiangqiAiSource.fromKey(prefs.getString(KEY_DUEL_B, null)),
    )

    companion object {
        private const val PREFS_NAME = "xiangqi_ai_source"
        private const val KEY_FAST = "fast_source"
        private const val KEY_DUEL_A = "duel_a_source"
        private const val KEY_DUEL_B = "duel_b_source"
    }
}
