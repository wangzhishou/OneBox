package com.shifenmiao.database.habit.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit"
)
data class HabitEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "icon_key") val iconKey: String = "waterdrop",
    /** null = 跟随主题自动配色 */
    @ColumnInfo(name = "color_argb") val colorArgb: Long? = null,
    /** DAILY / WEEKLY_TIMES / MONTHLY_TIMES / CUSTOM_WEEKDAYS */
    @ColumnInfo(name = "repeat_type") val repeatType: String = "DAILY",
    @ColumnInfo(name = "repeat_target") val repeatTarget: Int = 1,
    /** CUSTOM_WEEKDAYS 时的星期位掩码,bit0=周一 … bit6=周日 */
    @ColumnInfo(name = "weekdays_mask") val weekdaysMask: Int = 0,
    /** 提醒时间(一天内分钟数),null = 不提醒 */
    @ColumnInfo(name = "remind_minutes") val remindMinutes: Int? = null,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "stats_enabled") val statsEnabled: Boolean = true,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
