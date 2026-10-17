package com.wanbaohe.core.weather.data.source

import android.content.Context
import com.qweather.sdk.Callback
import com.qweather.sdk.QWeather
import com.qweather.sdk.basic.Indices
import com.qweather.sdk.basic.Lang
import com.qweather.sdk.basic.Unit as QWeatherUnit
import com.qweather.sdk.parameter.air.AirV1Parameter
import com.qweather.sdk.parameter.alert.WeatherAlertCurrentParameter
import com.qweather.sdk.parameter.geo.GeoCityLookupParameter
import com.qweather.sdk.parameter.indices.IndicesParameter
import com.qweather.sdk.parameter.minutely.MinutelyParameter
import com.qweather.sdk.parameter.weather.WeatherCurrentParameter
import com.qweather.sdk.parameter.weather.WeatherDailyParameter
import com.qweather.sdk.parameter.weather.WeatherHourlyParameter
import com.qweather.sdk.parameter.weather.WeatherParameter
import com.qweather.sdk.response.ValueUnit
import com.qweather.sdk.response.air.v1.AirIndex
import com.qweather.sdk.response.air.v1.AirV1CurrentResponse
import com.qweather.sdk.response.alert.WeatherAlertCurrentResponse
import com.qweather.sdk.response.error.ErrorResponse
import com.qweather.sdk.response.geo.GeoCityLookupResponse
import com.qweather.sdk.response.indices.IndicesDailyResponse
import com.qweather.sdk.response.minutely.MinutelyResponse
import com.qweather.sdk.response.weather.WeatherCurrentResponse
import com.qweather.sdk.response.weather.WeatherDailyResponse
import com.qweather.sdk.response.weather.WeatherForecastDaily
import com.qweather.sdk.response.weather.WeatherForecastDailyResponse
import com.qweather.sdk.response.weather.WeatherForecastHourly
import com.qweather.sdk.response.weather.WeatherForecastHourlyResponse
import com.qweather.sdk.response.weather.WeatherNowResponse
import com.wanbaohe.core.weather.data.weatherLocale
import com.wanbaohe.core.weather.domain.model.AirQuality
import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.DailyOutlook
import com.wanbaohe.core.weather.domain.model.DailyWeatherInfo
import com.wanbaohe.core.weather.domain.model.HourlyForecast
import com.wanbaohe.core.weather.domain.model.LifeIndex
import com.wanbaohe.core.weather.domain.model.PlaceWeather
import com.wanbaohe.core.weather.domain.model.WeatherAlertItem
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import java.math.RoundingMode
import java.util.Locale
import kotlin.coroutines.resume

