package com.shifenmiao.database.habit.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_check_in",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habit_id", "date_epoch_day"], unique = true),
        Index(value = ["date_epoch_day"])
    ]
)
data class HabitCheckInEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "habit_id") val habitId: String,
    /** 打卡日期,LocalDate.toEpochDay() */
    @ColumnInfo(name = "date_epoch_day") val dateEpochDay: Long,
    @ColumnInfo(name = "checked_at") val checkedAt: Long = System.currentTimeMillis(),
)
