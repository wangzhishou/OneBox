package com.shifenmiao.lifetime.domain

import com.shifenmiao.lifetime.data.CountdownEventRepository
import com.shifenmiao.lifetime.data.holiday.HolidayProvider
import com.shifenmiao.lifetime.data.holiday.PresetHoliday
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 倒数日种子服务：
 * - 首次进入（DB 中没有 preset）时插入 [HolidayProvider] 给出的未来 [withinDays] 天节日
 * - 幂等：依赖 [CountdownEventRepository] 的 `getPresetCountdownsCount()` 判断
 */
@Singleton
class CountdownSeedService @Inject constructor(
    private val repository: CountdownEventRepository,
    private val holidayProvider: HolidayProvider,
) {

    suspend fun seedIfNeeded(
        now: LocalDate = LocalDate.now(),
        withinDays: Long = DEFAULT_LOOKAHEAD_DAYS,
    ) {
        if (repository.getPresetCountdownsCount() > 0) return
        val presets = holidayProvider.upcomingHolidays(now, withinDays)
        if (presets.isEmpty()) return
        repository.insertPresets(presets.map { it.toEntity() })
    }

    companion object {
        const val DEFAULT_LOOKAHEAD_DAYS = 90L
    }
}

private fun PresetHoliday.toEntity() = com.shifenmiao.database.lifetime.entity.CountdownEventEntity(
    name = name,
    iconKey = iconKey,
    targetDate = targetDate?.toEpochDay(),
    isLunarTarget = isLunar,
    lunarMonth = lunarMonth,
    lunarDay = lunarDay,
    note = null,
    color = null,
    sortOrder = sortOrder,
    isPreset = true,
    isFromHoliday = true,
    createdAt = System.currentTimeMillis(),
)
