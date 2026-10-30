package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a category (分组) of todo tasks.
 *
 * @property id Unique identifier for the category.
 * @property title Display name of the category.
 * @property icon Visual icon representing the category.
 * @property description Optional description of the category.
 * @property colorArgb Optional custom theme color (ARGB); null = auto theme color by position.
 * @property isPinned Whether the category is pinned to the top of the list.
 * @property tasks List of tasks belonging to this category.
 */
@Immutable
data class TodoCategory(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String? = null,
    val colorArgb: Int? = null,
    val isPinned: Boolean = false,
    val tasks: List<TodoTask> = emptyList()
) {
    /**
     * Returns the count of completed tasks in this category.
     */
    val completedCount: Int
        get() = tasks.count { it.isCompleted }

    /**
     * Returns the total count of tasks in this category.
     */
    val totalCount: Int
        get() = tasks.size

    /**
     * Returns the progress percentage (0-100).
     */
    val progressPercentage: Int
        get() = if (totalCount == 0) 0 else (completedCount * 100) / totalCount

    /**
     * 该分组下任务是否全部完成
     */
    val isAllCompleted: Boolean
        get() = totalCount > 0 && completedCount == totalCount
}
