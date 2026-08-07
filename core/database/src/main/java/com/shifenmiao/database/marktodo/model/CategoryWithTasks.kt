package com.shifenmiao.database.marktodo.model

import androidx.room.Embedded
import androidx.room.Relation
import com.shifenmiao.database.marktodo.entity.MarkTodoCategoryEntity
import com.shifenmiao.database.marktodo.entity.MarkTodoTaskEntity

/**
 * Convenience relation model for loading a category and its tasks.
 */
data class CategoryWithTasks(
    @Embedded val category: MarkTodoCategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "category_id"
    )
    val tasks: List<MarkTodoTaskEntity>
)

