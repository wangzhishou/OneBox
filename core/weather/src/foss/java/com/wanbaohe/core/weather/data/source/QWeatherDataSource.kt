package com.wanbaohe.core.weather.data.source

import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.WeatherInfo

/**
 * foss (F-Droid) 渠道 stub: 不打包和风天气 SDK, 所有请求直接失败,
 * 上层 WeatherRepository 按常规失败路径降级处理。
 * 签名必须与 src/nonfoss 的真实实现保持一致。
 */
class QWeatherDataSource {

    suspend fun geoCityLookup(lat: Double, lon: Double): Result<CityInfo> =
        Result.failure(UnsupportedOperationException("QWeather SDK not available in foss build"))

    suspend fun getWeatherNow(cityId: String): Result<WeatherInfo> =
        Result.failure(UnsupportedOperationException("QWeather SDK not available in foss build"))
}
