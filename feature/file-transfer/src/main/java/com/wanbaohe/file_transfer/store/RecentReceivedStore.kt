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

package com.wanbaohe.file_transfer.store

import android.content.Context
import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.file_transfer.model.ReceivedFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * "最近接收"本地存储（SharedPreferences + JSON）。
 *
 * 说明：没有落到 FeatureDatabase，是因为这张表只服务文件传输一个模块，
 * 走 Room 需要动共享库 schema/migration，代价大于收益。
 */
object RecentReceivedStore {

    private const val PREFS_NAME = "file_transfer_received"
    private const val KEY_ITEMS = "items"
    private const val MAX_ITEMS = 30

    private val _items = MutableStateFlow<List<ReceivedFile>>(emptyList())
    val items: StateFlow<List<ReceivedFile>> = _items.asStateFlow()

    @Volatile
    private var loaded = false

    fun init(context: Context) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            loaded = true
            _items.value = read(context.applicationContext)
        }
    }

    fun add(context: Context, file: ReceivedFile) {
        val appContext = context.applicationContext
        init(appContext)

        val next = (listOf(file) + _items.value.filterNot { it.id == file.id }).take(MAX_ITEMS)
        _items.value = next
        write(appContext, next)
    }

    fun clear(context: Context) {
        val appContext = context.applicationContext
        init(appContext)
        _items.value = emptyList()
        write(appContext, emptyList())
    }

    private fun read(context: Context): List<ReceivedFile> = runCatching {
        val raw = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ITEMS, null)

        if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            AppJson.decodeFromString<List<ReceivedFile>>(raw)
        }
    }.getOrDefault(emptyList())

    private fun write(context: Context, list: List<ReceivedFile>) {
        runCatching {
            context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEMS, AppJson.encodeToString(list))
                .apply()
        }
    }
}
