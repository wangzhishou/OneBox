package com.wanbaohe.core.weather.data.cache

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class CachedWeather(
    val data: WeatherInfo,
    val timestamp: Long
)

private val Context.weatherDataStore by preferencesDataStore(name = "weather_data_cache")

class WeatherDataCache(
    private val context: Context,
    private val gson: Gson
) {
    private val validDuration = 30 * 60 * 1000L // 30 分钟有效期

    suspend fun getWeather(cityId: String): WeatherInfo? {
        val key = stringPreferencesKey(cityId)
        val json = context.weatherDataStore.data.map { preferences ->
            preferences[key]
        }.first()

        if (json == null) return null

        val cached = gson.fromJson(json, CachedWeather::class.java)

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
        val json = gson.toJson(cached)

        context.weatherDataStore.edit { preferences ->
            preferences[key] = json
        }
    }
}
