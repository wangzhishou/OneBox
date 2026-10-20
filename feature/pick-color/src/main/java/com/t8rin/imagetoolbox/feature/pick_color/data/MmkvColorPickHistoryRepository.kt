package com.t8rin.imagetoolbox.feature.pick_color.data

import com.shifenmiao.model.color.ColorPickHistoryEntry
import com.shifenmiao.storage.ColorPickHistoryStore
import com.t8rin.imagetoolbox.feature.pick_color.domain.ColorPickHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MmkvColorPickHistoryRepository @Inject constructor() : ColorPickHistoryRepository {

    override fun load(): List<ColorPickHistoryEntry> = ColorPickHistoryStore.loadHistory()

    override fun record(argb: Int): List<ColorPickHistoryEntry> =
        ColorPickHistoryStore.record(argb)

    override fun setFavorite(
        argb: Int,
        isFavorite: Boolean
    ): List<ColorPickHistoryEntry> = ColorPickHistoryStore.setFavorite(
        argb = argb,
        isFavorite = isFavorite
    )

    override fun remove(argb: Int): List<ColorPickHistoryEntry> =
        ColorPickHistoryStore.remove(argb)

    override fun clear() {
        ColorPickHistoryStore.clear()
    }
}
