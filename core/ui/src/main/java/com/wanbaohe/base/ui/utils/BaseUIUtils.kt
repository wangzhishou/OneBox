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

package com.wanbaohe.base.ui.utils

import android.content.Context
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ui.picker.CityData
import com.shifenmiao.model.ui.picker.CountryData
import com.shifenmiao.model.ui.picker.ProvinceData
import java.io.IOException
import kotlinx.serialization.decodeFromString

object BaseUIUtils {
    fun loadJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    /**
     * 解析 pca.json(省 → 市 → 区县三级嵌套对象)。
     * 顶层是 {省名: {市名: [区县]}},与 Gson 时代 CountryDataDeserializer 的产物同构。
     */
    fun parseProvinces(jsonString: String): CountryData {
        return try {
            val map = AppJson.decodeFromString<Map<String, Map<String, List<String>>>>(jsonString)
            CountryData(
                map.map { (provinceName, cities) ->
                    ProvinceData(
                        name = provinceName,
                        cities = cities.map { (cityName, districts) -> CityData(cityName, districts) }
                    )
                }
            )
        } catch (e: Exception) {
            CountryData(emptyList())
        }
    }
}
