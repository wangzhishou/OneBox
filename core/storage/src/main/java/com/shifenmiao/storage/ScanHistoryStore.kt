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

import com.shifenmiao.model.scan.ScanHistoryEntry
import com.tencent.mmkv.MMKV
import kotlinx.serialization.json.Json

object ScanHistoryStore {

    private const val KEY_HISTORY = "scan_history_list"
    private const val KEY_LAST = "scan_history_last"
    private const val MAX_SIZE = 20

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.SCAN_HISTORY)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun loadHistory(): List<ScanHistoryEntry> {
        val raw = mmkv.decodeString(KEY_HISTORY).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<ScanHistoryEntry>>(raw)
        }.getOrDefault(emptyList())
    }

    fun loadLast(): ScanHistoryEntry? {
        val raw = mmkv.decodeString(KEY_LAST).orEmpty()
        if (raw.isBlank()) return null
        return runCatching {
            json.decodeFromString<ScanHistoryEntry>(raw)
        }.getOrNull()
    }

    fun save(raw: String): ScanHistoryEntry {
        val entry = ScanHistoryEntry(
            raw = raw,
            scannedAtMillis = System.currentTimeMillis()
        )
        val updatedHistory = buildList {
            add(entry)
            addAll(loadHistory().filterNot { it.raw == raw })
        }.take(MAX_SIZE)

        mmkv.encode(KEY_HISTORY, json.encodeToString(updatedHistory))
        mmkv.encode(KEY_LAST, json.encodeToString(entry))
        return entry
    }

    fun remove(raw: String) {
        val updatedHistory = loadHistory().filterNot { it.raw == raw }
        mmkv.encode(KEY_HISTORY, json.encodeToString(updatedHistory))
        val last = updatedHistory.firstOrNull()
        if (last == null) {
            mmkv.removeValueForKey(KEY_LAST)
        } else {
            mmkv.encode(KEY_LAST, json.encodeToString(last))
        }
    }

    fun clear() {
        mmkv.removeValuesForKeys(arrayOf(KEY_HISTORY, KEY_LAST))
    }
}

