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

package com.wanbaohe.iching.data

import com.tencent.mmkv.MMKV

/**
 * 六爻音效开关,与起卦记录共用同一个 MMKV 库。
 *
 * 默认开启:摇卦这种强反馈的交互,没声音会明显发闷;不想听的用户一次点击就能关掉。
 */
object IChingSoundSettings {
    private const val STORAGE_ID = "iching_divination"
    private const val KEY_ENABLED = "sound_enabled_v1"

    private val mmkv: MMKV by lazy { MMKV.mmkvWithID(STORAGE_ID) }

    fun isEnabled(): Boolean = runCatching { mmkv.decodeBool(KEY_ENABLED, true) }.getOrDefault(true)

    fun setEnabled(enabled: Boolean) {
        runCatching { mmkv.encode(KEY_ENABLED, enabled) }
    }
}
