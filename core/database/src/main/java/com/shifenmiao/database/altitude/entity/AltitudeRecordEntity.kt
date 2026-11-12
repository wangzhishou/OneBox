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

package com.shifenmiao.database.altitude.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 海拔记录数据库实体
 * 每次用户手动保存时写入一条记录
 */
@Entity(tableName = "altitude_record")
data class AltitudeRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 海拔高度（米） */
    val altitudeMeters: Float,

    /** 数据来源：BAROMETER 或 GPS */
    val source: String,

    /** GPS 精度（米，气压计时为 0） */
    val accuracyMeters: Float = 0f,

    /** 纬度（气压计时为 null） */
    val latitude: Double? = null,

    /** 经度（气压计时为 null） */
    val longitude: Double? = null,

    /** 历史标题（优先城市名） */
    val title: String = "",

    /** 城市信息快照 */
    val cityName: String = "",
    val cityAdm2: String = "",
    val cityAdm1: String = "",
    val cityCountry: String = "",
    val cityTz: String = "",
    val cityUtcOffset: String = "",

    /** 天气快照（保存时的预报/实况） */
    val weatherForecast: String = "",
    val weatherText: String = "",
    val weatherTemp: String = "",
    val weatherFeelsLike: String = "",
    val weatherWindDir: String = "",
    val weatherWindScale: String = "",
    val weatherWindSpeed: String = "",
    val weatherHumidity: String = "",
    val weatherPressure: String = "",
    val weatherObsTime: String = "",
    val weatherUpdateTime: String = "",
    val weatherDew: String = "",
    val weatherCloud: String = "",
    val weatherVis: String = "",
    val weatherPrecip: String = "",
    val weatherIcon: String = "",
    val weatherUv: String = "",
    val weatherAqi: String = "",
    val weatherAqiCategory: String = "",

    /** 用户备注 */
    val note: String = "",

    /** 记录时间戳（毫秒） */
    val recordedAt: Long = System.currentTimeMillis()
)

