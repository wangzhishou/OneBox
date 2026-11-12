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

package com.wanbaohe.xiangqi.application.port.outbound

import kotlinx.coroutines.flow.Flow

interface SettingsStore {
    fun observe(): Flow<AudioSettings>
    suspend fun get(): AudioSettings
    suspend fun update(settings: AudioSettings)
}

data class AudioSettings(
    val moveSoundUrl: String = "",
    val backgroundMusicUrl: String = "",
    val checkSoundUrl: String = "",
    val ttsEnabled: Boolean = true,
    val ttsTemplateTexts: Map<String, String> = emptyMap(),
    /**
     * 声音总开关（落子音效 + 背景音乐一起管）。
     *
     * 复盘页顶上那个小喇叭就是它：关掉之后整页安静，连 BGM 一起停。
     */
    val soundEnabled: Boolean = true,
)
