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

package com.wanbaohe.core.weather.data.source

import android.content.Context
import com.wanbaohe.core.weather.domain.model.CityInfo
import com.wanbaohe.core.weather.domain.model.DailyWeatherInfo
import com.wanbaohe.core.weather.domain.model.PlaceWeather
import com.wanbaohe.core.weather.domain.model.WeatherInfo

/**
 * foss (F-Droid) 渠道 stub: 不打包和风天气 SDK, 所有请求直接失败,
 * 上层 WeatherRepository 按常规失败路径降级处理。
 * 签名必须与 src/nonfoss 的真实实现保持一致。
 */
class QWeatherDataSource(
    @Suppress("unused") context: Context,
) {

    suspend fun geoCityLookup(lat: Double, lon: Double): Result<CityInfo> =
        Result.failure(UnsupportedOperationException("QWeather SDK not available in foss build"))

    suspend fun geoCityLookupByName(name: String): Result<CityInfo> =
        Result.failure(UnsupportedOperationException("QWeather SDK not available in foss build"))

    suspend fun getWeatherDaily(cityId: String, days: Int): Result<List<DailyWeatherInfo>> =
        Result.failure(UnsupportedOperationException("QWeather SDK not available in foss build"))

    suspend fun getWeatherNow(cityId: String): Result<WeatherInfo> =
        Result.failure(UnsupportedOperationException("QWeather SDK not available in foss build"))

    suspend fun getPlaceWeather(lat: Double, lon: Double): Result<PlaceWeather> =
        Result.failure(UnsupportedOperationException("QWeather SDK not available in foss build"))
}
