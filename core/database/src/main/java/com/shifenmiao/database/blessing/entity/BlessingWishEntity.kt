package com.shifenmiao.database.blessing.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "blessing_wish",
    primaryKeys = ["date", "type"],
    indices = [Index(value = ["date"])],
)
data class BlessingWishEntity(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
