package com.wanbaohe.core.weather.data.cache

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.wanbaohe.core.weather.domain.model.CityInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.cityDataStore by preferencesDataStore(name = "location_city_cache")

class LocationCityCache(
    private val context: Context,
    private val gson: Gson
) {

    // 将经纬度格式化为网格 Key，保留2位小数，大约代表 1.1km 的网格中心
    private fun getGridKey(lat: Double, lon: Double): String {
        return "%.2f,%.2f".format(lat, lon)
    }

    suspend fun getCity(lat: Double, lon: Double): CityInfo? {
        val key = stringPreferencesKey(getGridKey(lat, lon))
        val json = context.cityDataStore.data.map { preferences ->
            preferences[key]
        }.first()

        return json?.let { gson.fromJson(it, CityInfo::class.java) }
    }

    suspend fun saveCity(lat: Double, lon: Double, cityInfo: CityInfo) {
        val key = stringPreferencesKey(getGridKey(lat, lon))
        val json = gson.toJson(cityInfo)

        context.cityDataStore.edit { preferences ->
            preferences[key] = json
        }
    }
}
