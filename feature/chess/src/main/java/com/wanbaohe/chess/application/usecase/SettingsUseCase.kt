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

package com.wanbaohe.chess.application.usecase

import com.wanbaohe.chess.application.port.outbound.AudioSettings
import com.wanbaohe.chess.application.port.outbound.SettingsStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsUseCase @Inject constructor(
    private val store: SettingsStore,
) {

    fun observe(): Flow<AudioSettings> = store.observe()

    suspend fun current(): AudioSettings = store.get()

    suspend fun updateMoveSoundUrl(url: String) {
        store.update(store.get().copy(moveSoundUrl = url.trim()))
    }

    suspend fun updateBackgroundMusicUrl(url: String) {
        store.update(store.get().copy(backgroundMusicUrl = url.trim()))
    }

    suspend fun updateCheckSoundUrl(url: String) {
        store.update(store.get().copy(checkSoundUrl = url.trim()))
    }

    suspend fun updateTTSEnabled(enabled: Boolean) {
        store.update(store.get().copy(ttsEnabled = enabled))
    }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        store.update(store.get().copy(soundEnabled = enabled))
    }

    suspend fun updateTTSTemplateText(tag: String, text: String) {
        val current = store.get()
        val next = current.ttsTemplateTexts.toMutableMap()
        if (text.isBlank()) next.remove(tag) else next[tag] = text.trim()
        store.update(current.copy(ttsTemplateTexts = next))
    }
}
