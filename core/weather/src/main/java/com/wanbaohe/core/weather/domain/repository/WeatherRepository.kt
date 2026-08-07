package com.wanbaohe.core.weather.domain.repository

import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.wanbaohe.core.weather.domain.model.CityInfo

interface WeatherRepository {
    suspend fun getWeatherAtLocation(lat: Double, lon: Double): Result<WeatherInfo>
    suspend fun getCityAtLocation(lat: Double, lon: Double): Result<CityInfo>
}

