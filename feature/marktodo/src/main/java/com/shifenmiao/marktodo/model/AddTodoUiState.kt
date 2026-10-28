package com.shifenmiao.marktodo.model

import androidx.compose.runtime.Immutable

/**
 * 新增/编辑待办页状态。
 *
 * 编辑模式（[editingTaskId] 非空）进入时加载原任务，底部显示「删除 + 保存」，
 * 保存成功后同步日程；删除与日程清理由 Service 统一处理。
 */
@Immutable
data class AddTodoUiState(
    val categories: List<TodoCategory> = emptyList(),
    val selectedCategory: TodoCategory? = null,
    val tags: List<TodoTag> = emptyList(),
    val editingTaskId: String? = null,
    val editingTaskLoaded: Boolean = false,
    val taskTitle: String = "",
    val taskNote: String = "",
    val selectedTagNames: Set<String> = emptySet(),
    val priority: Int = TodoTask.PRIORITY_MEDIUM,
    val dueDateMillis: Long? = null,
    val hasDueTime: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showValidationErrors: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val isCreatingSchedule: Boolean = false,
    val isSaving: Boolean = false,
) {
    val isEditMode: Boolean get() = editingTaskId != null
    val isTitleValid: Boolean get() = taskTitle.isNotBlank()
    val hasTitleError: Boolean get() = showValidationErrors && !isTitleValid
    val noteAtMaxLength: Boolean get() = taskNote.length >= NOTE_MAX_LENGTH

    companion object {
        const val NOTE_MAX_LENGTH = 200
    }
}

sealed interface AddTodoUiEvent {
    data class SelectCategory(val category: TodoCategory?) : AddTodoUiEvent
    data object AddCategoryClicked : AddTodoUiEvent

    data class UpdateTaskTitle(val title: String) : AddTodoUiEvent
    data class UpdateTaskNote(val note: String) : AddTodoUiEvent
    data class ToggleTag(val tagName: String) : AddTodoUiEvent
    data class AddCustomTag(val name: String, val colorArgb: Int?) : AddTodoUiEvent
    data class SelectPriority(val priority: Int) : AddTodoUiEvent
    data class UpdateDueDate(val dateMillis: Long?) : AddTodoUiEvent
    data class UpdateDueTime(val hour: Int, val minute: Int) : AddTodoUiEvent
    data object ClearDueTime : AddTodoUiEvent
    data class ToggleDatePicker(val show: Boolean) : AddTodoUiEvent
    data class ToggleTimePicker(val show: Boolean) : AddTodoUiEvent

    // 日程联动（仅编辑模式可用）
    data object CreateLinkedSchedule : AddTodoUiEvent
    data object OpenScheduleHub : AddTodoUiEvent

    data object SubmitTask : AddTodoUiEvent
    data object DeleteTask : AddTodoUiEvent
    data object ConfirmDeleteTask : AddTodoUiEvent
    data object DismissDeleteConfirm : AddTodoUiEvent
    data object NavigateBack : AddTodoUiEvent
}
