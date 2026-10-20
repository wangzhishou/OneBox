package com.shifenmiao.storage

import com.shifenmiao.model.color.ColorPickHistoryEntry
import com.tencent.mmkv.MMKV
import kotlinx.serialization.json.Json

/**
 * 图片取色历史（MMKV 持久化）。
 *
 * 记录按取色时间倒序保存；同色只保留一条，再次取到会把它顶到最前面并刷新时间。
 * 收藏是记录上的标记：没有记录的颜色被收藏时会补一条记录，保证收藏不会丢。
 */
object ColorPickHistoryStore {

    private const val KEY_HISTORY = "color_pick_history_list"
    private const val MAX_SIZE = 60

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.COLOR_PICK_HISTORY)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun loadHistory(): List<ColorPickHistoryEntry> {
        val raw = mmkv.decodeString(KEY_HISTORY).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<ColorPickHistoryEntry>>(raw)
        }.getOrDefault(emptyList())
    }

    fun record(argb: Int): List<ColorPickHistoryEntry> {
        val history = loadHistory()
        return upsert(
            history = history,
            argb = argb,
            isFavorite = history.firstOrNull { it.argb == argb }?.isFavorite ?: false
        )
    }

    fun setFavorite(
        argb: Int,
        isFavorite: Boolean
    ): List<ColorPickHistoryEntry> {
        val history = loadHistory()
        if (history.none { it.argb == argb }) {
            return upsert(
                history = history,
                argb = argb,
                isFavorite = isFavorite
            )
        }
        // 只改收藏标记，不动取色时间：否则历史里的时间会被“点爱心”刷成当前时间。
        val updated = history.map {
            if (it.argb == argb) it.copy(isFavorite = isFavorite) else it
        }
        persist(updated)
        return updated
    }

    fun remove(argb: Int): List<ColorPickHistoryEntry> {
        val updated = loadHistory().filterNot { it.argb == argb }
        persist(updated)
        return updated
    }

    fun clear() {
        mmkv.removeValueForKey(KEY_HISTORY)
    }

    private fun upsert(
        history: List<ColorPickHistoryEntry>,
        argb: Int,
        isFavorite: Boolean
    ): List<ColorPickHistoryEntry> {
        val entry = ColorPickHistoryEntry(
            argb = argb,
            pickedAtMillis = System.currentTimeMillis(),
            isFavorite = isFavorite
        )
        val updated = buildList {
            add(entry)
            addAll(history.filterNot { it.argb == argb })
        }.take(MAX_SIZE)
        persist(updated)
        return updated
    }

    private fun persist(history: List<ColorPickHistoryEntry>) {
        mmkv.encode(KEY_HISTORY, json.encodeToString(history))
    }
}
