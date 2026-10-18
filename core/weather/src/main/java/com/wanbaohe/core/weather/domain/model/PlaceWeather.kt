package com.wanbaohe.core.weather.domain.model

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
) {
    /** 把客户端拿到这份数据的时间写进实况，供界面显示上次更新。 */
    fun withClientUpdatedAt(epochMs: Long): PlaceWeather {
        if (epochMs <= 0L) return this
        val formatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(epochMs))
        return copy(current = current.copy(updateTime = formatted))
    }

    /** 读回 [withClientUpdatedAt] 写上的时间。解析失败时返回 0。 */
    fun clientUpdatedAtMillis(): Long {
        val text = current.updateTime.trim()
        if (text.isEmpty()) return 0L
        return runCatching {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(text)?.time ?: 0L
        }.getOrDefault(0L)
    }
}

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
