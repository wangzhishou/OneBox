package com.shifenmiao.model.ui.picker

import com.google.gson.*
import java.lang.reflect.Type

class CountryDataDeserializer : JsonDeserializer<CountryData> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CountryData {
        val jsonObject = json.asJsonObject
        val provinces = mutableListOf<ProvinceData>()

        for ((provinceName, provinceElement) in jsonObject.entrySet()) {
            val provinceData = provinceElement.asJsonObject
            val cities = mutableListOf<CityData>()

            for ((cityName, cityElement) in provinceData.entrySet()) {
                val districtsArray = cityElement.asJsonArray
                val districts = mutableListOf<String>()

                for (districtElement in districtsArray) {
                    districts.add(districtElement.asString)
                }

                cities.add(CityData(cityName, districts))
            }

            provinces.add(ProvinceData(provinceName, cities))
        }

        return CountryData(provinces)
    }
}