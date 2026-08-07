package com.shifenmiao.lifetime.data

import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.lifetime.entity.FrequencyEventEntity
import com.shifenmiao.lifetime.domain.model.FrequencyEvent
import com.shifenmiao.lifetime.domain.model.FrequencyType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 频率事件仓库
 */
@Singleton
class FrequencyEventRepository @Inject constructor(
    private val database: FeatureDatabase
) {
    private val dao = database.frequencyEventDao()

    /**
     * 获取所有启用的事件（Flow）
     */
    val enabledEventsFlow: Flow<List<FrequencyEvent>> = dao.getAllEnabledEvents()
        .map { entities -> entities.map { it.toDomainModel() } }

    /**
     * 获取所有事件
     */
    val allEventsFlow: Flow<List<FrequencyEvent>> = dao.getAllEvents()
        .map { entities -> entities.map { it.toDomainModel() } }

    /**
     * 初始化预设事件（仅在首次运行时）
     * 包含节日和日常活动
     * isRecommended = true 的事件默认启用，其他需要用户手动添加
     */
    suspend fun initializePresetEvents() {
        if (dao.getPresetEventsCount() == 0) {
            val presetEvents = listOf(
                // 传统节日（推荐，默认启用）
                FrequencyEventEntity(
                    name = "春节",
                    iconKey = "Celebration",
                    frequencyType = FrequencyType.YEARLY.name,
                    timesPerPeriod = 1,
                    unit = "次",
                    sortOrder = 1,
                    isPreset = true,
                    isRecommended = true,
                    isEnabled = true
                ),
                FrequencyEventEntity(
                    name = "中秋节",
                    iconKey = "Nightlight",
                    frequencyType = FrequencyType.YEARLY.name,
                    timesPerPeriod = 1,
                    unit = "次",
                    sortOrder = 2,
                    isPreset = true,
                    isRecommended = true,
                    isEnabled = true
                ),
                FrequencyEventEntity(
                    name = "圣诞节",
                    iconKey = "Star",
                    frequencyType = FrequencyType.YEARLY.name,
                    timesPerPeriod = 1,
                    unit = "次",
                    sortOrder = 3,
                    isPreset = true,
                    isRecommended = false,
                    isEnabled = false
                ),
                // 日常活动（推荐的默认启用）
                FrequencyEventEntity(
                    name = "吃饭",
                    iconKey = "Restaurant",
                    frequencyType = FrequencyType.DAILY.name,
                    timesPerPeriod = 3,
                    unit = "顿",
                    sortOrder = 10,
                    isPreset = true,
                    isRecommended = true,
                    isEnabled = true
                ),
                FrequencyEventEntity(
                    name = "睡觉",
                    iconKey = "Bedtime",
                    frequencyType = FrequencyType.DAILY.name,
                    timesPerPeriod = 1,
                    unit = "次",
                    sortOrder = 11,
                    isPreset = true,
                    isRecommended = true,
                    isEnabled = true
                ),
                FrequencyEventEntity(
                    name = "写日记",
                    iconKey = "Edit",
                    frequencyType = FrequencyType.DAILY.name,
                    timesPerPeriod = 1,
                    unit = "篇",
                    sortOrder = 12,
                    isPreset = true,
                    isRecommended = false,
                    isEnabled = false
                ),
                FrequencyEventEntity(
                    name = "运动",
                    iconKey = "FitnessCenter",
                    frequencyType = FrequencyType.DAILY.name,
                    timesPerPeriod = 2,
                    unit = "公里",
                    sortOrder = 13,
                    isPreset = true,
                    isRecommended = false,
                    isEnabled = false
                ),
                FrequencyEventEntity(
                    name = "看书",
                    iconKey = "AutoStories",
                    frequencyType = FrequencyType.WEEKLY.name,
                    timesPerPeriod = 3,
                    unit = "本",
                    sortOrder = 20,
                    isPreset = true,
                    isRecommended = false,
                    isEnabled = false
                ),
                FrequencyEventEntity(
                    name = "看电影",
                    iconKey = "Movie",
                    frequencyType = FrequencyType.MONTHLY.name,
                    timesPerPeriod = 2,
                    unit = "部",
                    sortOrder = 21,
                    isPreset = true,
                    isRecommended = false,
                    isEnabled = false
                ),
                FrequencyEventEntity(
                    name = "旅行",
                    iconKey = "Flight",
                    frequencyType = FrequencyType.YEARLY.name,
                    timesPerPeriod = 2,
                    unit = "次",
                    sortOrder = 30,
                    isPreset = true,
                    isRecommended = false,
                    isEnabled = false
                )
            )
            dao.insertEvents(presetEvents)
        }
    }

    /**
     * 添加事件
     */
    suspend fun addEvent(event: FrequencyEvent): Long {
        return dao.insertEvent(event.toEntity())
    }

    /**
     * 更新事件
     */
    suspend fun updateEvent(event: FrequencyEvent) {
        dao.updateEvent(event.toEntity())
    }

    /**
     * 删除事件
     */
    suspend fun deleteEvent(event: FrequencyEvent) {
        dao.deleteEvent(event.toEntity())
    }

    /**
     * 切换启用状态
     */
    suspend fun toggleEnabled(id: Long, isEnabled: Boolean) {
        dao.toggleEnabled(id, isEnabled)
    }
}

/**
 * Entity → Domain Model
 */
private fun FrequencyEventEntity.toDomainModel() = FrequencyEvent(
    id = id,
    name = name,
    iconKey = iconKey,
    frequencyType = FrequencyType.valueOf(frequencyType),
    timesPerPeriod = timesPerPeriod,
    unit = unit,
    specificDate = specificDate,
    sortOrder = sortOrder,
    color = color,
    isEnabled = isEnabled,
    isPreset = isPreset,
    isRecommended = isRecommended,
    createdAt = createdAt
)

private fun FrequencyEvent.toEntity() = FrequencyEventEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    frequencyType = frequencyType.name,
    timesPerPeriod = timesPerPeriod,
    unit = unit,
    specificDate = specificDate,
    sortOrder = sortOrder,
    color = color,
    isEnabled = isEnabled,
    isPreset = isPreset,
    isRecommended = isRecommended,
    createdAt = createdAt
)

