package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

/**
 * Represents a single todo task item.
 *
 * @property id Unique identifier for the task.
 * @property title Main title/description of the task.
 * @property note Optional additional notes or details about the task.
 * @property startDate Task creation/start time in milliseconds (Unix timestamp).
 * @property dueDate Optional due date or deadline time in milliseconds (Unix timestamp).
 * @property tags Optional list of tag names for categorizing the task.
 * @property isCompleted Whether the task has been marked as complete.
 * @property isStarred Whether the task has been marked as starred/important.
 */
@Immutable
data class TodoTask(
    val id: String,
    val title: String,
    val note: String? = null,
    val startDate: Long = System.currentTimeMillis(), // 开始时间，默认为创建时间
    val dueDate: Long? = null, // 截止日期
    val tags: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val isStarred: Boolean = false,
    val sortOrder: Int = 0
) {
    /**
     * 检查任务是否已过期（截止日期已过且未完成）
     */
    val isOverdue: Boolean
        get() = dueDate?.let { it < System.currentTimeMillis() && !isCompleted } ?: false

    /**
     * 检查任务是否即将到期（24小时内到期且未完成）
     */
    val isDueSoon: Boolean
        get() = dueDate?.let {
            val now = System.currentTimeMillis()
            val oneDayMillis = 24 * 60 * 60 * 1000
            it in now..(now + oneDayMillis) && !isCompleted
        } ?: false

    /**
     * 向后兼容：保留date属性作为dueDate的别名
     */
    @Deprecated("Use dueDate instead", ReplaceWith("dueDate"))
    val date: Long? get() = dueDate
}
