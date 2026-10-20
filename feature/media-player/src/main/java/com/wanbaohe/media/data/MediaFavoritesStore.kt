package com.wanbaohe.media.data

import android.content.Context
import com.wanbaohe.media.domain.MediaEntry
import com.wanbaohe.media.domain.MediaFavoritesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 收藏集合的本地存储。
 *
 * 收藏是"这个文件我常听/常看"，键用 URI，跟着文件走而不是跟着记录行走；
 * 同时记下收藏时的显示名——文件从最近列表里被挤掉之后，收藏页仍然能显示一条可点的条目。
 */
@Singleton
class MediaFavoritesStore @Inject constructor(
    @ApplicationContext context: Context,
) : MediaFavoritesRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _favorites = MutableStateFlow(
        prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet().orEmpty()
    )
    override val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _favoriteNames = MutableStateFlow(readNames())
    override val favoriteNames: StateFlow<Map<String, String>> = _favoriteNames.asStateFlow()

    override fun toggle(entry: MediaEntry) {
        val uri = entry.uri
        if (uri.isBlank()) return
        val next = _favorites.value.toMutableSet().apply {
            if (!add(uri)) remove(uri)
        }
        _favorites.value = next

        val names = _favoriteNames.value.toMutableMap().apply {
            if (uri in next) put(uri, entry.displayName) else remove(uri)
        }
        _favoriteNames.value = names

        prefs.edit()
            .putStringSet(KEY_FAVORITES, next)
            .putString(KEY_NAMES, encodeNames(names))
            .apply()
    }

    private fun readNames(): Map<String, String> = runCatching {
        val raw = prefs.getString(KEY_NAMES, null) ?: return@runCatching emptyMap()
        val json = JSONObject(raw)
        buildMap {
            json.keys().forEach { key ->
                put(key, json.optString(key))
            }
        }
    }.getOrDefault(emptyMap())

    private fun encodeNames(names: Map<String, String>): String =
        JSONObject(names as Map<*, *>).toString()

    private companion object {
        const val PREFS_NAME = "media_player_prefs"
        const val KEY_FAVORITES = "favorites"
        const val KEY_NAMES = "favorite_names"
    }
}
