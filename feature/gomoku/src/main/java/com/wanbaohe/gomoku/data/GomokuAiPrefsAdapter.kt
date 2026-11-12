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
