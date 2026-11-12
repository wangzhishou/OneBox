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

@Serializable
data class CityInfo(
    // 默认值兜底 DataStore 里 Gson 时代的缓存(缺字段时 Gson 给 null/0.0)
    /** 地区/城市ID */
    val id: String = "",
    /** 地区/城市名称 */
    val name: String = "",
    /** 地区/城市纬度 */
    val lat: Double = 0.0,
    /** 地区/城市经度 */
    val lon: Double = 0.0,
    /** 地区/城市的上级行政区划名称 */
    val adm2: String = "",
    /** 地区/城市所属一级行政区域 */
    val adm1: String = "",
    /** 地区/城市所属国家名称 */
    val country: String = "",
    /** 地区/城市所在时区 */
    val tz: String = "",
    /** 地区/城市目前与UTC时间偏移的小时数 */
    val utcOffset: String = "",
    /** 地区/城市是否当前处于夏令时。1 表示当前处于夏令时，0 表示当前不是夏令时 */
    val isDst: String = "",
    /** 地区/城市的属性 */
    val type: String = "",
    /** 地区评分 */
    val rank: String = "",
    /** 该地区的天气预报网页链接，便于嵌入你的网站或应用 */
    val fxLink: String = ""
)
