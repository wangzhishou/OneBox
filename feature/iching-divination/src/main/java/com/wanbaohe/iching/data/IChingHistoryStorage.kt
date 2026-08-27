package com.wanbaohe.iching.data

import com.tencent.mmkv.MMKV
import com.wanbaohe.iching.domain.HexagramGenerator
import com.wanbaohe.iching.model.DivinationResult
import com.wanbaohe.iching.model.HexagramLine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class IChingHistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    /** Six line values in bottom-to-top order. */
    val lineValues: List<Int>,
    val primaryNumber: Int,
    val primaryName: String,
    val changedNumber: Int? = null,
    val changedName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val aiContent: String = "",
) {
    val isValid: Boolean
        get() = id.isNotBlank() && lineValues.size == 6 && lineValues.all { it in 6..9 }

    fun toResult(generator: HexagramGenerator): DivinationResult =
        generator.create(question, lineValues.map(::HexagramLine))

    companion object {
        fun from(result: DivinationResult): IChingHistoryRecord = IChingHistoryRecord(
            question = result.question,
            lineValues = result.lines.map(HexagramLine::value),
            primaryNumber = result.primary.number,
            primaryName = result.primary.name,
            changedNumber = result.changed?.number,
            changedName = result.changed?.name,
        )
    }
}

internal object IChingHistoryCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(records: List<IChingHistoryRecord>): String = json.encodeToString(records)

    fun decode(raw: String): List<IChingHistoryRecord> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<IChingHistoryRecord>>(raw)
                .filter(IChingHistoryRecord::isValid)
        }.getOrDefault(emptyList())
    }
}

internal object IChingHistoryOperations {
    fun append(
        current: List<IChingHistoryRecord>,
        record: IChingHistoryRecord,
        maxSize: Int,
    ): List<IChingHistoryRecord> = buildList {
        add(record)
        addAll(current.filterNot { it.id == record.id })
    }.take(maxSize.coerceAtLeast(0))

    fun updateAIContent(
        current: List<IChingHistoryRecord>,
        id: String,
        content: String,
    ): List<IChingHistoryRecord> = current.map { record ->
        if (record.id == id) record.copy(aiContent = content) else record
    }

    fun remove(current: List<IChingHistoryRecord>, id: String): List<IChingHistoryRecord> =
        current.filterNot { it.id == id }
}

/** Thread-safe, single-process MMKV history storage. */
object IChingHistoryStorage {
    private const val STORAGE_ID = "iching_divination"
    private const val KEY_HISTORY = "history_v1"
    const val MAX_HISTORY = 100

    private val lock = Any()
    private val mmkv: MMKV by lazy { MMKV.mmkvWithID(STORAGE_ID) }

    fun loadHistory(): List<IChingHistoryRecord> = synchronized(lock) {
        loadInternal()
    }

    fun append(record: IChingHistoryRecord): List<IChingHistoryRecord> = synchronized(lock) {
        val updated = IChingHistoryOperations.append(loadInternal(), record, MAX_HISTORY)
        saveInternal(updated)
        updated
    }

    fun updateAIContent(id: String, content: String): List<IChingHistoryRecord> = synchronized(lock) {
        val current = loadInternal()
        val updated = IChingHistoryOperations.updateAIContent(current, id, content)
        if (updated != current) saveInternal(updated)
        updated
    }

    fun remove(id: String): List<IChingHistoryRecord> = synchronized(lock) {
        val updated = IChingHistoryOperations.remove(loadInternal(), id)
        saveInternal(updated)
        updated
    }

    fun clear() = synchronized(lock) {
        mmkv.removeValueForKey(KEY_HISTORY)
    }

    private fun loadInternal(): List<IChingHistoryRecord> =
        IChingHistoryCodec.decode(mmkv.decodeString(KEY_HISTORY).orEmpty())

    private fun saveInternal(records: List<IChingHistoryRecord>) {
        if (records.isEmpty()) mmkv.removeValueForKey(KEY_HISTORY)
        else mmkv.encode(KEY_HISTORY, IChingHistoryCodec.encode(records))
    }
}

