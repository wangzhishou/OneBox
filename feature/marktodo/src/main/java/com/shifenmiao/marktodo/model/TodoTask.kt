package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable
import com.shifenmiao.marktodo.domain.TodoTaskTimeRules
import java.time.LocalDate

@Immutable
data class TodoTask(
    val id: String,
    val title: String,
    val categoryId: String? = null,
    val note: String? = null,
    val startDate: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val hasDueTime: Boolean = false,
    val tags: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val isStarred: Boolean = false,
    val priority: Int = PRIORITY_MEDIUM,
    val completedAt: Long? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = startDate,
    val updatedAt: Long = createdAt,
) {
    val isOverdue: Boolean
        get() = TodoTaskTimeRules.isOverdue(
            dueDateMillis = dueDate,
            hasDueTime = hasDueTime,
            isCompleted = isCompleted,
            now = System.currentTimeMillis(),
            today = LocalDate.now(),
        )

    val isDueSoon: Boolean
        get() = TodoTaskTimeRules.isDueSoon(
            dueDateMillis = dueDate,
            hasDueTime = hasDueTime,
            isCompleted = isCompleted,
            now = System.currentTimeMillis(),
            today = LocalDate.now(),
        )

    @Deprecated("Use dueDate instead", ReplaceWith("dueDate"))
    val date: Long? get() = dueDate

    companion object {
        const val PRIORITY_LOW = 0
        const val PRIORITY_MEDIUM = 1
        const val PRIORITY_HIGH = 2
    }
}
