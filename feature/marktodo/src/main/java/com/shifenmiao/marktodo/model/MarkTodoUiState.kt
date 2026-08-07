package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

/**
 * Represents the UI state for the MarkTodo screen.
 * Using immutable data classes ensures predictable state updates and better performance.
 */
@Immutable
data class MarkTodoUiState(
    val categories: List<TodoCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
) {
    val hasCategories: Boolean get() = categories.isNotEmpty()
}

/**
 * Represents dialog states using sealed interface for type safety.
 */
sealed interface DialogState {
    data object Dismissed : DialogState

    data class AddCategory(
        val editingCategory: TodoCategory? = null, // 如果不为null，表示编辑模式
        val title: String = "",
        val iconKey: String = "inbox",
        val showValidationErrors: Boolean = false
    ) : DialogState {
        val isEditMode: Boolean get() = editingCategory != null
        val isTitleValid: Boolean get() = title.isNotBlank()
        val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid
    }

    data class AddTask(
        val category: TodoCategory,
        val editingTask: TodoTask? = null, // 如果不为null，表示编辑模式
        val title: String = "",
        val note: String = "",
        val tagsText: String = "",
        val dueDateMillis: Long? = null,
        val showDatePicker: Boolean = false,
        val showValidationErrors: Boolean = false
    ) : DialogState {
        val isEditMode: Boolean get() = editingTask != null
        val isTitleValid: Boolean get() = title.isNotBlank()
        val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid

        val parsedTags: List<String> get() = tagsText
            .split(",", "，")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    data class DeleteCategoryConfirm(
        val category: TodoCategory
    ) : DialogState
}

/**
 * Represents user actions/events in the MarkTodo screen.
 */
sealed interface MarkTodoUiEvent {
    // Category actions
    data class CategoryClicked(val category: TodoCategory) : MarkTodoUiEvent
    data object AddCategoryClicked : MarkTodoUiEvent
    data class EditCategoryClicked(val category: TodoCategory) : MarkTodoUiEvent
    data class DeleteCategory(val category: TodoCategory) : MarkTodoUiEvent
    data class ConfirmDeleteCategory(val category: TodoCategory) : MarkTodoUiEvent

    // Task actions
    data class TaskClicked(val task: TodoTask) : MarkTodoUiEvent
    data class AddTaskClicked(val category: TodoCategory) : MarkTodoUiEvent
    data class ToggleTaskComplete(val task: TodoTask) : MarkTodoUiEvent
    data class ToggleTaskStar(val task: TodoTask) : MarkTodoUiEvent

    // Edit mode
    data object ToggleEditMode : MarkTodoUiEvent

    // Category reordering
    data class ReorderCategories(val fromIndex: Int, val toIndex: Int) : MarkTodoUiEvent

    // Dialog actions
    data object DismissDialog : MarkTodoUiEvent

    // Navigation
    data object NavigateBack : MarkTodoUiEvent
}

/**
 * Represents the result of an operation.
 */
sealed interface OperationResult {
    data object Success : OperationResult
    data class Error(val message: String) : OperationResult
    data object Loading : OperationResult
}
