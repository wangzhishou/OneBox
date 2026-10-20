package com.t8rin.imagetoolbox.feature.pick_color.domain

import com.shifenmiao.model.color.ColorPickHistoryEntry

/**
 * 取色历史的读写契约。
 *
 * 变更类操作统一返回变更后的完整列表，调用方（Component）据此刷新状态，
 * 不再自己拼一份“预期结果”，避免存储与 UI 两份状态漂移。
 */
interface ColorPickHistoryRepository {
    fun load(): List<ColorPickHistoryEntry>

    fun record(argb: Int): List<ColorPickHistoryEntry>

    fun setFavorite(
        argb: Int,
        isFavorite: Boolean
    ): List<ColorPickHistoryEntry>

    fun remove(argb: Int): List<ColorPickHistoryEntry>

    fun clear()
}
