package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

/**
 * 待办详情页的 UI 状态
 */
@Immutable
data class TodoDetailUiState(
    val task: TodoTask? = null,
    val categoryTitle: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreatingSchedule: Boolean = false,
    val isEditing: Boolean = false,
    // 编辑表单状态
    val editTitle: String = "",
    val editNote: String = "",
    val editTagsText: String = "",
    val editDueDateMillis: Long? = null,
    val showDatePicker: Boolean = false,
    val showValidationErrors: Boolean = false,
) {
    val isTitleValid: Boolean get() = editTitle.isNotBlank()
    val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid
    val parsedTags: List<String> get() = editTagsText
        .split(",", "，")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}

sealed interface TodoDetailNotice {
    data object MissingDueDate : TodoDetailNotice
    data object ScheduleCreated : TodoDetailNotice
    data object ScheduleCreateFailed : TodoDetailNotice
}

/**
 * 待办详情页的用户事件
 */
sealed interface TodoDetailUiEvent {
    // 任务操作
    data object ToggleComplete : TodoDetailUiEvent
    data object ToggleStar : TodoDetailUiEvent
    data object DeleteTask : TodoDetailUiEvent
    data object ConfirmDelete : TodoDetailUiEvent
    data object OpenScheduleHub : TodoDetailUiEvent
    data object CreateLinkedSchedule : TodoDetailUiEvent

    // 编辑模式
    data object EnterEditMode : TodoDetailUiEvent
    data object CancelEdit : TodoDetailUiEvent
    data object SaveEdit : TodoDetailUiEvent

    // 编辑表单更新
    data class UpdateEditTitle(val title: String) : TodoDetailUiEvent
    data class UpdateEditNote(val note: String) : TodoDetailUiEvent
    data class UpdateEditTags(val tags: String) : TodoDetailUiEvent
    data class UpdateEditDueDate(val dateMillis: Long?) : TodoDetailUiEvent
    data class ToggleDatePicker(val show: Boolean) : TodoDetailUiEvent

    // 导航
    data object NavigateBack : TodoDetailUiEvent
}
