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
