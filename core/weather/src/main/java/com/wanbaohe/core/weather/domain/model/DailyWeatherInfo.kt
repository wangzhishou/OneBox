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

/** 单日天气预报 */
data class DailyWeatherInfo(
    /** 预报日期,yyyy-MM-dd */
    val fxDate: String = "",
    /** 日出时间,HH:mm */
    val sunrise: String = "",
    /** 日落时间,HH:mm */
    val sunset: String = "",
    /** 最高温度,默认单位:摄氏度 */
    val tempMax: String = "",
    /** 最低温度,默认单位:摄氏度 */
    val tempMin: String = "",
    /** 白天天气状况的文字描述 */
    val textDay: String = "",
    /** 晚间天气状况的文字描述 */
    val textNight: String = "",
    /** 白天风向 */
    val windDirDay: String = "",
    /** 白天风力等级 */
    val windScaleDay: String = "",
    /** 相对湿度,百分比数值 */
    val humidity: String = "",
    /** 预报当天总降水量,默认单位:毫米 */
    val precip: String = "",
)
