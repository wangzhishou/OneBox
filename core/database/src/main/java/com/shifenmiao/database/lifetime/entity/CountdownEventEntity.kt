package com.shifenmiao.database.lifetime.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 倒数日实体。
 *
 * - [targetDate] 存储为 epochDay(Long)，仅在 [isLunarTarget] = false 时使用
 * - [lunarMonth] / [lunarDay] 仅在 [isLunarTarget] = true 时使用，跨年自动滚动到下一年
 * - [isFromHoliday] 标识是否由节日兜底生成；用户可以删除但不能再 "恢复"
 */
@Entity(
    tableName = "countdown_events",
    indices = [Index("targetDate"), Index("isPreset")]
)
data class CountdownEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val iconKey: String = "Event",

    /** epochDay (days since 1970-01-01)；公历倒计时使用 */
    val targetDate: Long? = null,

    val isLunarTarget: Boolean = false,

    val lunarMonth: Int? = null,

    val lunarDay: Int? = null,

    val note: String? = null,

    val color: String? = null,

    val sortOrder: Int = 999,

    val isPreset: Boolean = false,

    val isFromHoliday: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)
