package com.shifenmiao.database.marktodo.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marktodo_category")
data class MarkTodoCategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    /**
     * Display title.
     * Note: stored as resolved string to keep the table stable across app locales.
     */
    @ColumnInfo(name = "title") val title: String,
    /**
     * Stable icon key string that UI can map to an ImageVector.
     */
    @ColumnInfo(name = "icon_key") val iconKey: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
