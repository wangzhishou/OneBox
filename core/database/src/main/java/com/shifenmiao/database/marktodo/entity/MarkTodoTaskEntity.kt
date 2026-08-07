package com.shifenmiao.database.marktodo.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "marktodo_task",
    foreignKeys = [
        ForeignKey(
            entity = MarkTodoCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["start_date"]),
        Index(value = ["updated_at"])
    ]
)
data class MarkTodoTaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,

    @ColumnInfo(name = "category_id") val categoryId: String,

    @ColumnInfo(name = "title") val title: String,

    @ColumnInfo(name = "note") val note: String? = null,

    @ColumnInfo(name = "start_date") val startDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "due_date") val dueDate: Long? = null,

    @ColumnInfo(name = "tags") val tags: List<String> = emptyList(),

    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,

    @ColumnInfo(name = "is_starred") val isStarred: Boolean = false,

    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,

    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
