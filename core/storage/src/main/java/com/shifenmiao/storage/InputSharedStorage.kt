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

package com.shifenmiao.storage

import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object InputSharedStorage {
    private val mmkv: MMKV = MMKV.defaultMMKV()
    private val memoryCache = mutableMapOf<String, String>()

    fun save(key: String, value: String) {
        memoryCache[key] = value
        CoroutineScope(Dispatchers.IO).launch {
            mmkv.encode(key, value)
        }
    }

    fun load(key: String, defaultValue: String? = null): String? {
        memoryCache[key]?.let {
            return it
        }
        return mmkv.decodeString(key, defaultValue).also {
            if (it != null) memoryCache[key] = it
        }
    }

    fun clear(key: String) {
        CoroutineScope(Dispatchers.IO).launch {
            mmkv.remove(key)
        }
        memoryCache.remove(key)
    }
}