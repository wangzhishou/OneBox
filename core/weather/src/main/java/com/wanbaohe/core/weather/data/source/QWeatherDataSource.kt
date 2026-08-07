package com.wanbaohe.core.weather.data.source

import com.qweather.sdk.Callback
import com.qweather.sdk.QWeather
import com.qweather.sdk.basic.Lang
import com.qweather.sdk.basic.Unit
import com.qweather.sdk.parameter.geo.GeoCityLookupParameter
import com.qweather.sdk.parameter.weather.WeatherParameter
import com.qweather.sdk.response.error.ErrorResponse
import com.qweather.sdk.response.geo.GeoCityLookupResponse
import com.qweather.sdk.response.weather.WeatherNowResponse
import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import java.math.RoundingMode
import java.util.Locale
import kotlin.coroutines.resume

class QWeatherDataSource {

    suspend fun geoCityLookup(lat: Double, lon: Double): Result<CityInfo> =
        suspendCancellableCoroutine { continuation ->
            val lonRoundingMode = lon.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            val latRoundingMode = lat.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            val location = "$lonRoundingMode,$latRoundingMode"
            val parameter = GeoCityLookupParameter(location)

            QWeather.instance.geoCityLookup(parameter, object : Callback<GeoCityLookupResponse?> {
                override fun onSuccess(response: GeoCityLookupResponse?) {
                    val locationResult = response?.location?.firstOrNull()
                    if (locationResult != null) {
                        continuation.resume(
                            Result.success(
                                CityInfo(
                                    id = locationResult.id,
                                    name = locationResult.name,
                                    lat = locationResult.lat.toDoubleOrNull() ?: lat,
                                    lon = locationResult.lon.toDoubleOrNull() ?: lon,
                                    adm2 = locationResult.adm2 ?: "",
                                    adm1 = locationResult.adm1 ?: "",
                                    country = locationResult.country ?: "",
                                    tz = locationResult.tz ?: "",
                                    utcOffset = locationResult.utcOffset ?: "",
                                    isDst = locationResult.isDst ?: "",
                                    type = locationResult.type ?: "",
                                    rank = locationResult.rank ?: "",
                                    fxLink = locationResult.fxLink ?: ""
                                )
                            )
                        )
                    } else {
                        continuation.resume(Result.failure(Exception("City not found")))
                    }
                }

                override fun onFailure(errorResponse: ErrorResponse?) {
                    continuation.resume(Result.failure(Exception("QWeather failure: $errorResponse")))
                }

                override fun onException(e: Throwable) {
                    continuation.resume(Result.failure(e))
                }
            })
        }

    suspend fun getWeatherNow(cityId: String): Result<WeatherInfo> =
        suspendCancellableCoroutine { continuation ->
            // 中文系统用简体中文,其他语言用英文(和风天气多语言支持)
            val lang = if (Locale.getDefault().toLanguageTag().startsWith("zh")) {
                Lang.ZH_HANS
            } else {
                Lang.EN
            }
            val parameter = WeatherParameter(cityId)
                .lang(lang)
                .unit(Unit.METRIC)

            QWeather.instance.weatherNow(parameter, object : Callback<WeatherNowResponse?> {
                override fun onSuccess(response: WeatherNowResponse?) {
                    val now = response?.now
                    if (response != null && now != null) {
                        continuation.resume(
                            Result.success(
                                WeatherInfo(
                                    updateTime = response.updateTime ?: "",
                                    fxLink = response.fxLink ?: "",
                                    obsTime = now.obsTime ?: "",
                                    temp = now.temp ?: "",
                                    feelsLike = now.feelsLike ?: "",
                                    icon = now.icon ?: "",
                                    text = now.text ?: "",
                                    wind360 = now.wind360 ?: "",
                                    windDir = now.windDir ?: "",
                                    windScale = now.windScale ?: "",
                                    windSpeed = now.windSpeed ?: "",
                                    humidity = now.humidity ?: "",
                                    precip = now.precip ?: "",
                                    pressure = now.pressure ?: "",
                                    vis = now.vis ?: "",
                                    cloud = now.cloud ?: "",
                                    dew = now.dew ?: ""
                                )
                            )
                        )
                    } else {
                        continuation.resume(Result.failure(Exception("Weather data is empty")))
                    }
                }

                override fun onFailure(errorResponse: ErrorResponse?) {
                    continuation.resume(Result.failure(Exception("QWeather failure: $errorResponse")))
                }

                override fun onException(e: Throwable) {
                    continuation.resume(Result.failure(e))
                }
            })
        }
}
