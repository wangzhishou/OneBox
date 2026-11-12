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

package com.wanbaohe.core.weather.domain.repository

import com.wanbaohe.core.weather.domain.model.DailyWeatherInfo
import com.wanbaohe.core.weather.domain.model.LatestPlace
import com.wanbaohe.core.weather.domain.model.PlaceWeather
import com.wanbaohe.core.weather.domain.model.WeatherInfo
import com.wanbaohe.core.weather.domain.model.CityInfo

interface WeatherRepository {
    suspend fun getWeatherAtLocation(lat: Double, lon: Double): Result<WeatherInfo>
    suspend fun getCityAtLocation(lat: Double, lon: Double): Result<CityInfo>

    /** 按城市名(中文/英文/拼音或 LocationID)反查城市信息 */
    suspend fun getCityByName(name: String): Result<CityInfo>

    /**
     * 用出口 IP 判断当前城市，不需要定位权限。
     * 国内解析城市名，海外解析经纬度，再交给和风反查。
     */
    suspend fun getCityFromNetwork(): Result<CityInfo>

    /** 指定城市的逐日天气预报,days 取 1-7 */
    suspend fun getDailyForecast(cityId: String, days: Int): Result<List<DailyWeatherInfo>>

    /**
     * 经纬度聚合天气：实况、24 小时、7 天、空气、预警、生活指数、分钟降水概况。
     * 30 分钟内直接用缓存；更久或请求失败时，仍返回该地点上次成功的结果。
     */
    suspend fun getPlaceWeather(lat: Double, lon: Double): Result<PlaceWeather>

    /** 最近一次成功的地点天气，不限是否过期。没有成功记录时返回 null。 */
    suspend fun latestPlace(): LatestPlace?

    /**
     * 把城市、天气和更新时间写成一份快照。
     * 只在确认这份天气属于该城市时调用。写入失败不影响已经展示的结果。
     */
    suspend fun rememberLatestPlace(city: CityInfo, place: PlaceWeather, updatedAt: Long)
}

