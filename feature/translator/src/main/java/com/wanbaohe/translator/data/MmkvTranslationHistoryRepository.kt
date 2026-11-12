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

package com.wanbaohe.translator.data

import com.tencent.mmkv.MMKV
import com.wanbaohe.translator.domain.TranslationHistoryRepository
import com.wanbaohe.translator.domain.TranslationLanguage
import com.wanbaohe.translator.domain.TranslationRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MmkvTranslationHistoryRepository @Inject constructor() : TranslationHistoryRepository {
    private val mmkv: MMKV by lazy { MMKV.mmkvWithID(STORAGE_ID) }
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val lock = Any()
    private val _records = MutableStateFlow(loadRecords())
    override val records: StateFlow<List<TranslationRecord>> = _records.asStateFlow()

    override suspend fun save(
        sourceText: String,
        translatedText: String,
        sourceLanguage: TranslationLanguage,
        targetLanguage: TranslationLanguage,
        modelName: String,
    ): TranslationRecord {
        val record = TranslationRecord(
            id = UUID.randomUUID().toString(),
            sourceText = sourceText,
            translatedText = translatedText,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            modelName = modelName,
            createdAt = System.currentTimeMillis(),
        )
        synchronized(lock) {
            saveRecords(buildList {
                add(record)
                addAll(loadRecords())
            }.take(MAX_HISTORY))
        }
        return record
    }

    override suspend fun toggleFavorite(id: String) {
        synchronized(lock) {
            saveRecords(loadRecords().map { record ->
                if (record.id == id) record.copy(isFavorite = !record.isFavorite) else record
            })
        }
    }

    override suspend fun delete(id: String) {
        synchronized(lock) {
            saveRecords(loadRecords().filterNot { it.id == id })
        }
    }

    override suspend fun clear() {
        synchronized(lock) { saveRecords(emptyList()) }
    }

    private fun loadRecords(): List<TranslationRecord> {
        val raw = mmkv.decodeString(KEY_HISTORY).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<TranslationRecord>>(raw)
                .filter { it.id.isNotBlank() && it.sourceText.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun saveRecords(records: List<TranslationRecord>) {
        if (records.isEmpty()) {
            mmkv.removeValueForKey(KEY_HISTORY)
        } else {
            mmkv.encode(KEY_HISTORY, json.encodeToString(records))
        }
        _records.value = records
    }

    private companion object {
        const val STORAGE_ID = "translator"
        const val KEY_HISTORY = "history_v1"
        const val MAX_HISTORY = 200
    }
}
