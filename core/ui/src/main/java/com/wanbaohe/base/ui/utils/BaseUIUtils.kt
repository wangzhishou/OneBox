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
