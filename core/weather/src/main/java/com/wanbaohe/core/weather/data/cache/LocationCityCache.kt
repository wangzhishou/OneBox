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
