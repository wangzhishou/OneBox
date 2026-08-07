package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

@Immutable
data class AddTodoUiState(
    val categories: List<TodoCategory> = emptyList(),
    val selectedCategory: TodoCategory? = null,
    val taskTitle: String = "",
    val taskNote: String = "",
    val taskTagsText: String = "",
    val dueDateMillis: Long? = null,
    val showDatePicker: Boolean = false,
    val showValidationErrors: Boolean = false
) {
    val isTitleValid: Boolean get() = taskTitle.isNotBlank()
    val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid
    
    val parsedTags: List<String> get() = taskTagsText
        .split(",", "，")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

sealed interface AddTodoUiEvent {
    data class SelectCategory(val category: TodoCategory) : AddTodoUiEvent
    data object AddCategoryClicked : AddTodoUiEvent

    data class UpdateTaskTitle(val title: String) : AddTodoUiEvent
    data class UpdateTaskNote(val note: String) : AddTodoUiEvent
    data class UpdateTaskTags(val tags: String) : AddTodoUiEvent
    data class UpdateDueDate(val dateMillis: Long?) : AddTodoUiEvent
    data class ToggleDatePicker(val show: Boolean) : AddTodoUiEvent

    data object SubmitTask : AddTodoUiEvent
    data object NavigateBack : AddTodoUiEvent
}