class QWeatherDataSource(
    private val context: Context,
) {

    suspend fun geoCityLookup(lat: Double, lon: Double): Result<CityInfo> =
        suspendCancellableCoroutine { continuation ->
            val lonRoundingMode = lon.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            val latRoundingMode = lat.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            val location = "$lonRoundingMode,$latRoundingMode"
            val parameter = GeoCityLookupParameter(location)
                .lang(preferredLang())

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

    suspend fun geoCityLookupByName(name: String): Result<CityInfo> =
        suspendCancellableCoroutine { continuation ->
            val parameter = GeoCityLookupParameter(name)
                .lang(preferredLang())
                .number(1)

            QWeather.instance.geoCityLookup(parameter, object : Callback<GeoCityLookupResponse?> {
                override fun onSuccess(response: GeoCityLookupResponse?) {
                    val locationResult = response?.location?.firstOrNull()
                    if (locationResult != null) {
                        continuation.resume(
                            Result.success(
                                CityInfo(
                                    id = locationResult.id,
                                    name = locationResult.name,
                                    lat = locationResult.lat.toDoubleOrNull() ?: 0.0,
                                    lon = locationResult.lon.toDoubleOrNull() ?: 0.0,
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

    suspend fun getWeatherDaily(cityId: String, days: Int): Result<List<DailyWeatherInfo>> =
        suspendCancellableCoroutine { continuation ->
            val parameter = WeatherParameter(cityId)
                .lang(preferredLang())
                .unit(QWeatherUnit.METRIC)

            val callback = object : Callback<WeatherDailyResponse?> {
                override fun onSuccess(response: WeatherDailyResponse?) {
                    val daily = response?.daily
                    if (daily != null) {
                        continuation.resume(
                            Result.success(
                                daily.map { item ->
                                    DailyWeatherInfo(
                                        fxDate = item.fxDate ?: "",
                                        sunrise = item.sunrise ?: "",
                                        sunset = item.sunset ?: "",
                                        tempMax = item.tempMax ?: "",
                                        tempMin = item.tempMin ?: "",
                                        textDay = item.textDay ?: "",
                                        textNight = item.textNight ?: "",
                                        windDirDay = item.windDirDay ?: "",
                                        windScaleDay = item.windScaleDay ?: "",
                                        humidity = item.humidity ?: "",
                                        precip = item.precip ?: "",
                                    )
                                }
                            )
                        )
                    } else {
                        continuation.resume(Result.failure(Exception("Forecast data is empty")))
                    }
                }

                override fun onFailure(errorResponse: ErrorResponse?) {
                    continuation.resume(Result.failure(Exception("QWeather failure: $errorResponse")))
                }

                override fun onException(e: Throwable) {
                    continuation.resume(Result.failure(e))
                }
            }

            if (days <= 3) {
                QWeather.instance.weather3d(parameter, callback)
            } else {
                QWeather.instance.weather7d(parameter, callback)
            }
        }

    suspend fun getPlaceWeather(lat: Double, lon: Double): Result<PlaceWeather> = coroutineScope {
        val latitude = round2(lat)
        val longitude = round2(lon)
        val current = async { weatherCurrent(latitude, longitude) }
        val hourly = async { weatherHourly(latitude, longitude) }
        val daily = async { weatherDaily(latitude, longitude) }
        val air = async { airCurrent(latitude, longitude) }
        val alerts = async { weatherAlerts(latitude, longitude) }
        val indices = async { lifeIndices(longitude, latitude) }
        val minutely = async { minutelySummary(longitude, latitude) }

        val currentResult = current.await()
        if (currentResult.isFailure) {
            return@coroutineScope Result.failure(
                currentResult.exceptionOrNull() ?: IllegalStateException("Weather data is empty")
            )
        }
        Result.success(
            PlaceWeather(
                current = currentResult.getOrThrow(),
                hourly = hourly.await().getOrDefault(emptyList()),
                daily = daily.await().getOrDefault(emptyList()),
                air = air.await().getOrNull(),
                alerts = alerts.await().getOrDefault(emptyList()),
                indices = indices.await().getOrDefault(emptyList()),
                minutelySummary = minutely.await().getOrDefault(""),
            )
        )
    }

    private suspend fun weatherCurrent(lat: Double, lon: Double): Result<WeatherInfo> {
        val parameter = WeatherCurrentParameter(lat, lon)
            .localTime(true)
            .lang(preferredLang())
        return awaitCall<WeatherCurrentResponse> { callback ->
            QWeather.instance.weatherCurrent(parameter, callback)
        }.mapCatching { response -> response.toWeatherInfo(context.weatherLocale()) }
    }

    private suspend fun weatherHourly(lat: Double, lon: Double): Result<List<HourlyForecast>> {
        val parameter = WeatherHourlyParameter(lat, lon)
            .hours(24)
            .localTime(true)
            .lang(preferredLang())
        return awaitCall<WeatherForecastHourlyResponse> { callback ->
            QWeather.instance.weatherForecastHourly(parameter, callback)
        }.map { response ->
            response.hours.orEmpty().map { it.toHourlyForecast() }
        }
    }

    private suspend fun weatherDaily(lat: Double, lon: Double): Result<List<DailyOutlook>> {
        val parameter = WeatherDailyParameter(lat, lon)
            .days(7)
            .localTime(true)
            .lang(preferredLang())
        return awaitCall<WeatherForecastDailyResponse> { callback ->
            QWeather.instance.weatherForecastDaily(parameter, callback)
        }.map { response ->
            response.days.orEmpty().map { it.toDailyOutlook() }
        }
    }

    private suspend fun airCurrent(lat: Double, lon: Double): Result<AirQuality?> {
        val parameter = AirV1Parameter(lat, lon).setLang(preferredLang())
        return awaitCall<AirV1CurrentResponse> { callback ->
            QWeather.instance.airCurrent(parameter, callback)
        }.map { response -> response.indexes.orEmpty().preferredIndex()?.toAirQuality() }
    }

    private suspend fun weatherAlerts(lat: Double, lon: Double): Result<List<WeatherAlertItem>> {
        val parameter = WeatherAlertCurrentParameter(lat, lon, true).setLang(preferredLang())
        return awaitCall<WeatherAlertCurrentResponse> { callback ->
            QWeather.instance.weatherAlertCurrent(parameter, callback)
        }.map { response ->
            response.alerts.orEmpty().mapNotNull { alert ->
                val headline = alert.headline.orEmpty()
                if (headline.isBlank()) null
                else WeatherAlertItem(
                    headline = headline,
                    instruction = alert.instruction.orEmpty(),
                )
            }.take(2)
        }
    }

    private suspend fun lifeIndices(lon: Double, lat: Double): Result<List<LifeIndex>> {
        val parameter = IndicesParameter(
            "$lon,$lat",
            Indices.DRSG,
            Indices.UV,
            Indices.CW,
            Indices.SPT,
            Indices.FLU,
            Indices.TRA,
        ).lang(preferredLang())
        return awaitCall<IndicesDailyResponse> { callback ->
            QWeather.instance.indices1d(parameter, callback)
        }.map { response ->
            response.daily.orEmpty().mapNotNull { item ->
                val name = item.name.orEmpty()
                val category = item.category.orEmpty()
                if (name.isBlank() && category.isBlank()) null
                else LifeIndex(name = name, category = category)
            }
        }
    }

    /** 分钟降水的 location 串是经度,纬度，构造参数顺序与实况接口相反。 */
    private suspend fun minutelySummary(lon: Double, lat: Double): Result<String> {
        val parameter = MinutelyParameter(lon, lat).lang(preferredLang())
        return awaitCall<MinutelyResponse> { callback ->
            QWeather.instance.minutely(parameter, callback)
        }.map { response ->
            val raining = response.minutely.orEmpty().any { item ->
                (item.precip?.toDoubleOrNull() ?: 0.0) > 0.0
            }
            if (raining) response.summary.orEmpty() else ""
        }
    }

    private suspend inline fun <reified T> awaitCall(
        crossinline call: (Callback<T>) -> kotlin.Unit,
    ): Result<T> = suspendCancellableCoroutine { continuation ->
        call(object : Callback<T> {
            override fun onSuccess(response: T) {
                if (continuation.isActive) continuation.resume(Result.success(response))
            }

            override fun onFailure(errorResponse: ErrorResponse?) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(Exception("QWeather failure: $errorResponse")))
                }
            }

            override fun onException(e: Throwable) {
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
        })
    }

    private fun round2(value: Double): Double =
        value.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()

    private fun preferredLang(): Lang {
        val locale = context.weatherLocale()
        val tag = locale.toLanguageTag()
        return when {
            tag.startsWith("zh-Hant") || tag.contains("TW") || tag.contains("HK") || tag.contains("MO") ->
                Lang.ZH_HANT
            locale.language == "zh" -> Lang.ZH_HANS
            locale.language == "de" -> Lang.DE
            locale.language == "es" -> Lang.ES
            locale.language == "fr" -> Lang.FR
            locale.language == "ja" -> Lang.JA
            locale.language == "ko" -> Lang.KO
            locale.language == "ru" -> Lang.RU
            locale.language == "hi" -> Lang.HI
            locale.language == "ar" -> Lang.AR
            locale.language == "pt" -> Lang.PT
            locale.language == "in" || locale.language == "id" -> Lang.ID
            locale.language == "tr" -> Lang.TR
            locale.language == "vi" -> Lang.VI
            locale.language == "fil" || locale.language == "tl" -> Lang.FIL
            else -> Lang.EN
        }
    }

    suspend fun getWeatherNow(cityId: String): Result<WeatherInfo> =
        suspendCancellableCoroutine { continuation ->
            val parameter = WeatherParameter(cityId)
                .lang(preferredLang())
                .unit(QWeatherUnit.METRIC)

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

private fun WeatherCurrentResponse.toWeatherInfo(locale: Locale): WeatherInfo {
    val condition = condition
    if (condition == null && temperature == null) {
        throw IllegalStateException("Weather data is empty")
    }
    return WeatherInfo(
        temp = formatMeasure(temperature, Measure.PLAIN),
        feelsLike = formatMeasure(feelsLike, Measure.PLAIN),
        icon = condition?.code.orEmpty(),
        text = condition?.text.orEmpty(),
        wind360 = wind?.direction?.degree?.toString().orEmpty(),
        windDir = compassText(wind?.direction?.compass, locale),
        windScale = wind?.scale?.toString().orEmpty(),
        windSpeed = formatMeasure(wind?.speed, Measure.WIND_KMH),
        humidity = formatRatioPercent(humidity),
        precip = formatMeasure(precipitation?.intensity, Measure.PLAIN),
        pressure = formatMeasure(pressure, Measure.PLAIN),
        vis = formatMeasure(visibility, Measure.VIS_KM),
        cloud = formatRatioPercent(cloudCover),
        dew = formatMeasure(dewPoint, Measure.PLAIN),
        uvIndex = uvIndex?.toString().orEmpty(),
    )
}

private fun WeatherForecastHourly.toHourlyForecast() = HourlyForecast(
    time = forecastTime.orEmpty().substringAfter('T', forecastTime.orEmpty()).take(5),
    temp = formatMeasure(temperature, Measure.PLAIN),
    icon = condition?.code.orEmpty(),
    precipProbability = formatPercent(precipitation?.probability),
)

private fun WeatherForecastDaily.toDailyOutlook(): DailyOutlook {
    val day = daytime
    val night = nighttime
    return DailyOutlook(
        date = forecastStartTime.orEmpty().take(10),
        tempMax = formatMeasure(temperatureMax, Measure.PLAIN),
        tempMin = formatMeasure(temperatureMin, Measure.PLAIN),
        textDay = day?.condition?.text.orEmpty(),
        iconDay = day?.condition?.code.orEmpty(),
        iconNight = night?.condition?.code.orEmpty(),
        precipProbability = formatPercent(
            day?.precipitation?.probability ?: night?.precipitation?.probability
        ),
    )
}

private fun List<AirIndex>.preferredIndex(): AirIndex? =
    firstOrNull { it.code.orEmpty().equals("cn-mee", ignoreCase = true) }
        ?: firstOrNull {
            val code = it.code.orEmpty()
            code.isNotBlank() && !code.equals("qaqi", ignoreCase = true)
        }
        ?: firstOrNull {
            it.aqiDisplay.orEmpty().isNotBlank() || it.category.orEmpty().isNotBlank() || it.aqi > 0.0
        }

private fun AirIndex.toAirQuality(): AirQuality {
    val display = aqiDisplay.orEmpty().ifBlank { formatPlain(aqi) }
    return AirQuality(
        aqi = display,
        category = category.orEmpty(),
        primaryPollutant = primaryPollutant?.name.orEmpty()
            .ifBlank { primaryPollutant?.fullName.orEmpty() },
        advice = health?.advice?.generalPopulation.orEmpty()
            .ifBlank { health?.effect.orEmpty() },
    )
}

private enum class Measure { PLAIN, VIS_KM, WIND_KMH }

private fun formatMeasure(value: ValueUnit?, measure: Measure): String {
    val raw = value?.value ?: return ""
    val unit = value.unit.orEmpty().lowercase(Locale.US)
    val converted = when (measure) {
        Measure.VIS_KM -> if (unit == "m" || unit == "meter" || unit == "meters") raw / 1000.0 else raw
        Measure.WIND_KMH -> if (unit.contains("m/s")) raw * 3.6 else raw
        Measure.PLAIN -> raw
    }
    return formatPlain(converted)
}

private fun formatPlain(value: Double?): String {
    if (value == null) return ""
    val nearest = kotlin.math.round(value)
    return if (kotlin.math.abs(value - nearest) < 0.05) {
        nearest.toLong().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private fun formatRatioPercent(value: Double?): String {
    if (value == null) return ""
    val percent = if (value <= 1.0) value * 100.0 else value
    return formatPlain(percent)
}

private fun compassText(raw: String?, locale: Locale): String {
    val key = raw.orEmpty().trim().lowercase(Locale.US)
    if (key.isEmpty()) return ""
    val zh = locale.language == "zh"
    val label = when (key) {
        "n" -> if (zh) "北风" else "N"
        "nne" -> if (zh) "东北风" else "NNE"
        "ne" -> if (zh) "东北风" else "NE"
        "ene" -> if (zh) "东北风" else "ENE"
        "e" -> if (zh) "东风" else "E"
        "ese" -> if (zh) "东南风" else "ESE"
        "se" -> if (zh) "东南风" else "SE"
        "sse" -> if (zh) "东南风" else "SSE"
        "s" -> if (zh) "南风" else "S"
        "ssw" -> if (zh) "西南风" else "SSW"
        "sw" -> if (zh) "西南风" else "SW"
        "wsw" -> if (zh) "西南风" else "WSW"
        "w" -> if (zh) "西风" else "W"
        "wnw" -> if (zh) "西北风" else "WNW"
        "nw" -> if (zh) "西北风" else "NW"
        "nnw" -> if (zh) "西北风" else "NNW"
        else -> raw.orEmpty()
    }
    return label
}

private fun formatPercent(value: Double?): String {
    if (value == null) return ""
    val percent = if (value <= 1.0) value * 100.0 else value
    return percent.toInt().toString()
}
