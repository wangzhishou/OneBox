package com.wanbaohe.core.weather.data.cache

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.core.weather.data.weatherLocale
import com.wanbaohe.core.weather.domain.model.CityInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

private val Context.cityDataStore by preferencesDataStore(name = "location_city_cache")

class LocationCityCache(
    private val context: Context
) {

    // 将经纬度格式化为网格 Key，保留2位小数，大约代表 1.1km 的网格中心
    private fun getGridKey(lat: Double, lon: Double): String {
        val locale = context.weatherLocale().toLanguageTag()
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
        return "%.2f,%.2f|%s".format(lat, lon, locale)
    }

    suspend fun getCity(lat: Double, lon: Double): CityInfo? {
        val key = stringPreferencesKey(getGridKey(lat, lon))
        val json = context.cityDataStore.data.map { preferences ->
            preferences[key]
        }.first()

        return json?.let { runCatching { AppJson.decodeFromString<CityInfo>(it) }.getOrNull() }
    }

    suspend fun saveCity(lat: Double, lon: Double, cityInfo: CityInfo) {
        val key = stringPreferencesKey(getGridKey(lat, lon))
        val json = AppJson.encodeToString(cityInfo)

        context.cityDataStore.edit { preferences ->
            preferences[key] = json
        }
    }
}
