package com.shifenmiao.base.ui.utils

import android.content.Context
import com.google.gson.GsonBuilder
import com.shifenmiao.model.ui.picker.CountryData
import com.shifenmiao.model.ui.picker.CountryDataDeserializer
import java.io.IOException

object BaseUIUtils {
    fun loadJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    fun parseProvinces(jsonString: String): CountryData {
        try {
            val gson = GsonBuilder()
                .registerTypeAdapter(CountryData::class.java, CountryDataDeserializer())
                .create()

            val countryData: CountryData = gson.fromJson(jsonString, CountryData::class.java)
            return countryData
        } catch (e: Exception) {
            return CountryData(emptyList())
        }
    }
}