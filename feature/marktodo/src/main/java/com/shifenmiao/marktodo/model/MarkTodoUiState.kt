package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

@Immutable
data class MarkTodoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val homeView: HomeView = HomeView.THEMES,
    val searchQuery: String = "",
    val filter: TaskFilter = TaskFilter(),
    val sort: TaskSortConfig = TaskSortConfig(),
    val pendingCompletionIds: Set<String> = emptySet(),
    val isFilterSortSheetVisible: Boolean = false,
    val isDeletingCategory: Boolean = false,
    val isReorderMode: Boolean = false,
    val isGridView: Boolean = true,
)

sealed interface DialogState {
    data object Dismissed : DialogState

    data class DeleteCategoryConfirm(
        val category: TodoCategory,
    ) : DialogState
}

sealed interface MarkTodoUiEvent {
    data class CategoryClicked(val category: TodoCategory) : MarkTodoUiEvent
    data object AddCategoryClicked : MarkTodoUiEvent
    data class EditCategoryClicked(val category: TodoCategory) : MarkTodoUiEvent
    data class DeleteCategory(val category: TodoCategory) : MarkTodoUiEvent
    data class ConfirmDeleteCategory(val category: TodoCategory) : MarkTodoUiEvent

    data class TaskClicked(val task: TodoTask) : MarkTodoUiEvent
    data class AddTaskClicked(val category: TodoCategory? = null) : MarkTodoUiEvent
    data class ToggleTaskComplete(val task: TodoTask) : MarkTodoUiEvent
    data class ToggleTaskStar(val task: TodoTask) : MarkTodoUiEvent
    data class DeleteTask(val task: TodoTask) : MarkTodoUiEvent

    data class SelectHomeView(val view: HomeView) : MarkTodoUiEvent
    data class SearchQueryChanged(val query: String) : MarkTodoUiEvent
    data class TagOnTaskClicked(val tagName: String) : MarkTodoUiEvent
    data class SetFilter(val filter: TaskFilter) : MarkTodoUiEvent
    data class SetSort(val sort: TaskSortConfig) : MarkTodoUiEvent
    data object ShowFilterSortSheet : MarkTodoUiEvent
    data object DismissFilterSortSheet : MarkTodoUiEvent
    data class ApplyFilterSort(val filter: TaskFilter, val sort: TaskSortConfig) : MarkTodoUiEvent
    data object ResetQuery : MarkTodoUiEvent
    data class SetGridView(val isGrid: Boolean) : MarkTodoUiEvent

    data class SetReorderMode(val enabled: Boolean) : MarkTodoUiEvent
    data class ReorderCategories(
        val fromIndex: Int,
        val toIndex: Int,
        val visibleCategoryIds: List<String>,
    ) : MarkTodoUiEvent

    data object DismissDialog : MarkTodoUiEvent
    data object NavigateBack : MarkTodoUiEvent
}
