package com.shifenmiao.database.blessing.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blessing_record",
    indices = [
        Index(value = ["date"]),
        Index(value = ["date", "type"], unique = true)
    ]
)
data class BlessingRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "count") val count: Int = 0,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
