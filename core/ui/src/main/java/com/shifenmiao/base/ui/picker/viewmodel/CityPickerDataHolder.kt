package com.shifenmiao.base.ui.picker.viewmodel

import android.content.Context
import com.shifenmiao.base.ui.utils.BaseUIUtils
import com.shifenmiao.model.ui.picker.CountryData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityPickerDataHolder @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _countryData = MutableStateFlow(CountryData(emptyList()))
    val countryData: StateFlow<CountryData> = _countryData

    init {
        initData(context)
    }

    private fun initData(context: Context) {
        scope.launch {
            val jsonString = BaseUIUtils.loadJsonFromAssets(context, "picker/pca.json")
            jsonString?.let {
                _countryData.value = BaseUIUtils.parseProvinces(it)
            }
        }
    }

    fun getDistricts(province: String, city: String): List<String> {
        return countryData.value.provinces
            .find { it.name == province }
            ?.cities
            ?.find { it.name == city }
            ?.districts ?: emptyList()
    }

    fun getCities(province: String): List<String> {
        return countryData.value.provinces
            .find { it.name == province }
            ?.cities
            ?.map { it.name } ?: emptyList()
    }

    fun getProvinces(): List<String> {
        return countryData.value.provinces.map { it.name }
    }
}

