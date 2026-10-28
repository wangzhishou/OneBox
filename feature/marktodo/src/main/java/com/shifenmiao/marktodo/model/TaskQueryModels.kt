package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

@Immutable
enum class HomeView {
    THEMES, INBOX
}

@Immutable
enum class TaskCompletionFilter {
    ALL, ACTIVE, COMPLETED
}

@Immutable
enum class TaskDateScope {
    ALL, TODAY, OVERDUE, UPCOMING, NO_DATE, RANGE
}

@Immutable
data class TaskFilter(
    val completion: TaskCompletionFilter = TaskCompletionFilter.ACTIVE,
    val categoryIds: Set<String> = emptySet(),
    val includeInbox: Boolean = false,
    val tagNames: Set<String> = emptySet(),
    val priorities: Set<Int> = emptySet(),
    val dateScope: TaskDateScope = TaskDateScope.ALL,
    val dueStartMillis: Long? = null,
    val dueEndMillis: Long? = null,
    val starredOnly: Boolean = false,
) {
    val hasCategoryFilter: Boolean get() = categoryIds.isNotEmpty() || includeInbox
    val hasDueRange: Boolean get() = dueStartMillis != null || dueEndMillis != null
    val hasDateFilter: Boolean get() = hasSmartDateFilter || hasDueRange
    val hasSmartDateFilter: Boolean get() = dateScope != TaskDateScope.ALL && dateScope != TaskDateScope.RANGE
}

@Immutable
enum class TaskSortMode {
    MANUAL, DUE_DATE, PRIORITY, CREATED_AT
}

@Immutable
data class TaskSortConfig(
    val mode: TaskSortMode = TaskSortMode.MANUAL,
    val ascending: Boolean = true,
)

@Immutable
enum class TodayGroup {
    OVERDUE, TODAY, UPCOMING, NO_DATE
}

@Immutable
data class TaskWithCategory(
    val task: TodoTask,
    val category: TodoCategory? = null,
    val categoryOrder: Int = 0,
)

@Immutable
data class TodayTaskGroup(
    val group: TodayGroup,
    val tasks: List<TaskWithCategory>,
)
