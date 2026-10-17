package com.wanbaohe.core.weather.domain.model

import kotlinx.serialization.Serializable

/** 某一地点的天气聚合。各段独立成败，缺段用空列表或 null。 */
@Serializable
data class PlaceWeather(
    val current: WeatherInfo = WeatherInfo(),
    val hourly: List<HourlyForecast> = emptyList(),
    val daily: List<DailyOutlook> = emptyList(),
    val air: AirQuality? = null,
    val alerts: List<WeatherAlertItem> = emptyList(),
    val indices: List<LifeIndex> = emptyList(),
    /** 未来两小时有降水时的概况，无降水为空 */
    val minutelySummary: String = "",
)

@Serializable
data class HourlyForecast(
    /** 当地时间 HH:mm */
    val time: String = "",
    val temp: String = "",
    val icon: String = "",
    /** 降水概率，百分比数值，不含百分号 */
    val precipProbability: String = "",
)

@Serializable
data class DailyOutlook(
    /** yyyy-MM-dd */
    val date: String = "",
    val tempMax: String = "",
    val tempMin: String = "",
    val textDay: String = "",
    val iconDay: String = "",
    val iconNight: String = "",
    /** 降水概率，百分比数值，不含百分号 */
    val precipProbability: String = "",
)

@Serializable
data class AirQuality(
    val aqi: String = "",
    val category: String = "",
    val primaryPollutant: String = "",
    val advice: String = "",
)

@Serializable
data class WeatherAlertItem(
    val headline: String = "",
    val instruction: String = "",
)

@Serializable
data class LifeIndex(
    val name: String = "",
    val category: String = "",
)
