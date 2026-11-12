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

package com.wanbaohe.decisionwheel.data

import com.tencent.mmkv.MMKV
import com.wanbaohe.decisionwheel.component.WheelSettings

/**
 * 转盘设置的轻量持久化（MMKV）。
 *
 * 之前这些值存在 Screen 的 remember 里，重建即丢；改为持久化后跨进程重建保留。
 */
object WheelSettingsStorage {

    private const val KEY_DURATION = "spin_duration_millis"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_HAPTICS = "haptics_enabled"
    private const val KEY_REMOVE_ON_SPIN = "remove_on_spin"

    private val mmkv: MMKV get() = MMKV.mmkvWithID("decision_wheel")

    fun load(): WheelSettings = WheelSettings(
        spinDurationMillis = mmkv.decodeInt(KEY_DURATION, 4000).coerceIn(1500, 8000),
        soundEnabled = mmkv.decodeBool(KEY_SOUND, true),
        hapticsEnabled = mmkv.decodeBool(KEY_HAPTICS, true),
        removeOnSpin = mmkv.decodeBool(KEY_REMOVE_ON_SPIN, false)
    )

    fun save(settings: WheelSettings) {
        mmkv.encode(KEY_DURATION, settings.spinDurationMillis.coerceIn(1500, 8000))
        mmkv.encode(KEY_SOUND, settings.soundEnabled)
        mmkv.encode(KEY_HAPTICS, settings.hapticsEnabled)
        mmkv.encode(KEY_REMOVE_ON_SPIN, settings.removeOnSpin)
    }
}
