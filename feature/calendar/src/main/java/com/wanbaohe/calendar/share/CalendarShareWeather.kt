package com.wanbaohe.calendar.share

import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.PlaceWeather

/**
 * 把和风天气的地点聚合数据压成卡面需要的几项，缺项留空。
 */
fun CityInfo.toShareWeather(place: PlaceWeather): CalendarShareWeather? {
    val current = place.current
    val daily = place.daily.firstOrNull()
    val weather = CalendarShareWeather(
        city = name,
        text = current.text,
        temp = current.temp.toIntTemperature(),
        tempMin = daily?.tempMin?.toIntTemperature().orEmpty(),
        tempMax = daily?.tempMax?.toIntTemperature().orEmpty(),
        aqi = place.air?.aqi.orEmpty(),
        aqiCategory = place.air?.category.orEmpty(),
        iconCode = current.icon,
    )
    return weather.takeIf { it.hasContent }
}

private fun String.toIntTemperature(): String =
    toDoubleOrNull()?.let { if (it % 1.0 == 0.0) it.toInt().toString() else "%.0f".format(it) }
        ?: this
