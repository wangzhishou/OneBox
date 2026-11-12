/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
