package com.wanbaohe.core.weather.data.repository

import com.wanbaohe.core.weather.WeatherInitializer
import com.wanbaohe.core.weather.data.cache.LocationCityCache
import com.wanbaohe.core.weather.data.cache.WeatherDataCache
import com.wanbaohe.core.weather.data.source.QWeatherDataSource
import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.wanbaohe.core.weather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val dataSource: QWeatherDataSource,
    private val cityCache: LocationCityCache,
    private val weatherCache: WeatherDataCache
) : WeatherRepository {

    init {
        WeatherInitializer.init()
    }

    override suspend fun getCityAtLocation(lat: Double, lon: Double): Result<CityInfo> {
        var cityInfo = cityCache.getCity(lat, lon)
        if (cityInfo != null) {
            return Result.success(cityInfo)
        }

        val result = dataSource.geoCityLookup(lat, lon)
        if (result.isSuccess) {
            cityInfo = result.getOrNull()!!
            cityCache.saveCity(lat, lon, cityInfo)
            return Result.success(cityInfo)
        }
        return Result.failure(result.exceptionOrNull()!!)
    }

    override suspend fun getWeatherAtLocation(lat: Double, lon: Double): Result<WeatherInfo> {
        val cityResult = getCityAtLocation(lat, lon)
        if (cityResult.isFailure) {
            return Result.failure(cityResult.exceptionOrNull()!!)
        }

        val cityId = cityResult.getOrNull()!!.id
        val cachedWeather = weatherCache.getWeather(cityId)
        if (cachedWeather != null) {
            return Result.success(cachedWeather)
        }

        val weatherResult = dataSource.getWeatherNow(cityId)
        return if (weatherResult.isSuccess) {
            val weatherInfo = weatherResult.getOrNull()!!
            weatherCache.saveWeather(cityId, weatherInfo)
            Result.success(weatherInfo)
        } else {
            Result.failure(weatherResult.exceptionOrNull()!!)
        }
    }
}

