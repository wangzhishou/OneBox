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

    /** 用户备注 */
    val note: String = "",

    /** 记录时间戳（毫秒） */
    val recordedAt: Long = System.currentTimeMillis()
)

