package com.wanbaohe.altitude.domain

import com.shifenmiao.core.R as CoreR
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.WeatherInfo

/**
 * 海拔数据来源
 */
enum class AltitudeSource(val tag: String) {
    /** 气压传感器（精度高，无需权限，默认） */
    BAROMETER("BAROMETER"),
    /** GPS/网络定位（用户手动触发校准） */
    GPS("GPS")
}

/**
 * 海拔显示单位
 */
enum class AltitudeUnit(val suffix: String, val toMeters: Float) {
    METERS("m", 1f),
    FEET("ft", 0.3048f);

    /** 将米转换为当前单位 */
    fun fromMeters(meters: Float): Float = meters / toMeters
}

/**
 * 海拔记录领域模型（UI / 业务层使用）
 */
data class AltitudeRecord(
    val id: Long = 0,
    val altitudeMeters: Float,
    val source: AltitudeSource,
    val accuracyMeters: Float = 0f,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val citySnapshot: CitySnapshot? = null,
    val weatherSnapshot: WeatherSnapshot? = null,
    val note: String = "",
    val recordedAt: Long = System.currentTimeMillis()
) {
    /** 格式化海拔值（保留1位小数） */
    fun formattedAltitude(unit: AltitudeUnit): String =
        "%.1f".format(unit.fromMeters(altitudeMeters))

    /** 历史卡片标题：优先使用城市信息 */
    val resolvedTitle: String
        get() = citySnapshot?.title?.takeIf { it.isNotBlank() }
            ?: run {
                if (latitude != null && longitude != null) {
                    "${"%.2f".format(latitude)}, ${"%.2f".format(longitude)}"
                } else {
                    "--"
                }
            }
}

data class CitySnapshot(
    val name: String = "",
    val adm2: String = "",
    val adm1: String = "",
    val country: String = "",
    val tz: String = "",
    val utcOffset: String = ""
) {
    val title: String
        get() = sequenceOf(name, adm2, adm1)
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?: country

    /** 转为 CityInfo 以便复用仪表盘组件 */
    fun toCityInfo(lat: Double = 0.0, lon: Double = 0.0): CityInfo = CityInfo(
        id = "",
        name = name,
        lat = lat,
        lon = lon,
        adm2 = adm2,
        adm1 = adm1,
        country = country,
        tz = tz,
        utcOffset = utcOffset
    )
}

data class WeatherSnapshot(
    /** 预报摘要（历史落库快照） */
    val forecast: String = "",
    val text: String = "",
    val temp: String = "",
    val feelsLike: String = "",
    val windDir: String = "",
    val windScale: String = "",
    val windSpeed: String = "",
    val humidity: String = "",
    val pressure: String = "",
    val obsTime: String = "",
    val updateTime: String = "",
    val dew: String = "",
    val cloud: String = "",
    val vis: String = "",
    val precip: String = ""
) {
    val brief: String
        get() = forecast.ifBlank {
            buildString {
            if (text.isNotBlank()) append(text)
            if (temp.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(temp).append("°C")
            }
            if (humidity.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(AppContext.getString(CoreR.string.altitude_humidity_value, humidity))
            }
            }
        }

    /** 转为 WeatherInfo 以便复用仪表盘组件 */
    fun toWeatherInfo(): WeatherInfo = WeatherInfo(
        obsTime = obsTime,
        updateTime = updateTime,
        temp = temp,
        feelsLike = feelsLike,
        text = text,
        windDir = windDir,
        windScale = windScale,
        windSpeed = windSpeed,
        humidity = humidity,
        pressure = pressure,
        dew = dew,
        cloud = cloud,
        vis = vis,
        precip = precip
    )
}

