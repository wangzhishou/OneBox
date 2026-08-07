package com.wanbaohe.altitude.data

import com.shifenmiao.database.altitude.dao.AltitudeRecordDao
import com.shifenmiao.database.altitude.entity.AltitudeRecordEntity
import com.wanbaohe.altitude.domain.AltitudeRecord
import com.wanbaohe.altitude.domain.AltitudeSource
import com.wanbaohe.altitude.domain.CitySnapshot
import com.wanbaohe.altitude.domain.WeatherSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 海拔记录仓库
 * 负责 DB 实体与领域模型之间的映射
 */
@Singleton
class AltitudeRepository @Inject constructor(
    private val dao: AltitudeRecordDao
) {
    /** 获取所有记录（实时 Flow，时间倒序） */
    fun getAll(): Flow<List<AltitudeRecord>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    /** 获取最近 N 条记录（用于趋势图，时间倒序） */
    fun getRecent(limit: Int = 50): Flow<List<AltitudeRecord>> =
        dao.getRecent(limit).map { list -> list.map { it.toDomain() } }

    /** 保存一条记录，返回 rowId */
    suspend fun save(record: AltitudeRecord): Long =
        dao.insert(record.toEntity())

    /** 删除单条记录 */
    suspend fun deleteById(id: Long) = dao.deleteById(id)

    /** 清空全部历史 */
    suspend fun clearAll() = dao.clearAll()
}

// ─── 私有映射扩展 ────────────────────────────────────────────────────────────

private fun AltitudeRecordEntity.toDomain() = AltitudeRecord(
    id = id,
    altitudeMeters = altitudeMeters,
    source = runCatching { AltitudeSource.valueOf(source) }.getOrDefault(AltitudeSource.BAROMETER),
    accuracyMeters = accuracyMeters,
    latitude = latitude,
    longitude = longitude,
    citySnapshot = CitySnapshot(
        name = cityName,
        adm2 = cityAdm2,
        adm1 = cityAdm1,
        country = cityCountry,
        tz = cityTz,
        utcOffset = cityUtcOffset
    ).takeIf { it.title.isNotBlank() },
    weatherSnapshot = WeatherSnapshot(
        forecast = weatherForecast,
        text = weatherText,
        temp = weatherTemp,
        feelsLike = weatherFeelsLike,
        windDir = weatherWindDir,
        windScale = weatherWindScale,
        windSpeed = weatherWindSpeed,
        humidity = weatherHumidity,
        pressure = weatherPressure,
        obsTime = weatherObsTime,
        updateTime = weatherUpdateTime,
        dew = weatherDew,
        cloud = weatherCloud,
        vis = weatherVis,
        precip = weatherPrecip
    ).takeIf { it.brief.isNotBlank() || it.obsTime.isNotBlank() },
    note = note,
    recordedAt = recordedAt
)

private fun AltitudeRecord.toEntity() = AltitudeRecordEntity(
    id = id,
    altitudeMeters = altitudeMeters,
    source = source.tag,
    accuracyMeters = accuracyMeters,
    latitude = latitude,
    longitude = longitude,
    title = resolvedTitle,
    cityName = citySnapshot?.name.orEmpty(),
    cityAdm2 = citySnapshot?.adm2.orEmpty(),
    cityAdm1 = citySnapshot?.adm1.orEmpty(),
    cityCountry = citySnapshot?.country.orEmpty(),
    cityTz = citySnapshot?.tz.orEmpty(),
    cityUtcOffset = citySnapshot?.utcOffset.orEmpty(),
    weatherText = weatherSnapshot?.text.orEmpty(),
    weatherForecast = weatherSnapshot?.forecast.orEmpty(),
    weatherTemp = weatherSnapshot?.temp.orEmpty(),
    weatherFeelsLike = weatherSnapshot?.feelsLike.orEmpty(),
    weatherWindDir = weatherSnapshot?.windDir.orEmpty(),
    weatherWindScale = weatherSnapshot?.windScale.orEmpty(),
    weatherWindSpeed = weatherSnapshot?.windSpeed.orEmpty(),
    weatherHumidity = weatherSnapshot?.humidity.orEmpty(),
    weatherPressure = weatherSnapshot?.pressure.orEmpty(),
    weatherObsTime = weatherSnapshot?.obsTime.orEmpty(),
    weatherUpdateTime = weatherSnapshot?.updateTime.orEmpty(),
    weatherDew = weatherSnapshot?.dew.orEmpty(),
    weatherCloud = weatherSnapshot?.cloud.orEmpty(),
    weatherVis = weatherSnapshot?.vis.orEmpty(),
    weatherPrecip = weatherSnapshot?.precip.orEmpty(),
    note = note,
    recordedAt = recordedAt
)

