package com.wanbaohe.core.weather.data.repository

import android.content.Context
import com.wanbaohe.core.weather.WeatherInitializer
import com.wanbaohe.core.weather.data.cache.LocationCityCache
import com.wanbaohe.core.weather.data.weatherLocale
import com.wanbaohe.core.weather.data.cache.WeatherDataCache
import com.wanbaohe.core.weather.data.location.NetworkCityLocator
import com.wanbaohe.core.weather.data.source.QWeatherDataSource
import com.wanbaohe.core.weather.domain.model.CityInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.wanbaohe.core.weather.domain.model.DailyWeatherInfo
import com.wanbaohe.core.weather.domain.model.LatestPlace
import com.wanbaohe.core.weather.domain.model.PlaceWeather
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import java.math.RoundingMode
import com.wanbaohe.core.weather.domain.repository.WeatherRepository

class WeatherRepositoryImpl(
    private val dataSource: QWeatherDataSource,
    private val cityCache: LocationCityCache,
    private val weatherCache: WeatherDataCache,
    private val context: Context,
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

    override suspend fun getCityByName(name: String): Result<CityInfo> {
        val safeName = name.trim()
        if (safeName.isEmpty()) {
            return Result.failure(IllegalArgumentException("city name is blank"))
        }
        return dataSource.geoCityLookupByName(safeName)
    }

    override suspend fun getCityFromNetwork(): Result<CityInfo> = withContext(Dispatchers.IO) {
        val hit = NetworkCityLocator.locate()
            ?: return@withContext Result.failure(IllegalStateException("network city unavailable"))
        val latitude = hit.latitude
        val longitude = hit.longitude
        if (latitude != null && longitude != null) {
            return@withContext getCityAtLocation(latitude, longitude)
        }
        val name = hit.name?.trim().orEmpty()
        if (name.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("network city unavailable"))
        }
        getCityByName(name)
    }

    override suspend fun getDailyForecast(cityId: String, days: Int): Result<List<DailyWeatherInfo>> {
        return dataSource.getWeatherDaily(cityId, days.coerceIn(1, 7))
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

    override suspend fun getPlaceWeather(lat: Double, lon: Double): Result<PlaceWeather> {
        val now = System.currentTimeMillis()
        val cacheKey = placeCacheKey(lat, lon)
        val cached = weatherCache.peekPlaceWeather(cacheKey)
        if (cached != null && weatherCache.isFresh(cached.timestamp, now)) {
            return Result.success(cached.data.withClientUpdatedAt(cached.timestamp))
        }
        val result = dataSource.getPlaceWeather(lat, lon)
        if (result.isSuccess) {
            val weather = result.getOrThrow()
            weatherCache.savePlaceWeather(cacheKey, weather)
            return Result.success(weather.withClientUpdatedAt(now))
        }
        if (cached != null) {
            return Result.success(cached.data.withClientUpdatedAt(cached.timestamp))
        }
        return result
    }

    override suspend fun latestPlace(): LatestPlace? = weatherCache.latestPlace()

    override suspend fun rememberLatestPlace(city: CityInfo, place: PlaceWeather, updatedAt: Long) {
        weatherCache.saveLatest(LatestPlace(city = city, place = place, updatedAt = updatedAt))
    }

    private fun placeCacheKey(lat: Double, lon: Double): String {
        val latitude = lat.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        val longitude = lon.toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        return "$latitude,$longitude|${context.weatherLocale().toLanguageTag()}"
    }
}

