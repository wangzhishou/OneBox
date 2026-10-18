package com.wanbaohe.core.weather.domain.model

import kotlinx.serialization.Serializable

/** 最近一次成功拿到的地点天气。过期后仍可展示，刷新失败时不丢。 */
@Serializable
data class LatestPlace(
    val city: CityInfo = CityInfo(),
    val place: PlaceWeather = PlaceWeather(),
    val updatedAt: Long = 0L,
)
