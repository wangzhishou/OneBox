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

package com.wanbaohe.ocr.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tencent.mmkv.MMKV
import com.wanbaohe.ocr.domain.OcrHistoryEntry
import com.wanbaohe.ocr.domain.OcrHistoryRepository
import com.wanbaohe.ocr.domain.RecognizedLine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MmkvOcrHistoryRepository @Inject constructor(
    @ApplicationContext context: Context,
) : OcrHistoryRepository {
    private val imageDirectory = File(context.filesDir, IMAGE_DIRECTORY)
    private val mmkv: MMKV by lazy { MMKV.mmkvWithID(STORAGE_ID) }
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val lock = Any()
    private val _records = MutableStateFlow(loadRecords())
    override val records: StateFlow<List<OcrHistoryEntry>> = _records.asStateFlow()

    override suspend fun save(
        bitmap: Bitmap,
        lines: List<RecognizedLine>,
        existingId: String?,
    ): OcrHistoryEntry {
        val existing = _records.value.firstOrNull { it.id == existingId }
        val id = existing?.id ?: UUID.randomUUID().toString()
        val imageFile = existing?.imagePath?.let(::File) ?: File(imageDirectory, "$id.jpg")
        writeImage(bitmap, imageFile)
        val entry = OcrHistoryEntry(
            id = id,
            createdAt = System.currentTimeMillis(),
            imagePath = imageFile.absolutePath,
            lines = lines,
        )
        val removed = synchronized(lock) {
            val updated = buildList {
                add(entry)
                addAll(loadRecords().filterNot { it.id == id })
            }
            saveRecords(updated.take(MAX_HISTORY))
            updated.drop(MAX_HISTORY)
        }
        removed.forEach { record -> File(record.imagePath).delete() }
        return entry
    }

    override suspend fun loadImage(record: OcrHistoryEntry): Bitmap {
        return BitmapFactory.decodeFile(record.imagePath) ?: error("History image decode failed")
    }

    override suspend fun delete(id: String) {
        val removed = synchronized(lock) {
            val current = loadRecords()
            saveRecords(current.filterNot { it.id == id })
            current.filter { it.id == id }
        }
        removed.forEach { record -> File(record.imagePath).delete() }
    }

    override suspend fun clear() {
        val removed = synchronized(lock) {
            val current = loadRecords()
            saveRecords(emptyList())
            current
        }
        removed.forEach { record -> File(record.imagePath).delete() }
    }

    private fun writeImage(bitmap: Bitmap, file: File) {
        file.parentFile?.let { parent ->
            if (!parent.exists() && !parent.mkdirs()) error("Could not create OCR history directory")
        }
        FileOutputStream(file, false).use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                error("Could not save OCR history image")
            }
        }
    }

    private fun loadRecords(): List<OcrHistoryEntry> {
        val raw = mmkv.decodeString(KEY_HISTORY).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<OcrHistoryEntry>>(raw)
                .filter { it.id.isNotBlank() && it.imagePath.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun saveRecords(records: List<OcrHistoryEntry>) {
        if (records.isEmpty()) {
            mmkv.removeValueForKey(KEY_HISTORY)
        } else {
            mmkv.encode(KEY_HISTORY, json.encodeToString(records))
        }
        _records.value = records
    }

    private companion object {
        const val STORAGE_ID = "on_device_ocr"
        const val KEY_HISTORY = "history_v1"
        const val IMAGE_DIRECTORY = "ocr_history"
        const val MAX_HISTORY = 50
        const val JPEG_QUALITY = 90
    }
}
