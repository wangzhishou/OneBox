package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

@Immutable
data class CategoryDetailUiState(
    val category: TodoCategory? = null,
    val tags: List<TodoTag> = emptyList(),
    val filterMode: TaskFilterMode = TaskFilterMode.ALL,
    val sort: TaskSortConfig = TaskSortConfig(),
    val pendingCompletionIds: Set<String> = emptySet(),
    val isSortSheetVisible: Boolean = false,
    val isDeletingCategory: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val totalCount: Int get() = category?.totalCount ?: 0
    val completedCount: Int get() = category?.completedCount ?: 0
}

enum class TaskFilterMode {
    ALL, ACTIVE, COMPLETED, STARRED
}

sealed interface CategoryDetailUiEvent {
    data class TaskClicked(val task: TodoTask) : CategoryDetailUiEvent
    data object AddTaskClicked : CategoryDetailUiEvent
    data class ToggleTaskComplete(val task: TodoTask) : CategoryDetailUiEvent
    data class ToggleTaskStar(val task: TodoTask) : CategoryDetailUiEvent
    data class DeleteTask(val task: TodoTask) : CategoryDetailUiEvent

    data class ChangeFilter(val filterMode: TaskFilterMode) : CategoryDetailUiEvent
    data object ShowSortSheet : CategoryDetailUiEvent
    data object DismissSortSheet : CategoryDetailUiEvent
    data class ChangeSort(val sort: TaskSortConfig) : CategoryDetailUiEvent

    data object EditCategoryClicked : CategoryDetailUiEvent
    data object DeleteCategoryClicked : CategoryDetailUiEvent
    data object DismissDeleteCategory : CategoryDetailUiEvent
    data object ConfirmDeleteCategory : CategoryDetailUiEvent

    data object NavigateBack : CategoryDetailUiEvent
}
