package com.shifenmiao.model.ui.picker

import kotlinx.serialization.Serializable

@Serializable
data class CityData(
    val name: String,
    val districts: List<String>
)

@Serializable
data class ProvinceData(
    val name: String,
    val cities: List<CityData>
)

@Serializable
data class CountryData(
    val provinces: List<ProvinceData>
)

data class  SelectedCountryData(
    var province: String? = null,
    var city: String? = null,
    var district: String? = null
)