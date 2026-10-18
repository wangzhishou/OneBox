package com.wanbaohe.core.weather.data.cache

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.core.weather.domain.model.LatestPlace
import com.wanbaohe.core.weather.domain.model.PlaceWeather
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Serializable
data class CachedWeather(
    // 默认值兜底 Gson 时代缓存(缺 timestamp 时 Gson 给 0,按过期处理)
    val data: WeatherInfo = WeatherInfo(),
    val timestamp: Long = 0L
)

private val Context.weatherDataStore by preferencesDataStore(name = "weather_data_cache")

data class PlaceWeatherEntry(
    val data: PlaceWeather,
    val timestamp: Long,
)

class WeatherDataCache(
    private val context: Context
) {
    private val validDuration = 30 * 60 * 1000L // 30 分钟内视为新鲜，过期仍可展示
    private val latestMutex = Mutex()
    private val latestKey = stringPreferencesKey("latest_place_v1")

    private suspend fun persist(block: suspend () -> Unit) {
        runCatching { block() }.onFailure { error ->
            Log.w(TAG, "weather cache write failed: ${error.message}")
        }
    }

    fun isFresh(timestamp: Long, now: Long = System.currentTimeMillis()): Boolean =
        timestamp > 0L && now - timestamp <= validDuration

    suspend fun getWeather(cityId: String): WeatherInfo? {
        val key = stringPreferencesKey(cityId)
        val json = context.weatherDataStore.data.map { preferences ->
            preferences[key]
        }.first()

        if (json == null) return null

        // 解析失败按无缓存处理(旧 Gson 格式字段名一致,正常可读)
        val cached = runCatching { AppJson.decodeFromString<CachedWeather>(json) }.getOrNull()
            ?: return null

        if (System.currentTimeMillis() - cached.timestamp > validDuration) {
            context.weatherDataStore.edit { preferences ->
                preferences.remove(key)
            }
            return null
        }
        return cached.data
    }

    suspend fun saveWeather(cityId: String, weatherInfo: WeatherInfo) {
        val key = stringPreferencesKey(cityId)
        val cached = CachedWeather(weatherInfo, System.currentTimeMillis())
        val json = AppJson.encodeToString(cached)

        context.weatherDataStore.edit { preferences ->
            preferences[key] = json
        }
    }

    /** 读出该地点的上次结果，过期也不删。解析失败按没有缓存。 */
    suspend fun peekPlaceWeather(cacheKey: String): PlaceWeatherEntry? {
        val key = stringPreferencesKey(placeKey(cacheKey))
        val json = context.weatherDataStore.data.map { preferences ->
            preferences[key]
        }.first() ?: return null
        val cached = runCatching { AppJson.decodeFromString<CachedPlaceWeather>(json) }.getOrNull()
            ?: return null
        return PlaceWeatherEntry(cached.data, cached.timestamp)
    }

    suspend fun savePlaceWeather(cacheKey: String, weather: PlaceWeather) {
        persist {
            val key = stringPreferencesKey(placeKey(cacheKey))
            val json = AppJson.encodeToString(
                CachedPlaceWeather(weather, System.currentTimeMillis())
            )
            context.weatherDataStore.edit { preferences ->
                preferences[key] = json
            }
            trimPlaceCache()
        }
    }

    suspend fun latestPlace(): LatestPlace? {
        val json = context.weatherDataStore.data.map { preferences ->
            preferences[latestKey]
        }.first()
        if (json != null) {
            runCatching { AppJson.decodeFromString<LatestPlace>(json) }.getOrNull()?.let { return it }
        }
        return newestPlaceCache()
    }

    /** 升级前只按地点存过缓存，没有总快照时，取时间最新的一条。 */
    private suspend fun newestPlaceCache(): LatestPlace? {
        val preferences = context.weatherDataStore.data.first()
        var best: CachedPlaceWeather? = null
        for ((key, value) in preferences.asMap()) {
            if (!key.name.startsWith("place2_")) continue
            val raw = value as? String ?: continue
            val cached = runCatching { AppJson.decodeFromString<CachedPlaceWeather>(raw) }.getOrNull()
                ?: continue
            if (best == null || cached.timestamp >= best.timestamp) best = cached
        }
        val found = best ?: return null
        return LatestPlace(place = found.data, updatedAt = found.timestamp)
    }

    /** 整份替换最近一次快照。城市和天气必须来自同一次确认，不能只改其中一项。 */
    suspend fun saveLatest(snapshot: LatestPlace) {
        if (snapshot.updatedAt <= 0L) return
        if (snapshot.city.name.isBlank() && snapshot.city.id.isBlank()) return
        if (snapshot.place.current.temp.isBlank() && snapshot.place.current.text.isBlank()) return
        persist {
            latestMutex.withLock {
                val json = AppJson.encodeToString(snapshot)
                context.weatherDataStore.edit { preferences ->
                    preferences[latestKey] = json
                }
            }
        }
    }

    /** 地点缓存按坐标和语言分开存，只保留最近若干条。 */
    private suspend fun trimPlaceCache() {
        val preferences = context.weatherDataStore.data.first()
        val entries = preferences.asMap().mapNotNull { (key, value) ->
            if (!key.name.startsWith("place2_")) return@mapNotNull null
            val raw = value as? String ?: return@mapNotNull null
            val cached = runCatching { AppJson.decodeFromString<CachedPlaceWeather>(raw) }.getOrNull()
                ?: return@mapNotNull null
            key.name to cached.timestamp
        }.sortedByDescending { it.second }
        if (entries.size <= MAX_PLACE_ENTRIES) return
        val drop = entries.drop(MAX_PLACE_ENTRIES).map { it.first }.toSet()
        context.weatherDataStore.edit { prefs ->
            prefs.asMap().keys.filter { it.name in drop }.forEach { prefs.remove(it) }
        }
    }
}

private const val TAG = "WeatherDataCache"
private const val MAX_PLACE_ENTRIES = 12

private fun placeKey(cacheKey: String): String =
    "place2_" + cacheKey.replace(Regex("[^A-Za-z0-9._-]"), "_")

@Serializable
private data class CachedPlaceWeather(
    val data: PlaceWeather = PlaceWeather(),
    val timestamp: Long = 0L,
)
