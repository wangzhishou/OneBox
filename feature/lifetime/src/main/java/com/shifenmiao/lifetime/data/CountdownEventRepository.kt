package com.shifenmiao.lifetime.data

import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.lifetime.dao.CountdownEventDao
import com.shifenmiao.database.lifetime.entity.CountdownEventEntity
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 倒数日仓库
 */
@Singleton
class CountdownEventRepository @Inject constructor(
    database: FeatureDatabase
) {
    private val dao: CountdownEventDao = database.countdownEventDao()

    val allCountdownsFlow: Flow<List<CountdownEvent>> = dao.getAllCountdowns()
        .map { entities -> entities.map { it.toDomainModel() } }

    suspend fun addCountdown(event: CountdownEvent): Long {
        return dao.insertCountdown(event.toEntity())
    }

    suspend fun updateCountdown(event: CountdownEvent) {
        dao.updateCountdown(event.toEntity())
    }

    suspend fun deleteCountdown(event: CountdownEvent) {
        dao.deleteCountdown(event.toEntity())
    }

    suspend fun deleteCountdownById(id: Long) {
        dao.deleteCountdownById(id)
    }

    suspend fun getCountdownById(id: Long): CountdownEvent? {
        return dao.getCountdownById(id)?.toDomainModel()
    }

    suspend fun getPresetCountdownsCount(): Int = dao.getPresetCountdownsCount()

    /** 批量插入预置节日（用 IGNORE 策略避免重复） */
    suspend fun insertPresets(presets: List<CountdownEventEntity>) {
        dao.insertCountdowns(presets)
    }
}

private fun CountdownEventEntity.toDomainModel() = CountdownEvent(
    id = id,
    name = name,
    iconKey = iconKey,
    targetDate = targetDate?.let { LocalDate.ofEpochDay(it) },
    isLunarTarget = isLunarTarget,
    lunarMonth = lunarMonth,
    lunarDay = lunarDay,
    note = note,
    color = color,
    sortOrder = sortOrder,
    isPreset = isPreset,
    isFromHoliday = isFromHoliday,
    createdAt = createdAt,
)

private fun CountdownEvent.toEntity() = CountdownEventEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    targetDate = targetDate?.toEpochDay(),
    isLunarTarget = isLunarTarget,
    lunarMonth = lunarMonth,
    lunarDay = lunarDay,
    note = note,
    color = color,
    sortOrder = sortOrder,
    isPreset = isPreset,
    isFromHoliday = isFromHoliday,
    createdAt = createdAt,
)
